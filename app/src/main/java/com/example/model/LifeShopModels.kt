package com.example.model

enum class LifePurchaseCurrency {
    BRL,
    XP,
    CRYSTALS
}

enum class LifePaymentMethod(
    val labelPt: String,
    val labelEn: String
) {
    PIX(labelPt = "Pix Instantâneo", labelEn = "Pix Instant"),
    CREDIT_CARD(labelPt = "Cartão de Crédito", labelEn = "Credit Card"),
    GOOGLE_PLAY(labelPt = "Saldo Google Play", labelEn = "Google Play Balance");

    fun label(language: Language): String = if (language == Language.PT) labelPt else labelEn
}

data class LifeShopPackage(
    val id: String,
    val livesAmount: Int,
    val costAmount: Int,
    val currency: LifePurchaseCurrency,
    val titlePt: String,
    val titleEn: String,
    val subtitlePt: String,
    val subtitleEn: String,
    val badgePt: String? = null,
    val badgeEn: String? = null,
    val priceLabelPt: String? = null,
    val priceLabelEn: String? = null,
    val playProductId: String? = null
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
    fun subtitle(language: Language): String = if (language == Language.PT) subtitlePt else subtitleEn
    fun badge(language: Language): String? = if (language == Language.PT) badgePt else badgeEn

    fun formattedPrice(
        language: Language,
        livePlayPrices: Map<String, String> = emptyMap()
    ): String = when (currency) {
        LifePurchaseCurrency.BRL -> {
            val livePrice = playProductId?.let { livePlayPrices[it] }
            if (!livePrice.isNullOrBlank()) {
                livePrice
            } else if (language == Language.PT) {
                priceLabelPt ?: "R$ $costAmount,00"
            } else {
                priceLabelEn ?: "R$ $costAmount.00"
            }
        }
        LifePurchaseCurrency.XP -> "$costAmount XP"
        LifePurchaseCurrency.CRYSTALS -> "$costAmount 💎"
    }
}

data class LifePurchaseReceipt(
    val pkg: LifeShopPackage,
    val newRemainingLives: Int,
    val paymentMethod: LifePaymentMethod? = null,
    val orderId: String? = null,
    val quantity: Int = 1,
    val timestampMillis: Long = System.currentTimeMillis()
)

sealed interface LifePurchaseResult {
    data class Success(
        val pkg: LifeShopPackage,
        val newRemainingLives: Int,
        val paymentMethod: LifePaymentMethod? = null,
        val orderId: String? = null
    ) : LifePurchaseResult

    data class InsufficientFunds(
        val required: Int,
        val available: Int,
        val currency: LifePurchaseCurrency
    ) : LifePurchaseResult

    data class QuotaStillAvailable(
        val remainingLives: Int
    ) : LifePurchaseResult

    data class RequiresRealBilling(
        val pkg: LifeShopPackage
    ) : LifePurchaseResult
}

object LifeShopCatalog {
    const val PRODUCT_ID_LIVES_10 = "lives_brl_10"
    const val PRODUCT_ID_LIVES_20 = "lives_brl_20"
    const val PRODUCT_ID_LIVES_50 = "lives_brl_50"

    val brlPackages: List<LifeShopPackage> = listOf(
        LifeShopPackage(
            id = PRODUCT_ID_LIVES_10,
            livesAmount = 10,
            costAmount = 5,
            currency = LifePurchaseCurrency.BRL,
            titlePt = "+10 Vidas Extras",
            titleEn = "+10 Extra Lives",
            subtitlePt = "Ideal para continuar sua sequência de desafios",
            subtitleEn = "Ideal to keep your challenge streak alive",
            priceLabelPt = "R$ 5,00",
            priceLabelEn = "R$ 5.00",
            playProductId = PRODUCT_ID_LIVES_10
        ),
        LifeShopPackage(
            id = PRODUCT_ID_LIVES_20,
            livesAmount = 20,
            costAmount = 10,
            currency = LifePurchaseCurrency.BRL,
            titlePt = "+20 Vidas Extras",
            titleEn = "+20 Extra Lives",
            subtitlePt = "O dobro de vidas pelo mesmo custo por vida",
            subtitleEn = "Double the extra lives for your daily practice",
            badgePt = "Mais Popular",
            badgeEn = "Popular",
            priceLabelPt = "R$ 10,00",
            priceLabelEn = "R$ 10.00",
            playProductId = PRODUCT_ID_LIVES_20
        ),
        LifeShopPackage(
            id = PRODUCT_ID_LIVES_50,
            livesAmount = 50,
            costAmount = 35,
            currency = LifePurchaseCurrency.BRL,
            titlePt = "+50 Vidas Extras",
            titleEn = "+50 Extra Lives",
            subtitlePt = "Super pacote para maratonas intensas de estudo",
            subtitleEn = "Mega pack for intensive coding study marathons",
            badgePt = "Super Pacote",
            badgeEn = "Mega Pack",
            priceLabelPt = "R$ 35,00",
            priceLabelEn = "R$ 35.00",
            playProductId = PRODUCT_ID_LIVES_50
        )
    )

    val packages: List<LifeShopPackage> = listOf(
        LifeShopPackage(
            id = "lives_quick_5",
            livesAmount = 5,
            costAmount = 150,
            currency = LifePurchaseCurrency.XP,
            titlePt = "Recarga Rápida (+5 Vidas)",
            titleEn = "Quick Refill (+5 Lives)",
            subtitlePt = "Volte imediatamente para terminar seu desafio",
            subtitleEn = "Jump right back in to finish your challenge"
        ),
        LifeShopPackage(
            id = "lives_study_12",
            livesAmount = 12,
            costAmount = 350,
            currency = LifePurchaseCurrency.XP,
            titlePt = "Pacote Estudante (+12 Vidas)",
            titleEn = "Study Pack (+12 Lives)",
            subtitlePt = "Melhor custo-benefício em XP por vida extra",
            subtitleEn = "Best XP value per extra life",
            badgePt = "Mais Popular",
            badgeEn = "Popular"
        ),
        LifeShopPackage(
            id = "lives_full_25",
            livesAmount = 25,
            costAmount = 650,
            currency = LifePurchaseCurrency.XP,
            titlePt = "Cota Completa (+25 Vidas)",
            titleEn = "Full Quota (+25 Lives)",
            subtitlePt = "Restaura todas as 25 vidas diárias agora",
            subtitleEn = "Restore all 25 daily lives right now",
            badgePt = "Cota Completa",
            badgeEn = "Full Refill"
        ),
        LifeShopPackage(
            id = "lives_crystals_10",
            livesAmount = 10,
            costAmount = 50,
            currency = LifePurchaseCurrency.CRYSTALS,
            titlePt = "Pacote Diamante 3D (+10 Vidas)",
            titleEn = "3D Diamond Pack (+10 Lives)",
            subtitlePt = "Mínimo de 50 diamantes coletados no Jogo 3D",
            subtitleEn = "Minimum 50 diamonds collected in the 3D Game",
            badgePt = "Jogo 3D",
            badgeEn = "3D Game"
        )
    )

    val allPackages: List<LifeShopPackage>
        get() = brlPackages + packages

    fun findBrlPackageByProductId(productId: String): LifeShopPackage? =
        brlPackages.firstOrNull { it.playProductId == productId || it.id == productId }

    fun evaluatePurchaseEligibility(
        remainingLives: Int,
        pkg: LifeShopPackage,
        availableXp: Int,
        availableCrystals: Int
    ): LifePurchaseResult? {
        if (pkg.currency == LifePurchaseCurrency.BRL) {
            return LifePurchaseResult.RequiresRealBilling(pkg)
        }
        if (remainingLives > 0) {
            return LifePurchaseResult.QuotaStillAvailable(remainingLives)
        }
        return when (pkg.currency) {
            LifePurchaseCurrency.BRL -> LifePurchaseResult.RequiresRealBilling(pkg)
            LifePurchaseCurrency.XP -> {
                if (availableXp < pkg.costAmount) {
                    LifePurchaseResult.InsufficientFunds(
                        required = pkg.costAmount,
                        available = availableXp,
                        currency = LifePurchaseCurrency.XP
                    )
                } else null
            }
            LifePurchaseCurrency.CRYSTALS -> {
                if (availableCrystals < pkg.costAmount) {
                    LifePurchaseResult.InsufficientFunds(
                        required = pkg.costAmount,
                        available = availableCrystals,
                        currency = LifePurchaseCurrency.CRYSTALS
                    )
                } else null
            }
        }
    }
}
