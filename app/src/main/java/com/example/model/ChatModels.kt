package com.example.model

enum class ChatRequestStatus(val raw: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    DECLINED("declined");

    companion object {
        fun fromRaw(raw: String?): ChatRequestStatus =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: PENDING
    }
}

enum class ChatMessageType(val raw: String) {
    TEXT("text"),
    IMAGE("image"),
    VIDEO("video"),
    FILE("file"),
    CALL("call");

    companion object {
        fun fromRaw(raw: String?): ChatMessageType =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: TEXT
    }
}

enum class ChatCallType(val raw: String) {
    AUDIO("audio"),
    VIDEO("video");

    companion object {
        fun fromRaw(raw: String?): ChatCallType =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: AUDIO
    }
}

enum class ChatCallStatus(val raw: String) {
    RINGING("ringing"),
    ACCEPTED("accepted"),
    DECLINED("declined"),
    ENDED("ended"),
    MISSED("missed");

    companion object {
        fun fromRaw(raw: String?): ChatCallStatus =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: ENDED
    }
}

data class ChatDirectoryUser(
    val userId: String = "",
    val displayName: String = "",
    val displayNameLower: String = "",
    val photoUrl: String = "",
    val levelNumber: Int = 1,
    val levelTitle: String = "",
    val totalScore: Int = 0
)

data class ChatRequestItem(
    val requestId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val recipientId: String = "",
    val recipientName: String = "",
    val recipientPhotoUrl: String = "",
    val status: ChatRequestStatus = ChatRequestStatus.PENDING,
    val introMessage: String = "",
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
) {
    fun isIncomingFor(currentUserId: String): Boolean = recipientId == currentUserId
    fun peerIdFor(currentUserId: String): String = if (senderId == currentUserId) recipientId else senderId
    fun peerNameFor(currentUserId: String): String = if (senderId == currentUserId) recipientName else senderName
    fun peerPhotoFor(currentUserId: String): String = if (senderId == currentUserId) recipientPhotoUrl else senderPhotoUrl
}

data class ChatConversationItem(
    val chatId: String = "",
    val peerUserId: String = "",
    val peerDisplayName: String = "",
    val peerPhotoUrl: String = "",
    val lastMessageText: String = "",
    val lastMessageType: ChatMessageType = ChatMessageType.TEXT,
    val lastSenderId: String = "",
    val lastMessageAtMillis: Long = 0L,
    val unreadCount: Int = 0
)

data class ChatMessageItem(
    val messageId: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val messageType: ChatMessageType = ChatMessageType.TEXT,
    val text: String = "",
    val mediaName: String = "",
    val mediaMimeType: String = "",
    val mediaSizeBytes: Long = 0L,
    val localFilePath: String = "",
    val inlineMediaDataUrl: String = "",
    val thumbnailDataUrl: String = "",
    val chunkCount: Int = 0,
    val callType: String = "",
    val callDurationSec: Int = 0,
    val createdAtMillis: Long = 0L,
    val isSyncedToCloud: Boolean = true
)

data class ActiveCallSession(
    val callId: String = "",
    val chatId: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerPhotoUrl: String = "",
    val calleeId: String = "",
    val calleeName: String = "",
    val callType: ChatCallType = ChatCallType.AUDIO,
    val status: ChatCallStatus = ChatCallStatus.RINGING,
    val offerSdp: String = "",
    val answerSdp: String = "",
    val isOutgoing: Boolean = true,
    val isMicMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraEnabled: Boolean = false,
    val elapsedSeconds: Int = 0,
    val peerConnected: Boolean = false
) {
    fun peerName(currentUserId: String): String =
        if (callerId == currentUserId) calleeName else callerName

    fun peerId(currentUserId: String): String =
        if (callerId == currentUserId) calleeId else callerId
}

object ChatIdUtil {
    private val invalidChars = Regex("[^a-zA-Z0-9_\\-]")

    fun sanitizeId(raw: String): String =
        raw.replace(invalidChars, "_").take(60)

    fun deterministicPairId(uidA: String, uidB: String): String {
        val a = sanitizeId(uidA)
        val b = sanitizeId(uidB)
        return if (a <= b) "${a}_${b}" else "${b}_${a}"
    }

    fun sortedParticipantIds(uidA: String, uidB: String): List<String> =
        if (uidA <= uidB) listOf(uidA, uidB) else listOf(uidB, uidA)
}

enum class LiveRoomMode(val raw: String) {
    AUDIO("audio"),
    VIDEO("video");

    companion object {
        fun fromRaw(raw: String?): LiveRoomMode =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: VIDEO
    }
}

enum class LiveRoomStatus(val raw: String) {
    LIVE("live"),
    ENDED("ended");

    companion object {
        fun fromRaw(raw: String?): LiveRoomStatus =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: ENDED
    }
}

enum class LiveMessageType(val raw: String) {
    TEXT("text"),
    HAND_RAISE("hand_raise"),
    SYSTEM("system");

    companion object {
        fun fromRaw(raw: String?): LiveMessageType =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: TEXT
    }
}

data class LiveStudyRoom(
    val roomId: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val hostPhotoUrl: String = "",
    val title: String = "",
    val topicTechId: String = "general",
    val topicLabel: String = "Geral",
    val description: String = "",
    val liveMode: LiveRoomMode = LiveRoomMode.VIDEO,
    val status: LiveRoomStatus = LiveRoomStatus.LIVE,
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
) {
    val participantCount: Int
        get() = participantIds.size.coerceAtLeast(1)
}

data class LiveRoomMessage(
    val messageId: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String = "",
    val text: String = "",
    val messageType: LiveMessageType = LiveMessageType.TEXT,
    val createdAtMillis: Long = 0L
)

data class LiveSignalPayload(
    val signalId: String = "",
    val fromUid: String = "",
    val toUid: String = "",
    val type: String = "",
    val sdp: String = "",
    val sdpMid: String = "0",
    val sdpMLineIndex: Int = 0,
    val candidate: String = ""
)

enum class DirectoryFilterMode {
    ALL,
    AVAILABLE,
    CONNECTED
}

enum class DirectorySortMode {
    ALPHABETICAL,
    HIGHEST_LEVEL
}

object ChatDirectorySearchHelper {
    private val combiningMarksRegex = Regex("\\p{InCombiningDiacriticalMarks}+")

    fun normalizeForSearch(raw: String): String {
        val decomposed = java.text.Normalizer.normalize(raw.trim(), java.text.Normalizer.Form.NFD)
        return combiningMarksRegex.replace(decomposed, "").lowercase()
    }

    /**
     * Filters and ranks directory users interactively as the user types.
     * - When [query] is blank: returns all users (excluding [currentUserId]) sorted by [sortMode].
     * - When [query] is non-blank: matches name, levelTitle, or level number and ranks by relevance:
     *   exact match -> starts with query -> word starts with query -> earliest substring match -> title match.
     */
    fun filterAndSortUsers(
        users: List<ChatDirectoryUser>,
        query: String,
        currentUserId: String?,
        connectedUserIds: Set<String> = emptySet(),
        filterMode: DirectoryFilterMode = DirectoryFilterMode.ALL,
        sortMode: DirectorySortMode = DirectorySortMode.ALPHABETICAL
    ): List<ChatDirectoryUser> {
        val base = users
            .asSequence()
            .filter { it.userId != currentUserId }
            .filter { user ->
                when (filterMode) {
                    DirectoryFilterMode.ALL -> true
                    DirectoryFilterMode.CONNECTED -> connectedUserIds.contains(user.userId)
                    DirectoryFilterMode.AVAILABLE -> !connectedUserIds.contains(user.userId)
                }
            }
            .toList()

        val normQuery = normalizeForSearch(query)
        if (normQuery.isEmpty()) {
            return when (sortMode) {
                DirectorySortMode.ALPHABETICAL -> base.sortedWith(
                    compareBy<ChatDirectoryUser> { normalizeForSearch(it.displayName) }
                        .thenByDescending { it.totalScore }
                )
                DirectorySortMode.HIGHEST_LEVEL -> base.sortedWith(
                    compareByDescending<ChatDirectoryUser> { it.levelNumber }
                        .thenByDescending { it.totalScore }
                        .thenBy { normalizeForSearch(it.displayName) }
                )
            }
        }

        data class RankedUser(val user: ChatDirectoryUser, val score: Int, val normalizedName: String)

        return base.mapNotNull { user ->
            val normName = normalizeForSearch(user.displayName.ifBlank { user.displayNameLower })
            val normTitle = normalizeForSearch(user.levelTitle)
            val levelText = "nivel ${user.levelNumber}"
            val nameIdx = normName.indexOf(normQuery)
            val words = normName.split(Regex("\\s+")).filter { it.isNotEmpty() }

            val rankScore = when {
                normName == normQuery -> 0
                normName.startsWith(normQuery) -> 1
                words.any { it.startsWith(normQuery) } -> 2
                nameIdx >= 0 -> 10 + nameIdx
                normTitle.contains(normQuery) || levelText.contains(normQuery) -> 1000
                else -> return@mapNotNull null
            }
            RankedUser(user, rankScore, normName)
        }.sortedWith(
            compareBy<RankedUser> { it.score }
                .thenBy { it.normalizedName }
                .thenByDescending { it.user.totalScore }
        ).map { it.user }
    }

    /**
     * Returns the character index range in [originalText] matching [query] (case- & accent-insensitive).
     */
    fun findMatchRange(originalText: String, query: String): IntRange? {
        val normQuery = normalizeForSearch(query)
        if (normQuery.isEmpty() || originalText.isEmpty()) return null

        val normalizedChars = StringBuilder()
        val originalIndices = mutableListOf<Int>()
        for (i in originalText.indices) {
            val ch = originalText[i]
            val normCh = normalizeForSearch(ch.toString())
            if (normCh.isNotEmpty()) {
                for (c in normCh) {
                    normalizedChars.append(c)
                    originalIndices.add(i)
                }
            }
        }
        val matchStart = normalizedChars.indexOf(normQuery)
        if (matchStart < 0) return null
        val matchEndIdx = (matchStart + normQuery.length - 1).coerceAtMost(originalIndices.lastIndex)
        return originalIndices[matchStart]..originalIndices[matchEndIdx]
    }
}

