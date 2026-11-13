package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.GameScoreEntity
import com.example.data.local.StudyNoteEntity
import com.example.model.SqlQueryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository wrapping the local SQLite SQL database via Room DAOs and safe read-only SQL inspector.
 */
class SqlLabRepository(
    private val database: AppDatabase
) {
    private val noteDao = database.studyNoteDao()
    private val scoreDao = database.gameScoreDao()

    val allNotes: Flow<List<StudyNoteEntity>> = noteDao.observeAllNotes()
    val topGameScores: Flow<List<GameScoreEntity>> = scoreDao.observeTopScores()
    val bestGameScore: Flow<Int> = scoreDao.observeBestScore()
    val totalGameCrystals: Flow<Int> = scoreDao.observeTotalCrystals()

    suspend fun populateStarterNotesIfEmpty() {
        if (noteDao.getNotesCount() == 0) {
            val now = System.currentTimeMillis()
            noteDao.insertNote(
                StudyNoteEntity(
                    techId = "sql",
                    title = "Índices e JOINs Otimizados",
                    content = "Sempre indexar colunas usadas em FOREIGN KEY, WHERE e ORDER BY. Usar EXPLAIN QUERY PLAN para validar scans.",
                    isVoiceTranscribed = false,
                    createdAtMillis = now - 7_200_000L
                )
            )
            noteDao.insertNote(
                StudyNoteEntity(
                    techId = "javascript",
                    title = "Event Loop & Promises",
                    content = "Microtasks (Promise.then / async-await) executam antes das macrotasks (setTimeout) no navegador e Node.js.",
                    isVoiceTranscribed = false,
                    createdAtMillis = now - 3_600_000L
                )
            )
            noteDao.insertNote(
                StudyNoteEntity(
                    techId = "kotlin",
                    title = "StateFlow & Coroutines",
                    content = "Coletar fluxos de estado com collectAsStateWithLifecycle e preservar CancellationException em blocos try/catch.",
                    isVoiceTranscribed = false,
                    createdAtMillis = now - 1_800_000L
                )
            )
        }
    }

    suspend fun insertStudyNote(
        techId: String,
        title: String,
        content: String,
        isVoiceTranscribed: Boolean
    ): Long {
        return noteDao.insertNote(
            StudyNoteEntity(
                techId = techId,
                title = title.trim(),
                content = content.trim(),
                isVoiceTranscribed = isVoiceTranscribed
            )
        )
    }

    suspend fun deleteStudyNote(id: Long) {
        noteDao.deleteNoteById(id)
    }

    suspend fun recordGameScore(
        playerName: String,
        score: Int,
        maxCombo: Int,
        crystalsCollected: Int,
        durationSeconds: Int,
        techFocusId: String
    ): Long {
        return scoreDao.insertScore(
            GameScoreEntity(
                playerName = playerName.ifBlank { "DevPlayer" },
                score = score,
                maxCombo = maxCombo,
                crystalsCollected = crystalsCollected,
                durationSeconds = durationSeconds,
                techFocusId = techFocusId
            )
        )
    }

    /**
     * Executes a read-only SQLite query (SELECT / PRAGMA / EXPLAIN) safely against the Room database
     * and returns formatted tabular columns and rows.
     */
    suspend fun executeReadOnlySql(rawSql: String): SqlQueryResult = withContext(Dispatchers.IO) {
        val trimmed = rawSql.trim().removeSuffix(";").trim()
        if (trimmed.isBlank()) {
            return@withContext SqlQueryResult(
                query = rawSql,
                errorMessage = "Digite uma consulta SQL SELECT ou PRAGMA válida."
            )
        }
        val validationError = validateReadOnlySql(trimmed)
        if (validationError != null) {
            return@withContext SqlQueryResult(query = rawSql, errorMessage = validationError)
        }

        val start = System.currentTimeMillis()
        try {
            val db = database.openHelper.readableDatabase
            val cursor = db.query(trimmed)
            cursor.use { c ->
                val cols = c.columnNames.toList()
                val rows = mutableListOf<List<String>>()
                var rowCount = 0
                while (c.moveToNext() && rowCount < 100) {
                    val row = cols.indices.map { idx ->
                        when {
                            c.isNull(idx) -> "NULL"
                            else -> try {
                                c.getString(idx) ?: "NULL"
                            } catch (_: Exception) {
                                "[BLOB]"
                            }
                        }
                    }
                    rows.add(row)
                    rowCount++
                }
                val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1L)
                SqlQueryResult(
                    query = trimmed,
                    columns = cols,
                    rows = rows,
                    executionTimeMs = elapsed
                )
            }
        } catch (e: Exception) {
            SqlQueryResult(
                query = trimmed,
                executionTimeMs = System.currentTimeMillis() - start,
                errorMessage = e.localizedMessage ?: "Erro ao executar consulta SQL."
            )
        }
    }

    companion object {
        private val ALLOWED_PREFIXES = listOf("SELECT", "PRAGMA", "EXPLAIN", "WITH")
        private val FORBIDDEN_KEYWORDS = listOf("DROP ", "DELETE ", "UPDATE ", "INSERT ", "ALTER ", "ATTACH ", "DETACH ", "VACUUM")

        fun validateReadOnlySql(sql: String): String? {
            val upper = sql.trim().uppercase()
            if (ALLOWED_PREFIXES.none { upper.startsWith(it) }) {
                return "Console somente leitura: use consultas SELECT, WITH, PRAGMA ou EXPLAIN."
            }
            if (sql.contains(';')) {
                return "Execute apenas uma instrução SQL por vez."
            }
            val hitForbidden = FORBIDDEN_KEYWORDS.firstOrNull { upper.contains(it) }
            if (hitForbidden != null) {
                return "Operação '${hitForbidden.trim()}' bloqueada no console interativo somente leitura."
            }
            return null
        }

        val PRESET_QUERIES = listOf(
            "SELECT * FROM study_notes ORDER BY createdAtMillis DESC",
            "SELECT techId, COUNT(*) AS total_notes FROM study_notes GROUP BY techId ORDER BY total_notes DESC",
            "SELECT playerName, score, maxCombo, crystalsCollected, techFocusId FROM game_scores ORDER BY score DESC LIMIT 10",
            "SELECT id, titlePt, category, xpReward, isUnlocked FROM user_badges WHERE isUnlocked = 1",
            "SELECT name FROM sqlite_master WHERE type = 'table' ORDER BY name"
        )
    }
}
