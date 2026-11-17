package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.LearningTrackOption
import com.example.model.TargetSubjectsCatalog
import com.example.model.TargetSubjectsPreference
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "TargetSubjectsRepo"

class TargetSubjectsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val appContext: Context
) {
    constructor(context: Context, auth: FirebaseAuth) : this(
        db = FirebaseFirestore.getInstance(context.applicationContext.getString(R.string.firestore_database_id)),
        auth = auth,
        appContext = context.applicationContext
    )

    private val localPrefs by lazy {
        appContext.getSharedPreferences("devroutine_target_subjects_prefs", Context.MODE_PRIVATE)
    }

    private fun requireFirebaseUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Google Sign-In required to access Firestore target subjects.")
    }

    private fun documentPath(userId: String): String =
        "users/$userId/$PREFERENCES_COLLECTION/$TARGET_SUBJECTS_DOC_ID"

    private fun docRef(userId: String) = db
        .collection("users")
        .document(userId)
        .collection(PREFERENCES_COLLECTION)
        .document(TARGET_SUBJECTS_DOC_ID)

    fun observeTargetSubjects(): Flow<TargetSubjectsPreference?> = flow {
        val uid = requireFirebaseUserId()
        val path = documentPath(uid)

        // Emit local cache immediately if available while cloud snapshot connects
        loadLocalPreferences(uid)?.let { emit(it) }

        emitAll(
            docRef(uid)
                .snapshots()
                .map { snapshot ->
                    parseSnapshot(snapshot, uid)?.also { parsed ->
                        saveLocalPreferences(uid, parsed)
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    if (auth.currentUser?.uid == uid) {
                        handleFirestoreError(firestoreError ?: error, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun saveTargetSubjects(
        subjectIds: List<String>,
        dailyGoalMinutes: Int,
        learningTrack: String,
        studyGoalNote: String
    ): Result<TargetSubjectsPreference> = withContext(Dispatchers.IO) {
        val uid = try {
            requireFirebaseUserId()
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }

        val cleanIds = TargetSubjectsCatalog.normalizeSubjectIds(subjectIds)
        if (cleanIds.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("Select at least one target subject.")
            )
        }

        val cleanMinutes = TargetSubjectsCatalog.normalizeDailyGoalMinutes(dailyGoalMinutes)
        val cleanTrack = TargetSubjectsCatalog.normalizeTrackId(learningTrack)
        val cleanNote = TargetSubjectsCatalog.normalizeGoalNote(studyGoalNote)

        val prefModel = TargetSubjectsPreference(
            id = TARGET_SUBJECTS_DOC_ID,
            userId = uid,
            subjectIds = cleanIds,
            dailyGoalMinutes = cleanMinutes,
            learningTrack = cleanTrack,
            studyGoalNote = cleanNote
        )

        val ref = docRef(uid)
        val path = documentPath(uid)

        try {
            val existing = ref.get().await()
            if (existing.exists()) {
                ref.update(prefModel.toUpdateMap()).await()
            } else {
                ref.set(prefModel.toCreateMap(uid)).await()
            }
            saveLocalPreferences(uid, prefModel)
            Log.d(TAG, "Saved target subjects (${cleanIds.joinToString()}) to $path")
            Result.success(prefModel)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteTargetSubjects(): Result<Unit> = withContext(Dispatchers.IO) {
        val uid = try {
            requireFirebaseUserId()
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }

        val path = documentPath(uid)
        try {
            docRef(uid).delete().await()
            clearLocalPreferences(uid)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    fun loadLocalPreferences(ownerKey: String): TargetSubjectsPreference? {
        if (ownerKey.isBlank()) return null
        val raw = localPrefs.getString(localCacheKey(ownerKey), null) ?: return null
        return try {
            val obj = JSONObject(raw)
            val arr = obj.optJSONArray("subjectIds") ?: JSONArray()
            val rawIds = buildList {
                for (i in 0 until arr.length()) {
                    add(arr.optString(i))
                }
            }
            val cleanIds = TargetSubjectsCatalog.normalizeSubjectIds(rawIds)
            if (cleanIds.isEmpty()) return null
            TargetSubjectsPreference(
                id = TARGET_SUBJECTS_DOC_ID,
                userId = ownerKey,
                subjectIds = cleanIds,
                dailyGoalMinutes = TargetSubjectsCatalog.normalizeDailyGoalMinutes(
                    obj.optInt("dailyGoalMinutes", 30)
                ),
                learningTrack = TargetSubjectsCatalog.normalizeTrackId(
                    obj.optString("learningTrack", LearningTrackOption.CUSTOM.id)
                ),
                studyGoalNote = TargetSubjectsCatalog.normalizeGoalNote(
                    obj.optString("studyGoalNote", "")
                )
            )
        } catch (_: Exception) {
            null
        }
    }

    fun saveLocalPreferences(ownerKey: String, preference: TargetSubjectsPreference) {
        if (ownerKey.isBlank()) return
        val cleanIds = TargetSubjectsCatalog.normalizeSubjectIds(preference.subjectIds)
        val obj = JSONObject().apply {
            put("subjectIds", JSONArray(cleanIds))
            put("dailyGoalMinutes", TargetSubjectsCatalog.normalizeDailyGoalMinutes(preference.dailyGoalMinutes))
            put("learningTrack", TargetSubjectsCatalog.normalizeTrackId(preference.learningTrack))
            put("studyGoalNote", TargetSubjectsCatalog.normalizeGoalNote(preference.studyGoalNote))
        }
        localPrefs.edit().putString(localCacheKey(ownerKey), obj.toString()).apply()
    }

    fun clearLocalPreferences(ownerKey: String) {
        if (ownerKey.isBlank()) return
        localPrefs.edit().remove(localCacheKey(ownerKey)).apply()
    }

    private fun localCacheKey(ownerKey: String): String = "target_subjects_cache_$ownerKey"

    private fun parseSnapshot(
        snapshot: DocumentSnapshot,
        expectedUid: String
    ): TargetSubjectsPreference? {
        if (!snapshot.exists()) return null
        val data = snapshot.getData(DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) ?: return null
        val rawIds = (data["subjectIds"] as? List<*>)?.mapNotNull { it?.toString() }.orEmpty()
        val cleanIds = TargetSubjectsCatalog.normalizeSubjectIds(rawIds)
        val minutes = TargetSubjectsCatalog.normalizeDailyGoalMinutes(
            (data["dailyGoalMinutes"] as? Number)?.toInt() ?: 30
        )
        val track = TargetSubjectsCatalog.normalizeTrackId(data["learningTrack"]?.toString().orEmpty())
        val note = TargetSubjectsCatalog.normalizeGoalNote(data["studyGoalNote"]?.toString().orEmpty())

        return TargetSubjectsPreference(
            id = snapshot.id,
            userId = data["userId"]?.toString()?.takeIf { it.isNotBlank() } ?: expectedUid,
            subjectIds = cleanIds,
            dailyGoalMinutes = minutes,
            learningTrack = track,
            studyGoalNote = note,
            createdAt = snapshot.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
            updatedAt = snapshot.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        )
    }

    companion object {
        const val PREFERENCES_COLLECTION = "preferences"
        const val TARGET_SUBJECTS_DOC_ID = "target_subjects"
    }
}
