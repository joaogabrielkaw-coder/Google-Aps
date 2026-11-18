package com.example

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.model.ChatCallStatus
import com.example.model.ChatRequestStatus
import com.example.model.Language
import com.example.model.LifeShopCatalog
import com.example.model.PlayBillingConnectionStatus
import com.example.model.ProSubscriptionPlan
import com.example.model.ThemePreference
import com.example.model.UserAccount
import com.example.ui.AppDestination
import com.example.ui.MainViewModel
import com.example.ui.components.ActiveCallTopBanner
import com.example.ui.components.ActiveLiveTopBanner
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.components.AuthLoginDialog
import com.example.ui.components.BadgeDetailDialog
import com.example.ui.components.ChatCallOverlay
import com.example.ui.components.InitialAssessmentDialog
import com.example.ui.components.LiveStudyRoomOverlay
import com.example.ui.components.ModerateInterstitialAdDialog
import com.example.ui.components.ProSubscriptionDialog
import com.example.ui.components.ProfilePhotoDialog
import com.example.ui.components.WeeklyStudySummaryModal
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CommunityChatScreen
import com.example.ui.screens.GameAndScene3DScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguageIdleScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SqlDatabaseScreen
import com.example.ui.screens.StudyMaterialsStoreScreen
import com.example.ui.screens.TargetSubjectsScreen
import com.example.ui.screens.TechDetailScreen
import com.example.ui.screens.TechHubScreen
import com.example.ui.theme.DevRoutineTheme
import com.example.util.AppLocaleHelper
import com.example.util.configureAppCheck
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.util.Locale
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleHelper.wrapBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val themePreference by viewModel.themePreference.collectAsState()
            val themeCustomization by viewModel.themeCustomization.collectAsState()
            val accessibilitySettings by viewModel.accessibilitySettings.collectAsState()
            val language by viewModel.language.collectAsState()

            val baseConfiguration = LocalConfiguration.current
            val baseContext = LocalContext.current
            val localizedConfiguration = remember(baseConfiguration, language) {
                AppLocaleHelper.createLocalizedConfiguration(baseConfiguration, language)
            }
            val localizedContext = remember(baseContext, language) {
                AppLocaleHelper.createLocalizedContext(baseContext, language)
            }
            Locale.setDefault(AppLocaleHelper.toLocale(language))

            val isDarkTheme = when (themePreference) {
                ThemePreference.DARK -> true
                ThemePreference.LIGHT -> false
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfiguration,
                LocalContext provides localizedContext
            ) {
                DevRoutineTheme(
                    darkTheme = isDarkTheme,
                    customization = themeCustomization,
                    accessibilitySettings = accessibilitySettings
                ) {
                    MainAppContainer(
                        viewModel = viewModel,
                        hostActivity = this@MainActivity,
                        isDarkTheme = isDarkTheme,
                        currentLanguage = language,
                        currentTheme = themePreference
                    )
                }
            }
        }
    }
}

@Composable
fun MainAppContainer(
    viewModel: MainViewModel,
    hostActivity: Activity,
    isDarkTheme: Boolean,
    currentLanguage: Language,
    currentTheme: ThemePreference
) {
    val destination by viewModel.currentDestination.collectAsState()
    val streakInfo by viewModel.streakInfo.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allRoutinesWithEvents by viewModel.allRoutinesWithEvents.collectAsState()
    val selectedTech by viewModel.selectedTech.collectAsState()
    val calendarSelectedDate by viewModel.calendarSelectedDate.collectAsState()
    val calendarTechFilter by viewModel.calendarTechFilter.collectAsState()

    val aiMessages by viewModel.aiMessages.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val streamingText by viewModel.currentStreamingText.collectAsState()
    val aiErrorMessage by viewModel.aiErrorMessage.collectAsState()
    val scoreInfo by viewModel.scoreInfo.collectAsState()
    val pointsFeedback by viewModel.pointsFeedback.collectAsState()
    val allBadges by viewModel.allBadges.collectAsState()
    val newlyUnlockedBadge by viewModel.newlyUnlockedBadge.collectAsState()

    val userAccount by viewModel.userAccount.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val leaderboardEntries by viewModel.leaderboardEntries.collectAsState()
    val trackerSelectedTech by viewModel.trackerSelectedTech.collectAsState()
    val todayProgressMap by viewModel.todayLanguageProgressMap.collectAsState()
    val weeklyChallengeHistory by viewModel.weeklyChallengeHistory.collectAsState()
    val dailyLives by viewModel.dailyLives.collectAsState()
    val todayScreenTimeMinutes by viewModel.todayScreenTimeMinutes.collectAsState()
    val screenTimeMinutes by viewModel.screenTimeMinutes.collectAsState()
    val dailyStudyGoalState by viewModel.dailyStudyGoalState.collectAsState()
    val isInitialAssessmentCompleted by viewModel.isInitialAssessmentCompleted.collectAsState()
    val isWeeklySummaryModalOpen by viewModel.isWeeklySummaryModalOpen.collectAsState()
    val weeklySummaryState by viewModel.weeklySummaryState.collectAsState()
    val transcriptionState by viewModel.transcriptionState.collectAsState()
    val allStudyNotes by viewModel.allStudyNotes.collectAsState()
    val topGameScores by viewModel.topGameScores.collectAsState()
    val bestGameScore by viewModel.bestGameScore.collectAsState()
    val totalGameCrystals by viewModel.totalGameCrystals.collectAsState()
    val availableShopXp by viewModel.availableShopXp.collectAsState()
    val availableGameCrystals by viewModel.availableGameCrystals.collectAsState()
    val sqlQueryResult by viewModel.sqlQueryResult.collectAsState()
    val themeCustomization by viewModel.themeCustomization.collectAsState()
    val accessibilitySettings by viewModel.accessibilitySettings.collectAsState()
    val appGeneralSettings by viewModel.appGeneralSettings.collectAsState()
    val userFeedbackNotes by viewModel.userFeedbackNotes.collectAsState()
    val levelUpEvent by viewModel.levelUpEvent.collectAsState()
    val completedCourseLessonIds by viewModel.completedCourseLessonIds.collectAsState()

    val isFirebaseChatAuthenticated by viewModel.isFirebaseChatAuthenticated.collectAsState()
    val chatOwnerId by viewModel.chatOwnerId.collectAsState()
    val chatDirectoryUsers by viewModel.chatDirectoryUsers.collectAsState()
    val chatRequests by viewModel.chatRequests.collectAsState()
    val chatConversations by viewModel.chatConversations.collectAsState()
    val activeChatId by viewModel.activeChatId.collectAsState()
    val activeChatMessages by viewModel.activeChatMessages.collectAsState()
    val isChatUploading by viewModel.isChatUploading.collectAsState()
    val chatStatusFeedback by viewModel.chatStatusFeedback.collectAsState()
    val activeCallSession by viewModel.activeCallSession.collectAsState()
    val activeLiveRooms by viewModel.activeLiveRooms.collectAsState()
    val joinedLiveRoom by viewModel.joinedLiveRoom.collectAsState()
    val liveRoomMessages by viewModel.liveRoomMessages.collectAsState()
    val isLiveMicMuted by viewModel.isLiveMicMuted.collectAsState()
    val isLiveCameraEnabled by viewModel.isLiveCameraEnabled.collectAsState()
    val isLiveSpeakerOn by viewModel.isLiveSpeakerOn.collectAsState()
    val isLivePeerConnected by viewModel.isLivePeerConnected.collectAsState()
    val liveElapsedSeconds by viewModel.liveElapsedSeconds.collectAsState()
    val targetSubjectsPreference by viewModel.targetSubjectsPreference.collectAsState()
    val isTargetSubjectsCloudSynced by viewModel.isTargetSubjectsCloudSynced.collectAsState()
    val targetSubjectsUiState by viewModel.targetSubjectsUiState.collectAsState()
    val proSubscriptionState by viewModel.proSubscriptionState.collectAsState()
    val billingStoreState by viewModel.billingStoreUiState.collectAsState()
    val studyMaterialProductDetails by viewModel.studyMaterialProductDetails.collectAsState()
    val ownedStudyMaterialIds by viewModel.ownedStudyMaterialIds.collectAsState()
    val lastLifePurchaseReceipt by viewModel.lastLifePurchaseReceipt.collectAsState()
    val activeModerateInterstitial by viewModel.activeModerateInterstitial.collectAsState()
    val isProSubscriber = proSubscriptionState.isCurrentlyActive()
    val idleSelectedTech by viewModel.idleSelectedTech.collectAsState()
    val idleCodeByTech by viewModel.idleCodeByTech.collectAsState()
    val idleResultByTech by viewModel.idleResultByTech.collectAsState()

    val pendingChatRequestsCount = remember(chatRequests, chatOwnerId) {
        val owner = chatOwnerId
        if (owner.isNullOrBlank()) 0
        else chatRequests.count { it.isIncomingFor(owner) && it.status == ChatRequestStatus.PENDING }
    }

    var showAuthDialog by remember { mutableStateOf(false) }
    var showInitialAssessmentDialog by remember { mutableStateOf(false) }
    var showSettingsPhotoDialog by remember { mutableStateOf(false) }
    var showDailyLivesInfoDialog by remember { mutableStateOf(false) }
    var showLifePurchaseShopDialog by remember { mutableStateOf(false) }
    var showProSubscriptionDialog by remember { mutableStateOf(false) }
    var hasDeferredAssessmentInSession by remember { mutableStateOf(false) }
    var isCallMinimized by remember(activeCallSession?.callId) { mutableStateOf(false) }
    var isLiveMinimized by remember(joinedLiveRoom?.roomId) { mutableStateOf(false) }
    var pendingTargetSubjectsDraft by remember { mutableStateOf<TargetSubjectsDraft?>(null) }

    LaunchedEffect(isFirebaseChatAuthenticated, pendingTargetSubjectsDraft) {
        val draft = pendingTargetSubjectsDraft
        if (isFirebaseChatAuthenticated && draft != null) {
            pendingTargetSubjectsDraft = null
            viewModel.saveTargetSubjectsPreferences(
                subjectIds = draft.subjectIds,
                dailyGoalMinutes = draft.dailyGoalMinutes,
                learningTrack = draft.learningTrack,
                studyGoalNote = draft.studyGoalNote
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.setAppInForeground(true)
                    viewModel.recordUserActivity()
                    viewModel.refreshDailyLivesIfNewDay()
                    viewModel.refreshDailyScreenTimeIfNewDay()
                    viewModel.refreshBillingOnResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.setAppInForeground(false)
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshDailyLivesIfNewDay()
        viewModel.refreshDailyScreenTimeIfNewDay()
    }

    LaunchedEffect(showLifePurchaseShopDialog) {
        if (showLifePurchaseShopDialog) {
            val allLivesSkusLoaded = LifeShopCatalog.brlPackages.all { pkg ->
                pkg.playProductId != null && billingStoreState.isProductReadyInPlay(pkg.playProductId)
            }
            if (billingStoreState.connectionStatus != PlayBillingConnectionStatus.READY || !allLivesSkusLoaded) {
                viewModel.refreshStudyMaterialsCatalog(userInitiated = false)
            }
        }
    }

    LaunchedEffect(showProSubscriptionDialog) {
        if (showProSubscriptionDialog) {
            if (billingStoreState.connectionStatus != PlayBillingConnectionStatus.READY ||
                !billingStoreState.isProductReadyInPlay(ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY)
            ) {
                viewModel.refreshStudyMaterialsCatalog(userInitiated = false)
            }
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val serverClientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        ""
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Prompt initial assessment ONLY after leaving the login screen (when user enters the dashboard)
    LaunchedEffect(destination, isInitialAssessmentCompleted, hasDeferredAssessmentInSession) {
        if (destination != AppDestination.LOGIN && !isInitialAssessmentCompleted && !hasDeferredAssessmentInSession) {
            showInitialAssessmentDialog = true
        }
    }

    val launchGoogleSignIn: (String?, () -> Unit) -> Unit = { extraPhoneNumber, onSuccess ->
        coroutineScope.launch {
            try {
                val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(
                    request = request,
                    context = context.findActivity() ?: context
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.loginWithGoogle(
                        idToken = googleIdTokenCredential.idToken,
                        displayName = googleIdTokenCredential.displayName,
                        email = googleIdTokenCredential.id,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                        extraPhone = extraPhoneNumber.orEmpty()
                    )
                    onSuccess()
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Google sign-in credential manager error: ${e.message}", e)
            }
        }
    }

    // Back handling for sub-screens
    BackHandler(enabled = destination != AppDestination.HOME && destination != AppDestination.LOGIN) {
        when {
            destination == AppDestination.COMMUNITY_CHAT && activeChatId != null -> {
                viewModel.closeActiveChatConversation()
            }
            destination == AppDestination.TECH_DETAIL -> {
                viewModel.navigateTo(AppDestination.TECH_HUB)
            }
            else -> {
                viewModel.navigateTo(AppDestination.HOME)
            }
        }
    }

    if (destination == AppDestination.LOGIN) {
        LoginScreen(
            language = currentLanguage,
            isLoading = isAuthLoading,
            onToggleLanguage = {
                viewModel.setLanguage(if (currentLanguage == Language.PT) Language.EN else Language.PT)
            },
            onGoogleSignInSuccess = { idToken, name, email, photo ->
                viewModel.loginWithGoogle(
                    idToken = idToken,
                    displayName = name,
                    email = email,
                    photoUrl = photo
                )
            },
            onPhoneSignInSuccess = { name, phone ->
                viewModel.loginWithPhoneNumber(name, phone)
            },
            onExploreWithoutAuth = {
                viewModel.navigateTo(AppDestination.HOME)
            }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("app_scaffold"),
            topBar = {
                val showTopBar = destination != AppDestination.TECH_DETAIL &&
                    destination != AppDestination.GAME_3D &&
                    destination != AppDestination.TARGET_SUBJECTS &&
                    destination != AppDestination.STUDY_STORE &&
                    destination != AppDestination.LANGUAGE_IDLE &&
                    !(destination == AppDestination.COMMUNITY_CHAT && activeChatId != null)
                if (showTopBar) {
                    AppTopBar(
                        currentLanguage = currentLanguage,
                        currentTheme = currentTheme,
                        userAccount = userAccount,
                        dailyLives = dailyLives,
                        pendingChatRequestsCount = pendingChatRequestsCount,
                        isCommunityChatSelected = destination == AppDestination.COMMUNITY_CHAT,
                        isSettingsSelected = destination == AppDestination.SETTINGS,
                        onOpenLivesInfo = { showDailyLivesInfoDialog = true },
                        onOpenLifeShop = { showLifePurchaseShopDialog = true },
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onOpenCommunityChat = {
                            viewModel.navigateTo(
                                if (destination == AppDestination.COMMUNITY_CHAT) AppDestination.HOME
                                else AppDestination.COMMUNITY_CHAT
                            )
                        },
                        onOpenLeaderboard = {
                            viewModel.navigateTo(AppDestination.LEADERBOARD)
                        },
                        onOpenAuth = {
                            if (userAccount?.isLoggedIn == true) {
                                showAuthDialog = true
                            } else {
                                viewModel.navigateTo(AppDestination.LOGIN)
                            }
                        },
                        onOpenSettings = {
                            viewModel.navigateTo(
                                if (destination == AppDestination.SETTINGS) AppDestination.HOME
                                else AppDestination.SETTINGS
                            )
                        }
                    )
                }
            },
            bottomBar = {
                if (destination != AppDestination.GAME_3D) {
                    AppBottomNav(
                        currentDestination = destination,
                        language = currentLanguage,
                        pendingChatBadgeCount = pendingChatRequestsCount,
                        hasActiveLives = activeLiveRooms.isNotEmpty(),
                        onNavigate = { dest -> viewModel.navigateTo(dest) }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent(PointerEventPass.Initial)
                                viewModel.recordUserActivity()
                            }
                        }
                    }
            ) {
            when (destination) {
                AppDestination.HOME -> {
                    HomeScreen(
                        language = currentLanguage,
                        streakInfo = streakInfo,
                        scoreInfo = scoreInfo,
                        badges = allBadges,
                        sessions = allSessions,
                        routinesWithEvents = allRoutinesWithEvents,
                        selectedDateIso = calendarSelectedDate,
                        trackerSelectedTech = trackerSelectedTech,
                        onSelectTrackerTech = { tech -> viewModel.setTrackerSelectedTech(tech) },
                        todayProgressMap = todayProgressMap,
                        isLoggedIn = userAccount?.isLoggedIn == true,
                        todayScreenTimeMinutes = todayScreenTimeMinutes,
                        screenTimeMinutes = screenTimeMinutes,
                        dailyStudyGoalState = dailyStudyGoalState,
                        isInitialAssessmentCompleted = isInitialAssessmentCompleted,
                        onStartInitialAssessment = { showInitialAssessmentDialog = true },
                        onSelectDate = { dateIso -> viewModel.setCalendarDate(dateIso) },
                        onToggleSession = { viewModel.toggleSession(it) },
                        getTechScore = { tech -> viewModel.getTechScore(tech) },
                        onPromptAiTopic = { tech, topicTitle -> viewModel.promptAiAboutTopic(tech, topicTitle) },
                        dailyLives = dailyLives,
                        pendingChatRequestsCount = pendingChatRequestsCount,
                        isProSubscriber = isProSubscriber,
                        onOpenProSubscription = { showProSubscriptionDialog = true },
                        targetSubjectIds = targetSubjectsPreference?.subjectIds.orEmpty(),
                        completedLessonIds = completedCourseLessonIds,
                        onOpenTargetSubjects = { viewModel.navigateTo(AppDestination.TARGET_SUBJECTS) },
                        onOpenLivesInfo = { showDailyLivesInfoDialog = true },
                        onOpenLifeShop = { showLifePurchaseShopDialog = true },
                        onOpenWeeklyAiSummary = { viewModel.openWeeklyStudySummaryModal() },
                        onOpenTech = { tech -> viewModel.openTechDetail(tech) },
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onNavigate = { dest -> viewModel.navigateTo(dest) }
                    )
                }

                AppDestination.TECH_HUB -> {
                    TechHubScreen(
                        language = currentLanguage,
                        isProSubscriber = isProSubscriber,
                        onOpenProSubscription = { showProSubscriptionDialog = true },
                        targetSubjectIds = targetSubjectsPreference?.subjectIds.orEmpty(),
                        scoreInfo = scoreInfo,
                        completedLessonIds = completedCourseLessonIds,
                        sessions = allSessions,
                        onOpenTargetSubjects = { viewModel.navigateTo(AppDestination.TARGET_SUBJECTS) },
                        onOpenStudyStore = { viewModel.navigateTo(AppDestination.STUDY_STORE) },
                        onSelectTech = { tech -> viewModel.openTechDetail(tech) }
                    )
                }

                AppDestination.TECH_DETAIL -> {
                    TechDetailScreen(
                        tech = selectedTech,
                        language = currentLanguage,
                        scoreInfo = scoreInfo,
                        onAwardQuizPoints = { tech, qId, pts -> viewModel.awardQuizPoints(tech, qId, pts) },
                        onAwardActivityPoints = { tech, actId, pts -> viewModel.awardActivityPoints(tech, actId, pts) },
                        getTechScore = { tech -> viewModel.getTechScore(tech) },
                        pointsFeedback = pointsFeedback,
                        onDismissPointsFeedback = { viewModel.dismissPointsFeedback() },
                        onBack = { viewModel.navigateTo(AppDestination.TECH_HUB) },
                        onAskAi = { viewModel.promptAiAboutTech(selectedTech) },
                        todayChallengesCount = todayProgressMap[selectedTech.id]?.completedChallengesCount ?: 0,
                        badges = allBadges,
                        dailyLives = dailyLives,
                        appGeneralSettings = appGeneralSettings,
                        onConsumeLife = { viewModel.consumeLifeForAttempt() },
                        availableShopXp = availableShopXp,
                        availableGameCrystals = availableGameCrystals,
                        onPurchaseLives = { pkg -> viewModel.purchaseLives(pkg) },
                        onOpenLifeShop = { showLifePurchaseShopDialog = true },
                        onOpen3DGame = { viewModel.navigateTo(AppDestination.GAME_3D) },
                        completedLessonIds = completedCourseLessonIds,
                        onToggleLessonCompleted = { tech, lessonId ->
                            viewModel.toggleCourseLessonCompleted(tech, lessonId)
                        },
                        idleCode = idleCodeByTech[selectedTech]
                            ?: com.example.model.LanguageIdleCatalog.getSpec(selectedTech).templates.first().code,
                        idleResult = idleResultByTech[selectedTech],
                        onUpdateIdleCode = { newCode -> viewModel.updateIdleCode(selectedTech, newCode) },
                        onRunIdleCode = { viewModel.runIdleCode(selectedTech) },
                        onResetIdleCode = { viewModel.resetIdleCode(selectedTech) },
                        onClearIdleOutput = { viewModel.clearIdleOutput(selectedTech) },
                        onOpenFullIdleHub = { viewModel.openLanguageIdle(selectedTech) }
                    )
                }

                AppDestination.PROGRESS -> {
                    ProgressScreen(
                        language = currentLanguage,
                        scoreInfo = scoreInfo,
                        badges = allBadges,
                        todayProgressMap = todayProgressMap,
                        weeklyChallengeHistory = weeklyChallengeHistory,
                        dailyStudyGoalState = dailyStudyGoalState,
                        completedLessonIds = completedCourseLessonIds,
                        sessions = allSessions,
                        targetSubjectIds = targetSubjectsPreference?.subjectIds.orEmpty(),
                        getTechScore = { tech -> viewModel.getTechScore(tech) },
                        onOpenTechDetail = { tech -> viewModel.openTechDetail(tech) },
                        onOpenWeeklyAiSummary = { viewModel.openWeeklyStudySummaryModal() }
                    )
                }

                AppDestination.CALENDAR -> {
                    CalendarScreen(
                        language = currentLanguage,
                        sessions = allSessions,
                        selectedDateIso = calendarSelectedDate,
                        filterTech = calendarTechFilter,
                        onSelectDate = { dateIso -> viewModel.setCalendarDate(dateIso) },
                        onSetFilterTech = { tech -> viewModel.setCalendarFilter(tech) },
                        onToggleSession = { viewModel.toggleSession(it) },
                        onDeleteSession = { viewModel.deleteSession(it) },
                        onAddSession = { viewModel.addManualSession(it) },
                        onOpenAiPlanner = { viewModel.navigateTo(AppDestination.AI_CHAT) }
                    )
                }

                AppDestination.AI_CHAT -> {
                    AiChatScreen(
                        language = currentLanguage,
                        messages = aiMessages,
                        isLoading = isAiLoading,
                        streamingText = streamingText,
                        errorMessage = aiErrorMessage,
                        transcriptionState = transcriptionState,
                        onRecordingStateChanged = { isRec -> viewModel.setAudioRecordingState(isRec) },
                        onAudioCaptured = { bytes, mime, dur ->
                            viewModel.transcribeAudioPayload(bytes, mime, dur)
                        },
                        onClearTranscriptionState = { viewModel.clearTranscriptionState() },
                        onSendMessage = { prompt -> viewModel.sendAiUserMessage(prompt) },
                        onGenerateRoutine = { goal, techs, days, minutes ->
                            viewModel.generateRoutineWithAi(goal, techs, days, minutes)
                        },
                        onSaveRoutineToCalendar = { plan ->
                            viewModel.saveRoutinePlanToCalendar(plan)
                        }
                    )
                }

                AppDestination.GAME_3D -> {
                    GameAndScene3DScreen(
                        language = currentLanguage,
                        bestScore = bestGameScore,
                        selectedFocusTech = trackerSelectedTech,
                        onGameFinished = { sc, maxC, cr, dur, tech ->
                            viewModel.record3DGameScore(sc, maxC, cr, dur, tech)
                        },
                        onExit = { viewModel.navigateTo(AppDestination.HOME) }
                    )
                }

                AppDestination.SQL_STUDIO -> {
                    SqlDatabaseScreen(
                        language = currentLanguage,
                        studyNotes = allStudyNotes,
                        topGameScores = topGameScores,
                        bestGameScore = bestGameScore,
                        totalCrystals = totalGameCrystals,
                        sqlQueryResult = sqlQueryResult,
                        transcriptionState = transcriptionState,
                        onRecordingStateChanged = { isRec -> viewModel.setAudioRecordingState(isRec) },
                        onAudioCaptured = { bytes, mime, dur ->
                            viewModel.transcribeAudioPayload(bytes, mime, dur)
                        },
                        onClearTranscriptionState = { viewModel.clearTranscriptionState() },
                        onAddNote = { tech, title, content, isVoice ->
                            viewModel.addStudyNote(tech, title, content, isVoice)
                        },
                        onDeleteNote = { id -> viewModel.deleteStudyNote(id) },
                        onRunSqlQuery = { sql -> viewModel.executeSqlQuery(sql) },
                        onOpen3DGame = { viewModel.navigateTo(AppDestination.GAME_3D) }
                    )
                }

                AppDestination.LEADERBOARD -> {
                    LeaderboardScreen(
                        leaderboardEntries = leaderboardEntries,
                        currentUserAccount = userAccount,
                        language = currentLanguage,
                        onOpenAuthDialog = {
                            if (userAccount?.isLoggedIn == true) {
                                showAuthDialog = true
                            } else {
                                viewModel.navigateTo(AppDestination.LOGIN)
                            }
                        },
                        onSyncLeaderboard = { viewModel.syncScoreToLeaderboard() }
                    )
                }

                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        currentLanguage = currentLanguage,
                        currentTheme = currentTheme,
                        themeCustomization = themeCustomization,
                        accessibilitySettings = accessibilitySettings,
                        appGeneralSettings = appGeneralSettings,
                        userFeedbackNotes = userFeedbackNotes,
                        userAccount = userAccount,
                        onBack = { viewModel.navigateTo(AppDestination.HOME) },
                        onSetLanguage = { viewModel.setLanguage(it) },
                        onSetTheme = { viewModel.setThemePreference(it) },
                        onSelectThemePreset = { viewModel.selectThemePreset(it) },
                        onUpdateThemeCustomization = { viewModel.updateThemeCustomization(it) },
                        onResetThemeDefault = { viewModel.resetThemeCustomizationToDefault() },
                        onUpdateAccessibility = { viewModel.updateAccessibilitySettings(it) },
                        onResetAccessibility = { viewModel.resetAccessibilitySettings() },
                        onUpdateAppGeneralSettings = { viewModel.updateAppGeneralSettings(it) },
                        onSubmitFeedback = { category, rating, message ->
                            viewModel.submitUserAppFeedback(category, rating, message)
                        },
                        onDeleteFeedback = { noteId -> viewModel.deleteUserAppFeedback(noteId) },
                        onOpenWeeklyAiSummary = { viewModel.openWeeklyStudySummaryModal() },
                        onOpenPhotoPicker = { showSettingsPhotoDialog = true },
                        onRedoAssessment = { showInitialAssessmentDialog = true },
                        onResetStarterData = {},
                        onSendTestNotification = { viewModel.sendTestReminderNotification() },
                        targetSubjectsCount = targetSubjectsPreference?.subjectIds?.size ?: 0,
                        onOpenTargetSubjects = { viewModel.navigateTo(AppDestination.TARGET_SUBJECTS) },
                        proSubscriptionState = proSubscriptionState,
                        onOpenProSubscription = { showProSubscriptionDialog = true }
                    )
                }

                AppDestination.TARGET_SUBJECTS -> {
                    TargetSubjectsScreen(
                        language = currentLanguage,
                        savedPreference = targetSubjectsPreference,
                        uiState = targetSubjectsUiState,
                        isFirebaseAuthenticated = isFirebaseChatAuthenticated,
                        isCloudSynced = isTargetSubjectsCloudSynced,
                        getTechScore = { tech -> viewModel.getTechScore(tech) },
                        onBack = { viewModel.navigateTo(AppDestination.HOME) },
                        onGoogleSignInClick = { launchGoogleSignIn(null) {} },
                        onSavePreferences = { subjectIds, dailyGoalMinutes, learningTrack, studyGoalNote ->
                            if (isFirebaseChatAuthenticated) {
                                viewModel.saveTargetSubjectsPreferences(
                                    subjectIds = subjectIds,
                                    dailyGoalMinutes = dailyGoalMinutes,
                                    learningTrack = learningTrack,
                                    studyGoalNote = studyGoalNote
                                )
                            } else {
                                pendingTargetSubjectsDraft = TargetSubjectsDraft(
                                    subjectIds = subjectIds,
                                    dailyGoalMinutes = dailyGoalMinutes,
                                    learningTrack = learningTrack,
                                    studyGoalNote = studyGoalNote
                                )
                                launchGoogleSignIn(null) {}
                            }
                        },
                        onClearFeedback = { viewModel.clearTargetSubjectsFeedback() },
                        onOpenStudyTracks = { viewModel.navigateTo(AppDestination.TECH_HUB) }
                    )
                }

                AppDestination.STUDY_STORE -> {
                    StudyMaterialsStoreScreen(
                        language = currentLanguage,
                        billingStoreState = billingStoreState,
                        productDetailsMap = studyMaterialProductDetails,
                        ownedProductIds = ownedStudyMaterialIds,
                        onBack = { viewModel.navigateTo(AppDestination.HOME) },
                        onPurchasePackage = { productId ->
                            viewModel.launchStudyMaterialBilling(hostActivity, productId)
                        },
                        onRefreshCatalog = { viewModel.refreshStudyMaterialsCatalog(userInitiated = true) },
                        onAutoRefreshCatalog = { viewModel.refreshStudyMaterialsCatalog(userInitiated = false) },
                        onRestorePurchases = { viewModel.restorePlayPurchases() },
                        onDismissStatusMessage = { viewModel.clearBillingStatusMessage() }
                    )
                }

                AppDestination.LANGUAGE_IDLE -> {
                    LanguageIdleScreen(
                        selectedTech = idleSelectedTech,
                        codeByTech = idleCodeByTech,
                        resultByTech = idleResultByTech,
                        language = currentLanguage,
                        onSelectTech = { tech -> viewModel.selectIdleTech(tech) },
                        onUpdateCode = { tech, code -> viewModel.updateIdleCode(tech, code) },
                        onRunCode = { tech -> viewModel.runIdleCode(tech) },
                        onResetCode = { tech -> viewModel.resetIdleCode(tech) },
                        onClearOutput = { tech -> viewModel.clearIdleOutput(tech) },
                        onBack = { viewModel.navigateTo(AppDestination.HOME) }
                    )
                }

                AppDestination.COMMUNITY_CHAT -> {
                    val callBanner: (@Composable () -> Unit)? = activeCallSession?.let { call ->
                        if (call.status == ChatCallStatus.ACCEPTED && isCallMinimized) {
                            {
                                ActiveCallTopBanner(
                                    callSession = call,
                                    currentUserId = chatOwnerId,
                                    language = currentLanguage,
                                    onExpand = { isCallMinimized = false },
                                    onToggleMicMute = { viewModel.toggleCallMicMute() },
                                    onEndCall = { viewModel.declineOrEndCall() }
                                )
                            }
                        } else null
                    }
                    val liveBanner: (@Composable () -> Unit)? = joinedLiveRoom?.let { room ->
                        if (isLiveMinimized) {
                            {
                                ActiveLiveTopBanner(
                                    liveRoom = room,
                                    currentUserId = chatOwnerId,
                                    language = currentLanguage,
                                    isMicMuted = isLiveMicMuted,
                                    elapsedSeconds = liveElapsedSeconds,
                                    onExpand = { isLiveMinimized = false },
                                    onToggleMicMute = { viewModel.toggleLiveMicMute() },
                                    onLeaveOrEnd = { viewModel.leaveOrEndLiveRoom() }
                                )
                            }
                        } else null
                    }
                    CommunityChatScreen(
                        language = currentLanguage,
                        currentUserId = chatOwnerId,
                        isFirebaseAuthenticated = isFirebaseChatAuthenticated,
                        directoryUsers = chatDirectoryUsers,
                        leaderboardEntries = leaderboardEntries,
                        chatRequests = chatRequests,
                        conversations = chatConversations,
                        activeLiveRooms = activeLiveRooms,
                        activeChatId = activeChatId,
                        activeMessages = activeChatMessages,
                        isUploading = isChatUploading,
                        statusFeedback = chatStatusFeedback,
                        onDismissFeedback = { viewModel.dismissChatFeedback() },
                        onGoogleSignInClick = { launchGoogleSignIn(null) {} },
                        onOpenConversation = { chatId -> viewModel.openChatConversation(chatId) },
                        onCloseConversation = { viewModel.closeActiveChatConversation() },
                        onSendRequest = { recipient, intro -> viewModel.sendChatRequest(recipient, intro) },
                        onAcceptRequest = { req -> viewModel.acceptChatRequest(req, openImmediately = true) },
                        onDeclineOrDeleteRequest = { reqId -> viewModel.declineOrDeleteChatRequest(reqId) },
                        onSendTextMessage = { text -> viewModel.sendChatTextMessage(text) },
                        onSendAttachment = { uri, type, caption -> viewModel.sendChatAttachment(uri, type, caption) },
                        onEnsureMediaLocalPath = { msg -> viewModel.ensureMessageMediaLocalPath(msg) },
                        onOpenAttachmentExternal = { msg -> viewModel.openChatAttachmentExternal(msg) },
                        onDeleteMessage = { msgId -> viewModel.deleteChatMessage(msgId) },
                        onDeleteConversation = { chatId -> viewModel.deleteChatConversation(chatId) },
                        onStartCall = { callType -> viewModel.startCall(callType) },
                        onStartCallWithConversation = { conv, callType ->
                            viewModel.startCallWithConversation(conv, callType)
                        },
                        onCreateLiveRoom = { title, techId, techLabel, desc, mode ->
                            viewModel.createAndJoinLiveRoom(title, techId, techLabel, desc, mode)
                        },
                        onJoinLiveRoom = { room -> viewModel.joinLiveRoom(room) },
                        activeCallBanner = callBanner,
                        activeLiveBanner = liveBanner
                    )
                }

                AppDestination.LOGIN -> {
                    // Handled at root level outside the Scaffold
                }
            }
        }
    }
}

    // Celebration dialog when user earns a new badge
    newlyUnlockedBadge?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            language = currentLanguage,
            onDismiss = { viewModel.dismissBadgeCelebration() }
        )
    }

    // Login and Profile Dialog (Google Sign-In + Phone Number Authentication)
    if (showAuthDialog) {
        AuthLoginDialog(
            userAccount = userAccount,
            scoreInfo = scoreInfo,
            streakDays = streakInfo.streakDays,
            badgesCount = allBadges.count { it.isUnlocked },
            badges = allBadges,
            language = currentLanguage,
            isLoading = isAuthLoading,
            onGoogleSignIn = { extraPhoneNumber ->
                launchGoogleSignIn(extraPhoneNumber) {
                    showAuthDialog = false
                }
            },
            onPhoneSignIn = { name, phone ->
                viewModel.loginWithPhoneNumber(name, phone)
                showAuthDialog = false
            },
            onSyncLeaderboard = {
                viewModel.syncScoreToLeaderboard()
            },
            onUpdatePhoto = { newPhoto ->
                viewModel.updateUserProfilePhoto(newPhoto)
            },
            onOpenThemeSettings = {
                showAuthDialog = false
                viewModel.navigateTo(AppDestination.SETTINGS)
            },
            onSignOut = {
                viewModel.signOut()
                showAuthDialog = false
            },
            onDismiss = {
                showAuthDialog = false
            }
        )
    }

    // Initial Diagnostic Assessment Dialog (Only displayed after user has passed the Login screen)
    if (showInitialAssessmentDialog && destination != AppDestination.LOGIN) {
        InitialAssessmentDialog(
            language = currentLanguage,
            onComplete = { level, titlePt, titleEn, xp ->
                viewModel.completeInitialAssessment(level, titlePt, titleEn, xp)
                showInitialAssessmentDialog = false
            },
            onDismiss = {
                hasDeferredAssessmentInSession = true
                showInitialAssessmentDialog = false
            }
        )
    }

    // Profile Photo Dialog triggered from Settings screen
    if (showSettingsPhotoDialog) {
        ProfilePhotoDialog(
            currentPhotoUrl = userAccount?.photoUrl,
            displayName = userAccount?.displayName ?: "Dev",
            language = currentLanguage,
            onSavePhoto = { newPhoto ->
                viewModel.updateUserProfilePhoto(newPhoto)
                showSettingsPhotoDialog = false
            },
            onDismiss = {
                showSettingsPhotoDialog = false
            }
        )
    }

    if (showDailyLivesInfoDialog) {
        com.example.ui.components.DailyLivesInfoDialog(
            livesInfo = dailyLives,
            language = currentLanguage,
            availableXp = availableShopXp,
            availableCrystals = availableGameCrystals,
            onPurchaseLives = { pkg -> viewModel.purchaseLives(pkg) },
            onOpenLifeShop = { showLifePurchaseShopDialog = true },
            onOpen3DGame = {
                showDailyLivesInfoDialog = false
                viewModel.navigateTo(AppDestination.GAME_3D)
            },
            onDismiss = { showDailyLivesInfoDialog = false }
        )
    }

    if (showLifePurchaseShopDialog) {
        com.example.ui.components.LifePurchaseShopDialog(
            livesInfo = dailyLives,
            language = currentLanguage,
            availableXp = availableShopXp,
            availableCrystals = availableGameCrystals,
            proSubscriptionState = proSubscriptionState,
            billingStoreState = billingStoreState,
            externalConfirmedReceipt = lastLifePurchaseReceipt,
            onPurchaseLives = { pkg, method -> viewModel.purchaseLives(pkg, method) },
            onLaunchRealBrlPurchase = { pkg ->
                viewModel.launchLifePackBilling(hostActivity, pkg)
            },
            onRestorePurchases = { viewModel.restorePlayPurchases() },
            onRefreshBilling = { viewModel.refreshStudyMaterialsCatalog() },
            onClearExternalReceipt = { viewModel.clearLastLifePurchaseReceipt() },
            onOpenProSubscription = { showProSubscriptionDialog = true },
            onOpen3DGame = {
                showLifePurchaseShopDialog = false
                viewModel.navigateTo(AppDestination.GAME_3D)
            },
            onDismiss = {
                viewModel.clearLastLifePurchaseReceipt()
                viewModel.clearBillingStatusMessage()
                showLifePurchaseShopDialog = false
            }
        )
    }

    if (showProSubscriptionDialog) {
        ProSubscriptionDialog(
            subscriptionState = proSubscriptionState,
            dailyLives = dailyLives,
            language = currentLanguage,
            billingStoreState = billingStoreState,
            onSubscribeWithPlay = {
                viewModel.launchProSubscriptionBilling(hostActivity)
            },
            onRestorePurchases = { viewModel.restorePlayPurchases() },
            onRefreshBilling = { viewModel.refreshStudyMaterialsCatalog() },
            onManageInPlayStore = {
                val uri = Uri.parse(
                    "https://play.google.com/store/account/subscriptions?sku=${ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY}&package=${context.packageName}"
                )
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            },
            onDismiss = {
                viewModel.clearBillingStatusMessage()
                showProSubscriptionDialog = false
            }
        )
    }

    val currentInterstitialAd = activeModerateInterstitial
    if (currentInterstitialAd != null && !isProSubscriber) {
        ModerateInterstitialAdDialog(
            ad = currentInterstitialAd,
            language = currentLanguage,
            onUpgradeProClick = {
                viewModel.dismissModerateInterstitial()
                showProSubscriptionDialog = true
            },
            onDismiss = { viewModel.dismissModerateInterstitial() }
        )
    }

    if (isWeeklySummaryModalOpen) {
        WeeklyStudySummaryModal(
            uiState = weeklySummaryState,
            weeklyHistory = weeklyChallengeHistory,
            scoreInfo = scoreInfo,
            streakDays = streakInfo.streakDays,
            language = currentLanguage,
            onRetry = { viewModel.generateWeeklyStudySummary() },
            onRegenerate = { viewModel.generateWeeklyStudySummary() },
            onOpenTech = { tech -> viewModel.openTechDetail(tech) },
            onDismiss = { viewModel.dismissWeeklyStudySummaryModal() }
        )
    }

    levelUpEvent?.let { event ->
        com.example.ui.components.LevelUpCelebrationDialog(
            event = event,
            language = currentLanguage,
            onDismiss = { viewModel.dismissLevelUpEvent() }
        )
    }

    if (activeCallSession != null && !isCallMinimized) {
        ChatCallOverlay(
            callSession = activeCallSession,
            currentUserId = chatOwnerId,
            language = currentLanguage,
            webRtcCallManager = viewModel.webRtcCallManager,
            onAcceptCall = { viewModel.acceptIncomingCall() },
            onDeclineOrEndCall = { viewModel.declineOrEndCall() },
            onToggleMicMute = { viewModel.toggleCallMicMute() },
            onToggleSpeaker = { viewModel.toggleCallSpeaker() },
            onToggleCamera = { viewModel.toggleCallCamera() },
            onMinimize = { isCallMinimized = true }
        )
    }

    if (joinedLiveRoom != null && !isLiveMinimized) {
        LiveStudyRoomOverlay(
            liveRoom = joinedLiveRoom,
            currentUserId = chatOwnerId,
            language = currentLanguage,
            messages = liveRoomMessages,
            isMicMuted = isLiveMicMuted,
            isCameraEnabled = isLiveCameraEnabled,
            isSpeakerOn = isLiveSpeakerOn,
            isPeerConnected = isLivePeerConnected,
            elapsedSeconds = liveElapsedSeconds,
            webRtcLiveManager = viewModel.webRtcLiveManager,
            onSendMessage = { text, msgType -> viewModel.sendLiveRoomMessage(text, msgType) },
            onToggleMicMute = { viewModel.toggleLiveMicMute() },
            onToggleCamera = { viewModel.toggleLiveCamera() },
            onToggleSpeaker = { viewModel.toggleLiveSpeaker() },
            onLeaveOrEndLive = { viewModel.leaveOrEndLiveRoom() },
            onMinimize = { isLiveMinimized = true }
        )
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}


private data class TargetSubjectsDraft(
    val subjectIds: List<String>,
    val dailyGoalMinutes: Int,
    val learningTrack: String,
    val studyGoalNote: String
)
