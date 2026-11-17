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
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "WebRtcLiveManager"

/**
 * Gerencia sessões WebRTC para Salas Ao Vivo (Live de Estudos) usando topologia
 * estrela centrada no host, compartilhando faixas locais de áudio/vídeo com todos
 * os participantes conectados.
 */
class WebRtcLiveManager(private val appContext: Context) {

    private var eglBase: EglBase? = null
    private var factory: PeerConnectionFactory? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private val peerConnections = ConcurrentHashMap<String, PeerConnection>()

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
            Log.w(TAG, "WebRTC live native init unavailable: ${t.message}")
            false
        }
    }

    fun startLocalMedia(enableVideo: Boolean, speakerOn: Boolean = true): Boolean {
        if (!ensureInitialized()) return false
        val pcf = factory ?: return false

        if (localAudioTrack == null) {
            try {
                val aSrc = pcf.createAudioSource(MediaConstraints())
                audioSource = aSrc
                localAudioTrack = pcf.createAudioTrack("LIVE_AUDIO_0", aSrc).apply { setEnabled(true) }
            } catch (t: Throwable) {
                Log.w(TAG, "Live audio track warning: ${t.message}")
            }
        }

        if (enableVideo && localVideoTrack == null) {
            setupLocalVideoTrack(pcf)
        }

        configureAudioRouting(speakerOn = speakerOn)
        return true
    }

    private fun setupLocalVideoTrack(pcf: PeerConnectionFactory) {
        try {
            val enumerator = Camera2Enumerator(appContext)
            val camName = enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) }
                ?: enumerator.deviceNames.firstOrNull()
                ?: return
            val capturer = enumerator.createCapturer(camName, null) ?: return
            videoCapturer = capturer
            val eglCtx = eglBase?.eglBaseContext ?: return
            val helper = SurfaceTextureHelper.create("LiveCaptureThread", eglCtx)
            surfaceTextureHelper = helper
            val vSrc = pcf.createVideoSource(capturer.isScreencast)
            videoSource = vSrc
            capturer.initialize(helper, appContext, vSrc.capturerObserver)
            capturer.startCapture(640, 480, 24)
            val vTrack = pcf.createVideoTrack("LIVE_VIDEO_0", vSrc).apply { setEnabled(true) }
            localVideoTrack = vTrack
            localRenderer?.let { vTrack.addSink(it) }
        } catch (t: Throwable) {
            Log.w(TAG, "Live camera track warning: ${t.message}")
        }
    }

    fun getOrCreatePeerConnection(
        remoteUid: String,
        onIceCandidateGenerated: (IceCandidate) -> Unit,
        onConnectionChanged: (Boolean) -> Unit
    ): PeerConnection? {
        peerConnections[remoteUid]?.let { return it }
        if (!ensureInitialized()) return null
        val pcf = factory ?: return null

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
                val connected = state == PeerConnection.IceConnectionState.CONNECTED ||
                    state == PeerConnection.IceConnectionState.COMPLETED
                onConnectionChanged(connected)
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

        val pc = pcf.createPeerConnection(rtcConfig, observer) ?: return null
        localAudioTrack?.let { pc.addTrack(it, listOf("LIVE_STREAM")) }
        localVideoTrack?.let { pc.addTrack(it, listOf("LIVE_STREAM")) }
        peerConnections[remoteUid] = pc
        return pc
    }

    suspend fun createOfferForPeer(remoteUid: String): String? = suspendCoroutine { cont ->
        val pc = peerConnections[remoteUid]
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
            override fun onCreateFailure(err: String?) { cont.resume(null) }
            override fun onSetFailure(err: String?) {}
        }, constraints)
    }

    suspend fun createAnswerForPeer(remoteUid: String, offerSdp: String): String? = suspendCoroutine { cont ->
        val pc = peerConnections[remoteUid]
        if (pc == null || offerSdp.isBlank()) {
            cont.resume(null)
            return@suspendCoroutine
        }
        val offerDesc = SessionDescription(SessionDescription.Type.OFFER, offerSdp)
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
                    override fun onCreateFailure(err: String?) { cont.resume(null) }
                    override fun onSetFailure(err: String?) {}
                }, constraints)
            }
            override fun onCreateFailure(err: String?) {}
            override fun onSetFailure(err: String?) { cont.resume(null) }
        }, offerDesc)
    }

    fun applyRemoteAnswerForPeer(remoteUid: String, answerSdp: String) {
        val pc = peerConnections[remoteUid] ?: return
        if (answerSdp.isBlank()) return
        try {
            val answerDesc = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
            pc.setRemoteDescription(object : SdpObserver {
                override fun onCreateSuccess(p0: SessionDescription?) {}
                override fun onSetSuccess() {}
                override fun onCreateFailure(err: String?) {}
                override fun onSetFailure(err: String?) {}
            }, answerDesc)
        } catch (_: Throwable) {}
    }

    fun addRemoteIceCandidate(remoteUid: String, sdpMid: String, sdpMLineIndex: Int, candidate: String) {
        try {
            peerConnections[remoteUid]?.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, candidate))
        } catch (_: Throwable) {}
    }

    fun hasPeer(remoteUid: String): Boolean = peerConnections.containsKey(remoteUid)

    fun attachLocalRenderer(renderer: SurfaceViewRenderer?) {
        localRenderer = renderer
        if (renderer != null) localVideoTrack?.addSink(renderer)
    }

    fun attachRemoteRenderer(renderer: SurfaceViewRenderer?) {
        remoteRenderer = renderer
        if (renderer != null) remoteVideoTrack?.addSink(renderer)
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
            if (enabled && localVideoTrack == null) {
                factory?.let { pcf ->
                    setupLocalVideoTrack(pcf)
                    localVideoTrack?.let { track ->
                        peerConnections.values.forEach { pc ->
                            pc.addTrack(track, listOf("LIVE_STREAM"))
                        }
                    }
                }
            }
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

    fun endLiveSession() {
        try {
            localRenderer = null
            remoteRenderer = null
            peerConnections.values.forEach { pc ->
                try {
                    pc.close()
                    pc.dispose()
                } catch (_: Throwable) {}
            }
            peerConnections.clear()
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

            val am = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            am?.isMicrophoneMute = false
            am?.isSpeakerphoneOn = false
            am?.mode = AudioManager.MODE_NORMAL
        } catch (t: Throwable) {
            Log.w(TAG, "Live WebRTC cleanup warning: ${t.message}")
        }
    }
}
