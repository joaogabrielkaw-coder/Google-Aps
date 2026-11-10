package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TechContentRepository
import com.example.model.ActivityDifficulty
import com.example.model.Language
import com.example.model.PracticalActivity
import com.example.model.QuizQuestion
import com.example.model.RoadmapStep
import com.example.model.Technology
import com.example.model.UserScoreInfo
import com.example.ui.components.CodeSnippetCard
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RedAccent
import kotlinx.coroutines.delay

@Composable
fun TechHubScreen(
    language: Language,
    onSelectTech: (Technology) -> Unit
) {
    val isPt = language == Language.PT
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = if (isPt) listOf("Todas (8)", "Linguagens & Web (5)", "Bancos de Dados (3)")
    else listOf("All (8)", "Languages & Web (5)", "Databases (3)")

    val filteredTechs = when (selectedCategoryIndex) {
        1 -> listOf(Technology.PYTHON, Technology.SQL, Technology.HTML, Technology.CSS, Technology.JAVA)
        2 -> listOf(Technology.MYSQL, Technology.POSTGRESQL, Technology.MONGODB)
        else -> Technology.entries
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_tech_hub")
    ) {
        // Title & Description
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isPt) "Trilhas de Estudo" else "Study Tracks",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isPt) "Selecione uma tecnologia para ver o resumo, atividades práticas, código, roteiro e teste prático com pontuação."
                else "Select a technology to explore guides, hands-on practice, cheatsheets, roadmap, and scored quizzes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEachIndexed { index, title ->
                    val isSelected = selectedCategoryIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedCategoryIndex = index }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredTechs) { tech ->
                TechListItemCard(
                    tech = tech,
                    language = language,
                    onClick = { onSelectTech(tech) }
                )
            }
        }
    }
}

@Composable
fun TechListItemCard(
    tech: Technology,
    language: Language,
    onClick: () -> Unit
) {
    val isPt = language == Language.PT
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tech_item_${tech.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(tech.accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tech.icon,
                    contentDescription = tech.displayName,
                    tint = tech.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tech.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tech.accentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tech.badge,
                            color = tech.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPt) tech.categoryPt else tech.categoryEn,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isPt) tech.shortDescPt else tech.shortDescEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechDetailScreen(
    tech: Technology,
    language: Language,
    scoreInfo: UserScoreInfo,
    onAwardQuizPoints: (Technology, Int, Int) -> Unit,
    onAwardActivityPoints: (Technology, String, Int) -> Unit,
    getTechScore: (Technology) -> Int,
    pointsFeedback: String?,
    onDismissPointsFeedback: () -> Unit,
    onBack: () -> Unit,
    onAskAi: () -> Unit
) {
    val isPt = language == Language.PT
    var selectedTabIndex by remember { mutableIntStateOf(1) } // Default to Hands-on Practice tab

    val tabTitles = if (isPt) {
        listOf("Visão Geral", "Atividades Práticas", "Quiz com Pontos", "Cheatsheet", "Roadmap")
    } else {
        listOf("Overview", "Hands-on Practice", "Scored Quiz", "Cheatsheet", "Roadmap")
    }

    // Auto-dismiss floating score feedback after 3 seconds
    LaunchedEffect(pointsFeedback) {
        if (pointsFeedback != null) {
            delay(3000L)
            onDismissPointsFeedback()
        }
    }

    val techCurrentScore = getTechScore(tech)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(tech.accentColor.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tech.icon,
                                    contentDescription = null,
                                    tint = tech.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tech.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Technology XP Chip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = RedAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$techCurrentScore XP",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("button_back_tech")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAskAi,
                containerColor = RedAccent,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.testTag("fab_ask_ai_tech")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPt) "Dúvidas com IA" else "Ask AI",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("screen_tech_detail_${tech.id}")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Gamification banner
                TechScoreHeaderBanner(
                    scoreInfo = scoreInfo,
                    tech = tech,
                    techScore = techCurrentScore,
                    isPt = isPt
                )

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = RedAccent
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) RedAccent else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                    }
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> TechOverviewTab(tech, isPt)
                    1 -> TechPracticalActivitiesTab(
                        tech = tech,
                        language = language,
                        scoreInfo = scoreInfo,
                        onAwardPoints = { actId, pts -> onAwardActivityPoints(tech, actId, pts) }
                    )
                    2 -> TechQuizTab(
                        tech = tech,
                        language = language,
                        scoreInfo = scoreInfo,
                        onAwardQuizPoints = { qId, pts -> onAwardQuizPoints(tech, qId, pts) }
                    )
                    3 -> TechCheatsheetTab(tech, language)
                    4 -> TechRoadmapTab(tech, language)
                }
            }

            // Animated points feedback banner at top
            AnimatedVisibility(
                visible = pointsFeedback != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1B4D2B),
                    shadowElevation = 8.dp,
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = pointsFeedback ?: "",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TechScoreHeaderBanner(
    scoreInfo: UserScoreInfo,
    tech: Technology,
    techScore: Int,
    isPt: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = RedAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isPt) "Nível ${scoreInfo.levelNumber}: ${scoreInfo.levelTitlePt}"
                            else "Level ${scoreInfo.levelNumber}: ${scoreInfo.levelTitleEn}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isPt) "Total: ${scoreInfo.totalScore} XP acumulados"
                            else "Total: ${scoreInfo.totalScore} XP accumulated",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RedAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+XP por acerto",
                        color = RedAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress towards next level
            LinearProgressIndicator(
                progress = scoreInfo.progressFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = RedAccent,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun TechPracticalActivitiesTab(
    tech: Technology,
    language: Language,
    scoreInfo: UserScoreInfo,
    onAwardPoints: (String, Int) -> Unit
) {
    val isPt = language == Language.PT
    val allActivities = remember(tech) { TechContentRepository.getPracticalActivities(tech) }
    var selectedDifficultyFilter by remember { mutableStateOf<ActivityDifficulty?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredActivities = remember(selectedDifficultyFilter, searchQuery, allActivities) {
        allActivities.filter { activity ->
            val matchesDiff = selectedDifficultyFilter == null || activity.difficulty == selectedDifficultyFilter
            val matchesQuery = searchQuery.isBlank() ||
                activity.titlePt.contains(searchQuery, ignoreCase = true) ||
                activity.titleEn.contains(searchQuery, ignoreCase = true) ||
                activity.instructionsPt.contains(searchQuery, ignoreCase = true) ||
                activity.instructionsEn.contains(searchQuery, ignoreCase = true) ||
                activity.id.contains(searchQuery, ignoreCase = true)
            matchesDiff && matchesQuery
        }
    }

    val completedCount = remember(scoreInfo.completedActivityIds, allActivities) {
        allActivities.count { scoreInfo.completedActivityIds.contains(it.id) }
    }
    val progressFraction = if (allActivities.isNotEmpty()) completedCount.toFloat() / allActivities.size else 0f

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPt) "Mais de 100 Desafios de Código" else "100+ Coding Challenges",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPt) "${allActivities.size} desafios práticos balanceados em Iniciante, Intermediário e Avançado."
                            else "${allActivities.size} practical challenges balanced across Beginner, Intermediate, and Advanced.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isPt) "Desafios Concluídos" else "Challenges Solved",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$completedCount / ${allActivities.size} (${(progressFraction * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp).testTag("input_search_challenges"),
                    placeholder = {
                        Text(
                            text = if (isPt) "Buscar por nome ou tópico (ex: f-string, JOIN, flexbox...)"
                            else "Search challenge or keyword (e.g. f-string, JOIN, flexbox...)",
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Difficulty Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isAllSelected = selectedDifficultyFilter == null
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedDifficultyFilter = null }
                    ) {
                        Text(
                            text = if (isPt) "Todas (${allActivities.size})" else "All (${allActivities.size})",
                            color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    ActivityDifficulty.entries.forEach { diff ->
                        val isSelected = selectedDifficultyFilter == diff
                        val diffColor = Color(diff.badgeColorHex)
                        val countInDiff = allActivities.count { it.difficulty == diff }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) diffColor else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { selectedDifficultyFilter = diff }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else diffColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPt) "${diff.labelPt} ($countInDiff)"
                                    else "${diff.labelEn} ($countInDiff)",
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        itemsIndexed(filteredActivities, key = { _, act -> act.id }) { index, activity ->
            val isCompleted = scoreInfo.completedActivityIds.contains(activity.id)
            PracticalActivityCard(
                activity = activity,
                challengeNumber = allActivities.indexOf(activity) + 1,
                isPt = isPt,
                isAlreadyCompleted = isCompleted,
                onCompleteActivity = { pts -> onAwardPoints(activity.id, pts) }
            )
        }
    }
}

@Composable
fun PracticalActivityCard(
    activity: PracticalActivity,
    challengeNumber: Int = 1,
    isPt: Boolean,
    isAlreadyCompleted: Boolean,
    onCompleteActivity: (Int) -> Unit
) {
    var userCode by remember(activity.id) { mutableStateOf(activity.starterCode) }
    var showHints by remember(activity.id) { mutableStateOf(false) }
    var showSolution by remember(activity.id) { mutableStateOf(false) }
    var validationResult by remember(activity.id) { mutableStateOf<Boolean?>(null) }
    var feedbackMessage by remember(activity.id) { mutableStateOf<String?>(null) }

    val difficultyColor = when (activity.difficulty) {
        ActivityDifficulty.BEGINNER -> Color(0xFF2E7D32)
        ActivityDifficulty.INTERMEDIATE -> Color(0xFFE65100)
        ActivityDifficulty.ADVANCED -> Color(0xFFC2185B)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("activity_card_${activity.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row with challenge number, difficulty & score badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "#$challengeNumber",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(difficultyColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isPt) activity.difficulty.labelPt else activity.difficulty.labelEn,
                            color = difficultyColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RedAccent.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = RedAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${activity.pointsValue} XP",
                                color = RedAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (isAlreadyCompleted || validationResult == true) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1B4D2B)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF80FFAB),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPt) "Resolvido" else "Solved",
                                color = Color(0xFF80FFAB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = if (isPt) activity.titlePt else activity.titleEn,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Instructions
            Text(
                text = if (isPt) activity.instructionsPt else activity.instructionsEn,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Code Editor Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF161420))
                    .border(1.dp, Color(0xFF3B3355), RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPt) "💻 Editor de Código:" else "💻 Code Editor:",
                            color = Color(0xFFA594F9),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                userCode = activity.starterCode
                                validationResult = null
                                feedbackMessage = null
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Resetar",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = userCode,
                        onValueChange = { userCode = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("code_input_${activity.id}"),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color(0xFFE2E0EC)
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hints toggle & Hints content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showHints = !showHints },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showHints) (if (isPt) "Ocultar Dicas" else "Hide Hints")
                        else (if (isPt) "Ver Dica" else "View Hint"),
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = {
                        val isCorrect = validateUserCode(userCode, activity)
                        validationResult = isCorrect
                        if (isCorrect) {
                            feedbackMessage = if (isPt) {
                                "Parabéns! Sua solução atende a todos os critérios. +${activity.pointsValue} XP adicionados!"
                            } else {
                                "Great job! Your solution meets all criteria. +${activity.pointsValue} XP earned!"
                            }
                            onCompleteActivity(activity.pointsValue)
                        } else {
                            val missing = activity.requiredKeywords.firstOrNull { kw ->
                                !userCode.contains(kw, ignoreCase = true)
                            }
                            feedbackMessage = if (isPt) {
                                if (missing != null) "Dica de correção: certifique-se de utilizar '$missing' na sintaxe."
                                else "Quase lá! Revise a lógica e os parâmetros solicitados no enunciado."
                            } else {
                                if (missing != null) "Correction hint: ensure you include '$missing' in your code."
                                else "Almost there! Double check the logic and requirements."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_verify_${activity.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPt) "Testar Solução" else "Test Solution",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Hints display
            if (showHints) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF332A15)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isPt) "💡 Dicas pedagógicas:" else "💡 Guided Hints:",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val hints = if (isPt) activity.hintsPt else activity.hintsEn
                        hints.forEach { hint ->
                            Text(
                                text = "• $hint",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Verification Result Card
            if (validationResult != null) {
                Spacer(modifier = Modifier.height(10.dp))
                val isSuccess = validationResult == true
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSuccess) Color(0xFF13381E) else Color(0xFF470C15),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = if (isSuccess) Color(0xFF80FFAB) else Color(0xFFFF94A3),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSuccess) (if (isPt) "Sucesso! Código Válido 🎉" else "Success! Valid Code 🎉")
                                else (if (isPt) "Ajuste Necessário" else "Adjustment Needed"),
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) Color(0xFF80FFAB) else Color(0xFFFF94A3),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = feedbackMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isPt) activity.explanationPt else activity.explanationEn,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE2E0EC)
                        )
                    }
                }
            }

            // Reveal solution option
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showSolution = !showSolution }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (showSolution) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (showSolution) (if (isPt) "Ocultar Solução" else "Hide Solution")
                    else (if (isPt) "Ver Solução Oficial" else "View Official Solution"),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showSolution) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF101018))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = if (isPt) "Código Solução Esperado:" else "Expected Solution Code:",
                            color = Color(0xFF80FFAB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activity.solutionCode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFFE2E0EC)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Validação inteligente do código do usuário baseada em palavras-chave e ausência de placeholders não preenchidos.
 */
private fun validateUserCode(userCode: String, activity: PracticalActivity): Boolean {
    // If placeholders '___' remain, it's not finished
    if (userCode.contains("___")) {
        return false
    }

    // Check all required semantic keywords
    val hasAllKeywords = activity.requiredKeywords.all { kw ->
        userCode.contains(kw, ignoreCase = true)
    }

    if (!hasAllKeywords) return false

    // Normalize whitespace for loose comparison
    val normalizedUser = userCode.replace("\\s+".toRegex(), " ").trim()
    val normalizedSolution = activity.solutionCode.replace("\\s+".toRegex(), " ").trim()

    // If keywords match and code length is reasonable compared to solution, consider valid
    return normalizedUser.length >= (normalizedSolution.length * 0.6)
}

@Composable
fun TechQuizTab(
    tech: Technology,
    language: Language,
    scoreInfo: UserScoreInfo,
    onAwardQuizPoints: (Int, Int) -> Unit
) {
    val isPt = language == Language.PT
    val questions = TechContentRepository.getQuizQuestions(tech)
    val userAnswers = remember { mutableStateMapOf<Int, Int>() }

    if (questions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isPt) "Testes interativos em preparação para esta trilha."
                else "Interactive quizzes coming soon for this track.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isPt) "Quiz com Pontuação" else "Scored Knowledge Quiz",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isPt) "Cada resposta correta vale +20 XP imediatos!"
                            else "Each correct answer grants +20 XP instantly!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RedAccent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "+20 XP / acerto",
                            color = RedAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            items(questions) { q ->
                val selectedOption = userAnswers[q.id]
                val hasAnswered = selectedOption != null
                val isCorrect = selectedOption == q.correctIndex
                val isAlreadyScored = scoreInfo.completedQuizIds.contains(q.id)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = q.question(language),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )

                            if (isAlreadyScored || (hasAnswered && isCorrect)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1B4D2B),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "+${q.pointsValue} XP",
                                        color = Color(0xFF80FFAB),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        q.options(language).forEachIndexed { index, optionText ->
                            val optionSelected = selectedOption == index
                            val optionBg = when {
                                hasAnswered && index == q.correctIndex -> Color(0xFF1B4D2B)
                                hasAnswered && optionSelected && !isCorrect -> Color(0xFF6B1220)
                                optionSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else -> Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(optionBg)
                                    .clickable {
                                        if (!hasAnswered) {
                                            userAnswers[q.id] = index
                                            if (index == q.correctIndex) {
                                                onAwardQuizPoints(q.id, q.pointsValue)
                                            }
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = optionSelected,
                                    onClick = {
                                        if (!hasAnswered) {
                                            userAnswers[q.id] = index
                                            if (index == q.correctIndex) {
                                                onAwardQuizPoints(q.id, q.pointsValue)
                                            }
                                        }
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hasAnswered && index == q.correctIndex) Color(0xFF80FFAB) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (hasAnswered) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isCorrect) Color(0xFF13381E) else Color(0xFF470C15)
                                    )
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (isCorrect) (if (isPt) "🎉 Correto! (+${q.pointsValue} XP)" else "🎉 Correct! (+${q.pointsValue} XP)")
                                        else (if (isPt) "❌ Incorreto." else "❌ Incorrect."),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isCorrect) Color(0xFF80FFAB) else Color(0xFFFF94A3)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = q.explanation(language),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
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

@Composable
fun TechOverviewTab(tech: Technology, isPt: Boolean) {
    val overview = TechContentRepository.getOverview(tech, isPt)
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isPt) "Sobre a Tecnologia" else "About the Technology",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tech.accentColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = overview,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isPt) "💡 Dica de Estudos do Mentor" else "💡 Mentor's Study Tip",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = RedAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isPt) {
                            "Para fixar ${tech.displayName}, pratique escrevendo código e executando comandos diariamente na aba 'Atividades Práticas'. Quanto mais você acerta, mais pontos ganha para subir de nível!"
                        } else {
                            "To master ${tech.displayName}, write code and run commands daily in the 'Hands-on Practice' tab. The more you solve correctly, the more XP you earn to level up!"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TechCheatsheetTab(tech: Technology, language: Language) {
    val snippets = TechContentRepository.getCheatsheet(tech)
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(snippets) { item ->
            CodeSnippetCard(
                title = item.title(language),
                code = item.codeSnippet,
                explanation = item.explanation(language)
            )
        }
    }
}

@Composable
fun TechRoadmapTab(tech: Technology, language: Language) {
    val steps = TechContentRepository.getRoadmap(tech)
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(steps) { step ->
            RoadmapStepCard(step = step, language = language)
        }
    }
}

@Composable
fun RoadmapStepCard(step: RoadmapStep, language: Language) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = step.title(language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "~${step.estimatedHours}h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = step.desc(language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Topics list
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                step.topics(language).forEach { topic ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(RedAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = topic, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
