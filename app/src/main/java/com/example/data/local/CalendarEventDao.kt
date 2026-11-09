package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para operações de persistência e consulta
 * dos eventos e sessões agendadas no calendário de estudos.
 */
@Dao
interface CalendarEventDao {

    @Query("SELECT * FROM calendar_events ORDER BY dateIso ASC, timeFormatted ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE dateIso = :dateIso ORDER BY timeFormatted ASC")
    fun getEventsForDate(dateIso: String): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE techId = :techId ORDER BY dateIso ASC")
    fun getEventsForTech(techId: String): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE routineId = :routineId ORDER BY dateIso ASC")
    fun getEventsForRoutine(routineId: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT COUNT(*) FROM calendar_events")
    suspend fun getEventsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEventEntity>): List<Long>

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)

    @Query("UPDATE calendar_events SET isCompleted = :completed, completedAtMillis = :completedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, completed: Boolean, completedAt: Long)

    @Query("SELECT COUNT(*) FROM calendar_events WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("DELETE FROM calendar_events WHERE routineId = :routineId")
    suspend fun deleteEventsForRoutine(routineId: Long)
}
