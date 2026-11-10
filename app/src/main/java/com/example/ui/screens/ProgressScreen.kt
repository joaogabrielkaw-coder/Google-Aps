package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserBadgeEntity
import com.example.model.ActivityDifficulty
import com.example.model.Language
import com.example.model.Technology
import com.example.model.UserScoreInfo
import com.example.ui.components.UserBadgesSection
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.RedAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.example.data.repository.LanguageDailyProgress

@Composable
fun ProgressScreen(
    language: Language,
    scoreInfo: UserScoreInfo,
    badges: List<UserBadgeEntity> = emptyList(),
    todayProgressMap: Map<String, LanguageDailyProgress> = emptyMap(),
    getTechScore: (Technology) -> Int,
    onOpenTechDetail: (Technology) -> Unit
) {
    val isPt = language == Language.PT
    var selectedTimeframe by remember { mutableIntStateOf(0) } // 0 = 7 days, 1 = 30 days, 2 = All
    val timeframes = if (isPt) listOf("Últimos 7 Dias", "30 Dias", "Geral") else listOf("Last 7 Days", "30 Days", "Overall")

    // Historical data points simulated based on user's current score
    val currentTotal = scoreInfo.totalScore
    val historyPoints = remember(currentTotal, selectedTimeframe) {
        val days = if (selectedTimeframe == 0) 7 else if (selectedTimeframe == 1) 14 else 8
        val list = mutableListOf<Float>()
        for (i in 0 until days) {
            val factor = (i + 1).toFloat() / days.toFloat()
            // Progressive curve simulating daily study gains
            val point = (currentTotal * factor * (0.85f + (i % 2) * 0.15f)).coerceAtLeast(10f * (i + 1))
            list.add(point)
        }
        list
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_progress"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Subtitle
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = null,
                        tint = RedAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPt) "Visualização de Progresso" else "Progress Analytics",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isPt) "Acompanhe a evolução da sua pontuação e domínio prático ao longo do tempo em cada linguagem."
                    else "Track your score evolution and mastery over time across each programming language and database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Timeframe selector chips
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                timeframes.forEachIndexed { index, label ->
                    val isSelected = selectedTimeframe == index
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) RedAccent else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { selectedTimeframe = index }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 1. Line/Area Evolution Chart
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPt) "📈 Evolução de XP ao Longo do Tempo" else "📈 Score Evolution Over Time",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isPt) "Pontuação acumulada por atividades e quizzes"
                                else "Cumulative XP earned through practice and quizzes",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PurplePrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${scoreInfo.totalScore} XP",
                                color = PurplePrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Native Compose Line/Area Chart Component
                    ScoreOverTimeLineChart(
                        dataPoints = historyPoints,
                        isPt = isPt
                    )
                }
            }
        }

        // 2. Bar Chart by Language & Database
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isPt) "📊 Domínio por Linguagem e Banco" else "📊 Mastery by Language & Database",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isPt) "XP conquistado em cada trilha tecnológica"
                                else "XP earned in each technology track",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "Max: 180 XP/trilha",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Horizontal comparative bars
                    TechnologyScoreBarsChart(
                        getTechScore = getTechScore,
                        isPt = isPt,
                        onOpenTech = onOpenTechDetail
                    )
                }
            }
        }

        // 3. Difficulty Breakdown Stats
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isPt) "🎯 Atividades por Dificuldade" else "🎯 Activities by Difficulty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPt) "Distribuição balanceada entre os 3 níveis calibrados"
                        else "Balanced distribution across all 3 calibrated levels",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DifficultyStatCard(
                            title = if (isPt) "Iniciante" else "Beginner",
                            points = "30 XP",
                            color = Color(0xFF2E7D32),
                            completedCount = (scoreInfo.completedActivitiesCount / 3).coerceAtLeast(0),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DifficultyStatCard(
                            title = if (isPt) "Intermediário" else "Intermediate",
                            points = "60 XP",
                            color = Color(0xFFE65100),
                            completedCount = (scoreInfo.completedActivitiesCount / 3).coerceAtLeast(0),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DifficultyStatCard(
                            title = if (isPt) "Avançado" else "Advanced",
                            points = "90 XP",
                            color = Color(0xFFC2185B),
                            completedCount = (scoreInfo.completedActivitiesCount / 3).coerceAtLeast(0),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3b. Daily Study Progress Tracker in Firestore Breakdown
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isPt) "🔥 Registro Diário no Cloud Firestore" else "🔥 Daily Cloud Firestore Record",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isPt) "Progresso salvo na nuvem por linguagem selecionada hoje"
                        else "Cloud-persisted progress per selected language today",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Technology.values().forEach { tech ->
                        val prog = todayProgressMap[tech.id]
                        val xp = prog?.xpEarned ?: 0
                        val challenges = prog?.completedChallengesCount ?: 0
                        val minutes = prog?.minutesStudied ?: 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenTechDetail(tech) }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (xp > 0 || minutes > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tech.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (minutes > 0) {
                                    Text(
                                        text = "${minutes}m",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                if (challenges > 0) {
                                    Text(
                                        text = "${challenges} des.",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (xp > 0) PurplePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "+$xp XP",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (xp > 0) PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Room Badges & Achievements Section (Streaks, Python Master, Night Owl, etc.)
        item {
            UserBadgesSection(
                badges = badges,
                language = language,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ScoreOverTimeLineChart(
    dataPoints: List<Float>,
    isPt: Boolean
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxScore = (dataPoints.maxOrNull() ?: 100f).coerceAtLeast(100f)

    val daysLabels = if (isPt) {
        listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
    } else {
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF13101C))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val step = size.width / (dataPoints.size - 1).coerceAtLeast(1)
                            val tappedIndex = (offset.x / step).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = tappedIndex
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                // Draw horizontal guide lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = Color(0xFF2B253D),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                if (dataPoints.size >= 2) {
                    val stepX = width / (dataPoints.size - 1)

                    val linePath = Path()
                    val fillPath = Path()

                    fillPath.moveTo(0f, height)

                    dataPoints.forEachIndexed { index, value ->
                        val x = index * stepX
                        val normalizedY = (1f - (value / maxScore)).coerceIn(0f, 1f)
                        val y = normalizedY * (height - 20f) + 10f

                        if (index == 0) {
                            linePath.moveTo(x, y)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (index - 1) * stepX
                            val prevVal = dataPoints[index - 1]
                            val prevY = (1f - (prevVal / maxScore)).coerceIn(0f, 1f) * (height - 20f) + 10f

                            // Cubic bezier curve for smooth graph aesthetic
                            val controlPointX1 = prevX + (x - prevX) / 2
                            val controlPointY1 = prevY
                            val controlPointX2 = prevX + (x - prevX) / 2
                            val controlPointY2 = y

                            linePath.cubicTo(controlPointX1, controlPointY1, controlPointX2, controlPointY2, x, y)
                            fillPath.cubicTo(controlPointX1, controlPointY1, controlPointX2, controlPointY2, x, y)
                        }
                    }

                    fillPath.lineTo(width, height)
                    fillPath.close()

                    // Gradient under line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                RedAccent.copy(alpha = 0.35f),
                                PurplePrimary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )

                    // Line stroke
                    drawPath(
                        path = linePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(PurplePrimary, RedAccent)
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw data dots
                    dataPoints.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = (1f - (value / maxScore)).coerceIn(0f, 1f) * (height - 20f) + 10f
                        val isHighlighted = selectedIndex == index || (selectedIndex == null && index == dataPoints.size - 1)

                        drawCircle(
                            color = if (isHighlighted) RedAccent else PurplePrimary,
                            radius = if (isHighlighted) 6.dp.toPx() else 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (isHighlighted) 3.dp.toPx() else 2.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val count = dataPoints.size
            for (i in 0 until count) {
                val label = daysLabels[i % daysLabels.size]
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = if (selectedIndex == i) RedAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selectedIndex == i) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun TechnologyScoreBarsChart(
    getTechScore: (Technology) -> Int,
    isPt: Boolean,
    onOpenTech: (Technology) -> Unit
) {
    val maxTrackScore = 180f // 30 + 60 + 90 = 180 XP maximum for 3 balanced activities

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Technology.entries.forEach { tech ->
            val score = getTechScore(tech)
            val progress = (score / maxTrackScore).coerceIn(0f, 1f)
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(600),
                label = "tech_bar_${tech.id}"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTech(tech) }
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(tech.accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tech.icon,
                                contentDescription = null,
                                tint = tech.accentColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tech.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = "$score / 180 XP (${(progress * 100).toInt()}%)",
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (score > 0) tech.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = tech.accentColor,
                    trackColor = Color(0xFF282338)
                )
            }
        }
    }
}

@Composable
fun DifficultyStatCard(
    title: String,
    points: String,
    color: Color,
    completedCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF14111E),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = points,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$completedCount feitos",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
