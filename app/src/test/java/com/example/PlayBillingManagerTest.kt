package com.example

import android.app.Activity
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.OnPurchasesUpdatedSubResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.billing.BillingGateway
import com.example.billing.PlayBillingManager
import com.example.billing.QueryProductDetailsOutcomeRaw
import com.example.model.Language
import com.example.model.LifeShopPackage
import com.example.model.PlayBillingConnectionStatus
import com.example.model.ProSubscriptionPlan
import com.example.model.StudyMaterialCatalog
import com.example.model.StudyMaterialPackage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlayBillingManagerTest {

    private lateinit var appContext: Context
    private lateinit var testScope: CoroutineScope
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        appContext = ApplicationProvider.getApplicationContext()
        testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `startConnection invokes state listener transitions and handles disconnect`() {
        val gateway = FakeBillingGateway()
        val prefs = appContext.getSharedPreferences("billing_test_1", Context.MODE_PRIVATE).apply {
            edit().clear().commit()
        }
        val manager = PlayBillingManager(
            context = appContext,
            prefs = prefs,
            scope = testScope,
            languageProvider = { Language.EN },
            obfuscatedAccountIdProvider = { "uid_1" },
            onConsumableLivesGranted = { _, _, _, _ -> },
            onSubscriptionVerified = { _, _, _, _, _ -> },
            onSubscriptionNotActiveInPlay = {},
            gatewayFactory = { listener ->
                gateway.purchasesListener = listener
                gateway
            }
        )

        manager.startConnectionAndSync(userInitiated = true)
        waitUntil { manager.storeUiState.value.connectionStatus == PlayBillingConnectionStatus.READY }

        assertEquals(1, gateway.startConnectionCount.get())
        assertEquals(PlayBillingConnectionStatus.READY, manager.storeUiState.value.connectionStatus)

        // Simulate Play service disconnect notification
        gateway.simulateDisconnect()
        assertEquals(PlayBillingConnectionStatus.DISCONNECTED, manager.storeUiState.value.connectionStatus)

        // Reconnect via refreshOnResume
        manager.refreshOnResume()
        waitUntil { manager.storeUiState.value.connectionStatus == PlayBillingConnectionStatus.READY }
        assertEquals(2, gateway.startConnectionCount.get())
    }

    @Test
    fun `transient startConnection disconnect retries and succeeds`() {
        val gateway = FakeBillingGateway().apply {
            connectResultsQueue.addAll(
                listOf(
                    billingResult(BillingResponseCode.SERVICE_DISCONNECTED, "transient"),
                    billingResult(BillingResponseCode.OK)
                )
            )
        }
        val prefs = appContext.getSharedPreferences("billing_test_2", Context.MODE_PRIVATE).apply {
            edit().clear().commit()
        }
        val manager = PlayBillingManager(
            context = appContext,
            prefs = prefs,
            scope = testScope,
            languageProvider = { Language.PT },
            obfuscatedAccountIdProvider = { "uid_2" },
            onConsumableLivesGranted = { _, _, _, _ -> },
            onSubscriptionVerified = { _, _, _, _, _ -> },
            onSubscriptionNotActiveInPlay = {},
            gatewayFactory = { listener ->
                gateway.purchasesListener = listener
                gateway
            }
        )

        manager.startConnectionAndSync(userInitiated = true)
        waitUntil(timeoutMs = 5_000L) {
            manager.storeUiState.value.connectionStatus == PlayBillingConnectionStatus.READY
        }

        assertEquals(2, gateway.startConnectionCount.get())
        assertFalse(manager.storeUiState.value.isError)
    }

    @Test
    fun `study material purchase retries transient acknowledge failure and unlocks only on ack OK`() {
        val studyPkg = StudyMaterialCatalog.packages.first()
        val unackedPurchase = buildPurchase(
            productId = studyPkg.productId,
            purchaseToken = "tok_study_1",
            orderId = "GPA.1111",
            purchaseStateJson = 0, // PURCHASED
            acknowledged = false
        )
        val gateway = FakeBillingGateway().apply {
            acknowledgeResultsQueue.addAll(
                listOf(
                    billingResult(BillingResponseCode.SERVICE_DISCONNECTED, "lost binder"),
                    billingResult(BillingResponseCode.OK)
                )
            )
        }
        val verifiedCallbacks = CopyOnWriteArrayList<StudyMaterialPackage>()
        val prefs = appContext.getSharedPreferences("billing_test_3", Context.MODE_PRIVATE).apply {
            edit().clear().commit()
        }
        val manager = PlayBillingManager(
            context = appContext,
            prefs = prefs,
            scope = testScope,
            languageProvider = { Language.EN },
            obfuscatedAccountIdProvider = { "uid_3" },
            onConsumableLivesGranted = { _, _, _, _ -> },
            onSubscriptionVerified = { _, _, _, _, _ -> },
            onSubscriptionNotActiveInPlay = {},
            onStudyMaterialVerified = { pkg, _, _ -> verifiedCallbacks.add(pkg) },
            gatewayFactory = { listener ->
                gateway.purchasesListener = listener
                gateway
            }
        )

        manager.startConnectionAndSync(userInitiated = false)
        waitUntil { manager.storeUiState.value.connectionStatus == PlayBillingConnectionStatus.READY }

        gateway.emitPurchasesUpdated(billingResult(BillingResponseCode.OK), listOf(unackedPurchase))

        waitUntil(timeoutMs = 5_000L) { verifiedCallbacks.isNotEmpty() }
        assertEquals(2, gateway.acknowledgeCount.get())
        assertTrue(studyPkg.productId in manager.ownedStudyMaterialIds.value)
        assertFalse(manager.storeUiState.value.isError)

        // Permanent ack error on another SKU must NOT unlock entitlement
        val blockedPkg = StudyMaterialCatalog.packages[1]
        val blockedPurchase = buildPurchase(
            productId = blockedPkg.productId,
            purchaseToken = "tok_study_blocked",
            orderId = "GPA.2222",
            purchaseStateJson = 0,
            acknowledged = false
        )
        gateway.acknowledgeResultsQueue.add(
            billingResult(BillingResponseCode.BILLING_UNAVAILABLE, "ack rejected")
        )
        gateway.emitPurchasesUpdated(billingResult(BillingResponseCode.OK), listOf(blockedPurchase))

        waitUntil { manager.storeUiState.value.isError }
        assertFalse(blockedPkg.productId in manager.ownedStudyMaterialIds.value)
        assertEquals(1, verifiedCallbacks.size)
    }

    @Test
    fun `pending purchase does not acknowledge or grant until transitioning to purchased`() {
        val gateway = FakeBillingGateway()
        val verifiedCount = AtomicInteger(0)
        val prefs = appContext.getSharedPreferences("billing_test_4", Context.MODE_PRIVATE).apply {
            edit().clear().commit()
        }
        val manager = PlayBillingManager(
            context = appContext,
            prefs = prefs,
            scope = testScope,
            languageProvider = { Language.PT },
            obfuscatedAccountIdProvider = { "uid_4" },
            onConsumableLivesGranted = { _, _, _, _ -> },
            onSubscriptionVerified = { _, _, _, _, _ -> verifiedCount.incrementAndGet() },
            onSubscriptionNotActiveInPlay = {},
            gatewayFactory = { listener ->
                gateway.purchasesListener = listener
                gateway
            }
        )

        val pendingSub = buildPurchase(
            productId = ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY,
            purchaseToken = "tok_sub_pending",
            orderId = "GPA.3333",
            purchaseStateJson = 4, // PENDING
            acknowledged = false
        )
        gateway.emitPurchasesUpdated(billingResult(BillingResponseCode.OK), listOf(pendingSub))

        waitUntil { ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY in manager.storeUiState.value.pendingProductIds }
        assertEquals(0, gateway.acknowledgeCount.get())
        assertEquals(0, verifiedCount.get())
        assertFalse(manager.storeUiState.value.isError)

        // Now payment settles -> PURCHASED
        val settledSub = buildPurchase(
            productId = ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY,
            purchaseToken = "tok_sub_pending",
            orderId = "GPA.3333",
            purchaseStateJson = 0, // PURCHASED
            acknowledged = false
        )
        gateway.emitPurchasesUpdated(billingResult(BillingResponseCode.OK), listOf(settledSub))

        waitUntil { verifiedCount.get() == 1 }
        assertEquals(1, gateway.acknowledgeCount.get())
        assertFalse(ProSubscriptionPlan.PRODUCT_ID_PRO_MONTHLY in manager.storeUiState.value.pendingProductIds)
        assertFalse(manager.storeUiState.value.isError)
    }

    @Test
    fun `concurrent purchasesUpdated and refreshOnResume deduplicates consumable consumption and preserves confirmation`() {
        val lifePurchase = buildPurchase(
            productId = "lives_brl_10",
            purchaseToken = "tok_life_concurrent",
            orderId = "GPA.4444",
            purchaseStateJson = 0,
            acknowledged = true,
            quantity = 1
        )
        val gateway = FakeBillingGateway().apply {
            inAppPurchasesToReturn = listOf(lifePurchase)
            consumeDelayMs = 80L
        }
        val grantedLives = CopyOnWriteArrayList<Pair<LifeShopPackage, Int>>()
        val prefs = appContext.getSharedPreferences("billing_test_5", Context.MODE_PRIVATE).apply {
            edit().clear().commit()
        }
        val manager = PlayBillingManager(
            context = appContext,
            prefs = prefs,
            scope = testScope,
            languageProvider = { Language.EN },
            obfuscatedAccountIdProvider = { "uid_5" },
            onConsumableLivesGranted = { pkg, qty, _, _ -> grantedLives.add(pkg to qty) },
            onSubscriptionVerified = { _, _, _, _, _ -> },
            onSubscriptionNotActiveInPlay = {},
            gatewayFactory = { listener ->
                gateway.purchasesListener = listener
                gateway
            }
        )

        manager.startConnectionAndSync(userInitiated = false)
        waitUntil { manager.storeUiState.value.connectionStatus == PlayBillingConnectionStatus.READY }

        // Fire ON_RESUME sync and PurchasesUpdatedListener nearly simultaneously
        manager.refreshOnResume()
        gateway.emitPurchasesUpdated(billingResult(BillingResponseCode.OK), listOf(lifePurchase))

        waitUntil(timeoutMs = 5_000L) {
            grantedLives.isNotEmpty() &&
                manager.storeUiState.value.statusMessage?.contains("lives credited") == true
        }
        Thread.sleep(200L)

        assertEquals("Consumable must be consumed exactly once", 1, gateway.consumeCount.get())
        assertEquals("Lives must be granted exactly once", 1, grantedLives.size)
        assertFalse(manager.storeUiState.value.isError)
    }

    @Test
    fun `userCanceled with insufficient funds subResponseCode surfaces error state`() {
        val gateway = FakeBillingGateway()
        val prefs = appContext.getSharedPreferences("billing_test_6", Context.MODE_PRIVATE).apply {
            edit().clear().commit()
        }
        val manager = PlayBillingManager(
            context = appContext,
            prefs = prefs,
            scope = testScope,
            languageProvider = { Language.EN },
            obfuscatedAccountIdProvider = { "uid_6" },
            onConsumableLivesGranted = { _, _, _, _ -> },
            onSubscriptionVerified = { _, _, _, _, _ -> },
            onSubscriptionNotActiveInPlay = {},
            gatewayFactory = { listener ->
                gateway.purchasesListener = listener
                gateway
            }
        )

        val declinedResult = BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.USER_CANCELED)
            .setOnPurchasesUpdatedSubResponseCode(
                OnPurchasesUpdatedSubResponseCode.PAYMENT_DECLINED_DUE_TO_INSUFFICIENT_FUNDS
            )
            .build()
        gateway.emitPurchasesUpdated(declinedResult, null)

        waitUntil { manager.storeUiState.value.statusMessage != null }
        assertTrue(manager.storeUiState.value.isError)
        assertTrue(manager.storeUiState.value.statusMessage!!.contains("insufficient funds", ignoreCase = true))

        val plainCancel = BillingResult.newBuilder()
            .setResponseCode(BillingResponseCode.USER_CANCELED)
            .build()
        gateway.emitPurchasesUpdated(plainCancel, null)
        assertFalse(manager.storeUiState.value.isError)
        assertNotNull(manager.storeUiState.value.statusMessage)
    }

    private fun waitUntil(timeoutMs: Long = 3_000L, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(15L)
        }
        assertTrue("Condition timed out after ${timeoutMs}ms", condition())
    }

    private fun billingResult(code: Int, debug: String = ""): BillingResult =
        BillingResult.newBuilder()
            .setResponseCode(code)
            .setDebugMessage(debug)
            .build()

    private fun buildPurchase(
        productId: String,
        purchaseToken: String,
        orderId: String,
        purchaseStateJson: Int,
        acknowledged: Boolean,
        quantity: Int = 1
    ): Purchase {
        val json = JSONObject().apply {
            put("orderId", orderId)
            put("packageName", "com.aistudio.devroutine.kjxq")
            put("productIds", JSONArray().put(productId))
            put("purchaseTime", 1_760_000_000_000L)
            put("purchaseState", purchaseStateJson)
            put("purchaseToken", purchaseToken)
            put("acknowledged", acknowledged)
            put("quantity", quantity)
        }
        return Purchase(json.toString(), "test_sig")
    }

    private class FakeBillingGateway : BillingGateway {
        @Volatile
        private var state: Int = BillingClient.ConnectionState.DISCONNECTED

        @Volatile
        private var activeStateListener: BillingClientStateListener? = null

        lateinit var purchasesListener: PurchasesUpdatedListener

        val startConnectionCount = AtomicInteger(0)
        val consumeCount = AtomicInteger(0)
        val acknowledgeCount = AtomicInteger(0)

        val connectResultsQueue = ArrayDeque<BillingResult>()
        val acknowledgeResultsQueue = ArrayDeque<BillingResult>()

        @Volatile
        var inAppPurchasesToReturn: List<Purchase> = emptyList()

        @Volatile
        var subsPurchasesToReturn: List<Purchase> = emptyList()

        @Volatile
        var consumeDelayMs: Long = 0L

        override val connectionState: Int
            get() = state

        override fun startConnection(listener: BillingClientStateListener) {
            startConnectionCount.incrementAndGet()
            activeStateListener = listener
            val result = if (connectResultsQueue.isNotEmpty()) {
                connectResultsQueue.removeFirst()
            } else {
                BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
            }
            if (result.responseCode == BillingResponseCode.OK) {
                state = BillingClient.ConnectionState.CONNECTED
            } else {
                state = BillingClient.ConnectionState.DISCONNECTED
            }
            listener.onBillingSetupFinished(result)
        }

        fun simulateDisconnect() {
            state = BillingClient.ConnectionState.DISCONNECTED
            activeStateListener?.onBillingServiceDisconnected()
        }

        fun emitPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
            purchasesListener.onPurchasesUpdated(result, purchases)
        }

        override fun endConnection() {
            state = BillingClient.ConnectionState.CLOSED
        }

        override fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult =
            BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()

        override fun queryProductDetailsAsync(
            params: QueryProductDetailsParams,
            onResult: (QueryProductDetailsOutcomeRaw) -> Unit
        ) {
            onResult(
                QueryProductDetailsOutcomeRaw(
                    billingResult = BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
                )
            )
        }

        override fun queryPurchasesAsync(
            params: QueryPurchasesParams,
            onResult: (BillingResult, List<Purchase>) -> Unit
        ) {
            val ok = BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
            onResult(ok, inAppPurchasesToReturn + subsPurchasesToReturn)
        }

        override fun consumeAsync(params: ConsumeParams, onResult: (BillingResult, String) -> Unit) {
            if (consumeDelayMs > 0) Thread.sleep(consumeDelayMs)
            consumeCount.incrementAndGet()
            onResult(
                BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build(),
                params.purchaseToken
            )
        }

        override fun acknowledgePurchase(params: AcknowledgePurchaseParams, onResult: (BillingResult) -> Unit) {
            acknowledgeCount.incrementAndGet()
            val result = if (acknowledgeResultsQueue.isNotEmpty()) {
                acknowledgeResultsQueue.removeFirst()
            } else {
                BillingResult.newBuilder().setResponseCode(BillingResponseCode.OK).build()
            }
            onResult(result)
        }
    }
}
