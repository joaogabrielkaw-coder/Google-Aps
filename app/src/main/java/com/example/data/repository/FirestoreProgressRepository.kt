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
     * Registra uma sessão rápida de estudos adicionando minutos e XP para a tecnologia hoje.
     */
    suspend fun logQuickStudySession(
        userId: String,
        tech: Technology,
        minutesToAdd: Int,
        xpToAdd: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val currentLocal = getLocalProgress(todayIso, tech.id)
        val newMinutes = currentLocal.minutesStudied + minutesToAdd
        val newXp = currentLocal.xpEarned + xpToAdd

        saveLocalProgress(
            todayIso = todayIso,
            techId = tech.id,
            challenges = currentLocal.completedChallengesCount,
            quizzes = currentLocal.completedQuizCount,
            xp = newXp,
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
                "minutesStudied" to FieldValue.increment(minutesToAdd.toLong()),
                "xpEarned" to FieldValue.increment(xpToAdd.toLong()),
                "updatedAt" to Timestamp.now()
            )

            docRef.set(updates, SetOptions.merge()).await()
            Log.d(TAG, "Logged quick study for ${tech.displayName}: +$minutesToAdd min, +$xpToAdd XP")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error logging quick study session to Firestore: ${e.message}", e)
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
