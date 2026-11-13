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
                id = "badge_js_ninja",
                titlePt = "Ninja do JavaScript & Web",
                titleEn = "JavaScript & Web Ninja",
                descriptionPt = "Alcance 90+ XP dominando JavaScript ou TypeScript.",
                descriptionEn = "Reach 90+ XP mastering JavaScript or TypeScript.",
                category = "TECH",
                iconKey = "code",
                maxProgress = 90,
                xpReward = 100
            ),
            UserBadgeEntity(
                id = "badge_kotlin_architect",
                titlePt = "Arquiteto Kotlin",
                titleEn = "Kotlin Architect",
                descriptionPt = "Alcance 90+ XP com Kotlin, Coroutines e Jetpack.",
                descriptionEn = "Reach 90+ XP with Kotlin, Coroutines & Jetpack.",
                category = "TECH",
                iconKey = "code",
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
            ),
            UserBadgeEntity(
                id = "badge_assessment_done",
                titlePt = "Diagnóstico Dev Inicial",
                titleEn = "Initial Dev Diagnostic",
                descriptionPt = "Concluiu a avaliação diagnóstica inicial definindo seu nível de desenvolvedor.",
                descriptionEn = "Completed the initial diagnostic assessment calibrating your dev level.",
                category = "MILESTONE",
                iconKey = "trophy",
                maxProgress = 1,
                xpReward = 100
            ),
            UserBadgeEntity(
                id = "badge_weekly_warrior",
                titlePt = "Guerreiro Semanal",
                titleEn = "Weekly Routine Warrior",
                descriptionPt = "Cumpriu rotina semanal com 5 ou mais dias de estudo ativo.",
                descriptionEn = "Fulfilled weekly routine with 5+ days of active study.",
                category = "STREAK",
                iconKey = "fire",
                maxProgress = 5,
                xpReward = 120
            ),
            UserBadgeEntity(
                id = "badge_monthly_master",
                titlePt = "Mestre Mensal da Rotina",
                titleEn = "Monthly Routine Champion",
                descriptionPt = "Manteve regularidade com 15 ou mais sessões concluídas no mês.",
                descriptionEn = "Maintained monthly consistency with 15+ completed study sessions.",
                category = "STREAK",
                iconKey = "star",
                maxProgress = 15,
                xpReward = 250
            ),
            UserBadgeEntity(
                id = "badge_challenges_10",
                titlePt = "10 Desafios Superados",
                titleEn = "10 Challenges Conquered",
                descriptionPt = "Completou 10 atividades práticas de código no app.",
                descriptionEn = "Completed 10 practical coding challenges in the app.",
                category = "ACTIVITY",
                iconKey = "code",
                maxProgress = 10,
                xpReward = 150
            ),
            UserBadgeEntity(
                id = "badge_challenges_25",
                titlePt = "25 Desafios de Código",
                titleEn = "25 Coding Challenges",
                descriptionPt = "Completou 25 atividades práticas de codificação no app.",
                descriptionEn = "Completed 25 practical coding challenges in the app.",
                category = "ACTIVITY",
                iconKey = "trophy",
                maxProgress = 25,
                xpReward = 300
            ),
            UserBadgeEntity(
                id = "badge_screen_time_1h",
                titlePt = "1 Hora em Foco",
                titleEn = "1 Hour in Focus",
                descriptionPt = "Acumulou 60 minutos de tempo de tela e estudo prático no app.",
                descriptionEn = "Accumulated 60 minutes of active screen time & study in the app.",
                category = "TIME",
                iconKey = "clock",
                maxProgress = 60,
                xpReward = 80
            ),
            UserBadgeEntity(
                id = "badge_screen_time_3h",
                titlePt = "3 Horas de Imersão",
                titleEn = "3 Hours Immersive Study",
                descriptionPt = "Acumulou 180 minutos de imersão de tela e prática ativa no app.",
                descriptionEn = "Accumulated 180 minutes of immersive screen time in the app.",
                category = "TIME",
                iconKey = "clock",
                maxProgress = 180,
                xpReward = 180
            ),
            UserBadgeEntity(
                id = "badge_screen_time_10h",
                titlePt = "10 Horas de Maestria",
                titleEn = "10 Hours Dev Mastery",
                descriptionPt = "Acumulou 600 minutos de dedicação e tela ativa estudando código.",
                descriptionEn = "Accumulated 600 minutes of active study & practice in the app.",
                category = "TIME",
                iconKey = "trophy",
                maxProgress = 600,
                xpReward = 400
            )
        ) + Technology.entries.flatMap { tech ->
            listOf(
                UserBadgeEntity(
                    id = "badge_daily_${tech.id}_bronze",
                    titlePt = "Selo Bronze: ${tech.displayName}",
                    titleEn = "Bronze Seal: ${tech.displayName}",
                    descriptionPt = "Complete 3 desafios práticos de ${tech.displayName} em um único dia.",
                    descriptionEn = "Complete 3 ${tech.displayName} practical challenges in a single day.",
                    category = "DAILY_TECH",
                    iconKey = "daily_${tech.id}_bronze",
                    maxProgress = 3,
                    xpReward = 60
                ),
                UserBadgeEntity(
                    id = "badge_daily_${tech.id}_silver",
                    titlePt = "Selo Prata: ${tech.displayName}",
                    titleEn = "Silver Seal: ${tech.displayName}",
                    descriptionPt = "Complete 5 desafios práticos de ${tech.displayName} em um único dia.",
                    descriptionEn = "Complete 5 ${tech.displayName} practical challenges in a single day.",
                    category = "DAILY_TECH",
                    iconKey = "daily_${tech.id}_silver",
                    maxProgress = 5,
                    xpReward = 120
                ),
                UserBadgeEntity(
                    id = "badge_daily_${tech.id}_gold",
                    titlePt = "Selo Ouro: ${tech.displayName}",
                    titleEn = "Gold Seal: ${tech.displayName}",
                    descriptionPt = "Complete 10 desafios práticos de ${tech.displayName} em um único dia.",
                    descriptionEn = "Complete 10 ${tech.displayName} practical challenges in a single day.",
                    category = "DAILY_TECH",
                    iconKey = "daily_${tech.id}_gold",
                    maxProgress = 10,
                    xpReward = 250
                )
            )
        }
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
        hasAiRoutine: Boolean,
        screenTimeMinutes: Int = 0,
        isAssessmentCompleted: Boolean = false,
        weeklyDaysCount: Int = 0,
        monthlySessionsCount: Int = 0,
        dailyChallengeCountsByTech: Map<Technology, Int> = emptyMap()
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

        val jsScore = maxOf(
            techScores[Technology.JAVASCRIPT] ?: 0,
            techScores[Technology.TYPESCRIPT] ?: 0
        )
        updateBadgeProgressIfChanged(
            id = "badge_js_ninja",
            progress = jsScore.coerceAtMost(90),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        val kotlinScore = techScores[Technology.KOTLIN] ?: 0
        updateBadgeProgressIfChanged(
            id = "badge_kotlin_architect",
            progress = kotlinScore.coerceAtMost(90),
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
            id = "badge_challenges_10",
            progress = scoreInfo.completedActivitiesCount.coerceAtMost(10),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        updateBadgeProgressIfChanged(
            id = "badge_challenges_25",
            progress = scoreInfo.completedActivitiesCount.coerceAtMost(25),
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

        // 8. Diagnóstico Inicial Concluído
        updateBadgeProgressIfChanged(
            id = "badge_assessment_done",
            progress = if (isAssessmentCompleted) 1 else 0,
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        // 9. Rotinas Semanal e Mensal
        val effectiveWeeklyDays = maxOf(weeklyDaysCount, streakDays.coerceAtMost(7))
        updateBadgeProgressIfChanged(
            id = "badge_weekly_warrior",
            progress = effectiveWeeklyDays.coerceAtMost(5),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        val effectiveMonthlySessions = maxOf(monthlySessionsCount, completedSessions.size)
        updateBadgeProgressIfChanged(
            id = "badge_monthly_master",
            progress = effectiveMonthlySessions.coerceAtMost(15),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        // 10. Tempo de Tela e Estudo Ativo
        updateBadgeProgressIfChanged(
            id = "badge_screen_time_1h",
            progress = screenTimeMinutes.coerceAtMost(60),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        updateBadgeProgressIfChanged(
            id = "badge_screen_time_3h",
            progress = screenTimeMinutes.coerceAtMost(180),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        updateBadgeProgressIfChanged(
            id = "badge_screen_time_10h",
            progress = screenTimeMinutes.coerceAtMost(600),
            now = now,
            newlyUnlocked = newlyUnlocked
        )

        // 11. Selos Diários por Linguagem (Bronze: 3/dia, Prata: 5/dia, Ouro: 10/dia)
        Technology.entries.forEach { tech ->
            val dailyCount = dailyChallengeCountsByTech[tech] ?: 0
            updateBadgeProgressIfChanged(
                id = "badge_daily_${tech.id}_bronze",
                progress = dailyCount.coerceAtMost(3),
                now = now,
                newlyUnlocked = newlyUnlocked
            )
            updateBadgeProgressIfChanged(
                id = "badge_daily_${tech.id}_silver",
                progress = dailyCount.coerceAtMost(5),
                now = now,
                newlyUnlocked = newlyUnlocked
            )
            updateBadgeProgressIfChanged(
                id = "badge_daily_${tech.id}_gold",
                progress = dailyCount.coerceAtMost(10),
                now = now,
                newlyUnlocked = newlyUnlocked
            )
        }

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
        if (wasUnlocked) {
            if (existing.currentProgress < existing.maxProgress) {
                badgeDao.updateProgress(id = id, progress = existing.maxProgress, timestamp = now)
            }
            return
        }

        val clampedProgress = progress.coerceAtMost(existing.maxProgress)
        val willUnlock = clampedProgress >= existing.maxProgress

        if (clampedProgress != existing.currentProgress || willUnlock) {
            badgeDao.updateProgress(id = id, progress = clampedProgress, timestamp = now)
            if (willUnlock) {
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
