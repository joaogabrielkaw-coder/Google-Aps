package com.example.model

enum class Language(val code: String, val label: String, val flag: String) {
    PT("pt", "Português", "🇧🇷"),
    EN("en", "English", "🇺🇸")
}

enum class ThemePreference(val code: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light")
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
 * Informações do sistema de pontuação e gamificação do usuário.
 */
data class UserScoreInfo(
    val totalScore: Int = 0,
    val correctQuizCount: Int = 0,
    val completedActivitiesCount: Int = 0,
    val levelNumber: Int = 1,
    val levelTitlePt: String = "Aspirante a Dev",
    val levelTitleEn: String = "Aspiring Dev",
    val currentLevelMin: Int = 0,
    val nextLevelTarget: Int = 100,
    val progressFraction: Float = 0f,
    val completedActivityIds: Set<String> = emptySet(),
    val completedQuizIds: Set<Int> = emptySet()
)
