package com.example.data.repository

import com.example.data.local.QuizAttemptEntity
import com.example.data.local.SavedDiagramEntity
import com.example.data.local.StudyDao
import com.example.data.local.StudyTopicEntity
import com.example.data.remote.GeminiService
import com.example.model.DiagramData
import com.example.model.DiagramType
import com.example.model.QuizSession
import com.example.model.StudyExplanation
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio central de estudio para Repaso IA.
 *
 * PRECAUCIÓN DE ARQUITECTURA:
 * El Repository actúa como la única fuente de verdad (Single Source of Truth) para la UI.
 * Los ViewModels nunca deben interactuar directamente con la base de datos o con Retrofit/OkHttp;
 * todo pasa por este repositorio.
 */
class StudyRepository(
    private val studyDao: StudyDao,
    private val geminiService: GeminiService = GeminiService()
) {

    // Flujos reactivos de datos persistidos en Room
    val allTopics: Flow<List<StudyTopicEntity>> = studyDao.getAllTopics()
    val allQuizAttempts: Flow<List<QuizAttemptEntity>> = studyDao.getAllQuizAttempts()
    val allDiagrams: Flow<List<SavedDiagramEntity>> = studyDao.getAllDiagrams()
    val quizCount: Flow<Int> = studyDao.getQuizCount()
    val diagramCount: Flow<Int> = studyDao.getDiagramCount()

    /**
     * 1. Solicita la explicación pedagógica estructurada para Manuelito.
     */
    suspend fun getTopicExplanation(topic: String): StudyExplanation {
        val explanation = geminiService.explainTopic(topic)
        // Guardar o actualizar el tema en la base de datos local para historial
        studyDao.insertTopic(
            StudyTopicEntity(
                title = topic,
                subject = "Estudio Examen",
                summary = explanation.summary,
                lastStudiedAt = System.currentTimeMillis()
            )
        )
        return explanation
    }

    /**
     * 2. Genera y guarda un diagrama inteligente para el tema dado.
     */
    suspend fun getDiagram(topic: String, type: DiagramType): DiagramData {
        val diagram = geminiService.generateDiagram(topic, type)
        // Registrar en Room para estadísticas de repaso
        studyDao.insertDiagram(
            SavedDiagramEntity(
                topic = topic,
                diagramType = type.name,
                summary = diagram.summary,
                nodeCount = diagram.nodes.size,
                timestamp = System.currentTimeMillis()
            )
        )
        return diagram
    }

    /**
     * 3. Genera un cuestionario interactivo para el tema dado.
     */
    suspend fun getQuiz(topic: String): QuizSession {
        return geminiService.generateQuiz(topic)
    }

    /**
     * Guarda el resultado del cuestionario tras completarlo.
     */
    suspend fun recordQuizResult(topic: String, score: Int, total: Int) {
        val percentage = if (total > 0) (score * 100) / total else 0
        studyDao.insertQuizAttempt(
            QuizAttemptEntity(
                topic = topic,
                score = score,
                totalQuestions = total,
                percentage = percentage,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * Permite a Manuelito agregar nuevos temas a su lista permanente sin que se borren.
     */
    suspend fun addCustomTopic(title: String, subject: String, summary: String = ""): Long {
        return studyDao.insertTopic(
            StudyTopicEntity(
                title = title.trim(),
                subject = subject.ifBlank { "General" }.trim(),
                summary = if (summary.isNotBlank()) summary.trim() else "Tema preparado para repasar con IA, diagramas y cuestionario.",
                lastStudiedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Modifica el nombre, materia o resumen de un tema existente.
     */
    suspend fun updateTopic(id: Long, title: String, subject: String, summary: String) {
        studyDao.updateTopic(id, title.trim(), subject.trim(), summary.trim())
    }

    /**
     * Elimina un tema específico de la base de datos local.
     */
    suspend fun deleteTopic(id: Long) {
        studyDao.deleteTopicById(id)
    }

    /**
     * Solicita y valida el [SELLO DE IA DE EstudIAnte] para el tema indicado.
     */
    suspend fun getStudySeal(topic: String): com.example.model.StudySeal {
        return geminiService.generateStudySeal(topic)
    }
}
