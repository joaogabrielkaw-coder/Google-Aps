package com.example.data.repository

import android.util.Log
import com.example.data.local.CalendarEventDao
import com.example.data.local.CalendarEventEntity
import com.example.data.local.RoutineWithEvents
import com.example.data.local.StudyRoutineDao
import com.example.data.local.StudyRoutineEntity
import com.example.data.local.StudySessionDao
import com.example.data.local.StudySessionEntity
import com.example.model.RoutinePlan
import com.example.model.StudySession
import com.example.model.Technology
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "StudyRepository"

class StudyRepository(
    private val dao: StudySessionDao,
    private val routineDao: StudyRoutineDao? = null,
    private val calendarDao: CalendarEventDao? = null
) {

    val allSessions: Flow<List<StudySession>> = dao.getAllSessions().map { entities ->
        entities.map { it.toDomain() }
    }

    val completedCount: Flow<Int> = dao.getCompletedCount()

    val allRoutinesWithEvents: Flow<List<RoutineWithEvents>> =
        routineDao?.getAllRoutinesWithEvents() ?: emptyFlow()

    fun getSessionsForDate(dateIso: String): Flow<List<StudySession>> {
        return dao.getSessionsForDate(dateIso).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getSessionsForTech(tech: Technology): Flow<List<StudySession>> {
        return dao.getSessionsForTech(tech.id).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertSession(session: StudySession): Long = withContext(Dispatchers.IO) {
        try {
            val sessionId = dao.insertSession(StudySessionEntity.fromDomain(session))
            calendarDao?.insertEvent(
                CalendarEventEntity(
                    id = sessionId,
                    routineId = null,
                    title = session.titlePt,
                    description = session.topicPt.ifBlank { session.notesPt },
                    dateIso = session.dateIso,
                    timeFormatted = session.timeFormatted,
                    isCompleted = session.isCompleted,
                    techId = session.tech.id,
                    durationMinutes = session.durationMinutes,
                    completedAtMillis = session.completedAtMillis,
                    aiGenerated = session.aiGenerated,
                    titlePt = session.titlePt,
                    titleEn = session.titleEn,
                    topicPt = session.topicPt,
                    topicEn = session.topicEn,
                    notesPt = session.notesPt,
                    notesEn = session.notesEn
                )
            )
            sessionId
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting session", e)
            0L
        }
    }

    suspend fun insertSessions(sessions: List<StudySession>): List<Long> = withContext(Dispatchers.IO) {
        try {
            dao.insertSessions(sessions.map { StudySessionEntity.fromDomain(it) })
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting multiple sessions", e)
            emptyList()
        }
    }

    suspend fun deleteSession(session: StudySession) = withContext(Dispatchers.IO) {
        try {
            dao.deleteSession(StudySessionEntity.fromDomain(session))
            calendarDao?.deleteEvent(
                CalendarEventEntity(
                    id = session.id,
                    title = session.titlePt,
                    description = session.topicPt,
                    dateIso = session.dateIso
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting session", e)
        }
    }

    suspend fun toggleCompleted(session: StudySession) = withContext(Dispatchers.IO) {
        try {
            val newStatus = !session.isCompleted
            val completedAt = if (newStatus) System.currentTimeMillis() else 0L
            dao.updateCompletionStatus(session.id, newStatus, completedAt)
            calendarDao?.updateCompletionStatus(session.id, newStatus, completedAt)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating completion status", e)
        }
    }

    suspend fun populateStarterRoutinesIfEmpty() = withContext(Dispatchers.IO) {
        try {
            // Check if sessions already populated to avoid duplicate inserts on cold starts
            val count = dao.getSessionsCount()
            if (count > 0) {
                Log.d(TAG, "Database already populated ($count sessions found), skipping seed.")
                return@withContext
            }

            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE

            val starter = listOf(
                StudySession(
                    tech = Technology.PYTHON,
                    titlePt = "Fundamentos de Python & Estruturas de Dados",
                    titleEn = "Python Fundamentals & Data Structures",
                    topicPt = "Variáveis, Tipagem dinâmica, Listas e Dicionários",
                    topicEn = "Variables, Dynamic Typing, Lists & Dictionaries",
                    notesPt = "Praticar list comprehensions e funções lambda no terminal.",
                    notesEn = "Practice list comprehensions and lambda functions in the terminal.",
                    dateIso = today.format(formatter),
                    timeFormatted = "19:00",
                    durationMinutes = 45,
                    isCompleted = false,
                    aiGenerated = false
                ),
                StudySession(
                    tech = Technology.SQL,
                    titlePt = "Consultas SQL & Cláusulas Essenciais",
                    titleEn = "SQL Queries & Essential Clauses",
                    topicPt = "SELECT, WHERE, ORDER BY, GROUP BY e HAVING",
                    topicEn = "SELECT, WHERE, ORDER BY, GROUP BY and HAVING",
                    notesPt = "Treinar agrupamentos e filtros de agregação em tabelas relacionais.",
                    notesEn = "Practice aggregation filters and grouping on relational tables.",
                    dateIso = today.plusDays(1).format(formatter),
                    timeFormatted = "19:30",
                    durationMinutes = 40,
                    isCompleted = false,
                    aiGenerated = false
                ),
                StudySession(
                    tech = Technology.MYSQL,
                    titlePt = "Modelagem Relacional com MySQL",
                    titleEn = "Relational Modeling with MySQL",
                    topicPt = "Tabelas, Chaves Primárias, Chaves Estrangeiras & InnoDB",
                    topicEn = "Tables, Primary Keys, Foreign Keys & InnoDB Engine",
                    notesPt = "Construir um schema de ecommerce simples com integridade referencial.",
                    notesEn = "Build a simple ecommerce schema with referential integrity.",
                    dateIso = today.plusDays(2).format(formatter),
                    timeFormatted = "20:00",
                    durationMinutes = 50,
                    isCompleted = false,
                    aiGenerated = false
                ),
                StudySession(
                    tech = Technology.HTML,
                    titlePt = "HTML5 Semântico e Acessibilidade",
                    titleEn = "Semantic HTML5 and Accessibility",
                    topicPt = "Tags <main>, <nav>, <article>, <section> e formulários modernos",
                    topicEn = "Tags <main>, <nav>, <article>, <section> and modern forms",
                    notesPt = "Estruturar uma landing page acessível com atributos aria e validação nativa.",
                    notesEn = "Structure an accessible landing page with aria attributes and native validation.",
                    dateIso = today.plusDays(3).format(formatter),
                    timeFormatted = "18:30",
                    durationMinutes = 40,
                    isCompleted = false,
                    aiGenerated = false
                ),
                StudySession(
                    tech = Technology.CSS,
                    titlePt = "Mastering CSS Flexbox & CSS Grid",
                    titleEn = "Mastering CSS Flexbox & CSS Grid",
                    topicPt = "Alinhamento, justify-content, grid-template-columns e responsividade",
                    topicEn = "Alignment, justify-content, grid-template-columns & responsive layouts",
                    notesPt = "Criar um layout de dashboard flexível sem media queries complexas.",
                    notesEn = "Build a flexible dashboard layout without heavy media queries.",
                    dateIso = today.plusDays(4).format(formatter),
                    timeFormatted = "19:00",
                    durationMinutes = 45,
                    isCompleted = false,
                    aiGenerated = false
                ),
                StudySession(
                    tech = Technology.JAVA,
                    titlePt = "Java OOP: Herança, Interfaces e Polimorfismo",
                    titleEn = "Java OOP: Inheritance, Interfaces & Polymorphism",
                    topicPt = "Classes abstratas, interfaces modernas e encapsulamento na JVM",
                    topicEn = "Abstract classes, modern interfaces and encapsulation on JVM",
                    notesPt = "Desenvolver um sistema de pagamento utilizando polimorfismo limpo.",
                    notesEn = "Implement a payment system using clean polymorphism.",
                    dateIso = today.plusDays(5).format(formatter),
                    timeFormatted = "20:00",
                    durationMinutes = 60,
                    isCompleted = false,
                    aiGenerated = false
                ),
                StudySession(
                    tech = Technology.POSTGRESQL,
                    titlePt = "PostgreSQL: Tipos Avançados & JSONB",
                    titleEn = "PostgreSQL: Advanced Types & JSONB Queries",
                    topicPt = "Consultas em JSONB, índices GIN e CTEs (WITH queries)",
                    topicEn = "Querying JSONB, GIN indexes and CTEs (WITH queries)",
                    notesPt = "Indexar payloads JSON e realizar queries híbridas relacional + noSQL.",
                    notesEn = "Index JSON payloads and execute hybrid relational + NoSQL queries.",
                    dateIso = today.plusDays(6).format(formatter),
                    timeFormatted = "19:00",
                    durationMinutes = 50,
                    isCompleted = false,
                    aiGenerated = false
                ),
                StudySession(
                    tech = Technology.MONGODB,
                    titlePt = "MongoDB: Documentos & Pipelines de Agregação",
                    titleEn = "MongoDB: Documents & Aggregation Pipelines",
                    topicPt = "Operadores \$match, \$group, \$project e indexação de coleções",
                    topicEn = "Operators \$match, \$group, \$project and collection indexing",
                    notesPt = "Montar pipeline para calcular relatórios de vendas diretamente no Mongo.",
                    notesEn = "Assemble a pipeline to compute sales reports directly in MongoDB.",
                    dateIso = today.plusDays(7).format(formatter),
                    timeFormatted = "19:30",
                    durationMinutes = 50,
                    isCompleted = false,
                    aiGenerated = false
                )
            )

            val routineEntity = StudyRoutineEntity(
                routineUid = "starter-foundation-track",
                title = "Imersão Completa: Código & Bancos de Dados",
                titlePt = "Imersão Completa: Código & Bancos de Dados",
                titleEn = "Full Immersion: Code & Databases",
                description = "Rotina equilibrada abordando Python, SQL, HTML, CSS, Java, MySQL, PostgreSQL e MongoDB.",
                descriptionPt = "Rotina equilibrada abordando Python, SQL, HTML, CSS, Java, MySQL, PostgreSQL e MongoDB.",
                descriptionEn = "Balanced routine covering Python, SQL, HTML, CSS, Java, MySQL, PostgreSQL, and MongoDB.",
                targetGoal = "Aprender linguagens fundamentais e bancos relacionais e NoSQL",
                targetTechnologies = "Python, SQL, HTML, CSS, Java, MySQL, PostgreSQL, MongoDB",
                targetTechsCsv = "python,sql,html,css,java,mysql,postgresql,mongodb",
                totalDays = starter.size,
                dailyCommitmentMinutes = 45,
                totalWeeks = 2,
                createdAtMillis = System.currentTimeMillis(),
                isActive = true
            )
            val insertedRoutineId = routineDao?.insertRoutine(routineEntity)

            val insertedIds = dao.insertSessions(starter.map { StudySessionEntity.fromDomain(it) })

            calendarDao?.let { cDao ->
                val events = starter.mapIndexed { idx, s ->
                    val assignedId = insertedIds.getOrNull(idx) ?: 0L
                    CalendarEventEntity(
                        id = assignedId,
                        routineId = insertedRoutineId,
                        title = s.titlePt,
                        description = s.topicPt,
                        dateIso = s.dateIso,
                        timeFormatted = s.timeFormatted,
                        isCompleted = s.isCompleted,
                        techId = s.tech.id,
                        durationMinutes = s.durationMinutes,
                        completedAtMillis = s.completedAtMillis,
                        aiGenerated = s.aiGenerated,
                        titlePt = s.titlePt,
                        titleEn = s.titleEn,
                        topicPt = s.topicPt,
                        topicEn = s.topicEn,
                        notesPt = s.notesPt,
                        notesEn = s.notesEn
                    )
                }
                cDao.insertEvents(events)
            }
            Log.d(TAG, "Successfully populated starter routines and calendar events.")
        } catch (e: Exception) {
            Log.e(TAG, "Error populating starter routines", e)
        }
    }

    suspend fun saveAiRoutine(plan: RoutinePlan) = withContext(Dispatchers.IO) {
        try {
            val routineEntity = StudyRoutineEntity(
                routineUid = plan.id,
                title = plan.titlePt,
                description = plan.descriptionPt,
                targetGoal = plan.titlePt,
                targetTechnologies = plan.targetTechs.joinToString(", ") { it.displayName },
                totalDays = plan.sessions.size,
                dailyCommitmentMinutes = plan.sessions.firstOrNull()?.durationMinutes ?: 45,
                totalWeeks = plan.totalWeeks,
                createdAtMillis = System.currentTimeMillis(),
                titlePt = plan.titlePt,
                titleEn = plan.titleEn,
                descriptionPt = plan.descriptionPt,
                descriptionEn = plan.descriptionEn,
                targetTechsCsv = plan.targetTechs.joinToString(",") { it.id },
                isActive = true
            )
            val generatedRoutineId = routineDao?.insertRoutine(routineEntity)

            val sessionEntities = plan.sessions.map { session ->
                StudySessionEntity.fromDomain(session.copy(aiGenerated = true))
            }
            val insertedIds = dao.insertSessions(sessionEntities)

            calendarDao?.let { cDao ->
                val calendarEvents = plan.sessions.mapIndexed { idx, session ->
                    val assignedId = insertedIds.getOrNull(idx) ?: 0L
                    CalendarEventEntity(
                        id = assignedId,
                        routineId = generatedRoutineId,
                        title = session.titlePt,
                        description = session.topicPt.ifBlank { session.notesPt },
                        dateIso = session.dateIso,
                        timeFormatted = session.timeFormatted,
                        isCompleted = false,
                        techId = session.tech.id,
                        durationMinutes = session.durationMinutes,
                        completedAtMillis = 0L,
                        aiGenerated = true,
                        titlePt = session.titlePt,
                        titleEn = session.titleEn,
                        topicPt = session.topicPt,
                        topicEn = session.topicEn,
                        notesPt = session.notesPt,
                        notesEn = session.notesEn
                    )
                }
                cDao.insertEvents(calendarEvents)
            }
            Log.d(TAG, "AI Routine saved successfully with id $generatedRoutineId")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving AI routine", e)
        }
    }
}
