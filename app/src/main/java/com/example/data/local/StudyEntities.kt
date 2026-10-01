package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla para almacenar los temas de estudio que Manuelito ha repasado o guardado.
 */
@Entity(tableName = "study_topics")
data class StudyTopicEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subject: String,
    val summary: String,
    val lastStudiedAt: Long = System.currentTimeMillis()
)

/**
 * Tabla para registrar el historial de cuestionarios y notas de examen.
 */
@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topic: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Tabla para registrar diagramas generados.
 */
@Entity(tableName = "saved_diagrams")
data class SavedDiagramEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topic: String,
    val diagramType: String,
    val summary: String,
    val nodeCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
