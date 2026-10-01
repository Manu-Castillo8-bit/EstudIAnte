package com.example.data.local

import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestor de respaldo y exportación de la base de datos SQLite (Room) de Repaso IA.
 */
object BackupManager {

    /**
     * Devuelve la ruta absoluta exacta donde SQLite almacena el archivo físico en el celular.
     */
    fun getDatabaseFilePath(context: Context): String {
        return context.getDatabasePath("repaso_ia.db").absolutePath
    }

    /**
     * Exporta todos los datos guardados en SQLite a un formato JSON legible y respaldable.
     */
    suspend fun generateBackupJson(context: Context): String = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.studyDao()

        val topics = dao.getAllTopics().first()
        val quizzes = dao.getAllQuizAttempts().first()
        val diagrams = dao.getAllDiagrams().first()

        val root = JSONObject().apply {
            put("app", "Repaso IA")
            put("version", 1)
            put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            put("sqliteDatabasePath", getDatabaseFilePath(context))

            // 1. Temas de Estudio
            val topicsArray = JSONArray()
            for (t in topics) {
                topicsArray.put(JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("subject", t.subject)
                    put("summary", t.summary)
                    put("lastStudiedAt", t.lastStudiedAt)
                })
            }
            put("study_topics", topicsArray)

            // 2. Historial de Cuestionarios
            val quizzesArray = JSONArray()
            for (q in quizzes) {
                quizzesArray.put(JSONObject().apply {
                    put("id", q.id)
                    put("topic", q.topic)
                    put("score", q.score)
                    put("totalQuestions", q.totalQuestions)
                    put("percentage", q.percentage)
                    put("timestamp", q.timestamp)
                })
            }
            put("quiz_attempts", quizzesArray)

            // 3. Diagramas Guardados
            val diagramsArray = JSONArray()
            for (d in diagrams) {
                diagramsArray.put(JSONObject().apply {
                    put("id", d.id)
                    put("topic", d.topic)
                    put("diagramType", d.diagramType)
                    put("summary", d.summary)
                    put("nodeCount", d.nodeCount)
                    put("timestamp", d.timestamp)
                })
            }
            put("saved_diagrams", diagramsArray)
        }

        root.toString(2)
    }

    /**
     * Abre el menú nativo de compartir de Android para enviar el respaldo (por WhatsApp, Drive, Gmail, etc.).
     */
    fun shareBackup(context: Context, jsonContent: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, "Respaldo Repaso IA - Manuelito")
            putExtra(Intent.EXTRA_TEXT, jsonContent)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Exportar respaldo de Repaso IA a...")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
