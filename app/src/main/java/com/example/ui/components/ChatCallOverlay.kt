package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.model.ActiveCallSession
import com.example.model.ChatCallStatus
import com.example.model.ChatCallType
import com.example.model.Language
import com.example.ui.theme.LocalDevRoutineColors
import com.example.util.WebRtcCallManager
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import java.util.Locale

@Composable
fun ChatCallOverlay(
    callSession: ActiveCallSession?,
    currentUserId: String?,
    language: Language,
    webRtcCallManager: WebRtcCallManager,
    onAcceptCall: () -> Unit,
    onDeclineOrEndCall: () -> Unit,
    onToggleMicMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleCamera: () -> Unit,
    onMinimize: (() -> Unit)? = null
) {
    val session = callSession ?: return
    val isPt = language == Language.PT
    val brand = LocalDevRoutineColors.current
    val context = LocalContext.current

    val peerName = if (!currentUserId.isNullOrBlank()) {
        session.peerName(currentUserId)
    } else {
        session.calleeName.ifBlank { session.callerName }
    }

    val isIncomingRinging = !session.isOutgoing && session.status == ChatCallStatus.RINGING
    val isVideo = session.callType == ChatCallType.VIDEO

    val answerPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val audioOk = grants[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val camOk = !isVideo ||
            grants[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (audioOk && camOk) {
            onAcceptCall()
        }
    }

    fun acceptWithPermissions() {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.RECORD_AUDIO)
        }
        if (isVideo &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            needed.add(Manifest.permission.CAMERA)
        }
        if (needed.isEmpty()) {
            onAcceptCall()
        } else {
            answerPermissionLauncher.launch(needed.toTypedArray())
        }
    }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0E071B), Color(0xFF1A0D2E), Color(0xFF090412))
                    )
                )
                .testTag("overlay_active_call")
        ) {
            // Remote video surface when video call is accepted and WebRTC EGL is available
            val eglCtx = webRtcCallManager.eglContext
            if (isVideo && session.status == ChatCallStatus.ACCEPTED && eglCtx != null) {
                var remoteRendererRef by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
                var localRendererRef by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

                AndroidView(
                    factory = { ctx ->
                        SurfaceViewRenderer(ctx).apply {
                            runCatching {
                                init(eglCtx, null)
                                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                                setEnableHardwareScaler(true)
                            }
                            remoteRendererRef = this
                            webRtcCallManager.attachRemoteRenderer(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (session.isCameraEnabled) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, brand.primary),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 48.dp, end = 16.dp)
                            .size(width = 110.dp, height = 160.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                SurfaceViewRenderer(ctx).apply {
                                    runCatching {
                                        init(eglCtx, null)
                                        setMirror(true)
                                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                                        setZOrderMediaOverlay(true)
                                    }
                                    localRendererRef = this
                                    webRtcCallManager.attachLocalRenderer(this)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                DisposableEffect(session.callId) {
                    onDispose {
                        webRtcCallManager.attachRemoteRenderer(null)
                        webRtcCallManager.attachLocalRenderer(null)
                        runCatching { remoteRendererRef?.release() }
                        runCatching { localRendererRef?.release() }
                    }
                }
            }

            // Top Minimize pill (returns to conversation while call stays active at the top)
            if (onMinimize != null && session.status == ChatCallStatus.ACCEPTED) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.55f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 42.dp, start = 16.dp)
                        .minimumInteractiveComponentSize()
                        .clickable { onMinimize() }
                        .testTag("button_minimize_call")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isPt) "Minimizar chamada" else "Minimize call",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isPt) "Chat" else "Chat",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Center Info Card
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isVideo || session.status != ChatCallStatus.ACCEPTED) {
                    UserAvatar(
                        photoUrl = session.callerPhotoUrl,
                        displayName = peerName,
                        size = 104.dp,
                        borderWidth = 3.dp,
                        borderColor = brand.primaryLight
                    )
                }

                Text(
                    text = peerName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                val statusText = when {
                    isIncomingRinging ->
                        if (isPt) {
                            if (isVideo) "Chamada de vídeo recebida..." else "Ligação de voz recebida..."
                        } else {
                            if (isVideo) "Incoming video call..." else "Incoming voice call..."
                        }
                    session.status == ChatCallStatus.RINGING ->
                        if (isPt) "Chamando..." else "Calling..."
                    session.status == ChatCallStatus.ACCEPTED -> {
                        val mins = session.elapsedSeconds / 60
                        val secs = session.elapsedSeconds % 60
                        val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)
                        val connHint = if (session.peerConnected) "" else if (isPt) " • Conectando áudio/vídeo" else " • Connecting"
                        "$timeStr$connHint"
                    }
                    else -> ""
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = statusText,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Bottom Call Controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 44.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (isIncomingRinging) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = onDeclineOrEndCall,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.height(60.dp).testTag("button_decline_incoming_call")
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = if (isPt) "Recusar chamada" else "Decline call")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPt) "Recusar" else "Decline", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { acceptWithPermissions() },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.height(60.dp).testTag("button_accept_incoming_call")
                        ) {
                            Icon(
                                if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                                contentDescription = if (isPt) "Atender chamada" else "Accept call"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isPt) "Atender" else "Answer", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CallActionCircleButton(
                            icon = if (session.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (isPt) "Mudo" else "Mute",
                            active = session.isMicMuted,
                            onClick = onToggleMicMute,
                            testTag = "button_call_toggle_mic"
                        )

                        CallActionCircleButton(
                            icon = if (session.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.PhoneInTalk,
                            label = if (isPt) "Viva-voz" else "Speaker",
                            active = session.isSpeakerOn,
                            onClick = onToggleSpeaker,
                            testTag = "button_call_toggle_speaker"
                        )

                        if (isVideo) {
                            CallActionCircleButton(
                                icon = if (session.isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                label = if (isPt) "Câmera" else "Camera",
                                active = !session.isCameraEnabled,
                                onClick = onToggleCamera,
                                testTag = "button_call_toggle_camera"
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = onDeclineOrEndCall,
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD32F2F))
                                    .testTag("button_end_active_call")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CallEnd,
                                    contentDescription = if (isPt) "Encerrar ligação" else "End call",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(if (isPt) "Encerrar" else "End", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallActionCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (active) Color.White else Color.White.copy(alpha = 0.16f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) Color.Black else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
    }
}

@Composable
fun ActiveCallTopBanner(
    callSession: ActiveCallSession,
    currentUserId: String?,
    language: Language,
    onExpand: () -> Unit,
    onToggleMicMute: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val peerName = if (!currentUserId.isNullOrBlank()) {
        callSession.peerName(currentUserId)
    } else {
        callSession.calleeName.ifBlank { callSession.callerName }
    }
    val isVideo = callSession.callType == ChatCallType.VIDEO
    val mins = callSession.elapsedSeconds / 60
    val secs = callSession.elapsedSeconds % 60
    val timer = String.format(Locale.US, "%02d:%02d", mins, secs)

    Surface(
        color = Color(0xFF1B5E20),
        contentColor = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpand() }
            .testTag("banner_active_call_top")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserAvatar(
                photoUrl = callSession.callerPhotoUrl,
                displayName = peerName,
                size = 30.dp,
                borderWidth = 1.5.dp,
                borderColor = Color(0xFF69F0AE)
            )
            Icon(
                imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = null,
                tint = Color(0xFF69F0AE),
                modifier = Modifier.size(16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = peerName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = if (isPt) {
                        "${if (isVideo) "Vídeo" else "Voz"} em andamento • $timer"
                    } else {
                        "${if (isVideo) "Video" else "Voice"} active • $timer"
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
            IconButton(
                onClick = onToggleMicMute,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("button_banner_call_mute")
            ) {
                Icon(
                    imageVector = if (callSession.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isPt) "Microfone" else "Mic",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onExpand,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("button_banner_call_expand")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = if (isPt) "Expandir chamada" else "Expand call",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD32F2F))
                    .testTag("button_banner_call_end")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = if (isPt) "Encerrar" else "End",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

