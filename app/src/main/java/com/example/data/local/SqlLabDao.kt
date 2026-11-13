package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyNoteDao {
    @Query("SELECT * FROM study_notes ORDER BY createdAtMillis DESC")
    fun observeAllNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE techId = :techId ORDER BY createdAtMillis DESC")
    fun observeNotesByTech(techId: String): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY createdAtMillis DESC")
    fun searchNotes(query: String): Flow<List<StudyNoteEntity>>

    @Query("SELECT COUNT(*) FROM study_notes")
    suspend fun getNotesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity): Long

    @Query("DELETE FROM study_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface GameScoreDao {
    @Query("SELECT * FROM game_scores ORDER BY score DESC, playedAtMillis DESC LIMIT 50")
    fun observeTopScores(): Flow<List<GameScoreEntity>>

    @Query("SELECT COALESCE(MAX(score), 0) FROM game_scores")
    fun observeBestScore(): Flow<Int>

    @Query("SELECT COALESCE(SUM(crystalsCollected), 0) FROM game_scores")
    fun observeTotalCrystals(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: GameScoreEntity): Long

    @Query("DELETE FROM game_scores")
    suspend fun clearScores()
}
