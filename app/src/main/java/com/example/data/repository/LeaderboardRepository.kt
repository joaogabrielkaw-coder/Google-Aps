package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.LeaderboardEntry
import com.example.model.UserAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "LeaderboardRepo"

class LeaderboardRepository(context: Context) {

    private val db: FirebaseFirestore by lazy {
        val dbId = try {
            context.getString(R.string.firestore_database_id)
        } catch (e: Exception) {
            "ai-studio-android-devrouti-2b37712a-bcfc-487f-9b1b-917199bffc52"
        }
        FirebaseFirestore.getInstance(dbId)
    }

    // Benchmark community developers to populate the competitive leaderboard
    private val communityBenchDevs = listOf(
        LeaderboardEntry(
            userId = "comm_dev_1",
            displayName = "Lucas Moura",
            phoneNumber = "+5511988881234",
            authMethod = "google",
            totalScore = 840,
            levelNumber = 4,
            levelTitlePt = "Dev Sênior",
            levelTitleEn = "Senior Dev",
            streakDays = 12,
            badgesCount = 9
        ),
        LeaderboardEntry(
            userId = "comm_dev_2",
            displayName = "Beatriz Costa",
            phoneNumber = "+5521997775678",
            authMethod = "phone",
            totalScore = 690,
            levelNumber = 4,
            levelTitlePt = "Dev Sênior",
            levelTitleEn = "Senior Dev",
            streakDays = 9,
            badgesCount = 7
        ),
        LeaderboardEntry(
            userId = "comm_dev_3",
            displayName = "Carlos Eduardo",
            phoneNumber = "+5531996669012",
            authMethod = "google",
            totalScore = 480,
            levelNumber = 3,
            levelTitlePt = "Dev Pleno",
            levelTitleEn = "Mid-Level Dev",
            streakDays = 6,
            badgesCount = 5
        ),
        LeaderboardEntry(
            userId = "comm_dev_4",
            displayName = "Ana Clara Silva",
            phoneNumber = "+5541995553456",
            authMethod = "phone",
            totalScore = 320,
            levelNumber = 3,
            levelTitlePt = "Dev Pleno",
            levelTitleEn = "Mid-Level Dev",
            streakDays = 5,
            badgesCount = 4
        ),
        LeaderboardEntry(
            userId = "comm_dev_5",
            displayName = "Guilherme Santos",
            phoneNumber = "+5561994447890",
            authMethod = "google",
            totalScore = 180,
            levelNumber = 2,
            levelTitlePt = "Dev Júnior",
            levelTitleEn = "Junior Dev",
            streakDays = 3,
            badgesCount = 3
        ),
        LeaderboardEntry(
            userId = "comm_dev_6",
            displayName = "Mariana Rocha",
            phoneNumber = "+5581993332345",
            authMethod = "phone",
            totalScore = 90,
            levelNumber = 1,
            levelTitlePt = "Aspirante a Dev",
            levelTitleEn = "Aspiring Dev",
            streakDays = 2,
            badgesCount = 2
        )
    )

    /**
     * Salva ou atualiza a entrada do usuário autenticado no placar do Firestore
     */
    suspend fun syncUserToLeaderboard(account: UserAccount): Boolean = withContext(Dispatchers.IO) {
        if (account.userId.isBlank()) return@withContext false
        try {
            val payload = mapOf(
                "userId" to account.userId,
                "displayName" to account.displayName,
                "email" to account.email,
                "phoneNumber" to account.phoneNumber,
                "photoUrl" to account.photoUrl,
                "authProvider" to account.authMethod.name.lowercase(),
                "totalScore" to account.totalScore,
                "levelNumber" to account.levelNumber,
                "levelTitle" to account.levelTitlePt,
                "streakDays" to account.streakDays,
                "completedActivitiesCount" to account.completedActivitiesCount,
                "badgesCount" to account.badgesCount,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )

            // Salva na coleção pública do placar
            db.collection("leaderboard")
                .document(account.userId)
                .set(payload)
                .await()

            // Salva no perfil privado do usuário
            try {
                db.collection("users")
                    .document(account.userId)
                    .set(payload)
                    .await()
            } catch (e: Exception) {
                Log.w(TAG, "Note: users collection write skipped or non-fatal: ${e.message}")
            }

            Log.d(TAG, "User ${account.displayName} successfully synced to leaderboard with ${account.totalScore} XP")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user to Firestore leaderboard: ${e.message}", e)
            false
        }
    }

    /**
     * Observa a lista em tempo real do placar de desenvolvedores
     */
    fun observeLeaderboard(currentAccount: UserAccount?): Flow<List<LeaderboardEntry>> = callbackFlow {
        val currentUserId = currentAccount?.userId ?: ""

        val registration = db.collection("leaderboard")
            .orderBy("totalScore", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore leaderboard listener error, falling back to local merge", error)
                    val merged = buildMergedList(emptyList(), currentAccount)
                    trySend(merged)
                    return@addSnapshotListener
                }

                val remoteEntries = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val uid = doc.getString("userId") ?: doc.id
                        val name = doc.getString("displayName") ?: "Dev Anônimo"
                        val phone = doc.getString("phoneNumber") ?: ""
                        val photo = doc.getString("photoUrl") ?: ""
                        val authMethod = doc.getString("authProvider") ?: "google"
                        val score = (doc.get("totalScore") as? Number)?.toInt() ?: 0
                        val levelNum = (doc.get("levelNumber") as? Number)?.toInt() ?: 1
                        val levelTitle = doc.getString("levelTitle") ?: "Aspirante a Dev"
                        val streak = (doc.get("streakDays") as? Number)?.toInt() ?: 0
                        val badges = (doc.get("badgesCount") as? Number)?.toInt() ?: 0

                        LeaderboardEntry(
                            userId = uid,
                            displayName = name,
                            phoneNumber = phone,
                            photoUrl = photo,
                            authMethod = authMethod,
                            totalScore = score,
                            levelNumber = levelNum,
                            levelTitlePt = levelTitle,
                            levelTitleEn = levelTitle,
                            streakDays = streak,
                            badgesCount = badges,
                            isCurrentUser = (uid == currentUserId)
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                val merged = buildMergedList(remoteEntries, currentAccount)
                trySend(merged)
            }

        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    private fun buildMergedList(
        remoteEntries: List<LeaderboardEntry>,
        currentAccount: UserAccount?
    ): List<LeaderboardEntry> {
        val map = mutableMapOf<String, LeaderboardEntry>()

        // 1. Popula com os devs de comunidade benchmark
        communityBenchDevs.forEach { map[it.userId] = it }

        // 2. Mescla os registros remotos do Firestore
        remoteEntries.forEach { map[it.userId] = it }

        // 3. Adiciona o usuário logado com a pontuação atual
        if (currentAccount != null && currentAccount.isLoggedIn) {
            val userEntry = LeaderboardEntry(
                userId = currentAccount.userId,
                displayName = currentAccount.displayName,
                phoneNumber = currentAccount.phoneNumber,
                photoUrl = currentAccount.photoUrl,
                authMethod = currentAccount.authMethod.name.lowercase(),
                totalScore = currentAccount.totalScore,
                levelNumber = currentAccount.levelNumber,
                levelTitlePt = currentAccount.levelTitlePt,
                levelTitleEn = currentAccount.levelTitleEn,
                streakDays = currentAccount.streakDays,
                badgesCount = currentAccount.badgesCount,
                isCurrentUser = true
            )
            map[currentAccount.userId] = userEntry
        }

        // 4. Ordena por XP decrescente e atribui posições de ranking (1, 2, 3...)
        return map.values
            .sortedByDescending { it.totalScore }
            .mapIndexed { index, entry ->
                entry.copy(rank = index + 1)
            }
    }
}
