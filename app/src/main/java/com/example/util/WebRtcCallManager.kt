package com.example.util

import android.content.Context
import android.media.AudioManager
import android.util.Log
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "WebRtcCallManager"

class WebRtcCallManager(private val appContext: Context) {

    private var eglBase: EglBase? = null
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private var localRenderer: SurfaceViewRenderer? = null
    private var remoteRenderer: SurfaceViewRenderer? = null

    val eglContext: EglBase.Context?
        get() = eglBase?.eglBaseContext

    fun ensureInitialized(): Boolean {
        if (factory != null) return true
        return try {
            val egl = EglBase.create()
            eglBase = egl
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                    .createInitializationOptions()
            )
            factory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(DefaultVideoEncoderFactory(egl.eglBaseContext, true, true))
                .setVideoDecoderFactory(DefaultVideoDecoderFactory(egl.eglBaseContext))
                .createPeerConnectionFactory()
            true
        } catch (t: Throwable) {
            Log.w(TAG, "WebRTC native initialization unavailable on this environment: ${t.message}")
            false
        }
    }

    fun startPeerSession(
        isVideoCall: Boolean,
        onIceCandidateGenerated: (IceCandidate) -> Unit,
        onIceConnectionStateChanged: (PeerConnection.IceConnectionState) -> Unit
    ): Boolean {
        if (!ensureInitialized()) return false
        val pcf = factory ?: return false

        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        val observer = object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                if (state != null) onIceConnectionStateChanged(state)
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate?) {
                if (candidate != null) onIceCandidateGenerated(candidate)
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {
                val track = stream?.videoTracks?.firstOrNull()
                if (track != null) {
                    remoteVideoTrack = track
                    remoteRenderer?.let { track.addSink(it) }
                }
            }
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                val track = receiver?.track() as? VideoTrack
                if (track != null) {
                    remoteVideoTrack = track
                    remoteRenderer?.let { track.addSink(it) }
                }
            }
        }

        val pc = pcf.createPeerConnection(rtcConfig, observer) ?: return false
        peerConnection = pc

        try {
            val audioConstraints = MediaConstraints()
            val aSrc = pcf.createAudioSource(audioConstraints)
            audioSource = aSrc
            val aTrack = pcf.createAudioTrack("ARDAMSa0", aSrc).apply { setEnabled(true) }
            localAudioTrack = aTrack
            pc.addTrack(aTrack, listOf("ARDAMS"))
        } catch (t: Throwable) {
            Log.w(TAG, "Audio track setup warning: ${t.message}")
        }

        if (isVideoCall) {
            setupLocalVideoTrack(pcf, pc)
        }

        configureAudioRouting(speakerOn = isVideoCall)
        return true
    }

    private fun setupLocalVideoTrack(pcf: PeerConnectionFactory, pc: PeerConnection) {
        try {
            val enumerator = Camera2Enumerator(appContext)
            val frontCam = enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) }
                ?: enumerator.deviceNames.firstOrNull()
                ?: return
            val capturer = enumerator.createCapturer(frontCam, null) ?: return
            videoCapturer = capturer
            val eglCtx = eglBase?.eglBaseContext ?: return
            val helper = SurfaceTextureHelper.create("CaptureThread", eglCtx)
            surfaceTextureHelper = helper
            val vSrc = pcf.createVideoSource(capturer.isScreencast)
            videoSource = vSrc
            capturer.initialize(helper, appContext, vSrc.capturerObserver)
            capturer.startCapture(640, 480, 24)

            val vTrack = pcf.createVideoTrack("ARDAMSv0", vSrc).apply { setEnabled(true) }
            localVideoTrack = vTrack
            localRenderer?.let { vTrack.addSink(it) }
            pc.addTrack(vTrack, listOf("ARDAMS"))
        } catch (t: Throwable) {
            Log.w(TAG, "Video track setup warning: ${t.message}")
        }
    }

    fun attachLocalRenderer(renderer: SurfaceViewRenderer?) {
        localRenderer = renderer
        if (renderer != null) {
            localVideoTrack?.addSink(renderer)
        }
    }

    fun attachRemoteRenderer(renderer: SurfaceViewRenderer?) {
        remoteRenderer = renderer
        if (renderer != null) {
            remoteVideoTrack?.addSink(renderer)
        }
    }

    suspend fun createOfferSdp(): String? = suspendCoroutine { cont ->
        val pc = peerConnection
        if (pc == null) {
            cont.resume(null)
            return@suspendCoroutine
        }
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
        pc.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                if (desc == null) {
                    cont.resume(null)
                    return
                }
                pc.setLocalDescription(object : SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() { cont.resume(desc.description) }
                    override fun onCreateFailure(err: String?) {}
                    override fun onSetFailure(err: String?) { cont.resume(desc.description) }
                }, desc)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(err: String?) {
                Log.w(TAG, "createOffer failed: $err")
                cont.resume(null)
            }
            override fun onSetFailure(err: String?) {}
        }, constraints)
    }

    suspend fun createAnswerSdp(remoteOfferSdp: String): String? = suspendCoroutine { cont ->
        val pc = peerConnection
        if (pc == null || remoteOfferSdp.isBlank()) {
            cont.resume(null)
            return@suspendCoroutine
        }
        val offerDesc = SessionDescription(SessionDescription.Type.OFFER, remoteOfferSdp)
        pc.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                val constraints = MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
                }
                pc.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(answerDesc: SessionDescription?) {
                        if (answerDesc == null) {
                            cont.resume(null)
                            return
                        }
                        pc.setLocalDescription(object : SdpObserver {
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onSetSuccess() { cont.resume(answerDesc.description) }
                            override fun onCreateFailure(err: String?) {}
                            override fun onSetFailure(err: String?) { cont.resume(answerDesc.description) }
                        }, answerDesc)
                    }
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(err: String?) {
                        Log.w(TAG, "createAnswer failed: $err")
                        cont.resume(null)
                    }
                    override fun onSetFailure(err: String?) {}
                }, constraints)
            }
            override fun onCreateFailure(err: String?) {}
            override fun onSetFailure(err: String?) {
                Log.w(TAG, "setRemoteDescription offer failed: $err")
                cont.resume(null)
            }
        }, offerDesc)
    }

    fun applyRemoteAnswerSdp(answerSdp: String) {
        val pc = peerConnection ?: return
        if (answerSdp.isBlank()) return
        try {
            val answerDesc = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
            pc.setRemoteDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onSetSuccess() { Log.d(TAG, "Remote answer applied") }
                override fun onCreateFailure(err: String?) {}
                override fun onSetFailure(err: String?) { Log.w(TAG, "setRemoteDescription answer failed: $err") }
            }, answerDesc)
        } catch (t: Throwable) {
            Log.w(TAG, "applyRemoteAnswerSdp warning: ${t.message}")
        }
    }

    fun addRemoteIceCandidate(sdpMid: String, sdpMLineIndex: Int, candidate: String) {
        try {
            peerConnection?.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, candidate))
        } catch (t: Throwable) {
            Log.w(TAG, "addIceCandidate warning: ${t.message}")
        }
    }

    fun setMicMuted(muted: Boolean) {
        try {
            localAudioTrack?.setEnabled(!muted)
            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            am?.isMicrophoneMute = muted
        } catch (_: Throwable) {}
    }

    fun setCameraEnabled(enabled: Boolean) {
        try {
            localVideoTrack?.setEnabled(enabled)
        } catch (_: Throwable) {}
    }

    fun configureAudioRouting(speakerOn: Boolean) {
        try {
            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            am.mode = AudioManager.MODE_IN_COMMUNICATION
            am.isSpeakerphoneOn = speakerOn
        } catch (_: Throwable) {}
    }

    fun endSession() {
        try {
            localRenderer = null
            remoteRenderer = null
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null
            surfaceTextureHelper?.dispose()
            surfaceTextureHelper = null
            localVideoTrack?.dispose()
            localVideoTrack = null
            remoteVideoTrack = null
            videoSource?.dispose()
            videoSource = null
            localAudioTrack?.dispose()
            localAudioTrack = null
            audioSource?.dispose()
            audioSource = null
            peerConnection?.close()
            peerConnection?.dispose()
            peerConnection = null

            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            am?.isMicrophoneMute = false
            am?.isSpeakerphoneOn = false
            am?.mode = AudioManager.MODE_NORMAL
        } catch (t: Throwable) {
            Log.w(TAG, "WebRTC cleanup warning: ${t.message}")
        }
    }
}
