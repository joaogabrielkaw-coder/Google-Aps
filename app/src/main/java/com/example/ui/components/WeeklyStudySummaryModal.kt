package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.DailyLanguageChallengeSummary
import com.example.model.Language
import com.example.model.Technology
import com.example.model.UserScoreInfo
import com.example.model.WeeklyStudySummaryFeedback
import com.example.model.WeeklySummaryUiState
import com.example.ui.theme.LocalDevRoutineColors

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

/**
 * Modal interativo "Resumo Semanal com IA" / "Weekly Study Summary".
 * Analisa o histórico real dos últimos 7 dias de desafios concluídos por linguagem,
 * XP e sequência via Firebase AI Logic e apresenta feedback estruturado e acionável.
 */
@Composable
fun WeeklyStudySummaryModal(
    uiState: WeeklySummaryUiState,
    weeklyHistory: List<DailyLanguageChallengeSummary>,
    scoreInfo: UserScoreInfo,
    streakDays: Int,
    language: Language,
    onRetry: () -> Unit,
    onRegenerate: () -> Unit,
    onOpenTech: (Technology) -> Unit,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val totalWeeklyChallenges = remember(weeklyHistory) {
        weeklyHistory.sumOf { it.totalChallenges }
    }
    val activeDaysCount = remember(weeklyHistory) {
        weeklyHistory.count { it.totalChallenges > 0 }
    }
    val topTechThisWeek = remember(weeklyHistory) {
        Technology.entries
            .map { tech -> tech to weeklyHistory.sumOf { day -> day.countsByTech[tech] ?: 0 } }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            border = BorderStroke(1.5.dp, PurplePrimary.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 560.dp)
                .heightIn(max = 720.dp)
                .testTag("modal_weekly_study_summary")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Gradient Crest
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(PurplePrimary, RedAccent)
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isPt) "Resumo Semanal com IA" else "Weekly Study Summary",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = if (isPt) "Feedback personalizado dos últimos 7 dias"
                                    else "AI-powered feedback on your past 7 days",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                                .testTag("btn_close_weekly_summary_modal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isPt) "Fechar resumo semanal" else "Close weekly summary",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Scrollable Modal Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Grounded 7-Day Real Snapshot Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SummaryMetricTile(
                            label = if (isPt) "Desafios (7d)" else "Challenges (7d)",
                            value = "$totalWeeklyChallenges",
                            accent = RedAccent,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricTile(
                            label = if (isPt) "Dias Ativos" else "Active Days",
                            value = "$activeDaysCount/7",
                            accent = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryMetricTile(
                            label = if (isPt) "Destaque" else "Top Tech",
                            value = topTechThisWeek?.first?.badge ?: "Lv.${scoreInfo.levelNumber}",
                            accent = topTechThisWeek?.first?.accentColor ?: PurplePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // AI State Section
                    when (uiState) {
                        is WeeklySummaryUiState.Idle,
                        is WeeklySummaryUiState.Loading -> {
                            WeeklySummaryLoadingCard(isPt = isPt)
                        }

                        is WeeklySummaryUiState.Error -> {
                            WeeklySummaryErrorCard(
                                message = uiState.message,
                                isPt = isPt,
                                onRetry = onRetry
                            )
                        }

                        is WeeklySummaryUiState.Success -> {
                            WeeklySummarySuccessContent(
                                feedback = uiState.feedback,
                                streakDays = streakDays,
                                isPt = isPt,
                                onOpenTech = { tech ->
                                    onDismiss()
                                    onOpenTech(tech)
                                }
                            )
                        }
                    }
                }

                // Footer Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onRegenerate,
                            enabled = uiState !is WeeklySummaryUiState.Loading,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_regenerate_weekly_summary")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPt) "Atualizar IA" else "Refresh AI",
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_done_weekly_summary")
                        ) {
                            Text(
                                text = if (isPt) "Fechar" else "Close",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeeklySummaryLoadingCard(isPt: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_summary_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_summary_loading")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PurplePrimary, RedAccent))),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(30.dp)
                )
            }

            Text(
                text = if (isPt) "Analisando sua semana de código..."
                else "Analyzing your coding week...",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isPt) "A IA está revisando seu histórico de desafios por linguagem, sequência e XP para montar um feedback sob medida."
                else "AI is reviewing your 7-day challenge completion history, streak, and XP growth to craft tailored coaching feedback.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WeeklySummaryErrorCard(
    message: String,
    isPt: Boolean,
    onRetry: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_summary_error")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = if (isPt) "Não foi possível gerar a análise com IA"
                else "Could not generate AI summary",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("btn_retry_weekly_summary")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPt) "Tentar Novamente" else "Try Again",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WeeklySummarySuccessContent(
    feedback: WeeklyStudySummaryFeedback,
    streakDays: Int,
    isPt: Boolean,
    onOpenTech: (Technology) -> Unit
) {
    val recommendedTech = remember(feedback.recommendedTechId) {
        Technology.fromId(feedback.recommendedTechId)
    }

    // 1. Headline & Assessment Hero Card
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_summary_success")
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
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PurplePrimary.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = PurplePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = feedback.consistencyLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurplePrimary
                        )
                    }
                }

                if (streakDays > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.16f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${streakDays}d",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }

            Text(
                text = feedback.headline,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = feedback.overallAssessment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }

    // 2. Strengths Section
    if (feedback.strengths.isNotEmpty()) {
        val emerald = Color(0xFF2E7D32)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = emerald.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, emerald.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = emerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPt) "Pontos Fortes da Semana" else "Weekly Strengths",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = emerald
                        )
                    )
                }

                feedback.strengths.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontWeight = FontWeight.Bold,
                            color = emerald,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // 3. Growth Areas Section
    if (feedback.growthAreas.isNotEmpty()) {
        val amber = Color(0xFFE65100)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = amber.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, amber.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = amber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPt) "Oportunidades de Evolução" else "Growth Opportunities",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = amber
                        )
                    )
                }

                feedback.growthAreas.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            fontWeight = FontWeight.Bold,
                            color = amber,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    // 4. Action Plan + Recommended Tech Spotlight
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, recommendedTech.accentColor.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPt) "Plano para a Próxima Semana" else "Next Week Action Plan",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PurplePrimary.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (isPt) "Meta: ${feedback.recommendedDailyTarget}/dia"
                        else "Goal: ${feedback.recommendedDailyTarget}/day",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = feedback.nextWeekActionPlan,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp
            )

            Button(
                onClick = { onOpenTech(recommendedTech) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = recommendedTech.accentColor),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("btn_open_recommended_tech_${recommendedTech.id}")
            ) {
                Icon(
                    imageVector = recommendedTech.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPt) "Praticar ${recommendedTech.displayName} agora"
                    else "Practice ${recommendedTech.displayName} now",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    // 5. Encouraging Coach Quote
    if (feedback.encouragementQuote.isNotBlank()) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = PurplePrimary.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = PurplePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "\"${feedback.encouragementQuote}\"",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}
