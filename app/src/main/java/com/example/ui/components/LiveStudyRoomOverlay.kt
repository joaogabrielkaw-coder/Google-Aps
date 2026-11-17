package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.model.Language
import com.example.model.LiveMessageType
import com.example.model.LiveRoomMessage
import com.example.model.LiveRoomMode
import com.example.model.LiveStudyRoom
import com.example.ui.theme.LocalDevRoutineColors
import com.example.util.WebRtcLiveManager
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import java.util.Locale

@Composable
fun LiveStudyRoomOverlay(
    liveRoom: LiveStudyRoom?,
    currentUserId: String?,
    language: Language,
    messages: List<LiveRoomMessage>,
    isMicMuted: Boolean,
    isCameraEnabled: Boolean,
    isSpeakerOn: Boolean,
    isPeerConnected: Boolean,
    elapsedSeconds: Int,
    webRtcLiveManager: WebRtcLiveManager,
    onSendMessage: (String, LiveMessageType) -> Unit,
    onToggleMicMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onLeaveOrEndLive: () -> Unit,
    onMinimize: (() -> Unit)? = null
) {
    val room = liveRoom ?: return
    val isPt = language == Language.PT
    val brand = LocalDevRoutineColors.current
    val context = LocalContext.current
    val isHost = !currentUserId.isNullOrBlank() && currentUserId == room.hostId

    var draftMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onToggleMicMute()
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onToggleCamera()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val durationText = remember(elapsedSeconds) {
        val m = elapsedSeconds / 60
        val s = elapsedSeconds % 60
        String.format(Locale.US, "%02d:%02d", m, s)
    }

    Dialog(
        onDismissRequest = { /* explicit leave button */ },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        BackHandler { onLeaveOrEndLive() }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("overlay_live_study_room"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF100720),
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                // Header Bar da Live
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE53935)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sensors,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = if (isPt) "AO VIVO • $durationText" else "LIVE • $durationText",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = brand.primary.copy(alpha = 0.24f)
                            ) {
                                Text(
                                    text = room.topicLabel,
                                    color = brand.primaryLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Text("${room.participantCount}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = room.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isPt) "Host: ${room.hostName}" else "Host: ${room.hostName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    if (onMinimize != null) {
                        IconButton(
                            onClick = onMinimize,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("button_minimize_live")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isPt) "Minimizar Live" else "Minimize Live",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Button(
                        onClick = onLeaveOrEndLive,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("button_live_leave_or_end")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHost) (if (isPt) "Encerrar" else "End") else (if (isPt) "Sair" else "Leave"),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Palco de Vídeo / Áudio da Live
                val eglContext = remember(room.roomId) { webRtcLiveManager.eglContext }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(210.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF170F2B)),
                    border = BorderStroke(1.dp, brand.primary.copy(alpha = 0.45f))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (room.liveMode == LiveRoomMode.VIDEO && eglContext != null &&
                            ((isHost && isCameraEnabled) || (!isHost && isPeerConnected))
                        ) {
                            var videoRendererRef by remember { mutableStateOf<SurfaceViewRenderer?>(null) }
                            AndroidView(
                                factory = { ctx ->
                                    SurfaceViewRenderer(ctx).apply {
                                        init(eglContext, null)
                                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                                        setMirror(isHost)
                                        if (isHost) webRtcLiveManager.attachLocalRenderer(this)
                                        else webRtcLiveManager.attachRemoteRenderer(this)
                                        videoRendererRef = this
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            DisposableEffect(room.roomId, isHost) {
                                onDispose {
                                    if (isHost) webRtcLiveManager.attachLocalRenderer(null)
                                    else webRtcLiveManager.attachRemoteRenderer(null)
                                    try { videoRendererRef?.release() } catch (_: Throwable) {}
                                }
                            }
                        } else {
                            // Palco Visual de Discussão por Áudio / Sala de Estudo
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, Color(0xFFE53935), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    UserAvatar(
                                        photoUrl = room.hostPhotoUrl,
                                        displayName = room.hostName,
                                        size = 66.dp
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = if (room.liveMode == LiveRoomMode.AUDIO) {
                                            if (isPt) "Sala de Áudio & Discussão em andamento" else "Live Audio Discussion in progress"
                                        } else {
                                            if (isPt) "Transmissão de Vídeo • Conectando participantes..." else "Live Video Stage • Connecting peers..."
                                        },
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (room.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = room.description,
                                        color = Color.White.copy(alpha = 0.72f),
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Lista de participantes no rodapé do palco
                        val participantEntries = room.participantNames.entries.toList()
                        if (participantEntries.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(participantEntries, key = { it.key }) { (uid, name) ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (uid == room.hostId) brand.primary.copy(alpha = 0.85f)
                                        else Color.White.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = if (uid == room.hostId) "★ $name" else name,
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Controles da Live (Microfone, Câmera, Viva-voz, Pedir Fala)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isMicMuted) MaterialTheme.colorScheme.surfaceVariant else brand.primary.copy(alpha = 0.22f),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                val hasAudio = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasAudio || !isMicMuted) {
                                    onToggleMicMute()
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("button_live_toggle_mic")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = if (isPt) "Microfone" else "Microphone",
                                    tint = if (isMicMuted) MaterialTheme.colorScheme.onSurfaceVariant else brand.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isMicMuted) (if (isPt) "Mudo" else "Muted") else (if (isPt) "Voz" else "Mic"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (!isCameraEnabled) MaterialTheme.colorScheme.surfaceVariant else brand.primary.copy(alpha = 0.22f),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                val hasCam = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCam || isCameraEnabled) {
                                    onToggleCamera()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("button_live_toggle_camera")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = if (isPt) "Câmera" else "Camera",
                                    tint = if (isCameraEnabled) brand.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isPt) "Vídeo" else "Cam",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSpeakerOn) brand.primary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        IconButton(
                            onClick = onToggleSpeaker,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("button_live_toggle_speaker")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = if (isPt) "Alto-falante" else "Speaker",
                                tint = if (isSpeakerOn) brand.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val raiseText = if (isPt) "✋ Pediu a palavra para discutir uma dúvida!"
                            else "✋ Raised hand to discuss a question!"
                            onSendMessage(raiseText, LiveMessageType.HAND_RAISE)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brand.accent),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("button_live_raise_hand")
                    ) {
                        Icon(Icons.Default.PanTool, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPt) "Falar" else "Raise",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Título da Área de Discussão / Perguntas da Live
                Text(
                    text = if (isPt) "Chat da Live • Tire dúvidas e discuta os conteúdos"
                    else "Live Discussion • Ask questions and share insights",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = brand.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )

                // Mensagens em Tempo Real da Live
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.messageId }) { msg ->
                        when (msg.messageType) {
                            LiveMessageType.SYSTEM -> {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${msg.senderName}: ${msg.text}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            LiveMessageType.HAND_RAISE -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = brand.accent.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, brand.accent.copy(alpha = 0.55f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        UserAvatar(
                                            photoUrl = msg.senderPhotoUrl,
                                            displayName = msg.senderName,
                                            size = 28.dp
                                        )
                                        Text(
                                            text = "${msg.senderName} ${msg.text}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = brand.accent
                                        )
                                    }
                                }
                            }
                            LiveMessageType.TEXT -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    UserAvatar(
                                        photoUrl = msg.senderPhotoUrl,
                                        displayName = msg.senderName,
                                        size = 30.dp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                                            Text(
                                                text = msg.senderName,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (msg.senderId == room.hostId) brand.accent else brand.primary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Campo para enviar mensagem/dúvida durante a Live
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = draftMessage,
                            onValueChange = { if (it.length <= 500) draftMessage = it },
                            placeholder = {
                                Text(
                                    if (isPt) "Comente ou tire uma dúvida na Live..."
                                    else "Ask a question or join the discussion..."
                                )
                            },
                            maxLines = 3,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("live_room_chat_input")
                        )
                        IconButton(
                            onClick = {
                                if (draftMessage.isNotBlank()) {
                                    onSendMessage(draftMessage, LiveMessageType.TEXT)
                                    draftMessage = ""
                                }
                            },
                            enabled = draftMessage.isNotBlank(),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (draftMessage.isNotBlank()) brand.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("button_live_room_send")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = if (isPt) "Enviar na Live" else "Send in Live",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveLiveTopBanner(
    liveRoom: LiveStudyRoom,
    currentUserId: String?,
    language: Language,
    isMicMuted: Boolean,
    elapsedSeconds: Int,
    onExpand: () -> Unit,
    onToggleMicMute: () -> Unit,
    onLeaveOrEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val isHost = !currentUserId.isNullOrBlank() && currentUserId == liveRoom.hostId
    val mins = elapsedSeconds / 60
    val secs = elapsedSeconds % 60
    val timer = String.format(Locale.US, "%02d:%02d", mins, secs)

    Surface(
        color = Color(0xFFB71C1C),
        contentColor = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpand() }
            .testTag("banner_active_live_top")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserAvatar(
                photoUrl = liveRoom.hostPhotoUrl,
                displayName = liveRoom.hostName,
                size = 30.dp,
                borderWidth = 1.5.dp,
                borderColor = Color.White
            )
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = liveRoom.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isPt) "LIVE ${liveRoom.topicLabel} • ${liveRoom.participantCount} na sala • $timer"
                    else "LIVE ${liveRoom.topicLabel} • ${liveRoom.participantCount} in room • $timer",
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
            IconButton(
                onClick = onToggleMicMute,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("button_banner_live_mute")
            ) {
                Icon(
                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isPt) "Microfone da Live" else "Live Mic",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onExpand,
                modifier = Modifier.minimumInteractiveComponentSize().testTag("button_banner_live_expand")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = if (isPt) "Voltar à Live" else "Expand Live",
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }
            IconButton(
                onClick = onLeaveOrEnd,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .testTag("button_banner_live_leave")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = if (isHost) (if (isPt) "Encerrar" else "End") else (if (isPt) "Sair" else "Leave"),
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

