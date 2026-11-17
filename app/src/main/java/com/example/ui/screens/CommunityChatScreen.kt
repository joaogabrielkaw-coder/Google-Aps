package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.model.ChatCallType
import com.example.model.ChatConversationItem
import com.example.model.ChatDirectorySearchHelper
import com.example.model.ChatDirectoryUser
import com.example.model.ChatIdUtil
import com.example.model.ChatMessageItem
import com.example.model.ChatMessageType
import com.example.model.ChatRequestItem
import com.example.model.ChatRequestStatus
import com.example.model.DirectoryFilterMode
import com.example.model.DirectorySortMode
import com.example.model.Language
import com.example.model.LeaderboardEntry
import com.example.model.LiveRoomMode
import com.example.model.LiveStudyRoom
import com.example.model.Technology
import com.example.ui.components.UserAvatar
import com.example.ui.theme.LocalDevRoutineColors
import com.example.util.AvatarImageCodec
import com.example.util.ChatMediaCodec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityChatScreen(
    language: Language,
    currentUserId: String?,
    isFirebaseAuthenticated: Boolean,
    directoryUsers: List<ChatDirectoryUser>,
    leaderboardEntries: List<LeaderboardEntry> = emptyList(),
    chatRequests: List<ChatRequestItem>,
    conversations: List<ChatConversationItem>,
    activeLiveRooms: List<LiveStudyRoom>,
    activeChatId: String?,
    activeMessages: List<ChatMessageItem>,
    isUploading: Boolean,
    statusFeedback: String?,
    onDismissFeedback: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onOpenConversation: (String) -> Unit,
    onCloseConversation: () -> Unit,
    onSendRequest: (ChatDirectoryUser, String) -> Unit,
    onAcceptRequest: (ChatRequestItem) -> Unit,
    onDeclineOrDeleteRequest: (String) -> Unit,
    onSendTextMessage: (String) -> Unit,
    onSendAttachment: (Uri, ChatMessageType, String) -> Unit,
    onEnsureMediaLocalPath: suspend (ChatMessageItem) -> String?,
    onOpenAttachmentExternal: (ChatMessageItem) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onStartCall: (ChatCallType) -> Unit,
    onStartCallWithConversation: (ChatConversationItem, ChatCallType) -> Unit,
    onCreateLiveRoom: (String, String, String, String, LiveRoomMode) -> Unit,
    onJoinLiveRoom: (LiveStudyRoom) -> Unit,
    activeCallBanner: (@Composable () -> Unit)? = null,
    activeLiveBanner: (@Composable () -> Unit)? = null
) {
    val activeConv = conversations.firstOrNull { it.chatId == activeChatId }

    if (activeChatId != null && activeConv != null) {
        val peerProfile = remember(directoryUsers, activeConv.peerUserId) {
            directoryUsers.firstOrNull { it.userId == activeConv.peerUserId }
        }
        val peerLeaderboard = remember(leaderboardEntries, activeConv.peerUserId) {
            leaderboardEntries.firstOrNull { it.userId == activeConv.peerUserId }
        }
        ChatThreadView(
            language = language,
            currentUserId = currentUserId,
            conversation = activeConv,
            peerProfile = peerProfile,
            peerLeaderboard = peerLeaderboard,
            messages = activeMessages,
            isUploading = isUploading,
            statusFeedback = statusFeedback,
            onDismissFeedback = onDismissFeedback,
            onBack = onCloseConversation,
            onSendTextMessage = onSendTextMessage,
            onSendAttachment = onSendAttachment,
            onEnsureMediaLocalPath = onEnsureMediaLocalPath,
            onOpenAttachmentExternal = onOpenAttachmentExternal,
            onDeleteMessage = onDeleteMessage,
            onDeleteConversation = { onDeleteConversation(activeConv.chatId) },
            onStartCall = onStartCall,
            onCreateLiveRoom = onCreateLiveRoom,
            activeCallBanner = activeCallBanner,
            activeLiveBanner = activeLiveBanner
        )
    } else {
        ChatHubView(
            language = language,
            currentUserId = currentUserId,
            isFirebaseAuthenticated = isFirebaseAuthenticated,
            directoryUsers = directoryUsers,
            leaderboardEntries = leaderboardEntries,
            chatRequests = chatRequests,
            conversations = conversations,
            activeLiveRooms = activeLiveRooms,
            statusFeedback = statusFeedback,
            onDismissFeedback = onDismissFeedback,
            onGoogleSignInClick = onGoogleSignInClick,
            onOpenConversation = onOpenConversation,
            onSendRequest = onSendRequest,
            onAcceptRequest = onAcceptRequest,
            onDeclineOrDeleteRequest = onDeclineOrDeleteRequest,
            onDeleteConversation = onDeleteConversation,
            onStartCallWithConversation = onStartCallWithConversation,
            onCreateLiveRoom = onCreateLiveRoom,
            onJoinLiveRoom = onJoinLiveRoom,
            activeCallBanner = activeCallBanner,
            activeLiveBanner = activeLiveBanner
        )
    }
}

@Composable
private fun ChatHubView(
    language: Language,
    currentUserId: String?,
    isFirebaseAuthenticated: Boolean,
    directoryUsers: List<ChatDirectoryUser>,
    leaderboardEntries: List<LeaderboardEntry>,
    chatRequests: List<ChatRequestItem>,
    conversations: List<ChatConversationItem>,
    activeLiveRooms: List<LiveStudyRoom>,
    statusFeedback: String?,
    onDismissFeedback: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onOpenConversation: (String) -> Unit,
    onSendRequest: (ChatDirectoryUser, String) -> Unit,
    onAcceptRequest: (ChatRequestItem) -> Unit,
    onDeclineOrDeleteRequest: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onStartCallWithConversation: (ChatConversationItem, ChatCallType) -> Unit,
    onCreateLiveRoom: (String, String, String, String, LiveRoomMode) -> Unit,
    onJoinLiveRoom: (LiveStudyRoom) -> Unit,
    activeCallBanner: (@Composable () -> Unit)? = null,
    activeLiveBanner: (@Composable () -> Unit)? = null
) {
    val isPt = language == Language.PT
    val brand = LocalDevRoutineColors.current
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var directoryFilterMode by remember { mutableStateOf(DirectoryFilterMode.ALL) }
    var directorySortMode by remember { mutableStateOf(DirectorySortMode.ALPHABETICAL) }
    var selectedDirectoryUserInfo by remember { mutableStateOf<ChatDirectoryUser?>(null) }
    var requestTargetUser by remember { mutableStateOf<ChatDirectoryUser?>(null) }
    var requestIntroText by remember { mutableStateOf("") }
    var conversationToDelete by remember { mutableStateOf<ChatConversationItem?>(null) }
    var showCreateLiveDialog by remember { mutableStateOf(false) }

    var pendingCallTarget by remember { mutableStateOf<Pair<ChatConversationItem, ChatCallType>?>(null) }
    var pendingLiveJoin by remember { mutableStateOf<LiveStudyRoom?>(null) }
    var pendingLiveCreate by remember { mutableStateOf<CreateLiveParams?>(null) }

    val hubPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val audioOk = grants[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        pendingCallTarget?.let { (conv, type) ->
            pendingCallTarget = null
            if (audioOk) onStartCallWithConversation(conv, type)
        }
        pendingLiveJoin?.let { room ->
            pendingLiveJoin = null
            onJoinLiveRoom(room)
        }
        pendingLiveCreate?.let { params ->
            pendingLiveCreate = null
            onCreateLiveRoom(params.title, params.techId, params.techLabel, params.description, params.mode)
        }
    }

    val launchQuickCall: (ChatConversationItem, ChatCallType) -> Unit = { conv, callType ->
        val perms = if (callType == ChatCallType.VIDEO) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
        val allGranted = perms.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            onStartCallWithConversation(conv, callType)
        } else {
            pendingCallTarget = conv to callType
            hubPermissionLauncher.launch(perms)
        }
    }

    val launchCreateLiveWithPermissions: (CreateLiveParams) -> Unit = { params ->
        val perms = if (params.mode == LiveRoomMode.VIDEO) {
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
        val allGranted = perms.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            onCreateLiveRoom(params.title, params.techId, params.techLabel, params.description, params.mode)
        } else {
            pendingLiveCreate = params
            hubPermissionLauncher.launch(perms)
        }
    }

    val launchJoinLiveWithPermissions: (LiveStudyRoom) -> Unit = { room ->
        val hasAudio = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasAudio) {
            onJoinLiveRoom(room)
        } else {
            pendingLiveJoin = room
            hubPermissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }

    if (showCreateLiveDialog) {
        CreateLiveRoomDialog(
            language = language,
            onDismiss = { showCreateLiveDialog = false },
            onConfirmCreate = { title, techId, techLabel, desc, mode ->
                showCreateLiveDialog = false
                launchCreateLiveWithPermissions(
                    CreateLiveParams(title, techId, techLabel, desc, mode)
                )
            }
        )
    }

    val incomingPending = remember(chatRequests, currentUserId) {
        if (currentUserId.isNullOrBlank()) emptyList()
        else chatRequests.filter {
            it.isIncomingFor(currentUserId) && it.status == ChatRequestStatus.PENDING
        }
    }
    val outgoingRequests = remember(chatRequests, currentUserId) {
        if (currentUserId.isNullOrBlank()) emptyList()
        else chatRequests.filter {
            !it.isIncomingFor(currentUserId) && it.status != ChatRequestStatus.ACCEPTED
        }
    }

    val connectedUserIds = remember(conversations) {
        conversations.map { it.peerUserId }.toSet()
    }
    val filteredUsers = remember(
        directoryUsers,
        searchQuery,
        currentUserId,
        connectedUserIds,
        directoryFilterMode,
        directorySortMode
    ) {
        ChatDirectorySearchHelper.filterAndSortUsers(
            users = directoryUsers,
            query = searchQuery,
            currentUserId = currentUserId,
            connectedUserIds = connectedUserIds,
            filterMode = directoryFilterMode,
            sortMode = directorySortMode
        )
    }

    selectedDirectoryUserInfo?.let { infoUser ->
        val pairId = if (!currentUserId.isNullOrBlank()) {
            ChatIdUtil.deterministicPairId(currentUserId, infoUser.userId)
        } else ""
        val existingConv = conversations.firstOrNull {
            it.peerUserId == infoUser.userId || (pairId.isNotEmpty() && it.chatId == pairId)
        }
        val existingReq = chatRequests.firstOrNull { it.requestId == pairId }
        val leaderboardInfo = leaderboardEntries.firstOrNull { it.userId == infoUser.userId }
        DirectoryUserProfileDialog(
            language = language,
            user = infoUser,
            leaderboardEntry = leaderboardInfo,
            hasConversation = existingConv != null,
            hasIncomingRequest = existingReq != null && existingReq.status == ChatRequestStatus.PENDING &&
                !currentUserId.isNullOrBlank() && existingReq.isIncomingFor(currentUserId),
            hasOutgoingRequest = existingReq != null && existingReq.status == ChatRequestStatus.PENDING &&
                !currentUserId.isNullOrBlank() && !existingReq.isIncomingFor(currentUserId),
            onOpenChat = {
                selectedDirectoryUserInfo = null
                existingConv?.let { onOpenConversation(it.chatId) }
            },
            onRequestChat = {
                selectedDirectoryUserInfo = null
                requestIntroText = ""
                requestTargetUser = infoUser
            },
            onAcceptRequest = {
                selectedDirectoryUserInfo = null
                existingReq?.let { onAcceptRequest(it) }
            },
            onCancelRequest = {
                selectedDirectoryUserInfo = null
                existingReq?.let { onDeclineOrDeleteRequest(it.requestId) }
            },
            onDismiss = { selectedDirectoryUserInfo = null }
        )
    }

    requestTargetUser?.let { target ->
        AlertDialog(
            onDismissRequest = { requestTargetUser = null },
            title = {
                Text(
                    text = if (isPt) "Solicitar conversa com ${target.displayName}"
                    else "Request chat with ${target.displayName}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isPt) "O usuário precisa aceitar sua solicitação antes de iniciar o bate-papo e ligações."
                        else "The user must accept your request before chatting or calling.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = requestIntroText,
                        onValueChange = { if (it.length <= 250) requestIntroText = it },
                        label = { Text(if (isPt) "Mensagem inicial (opcional)" else "Intro message (optional)") },
                        placeholder = {
                            Text(
                                if (isPt) "Olá! Podemos trocar dicas de estudos?"
                                else "Hi! Let's collaborate and share study tips?"
                            )
                        },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("chat_request_intro_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSendRequest(target, requestIntroText.trim())
                        requestIntroText = ""
                        requestTargetUser = null
                    },
                    modifier = Modifier.testTag("button_confirm_send_chat_request")
                ) {
                    Text(if (isPt) "Enviar Solicitação" else "Send Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { requestTargetUser = null }) {
                    Text(if (isPt) "Cancelar" else "Cancel")
                }
            }
        )
    }

    conversationToDelete?.let { conv ->
        AlertDialog(
            onDismissRequest = { conversationToDelete = null },
            title = { Text(if (isPt) "Deletar conversa?" else "Delete conversation?") },
            text = {
                Text(
                    if (isPt) "A conversa com ${conv.peerDisplayName}, suas mensagens, fotos, vídeos e arquivos serão apagados do aparelho e da nuvem."
                    else "Your conversation with ${conv.peerDisplayName}, messages, and cached media will be permanently deleted from this device and the cloud."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteConversation(conv.chatId)
                        conversationToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("button_confirm_delete_conversation")
                ) {
                    Text(if (isPt) "Deletar" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationToDelete = null }) {
                    Text(if (isPt) "Cancelar" else "Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_community_chat")
    ) {
        activeCallBanner?.invoke()
        activeLiveBanner?.invoke()
        // Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(brand.primary.copy(alpha = 0.18f), brand.accent.copy(alpha = 0.14f))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(brand.primary, brand.accent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPt) "Chat & Ligações" else "Community Chat & Calls",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isPt) "Mensagens, arquivos, imagens, vídeos e chamadas"
                            else "Messages, files, photos, videos & voice/video calls",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFirebaseAuthenticated) Color(0xFF1B5E20).copy(alpha = 0.25f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isFirebaseAuthenticated) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isFirebaseAuthenticated) Color(0xFF69F0AE) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isFirebaseAuthenticated) (if (isPt) "Nuvem + Local" else "Cloud + Local")
                            else (if (isPt) "Modo Local" else "Local Only"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Status feedback banner
        AnimatedVisibility(visible = !statusFeedback.isNullOrBlank()) {
            Surface(
                color = brand.primary.copy(alpha = 0.16f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusFeedback.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismissFeedback,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = if (isPt) "Fechar aviso" else "Dismiss", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Google Sign-In banner if not authenticated with Firebase Auth
        if (!isFirebaseAuthenticated) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, brand.accent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isPt) "Conecte sua conta Google para conversar na nuvem"
                        else "Connect your Google account for cloud chat & calls",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isPt) "Suas conversas salvas localmente continuam acessíveis abaixo. Para pesquisar usuários, enviar solicitações, trocar mídias e fazer ligações, entre com o Google."
                        else "Your locally cached conversations remain accessible below. Sign in with Google to search users, send chat requests, share media, and place calls.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onGoogleSignInClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = brand.primary),
                        modifier = Modifier.testTag("button_chat_google_signin")
                    ) {
                        Text(if (isPt) "Entrar com Google" else "Sign in with Google", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Tabs: Conversas | Live & Voz | Pedidos | Pesquisar
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.testTag("tab_chat_conversations"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isPt) "Chats" else "Chats",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                        if (conversations.isNotEmpty()) {
                            Badge(containerColor = brand.primary) {
                                Text("${conversations.size}", fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.testTag("tab_chat_live_calls"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isPt) "Live" else "Live",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                        if (activeLiveRooms.isNotEmpty()) {
                            Badge(containerColor = Color(0xFFE53935)) {
                                Text("${activeLiveRooms.size}", fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier.testTag("tab_chat_requests"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (isPt) "Pedidos" else "Requests",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                        if (incomingPending.isNotEmpty()) {
                            Badge(containerColor = brand.accent) {
                                Text("${incomingPending.size}", fontSize = 9.sp, color = Color.White)
                            }
                        }
                    }
                }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                modifier = Modifier.testTag("tab_chat_directory"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(14.dp))
                        Text(
                            text = if (isPt) "Buscar" else "Search",
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.sp
                        )
                    }
                }
            )
        }

        when (selectedTab) {
            // TAB 0: CONVERSAS SALVAS (LOCAL + NUVEM)
            0 -> {
                if (conversations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = brand.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = if (isPt) "Nenhuma conversa ativa ainda" else "No active conversations yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isPt) "Pesquise usuários para conversar, enviar mídias e fazer ligações, ou entre na aba Live para discutir conteúdos ao vivo!"
                                else "Search users to chat, share media, and place calls, or open the Live tab for live study discussions!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { selectedTab = 3 },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.testTag("button_empty_go_to_search")
                                ) {
                                    Icon(Icons.Default.PersonSearch, contentDescription = null, modifier = Modifier.size(17.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isPt) "Pesquisar Usuários" else "Search Users", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { selectedTab = 1 },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.testTag("button_empty_go_to_live")
                                ) {
                                    Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(17.dp), tint = Color(0xFFE53935))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isPt) "Lives & Voz" else "Lives & Calls", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(conversations, key = { it.chatId }) { conv ->
                            val peerInfo = directoryUsers.firstOrNull { it.userId == conv.peerUserId }
                            var showCardMenu by remember(conv.chatId) { mutableStateOf(false) }

                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                                tonalElevation = 2.dp,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { onOpenConversation(conv.chatId) }
                                    .testTag("chat_conversation_item_${conv.chatId}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box {
                                        UserAvatar(
                                            photoUrl = conv.peerPhotoUrl,
                                            displayName = conv.peerDisplayName,
                                            size = 52.dp,
                                            borderWidth = 2.dp,
                                            borderColor = brand.primary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(13.dp)
                                                .align(Alignment.BottomEnd)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00E676))
                                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = conv.peerDisplayName,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                if (peerInfo != null) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = brand.primary.copy(alpha = 0.18f)
                                                    ) {
                                                        Text(
                                                            text = "Nv.${peerInfo.levelNumber}",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = brand.primary,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = formatChatTimestamp(conv.lastMessageAtMillis),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = brand.primary.copy(alpha = 0.85f),
                                                fontSize = 11.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = null,
                                                tint = Color(0xFF00E5FF),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = conv.lastMessageText.ifBlank {
                                                    if (isPt) "Conversa iniciada • toque para abrir" else "Conversation started • tap to open"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Box {
                                        IconButton(
                                            onClick = { showCardMenu = true },
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .testTag("button_conv_menu_${conv.chatId}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = if (isPt) "Opções da conversa" else "Conversation options",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = showCardMenu,
                                            onDismissRequest = { showCardMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF4CAF50)) },
                                                text = { Text(if (isPt) "Chamada de Voz" else "Voice Call") },
                                                onClick = {
                                                    showCardMenu = false
                                                    launchQuickCall(conv, ChatCallType.AUDIO)
                                                }
                                            )
                                            DropdownMenuItem(
                                                leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null, tint = brand.primary) },
                                                text = { Text(if (isPt) "Chamada de Vídeo" else "Video Call") },
                                                onClick = {
                                                    showCardMenu = false
                                                    launchQuickCall(conv, ChatCallType.VIDEO)
                                                }
                                            )
                                            DropdownMenuItem(
                                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                                text = { Text(if (isPt) "Deletar conversa" else "Delete conversation", color = MaterialTheme.colorScheme.error) },
                                                onClick = {
                                                    showCardMenu = false
                                                    conversationToDelete = conv
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 1: LIVES DE ESTUDO & CHAMADAS DIRETAS (VOZ / VÍDEO)
            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card principal para Iniciar Live de Estudo
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(listOf(Color(0xFFE53935), brand.primary))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(Color(0xFFE53935), brand.accent))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sensors,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isPt) "Live de Discussão & Estudos" else "Live Study & Discussion Room",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = if (isPt) "Abra uma sala ao vivo de áudio ou vídeo para debater Python, SQL, Java e tirar dúvidas em grupo."
                                            else "Start a live audio/video room to discuss Python, SQL, Java, and solve questions together.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Button(
                                    onClick = { showCreateLiveDialog = true },
                                    enabled = isFirebaseAuthenticated,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("button_start_live_study_room")
                                ) {
                                    Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isPt) "Iniciar Nova Live de Conteúdo" else "Start Live Study Room",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Salas Ao Vivo no Ar Agora
                    item {
                        Text(
                            text = if (isPt) "Salas Ao Vivo Agora (${activeLiveRooms.size})"
                            else "Live Rooms Active Now (${activeLiveRooms.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE53935)
                        )
                    }

                    if (activeLiveRooms.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = brand.primary)
                                    Text(
                                        text = if (isPt) "Nenhuma Live acontecendo neste momento. Clique acima para abrir uma sala de estudos ao vivo!"
                                        else "No Live rooms right now. Tap above to start a live discussion!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(activeLiveRooms, key = { it.roomId }) { room ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("live_room_card_${room.roomId}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE53935)) {
                                                Text(
                                                    text = if (isPt) "AO VIVO" else "LIVE",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Surface(shape = RoundedCornerShape(6.dp), color = brand.primary.copy(alpha = 0.2f)) {
                                                Text(
                                                    text = room.topicLabel,
                                                    color = brand.primaryLight,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (room.liveMode == LiveRoomMode.VIDEO) Icons.Default.Videocam else Icons.Default.GraphicEq,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Groups,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = "${room.participantCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Text(
                                        text = room.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (room.description.isNotBlank()) {
                                        Text(
                                            text = room.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            UserAvatar(
                                                photoUrl = room.hostPhotoUrl,
                                                displayName = room.hostName,
                                                size = 30.dp
                                            )
                                            Text(
                                                text = "Host: ${room.hostName}",
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Button(
                                            onClick = { launchJoinLiveWithPermissions(room) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = brand.primary),
                                            modifier = Modifier
                                                .minimumInteractiveComponentSize()
                                                .testTag("button_join_live_${room.roomId}")
                                        ) {
                                            Text(if (isPt) "Entrar na Live" else "Join Live", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Seção de Ligação Rápida 1-a-1 (Voz & Vídeo)
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isPt) "Ligação Direta (Voz ou Vídeo)" else "Direct Calls (Voice or Video)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = brand.primary
                        )
                    }

                    if (conversations.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isPt) "Adicione contatos na aba Buscar para fazer ligações individuais de voz ou vídeo."
                                        else "Connect with users in Search to place direct 1-on-1 voice or video calls.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    OutlinedButton(
                                        onClick = { selectedTab = 3 },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (isPt) "Buscar Usuários" else "Search Users", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        items(conversations, key = { "call_${it.chatId}" }) { conv ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(
                                        photoUrl = conv.peerPhotoUrl,
                                        displayName = conv.peerDisplayName,
                                        size = 40.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = conv.peerDisplayName,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isPt) "Toque para ligar agora" else "Tap to call now",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = { launchQuickCall(conv, ChatCallType.AUDIO) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("button_quick_voice_${conv.chatId}")
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = if (isPt) "Ligar Voz" else "Voice", modifier = Modifier.size(15.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isPt) "Voz" else "Voice", fontSize = 11.sp, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = { launchQuickCall(conv, ChatCallType.VIDEO) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = brand.primary),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("button_quick_video_${conv.chatId}")
                                    ) {
                                        Icon(Icons.Default.Videocam, contentDescription = if (isPt) "Ligar Vídeo" else "Video", modifier = Modifier.size(15.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isPt) "Vídeo" else "Video", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 2: SOLICITAÇÕES RECEBIDAS & ENVIADAS
            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = if (isPt) "Solicitações Recebidas (${incomingPending.size})"
                            else "Incoming Requests (${incomingPending.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = brand.primary
                        )
                    }
                    if (incomingPending.isEmpty()) {
                        item {
                            Text(
                                text = if (isPt) "Nenhuma solicitação aguardando sua aprovação."
                                else "No incoming requests waiting for approval.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(incomingPending, key = { it.requestId }) { req ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, brand.primary.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth().testTag("incoming_request_${req.requestId}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        UserAvatar(
                                            photoUrl = req.senderPhotoUrl,
                                            displayName = req.senderName,
                                            size = 44.dp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(req.senderName, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = if (isPt) "Quer iniciar uma conversa com você"
                                                else "Wants to start a conversation with you",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (req.introMessage.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Text(
                                                text = "\"${req.introMessage}\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onAcceptRequest(req) },
                                            modifier = Modifier.weight(1f).testTag("button_accept_request_${req.requestId}"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isPt) "Aceitar" else "Accept")
                                        }
                                        OutlinedButton(
                                            onClick = { onDeclineOrDeleteRequest(req.requestId) },
                                            modifier = Modifier.weight(1f).testTag("button_decline_request_${req.requestId}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isPt) "Recusar" else "Decline")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isPt) "Solicitações Enviadas (${outgoingRequests.size})"
                            else "Sent Requests (${outgoingRequests.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (outgoingRequests.isEmpty()) {
                        item {
                            Text(
                                text = if (isPt) "Nenhuma solicitação pendente enviada por você."
                                else "No outgoing requests.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(outgoingRequests, key = { it.requestId }) { req ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    UserAvatar(
                                        photoUrl = req.recipientPhotoUrl,
                                        displayName = req.recipientName,
                                        size = 40.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(req.recipientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = if (req.status == ChatRequestStatus.DECLINED) {
                                                if (isPt) "Recusada" else "Declined"
                                            } else {
                                                if (isPt) "Aguardando aceitação..." else "Waiting for acceptance..."
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(onClick = { onDeclineOrDeleteRequest(req.requestId) }) {
                                        Text(if (isPt) "Cancelar" else "Cancel", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAB 3: LISTA COMPLETA E INTERATIVA DE USUÁRIOS + ORDENAÇÃO DINÂMICA POR BUSCA
            3 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = brand.primary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = if (isPt) "Limpar busca" else "Clear search")
                                }
                            }
                        },
                        label = { Text(if (isPt) "Pesquisar usuário por nome ou nível..." else "Search user by name or level...") },
                        placeholder = {
                            Text(
                                if (isPt) "Digite para ordenar e destacar resultados..."
                                else "Type to rank & highlight matches..."
                            )
                        },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("chat_directory_search_input")
                    )

                    // Chips interativos de filtro e ordenação
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = directoryFilterMode == DirectoryFilterMode.ALL,
                                onClick = { directoryFilterMode = DirectoryFilterMode.ALL },
                                label = { Text(if (isPt) "Todos" else "All", fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_dir_filter_all")
                            )
                        }
                        item {
                            FilterChip(
                                selected = directoryFilterMode == DirectoryFilterMode.AVAILABLE,
                                onClick = { directoryFilterMode = DirectoryFilterMode.AVAILABLE },
                                label = { Text(if (isPt) "Novos" else "New", fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_dir_filter_available")
                            )
                        }
                        item {
                            FilterChip(
                                selected = directoryFilterMode == DirectoryFilterMode.CONNECTED,
                                onClick = { directoryFilterMode = DirectoryFilterMode.CONNECTED },
                                label = { Text(if (isPt) "Com Chat" else "Connected", fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_dir_filter_connected")
                            )
                        }
                        item {
                            FilterChip(
                                selected = directorySortMode == DirectorySortMode.HIGHEST_LEVEL && searchQuery.isBlank(),
                                onClick = {
                                    directorySortMode = if (directorySortMode == DirectorySortMode.ALPHABETICAL) {
                                        DirectorySortMode.HIGHEST_LEVEL
                                    } else {
                                        DirectorySortMode.ALPHABETICAL
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (directorySortMode == DirectorySortMode.HIGHEST_LEVEL) Icons.Default.Star else Icons.Default.SortByAlpha,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = when {
                                            searchQuery.isNotBlank() -> if (isPt) "Por relevância" else "Best match"
                                            directorySortMode == DirectorySortMode.HIGHEST_LEVEL -> if (isPt) "Maior Nível" else "Top Level"
                                            else -> if (isPt) "Ordem A–Z" else "A–Z"
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier.testTag("chip_dir_sort_toggle")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) {
                                if (isPt) "Todos os usuários (${filteredUsers.size})"
                                else "All registered users (${filteredUsers.size})"
                            } else {
                                if (isPt) "${filteredUsers.size} encontrados para \"$searchQuery\""
                                else "${filteredUsers.size} matches for \"$searchQuery\""
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isPt) "Toque p/ detalhes" else "Tap for details",
                            style = MaterialTheme.typography.labelSmall,
                            color = brand.primary
                        )
                    }

                    if (filteredUsers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when {
                                    !isFirebaseAuthenticated ->
                                        if (isPt) "Entre com Google para carregar a lista de usuários cadastrados."
                                        else "Sign in with Google to load registered users."
                                    searchQuery.isNotBlank() ->
                                        if (isPt) "Nenhum usuário encontrado para \"$searchQuery\"."
                                        else "No user found matching \"$searchQuery\"."
                                    else ->
                                        if (isPt) "Nenhum usuário encontrado neste filtro."
                                        else "No users match this filter yet."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredUsers, key = { it.userId }) { user ->
                                val pairId = if (!currentUserId.isNullOrBlank()) {
                                    ChatIdUtil.deterministicPairId(currentUserId, user.userId)
                                } else ""
                                val existingConv = conversations.firstOrNull {
                                    it.peerUserId == user.userId || (pairId.isNotEmpty() && it.chatId == pairId)
                                }
                                val existingReq = chatRequests.firstOrNull { it.requestId == pairId }
                                val leaderboardInfo = leaderboardEntries.firstOrNull { it.userId == user.userId }
                                val matchRange = remember(user.displayName, searchQuery) {
                                    ChatDirectorySearchHelper.findMatchRange(user.displayName, searchQuery)
                                }

                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f),
                                    border = BorderStroke(
                                        width = if (matchRange != null && matchRange.first == 0) 1.5.dp else 1.dp,
                                        color = if (matchRange != null && matchRange.first == 0) brand.primary.copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable { selectedDirectoryUserInfo = user }
                                        .testTag("directory_user_card_${user.userId}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        UserAvatar(
                                            photoUrl = user.photoUrl,
                                            displayName = user.displayName,
                                            size = 48.dp,
                                            borderWidth = 2.dp,
                                            borderColor = brand.primaryLight
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            val highlightedName = remember(user.displayName, matchRange, brand.primary) {
                                                buildAnnotatedString {
                                                    if (matchRange == null) {
                                                        append(user.displayName)
                                                    } else {
                                                        val s = matchRange.first.coerceIn(0, user.displayName.length)
                                                        val e = (matchRange.last + 1).coerceIn(s, user.displayName.length)
                                                        append(user.displayName.substring(0, s))
                                                        withStyle(
                                                            SpanStyle(
                                                                color = brand.primary,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                background = brand.primary.copy(alpha = 0.16f)
                                                            )
                                                        ) {
                                                            append(user.displayName.substring(s, e))
                                                        }
                                                        append(user.displayName.substring(e))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = highlightedName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = buildString {
                                                    append("Nível ${user.levelNumber}")
                                                    if (user.levelTitle.isNotBlank()) append(" • ${user.levelTitle}")
                                                    append(" • ${user.totalScore} XP")
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (leaderboardInfo != null && leaderboardInfo.maskedPhone.isNotBlank()) {
                                                Text(
                                                    text = "Tel: ${leaderboardInfo.maskedPhone}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = brand.primary.copy(alpha = 0.85f),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        when {
                                            existingConv != null -> {
                                                Button(
                                                    onClick = { onOpenConversation(existingConv.chatId) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier
                                                        .minimumInteractiveComponentSize()
                                                        .testTag("button_open_existing_chat_${user.userId}")
                                                ) {
                                                    Text(if (isPt) "Conversar" else "Chat", fontSize = 12.sp)
                                                }
                                            }
                                            existingReq != null && existingReq.status == ChatRequestStatus.PENDING &&
                                                !currentUserId.isNullOrBlank() && existingReq.isIncomingFor(currentUserId) -> {
                                                Button(
                                                    onClick = { onAcceptRequest(existingReq) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.minimumInteractiveComponentSize()
                                                ) {
                                                    Text(if (isPt) "Aceitar" else "Accept", fontSize = 12.sp)
                                                }
                                            }
                                            existingReq != null && existingReq.status == ChatRequestStatus.PENDING -> {
                                                OutlinedButton(
                                                    onClick = { onDeclineOrDeleteRequest(existingReq.requestId) },
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.minimumInteractiveComponentSize()
                                                ) {
                                                    Text(if (isPt) "Enviada" else "Sent", fontSize = 12.sp)
                                                }
                                            }
                                            else -> {
                                                Button(
                                                    onClick = {
                                                        requestIntroText = ""
                                                        requestTargetUser = user
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier
                                                        .minimumInteractiveComponentSize()
                                                        .testTag("button_request_chat_${user.userId}")
                                                ) {
                                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(15.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(if (isPt) "Solicitar" else "Request", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatThreadView(
    language: Language,
    currentUserId: String?,
    conversation: ChatConversationItem,
    peerProfile: ChatDirectoryUser?,
    peerLeaderboard: LeaderboardEntry?,
    messages: List<ChatMessageItem>,
    isUploading: Boolean,
    statusFeedback: String?,
    onDismissFeedback: () -> Unit,
    onBack: () -> Unit,
    onSendTextMessage: (String) -> Unit,
    onSendAttachment: (Uri, ChatMessageType, String) -> Unit,
    onEnsureMediaLocalPath: suspend (ChatMessageItem) -> String?,
    onOpenAttachmentExternal: (ChatMessageItem) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onDeleteConversation: () -> Unit,
    onStartCall: (ChatCallType) -> Unit,
    onCreateLiveRoom: (String, String, String, String, LiveRoomMode) -> Unit,
    activeCallBanner: (@Composable () -> Unit)? = null,
    activeLiveBanner: (@Composable () -> Unit)? = null
) {
    BackHandler { onBack() }

    val isPt = language == Language.PT
    val brand = LocalDevRoutineColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var draftText by remember { mutableStateOf("") }
    var pendingCallType by remember { mutableStateOf<ChatCallType?>(null) }
    var pendingLiveCreate by remember { mutableStateOf<CreateLiveParams?>(null) }
    var showDeleteConvDialog by remember { mutableStateOf(false) }
    var showPeerInfoDialog by remember { mutableStateOf(false) }
    var showThreadMenu by remember { mutableStateOf(false) }
    var showCreateLiveDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<ChatMessageItem?>(null) }
    var lightboxImageMsg by remember { mutableStateOf<ChatMessageItem?>(null) }
    var activeVideoPath by remember { mutableStateOf<String?>(null) }
    var activeVideoTitle by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    // Play-compliant Photo Picker (Images)
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onSendAttachment(uri, ChatMessageType.IMAGE, draftText)
            draftText = ""
        }
    }

    // Play-compliant Photo Picker (Videos)
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onSendAttachment(uri, ChatMessageType.VIDEO, draftText)
            draftText = ""
        }
    }

    // Play-compliant Storage Access Framework (Arbitrary Files)
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onSendAttachment(uri, ChatMessageType.FILE, draftText)
            draftText = ""
        }
    }

    // Runtime Call & Live Permission Launcher (RECORD_AUDIO + CAMERA)
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val callType = pendingCallType
        pendingCallType = null
        if (callType != null) {
            val audioOk = grants[Manifest.permission.RECORD_AUDIO] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            val camOk = callType != ChatCallType.VIDEO ||
                grants[Manifest.permission.CAMERA] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (audioOk && camOk) {
                onStartCall(callType)
            }
        }
        pendingLiveCreate?.let { params ->
            pendingLiveCreate = null
            onCreateLiveRoom(params.title, params.techId, params.techLabel, params.description, params.mode)
        }
    }

    fun launchCallWithPermissionCheck(callType: ChatCallType) {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.RECORD_AUDIO)
        }
        if (callType == ChatCallType.VIDEO &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            needed.add(Manifest.permission.CAMERA)
        }
        if (needed.isEmpty()) {
            onStartCall(callType)
        } else {
            pendingCallType = callType
            callPermissionLauncher.launch(needed.toTypedArray())
        }
    }

    fun launchLiveFromThreadWithPermissions(params: CreateLiveParams) {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.RECORD_AUDIO)
        }
        if (params.mode == LiveRoomMode.VIDEO &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            needed.add(Manifest.permission.CAMERA)
        }
        if (needed.isEmpty()) {
            onCreateLiveRoom(params.title, params.techId, params.techLabel, params.description, params.mode)
        } else {
            pendingLiveCreate = params
            callPermissionLauncher.launch(needed.toTypedArray())
        }
    }

    if (showCreateLiveDialog) {
        CreateLiveRoomDialog(
            language = language,
            initialTitle = if (isPt) "Live de Estudos com ${conversation.peerDisplayName}" else "Study Live with ${conversation.peerDisplayName}",
            onDismiss = { showCreateLiveDialog = false },
            onConfirmCreate = { title, techId, techLabel, desc, mode ->
                showCreateLiveDialog = false
                launchLiveFromThreadWithPermissions(
                    CreateLiveParams(title, techId, techLabel, desc, mode)
                )
            }
        )
    }

    if (showPeerInfoDialog) {
        PeerContactInfoDialog(
            language = language,
            conversation = conversation,
            peerProfile = peerProfile,
            peerLeaderboard = peerLeaderboard,
            messages = messages,
            onVoiceCall = {
                showPeerInfoDialog = false
                launchCallWithPermissionCheck(ChatCallType.AUDIO)
            },
            onVideoCall = {
                showPeerInfoDialog = false
                launchCallWithPermissionCheck(ChatCallType.VIDEO)
            },
            onStartLive = {
                showPeerInfoDialog = false
                showCreateLiveDialog = true
            },
            onDeleteConversation = {
                showPeerInfoDialog = false
                showDeleteConvDialog = true
            },
            onDismiss = { showPeerInfoDialog = false }
        )
    }

    if (showDeleteConvDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConvDialog = false },
            title = { Text(if (isPt) "Deletar conversa?" else "Delete conversation?") },
            text = {
                Text(
                    if (isPt) "Tem certeza? Todas as mensagens, fotos, vídeos e arquivos desta conversa com ${conversation.peerDisplayName} serão removidos."
                    else "All messages and cached media with ${conversation.peerDisplayName} will be deleted locally and from the cloud."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConvDialog = false
                        onDeleteConversation()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("button_confirm_delete_active_conv")
                ) {
                    Text(if (isPt) "Deletar" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConvDialog = false }) {
                    Text(if (isPt) "Cancelar" else "Cancel")
                }
            }
        )
    }

    messageToDelete?.let { msg ->
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text(if (isPt) "Apagar mensagem?" else "Delete message?") },
            text = {
                Text(
                    if (isPt) "Esta mensagem será removida localmente e da nuvem."
                    else "This message will be permanently removed from your device and the cloud."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMessage(msg.messageId)
                        messageToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("button_confirm_delete_message")
                ) {
                    Text(if (isPt) "Apagar" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text(if (isPt) "Cancelar" else "Cancel")
                }
            }
        )
    }

    lightboxImageMsg?.let { imgMsg ->
        Dialog(
            onDismissRequest = { lightboxImageMsg = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.94f))
            ) {
                ChatImageBitmapPreview(
                    message = imgMsg,
                    onEnsureLocal = onEnsureMediaLocalPath,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentScale = ContentScale.Fit
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { onOpenAttachmentExternal(imgMsg) }) {
                        Text(if (isPt) "Abrir fora" else "Open external", color = Color.White)
                    }
                    IconButton(onClick = { lightboxImageMsg = null }) {
                        Icon(Icons.Default.Close, contentDescription = if (isPt) "Fechar imagem" else "Close image", tint = Color.White)
                    }
                }
            }
        }
    }

    activeVideoPath?.let { path ->
        Dialog(
            onDismissRequest = { activeVideoPath = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeVideoTitle,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { activeVideoPath = null }) {
                            Icon(Icons.Default.Close, contentDescription = if (isPt) "Fechar vídeo" else "Close video", tint = Color.White)
                        }
                    }
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                val mc = MediaController(ctx)
                                mc.setAnchorView(this)
                                setMediaController(mc)
                                setVideoPath(path)
                                setOnPreparedListener { mp ->
                                    mp.isLooping = false
                                    start()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_chat_thread")
    ) {
        // Barras flutuantes de Chamada / Live ativa bem no topo (acima do ícone e foto do usuário)
        activeCallBanner?.invoke()
        activeLiveBanner?.invoke()

        // Cabeçalho estilo WhatsApp: Foto + Nome/Informações à esquerda e Live / Vídeo / Voz / Menu lá em cima
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 5.dp,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                brand.primary.copy(alpha = 0.14f),
                                Color.Transparent,
                                brand.accent.copy(alpha = 0.08f)
                            )
                        )
                    )
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.minimumInteractiveComponentSize().testTag("button_chat_back")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isPt) "Voltar" else "Back")
                }

                // Bloco clicável com foto do usuário, nome e informações (toque para ver perfil/números)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showPeerInfoDialog = true }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .testTag("button_thread_peer_info"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        UserAvatar(
                            photoUrl = conversation.peerPhotoUrl,
                            displayName = conversation.peerDisplayName,
                            size = 42.dp,
                            borderWidth = 1.8.dp,
                            borderColor = brand.primary
                        )
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF00E676))
                                .border(1.5.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = conversation.peerDisplayName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val infoSubtitle = remember(peerProfile, peerLeaderboard, isPt) {
                            buildString {
                                val lvl = peerProfile?.levelNumber ?: peerLeaderboard?.levelNumber
                                val xp = peerProfile?.totalScore ?: peerLeaderboard?.totalScore
                                val phone = peerLeaderboard?.maskedPhone.orEmpty()
                                if (lvl != null && xp != null) {
                                    append("Nv.$lvl • $xp XP")
                                } else {
                                    append(if (isPt) "Local & Nuvem" else "Local & Cloud")
                                }
                                if (phone.isNotBlank()) {
                                    append(" • $phone")
                                }
                            }
                        }
                        Text(
                            text = infoSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Opções de Live, Vídeo e Voz lá em cima ao lado do usuário
                IconButton(
                    onClick = { showCreateLiveDialog = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("button_thread_start_live")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = if (isPt) "Iniciar Live" else "Start Live",
                        tint = Color(0xFFE53935)
                    )
                }

                IconButton(
                    onClick = { launchCallWithPermissionCheck(ChatCallType.VIDEO) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("button_start_video_call")
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = if (isPt) "Chamada de vídeo" else "Video call",
                        tint = brand.primary
                    )
                }

                IconButton(
                    onClick = { launchCallWithPermissionCheck(ChatCallType.AUDIO) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("button_start_voice_call")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = if (isPt) "Ligação de voz" else "Voice call",
                        tint = Color(0xFF4CAF50)
                    )
                }

                Box {
                    IconButton(
                        onClick = { showThreadMenu = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("button_thread_more_options")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = if (isPt) "Mais opções" else "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = showThreadMenu,
                        onDismissRequest = { showThreadMenu = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = brand.primary) },
                            text = { Text(if (isPt) "Informações do usuário" else "User info & stats") },
                            onClick = {
                                showThreadMenu = false
                                showPeerInfoDialog = true
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.Sensors, contentDescription = null, tint = Color(0xFFE53935)) },
                            text = { Text(if (isPt) "Iniciar Live de Estudo" else "Start Study Live") },
                            onClick = {
                                showThreadMenu = false
                                showCreateLiveDialog = true
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            text = { Text(if (isPt) "Deletar conversa" else "Delete conversation", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showThreadMenu = false
                                showDeleteConvDialog = true
                            },
                            modifier = Modifier.testTag("button_delete_active_conversation")
                        )
                    }
                }
            }
        }

        if (isUploading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        AnimatedVisibility(visible = !statusFeedback.isNullOrBlank()) {
            Surface(color = brand.primary.copy(alpha = 0.16f), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(statusFeedback.orEmpty(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismissFeedback, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = if (isPt) "Fechar" else "Close", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Fundo estilo WhatsApp com padrão sutil + separadores por dia
        val dotColor = brand.primary.copy(alpha = 0.07f)
        val accentDotColor = brand.accent.copy(alpha = 0.05f)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .drawBehind {
                    val step = 36.dp.toPx()
                    var y = 14f
                    var rowIdx = 0
                    while (y < size.height) {
                        var x = if (rowIdx % 2 == 0) 14f else 32f
                        while (x < size.width) {
                            drawCircle(
                                color = if ((rowIdx + x.toInt()) % 3 == 0) accentDotColor else dotColor,
                                radius = 2.2.dp.toPx(),
                                center = Offset(x, y)
                            )
                            x += step
                        }
                        y += step
                        rowIdx++
                    }
                }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages.size, key = { idx -> messages[idx].messageId }) { index ->
                    val msg = messages[index]
                    val currentDayKey = formatMessageDatePill(msg.createdAtMillis, isPt)
                    val prevDayKey = if (index > 0) formatMessageDatePill(messages[index - 1].createdAtMillis, isPt) else ""
                    if (currentDayKey.isNotEmpty() && currentDayKey != prevDayKey) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            ) {
                                Text(
                                    text = currentDayKey,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    val isOwn = !currentUserId.isNullOrBlank() && msg.senderId == currentUserId
                    ChatMessageBubble(
                        message = msg,
                        isOwn = isOwn,
                        isPt = isPt,
                        onEnsureMediaLocalPath = onEnsureMediaLocalPath,
                        onClickImage = { lightboxImageMsg = msg },
                        onClickVideo = {
                            scope.launch {
                                val localPath = onEnsureMediaLocalPath(msg)
                                if (!localPath.isNullOrBlank()) {
                                    activeVideoTitle = msg.mediaName.ifBlank { if (isPt) "Vídeo" else "Video" }
                                    activeVideoPath = localPath
                                } else {
                                    onOpenAttachmentExternal(msg)
                                }
                            }
                        },
                        onClickFile = { onOpenAttachmentExternal(msg) },
                        onRequestDeleteMessage = { messageToDelete = msg }
                    )
                }
            }
        }

        // Composer Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                // Media attachment buttons row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("button_attach_image")
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = brand.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPt) "Imagem" else "Photo", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("button_attach_video")
                    ) {
                        Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp), tint = brand.accent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPt) "Vídeo" else "Video", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { filePicker.launch(arrayOf("*/*")) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("button_attach_file")
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPt) "Arquivo" else "File", fontSize = 11.sp)
                    }
                }

                // Text input + Send button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { if (it.length <= 4000) draftText = it },
                        placeholder = { Text(if (isPt) "Digite sua mensagem..." else "Type a message...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_message_input")
                    )
                    IconButton(
                        onClick = {
                            val clean = draftText.trim()
                            if (clean.isNotEmpty()) {
                                onSendTextMessage(clean)
                                draftText = ""
                            }
                        },
                        enabled = draftText.isNotBlank(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (draftText.isNotBlank()) brand.primary
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                            .testTag("button_send_chat_message")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = if (isPt) "Enviar mensagem" else "Send message",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageItem,
    isOwn: Boolean,
    isPt: Boolean,
    onEnsureMediaLocalPath: suspend (ChatMessageItem) -> String?,
    onClickImage: () -> Unit,
    onClickVideo: () -> Unit,
    onClickFile: () -> Unit,
    onRequestDeleteMessage: () -> Unit
) {
    val brand = LocalDevRoutineColors.current

    if (message.messageType == ChatMessageType.CALL) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (message.callType == "video") Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = null,
                        tint = brand.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(message.text, style = MaterialTheme.typography.labelMedium)
                    Text(
                        formatChatTimestamp(message.createdAtMillis),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onRequestDeleteMessage,
                        modifier = Modifier.size(24.dp).testTag("button_delete_msg_${message.messageId}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = if (isPt) "Apagar registro" else "Delete log",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwn) 16.dp else 4.dp,
                bottomEnd = if (isOwn) 4.dp else 16.dp
            ),
            color = if (isOwn) brand.primary.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
                1.dp,
                if (isOwn) brand.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            ),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .testTag("chat_message_${message.messageId}")
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                when (message.messageType) {
                    ChatMessageType.IMAGE -> {
                        ChatImageBitmapPreview(
                            message = message,
                            onEnsureLocal = onEnsureMediaLocalPath,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 240.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onClickImage() },
                            contentScale = ContentScale.Crop
                        )
                    }

                    ChatMessageType.VIDEO -> {
                        val thumbBitmap = remember(message.thumbnailDataUrl) {
                            decodeDataUrlBitmap(message.thumbnailDataUrl)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.8f))
                                .clickable { onClickVideo() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (thumbBitmap != null) {
                                Image(
                                    bitmap = thumbBitmap,
                                    contentDescription = message.mediaName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = if (isPt) "Reproduzir vídeo" else "Play video", tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)
                            ) {
                                Text(
                                    text = "${message.mediaName} • ${ChatMediaCodec.formatByteSize(message.mediaSizeBytes)}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    ChatMessageType.FILE -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClickFile() }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(brand.primary.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = brand.primary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = message.mediaName.ifBlank { if (isPt) "Arquivo" else "File" },
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${ChatMediaCodec.formatByteSize(message.mediaSizeBytes)} • ${if (isPt) "Toque para abrir" else "Tap to open"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    else -> Unit
                }

                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatBubbleTimeOnly(message.createdAtMillis),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isOwn) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (message.isSyncedToCloud) Icons.Default.DoneAll else Icons.Default.Schedule,
                            contentDescription = if (message.isSyncedToCloud) {
                                if (isPt) "Salvo na nuvem" else "Synced"
                            } else {
                                if (isPt) "Salvo local" else "Local"
                            },
                            tint = if (message.isSyncedToCloud) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onRequestDeleteMessage,
                        modifier = Modifier.size(26.dp).testTag("button_delete_msg_${message.messageId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = if (isPt) "Apagar mensagem" else "Delete message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatImageBitmapPreview(
    message: ChatMessageItem,
    onEnsureLocal: suspend (ChatMessageItem) -> String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var resolvedPath by remember(message.messageId, message.localFilePath) {
        mutableStateOf(message.localFilePath.takeIf { it.isNotBlank() && File(it).exists() })
    }
    var bitmap by remember(message.messageId, resolvedPath, message.inlineMediaDataUrl) {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(message.messageId, message.localFilePath, message.inlineMediaDataUrl) {
        if (resolvedPath == null && (message.inlineMediaDataUrl.isNotBlank() || message.chunkCount > 0)) {
            resolvedPath = onEnsureLocal(message)
        }
        val bmp = withContext(Dispatchers.IO) {
            val localFile = resolvedPath?.let { File(it) }?.takeIf { it.exists() }
            if (localFile != null) {
                BitmapFactory.decodeFile(localFile.absolutePath)?.asImageBitmap()
            } else if (message.inlineMediaDataUrl.isNotBlank()) {
                decodeDataUrlBitmap(message.inlineMediaDataUrl)
            } else null
        }
        bitmap = bmp
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = message.mediaName.ifBlank { "Imagem do chat" },
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp))
        }
    }
}

private fun decodeDataUrlBitmap(dataUrl: String): ImageBitmap? {
    if (dataUrl.isBlank()) return null
    val bytes = AvatarImageCodec.decodeDataUrlToBytes(dataUrl) ?: return null
    return try {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun formatChatTimestamp(millis: Long): String {
    if (millis <= 0L) return ""
    val df = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return df.format(Date(millis))
}

private fun formatBubbleTimeOnly(millis: Long): String {
    if (millis <= 0L) return ""
    val df = SimpleDateFormat("HH:mm", Locale.getDefault())
    return df.format(Date(millis))
}

private fun formatMessageDatePill(millis: Long, isPt: Boolean): String {
    if (millis <= 0L) return ""
    val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val today = df.format(Date())
    val yesterday = df.format(Date(System.currentTimeMillis() - 86_400_000L))
    val target = df.format(Date(millis))
    return when (target) {
        today -> if (isPt) "Hoje" else "Today"
        yesterday -> if (isPt) "Ontem" else "Yesterday"
        else -> target
    }
}

private data class CreateLiveParams(
    val title: String,
    val techId: String,
    val techLabel: String,
    val description: String,
    val mode: LiveRoomMode
)

@Composable
private fun PeerContactInfoDialog(
    language: Language,
    conversation: ChatConversationItem,
    peerProfile: ChatDirectoryUser?,
    peerLeaderboard: LeaderboardEntry?,
    messages: List<ChatMessageItem>,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit,
    onStartLive: () -> Unit,
    onDeleteConversation: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val brand = LocalDevRoutineColors.current
    val mediaCount = remember(messages) {
        messages.count {
            it.messageType == ChatMessageType.IMAGE ||
                it.messageType == ChatMessageType.VIDEO ||
                it.messageType == ChatMessageType.FILE
        }
    }
    val level = peerProfile?.levelNumber ?: peerLeaderboard?.levelNumber ?: 1
    val levelTitle = peerProfile?.levelTitle?.ifBlank { null }
        ?: (if (isPt) peerLeaderboard?.levelTitlePt else peerLeaderboard?.levelTitleEn).orEmpty()
    val xp = peerProfile?.totalScore ?: peerLeaderboard?.totalScore ?: 0
    val maskedPhone = peerLeaderboard?.maskedPhone.orEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserAvatar(
                    photoUrl = conversation.peerPhotoUrl,
                    displayName = conversation.peerDisplayName,
                    size = 82.dp,
                    borderWidth = 3.dp,
                    borderColor = brand.primary
                )
                Text(
                    text = conversation.peerDisplayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (levelTitle.isNotBlank()) "Nível $level • $levelTitle" else "Nível $level",
                    style = MaterialTheme.typography.labelMedium,
                    color = brand.primary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Opções rápidas de Chamada e Live
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onVoiceCall,
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                            Text(if (isPt) "Voz" else "Voice", fontSize = 11.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = onVideoCall,
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = brand.primary, modifier = Modifier.size(18.dp))
                            Text(if (isPt) "Vídeo" else "Video", fontSize = 11.sp)
                        }
                    }
                    OutlinedButton(
                        onClick = onStartLive,
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                            Text("Live", fontSize = 11.sp)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InfoKeyValueRow(
                            label = if (isPt) "Telefone" else "Phone",
                            value = maskedPhone.ifBlank { if (isPt) "Conta Google" else "Google Account" }
                        )
                        InfoKeyValueRow(
                            label = "XP Total",
                            value = "$xp XP"
                        )
                        if (peerLeaderboard != null) {
                            InfoKeyValueRow(
                                label = if (isPt) "Sequência / Selos" else "Streak / Badges",
                                value = "${peerLeaderboard.streakDays}d • ${peerLeaderboard.badgesCount} selos"
                            )
                        }
                        InfoKeyValueRow(
                            label = if (isPt) "Mensagens / Mídias" else "Messages / Media",
                            value = "${messages.size} • $mediaCount mídias"
                        )
                        InfoKeyValueRow(
                            label = "ID",
                            value = conversation.peerUserId.take(12)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isPt) "Fechar" else "Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDeleteConversation) {
                Text(if (isPt) "Deletar conversa" else "Delete chat", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun DirectoryUserProfileDialog(
    language: Language,
    user: ChatDirectoryUser,
    leaderboardEntry: LeaderboardEntry?,
    hasConversation: Boolean,
    hasIncomingRequest: Boolean,
    hasOutgoingRequest: Boolean,
    onOpenChat: () -> Unit,
    onRequestChat: () -> Unit,
    onAcceptRequest: () -> Unit,
    onCancelRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val brand = LocalDevRoutineColors.current
    val phone = leaderboardEntry?.maskedPhone.orEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserAvatar(
                    photoUrl = user.photoUrl,
                    displayName = user.displayName,
                    size = 78.dp,
                    borderWidth = 2.5.dp,
                    borderColor = brand.primary
                )
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (user.levelTitle.isNotBlank()) "Nível ${user.levelNumber} • ${user.levelTitle}"
                    else "Nível ${user.levelNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    color = brand.primary
                )
            }
        },
        text = {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoKeyValueRow(label = "XP Total", value = "${user.totalScore} XP")
                    InfoKeyValueRow(
                        label = if (isPt) "Telefone" else "Phone",
                        value = phone.ifBlank { if (isPt) "Privado (Google)" else "Private (Google)" }
                    )
                    if (leaderboardEntry != null) {
                        InfoKeyValueRow(
                            label = if (isPt) "Sequência / Selos" else "Streak / Badges",
                            value = "${leaderboardEntry.streakDays}d • ${leaderboardEntry.badgesCount} selos"
                        )
                    }
                    InfoKeyValueRow(label = "ID", value = user.userId.take(12))
                }
            }
        },
        confirmButton = {
            when {
                hasConversation -> {
                    Button(onClick = onOpenChat) {
                        Text(if (isPt) "Abrir Conversa" else "Open Chat")
                    }
                }
                hasIncomingRequest -> {
                    Button(
                        onClick = onAcceptRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text(if (isPt) "Aceitar Solicitação" else "Accept Request")
                    }
                }
                hasOutgoingRequest -> {
                    OutlinedButton(onClick = onCancelRequest) {
                        Text(if (isPt) "Cancelar Solicitação" else "Cancel Request")
                    }
                }
                else -> {
                    Button(onClick = onRequestChat) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPt) "Solicitar Conversa" else "Request Chat")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isPt) "Fechar" else "Close")
            }
        }
    )
}

@Composable
private fun InfoKeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun CreateLiveRoomDialog(
    language: Language,
    initialTitle: String = "",
    onDismiss: () -> Unit,
    onConfirmCreate: (String, String, String, String, LiveRoomMode) -> Unit
) {
    val isPt = language == Language.PT
    val brand = LocalDevRoutineColors.current
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf("") }
    var selectedTechId by remember { mutableStateOf("general") }
    var selectedTechLabel by remember { mutableStateOf(if (isPt) "Geral" else "General") }
    var selectedMode by remember { mutableStateOf(LiveRoomMode.VIDEO) }

    val topics = remember(isPt) {
        listOf("general" to (if (isPt) "Geral" else "General")) +
            Technology.entries.map { it.id to it.displayName }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    tint = Color(0xFFE53935)
                )
                Text(
                    text = if (isPt) "Iniciar Live de Estudos" else "Start Live Study Room",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 64) title = it },
                    singleLine = true,
                    label = { Text(if (isPt) "Título da Live" else "Live Room Title") },
                    placeholder = {
                        Text(
                            if (isPt) "Ex: Revisão de JOINs em SQL"
                            else "Ex: SQL JOINs Deep Dive"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 160) description = it },
                    maxLines = 2,
                    label = { Text(if (isPt) "Pauta / Assunto (opcional)" else "Agenda / Notes (optional)") },
                    placeholder = {
                        Text(
                            if (isPt) "O que vamos discutir hoje..."
                            else "What we will cover today..."
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_desc_input")
                )

                Text(
                    text = if (isPt) "Conteúdo / Tecnologia" else "Topic / Technology",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(topics, key = { it.first }) { (id, label) ->
                        val isSelected = selectedTechId == id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedTechId = id
                                selectedTechLabel = label
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = brand.primary.copy(alpha = 0.22f),
                                selectedLabelColor = brand.primary
                            )
                        )
                    }
                }

                Text(
                    text = if (isPt) "Formato da Live" else "Room Mode",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedMode == LiveRoomMode.VIDEO,
                        onClick = { selectedMode = LiveRoomMode.VIDEO },
                        leadingIcon = {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text(if (isPt) "Vídeo + Chat" else "Video + Chat") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_live_mode_video")
                    )
                    FilterChip(
                        selected = selectedMode == LiveRoomMode.AUDIO,
                        onClick = { selectedMode = LiveRoomMode.AUDIO },
                        leadingIcon = {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text(if (isPt) "Áudio + Chat" else "Audio + Chat") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chip_live_mode_audio")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.trim().ifBlank {
                        if (isPt) "Live de $selectedTechLabel" else "$selectedTechLabel Live Study"
                    }
                    onConfirmCreate(
                        finalTitle,
                        selectedTechId,
                        selectedTechLabel,
                        description.trim(),
                        selectedMode
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("button_confirm_create_live")
            ) {
                Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isPt) "Abrir Live" else "Go Live", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isPt) "Cancelar" else "Cancel")
            }
        }
    )
}


