package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_sessions ORDER BY dateIso ASC, timeFormatted ASC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE dateIso = :dateIso ORDER BY timeFormatted ASC")
    fun getSessionsForDate(dateIso: String): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE techId = :techId ORDER BY dateIso ASC")
    fun getSessionsForTech(techId: String): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<StudySessionEntity>): List<Long>

    @Update
    suspend fun updateSession(session: StudySessionEntity)

    @Delete
    suspend fun deleteSession(session: StudySessionEntity)

    @Query("UPDATE study_sessions SET isCompleted = :completed, completedAtMillis = :completedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, completed: Boolean, completedAt: Long)

    @Query("SELECT COUNT(*) FROM study_sessions WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM study_sessions")
    suspend fun getSessionsCount(): Int

    @Query("DELETE FROM study_sessions")
    suspend fun clearAll()
}
