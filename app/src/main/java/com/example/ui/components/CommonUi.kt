package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Language
import com.example.model.ThemePreference
import com.example.model.UserAccount
import com.example.ui.AppDestination
import com.example.ui.theme.LocalDevRoutineColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    currentLanguage: Language,
    currentTheme: ThemePreference,
    userAccount: UserAccount? = null,
    dailyLives: com.example.ui.DailyLivesInfo = com.example.ui.DailyLivesInfo(),
    pendingChatRequestsCount: Int = 0,
    isCommunityChatSelected: Boolean = false,
    isSettingsSelected: Boolean = false,
    onOpenLivesInfo: () -> Unit = {},
    onOpenLifeShop: () -> Unit = {},
    onToggleLanguage: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onOpenCommunityChat: () -> Unit = {},
    onOpenLeaderboard: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val brand = LocalDevRoutineColors.current
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(brand.primary, brand.accent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.app_topbar_subtitle),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        actions = {
            // Vidas Diárias (25/25) + botão "+" para loja de vidas extras
            DailyLivesPill(
                livesInfo = dailyLives,
                onClick = onOpenLivesInfo,
                onAddLivesClick = onOpenLifeShop
            )

            // Chat & Ligações da Comunidade
            IconButton(
                onClick = onOpenCommunityChat,
                modifier = Modifier.testTag("button_top_community_chat")
            ) {
                BadgedBox(
                    badge = {
                        if (pendingChatRequestsCount > 0) {
                            Badge(
                                containerColor = brand.accent,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (pendingChatRequestsCount > 9) "9+" else pendingChatRequestsCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = stringResource(R.string.action_community_chat),
                        tint = if (isCommunityChatSelected) brand.accent else brand.primary
                    )
                }
            }

            // Placar / Leaderboard button
            IconButton(
                onClick = onOpenLeaderboard,
                modifier = Modifier.testTag("button_top_leaderboard")
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = stringResource(R.string.action_leaderboard),
                    tint = brand.primary
                )
            }

            // User Profile / Login button
            IconButton(
                onClick = onOpenAuth,
                modifier = Modifier.testTag("button_top_auth")
            ) {
                if (userAccount != null && userAccount.isLoggedIn) {
                    UserAvatar(
                        photoUrl = userAccount.photoUrl,
                        displayName = userAccount.displayName,
                        size = 30.dp,
                        borderWidth = 1.5.dp,
                        borderColor = brand.primaryLight
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.action_sign_in_profile),
                        tint = brand.accent
                    )
                }
            }

            // Engrenagem de Configurações bem no canto direito
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("button_top_settings")
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSettingsSelected) brand.primary.copy(alpha = 0.22f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                        )
                        .border(
                            width = if (isSettingsSelected) 1.5.dp else 1.dp,
                            color = if (isSettingsSelected) brand.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.action_settings),
                        tint = if (isSettingsSelected) brand.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun AppBottomNav(
    currentDestination: AppDestination,
    language: Language,
    pendingChatBadgeCount: Int = 0,
    hasActiveLives: Boolean = false,
    onNavigate: (AppDestination) -> Unit
) {
    val brand = LocalDevRoutineColors.current
    val homeLabel = stringResource(R.string.nav_home)
    val techsLabel = stringResource(R.string.nav_techs)
    val progressLabel = stringResource(R.string.nav_progress)
    val calendarLabel = stringResource(R.string.nav_calendar)
    val aiLabel = stringResource(R.string.nav_ai)
    val chatLabel = stringResource(R.string.nav_chat)
    val chatDesc = stringResource(R.string.nav_chat_live_desc)
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentDestination == AppDestination.HOME,
            onClick = { onNavigate(AppDestination.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = homeLabel) },
            label = {
                Text(
                    text = homeLabel,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            modifier = Modifier.testTag("nav_home"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )
        NavigationBarItem(
            selected = currentDestination == AppDestination.TECH_HUB || currentDestination == AppDestination.TECH_DETAIL,
            onClick = { onNavigate(AppDestination.TECH_HUB) },
            icon = { Icon(Icons.Default.School, contentDescription = techsLabel) },
            label = {
                Text(
                    text = techsLabel,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            modifier = Modifier.testTag("nav_techs"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary
            )
        )
        NavigationBarItem(
            selected = currentDestination == AppDestination.PROGRESS,
            onClick = { onNavigate(AppDestination.PROGRESS) },
            icon = { Icon(Icons.Default.QueryStats, contentDescription = progressLabel) },
            label = {
                Text(
                    text = progressLabel,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            modifier = Modifier.testTag("nav_progress"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = brand.accent,
                indicatorColor = brand.accent
            )
        )
        NavigationBarItem(
            selected = currentDestination == AppDestination.CALENDAR,
            onClick = { onNavigate(AppDestination.CALENDAR) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = calendarLabel) },
            label = {
                Text(
                    text = calendarLabel,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            modifier = Modifier.testTag("nav_calendar"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onSecondary,
                selectedTextColor = MaterialTheme.colorScheme.secondary,
                indicatorColor = MaterialTheme.colorScheme.secondary
            )
        )
        NavigationBarItem(
            selected = currentDestination == AppDestination.AI_CHAT,
            onClick = { onNavigate(AppDestination.AI_CHAT) },
            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = aiLabel) },
            label = {
                Text(
                    text = aiLabel,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            modifier = Modifier.testTag("nav_ai"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = brand.primary,
                indicatorColor = brand.primary
            )
        )
        NavigationBarItem(
            selected = currentDestination == AppDestination.COMMUNITY_CHAT,
            onClick = { onNavigate(AppDestination.COMMUNITY_CHAT) },
            icon = {
                BadgedBox(
                    badge = {
                        when {
                            pendingChatBadgeCount > 0 -> {
                                Badge(containerColor = brand.accent, contentColor = Color.White) {
                                    Text(
                                        text = if (pendingChatBadgeCount > 9) "9+" else "$pendingChatBadgeCount",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            hasActiveLives -> {
                                Badge(containerColor = Color(0xFFE53935), contentColor = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = chatDesc
                    )
                }
            },
            label = {
                Text(
                    text = chatLabel,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            },
            modifier = Modifier.testTag("nav_community_chat"),
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color(0xFF00ACC1),
                indicatorColor = Color(0xFF00ACC1)
            )
        )
    }
}

@Composable
fun CodeSnippetCard(
    title: String,
    code: String,
    explanation: String
) {
    val clipboardManager = LocalClipboardManager.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = { clipboardManager.setText(AnnotatedString(code)) },
                    modifier = Modifier.size(28.dp).testTag("button_copy_code")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.common_copy_code),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090314))
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFFF1D8FF),
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
