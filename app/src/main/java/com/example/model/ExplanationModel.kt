package com.example.model

/**
 * Modelos de datos para el agente explicador de IA orientado a estudiantes.
 *
 * NOTA CRÍTICA:
 * Estructurar la explicación en partes fijas (definición, analogía, puntos de examen, trampas)
 * evita que el estudiante se abrume con bloques gigantescos de texto plano difíciles de memorizar.
 */
data class StudyExplanation(
    val topic: String,
    val summary: String,
    val simpleDefinition: String,
    val analogy: String,
    val keyPoints: List<String>,
    val examTraps: List<String>,
    val checkQuestion: String? = null,
    val checkAnswer: String? = null
)

/**
 * Representa un mensaje en la conversación interactiva con el tutor IA.
 */
data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val explanation: StudyExplanation? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)
