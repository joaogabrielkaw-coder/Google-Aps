package com.example.model

import com.example.data.challenges.TechChallengeBank
import com.example.data.repository.TechContentRepository
import com.example.data.repository.TechCourseModulesRepository
import kotlin.math.roundToInt

/**
 * Resumo consolidado de tarefas de estudo concluídas para uma linguagem ou banco de dados.
 */
data class LanguageTaskProgressSummary(
    val tech: Technology,
    val completedLessons: Int,
    val totalLessons: Int,
    val completedChallenges: Int,
    val totalChallenges: Int,
    val completedQuizzes: Int,
    val totalQuizzes: Int,
    val completedSessions: Int,
    val totalSessions: Int
) {
    val completedTasks: Int
        get() = completedLessons + completedChallenges + completedQuizzes + completedSessions

    val totalTasks: Int
        get() = (totalLessons + totalChallenges + totalQuizzes + totalSessions).coerceAtLeast(1)

    val progressFraction: Float
        get() = (completedTasks.toFloat() / totalTasks.toFloat()).coerceIn(0f, 1f)

    val progressPercent: Int
        get() = when {
            completedTasks <= 0 -> 0
            completedTasks >= totalTasks -> 100
            else -> (progressFraction * 100f).roundToInt().coerceIn(1, 99)
        }

    val isStarted: Boolean
        get() = completedTasks > 0

    val isCompletedAll: Boolean
        get() = completedTasks >= totalTasks
}

data class OverallLanguageTaskProgress(
    val completedTasks: Int,
    val totalTasks: Int,
    val startedLanguagesCount: Int,
    val totalLanguagesCount: Int
) {
    val progressFraction: Float
        get() = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks.toFloat()).coerceIn(0f, 1f) else 0f

    val progressPercent: Int
        get() = when {
            completedTasks <= 0 || totalTasks <= 0 -> 0
            completedTasks >= totalTasks -> 100
            else -> (progressFraction * 100f).roundToInt().coerceIn(1, 99)
        }
}

object LanguageTaskProgressCalculator {

    fun computeForTech(
        tech: Technology,
        completedActivityIds: Set<String> = emptySet(),
        completedQuizIds: Set<Int> = emptySet(),
        completedLessonIds: Set<String> = emptySet(),
        sessions: List<StudySession> = emptyList()
    ): LanguageTaskProgressSummary {
        val lessonIds = TechCourseModulesRepository.getAllLessonIds(tech)
        val completedLessons = lessonIds.count { it in completedLessonIds }

        val challenges = TechChallengeBank.getChallenges(tech)
        val completedChallenges = challenges.count { it.id in completedActivityIds }

        val quizzes = TechContentRepository.getQuizQuestions(tech)
        val completedQuizzes = quizzes.count { it.id in completedQuizIds }

        val techSessions = sessions.filter { it.tech == tech }
        val completedSessions = techSessions.count { it.isCompleted }

        return LanguageTaskProgressSummary(
            tech = tech,
            completedLessons = completedLessons,
            totalLessons = lessonIds.size,
            completedChallenges = completedChallenges,
            totalChallenges = challenges.size,
            completedQuizzes = completedQuizzes,
            totalQuizzes = quizzes.size,
            completedSessions = completedSessions,
            totalSessions = techSessions.size
        )
    }

    fun computeAll(
        completedActivityIds: Set<String> = emptySet(),
        completedQuizIds: Set<Int> = emptySet(),
        completedLessonIds: Set<String> = emptySet(),
        sessions: List<StudySession> = emptyList()
    ): List<LanguageTaskProgressSummary> {
        return Technology.entries.map { tech ->
            computeForTech(
                tech = tech,
                completedActivityIds = completedActivityIds,
                completedQuizIds = completedQuizIds,
                completedLessonIds = completedLessonIds,
                sessions = sessions
            )
        }
    }

    fun computeOverall(summaries: List<LanguageTaskProgressSummary>): OverallLanguageTaskProgress {
        val completed = summaries.sumOf { it.completedTasks }
        val total = summaries.sumOf { it.totalTasks }
        val started = summaries.count { it.isStarted }
        return OverallLanguageTaskProgress(
            completedTasks = completed,
            totalTasks = total,
            startedLanguagesCount = started,
            totalLanguagesCount = summaries.size
        )
    }
}
