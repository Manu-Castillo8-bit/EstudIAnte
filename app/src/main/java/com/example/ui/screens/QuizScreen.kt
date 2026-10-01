package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuizOption
import com.example.model.QuizQuestion
import com.example.model.QuizResult
import com.example.model.QuizSession
import com.example.ui.theme.RepasoError
import com.example.ui.theme.RepasoErrorContainer
import com.example.ui.theme.RepasoSuccess

/**
 * Pantalla de Cuestionario Interactivo para Repaso de Exámenes.
 *
 * PRECAUCIÓN DE ESTADO:
 * 1. Bloquear cambios de opción una vez que el usuario ya seleccionó una respuesta
 *    (`hasAnsweredCurrent`), para evitar trampas en la puntuación del examen.
 * 2. Dar retroalimentación pedagógica instantánea (explicando no solo qué está bien, sino
 *    por qué las alternativas incorrectas son erróneas).
 */
@Composable
fun QuizScreen(
    topic: String,
    quizSession: QuizSession?,
    currentQuestionIndex: Int,
    selectedOptionId: String?,
    hasAnsweredCurrent: Boolean,
    score: Int,
    isCompleted: Boolean,
    quizResult: QuizResult?,
    isGenerating: Boolean,
    onStartQuiz: (String) -> Unit,
    onSelectOption: (QuizOption) -> Unit,
    onNextQuestion: () -> Unit,
    onRetryQuiz: () -> Unit,
    onOpenExplain: (String) -> Unit,
    onOpenDiagram: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(topic) {
        if (quizSession == null || quizSession.topic != topic) {
            onStartQuiz(topic)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("quiz_screen")
    ) {
        if (isGenerating) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Preparando preguntas de examen para $topic...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Redactando opciones y explicaciones pedagógicas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (isCompleted && quizResult != null) {
            // PANTALLA DE RESULTADOS DEL EXAMEN
            QuizResultsView(
                result = quizResult,
                onRetry = onRetryQuiz,
                onNewQuiz = { onStartQuiz(topic) },
                onOpenExplain = { onOpenExplain(topic) },
                onOpenDiagram = { onOpenDiagram(topic) },
                onFinish = onNavigateBack
            )
        } else if (quizSession != null && quizSession.questions.isNotEmpty()) {
            val question = quizSession.questions.getOrNull(currentQuestionIndex)
            if (question != null) {
                QuizQuestionView(
                    topic = topic,
                    question = question,
                    questionNumber = currentQuestionIndex + 1,
                    totalQuestions = quizSession.questions.size,
                    selectedOptionId = selectedOptionId,
                    hasAnswered = hasAnsweredCurrent,
                    onOptionSelected = onSelectOption,
                    onNext = onNextQuestion
                )
            }
        }
    }
}

@Composable
private fun QuizQuestionView(
    topic: String,
    question: QuizQuestion,
    questionNumber: Int,
    totalQuestions: Int,
    selectedOptionId: String?,
    hasAnswered: Boolean,
    onOptionSelected: (QuizOption) -> Unit,
    onNext: () -> Unit
) {
    val progress = questionNumber.toFloat() / totalQuestions.toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado de Progreso
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Simulacro: $topic",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pregunta $questionNumber de $totalQuestions",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Tarjeta de la Pregunta
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("question_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = "Nivel: ${question.difficulty}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // Opciones Interactivas
        items(question.options.size) { index ->
            val option = question.options[index]
            val isSelected = selectedOptionId == option.id

            val (bgColor, borderColor, textColor) = when {
                !hasAnswered -> Triple(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    MaterialTheme.colorScheme.onSurface
                )
                option.isCorrect -> Triple(
                    RepasoSuccess.copy(alpha = 0.15f),
                    RepasoSuccess,
                    RepasoSuccess
                )
                isSelected && !option.isCorrect -> Triple(
                    RepasoErrorContainer,
                    RepasoError,
                    RepasoError
                )
                else -> Triple(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !hasAnswered) { onOptionSelected(option) }
                    .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                    .testTag("quiz_option_${option.id}"),
                shape = RoundedCornerShape(16.dp),
                color = bgColor
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letra de la opción (A, B, C, D)
                        Surface(
                            shape = CircleShape,
                            color = borderColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ('A'.code + index).toChar().toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = if (hasAnswered && (option.isCorrect || isSelected)) borderColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )

                        // Icono de validación
                        if (hasAnswered) {
                            if (option.isCorrect) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Correcto",
                                    tint = RepasoSuccess,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Incorrecto",
                                    tint = RepasoError,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // Justificación inmediata de la opción
                    if (hasAnswered && (isSelected || option.isCorrect)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = option.feedback,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (option.isCorrect) RepasoSuccess else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Explicación global de la pregunta (aparece tras responder)
        if (hasAnswered) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Explicación para asimilar:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Botón de Siguiente Pregunta
            item {
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_question_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = if (questionNumber < totalQuestions) "Siguiente Pregunta" else "Ver Resultados del Examen",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun QuizResultsView(
    result: QuizResult,
    onRetry: () -> Unit,
    onNewQuiz: () -> Unit,
    onOpenExplain: () -> Unit,
    onOpenDiagram: () -> Unit,
    onFinish: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("quiz_results_view"),
        contentPadding = PaddingValues(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                shape = CircleShape,
                color = if (result.isApproved) RepasoSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (result.isApproved) Icons.Default.EmojiEvents else Icons.Default.School,
                        contentDescription = null,
                        tint = if (result.isApproved) RepasoSuccess else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = if (result.isApproved) "¡Excelente trabajo, Manuelito!" else "¡Buen intento, Manuelito!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tema evaluado: ${result.topic}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Puntuación
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${result.score} de ${result.totalQuestions} Correctas",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Calificación: ${result.percentage}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (result.isApproved) RepasoSuccess else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = result.feedbackMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Acciones recomendadas tras el resultado
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ÚNICO BOTÓN PRINCIPAL (FILLED) DE ESTA PANTALLA
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reintentar este Cuestionario", style = MaterialTheme.typography.titleSmall)
                }

                // TODOS LOS DEMÁS BOTONES SON SECUNDARIOS (OUTLINED)
                OutlinedButton(
                    onClick = onNewQuiz,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("Generar Nuevas Preguntas con IA", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenDiagram,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Diagrama", style = MaterialTheme.typography.bodyMedium)
                    }

                    OutlinedButton(
                        onClick = onOpenExplain,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tutor IA", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                OutlinedButton(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text("Volver al Inicio", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}
