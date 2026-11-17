package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.ChatDao
import com.example.data.local.ChatConversationEntity
import com.example.data.local.ChatMessageEntity
import com.example.model.ActiveCallSession
import com.example.model.ChatCallStatus
import com.example.model.ChatCallType
import com.example.model.ChatConversationItem
import com.example.model.ChatDirectoryUser
import com.example.model.ChatIdUtil
import com.example.model.ChatMessageItem
import com.example.model.ChatMessageType
import com.example.model.ChatRequestItem
import com.example.model.ChatRequestStatus
import com.example.model.LiveMessageType
import com.example.model.LiveRoomMessage
import com.example.model.LiveRoomMode
import com.example.model.LiveRoomStatus
import com.example.model.LiveSignalPayload
import com.example.model.LiveStudyRoom
import com.example.util.ChatMediaCodec
import com.example.util.OperationType
import com.example.util.PreparedChatMedia
import com.example.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private const val TAG = "ChatRepository"

data class IceCandidatePayload(
    val candidateId: String = "",
    val senderId: String = "",
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val candidate: String = ""
)

class ChatRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val chatDao: ChatDao,
    private val appContext: Context
) {
    constructor(context: Context, auth: FirebaseAuth, chatDao: ChatDao) : this(
        db = FirebaseFirestore.getInstance(context.applicationContext.getString(R.string.firestore_database_id)),
        auth = auth,
        chatDao = chatDao,
        appContext = context.applicationContext
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Google Sign-In required before accessing cloud chat.")
    }

    fun observeLocalConversations(ownerUserId: String): Flow<List<ChatConversationItem>> {
        if (ownerUserId.isBlank()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return chatDao.observeConversations(ownerUserId).map { list ->
            list.map { it.toModel() }
        }
    }

    fun observeLocalMessages(chatId: String): Flow<List<ChatMessageItem>> {
        if (chatId.isBlank()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return chatDao.observeMessages(chatId).map { list ->
            list.map { it.toModel() }
        }
    }

    suspend fun publishDirectoryProfile(
        displayName: String,
        photoUrl: String,
        levelNumber: Int,
        levelTitle: String,
        totalScore: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext Result.failure(
            IllegalStateException("Google Sign-In required to publish chat directory profile.")
        )
        val path = "chat_directory/$uid"
        val cleanName = displayName.trim().ifBlank { "Dev" }.take(100)
        val payload = mutableMapOf<String, Any>(
            "userId" to uid,
            "displayName" to cleanName,
            "displayNameLower" to cleanName.lowercase(),
            "photoUrl" to photoUrl.take(59_000),
            "levelNumber" to levelNumber.coerceIn(1, 100),
            "levelTitle" to levelTitle.take(100),
            "totalScore" to totalScore.coerceAtLeast(0),
            "isPublic" to true,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            db.collection("chat_directory").document(uid).set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    fun observeDirectoryUsers(): Flow<List<ChatDirectoryUser>> = flow {
        val uid = requireUserId()
        val path = "chat_directory"
        emitAll(
            db.collection("chat_directory")
                .whereEqualTo("isPublic", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val userId = doc.getString("userId") ?: doc.id
                        val name = doc.getString("displayName") ?: return@mapNotNull null
                        ChatDirectoryUser(
                            userId = userId,
                            displayName = name,
                            displayNameLower = doc.getString("displayNameLower") ?: name.lowercase(),
                            photoUrl = doc.getString("photoUrl") ?: "",
                            levelNumber = (doc.get("levelNumber") as? Number)?.toInt() ?: 1,
                            levelTitle = doc.getString("levelTitle") ?: "",
                            totalScore = (doc.get("totalScore") as? Number)?.toInt() ?: 0
                        )
                    }.sortedBy { it.displayNameLower }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    fun observeChatRequests(): Flow<List<ChatRequestItem>> = flow {
        val uid = requireUserId()
        val path = "chat_requests"
        emitAll(
            db.collection("chat_requests")
                .whereArrayContains("participantIds", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc -> parseChatRequest(doc) }
                        .sortedByDescending { it.updatedAtMillis }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun sendChatRequest(
        senderName: String,
        senderPhotoUrl: String,
        recipient: ChatDirectoryUser,
        introMessage: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        require(recipient.userId.isNotBlank() && recipient.userId != uid) {
            "Recipient userId must differ from current user"
        }
        val reqId = ChatIdUtil.deterministicPairId(uid, recipient.userId)
        val chatId = reqId
        val docRef = db.collection("chat_requests").document(reqId)
        try {
            val existing = docRef.get().await()
            val participants = ChatIdUtil.sortedParticipantIds(uid, recipient.userId)
            val cleanSender = senderName.trim().ifBlank { "Dev" }.take(100)
            val cleanRecipient = recipient.displayName.trim().ifBlank { "Dev" }.take(100)
            val cleanIntro = introMessage.trim().take(300)

            if (existing.exists()) {
                val currentStatus = existing.getString("status")
                if (currentStatus == "accepted") {
                    return@withContext Result.success(reqId)
                }
                val currentSender = existing.getString("senderId").orEmpty()
                if (currentSender == uid) {
                    val updatePayload = mapOf(
                        "status" to ChatRequestStatus.PENDING.raw,
                        "senderName" to cleanSender,
                        "senderPhotoUrl" to senderPhotoUrl.take(59_000),
                        "introMessage" to cleanIntro,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                    docRef.update(updatePayload).await()
                    return@withContext Result.success(reqId)
                } else {
                    // Other user already sent us a request; delete & recreate or notify
                    return@withContext Result.success(reqId)
                }
            }

            val payload = mapOf(
                "senderId" to uid,
                "senderName" to cleanSender,
                "senderPhotoUrl" to senderPhotoUrl.take(59_000),
                "recipientId" to recipient.userId,
                "recipientName" to cleanRecipient,
                "recipientPhotoUrl" to recipient.photoUrl.take(59_000),
                "participantIds" to participants,
                "status" to ChatRequestStatus.PENDING.raw,
                "introMessage" to cleanIntro,
                "chatId" to chatId,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(reqId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun acceptChatRequest(
        request: ChatRequestItem,
        accepterName: String,
        accepterPhotoUrl: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val reqRef = db.collection("chat_requests").document(request.requestId)
        val chatId = request.chatId.ifBlank { ChatIdUtil.deterministicPairId(request.senderId, request.recipientId) }
        val chatRef = db.collection("chats").document(chatId)
        val participants = ChatIdUtil.sortedParticipantIds(request.senderId, request.recipientId)
        val cleanAccepter = accepterName.trim().ifBlank { request.recipientName }.take(100)

        try {
            val reqUpdate = mapOf(
                "status" to ChatRequestStatus.ACCEPTED.raw,
                "recipientName" to cleanAccepter,
                "recipientPhotoUrl" to accepterPhotoUrl.take(59_000),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            reqRef.update(reqUpdate).await()

            val namesMap = mapOf(
                request.senderId to request.senderName.ifBlank { "Dev" },
                request.recipientId to cleanAccepter
            )
            val photosMap = mapOf(
                request.senderId to request.senderPhotoUrl.take(59_000),
                request.recipientId to accepterPhotoUrl.take(59_000)
            )
            val preview = request.introMessage.ifBlank {
                "Solicitação de conversa aceita!"
            }.take(500)

            val chatSnapshot = chatRef.get().await()
            if (!chatSnapshot.exists()) {
                val chatPayload = mapOf(
                    "participantIds" to participants,
                    "participantNames" to namesMap,
                    "participantPhotos" to photosMap,
                    "lastMessageText" to preview,
                    "lastMessageType" to ChatMessageType.TEXT.raw,
                    "lastSenderId" to uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                chatRef.set(chatPayload).await()
            } else {
                val chatUpdate = mapOf(
                    "participantNames" to namesMap,
                    "participantPhotos" to photosMap,
                    "lastMessageText" to preview,
                    "lastMessageType" to ChatMessageType.TEXT.raw,
                    "lastSenderId" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                chatRef.update(chatUpdate).await()
            }

            // Also persist locally in Room right away
            chatDao.upsertConversation(
                ChatConversationEntity(
                    chatId = chatId,
                    ownerUserId = uid,
                    peerUserId = request.peerIdFor(uid),
                    peerDisplayName = request.peerNameFor(uid),
                    peerPhotoUrl = request.peerPhotoFor(uid),
                    lastMessageText = preview,
                    lastMessageType = ChatMessageType.TEXT.raw,
                    lastSenderId = uid,
                    lastMessageAtMillis = System.currentTimeMillis()
                )
            )
            Result.success(chatId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, chatRef.path)
            Result.failure(e)
        }
    }

    suspend fun declineOrDeleteRequest(requestId: String): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val reqRef = db.collection("chat_requests").document(requestId)
        try {
            reqRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, reqRef.path)
            Result.failure(e)
        }
    }

    fun observeAndCacheCloudConversations(): Flow<List<ChatConversationItem>> = flow {
        val uid = requireUserId()
        val path = "chats"
        emitAll(
            db.collection("chats")
                .whereArrayContains("participantIds", uid)
                .snapshots()
                .map { snapshot ->
                    val entities = snapshot.documents.mapNotNull { doc ->
                        parseConversationEntity(doc, uid)
                    }.sortedByDescending { it.lastMessageAtMillis }

                    val ids = entities.map { it.chatId }
                    if (ids.isEmpty()) {
                        chatDao.deleteAllConversationsForOwner(uid)
                    } else {
                        chatDao.upsertConversations(entities)
                        chatDao.pruneRemovedConversations(uid, ids)
                    }
                    entities.map { it.toModel() }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    fun observeAndCacheCloudMessages(chatId: String): Flow<List<ChatMessageItem>> = flow {
        val uid = requireUserId()
        val path = "chats/$chatId/messages"
        emitAll(
            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .whereArrayContains("participantIds", uid)
                .snapshots()
                .map { snapshot ->
                    val messages = snapshot.documents.mapNotNull { doc ->
                        parseMessageEntity(doc, chatId, uid)
                    }.sortedBy { it.createdAtMillis }

                    val merged = messages.map { entity ->
                        val existingLocal = chatDao.getMessageById(entity.messageId)
                        val preservedLocalPath = existingLocal?.localFilePath
                            ?.takeIf { it.isNotBlank() && File(it).exists() }
                            ?: entity.localFilePath
                        entity.copy(localFilePath = preservedLocalPath)
                    }

                    if (merged.isNotEmpty()) {
                        chatDao.upsertMessages(merged)
                        chatDao.pruneRemovedMessages(chatId, merged.map { it.messageId })
                    } else {
                        chatDao.deleteMessagesForChat(chatId)
                    }
                    merged.map { it.toModel() }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun downloadAttachmentIfNeeded(message: ChatMessageItem): String? = withContext(Dispatchers.IO) {
        if (message.localFilePath.isNotBlank() && File(message.localFilePath).exists()) {
            return@withContext message.localFilePath
        }
        val uid = auth.currentUser?.uid ?: return@withContext null

        val savedPath: String? = when {
            message.inlineMediaDataUrl.isNotBlank() -> {
                ChatMediaCodec.saveBase64PayloadToLocalFile(
                    context = appContext,
                    chatId = message.chatId,
                    messageId = message.messageId,
                    fileName = message.mediaName.ifBlank { "media_${message.messageId}" },
                    dataUrlOrBase64 = message.inlineMediaDataUrl
                )
            }
            message.chunkCount > 0 -> {
                val chunksPath = "chats/${message.chatId}/messages/${message.messageId}/chunks"
                try {
                    val snap = db.collection("chats")
                        .document(message.chatId)
                        .collection("messages")
                        .document(message.messageId)
                        .collection("chunks")
                        .whereArrayContains("participantIds", uid)
                        .get()
                        .await()
                    val combinedBase64 = snap.documents
                        .mapNotNull { doc ->
                            val idx = (doc.get("chunkIndex") as? Number)?.toInt() ?: return@mapNotNull null
                            val data = doc.getString("dataBase64") ?: return@mapNotNull null
                            idx to data
                        }
                        .sortedBy { it.first }
                        .joinToString(separator = "") { it.second }

                    if (combinedBase64.isBlank()) null
                    else ChatMediaCodec.saveBase64PayloadToLocalFile(
                        context = appContext,
                        chatId = message.chatId,
                        messageId = message.messageId,
                        fileName = message.mediaName.ifBlank { "media_${message.messageId}" },
                        dataUrlOrBase64 = combinedBase64
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    handleFirestoreError(e, OperationType.LIST, chunksPath)
                    null
                }
            }
            else -> null
        }

        if (!savedPath.isNullOrBlank()) {
            chatDao.updateMessageLocalFilePath(message.messageId, savedPath)
        }
        savedPath
    }

    suspend fun sendTextMessage(
        chatId: String,
        peerUserId: String,
        senderName: String,
        text: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val cleanText = text.trim().take(4000)
        if (cleanText.isEmpty()) return@withContext Result.failure(IllegalArgumentException("Empty message"))
        val messageId = UUID.randomUUID().toString()
        val participants = ChatIdUtil.sortedParticipantIds(uid, peerUserId)
        val cleanSender = senderName.trim().ifBlank { "Dev" }.take(100)

        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        val chatRef = db.collection("chats").document(chatId)
        val nowMillis = System.currentTimeMillis()

        // Local immediate cache in Room
        chatDao.upsertMessage(
            ChatMessageEntity(
                messageId = messageId,
                chatId = chatId,
                ownerUserId = uid,
                senderId = uid,
                senderName = cleanSender,
                messageType = ChatMessageType.TEXT.raw,
                text = cleanText,
                createdAtMillis = nowMillis,
                isSyncedToCloud = false
            )
        )

        val msgPayload = mapOf(
            "chatId" to chatId,
            "senderId" to uid,
            "senderName" to cleanSender,
            "participantIds" to participants,
            "messageType" to ChatMessageType.TEXT.raw,
            "text" to cleanText,
            "createdAt" to FieldValue.serverTimestamp()
        )
        return@withContext try {
            msgRef.set(msgPayload).await()
            chatRef.update(
                mapOf(
                    "lastMessageText" to cleanText.take(500),
                    "lastMessageType" to ChatMessageType.TEXT.raw,
                    "lastSenderId" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            chatDao.upsertMessage(
                ChatMessageEntity(
                    messageId = messageId,
                    chatId = chatId,
                    ownerUserId = uid,
                    senderId = uid,
                    senderName = cleanSender,
                    messageType = ChatMessageType.TEXT.raw,
                    text = cleanText,
                    createdAtMillis = nowMillis,
                    isSyncedToCloud = true
                )
            )
            Result.success(messageId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendMediaMessage(
        chatId: String,
        peerUserId: String,
        senderName: String,
        messageType: ChatMessageType,
        caption: String,
        media: PreparedChatMedia,
        preGeneratedMessageId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val participants = ChatIdUtil.sortedParticipantIds(uid, peerUserId)
        val cleanSender = senderName.trim().ifBlank { "Dev" }.take(100)
        val cleanCaption = caption.trim().take(4000)
        val nowMillis = System.currentTimeMillis()
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(preGeneratedMessageId)
        val chatRef = db.collection("chats").document(chatId)

        val localEntity = ChatMessageEntity(
            messageId = preGeneratedMessageId,
            chatId = chatId,
            ownerUserId = uid,
            senderId = uid,
            senderName = cleanSender,
            messageType = messageType.raw,
            text = cleanCaption,
            mediaName = media.fileName.take(255),
            mediaMimeType = media.mimeType.take(120),
            mediaSizeBytes = media.sizeBytes,
            localFilePath = media.localFilePath,
            inlineMediaDataUrl = media.inlineDataUrl,
            thumbnailDataUrl = media.thumbnailDataUrl,
            chunkCount = media.base64Chunks.size,
            createdAtMillis = nowMillis,
            isSyncedToCloud = false
        )
        chatDao.upsertMessage(localEntity)

        if (media.exceedsCloudTransferLimit) {
            return@withContext Result.failure(
                IllegalArgumentException("FILE_TOO_LARGE_FOR_FIRESTORE")
            )
        }

        val payload = mutableMapOf<String, Any>(
            "chatId" to chatId,
            "senderId" to uid,
            "senderName" to cleanSender,
            "participantIds" to participants,
            "messageType" to messageType.raw,
            "text" to cleanCaption,
            "mediaName" to media.fileName.take(255),
            "mediaMimeType" to media.mimeType.take(120),
            "mediaSizeBytes" to media.sizeBytes,
            "chunkCount" to media.base64Chunks.size,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (media.inlineDataUrl.isNotBlank()) {
            payload["inlineMediaDataUrl"] = media.inlineDataUrl
        }
        if (media.thumbnailDataUrl.isNotBlank()) {
            payload["thumbnailDataUrl"] = media.thumbnailDataUrl
        }

        return@withContext try {
            msgRef.set(payload).await()
            media.base64Chunks.forEachIndexed { index, chunk ->
                val chunkPayload = mapOf(
                    "senderId" to uid,
                    "participantIds" to participants,
                    "chunkIndex" to index,
                    "dataBase64" to chunk,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                msgRef.collection("chunks").document("chunk_$index").set(chunkPayload).await()
            }

            val previewLabel = when (messageType) {
                ChatMessageType.IMAGE -> cleanCaption.ifBlank { "📷 Foto: ${media.fileName}" }
                ChatMessageType.VIDEO -> cleanCaption.ifBlank { "🎬 Vídeo: ${media.fileName}" }
                ChatMessageType.FILE -> cleanCaption.ifBlank { "📎 Arquivo: ${media.fileName}" }
                else -> cleanCaption
            }.take(500)

            chatRef.update(
                mapOf(
                    "lastMessageText" to previewLabel,
                    "lastMessageType" to messageType.raw,
                    "lastSenderId" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            chatDao.upsertMessage(localEntity.copy(isSyncedToCloud = true))
            Result.success(preGeneratedMessageId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun recordCallLogMessage(
        chatId: String,
        peerUserId: String,
        senderName: String,
        callType: ChatCallType,
        durationSeconds: Int,
        summaryText: String
    ) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val messageId = UUID.randomUUID().toString()
        val participants = ChatIdUtil.sortedParticipantIds(uid, peerUserId)
        val cleanSender = senderName.trim().ifBlank { "Dev" }.take(100)
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        val chatRef = db.collection("chats").document(chatId)
        try {
            msgRef.set(
                mapOf(
                    "chatId" to chatId,
                    "senderId" to uid,
                    "senderName" to cleanSender,
                    "participantIds" to participants,
                    "messageType" to ChatMessageType.CALL.raw,
                    "text" to summaryText.take(400),
                    "callType" to callType.raw,
                    "callDurationSec" to durationSeconds.coerceAtLeast(0),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            chatRef.update(
                mapOf(
                    "lastMessageText" to summaryText.take(500),
                    "lastMessageType" to ChatMessageType.CALL.raw,
                    "lastSenderId" to uid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
        }
    }

    suspend fun deleteMessage(chatId: String, messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val local = chatDao.getMessageById(messageId)
        if (local != null && local.localFilePath.isNotBlank()) {
            try { File(local.localFilePath).delete() } catch (_: Exception) {}
        }
        chatDao.deleteMessageById(messageId)

        val uid = auth.currentUser?.uid ?: return@withContext Result.success(Unit)
        val msgRef = db.collection("chats").document(chatId).collection("messages").document(messageId)
        return@withContext try {
            val chunks = msgRef.collection("chunks")
                .whereArrayContains("participantIds", uid)
                .get()
                .await()
            chunks.documents.forEach { it.reference.delete().await() }
            msgRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteConversation(chatId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val localMessages = chatDao.getMessagesForChat(chatId)
        localMessages.forEach { msg ->
            if (msg.localFilePath.isNotBlank()) {
                try { File(msg.localFilePath).delete() } catch (_: Exception) {}
            }
        }
        chatDao.deleteMessagesForChat(chatId)
        chatDao.deleteConversationById(chatId)

        val uid = auth.currentUser?.uid ?: return@withContext Result.success(Unit)
        val chatRef = db.collection("chats").document(chatId)
        return@withContext try {
            val msgs = chatRef.collection("messages")
                .whereArrayContains("participantIds", uid)
                .get()
                .await()
            msgs.documents.forEach { doc ->
                val chunks = doc.reference.collection("chunks")
                    .whereArrayContains("participantIds", uid)
                    .get()
                    .await()
                chunks.documents.forEach { it.reference.delete().await() }
                doc.reference.delete().await()
            }
            chatRef.delete().await()
            try {
                db.collection("chat_requests").document(chatId).delete().await()
            } catch (_: Exception) {}
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, chatRef.path)
            Result.failure(e)
        }
    }

    // --- Call Signaling (Firestore call_sessions) ---

    fun observeCallSessions(): Flow<List<ActiveCallSession>> = flow {
        val uid = requireUserId()
        val path = "call_sessions"
        emitAll(
            db.collection("call_sessions")
                .whereArrayContains("participantIds", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc -> parseCallSession(doc, uid) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun createCallSession(
        callId: String,
        chatId: String,
        callerName: String,
        callerPhotoUrl: String,
        calleeId: String,
        calleeName: String,
        callType: ChatCallType,
        offerSdp: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val callRef = db.collection("call_sessions").document(callId)
        val participants = ChatIdUtil.sortedParticipantIds(uid, calleeId)
        val payload = mapOf(
            "chatId" to chatId,
            "callerId" to uid,
            "callerName" to callerName.trim().ifBlank { "Dev" }.take(100),
            "callerPhotoUrl" to callerPhotoUrl.take(59_000),
            "calleeId" to calleeId,
            "calleeName" to calleeName.trim().ifBlank { "Dev" }.take(100),
            "participantIds" to participants,
            "callType" to callType.raw,
            "status" to ChatCallStatus.RINGING.raw,
            "offerSdp" to offerSdp.take(34_000),
            "answerSdp" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            callRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, callRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateCallSessionStatus(
        callId: String,
        status: ChatCallStatus,
        answerSdp: String? = null,
        offerSdp: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        requireUserId()
        val callRef = db.collection("call_sessions").document(callId)
        val updates = mutableMapOf<String, Any>(
            "status" to status.raw,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (answerSdp != null) updates["answerSdp"] = answerSdp.take(34_000)
        if (offerSdp != null) updates["offerSdp"] = offerSdp.take(34_000)
        try {
            callRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, callRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendIceCandidate(
        callId: String,
        peerUserId: String,
        sdpMid: String,
        sdpMLineIndex: Int,
        candidate: String
    ) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        val participants = ChatIdUtil.sortedParticipantIds(uid, peerUserId)
        val candId = UUID.randomUUID().toString()
        val ref = db.collection("call_sessions").document(callId).collection("candidates").document(candId)
        try {
            ref.set(
                mapOf(
                    "senderId" to uid,
                    "participantIds" to participants,
                    "sdpMid" to sdpMid.take(64),
                    "sdpMLineIndex" to sdpMLineIndex.coerceIn(0, 32),
                    "candidate" to candidate.take(2048),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
        }
    }

    fun observeIceCandidates(callId: String): Flow<List<IceCandidatePayload>> = flow {
        val uid = requireUserId()
        val path = "call_sessions/$callId/candidates"
        emitAll(
            db.collection("call_sessions")
                .document(callId)
                .collection("candidates")
                .whereArrayContains("participantIds", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val senderId = doc.getString("senderId") ?: return@mapNotNull null
                        val cand = doc.getString("candidate") ?: return@mapNotNull null
                        IceCandidatePayload(
                            candidateId = doc.id,
                            senderId = senderId,
                            sdpMid = doc.getString("sdpMid") ?: "0",
                            sdpMLineIndex = (doc.get("sdpMLineIndex") as? Number)?.toInt() ?: 0,
                            candidate = cand
                        )
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    private fun parseChatRequest(doc: DocumentSnapshot): ChatRequestItem? {
        return try {
            val senderId = doc.getString("senderId") ?: return null
            val recipientId = doc.getString("recipientId") ?: return null
            ChatRequestItem(
                requestId = doc.id,
                chatId = doc.getString("chatId") ?: ChatIdUtil.deterministicPairId(senderId, recipientId),
                senderId = senderId,
                senderName = doc.getString("senderName") ?: "Dev",
                senderPhotoUrl = doc.getString("senderPhotoUrl") ?: "",
                recipientId = recipientId,
                recipientName = doc.getString("recipientName") ?: "Dev",
                recipientPhotoUrl = doc.getString("recipientPhotoUrl") ?: "",
                status = ChatRequestStatus.fromRaw(doc.getString("status")),
                introMessage = doc.getString("introMessage") ?: "",
                createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                updatedAtMillis = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Skipping malformed chat_request ${doc.id}: ${e.message}")
            null
        }
    }

    private fun parseConversationEntity(doc: DocumentSnapshot, currentUid: String): ChatConversationEntity? {
        return try {
            val participants = (doc.get("participantIds") as? List<*>)?.mapNotNull { it?.toString() } ?: return null
            val peerId = participants.firstOrNull { it != currentUid } ?: return null
            val names = doc.get("participantNames") as? Map<*, *>
            val photos = doc.get("participantPhotos") as? Map<*, *>
            ChatConversationEntity(
                chatId = doc.id,
                ownerUserId = currentUid,
                peerUserId = peerId,
                peerDisplayName = names?.get(peerId)?.toString()?.ifBlank { "Dev" } ?: "Dev",
                peerPhotoUrl = photos?.get(peerId)?.toString() ?: "",
                lastMessageText = doc.getString("lastMessageText") ?: "",
                lastMessageType = doc.getString("lastMessageType") ?: "text",
                lastSenderId = doc.getString("lastSenderId") ?: "",
                lastMessageAtMillis = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Skipping malformed chat ${doc.id}: ${e.message}")
            null
        }
    }

    private fun parseMessageEntity(doc: DocumentSnapshot, chatId: String, ownerUid: String): ChatMessageEntity? {
        return try {
            ChatMessageEntity(
                messageId = doc.id,
                chatId = doc.getString("chatId") ?: chatId,
                ownerUserId = ownerUid,
                senderId = doc.getString("senderId") ?: "",
                senderName = doc.getString("senderName") ?: "Dev",
                messageType = doc.getString("messageType") ?: "text",
                text = doc.getString("text") ?: "",
                mediaName = doc.getString("mediaName") ?: "",
                mediaMimeType = doc.getString("mediaMimeType") ?: "",
                mediaSizeBytes = (doc.get("mediaSizeBytes") as? Number)?.toLong() ?: 0L,
                localFilePath = "",
                inlineMediaDataUrl = doc.getString("inlineMediaDataUrl") ?: "",
                thumbnailDataUrl = doc.getString("thumbnailDataUrl") ?: "",
                chunkCount = (doc.get("chunkCount") as? Number)?.toInt() ?: 0,
                callType = doc.getString("callType") ?: "",
                callDurationSec = (doc.get("callDurationSec") as? Number)?.toInt() ?: 0,
                createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                isSyncedToCloud = true
            )
        } catch (e: Exception) {
            Log.w(TAG, "Skipping malformed message ${doc.id}: ${e.message}")
            null
        }
    }

    private fun parseCallSession(doc: DocumentSnapshot, currentUid: String): ActiveCallSession? {
        return try {
            val callerId = doc.getString("callerId") ?: return null
            val calleeId = doc.getString("calleeId") ?: return null
            ActiveCallSession(
                callId = doc.id,
                chatId = doc.getString("chatId") ?: "",
                callerId = callerId,
                callerName = doc.getString("callerName") ?: "Dev",
                callerPhotoUrl = doc.getString("callerPhotoUrl") ?: "",
                calleeId = calleeId,
                calleeName = doc.getString("calleeName") ?: "Dev",
                callType = ChatCallType.fromRaw(doc.getString("callType")),
                status = ChatCallStatus.fromRaw(doc.getString("status")),
                offerSdp = doc.getString("offerSdp") ?: "",
                answerSdp = doc.getString("answerSdp") ?: "",
                isOutgoing = callerId == currentUid
            )
        } catch (e: Exception) {
            null
        }
    }

    // --- Salas Ao Vivo (Live de Estudo & Discussão de Conteúdos) ---

    fun observeActiveLiveRooms(): Flow<List<LiveStudyRoom>> = flow {
        val uid = requireUserId()
        val path = "live_rooms"
        emitAll(
            db.collection("live_rooms")
                .whereEqualTo("status", LiveRoomStatus.LIVE.raw)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc -> parseLiveRoom(doc) }
                        .sortedByDescending { it.updatedAtMillis }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun createLiveRoom(
        title: String,
        topicTechId: String,
        topicLabel: String,
        description: String,
        liveMode: LiveRoomMode,
        hostName: String,
        hostPhotoUrl: String
    ): Result<LiveStudyRoom> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val roomId = "live_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val cleanTitle = title.trim().ifBlank { "Live de Estudos" }.take(120)
        val cleanHost = hostName.trim().ifBlank { "Dev" }.take(100)
        val cleanTechId = topicTechId.trim().ifBlank { "general" }.take(40)
        val cleanTechLabel = topicLabel.trim().ifBlank { "Geral" }.take(60)
        val cleanDesc = description.trim().take(300)
        val nowMillis = System.currentTimeMillis()
        val roomRef = db.collection("live_rooms").document(roomId)

        val payload = mapOf(
            "hostId" to uid,
            "hostName" to cleanHost,
            "hostPhotoUrl" to hostPhotoUrl.take(59_000),
            "title" to cleanTitle,
            "topicTechId" to cleanTechId,
            "topicLabel" to cleanTechLabel,
            "description" to cleanDesc,
            "liveMode" to liveMode.raw,
            "status" to LiveRoomStatus.LIVE.raw,
            "participantIds" to listOf(uid),
            "participantNames" to mapOf(uid to cleanHost),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return@withContext try {
            roomRef.set(payload).await()
            Result.success(
                LiveStudyRoom(
                    roomId = roomId,
                    hostId = uid,
                    hostName = cleanHost,
                    hostPhotoUrl = hostPhotoUrl.take(59_000),
                    title = cleanTitle,
                    topicTechId = cleanTechId,
                    topicLabel = cleanTechLabel,
                    description = cleanDesc,
                    liveMode = liveMode,
                    status = LiveRoomStatus.LIVE,
                    participantIds = listOf(uid),
                    participantNames = mapOf(uid to cleanHost),
                    createdAtMillis = nowMillis,
                    updatedAtMillis = nowMillis
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, roomRef.path)
            Result.failure(e)
        }
    }

    suspend fun joinLiveRoom(roomId: String, userName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val cleanName = userName.trim().ifBlank { "Dev" }.take(100)
        val roomRef = db.collection("live_rooms").document(roomId)
        try {
            val snap = roomRef.get().await()
            if (!snap.exists()) return@withContext Result.failure(IllegalStateException("Live room ended"))
            val currentIds = (snap.get("participantIds") as? List<*>)
                ?.mapNotNull { it?.toString() }
                ?.toMutableList() ?: mutableListOf()
            if (!currentIds.contains(uid) && currentIds.size < 25) {
                currentIds.add(uid)
            }
            val currentNames = ((snap.get("participantNames") as? Map<*, *>)
                ?.mapNotNull { (k, v) -> if (k != null && v != null) k.toString() to v.toString() else null }
                ?.toMap() ?: emptyMap()).toMutableMap()
            currentNames[uid] = cleanName

            roomRef.update(
                mapOf(
                    "participantIds" to currentIds,
                    "participantNames" to currentNames,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, roomRef.path)
            Result.failure(e)
        }
    }

    suspend fun leaveOrEndLiveRoom(roomId: String, isHost: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext Result.success(Unit)
        val roomRef = db.collection("live_rooms").document(roomId)
        try {
            if (isHost) {
                roomRef.update(
                    mapOf(
                        "status" to LiveRoomStatus.ENDED.raw,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val snap = roomRef.get().await()
                if (snap.exists() && snap.getString("status") == LiveRoomStatus.LIVE.raw) {
                    val hostId = snap.getString("hostId").orEmpty()
                    val currentIds = (snap.get("participantIds") as? List<*>)
                        ?.mapNotNull { it?.toString() }
                        ?.filter { it != uid || it == hostId }
                        ?: listOf(hostId)
                    val currentNames = ((snap.get("participantNames") as? Map<*, *>)
                        ?.mapNotNull { (k, v) ->
                            val ks = k?.toString()
                            if (ks != null && v != null && (ks != uid || ks == hostId)) ks to v.toString() else null
                        }?.toMap() ?: emptyMap())
                    if (currentIds.isNotEmpty()) {
                        roomRef.update(
                            mapOf(
                                "participantIds" to currentIds,
                                "participantNames" to currentNames,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, roomRef.path)
            Result.failure(e)
        }
    }

    fun observeLiveMessages(roomId: String): Flow<List<LiveRoomMessage>> = flow {
        val uid = requireUserId()
        val path = "live_rooms/$roomId/messages"
        emitAll(
            db.collection("live_rooms")
                .document(roomId)
                .collection("messages")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val senderId = doc.getString("senderId") ?: return@mapNotNull null
                        LiveRoomMessage(
                            messageId = doc.id,
                            roomId = doc.getString("roomId") ?: roomId,
                            senderId = senderId,
                            senderName = doc.getString("senderName") ?: "Dev",
                            senderPhotoUrl = doc.getString("senderPhotoUrl") ?: "",
                            text = doc.getString("text") ?: "",
                            messageType = LiveMessageType.fromRaw(doc.getString("messageType")),
                            createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                        )
                    }.sortedBy { it.createdAtMillis }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun sendLiveMessage(
        roomId: String,
        senderName: String,
        senderPhotoUrl: String,
        text: String,
        messageType: LiveMessageType = LiveMessageType.TEXT
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val cleanText = text.trim().take(1000)
        if (cleanText.isEmpty()) return@withContext Result.failure(IllegalArgumentException("Empty live message"))
        val msgId = UUID.randomUUID().toString()
        val ref = db.collection("live_rooms").document(roomId).collection("messages").document(msgId)
        try {
            ref.set(
                mapOf(
                    "roomId" to roomId,
                    "senderId" to uid,
                    "senderName" to senderName.trim().ifBlank { "Dev" }.take(100),
                    "senderPhotoUrl" to senderPhotoUrl.take(59_000),
                    "text" to cleanText,
                    "messageType" to messageType.raw,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            Result.failure(e)
        }
    }

    suspend fun sendLiveSignal(
        roomId: String,
        toUid: String,
        type: String,
        sdp: String = "",
        sdpMid: String = "0",
        sdpMLineIndex: Int = 0,
        candidate: String = ""
    ) = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext
        if (toUid.isBlank() || toUid == uid) return@withContext
        val signalId = UUID.randomUUID().toString()
        val ref = db.collection("live_rooms").document(roomId).collection("signals").document(signalId)
        val payload = mutableMapOf<String, Any>(
            "fromUid" to uid,
            "toUid" to toUid,
            "peerPair" to ChatIdUtil.sortedParticipantIds(uid, toUid),
            "type" to type,
            "createdAt" to FieldValue.serverTimestamp()
        )
        if (sdp.isNotEmpty()) payload["sdp"] = sdp.take(34_000)
        if (type == "ice") {
            payload["sdpMid"] = sdpMid.take(64)
            payload["sdpMLineIndex"] = sdpMLineIndex.coerceIn(0, 32)
            payload["candidate"] = candidate.take(2048)
        }
        try {
            ref.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
        }
    }

    fun observeLiveSignals(roomId: String): Flow<List<LiveSignalPayload>> = flow {
        val uid = requireUserId()
        val path = "live_rooms/$roomId/signals"
        emitAll(
            db.collection("live_rooms")
                .document(roomId)
                .collection("signals")
                .whereArrayContains("peerPair", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        val fromUid = doc.getString("fromUid") ?: return@mapNotNull null
                        val toUid = doc.getString("toUid") ?: return@mapNotNull null
                        val type = doc.getString("type") ?: return@mapNotNull null
                        LiveSignalPayload(
                            signalId = doc.id,
                            fromUid = fromUid,
                            toUid = toUid,
                            type = type,
                            sdp = doc.getString("sdp") ?: "",
                            sdpMid = doc.getString("sdpMid") ?: "0",
                            sdpMLineIndex = (doc.get("sdpMLineIndex") as? Number)?.toInt() ?: 0,
                            candidate = doc.getString("candidate") ?: ""
                        )
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<com.google.firebase.firestore.FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    private fun parseLiveRoom(doc: DocumentSnapshot): LiveStudyRoom? {
        return try {
            val hostId = doc.getString("hostId") ?: return null
            val ids = (doc.get("participantIds") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf(hostId)
            val names = (doc.get("participantNames") as? Map<*, *>)
                ?.mapNotNull { (k, v) -> if (k != null && v != null) k.toString() to v.toString() else null }
                ?.toMap() ?: emptyMap()
            LiveStudyRoom(
                roomId = doc.id,
                hostId = hostId,
                hostName = doc.getString("hostName") ?: "Dev",
                hostPhotoUrl = doc.getString("hostPhotoUrl") ?: "",
                title = doc.getString("title") ?: "Live de Estudos",
                topicTechId = doc.getString("topicTechId") ?: "general",
                topicLabel = doc.getString("topicLabel") ?: "Geral",
                description = doc.getString("description") ?: "",
                liveMode = LiveRoomMode.fromRaw(doc.getString("liveMode")),
                status = LiveRoomStatus.fromRaw(doc.getString("status")),
                participantIds = ids,
                participantNames = names,
                createdAtMillis = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                updatedAtMillis = doc.getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
}
