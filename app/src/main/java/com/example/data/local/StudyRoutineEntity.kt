package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room que representa a rotina de estudos gerada pela Inteligência Artificial.
 *
 * Contém os campos fundamentais solicitados:
 * - ID (@PrimaryKey autogerado)
 * - Título da rotina de estudos
 * - Descrição e objetivo pedagógico
 * - Data e hora de criação / início
 * - Status de conclusão da rotina (isCompleted)
 * - Tecnologias e duração diária planejada
 */
@Entity(
    tableName = "study_routines",
    indices = [Index(value = ["routineUid"], unique = true)]
)
data class StudyRoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                                // ID único da rotina

    val routineUid: String = "",                     // UID único para referência externa

    val title: String,                               // Título da rotina (ex: "Imersão Backend: Python & MySQL")

    val description: String,                         // Descrição do planejamento gerado pela IA

    val targetGoal: String = "",                     // Meta do aluno (ex: "Aprender a construir APIs e queries")

    val targetTechnologies: String = "",             // Tecnologias inclusas (ex: "Python, SQL, MySQL")

    val totalDays: Int = 14,                         // Duração total em dias do cronograma

    val dailyCommitmentMinutes: Int = 45,            // Dedicação recomendada por dia em minutos

    val totalWeeks: Int = 2,                         // Total de semanas do cronograma

    val createdAtMillis: Long = 0L,                  // Data/hora de criação da rotina (timestamp)

    val isCompleted: Boolean = false,                // Status de conclusão da rotina de estudos

    val isActive: Boolean = true,                    // Indica se é a rotina atual em andamento

    // Campos de suporte multilíngue
    val titlePt: String = "",
    val titleEn: String = "",
    val descriptionPt: String = "",
    val descriptionEn: String = "",
    val targetTechsCsv: String = ""
) {
    val displayTitle: String
        get() = titlePt.ifBlank { title }

    val displayDescription: String
        get() = descriptionPt.ifBlank { description }
}
