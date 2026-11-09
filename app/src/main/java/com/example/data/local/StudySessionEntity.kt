package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.StudySession
import com.example.model.Technology

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val techId: String,
    val titlePt: String,
    val titleEn: String,
    val topicPt: String,
    val topicEn: String,
    val notesPt: String,
    val notesEn: String,
    val dateIso: String, // YYYY-MM-DD
    val timeFormatted: String,
    val durationMinutes: Int,
    val isCompleted: Boolean,
    val completedAtMillis: Long,
    val aiGenerated: Boolean
) {
    fun toDomain(): StudySession = StudySession(
        id = id,
        tech = Technology.fromId(techId),
        titlePt = titlePt,
        titleEn = titleEn,
        topicPt = topicPt,
        topicEn = topicEn,
        notesPt = notesPt,
        notesEn = notesEn,
        dateIso = dateIso,
        timeFormatted = timeFormatted,
        durationMinutes = durationMinutes,
        isCompleted = isCompleted,
        completedAtMillis = completedAtMillis,
        aiGenerated = aiGenerated
    )

    companion object {
        fun fromDomain(session: StudySession): StudySessionEntity = StudySessionEntity(
            id = session.id,
            techId = session.tech.id,
            titlePt = session.titlePt,
            titleEn = session.titleEn,
            topicPt = session.topicPt,
            topicEn = session.topicEn,
            notesPt = session.notesPt,
            notesEn = session.notesEn,
            dateIso = session.dateIso,
            timeFormatted = session.timeFormatted,
            durationMinutes = session.durationMinutes,
            isCompleted = session.isCompleted,
            completedAtMillis = session.completedAtMillis,
            aiGenerated = session.aiGenerated
        )
    }
}
