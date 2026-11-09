package com.example

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.model.Language
import com.example.model.ThemePreference
import com.example.model.UserAccount
import com.example.ui.AppDestination
import com.example.ui.MainViewModel
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.components.AuthLoginDialog
import com.example.ui.components.BadgeDetailDialog
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TechDetailScreen
import com.example.ui.screens.TechHubScreen
import com.example.ui.theme.DevRoutineTheme
import com.example.util.configureAppCheck
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val themePreference by viewModel.themePreference.collectAsState()
            val language by viewModel.language.collectAsState()

            val isDarkTheme = when (themePreference) {
                ThemePreference.DARK -> true
                ThemePreference.LIGHT -> false
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            DevRoutineTheme(darkTheme = isDarkTheme) {
                MainAppContainer(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    currentLanguage = language,
                    currentTheme = themePreference
                )
            }
        }
    }
}

@Composable
fun MainAppContainer(
    viewModel: MainViewModel,
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

    var showAuthDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val serverClientId = try {
        context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
        "188401334450-5nqlrid83amqbc0p3unt2557rkovdb8i.apps.googleusercontent.com"
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

    // Back handling for sub-screens
    BackHandler(enabled = destination != AppDestination.HOME) {
        if (destination == AppDestination.TECH_DETAIL) {
            viewModel.navigateTo(AppDestination.TECH_HUB)
        } else {
            viewModel.navigateTo(AppDestination.HOME)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        topBar = {
            if (destination != AppDestination.TECH_DETAIL) {
                AppTopBar(
                    currentLanguage = currentLanguage,
                    currentTheme = currentTheme,
                    userAccount = userAccount,
                    onToggleLanguage = {
                        viewModel.setLanguage(if (currentLanguage == Language.PT) Language.EN else Language.PT)
                    },
                    onToggleTheme = {
                        val nextTheme = if (currentTheme == ThemePreference.DARK) ThemePreference.LIGHT else ThemePreference.DARK
                        viewModel.setThemePreference(nextTheme)
                    },
                    onOpenLeaderboard = {
                        viewModel.navigateTo(AppDestination.LEADERBOARD)
                    },
                    onOpenAuth = {
                        showAuthDialog = true
                    }
                )
            }
        },
        bottomBar = {
            AppBottomNav(
                currentDestination = destination,
                language = currentLanguage,
                onNavigate = { dest -> viewModel.navigateTo(dest) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
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
                        onSelectDate = { dateIso -> viewModel.setCalendarDate(dateIso) },
                        onToggleSession = { viewModel.toggleSession(it) },
                        onOpenTech = { tech -> viewModel.openTechDetail(tech) },
                        onNavigate = { dest -> viewModel.navigateTo(dest) }
                    )
                }

                AppDestination.TECH_HUB -> {
                    TechHubScreen(
                        language = currentLanguage,
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
                        onAskAi = { viewModel.promptAiAboutTech(selectedTech) }
                    )
                }

                AppDestination.PROGRESS -> {
                    ProgressScreen(
                        language = currentLanguage,
                        scoreInfo = scoreInfo,
                        badges = allBadges,
                        getTechScore = { tech -> viewModel.getTechScore(tech) },
                        onOpenTechDetail = { tech -> viewModel.openTechDetail(tech) }
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
                        onSendMessage = { prompt -> viewModel.sendAiUserMessage(prompt) },
                        onGenerateRoutine = { goal, techs, days, minutes ->
                            viewModel.generateRoutineWithAi(goal, techs, days, minutes)
                        },
                        onSaveRoutineToCalendar = { plan ->
                            viewModel.saveRoutinePlanToCalendar(plan)
                        }
                    )
                }

                AppDestination.LEADERBOARD -> {
                    LeaderboardScreen(
                        leaderboardEntries = leaderboardEntries,
                        currentUserAccount = userAccount,
                        language = currentLanguage,
                        onOpenAuthDialog = { showAuthDialog = true },
                        onSyncLeaderboard = { viewModel.syncScoreToLeaderboard() }
                    )
                }

                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        currentLanguage = currentLanguage,
                        currentTheme = currentTheme,
                        onSetLanguage = { viewModel.setLanguage(it) },
                        onSetTheme = { viewModel.setThemePreference(it) },
                        onResetStarterData = {},
                        onSendTestNotification = { viewModel.sendTestReminderNotification() }
                    )
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
            language = currentLanguage,
            isLoading = isAuthLoading,
            onGoogleSignIn = { extraPhoneNumber ->
                coroutineScope.launch {
                    try {
                        val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                            .build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()
                        val result = credentialManager.getCredential(request = request, context = context)
                        val credential = result.credential
                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            viewModel.loginWithGoogle(
                                idToken = googleIdTokenCredential.idToken,
                                displayName = googleIdTokenCredential.displayName,
                                email = googleIdTokenCredential.id,
                                photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                                extraPhone = extraPhoneNumber
                            )
                            showAuthDialog = false
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Google sign-in credential manager error: ${e.message}", e)
                    }
                }
            },
            onPhoneSignIn = { name, phone ->
                viewModel.loginWithPhoneNumber(name, phone)
                showAuthDialog = false
            },
            onSyncLeaderboard = {
                viewModel.syncScoreToLeaderboard()
            },
            onSignOut = {
                viewModel.signOut()
            },
            onDismiss = {
                showAuthDialog = false
            }
        )
    }
}
