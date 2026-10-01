package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos Room local para Repaso IA.
 *
 * PRECAUCIÓN:
 * Usar `exportSchema = false` para evitar advertencias de compilación cuando no se define
 * un directorio de esquema en Gradle.
 * `fallbackToDestructiveMigration()` asegura que si en desarrollo cambia un campo de la entidad,
 * la app no falle al arrancar.
 */
@Database(
    entities = [
        StudyTopicEntity::class,
        QuizAttemptEntity::class,
        SavedDiagramEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "repaso_ia.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Callback para sembrar datos iniciales de estudio para Manuelito en el primer inicio.
     */
    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-poblar temas iniciales para que Manuelito pueda probar la app de inmediato
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).studyDao()
                dao.insertTopic(
                    StudyTopicEntity(
                        title = "La Célula y Mitosis",
                        subject = "Biología",
                        summary = "Fases de la división celular: profase, metafase, anafase y telofase."
                    )
                )
                dao.insertTopic(
                    StudyTopicEntity(
                        title = "Leyes de Newton",
                        subject = "Física",
                        summary = "Inercia, fuerza igual a masa por aceleración, y acción-reacción."
                    )
                )
                dao.insertTopic(
                    StudyTopicEntity(
                        title = "Revolución Industrial",
                        subject = "Historia",
                        summary = "Transformación socioeconómica, máquina de vapor y auge urbano."
                    )
                )
                dao.insertTopic(
                    StudyTopicEntity(
                        title = "Algoritmos y Estructuras de Datos",
                        subject = "Programación",
                        summary = "Complejidad Big-O, arrays, listas enlazadas, árboles binarios y grafos."
                    )
                )
            }
        }
    }
}
