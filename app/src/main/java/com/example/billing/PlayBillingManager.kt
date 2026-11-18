package com.example.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.OnPurchasesUpdatedSubResponseCode
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
import com.android.billingclient.api.UnfetchedProduct
import com.example.model.Language
import com.example.model.LifeShopCatalog
import com.example.model.LifeShopPackage
import com.example.model.PlayBillingConnectionStatus
import com.example.model.PlayBillingStoreUiState
import com.example.model.ProSubscriptionPlan
import com.example.model.StudyMaterialCatalog
import com.example.model.StudyMaterialOwnershipStorage
import com.example.model.StudyMaterialPackage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

/**
 * Registro persistente de tokens de compra já creditados para evitar
 * duplicidade caso o Google Play reentregue a mesma compra antes do consumo.
 */
object PlayBillingTokenRegistry {
    private const val KEY_PROCESSED_TOKENS = "processed_play_purchase_tokens"
    private const val KEY_ACKNOWLEDGED_TOKENS = "acknowledged_play_purchase_tokens"
    private const val MAX_STORED_TOKENS = 100

    fun hasProcessedToken(prefs: SharedPreferences, purchaseToken: String): Boolean {
        if (purchaseToken.isBlank()) return false
        val saved = prefs.getStringSet(KEY_PROCESSED_TOKENS, emptySet()) ?: emptySet()
        return purchaseToken in saved
    }

    fun markTokenProcessed(prefs: SharedPreferences, purchaseToken: String) {
        markTokenInSet(prefs, KEY_PROCESSED_TOKENS, purchaseToken)
    }

    fun hasAcknowledgedToken(prefs: SharedPreferences, purchaseToken: String): Boolean {
        if (purchaseToken.isBlank()) return false
        val saved = prefs.getStringSet(KEY_ACKNOWLEDGED_TOKENS, emptySet()) ?: emptySet()
        return purchaseToken in saved
    }

    fun markTokenAcknowledged(prefs: SharedPreferences, purchaseToken: String) {
        markTokenInSet(prefs, KEY_ACKNOWLEDGED_TOKENS, purchaseToken)
    }

    private fun markTokenInSet(prefs: SharedPreferences, key: String, purchaseToken: String) {
        if (purchaseToken.isBlank()) return
        val existing = prefs.getStringSet(key, emptySet())?.toMutableList() ?: mutableListOf()
        if (purchaseToken !in existing) {
            existing.add(purchaseToken)
            val trimmed = if (existing.size > MAX_STORED_TOKENS) {
                existing.takeLast(MAX_STORED_TOKENS)
            } else {
                existing
            }
            prefs.edit().putStringSet(key, trimmed.toSet()).apply()
        }
    }
}

internal object PlayBillingHelpers {
    private const val MAX_OBFUSCATED_ID_LENGTH = 64

    fun sanitizeObfuscatedAccountId(rawId: String?): String? {
        val trimmed = rawId?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        if (trimmed.length <= MAX_OBFUSCATED_ID_LENGTH) return trimmed
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(trimmed.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
            .take(MAX_OBFUSCATED_ID_LENGTH)
    }

    fun formatUnfetchedProductError(
        productId: String,
        statusCode: Int?,
        isPt: Boolean
    ): String {
        return when (statusCode) {
            UnfetchedProduct.StatusCode.PRODUCT_NOT_FOUND -> if (isPt) {
                "Produto \"$productId\" não cadastrado ou inativo no Google Play Console para este app."
            } else {
                "Product \"$productId\" is not registered or active in Google Play Console for this app."
            }

            UnfetchedProduct.StatusCode.NO_ELIGIBLE_OFFER -> if (isPt) {
                "Produto \"$productId\" sem oferta ou plano base elegível para esta conta/região no Play."
            } else {
                "Product \"$productId\" has no eligible offer or base plan for this Play account/region."
            }

            UnfetchedProduct.StatusCode.INVALID_PRODUCT_ID_FORMAT -> if (isPt) {
                "Formato do ID de produto \"$productId\" inválido para o Google Play."
            } else {
                "Invalid Google Play product ID format for \"$productId\"."
            }

            null -> if (isPt) {
                "Produto \"$productId\" não encontrado no Google Play Console. Cadastre e ative o SKU na faixa de teste."
            } else {
                "Product \"$productId\" not found in Google Play Console. Configure and activate this SKU in a test track."
            }

            else -> if (isPt) {
                "Produto \"$productId\" indisponível no Google Play (status $statusCode)."
            } else {
                "Product \"$productId\" unavailable in Google Play (status $statusCode)."
            }
        }
    }

    fun mapBillingErrorMessage(
        responseCode: Int,
        subResponseCode: Int = OnPurchasesUpdatedSubResponseCode.NO_APPLICABLE_SUB_RESPONSE_CODE,
        debugMessage: String = "",
        isPt: Boolean
    ): String {
        when (subResponseCode) {
            OnPurchasesUpdatedSubResponseCode.PAYMENT_DECLINED_DUE_TO_INSUFFICIENT_FUNDS -> return if (isPt) {
                "Pagamento recusado por saldo insuficiente. Atualize sua forma de pagamento no Google Play."
            } else {
                "Payment declined due to insufficient funds. Update your payment method in Google Play."
            }

            OnPurchasesUpdatedSubResponseCode.USER_INELIGIBLE -> return if (isPt) {
                "Esta conta Google Play não é elegível para esta oferta/assinatura."
            } else {
                "This Google Play account is not eligible for this offer or subscription."
            }
        }

        val cleanDebug = debugMessage.trim()
        return when (responseCode) {
            BillingResponseCode.BILLING_UNAVAILABLE -> if (isPt) {
                "Google Play Billing indisponível neste dispositivo (verifique se há uma conta Google Play conectada e a Play Store atualizada)."
            } else {
                "Google Play Billing is unavailable on this device (ensure an active Google Play account and updated Play Store)."
            }

            BillingResponseCode.FEATURE_NOT_SUPPORTED -> if (isPt) {
                "Recurso de compras não suportado pela versão atual da Google Play Store neste dispositivo."
            } else {
                "In-app billing feature is not supported by the Play Store version on this device."
            }

            BillingResponseCode.ITEM_UNAVAILABLE -> if (isPt) {
                "Item indisponível para compra no Google Play Console. Verifique publicação e disponibilidade por país."
            } else {
                "Item unavailable for purchase in Google Play Console. Verify publishing and country availability."
            }

            BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingResponseCode.SERVICE_DISCONNECTED,
            BillingResponseCode.NETWORK_ERROR -> if (isPt) {
                "Sem resposta da Google Play Store. Verifique internet/conta Play e toque para tentar novamente."
            } else {
                "Could not reach Google Play Store. Check internet/Play account and try again."
            }

            BillingResponseCode.DEVELOPER_ERROR -> if (isPt) {
                "Configuração pendente no Google Play Console para este pacote ou assinatura${if (cleanDebug.isNotBlank()) " ($cleanDebug)" else "."}"
            } else {
                "Google Play Console configuration pending for this SKU or subscription${if (cleanDebug.isNotBlank()) " ($cleanDebug)" else "."}"
            }

            BillingResponseCode.USER_CANCELED -> if (isPt) {
                "Compra cancelada no Google Play."
            } else {
                "Purchase cancelled in Google Play."
            }

            BillingResponseCode.ITEM_ALREADY_OWNED -> if (isPt) {
                "Item já adquirido no Google Play. Sincronizando entrega..."
            } else {
                "Item already owned on Google Play. Syncing entitlement..."
            }

            BillingResponseCode.ITEM_NOT_OWNED -> if (isPt) {
                "Compra ainda não confirmada para esta conta Google Play."
            } else {
                "Purchase not owned by this Google Play account."
            }

            else -> if (isPt) {
                "Falha no pagamento Google Play (código $responseCode${if (cleanDebug.isNotBlank()) ": $cleanDebug" else ""})."
            } else {
                "Google Play billing failed (code $responseCode${if (cleanDebug.isNotBlank()) ": $cleanDebug" else ""})."
            }
        }
    }
}

private data class ProductDetailsQueryOutcome(
    val details: List<ProductDetails> = emptyList(),
    val billingResult: BillingResult,
    val unfetchedStatuses: Map<String, Int> = emptyMap()
)

internal data class QueryProductDetailsOutcomeRaw(
    val billingResult: BillingResult,
    val productDetailsList: List<ProductDetails> = emptyList(),
    val unfetchedStatuses: Map<String, Int> = emptyMap()
)

internal interface BillingGateway {
    val connectionState: Int
    fun startConnection(listener: BillingClientStateListener)
    fun endConnection()
    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult
    fun queryProductDetailsAsync(
        params: QueryProductDetailsParams,
        onResult: (QueryProductDetailsOutcomeRaw) -> Unit
    )
    fun queryPurchasesAsync(
        params: QueryPurchasesParams,
        onResult: (BillingResult, List<Purchase>) -> Unit
    )
    fun consumeAsync(
        params: ConsumeParams,
        onResult: (BillingResult, String) -> Unit
    )
    fun acknowledgePurchase(
        params: AcknowledgePurchaseParams,
        onResult: (BillingResult) -> Unit
    )
}

private class RealBillingGateway(
    private val appContext: Context,
    private val purchasesUpdatedListener: PurchasesUpdatedListener
) : BillingGateway {
    @Volatile
    private var client: BillingClient = buildClient()

    private fun buildClient(): BillingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .enablePrepaidPlans()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    @Synchronized
    private fun ensureOpenClient(): BillingClient {
        if (client.connectionState == BillingClient.ConnectionState.CLOSED) {
            client = buildClient()
        }
        return client
    }

    override val connectionState: Int
        get() = client.connectionState

    override fun startConnection(listener: BillingClientStateListener) {
        ensureOpenClient().startConnection(listener)
    }

    override fun endConnection() {
        val current = client
        if (current.connectionState != BillingClient.ConnectionState.CLOSED) {
            current.endConnection()
        }
    }

    override fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult =
        ensureOpenClient().launchBillingFlow(activity, params)

    override fun queryProductDetailsAsync(
        params: QueryProductDetailsParams,
        onResult: (QueryProductDetailsOutcomeRaw) -> Unit
    ) {
        ensureOpenClient().queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            val unfetched = queryProductDetailsResult.unfetchedProductList
                .associate { it.productId to it.statusCode }
            val list = if (billingResult.responseCode == BillingResponseCode.OK) {
                queryProductDetailsResult.productDetailsList
            } else {
                emptyList()
            }
            onResult(
                QueryProductDetailsOutcomeRaw(
                    billingResult = billingResult,
                    productDetailsList = list,
                    unfetchedStatuses = unfetched
                )
            )
        }
    }

    override fun queryPurchasesAsync(
        params: QueryPurchasesParams,
        onResult: (BillingResult, List<Purchase>) -> Unit
    ) {
        ensureOpenClient().queryPurchasesAsync(params) { billingResult, purchases ->
            onResult(billingResult, purchases)
        }
    }

    override fun consumeAsync(
        params: ConsumeParams,
        onResult: (BillingResult, String) -> Unit
    ) {
        ensureOpenClient().consumeAsync(params) { billingResult, token ->
            onResult(billingResult, token)
        }
    }

    override fun acknowledgePurchase(
        params: AcknowledgePurchaseParams,
        onResult: (BillingResult) -> Unit
    ) {
        ensureOpenClient().acknowledgePurchase(params) { billingResult ->
            onResult(billingResult)
        }
    }
}

/**
 * Gerenciador real do Google Play Billing Library para todos os itens pagos da loja:
 * - Pacotes consumíveis de vidas: lives_brl_10, lives_brl_20, lives_brl_50 (ProductType.INAPP)
 * - Materiais de estudo: study_pack_* (ProductType.INAPP)
 * - Assinatura mensal: devroutine_pro_monthly (ProductType.SUBS)
 *
 * Nunca credita vidas, libera materiais nem ativa assinatura sem confirmação real `PurchaseState.PURCHASED`
 * e validação/consumo/reconhecimento via Google Play.
 */
class PlayBillingManager internal constructor(
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
    private val onOwnedStudyMaterialsSynced: (Set<String>) -> Unit = {},
    gatewayFactory: ((PurchasesUpdatedListener) -> BillingGateway)? = null
) {
    constructor(
        context: Context,
        prefs: SharedPreferences,
        scope: CoroutineScope,
        languageProvider: () -> Language,
        obfuscatedAccountIdProvider: () -> String?,
        onConsumableLivesGranted: (
            pkg: LifeShopPackage,
            quantity: Int,
            purchaseToken: String,
            orderId: String?
        ) -> Unit,
        onSubscriptionVerified: (
            purchaseToken: String,
            orderId: String?,
            purchaseTimeMillis: Long,
            isAutoRenewing: Boolean,
            userInitiated: Boolean
        ) -> Unit,
        onSubscriptionNotActiveInPlay: () -> Unit,
        onStudyMaterialVerified: (
            pkg: StudyMaterialPackage,
            orderId: String?,
            userInitiated: Boolean
        ) -> Unit = { _, _, _ -> },
        onOwnedStudyMaterialsSynced: (Set<String>) -> Unit = {}
    ) : this(
        context = context,
        prefs = prefs,
        scope = scope,
        languageProvider = languageProvider,
        obfuscatedAccountIdProvider = obfuscatedAccountIdProvider,
        onConsumableLivesGranted = onConsumableLivesGranted,
        onSubscriptionVerified = onSubscriptionVerified,
        onSubscriptionNotActiveInPlay = onSubscriptionNotActiveInPlay,
        onStudyMaterialVerified = onStudyMaterialVerified,
        onOwnedStudyMaterialsSynced = onOwnedStudyMaterialsSynced,
        gatewayFactory = null
    )

    companion object {
        private const val TAG = "PlayBillingManager"
        private const val CONNECTION_TIMEOUT_MS = 10_000L
        private const val QUERY_TIMEOUT_MS = 15_000L
        private const val MAX_CONNECT_ATTEMPTS = 3
        private const val INITIAL_CONNECT_RETRY_DELAY_MS = 400L
        private const val MAX_PURCHASE_OP_ATTEMPTS = 2
    }

    private val appContext = context.applicationContext
    private val productDetailsMap = ConcurrentHashMap<String, ProductDetails>()
    private val connectionMutex = Mutex()
    private val purchaseProcessingMutex = Mutex()
    private val isPreparingBillingFlow = AtomicBoolean(false)
    private val hasConnectedOnce = AtomicBoolean(false)

    @Volatile
    private var pendingConnectDeferred: CompletableDeferred<BillingResult>? = null

    @Volatile
    private var lastInAppQueryOutcome: ProductDetailsQueryOutcome? = null

    @Volatile
    private var lastSubsQueryOutcome: ProductDetailsQueryOutcome? = null

    private val _storeUiState = MutableStateFlow(PlayBillingStoreUiState())
    val storeUiState: StateFlow<PlayBillingStoreUiState> = _storeUiState.asStateFlow()

    private val _studyMaterialProductDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val studyMaterialProductDetails: StateFlow<Map<String, ProductDetails>> =
        _studyMaterialProductDetails.asStateFlow()

    private val _ownedStudyMaterialIds = MutableStateFlow(
        StudyMaterialOwnershipStorage.loadOwnedIds(prefs)
    )
    val ownedStudyMaterialIds: StateFlow<Set<String>> = _ownedStudyMaterialIds.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        _storeUiState.update {
            it.copy(
                isPurchaseInProgress = false,
                activePurchaseProductId = null,
                lastBillingResponseCode = billingResult.responseCode
            )
        }
        val isPt = languageProvider() == Language.PT
        val subResponseCode = billingResult.onPurchasesUpdatedSubResponseCode
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                if (!purchases.isNullOrEmpty()) {
                    scope.launch {
                        handlePurchasesList(
                            purchases = purchases,
                            userInitiated = true,
                            isFullSnapshot = false
                        )
                    }
                } else {
                    scope.launch {
                        queryAndProcessAllPurchases(syncRevocations = false, userInitiated = true)
                    }
                }
            }

            BillingResponseCode.USER_CANCELED -> {
                val hasDeclineSubCode =
                    subResponseCode == OnPurchasesUpdatedSubResponseCode.PAYMENT_DECLINED_DUE_TO_INSUFFICIENT_FUNDS ||
                        subResponseCode == OnPurchasesUpdatedSubResponseCode.USER_INELIGIBLE
                _storeUiState.update {
                    it.copy(
                        statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                            responseCode = billingResult.responseCode,
                            subResponseCode = subResponseCode,
                            debugMessage = billingResult.debugMessage,
                            isPt = isPt
                        ),
                        isError = hasDeclineSubCode
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
                        statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                            responseCode = billingResult.responseCode,
                            subResponseCode = subResponseCode,
                            debugMessage = billingResult.debugMessage,
                            isPt = isPt
                        ),
                        isError = true
                    )
                }
            }
        }
    }

    private val gateway: BillingGateway =
        gatewayFactory?.invoke(purchasesUpdatedListener)
            ?: RealBillingGateway(appContext, purchasesUpdatedListener)

    private fun isGatewayConnected(): Boolean =
        gateway.connectionState == BillingClient.ConnectionState.CONNECTED

    private fun isTransientBillingError(responseCode: Int): Boolean =
        responseCode == BillingResponseCode.SERVICE_DISCONNECTED ||
            responseCode == BillingResponseCode.SERVICE_UNAVAILABLE ||
            responseCode == BillingResponseCode.SERVICE_TIMEOUT ||
            responseCode == BillingResponseCode.NETWORK_ERROR ||
            responseCode == BillingResponseCode.ERROR

    fun startConnectionAndSync(userInitiated: Boolean = false) {
        scope.launch {
            val isPt = languageProvider() == Language.PT
            if (userInitiated) {
                _storeUiState.update {
                    it.copy(
                        connectionStatus = if (isGatewayConnected()) {
                            PlayBillingConnectionStatus.READY
                        } else {
                            PlayBillingConnectionStatus.CONNECTING
                        },
                        statusMessage = if (isPt) "Conectando ao Google Play..." else "Connecting to Google Play...",
                        isError = false
                    )
                }
            }

            val connectResult = ensureConnectedAwait()
            if (connectResult.responseCode == BillingResponseCode.OK) {
                refreshProductsAndPurchases(userInitiated = userInitiated)
            } else if (userInitiated) {
                _storeUiState.update {
                    it.copy(
                        statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                            responseCode = connectResult.responseCode,
                            subResponseCode = connectResult.onPurchasesUpdatedSubResponseCode,
                            debugMessage = connectResult.debugMessage,
                            isPt = isPt
                        ),
                        isError = true,
                        lastBillingResponseCode = connectResult.responseCode
                    )
                }
            }
        }
    }

    fun refreshOnResume() {
        scope.launch {
            val connectResult = ensureConnectedAwait()
            if (connectResult.responseCode != BillingResponseCode.OK) {
                clearOrphanedPurchaseInProgressAfterResume()
                return@launch
            }
            if (productDetailsMap.isEmpty()) {
                queryAllCatalogProducts()
            }
            queryAndProcessAllPurchases(syncRevocations = true, userInitiated = false)
            clearOrphanedPurchaseInProgressAfterResume()
        }
    }

    private suspend fun clearOrphanedPurchaseInProgressAfterResume() {
        if (!isPreparingBillingFlow.get() && _storeUiState.value.isPurchaseInProgress) {
            delay(500L)
            if (!isPreparingBillingFlow.get() && _storeUiState.value.isPurchaseInProgress) {
                _storeUiState.update {
                    it.copy(
                        isPurchaseInProgress = false,
                        activePurchaseProductId = null
                    )
                }
            }
        }
    }

    fun refreshCatalogAndPurchases(userInitiated: Boolean = true) {
        scope.launch {
            val isPt = languageProvider() == Language.PT
            if (userInitiated) {
                _storeUiState.update {
                    it.copy(
                        connectionStatus = if (isGatewayConnected()) {
                            PlayBillingConnectionStatus.READY
                        } else {
                            PlayBillingConnectionStatus.CONNECTING
                        },
                        statusMessage = if (isPt) "Atualizando catálogo do Google Play..." else "Refreshing Google Play catalog...",
                        isError = false
                    )
                }
            }
            val connectResult = ensureConnectedAwait()
            if (connectResult.responseCode != BillingResponseCode.OK) {
                if (userInitiated) {
                    _storeUiState.update {
                        it.copy(
                            statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                                responseCode = connectResult.responseCode,
                                subResponseCode = connectResult.onPurchasesUpdatedSubResponseCode,
                                debugMessage = connectResult.debugMessage,
                                isPt = isPt
                            ),
                            isError = true,
                            lastBillingResponseCode = connectResult.responseCode
                        )
                    }
                }
                return@launch
            }
            refreshProductsAndPurchases(userInitiated = userInitiated)
        }
    }

    fun clearStatusMessage() {
        _storeUiState.update { it.copy(statusMessage = null, isError = false) }
    }

    fun restorePurchases() {
        val isPt = languageProvider() == Language.PT
        scope.launch {
            _storeUiState.update {
                it.copy(
                    statusMessage = if (isPt) "Verificando compras no Google Play..." else "Checking Google Play purchases...",
                    isError = false
                )
            }
            val connectResult = ensureConnectedAwait()
            if (connectResult.responseCode != BillingResponseCode.OK) {
                _storeUiState.update {
                    it.copy(
                        statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                            responseCode = connectResult.responseCode,
                            subResponseCode = connectResult.onPurchasesUpdatedSubResponseCode,
                            debugMessage = connectResult.debugMessage,
                            isPt = isPt
                        ),
                        isError = true,
                        lastBillingResponseCode = connectResult.responseCode
                    )
                }
                return@launch
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
        if (!isPreparingBillingFlow.compareAndSet(false, true)) return
        val isPt = languageProvider() == Language.PT
        _storeUiState.update {
            it.copy(
                isPurchaseInProgress = true,
                activePurchaseProductId = productId,
                statusMessage = null,
                isError = false
            )
        }

        scope.launch {
            var launchedBillingSheet = false
            try {
                if (activity.isFinishing || activity.isDestroyed) {
                    _storeUiState.update {
                        it.copy(
                            statusMessage = if (isPt) {
                                "Não foi possível abrir o checkout agora. Tente novamente."
                            } else {
                                "Could not open checkout right now. Please try again."
                            },
                            isError = true
                        )
                    }
                    return@launch
                }

                val connectResult = ensureConnectedAwait()
                if (connectResult.responseCode != BillingResponseCode.OK) {
                    _storeUiState.update {
                        it.copy(
                            statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                                responseCode = connectResult.responseCode,
                                subResponseCode = connectResult.onPurchasesUpdatedSubResponseCode,
                                debugMessage = connectResult.debugMessage,
                                isPt = isPt
                            ),
                            isError = true,
                            lastBillingResponseCode = connectResult.responseCode
                        )
                    }
                    return@launch
                }

                if (productDetailsMap[productId] == null) {
                    queryAllCatalogProducts()
                }

                val details = productDetailsMap[productId]
                if (details == null) {
                    val isSubSku = productId == ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY
                    val relevantOutcome = if (isSubSku) lastSubsQueryOutcome else lastInAppQueryOutcome
                    val queryResult = relevantOutcome?.billingResult
                    val errorMsg = when {
                        queryResult != null && queryResult.responseCode != BillingResponseCode.OK -> {
                            PlayBillingHelpers.mapBillingErrorMessage(
                                responseCode = queryResult.responseCode,
                                subResponseCode = queryResult.onPurchasesUpdatedSubResponseCode,
                                debugMessage = queryResult.debugMessage,
                                isPt = isPt
                            )
                        }

                        else -> {
                            val unfetchedStatus = relevantOutcome?.unfetchedStatuses?.get(productId)
                            PlayBillingHelpers.formatUnfetchedProductError(
                                productId = productId,
                                statusCode = unfetchedStatus,
                                isPt = isPt
                            )
                        }
                    }
                    _storeUiState.update {
                        it.copy(
                            statusMessage = errorMsg,
                            isError = true,
                            lastBillingResponseCode = queryResult?.responseCode
                        )
                    }
                    return@launch
                }

                val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)

                if (details.productType == ProductType.SUBS) {
                    val subOfferToken = details.subscriptionOfferDetails
                        ?.firstOrNull { !it.offerToken.isNullOrBlank() }
                        ?.offerToken
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
                        ?.firstOrNull { !it.offerToken.isNullOrBlank() }
                        ?.offerToken
                        ?: details.oneTimePurchaseOfferDetails?.offerToken?.takeIf { it.isNotBlank() }
                    if (!inAppOfferToken.isNullOrBlank()) {
                        productParamsBuilder.setOfferToken(inAppOfferToken)
                    }
                }

                val flowParamsBuilder = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(productParamsBuilder.build()))

                PlayBillingHelpers.sanitizeObfuscatedAccountId(obfuscatedAccountIdProvider())
                    ?.let { safeAccountId ->
                        flowParamsBuilder.setObfuscatedAccountId(safeAccountId)
                    }

                val launchResult = withContext(Dispatchers.Main) {
                    gateway.launchBillingFlow(activity, flowParamsBuilder.build())
                }
                when (launchResult.responseCode) {
                    BillingResponseCode.OK -> {
                        launchedBillingSheet = true
                    }

                    BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        _storeUiState.update {
                            it.copy(
                                statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                                    responseCode = launchResult.responseCode,
                                    subResponseCode = launchResult.onPurchasesUpdatedSubResponseCode,
                                    debugMessage = launchResult.debugMessage,
                                    isPt = isPt
                                ),
                                isError = false,
                                lastBillingResponseCode = launchResult.responseCode
                            )
                        }
                        scope.launch {
                            queryAndProcessAllPurchases(syncRevocations = true, userInitiated = true)
                        }
                    }

                    else -> {
                        _storeUiState.update {
                            it.copy(
                                statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                                    responseCode = launchResult.responseCode,
                                    subResponseCode = launchResult.onPurchasesUpdatedSubResponseCode,
                                    debugMessage = launchResult.debugMessage,
                                    isPt = isPt
                                ),
                                isError = true,
                                lastBillingResponseCode = launchResult.responseCode
                            )
                        }
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                Log.e(TAG, "Error launching Play Billing flow for $productId", t)
                _storeUiState.update {
                    it.copy(
                        statusMessage = if (isPt) {
                            "Não foi possível iniciar o checkout do Google Play (${t.localizedMessage ?: "erro interno"})."
                        } else {
                            "Unable to start Google Play checkout (${t.localizedMessage ?: "internal error"})."
                        },
                        isError = true
                    )
                }
            } finally {
                isPreparingBillingFlow.set(false)
                if (!launchedBillingSheet) {
                    _storeUiState.update {
                        it.copy(
                            isPurchaseInProgress = false,
                            activePurchaseProductId = null
                        )
                    }
                }
            }
        }
    }

    fun endConnection() {
        runCatching {
            pendingConnectDeferred?.cancel()
            pendingConnectDeferred = null
            gateway.endConnection()
        }
    }

    private suspend fun ensureConnectedAwait(): BillingResult {
        var lastResult: BillingResult? = null
        for (attempt in 0 until MAX_CONNECT_ATTEMPTS) {
            val result = connectSingleAttemptAwait()
            if (result.responseCode == BillingResponseCode.OK) {
                return result
            }
            lastResult = result
            if (!isTransientBillingError(result.responseCode) || attempt == MAX_CONNECT_ATTEMPTS - 1) {
                return result
            }
            delay(INITIAL_CONNECT_RETRY_DELAY_MS * (attempt + 1))
        }
        return lastResult ?: BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
            .setDebugMessage("Play Billing connection unavailable")
            .build()
    }

    private suspend fun connectSingleAttemptAwait(): BillingResult {
        if (isGatewayConnected()) {
            _storeUiState.update {
                it.copy(connectionStatus = PlayBillingConnectionStatus.READY)
            }
            return BillingResult.newBuilder()
                .setResponseCode(BillingResponseCode.OK)
                .build()
        }

        val deferred = connectionMutex.withLock {
            if (isGatewayConnected()) {
                _storeUiState.update {
                    it.copy(connectionStatus = PlayBillingConnectionStatus.READY)
                }
                return BillingResult.newBuilder()
                    .setResponseCode(BillingResponseCode.OK)
                    .build()
            }
            val existing = pendingConnectDeferred
            if (existing != null && !existing.isCompleted) {
                existing
            } else {
                val created = CompletableDeferred<BillingResult>()
                pendingConnectDeferred = created
                _storeUiState.update {
                    it.copy(connectionStatus = PlayBillingConnectionStatus.CONNECTING)
                }
                scope.launch(Dispatchers.Main.immediate) {
                    try {
                        gateway.startConnection(object : BillingClientStateListener {
                            override fun onBillingSetupFinished(billingResult: BillingResult) {
                                val ok = billingResult.responseCode == BillingResponseCode.OK
                                val wasDisconnected = hasConnectedOnce.get() &&
                                    _storeUiState.value.connectionStatus != PlayBillingConnectionStatus.READY
                                if (ok) {
                                    hasConnectedOnce.set(true)
                                }
                                _storeUiState.update {
                                    it.copy(
                                        connectionStatus = when {
                                            ok -> PlayBillingConnectionStatus.READY
                                            billingResult.responseCode == BillingResponseCode.BILLING_UNAVAILABLE ||
                                                billingResult.responseCode == BillingResponseCode.FEATURE_NOT_SUPPORTED ->
                                                PlayBillingConnectionStatus.UNAVAILABLE
                                            else -> PlayBillingConnectionStatus.DISCONNECTED
                                        },
                                        lastBillingResponseCode = billingResult.responseCode
                                    )
                                }
                                if (pendingConnectDeferred === created) {
                                    pendingConnectDeferred = null
                                }
                                created.complete(billingResult)
                                if (ok && wasDisconnected) {
                                    scope.launch {
                                        if (productDetailsMap.isEmpty()) {
                                            queryAllCatalogProducts()
                                        }
                                        queryAndProcessAllPurchases(
                                            syncRevocations = true,
                                            userInitiated = false
                                        )
                                    }
                                }
                            }

                            override fun onBillingServiceDisconnected() {
                                _storeUiState.update {
                                    it.copy(connectionStatus = PlayBillingConnectionStatus.DISCONNECTED)
                                }
                                if (pendingConnectDeferred === created) {
                                    pendingConnectDeferred = null
                                }
                                if (!created.isCompleted) {
                                    created.complete(
                                        BillingResult.newBuilder()
                                            .setResponseCode(BillingResponseCode.SERVICE_DISCONNECTED)
                                            .setDebugMessage("Play Billing service disconnected")
                                            .build()
                                    )
                                }
                            }
                        })
                    } catch (t: Throwable) {
                        val failedResult = BillingResult.newBuilder()
                            .setResponseCode(BillingResponseCode.ERROR)
                            .setDebugMessage(t.localizedMessage ?: "startConnection error")
                            .build()
                        _storeUiState.update {
                            it.copy(
                                connectionStatus = PlayBillingConnectionStatus.UNAVAILABLE,
                                lastBillingResponseCode = BillingResponseCode.ERROR
                            )
                        }
                        if (pendingConnectDeferred === created) {
                            pendingConnectDeferred = null
                        }
                        created.complete(failedResult)
                    }
                }
                created
            }
        }

        val awaited = withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
            deferred.await()
        }
        if (awaited != null) {
            return awaited
        }

        connectionMutex.withLock {
            if (pendingConnectDeferred === deferred) {
                pendingConnectDeferred = null
            }
        }
        deferred.cancel()
        return BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
            .setDebugMessage("Connection timed out")
            .build().also {
                _storeUiState.update {
                    it.copy(
                        connectionStatus = PlayBillingConnectionStatus.DISCONNECTED,
                        lastBillingResponseCode = BillingResponseCode.SERVICE_UNAVAILABLE
                    )
                }
            }
    }

    private suspend fun refreshProductsAndPurchases(userInitiated: Boolean) {
        val isPt = languageProvider() == Language.PT
        queryAllCatalogProducts()
        val foundCount = queryAndProcessAllPurchases(syncRevocations = true, userInitiated = userInitiated)
        if (userInitiated) {
            val inAppRes = lastInAppQueryOutcome?.billingResult
            val subsRes = lastSubsQueryOutcome?.billingResult
            val failureResult = listOfNotNull(inAppRes, subsRes)
                .firstOrNull { it.responseCode != BillingResponseCode.OK }

            if (failureResult != null) {
                _storeUiState.update {
                    it.copy(
                        statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                            responseCode = failureResult.responseCode,
                            subResponseCode = failureResult.onPurchasesUpdatedSubResponseCode,
                            debugMessage = failureResult.debugMessage,
                            isPt = isPt
                        ),
                        isError = true,
                        lastBillingResponseCode = failureResult.responseCode
                    )
                }
            } else if (_storeUiState.value.availableProductIds.isEmpty() && foundCount == 0) {
                _storeUiState.update {
                    it.copy(
                        statusMessage = if (isPt) {
                            "Google Play conectado, mas nenhum SKU foi encontrado no Play Console para este aplicativo."
                        } else {
                            "Google Play connected, but no SKUs were returned by Play Console for this app."
                        },
                        isError = false
                    )
                }
            } else if (_storeUiState.value.statusMessage?.contains("Atualizando") == true ||
                _storeUiState.value.statusMessage?.contains("Refreshing") == true ||
                _storeUiState.value.statusMessage?.contains("Conectando") == true ||
                _storeUiState.value.statusMessage?.contains("Connecting") == true
            ) {
                _storeUiState.update {
                    it.copy(statusMessage = null, isError = false)
                }
            }
        }
    }

    private suspend fun queryAllCatalogProducts() {
        val lifeInAppIds = LifeShopCatalog.brlPackages.mapNotNull { it.playProductId }
        val studyMaterialIds = StudyMaterialCatalog.allProductIds
        val inAppIds = (lifeInAppIds + studyMaterialIds).distinct()

        val inAppOutcome = queryProductDetailsOutcome(inAppIds, ProductType.INAPP)
        val subsOutcome = queryProductDetailsOutcome(
            listOf(ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY),
            ProductType.SUBS
        )
        lastInAppQueryOutcome = inAppOutcome
        lastSubsQueryOutcome = subsOutcome

        val updatedPrices = mutableMapOf<String, String>()
        val availableIds = mutableSetOf<String>()
        val studyDetailsMap = mutableMapOf<String, ProductDetails>()

        inAppOutcome.details.forEach { details ->
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

        subsOutcome.details.forEach { details ->
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
                connectionStatus = if (isGatewayConnected()) PlayBillingConnectionStatus.READY else it.connectionStatus,
                productPrices = updatedPrices,
                availableProductIds = availableIds
            )
        }
    }

    private suspend fun queryProductDetailsOutcome(
        productIds: List<String>,
        @ProductType productType: String
    ): ProductDetailsQueryOutcome {
        if (productIds.isEmpty()) {
            return ProductDetailsQueryOutcome(
                details = emptyList(),
                billingResult = BillingResult.newBuilder()
                    .setResponseCode(BillingResponseCode.OK)
                    .build()
            )
        }
        if (!isGatewayConnected()) {
            val reconnect = ensureConnectedAwait()
            if (reconnect.responseCode != BillingResponseCode.OK) {
                return ProductDetailsQueryOutcome(
                    details = emptyList(),
                    billingResult = reconnect
                )
            }
        }
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
            val outcome = withTimeoutOrNull(QUERY_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    gateway.queryProductDetailsAsync(params) { raw ->
                        if (cont.isActive) {
                            cont.resume(
                                ProductDetailsQueryOutcome(
                                    details = raw.productDetailsList,
                                    billingResult = raw.billingResult,
                                    unfetchedStatuses = raw.unfetchedStatuses
                                )
                            )
                        }
                    }
                }
            }
            outcome ?: ProductDetailsQueryOutcome(
                details = emptyList(),
                billingResult = BillingResult.newBuilder()
                    .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
                    .setDebugMessage("Product query timed out")
                    .build()
            )
        }
    }

    private suspend fun queryAndProcessAllPurchases(
        syncRevocations: Boolean,
        userInitiated: Boolean
    ): Int {
        if (!isGatewayConnected()) {
            val reconnect = ensureConnectedAwait()
            if (reconnect.responseCode != BillingResponseCode.OK) {
                return 0
            }
        }
        val (inAppResult, inAppPurchases) = queryPurchasesAwait(ProductType.INAPP)
        val (subsResult, subsPurchases) = queryPurchasesAwait(ProductType.SUBS)

        val combined = buildList {
            if (inAppResult.responseCode == BillingResponseCode.OK) addAll(inAppPurchases)
            if (subsResult.responseCode == BillingResponseCode.OK) addAll(subsPurchases)
        }

        if (combined.isNotEmpty()) {
            handlePurchasesList(
                purchases = combined,
                userInitiated = userInitiated,
                isFullSnapshot = true
            )
        } else if (inAppResult.responseCode == BillingResponseCode.OK && subsResult.responseCode == BillingResponseCode.OK) {
            _storeUiState.update { it.copy(pendingProductIds = emptySet()) }
        }

        // Re-query after handlePurchasesList if needed? No: handlePurchasesList already updated
        // PlayBillingTokenRegistry.markTokenAcknowledged for any newly acknowledged purchases.
        if (syncRevocations && inAppResult.responseCode == BillingResponseCode.OK) {
            val activeStudyIdsFromPlay = inAppPurchases
                .filter { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        (purchase.isAcknowledged || PlayBillingTokenRegistry.hasAcknowledgedToken(prefs, purchase.purchaseToken))
                }
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
                    !purchase.isSuspended &&
                    (purchase.isAcknowledged || PlayBillingTokenRegistry.hasAcknowledgedToken(prefs, purchase.purchaseToken))
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
        if (!isGatewayConnected()) {
            val reconnect = ensureConnectedAwait()
            if (reconnect.responseCode != BillingResponseCode.OK) {
                return reconnect to emptyList()
            }
        }
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()
        return withContext(Dispatchers.IO) {
            val res = withTimeoutOrNull(QUERY_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    gateway.queryPurchasesAsync(params) { billingResult, purchases ->
                        if (cont.isActive) cont.resume(billingResult to purchases)
                    }
                }
            }
            res ?: (
                BillingResult.newBuilder()
                    .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
                    .setDebugMessage("Query purchases timed out")
                    .build() to emptyList()
                )
        }
    }

    private suspend fun handlePurchasesList(
        purchases: List<Purchase>,
        userInitiated: Boolean,
        isFullSnapshot: Boolean
    ) {
        purchaseProcessingMutex.withLock {
            val isPt = languageProvider() == Language.PT
            val pendingIds = mutableSetOf<String>()
            val completedIds = mutableSetOf<String>()

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
                        completedIds += purchase.products
                        for (productId in purchase.products) {
                            val lifePkg = LifeShopCatalog.findBrlPackageByProductId(productId)
                            val studyPkg = StudyMaterialCatalog.findByProductId(productId)
                            when {
                                lifePkg != null -> processConsumableLifePurchase(
                                    purchase = purchase,
                                    pkg = lifePkg,
                                    userInitiated = userInitiated,
                                    isPt = isPt
                                )
                                studyPkg != null -> processStudyMaterialPurchase(
                                    purchase = purchase,
                                    pkg = studyPkg,
                                    userInitiated = userInitiated,
                                    isPt = isPt
                                )
                                productId == ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY ->
                                    processProSubscriptionPurchase(
                                        purchase = purchase,
                                        userInitiated = userInitiated,
                                        isPt = isPt
                                    )
                            }
                        }
                    }

                    else -> Unit
                }
            }

            _storeUiState.update { current ->
                val updatedPending = if (isFullSnapshot) {
                    pendingIds
                } else {
                    (current.pendingProductIds - completedIds) + pendingIds
                }
                current.copy(pendingProductIds = updatedPending)
            }
        }
    }

    private suspend fun processConsumableLifePurchase(
        purchase: Purchase,
        pkg: LifeShopPackage,
        userInitiated: Boolean,
        isPt: Boolean
    ) {
        val token = purchase.purchaseToken
        if (token.isBlank()) return
        val qty = purchase.quantity.coerceAtLeast(1)

        if (PlayBillingTokenRegistry.hasProcessedToken(prefs, token)) {
            if (userInitiated) {
                _storeUiState.update {
                    it.copy(
                        statusMessage = if (isPt) {
                            "Compra confirmada no Google Play! +${pkg.livesAmount * qty} vidas creditadas."
                        } else {
                            "Google Play purchase confirmed! +${pkg.livesAmount * qty} lives credited."
                        },
                        isError = false
                    )
                }
            }
            return
        }

        val consumeResult = consumePurchaseWithRetry(token)
        if (consumeResult.responseCode == BillingResponseCode.OK) {
            if (!PlayBillingTokenRegistry.hasProcessedToken(prefs, token)) {
                PlayBillingTokenRegistry.markTokenProcessed(prefs, token)
                withContext(Dispatchers.Main) {
                    onConsumableLivesGranted(pkg, qty, token, purchase.orderId)
                }
            }
            _storeUiState.update {
                it.copy(
                    statusMessage = if (isPt) {
                        "Compra confirmada no Google Play! +${pkg.livesAmount * qty} vidas creditadas."
                    } else {
                        "Google Play purchase confirmed! +${pkg.livesAmount * qty} lives credited."
                    },
                    isError = false
                )
            }
        } else {
            _storeUiState.update {
                it.copy(
                    statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                        responseCode = consumeResult.responseCode,
                        subResponseCode = consumeResult.onPurchasesUpdatedSubResponseCode,
                        debugMessage = consumeResult.debugMessage,
                        isPt = isPt
                    ),
                    isError = true,
                    lastBillingResponseCode = consumeResult.responseCode
                )
            }
        }
    }

    private suspend fun processStudyMaterialPurchase(
        purchase: Purchase,
        pkg: StudyMaterialPackage,
        userInitiated: Boolean,
        isPt: Boolean
    ) {
        val token = purchase.purchaseToken
        if (token.isBlank()) return

        val alreadyAcknowledged =
            purchase.isAcknowledged || PlayBillingTokenRegistry.hasAcknowledgedToken(prefs, token)
        if (!alreadyAcknowledged) {
            val ackResult = acknowledgePurchaseWithRetry(token, ProductType.INAPP)
            if (ackResult.responseCode != BillingResponseCode.OK) {
                _storeUiState.update {
                    it.copy(
                        statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                            responseCode = ackResult.responseCode,
                            subResponseCode = ackResult.onPurchasesUpdatedSubResponseCode,
                            debugMessage = ackResult.debugMessage,
                            isPt = isPt
                        ),
                        isError = true,
                        lastBillingResponseCode = ackResult.responseCode
                    )
                }
                return
            }
        }
        PlayBillingTokenRegistry.markTokenAcknowledged(prefs, token)

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
        if (token.isBlank()) return

        val alreadyAcknowledged =
            purchase.isAcknowledged || PlayBillingTokenRegistry.hasAcknowledgedToken(prefs, token)
        if (!alreadyAcknowledged) {
            val ackResult = acknowledgePurchaseWithRetry(token, ProductType.SUBS)
            if (ackResult.responseCode != BillingResponseCode.OK) {
                _storeUiState.update {
                    it.copy(
                        statusMessage = PlayBillingHelpers.mapBillingErrorMessage(
                            responseCode = ackResult.responseCode,
                            subResponseCode = ackResult.onPurchasesUpdatedSubResponseCode,
                            debugMessage = ackResult.debugMessage,
                            isPt = isPt
                        ),
                        isError = true,
                        lastBillingResponseCode = ackResult.responseCode
                    )
                }
                return
            }
        }
        PlayBillingTokenRegistry.markTokenAcknowledged(prefs, token)

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

    private suspend fun consumePurchaseWithRetry(purchaseToken: String): BillingResult {
        var lastResult: BillingResult? = null
        for (attempt in 0 until MAX_PURCHASE_OP_ATTEMPTS) {
            val connectResult = ensureConnectedAwait()
            if (connectResult.responseCode != BillingResponseCode.OK) {
                lastResult = connectResult
                if (!isTransientBillingError(connectResult.responseCode) || attempt == MAX_PURCHASE_OP_ATTEMPTS - 1) {
                    return connectResult
                }
                delay(INITIAL_CONNECT_RETRY_DELAY_MS * (attempt + 1))
                continue
            }
            val res = consumePurchaseAwait(purchaseToken)
            if (res.responseCode == BillingResponseCode.OK) {
                return res
            }
            lastResult = res
            if (!isTransientBillingError(res.responseCode) || attempt == MAX_PURCHASE_OP_ATTEMPTS - 1) {
                return res
            }
            delay(INITIAL_CONNECT_RETRY_DELAY_MS * (attempt + 1))
        }
        return lastResult ?: BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
            .setDebugMessage("Consume failed")
            .build()
    }

    private suspend fun acknowledgePurchaseWithRetry(
        purchaseToken: String,
        @ProductType productType: String
    ): BillingResult {
        var lastResult: BillingResult? = null
        for (attempt in 0 until MAX_PURCHASE_OP_ATTEMPTS) {
            val connectResult = ensureConnectedAwait()
            if (connectResult.responseCode != BillingResponseCode.OK) {
                lastResult = connectResult
                if (!isTransientBillingError(connectResult.responseCode) || attempt == MAX_PURCHASE_OP_ATTEMPTS - 1) {
                    return connectResult
                }
                delay(INITIAL_CONNECT_RETRY_DELAY_MS * (attempt + 1))
                continue
            }
            val res = acknowledgePurchaseAwait(purchaseToken)
            if (res.responseCode == BillingResponseCode.OK) {
                return res
            }
            if (res.responseCode == BillingResponseCode.DEVELOPER_ERROR) {
                val (queryRes, purchases) = queryPurchasesAwait(productType)
                val confirmedAcked = queryRes.responseCode == BillingResponseCode.OK &&
                    purchases.any { it.purchaseToken == purchaseToken && it.isAcknowledged }
                if (confirmedAcked) {
                    return BillingResult.newBuilder()
                        .setResponseCode(BillingResponseCode.OK)
                        .build()
                }
            }
            lastResult = res
            if (!isTransientBillingError(res.responseCode) || attempt == MAX_PURCHASE_OP_ATTEMPTS - 1) {
                return res
            }
            delay(INITIAL_CONNECT_RETRY_DELAY_MS * (attempt + 1))
        }
        return lastResult ?: BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
            .setDebugMessage("Acknowledge failed")
            .build()
    }

    private suspend fun consumePurchaseAwait(purchaseToken: String): BillingResult {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        return withContext(Dispatchers.IO) {
            val res = withTimeoutOrNull(QUERY_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    gateway.consumeAsync(params) { billingResult, _ ->
                        if (cont.isActive) cont.resume(billingResult)
                    }
                }
            }
            res ?: BillingResult.newBuilder()
                .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
                .setDebugMessage("Consume timed out")
                .build()
        }
    }

    private suspend fun acknowledgePurchaseAwait(purchaseToken: String): BillingResult {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        return withContext(Dispatchers.IO) {
            val res = withTimeoutOrNull(QUERY_TIMEOUT_MS) {
                suspendCancellableCoroutine { cont ->
                    gateway.acknowledgePurchase(params) { billingResult ->
                        if (cont.isActive) cont.resume(billingResult)
                    }
                }
            }
            res ?: BillingResult.newBuilder()
                .setResponseCode(BillingResponseCode.SERVICE_UNAVAILABLE)
                .setDebugMessage("Acknowledge timed out")
                .build()
        }
    }
}
