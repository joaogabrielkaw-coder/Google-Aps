package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Language
import com.example.ui.theme.LocalDevRoutineColors

private val PurplePrimary: Color
    @Composable get() = LocalDevRoutineColors.current.primary

private val PurplePrimaryLight: Color
    @Composable get() = LocalDevRoutineColors.current.primaryLight

private val RedAccent: Color
    @Composable get() = LocalDevRoutineColors.current.accent

data class DiagnosticQuestion(
    val id: Int,
    val topicPt: String,
    val topicEn: String,
    val questionPt: String,
    val questionEn: String,
    val optionsPt: List<String>,
    val optionsEn: List<String>,
    val correctIndex: Int,
    val explanationPt: String,
    val explanationEn: String
)

val DIAGNOSTIC_QUESTIONS = listOf(
    DiagnosticQuestion(
        id = 1,
        topicPt = "Lógica & Algoritmos",
        topicEn = "Logic & Algorithms",
        questionPt = "Qual é a complexidade de tempo assintótica da busca binária em uma lista ordenada de tamanho N?",
        questionEn = "What is the asymptotic time complexity of binary search in a sorted array of size N?",
        optionsPt = listOf("O(1) constante", "O(log N) logarítmica", "O(N) linear", "O(N²) quadrática"),
        optionsEn = listOf("O(1) constant", "O(log N) logarithmic", "O(N) linear", "O(N²) quadratic"),
        correctIndex = 1,
        explanationPt = "A cada comparação, a busca binária reduz o espaço de busca pela metade, resultando em O(log N).",
        explanationEn = "Binary search divides the search interval in half every iteration, yielding O(log N)."
    ),
    DiagnosticQuestion(
        id = 2,
        topicPt = "Estruturas de Dados",
        topicEn = "Data Structures",
        questionPt = "Qual estrutura de dados opera sob o princípio LIFO (Last-In, First-Out)?",
        questionEn = "Which data structure operates on the LIFO (Last-In, First-Out) principle?",
        optionsPt = listOf("Fila (Queue)", "Pilha (Stack)", "Tabela Hash", "Lista Encadeada"),
        optionsEn = listOf("Queue", "Stack", "Hash Table", "Linked List"),
        correctIndex = 1,
        explanationPt = "A Pilha (Stack) insere e remove elementos sempre pelo topo (o último a entrar é o primeiro a sair).",
        explanationEn = "A Stack pushes and pops items exclusively from the top (last in, first out)."
    ),
    DiagnosticQuestion(
        id = 3,
        topicPt = "Bancos de Dados & SQL",
        topicEn = "Databases & SQL",
        questionPt = "Em SQL relacional, qual cláusula une registros retornando apenas linhas com correspondência exata em ambas as tabelas?",
        questionEn = "In relational SQL, which clause joins tables returning only rows that match in both tables?",
        optionsPt = listOf("LEFT JOIN", "INNER JOIN", "FULL OUTER JOIN", "CROSS JOIN"),
        optionsEn = listOf("LEFT JOIN", "INNER JOIN", "FULL OUTER JOIN", "CROSS JOIN"),
        correctIndex = 1,
        explanationPt = "O INNER JOIN filtra apenas as linhas em que a condição de junção é satisfeita em ambas as tabelas.",
        explanationEn = "INNER JOIN keeps exclusively the rows where the ON condition is satisfied in both tables."
    ),
    DiagnosticQuestion(
        id = 4,
        topicPt = "Boas Práticas & Clean Code",
        topicEn = "Clean Code & Best Practices",
        questionPt = "O que estabelece o princípio DRY em engenharia de software?",
        questionEn = "What does the DRY principle state in software engineering?",
        optionsPt = listOf(
            "Do Right Yearly (Fazer certo anualmente)",
            "Don't Repeat Yourself (Evitar duplicação desnecessária de código)",
            "Deploy Rapidly Yesterday (Publicar rápido)",
            "Direct Relational Yield (Rendimento direto)"
        ),
        optionsEn = listOf(
            "Do Right Yearly",
            "Don't Repeat Yourself (Avoid duplicate logic/abstractions)",
            "Deploy Rapidly Yesterday",
            "Direct Relational Yield"
        ),
        correctIndex = 1,
        explanationPt = "DRY prega que cada parte do conhecimento em um sistema deve ter uma representação única e inequívoca.",
        explanationEn = "DRY states that every piece of knowledge must have a single, unambiguous representation."
    ),
    DiagnosticQuestion(
        id = 5,
        topicPt = "Arquitetura & Versionamento",
        topicEn = "Architecture & Git",
        questionPt = "Qual comando do Git cria uma nova branch e alterna imediatamente para ela?",
        questionEn = "Which Git command creates a new branch and immediately switches to it?",
        optionsPt = listOf("git checkout -b <nome>", "git branch -d <nome>", "git merge <nome>", "git commit -m <nome>"),
        optionsEn = listOf("git checkout -b <name>", "git branch -d <name>", "git merge <name>", "git commit -m <name>"),
        correctIndex = 0,
        explanationPt = "A flag '-b' instrui o git checkout a criar o branch caso ele ainda não exista antes da alternância.",
        explanationEn = "The '-b' flag instructs git checkout to create the branch before switching to it."
    )
)

/**
 * Diálogo de Desafios Iniciais de Diagnóstico Dev.
 * Aparece na primeira vez em que o usuário abre o aplicativo ou sob demanda,
 * calibrando seu título inicial e nível de desenvolvedor com XP e emblema.
 */
@Composable
fun InitialAssessmentDialog(
    language: Language,
    onComplete: (levelNumber: Int, titlePt: String, titleEn: String, xpAwarded: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val isPt = language == Language.PT
    var currentIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() }
    var isSubmitted by remember { mutableStateOf(false) }

    val totalQuestions = DIAGNOSTIC_QUESTIONS.size
    val currentQuestion = DIAGNOSTIC_QUESTIONS[currentIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("dialog_initial_assessment"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isSubmitted) {
                    // Header com banner de diagnóstico
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PurplePrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = PurplePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isPt) "Diagnóstico Inicial Dev" else "Initial Dev Diagnostic",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (isPt) "Calibre seu Nível e Título (Opcional)" else "Calibrate your Level & Title (Optional)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp).testTag("btn_close_diagnostic")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fazer Depois", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Banner explícito de NÃO OBRIGATÓRIO
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PurplePrimary.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimaryLight.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = PurplePrimaryLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPt) "✨ Totalmente Opcional! Não é obrigatório — você pode fazer agora ou deixar para depois a qualquer momento."
                                else "✨ Completely Optional! Not mandatory — you can do it now or leave it for later anytime.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Barra de Progresso do Diagnóstico
                    val progress = (currentIndex + 1).toFloat() / totalQuestions.toFloat()
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RedAccent,
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isPt) "Desafio ${currentIndex + 1} de $totalQuestions" else "Challenge ${currentIndex + 1} of $totalQuestions",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedAccent
                        )
                        Text(
                            text = if (isPt) currentQuestion.topicPt else currentQuestion.topicEn,
                            fontSize = 11.sp,
                            color = PurplePrimaryLight,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pergunta
                    Text(
                        text = if (isPt) currentQuestion.questionPt else currentQuestion.questionEn,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Opções
                    val selectedOption = userAnswers[currentQuestion.id]
                    val options = if (isPt) currentQuestion.optionsPt else currentQuestion.optionsEn

                    options.forEachIndexed { optIndex, optionText ->
                        val isOptionSelected = selectedOption == optIndex
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOptionSelected) PurplePrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isOptionSelected) 1.5.dp else 1.dp,
                                color = if (isOptionSelected) PurplePrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    userAnswers[currentQuestion.id] = optIndex
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isOptionSelected,
                                    onClick = { userAnswers[currentQuestion.id] = optIndex },
                                    colors = RadioButtonDefaults.colors(selectedColor = PurplePrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = optionText,
                                    fontSize = 13.sp,
                                    fontWeight = if (isOptionSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isOptionSelected) PurplePrimaryLight else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botões de Navegação entre perguntas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentIndex > 0) {
                            OutlinedButton(
                                onClick = { currentIndex-- },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isPt) "Anterior" else "Previous")
                            }
                        } else {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isPt) "Fazer Depois" else "Skip for Now")
                            }
                        }

                        if (currentIndex < totalQuestions - 1) {
                            Button(
                                onClick = { currentIndex++ },
                                enabled = userAnswers.containsKey(currentQuestion.id),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                            ) {
                                Text(if (isPt) "Próximo" else "Next")
                            }
                        } else {
                            Button(
                                onClick = { isSubmitted = true },
                                enabled = userAnswers.size == totalQuestions,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RedAccent),
                                modifier = Modifier.testTag("btn_finish_diagnostic")
                            ) {
                                Icon(Icons.Default.MilitaryTech, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isPt) "Calcular Meu Nível" else "Calculate My Level",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_defer_diagnostic")
                    ) {
                        Text(
                            text = if (isPt) "Pular e Fazer Depois (Não obrigatório)" else "Skip and Do It Later (Not mandatory)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // TELA DE RESULTADO DO DIAGNÓSTICO
                    val correctCount = DIAGNOSTIC_QUESTIONS.count { q ->
                        userAnswers[q.id] == q.correctIndex
                    }

                    val xpReward = when (correctCount) {
                        5 -> 280
                        4 -> 200
                        3 -> 140
                        2 -> 90
                        else -> 50
                    }
                    val resolvedTier = com.example.model.DeveloperLevelCatalog.resolveForXp(xpReward)
                    val calibratedLevel = resolvedTier.levelNumber
                    val titlePt = resolvedTier.titlePt
                    val titleEn = resolvedTier.titleEn

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(PurplePrimary, RedAccent))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isPt) "🎉 Diagnóstico Concluído!" else "🎉 Diagnostic Complete!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isPt) "Você acertou $correctCount de $totalQuestions desafios conceituais"
                        else "You answered $correctCount of $totalQuestions challenges correctly",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Card com o Nível e Título Calibrados
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PurplePrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, PurplePrimaryLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isPt) "SEU TÍTULO INICIAL:" else "YOUR STARTING TITLE:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurplePrimaryLight,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isPt) titlePt else titleEn,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = RedAccent.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "NÍVEL $calibratedLevel",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = RedAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1B4D2B)
                                ) {
                                    Text(
                                        text = "+$xpReward XP Bônus",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color(0xFF80FFAB),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badge Desbloqueada de Reconhecimento
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB300).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isPt) "Medalha Conquistada:" else "Badge Earned:",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isPt) "Diagnóstico Dev Inicial" else "Initial Dev Diagnostic",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onComplete(calibratedLevel, titlePt, titleEn, xpReward)
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_accept_diagnostic_result"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPt) "Começar Minha Jornada Dev" else "Start My Dev Journey",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
