package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.StudySession
import com.example.model.Technology
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
fun CalendarScreen(
    language: Language,
    sessions: List<StudySession>,
    selectedDateIso: String,
    filterTech: Technology?,
    onSelectDate: (String) -> Unit,
    onSetFilterTech: (Technology?) -> Unit,
    onToggleSession: (StudySession) -> Unit,
    onDeleteSession: (StudySession) -> Unit,
    onAddSession: (StudySession) -> Unit,
    onOpenAiPlanner: () -> Unit
) {
    val isPt = language == Language.PT
    val currentSelectedDate = remember(selectedDateIso) {
        try { LocalDate.parse(selectedDateIso) } catch (e: Exception) { LocalDate.now() }
    }

    var currentYearMonth by remember { mutableStateOf(YearMonth.from(currentSelectedDate)) }
    var showAddDialog by remember { mutableStateOf(false) }

    val locale = if (isPt) Locale("pt", "BR") else Locale.ENGLISH

    // Filtered sessions for the selected date
    val daySessions = sessions.filter { session ->
        session.dateIso == selectedDateIso && (filterTech == null || session.tech == filterTech)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RedAccent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_session")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Sessão")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("screen_calendar"),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Calendar Month Navigation Header
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPt) "Calendário de Estudos" else "Study Calendar",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = currentYearMonth.month.getDisplayName(TextStyle.FULL, locale)
                                    .replaceFirstChar { it.uppercase() } + " " + currentYearMonth.year,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row {
                            IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mês anterior")
                            }
                            IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Próximo mês")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Month Grid View
                    CalendarMonthGrid(
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

            // AI Routine generation quick link
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onOpenAiPlanner() }
                        .testTag("button_plan_with_ai"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RedAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = RedAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPt) "Agendar Nova Rotina com a IA" else "Schedule New Routine with AI",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isPt) "A IA cria um cronograma personalizado de 1 a 4 semanas."
                                else "AI generates a customized 1-4 week study roadmap.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Technology Filter Chips
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = if (isPt) "Filtrar por Tecnologia" else "Filter by Technology",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            val isAll = filterTech == null
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isAll) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onSetFilterTech(null) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isPt) "Todas" else "All",
                                    color = if (isAll) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = if (isAll) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        items(Technology.entries) { tech ->
                            val isSelected = filterTech == tech
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) tech.accentColor
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { onSetFilterTech(if (isSelected) null else tech) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tech.displayName,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Selected Day Header & Sessions List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (if (isPt) "Estudos em: " else "Studies on: ") +
                                currentSelectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${daySessions.size} " + if (isPt) "sessões" else "sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (daySessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isPt) "Nenhuma sessão de estudo neste dia."
                                else "No study sessions scheduled for this day.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("button_add_session_empty")
                            ) {
                                Text(if (isPt) "Agendar Estudo" else "Schedule Study")
                            }
                        }
                    }
                }
            } else {
                items(daySessions) { session ->
                    CalendarSessionCard(
                        session = session,
                        language = language,
                        onToggle = { onToggleSession(session) },
                        onDelete = { onDeleteSession(session) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddStudySessionDialog(
            language = language,
            defaultDate = currentSelectedDate,
            onDismiss = { showAddDialog = false },
            onConfirm = { newSession ->
                onAddSession(newSession)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CalendarMonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    sessions: List<StudySession>,
    onSelectDate: (LocalDate) -> Unit,
    locale: Locale
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7 // 0 = Sun, 1 = Mon ...
    val daysOfWeek = if (locale.language == "pt") listOf("D", "S", "T", "Q", "Q", "S", "S")
    else listOf("S", "M", "T", "W", "T", "F", "S")

    // Collect dates that have sessions
    val datesWithSessions = remember(sessions) {
        sessions.groupBy { it.dateIso }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Days of week header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                daysOfWeek.forEach { dayName ->
                    Text(
                        text = dayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Month days grid (up to 6 rows)
            var currentDay = 1
            for (week in 0..5) {
                if (currentDay > daysInMonth) break
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    for (dayOfWeek in 0..6) {
                        if (week == 0 && dayOfWeek < firstDayOfWeek || currentDay > daysInMonth) {
                            Box(modifier = Modifier.size(36.dp))
                        } else {
                            val thisDate = yearMonth.atDay(currentDay)
                            val isSelected = thisDate == selectedDate
                            val isToday = thisDate == LocalDate.now()
                            val dateIso = thisDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            val hasSessions = datesWithSessions.containsKey(dateIso)
                            val dayNumber = currentDay

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onSelectDate(thisDate) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNumber",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasSessions) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
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
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun CalendarSessionCard(
    session: StudySession,
    language: Language,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("calendar_session_${session.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(session.tech.accentColor.copy(alpha = 0.5f)),
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
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(session.tech.accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = session.tech.displayName,
                            color = session.tech.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${session.timeFormatted} • ${session.durationMinutes} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (session.aiGenerated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "IA",
                            tint = RedAccent,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = session.title(language),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (session.topic(language).isNotBlank()) {
                    Text(
                        text = session.topic(language),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (session.notes(language).isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "📝 " + session.notes(language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Excluir Sessão",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddStudySessionDialog(
    language: Language,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (StudySession) -> Unit
) {
    val isPt = language == Language.PT
    var selectedTech by remember { mutableStateOf(Technology.PYTHON) }
    var title by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableIntStateOf(45) }
    var timeFormatted by remember { mutableStateOf("19:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isPt) "Agendar Nova Sessão" else "Schedule New Session",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (isPt) "Tecnologia:" else "Technology:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Technology.entries) { tech ->
                            val isSelected = selectedTech == tech
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) tech.accentColor
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedTech = tech }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tech.displayName,
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (isPt) "Título da Sessão" else "Session Title") },
                        placeholder = { Text(if (isPt) "Ex: Revisar Joins no SQL" else "Ex: Review SQL Joins") },
                        modifier = Modifier.fillMaxWidth().testTag("input_session_title"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text(if (isPt) "Tópico Principal" else "Core Topic") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isPt) "Anotações / Exercícios" else "Notes / Practice Tasks") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = timeFormatted,
                            onValueChange = { timeFormatted = it },
                            label = { Text(if (isPt) "Horário" else "Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = "$durationMinutes min",
                            onValueChange = {
                                it.filter { ch -> ch.isDigit() }.toIntOrNull()?.let { d -> durationMinutes = d }
                            },
                            label = { Text(if (isPt) "Duração" else "Duration") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = title.ifBlank {
                        if (isPt) "Estudo de ${selectedTech.displayName}" else "${selectedTech.displayName} Study"
                    }
                    onConfirm(
                        StudySession(
                            tech = selectedTech,
                            titlePt = finalTitle,
                            titleEn = finalTitle,
                            topicPt = topic,
                            topicEn = topic,
                            notesPt = notes,
                            notesEn = notes,
                            dateIso = defaultDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            timeFormatted = timeFormatted,
                            durationMinutes = durationMinutes,
                            isCompleted = false,
                            aiGenerated = false
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                modifier = Modifier.testTag("button_confirm_add_session")
            ) {
                Text(if (isPt) "Salvar" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isPt) "Cancelar" else "Cancel")
            }
        }
    )
}
