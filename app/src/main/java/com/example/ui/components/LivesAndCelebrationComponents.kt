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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.example.model.LifePaymentMethod
import com.example.model.LifePurchaseCurrency
import com.example.model.LifePurchaseReceipt
import com.example.model.LifePurchaseResult
import com.example.model.LifeShopCatalog
import com.example.model.LifeShopPackage
import com.example.model.PlayBillingConnectionStatus
import com.example.model.PlayBillingStoreUiState
import com.example.model.ProSubscriptionPlan
import com.example.model.ProSubscriptionState
import com.example.model.UserScoreInfo
import com.example.ui.DailyLivesInfo
import com.example.ui.theme.LocalAccessibilitySettings
import com.example.ui.theme.LocalDevRoutineColors
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
 * Pill decorativa com coração pulsante exibindo as vidas diárias restantes (25/25)
 * e um botão "+" ao lado dos corações para abrir a interface de compra de vidas extras.
 */
@Composable
fun DailyLivesPill(
    livesInfo: DailyLivesInfo,
    onClick: () -> Unit,
    onAddLivesClick: (() -> Unit)? = null,
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isEmpty) Color(0xFF3A1D2B) else HeartRose.copy(alpha = 0.16f),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.linearGradient(listOf(HeartRose, HeartCoral))
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .testTag("pill_daily_lives")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
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
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${livesInfo.remaining}/${livesInfo.maxDaily}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isEmpty) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (onAddLivesClick != null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable(onClick = onAddLivesClick)
                    .testTag("btn_add_lives_plus")
            ) {
                Surface(
                    shape = CircleShape,
                    color = HeartRose,
                    border = BorderStroke(1.2.dp, GoldSpark),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Comprar mais vidas",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cartão enfeitado para Dashboard/Trilha exibindo as 25 vidas diárias com barra decorada
 * e botão "+" ao lado dos corações para abrir a loja de vidas extras.
 */
@Composable
fun DailyLivesBannerCard(
    livesInfo: DailyLivesInfo,
    language: Language,
    onClick: () -> Unit = {},
    onAddLivesClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val isEmpty = livesInfo.remaining <= 0
    val fraction = (livesInfo.remaining.toFloat() / livesInfo.maxDaily.coerceAtLeast(1)).coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                if (isEmpty) listOf(RedAccent, HeartRose, WarmPeach)
                else listOf(HeartRose, WarmPeach, PurplePrimary)
            )
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
                            imageVector = if (isEmpty) Icons.Default.ShoppingCart else Icons.Default.Favorite,
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
                            text = when {
                                isEmpty && isPt -> "Cota diária zerada • Toque no + para comprar vidas ❤️"
                                isEmpty -> "Daily quota depleted • Tap + to buy lives ❤️"
                                isPt -> "Toque em + para comprar +10, +20 ou +50 vidas"
                                else -> "Tap + to buy +10, +20 or +50 extra lives"
                            },
                            fontSize = 11.sp,
                            color = if (isEmpty) HeartRose else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isEmpty) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                if (onAddLivesClick != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = HeartRose,
                        border = BorderStroke(1.2.dp, GoldSpark),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onAddLivesClick)
                            .testTag("btn_banner_add_lives_plus")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = if (isPt) "Comprar vidas extras" else "Buy extra lives",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isPt) "Vidas" else "Lives",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
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
 * Seção reutilizável de compra de vidas usando XP disponível ou Cristais 3D.
 * Exibida automaticamente quando a cota diária de 25 vidas chega a 0.
 */
@Composable
fun LifePurchaseOptionsSection(
    language: Language,
    availableXp: Int,
    availableCrystals: Int,
    onPurchasePackage: (LifeShopPackage) -> LifePurchaseResult?,
    onOpen3DGame: (() -> Unit)? = null,
    feedbackMessage: String? = null,
    isFeedbackError: Boolean = false,
    darkSurface: Boolean = false,
    isLockedByQuota: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val titleColor = if (darkSurface) Color.White else MaterialTheme.colorScheme.onSurface
    val subTextColor = if (darkSurface) Color(0xFFB8B0D8) else MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = if (darkSurface) Color(0xFF1E1A2E) else MaterialTheme.colorScheme.surfaceVariant
    val hasMinCrystalsForPack = LifeShopCatalog.packages
        .filter { it.currency == LifePurchaseCurrency.CRYSTALS }
        .any { availableCrystals >= it.costAmount }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("section_life_purchase_options"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Minimal header with inline XP & Crystal balances
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isPt) "Resgatar grátis" else "Redeem free",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = subTextColor
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PurplePrimary.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "$availableXp XP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldSpark,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGlow.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "$availableCrystals 💎",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGlow,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }

        if (!feedbackMessage.isNullOrBlank()) {
            Text(
                text = feedbackMessage,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isFeedbackError) Color(0xFFFCA5A5) else subTextColor,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Smaller aligned side-by-side buttons for XP & Crystal packs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LifeShopCatalog.packages.forEach { pkg ->
                val canAfford = when (pkg.currency) {
                    LifePurchaseCurrency.BRL -> true
                    LifePurchaseCurrency.XP -> availableXp >= pkg.costAmount
                    LifePurchaseCurrency.CRYSTALS -> availableCrystals >= pkg.costAmount
                }
                val isCrystalPack = pkg.currency == LifePurchaseCurrency.CRYSTALS
                val pillColor = when {
                    isLockedByQuota || !canAfford -> Color(0xFF3E3854)
                    isCrystalPack -> Color(0xFF15803D)
                    else -> HeartRose
                }
                val borderColor = when {
                    isLockedByQuota -> Color(0xFF2E2842)
                    canAfford && isCrystalPack -> EmeraldGlow.copy(alpha = 0.45f)
                    canAfford -> HeartRose.copy(alpha = 0.45f)
                    else -> Color(0xFF2E2842)
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = cardBg,
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onPurchasePackage(pkg) }
                        .testTag("btn_buy_lives_${pkg.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (isLockedByQuota) Icons.Default.Lock else Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (isLockedByQuota) Color(0xFF9E96BD) else HeartRose,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "+${pkg.livesAmount}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLockedByQuota) Color(0xFFB8B0D8) else titleColor
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = pillColor,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isCrystalPack) "${pkg.costAmount} 💎" else "${pkg.costAmount} XP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        if (!hasMinCrystalsForPack && onOpen3DGame != null) {
            OutlinedButton(
                onClick = onOpen3DGame,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("btn_open_3d_game_for_crystals")
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = EmeraldGlow,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPt) "Coletar Cristais no Jogo 3D" else "Earn Crystals in 3D Game",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldGlow
                )
            }
        }
    }
}

/**
 * Interface minimalista de Compra de Vidas Extras (aberta pelo botão "+" ao lado dos corações).
 * Exibe os pacotes em Reais (10 por R$ 5, 20 por R$ 10, 50 por R$ 35) e opções XP/Cristais
 * com botões compactos alinhados lado a lado, além do atalho para a assinatura DevRoutine Pro.
 */
@Composable
fun LifePurchaseShopDialog(
    livesInfo: DailyLivesInfo,
    language: Language,
    availableXp: Int,
    availableCrystals: Int,
    proSubscriptionState: ProSubscriptionState = ProSubscriptionState(),
    billingStoreState: PlayBillingStoreUiState = PlayBillingStoreUiState(),
    externalConfirmedReceipt: LifePurchaseReceipt? = null,
    onPurchaseLives: (LifeShopPackage, LifePaymentMethod?) -> LifePurchaseResult?,
    onLaunchRealBrlPurchase: (LifeShopPackage) -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    onRefreshBilling: () -> Unit = {},
    onClearExternalReceipt: () -> Unit = {},
    onOpenProSubscription: (() -> Unit)? = null,
    onOpen3DGame: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    var selectedBrlPackage by remember { mutableStateOf<LifeShopPackage?>(null) }
    var localConfirmedReceipt by remember { mutableStateOf<LifePurchaseReceipt?>(null) }
    var shopFeedback by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    val activeReceipt = externalConfirmedReceipt ?: localConfirmedReceipt

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF151221)),
            border = BorderStroke(1.dp, Color(0xFF2D2742)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("dialog_life_purchase_shop")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Minimalist Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (selectedBrlPackage != null && activeReceipt == null) {
                            IconButton(
                                onClick = { selectedBrlPackage = null },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("btn_back_to_life_catalog")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = if (isPt) "Voltar" else "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = HeartRose,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = when {
                                activeReceipt != null -> if (isPt) "Confirmado!" else "Confirmed!"
                                selectedBrlPackage != null -> if (isPt) "Google Play" else "Checkout"
                                isPt -> "Loja de Vidas"
                                else -> "Lives Shop"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HeartRose.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, HeartRose.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${livesInfo.remaining} ❤️",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("btn_close_life_shop")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isPt) "Fechar" else "Close",
                            tint = Color(0xFFB8B0D8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                when {
                    // Step 3: Confirmation receipt
                    activeReceipt != null -> {
                        val receipt = activeReceipt
                        val totalLivesAdded = receipt.pkg.livesAmount * receipt.quantity.coerceAtLeast(1)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1E1A2E),
                            border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("card_life_purchase_receipt")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldGlow,
                                    modifier = Modifier.size(38.dp)
                                )
                                Text(
                                    text = if (isPt) "+$totalLivesAdded vidas adicionadas"
                                    else "+$totalLivesAdded lives added",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = buildString {
                                        append(receipt.pkg.formattedPrice(language, billingStoreState.productPrices))
                                        receipt.paymentMethod?.let { method ->
                                            append(" • ")
                                            append(method.label(language))
                                        }
                                        append(" • ")
                                        append(if (isPt) "Saldo: ${receipt.newRemainingLives} ❤️" else "Balance: ${receipt.newRemainingLives} ❤️")
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFFDCD6F7),
                                    textAlign = TextAlign.Center
                                )
                                receipt.orderId?.takeIf { it.isNotBlank() }?.let { oid ->
                                    Text(
                                        text = if (isPt) "Pedido: $oid" else "Order: $oid",
                                        fontSize = 10.sp,
                                        color = Color(0xFF9E96BD),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    localConfirmedReceipt = null
                                    onClearExternalReceipt()
                                    selectedBrlPackage = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SoftLavender.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = if (isPt) "Mais" else "More",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = HeartRose),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = if (isPt) "Praticar" else "Practice",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Step 2: Real Google Play Billing Checkout for selected BRL package
                    selectedBrlPackage != null -> {
                        val pkg = selectedBrlPackage!!
                        val projectedLives = livesInfo.remaining + pkg.livesAmount
                        val livePrice = pkg.formattedPrice(language, billingStoreState.productPrices)
                        val isPending = pkg.playProductId != null && pkg.playProductId in billingStoreState.pendingProductIds

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E1A2E),
                            border = BorderStroke(1.dp, Color(0xFF332C4A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "+${pkg.livesAmount} " + if (isPt) "vidas extras" else "extra lives",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${livesInfo.remaining} ❤️ → $projectedLives ❤️",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldGlow
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF16A34A).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = livePrice,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldGlow,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1A1629),
                            border = BorderStroke(1.dp, Color(0xFF332C4A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isPt) "Cobrança real via Google Play Billing" else "Real billing via Google Play",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isPt) {
                                        "Aceita Pix, Cartão de Crédito e Saldo Play direto no checkout oficial do Google Play."
                                    } else {
                                        "Supports Pix, Credit Card, and Play Balance directly in Google Play checkout."
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFFB8B0D8)
                                )
                                val connectionLabel = when (billingStoreState.connectionStatus) {
                                    PlayBillingConnectionStatus.READY ->
                                        if (pkg.playProductId != null && billingStoreState.isProductReadyInPlay(pkg.playProductId)) {
                                            if (isPt) "• Play Store conectada e produto ativo" else "• Play Store connected & SKU active"
                                        } else {
                                            if (isPt) "• Play Store conectada (aguardando SKU no Play Console)" else "• Play Store connected (awaiting Play Console SKU)"
                                        }
                                    PlayBillingConnectionStatus.CONNECTING ->
                                        if (isPt) "• Conectando ao Google Play..." else "• Connecting to Google Play..."
                                    PlayBillingConnectionStatus.UNAVAILABLE ->
                                        if (isPt) "• Google Play Billing indisponível neste dispositivo" else "• Google Play Billing unavailable on device"
                                    PlayBillingConnectionStatus.DISCONNECTED ->
                                        if (isPt) "• Play Billing desconectado — toque para tentar novamente" else "• Play Billing disconnected — tap to retry"
                                }
                                Text(
                                    text = connectionLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (billingStoreState.connectionStatus == PlayBillingConnectionStatus.READY) EmeraldGlow else Color(0xFFFCD34D)
                                )
                            }
                        }

                        if (isPending) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF422006),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isPt) {
                                        "Pagamento pendente no Google Play. As vidas serão liberadas assim que a compensação confirmar."
                                    } else {
                                        "Payment pending in Google Play. Lives unlock as soon as payment settles."
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFFFDE68A),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        billingStoreState.statusMessage?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (billingStoreState.isError) Color(0xFF3F1D2B) else Color(0xFF172554),
                                border = BorderStroke(
                                    1.dp,
                                    if (billingStoreState.isError) HeartRose.copy(alpha = 0.6f) else Color(0xFF60A5FA).copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 11.sp,
                                    color = if (billingStoreState.isError) Color(0xFFFDA4AF) else Color(0xFFBFDBFE),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { selectedBrlPackage = null },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF3E355E)),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(44.dp)
                            ) {
                                Text(
                                    text = if (isPt) "Voltar" else "Back",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Button(
                                onClick = { onLaunchRealBrlPurchase(pkg) },
                                enabled = !billingStoreState.isPurchaseInProgress,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(44.dp)
                                    .testTag("btn_confirm_brl_purchase")
                            ) {
                                if (billingStoreState.isPurchaseInProgress) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = if (billingStoreState.isPurchaseInProgress) {
                                        if (isPt) "Abrindo Play..." else "Opening Play..."
                                    } else {
                                        if (isPt) "Pagar no Google Play" else "Pay with Google Play"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        if (billingStoreState.connectionStatus != PlayBillingConnectionStatus.READY ||
                            billingStoreState.isError
                        ) {
                            OutlinedButton(
                                onClick = onRefreshBilling,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SoftLavender.copy(alpha = 0.55f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("btn_refresh_life_billing")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = SoftLavender,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPt) "Reconectar / Atualizar Google Play" else "Reconnect / Refresh Google Play",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onRestorePurchases,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF3E355E)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("btn_restore_life_purchases")
                        ) {
                            Text(
                                text = if (isPt) "Verificar / Restaurar compra no Play" else "Check / Restore Play purchase",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFDCD6F7)
                            )
                        }
                    }

                    // Step 1: Minimalist catalog with compact aligned buttons side by side
                    else -> {
                        if (onOpenProSubscription != null) {
                            val isSubActive = proSubscriptionState.isCurrentlyActive()
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSubActive) Color(0xFF152E24) else Color(0xFF1E1930),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSubActive) EmeraldGlow.copy(alpha = 0.5f) else GoldSpark.copy(alpha = 0.45f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        onDismiss()
                                        onOpenProSubscription()
                                    }
                                    .testTag("card_pro_subscription_banner")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = if (isSubActive) EmeraldGlow else GoldSpark,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isSubActive) {
                                                if (isPt) "Pro Ativo" else "Pro Active"
                                            } else {
                                                if (isPt) "Sem Anúncios +15/dia" else "Ad-Free +15/day"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (isSubActive) {
                                                if (isPt) "40 vidas diárias • ${proSubscriptionState.remainingDays()}d"
                                                else "40 daily lives • ${proSubscriptionState.remainingDays()}d"
                                            } else {
                                                if (isPt) "40 vidas todo dia" else "40 lives daily"
                                            },
                                            fontSize = 10.sp,
                                            color = Color(0xFFB8B0D8)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(9.dp),
                                        color = if (isSubActive) EmeraldGlow else GoldSpark,
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .clip(RoundedCornerShape(9.dp))
                                            .clickable {
                                                onDismiss()
                                                onOpenProSubscription()
                                            }
                                            .testTag("btn_open_pro_subscription")
                                    ) {
                                        Text(
                                            text = if (isSubActive) {
                                                if (isPt) "Ativa" else "Active"
                                            } else {
                                                val playMonthlyPrice = billingStoreState.productPrices[ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY]
                                                if (!playMonthlyPrice.isNullOrBlank()) {
                                                    if (isPt) "$playMonthlyPrice/mês" else "$playMonthlyPrice/mo"
                                                } else {
                                                    if (isPt) "R$ 13/mês" else "R$ 13/mo"
                                                }
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSubActive) Color.White else Color(0xFF1F1638),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = if (isPt) "Pacotes de vidas" else "Life packs",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB8B0D8)
                        )

                        // 3 compact aligned buttons side by side: +10 (R$ 5), +20 (R$ 10), +50 (R$ 35)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LifeShopCatalog.brlPackages.forEach { pkg ->
                                val isBest = pkg.livesAmount == 50
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF1E1A2E),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isBest) GoldSpark.copy(alpha = 0.6f) else HeartRose.copy(alpha = 0.45f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedBrlPackage = pkg }
                                        .testTag("card_brl_pack_${pkg.id}")
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = null,
                                                tint = HeartRose,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "+${pkg.livesAmount}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF16A34A),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("btn_buy_brl_${pkg.id}")
                                        ) {
                                            Text(
                                                text = pkg.formattedPrice(language, billingStoreState.productPrices),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        LifePurchaseOptionsSection(
                            language = language,
                            availableXp = availableXp,
                            availableCrystals = availableCrystals,
                            feedbackMessage = shopFeedback?.first ?: if (livesInfo.remaining > 0) {
                                if (isPt) "XP e Cristais liberam quando suas vidas chegarem a 0."
                                else "XP & Crystals unlock when lives reach 0."
                            } else null,
                            isFeedbackError = shopFeedback?.second ?: false,
                            darkSurface = true,
                            isLockedByQuota = livesInfo.remaining > 0,
                            onPurchasePackage = { pkg ->
                                val result = onPurchaseLives(pkg, null)
                                when (result) {
                                    is LifePurchaseResult.Success -> {
                                        shopFeedback = null
                                        localConfirmedReceipt = LifePurchaseReceipt(
                                            pkg = result.pkg,
                                            newRemainingLives = result.newRemainingLives,
                                            paymentMethod = null
                                        )
                                    }
                                    is LifePurchaseResult.RequiresRealBilling -> {
                                        selectedBrlPackage = pkg
                                    }
                                    is LifePurchaseResult.QuotaStillAvailable -> {
                                        shopFeedback = (
                                            if (isPt) "Grátis com XP/Cristais desbloqueia quando suas vidas chegarem a 0 (restam ${livesInfo.remaining} ❤️). Pacotes em R$ funcionam agora."
                                            else "Free XP/Crystal packs unlock once lives reach 0 (${livesInfo.remaining} ❤️ left). R$ packs work anytime."
                                        ) to false
                                    }
                                    is LifePurchaseResult.InsufficientFunds -> {
                                        val unit = if (result.currency == LifePurchaseCurrency.XP) "XP"
                                        else if (isPt) "cristais" else "crystals"
                                        shopFeedback = (
                                            if (isPt) "Precisa de ${result.required} $unit (tem ${result.available})."
                                            else "Needs ${result.required} $unit (have ${result.available})."
                                        ) to true
                                    }
                                    null -> Unit
                                }
                                result
                            },
                            onOpen3DGame = onOpen3DGame
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo informativo ao tocar na pill/card de Vidas Diárias.
 * Quando a cota diária chega a 0, exibe também as opções de compra de vidas.
 */
@Composable
fun DailyLivesInfoDialog(
    livesInfo: DailyLivesInfo,
    language: Language,
    availableXp: Int = 0,
    availableCrystals: Int = 0,
    onPurchaseLives: (LifeShopPackage) -> LifePurchaseResult? = { null },
    onOpenLifeShop: (() -> Unit)? = null,
    onOpen3DGame: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val isDepleted = livesInfo.remaining <= 0
    var purchaseFeedback by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

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
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(HeartRose, HeartCoral)))
                            .border(2.5.dp, GoldSpark, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDepleted) Icons.Default.ShoppingCart else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when {
                            isDepleted && isPt -> "Cota Diária Esgotada ❤️🛒"
                            isDepleted -> "Daily Quota Depleted ❤️🛒"
                            isPt -> "Suas ${livesInfo.maxDaily} Vidas Diárias ❤️✨"
                            else -> "Your ${livesInfo.maxDaily} Daily Lives ❤️✨"
                        },
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
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = when {
                            isDepleted && isPt -> {
                                "Sua cota de ${livesInfo.maxDaily} vidas de hoje acabou! Compre pacotes de +10, +20 ou +50 vidas ou use seu XP / Diamantes 3D abaixo."
                            }
                            isDepleted -> {
                                "Today's ${livesInfo.maxDaily} daily lives are used up! Buy +10, +20 or +50 extra lives or use your spendable XP / 3D Diamonds below."
                            }
                            isPt -> {
                                "Todos os dias você recebe ${livesInfo.maxDaily} vidas novinhas para praticar desafios e quizzes. Toque em '+' para comprar pacotes ou assinar o Pro (R$ 13/mês)."
                            }
                            else -> {
                                "Every day you receive ${livesInfo.maxDaily} fresh lives to solve coding challenges and quizzes. Tap '+' anytime to buy packs or go Pro (R$ 13/mo)."
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (onOpenLifeShop != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenLifeShop()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_open_brl_life_shop_from_info")
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPt) "Comprar Vidas (R$ 5 / R$ 10 / R$ 35)" else "Buy Extra Lives (R$ 5 / 10 / 35)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (isDepleted) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LifePurchaseOptionsSection(
                            language = language,
                            availableXp = availableXp,
                            availableCrystals = availableCrystals,
                            feedbackMessage = purchaseFeedback?.first,
                            isFeedbackError = purchaseFeedback?.second ?: false,
                            onPurchasePackage = { pkg ->
                                val result = onPurchaseLives(pkg)
                                purchaseFeedback = when (result) {
                                    is LifePurchaseResult.Success -> {
                                        val msg = if (isPt) {
                                            "❤️ +${result.pkg.livesAmount} vidas adicionadas! Novo saldo: ${result.newRemainingLives}"
                                        } else {
                                            "❤️ +${result.pkg.livesAmount} lives added! New balance: ${result.newRemainingLives}"
                                        }
                                        msg to false
                                    }
                                    is LifePurchaseResult.InsufficientFunds -> {
                                        val unit = if (result.currency == LifePurchaseCurrency.XP) {
                                            "XP"
                                        } else if (isPt) {
                                            "cristais 3D"
                                        } else {
                                            "3D crystals"
                                        }
                                        val msg = if (isPt) {
                                            "Saldo insuficiente: precisa de ${result.required} $unit (disponível: ${result.available})."
                                        } else {
                                            "Insufficient funds: requires ${result.required} $unit (available: ${result.available})."
                                        }
                                        msg to true
                                    }
                                    is LifePurchaseResult.RequiresRealBilling -> {
                                        onDismiss()
                                        onOpenLifeShop?.invoke()
                                        null
                                    }
                                    is LifePurchaseResult.QuotaStillAvailable -> null
                                    null -> null
                                }
                                result
                            },
                            onOpen3DGame = onOpen3DGame
                        )
                    } else if (purchaseFeedback?.second == false) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldGlow.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = purchaseFeedback?.first.orEmpty(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGlow,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
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
 * Diálogo exibido quando as 25 vidas diárias acabam, com opções imediatas de compra de vidas.
 */
@Composable
fun OutOfLivesDialog(
    language: Language,
    availableXp: Int = 0,
    availableCrystals: Int = 0,
    maxDailyLives: Int = DailyLivesInfo.MAX_DAILY_LIVES,
    onPurchaseLives: (LifeShopPackage) -> LifePurchaseResult? = { null },
    onOpenLifeShop: (() -> Unit)? = null,
    onOpen3DGame: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    var purchaseError by remember { mutableStateOf<String?>(null) }

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
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PurplePrimary, HeartRose))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = GoldSpark,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isPt) "Cota Diária Esgotada 🌙❤️" else "Daily Lives Reached 0 🌙❤️",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isPt) {
                        "Você usou suas $maxDailyLives vidas diárias! Compre +10, +20 ou +50 vidas ou resgate com XP / 50+ Diamantes 3D."
                    } else {
                        "You used all $maxDailyLives daily lives! Buy +10, +20 or +50 extra lives or redeem XP / 50+ 3D Diamonds."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDCD6F7),
                    textAlign = TextAlign.Center
                )

                if (onOpenLifeShop != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onDismiss()
                            onOpenLifeShop()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_open_brl_life_shop_from_depleted")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPt) "Abrir Loja (10/R$5 • 20/R$10 • 50/R$35)" else "Open Life Shop (R$ 5 / 10 / 35)",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LifePurchaseOptionsSection(
                    language = language,
                    availableXp = availableXp,
                    availableCrystals = availableCrystals,
                    feedbackMessage = purchaseError,
                    isFeedbackError = true,
                    darkSurface = true,
                    onPurchasePackage = { pkg ->
                        val result = onPurchaseLives(pkg)
                        when (result) {
                            is LifePurchaseResult.Success -> {
                                purchaseError = null
                                onDismiss()
                            }
                            is LifePurchaseResult.InsufficientFunds -> {
                                val unit = when (result.currency) {
                                    LifePurchaseCurrency.BRL -> "R$"
                                    LifePurchaseCurrency.XP -> "XP"
                                    LifePurchaseCurrency.CRYSTALS -> if (isPt) "cristais 3D" else "3D crystals"
                                }
                                purchaseError = if (isPt) {
                                    "Saldo insuficiente: precisa de ${result.required} $unit (disponível: ${result.available})."
                                } else {
                                    "Insufficient balance: requires ${result.required} $unit (available: ${result.available})."
                                }
                            }
                            is LifePurchaseResult.RequiresRealBilling -> {
                                purchaseError = null
                                onDismiss()
                                onOpenLifeShop?.invoke()
                            }
                            is LifePurchaseResult.QuotaStillAvailable -> {
                                purchaseError = null
                                onDismiss()
                            }
                            null -> Unit
                        }
                        result
                    },
                    onOpen3DGame = onOpen3DGame
                )

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF332A4D)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
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
                            text = if (isPt) "Subir de nível exige mais XP, mas restaura todas as suas vidas diárias na hora!"
                            else "Higher levels take more XP, and every level-up restores all your daily lives!",
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

