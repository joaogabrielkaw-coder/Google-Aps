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

  @Test
  fun achievementsSystem_unlocksPythonBeginnerAndTenHoursOfSqlAndFormatsHours() = kotlinx.coroutines.test.runTest {
    val fakeDao = InMemoryUserBadgeDao()
    val repo = com.example.data.repository.UserBadgeRepository(fakeDao)
    repo.initializeBadgesIfEmpty()

    val emptyScore = com.example.model.UserScoreInfo()

    // Partial progress: 20 Python XP & 135 SQL minutes (2h 15m across SQL/MySQL/Postgres)
    val partialUnlocked = repo.evaluateAndSyncBadges(
      streakDays = 0,
      completedSessions = emptyList(),
      scoreInfo = emptyScore,
      techScores = mapOf(Technology.PYTHON to 20),
      hasAiRoutine = false,
      techMinutes = mapOf(
        Technology.SQL to 60,
        Technology.MYSQL to 45,
        Technology.POSTGRESQL to 30
      )
    )
    assertFalse(fakeDao.getBadgeById("badge_python_beginner")?.isUnlocked ?: true)
    assertEquals(20, fakeDao.getBadgeById("badge_python_beginner")?.currentProgress)

    val sql1h = fakeDao.getBadgeById("badge_sql_hours_1")!!
    val sql10h = fakeDao.getBadgeById("badge_sql_hours_10")!!
    assertTrue(sql1h.isUnlocked)
    assertTrue(partialUnlocked.any { it.id == "badge_sql_hours_1" })
    assertFalse(sql10h.isUnlocked)
    assertEquals(135, sql10h.currentProgress)
    assertEquals("2h 15m / 10h", com.example.ui.components.formatBadgeProgressText(sql10h, com.example.model.Language.EN))
    assertEquals("45m", com.example.ui.components.formatMinutesAsHoursText(45))

    // Reach 30 Python XP and 600 combined SQL minutes -> Python Beginner & 10 Hours of SQL unlock
    val unlockedNow = repo.evaluateAndSyncBadges(
      streakDays = 0,
      completedSessions = emptyList(),
      scoreInfo = emptyScore,
      techScores = mapOf(Technology.PYTHON to 30),
      hasAiRoutine = false,
      techMinutes = mapOf(
        Technology.SQL to 300,
        Technology.MYSQL to 200,
        Technology.POSTGRESQL to 100
      )
    )
    val newlyIds = unlockedNow.map { it.id }.toSet()
    assertTrue(newlyIds.contains("badge_python_beginner"))
    assertTrue(newlyIds.contains("badge_sql_hours_10"))
    assertTrue(fakeDao.getBadgeById("badge_python_beginner")?.isUnlocked == true)
    assertTrue(fakeDao.getBadgeById("badge_sql_hours_10")?.isUnlocked == true)
    assertEquals(600, fakeDao.getBadgeById("badge_sql_hours_10")?.currentProgress)
    assertTrue(repo.getUnlockedBadgeIds().containsAll(listOf("badge_python_beginner", "badge_sql_hours_1", "badge_sql_hours_10")))

    // Restore cloud badges into a fresh repository
    val freshDao = InMemoryUserBadgeDao()
    val freshRepo = com.example.data.repository.UserBadgeRepository(freshDao)
    freshRepo.restoreUnlockedBadgeIdsFromCloud(setOf("badge_sql_hours_10", "badge_python_beginner"))
    assertTrue(freshDao.getBadgeById("badge_sql_hours_10")?.isUnlocked == true)
    assertTrue(freshDao.getBadgeById("badge_python_beginner")?.isUnlocked == true)
    assertFalse(freshDao.getBadgeById("badge_kotlin_hours_5")?.isUnlocked ?: true)
  }

  @Test
  fun chatModels_andMediaCodec_workDeterministically() {
    // Deterministic conversation IDs & sorted participants
    val idAb = com.example.model.ChatIdUtil.deterministicPairId("user_alpha", "user_beta")
    val idBa = com.example.model.ChatIdUtil.deterministicPairId("user_beta", "user_alpha")
    assertEquals(idAb, idBa)
    assertEquals(listOf("user_alpha", "user_beta"), com.example.model.ChatIdUtil.sortedParticipantIds("user_beta", "user_alpha"))

    // ChatRequestItem peer helpers
    val req = com.example.model.ChatRequestItem(
      requestId = idAb,
      chatId = idAb,
      senderId = "user_alpha",
      senderName = "Alice",
      senderPhotoUrl = "https://example.com/a.jpg",
      recipientId = "user_beta",
      recipientName = "Bob",
      recipientPhotoUrl = "https://example.com/b.jpg",
      status = com.example.model.ChatRequestStatus.PENDING
    )
    assertFalse(req.isIncomingFor("user_alpha"))
    assertTrue(req.isIncomingFor("user_beta"))
    assertEquals("user_beta", req.peerIdFor("user_alpha"))
    assertEquals("Bob", req.peerNameFor("user_alpha"))
    assertEquals("https://example.com/a.jpg", req.peerPhotoFor("user_beta"))

    // ActiveCallSession peer resolution
    val call = com.example.model.ActiveCallSession(
      callId = "call_1",
      chatId = idAb,
      callerId = "user_alpha",
      callerName = "Alice",
      calleeId = "user_beta",
      calleeName = "Bob",
      callType = com.example.model.ChatCallType.VIDEO,
      status = com.example.model.ChatCallStatus.RINGING
    )
    assertEquals("user_beta", call.peerId("user_alpha"))
    assertEquals("Bob", call.peerName("user_alpha"))
    assertEquals("user_alpha", call.peerId("user_beta"))
    assertEquals("Alice", call.peerName("user_beta"))

    // ChatMediaCodec Base64 chunk splitting & byte formatting
    assertTrue(com.example.util.ChatMediaCodec.splitBase64IntoChunks("", 100).isEmpty())
    val payload = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val chunks = com.example.util.ChatMediaCodec.splitBase64IntoChunks(payload, chunkSize = 10)
    assertEquals(4, chunks.size)
    assertEquals(payload, chunks.joinToString(""))

    assertEquals("512 B", com.example.util.ChatMediaCodec.formatByteSize(512L))
    assertEquals("4 KB", com.example.util.ChatMediaCodec.formatByteSize(4096L))
    assertTrue(com.example.util.ChatMediaCodec.formatByteSize(2_097_152L).contains("2"))
  }

  @Test
  fun liveStudyRoomModels_parseModesStatusesAndParticipantsCorrectly() {
    assertEquals(com.example.model.LiveRoomMode.AUDIO, com.example.model.LiveRoomMode.fromRaw("audio"))
    assertEquals(com.example.model.LiveRoomMode.VIDEO, com.example.model.LiveRoomMode.fromRaw("VIDEO"))
    assertEquals(com.example.model.LiveRoomMode.VIDEO, com.example.model.LiveRoomMode.fromRaw("unknown"))

    assertEquals(com.example.model.LiveRoomStatus.LIVE, com.example.model.LiveRoomStatus.fromRaw("live"))
    assertEquals(com.example.model.LiveRoomStatus.ENDED, com.example.model.LiveRoomStatus.fromRaw("ended"))
    assertEquals(com.example.model.LiveRoomStatus.ENDED, com.example.model.LiveRoomStatus.fromRaw("invalid"))

    assertEquals(com.example.model.LiveMessageType.HAND_RAISE, com.example.model.LiveMessageType.fromRaw("hand_raise"))
    assertEquals(com.example.model.LiveMessageType.SYSTEM, com.example.model.LiveMessageType.fromRaw("system"))
    assertEquals(com.example.model.LiveMessageType.TEXT, com.example.model.LiveMessageType.fromRaw("other"))

    val room = com.example.model.LiveStudyRoom(
      roomId = "live_sql_01",
      title = "SQL JOINs na Prática",
      topicTechId = "sql",
      topicLabel = "SQL",
      hostId = "host_1",
      hostName = "Maria",
      participantIds = listOf("host_1", "user_2", "user_3"),
      participantNames = mapOf("host_1" to "Maria", "user_2" to "João", "user_3" to "Ana")
    )
    assertEquals(3, room.participantCount)
    assertEquals(com.example.model.LiveRoomMode.VIDEO, room.liveMode)
    assertEquals(com.example.model.LiveRoomStatus.LIVE, room.status)

    val liveMsg = com.example.model.LiveRoomMessage(
      messageId = "msg_1",
      roomId = room.roomId,
      senderId = "user_2",
      senderName = "João",
      text = "Qual a diferença entre LEFT JOIN e INNER JOIN?"
    )
    assertEquals(com.example.model.LiveMessageType.TEXT, liveMsg.messageType)
  }

  @Test
  fun directorySearchHelper_listsAllUsersAndRanksMatchesByTypedOrder() {
    val users = listOf(
      com.example.model.ChatDirectoryUser(userId = "self", displayName = "Self", displayNameLower = "self", levelNumber = 5, totalScore = 1000),
      com.example.model.ChatDirectoryUser(userId = "u1", displayName = "Pedro João", displayNameLower = "pedro joao", levelNumber = 3, levelTitle = "Dev Jr", totalScore = 500),
      com.example.model.ChatDirectoryUser(userId = "u2", displayName = "João Silva", displayNameLower = "joao silva", levelNumber = 6, levelTitle = "Tech Lead", totalScore = 3000),
      com.example.model.ChatDirectoryUser(userId = "u3", displayName = "Ana Carolina", displayNameLower = "ana carolina", levelNumber = 8, levelTitle = "Arquiteta", totalScore = 5000),
      com.example.model.ChatDirectoryUser(userId = "u4", displayName = "João", displayNameLower = "joao", levelNumber = 2, levelTitle = "Iniciante", totalScore = 200),
      com.example.model.ChatDirectoryUser(userId = "u5", displayName = "Mariana Joana", displayNameLower = "mariana joana", levelNumber = 4, levelTitle = "Pleno", totalScore = 1200)
    )

    // Blank query lists all other users alphabetically (excluding self)
    val allAlpha = com.example.model.ChatDirectorySearchHelper.filterAndSortUsers(
      users = users,
      query = "",
      currentUserId = "self",
      sortMode = com.example.model.DirectorySortMode.ALPHABETICAL
    )
    assertEquals(listOf("Ana Carolina", "João", "João Silva", "Mariana Joana", "Pedro João"), allAlpha.map { it.displayName })

    // Typing "joao" (accent-insensitive) orders: exact ("João") -> starts with ("João Silva") -> word prefix ("Pedro João")
    val rankedJoao = com.example.model.ChatDirectorySearchHelper.filterAndSortUsers(
      users = users,
      query = "joao",
      currentUserId = "self"
    )
    assertEquals(listOf("João", "João Silva", "Pedro João"), rankedJoao.map { it.displayName })

    // Typing "joa" matches both "João..." and "Mariana Joana", ranked by starts-with then word-prefix alphabetically
    val rankedJoa = com.example.model.ChatDirectorySearchHelper.filterAndSortUsers(
      users = users,
      query = "joa",
      currentUserId = "self"
    )
    assertEquals(listOf("João", "João Silva", "Mariana Joana", "Pedro João"), rankedJoa.map { it.displayName })

    // Filter CONNECTED vs AVAILABLE
    val connectedOnly = com.example.model.ChatDirectorySearchHelper.filterAndSortUsers(
      users = users,
      query = "",
      currentUserId = "self",
      connectedUserIds = setOf("u2"),
      filterMode = com.example.model.DirectoryFilterMode.CONNECTED
    )
    assertEquals(listOf("João Silva"), connectedOnly.map { it.displayName })

    // Match range highlights accented substring cleanly
    val range = com.example.model.ChatDirectorySearchHelper.findMatchRange("João Silva", "joao")
    assertEquals(0..3, range)
  }

  @Test
  fun techCourseModulesRepository_providesVerifiedFreeVideosAndModulesForAll12Courses() {
    val allTechs = Technology.entries
    assertEquals(12, allTechs.size)

    val globalLessonIds = mutableSetOf<String>()

    allTechs.forEach { tech ->
      val curriculum = com.example.data.repository.TechCourseModulesRepository.getCurriculum(tech)
      assertEquals(tech, curriculum.tech)
      assertTrue("Modules missing for ${tech.id}", curriculum.modules.size >= 2)
      assertTrue("Free references missing for ${tech.id}", curriculum.references.size >= 3)

      val hasYouTubeRef = curriculum.references.any {
        (it.type == com.example.model.CourseResourceType.YOUTUBE_PLAYLIST ||
          it.type == com.example.model.CourseResourceType.YOUTUBE_VIDEO) &&
          it.url.startsWith("https://www.youtube.com/")
      }
      assertTrue("YouTube course reference missing for ${tech.id}", hasYouTubeRef)

      curriculum.references.forEach { ref ->
        assertTrue("Invalid URL in ${ref.id}", ref.url.startsWith("https://"))
        assertTrue(ref.titlePt.isNotBlank())
        assertTrue(ref.titleEn.isNotBlank())
        assertTrue(ref.provider.isNotBlank())
      }

      curriculum.modules.forEach { mod ->
        assertTrue("Module ${mod.id} should have >= 2 lessons", mod.lessons.size >= 2)
        assertTrue("Invalid module video URL in ${mod.id}", mod.primaryVideoUrl.startsWith("https://"))
        mod.lessons.forEach { lesson ->
          assertTrue("Duplicate lesson id ${lesson.id}", globalLessonIds.add(lesson.id))
          assertTrue(lesson.durationMinutes > 0)
          assertTrue(lesson.videoRefUrl.startsWith("https://"))
          assertTrue(lesson.topicsPt.isNotEmpty())
          assertTrue(lesson.topicsEn.isNotEmpty())
        }
      }
    }

    // Verify course progress helper accurately calculates completion percentage & minutes
    val pythonLessons = com.example.data.repository.TechCourseModulesRepository.getAllLessonIds(Technology.PYTHON)
    val initialProgress = com.example.data.repository.TechCourseModulesRepository.getCourseProgress(
      Technology.PYTHON,
      emptySet()
    )
    assertEquals(0, initialProgress.completedLessons)
    assertEquals(0, initialProgress.progressPercent)
    assertEquals(pythonLessons.size, initialProgress.totalLessons)

    val firstTwo = pythonLessons.take(2).toSet()
    val partialProgress = com.example.data.repository.TechCourseModulesRepository.getCourseProgress(
      Technology.PYTHON,
      firstTwo
    )
    assertEquals(2, partialProgress.completedLessons)
    assertTrue(partialProgress.completedMinutes > 0)
    assertTrue(partialProgress.progressPercent in 1..99)

    val fullProgress = com.example.data.repository.TechCourseModulesRepository.getCourseProgress(
      Technology.PYTHON,
      pythonLessons
    )
    assertEquals(100, fullProgress.progressPercent)
    assertEquals(0, fullProgress.remainingLessons)

    // Verify Curso em Vídeo portal link is included across all courses
    allTechs.forEach { tech ->
      val refs = com.example.data.repository.TechCourseModulesRepository.getReferences(tech)
      assertTrue(
        "Curso em Vídeo portal missing for ${tech.id}",
        refs.any { it.url == "https://www.cursoemvideo.com/cursos/" }
      )
    }
  }

  @Test
  fun courseChecklistManager_persistsCompletionWithoutAwardingAnyXpPoints() {
    val prefs = InMemorySharedPreferences()
    assertTrue(com.example.ui.CourseChecklistManager.loadCompletedIds(prefs).isEmpty())

    val afterCheck = com.example.ui.CourseChecklistManager.toggleLessonCompleted(
      prefs = prefs,
      currentLessons = emptySet(),
      lessonId = "py_m1_l1"
    )
    assertEquals(setOf("py_m1_l1"), afterCheck)
    assertEquals(setOf("py_m1_l1"), com.example.ui.CourseChecklistManager.loadCompletedIds(prefs))

    // Marking video course lessons must NEVER award XP or touch score prefs
    assertEquals(0, prefs.getInt("user_total_score", 0))
    assertEquals(0, prefs.getInt("score_tech_python", 0))

    val afterUncheck = com.example.ui.CourseChecklistManager.toggleLessonCompleted(
      prefs = prefs,
      currentLessons = afterCheck,
      lessonId = "py_m1_l1"
    )
    assertTrue(afterUncheck.isEmpty())
    assertEquals(0, prefs.getInt("user_total_score", 0))
  }

  @Test
  fun targetSubjectsCatalog_andPreferenceSerialization_validateNormalizeAndMapPayloads() {
    // Normalization removes duplicates, normalizes case, filters unknown IDs, preserves catalog order
    val normalized = com.example.model.TargetSubjectsCatalog.normalizeSubjectIds(
      listOf(" SQL ", "python", "PYTHON", "cobol", "html", "invalid_lang")
    )
    assertEquals(listOf("python", "sql", "html"), normalized)

    // Bounds enforcement for daily goal & note length
    assertEquals(10, com.example.model.TargetSubjectsCatalog.normalizeDailyGoalMinutes(2))
    assertEquals(240, com.example.model.TargetSubjectsCatalog.normalizeDailyGoalMinutes(500))
    assertEquals(45, com.example.model.TargetSubjectsCatalog.normalizeDailyGoalMinutes(45))

    val longNote = "x".repeat(250)
    assertEquals(200, com.example.model.TargetSubjectsCatalog.normalizeGoalNote("  $longNote  ").length)

    // Preset track inference
    assertEquals(
      com.example.model.LearningTrackOption.BACKEND_DATA,
      com.example.model.TargetSubjectsCatalog.inferMatchingTrack(listOf("sql", "python", "go", "postgresql"))
    )
    assertEquals(
      com.example.model.LearningTrackOption.CUSTOM,
      com.example.model.TargetSubjectsCatalog.inferMatchingTrack(listOf("python", "html"))
    )

    // Create map payload: never includes @DocumentId ("id"), includes userId & timestamps, omits blank note
    val createPref = com.example.model.TargetSubjectsPreference(
      id = "target_subjects",
      userId = "ignored_uid",
      subjectIds = listOf("HTML", "python", "sql", "cobol"),
      dailyGoalMinutes = 5,
      learningTrack = "balanced",
      studyGoalNote = "   "
    )
    val createMap = createPref.toCreateMap("owner_uid_123")
    assertFalse(createMap.containsKey("id"))
    assertFalse(createMap.containsKey("studyGoalNote"))
    assertEquals("owner_uid_123", createMap["userId"])
    assertEquals(listOf("python", "sql", "html"), createMap["subjectIds"])
    assertEquals(10, createMap["dailyGoalMinutes"])
    assertEquals("balanced", createMap["learningTrack"])
    assertTrue(createMap.containsKey("createdAt"))
    assertTrue(createMap.containsKey("updatedAt"))

    // Update map payload: omits immutable userId & createdAt and @DocumentId id
    val updatePref = createPref.copy(studyGoalNote = "Focus on backend APIs")
    val updateMap = updatePref.toUpdateMap()
    assertFalse(updateMap.containsKey("id"))
    assertFalse(updateMap.containsKey("userId"))
    assertFalse(updateMap.containsKey("createdAt"))
    assertEquals("Focus on backend APIs", updateMap["studyGoalNote"])
    assertTrue(updateMap.containsKey("updatedAt"))
  }

  @Test
  fun lifeShopCatalog_validatesDepletedQuotaAndCurrencyBalances() {
    assertEquals(4, com.example.model.LifeShopCatalog.packages.size)
    val quickPack = com.example.model.LifeShopCatalog.packages.first { it.id == "lives_quick_5" }
    val studyPack = com.example.model.LifeShopCatalog.packages.first { it.id == "lives_study_12" }
    val fullPack = com.example.model.LifeShopCatalog.packages.first { it.id == "lives_full_25" }
    val crystalPack = com.example.model.LifeShopCatalog.packages.first {
      it.currency == com.example.model.LifePurchaseCurrency.CRYSTALS
    }

    // Verify harder XP costs and minimum 50 diamonds requirement
    assertEquals(150, quickPack.costAmount)
    assertEquals(350, studyPack.costAmount)
    assertEquals(650, fullPack.costAmount)
    assertEquals(50, crystalPack.costAmount)

    // Rejects purchase if user still has daily lives left
    val quotaRejection = com.example.model.LifeShopCatalog.evaluatePurchaseEligibility(
      remainingLives = 3,
      pkg = quickPack,
      availableXp = 1000,
      availableCrystals = 100
    )
    assertTrue(quotaRejection is com.example.model.LifePurchaseResult.QuotaStillAvailable)
    assertEquals(3, (quotaRejection as com.example.model.LifePurchaseResult.QuotaStillAvailable).remainingLives)

    // Rejects XP pack when spendable XP is below harder threshold (150 XP)
    val xpShort = com.example.model.LifeShopCatalog.evaluatePurchaseEligibility(
      remainingLives = 0,
      pkg = quickPack,
      availableXp = 149,
      availableCrystals = 10
    )
    assertTrue(xpShort is com.example.model.LifePurchaseResult.InsufficientFunds)
    val xpShortResult = xpShort as com.example.model.LifePurchaseResult.InsufficientFunds
    assertEquals(150, xpShortResult.required)
    assertEquals(149, xpShortResult.available)
    assertEquals(com.example.model.LifePurchaseCurrency.XP, xpShortResult.currency)

    // Rejects Diamond pack when 3D game diamonds are below minimum 50
    val crystalShort = com.example.model.LifeShopCatalog.evaluatePurchaseEligibility(
      remainingLives = 0,
      pkg = crystalPack,
      availableXp = 500,
      availableCrystals = 49
    )
    assertTrue(crystalShort is com.example.model.LifePurchaseResult.InsufficientFunds)
    val crystalShortResult = crystalShort as com.example.model.LifePurchaseResult.InsufficientFunds
    assertEquals(50, crystalShortResult.required)
    assertEquals(49, crystalShortResult.available)
    assertEquals(com.example.model.LifePurchaseCurrency.CRYSTALS, crystalShortResult.currency)

    // Eligible when remainingLives == 0 and funds meet harder thresholds (150 XP / 50 diamonds)
    assertNull(
      com.example.model.LifeShopCatalog.evaluatePurchaseEligibility(
        remainingLives = 0,
        pkg = quickPack,
        availableXp = 150,
        availableCrystals = 0
      )
    )
    assertNull(
      com.example.model.LifeShopCatalog.evaluatePurchaseEligibility(
        remainingLives = 0,
        pkg = crystalPack,
        availableXp = 0,
        availableCrystals = 50
      )
    )
  }

  @Test
  fun lifeShopCatalog_providesBrlExtraLifePackages_requiringRealBilling() {
    val brl = com.example.model.LifeShopCatalog.brlPackages
    assertEquals(3, brl.size)

    val p10 = brl.first { it.livesAmount == 10 }
    val p20 = brl.first { it.livesAmount == 20 }
    val p50 = brl.first { it.livesAmount == 50 }

    assertEquals(5, p10.costAmount)
    assertEquals("R$ 5,00", p10.formattedPrice(com.example.model.Language.PT))
    assertEquals("lives_brl_10", p10.playProductId)
    assertEquals(10, p20.costAmount)
    assertEquals("R$ 10,00", p20.formattedPrice(com.example.model.Language.PT))
    assertEquals("lives_brl_20", p20.playProductId)
    assertEquals(35, p50.costAmount)
    assertEquals("R$ 35,00", p50.formattedPrice(com.example.model.Language.PT))
    assertEquals("lives_brl_50", p50.playProductId)

    // Live Play Store price overrides fallback label when fetched
    val livePrices = mapOf("lives_brl_10" to "R$ 5,90")
    assertEquals("R$ 5,90", p10.formattedPrice(com.example.model.Language.PT, livePrices))

    // BRL extra life packages ALWAYS require real Google Play Billing regardless of daily lives
    brl.forEach { pkg ->
      assertEquals(com.example.model.LifePurchaseCurrency.BRL, pkg.currency)
      val res = com.example.model.LifeShopCatalog.evaluatePurchaseEligibility(
        remainingLives = 25,
        pkg = pkg,
        availableXp = 0,
        availableCrystals = 0
      )
      assertTrue(res is com.example.model.LifePurchaseResult.RequiresRealBilling)
    }
  }

  @Test
  fun playBillingTokenRegistry_preventsDuplicateCreditingForSamePurchaseToken() {
    val prefs = InMemorySharedPreferences()
    val token = "gp_token_abc_123"
    assertFalse(com.example.billing.PlayBillingTokenRegistry.hasProcessedToken(prefs, token))
    com.example.billing.PlayBillingTokenRegistry.markTokenProcessed(prefs, token)
    assertTrue(com.example.billing.PlayBillingTokenRegistry.hasProcessedToken(prefs, token))
    assertFalse(com.example.billing.PlayBillingTokenRegistry.hasProcessedToken(prefs, "other_token"))
  }

  @Test
  fun dailyLivesManager_addsPurchasedLives_persists_capsAt999_andHandlesNewDayReset() {
    val prefs = InMemorySharedPreferences()
    val day1 = "2026-11-16"
    val day2 = "2026-11-17"
    val day3 = "2026-11-18"

    com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, day1)
    repeat(25) {
      assertTrue(com.example.ui.DailyLivesManager.consumeOneLife(prefs, day1).first)
    }
    assertEquals(0, com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, day1).remaining)

    // Purchase +10 lives when depleted -> 10 lives, then consume 1 -> 9 lives
    val afterFirstBuy = com.example.ui.DailyLivesManager.addPurchasedLives(prefs, 10, day1)
    assertEquals(10, afterFirstBuy.remaining)
    assertTrue(com.example.ui.DailyLivesManager.consumeOneLife(prefs, day1).first)
    assertEquals(9, com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, day1).remaining)

    // Since remaining (9) < 25, a new day resets back to 25 daily lives
    val nextDay = com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, day2)
    assertEquals(com.example.ui.DailyLivesInfo.MAX_DAILY_LIVES, nextDay.remaining)

    // Buying +50 extra lives while at 25/25 gives 75 lives and preserves >25 on day3
    val afterMegaPack = com.example.ui.DailyLivesManager.addPurchasedLives(prefs, 50, day2)
    assertEquals(75, afterMegaPack.remaining)
    assertEquals(75, com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, day3).remaining)

    // Capped at MAX_PURCHASED_LIVES_CAP (999)
    val capped = com.example.ui.DailyLivesManager.addPurchasedLives(prefs, 1500, day3)
    assertEquals(com.example.ui.DailyLivesInfo.MAX_PURCHASED_LIVES_CAP, capped.remaining)
  }

  @Test
  fun proSubscriptionManager_activatesVerifiedPlayPlan_grants15BonusDailyLives_removesAds_andDeactivatesCleanly() {
    val prefs = InMemorySharedPreferences()
    val day1 = "2026-11-16"
    val day2 = "2026-11-17"
    val now = 1_700_000_000_000L

    // Initially inactive -> 25 daily lives
    val initialSub = com.example.ui.ProSubscriptionManager.loadState(prefs, now)
    assertFalse(initialSub.isCurrentlyActive(now))
    assertEquals(25, com.example.ui.DailyLivesInfo.effectiveDailyMax(false))
    val freeLives = com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, day1, 25)
    assertEquals(25, freeLives.remaining)
    assertEquals(25, freeLives.maxDaily)

    // Activate verified DevRoutine Pro from Google Play (R$ 13/mês, +15 vidas/dia)
    val activeSub = com.example.ui.ProSubscriptionManager.activateVerifiedPlaySubscription(
      prefs = prefs,
      purchaseToken = "play_sub_token_xyz",
      orderId = "GPA.1234-5678",
      purchaseTimeMillis = now,
      isAutoRenewing = true,
      nowMillis = now
    )
    assertTrue(activeSub.isCurrentlyActive(now))
    assertTrue(activeSub.verifiedByPlayBilling)
    assertEquals("GPA.1234-5678", activeSub.orderId)
    assertEquals(13, activeSub.monthlyPriceBrl)
    assertEquals(15, activeSub.bonusDailyLives)
    assertEquals(30, activeSub.remainingDays(now))
    assertEquals(40, com.example.ui.DailyLivesInfo.effectiveDailyMax(true))

    // Immediate +15 lives grant raises 25 -> 40 and maxDaily -> 40 (only once per day!)
    assertTrue(com.example.ui.ProSubscriptionManager.shouldGrantImmediateBonus(prefs, day1))
    com.example.ui.ProSubscriptionManager.markImmediateBonusGranted(prefs, day1)
    assertFalse(com.example.ui.ProSubscriptionManager.shouldGrantImmediateBonus(prefs, day1))
    assertTrue(com.example.ui.ProSubscriptionManager.shouldGrantImmediateBonus(prefs, day2))
    val boosted = com.example.ui.DailyLivesManager.grantSubscriptionBonusLives(prefs, 15, day1)
    assertEquals(40, boosted.remaining)
    assertEquals(40, boosted.maxDaily)

    // Consume all 40 lives on day1, then verify new day resets to 40 (25 + 15) for Pro
    repeat(40) {
      assertTrue(com.example.ui.DailyLivesManager.consumeOneLife(prefs, day1, 40).first)
    }
    assertFalse(com.example.ui.DailyLivesManager.consumeOneLife(prefs, day1, 40).first)
    val day2Pro = com.example.ui.DailyLivesManager.loadOrResetForDate(prefs, day2, 40)
    assertEquals(40, day2Pro.remaining)
    assertEquals(40, day2Pro.maxDaily)

    // Level-up also restores to 40 for Pro subscribers
    assertTrue(com.example.ui.DailyLivesManager.consumeOneLife(prefs, day2, 40).first)
    val leveledUp = com.example.ui.DailyLivesManager.resetLivesForLevelUp(prefs, day2, 40)
    assertEquals(40, leveledUp.remaining)
    assertEquals(40, leveledUp.maxDaily)

    // Expiration after 30 days marks subscription inactive automatically
    val after31Days = now + 31L * com.example.model.ProSubscriptionState.MILLIS_PER_DAY
    assertFalse(com.example.ui.ProSubscriptionManager.loadState(prefs, after31Days).isCurrentlyActive(after31Days))

    // Deactivation clears Play token and subscription state cleanly without allowing another same-day immediate bonus
    com.example.ui.ProSubscriptionManager.activateVerifiedPlaySubscription(
      prefs = prefs,
      purchaseToken = "play_sub_token_xyz_2",
      orderId = "GPA.9999-0000",
      purchaseTimeMillis = now,
      nowMillis = now
    )
    val deactivated = com.example.ui.ProSubscriptionManager.deactivateSubscription(prefs)
    assertFalse(deactivated.isCurrentlyActive(now))
    assertFalse(com.example.ui.ProSubscriptionManager.loadState(prefs, now).isCurrentlyActive(now))
    assertFalse(com.example.ui.ProSubscriptionManager.shouldGrantImmediateBonus(prefs, day1))
  }

  @Test
  fun moderateAdFrequencyPolicy_enforcesModerateSpacing_andHidesAllAdsForPro() {
    val startTime = 1_700_000_000_000L
    val cooldown = com.example.model.ModerateAdFrequencyPolicy.MIN_COOLDOWN_MILLIS

    // Pro subscriber never sees ads regardless of actions or cooldown
    assertFalse(
      com.example.model.ModerateAdFrequencyPolicy.shouldShowInterstitial(
        isProActive = true,
        completedActionsSinceLastAd = 10,
        lastAdShownAtMillis = 0L,
        nowMillis = startTime
      )
    )

    // Fewer than 4 completed actions -> blocked
    assertFalse(
      com.example.model.ModerateAdFrequencyPolicy.shouldShowInterstitial(
        isProActive = false,
        completedActionsSinceLastAd = 3,
        lastAdShownAtMillis = 0L,
        nowMillis = startTime
      )
    )

    // First eligible ad after 4 actions with no prior ad
    assertTrue(
      com.example.model.ModerateAdFrequencyPolicy.shouldShowInterstitial(
        isProActive = false,
        completedActionsSinceLastAd = 4,
        lastAdShownAtMillis = 0L,
        nowMillis = startTime
      )
    )

    // Within 3-minute cooldown -> blocked even with 5 actions
    assertFalse(
      com.example.model.ModerateAdFrequencyPolicy.shouldShowInterstitial(
        isProActive = false,
        completedActionsSinceLastAd = 5,
        lastAdShownAtMillis = startTime,
        nowMillis = startTime + cooldown - 1_000L
      )
    )

    // Once cooldown elapsed and >= 4 actions -> allowed
    assertTrue(
      com.example.model.ModerateAdFrequencyPolicy.shouldShowInterstitial(
        isProActive = false,
        completedActionsSinceLastAd = 4,
        lastAdShownAtMillis = startTime,
        nowMillis = startTime + cooldown
      )
    )

    // Ad catalog returns non-empty content for any slot index
    assertTrue(com.example.model.ModerateAdCatalog.ads.isNotEmpty())
    assertEquals(com.example.model.ModerateAdCatalog.pickForSlot(0).id, com.example.model.ModerateAdCatalog.pickForSlot(4).id)
  }

  @Test
  fun studyMaterialCatalog_andOwnershipStorage_validateSkusBundleAndRevocations() {
    val packages = com.example.model.StudyMaterialCatalog.packages
    assertEquals(4, packages.size)
    assertEquals(4, packages.map { it.productId }.distinct().size)

    packages.forEach { pkg ->
      assertTrue(pkg.productId.isNotBlank())
      assertTrue(pkg.sections.isNotEmpty())
      assertTrue(pkg.highlightsPt.isNotEmpty())
      assertTrue(pkg.highlightsEn.isNotEmpty())
      assertTrue(pkg.fallbackPriceBrl.startsWith("R$"))
    }

    val algPkg = com.example.model.StudyMaterialCatalog.findByProductId(
      com.example.model.StudyMaterialCatalog.PRODUCT_ID_ALGORITHMS_DS
    )!!
    val sqlPkg = com.example.model.StudyMaterialCatalog.findByProductId(
      com.example.model.StudyMaterialCatalog.PRODUCT_ID_SQL_MASTERY
    )!!
    assertFalse(com.example.model.StudyMaterialCatalog.isPackageUnlocked(algPkg, emptySet()))
    assertTrue(
      com.example.model.StudyMaterialCatalog.isPackageUnlocked(
        algPkg,
        setOf(com.example.model.StudyMaterialCatalog.PRODUCT_ID_ALGORITHMS_DS)
      )
    )
    assertFalse(
      com.example.model.StudyMaterialCatalog.isPackageUnlocked(
        sqlPkg,
        setOf(com.example.model.StudyMaterialCatalog.PRODUCT_ID_ALGORITHMS_DS)
      )
    )

    // Full bundle unlocks every study material package
    val bundleOwned = setOf(com.example.model.StudyMaterialCatalog.PRODUCT_ID_FULL_BUNDLE)
    packages.forEach { pkg ->
      assertTrue(com.example.model.StudyMaterialCatalog.isPackageUnlocked(pkg, bundleOwned))
    }

    // Ownership storage persists verified Play productIds and clears on revocation sync
    val prefs = InMemorySharedPreferences()
    assertTrue(com.example.model.StudyMaterialOwnershipStorage.loadOwnedIds(prefs).isEmpty())
    val added = com.example.model.StudyMaterialOwnershipStorage.addOwnedId(
      prefs,
      com.example.model.StudyMaterialCatalog.PRODUCT_ID_SQL_MASTERY
    )
    assertEquals(setOf(com.example.model.StudyMaterialCatalog.PRODUCT_ID_SQL_MASTERY), added)
    val synced = com.example.model.StudyMaterialOwnershipStorage.replaceOwnedIds(prefs, emptySet())
    assertTrue(synced.isEmpty())
  }

  @Test
  fun languageIdleCatalog_providesUniqueSpecsAndRunnableTemplatesForAll12Languages() {
    val techs = Technology.entries
    assertEquals(12, techs.size)

    val specs = techs.map { com.example.model.LanguageIdleCatalog.getSpec(it) }
    assertEquals(12, specs.map { it.defaultFileName }.distinct().size)

    specs.forEach { spec ->
      assertTrue(spec.idleTitlePt.isNotBlank())
      assertTrue(spec.idleTitleEn.isNotBlank())
      assertTrue(spec.runtimeBadge.isNotBlank())
      assertTrue(spec.quickTokens.isNotEmpty())
      assertTrue("Templates missing for ${spec.tech}", spec.templates.size >= 2)
      spec.templates.forEach { tmpl ->
        assertTrue(tmpl.titlePt.isNotBlank())
        assertTrue(tmpl.titleEn.isNotBlank())
        assertTrue(tmpl.code.isNotBlank())
      }
    }

    assertTrue(com.example.model.LanguageIdleCatalog.getSpec(Technology.HTML).supportsHtmlPreview)
    assertTrue(com.example.model.LanguageIdleCatalog.getSpec(Technology.CSS).supportsHtmlPreview)
    assertTrue(com.example.model.LanguageIdleCatalog.getSpec(Technology.SQL).supportsTableOutput)
    assertTrue(com.example.model.LanguageIdleCatalog.getSpec(Technology.MYSQL).supportsTableOutput)
    assertTrue(com.example.model.LanguageIdleCatalog.getSpec(Technology.POSTGRESQL).supportsTableOutput)
  }

  @Test
  fun languageIdleEngine_executesAll12LanguagesAndHandlesErrorsCleanly() = kotlinx.coroutines.test.runTest {
    val langPt = com.example.model.Language.PT

    // Run every starter template across all 12 technologies cleanly
    Technology.entries.forEach { tech ->
      val spec = com.example.model.LanguageIdleCatalog.getSpec(tech)
      spec.templates.forEach { tmpl ->
        val res = com.example.idle.LanguageIdleEngine.execute(
          tech = tech,
          rawCode = tmpl.code,
          language = langPt,
          sqlLabRepository = null
        )
        assertEquals(
          "Template '${tmpl.id}' for ${tech.id} failed: ${res.errorMessage}",
          com.example.model.IdleExecutionStatus.SUCCESS,
          res.status
        )
        assertTrue(res.executionTimeMs >= 1L)
      }
    }

    // Verify concrete execution outputs
    val pyOut = com.example.idle.LanguageIdleEngine.execute(
      tech = Technology.PYTHON,
      rawCode = com.example.model.LanguageIdleCatalog.getSpec(Technology.PYTHON).templates.first().code,
      language = langPt
    )
    assertTrue(pyOut.outputLines.any { it.contains("Soma dos quadrados: 55") })
    assertTrue(pyOut.outputLines.any { it.contains("Total de metas: 4") })

    val jsOut = com.example.idle.LanguageIdleEngine.execute(
      tech = Technology.JAVASCRIPT,
      rawCode = com.example.model.LanguageIdleCatalog.getSpec(Technology.JAVASCRIPT).templates.first().code,
      language = langPt
    )
    assertTrue(jsOut.outputLines.any { it.contains("Média calculada: 9") })

    val tsOut = com.example.idle.LanguageIdleEngine.execute(
      tech = Technology.TYPESCRIPT,
      rawCode = com.example.model.LanguageIdleCatalog.getSpec(Technology.TYPESCRIPT).templates.first().code,
      language = langPt
    )
    assertTrue(tsOut.outputLines.any { it.contains("XP Ganho: 180") })

    val ktOut = com.example.idle.LanguageIdleEngine.execute(
      tech = Technology.KOTLIN,
      rawCode = com.example.model.LanguageIdleCatalog.getSpec(Technology.KOTLIN).templates.first().code,
      language = langPt
    )
    assertTrue(ktOut.outputLines.any { it.contains("Total final: 150") })

    val javaOut = com.example.idle.LanguageIdleEngine.execute(
      tech = Technology.JAVA,
      rawCode = com.example.model.LanguageIdleCatalog.getSpec(Technology.JAVA).templates.first().code,
      language = langPt
    )
    assertTrue(javaOut.outputLines.any { it.contains("5 x 7 = 35") })

    val goOut = com.example.idle.LanguageIdleEngine.execute(
      tech = Technology.GO,
      rawCode = com.example.model.LanguageIdleCatalog.getSpec(Technology.GO).templates.first().code,
      language = langPt
    )
    assertTrue(goOut.outputLines.any { it.contains("Soma total: 30") })

    val mongoOut = com.example.idle.LanguageIdleEngine.execute(
      tech = Technology.MONGODB,
      rawCode = "db.study_notes.insertOne({ techId: \"go\", title: \"Goroutines\" })\ndb.study_notes.countDocuments({ techId: \"go\" })",
      language = langPt
    )
    assertEquals(com.example.model.IdleExecutionStatus.SUCCESS, mongoOut.status)
    assertTrue(mongoOut.outputLines.any { it.contains("\"acknowledged\": true") })
    assertEquals("1", mongoOut.outputLines.last())

    val htmlOut = com.example.idle.LanguageIdleEngine.execute(
      tech = Technology.HTML,
      rawCode = "<h1>Hello IDLE</h1>",
      language = langPt
    )
    assertNotNull(htmlOut.htmlPreviewDocument)
    assertTrue(htmlOut.htmlPreviewDocument!!.contains("<h1>Hello IDLE</h1>"))

    val cssErr = com.example.idle.LanguageIdleEngine.execute(
      tech = Technology.CSS,
      rawCode = "body { color: red;",
      language = langPt
    )
    assertEquals(com.example.model.IdleExecutionStatus.ERROR, cssErr.status)
    assertNotNull(cssErr.errorMessage)

    val blankErr = com.example.idle.LanguageIdleEngine.execute(
      tech = Technology.PYTHON,
      rawCode = "   ",
      language = langPt
    )
    assertEquals(com.example.model.IdleExecutionStatus.ERROR, blankErr.status)
  }

  @Test
  fun languageIdleCodeStorage_savesLoadsAndResetsPerLanguage() {
    val prefs = InMemorySharedPreferences()
    val defaultPy = com.example.model.LanguageIdleCatalog.getSpec(Technology.PYTHON).templates.first().code
    val defaultKt = com.example.model.LanguageIdleCatalog.getSpec(Technology.KOTLIN).templates.first().code

    assertEquals(defaultPy, com.example.model.LanguageIdleCodeStorage.loadCode(prefs, Technology.PYTHON))
    assertEquals(defaultKt, com.example.model.LanguageIdleCodeStorage.loadCode(prefs, Technology.KOTLIN))

    com.example.model.LanguageIdleCodeStorage.saveCode(prefs, Technology.PYTHON, "print(42)")
    assertEquals("print(42)", com.example.model.LanguageIdleCodeStorage.loadCode(prefs, Technology.PYTHON))
    // Saving Python must not alter Kotlin code
    assertEquals(defaultKt, com.example.model.LanguageIdleCodeStorage.loadCode(prefs, Technology.KOTLIN))

    val resetPy = com.example.model.LanguageIdleCodeStorage.resetToDefault(prefs, Technology.PYTHON)
    assertEquals(defaultPy, resetPy)
    assertEquals(defaultPy, com.example.model.LanguageIdleCodeStorage.loadCode(prefs, Technology.PYTHON))
  }

  @Test
  fun languageTaskProgressCalculator_computesPerLanguageAndOverallPercentagesAccurately() {
    // 1. Empty completion state -> 12 technologies, all at 0%
    val emptyAll = com.example.model.LanguageTaskProgressCalculator.computeAll()
    assertEquals(12, emptyAll.size)
    emptyAll.forEach { summary ->
      assertEquals(0, summary.completedTasks)
      assertEquals(0, summary.progressPercent)
      assertEquals(0f, summary.progressFraction, 0.0001f)
      assertFalse(summary.isStarted)
      assertFalse(summary.isCompletedAll)
      assertTrue(summary.totalTasks > 0)
    }
    val emptyOverall = com.example.model.LanguageTaskProgressCalculator.computeOverall(emptyAll)
    assertEquals(0, emptyOverall.completedTasks)
    assertEquals(0, emptyOverall.progressPercent)
    assertEquals(0, emptyOverall.startedLanguagesCount)
    assertEquals(12, emptyOverall.totalLanguagesCount)

    // 2. Completing a single Python lesson guarantees >= 1% (not truncated to 0%) and does not affect Kotlin
    val pyLessons = com.example.data.repository.TechCourseModulesRepository.getAllLessonIds(Technology.PYTHON).toList()
    val pyChallenges = com.example.data.challenges.TechChallengeBank.getChallenges(Technology.PYTHON)
    val pyQuizzes = TechContentRepository.getQuizQuestions(Technology.PYTHON)

    val oneLessonSummaries = com.example.model.LanguageTaskProgressCalculator.computeAll(
      completedLessonIds = setOf(pyLessons.first())
    )
    val pyOne = oneLessonSummaries.first { it.tech == Technology.PYTHON }
    val ktOne = oneLessonSummaries.first { it.tech == Technology.KOTLIN }
    assertEquals(1, pyOne.completedLessons)
    assertEquals(1, pyOne.completedTasks)
    assertTrue(pyOne.isStarted)
    assertTrue(pyOne.progressPercent in 1..99)
    assertEquals(0, ktOne.completedTasks)
    assertEquals(0, ktOne.progressPercent)

    // 3. Multi-category aggregation (lessons + challenges + quizzes + scheduled sessions)
    val sessions = listOf(
      com.example.model.StudySession(
        id = 1L,
        tech = Technology.KOTLIN,
        titlePt = "Coroutines",
        titleEn = "Coroutines",
        topicPt = "Coroutines na prática",
        topicEn = "Coroutines practice",
        dateIso = "2026-11-17",
        durationMinutes = 45,
        isCompleted = true
      ),
      com.example.model.StudySession(
        id = 2L,
        tech = Technology.KOTLIN,
        titlePt = "Flow",
        titleEn = "Flow",
        topicPt = "Kotlin Flow",
        topicEn = "Kotlin Flow",
        dateIso = "2026-11-18",
        durationMinutes = 30,
        isCompleted = false
      )
    )
    val mixedSummaries = com.example.model.LanguageTaskProgressCalculator.computeAll(
      completedActivityIds = setOf(pyChallenges[0].id, pyChallenges[1].id),
      completedQuizIds = setOf(pyQuizzes[0].id),
      completedLessonIds = setOf(pyLessons[0]),
      sessions = sessions
    )
    val pyMixed = mixedSummaries.first { it.tech == Technology.PYTHON }
    assertEquals(1, pyMixed.completedLessons)
    assertEquals(2, pyMixed.completedChallenges)
    assertEquals(1, pyMixed.completedQuizzes)
    assertEquals(0, pyMixed.completedSessions)
    assertEquals(4, pyMixed.completedTasks)

    val ktMixed = mixedSummaries.first { it.tech == Technology.KOTLIN }
    assertEquals(1, ktMixed.completedSessions)
    assertEquals(2, ktMixed.totalSessions)
    assertEquals(1, ktMixed.completedTasks)

    val mixedOverall = com.example.model.LanguageTaskProgressCalculator.computeOverall(mixedSummaries)
    assertEquals(5, mixedOverall.completedTasks)
    assertEquals(2, mixedOverall.startedLanguagesCount)
    assertTrue(mixedOverall.progressPercent in 1..99)

    // 4. Full completion of every study task in Go -> exactly 100%
    val goLessons = com.example.data.repository.TechCourseModulesRepository.getAllLessonIds(Technology.GO).toSet()
    val goChallenges = com.example.data.challenges.TechChallengeBank.getChallenges(Technology.GO).map { it.id }.toSet()
    val goQuizzes = TechContentRepository.getQuizQuestions(Technology.GO).map { it.id }.toSet()
    val goSummary = com.example.model.LanguageTaskProgressCalculator.computeForTech(
      tech = Technology.GO,
      completedActivityIds = goChallenges,
      completedQuizIds = goQuizzes,
      completedLessonIds = goLessons,
      sessions = emptyList()
    )
    assertTrue(goSummary.isCompletedAll)
    assertEquals(100, goSummary.progressPercent)
    assertEquals(1f, goSummary.progressFraction, 0.0001f)
    assertEquals(goSummary.totalTasks, goSummary.completedTasks)
  }

  @Test
  fun appLocaleHelper_mapsLanguagesToLocalesAndPersistsSelection() {
    val prefs = InMemorySharedPreferences()
    // Default when unset is Portuguese
    assertEquals(
      com.example.model.Language.PT,
      com.example.util.AppLocaleHelper.loadSavedLanguage(prefs)
    )

    val ptLocale = com.example.util.AppLocaleHelper.toLocale(com.example.model.Language.PT)
    assertEquals("pt", ptLocale.language)
    assertEquals("BR", ptLocale.country)

    val enLocale = com.example.util.AppLocaleHelper.toLocale(com.example.model.Language.EN)
    assertEquals("en", enLocale.language)
    assertEquals("US", enLocale.country)

    com.example.util.AppLocaleHelper.saveLanguage(prefs, com.example.model.Language.EN)
    assertEquals(
      com.example.model.Language.EN,
      com.example.util.AppLocaleHelper.loadSavedLanguage(prefs)
    )

    com.example.util.AppLocaleHelper.saveLanguage(prefs, com.example.model.Language.PT)
    assertEquals(
      com.example.model.Language.PT,
      com.example.util.AppLocaleHelper.loadSavedLanguage(prefs)
    )
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
    override fun getUnlockedBadges() = kotlinx.coroutines.flow.flow { flow.collect { list -> emit(list.filter { it.isUnlocked }) } }
    override fun getUnlockedCount() = kotlinx.coroutines.flow.flow { flow.collect { list -> emit(list.count { it.isUnlocked }) } }
    override fun getTotalCount() = kotlinx.coroutines.flow.flow { flow.collect { list -> emit(list.size) } }

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
