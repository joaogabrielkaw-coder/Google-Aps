package com.example.model

enum class Language(val code: String, val label: String, val flag: String) {
    PT("pt", "Português", "🇧🇷"),
    EN("en", "English", "🇺🇸")
}

enum class ThemePreference(val code: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromCode(code: String?): ThemePreference = when (code?.lowercase()) {
            "light" -> LIGHT
            "system" -> SYSTEM
            else -> DARK
        }
    }
}

enum class ThemeSurfaceStyle(val id: String, val labelPt: String, val labelEn: String) {
    TINTED("tinted", "Tonalidade da Paleta", "Palette Tinted"),
    AMOLED("amoled", "AMOLED / Branco Puro", "AMOLED / Pure White"),
    SLATE("slate", "Ardósia Neutra", "Neutral Slate"),
    SOFT_WARM("soft_warm", "Conforto Suave", "Soft Warm");

    companion object {
        fun fromId(id: String?): ThemeSurfaceStyle =
            entries.firstOrNull { it.id == id } ?: TINTED
    }
}

enum class ThemePreset(
    val id: String,
    val namePt: String,
    val nameEn: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val tertiaryHex: Long
) {
    PURPLE_CRIMSON("purple_crimson", "Roxo & Carmesim", "Purple & Crimson", 0xFF9D4EDDL, 0xFFFF2A55L, 0xFFC77DFFL),
    CYBER_OCEAN("cyber_ocean", "Oceano Ciber", "Cyber Ocean", 0xFF00B4D8L, 0xFF3A86FFL, 0xFF72EFDDL),
    NEON_EMERALD("neon_emerald", "Esmeralda Neon", "Neon Emerald", 0xFF00C853L, 0xFF00BFA5L, 0xFF69F0AEL),
    SOLAR_SUNSET("solar_sunset", "Pôr do Sol", "Solar Sunset", 0xFFFF7B00L, 0xFFFF2A55L, 0xFFFFC300L),
    SAKURA_ROSE("sakura_rose", "Rosa Sakura", "Sakura Rose", 0xFFE91E63L, 0xFFAB47BCL, 0xFFFF80ABL),
    IMPERIAL_GOLD("imperial_gold", "Ouro Imperial", "Imperial Gold", 0xFFFFB300L, 0xFFFF6D00L, 0xFFFFE082L),
    ARCTIC_INDIGO("arctic_indigo", "Índigo Ártico", "Arctic Indigo", 0xFF536DFEL, 0xFF00E5FFL, 0xFF8C9EFFL),
    BOREAL_MINT("boreal_mint", "Menta Boreal", "Boreal Mint", 0xFF1DE9B6L, 0xFF00B0FFL, 0xFFA7FFEBL),
    RUBY_VOLCANO("ruby_volcano", "Vulcão Rubi", "Ruby Volcano", 0xFFFF1744L, 0xFFFF9100L, 0xFFFF8A80L),
    GRAPHITE_MONO("graphite_mono", "Grafite Platina", "Graphite Platinum", 0xFF78909CL, 0xFFB0BEC5L, 0xFFCFD8DCL),
    LAVENDER_SKY("lavender_sky", "Lavanda Celeste", "Lavender Sky", 0xFF7C4DFFL, 0xFF40C4FFL, 0xFFB388FFL),
    LIME_ELECTRIC("lime_electric", "Lima Elétrico", "Electric Lime", 0xFF64DD17L, 0xFF00E5FFL, 0xFFB2FF59L);

    companion object {
        fun fromId(id: String?): ThemePreset? = entries.firstOrNull { it.id == id }
    }
}

data class ThemeCustomization(
    val themeMode: ThemePreference = ThemePreference.DARK,
    val presetId: String = DEFAULT_PRESET_ID,
    val primaryColorHex: Long = DEFAULT_PRIMARY_HEX,
    val secondaryColorHex: Long = DEFAULT_SECONDARY_HEX,
    val tertiaryColorHex: Long = DEFAULT_TERTIARY_HEX,
    val surfaceStyle: ThemeSurfaceStyle = ThemeSurfaceStyle.TINTED,
    val backgroundTintIntensity: Float = DEFAULT_BG_TINT_INTENSITY,
    val updatedAtMillis: Long = 0L
) {
    val isDefaultPalette: Boolean
        get() = presetId == DEFAULT_PRESET_ID &&
            (primaryColorHex and 0x00FFFFFFL) == (DEFAULT_PRIMARY_HEX and 0x00FFFFFFL) &&
            (secondaryColorHex and 0x00FFFFFFL) == (DEFAULT_SECONDARY_HEX and 0x00FFFFFFL) &&
            (tertiaryColorHex and 0x00FFFFFFL) == (DEFAULT_TERTIARY_HEX and 0x00FFFFFFL) &&
            surfaceStyle == ThemeSurfaceStyle.TINTED

    companion object {
        const val DEFAULT_PRESET_ID = "purple_crimson"
        const val CUSTOM_PRESET_ID = "custom"
        const val DEFAULT_PRIMARY_HEX = 0xFF9D4EDDL
        const val DEFAULT_SECONDARY_HEX = 0xFFFF2A55L
        const val DEFAULT_TERTIARY_HEX = 0xFFC77DFFL
        const val DEFAULT_BG_TINT_INTENSITY = 0.45f

        fun fromPreset(
            preset: ThemePreset,
            themeMode: ThemePreference = ThemePreference.DARK,
            surfaceStyle: ThemeSurfaceStyle = ThemeSurfaceStyle.TINTED,
            backgroundTintIntensity: Float = DEFAULT_BG_TINT_INTENSITY
        ): ThemeCustomization = ThemeCustomization(
            themeMode = themeMode,
            presetId = preset.id,
            primaryColorHex = preset.primaryHex,
            secondaryColorHex = preset.secondaryHex,
            tertiaryColorHex = preset.tertiaryHex,
            surfaceStyle = surfaceStyle,
            backgroundTintIntensity = backgroundTintIntensity,
            updatedAtMillis = System.currentTimeMillis()
        )

        fun formatHex(argb: Long): String {
            val rgb = (argb and 0x00FFFFFFL).toInt()
            return String.format("#%06X", rgb)
        }

        fun parseHexColorOrNull(input: String): Long? {
            val cleaned = input.trim().removePrefix("#").uppercase()
            if (cleaned.length != 6 || !cleaned.all { it in '0'..'9' || it in 'A'..'F' }) {
                return null
            }
            val rgb = cleaned.toLongOrNull(16) ?: return null
            return 0xFF000000L or rgb
        }

        fun fromStorageMap(raw: Map<*, *>?): ThemeCustomization? {
            if (raw == null) return null
            val themeModeCode = raw["themeMode"]?.toString()
            val presetId = raw["presetId"]?.toString()?.takeIf { it.isNotBlank() } ?: DEFAULT_PRESET_ID
            val primaryHex = parseColorField(raw["primaryColorHex"]) ?: DEFAULT_PRIMARY_HEX
            val secondaryHex = parseColorField(raw["secondaryColorHex"]) ?: DEFAULT_SECONDARY_HEX
            val tertiaryHex = parseColorField(raw["tertiaryColorHex"]) ?: DEFAULT_TERTIARY_HEX
            val surfaceStyle = ThemeSurfaceStyle.fromId(raw["surfaceStyle"]?.toString())
            val tint = ((raw["backgroundTintIntensity"] as? Number)?.toFloat() ?: DEFAULT_BG_TINT_INTENSITY)
                .coerceIn(0f, 1f)
            val updatedAt = (raw["updatedAtMillis"] as? Number)?.toLong() ?: 0L

            return ThemeCustomization(
                themeMode = ThemePreference.fromCode(themeModeCode),
                presetId = presetId,
                primaryColorHex = primaryHex,
                secondaryColorHex = secondaryHex,
                tertiaryColorHex = tertiaryHex,
                surfaceStyle = surfaceStyle,
                backgroundTintIntensity = tint,
                updatedAtMillis = updatedAt
            )
        }

        private fun parseColorField(value: Any?): Long? {
            return when (value) {
                is Number -> 0xFF000000L or (value.toLong() and 0x00FFFFFFL)
                is String -> parseHexColorOrNull(value)
                else -> null
            }
        }
    }

    fun toStorageMap(): Map<String, Any> = mapOf(
        "themeMode" to themeMode.code,
        "presetId" to presetId,
        "primaryColorHex" to formatHex(primaryColorHex),
        "secondaryColorHex" to formatHex(secondaryColorHex),
        "tertiaryColorHex" to formatHex(tertiaryColorHex),
        "surfaceStyle" to surfaceStyle.id,
        "backgroundTintIntensity" to backgroundTintIntensity.toDouble(),
        "updatedAtMillis" to updatedAtMillis
    )
}

data class StudySession(
    val id: Long = 0,
    val tech: Technology,
    val titlePt: String,
    val titleEn: String,
    val topicPt: String,
    val topicEn: String,
    val notesPt: String = "",
    val notesEn: String = "",
    val dateIso: String, // YYYY-MM-DD
    val timeFormatted: String = "19:00",
    val durationMinutes: Int = 45,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long = 0L,
    val aiGenerated: Boolean = false
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
    fun topic(language: Language): String = if (language == Language.PT) topicPt else topicEn
    fun notes(language: Language): String = if (language == Language.PT) notesPt else notesEn
}

data class RoutinePlan(
    val id: String,
    val titlePt: String,
    val titleEn: String,
    val descriptionPt: String,
    val descriptionEn: String,
    val targetTechs: List<Technology>,
    val sessions: List<StudySession>,
    val totalWeeks: Int = 4
)

data class QuizQuestion(
    val id: Int,
    val tech: Technology,
    val questionPt: String,
    val questionEn: String,
    val optionsPt: List<String>,
    val optionsEn: List<String>,
    val correctIndex: Int,
    val explanationPt: String,
    val explanationEn: String,
    val pointsValue: Int = 20
) {
    fun question(language: Language): String = if (language == Language.PT) questionPt else questionEn
    fun options(language: Language): List<String> = if (language == Language.PT) optionsPt else optionsEn
    fun explanation(language: Language): String = if (language == Language.PT) explanationPt else explanationEn
}

data class CheatsheetItem(
    val titlePt: String,
    val titleEn: String,
    val codeSnippet: String,
    val explanationPt: String,
    val explanationEn: String
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
    fun explanation(language: Language): String = if (language == Language.PT) explanationPt else explanationEn
}

data class RoadmapStep(
    val stepNumber: Int,
    val titlePt: String,
    val titleEn: String,
    val descPt: String,
    val descEn: String,
    val estimatedHours: Int,
    val topicsPt: List<String>,
    val topicsEn: List<String>
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
    fun desc(language: Language): String = if (language == Language.PT) descPt else descEn
    fun topics(language: Language): List<String> = if (language == Language.PT) topicsPt else topicsEn
}

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val suggestedPlan: RoutinePlan? = null
)

enum class ActivityDifficulty(val labelPt: String, val labelEn: String, val badgeColorHex: Long) {
    BEGINNER("Iniciante", "Beginner", 0xFF2E7D32),
    INTERMEDIATE("Intermediário", "Intermediate", 0xFFE65100),
    ADVANCED("Avançado", "Advanced", 0xFFC2185B)
}

/**
 * Representa uma atividade prática de codificação ou estruturação de queries.
 */
data class PracticalActivity(
    val id: String,
    val tech: Technology,
    val titlePt: String,
    val titleEn: String,
    val difficulty: ActivityDifficulty,
    val pointsValue: Int = 50,
    val instructionsPt: String,
    val instructionsEn: String,
    val starterCode: String,
    val solutionCode: String,
    val hintsPt: List<String>,
    val hintsEn: List<String>,
    val requiredKeywords: List<String>,
    val explanationPt: String,
    val explanationEn: String
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
    fun instructions(language: Language): String = if (language == Language.PT) instructionsPt else instructionsEn
    fun hints(language: Language): List<String> = if (language == Language.PT) hintsPt else hintsEn
    fun explanation(language: Language): String = if (language == Language.PT) explanationPt else explanationEn
}

/**
 * Definição de um nível no sistema expandido de progressão do DevRoutine AI.
 */
data class DeveloperLevelTier(
    val levelNumber: Int,
    val minXp: Int,
    val nextLevelXp: Int,
    val titlePt: String,
    val titleEn: String,
    val rankBadgePt: String,
    val rankBadgeEn: String
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
    fun rankBadge(language: Language): String = if (language == Language.PT) rankBadgePt else rankBadgeEn
}

/**
 * Evento emitido quando o usuário alcança um novo nível e ganha reset completo de 25 vidas.
 */
data class LevelUpEvent(
    val oldLevel: Int,
    val newLevel: Int,
    val titlePt: String,
    val titleEn: String,
    val rankBadgePt: String,
    val rankBadgeEn: String,
    val totalXp: Int,
    val nextLevelTarget: Int,
    val livesRestored: Int = 25
)

/**
 * Catálogo oficial de 12 níveis com progressão mais lenta/desafiadora (curva acentuada de XP).
 */
object DeveloperLevelCatalog {
    val tiers: List<DeveloperLevelTier> = listOf(
        DeveloperLevelTier(1, 0, 250, "Aspirante a Dev", "Aspiring Dev", "Iniciante", "Starter"),
        DeveloperLevelTier(2, 250, 650, "Aprendiz de Lógica", "Logic Apprentice", "Bronze I", "Bronze I"),
        DeveloperLevelTier(3, 650, 1250, "Dev Júnior", "Junior Dev", "Bronze II", "Bronze II"),
        DeveloperLevelTier(4, 1250, 2100, "Explorador de Código", "Code Explorer", "Prata I", "Silver I"),
        DeveloperLevelTier(5, 2100, 3300, "Dev Pleno", "Mid-Level Dev", "Prata II", "Silver II"),
        DeveloperLevelTier(6, 3300, 4900, "Especialista Full-Stack", "Full-Stack Specialist", "Ouro I", "Gold I"),
        DeveloperLevelTier(7, 4900, 7000, "Dev Sênior", "Senior Dev", "Ouro II", "Gold II"),
        DeveloperLevelTier(8, 7000, 9800, "Arquiteto de Sistemas", "Systems Architect", "Platina", "Platinum"),
        DeveloperLevelTier(9, 9800, 13500, "Tech Lead", "Tech Lead", "Esmeralda", "Emerald"),
        DeveloperLevelTier(10, 13500, 18500, "Engenheiro Principal", "Principal Engineer", "Diamante", "Diamond"),
        DeveloperLevelTier(11, 18500, 25000, "Mestre da Engenharia", "Engineering Master", "Mestre", "Master"),
        DeveloperLevelTier(12, 25000, 25000, "Lenda DevRoutine", "DevRoutine Legend", "Grão-Mestre", "Grandmaster")
    )

    fun resolveForXp(xp: Int): DeveloperLevelTier {
        val safeXp = xp.coerceAtLeast(0)
        return tiers.lastOrNull { safeXp >= it.minXp } ?: tiers.first()
    }

    fun calculateScoreInfo(
        score: Int,
        quizCount: Int = 0,
        actCount: Int = 0,
        actIds: Set<String> = emptySet(),
        quizIds: Set<Int> = emptySet()
    ): UserScoreInfo {
        val safeScore = score.coerceAtLeast(0)
        val tier = resolveForXp(safeScore)
        val isMax = tier.levelNumber >= tiers.last().levelNumber
        val range = (tier.nextLevelXp - tier.minXp).coerceAtLeast(1)
        val progress = if (isMax) 1f else ((safeScore - tier.minXp).toFloat() / range).coerceIn(0f, 1f)

        return UserScoreInfo(
            totalScore = safeScore,
            correctQuizCount = quizCount,
            completedActivitiesCount = actCount,
            levelNumber = tier.levelNumber,
            levelTitlePt = tier.titlePt,
            levelTitleEn = tier.titleEn,
            rankBadgePt = tier.rankBadgePt,
            rankBadgeEn = tier.rankBadgeEn,
            currentLevelMin = tier.minXp,
            nextLevelTarget = tier.nextLevelXp,
            progressFraction = progress,
            isMaxLevel = isMax,
            completedActivityIds = actIds,
            completedQuizIds = quizIds
        )
    }
}

/**
 * Informações do sistema de pontuação e gamificação do usuário.
 */
data class UserScoreInfo(
    val totalScore: Int = 0,
    val correctQuizCount: Int = 0,
    val completedActivitiesCount: Int = 0,
    val levelNumber: Int = 1,
    val levelTitlePt: String = "Aspirante a Dev",
    val levelTitleEn: String = "Aspiring Dev",
    val rankBadgePt: String = "Iniciante",
    val rankBadgeEn: String = "Starter",
    val currentLevelMin: Int = 0,
    val nextLevelTarget: Int = 250,
    val progressFraction: Float = 0f,
    val isMaxLevel: Boolean = false,
    val completedActivityIds: Set<String> = emptySet(),
    val completedQuizIds: Set<Int> = emptySet()
)

/**
 * Feedback estruturado gerado pela IA para o resumo semanal de estudos do usuário.
 */
data class WeeklyStudySummaryFeedback(
    val headline: String,
    val overallAssessment: String,
    val consistencyLabel: String,
    val strengths: List<String>,
    val growthAreas: List<String>,
    val nextWeekActionPlan: String,
    val recommendedTechId: String,
    val recommendedDailyTarget: Int,
    val encouragementQuote: String,
    val generatedAtMillis: Long = System.currentTimeMillis()
)

sealed interface WeeklySummaryUiState {
    data object Idle : WeeklySummaryUiState
    data object Loading : WeeklySummaryUiState
    data class Success(val feedback: WeeklyStudySummaryFeedback) : WeeklySummaryUiState
    data class Error(val message: String) : WeeklySummaryUiState
}

/**
 * Estado da transcrição de áudio via microfone com o modelo gemini-3.5-transcribe.
 */
sealed interface AudioTranscriptionUiState {
    data object Idle : AudioTranscriptionUiState
    data object Recording : AudioTranscriptionUiState
    data object Transcribing : AudioTranscriptionUiState
    data class Success(val transcript: String, val durationSeconds: Int) : AudioTranscriptionUiState
    data class Error(val message: String) : AudioTranscriptionUiState
}

/**
 * Resultado de execução de uma consulta SQL ao vivo no banco Room/SQLite.
 */
data class SqlQueryResult(
    val query: String,
    val columns: List<String> = emptyList(),
    val rows: List<List<String>> = emptyList(),
    val executionTimeMs: Long = 0L,
    val errorMessage: String? = null
)

enum class FontScalePreset(val scale: Float, val labelPt: String, val labelEn: String) {
    COMPACT(0.90f, "Compacto", "Compact"),
    STANDARD(1.00f, "Padrão", "Standard"),
    LARGE(1.15f, "Grande", "Large"),
    EXTRA_LARGE(1.30f, "Extra", "Extra");

    companion object {
        fun fromScale(value: Float): FontScalePreset =
            entries.minByOrNull { kotlin.math.abs(it.scale - value) } ?: STANDARD
    }
}

data class AccessibilitySettings(
    val fontScalePreset: FontScalePreset = FontScalePreset.STANDARD,
    val highContrastBorders: Boolean = false,
    val reduceMotion: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true
)

data class AppGeneralSettings(
    val dailyRemindersEnabled: Boolean = true,
    val idlePauseMinutes: Int = 2,
    val showCelebrationDialogs: Boolean = true,
    val showGentleRetryTips: Boolean = true,
    val showXpToastBanners: Boolean = true
)

data class UserFeedbackNote(
    val id: String,
    val category: String,
    val rating: Int,
    val message: String,
    val createdAtIso: String
)
