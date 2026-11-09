package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserBadgeDao {

    @Query("SELECT * FROM user_badges ORDER BY isUnlocked DESC, (currentProgress * 1.0 / maxProgress) DESC, titlePt ASC")
    fun getAllBadges(): Flow<List<UserBadgeEntity>>

    @Query("SELECT * FROM user_badges WHERE isUnlocked = 1 ORDER BY unlockedAtMillis DESC")
    fun getUnlockedBadges(): Flow<List<UserBadgeEntity>>

    @Query("SELECT COUNT(*) FROM user_badges WHERE isUnlocked = 1")
    fun getUnlockedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_badges")
    fun getTotalCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBadges(badges: List<UserBadgeEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialBadges(badges: List<UserBadgeEntity>)

    @Query("SELECT * FROM user_badges WHERE id = :id LIMIT 1")
    suspend fun getBadgeById(id: String): UserBadgeEntity?

    @Query("UPDATE user_badges SET isUnlocked = 1, unlockedAtMillis = :timestamp, currentProgress = maxProgress WHERE id = :id AND isUnlocked = 0")
    suspend fun unlockBadge(id: String, timestamp: Long)

    @Query("UPDATE user_badges SET currentProgress = :progress, isUnlocked = CASE WHEN :progress >= maxProgress THEN 1 ELSE isUnlocked END, unlockedAtMillis = CASE WHEN :progress >= maxProgress AND unlockedAtMillis IS NULL THEN :timestamp ELSE unlockedAtMillis END WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, timestamp: Long)
}
