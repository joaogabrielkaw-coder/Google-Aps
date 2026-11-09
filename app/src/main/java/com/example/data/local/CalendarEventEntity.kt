package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room que representa os eventos do calendário de estudos.
 *
 * Contém os campos fundamentais solicitados:
 * - ID (@PrimaryKey autogerado)
 * - Título da sessão de estudo
 * - Descrição e notas pedagógicas
 * - Data e Hora agendada
 * - Status de conclusão (isCompleted)
 * - Vínculo opcional com a rotina de estudos gerada pela IA (routineId)
 */
@Entity(
    tableName = "calendar_events",
    indices = [
        Index(value = ["dateIso"]),
        Index(value = ["techId"]),
        Index(value = ["routineId"])
    ]
)
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                                // ID único do evento

    val routineId: Long? = null,                     // ID da rotina de estudos associada (se gerada pela IA)

    val title: String,                               // Título do evento de estudo (ex: "Python & Dicionários")

    val description: String,                         // Descrição detalhada / tópicos abordados

    val dateIso: String,                             // Data do evento no formato ISO "YYYY-MM-DD"

    val timeFormatted: String = "19:00",             // Hora do evento no formato "HH:mm"

    val isCompleted: Boolean = false,                // Status de conclusão do evento

    val techId: String = "python",                   // Tecnologia alvo (python, sql, html, css, java, mysql, postgresql, mongodb)

    val durationMinutes: Int = 45,                   // Duração estimada em minutos

    val completedAtMillis: Long = 0L,                // Data/hora em que o evento foi concluído (milissegundos)

    val aiGenerated: Boolean = false,                // Flag que indica se foi gerado pela IA

    // Campos de suporte multilíngue (Português / Inglês)
    val titlePt: String = "",
    val titleEn: String = "",
    val topicPt: String = "",
    val topicEn: String = "",
    val notesPt: String = "",
    val notesEn: String = ""
) {
    val displayTitle: String
        get() = titlePt.ifBlank { title }

    val displayDescription: String
        get() = topicPt.ifBlank { description }

    val dateTimeDisplay: String
        get() = "$dateIso às $timeFormatted"
}
