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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.UserBadgeEntity
import com.example.model.Language
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryLight
import com.example.ui.theme.RedAccent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Seção completa de Emblemas e Conquistas (Room Database)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserBadgesSection(
    badges: List<UserBadgeEntity>,
    language: Language,
    modifier: Modifier = Modifier,
    onBadgeClick: ((UserBadgeEntity) -> Unit)? = null
) {
    val isPt = language == Language.PT
    var selectedCategory by remember { mutableStateOf<String?>(null) } // null = Todos
    var selectedBadgeForDetail by remember { mutableStateOf<UserBadgeEntity?>(null) }

    val categories = remember(badges) {
        listOf(
            null to (if (isPt) "Todos" else "All"),
            "STREAK" to (if (isPt) "Sequências" else "Streaks"),
            "TECH" to (if (isPt) "Tecnologias" else "Tech"),
            "ACTIVITY" to (if (isPt) "Atividades" else "Activities"),
            "TIME" to (if (isPt) "Horários" else "Time"),
            "MILESTONE" to (if (isPt) "Marcos" else "Milestones")
        )
    }

    val filteredBadges = remember(badges, selectedCategory) {
        if (selectedCategory == null) badges
        else badges.filter { it.category.equals(selectedCategory, ignoreCase = true) }
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

        // Badges Grid / Cards
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
            brush = if (isUnlocked) {
                Brush.linearGradient(listOf(PurplePrimary, RedAccent))
            } else {
                androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            },
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
                            if (isUnlocked) {
                                Brush.linearGradient(listOf(PurplePrimary, RedAccent))
                            } else {
                                androidx.compose.ui.graphics.SolidColor(Color.DarkGray.copy(alpha = 0.4f))
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isUnlocked) Color.White else Color.Gray,
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

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isUnlocked) PurplePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isUnlocked) PurplePrimary else Color.Transparent
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
                        if (isUnlocked) Brush.linearGradient(listOf(PurplePrimary, RedAccent))
                        else androidx.compose.ui.graphics.SolidColor(Color.Gray.copy(alpha = 0.3f))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) Color.White else Color.Gray,
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
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Large Glowing Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isUnlocked) Brush.linearGradient(listOf(PurplePrimary, RedAccent))
                            else androidx.compose.ui.graphics.SolidColor(Color.DarkGray.copy(alpha = 0.4f))
                        )
                        .border(
                            width = 2.dp,
                            brush = if (isUnlocked) Brush.linearGradient(listOf(RedAccent, PurplePrimary))
                            else androidx.compose.ui.graphics.SolidColor(Color.Transparent),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) icon else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isUnlocked) Color.White else Color.Gray,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = badge.title(language),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = if (isUnlocked) Color(0xFF2E7D32).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isUnlocked) {
                            if (isPt) "✓ Emblema Conquistado" else "✓ Badge Earned"
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
                                text = if (isPt) "Meta do Emblema" else "Badge Target",
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
                            color = if (isUnlocked) Color(0xFF4CAF50) else PurplePrimary,
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
