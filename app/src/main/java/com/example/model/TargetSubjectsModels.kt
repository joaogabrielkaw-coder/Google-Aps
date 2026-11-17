package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class LearningTrackOption(
    val id: String,
    val labelPt: String,
    val labelEn: String,
    val subtitlePt: String,
    val subtitleEn: String,
    val presetTechs: List<Technology>
) {
    BACKEND_DATA(
        id = "backend_data",
        labelPt = "Backend & Dados",
        labelEn = "Backend & Data",
        subtitlePt = "Python, SQL, PostgreSQL & Go",
        subtitleEn = "Python, SQL, PostgreSQL & Go",
        presetTechs = listOf(Technology.PYTHON, Technology.SQL, Technology.POSTGRESQL, Technology.GO)
    ),
    FRONTEND_WEB(
        id = "frontend_web",
        labelPt = "Web Full-Stack",
        labelEn = "Full-Stack Web",
        subtitlePt = "HTML5, CSS3, JavaScript & TypeScript",
        subtitleEn = "HTML5, CSS3, JavaScript & TypeScript",
        presetTechs = listOf(Technology.HTML, Technology.CSS, Technology.JAVASCRIPT, Technology.TYPESCRIPT)
    ),
    MOBILE_CLOUD(
        id = "mobile_cloud",
        labelPt = "Mobile & APIs",
        labelEn = "Mobile & APIs",
        subtitlePt = "Kotlin, Java, SQL & MongoDB",
        subtitleEn = "Kotlin, Java, SQL & MongoDB",
        presetTechs = listOf(Technology.KOTLIN, Technology.JAVA, Technology.SQL, Technology.MONGODB)
    ),
    BALANCED(
        id = "balanced",
        labelPt = "Base Completa",
        labelEn = "Core Foundation",
        subtitlePt = "Python, SQL, HTML5, CSS3 & JavaScript",
        subtitleEn = "Python, SQL, HTML5, CSS3 & JavaScript",
        presetTechs = listOf(Technology.PYTHON, Technology.SQL, Technology.HTML, Technology.CSS, Technology.JAVASCRIPT)
    ),
    CUSTOM(
        id = "custom",
        labelPt = "Personalizado",
        labelEn = "Custom Mix",
        subtitlePt = "Sua combinação sob medida",
        subtitleEn = "Your custom subject mix",
        presetTechs = emptyList()
    );

    fun label(language: Language): String = if (language == Language.PT) labelPt else labelEn
    fun subtitle(language: Language): String = if (language == Language.PT) subtitlePt else subtitleEn

    companion object {
        fun fromId(raw: String?): LearningTrackOption =
            entries.firstOrNull { it.id.equals(raw?.trim(), ignoreCase = true) } ?: CUSTOM
    }
}

enum class SubjectCategoryFilter(
    val id: String,
    val labelPt: String,
    val labelEn: String
) {
    ALL("all", "Todas (12)", "All (12)"),
    LANGUAGES("languages", "Linguagens", "Languages"),
    WEB_FRONTEND("web", "Web & Frontend", "Web & Frontend"),
    DATABASES("databases", "Bancos de Dados", "Databases"),
    MOBILE_CLOUD("mobile_cloud", "Mobile & Cloud", "Mobile & Cloud");

    fun label(language: Language): String = if (language == Language.PT) labelPt else labelEn

    fun matches(tech: Technology): Boolean = when (this) {
        ALL -> true
        LANGUAGES -> tech in listOf(Technology.PYTHON, Technology.JAVA, Technology.JAVASCRIPT, Technology.TYPESCRIPT, Technology.KOTLIN, Technology.GO)
        WEB_FRONTEND -> tech in listOf(Technology.HTML, Technology.CSS, Technology.JAVASCRIPT, Technology.TYPESCRIPT)
        DATABASES -> tech in listOf(Technology.SQL, Technology.MYSQL, Technology.POSTGRESQL, Technology.MONGODB)
        MOBILE_CLOUD -> tech in listOf(Technology.KOTLIN, Technology.JAVA, Technology.GO, Technology.PYTHON)
    }
}

@Keep
@IgnoreExtraProperties
data class TargetSubjectsPreference(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val subjectIds: List<String> = emptyList(),
    val dailyGoalMinutes: Int = 30,
    val learningTrack: String = LearningTrackOption.CUSTOM.id,
    val studyGoalNote: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
) {
    fun selectedTechnologies(): List<Technology> =
        subjectIds.mapNotNull { id -> Technology.entries.firstOrNull { it.id == id } }

    fun toCreateMap(ownerUid: String): Map<String, Any> {
        val cleanIds = TargetSubjectsCatalog.normalizeSubjectIds(subjectIds)
        val cleanNote = TargetSubjectsCatalog.normalizeGoalNote(studyGoalNote)
        val map = mutableMapOf<String, Any>(
            "userId" to ownerUid,
            "subjectIds" to cleanIds,
            "dailyGoalMinutes" to TargetSubjectsCatalog.normalizeDailyGoalMinutes(dailyGoalMinutes),
            "learningTrack" to TargetSubjectsCatalog.normalizeTrackId(learningTrack),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (cleanNote.isNotEmpty()) {
            map["studyGoalNote"] = cleanNote
        }
        return map
    }

    fun toUpdateMap(): Map<String, Any> {
        val cleanIds = TargetSubjectsCatalog.normalizeSubjectIds(subjectIds)
        val cleanNote = TargetSubjectsCatalog.normalizeGoalNote(studyGoalNote)
        return mapOf(
            "subjectIds" to cleanIds,
            "dailyGoalMinutes" to TargetSubjectsCatalog.normalizeDailyGoalMinutes(dailyGoalMinutes),
            "learningTrack" to TargetSubjectsCatalog.normalizeTrackId(learningTrack),
            "studyGoalNote" to cleanNote,
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

object TargetSubjectsCatalog {
    const val MIN_DAILY_GOAL_MINUTES = 10
    const val MAX_DAILY_GOAL_MINUTES = 240
    const val MAX_NOTE_LENGTH = 200
    val DAILY_GOAL_OPTIONS = listOf(15, 30, 45, 60, 90)

    val ALLOWED_SUBJECT_IDS: Set<String> = Technology.entries.map { it.id }.toSet()

    fun normalizeSubjectIds(rawIds: Collection<String>): List<String> {
        val allowedSet = rawIds
            .map { it.trim().lowercase() }
            .filter { it in ALLOWED_SUBJECT_IDS }
            .toSet()
        return Technology.entries
            .map { it.id }
            .filter { it in allowedSet }
            .take(Technology.entries.size)
    }

    fun normalizeDailyGoalMinutes(minutes: Int): Int =
        minutes.coerceIn(MIN_DAILY_GOAL_MINUTES, MAX_DAILY_GOAL_MINUTES)

    fun normalizeGoalNote(note: String): String =
        note.trim().take(MAX_NOTE_LENGTH)

    fun normalizeTrackId(trackId: String): String =
        LearningTrackOption.fromId(trackId).id

    fun inferMatchingTrack(selectedIds: Collection<String>): LearningTrackOption {
        val normalized = normalizeSubjectIds(selectedIds).toSet()
        return LearningTrackOption.entries
            .filter { it != LearningTrackOption.CUSTOM }
            .firstOrNull { option ->
                option.presetTechs.map { it.id }.toSet() == normalized
            } ?: LearningTrackOption.CUSTOM
    }
}

sealed interface TargetSubjectsUiState {
    data object Idle : TargetSubjectsUiState
    data object Saving : TargetSubjectsUiState
    data class Success(val message: String) : TargetSubjectsUiState
    data class Error(val message: String) : TargetSubjectsUiState
}
