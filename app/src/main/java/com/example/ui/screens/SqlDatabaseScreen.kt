package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GameScoreEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.repository.SqlLabRepository
import com.example.model.AudioTranscriptionUiState
import com.example.model.Language
import com.example.model.SqlQueryResult
import com.example.model.Technology
import com.example.ui.components.VoiceRecorderIconButton
import com.example.ui.components.VoiceTranscriptionStatusBanner
import com.example.ui.theme.LocalDevRoutineColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

/**
 * Estúdio do Banco de Dados SQL (Room/SQLite) integrado com:
 * 1) Notas de Estudo SQL + Ditado por Voz (gemini-3.5-transcribe)
 * 2) Console SQL Interativo para consultar todas as tabelas do aplicativo
 * 3) Placar e Estatísticas SQL do Jogo 3D
 */
@Composable
fun SqlDatabaseScreen(
    language: Language,
    studyNotes: List<StudyNoteEntity>,
    topGameScores: List<GameScoreEntity>,
    bestGameScore: Int,
    totalCrystals: Int,
    sqlQueryResult: SqlQueryResult?,
    transcriptionState: AudioTranscriptionUiState,
    onRecordingStateChanged: (Boolean) -> Unit,
    onAudioCaptured: (bytes: ByteArray, mimeType: String, durationSeconds: Int) -> Unit,
    onClearTranscriptionState: () -> Unit,
    onAddNote: (tech: Technology, title: String, content: String, isVoice: Boolean) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onRunSqlQuery: (String) -> Unit,
    onOpen3DGame: () -> Unit
) {
    val isPt = language == Language.PT
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = if (isPt) listOf("Notas & Voz", "Console SQL", "Placar 3D") else listOf("SQL Notes & Mic", "SQL Console", "3D Scores")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_sql_database"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PurplePrimary, RedAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPt) "Banco de Dados SQL & Voz" else "SQL Database Studio & Voice",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = if (isPt) "SQLite Room v5 • ${studyNotes.size} notas • Console SELECT ao vivo"
                            else "SQLite Room v5 • ${studyNotes.size} notes • Live SQL SELECT console",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(14.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("tab_sql_$index")
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                item {
                    SqlNotesCreatorCard(
                        language = language,
                        transcriptionState = transcriptionState,
                        onRecordingStateChanged = onRecordingStateChanged,
                        onAudioCaptured = onAudioCaptured,
                        onClearTranscriptionState = onClearTranscriptionState,
                        onSaveNote = onAddNote
                    )
                }

                if (studyNotes.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = if (isPt) "Nenhuma nota salva na tabela study_notes ainda."
                                else "No notes saved in study_notes SQL table yet.",
                                modifier = Modifier.padding(18.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(studyNotes, key = { it.id }) { note ->
                        StudyNoteRowCard(
                            note = note,
                            isPt = isPt,
                            onDelete = { onDeleteNote(note.id) }
                        )
                    }
                }
            }

            1 -> {
                item {
                    LiveSqlConsoleCard(
                        language = language,
                        sqlQueryResult = sqlQueryResult,
                        onRunQuery = onRunSqlQuery
                    )
                }
            }

            else -> {
                item {
                    GameScoresSqlSummaryCard(
                        isPt = isPt,
                        bestScore = bestGameScore,
                        totalCrystals = totalCrystals,
                        scoresCount = topGameScores.size,
                        onOpen3DGame = onOpen3DGame
                    )
                }

                items(topGameScores, key = { it.id }) { score ->
                    GameScoreRowCard(score = score, isPt = isPt)
                }
            }
        }
    }
}

@Composable
private fun SqlNotesCreatorCard(
    language: Language,
    transcriptionState: AudioTranscriptionUiState,
    onRecordingStateChanged: (Boolean) -> Unit,
    onAudioCaptured: (bytes: ByteArray, mimeType: String, durationSeconds: Int) -> Unit,
    onClearTranscriptionState: () -> Unit,
    onSaveNote: (Technology, String, String, Boolean) -> Unit
) {
    val isPt = language == Language.PT
    var selectedTech by remember { mutableStateOf(Technology.SQL) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var usedVoiceInput by remember { mutableStateOf(false) }

    LaunchedEffect(transcriptionState) {
        if (transcriptionState is AudioTranscriptionUiState.Success) {
            noteContent = if (noteContent.isBlank()) {
                transcriptionState.transcript
            } else {
                "$noteContent\n${transcriptionState.transcript}"
            }
            if (noteTitle.isBlank()) {
                noteTitle = if (isPt) "Nota de Voz (${selectedTech.displayName})" else "Voice Note (${selectedTech.displayName})"
            }
            usedVoiceInput = true
            onClearTranscriptionState()
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, selectedTech.accentColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPt) "Nova Nota na Tabela study_notes" else "New Record in study_notes Table",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isPt) "Digite ou grave áudio para transcrever com gemini-3.5-transcribe"
                        else "Type or record voice audio to transcribe via gemini-3.5-transcribe",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                VoiceRecorderIconButton(
                    transcriptionState = transcriptionState,
                    language = language,
                    onRecordingStateChanged = onRecordingStateChanged,
                    onAudioCaptured = onAudioCaptured
                )
            }

            VoiceTranscriptionStatusBanner(
                transcriptionState = transcriptionState,
                language = language,
                onDismiss = onClearTranscriptionState
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(Technology.entries) { tech ->
                    val sel = selectedTech == tech
                    FilterChip(
                        selected = sel,
                        onClick = { selectedTech = tech },
                        label = { Text(tech.displayName, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tech.accentColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            OutlinedTextField(
                value = noteTitle,
                onValueChange = { noteTitle = it },
                label = { Text(if (isPt) "Título da nota" else "Note title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_sql_note_title")
            )

            OutlinedTextField(
                value = noteContent,
                onValueChange = { noteContent = it },
                label = { Text(if (isPt) "Conteúdo ou transcrição de voz" else "Content or voice transcript") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth().testTag("input_sql_note_content")
            )

            Button(
                onClick = {
                    onSaveNote(selectedTech, noteTitle, noteContent, usedVoiceInput)
                    noteTitle = ""
                    noteContent = ""
                    usedVoiceInput = false
                },
                enabled = noteTitle.isNotBlank() && noteContent.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("btn_save_sql_note")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isPt) "INSERT INTO study_notes" else "Save to SQL Database", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StudyNoteRowCard(
    note: StudyNoteEntity,
    isPt: Boolean,
    onDelete: () -> Unit
) {
    val tech = remember(note.techId) { Technology.fromId(note.techId) }
    val dateStr = remember(note.createdAtMillis) {
        SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(note.createdAtMillis))
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, tech.accentColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = tech.accentColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = tech.displayName,
                            color = tech.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (note.isVoiceTranscribed) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Mic, contentDescription = null, tint = RedAccent, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.minimumInteractiveComponentSize()) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = if (isPt) "Excluir nota" else "Delete note",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "ID #${note.id} • $dateStr",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LiveSqlConsoleCard(
    language: Language,
    sqlQueryResult: SqlQueryResult?,
    onRunQuery: (String) -> Unit
) {
    val isPt = language == Language.PT
    var sqlInput by remember { mutableStateOf(SqlLabRepository.PRESET_QUERIES.first()) }

    LaunchedEffect(Unit) {
        if (sqlQueryResult == null) {
            onRunQuery(sqlInput)
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, contentDescription = null, tint = PurplePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isPt) "Console SQL ao Vivo (SQLite)" else "Live SQL Query Console (SQLite)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isPt) "Execute SELECTs diretamente nas 6 tabelas do aplicativo"
                        else "Run real SELECT queries across all 6 live app tables",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(SqlLabRepository.PRESET_QUERIES) { preset ->
                    FilterChip(
                        selected = sqlInput.trim() == preset,
                        onClick = {
                            sqlInput = preset
                            onRunQuery(preset)
                        },
                        label = {
                            Text(
                                text = preset.take(28) + if (preset.length > 28) "..." else "",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    )
                }
            }

            OutlinedTextField(
                value = sqlInput,
                onValueChange = { sqlInput = it },
                textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("input_sql_query")
            )

            Button(
                onClick = { onRunQuery(sqlInput) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("btn_run_sql_query")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isPt) "Executar Consulta SQL" else "Execute SQL Query", color = Color.White, fontWeight = FontWeight.Bold)
            }

            sqlQueryResult?.let { res ->
                if (res.errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                    ) {
                        Text(
                            text = res.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Text(
                        text = if (isPt) "${res.rows.size} linhas em ${res.executionTimeMs}ms"
                        else "${res.rows.size} rows in ${res.executionTimeMs}ms",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0E0B18))
                            .horizontalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                res.columns.forEach { col ->
                                    Text(
                                        text = col,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        modifier = Modifier.width(120.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            res.rows.forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    row.forEach { cell ->
                                        Text(
                                            text = cell,
                                            color = Color.White.copy(alpha = 0.88f),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            modifier = Modifier.width(120.dp),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameScoresSqlSummaryCard(
    isPt: Boolean,
    bestScore: Int,
    totalCrystals: Int,
    scoresCount: Int,
    onOpen3DGame: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, RedAccent.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$bestScore", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RedAccent)
                    Text(if (isPt) "MAX(score)" else "MAX(score)", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalCrystals", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF00B0FF))
                    Text("SUM(crystals)", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$scoresCount", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = PurplePrimary)
                    Text("COUNT(runs)", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Button(
                onClick = onOpen3DGame,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPt) "Jogar DevCore 3D Runner" else "Play DevCore 3D Runner", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GameScoreRowCard(score: GameScoreEntity, isPt: Boolean) {
    val tech = remember(score.techFocusId) { Technology.fromId(score.techFocusId) }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(score.playerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "${tech.displayName} • ${score.durationSeconds}s • Combo ×${score.maxCombo}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${score.score} pts", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = RedAccent)
                Text(
                    text = if (isPt) "💎 ${score.crystalsCollected} cristais" else "💎 ${score.crystalsCollected} crystals",
                    fontSize = 11.sp,
                    color = Color(0xFF00B0FF)
                )
            }
        }
    }
}
