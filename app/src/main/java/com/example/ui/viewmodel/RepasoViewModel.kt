package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.SavedDiagramEntity
import com.example.data.local.StudyTopicEntity
import com.example.data.repository.StudyRepository
import com.example.model.ChatMessage
import com.example.model.DiagramData
import com.example.model.DiagramType
import com.example.model.QuizOption
import com.example.model.QuizQuestion
import com.example.model.QuizResult
import com.example.model.QuizSession
import com.example.model.StudyExplanation
import com.example.model.StudySeal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Pantallas de navegación de Repaso IA.
 */
enum class AppScreen {
    HOME,       // Bienvenida a Manuelito y selección de funciones
    EXPLAIN,    // Agente de IA explicador interactivo
    DIAGRAM,    // Generador de diagramas y gráficos inteligentes
    QUIZ        // Cuestionario interactivo de repaso
}

/**
 * ViewModel principal de la aplicación.
 *
 * PRECAUCIÓN FRECUENTE:
 * Heredar de AndroidViewModel para acceder de forma segura al Application Context al instanciar
 * la base de datos Room, evitando memory leaks asociados a Activities.
 */
class RepasoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StudyRepository(db.studyDao())
    }

    // Navegación
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Tema de estudio actual seleccionado o escrito por Manuelito
    private val _selectedTopic = MutableStateFlow("La Célula y Mitosis")
    val selectedTopic: StateFlow<String> = _selectedTopic.asStateFlow()

    // Historial y estadísticas locales desde Room
    val topics: StateFlow<List<StudyTopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizAttempts: StateFlow<List<QuizAttemptEntity>> = repository.allQuizAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedDiagrams: StateFlow<List<SavedDiagramEntity>> = repository.allDiagrams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // ESTADO: 1. Agente Explicador IA
    // -------------------------------------------------------------
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome_msg",
                text = "¡Hola Manuelito! Soy tu tutor de IA. ¿Qué tema o duda querés que te explique para tu examen? Puedo darte ejemplos cotidianos, analogías y avisarte de las trampas que suelen poner los profesores.",
                isFromUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isExplaining = MutableStateFlow(false)
    val isExplaining: StateFlow<Boolean> = _isExplaining.asStateFlow()

    // -------------------------------------------------------------
    // ESTADO: 2. Generador de Diagramas y Gráficos Inteligentes
    // -------------------------------------------------------------
    private val _currentDiagram = MutableStateFlow<DiagramData?>(null)
    val currentDiagram: StateFlow<DiagramData?> = _currentDiagram.asStateFlow()

    private val _selectedDiagramType = MutableStateFlow(DiagramType.MIND_MAP)
    val selectedDiagramType: StateFlow<DiagramType> = _selectedDiagramType.asStateFlow()

    private val _isGeneratingDiagram = MutableStateFlow(false)
    val isGeneratingDiagram: StateFlow<Boolean> = _isGeneratingDiagram.asStateFlow()

    // -------------------------------------------------------------
    // ESTADO: 3. Cuestionario Interactivo para Repaso
    // -------------------------------------------------------------
    private val _quizSession = MutableStateFlow<QuizSession?>(null)
    val quizSession: StateFlow<QuizSession?> = _quizSession.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedOptionId = MutableStateFlow<String?>(null)
    val selectedOptionId: StateFlow<String?> = _selectedOptionId.asStateFlow()

    private val _hasAnsweredCurrent = MutableStateFlow(false)
    val hasAnsweredCurrent: StateFlow<Boolean> = _hasAnsweredCurrent.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _isQuizCompleted = MutableStateFlow(false)
    val isQuizCompleted: StateFlow<Boolean> = _isQuizCompleted.asStateFlow()

    private val _isGeneratingQuiz = MutableStateFlow(false)
    val isGeneratingQuiz: StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()

    private val _quizResult = MutableStateFlow<QuizResult?>(null)
    val quizResult: StateFlow<QuizResult?> = _quizResult.asStateFlow()

    // =============================================================
    // ACCIONES DE NAVEGACIÓN
    // =============================================================

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectTopic(topic: String) {
        _selectedTopic.value = topic
    }

    /**
     * Agrega un nuevo tema a la lista permanente de Manuelito en Room.
     */
    fun addNewTopic(title: String, subject: String = "General") {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addCustomTopic(title = title, subject = subject)
            _selectedTopic.value = title.trim()
        }
    }

    /**
     * Permite a Manuelito editar el nombre o materia de un tema existente.
     */
    fun editTopic(id: Long, newTitle: String, newSubject: String, newSummary: String = "") {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            repository.updateTopic(id, newTitle, newSubject, newSummary)
        }
    }

    /**
     * Permite a Manuelito borrar un tema de su lista.
     */
    fun deleteTopic(id: Long) {
        viewModelScope.launch {
            repository.deleteTopic(id)
        }
    }

    fun startTopicStudy(topic: String, screen: AppScreen) {
        _selectedTopic.value = topic
        _currentScreen.value = screen
        when (screen) {
            AppScreen.EXPLAIN -> askTutor(topic)
            AppScreen.DIAGRAM -> generateDiagram(topic, _selectedDiagramType.value)
            AppScreen.QUIZ -> startQuiz(topic)
            AppScreen.HOME -> Unit
        }
    }

    // =============================================================
    // 1. FUNCIONALIDAD: EXPLICADOR IA
    // =============================================================

    fun askTutor(questionOrTopic: String) {
        if (questionOrTopic.isBlank()) return

        val userMessage = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            text = questionOrTopic,
            isFromUser = true
        )

        val loadingMessage = ChatMessage(
            id = "loading_${System.currentTimeMillis()}",
            text = "Analizando '$questionOrTopic' y preparando la mejor explicación para tu examen...",
            isFromUser = false,
            isLoading = true
        )

        _chatMessages.value = _chatMessages.value + userMessage + loadingMessage
        _isExplaining.value = true

        viewModelScope.launch {
            try {
                val explanation = repository.getTopicExplanation(questionOrTopic)
                val tutorMessage = ChatMessage(
                    id = "bot_${System.currentTimeMillis()}",
                    text = explanation.summary,
                    isFromUser = false,
                    explanation = explanation
                )
                // Reemplazamos el loading con el mensaje final
                _chatMessages.value = _chatMessages.value.filter { !it.isLoading } + tutorMessage
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    id = "err_${System.currentTimeMillis()}",
                    text = "Ocurrió un detalle al conectar con el tutor: ${e.message}. He preparado un resumen local para continuar tu repaso.",
                    isFromUser = false,
                    isError = true
                )
                _chatMessages.value = _chatMessages.value.filter { !it.isLoading } + errorMessage
            } finally {
                _isExplaining.value = false
            }
        }
    }

    // =============================================================
    // 2. FUNCIONALIDAD: DIAGRAMAS Y GRÁFICOS INTELIGENTES
    // =============================================================

    fun generateDiagram(topic: String = _selectedTopic.value, type: DiagramType = _selectedDiagramType.value) {
        _selectedDiagramType.value = type
        _isGeneratingDiagram.value = true

        viewModelScope.launch {
            try {
                val diagram = repository.getDiagram(topic, type)
                _currentDiagram.value = diagram
            } catch (e: Exception) {
                // Fallback seguro garantizado por repository
            } finally {
                _isGeneratingDiagram.value = false
            }
        }
    }

    fun switchDiagramType(newType: DiagramType) {
        if (_selectedDiagramType.value == newType && _currentDiagram.value != null) return
        _selectedDiagramType.value = newType
        generateDiagram(_selectedTopic.value, newType)
    }

    // =============================================================
    // 3. FUNCIONALIDAD: CUESTIONARIOS INTERACTIVOS
    // =============================================================

    fun startQuiz(topic: String = _selectedTopic.value) {
        _isGeneratingQuiz.value = true
        _currentQuestionIndex.value = 0
        _selectedOptionId.value = null
        _hasAnsweredCurrent.value = false
        _quizScore.value = 0
        _isQuizCompleted.value = false
        _quizResult.value = null

        viewModelScope.launch {
            try {
                val session = repository.getQuiz(topic)
                _quizSession.value = session
            } catch (e: Exception) {
                // Fallback seguro
            } finally {
                _isGeneratingQuiz.value = false
            }
        }
    }

    fun selectQuizOption(option: QuizOption) {
        if (_hasAnsweredCurrent.value) return // Evitar cambiar respuesta después de responder
        _selectedOptionId.value = option.id
        _hasAnsweredCurrent.value = true

        if (option.isCorrect) {
            _quizScore.value += 1
        }
    }

    fun nextQuizQuestion() {
        val session = _quizSession.value ?: return
        val nextIndex = _currentQuestionIndex.value + 1

        if (nextIndex < session.questions.size) {
            _currentQuestionIndex.value = nextIndex
            _selectedOptionId.value = null
            _hasAnsweredCurrent.value = false
        } else {
            // Fin del cuestionario
            finishQuiz(session.topic, _quizScore.value, session.questions.size)
        }
    }

    private fun finishQuiz(topic: String, score: Int, total: Int) {
        _isQuizCompleted.value = true
        val result = QuizResult(topic = topic, score = score, totalQuestions = total)
        _quizResult.value = result

        // Guardar resultado en Room
        viewModelScope.launch {
            repository.recordQuizResult(topic, score, total)
        }
    }

    fun retryQuiz() {
        _currentQuestionIndex.value = 0
        _selectedOptionId.value = null
        _hasAnsweredCurrent.value = false
        _quizScore.value = 0
        _isQuizCompleted.value = false
        _quizResult.value = null
    }

    // -------------------------------------------------------------
    // ESTADO: [SELLO DE IA DE EstudIAnte]
    // -------------------------------------------------------------
    private val _currentSeal = MutableStateFlow<StudySeal?>(null)
    val currentSeal: StateFlow<StudySeal?> = _currentSeal.asStateFlow()

    private val _isGeneratingSeal = MutableStateFlow(false)
    val isGeneratingSeal: StateFlow<Boolean> = _isGeneratingSeal.asStateFlow()

    private val _sealError = MutableStateFlow<String?>(null)
    val sealError: StateFlow<String?> = _sealError.asStateFlow()

    /**
     * Solicita a Gemini el [SELLO DE IA DE EstudIAnte] con validación de responseSchema
     * y manejo de fallos sin bloquear la app.
     */
    fun requestStudySeal(topic: String) {
        viewModelScope.launch {
            _isGeneratingSeal.value = true
            _sealError.value = null
            try {
                val seal = repository.getStudySeal(topic)
                _currentSeal.value = seal
            } catch (e: Exception) {
                _sealError.value = "No se pudo conectar con el auditor de IA en este momento. Se activó el dictamen local de respaldo."
            } finally {
                _isGeneratingSeal.value = false
            }
        }
    }

    fun dismissStudySeal() {
        _currentSeal.value = null
        _sealError.value = null
    }
}
