package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para las operaciones de base de datos local.
 *
 * PRECAUCIÓN FRECUENTE:
 * Nunca llamar métodos suspendidos ni queries directamente desde funciones @Composable.
 * Deben ser consumidas en el ViewModel mediante Coroutines o Flow.collectAsStateWithLifecycle.
 */
@Dao
interface StudyDao {

    // --- Temas de Estudio ---
    @Query("SELECT * FROM study_topics ORDER BY lastStudiedAt DESC")
    fun getAllTopics(): Flow<List<StudyTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: StudyTopicEntity): Long

    @Query("DELETE FROM study_topics WHERE id = :id")
    suspend fun deleteTopicById(id: Long)

    // --- Cuestionarios de Repaso ---
    @Query("SELECT * FROM quiz_attempts ORDER BY timestamp DESC")
    fun getAllQuizAttempts(): Flow<List<QuizAttemptEntity>>

    @Query("SELECT * FROM quiz_attempts WHERE topic = :topic ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestAttemptForTopic(topic: String): QuizAttemptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttemptEntity): Long

    // --- Diagramas Guardados ---
    @Query("SELECT * FROM saved_diagrams ORDER BY timestamp DESC")
    fun getAllDiagrams(): Flow<List<SavedDiagramEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagram(diagram: SavedDiagramEntity): Long

    @Query("SELECT COUNT(*) FROM quiz_attempts")
    fun getQuizCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM saved_diagrams")
    fun getDiagramCount(): Flow<Int>
}
