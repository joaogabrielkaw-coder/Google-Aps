package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.ViewList
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.DailyLanguageChallengeSummary
import com.example.model.Language
import com.example.model.Technology
import com.example.ui.theme.LocalDevRoutineColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

enum class WeeklyChartMode(val labelPt: String, val labelEn: String) {
    STACKED_BAR("Barras", "Stacked Bar"),
    LINE_TREND("Tendência", "Line Trend"),
    BY_LANGUAGE("Por Linguagem", "By Language")
}

/**
 * Painel Semanal de Desafios Concluídos por Linguagem (estilo Recharts nativo em Jetpack Compose).
 * Exibe métricas dos últimos 7 dias, gráficos interativos empilhados/linhas, legenda filtrável
 * por linguagem e tooltip ao tocar em qualquer dia da semana.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeeklyChallengesDashboardCard(
    weeklyHistory: List<DailyLanguageChallengeSummary>,
    language: Language,
    onOpenTech: (Technology) -> Unit,
    onOpenAiSummary: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    var chartMode by remember { mutableStateOf(WeeklyChartMode.STACKED_BAR) }
    var selectedTechFilter by remember { mutableStateOf<Technology?>(null) }
    var selectedDayIndex by remember(weeklyHistory) {
        mutableStateOf<Int?>(if (weeklyHistory.isNotEmpty()) weeklyHistory.lastIndex else null)
    }

    // Aggregate weekly totals per technology
    val totalsByTech = remember(weeklyHistory) {
        Technology.entries.associateWith { tech ->
            weeklyHistory.sumOf { day -> day.countsByTech[tech] ?: 0 }
        }
    }

    val activeTechs = remember(totalsByTech) {
        Technology.entries.filter { (totalsByTech[it] ?: 0) > 0 }
            .sortedByDescending { totalsByTech[it] ?: 0 }
    }

    val displayedTechs = remember(selectedTechFilter, activeTechs) {
        when {
            selectedTechFilter != null -> listOf(selectedTechFilter!!)
            activeTechs.isNotEmpty() -> activeTechs
            else -> Technology.entries.take(4)
        }
    }

    val totalWeeklyChallenges = remember(weeklyHistory, selectedTechFilter) {
        weeklyHistory.sumOf { day ->
            if (selectedTechFilter != null) day.countsByTech[selectedTechFilter] ?: 0
            else day.totalChallenges
        }
    }

    val activeDaysCount = remember(weeklyHistory, selectedTechFilter) {
        weeklyHistory.count { day ->
            val count = if (selectedTechFilter != null) day.countsByTech[selectedTechFilter] ?: 0
            else day.totalChallenges
            count > 0
        }
    }

    val topTech = remember(activeTechs) { activeTechs.firstOrNull() }
    val topTechCount = topTech?.let { totalsByTech[it] ?: 0 } ?: 0

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.28f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_weekly_challenges_dashboard")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PurplePrimary, RedAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPt) "Desafios na Última Semana" else "Past 7 Days Challenges",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isPt) "Distribuição diária por linguagem (últimos 7 dias)"
                            else "Daily challenges completed per language (7-day window)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PurplePrimary.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = if (isPt) "7 dias" else "7d",
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // KPI Summary Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyKpiPill(
                    label = if (isPt) "Total Semanal" else "Weekly Total",
                    value = "$totalWeeklyChallenges",
                    accentColor = RedAccent,
                    modifier = Modifier.weight(1f)
                )
                WeeklyKpiPill(
                    label = if (isPt) "Linguagem Top" else "Top Language",
                    value = if (topTech != null) "${topTech.badge} ($topTechCount)" else "—",
                    accentColor = topTech?.accentColor ?: PurplePrimary,
                    modifier = Modifier.weight(1f)
                )
                WeeklyKpiPill(
                    label = if (isPt) "Dias Ativos" else "Active Days",
                    value = "$activeDaysCount/7",
                    accentColor = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }

            if (onOpenAiSummary != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onOpenAiSummary,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("btn_open_weekly_ai_summary")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPt) "✨ Gerar Resumo Semanal com IA" else "✨ View AI Weekly Study Summary",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Mode Selector (Stacked Bar / Line Trend / By Language)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WeeklyChartMode.entries.forEach { mode ->
                    val isSelected = chartMode == mode
                    val icon = when (mode) {
                        WeeklyChartMode.STACKED_BAR -> Icons.Default.BarChart
                        WeeklyChartMode.LINE_TREND -> Icons.Default.ShowChart
                        WeeklyChartMode.BY_LANGUAGE -> Icons.Default.ViewList
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) PurplePrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clickable { chartMode = mode }
                            .testTag("btn_chart_mode_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPt) mode.labelPt else mode.labelEn,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Language Legend Filter Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTechFilter == null,
                        onClick = { selectedTechFilter = null },
                        label = {
                            Text(
                                text = if (isPt) "Todas (${Technology.entries.size})" else "All (${Technology.entries.size})",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PurplePrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_weekly_filter_all")
                    )
                }

                val chipsList = activeTechs.ifEmpty { Technology.entries }
                items(chipsList) { tech ->
                    val count = totalsByTech[tech] ?: 0
                    val isSelected = selectedTechFilter == tech
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedTechFilter = if (isSelected) null else tech
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else tech.accentColor)
                            )
                        },
                        label = {
                            Text(
                                text = "${tech.displayName} ($count)",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = tech.accentColor,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("chip_weekly_filter_${tech.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Chart Visualization Container
            when (chartMode) {
                WeeklyChartMode.STACKED_BAR -> {
                    WeeklyStackedBarChart(
                        weeklyHistory = weeklyHistory,
                        displayedTechs = displayedTechs,
                        selectedTechFilter = selectedTechFilter,
                        selectedDayIndex = selectedDayIndex,
                        onSelectDay = { idx -> selectedDayIndex = idx },
                        isPt = isPt
                    )
                }

                WeeklyChartMode.LINE_TREND -> {
                    WeeklyMultiLineTrendChart(
                        weeklyHistory = weeklyHistory,
                        displayedTechs = displayedTechs,
                        selectedTechFilter = selectedTechFilter,
                        selectedDayIndex = selectedDayIndex,
                        onSelectDay = { idx -> selectedDayIndex = idx },
                        isPt = isPt
                    )
                }

                WeeklyChartMode.BY_LANGUAGE -> {
                    WeeklyByLanguageBreakdownChart(
                        totalsByTech = totalsByTech,
                        isPt = isPt,
                        onOpenTech = onOpenTech
                    )
                }
            }

            // Recharts-style Interactive Day Tooltip Card
            AnimatedVisibility(visible = chartMode != WeeklyChartMode.BY_LANGUAGE && selectedDayIndex != null) {
                val safeIndex = selectedDayIndex?.coerceIn(0, (weeklyHistory.size - 1).coerceAtLeast(0))
                val selectedDay = safeIndex?.let { weeklyHistory.getOrNull(it) }
                if (selectedDay != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    WeeklyDayTooltipCard(
                        daySummary = selectedDay,
                        selectedTechFilter = selectedTechFilter,
                        isPt = isPt,
                        onOpenTech = onOpenTech
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyKpiPill(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeeklyStackedBarChart(
    weeklyHistory: List<DailyLanguageChallengeSummary>,
    displayedTechs: List<Technology>,
    selectedTechFilter: Technology?,
    selectedDayIndex: Int?,
    onSelectDay: (Int) -> Unit,
    isPt: Boolean
) {
    val dayTotals = remember(weeklyHistory, selectedTechFilter) {
        weeklyHistory.map { day ->
            if (selectedTechFilter != null) day.countsByTech[selectedTechFilter] ?: 0
            else day.totalChallenges
        }
    }
    val rawMax = dayTotals.maxOrNull() ?: 0
    val yMax = remember(rawMax) {
        when {
            rawMax <= 4 -> 4
            rawMax <= 10 -> ((rawMax + 1) / 2 * 2).coerceAtLeast(6)
            else -> (ceil(rawMax / 5.0).toInt() * 5)
        }
    }
    val yMid = yMax / 2
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 550),
        label = "weekly_bar_anim"
    )

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF151221), Color(0xFF100D18))
                    )
                )
                .padding(start = 10.dp, end = 14.dp, top = 16.dp, bottom = 10.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Y-Axis Ticks
                Column(
                    modifier = Modifier
                        .width(26.dp)
                        .height(160.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(text = "$yMax", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                    Text(text = "$yMid", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                    Text(text = "0", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .pointerInput(weeklyHistory) {
                                detectTapGestures { offset ->
                                    if (weeklyHistory.isNotEmpty()) {
                                        val slotW = size.width / weeklyHistory.size
                                        val idx = (offset.x / slotW).toInt().coerceIn(0, weeklyHistory.lastIndex)
                                        onSelectDay(idx)
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                        // Cartesian dashed grid lines
                        val gridSteps = 4
                        for (i in 0..gridSteps) {
                            val y = height * (i.toFloat() / gridSteps)
                            drawLine(
                                color = Color(0xFF2E2842),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = if (i == gridSteps) null else dashEffect
                            )
                        }

                        if (weeklyHistory.isNotEmpty()) {
                            val slotWidth = width / weeklyHistory.size
                            val barWidth = (slotWidth * 0.52f).coerceIn(14.dp.toPx(), 32.dp.toPx())

                            weeklyHistory.forEachIndexed { dayIndex, summary ->
                                val centerX = dayIndex * slotWidth + slotWidth / 2f
                                val isHighlighted = selectedDayIndex == dayIndex

                                if (isHighlighted) {
                                    drawRoundRect(
                                        color = Color.White.copy(alpha = 0.06f),
                                        topLeft = Offset(dayIndex * slotWidth + 2.dp.toPx(), 0f),
                                        size = Size(slotWidth - 4.dp.toPx(), height),
                                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                    )
                                }

                                val stacks = if (selectedTechFilter != null) {
                                    listOf(selectedTechFilter to (summary.countsByTech[selectedTechFilter] ?: 0))
                                } else {
                                    displayedTechs.map { tech -> tech to (summary.countsByTech[tech] ?: 0) }
                                }.filter { it.second > 0 }

                                var currentTopY = height
                                stacks.forEachIndexed { stackIdx, (tech, count) ->
                                    val segHeight = (count.toFloat() / yMax.toFloat()) * height * animProgress
                                    val clampedHeight = segHeight.coerceAtLeast(3.dp.toPx())
                                    val topY = (currentTopY - clampedHeight).coerceAtLeast(0f)
                                    val isTopMost = stackIdx == stacks.lastIndex

                                    drawRoundRect(
                                        color = tech.accentColor,
                                        topLeft = Offset(centerX - barWidth / 2f, topY),
                                        size = Size(barWidth, currentTopY - topY),
                                        cornerRadius = if (isTopMost) CornerRadius(6.dp.toPx(), 6.dp.toPx()) else CornerRadius.Zero
                                    )
                                    currentTopY = topY
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // X-Axis Weekday Labels
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weeklyHistory.forEachIndexed { idx, summary ->
                            val isSelected = selectedDayIndex == idx
                            Text(
                                text = formatShortWeekday(summary.dateIso, isPt),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSelectDay(idx) }
                            )
                        }
                    }
                }
            }

            if (rawMax == 0) {
                EmptyWeeklyChartOverlay(isPt = isPt, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun WeeklyMultiLineTrendChart(
    weeklyHistory: List<DailyLanguageChallengeSummary>,
    displayedTechs: List<Technology>,
    selectedTechFilter: Technology?,
    selectedDayIndex: Int?,
    onSelectDay: (Int) -> Unit,
    isPt: Boolean
) {
    val activeSeries = remember(weeklyHistory, displayedTechs, selectedTechFilter) {
        if (selectedTechFilter != null) listOf(selectedTechFilter)
        else displayedTechs.take(5)
    }

    val rawMax = remember(weeklyHistory, activeSeries) {
        weeklyHistory.maxOfOrNull { day ->
            activeSeries.maxOfOrNull { tech -> day.countsByTech[tech] ?: 0 } ?: 0
        } ?: 0
    }
    val yMax = remember(rawMax) {
        when {
            rawMax <= 4 -> 4
            rawMax <= 10 -> ((rawMax + 1) / 2 * 2).coerceAtLeast(6)
            else -> (ceil(rawMax / 5.0).toInt() * 5)
        }
    }
    val yMid = yMax / 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF151221), Color(0xFF100D18))))
            .padding(start = 10.dp, end = 14.dp, top = 16.dp, bottom = 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .width(26.dp)
                    .height(160.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "$yMax", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                Text(text = "$yMid", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                Text(text = "0", fontSize = 10.sp, color = Color(0xFF9CA3AF))
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .pointerInput(weeklyHistory) {
                            detectTapGestures { offset ->
                                if (weeklyHistory.size >= 2) {
                                    val step = size.width / (weeklyHistory.size - 1)
                                    val idx = ((offset.x + step / 2f) / step).toInt().coerceIn(0, weeklyHistory.lastIndex)
                                    onSelectDay(idx)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                    for (i in 0..4) {
                        val y = height * (i.toFloat() / 4f)
                        drawLine(
                            color = Color(0xFF2E2842),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = if (i == 4) null else dashEffect
                        )
                    }

                    if (weeklyHistory.size >= 2) {
                        val padX = 8.dp.toPx()
                        val usableW = (width - padX * 2).coerceAtLeast(1f)
                        val stepX = usableW / (weeklyHistory.size - 1)

                        selectedDayIndex?.let { selIdx ->
                            if (selIdx in weeklyHistory.indices) {
                                val cursorX = padX + selIdx * stepX
                                drawLine(
                                    color = Color.White.copy(alpha = 0.35f),
                                    start = Offset(cursorX, 0f),
                                    end = Offset(cursorX, height),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = dashEffect
                                )
                            }
                        }

                        activeSeries.forEach { tech ->
                            val linePath = Path()
                            val fillPath = Path()
                            val points = weeklyHistory.mapIndexed { idx, summary ->
                                val v = summary.countsByTech[tech] ?: 0
                                val x = padX + idx * stepX
                                val y = (1f - (v.toFloat() / yMax.toFloat())).coerceIn(0f, 1f) * (height - 12.dp.toPx()) + 6.dp.toPx()
                                Offset(x, y)
                            }

                            points.forEachIndexed { idx, pt ->
                                if (idx == 0) {
                                    linePath.moveTo(pt.x, pt.y)
                                    fillPath.moveTo(pt.x, height)
                                    fillPath.lineTo(pt.x, pt.y)
                                } else {
                                    val prev = points[idx - 1]
                                    val c1x = prev.x + (pt.x - prev.x) / 2f
                                    linePath.cubicTo(c1x, prev.y, c1x, pt.y, pt.x, pt.y)
                                    fillPath.cubicTo(c1x, prev.y, c1x, pt.y, pt.x, pt.y)
                                }
                            }

                            if (activeSeries.size == 1 && points.isNotEmpty()) {
                                fillPath.lineTo(points.last().x, height)
                                fillPath.close()
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        listOf(tech.accentColor.copy(alpha = 0.32f), Color.Transparent)
                                    )
                                )
                            }

                            drawPath(
                                path = linePath,
                                color = tech.accentColor,
                                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                            )

                            points.forEachIndexed { idx, pt ->
                                val isSel = selectedDayIndex == idx
                                drawCircle(
                                    color = tech.accentColor,
                                    radius = if (isSel) 5.5.dp.toPx() else 3.5.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = Color(0xFF151221),
                                    radius = if (isSel) 2.5.dp.toPx() else 1.5.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    weeklyHistory.forEachIndexed { idx, summary ->
                        val isSelected = selectedDayIndex == idx
                        Text(
                            text = formatShortWeekday(summary.dateIso, isPt),
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectDay(idx) }
                        )
                    }
                }
            }
        }

        if (rawMax == 0) {
            EmptyWeeklyChartOverlay(isPt = isPt, modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun WeeklyByLanguageBreakdownChart(
    totalsByTech: Map<Technology, Int>,
    isPt: Boolean,
    onOpenTech: (Technology) -> Unit
) {
    val sortedTechs = remember(totalsByTech) {
        Technology.entries.sortedByDescending { totalsByTech[it] ?: 0 }
    }
    val maxCount = (totalsByTech.values.maxOrNull() ?: 1).coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF151221))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        sortedTechs.forEach { tech ->
            val count = totalsByTech[tech] ?: 0
            val fraction = (count.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
            val animFraction by animateFloatAsState(
                targetValue = fraction,
                animationSpec = tween(500),
                label = "weekly_tech_bar_${tech.id}"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenTech(tech) }
                    .padding(vertical = 3.dp, horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = tech.icon,
                            contentDescription = null,
                            tint = tech.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tech.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = if (isPt) "$count desafios" else "$count challenges",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (count > 0) tech.accentColor else Color(0xFF9CA3AF)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { animFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape),
                    color = tech.accentColor,
                    trackColor = Color(0xFF282338)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeeklyDayTooltipCard(
    daySummary: DailyLanguageChallengeSummary,
    selectedTechFilter: Technology?,
    isPt: Boolean,
    onOpenTech: (Technology) -> Unit
) {
    val breakdown = remember(daySummary, selectedTechFilter) {
        Technology.entries
            .filter { selectedTechFilter == null || it == selectedTechFilter }
            .mapNotNull { tech ->
                val count = daySummary.countsByTech[tech] ?: 0
                if (count > 0) tech to count else null
            }
            .sortedByDescending { it.second }
    }
    val dayTotal = breakdown.sumOf { it.second }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weekly_day_tooltip_${daySummary.dateIso}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = PurplePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatFullDayTitle(daySummary.dateIso, isPt),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (dayTotal > 0) RedAccent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isPt) "$dayTotal concluídos" else "$dayTotal solved",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (dayTotal > 0) RedAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (breakdown.isEmpty()) {
                Text(
                    text = if (isPt) "Nenhum desafio prático registrado neste dia."
                    else "No practical challenges logged on this day.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    breakdown.forEach { (tech, count) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = tech.accentColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, tech.accentColor.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { onOpenTech(tech) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(tech.accentColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${tech.displayName}: $count",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyWeeklyChartOverlay(
    isPt: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1D182C).copy(alpha = 0.92f),
        border = BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.4f)),
        modifier = modifier.padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = PurplePrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isPt) "Resolva desafios práticos para preencher a semana!"
                else "Complete coding challenges to populate your 7-day chart!",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatShortWeekday(dateIso: String, isPt: Boolean): String {
    return try {
        val date = LocalDate.parse(dateIso)
        val locale = if (isPt) Locale.forLanguageTag("pt-BR") else Locale.ENGLISH
        val dayName = date.format(DateTimeFormatter.ofPattern("EEE", locale))
            .replace(".", "")
            .replaceFirstChar { it.uppercase() }
        val dayMonth = date.format(DateTimeFormatter.ofPattern("dd/MM"))
        "$dayName\n$dayMonth"
    } catch (e: Exception) {
        dateIso
    }
}

private fun formatFullDayTitle(dateIso: String, isPt: Boolean): String {
    return try {
        val date = LocalDate.parse(dateIso)
        val today = LocalDate.now()
        val locale = if (isPt) Locale.forLanguageTag("pt-BR") else Locale.ENGLISH
        val formatted = date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM", locale))
            .replaceFirstChar { it.uppercase() }
        if (date == today) {
            if (isPt) "Hoje • $formatted" else "Today • $formatted"
        } else {
            formatted
        }
    } catch (e: Exception) {
        dateIso
    }
}
