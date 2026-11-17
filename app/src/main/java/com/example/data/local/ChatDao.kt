package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_conversations WHERE ownerUserId = :ownerUserId ORDER BY lastMessageAtMillis DESC")
    fun observeConversations(ownerUserId: String): Flow<List<ChatConversationEntity>>

    @Query("SELECT * FROM chat_conversations WHERE chatId = :chatId LIMIT 1")
    suspend fun getConversationById(chatId: String): ChatConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ChatConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversations(conversations: List<ChatConversationEntity>)

    @Query("DELETE FROM chat_conversations WHERE chatId = :chatId")
    suspend fun deleteConversationById(chatId: String)

    @Query("DELETE FROM chat_conversations WHERE ownerUserId = :ownerUserId AND chatId NOT IN (:activeChatIds)")
    suspend fun pruneRemovedConversations(ownerUserId: String, activeChatIds: List<String>)

    @Query("DELETE FROM chat_conversations WHERE ownerUserId = :ownerUserId")
    suspend fun deleteAllConversationsForOwner(ownerUserId: String)

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY createdAtMillis ASC")
    fun observeMessages(chatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId")
    suspend fun getMessagesForChat(chatId: String): List<ChatMessageEntity>

    @Query("SELECT * FROM chat_messages WHERE messageId = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET localFilePath = :localFilePath WHERE messageId = :messageId")
    suspend fun updateMessageLocalFilePath(messageId: String, localFilePath: String)

    @Query("DELETE FROM chat_messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM chat_messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: String)

    @Query("DELETE FROM chat_messages WHERE chatId = :chatId AND messageId NOT IN (:activeMessageIds)")
    suspend fun pruneRemovedMessages(chatId: String, activeMessageIds: List<String>)
}
