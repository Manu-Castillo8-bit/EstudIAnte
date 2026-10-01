package com.example.model

/**
 * Representa una opción de respuesta interactiva en el cuestionario.
 */
data class QuizOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean,
    val feedback: String // Explicación inmediata de por qué es correcta o por qué es errónea
)

/**
 * Representa una pregunta de examen generada por la IA para repaso.
 */
data class QuizQuestion(
    val id: String,
    val question: String,
    val difficulty: String = "Examen", // "Básico", "Intermedio", "Examen"
    val options: List<QuizOption>,
    val explanation: String // Explicación completa para asimilar el concepto tras responder
)

/**
 * Sesión activa de cuestionario interactivo.
 */
data class QuizSession(
    val topic: String,
    val questions: List<QuizQuestion>
)

/**
 * Resumen de intento de examen para guardar en la base de datos local y mostrar resultados.
 */
data class QuizResult(
    val topic: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int = if (totalQuestions > 0) (score * 100) / totalQuestions else 0,
    val dateMillis: Long = System.currentTimeMillis()
) {
    val isApproved: Boolean get() = percentage >= 60
    val feedbackMessage: String
        get() = when {
            percentage == 100 -> "¡Perfecto, Manuelito! Dominás este tema al 100%. Estás listo para sacar la máxima nota."
            percentage >= 80 -> "¡Excelente rendimiento! Solo te faltaron pequeños detalles para la perfección."
            percentage >= 60 -> "¡Aprobado! Pero conviene revisar las respuestas incorrectas antes del examen."
            else -> "Te conviene repasar la teoría con el Explicador IA y ver el Diagrama Conceptual antes de volver a intentar."
        }
}
