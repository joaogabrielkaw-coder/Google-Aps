package com.example

import com.example.data.challenges.TechChallengeBank
import com.example.data.repository.TechContentRepository
import com.example.model.ActivityDifficulty
import com.example.model.AiStudyTopicsCatalog
import com.example.model.Technology
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun allTechnologies_have105OrderedChallengesAndCompleteContent() {
    assertEquals(12, Technology.entries.size)
    assertTrue(Technology.entries.indexOf(Technology.JAVASCRIPT) < Technology.entries.indexOf(Technology.TYPESCRIPT))
    assertTrue(Technology.entries.indexOf(Technology.TYPESCRIPT) < Technology.entries.indexOf(Technology.KOTLIN))
    assertTrue(Technology.entries.indexOf(Technology.KOTLIN) < Technology.entries.indexOf(Technology.GO))

    val allIds = mutableSetOf<String>()
    Technology.entries.forEach { tech ->
      val challenges = TechChallengeBank.getChallenges(tech)
      assertEquals("Challenge count mismatch for ${tech.name}", 105, challenges.size)

      // 1..35 Beginner (30 XP), 36..70 Intermediate (60 XP), 71..105 Advanced (90 XP)
      challenges.forEachIndexed { index, activity ->
        assertTrue("Duplicate activity id: ${activity.id}", allIds.add(activity.id))
        assertEquals(tech, activity.tech)
        when {
          index < 35 -> {
            assertEquals(ActivityDifficulty.BEGINNER, activity.difficulty)
            assertEquals(30, activity.pointsValue)
          }
          index < 70 -> {
            assertEquals(ActivityDifficulty.INTERMEDIATE, activity.difficulty)
            assertEquals(60, activity.pointsValue)
          }
          else -> {
            assertEquals(ActivityDifficulty.ADVANCED, activity.difficulty)
            assertEquals(90, activity.pointsValue)
          }
        }
      }

      assertTrue(TechContentRepository.getOverview(tech, isPt = true).isNotBlank())
      assertTrue(TechContentRepository.getOverview(tech, isPt = false).isNotBlank())
      assertTrue(TechContentRepository.getCheatsheet(tech).isNotEmpty())
      assertTrue(TechContentRepository.getRoadmap(tech).isNotEmpty())
      assertTrue(TechContentRepository.getQuizQuestions(tech).isNotEmpty())
      assertTrue(AiStudyTopicsCatalog.getSuggestionsForTech(tech).isNotEmpty())
    }

    assertEquals(1260, TechChallengeBank.getTotalChallengesCount())
  }

  @Test
  fun dailyLanguageSeals_coverAll12LanguagesAndThreeTiers() {
    val dailyBadges = com.example.data.repository.UserBadgeRepository.DEFAULT_BADGES.filter {
      it.category == "DAILY_TECH"
    }
    assertEquals(36, dailyBadges.size)

    Technology.entries.forEach { tech ->
      val bronze = dailyBadges.firstOrNull { it.id == "badge_daily_${tech.id}_bronze" }
      val silver = dailyBadges.firstOrNull { it.id == "badge_daily_${tech.id}_silver" }
      val gold = dailyBadges.firstOrNull { it.id == "badge_daily_${tech.id}_gold" }

      assertNotNull(bronze)
      assertNotNull(silver)
      assertNotNull(gold)

      assertEquals(3, bronze!!.maxProgress)
      assertEquals(60, bronze.xpReward)
      assertEquals(5, silver!!.maxProgress)
      assertEquals(120, silver.xpReward)
      assertEquals(10, gold!!.maxProgress)
      assertEquals(250, gold.xpReward)

      assertEquals(
        tech to com.example.ui.components.DailySealTier.BRONZE,
        com.example.ui.components.parseDailySealKey(bronze.iconKey)
      )
      assertEquals(
        tech to com.example.ui.components.DailySealTier.SILVER,
        com.example.ui.components.parseDailySealKey(silver.iconKey)
      )
      assertEquals(
        tech to com.example.ui.components.DailySealTier.GOLD,
        com.example.ui.components.parseDailySealKey(gold.iconKey)
      )
    }
  }

  @Test
  fun dailyLanguageSeals_unlockAtThresholdsAndPersistAcrossDays() = kotlinx.coroutines.test.runTest {
    val fakeDao = InMemoryUserBadgeDao()
    val repo = com.example.data.repository.UserBadgeRepository(fakeDao)
    repo.initializeBadgesIfEmpty()

    val emptyScore = com.example.model.UserScoreInfo()

    // 2 JavaScript challenges -> nothing unlocked yet, progress updated to 2
    val afterTwo = repo.evaluateAndSyncBadges(
      streakDays = 0,
      completedSessions = emptyList(),
      scoreInfo = emptyScore,
      techScores = emptyMap(),
      hasAiRoutine = false,
      dailyChallengeCountsByTech = mapOf(Technology.JAVASCRIPT to 2)
    )
    assertTrue(afterTwo.none { it.category == "DAILY_TECH" })
    assertEquals(2, fakeDao.getBadgeById("badge_daily_javascript_bronze")?.currentProgress)
    assertFalse(fakeDao.getBadgeById("badge_daily_javascript_bronze")?.isUnlocked ?: true)

    // 3 JavaScript challenges -> Bronze unlocked, Silver still locked at 3/5
    val afterThree = repo.evaluateAndSyncBadges(
      streakDays = 0,
      completedSessions = emptyList(),
      scoreInfo = emptyScore,
      techScores = emptyMap(),
      hasAiRoutine = false,
      dailyChallengeCountsByTech = mapOf(Technology.JAVASCRIPT to 3)
    )
    assertEquals(listOf("badge_daily_javascript_bronze"), afterThree.map { it.id })
    assertTrue(fakeDao.getBadgeById("badge_daily_javascript_bronze")?.isUnlocked == true)
    assertFalse(fakeDao.getBadgeById("badge_daily_javascript_silver")?.isUnlocked ?: true)
    assertFalse(fakeDao.getBadgeById("badge_daily_python_bronze")?.isUnlocked ?: true)

    // 10 JavaScript challenges -> Silver and Gold both unlock
    val afterTen = repo.evaluateAndSyncBadges(
      streakDays = 0,
      completedSessions = emptyList(),
      scoreInfo = emptyScore,
      techScores = emptyMap(),
      hasAiRoutine = false,
      dailyChallengeCountsByTech = mapOf(Technology.JAVASCRIPT to 10)
    )
    val unlockedIds = afterTen.map { it.id }.toSet()
    assertTrue(unlockedIds.contains("badge_daily_javascript_silver"))
    assertTrue(unlockedIds.contains("badge_daily_javascript_gold"))

    // Next day count resets to 0 -> previously unlocked seals stay permanently unlocked
    val nextDay = repo.evaluateAndSyncBadges(
      streakDays = 0,
      completedSessions = emptyList(),
      scoreInfo = emptyScore,
      techScores = emptyMap(),
      hasAiRoutine = false,
      dailyChallengeCountsByTech = mapOf(Technology.JAVASCRIPT to 0)
    )
    assertTrue(nextDay.none { it.id.startsWith("badge_daily_javascript_") })
    assertTrue(fakeDao.getBadgeById("badge_daily_javascript_bronze")?.isUnlocked == true)
    assertTrue(fakeDao.getBadgeById("badge_daily_javascript_silver")?.isUnlocked == true)
    assertTrue(fakeDao.getBadgeById("badge_daily_javascript_gold")?.isUnlocked == true)
    assertEquals(3, fakeDao.getBadgeById("badge_daily_javascript_bronze")?.currentProgress)
  }

  @Test
  fun weeklyChallengeSummary_aggregatesDailyTotalsPerLanguage() {
    val day1 = com.example.data.repository.DailyLanguageChallengeSummary(
      dateIso = "2026-11-06",
      countsByTech = mapOf(Technology.PYTHON to 3, Technology.JAVASCRIPT to 2)
    )
    val day2 = com.example.data.repository.DailyLanguageChallengeSummary(
      dateIso = "2026-11-07",
      countsByTech = mapOf(Technology.JAVASCRIPT to 4, Technology.KOTLIN to 1)
    )
    assertEquals(5, day1.totalChallenges)
    assertEquals(5, day2.totalChallenges)
    val week = listOf(day1, day2)
    val jsTotal = week.sumOf { it.countsByTech[Technology.JAVASCRIPT] ?: 0 }
    val pyTotal = week.sumOf { it.countsByTech[Technology.PYTHON] ?: 0 }
    val ktTotal = week.sumOf { it.countsByTech[Technology.KOTLIN] ?: 0 }
    assertEquals(6, jsTotal)
    assertEquals(3, pyTotal)
    assertEquals(1, ktTotal)
    assertEquals(3, com.example.ui.components.WeeklyChartMode.entries.size)
  }

  @Test
  fun dailyLivesManager_grants25DailyLives_consumesOnePerAttempt_andResetsOnNewDay() {
    val prefs = InMemorySharedPreferences()
    val day1 = "2026-11-12"
    val day2 = "2026-11-13"

    val initial = com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, day1)
    assertEquals(25, initial.remaining)
    assertEquals(25, initial.maxDaily)

    // Consume all 25 lives
    for (expected in 24 downTo 0) {
      val (consumed, info) = com.example.ui.DailyLivesManager.consumeOneLife(prefs, day1)
      assertTrue(consumed)
      assertEquals(expected, info.remaining)
    }

    // 26th attempt same day fails cleanly
    val (blocked, emptyInfo) = com.example.ui.DailyLivesManager.consumeOneLife(prefs, day1)
    assertFalse(blocked)
    assertEquals(0, emptyInfo.remaining)

    // Next day resets back to 25 fresh lives
    val refreshed = com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, day2)
    assertEquals(25, refreshed.remaining)
    val (afterResetConsumed, afterResetInfo) = com.example.ui.DailyLivesManager.consumeOneLife(prefs, day2)
    assertTrue(afterResetConsumed)
    assertEquals(24, afterResetInfo.remaining)
  }

  @Test
  fun weeklyStudySummaryPrompt_includesAll7DaysAndLanguageBreakdownInPtAndEn() {
    val history = listOf(
      com.example.data.repository.DailyLanguageChallengeSummary(
        dateIso = "2026-11-10",
        countsByTech = mapOf(Technology.JAVASCRIPT to 4, Technology.PYTHON to 2)
      ),
      com.example.data.repository.DailyLanguageChallengeSummary(
        dateIso = "2026-11-11",
        countsByTech = mapOf(Technology.TYPESCRIPT to 3, Technology.KOTLIN to 1)
      )
    )
    val scoreInfo = com.example.model.UserScoreInfo(
      totalScore = 540,
      correctQuizCount = 12,
      completedActivitiesCount = 10,
      levelNumber = 4,
      levelTitlePt = "Dev Sênior",
      levelTitleEn = "Senior Dev"
    )

    val ptPrompt = com.example.data.ai.AiStudyService.buildWeeklySummaryPrompt(
      weeklyHistory = history,
      scoreInfo = scoreInfo,
      streakDays = 5,
      unlockedBadgesCount = 6,
      dailySealsUnlockedCount = 2,
      remainingLivesToday = 21,
      techScores = mapOf(Technology.JAVASCRIPT to 180, Technology.TYPESCRIPT to 90),
      language = com.example.model.Language.PT
    )
    assertTrue(ptPrompt.contains("540 XP"))
    assertTrue(ptPrompt.contains("10 desafios em 2/7 dias ativos"))
    assertTrue(ptPrompt.contains("JavaScript (javascript): 4"))
    assertTrue(ptPrompt.contains("TypeScript (typescript): 3"))
    assertTrue(ptPrompt.contains("21 / 25"))
    assertTrue(ptPrompt.contains("go"))

    val enPrompt = com.example.data.ai.AiStudyService.buildWeeklySummaryPrompt(
      weeklyHistory = history,
      scoreInfo = scoreInfo,
      streakDays = 5,
      unlockedBadgesCount = 6,
      dailySealsUnlockedCount = 2,
      remainingLivesToday = 21,
      techScores = mapOf(Technology.JAVASCRIPT to 180),
      language = com.example.model.Language.EN
    )
    assertTrue(enPrompt.contains("10 challenges across 2/7 active days"))
    assertTrue(enPrompt.contains("Daily Language Seals: 2"))
    assertTrue(enPrompt.contains("2026-11-10: total=6"))
  }

  @Test
  fun weeklySummaryUiState_modelsIdleLoadingSuccessAndErrorProperly() {
    val feedback = com.example.model.WeeklyStudySummaryFeedback(
      headline = "Strong JavaScript Momentum!",
      overallAssessment = "You solved 10 challenges across 2 active days.",
      consistencyLabel = "Rising Streak",
      strengths = listOf("Consistent JavaScript practice"),
      growthAreas = listOf("Add SQL practice twice next week"),
      nextWeekActionPlan = "Solve 3 challenges daily in JS & SQL.",
      recommendedTechId = "sql",
      recommendedDailyTarget = 3,
      encouragementQuote = "Small daily commits build lasting mastery."
    )

    val idle: com.example.model.WeeklySummaryUiState = com.example.model.WeeklySummaryUiState.Idle
    val loading: com.example.model.WeeklySummaryUiState = com.example.model.WeeklySummaryUiState.Loading
    val success: com.example.model.WeeklySummaryUiState = com.example.model.WeeklySummaryUiState.Success(feedback)
    val error: com.example.model.WeeklySummaryUiState = com.example.model.WeeklySummaryUiState.Error("Timeout")

    assertTrue(idle is com.example.model.WeeklySummaryUiState.Idle)
    assertTrue(loading is com.example.model.WeeklySummaryUiState.Loading)
    assertEquals("sql", (success as com.example.model.WeeklySummaryUiState.Success).feedback.recommendedTechId)
    assertEquals("Timeout", (error as com.example.model.WeeklySummaryUiState.Error).message)
  }

  @Test
  fun sqlLabRepository_validatesReadOnlySqlSafetyCorrectly() {
    assertNull(com.example.data.repository.SqlLabRepository.validateReadOnlySql("SELECT * FROM study_notes"))
    assertNull(com.example.data.repository.SqlLabRepository.validateReadOnlySql("PRAGMA table_info(game_scores)"))
    assertNull(com.example.data.repository.SqlLabRepository.validateReadOnlySql("EXPLAIN QUERY PLAN SELECT * FROM user_badges"))

    assertNotNull(com.example.data.repository.SqlLabRepository.validateReadOnlySql("DROP TABLE study_notes"))
    assertNotNull(com.example.data.repository.SqlLabRepository.validateReadOnlySql("DELETE FROM game_scores"))
    assertNotNull(com.example.data.repository.SqlLabRepository.validateReadOnlySql("SELECT * FROM study_notes; DROP TABLE study_notes"))
  }

  @Test
  fun devCore3DEngine_buildsCompositeMultiPartModelsAndProjectsForwardPoints() {
    val craftFaces = com.example.ui.game3d.buildDevRunnerCraftFaces(com.example.ui.game3d.Vec3(0f, 0f, 0f))
    val botFaces = com.example.ui.game3d.buildDevBotExplorerFaces(com.example.ui.game3d.Vec3(0f, 0f, 0f), 0f)
    val clusterFaces = com.example.ui.game3d.buildSqlClusterTowerFaces(com.example.ui.game3d.Vec3(0f, 0f, 0f), 0f, 0f)

    // Each composite procedural model consists of many distinct faces (> 30)
    assertTrue(craftFaces.size >= 40)
    assertTrue(botFaces.size >= 45)
    assertTrue(clusterFaces.size >= 60)

    val cam = com.example.ui.game3d.Camera3D(
      position = com.example.ui.game3d.Vec3(0f, 3f, -5f),
      target = com.example.ui.game3d.Vec3(0f, 0f, 2f)
    )
    val projectedCenter = cam.project(com.example.ui.game3d.Vec3(0f, 0f, 2f), 1080f, 1920f)
    assertNotNull(projectedCenter)
    assertTrue(projectedCenter!!.depth > 0f)
  }

  @Test
  fun audioTranscriptionUiState_capturesStatesAndTranscriptCleanly() {
    val rec: com.example.model.AudioTranscriptionUiState = com.example.model.AudioTranscriptionUiState.Recording
    val trans: com.example.model.AudioTranscriptionUiState = com.example.model.AudioTranscriptionUiState.Transcribing
    val ok: com.example.model.AudioTranscriptionUiState = com.example.model.AudioTranscriptionUiState.Success("Test SQL JOIN", 4)
    val err: com.example.model.AudioTranscriptionUiState = com.example.model.AudioTranscriptionUiState.Error("Permission needed")

    assertTrue(rec is com.example.model.AudioTranscriptionUiState.Recording)
    assertTrue(trans is com.example.model.AudioTranscriptionUiState.Transcribing)
    assertEquals("Test SQL JOIN", (ok as com.example.model.AudioTranscriptionUiState.Success).transcript)
    assertEquals(4, ok.durationSeconds)
    assertEquals("Permission needed", (err as com.example.model.AudioTranscriptionUiState.Error).message)
  }

  @Test
  fun themeCustomization_keepsPurpleCrimsonDefaultAndRoundTripsStorageMap() {
    val defaultTheme = com.example.model.ThemeCustomization()
    assertTrue(defaultTheme.isDefaultPalette)
    assertEquals(com.example.model.ThemePreference.DARK, defaultTheme.themeMode)
    assertEquals("#9D4EDD", com.example.model.ThemeCustomization.formatHex(defaultTheme.primaryColorHex))
    assertEquals("#FF2A55", com.example.model.ThemeCustomization.formatHex(defaultTheme.secondaryColorHex))
    assertEquals(12, com.example.model.ThemePreset.entries.size)

    val parsedHex = com.example.model.ThemeCustomization.parseHexColorOrNull("#00b4d8")
    assertEquals(0xFF00B4D8L, parsedHex)
    assertNull(com.example.model.ThemeCustomization.parseHexColorOrNull("not-hex"))

    val custom = com.example.model.ThemeCustomization.fromPreset(
      preset = com.example.model.ThemePreset.CYBER_OCEAN,
      themeMode = com.example.model.ThemePreference.LIGHT,
      surfaceStyle = com.example.model.ThemeSurfaceStyle.SLATE,
      backgroundTintIntensity = 0.72f
    )
    assertFalse(custom.isDefaultPalette)

    val restored = com.example.model.ThemeCustomization.fromStorageMap(custom.toStorageMap())
    assertNotNull(restored)
    assertEquals(custom.themeMode, restored!!.themeMode)
    assertEquals(custom.presetId, restored.presetId)
    assertEquals(custom.primaryColorHex, restored.primaryColorHex)
    assertEquals(custom.secondaryColorHex, restored.secondaryColorHex)
    assertEquals(custom.tertiaryColorHex, restored.tertiaryColorHex)
    assertEquals(custom.surfaceStyle, restored.surfaceStyle)
    assertEquals(custom.backgroundTintIntensity, restored.backgroundTintIntensity, 0.001f)
    assertEquals(custom.updatedAtMillis, restored.updatedAtMillis)
  }

  @Test
  fun dailyScreenTimeManager_tracksRealMinutes_capsFocusMilestones_andResetsDaily() {
    val prefs = InMemorySharedPreferences()
    val day1 = "2026-11-12"
    val day2 = "2026-11-13"

    assertEquals(0, com.example.ui.DailyScreenTimeManager.loadOrResetTodayMinutes(prefs, day1))

    var milestoneAwardsDay1 = 0
    var lastTotal = 0
    for (minute in 1..75) {
      val result = com.example.ui.DailyScreenTimeManager.incrementOneRealMinute(prefs, day1)
      assertEquals(minute, result.todayMinutes)
      assertEquals(minute, result.totalMinutes)
      lastTotal = result.totalMinutes
      if (result.awardedFocusMilestoneBonus) milestoneAwardsDay1++
    }
    assertEquals(75, lastTotal)
    // Only 15, 30, 45, 60 award focus XP bonus (4/day max); minute 75 still increments real minutes without awarding extra XP
    assertEquals(4, milestoneAwardsDay1)

    // New day resets daily goal minutes & milestones, while keeping cumulative total minutes
    assertEquals(0, com.example.ui.DailyScreenTimeManager.loadOrResetTodayMinutes(prefs, day2))
    val firstMinDay2 = com.example.ui.DailyScreenTimeManager.incrementOneRealMinute(prefs, day2)
    assertEquals(1, firstMinDay2.todayMinutes)
    assertEquals(76, firstMinDay2.totalMinutes)
    assertFalse(firstMinDay2.awardedFocusMilestoneBonus)
  }

  @Test
  fun dailyScreenTimeManager_advancesActiveSeconds_rollsOverMinutes_andCalculatesGoalProgress() {
    val prefs = InMemorySharedPreferences()
    val day = "2026-11-13"

    assertEquals(0, com.example.ui.DailyScreenTimeManager.loadOrResetTodaySeconds(prefs, day))

    // Advance 59 active seconds -> still minute 0, no minute completion event yet
    for (s in 1..59) {
      val step = com.example.ui.DailyScreenTimeManager.advanceOneActiveSecond(prefs, day)
      assertEquals(s, step.todaySeconds)
      assertEquals(0, step.todayMinutes)
      assertFalse(step.completedNewMinute)
      assertFalse(step.awardedFocusMilestoneBonus)
    }

    // 60th active second completes minute 1
    val atOneMinute = com.example.ui.DailyScreenTimeManager.advanceOneActiveSecond(prefs, day)
    assertEquals(60, atOneMinute.todaySeconds)
    assertEquals(1, atOneMinute.todayMinutes)
    assertEquals(1, atOneMinute.totalMinutes)
    assertTrue(atOneMinute.completedNewMinute)
    assertEquals("01:00", com.example.ui.DailyScreenTimeManager.formatDurationMmSs(atOneMinute.todaySeconds))

    // Advance to 15 minutes (900 seconds) -> 50% of 30-min daily goal & focus milestone bonus awarded
    var milestoneAt15 = false
    var resultAt15 = atOneMinute
    for (s in 61..900) {
      resultAt15 = com.example.ui.DailyScreenTimeManager.advanceOneActiveSecond(prefs, day)
      if (resultAt15.awardedFocusMilestoneBonus) milestoneAt15 = true
    }
    assertTrue(milestoneAt15)
    assertEquals(900, resultAt15.todaySeconds)
    assertEquals(15, resultAt15.todayMinutes)
    assertEquals(50, resultAt15.goalPercent)
    assertEquals(0.5f, resultAt15.goalProgressFraction, 0.001f)

    // Verify automatic DailyStudyGoalState calculation at 30 min (1800s) and beyond
    val goalReached = com.example.ui.DailyScreenTimeManager.buildGoalState(
      todaySeconds = 1800,
      totalMinutes = 30,
      isTimerRunning = true
    )
    assertTrue(goalReached.isGoalReached)
    assertEquals(100, goalReached.progressPercent)
    assertEquals(1.0f, goalReached.progressFraction, 0.001f)
    assertEquals(0, goalReached.remainingSecondsToday)
    assertEquals("30:00", goalReached.formattedActiveTime)
  }

  @Test
  fun dailyScreenTimeManager_detectsIdleTimeoutAndBackgroundState() {
    val start = 1_000_000L
    // Active right after interaction in foreground
    assertTrue(
      com.example.ui.DailyScreenTimeManager.isUserActive(
        lastActivityMillis = start,
        nowMillis = start + 1_000L,
        isInForeground = true
      )
    )
    // Still active just before idle timeout
    assertTrue(
      com.example.ui.DailyScreenTimeManager.isUserActive(
        lastActivityMillis = start,
        nowMillis = start + com.example.ui.DailyScreenTimeManager.IDLE_TIMEOUT_MS - 1L,
        isInForeground = true
      )
    )
    // Pauses automatically once idle timeout elapses
    assertFalse(
      com.example.ui.DailyScreenTimeManager.isUserActive(
        lastActivityMillis = start,
        nowMillis = start + com.example.ui.DailyScreenTimeManager.IDLE_TIMEOUT_MS,
        isInForeground = true
      )
    )
    // Pauses immediately when app goes to background even with fresh activity
    assertFalse(
      com.example.ui.DailyScreenTimeManager.isUserActive(
        lastActivityMillis = start,
        nowMillis = start + 500L,
        isInForeground = false
      )
    )
  }

  @Test
  fun settingsModels_mapFontScalePresetsAndProvideSensibleDefaults() {
    assertEquals(
      com.example.model.FontScalePreset.COMPACT,
      com.example.model.FontScalePreset.fromScale(0.90f)
    )
    assertEquals(
      com.example.model.FontScalePreset.STANDARD,
      com.example.model.FontScalePreset.fromScale(1.00f)
    )
    assertEquals(
      com.example.model.FontScalePreset.LARGE,
      com.example.model.FontScalePreset.fromScale(1.15f)
    )
    assertEquals(
      com.example.model.FontScalePreset.EXTRA_LARGE,
      com.example.model.FontScalePreset.fromScale(1.30f)
    )
    assertEquals(
      com.example.model.FontScalePreset.STANDARD,
      com.example.model.FontScalePreset.fromScale(1.01f)
    )

    val defaultAcc = com.example.model.AccessibilitySettings()
    assertEquals(com.example.model.FontScalePreset.STANDARD, defaultAcc.fontScalePreset)
    assertFalse(defaultAcc.highContrastBorders)
    assertFalse(defaultAcc.reduceMotion)
    assertTrue(defaultAcc.hapticFeedbackEnabled)

    val customAcc = defaultAcc.copy(
      fontScalePreset = com.example.model.FontScalePreset.LARGE,
      highContrastBorders = true,
      reduceMotion = true,
      hapticFeedbackEnabled = false
    )
    assertEquals(1.15f, customAcc.fontScalePreset.scale, 0.001f)
    assertTrue(customAcc.highContrastBorders)
    assertTrue(customAcc.reduceMotion)
    assertFalse(customAcc.hapticFeedbackEnabled)

    val defaultGeneral = com.example.model.AppGeneralSettings()
    assertEquals(2, defaultGeneral.idlePauseMinutes)
    assertTrue(defaultGeneral.dailyRemindersEnabled)
    assertTrue(defaultGeneral.showCelebrationDialogs)
    assertTrue(defaultGeneral.showGentleRetryTips)
    assertTrue(defaultGeneral.showXpToastBanners)

    val note = com.example.model.UserFeedbackNote(
      id = "fb_1",
      category = "Sugestão",
      rating = 5,
      message = "Adorei o menu de acessibilidade!",
      createdAtIso = "2026-11-13"
    )
    assertEquals("fb_1", note.id)
    assertEquals(5, note.rating)
    assertEquals("Sugestão", note.category)
    assertEquals("2026-11-13", note.createdAtIso)
  }

  @Test
  fun avatarImageCodec_identifiesShareableUrlsAndRoundTripsDataUri() {
    assertTrue(com.example.util.AvatarImageCodec.isShareableAvatarUrl("https://example.com/photo.jpg"))
    assertTrue(com.example.util.AvatarImageCodec.isShareableAvatarUrl("preset:cyber"))
    assertTrue(com.example.util.AvatarImageCodec.isShareableAvatarUrl("data:image/jpeg;base64,/9j/4AAQ"))
    assertFalse(com.example.util.AvatarImageCodec.isShareableAvatarUrl("file:///data/user/0/app/avatar.jpg"))
    assertFalse(com.example.util.AvatarImageCodec.isShareableAvatarUrl("content://media/picker/0/1"))
    assertFalse(com.example.util.AvatarImageCodec.isShareableAvatarUrl(""))

    assertTrue(com.example.util.AvatarImageCodec.isLocalFileOrContentUri("file:///data/user/0/avatar.jpg"))
    assertTrue(com.example.util.AvatarImageCodec.isLocalFileOrContentUri("content://media/external/images/1"))
    assertFalse(com.example.util.AvatarImageCodec.isLocalFileOrContentUri("data:image/jpeg;base64,/9j/4AAQ"))
    assertFalse(com.example.util.AvatarImageCodec.isLocalFileOrContentUri("preset:python"))

    val sampleBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0x10, 0x20, 0x30, 0xFF.toByte(), 0xD9.toByte())
    val dataUrl = com.example.util.AvatarImageCodec.encodeJpegBytesToDataUrl(sampleBytes)
    assertTrue(dataUrl.startsWith(com.example.util.AvatarImageCodec.JPEG_DATA_URI_PREFIX))
    assertTrue(com.example.util.AvatarImageCodec.isShareableAvatarUrl(dataUrl))

    val decoded = com.example.util.AvatarImageCodec.decodeDataUrlToBytes(dataUrl)
    assertNotNull(decoded)
    assertArrayEquals(sampleBytes, decoded)
    assertNull(com.example.util.AvatarImageCodec.decodeDataUrlToBytes("https://example.com/a.jpg"))
  }

  @Test
  fun developerLevelCatalog_has12SteepTiersAndResolvesBoundsCorrectly() {
    val tiers = com.example.model.DeveloperLevelCatalog.tiers
    assertEquals(12, tiers.size)

    // Verify strictly slower, steeper XP curve (each gap increases or remains challenging)
    val expectedMinXps = listOf(0, 250, 650, 1250, 2100, 3300, 4900, 7000, 9800, 13500, 18500, 25000)
    assertEquals(expectedMinXps, tiers.map { it.minXp })
    for (i in 1 until tiers.size) {
      assertTrue(tiers[i].minXp > tiers[i - 1].minXp)
      assertEquals(tiers[i].minXp, tiers[i - 1].nextLevelXp)
      assertTrue(tiers[i].titlePt.isNotBlank())
      assertTrue(tiers[i].titleEn.isNotBlank())
      assertTrue(tiers[i].rankBadgePt.isNotBlank())
      assertTrue(tiers[i].rankBadgeEn.isNotBlank())
    }

    // Boundary checks
    val lv1Zero = com.example.model.DeveloperLevelCatalog.calculateScoreInfo(0)
    assertEquals(1, lv1Zero.levelNumber)
    assertEquals("Iniciante", lv1Zero.rankBadgePt)
    assertEquals(250, lv1Zero.nextLevelTarget)
    assertEquals(0f, lv1Zero.progressFraction, 0.001f)
    assertFalse(lv1Zero.isMaxLevel)

    val lv1JustBefore = com.example.model.DeveloperLevelCatalog.calculateScoreInfo(249)
    assertEquals(1, lv1JustBefore.levelNumber)
    assertEquals(249f / 250f, lv1JustBefore.progressFraction, 0.001f)

    val lv2Exact = com.example.model.DeveloperLevelCatalog.calculateScoreInfo(250)
    assertEquals(2, lv2Exact.levelNumber)
    assertEquals("Bronze I", lv2Exact.rankBadgePt)
    assertEquals(650, lv2Exact.nextLevelTarget)
    assertEquals(0f, lv2Exact.progressFraction, 0.001f)

    val lv5Mid = com.example.model.DeveloperLevelCatalog.calculateScoreInfo(2700)
    assertEquals(5, lv5Mid.levelNumber)
    assertEquals("Dev Pleno", lv5Mid.levelTitlePt)
    assertEquals("Prata II", lv5Mid.rankBadgePt)
    assertEquals(0.5f, lv5Mid.progressFraction, 0.001f)

    val lv12Max = com.example.model.DeveloperLevelCatalog.calculateScoreInfo(25000)
    assertEquals(12, lv12Max.levelNumber)
    assertEquals("Grão-Mestre", lv12Max.rankBadgePt)
    assertEquals("Grandmaster", lv12Max.rankBadgeEn)
    assertTrue(lv12Max.isMaxLevel)
    assertEquals(1f, lv12Max.progressFraction, 0.001f)

    val aboveMax = com.example.model.DeveloperLevelCatalog.calculateScoreInfo(50000)
    assertEquals(12, aboveMax.levelNumber)
    assertTrue(aboveMax.isMaxLevel)
    assertEquals(1f, aboveMax.progressFraction, 0.001f)
  }

  @Test
  fun dailyLivesManager_resets25LivesImmediatelyOnLevelUp() {
    val prefs = InMemorySharedPreferences()
    val today = "2026-11-13"

    // Consume all 25 lives for today
    com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, today)
    repeat(25) {
      com.example.ui.DailyLivesManager.consumeOneLife(prefs, today)
    }
    val depleted = com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, today)
    assertEquals(0, depleted.remaining)
    assertFalse(com.example.ui.DailyLivesManager.consumeOneLife(prefs, today).first)

    // Leveling up resets remaining lives back to 25 immediately on the same day
    val restored = com.example.ui.DailyLivesManager.resetLivesForLevelUp(prefs, today)
    assertEquals(25, restored.remaining)
    assertEquals(25, restored.maxDaily)
    assertEquals(25, com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, today).remaining)
    assertTrue(com.example.ui.DailyLivesManager.consumeOneLife(prefs, today).first)
  }

  private class InMemorySharedPreferences : android.content.SharedPreferences {
    private val data = mutableMapOf<String, Any?>()
    override fun getAll(): MutableMap<String, *> = data.toMutableMap()
    override fun getString(key: String?, defValue: String?): String? = data[key] as? String ?: defValue
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
      @Suppress("UNCHECKED_CAST") (data[key] as? MutableSet<String> ?: defValues)
    override fun getInt(key: String?, defValue: Int): Int = data[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = data[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = data[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = data[key] as? Boolean ?: defValue
    override fun contains(key: String?): Boolean = data.containsKey(key)
    override fun edit(): android.content.SharedPreferences.Editor = object : android.content.SharedPreferences.Editor {
      private val pending = mutableMapOf<String, Any?>()
      private val removals = mutableSetOf<String>()
      private var clearAll = false
      override fun putString(key: String?, value: String?) = apply { if (key != null) pending[key] = value }
      override fun putStringSet(key: String?, values: MutableSet<String>?) = apply { if (key != null) pending[key] = values }
      override fun putInt(key: String?, value: Int) = apply { if (key != null) pending[key] = value }
      override fun putLong(key: String?, value: Long) = apply { if (key != null) pending[key] = value }
      override fun putFloat(key: String?, value: Float) = apply { if (key != null) pending[key] = value }
      override fun putBoolean(key: String?, value: Boolean) = apply { if (key != null) pending[key] = value }
      override fun remove(key: String?) = apply { if (key != null) removals.add(key) }
      override fun clear() = apply { clearAll = true }
      override fun commit(): Boolean { apply(); return true }
      override fun apply() {
        if (clearAll) data.clear()
        removals.forEach { data.remove(it) }
        data.putAll(pending)
      }
    }
    override fun registerOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener?) {}
  }

  private class InMemoryUserBadgeDao : com.example.data.local.UserBadgeDao {
    private val map = linkedMapOf<String, com.example.data.local.UserBadgeEntity>()
    private val flow = kotlinx.coroutines.flow.MutableStateFlow<List<com.example.data.local.UserBadgeEntity>>(emptyList())

    override fun getAllBadges() = flow
    override fun getUnlockedBadges() = kotlinx.coroutines.flow.flowOf(map.values.filter { it.isUnlocked })
    override fun getUnlockedCount() = kotlinx.coroutines.flow.flowOf(map.values.count { it.isUnlocked })
    override fun getTotalCount() = kotlinx.coroutines.flow.flowOf(map.size)

    override suspend fun insertOrUpdateBadges(badges: List<com.example.data.local.UserBadgeEntity>) {
      badges.forEach { map[it.id] = it }
      flow.value = map.values.toList()
    }

    override suspend fun insertInitialBadges(badges: List<com.example.data.local.UserBadgeEntity>) {
      badges.forEach { if (!map.containsKey(it.id)) map[it.id] = it }
      flow.value = map.values.toList()
    }

    override suspend fun getBadgeById(id: String) = map[id]

    override suspend fun unlockBadge(id: String, timestamp: Long) {
      val current = map[id] ?: return
      if (!current.isUnlocked) {
        map[id] = current.copy(isUnlocked = true, unlockedAtMillis = timestamp, currentProgress = current.maxProgress)
        flow.value = map.values.toList()
      }
    }

    override suspend fun updateProgress(id: String, progress: Int, timestamp: Long) {
      val current = map[id] ?: return
      val willUnlock = progress >= current.maxProgress
      map[id] = current.copy(
        currentProgress = progress,
        isUnlocked = current.isUnlocked || willUnlock,
        unlockedAtMillis = if (willUnlock && current.unlockedAtMillis == null) timestamp else current.unlockedAtMillis
      )
      flow.value = map.values.toList()
    }
  }
}
