package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrackChanges
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.LearningTrackOption
import com.example.model.SubjectCategoryFilter
import com.example.model.TargetSubjectsCatalog
import com.example.model.TargetSubjectsPreference
import com.example.model.TargetSubjectsUiState
import com.example.model.Technology
import com.example.ui.theme.LocalDevRoutineColors

@Composable
fun TargetSubjectsScreen(
    language: Language,
    savedPreference: TargetSubjectsPreference?,
    uiState: TargetSubjectsUiState,
    isFirebaseAuthenticated: Boolean,
    isCloudSynced: Boolean,
    getTechScore: (Technology) -> Int,
    onBack: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onSavePreferences: (
        subjectIds: List<String>,
        dailyGoalMinutes: Int,
        learningTrack: String,
        studyGoalNote: String
    ) -> Unit,
    onClearFeedback: () -> Unit,
    onOpenStudyTracks: () -> Unit = {}
) {
    val isPt = language == Language.PT
    val brand = LocalDevRoutineColors.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.secondary

    var draftSubjectIds by remember {
        mutableStateOf(
            savedPreference?.subjectIds?.takeIf { it.isNotEmpty() }
                ?: listOf(Technology.PYTHON.id, Technology.SQL.id, Technology.HTML.id)
        )
    }
    var draftGoalMinutes by remember {
        mutableIntStateOf(savedPreference?.dailyGoalMinutes ?: 30)
    }
    var draftTrackId by remember {
        mutableStateOf(savedPreference?.learningTrack ?: LearningTrackOption.CUSTOM.id)
    }
    var draftGoalNote by remember {
        mutableStateOf(savedPreference?.studyGoalNote.orEmpty())
    }
    var activeCategory by remember { mutableStateOf(SubjectCategoryFilter.ALL) }

    // Sync local draft whenever cloud preference arrives for the signed-in user
    LaunchedEffect(
        savedPreference?.userId,
        savedPreference?.subjectIds,
        savedPreference?.dailyGoalMinutes,
        savedPreference?.learningTrack,
        savedPreference?.studyGoalNote
    ) {
        if (savedPreference != null && savedPreference.subjectIds.isNotEmpty()) {
            draftSubjectIds = TargetSubjectsCatalog.normalizeSubjectIds(savedPreference.subjectIds)
            draftGoalMinutes = TargetSubjectsCatalog.normalizeDailyGoalMinutes(savedPreference.dailyGoalMinutes)
            draftTrackId = TargetSubjectsCatalog.normalizeTrackId(savedPreference.learningTrack)
            draftGoalNote = TargetSubjectsCatalog.normalizeGoalNote(savedPreference.studyGoalNote)
        }
    }

    val selectedTechs = remember(draftSubjectIds) {
        draftSubjectIds.mapNotNull { id -> Technology.entries.firstOrNull { it.id == id } }
    }
    val visibleTechs = remember(activeCategory) {
        Technology.entries.filter { activeCategory.matches(it) }
    }
    val isSaving = uiState is TargetSubjectsUiState.Saving

    val hasUnsavedChanges = remember(
        savedPreference,
        draftSubjectIds,
        draftGoalMinutes,
        draftTrackId,
        draftGoalNote
    ) {
        if (savedPreference == null) true
        else {
            TargetSubjectsCatalog.normalizeSubjectIds(savedPreference.subjectIds) !=
                TargetSubjectsCatalog.normalizeSubjectIds(draftSubjectIds) ||
                savedPreference.dailyGoalMinutes != draftGoalMinutes ||
                savedPreference.learningTrack != draftTrackId ||
                savedPreference.studyGoalNote != TargetSubjectsCatalog.normalizeGoalNote(draftGoalNote)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen_target_subjects")
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Header Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(
                        1.2.dp,
                        Brush.horizontalGradient(listOf(brand.primary.copy(alpha = 0.7f), brand.accent.copy(alpha = 0.7f)))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("btn_target_subjects_back")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = if (isPt) "Voltar" else "Back",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(brand.primary, brand.accent))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrackChanges,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPt) "Matérias de Foco" else "Target Subjects",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    text = if (isPt) {
                                        "Escolha suas tecnologias alvo e salve no Firestore"
                                    } else {
                                        "Choose your target subjects & store them in Firestore"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = primaryColor.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "${draftSubjectIds.size}/${Technology.entries.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // Cloud sync indicator strip
                        val syncBgColor = when {
                            isFirebaseAuthenticated && isCloudSynced && !hasUnsavedChanges -> Color(0xFF2E7D32).copy(alpha = 0.16f)
                            isFirebaseAuthenticated -> primaryColor.copy(alpha = 0.14f)
                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        }
                        val syncContentColor = when {
                            isFirebaseAuthenticated && isCloudSynced && !hasUnsavedChanges -> Color(0xFF4CAF50)
                            isFirebaseAuthenticated -> primaryColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = syncBgColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isFirebaseAuthenticated) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                        contentDescription = null,
                                        tint = syncContentColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            !isFirebaseAuthenticated -> if (isPt) {
                                                "Entre com Google para salvar na nuvem (Firestore)"
                                            } else {
                                                "Sign in with Google to save preferences in Firestore"
                                            }
                                            hasUnsavedChanges -> if (isPt) {
                                                "Alterações pendentes — toque em Salvar para sincronizar"
                                            } else {
                                                "Unsaved changes — tap Save to sync to Firestore"
                                            }
                                            isCloudSynced -> if (isPt) {
                                                "Preferências sincronizadas no Cloud Firestore"
                                            } else {
                                                "Preferences synced with Cloud Firestore"
                                            }
                                            else -> if (isPt) {
                                                "Pronto para salvar suas matérias no Firestore"
                                            } else {
                                                "Ready to save your target subjects to Firestore"
                                            }
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = syncContentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Google Sign-In prompt card if Firebase user is not signed in
            if (!isFirebaseAuthenticated) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = accentColor.copy(alpha = 0.12f)
                        ),
                        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("card_target_subjects_auth_prompt")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPt) "Conta Google necessária para nuvem" else "Google account needed for cloud storage",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isPt) {
                                        "Você já pode escolher suas matérias abaixo e conectar sua conta Google para armazená-las com segurança no Firestore."
                                    } else {
                                        "Pick your subjects below and connect your Google account to securely persist them in Firestore."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = onGoogleSignInClick,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("btn_target_subjects_google_signin")
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPt) "Entrar" else "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Feedback banner (Success or Error)
            item {
                AnimatedVisibility(
                    visible = uiState is TargetSubjectsUiState.Success || uiState is TargetSubjectsUiState.Error,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val isError = uiState is TargetSubjectsUiState.Error
                    val msg = when (uiState) {
                        is TargetSubjectsUiState.Success -> uiState.message
                        is TargetSubjectsUiState.Error -> uiState.message
                        else -> ""
                    }
                    val bannerColor = if (isError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = bannerColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, bannerColor.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_target_subjects_feedback")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = bannerColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (!isError) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = onOpenStudyTracks,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("btn_open_tracks_after_save")
                                    ) {
                                        Text(
                                            text = if (isPt) "Explorar Trilhas" else "Explore Study Tracks",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = onClearFeedback,
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (isPt) "Fechar" else "Dismiss", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Quick Presets Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPt) "Combinações Rápidas (Presets)" else "Quick Focus Presets",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(LearningTrackOption.entries.filter { it != LearningTrackOption.CUSTOM }) { preset ->
                            val isPresetActive = draftTrackId == preset.id
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isPresetActive) primaryColor.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    width = if (isPresetActive) 1.5.dp else 1.dp,
                                    color = if (isPresetActive) primaryColor
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .width(180.dp)
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        onClearFeedback()
                                        draftSubjectIds = preset.presetTechs.map { it.id }
                                        draftTrackId = preset.id
                                    }
                                    .testTag("preset_track_${preset.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = preset.label(language),
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (isPresetActive) primaryColor else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isPresetActive) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = primaryColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = preset.subtitle(language),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Category Filters + Quick Select All / Reset
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPt) "Escolha suas Matérias" else "Choose Your Subjects",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onClearFeedback()
                                        draftSubjectIds = Technology.entries.map { it.id }
                                        draftTrackId = LearningTrackOption.CUSTOM.id
                                    }
                                    .testTag("btn_select_all_subjects")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isPt) "Todas" else "All",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                            }

                            if (draftSubjectIds.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            onClearFeedback()
                                            draftSubjectIds = emptyList()
                                            draftTrackId = LearningTrackOption.CUSTOM.id
                                        }
                                        .testTag("btn_clear_selected_subjects")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isPt) "Limpar" else "Clear",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SubjectCategoryFilter.entries) { category ->
                            val selected = activeCategory == category
                            FilterChip(
                                selected = selected,
                                onClick = { activeCategory = category },
                                label = {
                                    Text(
                                        text = category.label(language),
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.testTag("filter_subject_cat_${category.id}")
                            )
                        }
                    }
                }
            }

            // 6. 2-Column Subject Grid
            items(visibleTechs.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { tech ->
                        val isSelected = tech.id in draftSubjectIds
                        TargetSubjectCard(
                            tech = tech,
                            isSelected = isSelected,
                            xpScore = getTechScore(tech),
                            language = language,
                            onToggle = {
                                onClearFeedback()
                                val updatedSet = draftSubjectIds.toMutableSet()
                                if (isSelected) updatedSet.remove(tech.id) else updatedSet.add(tech.id)
                                val normalized = TargetSubjectsCatalog.normalizeSubjectIds(updatedSet)
                                draftSubjectIds = normalized
                                draftTrackId = TargetSubjectsCatalog.inferMatchingTrack(normalized).id
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // 7. Study Goal Configuration Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("card_target_study_goal")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPt) "Meta Diária de Estudo" else "Daily Study Target",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TargetSubjectsCatalog.DAILY_GOAL_OPTIONS.forEach { mins ->
                                val isChosen = draftGoalMinutes == mins
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isChosen) primaryColor.copy(alpha = 0.22f)
                                    else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        width = if (isChosen) 1.5.dp else 1.dp,
                                        color = if (isChosen) primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onClearFeedback()
                                            draftGoalMinutes = mins
                                        }
                                        .testTag("btn_goal_minutes_$mins")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${mins}m",
                                            fontSize = 12.sp,
                                            fontWeight = if (isChosen) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isChosen) primaryColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = draftGoalNote,
                            onValueChange = { incoming ->
                                onClearFeedback()
                                draftGoalNote = incoming.take(TargetSubjectsCatalog.MAX_NOTE_LENGTH)
                            },
                            label = {
                                Text(
                                    text = if (isPt) "Objetivo de estudo (opcional)" else "Study goal note (optional)"
                                )
                            },
                            placeholder = {
                                Text(
                                    text = if (isPt) "Ex: Dominar Python e SQL para backend" else "e.g. Master Python & SQL for backend"
                                )
                            },
                            supportingText = {
                                Text("${draftGoalNote.length}/${TargetSubjectsCatalog.MAX_NOTE_LENGTH}")
                            },
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_target_goal_note")
                        )
                    }
                }
            }
        }

        // 8. Bottom Sticky Action Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val summaryText = if (selectedTechs.isEmpty()) {
                        if (isPt) "Selecione ao menos 1 matéria para continuar"
                        else "Select at least 1 target subject to save"
                    } else {
                        val names = selectedTechs.take(3).joinToString(", ") { it.displayName }
                        val extra = if (selectedTechs.size > 3) " +${selectedTechs.size - 3}" else ""
                        "$names$extra • ${draftGoalMinutes}m/${if (isPt) "dia" else "day"}"
                    }
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (selectedTechs.isEmpty()) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        onSavePreferences(
                            draftSubjectIds,
                            draftGoalMinutes,
                            draftTrackId,
                            draftGoalNote
                        )
                    },
                    enabled = draftSubjectIds.isNotEmpty() && !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_target_subjects")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when {
                            isSaving -> if (isPt) "Salvando no Firestore..." else "Saving to Firestore..."
                            !isFirebaseAuthenticated -> if (isPt) "Entrar com Google e Salvar" else "Sign in with Google & Save"
                            else -> if (isPt) "Salvar Matérias no Firestore" else "Save Preferences to Firestore"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetSubjectCard(
    tech: Technology,
    isSelected: Boolean,
    xpScore: Int,
    language: Language,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) tech.accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
        animationSpec = spring(),
        label = "subject_border_color"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) tech.accentColor.copy(alpha = 0.16f)
        else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = spring(),
        label = "subject_bg_color"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 1.8.dp else 1.dp,
        animationSpec = spring(),
        label = "subject_border_width"
    )

    Card(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onToggle)
            .testTag("subject_card_${tech.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(tech.accentColor.copy(alpha = if (isSelected) 0.28f else 0.18f))
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) tech.accentColor else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = tech.icon,
                        contentDescription = tech.displayName,
                        tint = tech.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isSelected) {
                        if (isPt) "${tech.displayName} selecionada" else "${tech.displayName} selected"
                    } else {
                        if (isPt) "Selecionar ${tech.displayName}" else "Select ${tech.displayName}"
                    },
                    tint = if (isSelected) tech.accentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = tech.displayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isPt) tech.categoryPt else tech.categoryEn,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tech.accentColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = tech.badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = tech.accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                if (xpScore > 0) {
                    Text(
                        text = "$xpScore XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
