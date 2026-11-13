package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        StudySessionEntity::class,
        StudyRoutineEntity::class,
        CalendarEventEntity::class,
        UserBadgeEntity::class,
        StudyNoteEntity::class,
        GameScoreEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studySessionDao(): StudySessionDao
    abstract fun studyRoutineDao(): StudyRoutineDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun userBadgeDao(): UserBadgeDao
    abstract fun studyNoteDao(): StudyNoteDao
    abstract fun gameScoreDao(): GameScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "devroutine_database.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
