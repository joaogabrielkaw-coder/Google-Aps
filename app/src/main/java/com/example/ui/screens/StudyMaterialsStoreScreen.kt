package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.android.billingclient.api.ProductDetails
import com.example.model.Language
import com.example.model.PlayBillingConnectionStatus
import com.example.model.PlayBillingStoreUiState
import com.example.model.StudyMaterialBillingItem
import com.example.model.StudyMaterialCatalog
import com.example.model.StudyMaterialCategory
import com.example.model.StudyMaterialPackage
import com.example.ui.theme.LocalDevRoutineColors

private val EmeraldSuccess = Color(0xFF16A34A)
private val EmeraldBright = Color(0xFF4ADE80)
private val AmberPending = Color(0xFFF59E0B)

@Composable
fun StudyMaterialsStoreScreen(
    language: Language,
    billingStoreState: PlayBillingStoreUiState,
    productDetailsMap: Map<String, ProductDetails>,
    ownedProductIds: Set<String>,
    onBack: () -> Unit,
    onPurchasePackage: (String) -> Unit,
    onRefreshCatalog: () -> Unit,
    onRestorePurchases: () -> Unit,
    onDismissStatusMessage: () -> Unit
) {
    BackHandler { onBack() }

    val isPt = language == Language.PT
    val brand = LocalDevRoutineColors.current
    var selectedCategory by remember { mutableStateOf(StudyMaterialCategory.ALL) }
    var readerPackage by remember { mutableStateOf<StudyMaterialPackage?>(null) }

    val billingItems = remember(productDetailsMap, ownedProductIds, billingStoreState.pendingProductIds) {
        StudyMaterialCatalog.packages.map { pkg ->
            StudyMaterialBillingItem(
                pkg = pkg,
                productDetails = productDetailsMap[pkg.productId],
                isOwned = StudyMaterialCatalog.isPackageUnlocked(pkg, ownedProductIds),
                isPending = pkg.productId in billingStoreState.pendingProductIds
            )
        }
    }

    val filteredItems = remember(billingItems, selectedCategory) {
        if (selectedCategory == StudyMaterialCategory.ALL) {
            billingItems
        } else {
            billingItems.filter { it.pkg.category == selectedCategory }
        }
    }

    val unlockedCount = remember(billingItems) { billingItems.count { it.isOwned } }
    val liveSkuCount = remember(billingItems) { billingItems.count { it.isAvailableInPlay } }

    readerPackage?.let { activePkg ->
        StudyMaterialReaderDialog(
            pkg = activePkg,
            language = language,
            onDismiss = { readerPackage = null }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("screen_study_materials_store"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp)
        ) {
            // Dedicated Screen Header with Back, BillingClient Status, and Refresh
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_back_study_store")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isPt) "Voltar" else "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPt) "Materiais de Estudo" else "Study Materials Store",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isPt) "Loja oficial via Google Play BillingClient" else "Official store via Google Play BillingClient",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    PlayBillingStatusChip(
                        status = billingStoreState.connectionStatus,
                        isPt = isPt
                    )

                    IconButton(
                        onClick = onRefreshCatalog,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("btn_refresh_billing_catalog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = if (isPt) "Atualizar produtos do Play" else "Refresh Play products",
                            tint = brand.primary
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Hero Summary Banner showing live BillingClient metrics and Restore Purchases
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(
                            1.2.dp,
                            Brush.linearGradient(listOf(brand.primary, brand.accent))
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_study_store_hero")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            brand.primary.copy(alpha = 0.14f),
                                            brand.accent.copy(alpha = 0.08f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.linearGradient(listOf(brand.primary, brand.accent))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isPt) "Cadernos & Kits Avançados" else "Pro Study Kits & Handbooks",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                    Text(
                                        text = if (isPt) {
                                            "Pagamento único via Google Play (Pix, Cartão ou Saldo Play). Acesso vitalício vinculado à conta."
                                        } else {
                                            "One-time purchase via Google Play (Pix, Card, or Play Balance). Lifetime account access."
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                        Text(
                                            text = if (isPt) "Desbloqueados" else "Unlocked",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$unlockedCount / ${StudyMaterialCatalog.packages.size}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (unlockedCount > 0) EmeraldSuccess else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                        Text(
                                            text = if (isPt) "SKUs no Play" else "Live Play SKUs",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "$liveSkuCount / ${StudyMaterialCatalog.packages.size}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (liveSkuCount > 0) brand.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = onRestorePurchases,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, brand.primary.copy(alpha = 0.55f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("btn_restore_study_purchases")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = null,
                                        tint = brand.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPt) "Restaurar" else "Restore",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = brand.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // BillingClient Status / Error / Pending Feedback Banner
                item {
                    AnimatedVisibility(visible = !billingStoreState.statusMessage.isNullOrBlank()) {
                        val message = billingStoreState.statusMessage.orEmpty()
                        val isErr = billingStoreState.isError
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isErr) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isErr) MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                else brand.primary.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("banner_study_billing_status")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = message,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isErr) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = onDismissStatusMessage,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = if (isPt) "Fechar aviso" else "Dismiss message",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Category Filter Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(StudyMaterialCategory.entries) { category ->
                            val selected = selectedCategory == category
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (selected) brand.primary else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) brand.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedCategory = category }
                                    .testTag("chip_study_category_${category.name.lowercase()}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = category.label(language),
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Study Material Packages queried from BillingClient
                items(
                    items = filteredItems,
                    key = { it.pkg.productId }
                ) { item ->
                    StudyMaterialPackageCard(
                        item = item,
                        language = language,
                        isPurchaseInProgress = billingStoreState.isPurchaseInProgress,
                        onPurchaseClick = { onPurchasePackage(item.pkg.productId) },
                        onOpenMaterialClick = { readerPackage = item.pkg }
                    )
                }

                // Transparent Play Billing Info Footer
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isPt) "Detalhes do Google Play Billing" else "Google Play Billing Details",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isPt) {
                                    "Os preços exibidos são carregados em tempo real via BillingClient.queryProductDetailsAsync e validados via queryPurchasesAsync + acknowledgePurchase. Em compras pendentes (ex.: Pix), o desbloqueio ocorre após confirmação do Google Play."
                                } else {
                                    "Prices load live from BillingClient.queryProductDetailsAsync and entitlements verify via queryPurchasesAsync + acknowledgePurchase. Pending payments unlock automatically after Google Play confirmation."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayBillingStatusChip(
    status: PlayBillingConnectionStatus,
    isPt: Boolean
) {
    val (label, containerColor, contentColor, icon) = when (status) {
        PlayBillingConnectionStatus.READY -> Quadruple(
            if (isPt) "Play Ativo" else "Play Ready",
            EmeraldSuccess.copy(alpha = 0.16f),
            EmeraldSuccess,
            Icons.Default.CloudDone
        )
        PlayBillingConnectionStatus.CONNECTING -> Quadruple(
            if (isPt) "Conectando" else "Connecting",
            AmberPending.copy(alpha = 0.16f),
            AmberPending,
            Icons.Default.Schedule
        )
        PlayBillingConnectionStatus.UNAVAILABLE -> Quadruple(
            if (isPt) "Indisponível" else "Unavailable",
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error,
            Icons.Default.CloudOff
        )
        PlayBillingConnectionStatus.DISCONNECTED -> Quadruple(
            if (isPt) "Desconectado" else "Offline",
            AmberPending.copy(alpha = 0.16f),
            AmberPending,
            Icons.Default.CloudOff
        )
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.45f)),
        modifier = Modifier.testTag("chip_billing_connection_status")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}

@Composable
private fun StudyMaterialPackageCard(
    item: StudyMaterialBillingItem,
    language: Language,
    isPurchaseInProgress: Boolean,
    onPurchaseClick: () -> Unit,
    onOpenMaterialClick: () -> Unit
) {
    val isPt = language == Language.PT
    val pkg = item.pkg
    val accent = pkg.accentColor()
    val formattedPrice = item.formattedPrice(language)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (item.isOwned || pkg.isFullBundle) 1.6.dp else 1.dp,
            color = when {
                item.isOwned -> EmeraldSuccess.copy(alpha = 0.7f)
                pkg.isFullBundle -> accent.copy(alpha = 0.75f)
                else -> accent.copy(alpha = 0.35f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_study_material_${pkg.productId}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Accent Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            if (item.isOwned) listOf(EmeraldSuccess, EmeraldBright)
                            else listOf(accent, accent.copy(alpha = 0.4f))
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Badges Row: Category, Highlight Badge, and Live BillingClient SKU Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = pkg.category.label(language),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        pkg.badge(language)?.let { badgeText ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = badgeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (item.isAvailableInPlay) {
                            EmeraldSuccess.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = if (item.isAvailableInPlay) {
                                if (isPt) "SKU Play Ativo" else "Live Play SKU"
                            } else {
                                pkg.productId
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.isAvailableInPlay) {
                                EmeraldSuccess
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                // Title & Price Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.displayTitle(language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.displayDescription(language),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (item.isOwned) {
                            EmeraldSuccess.copy(alpha = 0.16f)
                        } else {
                            accent.copy(alpha = 0.14f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (item.isOwned) EmeraldSuccess.copy(alpha = 0.5f) else accent.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = if (item.isOwned) {
                                if (isPt) "Adquirido" else "Owned"
                            } else {
                                formattedPrice
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (item.isOwned) EmeraldSuccess else accent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Metadata strip (modules + hours)
                Text(
                    text = if (isPt) {
                        "${pkg.moduleCount} módulos práticos • ~${pkg.estimatedHours}h de estudo"
                    } else {
                        "${pkg.moduleCount} hands-on modules • ~${pkg.estimatedHours}h of study"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Topic Highlights
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    pkg.highlights(language).forEach { highlight ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (item.isOwned) EmeraldSuccess else accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = highlight,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Real Purchase / Open Material Action Button
                when {
                    item.isOwned -> {
                        Button(
                            onClick = onOpenMaterialClick,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_open_study_material_${pkg.productId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPt) "Desbloqueado • Abrir Material" else "Unlocked • Open Material",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    item.isPending -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AmberPending.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, AmberPending.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("badge_pending_study_material_${pkg.productId}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = AmberPending,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPt) "Pagamento Pendente no Play" else "Payment Pending in Play",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberPending
                                )
                            }
                        }
                    }

                    else -> {
                        Button(
                            onClick = onPurchaseClick,
                            enabled = !isPurchaseInProgress,
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_buy_study_material_${pkg.productId}")
                        ) {
                            if (isPurchaseInProgress) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPt) "Abrindo Google Play..." else "Opening Google Play...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isPt) {
                                        "Comprar no Google Play • $formattedPrice"
                                    } else {
                                        "Buy on Google Play • $formattedPrice"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
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
private fun StudyMaterialReaderDialog(
    pkg: StudyMaterialPackage,
    language: Language,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    val accent = pkg.accentColor()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, accent.copy(alpha = 0.65f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("dialog_study_material_reader")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = pkg.title(language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isPt) "Fechar" else "Close"
                        )
                    }
                }

                pkg.sections.forEach { section ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = section.title(language),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = section.summary(language),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0E0B1A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = section.codeSnippet,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFFE9D5FF),
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(
                        text = if (isPt) "Concluir Leitura" else "Done Reading",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
