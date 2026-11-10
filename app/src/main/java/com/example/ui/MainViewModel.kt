package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiStudyService
import com.example.data.local.AppDatabase
import com.example.data.local.RoutineWithEvents
import com.example.data.local.UserBadgeEntity
import com.example.data.repository.StudyRepository
import com.example.data.repository.UserBadgeRepository
import com.example.data.repository.LeaderboardRepository
import com.example.data.repository.FirestoreProgressRepository
import com.example.data.repository.LanguageDailyProgress
import com.example.model.AiChatMessage
import com.example.model.AuthMethod
import com.example.model.Language
import com.example.model.LeaderboardEntry
import com.example.model.RoutinePlan
import com.example.model.StudySession
import com.example.model.Technology
import com.example.model.ThemePreference
import com.example.model.UserAccount
import com.example.model.UserScoreInfo
import com.example.notification.NotificationScheduler
import com.example.notification.StudyReminderWorker
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    SETTINGS("settings")
}

data class UiStreakInfo(
    val streakDays: Int,
    val completedCount: Int,
    val totalMinutes: Int
)

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

    private var dailyProgressJob: kotlinx.coroutines.Job? = null

    fun setTrackerSelectedTech(tech: Technology) {
        _trackerSelectedTech.value = tech
    }

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
        if (prefs.getString("app_lang", "pt") == "en") Language.EN else Language.PT
    )
    val language: StateFlow<Language> = _language.asStateFlow()

    // Theme state
    private val _themePreference = MutableStateFlow(
        when (prefs.getString("app_theme", "dark")) {
            "light" -> ThemePreference.LIGHT
            "system" -> ThemePreference.SYSTEM
            else -> ThemePreference.DARK
        }
    )
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()

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
            val sessions = allSessions.value
            val streak = streakInfo.value.streakDays
            val score = scoreInfo.value
            val techScores = Technology.values().associateWith { getTechScore(it) }
            val hasAiRoutine = allRoutinesWithEvents.value.any { it.events.any { ev -> ev.aiGenerated } } || sessions.any { it.aiGenerated }
            val newlyUnlocked = badgeRepository.evaluateAndSyncBadges(
                streakDays = streak,
                completedSessions = sessions.filter { it.isCompleted },
                scoreInfo = score,
                techScores = techScores,
                hasAiRoutine = hasAiRoutine
            )
            if (newlyUnlocked.isNotEmpty()) {
                val badge = newlyUnlocked.first()
                _newlyUnlockedBadge.value = badge
                val isPt = _language.value == Language.PT
                _pointsFeedback.value = if (isPt) {
                    "🏆 Emblema Desbloqueado: ${badge.titlePt} (+${badge.xpReward} XP)!"
                } else {
                    "🏆 Badge Unlocked: ${badge.titleEn} (+${badge.xpReward} XP)!"
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            repository.populateStarterRoutinesIfEmpty()
            badgeRepository.initializeBadgesIfEmpty()
            triggerBadgeEvaluation()
        }
        setupInitialAiGreeting()
        StudyReminderWorker.createNotificationChannel(application)
        NotificationScheduler.scheduleDailyStudyCheck(application)

        // Observe streak & session changes to evaluate badges
        viewModelScope.launch {
            streakInfo.collect {
                triggerBadgeEvaluation()
            }
        }

        _userAccount.value = loadSavedUserAccount()
        restartLeaderboardObservation(_userAccount.value)
        restartDailyProgressObservation(_userAccount.value)
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
    }

    fun openTechDetail(tech: Technology) {
        _selectedTech.value = tech
        _currentDestination.value = AppDestination.TECH_DETAIL
    }

    fun setLanguage(lang: Language) {
        _language.value = lang
        prefs.edit().putString("app_lang", lang.code).apply()
        // Refresh greeting if only 1 message exists
        if (_aiMessages.value.size <= 1) {
            setupInitialAiGreeting()
        }
    }

    fun setThemePreference(pref: ThemePreference) {
        _themePreference.value = pref
        prefs.edit().putString("app_theme", pref.code).apply()
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
        val (levelNum, titlePt, titleEn, minScore, maxScore) = when {
            score >= 1000 -> Tuple5(5, "Dev Master / Tech Lead", "Dev Master / Tech Lead", 1000, 2000)
            score >= 500 -> Tuple5(4, "Dev Sênior", "Senior Dev", 500, 1000)
            score >= 250 -> Tuple5(3, "Dev Pleno", "Mid-Level Dev", 250, 500)
            score >= 100 -> Tuple5(2, "Dev Júnior", "Junior Dev", 100, 250)
            else -> Tuple5(1, "Aspirante a Dev", "Aspiring Dev", 0, 100)
        }

        val range = (maxScore - minScore).coerceAtLeast(1)
        val progress = ((score - minScore).toFloat() / range).coerceIn(0f, 1f)

        return UserScoreInfo(
            totalScore = score,
            correctQuizCount = quizCount,
            completedActivitiesCount = actCount,
            levelNumber = levelNum,
            levelTitlePt = titlePt,
            levelTitleEn = titleEn,
            currentLevelMin = minScore,
            nextLevelTarget = maxScore,
            progressFraction = progress,
            completedActivityIds = actIds,
            completedQuizIds = quizIds
        )
    }

    fun awardQuizPoints(tech: Technology, questionId: Int, points: Int = 20) {
        val current = _scoreInfo.value
        val isFirstTime = !current.completedQuizIds.contains(questionId)
        val pointsToAdd = if (isFirstTime) points else 5 // Bonus for review if already answered

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

        _scoreInfo.value = calculateScoreInfo(
            score = newScore,
            quizCount = newQuizCount,
            actCount = current.completedActivitiesCount,
            actIds = current.completedActivityIds,
            quizIds = newQuizIds
        )

        val isPt = _language.value == Language.PT
        _pointsFeedback.value = if (isPt) "+$pointsToAdd XP! Resposta Correta 🎯" else "+$pointsToAdd XP! Correct Answer 🎯"
        triggerBadgeEvaluation()
        val user = _userAccount.value
        if (user != null) {
            syncScoreToLeaderboard()
            viewModelScope.launch {
                val techXp = prefs.getInt("score_tech_${tech.id}", 0)
                val actCount = current.completedActivityIds.count { it.startsWith(tech.id.take(2)) }
                progressRepository.recordLanguageProgress(
                    userId = user.userId,
                    tech = tech,
                    completedChallenges = actCount,
                    completedQuizzes = newQuizCount,
                    xpEarned = techXp
                )
            }
        }
    }

    fun awardActivityPoints(tech: Technology, activityId: String, points: Int = 50) {
        val current = _scoreInfo.value
        val isFirstTime = !current.completedActivityIds.contains(activityId)
        val pointsToAdd = if (isFirstTime) points else 10 // Practice repetition bonus

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

        _scoreInfo.value = calculateScoreInfo(
            score = newScore,
            quizCount = current.correctQuizCount,
            actCount = newActCount,
            actIds = newActIds,
            quizIds = current.completedQuizIds
        )

        val isPt = _language.value == Language.PT
        _pointsFeedback.value = if (isPt) "+$pointsToAdd XP! Atividade Concluída 🚀" else "+$pointsToAdd XP! Activity Completed 🚀"
        triggerBadgeEvaluation()
        val user = _userAccount.value
        if (user != null) {
            syncScoreToLeaderboard()
            viewModelScope.launch {
                val techXp = prefs.getInt("score_tech_${tech.id}", 0)
                val actCount = newActIds.count { it.startsWith(tech.id.take(2)) }
                progressRepository.recordLanguageProgress(
                    userId = user.userId,
                    tech = tech,
                    completedChallenges = actCount,
                    completedQuizzes = current.correctQuizCount,
                    xpEarned = techXp
                )
            }
        }
    }

    fun dismissPointsFeedback() {
        _pointsFeedback.value = null
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
        val (lvl, titlePt, titleEn) = when {
            score >= 1000 -> Triple(5, "Dev Master / Tech Lead", "Dev Master / Tech Lead")
            score >= 500 -> Triple(4, "Dev Sênior", "Senior Dev")
            score >= 250 -> Triple(3, "Dev Pleno", "Mid-Level Dev")
            score >= 100 -> Triple(2, "Dev Júnior", "Junior Dev")
            else -> Triple(1, "Aspirante a Dev", "Aspiring Dev")
        }

        return UserAccount(
            userId = userId,
            displayName = displayName,
            email = email,
            phoneNumber = phone,
            photoUrl = photo,
            authMethod = authMethod,
            totalScore = score,
            levelNumber = lvl,
            levelTitlePt = titlePt,
            levelTitleEn = titleEn,
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
        val uid = account?.userId ?: ""
        dailyProgressJob = viewModelScope.launch {
            progressRepository.observeAllLanguagesTodayProgress(uid).collect { map ->
                _todayLanguageProgressMap.value = map
            }
        }
    }

    fun logQuickStudySession(tech: Technology, minutes: Int, xp: Int) {
        viewModelScope.launch {
            val uid = _userAccount.value?.userId ?: ""
            val success = progressRepository.logQuickStudySession(uid, tech, minutes, xp)
            if (success) {
                val current = _scoreInfo.value
                val newScore = current.totalScore + xp
                val techKey = "score_tech_${tech.id}"
                val currentTechScore = prefs.getInt(techKey, 0)
                prefs.edit()
                    .putInt("user_total_score", newScore)
                    .putInt(techKey, currentTechScore + xp)
                    .apply()

                _scoreInfo.value = calculateScoreInfo(
                    score = newScore,
                    quizCount = current.correctQuizCount,
                    actCount = current.completedActivitiesCount,
                    actIds = current.completedActivityIds,
                    quizIds = current.completedQuizIds
                )

                triggerBadgeEvaluation()
                val isPt = _language.value == Language.PT
                _pointsFeedback.value = if (isPt) "⏱️ +$minutes min e +$xp XP registrados em ${tech.displayName} no Firestore!"
                else "⏱️ +$minutes min and +$xp XP logged for ${tech.displayName} in Firestore!"

                if (_userAccount.value != null) {
                    syncScoreToLeaderboard()
                }
            }
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
                } catch (authEx: Exception) {
                    android.util.Log.w("MainViewModel", "Firebase signInWithCredential: ${authEx.message}")
                }

                val score = _scoreInfo.value
                val streak = streakInfo.value.streakDays
                val badges = unlockedBadgesCount.value
                val resolvedName = displayName?.takeIf { it.isNotBlank() }
                    ?: Firebase.auth.currentUser?.displayName?.takeIf { it.isNotBlank() }
                    ?: email?.substringBefore("@")
                    ?: "Dev Google"

                // Construct unique user id preferring the Firebase Auth UID
                val uid = firebaseUid ?: Firebase.auth.currentUser?.uid ?: ("g_" + (email ?: "user").replace(Regex("[^a-zA-Z0-9]"), "_"))

                val account = UserAccount(
                    userId = uid,
                    displayName = resolvedName,
                    email = email ?: Firebase.auth.currentUser?.email ?: "",
                    phoneNumber = extraPhone.ifBlank { Firebase.auth.currentUser?.phoneNumber ?: "" },
                    photoUrl = photoUrl ?: Firebase.auth.currentUser?.photoUrl?.toString() ?: "",
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

                // Sync to Firestore Leaderboard
                leaderboardRepository.syncUserToLeaderboard(account)
                restartLeaderboardObservation(account)
                restartDailyProgressObservation(account)

                val isPt = _language.value == Language.PT
                _pointsFeedback.value = if (isPt) "🎉 Bem-vindo, $resolvedName! Autenticado com Firebase e salvo no Placar."
                else "🎉 Welcome, $resolvedName! Authenticated with Firebase and saved to Leaderboard."

                // Route to main dashboard
                _currentDestination.value = AppDestination.HOME
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
                val score = _scoreInfo.value
                val streak = streakInfo.value.streakDays
                val badges = unlockedBadgesCount.value
                val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
                val uid = "ph_" + if (cleanPhone.isNotBlank()) cleanPhone else System.currentTimeMillis().toString()

                val account = UserAccount(
                    userId = uid,
                    displayName = name,
                    phoneNumber = phoneNumber,
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

                leaderboardRepository.syncUserToLeaderboard(account)
                restartLeaderboardObservation(account)
                restartDailyProgressObservation(account)

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
            val badges = unlockedBadgesCount.value
            val updated = current.copy(
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
            val success = leaderboardRepository.syncUserToLeaderboard(updated)
            _isAuthLoading.value = false
            val isPt = _language.value == Language.PT
            if (success) {
                _pointsFeedback.value = if (isPt) "⚡ Placar sincronizado com sucesso no Cloud Firestore!"
                else "⚡ Leaderboard synced successfully to Cloud Firestore!"
            }
        }
    }

    fun signOut() {
        try {
            Firebase.auth.signOut()
        } catch (e: Exception) {
            // ignore
        }
        _userAccount.value = null
        saveUserAccountToPrefs(null)
        restartLeaderboardObservation(null)
        restartDailyProgressObservation(null)
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
            .apply()
        _scoreInfo.value = calculateScoreInfo(0, 0, 0, emptySet(), emptySet())
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
