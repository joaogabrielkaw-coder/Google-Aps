package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val techId: String,
    val title: String,
    val content: String,
    val isVoiceTranscribed: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_scores")
data class GameScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerName: String,
    val score: Int,
    val maxCombo: Int,
    val crystalsCollected: Int,
    val durationSeconds: Int,
    val techFocusId: String,
    val playedAtMillis: Long = System.currentTimeMillis()
)
