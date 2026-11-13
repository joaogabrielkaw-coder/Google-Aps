package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.UserBadgeEntity
import com.example.data.repository.LanguageDailyProgress
import com.example.model.Language
import com.example.model.Technology
import com.example.ui.theme.LocalDevRoutineColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val PurplePrimaryLight: Color
    @Composable get() = LocalDevRoutineColors.current.primaryLight

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

enum class DailySealTier(
    val key: String,
    val target: Int,
    val xpReward: Int,
    val labelPt: String,
    val labelEn: String,
    val lightColor: Color,
    val mainColor: Color,
    val darkColor: Color
) {
    BRONZE(
        key = "bronze",
        target = 3,
        xpReward = 60,
        labelPt = "Bronze",
        labelEn = "Bronze",
        lightColor = Color(0xFFE6A566),
        mainColor = Color(0xFFCD7F32),
        darkColor = Color(0xFF7D4312)
    ),
    SILVER(
        key = "silver",
        target = 5,
        xpReward = 120,
        labelPt = "Prata",
        labelEn = "Silver",
        lightColor = Color(0xFFF3F6FA),
        mainColor = Color(0xFFB8C2CE),
        darkColor = Color(0xFF64748B)
    ),
    GOLD(
        key = "gold",
        target = 10,
        xpReward = 250,
        labelPt = "Ouro",
        labelEn = "Gold",
        lightColor = Color(0xFFFFF3B0),
        mainColor = Color(0xFFFFD700),
        darkColor = Color(0xFFB45309)
    );

    fun metallicBrush(): Brush = Brush.linearGradient(listOf(lightColor, mainColor, darkColor))
}

fun parseDailySealKey(iconKey: String): Pair<Technology, DailySealTier>? {
    if (!iconKey.startsWith("daily_")) return null
    val suffix = iconKey.removePrefix("daily_")
    val lastUnderscore = suffix.lastIndexOf('_')
    if (lastUnderscore <= 0) return null
    val techId = suffix.substring(0, lastUnderscore)
    val tierKey = suffix.substring(lastUnderscore + 1)
    val tech = Technology.fromId(techId) ?: return null
    val tier = DailySealTier.entries.firstOrNull { it.key == tierKey } ?: return null
    return tech to tier
}

/**
 * Seção completa de Emblemas e Conquistas (Room Database)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserBadgesSection(
    badges: List<UserBadgeEntity>,
    language: Language,
    modifier: Modifier = Modifier,
    todayProgressMap: Map<String, LanguageDailyProgress> = emptyMap(),
    onOpenTech: ((Technology) -> Unit)? = null,
    onBadgeClick: ((UserBadgeEntity) -> Unit)? = null
) {
    val isPt = language == Language.PT
    var selectedCategory by remember { mutableStateOf<String?>(null) } // null = Todos
    var selectedBadgeForDetail by remember { mutableStateOf<UserBadgeEntity?>(null) }

    val categories = remember(badges, isPt) {
        listOf(
            null to (if (isPt) "Todos" else "All"),
            "DAILY_TECH" to (if (isPt) "Selos Diários" else "Daily Seals"),
            "STREAK" to (if (isPt) "Sequências" else "Streaks"),
            "TECH" to (if (isPt) "Tecnologias" else "Tech"),
            "ACTIVITY" to (if (isPt) "Atividades" else "Activities"),
            "TIME" to (if (isPt) "Horários" else "Time"),
            "MILESTONE" to (if (isPt) "Marcos" else "Milestones")
        )
    }

    val dailyBadges = remember(badges) {
        badges.filter { it.category.equals("DAILY_TECH", ignoreCase = true) }
    }
    val unlockedDailyCount = dailyBadges.count { it.isUnlocked }

    val filteredBadges = remember(badges, selectedCategory) {
        when (selectedCategory) {
            null -> badges.filter { !it.category.equals("DAILY_TECH", ignoreCase = true) || it.isUnlocked }
            else -> badges.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    val unlockedCount = badges.count { it.isUnlocked }
    val totalCount = badges.size
    val totalXpEarned = badges.filter { it.isUnlocked }.sumOf { it.xpReward }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_badges_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overview Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(PurplePrimary, RedAccent))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isPt) "Emblemas & Conquistas" else "Badges & Achievements",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isPt) "$unlockedCount de $totalCount Conquistados" else "$unlockedCount of $totalCount Earned",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // XP Bonus pill
                    Surface(
                        color = RedAccent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RedAccent.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = RedAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+$totalXpEarned XP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = RedAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                val progressFraction = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction,
                    animationSpec = tween(600),
                    label = "badge_progress"
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isPt) "Progresso Total de Desbloqueio" else "Total Unlock Progress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(animatedProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = PurplePrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = PurplePrimary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(categories) { (categoryKey, label) ->
                val isSelected = selectedCategory == categoryKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = categoryKey },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("badge_chip_${categoryKey ?: "all"}")
                )
            }
        }

        // Quick highlight card when viewing "All" so Daily Language Seals are immediately discoverable
        if (selectedCategory == null && dailyBadges.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, DailySealTier.GOLD.mainColor.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedCategory = "DAILY_TECH" }
                    .testTag("card_daily_seals_summary")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(DailySealTier.GOLD.metallicBrush()),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFF3E2723),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isPt) "Selos Diários por Linguagem" else "Daily Language Seals",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isPt) "$unlockedDailyCount de ${dailyBadges.size} selos conquistados (3, 5 ou 10 desafios/dia)"
                                else "$unlockedDailyCount of ${dailyBadges.size} seals earned (3, 5, or 10 challenges/day)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = if (isPt) "Ver selos diários" else "View daily seals",
                        tint = DailySealTier.GOLD.mainColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Badges Grid / Language Grouped Daily Seals Showcase
        if (selectedCategory == "DAILY_TECH") {
            LanguageDailySealsShowcase(
                badges = dailyBadges,
                language = language,
                todayProgressMap = todayProgressMap,
                onBadgeClick = { badge ->
                    selectedBadgeForDetail = badge
                    onBadgeClick?.invoke(badge)
                },
                onOpenTech = onOpenTech
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                filteredBadges.forEach { badge ->
                    BadgeCard(
                        badge = badge,
                        language = language,
                        onClick = {
                            selectedBadgeForDetail = badge
                            onBadgeClick?.invoke(badge)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Interactive Badge Details Dialog
    selectedBadgeForDetail?.let { badge ->
        BadgeDetailDialog(
            badge = badge,
            language = language,
            onDismiss = { selectedBadgeForDetail = null }
        )
    }
}

/**
 * Vitrine dedicada aos Selos Diários agrupados pelas 12 linguagens/tecnologias
 * com medalhas metálicas Bronze (3), Prata (5) e Ouro (10).
 */
@Composable
fun LanguageDailySealsShowcase(
    badges: List<UserBadgeEntity>,
    language: Language,
    todayProgressMap: Map<String, LanguageDailyProgress> = emptyMap(),
    onBadgeClick: (UserBadgeEntity) -> Unit,
    onOpenTech: ((Technology) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val badgeById = remember(badges) { badges.associateBy { it.id } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("language_daily_seals_showcase"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Legend / Explanation Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DailySealTier.entries.forEach { tier ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(tier.metallicBrush())
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPt) "${tier.labelPt}: ${tier.target}/dia" else "${tier.labelEn}: ${tier.target}/day",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Technology.entries.forEach { tech ->
            val bronzeBadge = badgeById["badge_daily_${tech.id}_bronze"]
            val silverBadge = badgeById["badge_daily_${tech.id}_silver"]
            val goldBadge = badgeById["badge_daily_${tech.id}_gold"]
            val tierBadges = listOfNotNull(
                bronzeBadge?.let { DailySealTier.BRONZE to it },
                silverBadge?.let { DailySealTier.SILVER to it },
                goldBadge?.let { DailySealTier.GOLD to it }
            )
            val todayCount = todayProgressMap[tech.id]?.completedChallengesCount
                ?: tierBadges.maxOfOrNull { (_, b) -> if (!b.isUnlocked) b.currentProgress else 0 }
                ?: 0
            val unlockedInTech = tierBadges.count { (_, b) -> b.isUnlocked }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(
                    width = if (unlockedInTech > 0) 1.5.dp else 1.dp,
                    color = if (unlockedInTech > 0) tech.accentColor.copy(alpha = 0.65f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_seal_tech_card_${tech.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(tech.accentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = tech.icon,
                                    contentDescription = tech.displayName,
                                    tint = tech.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tech.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (isPt) "$todayCount desafios hoje • $unlockedInTech/3 selos"
                                    else "$todayCount solved today • $unlockedInTech/3 seals",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (onOpenTech != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = tech.accentColor.copy(alpha = 0.16f),
                                modifier = Modifier
                                    .clickable { onOpenTech(tech) }
                                    .testTag("btn_practice_seal_${tech.id}")
                            ) {
                                Text(
                                    text = if (isPt) "Praticar" else "Practice",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tech.accentColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tierBadges.forEach { (tier, badge) ->
                            DailySealMedallionTile(
                                tech = tech,
                                tier = tier,
                                badge = badge,
                                todayCount = todayCount,
                                language = language,
                                onClick = { onBadgeClick(badge) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailySealMedallionTile(
    tech: Technology,
    tier: DailySealTier,
    badge: UserBadgeEntity,
    todayCount: Int,
    language: Language,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val isUnlocked = badge.isUnlocked
    val progressRatio = if (isUnlocked) 1f else (todayCount.toFloat() / tier.target.toFloat()).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isUnlocked) tier.mainColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isUnlocked) 1.5.dp else 1.dp,
            color = if (isUnlocked) tier.mainColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("seal_tile_${tech.id}_${tier.key}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) tier.metallicBrush()
                        else SolidColor(Color.Gray.copy(alpha = 0.22f))
                    )
                    .border(
                        width = if (isUnlocked) 2.dp else 1.dp,
                        color = if (isUnlocked) tier.lightColor else Color.Gray.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) tech.icon else Icons.Default.Lock,
                    contentDescription = badge.title(language),
                    tint = if (isUnlocked) Color(0xFF1F160E) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isPt) tier.labelPt else tier.labelEn,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) tier.mainColor else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isUnlocked) "✓ +${tier.xpReward} XP"
                else "${todayCount.coerceAtMost(tier.target)}/${tier.target} ${if (isPt) "hoje" else "today"}",
                fontSize = 10.sp,
                fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Medium,
                color = if (isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(5.dp))
            LinearProgressIndicator(
                progress = { progressRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = if (isUnlocked) tier.mainColor else tech.accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

/**
 * Cartão de Emblema individual
 */
@Composable
fun BadgeCard(
    badge: UserBadgeEntity,
    language: Language,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val isUnlocked = badge.isUnlocked
    val icon = getBadgeIcon(badge.iconKey)
    val dailySealInfo = remember(badge.iconKey) { parseDailySealKey(badge.iconKey) }
    val themePrimary = PurplePrimary
    val themeAccent = RedAccent
    val activeBrush = remember(dailySealInfo, isUnlocked, themePrimary, themeAccent) {
        if (!isUnlocked) null
        else dailySealInfo?.second?.metallicBrush() ?: Brush.linearGradient(listOf(themePrimary, themeAccent))
    }

    Card(
        modifier = modifier
            .height(180.dp)
            .clickable(onClick = onClick)
            .testTag("badge_card_${badge.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = activeBrush ?: SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            width = if (isUnlocked) 1.5.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with Icon & Unlock status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            activeBrush ?: SolidColor(Color.DarkGray.copy(alpha = 0.4f))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isUnlocked) {
                            if (dailySealInfo != null) Color(0xFF1F160E) else Color.White
                        } else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Status pill
                if (isUnlocked) {
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isPt) "Obtido" else "Earned",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "+${badge.xpReward} XP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Title & Description
            Column {
                Text(
                    text = badge.title(language),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = badge.description(language),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }

            // Progress or Unlock Date
            if (isUnlocked) {
                val formattedDate = remember(badge.unlockedAtMillis) {
                    badge.unlockedAtMillis?.let {
                        val instant = Instant.ofEpochMilli(it)
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            .withZone(ZoneId.systemDefault())
                            .format(instant)
                    } ?: (if (isPt) "Conquistado" else "Achieved")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formattedDate,
                        fontSize = 10.sp,
                        color = PurplePrimaryLight
                    )
                    Text(
                        text = "+${badge.xpReward} XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedAccent
                    )
                }
            } else {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isPt) "Progresso" else "Progress",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${badge.currentProgress}/${badge.maxProgress}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { badge.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape),
                        color = if (badge.progressFraction > 0.5f) RedAccent else PurplePrimary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}

/**
 * Carrossel Horizontal Compacto para exibição rápida na HomeScreen
 */
@Composable
fun UserBadgesCompactRow(
    badges: List<UserBadgeEntity>,
    language: Language,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val unlockedBadges = badges.filter { it.isUnlocked }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("user_badges_compact_row")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = RedAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPt) "Conquistas & Emblemas" else "Badges & Milestones",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = if (isPt) "Ver todos (${unlockedBadges.size}/${badges.size})" else "View all (${unlockedBadges.size}/${badges.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PurplePrimary,
                    modifier = Modifier
                        .clickable(onClick = onViewAll)
                        .padding(4.dp)
                        .testTag("btn_view_all_badges")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (badges.isEmpty()) {
                Text(
                    text = if (isPt) "Carregando conquistas..." else "Loading badges...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(badges.take(6)) { badge ->
                        CompactBadgeChip(
                            badge = badge,
                            language = language,
                            onClick = onViewAll
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactBadgeChip(
    badge: UserBadgeEntity,
    language: Language,
    onClick: () -> Unit
) {
    val isUnlocked = badge.isUnlocked
    val icon = getBadgeIcon(badge.iconKey)
    val dailySealInfo = remember(badge.iconKey) { parseDailySealKey(badge.iconKey) }
    val activeColor = dailySealInfo?.second?.mainColor ?: PurplePrimary
    val activeBrush = dailySealInfo?.second?.metallicBrush() ?: Brush.linearGradient(listOf(PurplePrimary, RedAccent))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isUnlocked) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isUnlocked) activeColor else Color.Transparent
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("compact_badge_${badge.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) activeBrush
                        else SolidColor(Color.Gray.copy(alpha = 0.3f))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) {
                        if (dailySealInfo != null) Color(0xFF1F160E) else Color.White
                    } else Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = badge.title(language),
                    fontSize = 11.sp,
                    fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isUnlocked) "+${badge.xpReward} XP" else "${badge.currentProgress}/${badge.maxProgress}",
                    fontSize = 9.sp,
                    color = if (isUnlocked) RedAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Diálogo Interativo com detalhes do Emblema
 */
@Composable
fun BadgeDetailDialog(
    badge: UserBadgeEntity,
    language: Language,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val isUnlocked = badge.isUnlocked
    val icon = getBadgeIcon(badge.iconKey)
    val dailySealInfo = remember(badge.iconKey) { parseDailySealKey(badge.iconKey) }
    val medalBrush = dailySealInfo?.second?.metallicBrush() ?: Brush.linearGradient(listOf(PurplePrimary, RedAccent))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_badge_detail")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button top-right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Large Glowing Icon
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked) medalBrush
                            else SolidColor(Color.DarkGray.copy(alpha = 0.4f))
                        )
                        .border(
                            width = 2.5.dp,
                            brush = if (isUnlocked) medalBrush else SolidColor(Color.Transparent),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isUnlocked) {
                            if (dailySealInfo != null) Color(0xFF1F160E) else Color.White
                        } else Color.Gray,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = badge.title(language),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                if (dailySealInfo != null) {
                    val (sealTech, sealTier) = dailySealInfo
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = sealTech.accentColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isPt) "${sealTech.displayName} • ${sealTier.labelPt} (${sealTier.target} desafios/dia)"
                            else "${sealTech.displayName} • ${sealTier.labelEn} (${sealTier.target} challenges/day)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = sealTech.accentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = if (isUnlocked) Color(0xFF2E7D32).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isUnlocked) {
                            if (isPt) "✓ Selo/Emblema Conquistado" else "✓ Seal/Badge Earned"
                        } else {
                            if (isPt) "🔒 Em Bloqueio" else "🔒 Locked Milestone"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = badge.description(language),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Progress Info Box
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isPt) "Meta Diária / Marco" else "Daily Target / Milestone",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${badge.currentProgress} / ${badge.maxProgress}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { badge.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = if (isUnlocked) Color(0xFF4CAF50) else (dailySealInfo?.second?.mainColor ?: PurplePrimary),
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isPt) "Recompensa de XP:" else "XP Reward:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "+${badge.xpReward} XP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RedAccent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_close_badge_detail")
                ) {
                    Text(
                        text = if (isPt) "Continuar Focado" else "Keep Learning",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Retorna o ícone Material mais expressivo para a chave informada
 */
fun getBadgeIcon(iconKey: String): ImageVector {
    parseDailySealKey(iconKey)?.let { (tech, _) ->
        return tech.icon
    }
    return when (iconKey.lowercase()) {
        "python" -> Icons.Default.Terminal
        "night_owl" -> Icons.Default.DarkMode
        "clock", "time" -> Icons.Default.AccessTime
        "streak" -> Icons.Default.LocalFireDepartment
        "fire" -> Icons.Default.LocalFireDepartment
        "sql", "database" -> Icons.Default.Storage
        "code", "first_commit" -> Icons.Default.Code
        "trophy" -> Icons.Default.EmojiEvents
        "ai" -> Icons.Default.AutoAwesome
        "star" -> Icons.Default.Star
        else -> Icons.Default.WorkspacePremium
    }
}
