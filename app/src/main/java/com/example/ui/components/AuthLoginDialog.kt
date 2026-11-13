package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AuthMethod
import com.example.model.Language
import com.example.model.UserAccount
import com.example.model.UserScoreInfo
import androidx.compose.material.icons.filled.Palette
import com.example.ui.theme.LocalDevRoutineColors

@Composable
fun AuthLoginDialog(
    userAccount: UserAccount?,
    scoreInfo: UserScoreInfo,
    streakDays: Int,
    badgesCount: Int,
    language: Language,
    isLoading: Boolean,
    onGoogleSignIn: (extraPhoneNumber: String) -> Unit,
    onPhoneSignIn: (name: String, phoneNumber: String) -> Unit,
    onSyncLeaderboard: () -> Unit,
    onSignOut: () -> Unit,
    onUpdatePhoto: (String) -> Unit = {},
    onOpenThemeSettings: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val brand = LocalDevRoutineColors.current
    val PurplePrimary = brand.primary
    val RedAccent = brand.accent
    val isPt = language == Language.PT
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Google, 1 = Phone Number

    // Phone form states
    var phoneInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Optional phone linking for Google login
    var googleOptionalPhone by remember { mutableStateOf(userAccount?.phoneNumber ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(PurplePrimary, RedAccent)),
                width = 1.5.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_auth_login")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(PurplePrimary, RedAccent))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (userAccount?.isLoggedIn == true) Icons.Default.AccountCircle else Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (userAccount?.isLoggedIn == true) {
                                if (isPt) "Perfil & Placar" else "Profile & Scoreboard"
                            } else {
                                if (isPt) "Identificação do Usuário" else "User Sign-In"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("btn_close_auth_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // IF USER IS ALREADY LOGGED IN
                if (userAccount != null && userAccount.isLoggedIn) {
                    LoggedInProfileView(
                        user = userAccount,
                        scoreInfo = scoreInfo,
                        streakDays = streakDays,
                        badgesCount = badgesCount,
                        language = language,
                        isLoading = isLoading,
                        onUpdatePhoto = onUpdatePhoto,
                        onOpenThemeSettings = onOpenThemeSettings,
                        onSync = onSyncLeaderboard,
                        onSignOut = onSignOut
                    )
                } else {
                    // TABS: GOOGLE OU NÚMERO DE TELEFONE
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = if (selectedTab == 0) PurplePrimary else RedAccent,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = if (isPt) "Google" else "Google",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            icon = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )

                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = if (isPt) "Com Número" else "Phone Number",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            icon = {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedTab == 0) {
                        // TAB 1: GOOGLE SIGN-IN
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isPt) "Entre com sua conta Google para salvar automaticamente seus pontos e conquistas no Placar da Comunidade."
                                else "Sign in with your Google account to automatically sync your score and achievements to the Community Leaderboard.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = googleOptionalPhone,
                                onValueChange = { googleOptionalPhone = it },
                                label = {
                                    Text(if (isPt) "Número de Celular (Opcional)" else "Phone Number (Optional)", fontSize = 12.sp)
                                },
                                placeholder = {
                                    Text("+55 (11) 98765-4321", fontSize = 12.sp)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PurplePrimary,
                                    focusedLabelColor = PurplePrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("input_google_optional_phone")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onGoogleSignIn(googleOptionalPhone) },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_google_signin")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isPt) "Entrar com Google" else "Sign in with Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        // TAB 2: PHONE NUMBER SIGN-IN
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isPt) "Digite seu nome de desenvolvedor e número de celular para se registrar e aparecer no placar."
                                else "Enter your developer name and phone number to register and appear on the leaderboard.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it; phoneError = null },
                                label = {
                                    Text(if (isPt) "Nome ou Apelido Dev" else "Developer Name", fontSize = 12.sp)
                                },
                                placeholder = {
                                    Text(if (isPt) "Ex: Dev Gabriel" else "E.g.: Dev Gabriel", fontSize = 12.sp)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RedAccent,
                                    focusedLabelColor = RedAccent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("input_dev_name")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it; phoneError = null },
                                label = {
                                    Text(if (isPt) "Número de Celular com DDD" else "Phone Number with Area Code", fontSize = 12.sp)
                                },
                                placeholder = {
                                    Text("+55 (11) 98765-4321", fontSize = 12.sp)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RedAccent,
                                    focusedLabelColor = RedAccent
                                ),
                                shape = RoundedCornerShape(12.dp),
                                isError = phoneError != null,
                                supportingText = phoneError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth().testTag("input_phone_number")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (nameInput.isBlank()) {
                                        phoneError = if (isPt) "Informe seu nome de desenvolvedor." else "Please enter your name."
                                        return@Button
                                    }
                                    if (phoneInput.trim().length < 8) {
                                        phoneError = if (isPt) "Número de celular inválido." else "Invalid phone number."
                                        return@Button
                                    }
                                    onPhoneSignIn(nameInput.trim(), phoneInput.trim())
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_phone_signin")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isPt) "Entrar com este Número" else "Sign In with this Number",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
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

/**
 * Visualização do Perfil quando o usuário já está autenticado
 */
@Composable
private fun LoggedInProfileView(
    user: UserAccount,
    scoreInfo: UserScoreInfo,
    streakDays: Int,
    badgesCount: Int,
    language: Language,
    isLoading: Boolean,
    onUpdatePhoto: (String) -> Unit,
    onOpenThemeSettings: () -> Unit,
    onSync: () -> Unit,
    onSignOut: () -> Unit
) {
    val isPt = language == Language.PT
    val brand = LocalDevRoutineColors.current
    val PurplePrimary = brand.primary
    val RedAccent = brand.accent
    var showPhotoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar interativo com suporte a foto de galeria e avatares dev
        UserAvatar(
            photoUrl = user.photoUrl,
            displayName = user.displayName,
            size = 80.dp,
            borderWidth = 3.dp,
            borderColor = RedAccent,
            showEditBadge = true,
            onClick = { showPhotoDialog = true }
        )

        TextButton(
            onClick = { showPhotoDialog = true },
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = PurplePrimary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isPt) "Alterar Foto de Perfil" else "Change Profile Photo",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary
            )
        }

        if (showPhotoDialog) {
            ProfilePhotoDialog(
                currentPhotoUrl = user.photoUrl,
                displayName = user.displayName,
                language = language,
                onSavePhoto = { newPhoto ->
                    onUpdatePhoto(newPhoto)
                },
                onDismiss = { showPhotoDialog = false }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = user.displayName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        // Contact info pill
        val contactDetail = if (user.email.isNotBlank()) user.email else user.maskedPhone
        if (contactDetail.isNotBlank()) {
            Text(
                text = contactDetail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Auth Method Tag
        Surface(
            color = if (user.authMethod == AuthMethod.GOOGLE) PurplePrimary.copy(alpha = 0.15f) else RedAccent.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (user.authMethod == AuthMethod.GOOGLE) Icons.Default.AccountCircle else Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = if (user.authMethod == AuthMethod.GOOGLE) PurplePrimary else RedAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPt) "Conectado via ${user.authMethod.labelPt}" else "Connected via ${user.authMethod.labelEn}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (user.authMethod == AuthMethod.GOOGLE) PurplePrimary else RedAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mini Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "${scoreInfo.totalScore} XP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = scoreInfo.levelTitlePt, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$streakDays dias", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = if (isPt) "Sequência" else "Streak", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = RedAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$badgesCount", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = if (isPt) "Emblemas" else "Badges", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action: Sincronizar Placar
        Button(
            onClick = onSync,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_sync_leaderboard")
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPt) "Sincronizar Pontos no Placar" else "Sync Points to Leaderboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action: Personalizar Cores & Tema
        OutlinedButton(
            onClick = onOpenThemeSettings,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_profile_open_theme_settings")
        ) {
            Icon(Icons.Default.Palette, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isPt) "Personalizar Cores & Tema" else "Customize Colors & Theme",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action: Sair da conta
        OutlinedButton(
            onClick = onSignOut,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("btn_logout")
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isPt) "Sair da Conta" else "Sign Out", fontSize = 12.sp)
        }
    }
}
