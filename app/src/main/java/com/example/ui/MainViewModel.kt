package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.billing.PlayBillingManager
import com.example.data.ai.AiStudyService
import com.example.data.local.AppDatabase
import com.example.data.local.GameScoreEntity
import com.example.data.local.RoutineWithEvents
import com.example.data.local.StudyNoteEntity
import com.example.data.local.UserBadgeEntity
import com.example.data.repository.ChatRepository
import com.example.data.repository.StudyRepository
import com.example.data.repository.SqlLabRepository
import com.example.data.repository.UserBadgeRepository
import com.example.data.repository.LeaderboardRepository
import com.example.data.repository.DailyLanguageChallengeSummary
import com.example.data.repository.FirestoreProgressRepository
import com.example.data.repository.LanguageDailyProgress
import com.example.data.repository.TargetSubjectsRepository
import com.example.idle.LanguageIdleEngine
import com.example.model.AccessibilitySettings
import com.example.model.ActiveCallSession
import com.example.model.AiChatMessage
import com.example.model.AppGeneralSettings
import com.example.model.AudioTranscriptionUiState
import com.example.model.AuthMethod
import com.example.model.ChatCallStatus
import com.example.model.ChatCallType
import com.example.model.ChatConversationItem
import com.example.model.ChatDirectoryUser
import com.example.model.ChatIdUtil
import com.example.model.ChatMessageItem
import com.example.model.ChatMessageType
import com.example.model.ChatRequestItem
import com.example.model.ChatRequestStatus
import com.example.model.DeveloperLevelCatalog
import com.example.model.FontScalePreset
import com.example.model.IdleExecutionResult
import com.example.model.IdleExecutionStatus
import com.example.model.Language
import com.example.model.LanguageIdleCatalog
import com.example.model.LanguageIdleCodeStorage
import com.example.model.LeaderboardEntry
import com.example.model.LevelUpEvent
import com.example.model.LifePaymentMethod
import com.example.model.LifePurchaseCurrency
import com.example.model.LifePurchaseReceipt
import com.example.model.LifePurchaseResult
import com.example.model.LifeShopCatalog
import com.example.model.LifeShopPackage
import com.example.model.LiveMessageType
import com.example.model.LiveRoomMessage
import com.example.model.LiveRoomMode
import com.example.model.LiveStudyRoom
import com.example.model.ModerateAdCatalog
import com.example.model.ModerateAdContent
import com.example.model.ModerateAdFrequencyPolicy
import com.example.model.PlayBillingStoreUiState
import com.example.model.ProSubscriptionPlan
import com.example.model.ProSubscriptionState
import com.android.billingclient.api.ProductDetails
import com.example.model.RoutinePlan
import com.example.model.SqlQueryResult
import com.example.model.StudyMaterialPackage
import com.example.model.StudySession
import com.example.model.TargetSubjectsCatalog
import com.example.model.TargetSubjectsPreference
import com.example.model.TargetSubjectsUiState
import com.example.model.Technology
import com.example.model.ThemeCustomization
import com.example.model.ThemePreference
import com.example.model.ThemePreset
import com.example.model.ThemeSurfaceStyle
import com.example.model.UserAccount
import com.example.model.UserFeedbackNote
import com.example.util.AppLocaleHelper
import com.example.model.UserScoreInfo
import com.example.model.WeeklySummaryUiState
import com.example.notification.NotificationScheduler
import com.example.notification.StudyReminderWorker
import com.example.util.AvatarImageCodec
import com.example.util.ChatMediaCodec
import com.example.util.WebRtcCallManager
import com.example.util.WebRtcLiveManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class AppDestination(val route: String) {
    LOGIN("login"),
    HOME("home"),
    TECH_HUB("tech_hub"),
    TECH_DETAIL("tech_detail"),
    PROGRESS("progress"),
    CALENDAR("calendar"),
    LEADERBOARD("leaderboard"),
    AI_CHAT("ai_chat"),
    GAME_3D("game_3d"),
    SQL_STUDIO("sql_studio"),
    COMMUNITY_CHAT("community_chat"),
    TARGET_SUBJECTS("target_subjects"),
    STUDY_STORE("study_store"),
    LANGUAGE_IDLE("language_idle"),
    SETTINGS("settings")
}

data class UiStreakInfo(
    val streakDays: Int,
    val completedCount: Int,
    val totalMinutes: Int
)

data class DailyLivesInfo(
    val remaining: Int = MAX_DAILY_LIVES,
    val maxDaily: Int = MAX_DAILY_LIVES,
    val lastResetDateIso: String = ""
) {
    companion object {
        const val MAX_DAILY_LIVES = 25
        const val SUBSCRIBER_BONUS_DAILY_LIVES = 15
        const val SUBSCRIBER_MAX_DAILY_LIVES = MAX_DAILY_LIVES + SUBSCRIBER_BONUS_DAILY_LIVES
        const val MAX_PURCHASED_LIVES_CAP = 999

        fun effectiveDailyMax(isProSubscriber: Boolean): Int =
            if (isProSubscriber) SUBSCRIBER_MAX_DAILY_LIVES else MAX_DAILY_LIVES
    }
}

object ProSubscriptionManager {
    private const val KEY_PRO_ACTIVE = "pro_subscription_active"
    private const val KEY_PRO_ACTIVATED_AT = "pro_subscription_activated_at"
    private const val KEY_PRO_EXPIRES_AT = "pro_subscription_expires_at"
    private const val KEY_PRO_PAYMENT_METHOD = "pro_subscription_payment_method"
    private const val KEY_PRO_BONUS_GRANTED_DATE = "pro_subscription_bonus_granted_date_iso"
    private const val KEY_PRO_PURCHASE_TOKEN = "pro_subscription_purchase_token"
    private const val KEY_PRO_ORDER_ID = "pro_subscription_order_id"
    private const val KEY_PRO_AUTO_RENEWING = "pro_subscription_auto_renewing"
    private const val KEY_PRO_VERIFIED_BY_PLAY = "pro_subscription_verified_by_play"

    fun shouldGrantImmediateBonus(
        prefs: android.content.SharedPreferences,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ): Boolean = prefs.getString(KEY_PRO_BONUS_GRANTED_DATE, null) != todayIso

    fun markImmediateBonusGranted(
        prefs: android.content.SharedPreferences,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ) {
        prefs.edit().putString(KEY_PRO_BONUS_GRANTED_DATE, todayIso).apply()
    }

    fun loadState(
        prefs: android.content.SharedPreferences,
        nowMillis: Long = System.currentTimeMillis()
    ): ProSubscriptionState {
        val activeFlag = prefs.getBoolean(KEY_PRO_ACTIVE, false)
        val verifiedByPlay = prefs.getBoolean(KEY_PRO_VERIFIED_BY_PLAY, false)
        val purchaseToken = prefs.getString(KEY_PRO_PURCHASE_TOKEN, null)
        val orderId = prefs.getString(KEY_PRO_ORDER_ID, null)
        val autoRenewing = prefs.getBoolean(KEY_PRO_AUTO_RENEWING, false)
        val activatedAt = prefs.getLong(KEY_PRO_ACTIVATED_AT, 0L)
        val expiresAt = prefs.getLong(KEY_PRO_EXPIRES_AT, 0L)
        val methodName = prefs.getString(KEY_PRO_PAYMENT_METHOD, null)
        val method = methodName?.let { name ->
            LifePaymentMethod.entries.firstOrNull { it.name == name }
        }
        val hasValidToken = verifiedByPlay && !purchaseToken.isNullOrBlank()
        val stillValid = activeFlag && hasValidToken && (expiresAt <= 0L || nowMillis < expiresAt)
        if (activeFlag && !stillValid) {
            prefs.edit().putBoolean(KEY_PRO_ACTIVE, false).apply()
        }
        return ProSubscriptionState(
            isActive = stillValid,
            activatedAtMillis = if (stillValid) activatedAt else 0L,
            expiresAtMillis = if (stillValid) expiresAt else 0L,
            paymentMethod = if (stillValid) method else null,
            bonusDailyLives = DailyLivesInfo.SUBSCRIBER_BONUS_DAILY_LIVES,
            monthlyPriceBrl = ProSubscriptionState.DEFAULT_PLAN.priceBrl,
            purchaseToken = if (stillValid) purchaseToken else null,
            orderId = if (stillValid) orderId else null,
            isAutoRenewing = if (stillValid) autoRenewing else false,
            verifiedByPlayBilling = stillValid
        )
    }

    fun activateVerifiedPlaySubscription(
        prefs: android.content.SharedPreferences,
        purchaseToken: String,
        orderId: String? = null,
        purchaseTimeMillis: Long = System.currentTimeMillis(),
        isAutoRenewing: Boolean = true,
        nowMillis: Long = System.currentTimeMillis()
    ): ProSubscriptionState {
        require(purchaseToken.isNotBlank()) { "Verified Google Play purchaseToken is required." }
        val baseTime = maxOf(purchaseTimeMillis, nowMillis)
        val expiresAt = baseTime + ProSubscriptionState.DEFAULT_PLAN.billingPeriodDays * ProSubscriptionState.MILLIS_PER_DAY
        prefs.edit()
            .putBoolean(KEY_PRO_ACTIVE, true)
            .putBoolean(KEY_PRO_VERIFIED_BY_PLAY, true)
            .putString(KEY_PRO_PURCHASE_TOKEN, purchaseToken)
            .putString(KEY_PRO_ORDER_ID, orderId)
            .putBoolean(KEY_PRO_AUTO_RENEWING, isAutoRenewing)
            .putLong(KEY_PRO_ACTIVATED_AT, purchaseTimeMillis.coerceAtLeast(1L))
            .putLong(KEY_PRO_EXPIRES_AT, expiresAt)
            .putString(KEY_PRO_PAYMENT_METHOD, LifePaymentMethod.GOOGLE_PLAY.name)
            .apply()
        return ProSubscriptionState(
            isActive = true,
            activatedAtMillis = purchaseTimeMillis.coerceAtLeast(1L),
            expiresAtMillis = expiresAt,
            paymentMethod = LifePaymentMethod.GOOGLE_PLAY,
            bonusDailyLives = DailyLivesInfo.SUBSCRIBER_BONUS_DAILY_LIVES,
            monthlyPriceBrl = ProSubscriptionState.DEFAULT_PLAN.priceBrl,
            purchaseToken = purchaseToken,
            orderId = orderId,
            isAutoRenewing = isAutoRenewing,
            verifiedByPlayBilling = true
        )
    }

    fun deactivateSubscription(prefs: android.content.SharedPreferences): ProSubscriptionState {
        prefs.edit()
            .putBoolean(KEY_PRO_ACTIVE, false)
            .putBoolean(KEY_PRO_VERIFIED_BY_PLAY, false)
            .remove(KEY_PRO_PURCHASE_TOKEN)
            .remove(KEY_PRO_ORDER_ID)
            .putBoolean(KEY_PRO_AUTO_RENEWING, false)
            .putLong(KEY_PRO_EXPIRES_AT, 0L)
            .apply()
        return ProSubscriptionState(isActive = false)
    }
}

object DailyLivesManager {
    private const val KEY_LIVES_REMAINING = "daily_lives_remaining"
    private const val KEY_LIVES_DATE = "daily_lives_date_iso"

    fun loadOrResetForDate(
        prefs: android.content.SharedPreferences,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
        dailyMaxLives: Int = DailyLivesInfo.MAX_DAILY_LIVES
    ): DailyLivesInfo {
        val savedDate = prefs.getString(KEY_LIVES_DATE, null)
        return if (savedDate != todayIso) {
            val carriedOver = if (savedDate == null) {
                dailyMaxLives
            } else {
                maxOf(
                    prefs.getInt(KEY_LIVES_REMAINING, dailyMaxLives),
                    dailyMaxLives
                ).coerceIn(dailyMaxLives, DailyLivesInfo.MAX_PURCHASED_LIVES_CAP)
            }
            prefs.edit()
                .putInt(KEY_LIVES_REMAINING, carriedOver)
                .putString(KEY_LIVES_DATE, todayIso)
                .apply()
            DailyLivesInfo(
                remaining = carriedOver,
                maxDaily = dailyMaxLives,
                lastResetDateIso = todayIso
            )
        } else {
            val rem = prefs.getInt(KEY_LIVES_REMAINING, dailyMaxLives)
                .coerceIn(0, DailyLivesInfo.MAX_PURCHASED_LIVES_CAP)
            DailyLivesInfo(
                remaining = rem,
                maxDaily = dailyMaxLives,
                lastResetDateIso = todayIso
            )
        }
    }

    fun consumeOneLife(
        prefs: android.content.SharedPreferences,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
        dailyMaxLives: Int = DailyLivesInfo.MAX_DAILY_LIVES
    ): Pair<Boolean, DailyLivesInfo> {
        val current = loadOrResetForDate(prefs, todayIso, dailyMaxLives)
        if (current.remaining <= 0) {
            return false to current
        }
        val next = (current.remaining - 1).coerceAtLeast(0)
        prefs.edit()
            .putInt(KEY_LIVES_REMAINING, next)
            .putString(KEY_LIVES_DATE, todayIso)
            .apply()
        return true to current.copy(remaining = next)
    }

    fun resetLivesForLevelUp(
        prefs: android.content.SharedPreferences,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
        dailyMaxLives: Int = DailyLivesInfo.MAX_DAILY_LIVES
    ): DailyLivesInfo {
        val current = loadOrResetForDate(prefs, todayIso, dailyMaxLives)
        val target = maxOf(current.remaining, dailyMaxLives)
        prefs.edit()
            .putInt(KEY_LIVES_REMAINING, target)
            .putString(KEY_LIVES_DATE, todayIso)
            .apply()
        return DailyLivesInfo(
            remaining = target,
            maxDaily = dailyMaxLives,
            lastResetDateIso = todayIso
        )
    }

    fun addPurchasedLives(
        prefs: android.content.SharedPreferences,
        livesToAdd: Int,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
        dailyMaxLives: Int = DailyLivesInfo.MAX_DAILY_LIVES
    ): DailyLivesInfo {
        val current = loadOrResetForDate(prefs, todayIso, dailyMaxLives)
        val next = (current.remaining + livesToAdd.coerceAtLeast(0))
            .coerceIn(0, DailyLivesInfo.MAX_PURCHASED_LIVES_CAP)
        prefs.edit()
            .putInt(KEY_LIVES_REMAINING, next)
            .putString(KEY_LIVES_DATE, todayIso)
            .apply()
        return current.copy(remaining = next)
    }

    fun grantSubscriptionBonusLives(
        prefs: android.content.SharedPreferences,
        bonusLives: Int = DailyLivesInfo.SUBSCRIBER_BONUS_DAILY_LIVES,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ): DailyLivesInfo {
        return addPurchasedLives(
            prefs = prefs,
            livesToAdd = bonusLives,
            todayIso = todayIso,
            dailyMaxLives = DailyLivesInfo.SUBSCRIBER_MAX_DAILY_LIVES
        )
    }
}

object CourseChecklistManager {
    private const val KEY_COMPLETED_COURSE_LESSONS = "completed_course_lesson_ids"

    fun loadCompletedIds(prefs: android.content.SharedPreferences): Set<String> =
        prefs.getStringSet(KEY_COMPLETED_COURSE_LESSONS, emptySet())?.toSet() ?: emptySet()

    /**
     * Alterna a conclusão de uma aula de curso livre/vídeo apenas como controle de estudo.
     * Não concede XP nem altera pontuação, nível ou ranking.
     */
    fun toggleLessonCompleted(
        prefs: android.content.SharedPreferences,
        currentLessons: Set<String>,
        lessonId: String
    ): Set<String> {
        val updated = if (currentLessons.contains(lessonId)) {
            currentLessons - lessonId
        } else {
            currentLessons + lessonId
        }
        prefs.edit().putStringSet(KEY_COMPLETED_COURSE_LESSONS, updated.toSet()).apply()
        return updated
    }
}

data class DailyScreenTimeTickResult(
    val todayMinutes: Int,
    val totalMinutes: Int,
    val awardedFocusMilestoneBonus: Boolean
)

data class DailyScreenTimeSecondResult(
    val todaySeconds: Int,
    val todayMinutes: Int,
    val totalMinutes: Int,
    val completedNewMinute: Boolean,
    val awardedFocusMilestoneBonus: Boolean,
    val goalProgressFraction: Float,
    val goalPercent: Int
)

data class DailyStudyGoalState(
    val activeSecondsToday: Int = 0,
    val activeMinutesToday: Int = 0,
    val totalActiveMinutes: Int = 0,
    val goalMinutes: Int = DailyScreenTimeManager.DAILY_GOAL_MINUTES,
    val progressFraction: Float = 0f,
    val progressPercent: Int = 0,
    val remainingSecondsToday: Int = DailyScreenTimeManager.DAILY_GOAL_SECONDS,
    val isGoalReached: Boolean = false,
    val isTimerRunning: Boolean = false,
    val formattedActiveTime: String = "00:00",
    val formattedGoalTime: String = "30:00"
)

object DailyScreenTimeManager {
    const val DAILY_GOAL_MINUTES = 30
    const val DAILY_GOAL_SECONDS = DAILY_GOAL_MINUTES * 60
    const val IDLE_TIMEOUT_MS = 120_000L // Pausa automática após 2 min sem interação real

    private const val KEY_TODAY_SECONDS = "daily_screen_time_seconds"
    private const val KEY_TODAY_MINUTES = "daily_screen_time_minutes"
    private const val KEY_TODAY_DATE = "daily_screen_time_date_iso"
    private const val KEY_TOTAL_MINUTES = "total_screen_time_minutes"
    private const val KEY_FOCUS_MILESTONES_COUNT = "daily_focus_milestones_count"
    private const val MAX_DAILY_FOCUS_MILESTONES = 4 // Até 60 min/dia de bônus de foco real (15, 30, 45, 60m)

    fun isUserActive(
        lastActivityMillis: Long,
        nowMillis: Long,
        isInForeground: Boolean,
        idleTimeoutMs: Long = IDLE_TIMEOUT_MS
    ): Boolean {
        if (!isInForeground || lastActivityMillis <= 0L) return false
        val elapsed = nowMillis - lastActivityMillis
        return elapsed in 0L until idleTimeoutMs
    }

    fun formatDurationMmSs(totalSeconds: Int): String {
        val safeSeconds = totalSeconds.coerceAtLeast(0)
        val mins = safeSeconds / 60
        val secs = safeSeconds % 60
        return String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
    }

    fun buildGoalState(
        todaySeconds: Int,
        totalMinutes: Int,
        isTimerRunning: Boolean
    ): DailyStudyGoalState {
        val safeSeconds = todaySeconds.coerceAtLeast(0)
        val fraction = (safeSeconds.toFloat() / DAILY_GOAL_SECONDS).coerceIn(0f, 1f)
        val percent = ((safeSeconds.toFloat() / DAILY_GOAL_SECONDS) * 100f).toInt().coerceIn(0, 100)
        val remaining = (DAILY_GOAL_SECONDS - safeSeconds).coerceAtLeast(0)
        return DailyStudyGoalState(
            activeSecondsToday = safeSeconds,
            activeMinutesToday = safeSeconds / 60,
            totalActiveMinutes = totalMinutes.coerceAtLeast(0),
            goalMinutes = DAILY_GOAL_MINUTES,
            progressFraction = fraction,
            progressPercent = percent,
            remainingSecondsToday = remaining,
            isGoalReached = safeSeconds >= DAILY_GOAL_SECONDS,
            isTimerRunning = isTimerRunning,
            formattedActiveTime = formatDurationMmSs(safeSeconds),
            formattedGoalTime = formatDurationMmSs(DAILY_GOAL_SECONDS)
        )
    }

    fun loadOrResetTodaySeconds(
        prefs: android.content.SharedPreferences,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ): Int {
        val savedDate = prefs.getString(KEY_TODAY_DATE, null)
        return if (savedDate != todayIso) {
            prefs.edit()
                .putInt(KEY_TODAY_SECONDS, 0)
                .putInt(KEY_TODAY_MINUTES, 0)
                .putInt(KEY_FOCUS_MILESTONES_COUNT, 0)
                .putString(KEY_TODAY_DATE, todayIso)
                .apply()
            0
        } else {
            val secs = prefs.getInt(KEY_TODAY_SECONDS, 0).coerceAtLeast(0)
            val mins = prefs.getInt(KEY_TODAY_MINUTES, 0).coerceAtLeast(0)
            maxOf(secs, mins * 60)
        }
    }

    fun loadOrResetTodayMinutes(
        prefs: android.content.SharedPreferences,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ): Int {
        return loadOrResetTodaySeconds(prefs, todayIso) / 60
    }

    fun advanceOneActiveSecond(
        prefs: android.content.SharedPreferences,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ): DailyScreenTimeSecondResult {
        val currentSeconds = loadOrResetTodaySeconds(prefs, todayIso)
        val prevMinutes = currentSeconds / 60
        val nextSeconds = currentSeconds + 1
        val nextMinutes = nextSeconds / 60
        val completedNewMinute = nextMinutes > prevMinutes

        val currentTotalMinutes = prefs.getInt(KEY_TOTAL_MINUTES, 0).coerceAtLeast(0)
        val nextTotalMinutes = if (completedNewMinute) {
            currentTotalMinutes + (nextMinutes - prevMinutes)
        } else {
            currentTotalMinutes
        }

        val milestonesToday = prefs.getInt(KEY_FOCUS_MILESTONES_COUNT, 0).coerceAtLeast(0)
        val shouldAwardMilestone = completedNewMinute &&
            nextMinutes > 0 &&
            nextMinutes % 15 == 0 &&
            milestonesToday < MAX_DAILY_FOCUS_MILESTONES

        val editor = prefs.edit()
            .putInt(KEY_TODAY_SECONDS, nextSeconds)
            .putInt(KEY_TODAY_MINUTES, nextMinutes)
            .putInt(KEY_TOTAL_MINUTES, nextTotalMinutes)
            .putString(KEY_TODAY_DATE, todayIso)
        if (shouldAwardMilestone) {
            editor.putInt(KEY_FOCUS_MILESTONES_COUNT, milestonesToday + 1)
        }
        editor.apply()

        val fraction = (nextSeconds.toFloat() / DAILY_GOAL_SECONDS).coerceIn(0f, 1f)
        val percent = ((nextSeconds.toFloat() / DAILY_GOAL_SECONDS) * 100f).toInt().coerceIn(0, 100)

        return DailyScreenTimeSecondResult(
            todaySeconds = nextSeconds,
            todayMinutes = nextMinutes,
            totalMinutes = nextTotalMinutes,
            completedNewMinute = completedNewMinute,
            awardedFocusMilestoneBonus = shouldAwardMilestone,
            goalProgressFraction = fraction,
            goalPercent = percent
        )
    }

    fun incrementOneRealMinute(
        prefs: android.content.SharedPreferences,
        todayIso: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ): DailyScreenTimeTickResult {
        val currentSeconds = loadOrResetTodaySeconds(prefs, todayIso)
        val nextSeconds = currentSeconds + 60
        val nextToday = nextSeconds / 60
        val nextTotal = prefs.getInt(KEY_TOTAL_MINUTES, 0).coerceAtLeast(0) + 1
        val milestonesToday = prefs.getInt(KEY_FOCUS_MILESTONES_COUNT, 0).coerceAtLeast(0)

        val shouldAwardMilestone = nextToday > 0 && nextToday % 15 == 0 && milestonesToday < MAX_DAILY_FOCUS_MILESTONES
        val editor = prefs.edit()
            .putInt(KEY_TODAY_SECONDS, nextSeconds)
            .putInt(KEY_TODAY_MINUTES, nextToday)
            .putInt(KEY_TOTAL_MINUTES, nextTotal)
            .putString(KEY_TODAY_DATE, todayIso)
        if (shouldAwardMilestone) {
            editor.putInt(KEY_FOCUS_MILESTONES_COUNT, milestonesToday + 1)
        }
        editor.apply()

        return DailyScreenTimeTickResult(
            todayMinutes = nextToday,
            totalMinutes = nextTotal,
            awardedFocusMilestoneBonus = shouldAwardMilestone
        )
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("devroutine_prefs", Context.MODE_PRIVATE)

    private val database = AppDatabase.getInstance(application)
    private val repository = StudyRepository(
        database.studySessionDao(),
        database.studyRoutineDao(),
        database.calendarEventDao()
    )
    private val badgeRepository = UserBadgeRepository(database.userBadgeDao())
    private val aiService = AiStudyService()
    private val leaderboardRepository = LeaderboardRepository(application)
    private val progressRepository = FirestoreProgressRepository(application)
    private val sqlLabRepository = SqlLabRepository(database)
    private val chatRepository = ChatRepository(application, Firebase.auth, database.chatDao())
    private val targetSubjectsRepository = TargetSubjectsRepository(application, Firebase.auth)
    val webRtcCallManager = WebRtcCallManager(application)
    val webRtcLiveManager = WebRtcLiveManager(application)

    // Chat, Solicitações, Diretório de Usuários, Mídia, Ligações WebRTC & Salas Ao Vivo (Live)
    private val _isFirebaseChatAuthenticated = MutableStateFlow(Firebase.auth.currentUser != null)
    val isFirebaseChatAuthenticated: StateFlow<Boolean> = _isFirebaseChatAuthenticated.asStateFlow()

    private val _chatOwnerId = MutableStateFlow<String?>(Firebase.auth.currentUser?.uid)
    val chatOwnerId: StateFlow<String?> = _chatOwnerId.asStateFlow()

    private val _chatDirectoryUsers = MutableStateFlow<List<ChatDirectoryUser>>(emptyList())
    val chatDirectoryUsers: StateFlow<List<ChatDirectoryUser>> = _chatDirectoryUsers.asStateFlow()

    private val _chatRequests = MutableStateFlow<List<ChatRequestItem>>(emptyList())
    val chatRequests: StateFlow<List<ChatRequestItem>> = _chatRequests.asStateFlow()

    private val _chatConversations = MutableStateFlow<List<ChatConversationItem>>(emptyList())
    val chatConversations: StateFlow<List<ChatConversationItem>> = _chatConversations.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    private val _activeChatMessages = MutableStateFlow<List<ChatMessageItem>>(emptyList())
    val activeChatMessages: StateFlow<List<ChatMessageItem>> = _activeChatMessages.asStateFlow()

    private val _isChatUploading = MutableStateFlow(false)
    val isChatUploading: StateFlow<Boolean> = _isChatUploading.asStateFlow()

    private val _chatStatusFeedback = MutableStateFlow<String?>(null)
    val chatStatusFeedback: StateFlow<String?> = _chatStatusFeedback.asStateFlow()

    private val _activeCallSession = MutableStateFlow<ActiveCallSession?>(null)
    val activeCallSession: StateFlow<ActiveCallSession?> = _activeCallSession.asStateFlow()

    private val _activeLiveRooms = MutableStateFlow<List<LiveStudyRoom>>(emptyList())
    val activeLiveRooms: StateFlow<List<LiveStudyRoom>> = _activeLiveRooms.asStateFlow()

    private val _joinedLiveRoom = MutableStateFlow<LiveStudyRoom?>(null)
    val joinedLiveRoom: StateFlow<LiveStudyRoom?> = _joinedLiveRoom.asStateFlow()

    private val _liveRoomMessages = MutableStateFlow<List<LiveRoomMessage>>(emptyList())
    val liveRoomMessages: StateFlow<List<LiveRoomMessage>> = _liveRoomMessages.asStateFlow()

    private val _isLiveMicMuted = MutableStateFlow(false)
    val isLiveMicMuted: StateFlow<Boolean> = _isLiveMicMuted.asStateFlow()

    private val _isLiveCameraEnabled = MutableStateFlow(true)
    val isLiveCameraEnabled: StateFlow<Boolean> = _isLiveCameraEnabled.asStateFlow()

    private val _isLiveSpeakerOn = MutableStateFlow(true)
    val isLiveSpeakerOn: StateFlow<Boolean> = _isLiveSpeakerOn.asStateFlow()

    private val _isLivePeerConnected = MutableStateFlow(false)
    val isLivePeerConnected: StateFlow<Boolean> = _isLivePeerConnected.asStateFlow()

    private val _liveElapsedSeconds = MutableStateFlow(0)
    val liveElapsedSeconds: StateFlow<Int> = _liveElapsedSeconds.asStateFlow()

    private var chatDirectoryJob: kotlinx.coroutines.Job? = null
    private var chatRequestsJob: kotlinx.coroutines.Job? = null
    private var localConversationsJob: kotlinx.coroutines.Job? = null
    private var cloudConversationsJob: kotlinx.coroutines.Job? = null
    private var localMessagesJob: kotlinx.coroutines.Job? = null
    private var cloudMessagesJob: kotlinx.coroutines.Job? = null
    private var callSessionsJob: kotlinx.coroutines.Job? = null
    private var iceCandidatesJob: kotlinx.coroutines.Job? = null
    private var callDurationTickerJob: kotlinx.coroutines.Job? = null
    private var liveRoomsJob: kotlinx.coroutines.Job? = null
    private var liveMessagesJob: kotlinx.coroutines.Job? = null
    private var liveSignalsJob: kotlinx.coroutines.Job? = null
    private var liveDurationTickerJob: kotlinx.coroutines.Job? = null
    private var appliedAnswerForCallId: String? = null
    private val processedIceCandidateIds = mutableSetOf<String>()
    private val processedLiveSignalIds = mutableSetOf<String>()

    // User Account & Placar / Leaderboard State
    private val _userAccount = MutableStateFlow<UserAccount?>(null)
    val userAccount: StateFlow<UserAccount?> = _userAccount.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _leaderboardEntries = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboardEntries: StateFlow<List<LeaderboardEntry>> = _leaderboardEntries.asStateFlow()

    private var leaderboardJob: kotlinx.coroutines.Job? = null

    // Daily Study Progress Tracker (Firestore)
    private val _trackerSelectedTech = MutableStateFlow(Technology.PYTHON)
    val trackerSelectedTech: StateFlow<Technology> = _trackerSelectedTech.asStateFlow()

    private val _todayLanguageProgressMap = MutableStateFlow<Map<String, LanguageDailyProgress>>(emptyMap())
    val todayLanguageProgressMap: StateFlow<Map<String, LanguageDailyProgress>> = _todayLanguageProgressMap.asStateFlow()

    private val _weeklyChallengeHistory = MutableStateFlow(progressRepository.getLocalWeeklyChallengeHistory(7))
    val weeklyChallengeHistory: StateFlow<List<DailyLanguageChallengeSummary>> = _weeklyChallengeHistory.asStateFlow()

    private var dailyProgressJob: kotlinx.coroutines.Job? = null
    private var weeklyHistoryJob: kotlinx.coroutines.Job? = null

    // Target Subjects Cloud Preferences (Firestore /users/{uid}/preferences/target_subjects)
    private val _targetSubjectsPreference = MutableStateFlow<TargetSubjectsPreference?>(
        resolveInitialTargetSubjectsCache()
    )
    val targetSubjectsPreference: StateFlow<TargetSubjectsPreference?> = _targetSubjectsPreference.asStateFlow()

    private val _isTargetSubjectsCloudSynced = MutableStateFlow(false)
    val isTargetSubjectsCloudSynced: StateFlow<Boolean> = _isTargetSubjectsCloudSynced.asStateFlow()

    private val _targetSubjectsUiState = MutableStateFlow<TargetSubjectsUiState>(TargetSubjectsUiState.Idle)
    val targetSubjectsUiState: StateFlow<TargetSubjectsUiState> = _targetSubjectsUiState.asStateFlow()

    private var targetSubjectsJob: kotlinx.coroutines.Job? = null

    // Assinatura DevRoutine Pro (R$ 13/mês -> sem anúncios + 15 vidas diárias extras = 40/dia)
    private val _proSubscriptionState = MutableStateFlow(ProSubscriptionManager.loadState(prefs))
    val proSubscriptionState: StateFlow<ProSubscriptionState> = _proSubscriptionState.asStateFlow()

    // Anúncio intersticial moderado (máximo 1 a cada 4 desafios/quizzes e cooldown de 3 min; oculto para assinantes)
    private var completedActionsSinceLastAd = 0
    private var lastInterstitialShownAtMillis = prefs.getLong("last_moderate_interstitial_ad_ms", 0L)
    private var interstitialSlotCursor = 0
    private val _activeModerateInterstitial = MutableStateFlow<ModerateAdContent?>(null)
    val activeModerateInterstitial: StateFlow<ModerateAdContent?> = _activeModerateInterstitial.asStateFlow()

    // Sistema de vidas diárias (25 base ou 40 para assinantes Pro; 1 vida por tentativa; reseta ao subir de nível!)
    private val _dailyLives = MutableStateFlow(
        DailyLivesManager.loadOrResetForDate(
            prefs = prefs,
            dailyMaxLives = DailyLivesInfo.effectiveDailyMax(_proSubscriptionState.value.isCurrentlyActive())
        )
    )
    val dailyLives: StateFlow<DailyLivesInfo> = _dailyLives.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<LevelUpEvent?>(null)
    val levelUpEvent: StateFlow<LevelUpEvent?> = _levelUpEvent.asStateFlow()

    fun dismissLevelUpEvent() {
        _levelUpEvent.value = null
    }

    private fun currentEffectiveDailyMax(): Int {
        val refreshedSub = ProSubscriptionManager.loadState(prefs)
        if (_proSubscriptionState.value != refreshedSub) {
            _proSubscriptionState.value = refreshedSub
        }
        return DailyLivesInfo.effectiveDailyMax(refreshedSub.isCurrentlyActive())
    }

    fun refreshDailyLivesIfNewDay() {
        _dailyLives.value = DailyLivesManager.loadOrResetForDate(
            prefs = prefs,
            dailyMaxLives = currentEffectiveDailyMax()
        )
    }

    fun consumeLifeForAttempt(): Boolean {
        val (consumed, updated) = DailyLivesManager.consumeOneLife(
            prefs = prefs,
            dailyMaxLives = currentEffectiveDailyMax()
        )
        _dailyLives.value = updated
        return consumed
    }

    private val playBillingManager: PlayBillingManager by lazy {
        PlayBillingManager(
            context = getApplication(),
            prefs = prefs,
            scope = viewModelScope,
            languageProvider = { _language.value },
            obfuscatedAccountIdProvider = { _userAccount.value?.userId ?: Firebase.auth.currentUser?.uid },
            onConsumableLivesGranted = { pkg, quantity, _, orderId ->
                grantVerifiedPlayLifePack(pkg = pkg, quantity = quantity, orderId = orderId)
            },
            onSubscriptionVerified = { purchaseToken, orderId, purchaseTimeMillis, isAutoRenewing, userInitiated ->
                applyVerifiedPlayProSubscription(
                    purchaseToken = purchaseToken,
                    orderId = orderId,
                    purchaseTimeMillis = purchaseTimeMillis,
                    isAutoRenewing = isAutoRenewing,
                    userInitiated = userInitiated
                )
            },
            onSubscriptionNotActiveInPlay = {
                syncPlaySubscriptionRevokedOrExpired()
            },
            onStudyMaterialVerified = { pkg, _, userInitiated ->
                if (userInitiated) {
                    recordUserActivity()
                    val isPt = _language.value == Language.PT
                    _pointsFeedback.value = if (isPt) {
                        "📚 Material desbloqueado via Google Play: ${pkg.titlePt}!"
                    } else {
                        "📚 Study material unlocked via Google Play: ${pkg.titleEn}!"
                    }
                }
            }
        )
    }

    val billingStoreUiState: StateFlow<PlayBillingStoreUiState> by lazy {
        playBillingManager.storeUiState
    }

    val studyMaterialProductDetails: StateFlow<Map<String, ProductDetails>> by lazy {
        playBillingManager.studyMaterialProductDetails
    }

    val ownedStudyMaterialIds: StateFlow<Set<String>> by lazy {
        playBillingManager.ownedStudyMaterialIds
    }

    fun launchLifePackBilling(activity: Activity, pkg: LifeShopPackage) {
        val productId = pkg.playProductId
        if (pkg.currency != LifePurchaseCurrency.BRL || productId.isNullOrBlank()) {
            return
        }
        playBillingManager.launchPurchase(activity, productId)
    }

    fun launchStudyMaterialBilling(activity: Activity, productId: String) {
        if (productId.isBlank()) return
        playBillingManager.launchPurchase(activity, productId)
    }

    fun launchProSubscriptionBilling(activity: Activity) {
        playBillingManager.launchPurchase(activity, ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY)
    }

    fun refreshStudyMaterialsCatalog(userInitiated: Boolean = true) {
        playBillingManager.refreshCatalogAndPurchases(userInitiated = userInitiated)
    }

    fun restorePlayPurchases() {
        playBillingManager.restorePurchases()
    }

    fun refreshBillingOnResume() {
        playBillingManager.refreshOnResume()
    }

    fun clearBillingStatusMessage() {
        playBillingManager.clearStatusMessage()
    }

    private fun grantVerifiedPlayLifePack(
        pkg: LifeShopPackage,
        quantity: Int,
        orderId: String?
    ) {
        val totalLivesToAdd = pkg.livesAmount * quantity.coerceAtLeast(1)
        val effectiveDailyMax = currentEffectiveDailyMax()
        val updatedLives = DailyLivesManager.addPurchasedLives(
            prefs = prefs,
            livesToAdd = totalLivesToAdd,
            dailyMaxLives = effectiveDailyMax
        )
        _dailyLives.value = updatedLives
        _lastLifePurchaseReceipt.value = LifePurchaseReceipt(
            pkg = pkg,
            newRemainingLives = updatedLives.remaining,
            paymentMethod = LifePaymentMethod.GOOGLE_PLAY,
            orderId = orderId,
            quantity = quantity.coerceAtLeast(1)
        )
        recordUserActivity()
        val isPt = _language.value == Language.PT
        _pointsFeedback.value = if (isPt) {
            "❤️ +$totalLivesToAdd vidas confirmadas via Google Play! Saldo: ${updatedLives.remaining}"
        } else {
            "❤️ +$totalLivesToAdd lives confirmed via Google Play! Balance: ${updatedLives.remaining}"
        }
    }

    private fun applyVerifiedPlayProSubscription(
        purchaseToken: String,
        orderId: String?,
        purchaseTimeMillis: Long,
        isAutoRenewing: Boolean,
        userInitiated: Boolean
    ) {
        val wasAlreadyActive = _proSubscriptionState.value.isCurrentlyActive()
        val updatedSub = ProSubscriptionManager.activateVerifiedPlaySubscription(
            prefs = prefs,
            purchaseToken = purchaseToken,
            orderId = orderId,
            purchaseTimeMillis = purchaseTimeMillis,
            isAutoRenewing = isAutoRenewing
        )
        _proSubscriptionState.value = updatedSub
        _activeModerateInterstitial.value = null

        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val grantBonusNow = !wasAlreadyActive && ProSubscriptionManager.shouldGrantImmediateBonus(prefs, todayIso)
        val updatedLives = if (grantBonusNow) {
            ProSubscriptionManager.markImmediateBonusGranted(prefs, todayIso)
            DailyLivesManager.grantSubscriptionBonusLives(
                prefs = prefs,
                bonusLives = DailyLivesInfo.SUBSCRIBER_BONUS_DAILY_LIVES,
                todayIso = todayIso
            )
        } else {
            DailyLivesManager.loadOrResetForDate(
                prefs = prefs,
                todayIso = todayIso,
                dailyMaxLives = DailyLivesInfo.SUBSCRIBER_MAX_DAILY_LIVES
            )
        }
        _dailyLives.value = updatedLives
        if (userInitiated) {
            recordUserActivity()
            val isPt = _language.value == Language.PT
            _pointsFeedback.value = if (isPt) {
                "👑 Assinatura Pro confirmada via Google Play! Sem anúncios e 40 vidas/dia (${updatedLives.remaining}/${updatedLives.maxDaily})!"
            } else {
                "👑 Pro Subscription verified via Google Play! Ad-free and 40 lives/day (${updatedLives.remaining}/${updatedLives.maxDaily})!"
            }
        }
    }

    private fun syncPlaySubscriptionRevokedOrExpired() {
        if (!_proSubscriptionState.value.isCurrentlyActive()) return
        val deactivated = ProSubscriptionManager.deactivateSubscription(prefs)
        _proSubscriptionState.value = deactivated
        _dailyLives.value = DailyLivesManager.loadOrResetForDate(
            prefs = prefs,
            dailyMaxLives = DailyLivesInfo.MAX_DAILY_LIVES
        )
    }

    fun dismissModerateInterstitial() {
        _activeModerateInterstitial.value = null
    }

    private fun maybeTriggerModerateInterstitialAd() {
        val now = System.currentTimeMillis()
        val sub = ProSubscriptionManager.loadState(prefs, now)
        _proSubscriptionState.value = sub
        if (sub.isCurrentlyActive(now)) {
            _activeModerateInterstitial.value = null
            return
        }
        completedActionsSinceLastAd += 1
        if (
            ModerateAdFrequencyPolicy.shouldShowInterstitial(
                isProActive = false,
                completedActionsSinceLastAd = completedActionsSinceLastAd,
                lastAdShownAtMillis = lastInterstitialShownAtMillis,
                nowMillis = now
            )
        ) {
            _activeModerateInterstitial.value = ModerateAdCatalog.pickForSlot(interstitialSlotCursor++)
            completedActionsSinceLastAd = 0
            lastInterstitialShownAtMillis = now
            prefs.edit().putLong("last_moderate_interstitial_ad_ms", now).apply()
        }
    }

    fun setTrackerSelectedTech(tech: Technology) {
        _trackerSelectedTech.value = tech
        recordUserActivity()
    }

    // Screen Time & Study Timer State (tempo ativo real segundo a segundo com pausa automática por inatividade)
    private val _isAppInForeground = MutableStateFlow(true)
    @Volatile
    private var lastUserActivityMillis: Long = System.currentTimeMillis()

    private val _screenTimeMinutes = MutableStateFlow(prefs.getInt("total_screen_time_minutes", 0))
    val screenTimeMinutes: StateFlow<Int> = _screenTimeMinutes.asStateFlow()

    private val _todayScreenTimeSeconds = MutableStateFlow(DailyScreenTimeManager.loadOrResetTodaySeconds(prefs))
    val todayScreenTimeSeconds: StateFlow<Int> = _todayScreenTimeSeconds.asStateFlow()

    private val _todayScreenTimeMinutes = MutableStateFlow(_todayScreenTimeSeconds.value / 60)
    val todayScreenTimeMinutes: StateFlow<Int> = _todayScreenTimeMinutes.asStateFlow()

    private val _isStudyTimerActive = MutableStateFlow(false)
    val isStudyTimerActive: StateFlow<Boolean> = _isStudyTimerActive.asStateFlow()

    private val _completedCourseLessonIds = MutableStateFlow<Set<String>>(loadCompletedCourseLessonIds())
    val completedCourseLessonIds: StateFlow<Set<String>> = _completedCourseLessonIds.asStateFlow()

    val dailyStudyGoalState: StateFlow<DailyStudyGoalState> = combine(
        _todayScreenTimeSeconds,
        _screenTimeMinutes,
        _isStudyTimerActive
    ) { todaySeconds, totalMinutes, isTimerRunning ->
        DailyScreenTimeManager.buildGoalState(
            todaySeconds = todaySeconds,
            totalMinutes = totalMinutes,
            isTimerRunning = isTimerRunning
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailyScreenTimeManager.buildGoalState(
            todaySeconds = _todayScreenTimeSeconds.value,
            totalMinutes = _screenTimeMinutes.value,
            isTimerRunning = false
        )
    )

    fun recordUserActivity(nowMillis: Long = System.currentTimeMillis()) {
        lastUserActivityMillis = nowMillis
        if (_isAppInForeground.value && _currentDestination.value != AppDestination.LOGIN) {
            _isStudyTimerActive.value = true
        }
    }

    fun setAppInForeground(inForeground: Boolean) {
        _isAppInForeground.value = inForeground
        if (inForeground) {
            recordUserActivity()
            refreshDailyScreenTimeIfNewDay()
        } else {
            _isStudyTimerActive.value = false
        }
    }

    fun refreshDailyScreenTimeIfNewDay() {
        val secs = DailyScreenTimeManager.loadOrResetTodaySeconds(prefs)
        _todayScreenTimeSeconds.value = secs
        _todayScreenTimeMinutes.value = secs / 60
    }

    private val _isInitialAssessmentCompleted = MutableStateFlow(
        prefs.getBoolean("initial_assessment_completed", false)
    )
    val isInitialAssessmentCompleted: StateFlow<Boolean> = _isInitialAssessmentCompleted.asStateFlow()

    // Navigation state
    private val _currentDestination = MutableStateFlow(
        if (prefs.getString("auth_user_id", null) != null) {
            AppDestination.HOME
        } else {
            AppDestination.LOGIN
        }
    )
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _selectedTech = MutableStateFlow(Technology.PYTHON)
    val selectedTech: StateFlow<Technology> = _selectedTech.asStateFlow()

    // Language state
    private val _language = MutableStateFlow(
        AppLocaleHelper.loadSavedLanguage(prefs)
    )
    val language: StateFlow<Language> = _language.asStateFlow()

    // Theme & full color palette customization state (defaults to Purple & Crimson on first launch)
    private val _themeCustomization = MutableStateFlow(loadSavedThemeCustomization())
    val themeCustomization: StateFlow<ThemeCustomization> = _themeCustomization.asStateFlow()

    private val _themePreference = MutableStateFlow(_themeCustomization.value.themeMode)
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()

    // Accessibility, General App Preferences & Feedback Settings
    private val _accessibilitySettings = MutableStateFlow(loadSavedAccessibilitySettings())
    val accessibilitySettings: StateFlow<AccessibilitySettings> = _accessibilitySettings.asStateFlow()

    private val _appGeneralSettings = MutableStateFlow(loadSavedAppGeneralSettings())
    val appGeneralSettings: StateFlow<AppGeneralSettings> = _appGeneralSettings.asStateFlow()

    private val _userFeedbackNotes = MutableStateFlow(loadSavedFeedbackNotes())
    val userFeedbackNotes: StateFlow<List<UserFeedbackNote>> = _userFeedbackNotes.asStateFlow()

    // Calendar selected date
    private val _calendarSelectedDate = MutableStateFlow(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    val calendarSelectedDate: StateFlow<String> = _calendarSelectedDate.asStateFlow()

    private val _calendarTechFilter = MutableStateFlow<Technology?>(null)
    val calendarTechFilter: StateFlow<Technology?> = _calendarTechFilter.asStateFlow()

    // Sessions flow
    val allSessions: StateFlow<List<StudySession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Routines with events flow from Room
    val allRoutinesWithEvents: StateFlow<List<RoutineWithEvents>> = repository.allRoutinesWithEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Streak & progress
    val streakInfo: StateFlow<UiStreakInfo> = repository.allSessions.combine(repository.completedCount) { sessions, completedCount ->
        val completedSessions = sessions.filter { it.isCompleted }
        val totalMinutes = completedSessions.sumOf { it.durationMinutes }
        // Simple streak calculation based on dates completed
        val distinctDates = completedSessions.map { it.dateIso }.distinct().sortedDescending()
        var streak = 0
        var checkDate = LocalDate.now()
        for (dateStr in distinctDates) {
            val date = LocalDate.parse(dateStr)
            if (date == checkDate || date == checkDate.minusDays(1)) {
                streak++
                checkDate = date.minusDays(1)
            } else if (date < checkDate.minusDays(1)) {
                break
            }
        }
        UiStreakInfo(
            streakDays = if (streak == 0 && completedCount > 0) 1 else streak,
            completedCount = completedCount,
            totalMinutes = totalMinutes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiStreakInfo(0, 0, 0))

    // Gamification & Score system
    private val _scoreInfo = MutableStateFlow(loadInitialScoreInfo())
    val scoreInfo: StateFlow<UserScoreInfo> = _scoreInfo.asStateFlow()

    private val _pointsFeedback = MutableStateFlow<String?>(null)
    val pointsFeedback: StateFlow<String?> = _pointsFeedback.asStateFlow()

    // AI Chat state
    private val _aiMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _currentStreamingText = MutableStateFlow("")
    val currentStreamingText: StateFlow<String> = _currentStreamingText.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage: StateFlow<String?> = _aiErrorMessage.asStateFlow()

    // Weekly Study Summary AI Modal state
    private val _isWeeklySummaryModalOpen = MutableStateFlow(false)
    val isWeeklySummaryModalOpen: StateFlow<Boolean> = _isWeeklySummaryModalOpen.asStateFlow()

    private val _weeklySummaryState = MutableStateFlow<WeeklySummaryUiState>(WeeklySummaryUiState.Idle)
    val weeklySummaryState: StateFlow<WeeklySummaryUiState> = _weeklySummaryState.asStateFlow()

    fun openWeeklyStudySummaryModal(forceRefresh: Boolean = false) {
        _isWeeklySummaryModalOpen.value = true
        val current = _weeklySummaryState.value
        if (forceRefresh || current is WeeklySummaryUiState.Idle || current is WeeklySummaryUiState.Error) {
            generateWeeklyStudySummary()
        }
    }

    fun dismissWeeklyStudySummaryModal() {
        _isWeeklySummaryModalOpen.value = false
    }

    fun generateWeeklyStudySummary() {
        if (_weeklySummaryState.value is WeeklySummaryUiState.Loading) return
        val currentLang = _language.value
        val isPt = currentLang == Language.PT

        viewModelScope.launch {
            _weeklySummaryState.value = WeeklySummaryUiState.Loading
            try {
                val techScoresMap = Technology.entries.associateWith { getTechScore(it) }
                val unlockedBadgesList = allBadges.value.filter { it.isUnlocked }
                val unlockedDailySeals = unlockedBadgesList.count { it.category == "DAILY_TECH" }

                val feedback = aiService.generateWeeklyStudySummary(
                    weeklyHistory = _weeklyChallengeHistory.value,
                    scoreInfo = _scoreInfo.value,
                    streakDays = streakInfo.value.streakDays,
                    unlockedBadgesCount = unlockedBadgesList.size,
                    dailySealsUnlockedCount = unlockedDailySeals,
                    remainingLivesToday = _dailyLives.value.remaining,
                    techScores = techScoresMap,
                    language = currentLang
                )
                _weeklySummaryState.value = WeeklySummaryUiState.Success(feedback)
            } catch (e: TimeoutCancellationException) {
                android.util.Log.e("MainViewModel", "Weekly AI summary timed out", e)
                _weeklySummaryState.value = WeeklySummaryUiState.Error(
                    if (isPt) "A análise da IA expirou após 60 segundos. Verifique sua conexão e tente novamente."
                    else "AI summary generation timed out after 60 seconds. Please check your connection and try again."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Failed to generate weekly study summary", e)
                _weeklySummaryState.value = WeeklySummaryUiState.Error(
                    e.localizedMessage ?: if (isPt) "Não foi possível gerar seu resumo semanal agora."
                    else "Could not generate your weekly summary right now."
                )
            }
        }
    }

    // Audio Transcription state (gemini-3.5-transcribe)
    private val _transcriptionState = MutableStateFlow<AudioTranscriptionUiState>(AudioTranscriptionUiState.Idle)
    val transcriptionState: StateFlow<AudioTranscriptionUiState> = _transcriptionState.asStateFlow()

    fun setAudioRecordingState(isRecording: Boolean) {
        if (isRecording) {
            _transcriptionState.value = AudioTranscriptionUiState.Recording
        } else if (_transcriptionState.value is AudioTranscriptionUiState.Recording) {
            _transcriptionState.value = AudioTranscriptionUiState.Idle
        }
    }

    fun clearTranscriptionState() {
        _transcriptionState.value = AudioTranscriptionUiState.Idle
    }

    fun transcribeAudioPayload(audioBytes: ByteArray, mimeType: String, durationSeconds: Int) {
        val currentLang = _language.value
        val isPt = currentLang == Language.PT
        viewModelScope.launch {
            _transcriptionState.value = AudioTranscriptionUiState.Transcribing
            try {
                val transcript = aiService.transcribeAudio(
                    audioBytes = audioBytes,
                    mimeType = mimeType,
                    language = currentLang
                )
                _transcriptionState.value = AudioTranscriptionUiState.Success(
                    transcript = transcript,
                    durationSeconds = durationSeconds
                )
            } catch (e: TimeoutCancellationException) {
                android.util.Log.e("MainViewModel", "Audio transcription timed out", e)
                _transcriptionState.value = AudioTranscriptionUiState.Error(
                    if (isPt) "A transcrição expirou após 60 segundos. Tente gravar um trecho menor."
                    else "Audio transcription timed out after 60 seconds. Please try a shorter recording."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("MainViewModel", "Audio transcription failed", e)
                _transcriptionState.value = AudioTranscriptionUiState.Error(
                    e.localizedMessage ?: if (isPt) "Falha ao transcrever áudio com gemini-3.5-transcribe."
                    else "Failed to transcribe audio with gemini-3.5-transcribe."
                )
            }
        }
    }

    // SQLite SQL Database Studio & 3D Game Scores state (Room Database)
    val allStudyNotes: StateFlow<List<StudyNoteEntity>> = sqlLabRepository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topGameScores: StateFlow<List<GameScoreEntity>> = sqlLabRepository.topGameScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bestGameScore: StateFlow<Int> = sqlLabRepository.bestGameScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalGameCrystals: StateFlow<Int> = sqlLabRepository.totalGameCrystals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Loja de Vidas (pacotes em R$ a qualquer momento + troca de XP livre / Cristais 3D)
    private val _spentShopXp = MutableStateFlow(prefs.getInt("spent_shop_xp", 0).coerceAtLeast(0))
    private val _spentGameCrystals = MutableStateFlow(prefs.getInt("spent_game_crystals", 0).coerceAtLeast(0))
    private val _lastLifePurchaseReceipt = MutableStateFlow<LifePurchaseReceipt?>(null)
    val lastLifePurchaseReceipt: StateFlow<LifePurchaseReceipt?> = _lastLifePurchaseReceipt.asStateFlow()

    val availableShopXp: StateFlow<Int> = combine(_scoreInfo, _spentShopXp) { score, spent ->
        (score.totalScore - spent).coerceAtLeast(0)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        (_scoreInfo.value.totalScore - _spentShopXp.value).coerceAtLeast(0)
    )

    val availableGameCrystals: StateFlow<Int> = combine(totalGameCrystals, _spentGameCrystals) { total, spent ->
        (total - spent).coerceAtLeast(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun clearLastLifePurchaseReceipt() {
        _lastLifePurchaseReceipt.value = null
    }

    fun purchaseLives(
        pkg: LifeShopPackage,
        paymentMethod: LifePaymentMethod? = null
    ): LifePurchaseResult {
        val effectiveDailyMax = currentEffectiveDailyMax()
        val currentLives = DailyLivesManager.loadOrResetForDate(prefs, dailyMaxLives = effectiveDailyMax)
        _dailyLives.value = currentLives

        val currentAvailableXp = (_scoreInfo.value.totalScore - _spentShopXp.value).coerceAtLeast(0)
        val currentAvailableCrystals = (totalGameCrystals.value - _spentGameCrystals.value).coerceAtLeast(0)
        val isPt = _language.value == Language.PT

        val rejection = LifeShopCatalog.evaluatePurchaseEligibility(
            remainingLives = currentLives.remaining,
            pkg = pkg,
            availableXp = currentAvailableXp,
            availableCrystals = currentAvailableCrystals
        )
        if (rejection != null) {
            _pointsFeedback.value = when (rejection) {
                is LifePurchaseResult.RequiresRealBilling -> {
                    if (isPt) "Finalize o pagamento via Google Play Billing para receber este pacote."
                    else "Complete payment via Google Play Billing to receive this pack."
                }
                is LifePurchaseResult.QuotaStillAvailable -> {
                    if (isPt) "❤️ Você ainda tem ${rejection.remainingLives} vidas disponíveis hoje!"
                    else "❤️ You still have ${rejection.remainingLives} lives remaining today!"
                }
                is LifePurchaseResult.InsufficientFunds -> {
                    val currencyName = when (rejection.currency) {
                        LifePurchaseCurrency.BRL -> "R$"
                        LifePurchaseCurrency.XP -> "XP"
                        LifePurchaseCurrency.CRYSTALS -> if (isPt) "cristais 3D" else "3D crystals"
                    }
                    if (isPt) {
                        "Saldo insuficiente: precisa de ${rejection.required} $currencyName (você tem ${rejection.available})."
                    } else {
                        "Insufficient balance: requires ${rejection.required} $currencyName (you have ${rejection.available})."
                    }
                }
                is LifePurchaseResult.Success -> null
            }
            return rejection
        }

        when (pkg.currency) {
            LifePurchaseCurrency.BRL -> return LifePurchaseResult.RequiresRealBilling(pkg)
            LifePurchaseCurrency.XP -> {
                val updatedSpentXp = _spentShopXp.value + pkg.costAmount
                _spentShopXp.value = updatedSpentXp
                prefs.edit().putInt("spent_shop_xp", updatedSpentXp).apply()
            }
            LifePurchaseCurrency.CRYSTALS -> {
                val updatedSpentCrystals = _spentGameCrystals.value + pkg.costAmount
                _spentGameCrystals.value = updatedSpentCrystals
                prefs.edit().putInt("spent_game_crystals", updatedSpentCrystals).apply()
            }
        }

        val updatedLives = DailyLivesManager.addPurchasedLives(
            prefs = prefs,
            livesToAdd = pkg.livesAmount,
            dailyMaxLives = effectiveDailyMax
        )
        _dailyLives.value = updatedLives
        _lastLifePurchaseReceipt.value = LifePurchaseReceipt(
            pkg = pkg,
            newRemainingLives = updatedLives.remaining,
            paymentMethod = null
        )
        recordUserActivity()
        _pointsFeedback.value = if (isPt) {
            "❤️ +${pkg.livesAmount} vidas adicionadas (${pkg.formattedPrice(_language.value)})! Saldo: ${updatedLives.remaining}"
        } else {
            "❤️ +${pkg.livesAmount} lives added (${pkg.formattedPrice(_language.value)})! Balance: ${updatedLives.remaining}"
        }
        return LifePurchaseResult.Success(
            pkg = pkg,
            newRemainingLives = updatedLives.remaining,
            paymentMethod = null
        )
    }

    private val _sqlQueryResult = MutableStateFlow<SqlQueryResult?>(null)
    val sqlQueryResult: StateFlow<SqlQueryResult?> = _sqlQueryResult.asStateFlow()

    fun executeSqlQuery(query: String) {
        viewModelScope.launch {
            _sqlQueryResult.value = sqlLabRepository.executeReadOnlySql(query)
        }
    }

    fun addStudyNote(tech: Technology, title: String, content: String, isVoiceTranscribed: Boolean = false) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            sqlLabRepository.insertStudyNote(
                techId = tech.id,
                title = title,
                content = content,
                isVoiceTranscribed = isVoiceTranscribed
            )
            val isPt = _language.value == Language.PT
            _pointsFeedback.value = if (isPt) "📝 Nota salva no Banco SQL!" else "📝 Note saved to SQL Database!"
        }
    }

    fun deleteStudyNote(noteId: Long) {
        viewModelScope.launch {
            sqlLabRepository.deleteStudyNote(noteId)
        }
    }

    fun record3DGameScore(
        score: Int,
        maxCombo: Int,
        crystalsCollected: Int,
        durationSeconds: Int,
        techFocus: Technology
    ) {
        if (score <= 0 && crystalsCollected <= 0) return
        val playerName = _userAccount.value?.displayName ?: "DevPilot"
        viewModelScope.launch {
            sqlLabRepository.recordGameScore(
                playerName = playerName,
                score = score,
                maxCombo = maxCombo,
                crystalsCollected = crystalsCollected,
                durationSeconds = durationSeconds,
                techFocusId = techFocus.id
            )
            val bonusXp = (crystalsCollected * 6 + score / 60).coerceIn(5, 75)
            val current = _scoreInfo.value
            val newScore = current.totalScore + bonusXp
            val techKey = "score_tech_${techFocus.id}"
            val currentTechScore = prefs.getInt(techKey, 0)
            prefs.edit()
                .putInt("user_total_score", newScore)
                .putInt(techKey, currentTechScore + bonusXp)
                .apply()

            val nextScoreInfo = calculateScoreInfo(
                score = newScore,
                quizCount = current.correctQuizCount,
                actCount = current.completedActivitiesCount,
                actIds = current.completedActivityIds,
                quizIds = current.completedQuizIds
            )
            val leveledUp = applyScoreProgression(nextScoreInfo)
            val isPt = _language.value == Language.PT
            if (!leveledUp) {
                _pointsFeedback.value = if (isPt) {
                    "🎮 +$bonusXp XP salvos no Placar e no Banco SQL!"
                } else {
                    "🎮 +$bonusXp XP saved to Leaderboard & SQL Database!"
                }
            }
            if (_userAccount.value != null) {
                syncScoreToLeaderboard()
            }
            evaluateBadgesInternal()
        }
    }

    // Badges & Achievements state (Room Database)
    val allBadges: StateFlow<List<UserBadgeEntity>> = badgeRepository.allBadges
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unlockedBadgesCount: StateFlow<Int> = badgeRepository.unlockedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _newlyUnlockedBadge = MutableStateFlow<UserBadgeEntity?>(null)
    val newlyUnlockedBadge: StateFlow<UserBadgeEntity?> = _newlyUnlockedBadge.asStateFlow()

    fun dismissBadgeCelebration() {
        _newlyUnlockedBadge.value = null
    }

    fun triggerBadgeEvaluation() {
        viewModelScope.launch {
            evaluateBadgesInternal()
        }
    }

    private suspend fun currentUnlockedBadgeIds(): List<String> =
        badgeRepository.getUnlockedBadgeIds()

    private fun currentTechMinutesIdMap(): Map<String, Int> =
        progressRepository.getCumulativeTechMinutesMap().mapKeys { it.key.id }

    private suspend fun evaluateBadgesInternal() {
        val sessions = allSessions.value
        val streak = streakInfo.value.streakDays
        val score = scoreInfo.value
        val techScores = Technology.values().associateWith { getTechScore(it) }
        val hasAiRoutine = allRoutinesWithEvents.value.any { it.events.any { ev -> ev.aiGenerated } } || sessions.any { it.aiGenerated }
        val completedList = sessions.filter { it.isCompleted }
        val dailyCountsMap = progressRepository.getTodayChallengeCountsMap()
        val techMinutesMap = progressRepository.getCumulativeTechMinutesMap()
        val newlyUnlocked = badgeRepository.evaluateAndSyncBadges(
            streakDays = streak,
            completedSessions = completedList,
            scoreInfo = score,
            techScores = techScores,
            hasAiRoutine = hasAiRoutine,
            screenTimeMinutes = _screenTimeMinutes.value,
            isAssessmentCompleted = _isInitialAssessmentCompleted.value,
            weeklyDaysCount = streak.coerceAtMost(7),
            monthlySessionsCount = completedList.size,
            dailyChallengeCountsByTech = dailyCountsMap,
            techMinutes = techMinutesMap
        )
        if (newlyUnlocked.isNotEmpty()) {
            val bonusXp = newlyUnlocked.sumOf { it.xpReward }
            if (bonusXp > 0) {
                val latestScore = _scoreInfo.value
                val updatedTotal = latestScore.totalScore + bonusXp
                prefs.edit().putInt("user_total_score", updatedTotal).apply()
                applyScoreProgression(
                    calculateScoreInfo(
                        score = updatedTotal,
                        quizCount = latestScore.correctQuizCount,
                        actCount = latestScore.completedActivitiesCount,
                        actIds = latestScore.completedActivityIds,
                        quizIds = latestScore.completedQuizIds
                    )
                )
            }
            val badge = newlyUnlocked.last()
            val latestBadgeIds = currentUnlockedBadgeIds()
            prefs.edit().putInt("user_badges_count", latestBadgeIds.size).apply()
            _userAccount.value = _userAccount.value?.copy(badgesCount = latestBadgeIds.size)?.also {
                saveUserAccountToPrefs(it)
            }
            _newlyUnlockedBadge.value = badge
            val isPt = _language.value == Language.PT
            _pointsFeedback.value = if (isPt) {
                "🏆 Selo/Emblema Desbloqueado: ${badge.titlePt} (+${bonusXp} XP)!"
            } else {
                "🏆 Seal/Badge Unlocked: ${badge.titleEn} (+${bonusXp} XP)!"
            }
            if (_userAccount.value != null) {
                syncScoreToLeaderboard()
            }
        }
    }

    /**
     * Conclui o diagnóstico inicial do desenvolvedor, calibrando seu nível,
     * título e pontuação inicial, além de registrar o emblema de diagnóstico.
     */
    fun completeInitialAssessment(level: Int, titlePt: String, titleEn: String, xp: Int) {
        val alreadyAwardedXp = prefs.getBoolean("initial_assessment_xp_awarded", false)
        val grantedXp = if (alreadyAwardedXp) 0 else xp

        prefs.edit()
            .putBoolean("initial_assessment_completed", true)
            .putBoolean("initial_assessment_xp_awarded", true)
            .apply()
        _isInitialAssessmentCompleted.value = true

        val current = _scoreInfo.value
        val newScore = current.totalScore + grantedXp
        val nextScoreInfo = if (grantedXp > 0) {
            prefs.edit().putInt("user_total_score", newScore).apply()
            calculateScoreInfo(
                newScore,
                current.correctQuizCount,
                current.completedActivitiesCount,
                current.completedActivityIds,
                current.completedQuizIds
            ).also { applyScoreProgression(it) }
        } else {
            current
        }

        val user = _userAccount.value
        if (user != null) {
            val updated = user.copy(
                totalScore = newScore,
                levelNumber = nextScoreInfo.levelNumber,
                levelTitlePt = nextScoreInfo.levelTitlePt,
                levelTitleEn = nextScoreInfo.levelTitleEn
            )
            _userAccount.value = updated
            saveUserAccountToPrefs(updated)
            syncScoreToLeaderboard()
        }

        val isPt = _language.value == Language.PT
        _pointsFeedback.value = if (grantedXp > 0) {
            if (isPt) "🎉 Diagnóstico Concluído! Nível ${nextScoreInfo.levelNumber}: ${nextScoreInfo.levelTitlePt} (+${grantedXp} XP)"
            else "🎉 Diagnostic Completed! Level ${nextScoreInfo.levelNumber}: ${nextScoreInfo.levelTitleEn} (+${grantedXp} XP)"
        } else {
            if (isPt) "🧭 Diagnóstico recalibrado! Nível ${nextScoreInfo.levelNumber}: ${nextScoreInfo.levelTitlePt}"
            else "🧭 Diagnostic recalibrated! Level ${nextScoreInfo.levelNumber}: ${nextScoreInfo.levelTitleEn}"
        }
        triggerBadgeEvaluation()
    }

    fun resetInitialAssessment() {
        prefs.edit().putBoolean("initial_assessment_completed", false).apply()
        _isInitialAssessmentCompleted.value = false
    }

    /**
     * Converte URIs locais antigas (`file://` / `content://`) em Data URI Base64 compartilhável
     * para que a foto do usuário funcione no placar global de qualquer aparelho.
     */
    private suspend fun ensureShareablePhotoUrl(rawPhotoUrl: String): String {
        val clean = rawPhotoUrl.trim()
        if (clean.isBlank()) return ""
        if (AvatarImageCodec.isShareableAvatarUrl(clean)) return clean
        if (AvatarImageCodec.isLocalFileOrContentUri(clean)) {
            val encoded = withContext(Dispatchers.IO) {
                AvatarImageCodec.encodeUriToDataUrl(getApplication(), Uri.parse(clean))
            }
            if (!encoded.isNullOrBlank()) {
                return encoded
            }
        }
        return ""
    }

    /**
     * Atualiza a foto de perfil do usuário (galeria, foto remota ou preset temático de dev)
     * e sincroniza imediatamente no Cloud Firestore para todos os usuários.
     */
    fun updateUserProfilePhoto(newPhotoUrl: String) {
        viewModelScope.launch {
            val resolvedPhotoUrl = ensureShareablePhotoUrl(newPhotoUrl)
            prefs.edit().putString("auth_photo", resolvedPhotoUrl).apply()
            val user = _userAccount.value
            val updated = if (user != null) {
                user.copy(photoUrl = resolvedPhotoUrl)
            } else {
                UserAccount(
                    userId = prefs.getString("auth_user_id", "") ?: "",
                    displayName = prefs.getString("auth_display_name", "Dev") ?: "Dev",
                    photoUrl = resolvedPhotoUrl,
                    isLoggedIn = prefs.contains("auth_user_id")
                )
            }
            _userAccount.value = updated
            saveUserAccountToPrefs(updated)

            val isPt = _language.value == Language.PT
            if (updated.isLoggedIn && updated.userId.isNotBlank()) {
                val score = _scoreInfo.value
                val allTechScoresMap = Technology.entries.associate { it.id to getTechScore(it) }
                val synced = leaderboardRepository.syncUserToLeaderboard(
                    account = updated,
                    completedActivityIds = score.completedActivityIds,
                    completedQuizIds = score.completedQuizIds,
                    techScores = allTechScoresMap,
                    themeConfig = _themeCustomization.value
                )
                publishCurrentDirectoryProfileIfSignedIn()
                _pointsFeedback.value = if (synced) {
                    if (isPt) "Foto atualizada e visível para todos no Placar!"
                    else "Profile photo updated and visible to everyone on the Leaderboard!"
                } else {
                    if (isPt) "Foto salva localmente. Toque em Sincronizar no Placar para publicar."
                    else "Photo saved locally. Tap Sync on Leaderboard to publish."
                }
            } else {
                _pointsFeedback.value = if (isPt) "Foto salva! Entre na sua conta para exibi-la no Placar."
                else "Photo saved! Sign in to show it on the global Leaderboard."
            }
        }
    }

    init {
        playBillingManager.startConnectionAndSync(userInitiated = false)
        viewModelScope.launch {
            repository.populateStarterRoutinesIfEmpty()
            badgeRepository.initializeBadgesIfEmpty()
            sqlLabRepository.populateStarterNotesIfEmpty()
            triggerBadgeEvaluation()
        }
        setupInitialAiGreeting()
        StudyReminderWorker.createNotificationChannel(application)
        if (_appGeneralSettings.value.dailyRemindersEnabled) {
            NotificationScheduler.scheduleDailyStudyCheck(application)
        }

        // Cronômetro automático de tempo de estudo baseado em atividade real do usuário (segundo a segundo)
        viewModelScope.launch {
            while (isActive) {
                delay(1_000L)
                val isOnLoginGate = _currentDestination.value == AppDestination.LOGIN
                val idleMs = _appGeneralSettings.value.idlePauseMinutes.coerceIn(1, 10) * 60_000L
                val activeNow = !isOnLoginGate && DailyScreenTimeManager.isUserActive(
                    lastActivityMillis = lastUserActivityMillis,
                    nowMillis = System.currentTimeMillis(),
                    isInForeground = _isAppInForeground.value,
                    idleTimeoutMs = idleMs
                )
                _isStudyTimerActive.value = activeNow
                if (!activeNow) continue

                val tick = DailyScreenTimeManager.advanceOneActiveSecond(prefs)
                _todayScreenTimeSeconds.value = tick.todaySeconds
                _todayScreenTimeMinutes.value = tick.todayMinutes
                _screenTimeMinutes.value = tick.totalMinutes

                if (tick.completedNewMinute) {
                    val uid = _userAccount.value?.userId ?: ""
                    val activeTech = if (_currentDestination.value == AppDestination.TECH_DETAIL) {
                        _selectedTech.value
                    } else {
                        _trackerSelectedTech.value
                    }
                    progressRepository.recordRealScreenTimeMinute(uid, activeTech)

                    val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val updatedLocal = progressRepository.getLocalProgress(todayIso, activeTech.id)
                    _todayLanguageProgressMap.value = _todayLanguageProgressMap.value.toMutableMap().apply {
                        put(activeTech.id, updatedLocal)
                    }

                    // Bonificação automática controlada por marcos reais de 15 min em uso (máx 4x/dia)
                    if (tick.awardedFocusMilestoneBonus) {
                        val current = _scoreInfo.value
                        val newScore = current.totalScore + 25
                        prefs.edit().putInt("user_total_score", newScore).apply()
                        val leveledUp = applyScoreProgression(
                            calculateScoreInfo(
                                newScore,
                                current.correctQuizCount,
                                current.completedActivitiesCount,
                                current.completedActivityIds,
                                current.completedQuizIds
                            )
                        )
                        val isPt = _language.value == Language.PT
                        if (!leveledUp) {
                            _pointsFeedback.value = if (isPt) {
                                "⏱️ +25 XP bônus por ${tick.todayMinutes} min de tempo de tela ativo hoje!"
                            } else {
                                "⏱️ +25 XP bonus for ${tick.todayMinutes} min of active screen time today!"
                            }
                        }
                        triggerBadgeEvaluation()
                        syncScoreToLeaderboard()
                    } else {
                        triggerBadgeEvaluation()
                    }
                }
            }
        }

        // Observe streak & session changes to evaluate badges
        viewModelScope.launch {
            streakInfo.collect {
                triggerBadgeEvaluation()
            }
        }

        _userAccount.value = loadSavedUserAccount()
        _userAccount.value?.let { acc ->
            viewModelScope.launch {
                restoreUserProgressFromCloud(acc.userId)
                // Se a foto salva localmente ainda era uma URI local antiga (`file://`), converte e publica na nuvem
                val currentPhoto = _userAccount.value?.photoUrl.orEmpty()
                if (AvatarImageCodec.isLocalFileOrContentUri(currentPhoto)) {
                    val upgraded = ensureShareablePhotoUrl(currentPhoto)
                    if (upgraded.isNotBlank()) {
                        prefs.edit().putString("auth_photo", upgraded).apply()
                        val updatedAcc = (_userAccount.value ?: acc).copy(photoUrl = upgraded)
                        _userAccount.value = updatedAcc
                        saveUserAccountToPrefs(updatedAcc)
                        val score = _scoreInfo.value
                        val allTechScoresMap = Technology.entries.associate { it.id to getTechScore(it) }
                        leaderboardRepository.syncUserToLeaderboard(
                            account = updatedAcc,
                            completedActivityIds = score.completedActivityIds,
                            completedQuizIds = score.completedQuizIds,
                            techScores = allTechScoresMap,
                            themeConfig = _themeCustomization.value,
                            techMinutes = currentTechMinutesIdMap(),
                            unlockedBadgeIds = currentUnlockedBadgeIds()
                        )
                    }
                }
            }
        }
        restartLeaderboardObservation(_userAccount.value)
        restartDailyProgressObservation(_userAccount.value)
        restartTargetSubjectsObservation()
        restartChatSubsystem()
    }

    private fun setupInitialAiGreeting() {
        val isPt = _language.value == Language.PT
        val greeting = if (isPt) {
            "Olá! Sou o seu **DevTutor AI** 🚀.\n\nPosso te ajudar a dominar **Python, SQL, HTML, CSS, Java, MySQL, PostgreSQL e MongoDB**!\n\nPosso criar uma **rotina de estudos personalizada** para você e adicioná-la direto no seu **Calendário**. O que você deseja aprender hoje?"
        } else {
            "Hello! I am your **DevTutor AI** 🚀.\n\nI can help you master **Python, SQL, HTML, CSS, Java, MySQL, PostgreSQL, and MongoDB**!\n\nI can create a **personalized study routine** for you and add it directly to your **Calendar**. What would you like to learn today?"
        }
        _aiMessages.value = listOf(AiChatMessage(isUser = false, content = greeting))
    }

    fun navigateTo(dest: AppDestination) {
        _currentDestination.value = dest
        recordUserActivity()
    }

    fun openTechDetail(tech: Technology) {
        _selectedTech.value = tech
        _trackerSelectedTech.value = tech
        _currentDestination.value = AppDestination.TECH_DETAIL
        recordUserActivity()
    }

    fun toggleLanguage() {
        val next = if (_language.value == Language.PT) Language.EN else Language.PT
        setLanguage(next)
    }

    fun setLanguage(lang: Language) {
        if (_language.value == lang && AppLocaleHelper.loadSavedLanguage(prefs) == lang) return
        _language.value = lang
        AppLocaleHelper.saveLanguage(prefs, lang)
        // Refresh greeting if only 1 message exists
        if (_aiMessages.value.size <= 1) {
            setupInitialAiGreeting()
        }
        // Reset cached weekly AI summary so next open renders in the newly selected language
        _weeklySummaryState.value = WeeklySummaryUiState.Idle
        val localizedCtx = AppLocaleHelper.createLocalizedContext(getApplication(), lang)
        val langLabel = localizedCtx.getString(
            if (lang == Language.PT) R.string.lang_name_pt else R.string.lang_name_en
        )
        _pointsFeedback.value = localizedCtx.getString(R.string.lang_switched_feedback, langLabel)
    }

    fun updateAccessibilitySettings(updated: AccessibilitySettings) {
        _accessibilitySettings.value = updated
        prefs.edit()
            .putFloat("a11y_font_scale", updated.fontScalePreset.scale)
            .putBoolean("a11y_high_contrast", updated.highContrastBorders)
            .putBoolean("a11y_reduce_motion", updated.reduceMotion)
            .putBoolean("a11y_haptic_feedback", updated.hapticFeedbackEnabled)
            .apply()
    }

    fun resetAccessibilitySettings() {
        updateAccessibilitySettings(AccessibilitySettings())
        val localizedCtx = AppLocaleHelper.createLocalizedContext(getApplication(), _language.value)
        _pointsFeedback.value = localizedCtx.getString(R.string.settings_accessibility_reset_feedback)
    }

    fun updateAppGeneralSettings(updated: AppGeneralSettings) {
        val prevReminders = _appGeneralSettings.value.dailyRemindersEnabled
        _appGeneralSettings.value = updated
        prefs.edit()
            .putBoolean("gen_daily_reminders", updated.dailyRemindersEnabled)
            .putInt("gen_idle_pause_min", updated.idlePauseMinutes.coerceIn(1, 10))
            .putBoolean("fb_celebration_dialogs", updated.showCelebrationDialogs)
            .putBoolean("fb_gentle_retry_tips", updated.showGentleRetryTips)
            .putBoolean("fb_xp_toast_banners", updated.showXpToastBanners)
            .apply()

        if (prevReminders != updated.dailyRemindersEnabled) {
            if (updated.dailyRemindersEnabled) {
                NotificationScheduler.scheduleDailyStudyCheck(getApplication())
            } else {
                NotificationScheduler.cancelDailyStudyCheck(getApplication())
            }
        }
    }

    fun submitUserAppFeedback(category: String, rating: Int, message: String) {
        val cleanMsg = message.trim()
        if (cleanMsg.isBlank()) return
        val newNote = UserFeedbackNote(
            id = java.util.UUID.randomUUID().toString(),
            category = category,
            rating = rating.coerceIn(1, 5),
            message = cleanMsg,
            createdAtIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
        val updatedList = (listOf(newNote) + _userFeedbackNotes.value).take(20)
        _userFeedbackNotes.value = updatedList
        saveFeedbackNotes(updatedList)
        val isPt = _language.value == Language.PT
        _pointsFeedback.value = if (isPt) {
            "💜 Feedback registrado! Obrigado por ajudar a evoluir o DevRoutine."
        } else {
            "💜 Feedback saved! Thank you for helping improve DevRoutine."
        }
    }

    fun deleteUserAppFeedback(id: String) {
        val updatedList = _userFeedbackNotes.value.filterNot { it.id == id }
        _userFeedbackNotes.value = updatedList
        saveFeedbackNotes(updatedList)
    }

    private fun loadSavedAccessibilitySettings(): AccessibilitySettings {
        val fontScale = prefs.getFloat("a11y_font_scale", FontScalePreset.STANDARD.scale)
        return AccessibilitySettings(
            fontScalePreset = FontScalePreset.fromScale(fontScale),
            highContrastBorders = prefs.getBoolean("a11y_high_contrast", false),
            reduceMotion = prefs.getBoolean("a11y_reduce_motion", false),
            hapticFeedbackEnabled = prefs.getBoolean("a11y_haptic_feedback", true)
        )
    }

    private fun loadSavedAppGeneralSettings(): AppGeneralSettings {
        return AppGeneralSettings(
            dailyRemindersEnabled = prefs.getBoolean("gen_daily_reminders", true),
            idlePauseMinutes = prefs.getInt("gen_idle_pause_min", 2).coerceIn(1, 10),
            showCelebrationDialogs = prefs.getBoolean("fb_celebration_dialogs", true),
            showGentleRetryTips = prefs.getBoolean("fb_gentle_retry_tips", true),
            showXpToastBanners = prefs.getBoolean("fb_xp_toast_banners", true)
        )
    }

    private fun loadSavedFeedbackNotes(): List<UserFeedbackNote> {
        val raw = prefs.getString("user_feedback_notes_json", null) ?: return emptyList()
        return try {
            val arr = org.json.JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        UserFeedbackNote(
                            id = obj.optString("id"),
                            category = obj.optString("category"),
                            rating = obj.optInt("rating", 5),
                            message = obj.optString("message"),
                            createdAtIso = obj.optString("createdAtIso")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveFeedbackNotes(notes: List<UserFeedbackNote>) {
        val arr = org.json.JSONArray()
        notes.forEach { n ->
            arr.put(
                org.json.JSONObject().apply {
                    put("id", n.id)
                    put("category", n.category)
                    put("rating", n.rating)
                    put("message", n.message)
                    put("createdAtIso", n.createdAtIso)
                }
            )
        }
        prefs.edit().putString("user_feedback_notes_json", arr.toString()).apply()
    }

    fun setThemePreference(pref: ThemePreference) {
        val updated = _themeCustomization.value.copy(
            themeMode = pref,
            updatedAtMillis = System.currentTimeMillis()
        )
        applyAndPersistThemeCustomization(updated, markUserModified = true)
    }

    fun updateThemeCustomization(customization: ThemeCustomization) {
        val updated = customization.copy(updatedAtMillis = System.currentTimeMillis())
        applyAndPersistThemeCustomization(updated, markUserModified = true)
    }

    fun selectThemePreset(preset: ThemePreset) {
        val current = _themeCustomization.value
        val updated = ThemeCustomization.fromPreset(
            preset = preset,
            themeMode = current.themeMode,
            surfaceStyle = current.surfaceStyle,
            backgroundTintIntensity = current.backgroundTintIntensity
        )
        applyAndPersistThemeCustomization(updated, markUserModified = true)
    }

    fun resetThemeCustomizationToDefault() {
        val updated = ThemeCustomization(
            themeMode = ThemePreference.DARK,
            presetId = ThemeCustomization.DEFAULT_PRESET_ID,
            primaryColorHex = ThemeCustomization.DEFAULT_PRIMARY_HEX,
            secondaryColorHex = ThemeCustomization.DEFAULT_SECONDARY_HEX,
            tertiaryColorHex = ThemeCustomization.DEFAULT_TERTIARY_HEX,
            surfaceStyle = ThemeSurfaceStyle.TINTED,
            backgroundTintIntensity = ThemeCustomization.DEFAULT_BG_TINT_INTENSITY,
            updatedAtMillis = System.currentTimeMillis()
        )
        applyAndPersistThemeCustomization(updated, markUserModified = false)
        val isPt = _language.value == Language.PT
        _pointsFeedback.value = if (isPt) "🎨 Tema padrão (Roxo & Carmesim) restaurado!"
        else "🎨 Default theme (Purple & Crimson) restored!"
    }

    private fun applyAndPersistThemeCustomization(
        customization: ThemeCustomization,
        markUserModified: Boolean
    ) {
        _themeCustomization.value = customization
        _themePreference.value = customization.themeMode
        saveThemeCustomizationLocally(customization, markUserModified)

        val account = _userAccount.value
        if (account != null && account.isLoggedIn && account.userId.isNotBlank()) {
            viewModelScope.launch {
                leaderboardRepository.saveUserThemeConfig(account, customization)
            }
        }
    }

    private fun loadSavedThemeCustomization(): ThemeCustomization {
        val mode = ThemePreference.fromCode(prefs.getString("app_theme", "dark"))
        val presetId = prefs.getString("theme_preset_id", ThemeCustomization.DEFAULT_PRESET_ID)
            ?: ThemeCustomization.DEFAULT_PRESET_ID
        val primary = prefs.getLong("theme_primary_hex", ThemeCustomization.DEFAULT_PRIMARY_HEX)
        val secondary = prefs.getLong("theme_secondary_hex", ThemeCustomization.DEFAULT_SECONDARY_HEX)
        val tertiary = prefs.getLong("theme_tertiary_hex", ThemeCustomization.DEFAULT_TERTIARY_HEX)
        val surface = ThemeSurfaceStyle.fromId(prefs.getString("theme_surface_style", ThemeSurfaceStyle.TINTED.id))
        val tint = prefs.getFloat("theme_bg_tint_intensity", ThemeCustomization.DEFAULT_BG_TINT_INTENSITY)
            .coerceIn(0f, 1f)
        val updatedAt = prefs.getLong("theme_updated_at", 0L)

        return ThemeCustomization(
            themeMode = mode,
            presetId = presetId,
            primaryColorHex = primary,
            secondaryColorHex = secondary,
            tertiaryColorHex = tertiary,
            surfaceStyle = surface,
            backgroundTintIntensity = tint,
            updatedAtMillis = updatedAt
        )
    }

    private fun saveThemeCustomizationLocally(
        customization: ThemeCustomization,
        markUserModified: Boolean
    ) {
        prefs.edit()
            .putString("app_theme", customization.themeMode.code)
            .putString("theme_preset_id", customization.presetId)
            .putLong("theme_primary_hex", customization.primaryColorHex)
            .putLong("theme_secondary_hex", customization.secondaryColorHex)
            .putLong("theme_tertiary_hex", customization.tertiaryColorHex)
            .putString("theme_surface_style", customization.surfaceStyle.id)
            .putFloat("theme_bg_tint_intensity", customization.backgroundTintIntensity)
            .putLong("theme_updated_at", customization.updatedAtMillis)
            .putBoolean("theme_user_modified", markUserModified)
            .apply()
    }

    fun setCalendarDate(dateIso: String) {
        _calendarSelectedDate.value = dateIso
    }

    fun setCalendarFilter(tech: Technology?) {
        _calendarTechFilter.value = tech
    }

    fun toggleSession(session: StudySession) {
        viewModelScope.launch {
            repository.toggleCompleted(session)
            triggerBadgeEvaluation()
        }
    }

    fun addManualSession(session: StudySession) {
        viewModelScope.launch {
            repository.insertSession(session)
            NotificationScheduler.scheduleSessionReminder(getApplication(), session)
        }
    }

    fun deleteSession(session: StudySession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun saveRoutinePlanToCalendar(plan: RoutinePlan) {
        viewModelScope.launch {
            repository.saveAiRoutine(plan)
            triggerBadgeEvaluation()
            // Schedule notification for each generated session
            plan.sessions.forEach { session ->
                NotificationScheduler.scheduleSessionReminder(getApplication(), session)
            }
            val isPt = _language.value == Language.PT
            val confirmMsg = if (isPt) {
                "✅ Sucesso! Adicionei **${plan.sessions.size} sessões de estudo** da sua rotina ao Calendário e agendei lembretes locais com antecedência. Você já pode vê-las na aba **Calendário**!"
            } else {
                "✅ Success! Added **${plan.sessions.size} study sessions** to your Calendar and scheduled local push reminders in advance. Check them out in the **Calendar** tab!"
            }
            _aiMessages.value = _aiMessages.value + AiChatMessage(isUser = false, content = confirmMsg)
        }
    }

    fun sendTestReminderNotification() {
        NotificationScheduler.sendImmediateTestNotification(getApplication(), _language.value == Language.PT)
    }

    fun sendAiUserMessage(userPrompt: String) {
        if (userPrompt.isBlank() || _isAiLoading.value) return

        val userMsg = AiChatMessage(isUser = true, content = userPrompt)
        _aiMessages.value = _aiMessages.value + userMsg

        viewModelScope.launch {
            _isAiLoading.value = true
            _aiErrorMessage.value = null
            _currentStreamingText.value = ""

            val history = _aiMessages.value.map { Pair(it.isUser, it.content) }
            val accumulated = StringBuilder()

            try {
                aiService.streamChatResponse(
                    historyMessages = history,
                    userPrompt = userPrompt,
                    language = _language.value,
                    selectedTech = _selectedTech.value
                ).collect { chunk ->
                    accumulated.append(chunk)
                    _currentStreamingText.value = accumulated.toString()
                }

                val fullResponse = accumulated.toString()
                _aiMessages.value = _aiMessages.value + AiChatMessage(
                    isUser = false,
                    content = if (fullResponse.isNotBlank()) fullResponse else "..."
                )
            } catch (e: Exception) {
                val errorText = e.localizedMessage ?: "Erro na resposta da IA."
                _aiErrorMessage.value = errorText
                val isPt = _language.value == Language.PT
                val fallbackNotice = if (isPt) {
                    "⚠️ Ops, ocorreu uma instabilidade na conexão: $errorText. Você pode tentar novamente!"
                } else {
                    "⚠️ Connection issue: $errorText. Please try again!"
                }
                _aiMessages.value = _aiMessages.value + AiChatMessage(isUser = false, content = fallbackNotice)
            } finally {
                _isAiLoading.value = false
                _currentStreamingText.value = ""
            }
        }
    }

    fun generateRoutineWithAi(
        goalDescription: String,
        selectedTechs: List<Technology>,
        daysCount: Int,
        minutesPerDay: Int
    ) {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiErrorMessage.value = null
            val isPt = _language.value == Language.PT

            val userMsg = AiChatMessage(
                isUser = true,
                content = if (isPt) "Crie uma rotina de estudos de $daysCount dias para ${selectedTechs.joinToString { it.displayName }} ($minutesPerDay min/dia): $goalDescription"
                else "Create a $daysCount-day study routine for ${selectedTechs.joinToString { it.displayName }} ($minutesPerDay min/day): $goalDescription"
            )
            _aiMessages.value = _aiMessages.value + userMsg

            try {
                val plan = aiService.generateStudyRoutinePlan(
                    goalDescription = goalDescription,
                    selectedTechs = selectedTechs,
                    daysCount = daysCount,
                    minutesPerDay = minutesPerDay,
                    language = _language.value
                )

                val aiMsg = AiChatMessage(
                    isUser = false,
                    content = if (isPt) {
                        "🎯 **${plan.titlePt}**\n\n${plan.descriptionPt}\n\nMontei um cronograma com **${plan.sessions.size} sessões práticas** adaptadas à sua meta. Clique no botão abaixo para adicionar todas ao seu Calendário!"
                    } else {
                        "🎯 **${plan.titleEn}**\n\n${plan.descriptionEn}\n\nI generated **${plan.sessions.size} practical sessions** tailored to your goal. Tap the button below to add them all to your Calendar!"
                    },
                    suggestedPlan = plan
                )
                _aiMessages.value = _aiMessages.value + aiMsg
            } catch (e: Exception) {
                _aiErrorMessage.value = e.localizedMessage
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun promptAiAboutTech(tech: Technology) {
        _selectedTech.value = tech
        _currentDestination.value = AppDestination.AI_CHAT
        val isPt = _language.value == Language.PT
        val prompt = if (isPt) {
            "Gostaria de estudar ${tech.displayName} (${tech.categoryPt}). Pode me explicar os conceitos principais e me dar um roteiro de aprendizado?"
        } else {
            "I would like to study ${tech.displayName} (${tech.categoryEn}). Can you explain the core concepts and give me a learning path?"
        }
        sendAiUserMessage(prompt)
    }

    fun promptAiAboutTopic(tech: Technology, topicTitle: String) {
        _selectedTech.value = tech
        _currentDestination.value = AppDestination.AI_CHAT
        val isPt = _language.value == Language.PT
        val prompt = if (isPt) {
            "Olá DevTutor! Quero focar no tópico '$topicTitle' em ${tech.displayName}. Pode me ensinar o conceito essencial de forma clara, com um exemplo prático de código e me propor um desafio rápido para testar meu aprendizado?"
        } else {
            "Hello DevTutor! I want to focus on '$topicTitle' in ${tech.displayName}. Can you teach me the core concept clearly, with a practical code example, and give me a quick challenge to test my understanding?"
        }
        sendAiUserMessage(prompt)
    }

    private fun loadInitialScoreInfo(): UserScoreInfo {
        val total = prefs.getInt("user_total_score", 0)
        val quizCount = prefs.getInt("user_correct_quiz_count", 0)
        val actCount = prefs.getInt("user_completed_act_count", 0)
        val completedActSet = prefs.getStringSet("completed_act_ids", emptySet()) ?: emptySet()
        val completedQuizSetRaw = prefs.getStringSet("completed_quiz_ids", emptySet()) ?: emptySet()
        val completedQuizSet = completedQuizSetRaw.mapNotNull { it.toIntOrNull() }.toSet()

        return calculateScoreInfo(
            score = total,
            quizCount = quizCount,
            actCount = actCount,
            actIds = completedActSet,
            quizIds = completedQuizSet
        )
    }

    private fun calculateScoreInfo(
        score: Int,
        quizCount: Int,
        actCount: Int,
        actIds: Set<String>,
        quizIds: Set<Int>
    ): UserScoreInfo {
        return DeveloperLevelCatalog.calculateScoreInfo(
            score = score,
            quizCount = quizCount,
            actCount = actCount,
            actIds = actIds,
            quizIds = quizIds
        )
    }

    /**
     * Atualiza pontuação e avalia subida de nível no sistema expandido de 12 níveis.
     * Sempre que o usuário sobe de nível, suas 25 vidas são imediatamente restauradas!
     */
    private fun applyScoreProgression(
        nextScoreInfo: UserScoreInfo,
        allowLevelUpReward: Boolean = true
    ): Boolean {
        val previousLevel = _scoreInfo.value.levelNumber
        _scoreInfo.value = nextScoreInfo

        val storedLastLevel = prefs.getInt("user_last_known_level", previousLevel)
        val newLevel = nextScoreInfo.levelNumber
        val leveledUp = allowLevelUpReward && newLevel > maxOf(previousLevel, storedLastLevel)

        prefs.edit().putInt("user_last_known_level", maxOf(storedLastLevel, newLevel)).apply()

        _userAccount.value?.let { acc ->
            val updatedAcc = acc.copy(
                totalScore = nextScoreInfo.totalScore,
                levelNumber = nextScoreInfo.levelNumber,
                levelTitlePt = nextScoreInfo.levelTitlePt,
                levelTitleEn = nextScoreInfo.levelTitleEn
            )
            _userAccount.value = updatedAcc
            saveUserAccountToPrefs(updatedAcc)
        }

        if (leveledUp) {
            val effectiveMax = currentEffectiveDailyMax()
            val restoredLives = DailyLivesManager.resetLivesForLevelUp(
                prefs = prefs,
                dailyMaxLives = effectiveMax
            )
            _dailyLives.value = restoredLives
            val tier = DeveloperLevelCatalog.resolveForXp(nextScoreInfo.totalScore)
            _levelUpEvent.value = LevelUpEvent(
                oldLevel = previousLevel,
                newLevel = tier.levelNumber,
                titlePt = tier.titlePt,
                titleEn = tier.titleEn,
                rankBadgePt = tier.rankBadgePt,
                rankBadgeEn = tier.rankBadgeEn,
                totalXp = nextScoreInfo.totalScore,
                nextLevelTarget = nextScoreInfo.nextLevelTarget,
                livesRestored = effectiveMax
            )
            val isPt = _language.value == Language.PT
            _pointsFeedback.value = if (isPt) {
                "🎉 Subiu para o Nível ${tier.levelNumber}: ${tier.titlePt}! ❤️ ${effectiveMax}/${effectiveMax} vidas restauradas!"
            } else {
                "🎉 Level Up! Level ${tier.levelNumber}: ${tier.titleEn}! ❤️ ${effectiveMax}/${effectiveMax} lives restored!"
            }
        }
        return leveledUp
    }

    fun awardQuizPoints(tech: Technology, questionId: Int, points: Int = 20) {
        recordUserActivity()
        val current = _scoreInfo.value
        val isFirstTime = !current.completedQuizIds.contains(questionId)
        val pointsToAdd = if (isFirstTime) points else 2 // Reduced bonus for repeating already-answered quizzes

        val newQuizIds = current.completedQuizIds + questionId
        val newScore = current.totalScore + pointsToAdd
        val newQuizCount = if (isFirstTime) current.correctQuizCount + 1 else current.correctQuizCount

        // Persist
        val techKey = "score_tech_${tech.id}"
        val currentTechScore = prefs.getInt(techKey, 0)
        prefs.edit()
            .putInt("user_total_score", newScore)
            .putInt("user_correct_quiz_count", newQuizCount)
            .putStringSet("completed_quiz_ids", newQuizIds.map { it.toString() }.toSet())
            .putInt(techKey, currentTechScore + pointsToAdd)
            .apply()

        val leveledUp = applyScoreProgression(
            calculateScoreInfo(
                score = newScore,
                quizCount = newQuizCount,
                actCount = current.completedActivitiesCount,
                actIds = current.completedActivityIds,
                quizIds = newQuizIds
            )
        )

        val isPt = _language.value == Language.PT
        if (!leveledUp) {
            _pointsFeedback.value = if (isPt) "+$pointsToAdd XP! Resposta Correta 🎯" else "+$pointsToAdd XP! Correct Answer 🎯"
        }
        triggerBadgeEvaluation()
        maybeTriggerModerateInterstitialAd()
        val user = _userAccount.value
        if (user != null) {
            syncScoreToLeaderboard()
            viewModelScope.launch {
                val techXp = prefs.getInt("score_tech_${tech.id}", 0)
                val todayChallenges = progressRepository.getTodayChallengeCountsMap()[tech] ?: 0
                progressRepository.recordLanguageProgress(
                    userId = user.userId,
                    tech = tech,
                    completedChallenges = todayChallenges,
                    completedQuizzes = newQuizCount,
                    xpEarned = techXp
                )
            }
        }
    }

    fun awardActivityPoints(tech: Technology, activityId: String, points: Int = 50) {
        recordUserActivity()
        val current = _scoreInfo.value
        val isFirstTime = !current.completedActivityIds.contains(activityId)
        val pointsToAdd = if (isFirstTime) points else 3 // Reduced bonus for repeating already-completed activities

        val newActIds = current.completedActivityIds + activityId
        val newScore = current.totalScore + pointsToAdd
        val newActCount = if (isFirstTime) current.completedActivitiesCount + 1 else current.completedActivitiesCount

        // Persist
        val techKey = "score_tech_${tech.id}"
        val currentTechScore = prefs.getInt(techKey, 0)
        prefs.edit()
            .putInt("user_total_score", newScore)
            .putInt("user_completed_act_count", newActCount)
            .putStringSet("completed_act_ids", newActIds)
            .putInt(techKey, currentTechScore + pointsToAdd)
            .apply()

        val leveledUp = applyScoreProgression(
            calculateScoreInfo(
                score = newScore,
                quizCount = current.correctQuizCount,
                actCount = newActCount,
                actIds = newActIds,
                quizIds = current.completedQuizIds
            )
        )

        val isPt = _language.value == Language.PT
        if (!leveledUp) {
            _pointsFeedback.value = if (isPt) "+$pointsToAdd XP! Atividade Concluída 🚀" else "+$pointsToAdd XP! Activity Completed 🚀"
        }

        val user = _userAccount.value
        viewModelScope.launch {
            val techXp = prefs.getInt("score_tech_${tech.id}", 0)
            progressRepository.recordCompletedDailyChallenge(
                userId = user?.userId ?: "",
                tech = tech,
                activityId = activityId,
                xpEarned = techXp,
                completedQuizzes = current.correctQuizCount
            )
            // Refresh daily and weekly state immediately for responsive UI across all screens
            val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val updatedLocal = progressRepository.getLocalProgress(todayIso, tech.id)
            _todayLanguageProgressMap.value = _todayLanguageProgressMap.value.toMutableMap().apply {
                put(tech.id, updatedLocal)
            }
            _weeklyChallengeHistory.value = progressRepository.getLocalWeeklyChallengeHistory(7)

            evaluateBadgesInternal()
            if (user != null) {
                syncScoreToLeaderboard()
            }
        }
        maybeTriggerModerateInterstitialAd()
    }

    fun dismissPointsFeedback() {
        _pointsFeedback.value = null
    }

    private fun loadCompletedCourseLessonIds(): Set<String> =
        CourseChecklistManager.loadCompletedIds(prefs)

    /**
     * Marca ou desmarca uma aula/módulo de apoio aos estudos.
     * Importante: cursos em vídeo e aulas são apenas material de auxílio (sem ganho de XP/pontos).
     * Pontos (XP) são ganhos apenas nas Atividades Práticas e nos Quizzes.
     */
    fun toggleCourseLessonCompleted(tech: Technology, lessonId: String) {
        recordUserActivity()
        _completedCourseLessonIds.value = CourseChecklistManager.toggleLessonCompleted(
            prefs = prefs,
            currentLessons = _completedCourseLessonIds.value,
            lessonId = lessonId
        )
    }

    fun getTechScore(tech: Technology): Int {
        return prefs.getInt("score_tech_${tech.id}", 0)
    }

    private fun loadSavedUserAccount(): UserAccount? {
        val userId = prefs.getString("auth_user_id", null) ?: return null
        val displayName = prefs.getString("auth_display_name", "Dev") ?: "Dev"
        val email = prefs.getString("auth_email", "") ?: ""
        val phone = prefs.getString("auth_phone", "") ?: ""
        val photo = prefs.getString("auth_photo", "") ?: ""
        val methodStr = prefs.getString("auth_method", "google") ?: "google"
        val authMethod = if (methodStr == "phone") AuthMethod.PHONE else AuthMethod.GOOGLE

        val score = prefs.getInt("user_total_score", 0)
        val streak = prefs.getInt("user_streak_days", 0)
        val badges = prefs.getInt("user_badges_count", 0)
        val tier = DeveloperLevelCatalog.resolveForXp(score)

        return UserAccount(
            userId = userId,
            displayName = displayName,
            email = email,
            phoneNumber = phone,
            photoUrl = photo,
            authMethod = authMethod,
            totalScore = score,
            levelNumber = tier.levelNumber,
            levelTitlePt = tier.titlePt,
            levelTitleEn = tier.titleEn,
            streakDays = streak,
            completedActivitiesCount = prefs.getInt("user_completed_act_count", 0),
            badgesCount = badges,
            isLoggedIn = true
        )
    }

    private fun saveUserAccountToPrefs(account: UserAccount?) {
        if (account == null) {
            prefs.edit()
                .remove("auth_user_id")
                .remove("auth_display_name")
                .remove("auth_email")
                .remove("auth_phone")
                .remove("auth_photo")
                .remove("auth_method")
                .apply()
        } else {
            prefs.edit()
                .putString("auth_user_id", account.userId)
                .putString("auth_display_name", account.displayName)
                .putString("auth_email", account.email)
                .putString("auth_phone", account.phoneNumber)
                .putString("auth_photo", account.photoUrl)
                .putString("auth_method", account.authMethod.name.lowercase())
                .putInt("user_streak_days", account.streakDays)
                .putInt("user_badges_count", account.badgesCount)
                .apply()
        }
    }

    private fun restartLeaderboardObservation(account: UserAccount?) {
        leaderboardJob?.cancel()
        leaderboardJob = viewModelScope.launch {
            leaderboardRepository.observeLeaderboard(account).collect { entries ->
                _leaderboardEntries.value = entries
            }
        }
    }

    private fun restartDailyProgressObservation(account: UserAccount?) {
        dailyProgressJob?.cancel()
        weeklyHistoryJob?.cancel()
        val uid = account?.userId ?: ""
        dailyProgressJob = viewModelScope.launch {
            progressRepository.observeAllLanguagesTodayProgress(uid).collect { map ->
                _todayLanguageProgressMap.value = map
            }
        }
        weeklyHistoryJob = viewModelScope.launch {
            progressRepository.observeWeeklyChallengeHistory(uid, days = 7).collect { history ->
                _weeklyChallengeHistory.value = history
            }
        }
    }

    /**
     * Restaura o progresso do usuário do Cloud Firestore e mescla com os dados locais,
     * garantindo que nenhum ponto, desafio ou foto seja perdido ao fazer login ou reabrir o app.
     */
    private suspend fun restoreUserProgressFromCloud(userId: String) {
        if (userId.isBlank()) return
        try {
            val remote = leaderboardRepository.fetchUserProfile(userId) ?: return
            val remoteScore = (remote["totalScore"] as? Number)?.toInt() ?: 0
            val remoteQuizzes = (remote["completedQuizCount"] as? Number)?.toInt() ?: 0
            val remoteActs = (remote["completedActivitiesCount"] as? Number)?.toInt() ?: 0
            val remoteActIds = (remote["completedActivityIds"] as? List<*>)
                ?.mapNotNull { it?.toString() }?.toSet() ?: emptySet()
            val remoteQuizIds = (remote["completedQuizIds"] as? List<*>)
                ?.mapNotNull { it?.toString()?.toIntOrNull() }?.toSet() ?: emptySet()
            val remotePhoto = remote["photoUrl"]?.toString().orEmpty()
            val remoteStreak = (remote["streakDays"] as? Number)?.toInt() ?: 0
            val remoteBadges = (remote["badgesCount"] as? Number)?.toInt() ?: 0
            val remoteTechScores = (remote["techScores"] as? Map<*, *>) ?: emptyMap<String, Any>()
            val remoteTechMinutes = (remote["techMinutes"] as? Map<*, *>) ?: emptyMap<String, Any>()
            val remoteUnlockedBadgeIds = (remote["unlockedBadgeIds"] as? List<*>)
                ?.mapNotNull { it?.toString() }?.toSet() ?: emptySet()

            val current = _scoreInfo.value
            val finalScore = maxOf(remoteScore, current.totalScore)
            val finalActIds = current.completedActivityIds + remoteActIds
            val finalQuizIds = current.completedQuizIds + remoteQuizIds
            val finalQuizCount = maxOf(current.correctQuizCount, remoteQuizzes, finalQuizIds.size)
            val finalActCount = maxOf(current.completedActivitiesCount, remoteActs, finalActIds.size)

            val editor = prefs.edit()
            editor.putInt("user_total_score", finalScore)
            editor.putInt("user_correct_quiz_count", finalQuizCount)
            editor.putInt("user_completed_act_count", finalActCount)
            editor.putStringSet("completed_quiz_ids", finalQuizIds.map { it.toString() }.toSet())
            editor.putStringSet("completed_act_ids", finalActIds)
            editor.putInt("user_streak_days", maxOf(streakInfo.value.streakDays, remoteStreak))
            editor.putInt("user_badges_count", maxOf(unlockedBadgesCount.value, remoteBadges, remoteUnlockedBadgeIds.size))

            remoteTechScores.forEach { (key, value) ->
                val kStr = key.toString()
                val vInt = (value as? Number)?.toInt() ?: 0
                val localVal = prefs.getInt("score_tech_$kStr", 0)
                editor.putInt("score_tech_$kStr", maxOf(vInt, localVal))
            }

            // Mescla horas/minutos acumulados por tecnologia e emblemas desbloqueados da nuvem
            progressRepository.mergeRemoteTechMinutes(remoteTechMinutes)
            if (remoteUnlockedBadgeIds.isNotEmpty()) {
                badgeRepository.restoreUnlockedBadgeIdsFromCloud(remoteUnlockedBadgeIds)
            }

            // Preserva e atualiza foto garantindo preferência por URL compartilhável globalmente
            val rawSavedPhoto = prefs.getString("auth_photo", "").orEmpty()
            val shareableLocal = ensureShareablePhotoUrl(rawSavedPhoto)
            val shareableRemote = if (AvatarImageCodec.isShareableAvatarUrl(remotePhoto)) remotePhoto else ""
            val effectivePhoto = when {
                shareableLocal.isNotBlank() -> shareableLocal
                shareableRemote.isNotBlank() -> shareableRemote
                else -> ""
            }
            if (effectivePhoto.isNotBlank()) {
                editor.putString("auth_photo", effectivePhoto)
            } else if (AvatarImageCodec.isLocalFileOrContentUri(rawSavedPhoto)) {
                editor.putString("auth_photo", "")
            }
            editor.apply()

            val restoredScoreInfo = calculateScoreInfo(
                score = finalScore,
                quizCount = finalQuizCount,
                actCount = finalActCount,
                actIds = finalActIds,
                quizIds = finalQuizIds
            )
            // Ao restaurar progresso existente da nuvem, não dispara diálogo falso de level-up
            applyScoreProgression(restoredScoreInfo, allowLevelUpReward = false)

            val user = _userAccount.value
            if (user != null) {
                val updatedUser = user.copy(
                    totalScore = finalScore,
                    levelNumber = _scoreInfo.value.levelNumber,
                    levelTitlePt = _scoreInfo.value.levelTitlePt,
                    levelTitleEn = _scoreInfo.value.levelTitleEn,
                    photoUrl = effectivePhoto.ifBlank { user.photoUrl },
                    completedActivitiesCount = finalActCount,
                    badgesCount = maxOf(user.badgesCount, remoteBadges)
                )
                _userAccount.value = updatedUser
                saveUserAccountToPrefs(updatedUser)
            }
            // Restaura tema e paleta de cores personalizada salva na conta do usuário
            val remoteTheme = ThemeCustomization.fromStorageMap(remote["themeConfig"] as? Map<*, *>)
            if (remoteTheme != null) {
                val localTheme = _themeCustomization.value
                val localModified = prefs.getBoolean("theme_user_modified", false)
                if (!localModified || remoteTheme.updatedAtMillis >= localTheme.updatedAtMillis) {
                    _themeCustomization.value = remoteTheme
                    _themePreference.value = remoteTheme.themeMode
                    val isCustomOrNonDefault = !remoteTheme.isDefaultPalette || remoteTheme.themeMode != ThemePreference.DARK
                    saveThemeCustomizationLocally(remoteTheme, markUserModified = isCustomOrNonDefault)
                }
            }

            triggerBadgeEvaluation()
        } catch (e: Exception) {
            android.util.Log.w("MainViewModel", "Error restoring user progress from cloud: ${e.message}")
        }
    }

    fun loginWithGoogle(
        idToken: String,
        displayName: String?,
        email: String?,
        photoUrl: String?,
        extraPhone: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            try {
                // Firebase Authentication sign-in with Google credential
                var firebaseUid: String? = null
                try {
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    firebaseUid = authResult.user?.uid
                    android.util.Log.d("MainViewModel", "Firebase signInWithCredential success: $firebaseUid")
                } catch (authEx: Exception) {
                    android.util.Log.w("MainViewModel", "Firebase signInWithCredential note: ${authEx.message}")
                }

                val resolvedName = displayName?.takeIf { it.isNotBlank() }
                    ?: Firebase.auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: email?.substringBefore("@")
                    ?: "Dev Google"

                // Construct unique user id preferring the Firebase Auth UID
                val uid = firebaseUid ?: Firebase.auth.currentUser?.uid ?: ("g_" + (email ?: "user").replace(Regex("[^a-zA-Z0-9]"), "_"))

                // 1. Restaura primeiro qualquer progresso e pontos prévios salvos no Firestore
                restoreUserProgressFromCloud(uid)

                val score = _scoreInfo.value
                val streak = streakInfo.value.streakDays
                val badges = unlockedBadgesCount.value

                // 2. Preserva foto de perfil compartilhável escolhida pelo usuário
                val rawSavedPhoto = prefs.getString("auth_photo", "").orEmpty()
                val savedCustomPhoto = ensureShareablePhotoUrl(rawSavedPhoto)
                val finalPhoto = when {
                    savedCustomPhoto.isNotBlank() -> savedCustomPhoto
                    !photoUrl.isNullOrBlank() -> photoUrl
                    Firebase.auth.currentUser?.photoUrl != null -> Firebase.auth.currentUser?.photoUrl.toString()
                    else -> ""
                }
                prefs.edit().putString("auth_photo", finalPhoto).apply()

                val account = UserAccount(
                    userId = uid,
                    displayName = resolvedName,
                    email = email ?: Firebase.auth.currentUser?.email ?: "",
                    phoneNumber = extraPhone.ifBlank { Firebase.auth.currentUser?.phoneNumber ?: "" },
                    photoUrl = finalPhoto,
                    authMethod = AuthMethod.GOOGLE,
                    totalScore = score.totalScore,
                    levelNumber = score.levelNumber,
                    levelTitlePt = score.levelTitlePt,
                    levelTitleEn = score.levelTitleEn,
                    streakDays = streak,
                    completedActivitiesCount = score.completedActivitiesCount,
                    badgesCount = badges,
                    isLoggedIn = true
                )
                _userAccount.value = account
                saveUserAccountToPrefs(account)

                // Sync to Firestore Leaderboard & User profile com metadados de progresso
                val allTechScoresMap = Technology.entries.associate { it.id to getTechScore(it) }
                leaderboardRepository.syncUserToLeaderboard(
                    account = account,
                    completedActivityIds = score.completedActivityIds,
                    completedQuizIds = score.completedQuizIds,
                    techScores = allTechScoresMap,
                    themeConfig = _themeCustomization.value,
                    techMinutes = currentTechMinutesIdMap(),
                    unlockedBadgeIds = currentUnlockedBadgeIds()
                )
                restartLeaderboardObservation(account)
                restartDailyProgressObservation(account)
                restartTargetSubjectsObservation()
                restartChatSubsystem()

                val isPt = _language.value == Language.PT
                _pointsFeedback.value = if (isPt) "🎉 Bem-vindo, $resolvedName! Autenticado com Firebase e salvo no Placar."
                else "🎉 Welcome, $resolvedName! Authenticated with Firebase and saved to Leaderboard."

                // Only auto-route to dashboard when signing in from the Login screen
                if (_currentDestination.value == AppDestination.LOGIN) {
                    _currentDestination.value = AppDestination.HOME
                }
                onSuccess()
            } catch (e: Exception) {
                val isPt = _language.value == Language.PT
                _pointsFeedback.value = if (isPt) "Erro ao entrar: ${e.message}" else "Sign-in error: ${e.message}"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun loginWithPhoneNumber(name: String, phoneNumber: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            try {
                try {
                    if (Firebase.auth.currentUser != null) {
                        Firebase.auth.signOut()
                    }
                } catch (_: Exception) {}

                val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
                val uid = "ph_" + if (cleanPhone.isNotBlank()) cleanPhone else System.currentTimeMillis().toString()

                // 1. Restaura progresso prévio da nuvem antes de qualquer sincronização
                restoreUserProgressFromCloud(uid)

                val score = _scoreInfo.value
                val streak = streakInfo.value.streakDays
                val badges = unlockedBadgesCount.value
                val savedCustomPhoto = ensureShareablePhotoUrl(prefs.getString("auth_photo", "").orEmpty())
                if (savedCustomPhoto.isNotBlank()) {
                    prefs.edit().putString("auth_photo", savedCustomPhoto).apply()
                }

                val account = UserAccount(
                    userId = uid,
                    displayName = name,
                    phoneNumber = phoneNumber,
                    photoUrl = savedCustomPhoto,
                    authMethod = AuthMethod.PHONE,
                    totalScore = score.totalScore,
                    levelNumber = score.levelNumber,
                    levelTitlePt = score.levelTitlePt,
                    levelTitleEn = score.levelTitleEn,
                    streakDays = streak,
                    completedActivitiesCount = score.completedActivitiesCount,
                    badgesCount = badges,
                    isLoggedIn = true
                )
                _userAccount.value = account
                saveUserAccountToPrefs(account)

                val allTechScoresMap = Technology.entries.associate { it.id to getTechScore(it) }
                leaderboardRepository.syncUserToLeaderboard(
                    account = account,
                    completedActivityIds = score.completedActivityIds,
                    completedQuizIds = score.completedQuizIds,
                    techScores = allTechScoresMap,
                    themeConfig = _themeCustomization.value,
                    techMinutes = currentTechMinutesIdMap(),
                    unlockedBadgeIds = currentUnlockedBadgeIds()
                )
                restartLeaderboardObservation(account)
                restartDailyProgressObservation(account)
                restartTargetSubjectsObservation()
                restartChatSubsystem()

                val isPt = _language.value == Language.PT
                _pointsFeedback.value = if (isPt) "🎉 Olá, $name! Telefone registrado e pontuação salva no Placar."
                else "🎉 Hello, $name! Phone registered and score saved to Leaderboard."

                // Route to main dashboard
                _currentDestination.value = AppDestination.HOME
                onSuccess()
            } catch (e: Exception) {
                val isPt = _language.value == Language.PT
                _pointsFeedback.value = if (isPt) "Erro ao registrar telefone: ${e.message}" else "Phone sign-in error: ${e.message}"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun syncScoreToLeaderboard() {
        val current = _userAccount.value ?: return
        viewModelScope.launch {
            _isAuthLoading.value = true
            val score = _scoreInfo.value
            val streak = streakInfo.value.streakDays
            val unlockedBadgeIds = currentUnlockedBadgeIds()
            val badges = maxOf(unlockedBadgesCount.value, unlockedBadgeIds.size)
            val currentPhoto = ensureShareablePhotoUrl(
                prefs.getString("auth_photo", current.photoUrl).orEmpty()
            )
            if (currentPhoto != prefs.getString("auth_photo", "")) {
                prefs.edit().putString("auth_photo", currentPhoto).apply()
            }
            val updated = current.copy(
                photoUrl = currentPhoto,
                totalScore = score.totalScore,
                levelNumber = score.levelNumber,
                levelTitlePt = score.levelTitlePt,
                levelTitleEn = score.levelTitleEn,
                streakDays = streak,
                completedActivitiesCount = score.completedActivitiesCount,
                badgesCount = badges
            )
            _userAccount.value = updated
            saveUserAccountToPrefs(updated)
            val allTechScoresMap = Technology.entries.associate { it.id to getTechScore(it) }
            val success = leaderboardRepository.syncUserToLeaderboard(
                account = updated,
                completedActivityIds = score.completedActivityIds,
                completedQuizIds = score.completedQuizIds,
                techScores = allTechScoresMap,
                themeConfig = _themeCustomization.value,
                techMinutes = currentTechMinutesIdMap(),
                unlockedBadgeIds = unlockedBadgeIds
            )
            _isAuthLoading.value = false
            val isPt = _language.value == Language.PT
            if (success) {
                _pointsFeedback.value = if (isPt) "⚡ Placar sincronizado com sucesso no Cloud Firestore!"
                else "⚡ Leaderboard synced successfully to Cloud Firestore!"
            }
            publishCurrentDirectoryProfileIfSignedIn()
        }
    }

    // ==================== SISTEMA DE CHAT, ARQUIVOS & LIGAÇÕES WEBRTC ====================

    private fun resolveLocalChatOwnerId(): String {
        return Firebase.auth.currentUser?.uid
            ?: prefs.getString("last_firebase_uid", null)?.takeIf { it.isNotBlank() }
            ?: _userAccount.value?.userId
            ?: ""
    }

    fun dismissChatFeedback() {
        _chatStatusFeedback.value = null
    }

    private fun publishCurrentDirectoryProfileIfSignedIn() {
        val fbUser = Firebase.auth.currentUser ?: return
        prefs.edit().putString("last_firebase_uid", fbUser.uid).apply()
        val acc = _userAccount.value
        val score = _scoreInfo.value
        val levelTitle = if (_language.value == Language.PT) score.levelTitlePt else score.levelTitleEn
        val name = acc?.displayName?.takeIf { it.isNotBlank() }
            ?: fbUser.displayName?.takeIf { it.isNotBlank() }
            ?: fbUser.email?.substringBefore("@")
            ?: "Dev"
        val photo = acc?.photoUrl?.takeIf { it.isNotBlank() }
            ?: fbUser.photoUrl?.toString()
            ?: ""

        viewModelScope.launch {
            chatRepository.publishDirectoryProfile(
                displayName = name,
                photoUrl = photo,
                levelNumber = score.levelNumber,
                levelTitle = levelTitle,
                totalScore = score.totalScore
            )
        }
    }

    fun restartChatSubsystem() {
        chatDirectoryJob?.cancel()
        chatRequestsJob?.cancel()
        localConversationsJob?.cancel()
        cloudConversationsJob?.cancel()
        callSessionsJob?.cancel()
        liveRoomsJob?.cancel()

        val fbUid = Firebase.auth.currentUser?.uid
        _isFirebaseChatAuthenticated.value = fbUid != null
        if (fbUid != null) {
            prefs.edit().putString("last_firebase_uid", fbUid).apply()
        }

        val ownerId = resolveLocalChatOwnerId()
        _chatOwnerId.value = ownerId.ifBlank { null }
        if (ownerId.isNotBlank()) {
            localConversationsJob = viewModelScope.launch {
                chatRepository.observeLocalConversations(ownerId).collect { localConvs ->
                    _chatConversations.value = localConvs
                }
            }
        } else {
            _chatConversations.value = emptyList()
        }

        if (fbUid == null) {
            _chatDirectoryUsers.value = emptyList()
            _chatRequests.value = emptyList()
            _activeLiveRooms.value = emptyList()
            return
        }

        publishCurrentDirectoryProfileIfSignedIn()

        chatDirectoryJob = viewModelScope.launch {
            runCatching {
                chatRepository.observeDirectoryUsers().collect { users ->
                    _chatDirectoryUsers.value = users
                }
            }
        }

        chatRequestsJob = viewModelScope.launch {
            runCatching {
                chatRepository.observeChatRequests().collect { requests ->
                    _chatRequests.value = requests
                }
            }
        }

        cloudConversationsJob = viewModelScope.launch {
            runCatching {
                chatRepository.observeAndCacheCloudConversations().collect { cloudConvs ->
                    _chatConversations.value = cloudConvs
                }
            }
        }

        callSessionsJob = viewModelScope.launch {
            runCatching {
                chatRepository.observeCallSessions().collect { sessions ->
                    handleIncomingAndUpdatedCallSessions(sessions, fbUid)
                }
            }
        }

        liveRoomsJob?.cancel()
        liveRoomsJob = viewModelScope.launch {
            runCatching {
                chatRepository.observeActiveLiveRooms().collect { rooms ->
                    _activeLiveRooms.value = rooms
                    val joined = _joinedLiveRoom.value
                    if (joined != null) {
                        val updated = rooms.firstOrNull { it.roomId == joined.roomId }
                        if (updated == null) {
                            cleanupLocalLiveResources()
                            val isPt = _language.value == Language.PT
                            _chatStatusFeedback.value = if (isPt) "A Live foi encerrada." else "The Live room has ended."
                        } else {
                            _joinedLiveRoom.value = updated
                            if (updated.hostId == fbUid) {
                                updated.participantIds
                                    .filter { it != fbUid && !webRtcLiveManager.hasPeer(it) }
                                    .forEach { viewerUid -> initiateHostOfferToLiveViewer(updated.roomId, viewerUid) }
                            }
                        }
                    }
                }
            }
        }
    }

    fun openChatConversation(chatId: String) {
        _activeChatId.value = chatId
        localMessagesJob?.cancel()
        cloudMessagesJob?.cancel()

        localMessagesJob = viewModelScope.launch {
            chatRepository.observeLocalMessages(chatId).collect { msgs ->
                _activeChatMessages.value = msgs
            }
        }

        if (Firebase.auth.currentUser != null) {
            cloudMessagesJob = viewModelScope.launch {
                runCatching {
                    chatRepository.observeAndCacheCloudMessages(chatId).collect { cloudMsgs ->
                        _activeChatMessages.value = cloudMsgs
                    }
                }
            }
        }
        recordUserActivity()
    }

    fun closeActiveChatConversation() {
        _activeChatId.value = null
        localMessagesJob?.cancel()
        cloudMessagesJob?.cancel()
        _activeChatMessages.value = emptyList()
    }

    fun sendChatRequest(recipient: ChatDirectoryUser, introMessage: String = "") {
        val fbUser = Firebase.auth.currentUser
        val isPt = _language.value == Language.PT
        if (fbUser == null) {
            _chatStatusFeedback.value = if (isPt) "Entre com Google para enviar solicitações de conversa."
            else "Sign in with Google to send chat requests."
            return
        }
        val senderName = _userAccount.value?.displayName?.ifBlank { fbUser.displayName ?: "Dev" } ?: "Dev"
        val senderPhoto = _userAccount.value?.photoUrl ?: fbUser.photoUrl?.toString() ?: ""

        viewModelScope.launch {
            val result = chatRepository.sendChatRequest(
                senderName = senderName,
                senderPhotoUrl = senderPhoto,
                recipient = recipient,
                introMessage = introMessage
            )
            _chatStatusFeedback.value = if (result.isSuccess) {
                if (isPt) "Solicitação enviada para ${recipient.displayName}! Aguardando aceitação."
                else "Request sent to ${recipient.displayName}! Waiting for acceptance."
            } else {
                if (isPt) "Não foi possível enviar solicitação agora."
                else "Could not send request right now."
            }
        }
    }

    fun acceptChatRequest(request: ChatRequestItem, openImmediately: Boolean = true) {
        val fbUser = Firebase.auth.currentUser ?: return
        val isPt = _language.value == Language.PT
        val accepterName = _userAccount.value?.displayName?.ifBlank { fbUser.displayName ?: "Dev" } ?: "Dev"
        val accepterPhoto = _userAccount.value?.photoUrl ?: fbUser.photoUrl?.toString() ?: ""

        viewModelScope.launch {
            val res = chatRepository.acceptChatRequest(
                request = request,
                accepterName = accepterName,
                accepterPhotoUrl = accepterPhoto
            )
            res.onSuccess { chatId ->
                _chatStatusFeedback.value = if (isPt) "Conversa iniciada com ${request.peerNameFor(fbUser.uid)}!"
                else "Chat started with ${request.peerNameFor(fbUser.uid)}!"
                if (openImmediately) {
                    openChatConversation(chatId)
                }
            }.onFailure {
                _chatStatusFeedback.value = if (isPt) "Erro ao aceitar solicitação." else "Failed to accept request."
            }
        }
    }

    fun declineOrDeleteChatRequest(requestId: String) {
        val isPt = _language.value == Language.PT
        viewModelScope.launch {
            val res = chatRepository.declineOrDeleteRequest(requestId)
            if (res.isSuccess) {
                _chatStatusFeedback.value = if (isPt) "Solicitação removida." else "Request removed."
            }
        }
    }

    fun sendChatTextMessage(text: String) {
        val chatId = _activeChatId.value ?: return
        val conv = _chatConversations.value.firstOrNull { it.chatId == chatId } ?: return
        val fbUser = Firebase.auth.currentUser
        val isPt = _language.value == Language.PT
        if (fbUser == null) {
            _chatStatusFeedback.value = if (isPt) "Entre com Google para enviar mensagens."
            else "Sign in with Google to send messages."
            return
        }
        val senderName = _userAccount.value?.displayName?.ifBlank { fbUser.displayName ?: "Dev" } ?: "Dev"
        viewModelScope.launch {
            val res = chatRepository.sendTextMessage(
                chatId = chatId,
                peerUserId = conv.peerUserId,
                senderName = senderName,
                text = text
            )
            if (res.isFailure) {
                _chatStatusFeedback.value = if (isPt) "Salvo localmente. Sem conexão para enviar à nuvem."
                else "Saved locally. Cloud send failed."
            }
        }
    }

    fun sendChatAttachment(uri: Uri, messageType: ChatMessageType, caption: String = "") {
        val chatId = _activeChatId.value ?: return
        val conv = _chatConversations.value.firstOrNull { it.chatId == chatId } ?: return
        val fbUser = Firebase.auth.currentUser
        val isPt = _language.value == Language.PT
        if (fbUser == null) {
            _chatStatusFeedback.value = if (isPt) "Entre com Google para enviar mídias e arquivos."
            else "Sign in with Google to send media & files."
            return
        }
        val senderName = _userAccount.value?.displayName?.ifBlank { fbUser.displayName ?: "Dev" } ?: "Dev"
        val messageId = java.util.UUID.randomUUID().toString()

        viewModelScope.launch {
            _isChatUploading.value = true
            val prepared = withContext(Dispatchers.IO) {
                when (messageType) {
                    ChatMessageType.IMAGE -> ChatMediaCodec.encodeImageForChat(
                        context = getApplication(),
                        uri = uri,
                        chatId = chatId,
                        messageId = messageId
                    )
                    ChatMessageType.VIDEO -> ChatMediaCodec.prepareFileOrVideoForChat(
                        context = getApplication(),
                        uri = uri,
                        chatId = chatId,
                        messageId = messageId,
                        isVideo = true
                    )
                    else -> ChatMediaCodec.prepareFileOrVideoForChat(
                        context = getApplication(),
                        uri = uri,
                        chatId = chatId,
                        messageId = messageId,
                        isVideo = false
                    )
                }
            }

            if (prepared == null) {
                _isChatUploading.value = false
                _chatStatusFeedback.value = if (isPt) "Não foi possível ler o arquivo selecionado."
                else "Could not read selected file."
                return@launch
            }

            val res = chatRepository.sendMediaMessage(
                chatId = chatId,
                peerUserId = conv.peerUserId,
                senderName = senderName,
                messageType = messageType,
                caption = caption,
                media = prepared,
                preGeneratedMessageId = messageId
            )
            _isChatUploading.value = false
            res.onFailure { err ->
                _chatStatusFeedback.value = if (err.message == "FILE_TOO_LARGE_FOR_FIRESTORE") {
                    if (isPt) "Arquivo salvo localmente! Limite de envio na nuvem: até 2,6 MB."
                    else "Saved locally! Cloud transfer supports files up to 2.6 MB."
                } else {
                    if (isPt) "Erro ao enviar anexo." else "Failed to upload attachment."
                }
            }
        }
    }

    suspend fun ensureMessageMediaLocalPath(message: ChatMessageItem): String? {
        return chatRepository.downloadAttachmentIfNeeded(message)
    }

    fun openChatAttachmentExternal(message: ChatMessageItem) {
        val isPt = _language.value == Language.PT
        viewModelScope.launch {
            val localPath = chatRepository.downloadAttachmentIfNeeded(message)
            if (localPath.isNullOrBlank()) {
                _chatStatusFeedback.value = if (isPt) "Não foi possível baixar o arquivo."
                else "Could not download file."
                return@launch
            }
            val opened = ChatMediaCodec.openLocalMediaFile(
                context = getApplication(),
                localPath = localPath,
                mimeType = message.mediaMimeType
            )
            if (!opened) {
                _chatStatusFeedback.value = if (isPt) "Nenhum aplicativo disponível para abrir este arquivo."
                else "No app available to open this file."
            }
        }
    }

    fun deleteChatMessage(messageId: String) {
        val chatId = _activeChatId.value ?: return
        val isPt = _language.value == Language.PT
        viewModelScope.launch {
            val res = chatRepository.deleteMessage(chatId, messageId)
            _chatStatusFeedback.value = if (res.isSuccess) {
                if (isPt) "Mensagem apagada." else "Message deleted."
            } else {
                if (isPt) "Mensagem apagada localmente." else "Message deleted locally."
            }
        }
    }

    fun deleteChatConversation(chatId: String) {
        val isPt = _language.value == Language.PT
        if (_activeChatId.value == chatId) {
            closeActiveChatConversation()
        }
        viewModelScope.launch {
            val res = chatRepository.deleteConversation(chatId)
            _chatStatusFeedback.value = if (res.isSuccess) {
                if (isPt) "Conversa deletada do aparelho e da nuvem."
                else "Conversation deleted from device & cloud."
            } else {
                if (isPt) "Conversa removida localmente." else "Conversation deleted locally."
            }
        }
    }

    // --- Ligações de Voz e Vídeo (WebRTC + Firestore Signaling) ---

    fun startCall(callType: ChatCallType) {
        val chatId = _activeChatId.value ?: return
        val conv = _chatConversations.value.firstOrNull { it.chatId == chatId } ?: return
        startCallWithConversation(conv, callType)
    }

    fun startCallWithConversation(conv: ChatConversationItem, callType: ChatCallType) {
        val fbUser = Firebase.auth.currentUser
        val chatId = conv.chatId
        val isPt = _language.value == Language.PT
        if (fbUser == null) {
            _chatStatusFeedback.value = if (isPt) "Entre com Google para fazer ligações."
            else "Sign in with Google to place calls."
            return
        }
        if (_activeCallSession.value != null) return

        val callId = "call_${java.util.UUID.randomUUID().toString().replace("-", "").take(16)}"
        val callerName = _userAccount.value?.displayName?.ifBlank { fbUser.displayName ?: "Dev" } ?: "Dev"
        val callerPhoto = _userAccount.value?.photoUrl ?: fbUser.photoUrl?.toString() ?: ""

        val initialSession = ActiveCallSession(
            callId = callId,
            chatId = chatId,
            callerId = fbUser.uid,
            callerName = callerName,
            callerPhotoUrl = callerPhoto,
            calleeId = conv.peerUserId,
            calleeName = conv.peerDisplayName,
            callType = callType,
            status = ChatCallStatus.RINGING,
            isOutgoing = true,
            isSpeakerOn = callType == ChatCallType.VIDEO,
            isCameraEnabled = callType == ChatCallType.VIDEO
        )
        _activeCallSession.value = initialSession
        appliedAnswerForCallId = null
        processedIceCandidateIds.clear()

        viewModelScope.launch {
            webRtcCallManager.startPeerSession(
                isVideoCall = callType == ChatCallType.VIDEO,
                onIceCandidateGenerated = { cand ->
                    viewModelScope.launch {
                        chatRepository.sendIceCandidate(
                            callId = callId,
                            peerUserId = conv.peerUserId,
                            sdpMid = cand.sdpMid ?: "0",
                            sdpMLineIndex = cand.sdpMLineIndex,
                            candidate = cand.sdp
                        )
                    }
                },
                onIceConnectionStateChanged = { iceState ->
                    val connected = iceState == org.webrtc.PeerConnection.IceConnectionState.CONNECTED ||
                        iceState == org.webrtc.PeerConnection.IceConnectionState.COMPLETED
                    _activeCallSession.value = _activeCallSession.value?.let { cur ->
                        if (cur.callId == callId) cur.copy(peerConnected = connected || cur.peerConnected) else cur
                    }
                }
            )
            val offerSdp = webRtcCallManager.createOfferSdp()
                ?: "v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n"

            val res = chatRepository.createCallSession(
                callId = callId,
                chatId = chatId,
                callerName = callerName,
                callerPhotoUrl = callerPhoto,
                calleeId = conv.peerUserId,
                calleeName = conv.peerDisplayName,
                callType = callType,
                offerSdp = offerSdp
            )
            if (res.isFailure) {
                cleanupLocalCallResources()
                _chatStatusFeedback.value = if (isPt) "Erro ao iniciar ligação." else "Could not start call."
            } else {
                startObservingIceCandidates(callId, fbUser.uid)
            }
        }
    }

    fun acceptIncomingCall() {
        val session = _activeCallSession.value ?: return
        val fbUser = Firebase.auth.currentUser ?: return
        processedIceCandidateIds.clear()

        _activeCallSession.value = session.copy(
            status = ChatCallStatus.ACCEPTED,
            isSpeakerOn = session.callType == ChatCallType.VIDEO,
            isCameraEnabled = session.callType == ChatCallType.VIDEO
        )
        startCallDurationTicker()

        viewModelScope.launch {
            webRtcCallManager.startPeerSession(
                isVideoCall = session.callType == ChatCallType.VIDEO,
                onIceCandidateGenerated = { cand ->
                    viewModelScope.launch {
                        chatRepository.sendIceCandidate(
                            callId = session.callId,
                            peerUserId = session.callerId,
                            sdpMid = cand.sdpMid ?: "0",
                            sdpMLineIndex = cand.sdpMLineIndex,
                            candidate = cand.sdp
                        )
                    }
                },
                onIceConnectionStateChanged = { iceState ->
                    val connected = iceState == org.webrtc.PeerConnection.IceConnectionState.CONNECTED ||
                        iceState == org.webrtc.PeerConnection.IceConnectionState.COMPLETED
                    _activeCallSession.value = _activeCallSession.value?.let { cur ->
                        if (cur.callId == session.callId) cur.copy(peerConnected = connected || cur.peerConnected) else cur
                    }
                }
            )
            val answerSdp = webRtcCallManager.createAnswerSdp(session.offerSdp)
                ?: "v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n"

            chatRepository.updateCallSessionStatus(
                callId = session.callId,
                status = ChatCallStatus.ACCEPTED,
                answerSdp = answerSdp
            )
            startObservingIceCandidates(session.callId, fbUser.uid)
        }
    }

    fun declineOrEndCall() {
        val session = _activeCallSession.value ?: return
        val fbUser = Firebase.auth.currentUser
        val nextStatus = if (!session.isOutgoing && session.status == ChatCallStatus.RINGING) {
            ChatCallStatus.DECLINED
        } else {
            ChatCallStatus.ENDED
        }
        val duration = session.elapsedSeconds
        val isPt = _language.value == Language.PT

        cleanupLocalCallResources()

        if (fbUser != null) {
            viewModelScope.launch {
                chatRepository.updateCallSessionStatus(session.callId, nextStatus)
                if (session.chatId.isNotBlank() && session.isOutgoing) {
                    val mins = duration / 60
                    val secs = duration % 60
                    val formatted = String.format(java.util.Locale.US, "%02d:%02d", mins, secs)
                    val label = when {
                        nextStatus == ChatCallStatus.DECLINED ->
                            if (isPt) "Chamada recusada" else "Call declined"
                        duration > 0 ->
                            if (isPt) "Chamada (${if (session.callType == ChatCallType.VIDEO) "Vídeo" else "Voz"}) • $formatted"
                            else "${if (session.callType == ChatCallType.VIDEO) "Video" else "Voice"} call • $formatted"
                        else ->
                            if (isPt) "Chamada não atendida" else "Unanswered call"
                    }
                    val senderName = _userAccount.value?.displayName?.ifBlank { fbUser.displayName ?: "Dev" } ?: "Dev"
                    chatRepository.recordCallLogMessage(
                        chatId = session.chatId,
                        peerUserId = session.peerId(fbUser.uid),
                        senderName = senderName,
                        callType = session.callType,
                        durationSeconds = duration,
                        summaryText = label
                    )
                }
            }
        }
    }

    fun toggleCallMicMute() {
        val current = _activeCallSession.value ?: return
        val muted = !current.isMicMuted
        webRtcCallManager.setMicMuted(muted)
        _activeCallSession.value = current.copy(isMicMuted = muted)
    }

    fun toggleCallSpeaker() {
        val current = _activeCallSession.value ?: return
        val speaker = !current.isSpeakerOn
        webRtcCallManager.configureAudioRouting(speaker)
        _activeCallSession.value = current.copy(isSpeakerOn = speaker)
    }

    fun toggleCallCamera() {
        val current = _activeCallSession.value ?: return
        val cam = !current.isCameraEnabled
        webRtcCallManager.setCameraEnabled(cam)
        _activeCallSession.value = current.copy(isCameraEnabled = cam)
    }

    private fun handleIncomingAndUpdatedCallSessions(sessions: List<ActiveCallSession>, currentUid: String) {
        val current = _activeCallSession.value
        val isPt = _language.value == Language.PT

        if (current == null) {
            val incomingRinging = sessions.firstOrNull {
                !it.isOutgoing && it.status == ChatCallStatus.RINGING && it.calleeId == currentUid
            }
            if (incomingRinging != null) {
                _activeCallSession.value = incomingRinging
            }
            return
        }

        val remote = sessions.firstOrNull { it.callId == current.callId } ?: return
        if (remote.status == ChatCallStatus.DECLINED ||
            remote.status == ChatCallStatus.ENDED ||
            remote.status == ChatCallStatus.MISSED
        ) {
            cleanupLocalCallResources()
            _chatStatusFeedback.value = when (remote.status) {
                ChatCallStatus.DECLINED -> if (isPt) "Chamada recusada." else "Call declined."
                else -> if (isPt) "Chamada encerrada." else "Call ended."
            }
            return
        }

        if (remote.status == ChatCallStatus.ACCEPTED) {
            if (current.isOutgoing && remote.answerSdp.isNotBlank() && appliedAnswerForCallId != remote.callId) {
                appliedAnswerForCallId = remote.callId
                webRtcCallManager.applyRemoteAnswerSdp(remote.answerSdp)
                startCallDurationTicker()
            }
            _activeCallSession.value = current.copy(
                status = ChatCallStatus.ACCEPTED,
                answerSdp = remote.answerSdp,
                peerConnected = true
            )
        }
    }

    private fun startObservingIceCandidates(callId: String, currentUid: String) {
        iceCandidatesJob?.cancel()
        iceCandidatesJob = viewModelScope.launch {
            runCatching {
                chatRepository.observeIceCandidates(callId).collect { list ->
                    list.forEach { cand ->
                        if (cand.senderId != currentUid && processedIceCandidateIds.add(cand.candidateId)) {
                            webRtcCallManager.addRemoteIceCandidate(
                                sdpMid = cand.sdpMid,
                                sdpMLineIndex = cand.sdpMLineIndex,
                                candidate = cand.candidate
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startCallDurationTicker() {
        if (callDurationTickerJob?.isActive == true) return
        callDurationTickerJob = viewModelScope.launch {
            while (isActive && _activeCallSession.value?.status == ChatCallStatus.ACCEPTED) {
                delay(1_000L)
                _activeCallSession.value = _activeCallSession.value?.let {
                    it.copy(elapsedSeconds = it.elapsedSeconds + 1)
                }
            }
        }
    }

    private fun cleanupLocalCallResources() {
        iceCandidatesJob?.cancel()
        iceCandidatesJob = null
        callDurationTickerJob?.cancel()
        callDurationTickerJob = null
        appliedAnswerForCallId = null
        processedIceCandidateIds.clear()
        webRtcCallManager.endSession()
        _activeCallSession.value = null
    }

    // --- Salas Ao Vivo (Live de Estudo & Discussão de Conteúdos) ---

    fun createAndJoinLiveRoom(
        title: String,
        topicTechId: String,
        topicLabel: String,
        description: String,
        mode: LiveRoomMode
    ) {
        val fbUser = Firebase.auth.currentUser
        val isPt = _language.value == Language.PT
        if (fbUser == null) {
            _chatStatusFeedback.value = if (isPt) "Entre com Google para iniciar uma Live."
            else "Sign in with Google to start a Live room."
            return
        }
        val hostName = _userAccount.value?.displayName?.ifBlank { fbUser.displayName ?: "Dev" } ?: "Dev"
        val hostPhoto = _userAccount.value?.photoUrl ?: fbUser.photoUrl?.toString() ?: ""

        viewModelScope.launch {
            val res = chatRepository.createLiveRoom(
                title = title,
                topicTechId = topicTechId,
                topicLabel = topicLabel,
                description = description,
                liveMode = mode,
                hostName = hostName,
                hostPhotoUrl = hostPhoto
            )
            res.onSuccess { room ->
                enterJoinedLiveRoom(room, fbUser.uid, isHost = true)
                chatRepository.sendLiveMessage(
                    roomId = room.roomId,
                    senderName = hostName,
                    senderPhotoUrl = hostPhoto,
                    text = if (isPt) "Iniciou a Live sobre ${room.topicLabel}!"
                    else "Started Live discussion on ${room.topicLabel}!",
                    messageType = LiveMessageType.SYSTEM
                )
            }.onFailure {
                _chatStatusFeedback.value = if (isPt) "Erro ao iniciar a Live." else "Failed to start Live room."
            }
        }
        recordUserActivity()
    }

    fun joinLiveRoom(room: LiveStudyRoom) {
        val fbUser = Firebase.auth.currentUser
        val isPt = _language.value == Language.PT
        if (fbUser == null) {
            _chatStatusFeedback.value = if (isPt) "Entre com Google para participar da Live."
            else "Sign in with Google to join the Live room."
            return
        }
        val userName = _userAccount.value?.displayName?.ifBlank { fbUser.displayName ?: "Dev" } ?: "Dev"
        val userPhoto = _userAccount.value?.photoUrl ?: fbUser.photoUrl?.toString() ?: ""

        viewModelScope.launch {
            val res = chatRepository.joinLiveRoom(room.roomId, userName)
            res.onSuccess {
                enterJoinedLiveRoom(room, fbUser.uid, isHost = room.hostId == fbUser.uid)
                chatRepository.sendLiveMessage(
                    roomId = room.roomId,
                    senderName = userName,
                    senderPhotoUrl = userPhoto,
                    text = if (isPt) "entrou na discussão ao vivo." else "joined the live discussion.",
                    messageType = LiveMessageType.SYSTEM
                )
            }.onFailure {
                _chatStatusFeedback.value = if (isPt) "Não foi possível entrar na Live." else "Could not join Live room."
            }
        }
        recordUserActivity()
    }

    private fun enterJoinedLiveRoom(room: LiveStudyRoom, currentUid: String, isHost: Boolean) {
        cleanupLocalLiveResources()
        _joinedLiveRoom.value = room
        _isLiveMicMuted.value = !isHost
        _isLiveCameraEnabled.value = isHost && room.liveMode == LiveRoomMode.VIDEO
        _isLiveSpeakerOn.value = true
        _isLivePeerConnected.value = false
        _liveElapsedSeconds.value = 0

        webRtcLiveManager.startLocalMedia(
            enableVideo = isHost && room.liveMode == LiveRoomMode.VIDEO,
            speakerOn = true
        )
        webRtcLiveManager.setMicMuted(!isHost)

        startLiveMessagesObservation(room.roomId)
        startLiveSignalsObservation(room.roomId, currentUid)
        startLiveDurationTicker()
    }

    private fun initiateHostOfferToLiveViewer(roomId: String, viewerUid: String) {
        viewModelScope.launch {
            val pc = webRtcLiveManager.getOrCreatePeerConnection(
                remoteUid = viewerUid,
                onIceCandidateGenerated = { cand ->
                    viewModelScope.launch {
                        chatRepository.sendLiveSignal(
                            roomId = roomId,
                            toUid = viewerUid,
                            type = "ice",
                            sdpMid = cand.sdpMid ?: "0",
                            sdpMLineIndex = cand.sdpMLineIndex,
                            candidate = cand.sdp
                        )
                    }
                },
                onConnectionChanged = { connected ->
                    if (connected) _isLivePeerConnected.value = true
                }
            ) ?: return@launch
            val offerSdp = webRtcLiveManager.createOfferForPeer(viewerUid) ?: return@launch
            chatRepository.sendLiveSignal(
                roomId = roomId,
                toUid = viewerUid,
                type = "offer",
                sdp = offerSdp
            )
        }
    }

    private fun startLiveMessagesObservation(roomId: String) {
        liveMessagesJob?.cancel()
        liveMessagesJob = viewModelScope.launch {
            runCatching {
                chatRepository.observeLiveMessages(roomId).collect { msgs ->
                    _liveRoomMessages.value = msgs
                }
            }
        }
    }

    private fun startLiveSignalsObservation(roomId: String, currentUid: String) {
        liveSignalsJob?.cancel()
        processedLiveSignalIds.clear()
        liveSignalsJob = viewModelScope.launch {
            runCatching {
                chatRepository.observeLiveSignals(roomId).collect { signals ->
                    signals.forEach { sig ->
                        if (sig.toUid == currentUid && processedLiveSignalIds.add(sig.signalId)) {
                            when (sig.type) {
                                "offer" -> {
                                    webRtcLiveManager.getOrCreatePeerConnection(
                                        remoteUid = sig.fromUid,
                                        onIceCandidateGenerated = { cand ->
                                            viewModelScope.launch {
                                                chatRepository.sendLiveSignal(
                                                    roomId = roomId,
                                                    toUid = sig.fromUid,
                                                    type = "ice",
                                                    sdpMid = cand.sdpMid ?: "0",
                                                    sdpMLineIndex = cand.sdpMLineIndex,
                                                    candidate = cand.sdp
                                                )
                                            }
                                        },
                                        onConnectionChanged = { connected ->
                                            if (connected) _isLivePeerConnected.value = true
                                        }
                                    )
                                    val answer = webRtcLiveManager.createAnswerForPeer(sig.fromUid, sig.sdp)
                                    if (!answer.isNullOrBlank()) {
                                        chatRepository.sendLiveSignal(
                                            roomId = roomId,
                                            toUid = sig.fromUid,
                                            type = "answer",
                                            sdp = answer
                                        )
                                    }
                                }
                                "answer" -> {
                                    webRtcLiveManager.applyRemoteAnswerForPeer(sig.fromUid, sig.sdp)
                                    _isLivePeerConnected.value = true
                                }
                                "ice" -> {
                                    webRtcLiveManager.addRemoteIceCandidate(
                                        remoteUid = sig.fromUid,
                                        sdpMid = sig.sdpMid,
                                        sdpMLineIndex = sig.sdpMLineIndex,
                                        candidate = sig.candidate
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startLiveDurationTicker() {
        liveDurationTickerJob?.cancel()
        liveDurationTickerJob = viewModelScope.launch {
            while (isActive && _joinedLiveRoom.value != null) {
                delay(1_000L)
                _liveElapsedSeconds.value = _liveElapsedSeconds.value + 1
            }
        }
    }

    fun sendLiveRoomMessage(text: String, messageType: LiveMessageType = LiveMessageType.TEXT) {
        val room = _joinedLiveRoom.value ?: return
        val fbUser = Firebase.auth.currentUser ?: return
        val userName = _userAccount.value?.displayName?.ifBlank { fbUser.displayName ?: "Dev" } ?: "Dev"
        val userPhoto = _userAccount.value?.photoUrl ?: fbUser.photoUrl?.toString() ?: ""
        viewModelScope.launch {
            chatRepository.sendLiveMessage(
                roomId = room.roomId,
                senderName = userName,
                senderPhotoUrl = userPhoto,
                text = text,
                messageType = messageType
            )
        }
        recordUserActivity()
    }

    fun toggleLiveMicMute() {
        val next = !_isLiveMicMuted.value
        webRtcLiveManager.setMicMuted(next)
        _isLiveMicMuted.value = next
    }

    fun toggleLiveCamera() {
        val next = !_isLiveCameraEnabled.value
        webRtcLiveManager.setCameraEnabled(next)
        _isLiveCameraEnabled.value = next
    }

    fun toggleLiveSpeaker() {
        val next = !_isLiveSpeakerOn.value
        webRtcLiveManager.configureAudioRouting(next)
        _isLiveSpeakerOn.value = next
    }

    fun leaveOrEndLiveRoom() {
        val room = _joinedLiveRoom.value ?: return
        val uid = Firebase.auth.currentUser?.uid
        val isHost = uid != null && uid == room.hostId
        cleanupLocalLiveResources()
        viewModelScope.launch {
            chatRepository.leaveOrEndLiveRoom(room.roomId, isHost = isHost)
        }
    }

    private fun cleanupLocalLiveResources() {
        liveMessagesJob?.cancel()
        liveMessagesJob = null
        liveSignalsJob?.cancel()
        liveSignalsJob = null
        liveDurationTickerJob?.cancel()
        liveDurationTickerJob = null
        processedLiveSignalIds.clear()
        webRtcLiveManager.endLiveSession()
        _joinedLiveRoom.value = null
        _liveRoomMessages.value = emptyList()
        _isLivePeerConnected.value = false
        _liveElapsedSeconds.value = 0
    }

    // ==================== PREFERÊNCIAS DE MATÉRIAS DE FOCO (FIRESTORE) ====================

    private fun resolveInitialTargetSubjectsCache(): TargetSubjectsPreference? {
        val cacheKey = Firebase.auth.currentUser?.uid
            ?: prefs.getString("last_firebase_uid", null)?.takeIf { it.isNotBlank() }
            ?: prefs.getString("auth_user_id", null)?.takeIf { it.isNotBlank() }
            ?: return null
        return targetSubjectsRepository.loadLocalPreferences(cacheKey)
    }

    fun restartTargetSubjectsObservation() {
        targetSubjectsJob?.cancel()
        targetSubjectsJob = null

        val fbUid = Firebase.auth.currentUser?.uid
        if (fbUid == null) {
            _isTargetSubjectsCloudSynced.value = false
            val cached = resolveInitialTargetSubjectsCache()
            if (cached != null) {
                _targetSubjectsPreference.value = cached
            }
            return
        }

        prefs.edit().putString("last_firebase_uid", fbUid).apply()
        targetSubjectsRepository.loadLocalPreferences(fbUid)?.let { local ->
            _targetSubjectsPreference.value = local
        }

        targetSubjectsJob = viewModelScope.launch {
            targetSubjectsRepository.observeTargetSubjects()
                .catch { error ->
                    if (error is CancellationException) throw error
                    android.util.Log.w("MainViewModel", "Target subjects stream error: ${error.message}")
                    val isPt = _language.value == Language.PT
                    if (Firebase.auth.currentUser?.uid == fbUid) {
                        _isTargetSubjectsCloudSynced.value = false
                        _targetSubjectsUiState.value = TargetSubjectsUiState.Error(
                            if (isPt) "Não foi possível sincronizar suas matérias com a nuvem."
                            else "Could not sync target subjects from cloud."
                        )
                    }
                }
                .collect { remotePref ->
                    if (remotePref != null) {
                        _targetSubjectsPreference.value = remotePref
                        _isTargetSubjectsCloudSynced.value = true
                    } else {
                        _isTargetSubjectsCloudSynced.value = true
                    }
                }
        }
    }

    fun saveTargetSubjectsPreferences(
        subjectIds: List<String>,
        dailyGoalMinutes: Int,
        learningTrack: String,
        studyGoalNote: String
    ) {
        val isPt = _language.value == Language.PT
        val fbUid = Firebase.auth.currentUser?.uid
        if (fbUid.isNullOrBlank()) {
            _targetSubjectsUiState.value = TargetSubjectsUiState.Error(
                if (isPt) "Entre com Google para salvar suas matérias no Cloud Firestore."
                else "Sign in with Google to save your target subjects in Cloud Firestore."
            )
            return
        }

        val cleanIds = TargetSubjectsCatalog.normalizeSubjectIds(subjectIds)
        if (cleanIds.isEmpty()) {
            _targetSubjectsUiState.value = TargetSubjectsUiState.Error(
                if (isPt) "Selecione pelo menos 1 matéria para continuar."
                else "Select at least 1 target subject to continue."
            )
            return
        }

        recordUserActivity()
        viewModelScope.launch {
            _targetSubjectsUiState.value = TargetSubjectsUiState.Saving
            val result = targetSubjectsRepository.saveTargetSubjects(
                subjectIds = cleanIds,
                dailyGoalMinutes = dailyGoalMinutes,
                learningTrack = learningTrack,
                studyGoalNote = studyGoalNote
            )
            result.onSuccess { savedPref ->
                _targetSubjectsPreference.value = savedPref
                _isTargetSubjectsCloudSynced.value = true
                if (_trackerSelectedTech.value.id !in savedPref.subjectIds) {
                    savedPref.selectedTechnologies().firstOrNull()?.let { firstTech ->
                        _trackerSelectedTech.value = firstTech
                    }
                }
                _targetSubjectsUiState.value = TargetSubjectsUiState.Success(
                    if (isPt) "${savedPref.subjectIds.size} matérias salvas no Firestore!"
                    else "${savedPref.subjectIds.size} target subjects saved to Firestore!"
                )
            }.onFailure { err ->
                _isTargetSubjectsCloudSynced.value = false
                _targetSubjectsUiState.value = TargetSubjectsUiState.Error(
                    err.localizedMessage ?: if (isPt) "Erro ao salvar preferências no Firestore."
                    else "Failed to save preferences to Firestore."
                )
            }
        }
    }

    fun clearTargetSubjectsFeedback() {
        if (_targetSubjectsUiState.value !is TargetSubjectsUiState.Saving) {
            _targetSubjectsUiState.value = TargetSubjectsUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        playBillingManager.endConnection()
        targetSubjectsJob?.cancel()
        cleanupLocalCallResources()
        cleanupLocalLiveResources()
    }

    fun signOut() {
        targetSubjectsJob?.cancel()
        targetSubjectsJob = null
        _targetSubjectsPreference.value = null
        _isTargetSubjectsCloudSynced.value = false
        _targetSubjectsUiState.value = TargetSubjectsUiState.Idle
        cleanupLocalCallResources()
        cleanupLocalLiveResources()
        liveRoomsJob?.cancel()
        liveRoomsJob = null
        _activeLiveRooms.value = emptyList()
        closeActiveChatConversation()
        try {
            Firebase.auth.signOut()
        } catch (e: Exception) {
            // ignore
        }
        _userAccount.value = null
        saveUserAccountToPrefs(null)
        restartLeaderboardObservation(null)
        restartDailyProgressObservation(null)
        restartChatSubsystem()
        val isPt = _language.value == Language.PT
        _pointsFeedback.value = if (isPt) "Sessão encerrada com sucesso." else "Signed out successfully."
        _currentDestination.value = AppDestination.LOGIN
    }

    fun clearScoreData() {
        prefs.edit()
            .remove("user_total_score")
            .remove("user_correct_quiz_count")
            .remove("user_completed_act_count")
            .remove("completed_act_ids")
            .remove("completed_quiz_ids")
            .remove("user_last_known_level")
            .apply()
        _scoreInfo.value = calculateScoreInfo(0, 0, 0, emptySet(), emptySet())
    }

    // -------------------------------------------------------------------------
    // Dedicated IDLEs for all 12 Programming Languages
    // -------------------------------------------------------------------------

    private val _idleSelectedTech = MutableStateFlow(Technology.PYTHON)
    val idleSelectedTech: StateFlow<Technology> = _idleSelectedTech.asStateFlow()

    private val _idleCodeByTech = MutableStateFlow(
        Technology.entries.associateWith { tech ->
            LanguageIdleCodeStorage.loadCode(prefs, tech)
        }
    )
    val idleCodeByTech: StateFlow<Map<Technology, String>> = _idleCodeByTech.asStateFlow()

    private val _idleResultByTech = MutableStateFlow<Map<Technology, IdleExecutionResult>>(emptyMap())
    val idleResultByTech: StateFlow<Map<Technology, IdleExecutionResult>> = _idleResultByTech.asStateFlow()

    fun selectIdleTech(tech: Technology) {
        _idleSelectedTech.value = tech
        recordUserActivity()
    }

    fun openLanguageIdle(tech: Technology = _trackerSelectedTech.value) {
        _idleSelectedTech.value = tech
        _currentDestination.value = AppDestination.LANGUAGE_IDLE
        recordUserActivity()
    }

    fun updateIdleCode(tech: Technology, code: String) {
        _idleCodeByTech.value = _idleCodeByTech.value + (tech to code)
        LanguageIdleCodeStorage.saveCode(prefs, tech, code)
        recordUserActivity()
    }

    fun resetIdleCode(tech: Technology) {
        val defaultCode = LanguageIdleCodeStorage.resetToDefault(prefs, tech)
        _idleCodeByTech.value = _idleCodeByTech.value + (tech to defaultCode)
        _idleResultByTech.value = _idleResultByTech.value - tech
        recordUserActivity()
    }

    fun clearIdleOutput(tech: Technology) {
        _idleResultByTech.value = _idleResultByTech.value - tech
        recordUserActivity()
    }

    fun runIdleCode(tech: Technology) {
        val codeToRun = _idleCodeByTech.value[tech]
            ?: LanguageIdleCatalog.getSpec(tech).templates.first().code
        _idleResultByTech.value = _idleResultByTech.value + (
            tech to IdleExecutionResult(
                tech = tech,
                status = IdleExecutionStatus.RUNNING
            )
        )
        recordUserActivity()

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                LanguageIdleEngine.execute(
                    tech = tech,
                    rawCode = codeToRun,
                    language = _language.value,
                    sqlLabRepository = sqlLabRepository
                )
            }
            _idleResultByTech.value = _idleResultByTech.value + (tech to result)
        }
    }
}
