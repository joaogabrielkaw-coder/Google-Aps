package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operações de persistência e consulta
 * das rotinas de estudo geradas pela IA no Room.
 */
@Dao
interface StudyRoutineDao {

    @Query("SELECT * FROM study_routines ORDER BY createdAtMillis DESC")
    fun getAllRoutines(): Flow<List<StudyRoutineEntity>>

    @Query("SELECT * FROM study_routines WHERE isActive = 1 ORDER BY createdAtMillis DESC LIMIT 1")
    fun getActiveRoutine(): Flow<StudyRoutineEntity?>

    @Query("SELECT * FROM study_routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): StudyRoutineEntity?

    @Query("SELECT COUNT(*) FROM study_routines")
    suspend fun getRoutinesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: StudyRoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: StudyRoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: StudyRoutineEntity)

    @Transaction
    @Query("SELECT * FROM study_routines WHERE id = :routineId")
    fun getRoutineWithEvents(routineId: Long): Flow<RoutineWithEvents?>

    @Transaction
    @Query("SELECT * FROM study_routines ORDER BY createdAtMillis DESC")
    fun getAllRoutinesWithEvents(): Flow<List<RoutineWithEvents>>
}
