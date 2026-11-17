package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Language
import com.example.model.LanguageTaskProgressCalculator
import com.example.model.LanguageTaskProgressSummary
import com.example.model.Technology
import com.example.ui.theme.LocalAccessibilitySettings
import com.example.ui.theme.LocalDevRoutineColors

/**
 * Indicador circular animado de progresso de tarefas concluídas para uma tecnologia.
 */
@Composable
fun LanguageCircularProgressRing(
    progressFraction: Float,
    progressPercent: Int,
    accentColor: Color,
    languageName: String,
    modifier: Modifier = Modifier,
    ringSize: Dp = 68.dp,
    strokeWidth: Dp = 6.dp,
    showCompletedCheck: Boolean = true
) {
    val reduceMotion = LocalAccessibilitySettings.current.reduceMotion
    val animatedFraction by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = if (reduceMotion) {
            spring(stiffness = Spring.StiffnessHigh)
        } else {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
        },
        label = "circular_progress_$languageName"
    )

    val percentTextSize = when {
        ringSize >= 64.dp -> 15.sp
        ringSize >= 52.dp -> 12.sp
        else -> 10.sp
    }

    Box(
        modifier = modifier
            .size(ringSize)
            .semantics {
                contentDescription = "$languageName: $progressPercent%"
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(ringSize - strokeWidth * 1.4f)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.08f))
        )

        CircularProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier.fillMaxSize(),
            color = if (progressPercent >= 100) Color(0xFF22C55E) else accentColor,
            trackColor = accentColor.copy(alpha = 0.18f),
            strokeWidth = strokeWidth,
            strokeCap = StrokeCap.Round
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showCompletedCheck && progressPercent >= 100) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size((ringSize.value * 0.28f).dp)
                )
            }
            Text(
                text = "$progressPercent%",
                fontSize = percentTextSize,
                fontWeight = FontWeight.ExtraBold,
                color = when {
                    progressPercent >= 100 -> Color(0xFF22C55E)
                    progressPercent > 0 -> accentColor
                    else -> MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1
            )
        }
    }
}

/**
 * Selo compacto circular reutilizável para listas e cabeçalhos de tecnologias.
 */
@Composable
fun TechCircularProgressBadge(
    summary: LanguageTaskProgressSummary,
    language: Language,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.testTag("badge_circular_progress_${summary.tech.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        LanguageCircularProgressRing(
            progressFraction = summary.progressFraction,
            progressPercent = summary.progressPercent,
            accentColor = summary.tech.accentColor,
            languageName = summary.tech.displayName,
            ringSize = 46.dp,
            strokeWidth = 4.5.dp,
            showCompletedCheck = false
        )
        Text(
            text = "${summary.completedTasks}/${summary.totalTasks}",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = stringResource(R.string.common_tasks),
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            maxLines = 1
        )
    }
}

/**
 * Seção completa na tela de Progresso exibindo o rastreador visual por linguagem
 * com indicadores circulares, filtros e detalhamento por categoria de estudo.
 */
@Composable
fun LanguageTaskProgressTrackerSection(
    summaries: List<LanguageTaskProgressSummary>,
    language: Language,
    targetSubjectIds: List<String> = emptyList(),
    onOpenTech: (Technology) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDevRoutineColors.current
    val overall = remember(summaries) { LanguageTaskProgressCalculator.computeOverall(summaries) }
    var selectedFilter by remember { mutableIntStateOf(0) }
    var expandedTechId by remember { mutableStateOf<String?>(null) }

    val allFilterLabel = stringResource(R.string.tracker_filter_all, summaries.size)
    val startedFilterLabel = stringResource(R.string.tracker_filter_started, overall.startedLanguagesCount)
    val targetFilterLabel = stringResource(R.string.tracker_filter_target)
    val langsFilterLabel = stringResource(R.string.tracker_filter_languages)
    val dbsFilterLabel = stringResource(R.string.tracker_filter_databases)

    val filters = buildList {
        add(0 to allFilterLabel)
        add(1 to startedFilterLabel)
        if (targetSubjectIds.isNotEmpty()) {
            add(2 to targetFilterLabel)
        }
        add(3 to langsFilterLabel)
        add(4 to dbsFilterLabel)
    }

    val dbIds = remember { setOf(Technology.SQL.id, Technology.MYSQL.id, Technology.POSTGRESQL.id, Technology.MONGODB.id) }

    val filteredSummaries = remember(summaries, selectedFilter, targetSubjectIds) {
        when (selectedFilter) {
            1 -> summaries.filter { it.isStarted }
            2 -> summaries.filter { it.tech.id in targetSubjectIds }
            3 -> summaries.filter { it.tech.id !in dbIds }
            4 -> summaries.filter { it.tech.id in dbIds }
            else -> summaries
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("section_language_task_progress_tracker"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(colors.primary.copy(alpha = 0.6f), colors.accent.copy(alpha = 0.6f))
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Summary Header with overall circular indicator
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(colors.primary, colors.accent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DonutLarge,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.tracker_section_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = stringResource(
                                R.string.tracker_section_subtitle,
                                overall.completedTasks,
                                overall.totalTasks,
                                overall.startedLanguagesCount
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                LanguageCircularProgressRing(
                    progressFraction = overall.progressFraction,
                    progressPercent = overall.progressPercent,
                    accentColor = colors.primary,
                    languageName = stringResource(R.string.tracker_overall_name),
                    ringSize = 54.dp,
                    strokeWidth = 5.dp,
                    showCompletedCheck = false
                )
            }

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters, key = { it.first }) { (filterKey, label) ->
                    val isSelected = selectedFilter == filterKey
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) colors.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) colors.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedFilter = filterKey }
                            .testTag("filter_lang_progress_$filterKey")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Empty filter state
            if (filteredSummaries.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DonutLarge,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = stringResource(R.string.tracker_empty_title),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.tracker_empty_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { onOpenTech(Technology.PYTHON) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.tracker_empty_cta),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                // 2-column Grid of Language Circular Progress Cards
                filteredSummaries.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { summary ->
                            val isExpanded = expandedTechId == summary.tech.id
                            LanguageTaskProgressGridCard(
                                summary = summary,
                                isTarget = summary.tech.id in targetSubjectIds,
                                isExpanded = isExpanded,
                                language = language,
                                onToggleExpand = {
                                    expandedTechId = if (isExpanded) null else summary.tech.id
                                },
                                onOpenTech = { onOpenTech(summary.tech) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageTaskProgressGridCard(
    summary: LanguageTaskProgressSummary,
    isTarget: Boolean,
    isExpanded: Boolean,
    language: Language,
    onToggleExpand: () -> Unit,
    onOpenTech: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val tech = summary.tech

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggleExpand() }
            .testTag("card_lang_progress_${tech.id}"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(
            width = if (isExpanded || isTarget) 1.4.dp else 1.dp,
            color = if (isExpanded || summary.isStarted) {
                tech.accentColor.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row: Tech icon + Name + Expand indicator
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
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(tech.accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tech.icon,
                            contentDescription = tech.displayName,
                            tint = tech.accentColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tech.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = stringResource(R.string.tracker_breakdown_cd),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Central Circular Progress Indicator
            LanguageCircularProgressRing(
                progressFraction = summary.progressFraction,
                progressPercent = summary.progressPercent,
                accentColor = tech.accentColor,
                languageName = tech.displayName,
                ringSize = 70.dp,
                strokeWidth = 6.5.dp
            )

            // Tasks summary pill
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(
                        R.string.tracker_card_tasks_ratio,
                        summary.completedTasks,
                        summary.totalTasks
                    ),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isPt) tech.categoryPt else tech.categoryEn,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Expandable Task Breakdown + Direct Practice Button
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TaskCategoryRow(
                        label = stringResource(R.string.common_lessons),
                        done = summary.completedLessons,
                        total = summary.totalLessons,
                        color = tech.accentColor
                    )
                    TaskCategoryRow(
                        label = stringResource(R.string.common_challenges),
                        done = summary.completedChallenges,
                        total = summary.totalChallenges,
                        color = tech.accentColor
                    )
                    TaskCategoryRow(
                        label = stringResource(R.string.common_quiz),
                        done = summary.completedQuizzes,
                        total = summary.totalQuizzes,
                        color = tech.accentColor
                    )
                    if (summary.totalSessions > 0) {
                        TaskCategoryRow(
                            label = stringResource(R.string.common_sessions),
                            done = summary.completedSessions,
                            total = summary.totalSessions,
                            color = tech.accentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = tech.accentColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenTech() }
                            .testTag("btn_open_tech_from_progress_${tech.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.common_practice),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskCategoryRow(
    label: String,
    done: Int,
    total: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$done/$total",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (done > 0) color else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Strip horizontal compacto para a HomeScreen exibindo o progresso circular
 * de tarefas concluídas para todas as 12 linguagens.
 */
@Composable
fun HomeLanguageProgressStripCard(
    summaries: List<LanguageTaskProgressSummary>,
    language: Language,
    onOpenTech: (Technology) -> Unit,
    onOpenFullAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalDevRoutineColors.current
    val overall = remember(summaries) { LanguageTaskProgressCalculator.computeOverall(summaries) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_home_language_circular_tracker"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(colors.primary.copy(alpha = 0.55f), colors.accent.copy(alpha = 0.55f))
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tracker_home_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                    Text(
                        text = stringResource(
                            R.string.tracker_home_subtitle,
                            overall.completedTasks,
                            overall.totalTasks,
                            overall.progressPercent
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.primary.copy(alpha = 0.16f),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenFullAnalytics() }
                        .testTag("btn_home_view_all_lang_progress")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.tracker_home_analytics),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(summaries, key = { it.tech.id }) { summary ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        border = BorderStroke(
                            1.dp,
                            if (summary.isStarted) summary.tech.accentColor.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                        ),
                        modifier = Modifier
                            .width(96.dp)
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenTech(summary.tech) }
                            .testTag("home_circular_tile_${summary.tech.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LanguageCircularProgressRing(
                                progressFraction = summary.progressFraction,
                                progressPercent = summary.progressPercent,
                                accentColor = summary.tech.accentColor,
                                languageName = summary.tech.displayName,
                                ringSize = 52.dp,
                                strokeWidth = 5.dp,
                                showCompletedCheck = false
                            )
                            Text(
                                text = summary.tech.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${summary.completedTasks}/${summary.totalTasks}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
