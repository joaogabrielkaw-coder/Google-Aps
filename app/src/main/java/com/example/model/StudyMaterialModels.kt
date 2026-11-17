package com.example.model

import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.android.billingclient.api.ProductDetails

enum class StudyMaterialCategory(
    val labelPt: String,
    val labelEn: String
) {
    ALL(labelPt = "Todos", labelEn = "All"),
    ALGORITHMS(labelPt = "Algoritmos", labelEn = "Algorithms"),
    DATABASES(labelPt = "Bancos SQL", labelEn = "SQL & DBs"),
    BACKEND(labelPt = "Backend & APIs", labelEn = "Backend & APIs"),
    BUNDLE(labelPt = "Pacote Completo", labelEn = "Full Bundle");

    fun label(language: Language): String = if (language == Language.PT) labelPt else labelEn
}

data class StudyMaterialLessonSection(
    val titlePt: String,
    val titleEn: String,
    val summaryPt: String,
    val summaryEn: String,
    val codeSnippet: String
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
    fun summary(language: Language): String = if (language == Language.PT) summaryPt else summaryEn
}

data class StudyMaterialPackage(
    val productId: String,
    val category: StudyMaterialCategory,
    val titlePt: String,
    val titleEn: String,
    val subtitlePt: String,
    val subtitleEn: String,
    val highlightsPt: List<String>,
    val highlightsEn: List<String>,
    val fallbackPriceBrl: String,
    val fallbackPriceEn: String,
    val moduleCount: Int,
    val estimatedHours: Int,
    val accentColorHex: Long,
    val badgePt: String? = null,
    val badgeEn: String? = null,
    val isFullBundle: Boolean = false,
    val sections: List<StudyMaterialLessonSection> = emptyList()
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
    fun subtitle(language: Language): String = if (language == Language.PT) subtitlePt else subtitleEn
    fun highlights(language: Language): List<String> = if (language == Language.PT) highlightsPt else highlightsEn
    fun badge(language: Language): String? = if (language == Language.PT) badgePt else badgeEn
    fun accentColor(): Color = Color(accentColorHex)

    fun fallbackPrice(language: Language): String =
        if (language == Language.PT) fallbackPriceBrl else fallbackPriceEn
}

/**
 * Snapshot combinado entre o catálogo de pacotes de estudo e o ProductDetails real
 * retornado pelo Google Play BillingClient.
 */
data class StudyMaterialBillingItem(
    val pkg: StudyMaterialPackage,
    val productDetails: ProductDetails?,
    val isOwned: Boolean,
    val isPending: Boolean
) {
    val isAvailableInPlay: Boolean
        get() = productDetails != null

    fun displayTitle(language: Language): String {
        val playName = productDetails?.name?.takeIf { it.isNotBlank() }
        return playName ?: pkg.title(language)
    }

    fun displayDescription(language: Language): String {
        val playDesc = productDetails?.description?.takeIf { it.isNotBlank() }
        return playDesc ?: pkg.subtitle(language)
    }

    fun formattedPrice(language: Language): String {
        val livePrice = productDetails?.oneTimePurchaseOfferDetailsList
            ?.firstOrNull()
            ?.formattedPrice
            ?: productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
        return livePrice?.takeIf { it.isNotBlank() } ?: pkg.fallbackPrice(language)
    }
}

object StudyMaterialOwnershipStorage {
    private const val KEY_OWNED_STUDY_PACK_IDS = "play_owned_study_material_ids"

    fun loadOwnedIds(prefs: SharedPreferences): Set<String> =
        prefs.getStringSet(KEY_OWNED_STUDY_PACK_IDS, emptySet())?.toSet() ?: emptySet()

    fun addOwnedId(prefs: SharedPreferences, productId: String): Set<String> {
        if (productId.isBlank()) return loadOwnedIds(prefs)
        val updated = loadOwnedIds(prefs) + productId
        prefs.edit().putStringSet(KEY_OWNED_STUDY_PACK_IDS, updated).apply()
        return updated
    }

    fun replaceOwnedIds(prefs: SharedPreferences, productIds: Set<String>): Set<String> {
        val sanitized = productIds.filter { it.isNotBlank() }.toSet()
        prefs.edit().putStringSet(KEY_OWNED_STUDY_PACK_IDS, sanitized).apply()
        return sanitized
    }
}

object StudyMaterialCatalog {
    const val PRODUCT_ID_ALGORITHMS_DS = "study_pack_algorithms_ds"
    const val PRODUCT_ID_SQL_MASTERY = "study_pack_sql_mastery"
    const val PRODUCT_ID_BACKEND_INTERVIEWS = "study_pack_backend_interviews"
    const val PRODUCT_ID_FULL_BUNDLE = "study_pack_full_bundle"

    val packages: List<StudyMaterialPackage> = listOf(
        StudyMaterialPackage(
            productId = PRODUCT_ID_ALGORITHMS_DS,
            category = StudyMaterialCategory.ALGORITHMS,
            titlePt = "Kit Algoritmos & Estruturas",
            titleEn = "Algorithms & Data Structures Kit",
            subtitlePt = "Mapas de complexidade Big-O, árvores, grafos, programação dinâmica e 25 desafios comentados.",
            subtitleEn = "Big-O complexity maps, trees, graphs, dynamic programming, and 25 annotated drills.",
            highlightsPt = listOf(
                "Guia visual de Big-O e memória",
                "Árvores, Heaps, Grafos e DP passo a passo",
                "Exemplos prontos em Python e Kotlin"
            ),
            highlightsEn = listOf(
                "Visual Big-O & memory reference",
                "Trees, Heaps, Graphs & DP step-by-step",
                "Ready-to-run Python & Kotlin snippets"
            ),
            fallbackPriceBrl = "R$ 14,90",
            fallbackPriceEn = "R$ 14.90",
            moduleCount = 6,
            estimatedHours = 8,
            accentColorHex = 0xFF8B5CF6L,
            sections = listOf(
                StudyMaterialLessonSection(
                    titlePt = "Busca Binária & Janela Deslizante",
                    titleEn = "Binary Search & Sliding Window",
                    summaryPt = "Reduza buscas O(n) para O(log n) e elimine laços aninhados em subarrays mantendo dois ponteiros.",
                    summaryEn = "Drop O(n) scans to O(log n) and eliminate nested subarray loops using two pointers.",
                    codeSnippet = """
                        fun binarySearch(nums: IntArray, target: Int): Int {
                            var left = 0
                            var right = nums.lastIndex
                            while (left <= right) {
                                val mid = left + (right - left) / 2
                                when {
                                    nums[mid] == target -> return mid
                                    nums[mid] < target -> left = mid + 1
                                    else -> right = mid - 1
                                }
                            }
                            return -1
                        }
                    """.trimIndent()
                ),
                StudyMaterialLessonSection(
                    titlePt = "BFS em Grafos & Caminho Mínimo",
                    titleEn = "Graph BFS & Shortest Path",
                    summaryPt = "Use uma fila FIFO para explorar camadas por distância mínima em grafos não ponderados.",
                    summaryEn = "Use a FIFO queue to traverse level-by-level for shortest paths in unweighted graphs.",
                    codeSnippet = """
                        from collections import deque

                        def shortest_hops(graph, start, goal):
                            queue = deque([(start, 0)])
                            visited = {start}
                            while queue:
                                node, dist = queue.popleft()
                                if node == goal:
                                    return dist
                                for nxt in graph.get(node, []):
                                    if nxt not in visited:
                                        visited.add(nxt)
                                        queue.append((nxt, dist + 1))
                            return -1
                    """.trimIndent()
                )
            )
        ),
        StudyMaterialPackage(
            productId = PRODUCT_ID_SQL_MASTERY,
            category = StudyMaterialCategory.DATABASES,
            titlePt = "Caderno Mestre SQL & Bancos",
            titleEn = "SQL & Database Mastery Pack",
            subtitlePt = "Domine Window Functions, CTEs, índices B-Tree, tuning de queries e modelagem NoSQL.",
            subtitleEn = "Master Window Functions, CTEs, B-Tree indexes, query tuning, and NoSQL modeling.",
            highlightsPt = listOf(
                "Receituário de JOINs, CTEs e Window Functions",
                "Diagnóstico EXPLAIN ANALYZE para PostgreSQL & MySQL",
                "Padrões de agregação e schema no MongoDB"
            ),
            highlightsEn = listOf(
                "JOINs, CTEs & Window Functions handbook",
                "EXPLAIN ANALYZE tuning for Postgres & MySQL",
                "MongoDB aggregation & schema patterns"
            ),
            fallbackPriceBrl = "R$ 19,90",
            fallbackPriceEn = "R$ 19.90",
            moduleCount = 8,
            estimatedHours = 10,
            accentColorHex = 0xFF10B981L,
            badgePt = "Mais Vendido",
            badgeEn = "Top Pick",
            sections = listOf(
                StudyMaterialLessonSection(
                    titlePt = "Ranking com Window Functions",
                    titleEn = "Ranking with Window Functions",
                    summaryPt = "Calcule totais acumulados e posições por categoria sem colapsar as linhas com GROUP BY.",
                    summaryEn = "Compute running totals and per-category ranks without collapsing rows via GROUP BY.",
                    codeSnippet = """
                        SELECT
                            tech_id,
                            student_name,
                            xp_score,
                            DENSE_RANK() OVER (
                                PARTITION BY tech_id
                                ORDER BY xp_score DESC
                            ) AS rank_in_track
                        FROM leaderboard_scores;
                    """.trimIndent()
                ),
                StudyMaterialLessonSection(
                    titlePt = "Índices Compostos & Cobertura",
                    titleEn = "Composite & Covering Indexes",
                    summaryPt = "Ordene colunas do índice por seletividade e filtro de igualdade antes de colunas de intervalo.",
                    summaryEn = "Order composite index columns with equality predicates before range predicates.",
                    codeSnippet = """
                        CREATE INDEX idx_sessions_user_date
                        ON study_sessions (user_id, completed, date_iso DESC);
                    """.trimIndent()
                )
            )
        ),
        StudyMaterialPackage(
            productId = PRODUCT_ID_BACKEND_INTERVIEWS,
            category = StudyMaterialCategory.BACKEND,
            titlePt = "Guia Entrevistas Backend & APIs",
            titleEn = "Backend & API Interview Prep",
            subtitlePt = "Arquitetura Limpa, idempotência REST, concorrência, cache distribuído e perguntas reais.",
            subtitleEn = "Clean Architecture, REST idempotency, concurrency, distributed caching, and real Q&A.",
            highlightsPt = listOf(
                "Checklist de System Design para entrevistas",
                "Idempotência, retries, circuit breaker e filas",
                "Comparativos práticos em Kotlin, Java, Go e Python"
            ),
            highlightsEn = listOf(
                "System Design interview checklist",
                "Idempotency, retries, circuit breakers & queues",
                "Practical Kotlin, Java, Go & Python patterns"
            ),
            fallbackPriceBrl = "R$ 24,90",
            fallbackPriceEn = "R$ 24.90",
            moduleCount = 7,
            estimatedHours = 12,
            accentColorHex = 0xFF38BDF8L,
            sections = listOf(
                StudyMaterialLessonSection(
                    titlePt = "Chave de Idempotência em APIs de Pagamento",
                    titleEn = "Idempotency Keys in Payment APIs",
                    summaryPt = "Evite cobranças duplicadas em retentativas de rede armazenando o hash da requisição com TTL.",
                    summaryEn = "Prevent duplicate charges on network retries by persisting request keys with a TTL.",
                    codeSnippet = """
                        suspend fun processOrder(idempotencyKey: String, amountCents: Long): Receipt {
                            existingReceiptRepo.findByKey(idempotencyKey)?.let { return it }
                            val receipt = gateway.chargeOnce(idempotencyKey, amountCents)
                            existingReceiptRepo.save(idempotencyKey, receipt)
                            return receipt
                        }
                    """.trimIndent()
                )
            )
        ),
        StudyMaterialPackage(
            productId = PRODUCT_ID_FULL_BUNDLE,
            category = StudyMaterialCategory.BUNDLE,
            titlePt = "Acervo Completo DevRoutine",
            titleEn = "Complete DevRoutine Study Bundle",
            subtitlePt = "Desbloqueie todos os 3 kits de estudo + resumos offline exclusivos com economia de 33%.",
            subtitleEn = "Unlock all 3 study kits plus exclusive offline reference cards and save 33%.",
            highlightsPt = listOf(
                "Inclui Algoritmos + SQL Mestre + Backend & APIs",
                "Acesso vitalício vinculado à sua conta Google Play",
                "Atualizações futuras dos cadernos incluídas"
            ),
            highlightsEn = listOf(
                "Includes Algorithms + SQL Mastery + Backend & APIs",
                "Lifetime access tied to your Google Play account",
                "Future study pack updates included"
            ),
            fallbackPriceBrl = "R$ 39,90",
            fallbackPriceEn = "R$ 39.90",
            moduleCount = 21,
            estimatedHours = 30,
            accentColorHex = 0xFFF59E0BL,
            badgePt = "Melhor Valor",
            badgeEn = "Best Value",
            isFullBundle = true,
            sections = listOf(
                StudyMaterialLessonSection(
                    titlePt = "Roteiro Integrado de Alta Performance",
                    titleEn = "Integrated High-Performance Roadmap",
                    summaryPt = "Combine algoritmos, modelagem SQL e arquitetura backend em sprints semanais de 30 minutos.",
                    summaryEn = "Combine algorithms, SQL tuning, and backend design in focused 30-minute weekly sprints.",
                    codeSnippet = """
                        // Sprint Semanal DevRoutine:
                        // Seg/Qua: Estruturas de Dados & Big-O
                        // Ter/Qui: Queries SQL + Índices EXPLAIN
                        // Sex/Sáb: Design de APIs & Concorrência
                    """.trimIndent()
                )
            )
        )
    )

    val allProductIds: List<String>
        get() = packages.map { it.productId }

    fun findByProductId(productId: String): StudyMaterialPackage? =
        packages.firstOrNull { it.productId == productId }

    fun isPackageUnlocked(pkg: StudyMaterialPackage, ownedProductIds: Set<String>): Boolean =
        pkg.productId in ownedProductIds || PRODUCT_ID_FULL_BUNDLE in ownedProductIds
}
