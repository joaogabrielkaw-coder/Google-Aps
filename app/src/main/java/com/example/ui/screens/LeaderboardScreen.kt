package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.LeaderboardEntry
import com.example.model.UserAccount
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurplePrimaryLight
import com.example.ui.theme.RedAccent

@Composable
fun LeaderboardScreen(
    leaderboardEntries: List<LeaderboardEntry>,
    currentUserAccount: UserAccount?,
    language: Language,
    onOpenAuthDialog: () -> Unit,
    onSyncLeaderboard: () -> Unit
) {
    val isPt = language == Language.PT

    // Top 3 Podium devs
    val top1 = leaderboardEntries.getOrNull(0)
    val top2 = leaderboardEntries.getOrNull(1)
    val top3 = leaderboardEntries.getOrNull(2)

    val remainingDevs = remember(leaderboardEntries) {
        if (leaderboardEntries.size > 3) leaderboardEntries.drop(3) else emptyList()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_leaderboard"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PurplePrimary, RedAccent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPt) "Placar de Desenvolvedores" else "Developer Leaderboard",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isPt) "Ranking oficial por XP, sequências e desafios"
                                else "Official ranking by XP, streaks, and challenges",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Top Action: Login or Sync
                    if (currentUserAccount?.isLoggedIn == true) {
                        IconButton(
                            onClick = onOpenAuthDialog,
                            modifier = Modifier.testTag("btn_top_profile_leaderboard")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(PurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUserAccount.displayName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onOpenAuthDialog,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_top_login_leaderboard")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPt) "Entrar" else "Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Current User Status Strip
        item {
            CurrentUserRankingCard(
                user = currentUserAccount,
                leaderboardEntries = leaderboardEntries,
                isPt = isPt,
                onLogin = onOpenAuthDialog,
                onSync = onSyncLeaderboard
            )
        }

        // Top 3 Podium Visual
        if (top1 != null) {
            item {
                PodiumCard(
                    top1 = top1,
                    top2 = top2,
                    top3 = top3,
                    isPt = isPt
                )
            }
        }

        // Header for remaining list
        item {
            Text(
                text = if (isPt) "Demais Desenvolvedores no Ranking" else "Remaining Developers in Ranking",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // List of remaining developers
        itemsIndexed(remainingDevs) { index, entry ->
            val position = index + 4
            LeaderboardEntryRow(
                entry = entry,
                position = position,
                isPt = isPt
            )
        }
    }
}

/**
 * Pódio visual dos 3 melhores desenvolvedores
 */
@Composable
fun PodiumCard(
    top1: LeaderboardEntry,
    top2: LeaderboardEntry?,
    top3: LeaderboardEntry?,
    isPt: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("podium_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isPt) "🏆 Top 3 da Comunidade" else "🏆 Community Top 3",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                if (top2 != null) {
                    PodiumColumn(
                        entry = top2,
                        place = 2,
                        haloColor = Color(0xFFC0C0C0),
                        podiumHeight = 85.dp,
                        isPt = isPt,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // 1st Place (Gold - Center)
                PodiumColumn(
                    entry = top1,
                    place = 1,
                    haloColor = Color(0xFFFFD700),
                    podiumHeight = 110.dp,
                    isPt = isPt,
                    modifier = Modifier.weight(1.1f)
                )

                // 3rd Place (Bronze)
                if (top3 != null) {
                    PodiumColumn(
                        entry = top3,
                        place = 3,
                        haloColor = Color(0xFFCD7F32),
                        podiumHeight = 70.dp,
                        isPt = isPt,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PodiumColumn(
    entry: LeaderboardEntry,
    place: Int,
    haloColor: Color,
    podiumHeight: androidx.compose.ui.unit.Dp,
    isPt: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Place badge
        Surface(
            shape = CircleShape,
            color = haloColor.copy(alpha = 0.25f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, haloColor)
        ) {
            Text(
                text = if (place == 1) "👑 1º" else "${place}º",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = haloColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(if (place == 1) 54.dp else 46.dp)
                .clip(CircleShape)
                .background(
                    if (place == 1) Brush.linearGradient(listOf(PurplePrimary, RedAccent))
                    else Brush.linearGradient(listOf(PurplePrimary.copy(alpha = 0.6f), haloColor))
                )
                .border(2.dp, haloColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.displayName.take(2).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (place == 1) 16.sp else 13.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = entry.displayName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${entry.totalScore} XP",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (place == 1) RedAccent else PurplePrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Podium step block
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = haloColor.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, haloColor.copy(alpha = 0.4f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$place",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = haloColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Card com informações e posição do usuário atual
 */
@Composable
fun CurrentUserRankingCard(
    user: UserAccount?,
    leaderboardEntries: List<LeaderboardEntry>,
    isPt: Boolean,
    onLogin: () -> Unit,
    onSync: () -> Unit
) {
    val isLoggedIn = user != null && user.isLoggedIn
    val userEntry = if (isLoggedIn) {
        leaderboardEntries.firstOrNull { it.isCurrentUser || it.userId == user?.userId }
    } else null

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLoggedIn) PurplePrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (isLoggedIn) Brush.linearGradient(listOf(PurplePrimary, RedAccent))
            else androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant),
            width = if (isLoggedIn) 1.5.dp else 1.dp
        ),
        modifier = Modifier.fillMaxWidth().testTag("card_user_ranking_status")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoggedIn && user != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PurplePrimary, RedAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.displayName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PurplePrimary
                            ) {
                                Text(
                                    text = if (isPt) "Você" else "You",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        val rankText = userEntry?.let { "#${it.rank} no Placar" } ?: (if (isPt) "Registrado" else "Registered")
                        Text(
                            text = "$rankText • ${user.totalScore} XP (${user.levelTitlePt})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onSync,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_quick_sync_placar")
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPt) "Sincronizar" else "Sync", fontSize = 11.sp)
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isPt) "Participe do Placar da Comunidade" else "Join the Community Leaderboard",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isPt) "Faça login com Google ou Número de Celular para salvar e exibir sua pontuação."
                        else "Sign in with Google or Phone Number to save and showcase your XP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onLogin,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    modifier = Modifier.testTag("btn_login_prompt_card")
                ) {
                    Text(if (isPt) "Fazer Login" else "Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Linha individual de desenvolvedor no ranking do placar
 */
@Composable
fun LeaderboardEntryRow(
    entry: LeaderboardEntry,
    position: Int,
    isPt: Boolean
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) {
                PurplePrimary.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = if (entry.isCurrentUser) {
                Brush.linearGradient(listOf(PurplePrimary, RedAccent))
            } else {
                androidx.compose.ui.graphics.SolidColor(Color.Transparent)
            },
            width = if (entry.isCurrentUser) 1.5.dp else 0.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_row_${entry.userId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank Number + Avatar + Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Rank position pill
                Text(
                    text = "#$position",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (entry.isCurrentUser) PurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp)
                )

                // Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (entry.isCurrentUser) Brush.linearGradient(listOf(PurplePrimary, RedAccent))
                            else androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entry.displayName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = if (entry.isCurrentUser) Color.White else MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (entry.isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = RedAccent
                            ) {
                                Text(
                                    text = if (isPt) "Você" else "You",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.levelTitlePt,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (entry.maskedPhone.isNotBlank()) {
                            Text(
                                text = " • ${entry.maskedPhone}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Right side: Streaks, Badges, XP
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (entry.streakDays > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = RedAccent, modifier = Modifier.size(14.dp))
                        Text(text = "${entry.streakDays}d", fontSize = 11.sp, color = RedAccent, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PurplePrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${entry.totalScore} XP",
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
