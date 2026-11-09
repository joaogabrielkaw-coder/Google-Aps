package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Modelo de relação 1-para-Muitos do Room que une a rotina de estudos gerada pela IA
 * a todos os seus respectivos eventos e sessões agendadas no calendário.
 */
data class RoutineWithEvents(
    @Embedded
    val routine: StudyRoutineEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val events: List<CalendarEventEntity>
)
