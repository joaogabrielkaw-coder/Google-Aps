package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.LanguageDailyProgress
import com.example.model.Language
import com.example.model.Technology
import com.example.ui.theme.LocalDevRoutineColors
import com.example.ui.theme.TechCssColor
import com.example.ui.theme.TechGoColor
import com.example.ui.theme.TechHtmlColor
import com.example.ui.theme.TechJavaColor
import com.example.ui.theme.TechJsColor
import com.example.ui.theme.TechKotlinColor
import com.example.ui.theme.TechMongoColor
import com.example.ui.theme.TechMySqlColor
import com.example.ui.theme.TechPostgresColor
import com.example.ui.theme.TechPythonColor
import com.example.ui.theme.TechSqlColor
import com.example.ui.theme.TechTsColor

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val PurplePrimaryLight: Color
    @Composable get() = LocalDevRoutineColors.current.primaryLight

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

fun getTechnologyColor(tech: Technology): Color {
    return when (tech) {
        Technology.PYTHON -> TechPythonColor
        Technology.SQL -> TechSqlColor
        Technology.HTML -> TechHtmlColor
        Technology.CSS -> TechCssColor
        Technology.JAVA -> TechJavaColor
        Technology.MYSQL -> TechMySqlColor
        Technology.POSTGRESQL -> TechPostgresColor
        Technology.MONGODB -> TechMongoColor
        Technology.JAVASCRIPT -> TechJsColor
        Technology.TYPESCRIPT -> TechTsColor
        Technology.KOTLIN -> TechKotlinColor
        Technology.GO -> TechGoColor
    }
}

@Composable
fun DailyStudyProgressTrackerCard(
    language: Language,
    selectedTech: Technology,
    onSelectTech: (Technology) -> Unit,
    todayProgressMap: Map<String, LanguageDailyProgress>,
    isLoggedIn: Boolean,
    onOpenTechDetail: (Technology) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val progress = todayProgressMap[selectedTech.id] ?: LanguageDailyProgress(
        techId = selectedTech.id,
        techName = selectedTech.displayName,
        isSyncedToCloud = isLoggedIn
    )
    val techColor = getTechnologyColor(selectedTech)

    // Total studied languages count today
    val studiedCount = todayProgressMap.values.count { it.xpEarned > 0 || it.minutesStudied > 0 || it.completedChallengesCount > 0 }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_daily_study_tracker"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(techColor.copy(alpha = 0.5f), PurplePrimary.copy(alpha = 0.3f))),
            width = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Title & Firestore Cloud Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(listOf(PurplePrimary, RedAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isPt) "Rastreador Diário de Estudos" else "Daily Study Tracker",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = if (isPt) "Progresso salvo na nuvem com Firestore" else "Progress synced to Cloud Firestore",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Cloud Sync Indicator
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLoggedIn) Color(0xFF1B5E20).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(
                            listOf(
                                if (isLoggedIn) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        width = 1.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLoggedIn) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = if (isLoggedIn) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLoggedIn) "Firestore" else (if (isPt) "Local" else "Local"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLoggedIn) Color(0xFF81C784) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            // Horizontal Technology Selector Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isPt) "Selecione a linguagem para acompanhar seu progresso:" else "Select a language to track your progress:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(Technology.values()) { tech ->
                        val isSelected = tech == selectedTech
                        val color = getTechnologyColor(tech)
                        val techProgress = todayProgressMap[tech.id]
                        val hasStudiedToday = techProgress != null && (techProgress.xpEarned > 0 || techProgress.minutesStudied > 0 || techProgress.completedChallengesCount > 0)

                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectTech(tech) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (hasStudiedToday) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Praticado hoje",
                                            tint = if (isSelected) Color.White else Color(0xFF4CAF50),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = tech.displayName,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = color.copy(alpha = 0.4f),
                                selectedBorderColor = color
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("chip_tracker_tech_${tech.id}")
                        )
                    }
                }
            }

            // Detailed Progress Box for Selected Technology
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(techColor.copy(alpha = 0.10f))
                    .border(1.dp, techColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Header of Selected Tech
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
                                    .background(techColor.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedTech.displayName.take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = techColor,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedTech.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        // Daily Goal Status Tag
                        val dailyGoalXp = 60
                        val goalProgress = (progress.xpEarned.toFloat() / dailyGoalXp).coerceIn(0f, 1f)
                        val isGoalMet = progress.xpEarned >= dailyGoalXp

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isGoalMet) Color(0xFF2E7D32).copy(alpha = 0.2f) else PurplePrimary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isGoalMet) Icons.Default.Check else Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (isGoalMet) Color(0xFF4CAF50) else PurplePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isGoalMet) (if (isPt) "Meta atingida!" else "Goal met!")
                                    else "${progress.xpEarned}/$dailyGoalXp XP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = if (isGoalMet) Color(0xFF81C784) else PurplePrimaryLight
                                    )
                                )
                            }
                        }
                    }

                    // Progress Bar towards Daily Goal
                    val dailyGoalXp = 60
                    val goalFraction = (progress.xpEarned.toFloat() / dailyGoalXp).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isPt) "Meta diária (${progress.xpEarned}/$dailyGoalXp XP)" else "Daily goal (${progress.xpEarned}/$dailyGoalXp XP)",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = "${(goalFraction * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = techColor)
                            )
                        }
                        LinearProgressIndicator(
                            progress = { goalFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = techColor,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    }

                    // 4 Metric Badges in 2x2 Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricPill(
                            icon = Icons.Default.Star,
                            iconColor = PurplePrimaryLight,
                            label = if (isPt) "XP Hoje" else "Today's XP",
                            value = "+${progress.xpEarned} XP",
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            icon = Icons.Default.Timer,
                            iconColor = RedAccent,
                            label = if (isPt) "Tempo" else "Study Time",
                            value = "${progress.minutesStudied} min",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricPill(
                            icon = Icons.Default.CheckCircle,
                            iconColor = Color(0xFF4CAF50),
                            label = if (isPt) "Desafios" else "Challenges",
                            value = "${progress.completedChallengesCount} feitos",
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            icon = Icons.Default.School,
                            iconColor = TechMySqlColor,
                            label = if (isPt) "Quizzes" else "Quizzes",
                            value = "${progress.completedQuizCount} feitos",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Tempo automático + botão para praticar desafios (consumindo vidas reais)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPt) "⏱️ Tempo contabilizado automaticamente pelo uso real no app." else "⏱️ Time counted automatically from real app usage.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.5.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = { onOpenTechDetail(selectedTech) },
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("btn_practice_${selectedTech.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = techColor),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Text(
                                text = if (isPt) "Praticar" else "Practice",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Daily Summary Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPt) "Linguagens praticadas hoje: $studiedCount de ${Technology.entries.size}"
                    else "Languages practiced today: $studiedCount of ${Technology.entries.size}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                )

                Text(
                    text = if (isPt) "Sincronizado automaticamente" else "Auto-synced to Cloud",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PurplePrimaryLight,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.5.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        border = CardDefaults.outlinedCardBorder().copy(width = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}
