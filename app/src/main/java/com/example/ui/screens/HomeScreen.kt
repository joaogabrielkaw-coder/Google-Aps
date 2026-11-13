package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RoutineWithEvents
import com.example.data.local.UserBadgeEntity
import com.example.data.repository.LanguageDailyProgress
import com.example.model.AiSuggestedTopic
import com.example.model.AiStudyTopicsCatalog
import com.example.model.Language
import com.example.model.StudySession
import com.example.model.Technology
import com.example.model.UserScoreInfo
import com.example.ui.AppDestination
import com.example.ui.DailyScreenTimeManager
import com.example.ui.DailyStudyGoalState
import com.example.ui.UiStreakInfo
import com.example.ui.components.DailyStudyProgressTrackerCard
import com.example.ui.components.LevelRoadmapDialog
import com.example.ui.components.UserBadgesCompactRow
import com.example.ui.theme.LocalDevRoutineColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

@Composable
fun HomeScreen(
    language: Language,
    streakInfo: UiStreakInfo,
    scoreInfo: UserScoreInfo = UserScoreInfo(),
    badges: List<UserBadgeEntity> = emptyList(),
    sessions: List<StudySession>,
    routinesWithEvents: List<RoutineWithEvents>,
    selectedDateIso: String,
    trackerSelectedTech: Technology = Technology.PYTHON,
    onSelectTrackerTech: (Technology) -> Unit = {},
    todayProgressMap: Map<String, LanguageDailyProgress> = emptyMap(),
    isLoggedIn: Boolean = false,
    todayScreenTimeMinutes: Int = 0,
    screenTimeMinutes: Int = 0,
    dailyStudyGoalState: DailyStudyGoalState = DailyStudyGoalState(),
    isInitialAssessmentCompleted: Boolean = true,
    onStartInitialAssessment: () -> Unit = {},
    getTechScore: (Technology) -> Int = { 0 },
    onPromptAiTopic: (Technology, String) -> Unit = { _, _ -> },
    dailyLives: com.example.ui.DailyLivesInfo = com.example.ui.DailyLivesInfo(),
    onOpenLivesInfo: () -> Unit = {},
    onOpenWeeklyAiSummary: () -> Unit = {},
    onSelectDate: (String) -> Unit,
    onToggleSession: (StudySession) -> Unit,
    onOpenTech: (Technology) -> Unit,
    onNavigate: (AppDestination) -> Unit
) {
    val isPt = language == Language.PT
    val currentSelectedDate = remember(selectedDateIso) {
        try { LocalDate.parse(selectedDateIso) } catch (e: Exception) { LocalDate.now() }
    }
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(currentSelectedDate)) }
    val locale = if (isPt) Locale("pt", "BR") else Locale.ENGLISH

    // Language filter state for daily routines and sessions
    var selectedLanguageFilter by remember { mutableStateOf<Technology?>(null) }
    var showLevelRoadmapDialog by remember { mutableStateOf(false) }

    if (showLevelRoadmapDialog) {
        LevelRoadmapDialog(
            scoreInfo = scoreInfo,
            language = language,
            onDismiss = { showLevelRoadmapDialog = false }
        )
    }

    // Sessions scheduled on the selected day
    val selectedDaySessions = sessions.filter { it.dateIso == selectedDateIso }

    // Filtered sessions for the selected day according to the chosen technology
    val filteredDaySessions = remember(selectedDaySessions, selectedLanguageFilter) {
        if (selectedLanguageFilter == null) selectedDaySessions
        else selectedDaySessions.filter { it.tech == selectedLanguageFilter }
    }

    // Filtered study routines retrieved from Room database according to the chosen technology
    val filteredRoutines = remember(routinesWithEvents, selectedLanguageFilter) {
        if (selectedLanguageFilter == null) routinesWithEvents
        else {
            val filterId = selectedLanguageFilter!!.id.lowercase()
            routinesWithEvents.filter { item ->
                item.routine.targetTechsCsv.lowercase().contains(filterId) ||
                item.routine.targetTechnologies.lowercase().contains(filterId) ||
                item.events.any { it.techId.equals(filterId, ignoreCase = true) }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_home"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Hero Header & Streak Banner with Score
        item {
            HeroHeaderCard(
                streakInfo = streakInfo,
                scoreInfo = scoreInfo,
                screenTimeMinutes = screenTimeMinutes,
                dailyStudyGoalState = dailyStudyGoalState,
                isInitialAssessmentCompleted = isInitialAssessmentCompleted,
                isPt = isPt,
                onOpenAi = { onNavigate(AppDestination.AI_CHAT) },
                onStartInitialAssessment = onStartInitialAssessment,
                onOpenLevelRoadmap = { showLevelRoadmapDialog = true }
            )
        }

        // 1b. Daily Lives Banner (25 vidas diárias)
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                com.example.ui.components.DailyLivesBannerCard(
                    livesInfo = dailyLives,
                    language = language,
                    onClick = onOpenLivesInfo
                )
            }
        }

        // 2. High-impact Daily Progress Dashboard Card (Progresso diário do usuário)
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                DailyProgressDashboardOverviewCard(
                    language = language,
                    todayProgressMap = todayProgressMap,
                    todayScreenTimeMinutes = todayScreenTimeMinutes,
                    dailyStudyGoalState = dailyStudyGoalState,
                    scoreInfo = scoreInfo,
                    onPracticeNow = { onOpenTech(trackerSelectedTech) },
                    onNavigateToAi = { onNavigate(AppDestination.AI_CHAT) },
                    onOpenWeeklyAiSummary = onOpenWeeklyAiSummary
                )
            }
        }

        // 2b. DevStudio Lab: 3D Game & Scene, SQL Database Studio, AI Voice Transcriber
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedAccent.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(AppDestination.GAME_3D) }
                        .testTag("card_home_launch_3d_game")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = RedAccent, modifier = Modifier.size(22.dp))
                        Text(
                            text = if (isPt) "Jogo & 3D" else "3D Game",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(AppDestination.SQL_STUDIO) }
                        .testTag("card_home_launch_sql_studio")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(22.dp))
                        Text(
                            text = if (isPt) "Banco SQL" else "SQL DB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00ACC1).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(AppDestination.AI_CHAT) }
                        .testTag("card_home_launch_voice_ai")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF00ACC1), modifier = Modifier.size(22.dp))
                        Text(
                            text = if (isPt) "Voz & IA" else "Voice AI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Brush.linearGradient(listOf(PurplePrimary.copy(alpha = 0.6f), RedAccent.copy(alpha = 0.6f)))
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(AppDestination.SETTINGS) }
                        .testTag("card_home_launch_theme_settings")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(22.dp))
                        Text(
                            text = if (isPt) "Cores" else "Themes",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. Tópicos de estudo sugeridos pela IA
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                AiStudySuggestionsSection(
                    language = language,
                    scoreInfo = scoreInfo,
                    onPromptAiTopic = onPromptAiTopic,
                    onOpenTech = onOpenTech,
                    onOpenAiChat = { onNavigate(AppDestination.AI_CHAT) }
                )
            }
        }

        // 4. Layout de grade para as tecnologias suportadas (8 tecnologias)
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                TechGridSection(
                    allTechs = Technology.entries,
                    getTechScore = getTechScore,
                    todayProgressMap = todayProgressMap,
                    language = language,
                    onOpenTech = onOpenTech,
                    onNavigateToHub = { onNavigate(AppDestination.TECH_HUB) }
                )
            }
        }

        // 5. User Badges & Milestones Preview
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                UserBadgesCompactRow(
                    badges = badges,
                    language = language,
                    onViewAll = { onNavigate(AppDestination.PROGRESS) }
                )
            }
        }

        // 2b. Leaderboard / Placar Banner
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(PurplePrimary, RedAccent)),
                        width = 1.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(AppDestination.LEADERBOARD) }
                        .testTag("banner_home_leaderboard")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(PurplePrimary, RedAccent))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isPt) "Placar de Desenvolvedores" else "Developer Leaderboard",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isPt) "Veja o ranking online, entre com Google/Telefone e salve seus pontos"
                                    else "See online ranking, sign in with Google/Phone & sync points",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2c. Firestore Daily Study Progress Tracker
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                DailyStudyProgressTrackerCard(
                    language = language,
                    selectedTech = trackerSelectedTech,
                    onSelectTech = onSelectTrackerTech,
                    todayProgressMap = todayProgressMap,
                    isLoggedIn = isLoggedIn,
                    onOpenTechDetail = onOpenTech
                )
            }
        }

        // 3. Interactive Calendar Component directly on Home Screen
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isPt) "Calendário de Estudos" else "Study Calendar",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = currentYearMonth.month.getDisplayName(TextStyle.FULL, locale)
                                    .replaceFirstChar { it.uppercase() } + " " + currentYearMonth.year,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mês anterior", modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próximo mês", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Home Calendar Month Grid
                HomeCalendarMonthGrid(
                    yearMonth = currentYearMonth,
                    selectedDate = currentSelectedDate,
                    sessions = sessions,
                    onSelectDate = { date ->
                        onSelectDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    },
                    locale = locale
                )
            }
        }

        // 3. Componente de Filtro com Chips de Linguagem
        item {
            StudyLanguageFilterRow(
                selectedTech = selectedLanguageFilter,
                onSelectTech = { tech -> selectedLanguageFilter = tech },
                language = language,
                sessionsCount = filteredDaySessions.size,
                routinesCount = filteredRoutines.size
            )
        }

        // 4. Sessions of Selected Day (Filtered)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heading = if (selectedLanguageFilter != null) {
                        (if (isPt) "Sessões de " else "Sessions for ") +
                                selectedLanguageFilter!!.displayName +
                                " • " + currentSelectedDate.format(DateTimeFormatter.ofPattern("dd/MM"))
                    } else {
                        (if (isPt) "Sessões em " else "Sessions on ") +
                                currentSelectedDate.format(DateTimeFormatter.ofPattern("dd/MM"))
                    }
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${filteredDaySessions.count { it.isCompleted }}/${filteredDaySessions.size} " +
                                if (isPt) "concluídas" else "completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredDaySessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val emptyMsg = if (selectedLanguageFilter != null) {
                                if (isPt) "Nenhuma sessão de ${selectedLanguageFilter!!.displayName} para hoje."
                                else "No ${selectedLanguageFilter!!.displayName} sessions for today."
                            } else {
                                if (isPt) "Nenhuma sessão agendada para este dia."
                                else "No study sessions on this day."
                            }
                            Text(
                                text = emptyMsg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { onNavigate(AppDestination.CALENDAR) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("button_open_calendar_home")
                            ) {
                                Text(if (isPt) "Agendar" else "Schedule", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    filteredDaySessions.forEach { session ->
                        TodaySessionItem(
                            session = session,
                            language = language,
                            onToggle = { onToggleSession(session) },
                            onClick = { onOpenTech(session.tech) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // 5. Lista de Rotinas de Estudo Recuperadas do Banco de Dados Room (Filtered)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(RedAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = RedAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        val routineTitle = if (selectedLanguageFilter != null) {
                            if (isPt) "Rotinas de ${selectedLanguageFilter!!.displayName}"
                            else "${selectedLanguageFilter!!.displayName} Routines"
                        } else {
                            if (isPt) "Rotinas no Banco de Dados" else "Routines in Database"
                        }
                        Text(
                            text = routineTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedButton(
                        onClick = { onNavigate(AppDestination.AI_CHAT) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("button_new_routine_ai")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = RedAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPt) "+ Nova Rotina" else "+ New Routine", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredRoutines.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RedAccent, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            val emptyRoutineMsg = if (selectedLanguageFilter != null) {
                                if (isPt) "Nenhuma rotina contendo ${selectedLanguageFilter!!.displayName} salva no banco."
                                else "No routines containing ${selectedLanguageFilter!!.displayName} in the database."
                            } else {
                                if (isPt) "Nenhuma rotina salva ainda no banco de dados."
                                else "No routines saved in the database yet."
                            }
                            Text(
                                text = emptyRoutineMsg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onNavigate(AppDestination.AI_CHAT) },
                                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                val buttonLabel = if (selectedLanguageFilter != null) {
                                    if (isPt) "Criar Rotina de ${selectedLanguageFilter!!.displayName} com IA"
                                    else "Create ${selectedLanguageFilter!!.displayName} Routine with AI"
                                } else {
                                    if (isPt) "Gerar Rotina com IA" else "Generate Routine with AI"
                                }
                                Text(buttonLabel)
                            }
                        }
                    }
                } else {
                    filteredRoutines.forEach { routineWithEvents ->
                        DatabaseRoutineCard(
                            routineWithEvents = routineWithEvents,
                            language = language,
                            onOpenCalendar = { onNavigate(AppDestination.CALENDAR) },
                            onPracticeAi = { onNavigate(AppDestination.AI_CHAT) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

    }
}

/**
 * Card que exibe a Rotina de Estudos recuperada do Room Database (StudyRoutineEntity)
 * com contagem de eventos, barra de progresso e tecnologias envolvidas.
 */
@Composable
fun DatabaseRoutineCard(
    routineWithEvents: RoutineWithEvents,
    language: Language,
    onOpenCalendar: () -> Unit,
    onPracticeAi: () -> Unit
) {
    val routine = routineWithEvents.routine
    val events = routineWithEvents.events
    val isPt = language == Language.PT

    val totalEvents = events.size
    val completedEvents = events.count { it.isCompleted }
    val progressFraction = if (totalEvents > 0) completedEvents.toFloat() / totalEvents.toFloat() else 0f
    val progressPercent = (progressFraction * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(PurplePrimary.copy(alpha = 0.6f), RedAccent.copy(alpha = 0.6f))
            ),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Active badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPt) routine.titlePt else routine.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isPt) routine.descriptionPt else routine.descriptionEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RedAccent.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$progressPercent%",
                        color = RedAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = progressFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = RedAccent,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row: Duration & Target Techs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${routine.totalDays} " + (if (isPt) "dias" else "days") +
                                " • ${routine.dailyCommitmentMinutes} min/" + (if (isPt) "dia" else "day"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$completedEvents de $totalEvents " + if (isPt) "sessões feitas" else "sessions done",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenCalendar,
                    modifier = Modifier.weight(1f).testTag("button_view_routine_calendar"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPt) "Ver no Calendário" else "View in Calendar", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onPracticeAi,
                    modifier = Modifier.weight(1f).testTag("button_practice_ai_routine"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = RedAccent)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPt) "Tirar Dúvidas com IA" else "Ask AI Tutor", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun EmptyRoutineCard(isPt: Boolean, onGenerate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RedAccent, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPt) "Nenhuma rotina salva ainda no banco de dados."
                else "No routines saved in the database yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onGenerate,
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(if (isPt) "Gerar Rotina com IA" else "Generate Routine with AI")
            }
        }
    }
}

/**
 * Grid de calendário moderno e interativo exibido na Home screen
 */
@Composable
fun HomeCalendarMonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    sessions: List<StudySession>,
    onSelectDate: (LocalDate) -> Unit,
    locale: Locale
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
    val daysOfWeek = if (locale.language == "pt") listOf("D", "S", "T", "Q", "Q", "S", "S")
    else listOf("S", "M", "T", "W", "T", "F", "S")

    val datesWithSessions = remember(sessions) {
        sessions.groupBy { it.dateIso }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Days of Week Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeek.forEach { dayName ->
                    Text(
                        text = dayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Calendar days matrix
            var currentDay = 1
            for (week in 0..5) {
                if (currentDay > daysInMonth) break
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    for (dayOfWeek in 0..6) {
                        if (week == 0 && dayOfWeek < firstDayOfWeek || currentDay > daysInMonth) {
                            Box(modifier = Modifier.size(32.dp))
                        } else {
                            val thisDate = yearMonth.atDay(currentDay)
                            val isSelected = thisDate == selectedDate
                            val isToday = thisDate == LocalDate.now()
                            val dateIso = thisDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val hasSessions = datesWithSessions.containsKey(dateIso)
                            val dayNumber = currentDay

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onSelectDate(thisDate) }
                                    .testTag("home_calendar_day_$dayNumber"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNumber",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasSessions) {
                                        Box(
                                            modifier = Modifier
                                                .size(3.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else RedAccent)
                                        )
                                    }
                                }
                            }
                            currentDay++
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

@Composable
fun HeroHeaderCard(
    streakInfo: UiStreakInfo,
    scoreInfo: UserScoreInfo = UserScoreInfo(),
    screenTimeMinutes: Int = 0,
    dailyStudyGoalState: DailyStudyGoalState = DailyStudyGoalState(),
    isInitialAssessmentCompleted: Boolean = true,
    isPt: Boolean,
    onOpenAi: () -> Unit,
    onStartInitialAssessment: () -> Unit = {},
    onOpenLevelRoadmap: () -> Unit = {}
) {
    val rankLabel = if (isPt) scoreInfo.rankBadgePt else scoreInfo.rankBadgeEn
    val remainingXp = (scoreInfo.nextLevelTarget - scoreInfo.totalScore).coerceAtLeast(0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF38125C),
                            Color(0xFF5A1448),
                            Color(0xFF881B2B)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DevRoutine AI 🚀",
                                color = Color(0xFFFFCCD5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E072E).copy(alpha = 0.85f)
                            ) {
                                Text(
                                    text = "Nvl ${scoreInfo.levelNumber}",
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFD54F).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = rankLabel,
                                    color = Color(0xFFFFE57F),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPt) scoreInfo.levelTitlePt else scoreInfo.levelTitleEn,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E072E).copy(alpha = 0.7f),
                        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Ofensiva",
                                tint = RedAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${streakInfo.streakDays} " + if (isPt) "dias" else "days",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // XP Progress bar to next level + Level Roadmap trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (scoreInfo.isMaxLevel) {
                            if (isPt) "Nível Máximo Alcançado!" else "Max Level Reached!"
                        } else {
                            if (isPt) "Faltam $remainingXp XP para o Nvl ${scoreInfo.levelNumber + 1}"
                            else "$remainingXp XP left for Lv ${scoreInfo.levelNumber + 1}"
                        },
                        color = Color(0xFFE2E0EC),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${scoreInfo.totalScore} / ${scoreInfo.nextLevelTarget} XP",
                        color = Color(0xFFFFD54F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = scoreInfo.progressFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFFD54F),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E072E).copy(alpha = 0.55f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenLevelRoadmap() }
                        .testTag("btn_home_level_roadmap")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPt) "❤️ Subir de nível restaura 25 vidas"
                            else "❤️ Leveling up restores all 25 lives",
                            fontSize = 11.sp,
                            color = Color(0xFFFFD6E0),
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPt) "Ver Níveis" else "View Ranks",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = if (isPt) "Ver sistema de níveis" else "View level roadmap",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cronômetro ao vivo de tempo de estudo ativo (com pausa automática por inatividade)
                val isTimerRunning = dailyStudyGoalState.isTimerRunning
                val timerColor = if (isTimerRunning) Color(0xFF80FFAB) else Color(0xFFFFD54F)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E072E).copy(alpha = 0.65f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hero_live_study_timer")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = timerColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPt) {
                                    "Hoje: ${dailyStudyGoalState.formattedActiveTime} • Total: ${maxOf(screenTimeMinutes, dailyStudyGoalState.totalActiveMinutes)}m"
                                } else {
                                    "Today: ${dailyStudyGoalState.formattedActiveTime} • Total: ${maxOf(screenTimeMinutes, dailyStudyGoalState.totalActiveMinutes)}m"
                                },
                                fontSize = 11.sp,
                                color = timerColor,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = timerColor.copy(alpha = 0.16f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(timerColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTimerRunning) {
                                        if (isPt) "Ativo" else "Live"
                                    } else {
                                        if (isPt) "Em pausa" else "Idle"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = timerColor
                                )
                            }
                        }
                    }
                }

                // CTA para diagnóstico inicial quando ainda não realizado
                if (!isInitialAssessmentCompleted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2E124D),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStartInitialAssessment() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isPt) "🎯 Diagnóstico Inicial Dev (Opcional)" else "🎯 Initial Dev Diagnostic (Optional)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (isPt) "✨ Não é obrigatório • Toque aqui quando desejar definir seu nível e ganhar título + medalha!"
                                        else "✨ Not mandatory • Tap anytime to calibrate your level and earn title + badge!",
                                        fontSize = 10.sp,
                                        color = Color(0xFFE2E0EC)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatPill(
                        label = if (isPt) "XP Acumulado" else "Total XP",
                        value = "${scoreInfo.totalScore}"
                    )
                    StatPill(
                        label = if (isPt) "Atividades Feitas" else "Activities Done",
                        value = "${scoreInfo.completedActivitiesCount}"
                    )
                    StatPill(
                        label = if (isPt) "Quizzes Acertados" else "Quizzes Solved",
                        value = "${scoreInfo.correctQuizCount}"
                    )
                }
            }
        }
    }
}

@Composable
fun StatPill(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF160624).copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, color = Color(0xFFDCC8EC), fontSize = 10.sp)
    }
}

@Composable
fun TodaySessionItem(
    session: StudySession,
    language: Language,
    onToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("session_item_${session.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (session.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(session.tech.accentColor.copy(alpha = 0.5f), Color.Transparent)
            ),
            width = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = session.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = RedAccent,
                    uncheckedColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("checkbox_session_${session.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = session.tech.accentColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = session.tech.displayName,
                            color = session.tech.accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${session.timeFormatted} • ${session.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = session.title(language),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (session.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun TechChipCard(
    tech: Technology,
    language: Language,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick)
            .testTag("tech_card_${tech.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tech.accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tech.icon,
                    contentDescription = tech.displayName,
                    tint = tech.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tech.displayName,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = if (language == Language.PT) tech.categoryPt else tech.categoryEn,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * Componente moderno de filtro por chips de linguagem/tecnologia
 * Permite ao usuário filtrar rotinas de estudo e sessões do dia por linguagens específicas.
 */
@Composable
fun StudyLanguageFilterRow(
    selectedTech: Technology?,
    onSelectTech: (Technology?) -> Unit,
    language: Language,
    sessionsCount: Int,
    routinesCount: Int,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val allTechnologies = Technology.entries

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("study_language_filter_row")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PurplePrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isPt) "Filtrar por Linguagem" else "Filter by Language",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isPt) "Foque nas rotinas das linguagens que deseja praticar hoje"
                        else "Focus on routines for specific languages to practice today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (selectedTech != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RedAccent.copy(alpha = 0.15f),
                    modifier = Modifier
                        .clickable { onSelectTech(null) }
                        .testTag("chip_filter_clear")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar filtro",
                            tint = RedAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPt) "Limpar" else "Clear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedAccent
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal chips row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Chip "Todas"
            item {
                val isAllSelected = selectedTech == null
                FilterChip(
                    selected = isAllSelected,
                    onClick = { onSelectTech(null) },
                    label = {
                        Text(
                            text = if (isPt) "Todas" else "All",
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("filter_chip_all")
                )
            }

            // Tech Chips
            items(allTechnologies) { tech ->
                val isSelected = selectedTech == tech
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectTech(if (isSelected) null else tech) },
                    label = {
                        Text(
                            text = tech.displayName,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = tech.icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else tech.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = tech.accentColor,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.Transparent
                    ),
                    modifier = Modifier.testTag("filter_chip_${tech.id}")
                )
            }
        }

        // Active filter status banner
        if (selectedTech != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = selectedTech.accentColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, selectedTech.accentColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().testTag("active_filter_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(selectedTech.accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = (if (isPt) "Exibindo rotinas de " else "Showing routines for ") + selectedTech.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = selectedTech.accentColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isPt) "$sessionsCount sessões • $routinesCount rotinas"
                            else "$sessionsCount sessions • $routinesCount routines",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = selectedTech.accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dashboard Diário com telemetria de progresso, meta de minutos diários,
 * contagem de desafios/quizzes e resumo de tecnologias estudadas no dia.
 */
@Composable
fun DailyProgressDashboardOverviewCard(
    language: Language,
    todayProgressMap: Map<String, LanguageDailyProgress>,
    todayScreenTimeMinutes: Int = 0,
    dailyStudyGoalState: DailyStudyGoalState = DailyStudyGoalState(),
    scoreInfo: UserScoreInfo,
    onPracticeNow: () -> Unit = {},
    onNavigateToAi: () -> Unit,
    onOpenWeeklyAiSummary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val languageMinutesToday = todayProgressMap.values.sumOf { it.minutesStudied }
    val todaySecondsFromMinutes = maxOf(todayScreenTimeMinutes, languageMinutesToday) * 60
    val effectiveActiveSecondsToday = maxOf(dailyStudyGoalState.activeSecondsToday, todaySecondsFromMinutes)
    val todayTotalMinutes = effectiveActiveSecondsToday / 60
    val todayTotalChallenges = todayProgressMap.values.sumOf { it.completedChallengesCount }
    val todayTotalQuizzes = todayProgressMap.values.sumOf { it.completedQuizCount }
    val todayTotalTasks = todayTotalChallenges + todayTotalQuizzes
    val todayTotalXp = todayProgressMap.values.sumOf { it.xpEarned }

    val dailyGoalMinutes = DailyScreenTimeManager.DAILY_GOAL_MINUTES
    val dailyGoalSeconds = DailyScreenTimeManager.DAILY_GOAL_SECONDS
    val progressFraction = (effectiveActiveSecondsToday.toFloat() / dailyGoalSeconds).coerceIn(0f, 1f)
    val progressPercent = ((effectiveActiveSecondsToday.toFloat() / dailyGoalSeconds) * 100f).toInt().coerceIn(0, 100)
    val remainingSeconds = (dailyGoalSeconds - effectiveActiveSecondsToday).coerceAtLeast(0)
    val formattedActive = DailyScreenTimeManager.formatDurationMmSs(effectiveActiveSecondsToday)
    val formattedGoal = DailyScreenTimeManager.formatDurationMmSs(dailyGoalSeconds)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_daily_progress_dashboard"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(PurplePrimary.copy(alpha = 0.7f), RedAccent.copy(alpha = 0.7f))
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PurplePrimary, RedAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isPt) "Progresso Diário de Estudos" else "Daily Study Progress",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isPt) "Meta: $dailyGoalMinutes min • Calculada pelo cronômetro ativo"
                            else "Goal: $dailyGoalMinutes min • Auto-calculated from active timer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (progressPercent >= 100) Color(0xFF4CAF50).copy(alpha = 0.2f) else PurplePrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (progressPercent >= 100) (if (isPt) "🎉 Meta Atingida!" else "🎉 Goal Reached!") else "$progressPercent% " + if (isPt) "concluído" else "completed",
                        color = if (progressPercent >= 100) Color(0xFF4CAF50) else PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Live Activity Status Banner + Auto-Calculated Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val activeBadgeColor = if (dailyStudyGoalState.isTimerRunning) Color(0xFF4CAF50) else Color(0xFFFFB300)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = activeBadgeColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, activeBadgeColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("banner_daily_timer_status")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(activeBadgeColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dailyStudyGoalState.isTimerRunning) {
                                    if (isPt) "Cronômetro de atividade em execução" else "Activity timer running live"
                                } else {
                                    if (isPt) "Pausado por inatividade (interaja para retomar)" else "Paused on inactivity (interact to resume)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = activeBadgeColor
                            )
                        }
                        Text(
                            text = "$formattedActive / $formattedGoal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (progressPercent >= 100) Color(0xFF4CAF50) else PurplePrimary,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isPt) "$todayTotalMinutes de $dailyGoalMinutes min ativos" else "$todayTotalMinutes of $dailyGoalMinutes active min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (remainingSeconds <= 0) {
                            if (isPt) "Excelente consistência! 🔥" else "Great consistency! 🔥"
                        } else {
                            val remMin = remainingSeconds / 60
                            val remSec = remainingSeconds % 60
                            if (isPt) "Faltam ${remMin}m ${remSec}s hoje" else "${remMin}m ${remSec}s left today"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 3 Stat metric tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                    iconTint = Color(0xFF2196F3),
                    title = if (isPt) "Tempo Ativo" else "Active Time",
                    value = formattedActive,
                    subtext = if (dailyStudyGoalState.isTimerRunning) {
                        if (isPt) "ao vivo agora" else "live tracking"
                    } else {
                        if (isPt) "em pausa" else "paused"
                    }
                )
                StatMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    iconTint = Color(0xFF4CAF50),
                    title = if (isPt) "Atividades" else "Activities",
                    value = "$todayTotalTasks",
                    subtext = if (isPt) "desafios/quiz" else "challenges/quiz"
                )
                StatMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFFFB300),
                    title = if (isPt) "XP Hoje" else "XP Today",
                    value = "+$todayTotalXp",
                    subtext = if (isPt) "pontos" else "points"
                )
            }

            // Active studied technologies breakdown pills or quick session suggestion
            val studiedTechs = todayProgressMap.values.filter { it.minutesStudied > 0 || it.xpEarned > 0 }
            if (studiedTechs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isPt) "Linguagens praticadas hoje:" else "Practiced languages today:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        studiedTechs.take(4).forEach { item ->
                            val tech = Technology.entries.firstOrNull { it.id == item.techId }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = (tech?.accentColor ?: PurplePrimary).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    (tech?.accentColor ?: PurplePrimary).copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(tech?.accentColor ?: PurplePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${item.techName}: ${item.minutesStudied}m • +${item.xpEarned}XP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Automatic screen-time notice + direct link to real challenges (costing lives)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPt) "Meta contabilizada automaticamente pelo tempo de uso no app."
                            else "Daily goal is tracked automatically from real app usage time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        Button(
                            onClick = onPracticeNow,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text(if (isPt) "Praticar" else "Practice", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            // Quick 1-tap Weekly AI Summary Modal trigger
            OutlinedButton(
                onClick = onOpenWeeklyAiSummary,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.55f)),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_home_weekly_ai_summary")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPt) "✨ Resumo Semanal com IA (Feedback de 7 dias)"
                    else "✨ AI Weekly Study Summary (7-Day Feedback)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
            }
        }
    }
}

@Composable
private fun StatMetricCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    subtext: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Seção de Tópicos de Estudo Sugeridos pela IA com filtros e botões de ação para prática guiada.
 */
@Composable
fun AiStudySuggestionsSection(
    language: Language,
    scoreInfo: UserScoreInfo,
    onPromptAiTopic: (Technology, String) -> Unit,
    onOpenTech: (Technology) -> Unit,
    onOpenAiChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val filterOptions = listOf(
        Pair("ALL", if (isPt) "Todos" else "All"),
        Pair("JS_TS", "JavaScript / TS"),
        Pair("KOTLIN", "Kotlin"),
        Pair("GO", "Go"),
        Pair("PYTHON", "Python"),
        Pair("DATA", if (isPt) "SQL & Bancos" else "SQL & DB"),
        Pair("FRONTEND", "Frontend"),
        Pair("JAVA", "Java")
    )

    val allSuggestions = AiStudyTopicsCatalog.DEFAULT_SUGGESTIONS
    val filteredSuggestions = remember(selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "JS_TS" -> allSuggestions.filter { it.tech in listOf(Technology.JAVASCRIPT, Technology.TYPESCRIPT) }
            "KOTLIN" -> allSuggestions.filter { it.tech == Technology.KOTLIN }
            "GO" -> allSuggestions.filter { it.tech == Technology.GO }
            "PYTHON" -> allSuggestions.filter { it.tech == Technology.PYTHON }
            "DATA" -> allSuggestions.filter {
                it.tech in listOf(Technology.SQL, Technology.MYSQL, Technology.POSTGRESQL, Technology.MONGODB)
            }
            "FRONTEND" -> allSuggestions.filter {
                it.tech in listOf(Technology.HTML, Technology.CSS, Technology.JAVASCRIPT, Technology.TYPESCRIPT)
            }
            "JAVA" -> allSuggestions.filter { it.tech == Technology.JAVA }
            else -> allSuggestions
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("section_ai_study_suggestions"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PurplePrimary, RedAccent))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isPt) "Tópicos Sugeridos pela IA" else "AI Study Suggestions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isPt) "Recomendações para nível ${scoreInfo.levelTitlePt}"
                        else "Recommended for ${scoreInfo.levelTitleEn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PurplePrimary.copy(alpha = 0.15f),
                modifier = Modifier.clickable { onOpenAiChat() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DevTutor",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(filterOptions) { (key, label) ->
                val isSelected = selectedCategoryFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = key },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_chip_ai_$key")
                )
            }
        }

        // Suggestions Cards
        filteredSuggestions.take(4).forEach { suggestion ->
            AiSuggestedTopicCard(
                topic = suggestion,
                language = language,
                onPracticeWithAi = { onPromptAiTopic(suggestion.tech, suggestion.title(language)) },
                onOpenTechTrack = { onOpenTech(suggestion.tech) }
            )
        }
    }
}

/**
 * Card individual de tópico recomendado pela IA com tags pedagógicas e botões de prática.
 */
@Composable
fun AiSuggestedTopicCard(
    topic: AiSuggestedTopic,
    language: Language,
    onPracticeWithAi: () -> Unit,
    onOpenTechTrack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val tech = topic.tech

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_ai_topic_${topic.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(tech.accentColor.copy(alpha = 0.5f), PurplePrimary.copy(alpha = 0.3f))
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Badges row: Tech chip + difficulty + XP + duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Tech Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tech.accentColor.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tech.icon,
                                contentDescription = null,
                                tint = tech.accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tech.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tech.accentColor
                            )
                        }
                    }

                    // Difficulty badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = topic.difficulty(language),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // XP and duration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "~${topic.estimatedMinutes} min",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFB300).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "+${topic.xpReward} XP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Title & Explanation
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = topic.title(language),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = topic.explanation(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            // Tags row
            if (topic.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    topic.tags.forEach { tag ->
                        Text(
                            text = "#$tag",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action Buttons: Praticar com AI & Ver Trilha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPracticeWithAi,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("btn_practice_ai_${topic.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPt) "Praticar com AI" else "Practice with AI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenTechTrack,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_open_track_${topic.id}"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPt) "Ver Desafios" else "Challenges",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Seção em Grade (Layout de Grade de 2 Colunas) para as 8 tecnologias suportadas:
 * Python, SQL, HTML, CSS, Java, MySQL, PostgreSQL, MongoDB.
 */
@Composable
fun TechGridSection(
    allTechs: List<Technology>,
    getTechScore: (Technology) -> Int,
    todayProgressMap: Map<String, LanguageDailyProgress>,
    language: Language,
    onOpenTech: (Technology) -> Unit,
    onNavigateToHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("section_technologies_grid"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isPt) "Trilhas de Aprendizado (${allTechs.size})" else "Learning Tracks (${allTechs.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isPt) "Grade de tecnologias suportadas" else "Supported technologies grid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = if (isPt) "Ver todas" else "View all",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateToHub() }
            )
        }

        // 2-Column Grid Layout for all technologies
        allTechs.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TechGridCard(
                    tech = pair[0],
                    score = getTechScore(pair[0]),
                    todayMinutes = todayProgressMap[pair[0].id]?.minutesStudied ?: 0,
                    language = language,
                    onClick = { onOpenTech(pair[0]) },
                    modifier = Modifier.weight(1f)
                )

                if (pair.size > 1) {
                    TechGridCard(
                        tech = pair[1],
                        score = getTechScore(pair[1]),
                        todayMinutes = todayProgressMap[pair[1].id]?.minutesStudied ?: 0,
                        language = language,
                        onClick = { onOpenTech(pair[1]) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

/**
 * Card para cada tecnologia na Grade com indicador de nível/XP, minutos estudados hoje e categoria.
 */
@Composable
fun TechGridCard(
    tech: Technology,
    score: Int,
    todayMinutes: Int,
    language: Language,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val categoryText = if (isPt) tech.categoryPt else tech.categoryEn
    val targetNextXp = when {
        score < 50 -> 50
        score < 100 -> 100
        score < 250 -> 250
        score < 500 -> 500
        else -> 1000
    }
    val scoreProgress = (score.toFloat() / targetNextXp).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("tech_grid_card_${tech.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(tech.accentColor.copy(alpha = 0.5f), tech.accentColor.copy(alpha = 0.15f))
            ),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with tech icon and today minutes pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(tech.accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tech.icon,
                        contentDescription = tech.displayName,
                        tint = tech.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (todayMinutes > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${todayMinutes}m " + if (isPt) "hoje" else "today",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Tech Name & Category
            Column {
                Text(
                    text = tech.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = categoryText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Mini progress indicator for technology score / level
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                LinearProgressIndicator(
                    progress = { scoreProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = tech.accentColor,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$score XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tech.accentColor
                    )
                    Text(
                        text = "/$targetNextXp",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


