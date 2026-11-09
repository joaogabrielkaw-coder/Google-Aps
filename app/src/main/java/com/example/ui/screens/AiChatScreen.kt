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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiChatMessage
import com.example.model.Language
import com.example.model.RoutinePlan
import com.example.model.Technology
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RedAccent

@Composable
fun AiChatScreen(
    language: Language,
    messages: List<AiChatMessage>,
    isLoading: Boolean,
    streamingText: String,
    errorMessage: String?,
    onSendMessage: (String) -> Unit,
    onGenerateRoutine: (goal: String, techs: List<Technology>, days: Int, minutes: Int) -> Unit,
    onSaveRoutineToCalendar: (RoutinePlan) -> Unit
) {
    val isPt = language == Language.PT
    var inputText by remember { mutableStateOf("") }
    var showRoutineDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, streamingText) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size)
        }
    }

    val quickPrompts = if (isPt) listOf(
        "Criar rotina de 4 semanas para Python e MySQL",
        "Como integrar HTML, CSS e Java?",
        "Qual a diferença entre MySQL, PostgreSQL e MongoDB?",
        "Exercício prático de SQL com JOINs"
    ) else listOf(
        "Create a 4-week study routine for Python & MySQL",
        "How to connect HTML, CSS and Java backend?",
        "Key differences between MySQL, PostgreSQL & MongoDB?",
        "Give me a hands-on SQL query exercise"
    )

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Quick prompt suggestions
                LazyRow(
                    modifier = Modifier.padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPrompts) { prompt ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = !isLoading) { onSendMessage(prompt) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Chat Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                if (isPt) "Converse com a IA ou peça ideias de estudo..."
                                else "Chat with AI or ask for study ideas...",
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_ai_message"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            val textToSend = inputText.trim()
                            if (textToSend.isNotBlank()) {
                                inputText = ""
                                onSendMessage(textToSend)
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) RedAccent else MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("button_send_ai_message")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("screen_ai_chat"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header card with "Gerar Rotina Estruturada"
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = RedAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPt) "Gerador de Rotinas Inteligente" else "Smart Routine Generator",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isPt) "Crie um cronograma sob medida baseado no seu objetivo e adicione ao calendário."
                                else "Generate a custom study plan tailored to your target and add to calendar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showRoutineDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("button_open_routine_dialog")
                        ) {
                            Text(if (isPt) "Gerar" else "Generate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Chat Messages
            items(messages) { msg ->
                ChatBubbleItem(
                    message = msg,
                    language = language,
                    onSavePlan = onSaveRoutineToCalendar
                )
            }

            // Real-time streaming bubble if currently receiving chunks
            if (isLoading && streamingText.isNotBlank()) {
                item {
                    ChatBubbleItem(
                        message = AiChatMessage(isUser = false, content = streamingText),
                        language = language,
                        onSavePlan = onSaveRoutineToCalendar,
                        isStreaming = true
                    )
                }
            } else if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPt) "DevTutor AI está pensando..." else "DevTutor AI is thinking...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showRoutineDialog) {
        CreateRoutineDialog(
            language = language,
            onDismiss = { showRoutineDialog = false },
            onConfirm = { goal, techs, days, minutes ->
                showRoutineDialog = false
                onGenerateRoutine(goal, techs, days, minutes)
            }
        )
    }
}

@Composable
fun ChatBubbleItem(
    message: AiChatMessage,
    language: Language,
    onSavePlan: (RoutinePlan) -> Unit,
    isStreaming: Boolean = false
) {
    val isUser = message.isUser
    val isPt = language == Language.PT

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(PurplePrimary, RedAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "IA",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = if (!isUser) CardDefaults.outlinedCardBorder().copy(width = 1.dp) else null
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                            lineHeight = 21.sp
                        )
                    )

                    // Embedded Routine Card if AI generated a concrete study plan
                    message.suggestedPlan?.let { plan ->
                        Spacer(modifier = Modifier.height(12.dp))
                        RoutinePlanPreviewCard(
                            plan = plan,
                            language = language,
                            onAddToCalendar = { onSavePlan(plan) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoutinePlanPreviewCard(
    plan: RoutinePlan,
    language: Language,
    onAddToCalendar: () -> Unit
) {
    val isPt = language == Language.PT
    var isAdded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_suggested_routine_plan"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF130626)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(PurplePrimary, RedAccent)),
            width = 1.5.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPt) plan.titlePt else plan.titleEn,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RedAccent.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "${plan.sessions.size} " + if (isPt) "sessões" else "sessions",
                        color = Color(0xFFFFCCD5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // First 3 sessions preview
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                plan.sessions.take(3).forEach { session ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(session.tech.accentColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${session.tech.displayName}: ${session.title(language)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE8DEF2),
                            maxLines = 1
                        )
                    }
                }
                if (plan.sessions.size > 3) {
                    Text(
                        text = "+ ${plan.sessions.size - 3} " + if (isPt) "outras sessões programadas" else "more scheduled sessions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onAddToCalendar()
                    isAdded = true
                },
                enabled = !isAdded,
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("button_add_ai_routine_to_calendar")
            ) {
                Icon(
                    imageVector = if (isAdded) Icons.Default.Check else Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAdded) (if (isPt) "Adicionado ao Calendário!" else "Added to Calendar!")
                    else (if (isPt) "Adicionar Rotina ao Calendário" else "Add Routine to Calendar"),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CreateRoutineDialog(
    language: Language,
    onDismiss: () -> Unit,
    onConfirm: (goal: String, techs: List<Technology>, days: Int, minutes: Int) -> Unit
) {
    val isPt = language == Language.PT
    var goalText by remember { mutableStateOf("") }
    val selectedTechs = remember {
        mutableStateListOf(Technology.PYTHON, Technology.SQL, Technology.MYSQL)
    }
    var selectedDays by remember { mutableIntStateOf(14) }
    var selectedMinutes by remember { mutableIntStateOf(45) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = RedAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPt) "Planejar Rotina com IA" else "Plan Routine with AI",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (isPt) "Qual é o seu objetivo de estudo?" else "What is your study goal?",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        placeholder = {
                            Text(
                                if (isPt) "Ex: Dominar backend com Python e PostgreSQL para criar APIs."
                                else "Ex: Master backend with Python and PostgreSQL to build APIs.",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("input_routine_goal"),
                        maxLines = 3
                    )
                }

                item {
                    Text(
                        text = if (isPt) "Selecione as tecnologias:" else "Select technologies:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Technology.entries) { tech ->
                            val isSelected = selectedTechs.contains(tech)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        if (selectedTechs.size > 1) selectedTechs.remove(tech)
                                    } else {
                                        selectedTechs.add(tech)
                                    }
                                },
                                label = { Text(tech.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tech.accentColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = if (isPt) "Duração do Cronograma:" else "Schedule Duration:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(7, 14, 21, 30).forEach { days ->
                            val isSelected = selectedDays == days
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedDays = days }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$days " + if (isPt) "dias" else "days",
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isPt) "Tempo diário de estudo:" else "Daily study time:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30, 45, 60, 90).forEach { minutes ->
                            val isSelected = selectedMinutes == minutes
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) RedAccent
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedMinutes = minutes }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$minutes min",
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalGoal = goalText.ifBlank {
                        if (isPt) "Aprender e praticar ${selectedTechs.joinToString { it.displayName }}"
                        else "Learn and practice ${selectedTechs.joinToString { it.displayName }}"
                    }
                    onConfirm(finalGoal, selectedTechs.toList(), selectedDays, selectedMinutes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                modifier = Modifier.testTag("button_confirm_generate_routine")
            ) {
                Text(if (isPt) "Gerar com IA" else "Generate with AI")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isPt) "Cancelar" else "Cancel")
            }
        }
    )
}
