package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.Technology
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "ProgressRepo"

data class LanguageDailyProgress(
    val techId: String = "",
    val techName: String = "",
    val dateIso: String = "",
    val completedChallengesCount: Int = 0,
    val completedQuizCount: Int = 0,
    val minutesStudied: Int = 0,
    val xpEarned: Int = 0,
    val isSyncedToCloud: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class DailyLanguageChallengeSummary(
    val dateIso: String,
    val countsByTech: Map<Technology, Int>
) {
    val totalChallenges: Int get() = countsByTech.values.sum()
}

class FirestoreProgressRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("devroutine_daily_progress_prefs", Context.MODE_PRIVATE)

    private val db: FirebaseFirestore by lazy {
        val dbId = try {
            context.getString(R.string.firestore_database_id)
        } catch (e: Exception) {
            "ai-studio-android-devrouti-2b37712a-bcfc-487f-9b1b-917199bffc52"
        }
        FirebaseFirestore.getInstance(dbId)
    }

    /**
     * Salva o progresso diário de estudos da linguagem especificada no Firestore e no cache local.
     */
    suspend fun recordLanguageProgress(
        userId: String,
        tech: Technology,
        completedChallenges: Int,
        completedQuizzes: Int,
        xpEarned: Int,
        minutesStudied: Int = 0
    ): Boolean = withContext(Dispatchers.IO) {
        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val docId = "${todayIso}_${tech.id}"

        // Save local backup first
        saveLocalProgress(todayIso, tech.id, completedChallenges, completedQuizzes, xpEarned, minutesStudied)

        if (userId.isBlank()) {
            return@withContext true
        }

        try {
            val payload = mapOf(
                "userId" to userId,
                "techId" to tech.id,
                "techName" to tech.displayName,
                "dateIso" to todayIso,
                "completedChallengesCount" to completedChallenges,
                "completedQuizCount" to completedQuizzes,
                "xpEarned" to xpEarned,
                "minutesStudied" to minutesStudied,
                "updatedAt" to Timestamp.now()
            )

            db.collection("users")
                .document(userId)
                .collection("daily_progress")
                .document(docId)
                .set(payload, SetOptions.merge())
                .await()

            Log.d(TAG, "Daily study progress saved for ${tech.displayName} on $todayIso (XP: $xpEarned)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving daily study progress to Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Registra a conclusão de um desafio prático específico feito HOJE na linguagem informada,
     * mantendo a contagem diária exata por linguagem e atualizando o Cloud Firestore.
     */
    suspend fun recordCompletedDailyChallenge(
        userId: String,
        tech: Technology,
        activityId: String,
        xpEarned: Int,
        completedQuizzes: Int
    ): Int = withContext(Dispatchers.IO) {
        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val setKey = "${todayIso}_${tech.id}_challenge_ids"
        val currentSet = prefs.getStringSet(setKey, emptySet())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(activityId)

        val dailyCount = currentSet.size
        val currentLocal = getLocalProgress(todayIso, tech.id)
        val bestKey = "best_daily_challenges_${tech.id}"
        val bestCount = maxOf(prefs.getInt(bestKey, 0), dailyCount)

        prefs.edit()
            .putStringSet(setKey, currentSet)
            .putInt(bestKey, bestCount)
            .apply()

        saveLocalProgress(
            todayIso = todayIso,
            techId = tech.id,
            challenges = dailyCount,
            quizzes = maxOf(currentLocal.completedQuizCount, completedQuizzes),
            xp = maxOf(currentLocal.xpEarned, xpEarned),
            minutes = currentLocal.minutesStudied
        )

        if (userId.isNotBlank()) {
            val docId = "${todayIso}_${tech.id}"
            try {
                val payload = mapOf(
                    "userId" to userId,
                    "techId" to tech.id,
                    "techName" to tech.displayName,
                    "dateIso" to todayIso,
                    "completedChallengesCount" to dailyCount,
                    "dailyBestChallenges" to bestCount,
                    "completedChallengeIdsToday" to currentSet.toList(),
                    "completedQuizCount" to maxOf(currentLocal.completedQuizCount, completedQuizzes),
                    "xpEarned" to maxOf(currentLocal.xpEarned, xpEarned),
                    "updatedAt" to Timestamp.now()
                )
                db.collection("users")
                    .document(userId)
                    .collection("daily_progress")
                    .document(docId)
                    .set(payload, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.e(TAG, "Error saving daily challenge seal progress: ${e.message}", e)
            }
        }
        dailyCount
    }

    fun getTodayChallengeCountsMap(): Map<Technology, Int> {
        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return Technology.entries.associateWith { tech ->
            val setSize = prefs.getStringSet("${todayIso}_${tech.id}_challenge_ids", null)?.size ?: 0
            val savedInt = prefs.getInt("${todayIso}_${tech.id}_challenges", 0)
            maxOf(setSize, savedInt)
        }
    }

    /**
     * Contabiliza automaticamente 1 minuto real de tempo de uso ativo no app para a tecnologia em foco.
     * Não concede XP arbitrário manual (apenas atualiza o tempo real de tela).
     */
    suspend fun recordRealScreenTimeMinute(
        userId: String,
        tech: Technology
    ): Boolean = withContext(Dispatchers.IO) {
        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val currentLocal = getLocalProgress(todayIso, tech.id)
        val newMinutes = currentLocal.minutesStudied + 1

        saveLocalProgress(
            todayIso = todayIso,
            techId = tech.id,
            challenges = currentLocal.completedChallengesCount,
            quizzes = currentLocal.completedQuizCount,
            xp = currentLocal.xpEarned,
            minutes = newMinutes
        )

        if (userId.isBlank()) return@withContext true

        val docId = "${todayIso}_${tech.id}"
        try {
            val docRef = db.collection("users")
                .document(userId)
                .collection("daily_progress")
                .document(docId)

            val updates = mapOf(
                "userId" to userId,
                "techId" to tech.id,
                "techName" to tech.displayName,
                "dateIso" to todayIso,
                "minutesStudied" to FieldValue.increment(1L),
                "updatedAt" to Timestamp.now()
            )

            docRef.set(updates, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error recording real screen time minute to Firestore: ${e.message}", e)
            false
        }
    }

    /**
     * Observa o progresso diário de todas as linguagens para hoje.
     */
    fun observeAllLanguagesTodayProgress(userId: String): Flow<Map<String, LanguageDailyProgress>> = callbackFlow {
        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        // Initial emission from local cache for immediate responsiveness
        val localMap = Technology.values().associate { tech ->
            tech.id to getLocalProgress(todayIso, tech.id)
        }
        trySend(localMap)

        if (userId.isBlank()) {
            awaitClose { }
            return@callbackFlow
        }

        val listener = db.collection("users")
            .document(userId)
            .collection("daily_progress")
            .whereEqualTo("dateIso", todayIso)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing all daily progress: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val resultMap = localMap.toMutableMap()
                    for (doc in snapshot.documents) {
                        val techId = doc.getString("techId") ?: continue
                        val techName = doc.getString("techName") ?: techId
                        val challenges = (doc.get("completedChallengesCount") as? Number)?.toInt() ?: 0
                        val quizzes = (doc.get("completedQuizCount") as? Number)?.toInt() ?: 0
                        val xp = (doc.get("xpEarned") as? Number)?.toInt() ?: 0
                        val minutes = (doc.get("minutesStudied") as? Number)?.toInt() ?: 0

                        resultMap[techId] = LanguageDailyProgress(
                            techId = techId,
                            techName = techName,
                            dateIso = todayIso,
                            completedChallengesCount = challenges,
                            completedQuizCount = quizzes,
                            minutesStudied = minutes,
                            xpEarned = xp,
                            isSyncedToCloud = true
                        )
                    }
                    trySend(resultMap)
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    fun getLocalWeeklyChallengeHistory(
        days: Int = 7,
        endDate: LocalDate = LocalDate.now()
    ): List<DailyLanguageChallengeSummary> {
        val safeDays = days.coerceAtLeast(1)
        val startDate = endDate.minusDays((safeDays - 1).toLong())
        return (0 until safeDays).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val dateIso = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val counts = Technology.entries.associateWith { tech ->
                val setSize = prefs.getStringSet("${dateIso}_${tech.id}_challenge_ids", null)?.size ?: 0
                val savedInt = prefs.getInt("${dateIso}_${tech.id}_challenges", 0)
                maxOf(setSize, savedInt)
            }
            DailyLanguageChallengeSummary(dateIso = dateIso, countsByTech = counts)
        }
    }

    /**
     * Observa o histórico dos últimos 7 dias de desafios concluídos por linguagem,
     * combinando dados locais imediatos com registros do Cloud Firestore.
     */
    fun observeWeeklyChallengeHistory(
        userId: String,
        days: Int = 7
    ): Flow<List<DailyLanguageChallengeSummary>> = callbackFlow {
        val today = LocalDate.now()
        val safeDays = days.coerceAtLeast(1)
        val startDateIso = today.minusDays((safeDays - 1).toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE)

        val initialLocal = getLocalWeeklyChallengeHistory(safeDays, today)
        trySend(initialLocal)

        if (userId.isBlank()) {
            awaitClose { }
            return@callbackFlow
        }

        val listener = db.collection("users")
            .document(userId)
            .collection("daily_progress")
            .whereGreaterThanOrEqualTo("dateIso", startDateIso)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing weekly challenge history: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val base = getLocalWeeklyChallengeHistory(safeDays, LocalDate.now())
                        .associate { it.dateIso to it.countsByTech.toMutableMap() }
                        .toMutableMap()

                    for (doc in snapshot.documents) {
                        val dateIso = doc.getString("dateIso") ?: continue
                        val techId = doc.getString("techId") ?: continue
                        val tech = Technology.fromId(techId) ?: continue
                        val dayMap = base[dateIso] ?: continue
                        val remoteCount = (doc.get("completedChallengesCount") as? Number)?.toInt() ?: 0
                        val existing = dayMap[tech] ?: 0
                        dayMap[tech] = maxOf(existing, remoteCount)
                    }

                    val merged = base.entries
                        .sortedBy { it.key }
                        .map { (dateIso, counts) ->
                            DailyLanguageChallengeSummary(dateIso = dateIso, countsByTech = counts)
                        }
                    trySend(merged)
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    /**
     * Observa o progresso diário da linguagem selecionada para o usuário autenticado.
     */
    fun observeLanguageDailyProgress(userId: String, tech: Technology): Flow<LanguageDailyProgress?> = callbackFlow {
        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val docId = "${todayIso}_${tech.id}"

        // Emit local progress immediately
        val initialLocal = getLocalProgress(todayIso, tech.id)
        trySend(initialLocal)

        if (userId.isBlank()) {
            awaitClose { }
            return@callbackFlow
        }

        val listener = db.collection("users")
            .document(userId)
            .collection("daily_progress")
            .document(docId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to daily progress: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val challenges = (snapshot.get("completedChallengesCount") as? Number)?.toInt() ?: 0
                    val quizzes = (snapshot.get("completedQuizCount") as? Number)?.toInt() ?: 0
                    val xp = (snapshot.get("xpEarned") as? Number)?.toInt() ?: 0
                    val minutes = (snapshot.get("minutesStudied") as? Number)?.toInt() ?: 0
                    trySend(
                        LanguageDailyProgress(
                            techId = tech.id,
                            techName = tech.displayName,
                            dateIso = todayIso,
                            completedChallengesCount = challenges,
                            completedQuizCount = quizzes,
                            minutesStudied = minutes,
                            xpEarned = xp,
                            isSyncedToCloud = true
                        )
                    )
                } else {
                    trySend(initialLocal)
                }
            }

        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)

    private fun saveLocalProgress(
        todayIso: String,
        techId: String,
        challenges: Int,
        quizzes: Int,
        xp: Int,
        minutes: Int
    ) {
        prefs.edit()
            .putInt("${todayIso}_${techId}_challenges", challenges)
            .putInt("${todayIso}_${techId}_quizzes", quizzes)
            .putInt("${todayIso}_${techId}_xp", xp)
            .putInt("${todayIso}_${techId}_minutes", minutes)
            .apply()
    }

    fun getLocalProgress(todayIso: String, techId: String): LanguageDailyProgress {
        val challenges = prefs.getInt("${todayIso}_${techId}_challenges", 0)
        val quizzes = prefs.getInt("${todayIso}_${techId}_quizzes", 0)
        val xp = prefs.getInt("${todayIso}_${techId}_xp", 0)
        val minutes = prefs.getInt("${todayIso}_${techId}_minutes", 0)
        val tech = Technology.fromId(techId)

        return LanguageDailyProgress(
            techId = techId,
            techName = tech?.displayName ?: techId,
            dateIso = todayIso,
            completedChallengesCount = challenges,
            completedQuizCount = quizzes,
            minutesStudied = minutes,
            xpEarned = xp,
            isSyncedToCloud = false
        )
    }
}
