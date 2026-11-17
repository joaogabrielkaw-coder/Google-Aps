package com.example.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.model.Language
import com.example.model.LifeShopCatalog
import com.example.model.LifeShopPackage
import com.example.model.PlayBillingConnectionStatus
import com.example.model.PlayBillingStoreUiState
import com.example.model.ProSubscriptionPlan
import com.example.model.StudyMaterialCatalog
import com.example.model.StudyMaterialOwnershipStorage
import com.example.model.StudyMaterialPackage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

/**
 * Registro persistente de tokens de compra já creditados para evitar
 * duplicidade caso o Google Play reentregue a mesma compra antes do consumo.
 */
object PlayBillingTokenRegistry {
    private const val KEY_PROCESSED_TOKENS = "processed_play_purchase_tokens"
    private const val MAX_STORED_TOKENS = 100

    fun hasProcessedToken(prefs: SharedPreferences, purchaseToken: String): Boolean {
        if (purchaseToken.isBlank()) return false
        val saved = prefs.getStringSet(KEY_PROCESSED_TOKENS, emptySet()) ?: emptySet()
        return purchaseToken in saved
    }

    fun markTokenProcessed(prefs: SharedPreferences, purchaseToken: String) {
        if (purchaseToken.isBlank()) return
        val existing = prefs.getStringSet(KEY_PROCESSED_TOKENS, emptySet())?.toMutableList() ?: mutableListOf()
        if (purchaseToken !in existing) {
            existing.add(purchaseToken)
            val trimmed = if (existing.size > MAX_STORED_TOKENS) {
                existing.takeLast(MAX_STORED_TOKENS)
            } else {
                existing
            }
            prefs.edit().putStringSet(KEY_PROCESSED_TOKENS, trimmed.toSet()).apply()
        }
    }
}

/**
 * Gerenciador real do Google Play Billing Library para todos os itens pagos da loja:
 * - Pacotes consumíveis de vidas: lives_brl_10, lives_brl_20, lives_brl_50 (ProductType.INAPP)
 * - Assinatura mensal: devroutine_pro_monthly (ProductType.SUBS)
 *
 * Nunca credita vidas nem ativa assinatura sem confirmação real `PurchaseState.PURCHASED`
 * e validação/consumo/reconhecimento via Google Play.
 */
class PlayBillingManager(
    context: Context,
    private val prefs: SharedPreferences,
    private val scope: CoroutineScope,
    private val languageProvider: () -> Language,
    private val obfuscatedAccountIdProvider: () -> String?,
    private val onConsumableLivesGranted: (
        pkg: LifeShopPackage,
        quantity: Int,
        purchaseToken: String,
        orderId: String?
    ) -> Unit,
    private val onSubscriptionVerified: (
        purchaseToken: String,
        orderId: String?,
        purchaseTimeMillis: Long,
        isAutoRenewing: Boolean,
        userInitiated: Boolean
    ) -> Unit,
    private val onSubscriptionNotActiveInPlay: () -> Unit,
    private val onStudyMaterialVerified: (
        pkg: StudyMaterialPackage,
        orderId: String?,
        userInitiated: Boolean
    ) -> Unit = { _, _, _ -> },
    private val onOwnedStudyMaterialsSynced: (Set<String>) -> Unit = {}
) {
    private val appContext = context.applicationContext
    private val productDetailsMap = ConcurrentHashMap<String, ProductDetails>()
    private val inFlightConsumeTokens = ConcurrentHashMap.newKeySet<String>()
    private val inFlightAckTokens = ConcurrentHashMap.newKeySet<String>()

    private val _storeUiState = MutableStateFlow(PlayBillingStoreUiState())
    val storeUiState: StateFlow<PlayBillingStoreUiState> = _storeUiState.asStateFlow()

    private val _studyMaterialProductDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val studyMaterialProductDetails: StateFlow<Map<String, ProductDetails>> =
        _studyMaterialProductDetails.asStateFlow()

    private val _ownedStudyMaterialIds = MutableStateFlow(
        StudyMaterialOwnershipStorage.loadOwnedIds(prefs)
    )
    val ownedStudyMaterialIds: StateFlow<Set<String>> = _ownedStudyMaterialIds.asStateFlow()

    @Volatile
    private var isConnecting = false

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        _storeUiState.update { it.copy(isPurchaseInProgress = false) }
        val isPt = languageProvider() == Language.PT
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    scope.launch {
                        handlePurchasesList(purchases, userInitiated = true)
                    }
                }
            }
            BillingResponseCode.USER_CANCELED -> {
                _storeUiState.update {
                    it.copy(
                        statusMessage = if (isPt) "Compra cancelada no Google Play." else "Purchase cancelled in Google Play.",
                        isError = false
                    )
                }
            }
            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _storeUiState.update {
                    it.copy(
                        statusMessage = if (isPt) {
                            "Item já adquirido no Google Play. Sincronizando entrega..."
                        } else {
                            "Item already owned on Google Play. Syncing entitlement..."
                        },
                        isError = false
                    )
                }
                scope.launch {
                    queryAndProcessAllPurchases(syncRevocations = true, userInitiated = true)
                }
            }
            else -> {
                _storeUiState.update {
                    it.copy(
                        statusMessage = mapBillingErrorMessage(billingResult, isPt),
                        isError = true
                    )
                }
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun startConnectionAndSync(userInitiated: Boolean = false) {
        if (billingClient.isReady) {
            scope.launch {
                refreshProductsAndPurchases(userInitiated = userInitiated)
            }
            return
        }
        if (isConnecting) return
        isConnecting = true
        _storeUiState.update {
            it.copy(
                connectionStatus = PlayBillingConnectionStatus.CONNECTING,
                statusMessage = if (userInitiated) {
                    if (languageProvider() == Language.PT) "Conectando ao Google Play..." else "Connecting to Google Play..."
                } else it.statusMessage,
                isError = false
            )
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnecting = false
                val isPt = languageProvider() == Language.PT
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    _storeUiState.update {
                        it.copy(
                            connectionStatus = PlayBillingConnectionStatus.READY,
                            statusMessage = if (userInitiated) {
                                if (isPt) "Conectado ao Google Play Billing." else "Connected to Google Play Billing."
                            } else null,
                            isError = false
                        )
                    }
                    scope.launch {
                        refreshProductsAndPurchases(userInitiated = userInitiated)
                    }
                } else {
                    val status = if (billingResult.responseCode == BillingResponseCode.BILLING_UNAVAILABLE) {
                        PlayBillingConnectionStatus.UNAVAILABLE
                    } else {
                        PlayBillingConnectionStatus.DISCONNECTED
                    }
                    _storeUiState.update {
                        it.copy(
                            connectionStatus = status,
                            statusMessage = if (userInitiated) mapBillingErrorMessage(billingResult, isPt) else it.statusMessage,
                            isError = userInitiated
                        )
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnecting = false
                _storeUiState.update {
                    it.copy(connectionStatus = PlayBillingConnectionStatus.DISCONNECTED)
                }
            }
        })
    }

    fun refreshOnResume() {
        if (!billingClient.isReady) {
            startConnectionAndSync(userInitiated = false)
            return
        }
        scope.launch {
            queryAndProcessAllPurchases(syncRevocations = true, userInitiated = false)
        }
    }

    fun refreshCatalogAndPurchases() {
        if (!billingClient.isReady) {
            startConnectionAndSync(userInitiated = true)
            return
        }
        scope.launch {
            refreshProductsAndPurchases(userInitiated = true)
        }
    }

    fun clearStatusMessage() {
        _storeUiState.update { it.copy(statusMessage = null, isError = false) }
    }

    fun restorePurchases() {
        val isPt = languageProvider() == Language.PT
        if (!billingClient.isReady) {
            startConnectionAndSync(userInitiated = true)
            return
        }
        scope.launch {
            _storeUiState.update {
                it.copy(
                    statusMessage = if (isPt) "Verificando compras no Google Play..." else "Checking Google Play purchases...",
                    isError = false
                )
            }
            val foundCount = queryAndProcessAllPurchases(syncRevocations = true, userInitiated = true)
            if (foundCount == 0 && _storeUiState.value.pendingProductIds.isEmpty()) {
                _storeUiState.update {
                    it.copy(
                        statusMessage = if (isPt) {
                            "Nenhuma compra pendente ou assinatura ativa encontrada nesta conta Google Play."
                        } else {
                            "No pending purchases or active subscription found for this Google Play account."
                        },
                        isError = false
                    )
                }
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        val isPt = languageProvider() == Language.PT
        scope.launch {
            if (!billingClient.isReady) {
                val connected = ensureConnectedAwait()
                if (!connected) {
                    _storeUiState.update {
                        it.copy(
                            connectionStatus = PlayBillingConnectionStatus.UNAVAILABLE,
                            statusMessage = if (isPt) {
                                "Google Play Billing não está disponível neste dispositivo/emulador ou sem conta Play ativa."
                            } else {
                                "Google Play Billing is unavailable on this device/emulator or without an active Play account."
                            },
                            isError = true
                        )
                    }
                    return@launch
                }
            }

            if (productDetailsMap[productId] == null) {
                queryAllCatalogProducts()
            }

            val details = productDetailsMap[productId]
            if (details == null) {
                _storeUiState.update {
                    it.copy(
                        statusMessage = if (isPt) {
                            "Produto \"$productId\" não encontrado no Google Play Console. Cadastre e ative o item na faixa de teste do Play Console."
                        } else {
                            "Product \"$productId\" not found in Google Play Console. Configure and activate this SKU in a Play Console test track."
                        },
                        isError = true
                    )
                }
                return@launch
            }

            val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)

            if (details.productType == ProductType.SUBS) {
                val subOfferToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
                if (subOfferToken.isNullOrBlank()) {
                    _storeUiState.update {
                        it.copy(
                            statusMessage = if (isPt) {
                                "Plano base da assinatura \"$productId\" indisponível no Google Play Console."
                            } else {
                                "Base plan offer token for \"$productId\" is unavailable in Google Play Console."
                            },
                            isError = true
                        )
                    }
                    return@launch
                }
                productParamsBuilder.setOfferToken(subOfferToken)
            } else {
                val inAppOfferToken = details.oneTimePurchaseOfferDetailsList
                    ?.firstOrNull()
                    ?.offerToken
                if (!inAppOfferToken.isNullOrBlank()) {
                    productParamsBuilder.setOfferToken(inAppOfferToken)
                }
            }

            val flowParamsBuilder = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParamsBuilder.build()))

            obfuscatedAccountIdProvider()?.takeIf { it.isNotBlank() }?.let { uid ->
                flowParamsBuilder.setObfuscatedAccountId(uid)
            }

            _storeUiState.update { it.copy(isPurchaseInProgress = true, statusMessage = null, isError = false) }
            val launchResult = withContext(Dispatchers.Main) {
                billingClient.launchBillingFlow(activity, flowParamsBuilder.build())
            }
            if (launchResult.responseCode != BillingResponseCode.OK) {
                _storeUiState.update {
                    it.copy(
                        isPurchaseInProgress = false,
                        statusMessage = mapBillingErrorMessage(launchResult, isPt),
                        isError = true
                    )
                }
            }
        }
    }

    fun endConnection() {
        runCatching {
            if (billingClient.isReady) {
                billingClient.endConnection()
            }
        }
    }

    private suspend fun ensureConnectedAwait(): Boolean = suspendCancellableCoroutine { cont ->
        if (billingClient.isReady) {
            if (cont.isActive) cont.resume(true)
            return@suspendCancellableCoroutine
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val ok = billingResult.responseCode == BillingResponseCode.OK
                _storeUiState.update {
                    it.copy(
                        connectionStatus = if (ok) {
                            PlayBillingConnectionStatus.READY
                        } else if (billingResult.responseCode == BillingResponseCode.BILLING_UNAVAILABLE) {
                            PlayBillingConnectionStatus.UNAVAILABLE
                        } else {
                            PlayBillingConnectionStatus.DISCONNECTED
                        }
                    )
                }
                if (cont.isActive) cont.resume(ok)
            }

            override fun onBillingServiceDisconnected() {
                _storeUiState.update {
                    it.copy(connectionStatus = PlayBillingConnectionStatus.DISCONNECTED)
                }
                if (cont.isActive) cont.resume(false)
            }
        })
    }

    private suspend fun refreshProductsAndPurchases(userInitiated: Boolean) {
        queryAllCatalogProducts()
        queryAndProcessAllPurchases(syncRevocations = true, userInitiated = userInitiated)
    }

    private suspend fun queryAllCatalogProducts() {
        val lifeInAppIds = LifeShopCatalog.brlPackages.mapNotNull { it.playProductId }
        val studyMaterialIds = StudyMaterialCatalog.allProductIds
        val inAppIds = (lifeInAppIds + studyMaterialIds).distinct()
        val inAppDetails = queryProductDetailsList(inAppIds, ProductType.INAPP)
        val subsDetails = queryProductDetailsList(
            listOf(ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY),
            ProductType.SUBS
        )

        val updatedPrices = mutableMapOf<String, String>()
        val availableIds = mutableSetOf<String>()
        val studyDetailsMap = mutableMapOf<String, ProductDetails>()

        inAppDetails.forEach { details ->
            productDetailsMap[details.productId] = details
            availableIds += details.productId
            if (StudyMaterialCatalog.findByProductId(details.productId) != null) {
                studyDetailsMap[details.productId] = details
            }
            val price = details.oneTimePurchaseOfferDetailsList
                ?.firstOrNull()
                ?.formattedPrice
                ?: details.oneTimePurchaseOfferDetails?.formattedPrice
            if (!price.isNullOrBlank()) {
                updatedPrices[details.productId] = price
            }
        }

        subsDetails.forEach { details ->
            productDetailsMap[details.productId] = details
            availableIds += details.productId
            val price = details.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()
                ?.formattedPrice
            if (!price.isNullOrBlank()) {
                updatedPrices[details.productId] = price
            }
        }

        _studyMaterialProductDetails.value = studyDetailsMap
        _storeUiState.update {
            it.copy(
                connectionStatus = if (billingClient.isReady) PlayBillingConnectionStatus.READY else it.connectionStatus,
                productPrices = updatedPrices,
                availableProductIds = availableIds
            )
        }
    }

    private suspend fun queryProductDetailsList(
        productIds: List<String>,
        @ProductType productType: String
    ): List<ProductDetails> {
        if (productIds.isEmpty() || !billingClient.isReady) return emptyList()
        val products = productIds.map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(productType)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
                    val list = if (billingResult.responseCode == BillingResponseCode.OK) {
                        queryProductDetailsResult.productDetailsList
                    } else {
                        emptyList()
                    }
                    if (cont.isActive) cont.resume(list)
                }
            }
        }
    }

    private suspend fun queryAndProcessAllPurchases(
        syncRevocations: Boolean,
        userInitiated: Boolean
    ): Int {
        if (!billingClient.isReady) return 0
        val (inAppResult, inAppPurchases) = queryPurchasesAwait(ProductType.INAPP)
        val (subsResult, subsPurchases) = queryPurchasesAwait(ProductType.SUBS)

        val combined = buildList {
            if (inAppResult.responseCode == BillingResponseCode.OK) addAll(inAppPurchases)
            if (subsResult.responseCode == BillingResponseCode.OK) addAll(subsPurchases)
        }

        if (combined.isNotEmpty()) {
            handlePurchasesList(combined, userInitiated = userInitiated)
        } else {
            _storeUiState.update { it.copy(pendingProductIds = emptySet()) }
        }

        if (syncRevocations && inAppResult.responseCode == BillingResponseCode.OK) {
            val activeStudyIdsFromPlay = inAppPurchases
                .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                .flatMap { it.products }
                .filter { StudyMaterialCatalog.findByProductId(it) != null }
                .toSet()
            val syncedStudyIds = StudyMaterialOwnershipStorage.replaceOwnedIds(prefs, activeStudyIdsFromPlay)
            _ownedStudyMaterialIds.value = syncedStudyIds
            withContext(Dispatchers.Main) {
                onOwnedStudyMaterialsSynced(syncedStudyIds)
            }
        }

        if (syncRevocations && subsResult.responseCode == BillingResponseCode.OK) {
            val hasActiveProSub = subsPurchases.any { purchase ->
                ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY in purchase.products &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                    !purchase.isSuspended
            }
            if (!hasActiveProSub) {
                withContext(Dispatchers.Main) {
                    onSubscriptionNotActiveInPlay()
                }
            }
        }
        return combined.size
    }

    private suspend fun queryPurchasesAwait(@ProductType productType: String): Pair<BillingResult, List<Purchase>> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                    if (cont.isActive) cont.resume(billingResult to purchases)
                }
            }
        }
    }

    private suspend fun handlePurchasesList(
        purchases: List<Purchase>,
        userInitiated: Boolean
    ) {
        val isPt = languageProvider() == Language.PT
        val pendingIds = mutableSetOf<String>()

        for (purchase in purchases) {
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PENDING -> {
                    pendingIds += purchase.products
                    _storeUiState.update {
                        it.copy(
                            statusMessage = if (isPt) {
                                "Pagamento pendente no Google Play (ex.: Pix ou boleto). O crédito será liberado automaticamente assim que confirmar."
                            } else {
                                "Payment pending in Google Play. Entitlement will unlock automatically once payment clears."
                            },
                            isError = false
                        )
                    }
                }

                Purchase.PurchaseState.PURCHASED -> {
                    for (productId in purchase.products) {
                        val lifePkg = LifeShopCatalog.findBrlPackageByProductId(productId)
                        val studyPkg = StudyMaterialCatalog.findByProductId(productId)
                        when {
                            lifePkg != null -> processConsumableLifePurchase(purchase, lifePkg, isPt)
                            studyPkg != null -> processStudyMaterialPurchase(purchase, studyPkg, userInitiated, isPt)
                            productId == ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY ->
                                processProSubscriptionPurchase(purchase, userInitiated, isPt)
                        }
                    }
                }

                else -> Unit
            }
        }

        _storeUiState.update { it.copy(pendingProductIds = pendingIds) }
    }

    private suspend fun processConsumableLifePurchase(
        purchase: Purchase,
        pkg: LifeShopPackage,
        isPt: Boolean
    ) {
        val token = purchase.purchaseToken
        if (!inFlightConsumeTokens.add(token)) return
        try {
            val alreadyProcessed = PlayBillingTokenRegistry.hasProcessedToken(prefs, token)
            val consumeResult = consumePurchaseAwait(token)
            if (consumeResult.responseCode == BillingResponseCode.OK) {
                if (!alreadyProcessed) {
                    PlayBillingTokenRegistry.markTokenProcessed(prefs, token)
                    val qty = purchase.quantity.coerceAtLeast(1)
                    withContext(Dispatchers.Main) {
                        onConsumableLivesGranted(pkg, qty, token, purchase.orderId)
                    }
                }
                _storeUiState.update {
                    it.copy(
                        statusMessage = if (isPt) {
                            "Compra confirmada no Google Play! +${pkg.livesAmount * purchase.quantity.coerceAtLeast(1)} vidas creditadas."
                        } else {
                            "Google Play purchase confirmed! +${pkg.livesAmount * purchase.quantity.coerceAtLeast(1)} lives credited."
                        },
                        isError = false
                    )
                }
            } else {
                _storeUiState.update {
                    it.copy(
                        statusMessage = mapBillingErrorMessage(consumeResult, isPt),
                        isError = true
                    )
                }
            }
        } finally {
            inFlightConsumeTokens.remove(token)
        }
    }

    private suspend fun processStudyMaterialPurchase(
        purchase: Purchase,
        pkg: StudyMaterialPackage,
        userInitiated: Boolean,
        isPt: Boolean
    ) {
        val token = purchase.purchaseToken
        if (!purchase.isAcknowledged) {
            if (!inFlightAckTokens.add(token)) return
            try {
                val ackResult = acknowledgePurchaseAwait(token)
                if (ackResult.responseCode != BillingResponseCode.OK) {
                    _storeUiState.update {
                        it.copy(
                            statusMessage = mapBillingErrorMessage(ackResult, isPt),
                            isError = true
                        )
                    }
                    return
                }
            } finally {
                inFlightAckTokens.remove(token)
            }
        }

        val updatedOwned = StudyMaterialOwnershipStorage.addOwnedId(prefs, pkg.productId)
        _ownedStudyMaterialIds.value = updatedOwned

        withContext(Dispatchers.Main) {
            onStudyMaterialVerified(pkg, purchase.orderId, userInitiated)
        }
        if (userInitiated) {
            _storeUiState.update {
                it.copy(
                    statusMessage = if (isPt) {
                        "Material desbloqueado no Google Play: ${pkg.titlePt}!"
                    } else {
                        "Study material unlocked via Google Play: ${pkg.titleEn}!"
                    },
                    isError = false
                )
            }
        }
    }

    private suspend fun processProSubscriptionPurchase(
        purchase: Purchase,
        userInitiated: Boolean,
        isPt: Boolean
    ) {
        if (purchase.isSuspended) {
            withContext(Dispatchers.Main) {
                onSubscriptionNotActiveInPlay()
            }
            _storeUiState.update {
                it.copy(
                    statusMessage = if (isPt) {
                        "Assinatura suspensa no Google Play. Atualize a forma de pagamento na Play Store."
                    } else {
                        "Subscription suspended in Google Play. Update your payment method in the Play Store."
                    },
                    isError = true
                )
            }
            return
        }

        val token = purchase.purchaseToken
        if (!purchase.isAcknowledged) {
            if (!inFlightAckTokens.add(token)) return
            try {
                val ackResult = acknowledgePurchaseAwait(token)
                if (ackResult.responseCode != BillingResponseCode.OK) {
                    _storeUiState.update {
                        it.copy(
                            statusMessage = mapBillingErrorMessage(ackResult, isPt),
                            isError = true
                        )
                    }
                    return
                }
            } finally {
                inFlightAckTokens.remove(token)
            }
        }

        withContext(Dispatchers.Main) {
            onSubscriptionVerified(
                token,
                purchase.orderId,
                purchase.purchaseTime,
                purchase.isAutoRenewing,
                userInitiated
            )
        }
        if (userInitiated) {
            _storeUiState.update {
                it.copy(
                    statusMessage = if (isPt) {
                        "Assinatura DevRoutine Pro confirmada no Google Play!"
                    } else {
                        "DevRoutine Pro subscription confirmed in Google Play!"
                    },
                    isError = false
                )
            }
        }
    }

    private suspend fun consumePurchaseAwait(purchaseToken: String): BillingResult {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                billingClient.consumeAsync(params) { billingResult, _ ->
                    if (cont.isActive) cont.resume(billingResult)
                }
            }
        }
    }

    private suspend fun acknowledgePurchaseAwait(purchaseToken: String): BillingResult {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                billingClient.acknowledgePurchase(params) { billingResult ->
                    if (cont.isActive) cont.resume(billingResult)
                }
            }
        }
    }

    private fun mapBillingErrorMessage(billingResult: BillingResult, isPt: Boolean): String {
        return when (billingResult.responseCode) {
            BillingResponseCode.BILLING_UNAVAILABLE -> if (isPt) {
                "Google Play Billing indisponível neste dispositivo ou conta Play."
            } else {
                "Google Play Billing is unavailable on this device or Play account."
            }
            BillingResponseCode.ITEM_UNAVAILABLE -> if (isPt) {
                "Item indisponível no Google Play Console. Verifique a publicação do produto."
            } else {
                "Item unavailable in Google Play Console. Verify product activation."
            }
            BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingResponseCode.NETWORK_ERROR -> if (isPt) {
                "Sem conexão com o Google Play. Verifique sua internet e tente novamente."
            } else {
                "Cannot reach Google Play. Check your connection and try again."
            }
            BillingResponseCode.DEVELOPER_ERROR -> if (isPt) {
                "Configuração pendente no Google Play Console para este pacote/assinatura."
            } else {
                "Google Play Console configuration pending for this SKU/subscription."
            }
            else -> if (isPt) {
                "Falha no pagamento Google Play (código ${billingResult.responseCode})."
            } else {
                "Google Play billing failed (code ${billingResult.responseCode})."
            }
        }
    }
}
