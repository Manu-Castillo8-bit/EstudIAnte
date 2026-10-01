package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RepasoTopBar
import com.example.ui.components.StudySealDialog
import com.example.ui.screens.DiagramScreen
import com.example.ui.screens.ExplainScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.theme.RepasoIATheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.RepasoViewModel

/**
 * Actividad Principal de REPASO IA.
 *
 * CRITERIO DE ACEPTACIÓN:
 * Al abrir la app, Manuelito recibe una bienvenida clara y se le pregunta qué desea hacer,
 * sin errores en consola y con las tres funciones solicitadas:
 * 1. Agente de IA explicador.
 * 2. Generador de diagramas y gráficos inteligentes.
 * 3. Cuestionarios interactivos para repaso de examen.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: RepasoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Manejo mandatory de Edge-to-Edge para Android moderno
        enableEdgeToEdge()

        setContent {
            RepasoIATheme {
                RepasoApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RepasoApp(viewModel: RepasoViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedTopic by viewModel.selectedTopic.collectAsStateWithLifecycle()
    val topics by viewModel.topics.collectAsStateWithLifecycle()
    val quizAttempts by viewModel.quizAttempts.collectAsStateWithLifecycle()
    val savedDiagrams by viewModel.savedDiagrams.collectAsStateWithLifecycle()

    // Estados de Explicador IA
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isExplaining by viewModel.isExplaining.collectAsStateWithLifecycle()

    // Estados de Diagramas
    val currentDiagram by viewModel.currentDiagram.collectAsStateWithLifecycle()
    val selectedDiagramType by viewModel.selectedDiagramType.collectAsStateWithLifecycle()
    val isGeneratingDiagram by viewModel.isGeneratingDiagram.collectAsStateWithLifecycle()

    // Estados de Cuestionario
    val quizSession by viewModel.quizSession.collectAsStateWithLifecycle()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val selectedOptionId by viewModel.selectedOptionId.collectAsStateWithLifecycle()
    val hasAnsweredCurrent by viewModel.hasAnsweredCurrent.collectAsStateWithLifecycle()
    val quizScore by viewModel.quizScore.collectAsStateWithLifecycle()
    val isQuizCompleted by viewModel.isQuizCompleted.collectAsStateWithLifecycle()
    val isGeneratingQuiz by viewModel.isGeneratingQuiz.collectAsStateWithLifecycle()
    val quizResult by viewModel.quizResult.collectAsStateWithLifecycle()

    // Estados de [SELLO DE IA DE EstudIAnte]
    val currentSeal by viewModel.currentSeal.collectAsStateWithLifecycle()
    val isGeneratingSeal by viewModel.isGeneratingSeal.collectAsStateWithLifecycle()
    val sealError by viewModel.sealError.collectAsStateWithLifecycle()

    val topBarTitle = when (currentScreen) {
        AppScreen.HOME -> "Repaso IA"
        AppScreen.EXPLAIN -> "Tutor IA • Manuelito"
        AppScreen.DIAGRAM -> "Diagramas de Estudio"
        AppScreen.QUIZ -> "Cuestionario de Repaso"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            RepasoTopBar(
                title = topBarTitle,
                canNavigateBack = currentScreen != AppScreen.HOME,
                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) }
            )
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        when (currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(
                    selectedTopic = selectedTopic,
                    topics = topics,
                    quizAttempts = quizAttempts,
                    diagramCount = savedDiagrams.size,
                    onNavigateTo = { screen ->
                        viewModel.startTopicStudy(selectedTopic, screen)
                    },
                    onTopicSelected = { topic ->
                        viewModel.selectTopic(topic)
                    },
                    onAddTopic = { title, subject ->
                        viewModel.addNewTopic(title, subject)
                    },
                    onEditTopic = { id, title, subject, summary ->
                        viewModel.editTopic(id, title, subject, summary)
                    },
                    onDeleteTopic = { id ->
                        viewModel.deleteTopic(id)
                    },
                    onRequestSeal = { topic ->
                        viewModel.requestStudySeal(topic)
                    },
                    modifier = modifier
                )
            }

            AppScreen.EXPLAIN -> {
                ExplainScreen(
                    currentTopic = selectedTopic,
                    messages = chatMessages,
                    isExplaining = isExplaining,
                    onSendMessage = { query -> viewModel.askTutor(query) },
                    onOpenDiagram = { topic ->
                        viewModel.startTopicStudy(topic, AppScreen.DIAGRAM)
                    },
                    onOpenQuiz = { topic ->
                        viewModel.startTopicStudy(topic, AppScreen.QUIZ)
                    },
                    onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = modifier
                )
            }

            AppScreen.DIAGRAM -> {
                DiagramScreen(
                    topic = selectedTopic,
                    currentDiagram = currentDiagram,
                    selectedType = selectedDiagramType,
                    isGenerating = isGeneratingDiagram,
                    onGenerateDiagram = { topic, type ->
                        viewModel.generateDiagram(topic, type)
                    },
                    onSwitchType = { type ->
                        viewModel.switchDiagramType(type)
                    },
                    onExplainConcept = { concept ->
                        viewModel.startTopicStudy(concept, AppScreen.EXPLAIN)
                    },
                    onQuizConcept = { concept ->
                        viewModel.startTopicStudy(concept, AppScreen.QUIZ)
                    },
                    onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = modifier
                )
            }

            AppScreen.QUIZ -> {
                QuizScreen(
                    topic = selectedTopic,
                    quizSession = quizSession,
                    currentQuestionIndex = currentQuestionIndex,
                    selectedOptionId = selectedOptionId,
                    hasAnsweredCurrent = hasAnsweredCurrent,
                    score = quizScore,
                    isCompleted = isQuizCompleted,
                    quizResult = quizResult,
                    isGenerating = isGeneratingQuiz,
                    onStartQuiz = { topic -> viewModel.startQuiz(topic) },
                    onSelectOption = { option -> viewModel.selectQuizOption(option) },
                    onNextQuestion = { viewModel.nextQuizQuestion() },
                    onRetryQuiz = { viewModel.retryQuiz() },
                    onOpenExplain = { topic ->
                        viewModel.startTopicStudy(topic, AppScreen.EXPLAIN)
                    },
                    onOpenDiagram = { topic ->
                        viewModel.startTopicStudy(topic, AppScreen.DIAGRAM)
                    },
                    onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = modifier
                )
            }
        }
    }

    // Diálogo del [SELLO DE IA DE EstudIAnte] (Consumo de datos estructurados JSON)
    if (currentSeal != null || isGeneratingSeal || sealError != null) {
        StudySealDialog(
            seal = currentSeal,
            isLoading = isGeneratingSeal,
            errorMessage = sealError,
            onDismiss = { viewModel.dismissStudySeal() }
        )
    }
}
