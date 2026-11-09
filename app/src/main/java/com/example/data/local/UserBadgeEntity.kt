package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Language

/**
 * Entidade Room para armazenar emblemas (badges) e conquistas do usuário,
 * desbloqueados com base em sequências de estudo (streaks), horários e marcos práticos.
 */
@Entity(tableName = "user_badges")
data class UserBadgeEntity(
    @PrimaryKey
    val id: String,
    val titlePt: String,
    val titleEn: String,
    val descriptionPt: String,
    val descriptionEn: String,
    val category: String, // "STREAK", "TECH", "ACTIVITY", "TIME", "MILESTONE"
    val iconKey: String,   // "python", "night_owl", "streak", "sql", "trophy", "code", "ai", "clock", "fire", "star"
    val isUnlocked: Boolean = false,
    val unlockedAtMillis: Long? = null,
    val currentProgress: Int = 0,
    val maxProgress: Int = 1,
    val xpReward: Int = 50
) {
    fun title(language: Language): String = if (language == Language.PT) titlePt else titleEn
    fun description(language: Language): String = if (language == Language.PT) descriptionPt else descriptionEn

    val progressFraction: Float
        get() = if (maxProgress <= 0) 1f else (currentProgress.toFloat() / maxProgress.toFloat()).coerceIn(0f, 1f)
}
