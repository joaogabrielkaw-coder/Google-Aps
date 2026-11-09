package com.example.data.repository

import android.util.Log
import com.example.data.local.UserBadgeDao
import com.example.data.local.UserBadgeEntity
import com.example.model.StudySession
import com.example.model.Technology
import com.example.model.UserScoreInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

private const val TAG = "UserBadgeRepository"

class UserBadgeRepository(
    private val badgeDao: UserBadgeDao
) {

    val allBadges: Flow<List<UserBadgeEntity>> = badgeDao.getAllBadges()
    val unlockedBadges: Flow<List<UserBadgeEntity>> = badgeDao.getUnlockedBadges()
    val unlockedCount: Flow<Int> = badgeDao.getUnlockedCount()
    val totalCount: Flow<Int> = badgeDao.getTotalCount()

    companion object {
        val DEFAULT_BADGES = listOf(
            UserBadgeEntity(
                id = "badge_python_master",
                titlePt = "Mestre Python",
                titleEn = "Python Master",
                descriptionPt = "Alcance 90+ XP na trilha prática e de quizzes de Python.",
                descriptionEn = "Reach 90+ XP in Python practical tracks and quizzes.",
                category = "TECH",
                iconKey = "python",
                maxProgress = 90,
                xpReward = 100
            ),
            UserBadgeEntity(
                id = "badge_night_owl",
                titlePt = "Coruja da Noite",
                titleEn = "Night Owl Learner",
                descriptionPt = "Conclua uma sessão de estudos no período noturno (a partir das 20h).",
                descriptionEn = "Complete a study session at night (from 8 PM onwards).",
                category = "TIME",
                iconKey = "night_owl",
                maxProgress = 1,
                xpReward = 50
            ),
            UserBadgeEntity(
                id = "badge_early_bird",
                titlePt = "Madrugador do Código",
                titleEn = "Early Bird Coder",
                descriptionPt = "Conclua uma sessão de estudos pela manhã (antes das 9h).",
                descriptionEn = "Complete a study session early in the morning (before 9 AM).",
                category = "TIME",
                iconKey = "clock",
                maxProgress = 1,
                xpReward = 50
            ),
            UserBadgeEntity(
                id = "badge_streak_3",
                titlePt = "Pioneiro da Sequência",
                titleEn = "Streak Pioneer",
                descriptionPt = "Mantenha uma sequência de 3 dias de estudo ativo.",
                descriptionEn = "Maintain an active 3-day study streak.",
                category = "STREAK",
                iconKey = "streak",
                maxProgress = 3,
                xpReward = 60
            ),
            UserBadgeEntity(
                id = "badge_streak_7",
                titlePt = "Campeão da Sequência",
                titleEn = "Streak Champion",
                descriptionPt = "Mantenha 7 dias consecutivos de dedicação diária.",
                descriptionEn = "Maintain a 7-day study streak.",
                category = "STREAK",
                iconKey = "fire",
                maxProgress = 7,
                xpReward = 150
            ),
            UserBadgeEntity(
                id = "badge_sql_wizard",
                titlePt = "Mago do SQL & Dados",
                titleEn = "SQL & Data Wizard",
                descriptionPt = "Alcance 90+ XP dominando SQL, MySQL ou PostgreSQL.",
                descriptionEn = "Reach 90+ XP mastering SQL, MySQL, or PostgreSQL.",
                category = "TECH",
                iconKey = "sql",
                maxProgress = 90,
                xpReward = 100
            ),
            UserBadgeEntity(
                id = "badge_code_crafter",
                titlePt = "Primeiro Código",
                titleEn = "First Code Craft",
                descriptionPt = "Conclua sua primeira atividade prática com validação.",
                descriptionEn = "Complete your first practical coding challenge.",
                category = "ACTIVITY",
                iconKey = "code",
                maxProgress = 1,
                xpReward = 50
            ),
            UserBadgeEntity(
                id = "badge_activity_5",
                titlePt = "Caçador de Desafios",
                titleEn = "Challenge Hunter",
                descriptionPt = "Conclua 5 atividades práticas de codificação no app.",
                descriptionEn = "Complete 5 practical coding challenges in the app.",
                category = "ACTIVITY",
                iconKey = "trophy",
                maxProgress = 5,
                xpReward = 120
            ),
            UserBadgeEntity(
                id = "badge_polyglot",
                titlePt = "Poliglota Tech",
                titleEn = "Tech Polyglot",
                descriptionPt = "Pontue em pelo menos 4 tecnologias diferentes no app.",
                descriptionEn = "Earn score in at least 4 different technologies.",
                category = "MILESTONE",
                iconKey = "star",
                maxProgress = 4,
                xpReward = 100
            ),
            UserBadgeEntity(
                id = "badge_prodigy_500",
                titlePt = "Prodígio Dev",
                titleEn = "Dev Prodigy",
                descriptionPt = "Acumule 500 ou mais pontos de XP totais.",
                descriptionEn = "Reach 500+ total XP points.",
                category = "MILESTONE",
                iconKey = "trophy",
                maxProgress = 500,
                xpReward = 200
            ),
            UserBadgeEntity(
                id = "badge_ai_collaborator",
                titlePt = "Parceiro da IA",
                titleEn = "AI Collaborator",
                descriptionPt = "Gere e salve um plano de estudos com a IA no calendário.",
                descriptionEn = "Generate and save an AI study routine to calendar.",
                category = "MILESTONE",
                iconKey = "ai",
                maxProgress = 1,
                xpReward = 75
            ),
            UserBadgeEntity(
                id = "badge_quiz_master",
                titlePt = "Mestre dos Quizzes",
                titleEn = "Quiz Master",
                descriptionPt = "Acerte 6 perguntas conceituais de quiz.",
                descriptionEn = "Correctly answer 6 tech quiz questions.",
                category = "ACTIVITY",
                iconKey = "star",
                maxProgress = 6,
                xpReward = 80
            )
        )
    }

    suspend fun initializeBadgesIfEmpty() = withContext(Dispatchers.IO) {
        try {
            badgeDao.insertInitialBadges(DEFAULT_BADGES)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize badges in Room", e)
        }
    }

    /**
     * Avalia todas as regras de desbloqueio de emblemas de acordo com o progresso
     * do usuário (streaks, sessões, horários, atividades e pontuação por tecnologia).
     * Retorna a lista de badges recém-desbloqueados (se houver).
     */
    suspend fun evaluateAndSyncBadges(
        streakDays: Int,
        completedSessions: List<StudySession>,
        scoreInfo: UserScoreInfo,
        techScores: Map<Technology, Int>,
        hasAiRoutine: Boolean
    ): List<UserBadgeEntity> = withContext(Dispatchers.IO) {
        val newlyUnlocked = mutableListOf<UserBadgeEntity>()
        val now = System.currentTimeMillis()

        // Garante que os emblemas existam no banco
        badgeDao.insertInitialBadges(DEFAULT_BADGES)

        // 1. Regras de Sequência (Streak)
        updateBadgeProgressIfChanged(
            id = "badge_streak_3",
            progress = streakDays.coerceAtMost(3),
            now = now,
            newlyUnlocked = newlyUnlocked
        )
        updateBadgeProgressIfChanged(
            id = "badge_streak_7",
            progress = streakDays.coerceAtMost(7),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        // 2. Horários das Sessões (Night Owl & Early Bird)
        var hasNightSession = false
        var hasMorningSession = false

        for (session in completedSessions) {
            // Verifica pelo horário formatado "HH:mm" ou pelo completedAtMillis
            val hourFromFormatted = session.timeFormatted.split(":").firstOrNull()?.toIntOrNull()
            val hourFromMillis = if (session.completedAtMillis > 0) {
                Instant.ofEpochMilli(session.completedAtMillis)
                    .atZone(ZoneId.systemDefault())
                    .hour
            } else null

            val hour = hourFromMillis ?: hourFromFormatted ?: 12

            if (hour >= 20 || hour < 4) {
                hasNightSession = true
            }
            if (hour in 4..8) {
                hasMorningSession = true
            }
        }

        updateBadgeProgressIfChanged(
            id = "badge_night_owl",
            progress = if (hasNightSession) 1 else 0,
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        updateBadgeProgressIfChanged(
            id = "badge_early_bird",
            progress = if (hasMorningSession) 1 else 0,
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        // 3. Regras de Tecnologias Específicas
        val pythonScore = techScores[Technology.PYTHON] ?: 0
        updateBadgeProgressIfChanged(
            id = "badge_python_master",
            progress = pythonScore.coerceAtMost(90),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        val sqlScore = maxOf(
            techScores[Technology.SQL] ?: 0,
            techScores[Technology.MYSQL] ?: 0,
            techScores[Technology.POSTGRESQL] ?: 0
        )
        updateBadgeProgressIfChanged(
            id = "badge_sql_wizard",
            progress = sqlScore.coerceAtMost(90),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        // 4. Marcos de Atividades e Quizzes
        updateBadgeProgressIfChanged(
            id = "badge_code_crafter",
            progress = scoreInfo.completedActivitiesCount.coerceAtMost(1),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        updateBadgeProgressIfChanged(
            id = "badge_activity_5",
            progress = scoreInfo.completedActivitiesCount.coerceAtMost(5),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        updateBadgeProgressIfChanged(
            id = "badge_quiz_master",
            progress = scoreInfo.correctQuizCount.coerceAtMost(6),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        // 5. Poliglota (Pelo menos 4 tecnologias pontuadas)
        val activeTechsCount = techScores.count { it.value > 0 }
        updateBadgeProgressIfChanged(
            id = "badge_polyglot",
            progress = activeTechsCount.coerceAtMost(4),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        // 6. Prodígio Dev (500+ XP)
        updateBadgeProgressIfChanged(
            id = "badge_prodigy_500",
            progress = scoreInfo.totalScore.coerceAtMost(500),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        // 7. Colaborador com IA
        val isAiCollaborator = hasAiRoutine || completedSessions.any { it.aiGenerated }
        updateBadgeProgressIfChanged(
            id = "badge_ai_collaborator",
            progress = if (isAiCollaborator) 1 else 0,
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        newlyUnlocked
    }

    private suspend fun updateBadgeProgressIfChanged(
        id: String,
        progress: Int,
        now: Long,
        newlyUnlocked: MutableList<UserBadgeEntity>
    ) {
        val existing = badgeDao.getBadgeById(id) ?: return
        val wasUnlocked = existing.isUnlocked
        val willUnlock = progress >= existing.maxProgress

        if (progress != existing.currentProgress || (!wasUnlocked && willUnlock)) {
            badgeDao.updateProgress(id = id, progress = progress, timestamp = now)
            if (!wasUnlocked && willUnlock) {
                val updated = existing.copy(
                    isUnlocked = true,
                    unlockedAtMillis = now,
                    currentProgress = existing.maxProgress
                )
                newlyUnlocked.add(updated)
            }
        }
    }
}
