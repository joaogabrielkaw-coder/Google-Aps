package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DeveloperLevelCatalog
import com.example.model.Language
import com.example.model.LevelUpEvent
import com.example.model.UserScoreInfo
import com.example.ui.DailyLivesInfo
import com.example.ui.theme.LocalAccessibilitySettings
import com.example.ui.theme.LocalDevRoutineColors
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import kotlin.math.cos
import kotlin.math.sin

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

private val HeartRose = Color(0xFFFF4D7D)
private val HeartCoral = Color(0xFFFF856D)
private val GoldSpark = Color(0xFFFFD54F)
private val EmeraldGlow = Color(0xFF4ADE80)
private val SoftLavender = Color(0xFFC4B5FD)
private val WarmPeach = Color(0xFFFDBA74)

/**
 * Pill decorativa com coração pulsante exibindo as vidas diárias restantes (25/25).
 */
@Composable
fun DailyLivesPill(
    livesInfo: DailyLivesInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalAccessibilitySettings.current.reduceMotion
    val infiniteTransition = rememberInfiniteTransition(label = "lives_heart_pulse")
    val animatedPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )
    val pulse = if (reduceMotion) 1f else animatedPulse
    val isLow = livesInfo.remaining <= 5
    val isEmpty = livesInfo.remaining <= 0

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isEmpty) Color(0xFF3A1D2B) else HeartRose.copy(alpha = 0.16f),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(listOf(HeartRose, HeartCoral))
        ),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("pill_daily_lives")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isEmpty) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                contentDescription = "Vidas Diárias",
                tint = if (isEmpty) Color(0xFFFCA5A5) else HeartRose,
                modifier = Modifier
                    .size(16.dp)
                    .scale(if (isLow || isEmpty) 1f else pulse)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "${livesInfo.remaining}/${livesInfo.maxDaily}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isEmpty) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Cartão enfeitado para Dashboard/Trilha exibindo as 25 vidas diárias com barra decorada.
 */
@Composable
fun DailyLivesBannerCard(
    livesInfo: DailyLivesInfo,
    language: Language,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val fraction = (livesInfo.remaining.toFloat() / livesInfo.maxDaily.coerceAtLeast(1)).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(listOf(HeartRose, WarmPeach, PurplePrimary))
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("card_daily_lives_banner")
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(HeartRose, HeartCoral))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isPt) "Vidas Diárias • ${livesInfo.remaining} de ${livesInfo.maxDaily}"
                            else "Daily Lives • ${livesInfo.remaining} of ${livesInfo.maxDaily}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isPt) "25 vidas novas todo dia • 1 vida por tentativa"
                            else "25 fresh lives every day • 1 life per attempt",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (index == 1) GoldSpark else HeartRose.copy(alpha = 0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape),
                color = if (livesInfo.remaining > 5) HeartRose else WarmPeach,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

/**
 * Diálogo informativo ao tocar na pill/card de Vidas Diárias.
 */
@Composable
fun DailyLivesInfoDialog(
    livesInfo: DailyLivesInfo,
    language: Language,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(HeartRose, GoldSpark, PurplePrimary))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_daily_lives_info")
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                DecorativeSparklesBackground(
                    primaryColor = HeartRose,
                    secondaryColor = GoldSpark,
                    modifier = Modifier.matchParentSize()
                )
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(HeartRose, HeartCoral)))
                            .border(2.5.dp, GoldSpark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isPt) "Suas 25 Vidas Diárias ❤️✨" else "Your 25 Daily Lives ❤️✨",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HeartRose.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, HeartRose.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = if (isPt) "${livesInfo.remaining} de ${livesInfo.maxDaily} vidas disponíveis hoje"
                            else "${livesInfo.remaining} of ${livesInfo.maxDaily} lives remaining today",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = HeartRose,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isPt) {
                            "Todos os dias você recebe 25 vidas novinhas para praticar desafios e quizzes. Cada tentativa consome 1 vida — e sempre que você subir de nível, suas 25 vidas são restauradas na hora!"
                        } else {
                            "Every day you receive 25 fresh lives to solve coding challenges and quizzes. Each attempt costs 1 life — and every time you level up, all 25 lives reset back to full right away!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = HeartRose),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (isPt) "Entendi! Vamos Praticar ✨" else "Got it! Let's Practice ✨",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo comemorativo super enfeitado exibido a cada vitória em tarefas e quizzes.
 */
@Composable
fun VictoryCelebrationDialog(
    title: String,
    xpEarned: Int,
    remainingLives: Int,
    maxLives: Int = DailyLivesInfo.MAX_DAILY_LIVES,
    language: Language,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val reduceMotion = LocalAccessibilitySettings.current.reduceMotion
    val infiniteTransition = rememberInfiniteTransition(label = "victory_sparkle")
    val animatedHaloScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )
    val haloScale = if (reduceMotion) 1f else animatedHaloScale

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171326)),
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(GoldSpark, EmeraldGlow, PurplePrimary, HeartRose))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_victory_celebration")
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Festive animated confetti & twinkling stars canvas
                DecorativeSparklesBackground(
                    primaryColor = GoldSpark,
                    secondaryColor = EmeraldGlow,
                    includeConfetti = true,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Decorative ribbon
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GoldSpark.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, GoldSpark.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldSpark, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPt) "VITÓRIA CONQUISTADA!" else "VICTORY ACHIEVED!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldSpark,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldSpark, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Glowing Trophy Emblem
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(haloScale)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(GoldSpark.copy(alpha = 0.35f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(GoldSpark, Color(0xFFF59E0B), Color(0xFFD97706))))
                                .border(2.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFF2B1B05),
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isPt) "🎉 Parabéns! ✨" else "🎉 Congratulations! ✨",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isPt) {
                            "Você mandou muito bem em \"$title\"! Continue brilhando assim!"
                        } else {
                            "You nailed \"$title\"! Keep shining like a star!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE5E7EB),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // XP + Lives Rewards Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = EmeraldGlow.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.55f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmeraldGlow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "+$xpEarned XP",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = EmeraldGlow
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = HeartRose.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, HeartRose.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = HeartRose, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "$remainingLives/$maxLives",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFFB3C6)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldSpark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_close_victory_dialog")
                    ) {
                        Text(
                            text = if (isPt) "Continuar Brilhando 🚀" else "Keep Shining 🚀",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2B1B05)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo gentil e super enfeitado exibido a cada erro para incentivar o usuário a tentar novamente.
 */
@Composable
fun GentleRetryFeedbackDialog(
    hintText: String,
    remainingLives: Int,
    maxLives: Int = DailyLivesInfo.MAX_DAILY_LIVES,
    language: Language,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F162B)),
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(SoftLavender, HeartRose, WarmPeach))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_gentle_retry")
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                DecorativeSparklesBackground(
                    primaryColor = SoftLavender,
                    secondaryColor = WarmPeach,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SoftLavender.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, SoftLavender.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SoftLavender, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPt) "UM PASSO DE CADA VEZ" else "ONE STEP AT A TIME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftLavender,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = WarmPeach, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), HeartRose)))
                            .border(2.dp, WarmPeach.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isPt) "Quase lá! Tente novamente 💜✨" else "Almost there! Try again 💜✨",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isPt) {
                            "Não desanime! Errar faz parte da evolução de todo programador incrível. Respire fundo e tente novamente com calma!"
                        } else {
                            "Don't give up! Making mistakes is how every great developer grows. Take a breath and try again!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE9D5FF),
                        textAlign = TextAlign.Center
                    )

                    if (hintText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = WarmPeach.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, WarmPeach.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🌸 $hintText",
                                fontSize = 12.sp,
                                color = Color(0xFFFFEDD5),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HeartRose.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, HeartRose.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = HeartRose, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPt) "Você ainda tem $remainingLives/$maxLives vidas hoje"
                                else "You still have $remainingLives/$maxLives lives today",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFB3C6)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_close_gentle_retry")
                    ) {
                        Text(
                            text = if (isPt) "Tentar Novamente com Carinho ✨" else "Try Again Gently ✨",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo gentil exibido quando as 25 vidas diárias acabam.
 */
@Composable
fun OutOfLivesDialog(
    language: Language,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF191428)),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(HeartRose, SoftLavender))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_out_of_lives")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PurplePrimary, HeartRose))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = GoldSpark,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (isPt) "Vidas de Hoje Esgotadas 🌙💜" else "Today's Lives Reached 0 🌙💜",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isPt) {
                        "Você deu o seu máximo nas 25 tentativas de hoje! Aproveite para revisar o Cheatsheet e o Roadmap com calma — amanhã você receberá +25 vidas novinhas!"
                    } else {
                        "You gave it your all across today's 25 attempts! Take time to explore the Cheatsheet and Roadmap — 25 fresh lives arrive tomorrow!"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFDCD6F7),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = if (isPt) "Revisar Conteúdo ✨" else "Review Study Guides ✨",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Fundo animado com estrelas e confetes suaves para deixar as mensagens bem enfeitadas.
 */
@Composable
private fun DecorativeSparklesBackground(
    primaryColor: Color,
    secondaryColor: Color,
    includeConfetti: Boolean = false,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalAccessibilitySettings.current.reduceMotion
    val infiniteTransition = rememberInfiniteTransition(label = "sparkles_bg")
    val animatedPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_phase"
    )
    val phase = if (reduceMotion) 0f else animatedPhase

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val rad = Math.toRadians(phase.toDouble())

        val spots = listOf(
            Offset(w * 0.12f, h * 0.14f) to primaryColor,
            Offset(w * 0.88f, h * 0.12f) to secondaryColor,
            Offset(w * 0.08f, h * 0.55f) to secondaryColor,
            Offset(w * 0.92f, h * 0.48f) to primaryColor,
            Offset(w * 0.22f, h * 0.85f) to primaryColor,
            Offset(w * 0.82f, h * 0.82f) to secondaryColor,
            Offset(w * 0.50f, h * 0.07f) to GoldSpark
        )

        spots.forEachIndexed { idx, (pos, color) ->
            val driftY = sin(rad + idx * 1.1).toFloat() * 5.dp.toPx()
            val driftX = cos(rad + idx * 0.9).toFloat() * 4.dp.toPx()
            val center = Offset(pos.x + driftX, pos.y + driftY)
            val starSize = if (idx % 2 == 0) 6.dp.toPx() else 4.5.dp.toPx()

            rotate(degrees = phase * 0.5f + idx * 35f, pivot = center) {
                val starPath = Path().apply {
                    moveTo(center.x, center.y - starSize)
                    lineTo(center.x + starSize * 0.32f, center.y - starSize * 0.32f)
                    lineTo(center.x + starSize, center.y)
                    lineTo(center.x + starSize * 0.32f, center.y + starSize * 0.32f)
                    lineTo(center.x, center.y + starSize)
                    lineTo(center.x - starSize * 0.32f, center.y + starSize * 0.32f)
                    lineTo(center.x - starSize, center.y)
                    lineTo(center.x - starSize * 0.32f, center.y - starSize * 0.32f)
                    close()
                }
                drawPath(path = starPath, color = color.copy(alpha = 0.55f))
            }
        }

        if (includeConfetti) {
            val confettiColors = listOf(GoldSpark, HeartRose, EmeraldGlow, SoftLavender, WarmPeach)
            for (i in 0 until 12) {
                val xRatio = ((i * 37) % 90 + 5) / 100f
                val yRatio = ((i * 53) % 85 + 8) / 100f
                val cx = w * xRatio + cos(rad + i).toFloat() * 6.dp.toPx()
                val cy = h * yRatio + sin(rad + i).toFloat() * 6.dp.toPx()
                drawCircle(
                    color = confettiColors[i % confettiColors.size].copy(alpha = 0.38f),
                    radius = (2.5f + (i % 3)).dp.toPx(),
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

/**
 * Diálogo comemorativo exibido quando o usuário sobe de nível e tem suas 25 vidas restauradas.
 */
@Composable
fun LevelUpCelebrationDialog(
    event: LevelUpEvent,
    language: Language,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val reduceMotion = LocalAccessibilitySettings.current.reduceMotion
    val infiniteTransition = rememberInfiniteTransition(label = "level_up_halo")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "level_scale"
    )
    val badgeScale = if (reduceMotion) 1f else animatedScale

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF17122B)),
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(GoldSpark, HeartRose, PurplePrimary, EmeraldGlow))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_level_up_celebration")
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                DecorativeSparklesBackground(
                    primaryColor = GoldSpark,
                    secondaryColor = HeartRose,
                    includeConfetti = true,
                    modifier = Modifier.matchParentSize()
                )

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GoldSpark.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, GoldSpark.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldSpark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPt) "SUBIU DE NÍVEL!" else "LEVEL UP!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GoldSpark,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .scale(badgeScale)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PurplePrimary, HeartRose, GoldSpark)))
                            .border(2.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isPt) "NVL" else "LV",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "${event.newLevel}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isPt) event.titlePt else event.titleEn,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PurplePrimary.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, SoftLavender.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (isPt) "Elo ${event.rankBadgePt} • ${event.totalXp} XP Totais"
                            else "Rank ${event.rankBadgeEn} • ${event.totalXp} Total XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftLavender,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Life Reset Perk Highlight
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = HeartRose.copy(alpha = 0.18f),
                        border = BorderStroke(1.2.dp, HeartRose.copy(alpha = 0.65f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = HeartRose,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isPt) "❤️ ${event.livesRestored}/${event.livesRestored} Vidas Restauradas!"
                                    else "❤️ ${event.livesRestored}/${event.livesRestored} Lives Restored!",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isPt) "Recompensa por alcançar o Nível ${event.newLevel}"
                                    else "Reward for reaching Level ${event.newLevel}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFD6E0)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGlow),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_close_level_up_dialog")
                    ) {
                        Text(
                            text = if (isPt) "Continuar Evoluindo 🚀" else "Keep Climbing 🚀",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0A1D12)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal completo mostrando todos os 12 níveis, títulos, elos e requisitos de XP.
 */
@Composable
fun LevelRoadmapDialog(
    scoreInfo: UserScoreInfo,
    language: Language,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val remainingXp = (scoreInfo.nextLevelTarget - scoreInfo.totalScore).coerceAtLeast(0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.2.dp, Brush.linearGradient(listOf(PurplePrimary, GoldSpark))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("dialog_level_roadmap")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldSpark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isPt) "Sistema de Níveis e Títulos" else "Levels & Ranks Roadmap",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = if (scoreInfo.isMaxLevel) {
                                    if (isPt) "Nível Máximo Alcançado! (${scoreInfo.totalScore} XP)"
                                    else "Max Level Reached! (${scoreInfo.totalScore} XP)"
                                } else {
                                    if (isPt) "Faltam $remainingXp XP para o Nível ${scoreInfo.levelNumber + 1}"
                                    else "$remainingXp XP left for Level ${scoreInfo.levelNumber + 1}"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    androidx.compose.material3.IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isPt) "Fechar" else "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HeartRose.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, HeartRose.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = HeartRose, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPt) "Subir de nível exige mais XP, mas restaura suas 25 vidas na hora!"
                            else "Higher levels take more XP, and every level-up restores all 25 lives!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.height(340.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DeveloperLevelCatalog.tiers) { tier ->
                        val isCurrent = tier.levelNumber == scoreInfo.levelNumber
                        val isUnlocked = scoreInfo.totalScore >= tier.minXp

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isCurrent -> PurplePrimary.copy(alpha = 0.18f)
                                isUnlocked -> EmeraldGlow.copy(alpha = 0.10f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            },
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = when {
                                    isCurrent -> PurplePrimary
                                    isUnlocked -> EmeraldGlow.copy(alpha = 0.45f)
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCurrent -> PurplePrimary
                                                    isUnlocked -> EmeraldGlow.copy(alpha = 0.22f)
                                                    else -> MaterialTheme.colorScheme.surface
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${tier.levelNumber}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = when {
                                                isCurrent -> Color.White
                                                isUnlocked -> EmeraldGlow
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = tier.title(language),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isCurrent) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = PurplePrimary
                                                ) {
                                                    Text(
                                                        text = if (isPt) "Atual" else "Now",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${tier.rankBadge(language)} • ${tier.minXp} XP",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isUnlocked) EmeraldGlow else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
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

