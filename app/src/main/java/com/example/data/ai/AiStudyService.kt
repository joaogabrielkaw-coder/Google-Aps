package com.example.data.ai

import android.util.Log
import com.example.model.Language
import com.example.model.RoutinePlan
import com.example.model.StudySession
import com.example.model.Technology
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val TAG = "AiStudyService"

class AiStudyService {

    // Lazy initialization of Gemini model to prevent early startup crashes before App Check
    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    /**
     * Streams conversation with the AI Study Buddy in the selected language.
     */
    fun streamChatResponse(
        historyMessages: List<Pair<Boolean, String>>, // Pair(isUser, text)
        userPrompt: String,
        language: Language,
        selectedTech: Technology?
    ): Flow<String> = flow {
        val systemInstructions = buildSystemPrompt(language, selectedTech)

        val fullPrompt = buildString {
            append(systemInstructions)
            append("\n\n---\nHistórico recente / Recent conversation:\n")
            historyMessages.takeLast(6).forEach { (isUser, msg) ->
                val speaker = if (isUser) "Usuário/User" else "DevTutor AI"
                append("$speaker: $msg\n")
            }
            append("Usuário/User: $userPrompt\nDevTutor AI:")
        }

        withTimeout(60_000L) {
            val responseStream = model.generateContentStream(fullPrompt)
            responseStream.collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Generates a structured study routine based on the conversation and preferences,
     * returning a concrete RoutinePlan ready to be saved into the user's study calendar.
     */
    suspend fun generateStudyRoutinePlan(
        goalDescription: String,
        selectedTechs: List<Technology>,
        daysCount: Int,
        minutesPerDay: Int,
        language: Language
    ): RoutinePlan = withContext(Dispatchers.IO) {
        val prompt = buildRoutineGenerationPrompt(
            goalDescription = goalDescription,
            selectedTechs = selectedTechs,
            daysCount = daysCount,
            minutesPerDay = minutesPerDay,
            language = language
        )

        try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }

            val text = response.text ?: throw IllegalStateException("Empty response from AI")
            parseRoutinePlanFromJson(text, selectedTechs, language, daysCount, minutesPerDay)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Routine generation timed out", e)
            throw IllegalStateException(
                if (language == Language.PT) "O tempo limite de 60s expirou. Tente novamente."
                else "Generation timed out after 60s. Please try again."
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate routine with AI, using smart fallback routine", e)
            // Fallback smart routine generation ensuring the user is never stranded
            createFallbackRoutinePlan(selectedTechs, daysCount, minutesPerDay, language, goalDescription)
        }
    }

    private fun buildSystemPrompt(language: Language, selectedTech: Technology?): String {
        val langInstruction = if (language == Language.PT) {
            "Você é o 'DevTutor AI', um mentor sênior de engenharia de software focado em Python, SQL, HTML, CSS, Java, MySQL, PostgreSQL e MongoDB. Responda em Português do Brasil com entusiasmo didático, clareza, exemplos de código concisos e boas práticas de arquitetura. Ajude o usuário a criar rotinas de estudos, tirar dúvidas, estruturar projetos e planejar seu calendário."
        } else {
            "You are 'DevTutor AI', a senior software engineering mentor specialized in Python, SQL, HTML, CSS, Java, MySQL, PostgreSQL, and MongoDB. Respond in English with pedagogical clarity, clean concise code snippets, and modern architectural best practices. Help the user craft study routines, answer programming questions, build project ideas, and organize their study calendar."
        }

        val techContext = selectedTech?.let {
            "\nContexto da tecnologia atual: ${it.displayName} (${it.categoryPt})."
        } ?: ""

        return "$langInstruction$techContext"
    }

    private fun buildRoutineGenerationPrompt(
        goalDescription: String,
        selectedTechs: List<Technology>,
        daysCount: Int,
        minutesPerDay: Int,
        language: Language
    ): String {
        val techListStr = selectedTechs.joinToString(", ") { it.id }
        val startDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

        return """
Você é um arquiteto pedagógico de programação.
Gere uma rotina de estudos em formato JSON estrito para o seguinte objetivo:
Objetivo do aluno: "$goalDescription"
Tecnologias envolvidas: [$techListStr] (escolha entre: python, sql, html, css, java, mysql, postgresql, mongodb)
Quantidade de sessões/dias: $daysCount
Duração média por dia: $minutesPerDay minutos
Data de início: $startDate

Responda APENAS com um objeto JSON válido (sem texto antes ou depois) no seguinte formato:
{
  "titlePt": "Título da Rotina em Português",
  "titleEn": "Routine Title in English",
  "descPt": "Descrição motivadora da rotina em Português",
  "descEn": "Motivating routine description in English",
  "sessions": [
    {
      "techId": "python",
      "dayOffset": 0,
      "time": "19:00",
      "duration": $minutesPerDay,
      "titlePt": "Título da sessão em PT",
      "titleEn": "Session title in EN",
      "topicPt": "Tópico prático em PT",
      "topicEn": "Practical topic in EN",
      "notesPt": "Exercício recomendado em PT",
      "notesEn": "Recommended exercise in EN"
    }
  ]
}
""".trimIndent()
    }

    private fun parseRoutinePlanFromJson(
        rawText: String,
        targetTechs: List<Technology>,
        language: Language,
        daysCount: Int,
        minutesPerDay: Int
    ): RoutinePlan {
        // Strip markdown code fences if present
        val cleaned = rawText
            .substringAfter("```json")
            .substringAfter("```")
            .substringBeforeLast("```")
            .trim()

        val json = JSONObject(cleaned)
        val titlePt = json.optString("titlePt", "Rotina Personalizada por IA")
        val titleEn = json.optString("titleEn", "AI Personalized Study Routine")
        val descPt = json.optString("descPt", "Plano estruturado gerado pelo DevTutor AI.")
        val descEn = json.optString("descEn", "Structured plan crafted by DevTutor AI.")

        val sessionsArray = json.optJSONArray("sessions") ?: JSONArray()
        val sessionsList = mutableListOf<StudySession>()

        val baseDate = LocalDate.now().plusDays(1)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        for (i in 0 until sessionsArray.length()) {
            val item = sessionsArray.getJSONObject(i)
            val techId = item.optString("techId", targetTechs.firstOrNull()?.id ?: "python")
            val offset = item.optInt("dayOffset", i)
            val sessionDate = baseDate.plusDays(offset.toLong()).format(formatter)
            val sessionTime = item.optString("time", "19:00")
            val duration = item.optInt("duration", minutesPerDay)

            sessionsList.add(
                StudySession(
                    tech = Technology.fromId(techId),
                    titlePt = item.optString("titlePt", "Estudo Prático"),
                    titleEn = item.optString("titleEn", "Hands-on Study"),
                    topicPt = item.optString("topicPt", "Conceitos fundamentais"),
                    topicEn = item.optString("topicEn", "Core fundamentals"),
                    notesPt = item.optString("notesPt", "Prática no editor e exercícios."),
                    notesEn = item.optString("notesEn", "Hands-on editor practice."),
                    dateIso = sessionDate,
                    timeFormatted = sessionTime,
                    durationMinutes = duration,
                    isCompleted = false,
                    aiGenerated = true
                )
            )
        }

        if (sessionsList.isEmpty()) {
            return createFallbackRoutinePlan(targetTechs, daysCount, minutesPerDay, language, "Estudos")
        }

        return RoutinePlan(
            id = java.util.UUID.randomUUID().toString(),
            titlePt = titlePt,
            titleEn = titleEn,
            descriptionPt = descPt,
            descriptionEn = descEn,
            targetTechs = targetTechs,
            sessions = sessionsList,
            totalWeeks = (daysCount / 7).coerceAtLeast(1)
        )
    }

    private fun createFallbackRoutinePlan(
        techs: List<Technology>,
        daysCount: Int,
        minutesPerDay: Int,
        language: Language,
        goal: String
    ): RoutinePlan {
        val selected = if (techs.isEmpty()) listOf(Technology.PYTHON, Technology.SQL) else techs
        val baseDate = LocalDate.now().plusDays(1)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        val sessions = (0 until daysCount).map { i ->
            val tech = selected[i % selected.size]
            val date = baseDate.plusDays(i.toLong()).format(formatter)
            StudySession(
                tech = tech,
                titlePt = "Passo ${i + 1}: Imersão em ${tech.displayName}",
                titleEn = "Step ${i + 1}: ${tech.displayName} Immersion",
                topicPt = "Revisão e exercícios práticos de ${tech.displayName} voltados para $goal",
                topicEn = "Review and hands-on exercises in ${tech.displayName} focused on $goal",
                notesPt = "Consulte o cheatsheet e responda os testes práticos no app.",
                notesEn = "Review the cheatsheet and complete the interactive quiz in the app.",
                dateIso = date,
                timeFormatted = "19:00",
                durationMinutes = minutesPerDay,
                isCompleted = false,
                aiGenerated = true
            )
        }

        return RoutinePlan(
            id = java.util.UUID.randomUUID().toString(),
            titlePt = "Rotina Inteligente: ${selected.joinToString(" + ") { it.displayName }}",
            titleEn = "Smart Routine: ${selected.joinToString(" + ") { it.displayName }}",
            descriptionPt = "Rotina personalizada gerada para seu objetivo de estudo com foco em consistência diária.",
            descriptionEn = "Personalized study routine crafted for your goals with focus on daily consistency.",
            targetTechs = selected,
            sessions = sessions,
            totalWeeks = (daysCount / 7).coerceAtLeast(1)
        )
    }
}
