package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Language
import com.example.model.ModerateAdContent
import com.example.model.PlayBillingConnectionStatus
import com.example.model.PlayBillingStoreUiState
import com.example.model.ProSubscriptionPlan
import com.example.model.ProSubscriptionState
import com.example.ui.DailyLivesInfo

private val GoldCrown = Color(0xFFFFD54F)
private val EmeraldClean = Color(0xFF22C55E)
private val ProPurple = Color(0xFF7C3AED)
private val HeartRose = Color(0xFFFF4D7D)

/**
 * Banner de anúncio moderado e discreto exibido para usuários sem assinatura Pro.
 * Desaparece automaticamente assim que a assinatura Pro estiver ativa.
 */
@Composable
fun ModerateInlineAdBanner(
    ad: ModerateAdContent,
    language: Language,
    isProSubscriber: Boolean,
    onUpgradeProClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isProSubscriber) return

    val isPt = language == Language.PT
    val accent = ad.accentColor()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("moderate_inline_ad_${ad.id}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accent.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = if (isPt) "Anúncio" else "Ad",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = ad.sponsor(language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ProPurple.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, ProPurple.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onUpgradeProClick)
                        .testTag("btn_inline_ad_remove_${ad.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = GoldCrown,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isPt) "Sem anúncios • R$ 13/mês" else "Ad-free • R$ 13/mo",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                text = ad.headline(language),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = ad.body(language),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Anúncio intersticial moderado (exibido no máximo 1x a cada 4 desafios/quizzes e com cooldown de 3 min).
 * Nunca é exibido quando o usuário possui assinatura DevRoutine Pro.
 */
@Composable
fun ModerateInterstitialAdDialog(
    ad: ModerateAdContent,
    language: Language,
    onUpgradeProClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val accent = ad.accentColor()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171327)),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(accent, ProPurple))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_moderate_interstitial_ad")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (isPt) "PATROCINADO • ${ad.sponsor(language)}" else "SPONSORED • ${ad.sponsor(language)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_interstitial_ad")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isPt) "Fechar anúncio" else "Close ad",
                            tint = Color(0xFFDCD6F7)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(accent, ProPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = ad.headline(language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = ad.body(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDCD6F7),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        onDismiss()
                        onUpgradeProClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProPurple),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_interstitial_upgrade_pro")
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = GoldCrown,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPt) "Tirar Anúncios +15 Vidas/Dia (R$ 13/mês)"
                        else "Remove Ads +15 Lives/Day (R$ 13/mo)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF47415C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = if (isPt) "Fechar anúncio" else "Close ad",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFDCD6F7)
                    )
                }
            }
        }
    }
}

/**
 * Cartão resumido da Assinatura DevRoutine Pro (R$ 13/mês) para usar na Loja de Vidas e Configurações.
 */
@Composable
fun ProSubscriptionBannerCard(
    subscriptionState: ProSubscriptionState,
    language: Language,
    onOpenSubscriptionDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPt = language == Language.PT
    val isActive = subscriptionState.isCurrentlyActive()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF163226) else Color(0xFF23183D)
        ),
        border = BorderStroke(
            width = 1.4.dp,
            brush = Brush.linearGradient(
                if (isActive) listOf(EmeraldClean, GoldCrown)
                else listOf(GoldCrown, ProPurple, HeartRose)
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSubscriptionDialog)
            .testTag("card_pro_subscription_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            if (isActive) listOf(EmeraldClean, Color(0xFF15803D))
                            else listOf(GoldCrown, Color(0xFFF59E0B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = if (isActive) Color.White else Color(0xFF1F1638),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isActive) {
                        if (isPt) "Assinatura Pro Ativa 👑" else "Pro Subscription Active 👑"
                    } else {
                        if (isPt) "DevRoutine Pro • R$ 13,00/mês" else "DevRoutine Pro • R$ 13.00/mo"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isActive) {
                        if (isPt) "Sem anúncios • 40 vidas diárias (+15 extras) • ${subscriptionState.remainingDays()}d restantes"
                        else "Ad-free • 40 daily lives (+15 bonus) • ${subscriptionState.remainingDays()}d left"
                    } else {
                        if (isPt) "Remova todos os anúncios + ganhe +15 vidas todo dia (40/dia)"
                        else "Remove all ads + get +15 extra daily lives (40/day)"
                    },
                    fontSize = 11.sp,
                    color = Color(0xFFE2DCF8)
                )
            }

            Button(
                onClick = onOpenSubscriptionDialog,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) EmeraldClean else GoldCrown
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("btn_open_pro_subscription")
            ) {
                Text(
                    text = if (isActive) {
                        if (isPt) "Ativa" else "Active"
                    } else {
                        if (isPt) "R$ 13/mês" else "R$ 13/mo"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) Color.White else Color(0xFF1F1638)
                )
            }
        }
    }
}

/**
 * Interface de Assinatura Mensal DevRoutine Pro (R$ 13,00/mês):
 * Remove anúncios e adiciona +15 vidas diárias (passando de 25 para 40 por dia).
 */
@Composable
fun ProSubscriptionDialog(
    subscriptionState: ProSubscriptionState,
    dailyLives: DailyLivesInfo,
    language: Language,
    billingStoreState: PlayBillingStoreUiState = PlayBillingStoreUiState(),
    onSubscribeWithPlay: () -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    onRefreshBilling: () -> Unit = {},
    onManageInPlayStore: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val isActive = subscriptionState.isCurrentlyActive()
    val monthlyPriceText = ProSubscriptionState.DEFAULT_PLAN.formattedMonthlyPrice(
        language = language,
        livePlayPrices = billingStoreState.productPrices
    )
    val isPending = ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY in billingStoreState.pendingProductIds

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF171226)),
            border = BorderStroke(1.8.dp, Brush.linearGradient(listOf(GoldCrown, ProPurple, HeartRose))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("dialog_pro_subscription")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(GoldCrown, Color(0xFFF59E0B)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFF1F1638),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isPt) "Assinatura DevRoutine Pro" else "DevRoutine Pro Plan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Text(
                                text = if (isPt) "$monthlyPriceText • Cancele quando quiser no Play"
                                else "$monthlyPriceText • Cancel anytime on Google Play",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldCrown
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_pro_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isPt) "Fechar" else "Close",
                            tint = Color(0xFFDCD6F7)
                        )
                    }
                }

                // Benefits Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF221A38)),
                    border = BorderStroke(1.dp, GoldCrown.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = EmeraldClean, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = if (isPt) "Zero Anúncios no App" else "100% Ad-Free Experience",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isPt) "Remove todos os banners e anúncios entre desafios."
                                    else "Removes all banners and interstitial ads between challenges.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFDCD6F7)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = HeartRose, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = if (isPt) "+15 Vidas Diárias (40 por dia!)" else "+15 Daily Lives (40 per day!)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isPt) "Sua cota diária sobe de 25 para 40 vidas todo dia, além de +15 vidas na hora ao assinar."
                                    else "Daily quota increases from 25 to 40 lives every day, plus +15 lives immediately upon subscribing.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFDCD6F7)
                                )
                            }
                        }
                    }
                }

                if (isActive) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF153224)),
                        border = BorderStroke(1.2.dp, EmeraldClean.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldClean, modifier = Modifier.size(36.dp))
                            Text(
                                text = if (isPt) "Assinatura Pro Ativa via Google Play 👑" else "Pro Subscription Active via Google Play 👑",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = if (isPt) {
                                    "Vidas hoje: ${dailyLives.remaining}/${dailyLives.maxDaily} • Anúncios removidos • ${subscriptionState.remainingDays()} dias restantes no ciclo"
                                } else {
                                    "Lives today: ${dailyLives.remaining}/${dailyLives.maxDaily} • Ads removed • ${subscriptionState.remainingDays()} days left in cycle"
                                },
                                fontSize = 12.sp,
                                color = Color(0xFFBBF7D0),
                                textAlign = TextAlign.Center
                            )
                            subscriptionState.orderId?.takeIf { it.isNotBlank() }?.let { oid ->
                                Text(
                                    text = if (isPt) "Pedido Play: $oid" else "Play Order: $oid",
                                    fontSize = 10.sp,
                                    color = Color(0xFFA7F3D0),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onManageInPlayStore,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, GoldCrown.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(46.dp)
                                .testTag("btn_manage_pro_play_store")
                        ) {
                            Text(
                                text = if (isPt) "Gerenciar no Play" else "Manage on Play",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldCrown
                            )
                        }
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldClean),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text(
                                text = if (isPt) "Continuar ✨" else "Continue ✨",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E1A2E),
                        border = BorderStroke(1.dp, Color(0xFF332C4A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isPt) "Assinatura mensal via Google Play Billing" else "Monthly subscription via Google Play Billing",
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
                                    if (billingStoreState.isProductReadyInPlay(ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY)) {
                                        if (isPt) "• Play Store conectada e assinatura ativa no catálogo" else "• Play Store connected & subscription active in catalog"
                                    } else {
                                        if (isPt) "• Play Store conectada (aguardando SKU no Play Console)" else "• Play Store connected (awaiting Play Console SKU)"
                                    }
                                PlayBillingConnectionStatus.CONNECTING ->
                                    if (isPt) "• Conectando ao Google Play..." else "• Connecting to Google Play..."
                                PlayBillingConnectionStatus.UNAVAILABLE ->
                                    if (isPt) "• Google Play Billing indisponível neste dispositivo" else "• Google Play Billing unavailable on device"
                                PlayBillingConnectionStatus.DISCONNECTED ->
                                    if (isPt) "• Play Billing desconectado" else "• Play Billing disconnected"
                            }
                            Text(
                                text = connectionLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (billingStoreState.connectionStatus == PlayBillingConnectionStatus.READY) EmeraldClean else Color(0xFFFCD34D)
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
                                    "Assinatura pendente de confirmação no Google Play. Os benefícios serão liberados assim que o pagamento compensar."
                                } else {
                                    "Subscription pending in Google Play. Benefits unlock automatically once payment clears."
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

                    Button(
                        onClick = onSubscribeWithPlay,
                        enabled = !billingStoreState.isPurchaseInProgress,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldCrown),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_confirm_pro_subscription")
                    ) {
                        if (billingStoreState.isPurchaseInProgress) {
                            CircularProgressIndicator(
                                color = Color(0xFF1F1638),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFF1F1638),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (billingStoreState.isPurchaseInProgress) {
                                if (isPt) "Abrindo Google Play..." else "Opening Google Play..."
                            } else {
                                if (isPt) "Assinar no Google Play • $monthlyPriceText"
                                else "Subscribe via Google Play • $monthlyPriceText"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1F1638)
                        )
                    }

                    if (billingStoreState.connectionStatus != PlayBillingConnectionStatus.READY ||
                        billingStoreState.isError
                    ) {
                        OutlinedButton(
                            onClick = onRefreshBilling,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GoldCrown.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("btn_refresh_pro_billing")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = GoldCrown,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPt) "Reconectar / Atualizar Google Play" else "Reconnect / Refresh Google Play",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldCrown
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onRestorePurchases,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF3E355E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("btn_restore_pro_subscription")
                    ) {
                        Text(
                            text = if (isPt) "Restaurar assinatura do Google Play" else "Restore Google Play subscription",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDCD6F7)
                        )
                    }
                }
            }
        }
    }
}
