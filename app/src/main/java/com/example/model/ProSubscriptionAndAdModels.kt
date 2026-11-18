package com.example.model

import androidx.compose.ui.graphics.Color

data class ProSubscriptionPlan(
    val id: String = PRODUCT_ID_PRO_MONTHLY,
    val priceBrl: Int = 13,
    val bonusDailyLives: Int = 15,
    val removesAds: Boolean = true,
    val billingPeriodDays: Int = 30
) {
    fun formattedMonthlyPrice(
        language: Language,
        livePlayPrices: Map<String, String> = emptyMap()
    ): String {
        val livePrice = livePlayPrices[id]
        if (!livePrice.isNullOrBlank()) {
            return if (language == Language.PT) "$livePrice/mês" else "$livePrice/mo"
        }
        return if (language == Language.PT) "R$ $priceBrl,00/mês" else "R$ $priceBrl.00/mo"
    }

    companion object {
        const val PRODUCT_ID_PRO_MONTHLY = "devroutine_pro_monthly"
    }
}

enum class PlayBillingConnectionStatus {
    CONNECTING,
    READY,
    UNAVAILABLE,
    DISCONNECTED
}

data class PlayBillingStoreUiState(
    val connectionStatus: PlayBillingConnectionStatus = PlayBillingConnectionStatus.CONNECTING,
    val productPrices: Map<String, String> = emptyMap(),
    val availableProductIds: Set<String> = emptySet(),
    val pendingProductIds: Set<String> = emptySet(),
    val isPurchaseInProgress: Boolean = false,
    val activePurchaseProductId: String? = null,
    val statusMessage: String? = null,
    val isError: Boolean = false,
    val lastBillingResponseCode: Int? = null
) {
    fun isProductReadyInPlay(productId: String): Boolean =
        connectionStatus == PlayBillingConnectionStatus.READY && productId in availableProductIds
}

data class ProSubscriptionState(
    val isActive: Boolean = false,
    val activatedAtMillis: Long = 0L,
    val expiresAtMillis: Long = 0L,
    val paymentMethod: LifePaymentMethod? = null,
    val bonusDailyLives: Int = 15,
    val monthlyPriceBrl: Int = 13,
    val purchaseToken: String? = null,
    val orderId: String? = null,
    val isAutoRenewing: Boolean = false,
    val verifiedByPlayBilling: Boolean = false
) {
    fun isCurrentlyActive(nowMillis: Long = System.currentTimeMillis()): Boolean =
        isActive && (expiresAtMillis <= 0L || nowMillis < expiresAtMillis)

    fun remainingDays(nowMillis: Long = System.currentTimeMillis()): Int {
        if (!isCurrentlyActive(nowMillis) || expiresAtMillis <= 0L) return 0
        val diffMillis = (expiresAtMillis - nowMillis).coerceAtLeast(0L)
        return ((diffMillis / MILLIS_PER_DAY) + 1L).toInt().coerceIn(1, 30)
    }

    companion object {
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
        val DEFAULT_PLAN = ProSubscriptionPlan()
    }
}

data class ModerateAdContent(
    val id: String,
    val sponsorPt: String,
    val sponsorEn: String,
    val headlinePt: String,
    val headlineEn: String,
    val bodyPt: String,
    val bodyEn: String,
    val ctaPt: String,
    val ctaEn: String,
    val accentColorHex: Long
) {
    fun sponsor(language: Language): String = if (language == Language.PT) sponsorPt else sponsorEn
    fun headline(language: Language): String = if (language == Language.PT) headlinePt else headlineEn
    fun body(language: Language): String = if (language == Language.PT) bodyPt else bodyEn
    fun cta(language: Language): String = if (language == Language.PT) ctaPt else ctaEn
    fun accentColor(): Color = Color(accentColorHex)
}

object ModerateAdCatalog {
    val ads: List<ModerateAdContent> = listOf(
        ModerateAdContent(
            id = "ad_cloud_deploy",
            sponsorPt = "CloudDev Host",
            sponsorEn = "CloudDev Host",
            headlinePt = "Publique suas APIs Python e Node em segundos",
            headlineEn = "Deploy Python & Node APIs in seconds",
            bodyPt = "Contêineres leves com banco PostgreSQL gerenciado para projetos de estudo.",
            bodyEn = "Lightweight containers with managed PostgreSQL for your study projects.",
            ctaPt = "Conhecer plano",
            ctaEn = "View plan",
            accentColorHex = 0xFF38BDF8L
        ),
        ModerateAdContent(
            id = "ad_ergo_gear",
            sponsorPt = "KeyCraft Lab",
            sponsorEn = "KeyCraft Lab",
            headlinePt = "Teclados mecânicos compactos para programar",
            headlineEn = "Compact mechanical keyboards for coding",
            bodyPt = "Switches silenciosos e conforto para longas sessões de código.",
            bodyEn = "Quiet switches and comfort for long coding sessions.",
            ctaPt = "Ver modelos",
            ctaEn = "See models",
            accentColorHex = 0xFFA855F7L
        ),
        ModerateAdContent(
            id = "ad_sql_workbench",
            sponsorPt = "DataForge Studio",
            sponsorEn = "DataForge Studio",
            headlinePt = "Modelagem visual de bancos SQL & NoSQL",
            headlineEn = "Visual SQL & NoSQL database modeling",
            bodyPt = "Crie diagramas ER e exporte scripts SQL otimizados com um clique.",
            bodyEn = "Build ER diagrams and export optimized SQL scripts in one click.",
            ctaPt = "Explorar",
            ctaEn = "Explore",
            accentColorHex = 0xFF34D399L
        ),
        ModerateAdContent(
            id = "ad_dev_books",
            sponsorPt = "Código Limpo Press",
            sponsorEn = "Clean Code Press",
            headlinePt = "E-books práticos de Arquitetura & Algoritmos",
            headlineEn = "Hands-on Architecture & Algorithms e-books",
            bodyPt = "Guias ilustrados para preparar você para entrevistas técnicas.",
            bodyEn = "Illustrated guides to prepare you for technical interviews.",
            ctaPt = "Ler amostra",
            ctaEn = "Read sample",
            accentColorHex = 0xFFF59E0BL
        )
    )

    fun pickForSlot(slotIndex: Int): ModerateAdContent {
        val safeIdx = ((slotIndex % ads.size) + ads.size) % ads.size
        return ads[safeIdx]
    }
}

/**
 * Controlador de frequência moderada para anúncios intersticiais:
 * - Nunca exibe anúncios se a assinatura Pro estiver ativa.
 * - Exibe no máximo 1 anúncio a cada 4 ações concluídas (desafios/quizzes)
 *   E exige intervalo mínimo de 3 minutos entre exibições.
 */
object ModerateAdFrequencyPolicy {
    const val ACTIONS_PER_INTERSTITIAL = 4
    const val MIN_COOLDOWN_MILLIS = 3L * 60L * 1000L

    fun shouldShowInterstitial(
        isProActive: Boolean,
        completedActionsSinceLastAd: Int,
        lastAdShownAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (isProActive) return false
        if (completedActionsSinceLastAd < ACTIONS_PER_INTERSTITIAL) return false
        if (lastAdShownAtMillis > 0L && (nowMillis - lastAdShownAtMillis) < MIN_COOLDOWN_MILLIS) {
            return false
        }
        return true
    }
}
