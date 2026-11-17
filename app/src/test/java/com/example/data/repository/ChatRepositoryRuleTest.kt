package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.ChatConversationEntity
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.model.ChatCallStatus
import com.example.model.ChatCallType
import com.example.model.ChatDirectoryUser
import com.example.model.ChatIdUtil
import com.example.model.ChatRequestStatus
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ChatRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private fun newRepo(dao: ChatDao = InMemoryChatDao()): ChatRepository =
        ChatRepository(firestore, auth, dao, appContext)

    @Test
    fun publishDirectoryProfile_andObserveDirectoryUsers_succeedsForSignedInUsers(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = newRepo()

        val publishRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.publishDirectoryProfile(
                displayName = "Alice Dev",
                photoUrl = "preset:python",
                levelNumber = 3,
                levelTitle = "Dev Júnior",
                totalScore = 650
            )
        }
        assertTrue("Publish failed: ${publishRes.exceptionOrNull()}", publishRes.isSuccess)

        val users = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeDirectoryUsers().first { list -> list.any { it.userId == aliceUid } }
        }
        val alice = users.first { it.userId == aliceUid }
        assertEquals("Alice Dev", alice.displayName)
        assertEquals("alice dev", alice.displayNameLower)
    }

    @Test
    fun observeDirectoryUsers_unauthenticatedUser_failsWithPermissionDenied(): Unit = runBlocking {
        auth.signOut()
        val repo = newRepo()

        val ex = runCatching {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeDirectoryUsers().first()
            }
        }.exceptionOrNull()

        assertNotNull(ex)
        assertTrue(
            "Expected IllegalStateException or PERMISSION_DENIED, got $ex",
            ex is IllegalStateException ||
                ex!!.unwrapFirestoreException()?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
        )
    }

    @Test
    fun chatRequestAcceptance_messagingAndDeletion_succeedsEndToEnd(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)

        // Sign back in as Alice to publish and initiate request
        signInTestUser(ALICE_EMAIL)
        val aliceDao = InMemoryChatDao()
        val aliceRepo = newRepo(aliceDao)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.publishDirectoryProfile("Alice", "", 2, "Aprendiz", 300).getOrThrow()
        }

        // Clean any leftover state for this pair before testing
        val pairId = ChatIdUtil.deterministicPairId(aliceUid, bobUid)
        runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                aliceRepo.deleteConversation(pairId)
                aliceRepo.declineOrDeleteRequest(pairId)
            }
        }

        val bobDir = ChatDirectoryUser(
            userId = bobUid,
            displayName = "Bob",
            displayNameLower = "bob",
            photoUrl = ""
        )
        val reqId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.sendChatRequest("Alice", "", bobDir, "Oi Bob, vamos estudar SQL!").getOrThrow()
        }
        assertEquals(pairId, reqId)

        // Bob signs in, sees incoming pending request, and accepts it
        signInTestUser(BOB_EMAIL)
        val bobDao = InMemoryChatDao()
        val bobRepo = newRepo(bobDao)
        val pendingReq = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeChatRequests().first { list ->
                list.any { it.requestId == pairId && it.status == ChatRequestStatus.PENDING }
            }.first { it.requestId == pairId }
        }
        assertEquals(aliceUid, pendingReq.senderId)
        assertEquals(bobUid, pendingReq.recipientId)

        val acceptedChatId = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.acceptChatRequest(pendingReq, "Bob", "").getOrThrow()
        }
        assertEquals(pairId, acceptedChatId)

        // Bob sends a text message and caches in Room/DAO
        val msgId = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.sendTextMessage(pairId, aliceUid, "Bob", "Bora estudar JOINs!").getOrThrow()
        }

        // Alice sees conversation and message in cloud and caches locally
        signInTestUser(ALICE_EMAIL)
        val aliceConvs = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeAndCacheCloudConversations().first { list -> list.any { it.chatId == pairId } }
        }
        assertTrue(aliceConvs.any { it.chatId == pairId })

        val aliceMsgs = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeAndCacheCloudMessages(pairId).first { list -> list.any { it.messageId == msgId } }
        }
        assertEquals("Bora estudar JOINs!", aliceMsgs.first { it.messageId == msgId }.text)
        assertTrue(aliceDao.getMessagesForChat(pairId).any { it.messageId == msgId })

        // Alice deletes the single message
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.deleteMessage(pairId, msgId).getOrThrow()
        }
        assertFalse(aliceDao.getMessagesForChat(pairId).any { it.messageId == msgId })

        // Alice deletes the entire conversation
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.deleteConversation(pairId).getOrThrow()
        }
        assertTrue(aliceDao.getMessagesForChat(pairId).isEmpty())
    }

    @Test
    fun charlieCrossUserAccess_toAliceBobChatAndCall_failsWithPermissionDenied(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)
        val pairId = ChatIdUtil.deterministicPairId(aliceUid, bobUid)

        signInTestUser(ALICE_EMAIL)
        val aliceRepo = newRepo()
        val bobDir = ChatDirectoryUser(userId = bobUid, displayName = "Bob", displayNameLower = "bob")
        runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                aliceRepo.declineOrDeleteRequest(pairId)
            }
        }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.sendChatRequest("Alice", "", bobDir, "Private").getOrThrow()
        }

        val callId = "call_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createCallSession(
                callId = callId,
                chatId = pairId,
                callerName = "Alice",
                callerPhotoUrl = "",
                calleeId = bobUid,
                calleeName = "Bob",
                callType = ChatCallType.VIDEO,
                offerSdp = "v=0\r\no=- 1 1 IN IP4 127.0.0.1\r\n"
            ).getOrThrow()
        }

        // Bob can update call status to accepted
        signInTestUser(BOB_EMAIL)
        val bobRepo = newRepo()
        val acceptRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.updateCallSessionStatus(
                callId = callId,
                status = ChatCallStatus.ACCEPTED,
                answerSdp = "v=0\r\no=- 2 2 IN IP4 127.0.0.1\r\n"
            )
        }
        assertTrue("Bob accept call failed: ${acceptRes.exceptionOrNull()}", acceptRes.isSuccess)

        // Charlie (non-participant) tries to read or tamper with the chat_request and call_session
        signInTestUser(CHARLIE_EMAIL)
        val reqEx = runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("chat_requests").document(pairId).get().await()
            }
        }.exceptionOrNull()
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            reqEx?.unwrapFirestoreException()?.code
        )

        val callEx = runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("call_sessions").document(callId).get().await()
            }
        }.exceptionOrNull()
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            callEx?.unwrapFirestoreException()?.code
        )

        // Clean up call & request as Alice
        signInTestUser(ALICE_EMAIL)
        runCatching {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("call_sessions").document(callId).delete().await()
                aliceRepo.declineOrDeleteRequest(pairId)
            }
        }
    }

    private class InMemoryChatDao : ChatDao {
        private val conversations = linkedMapOf<String, ChatConversationEntity>()
        private val convFlow = MutableStateFlow<List<ChatConversationEntity>>(emptyList())
        private val messages = linkedMapOf<String, ChatMessageEntity>()
        private val msgFlow = MutableStateFlow<List<ChatMessageEntity>>(emptyList())

        override fun observeConversations(ownerUserId: String): Flow<List<ChatConversationEntity>> =
            convFlow.map { list ->
                list.filter { it.ownerUserId == ownerUserId }.sortedByDescending { it.lastMessageAtMillis }
            }

        override suspend fun getConversationById(chatId: String): ChatConversationEntity? = conversations[chatId]

        override suspend fun upsertConversation(conversation: ChatConversationEntity) {
            conversations[conversation.chatId] = conversation
            convFlow.value = conversations.values.toList()
        }

        override suspend fun upsertConversations(conversations: List<ChatConversationEntity>) {
            conversations.forEach { this.conversations[it.chatId] = it }
            convFlow.value = this.conversations.values.toList()
        }

        override suspend fun deleteConversationById(chatId: String) {
            conversations.remove(chatId)
            convFlow.value = conversations.values.toList()
        }

        override suspend fun pruneRemovedConversations(ownerUserId: String, activeChatIds: List<String>) {
            val active = activeChatIds.toSet()
            conversations.entries.removeIf { it.value.ownerUserId == ownerUserId && it.key !in active }
            convFlow.value = conversations.values.toList()
        }

        override suspend fun deleteAllConversationsForOwner(ownerUserId: String) {
            conversations.entries.removeIf { it.value.ownerUserId == ownerUserId }
            convFlow.value = conversations.values.toList()
        }

        override fun observeMessages(chatId: String): Flow<List<ChatMessageEntity>> =
            msgFlow.map { list ->
                list.filter { it.chatId == chatId }.sortedBy { it.createdAtMillis }
            }

        override suspend fun getMessagesForChat(chatId: String): List<ChatMessageEntity> =
            messages.values.filter { it.chatId == chatId }

        override suspend fun getMessageById(messageId: String): ChatMessageEntity? = messages[messageId]

        override suspend fun upsertMessage(message: ChatMessageEntity) {
            messages[message.messageId] = message
            msgFlow.value = messages.values.toList()
        }

        override suspend fun upsertMessages(messages: List<ChatMessageEntity>) {
            messages.forEach { this.messages[it.messageId] = it }
            msgFlow.value = this.messages.values.toList()
        }

        override suspend fun updateMessageLocalFilePath(messageId: String, localFilePath: String) {
            messages[messageId]?.let {
                messages[messageId] = it.copy(localFilePath = localFilePath)
                msgFlow.value = messages.values.toList()
            }
        }

        override suspend fun deleteMessageById(messageId: String) {
            messages.remove(messageId)
            msgFlow.value = messages.values.toList()
        }

        override suspend fun deleteMessagesForChat(chatId: String) {
            messages.entries.removeIf { it.value.chatId == chatId }
            msgFlow.value = messages.values.toList()
        }

        override suspend fun pruneRemovedMessages(chatId: String, activeMessageIds: List<String>) {
            val active = activeMessageIds.toSet()
            messages.entries.removeIf { it.value.chatId == chatId && it.key !in active }
            msgFlow.value = messages.values.toList()
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_chat@test.com"
        const val BOB_EMAIL = "bob_chat@test.com"
        const val CHARLIE_EMAIL = "charlie_chat@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
