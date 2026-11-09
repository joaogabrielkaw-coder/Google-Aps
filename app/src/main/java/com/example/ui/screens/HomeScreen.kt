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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timer
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
import com.example.model.Language
import com.example.model.StudySession
import com.example.model.Technology
import com.example.model.UserScoreInfo
import com.example.ui.AppDestination
import com.example.ui.UiStreakInfo
import com.example.ui.components.UserBadgesCompactRow
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RedAccent
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    language: Language,
    streakInfo: UiStreakInfo,
    scoreInfo: UserScoreInfo = UserScoreInfo(),
    badges: List<UserBadgeEntity> = emptyList(),
    sessions: List<StudySession>,
    routinesWithEvents: List<RoutineWithEvents>,
    selectedDateIso: String,
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
                isPt = isPt,
                onOpenAi = { onNavigate(AppDestination.AI_CHAT) }
            )
        }

        // 2. User Badges & Milestones Preview
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

        // 5. Technologies Tracks Grid (Python, SQL, HTML, CSS, Java, MySQL, PostgreSQL, MongoDB)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPt) "Trilhas de Aprendizado (8)" else "Learning Tracks (8)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isPt) "Ver detalhes" else "View tracks",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigate(AppDestination.TECH_HUB) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val allTechs = listOf(
                    Technology.PYTHON,
                    Technology.SQL,
                    Technology.HTML,
                    Technology.CSS,
                    Technology.JAVA,
                    Technology.MYSQL,
                    Technology.POSTGRESQL,
                    Technology.MONGODB
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(allTechs) { tech ->
                        TechChipCard(tech = tech, language = language, onClick = { onOpenTech(tech) })
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
    isPt: Boolean,
    onOpenAi: () -> Unit
) {
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isPt) "DevRoutine AI 🚀" else "DevRoutine AI 🚀",
                                color = Color(0xFFFFCCD5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E072E).copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = "Nvl ${scoreInfo.levelNumber}",
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
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

                // XP Progress bar to next level
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPt) "Progresso para o próximo nível" else "Progress to next level",
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
    val allTechnologies = listOf(
        Technology.PYTHON,
        Technology.SQL,
        Technology.HTML,
        Technology.CSS,
        Technology.JAVA,
        Technology.MYSQL,
        Technology.POSTGRESQL,
        Technology.MONGODB
    )

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

