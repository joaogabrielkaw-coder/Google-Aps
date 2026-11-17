package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ChatConversationItem
import com.example.model.ChatMessageItem
import com.example.model.ChatMessageType

@Entity(tableName = "chat_conversations")
data class ChatConversationEntity(
    @PrimaryKey val chatId: String,
    val ownerUserId: String,
    val peerUserId: String,
    val peerDisplayName: String,
    val peerPhotoUrl: String,
    val lastMessageText: String,
    val lastMessageType: String,
    val lastSenderId: String,
    val lastMessageAtMillis: Long,
    val unreadCount: Int = 0
) {
    fun toModel(): ChatConversationItem = ChatConversationItem(
        chatId = chatId,
        peerUserId = peerUserId,
        peerDisplayName = peerDisplayName,
        peerPhotoUrl = peerPhotoUrl,
        lastMessageText = lastMessageText,
        lastMessageType = ChatMessageType.fromRaw(lastMessageType),
        lastSenderId = lastSenderId,
        lastMessageAtMillis = lastMessageAtMillis,
        unreadCount = unreadCount
    )
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val messageId: String,
    val chatId: String,
    val ownerUserId: String,
    val senderId: String,
    val senderName: String,
    val messageType: String,
    val text: String,
    val mediaName: String = "",
    val mediaMimeType: String = "",
    val mediaSizeBytes: Long = 0L,
    val localFilePath: String = "",
    val inlineMediaDataUrl: String = "",
    val thumbnailDataUrl: String = "",
    val chunkCount: Int = 0,
    val callType: String = "",
    val callDurationSec: Int = 0,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isSyncedToCloud: Boolean = true
) {
    fun toModel(): ChatMessageItem = ChatMessageItem(
        messageId = messageId,
        chatId = chatId,
        senderId = senderId,
        senderName = senderName,
        messageType = ChatMessageType.fromRaw(messageType),
        text = text,
        mediaName = mediaName,
        mediaMimeType = mediaMimeType,
        mediaSizeBytes = mediaSizeBytes,
        localFilePath = localFilePath,
        inlineMediaDataUrl = inlineMediaDataUrl,
        thumbnailDataUrl = thumbnailDataUrl,
        chunkCount = chunkCount,
        callType = callType,
        callDurationSec = callDurationSec,
        createdAtMillis = createdAtMillis,
        isSyncedToCloud = isSyncedToCloud
    )
}
