package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.model.DiagramConnection
import com.example.model.DiagramData
import com.example.model.DiagramNode
import com.example.model.DiagramType
import com.example.model.QuizOption
import com.example.model.QuizQuestion
import com.example.model.QuizSession
import com.example.model.StudyExplanation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Servicio encargado de la comunicación con la API de Gemini (gemini-3.5-flash)
 * y de la generación pedagógica de respaldo local cuando no hay conexión o no se ha
 * configurado la API key en el panel de Secrets.
 *
 * PRECAUCIÓN CRÍTICA:
 * 1. NUNCA ejecutar llamadas de red en el hilo principal (Main Thread). Se envuelve todo en `Dispatchers.IO`.
 * 2. Las llamadas a modelos de IA pueden tardar más de 10s en responder; OkHttpClient DEBE tener un timeout de al menos 60s.
 * 3. En caso de fallo de red o clave no configurada, NUNCA dejar la app rota o en blanco: el generador pedagógico
 *    local provee una respuesta completa y estructurada en español para Manuelito.
 */
class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Comprueba si la API key está presente y no es un marcador de posición.
     */
    private fun isApiKeyConfigured(): Boolean {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            key.isNotBlank() && key != "MY_GEMINI_API_KEY"
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * 1. Solicita al agente de IA una explicación detallada y orientada a examen.
     */
    suspend fun explainTopic(topic: String): StudyExplanation = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext generateLocalExplanation(topic)
        }

        try {
            val prompt = """
                Sos un tutor pedagógico experto para Manuelito, un estudiante que está preparando un examen exigente.
                El tema de estudio es: "$topic".
                Devuelve ÚNICAMENTE un objeto JSON válido (sin formato markdown ni comillas invertidas) con este esquema exacto:
                {
                  "simpleDefinition": "Definición clara y precisa en palabras sencillas",
                  "analogy": "Analogía o ejemplo de la vida cotidiana para memorizarlo fácilmente",
                  "keyPoints": ["Punto clave 1 para el examen", "Punto clave 2 para el examen", "Punto clave 3 para el examen", "Punto clave 4"],
                  "examTraps": ["Trampa típica o error frecuente que suelen cometer los estudiantes", "Confusión conceptual típica"],
                  "checkQuestion": "Pregunta rápida de autoevaluación",
                  "checkAnswer": "Respuesta correcta a la pregunta de autoevaluación"
                }
            """.trimIndent()

            val responseText = callGeminiRaw(prompt)
            parseExplanationJson(topic, responseText)
        } catch (e: Exception) {
            Log.w("GeminiService", "Error consultando Gemini para explicación, usando motor local: ${e.message}")
            generateLocalExplanation(topic)
        }
    }

    /**
     * 2. Genera la estructura de nodos y conexiones para un diagrama inteligente.
     */
    suspend fun generateDiagram(topic: String, type: DiagramType): DiagramData = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext generateLocalDiagram(topic, type)
        }

        try {
            val typeDesc = when (type) {
                DiagramType.MIND_MAP -> "mapa conceptual con nodo central y ramas radiales de conceptos"
                DiagramType.FLOWCHART -> "diagrama de flujo paso a paso con secuencia de fases o causas y efectos"
                DiagramType.HIERARCHY_TREE -> "árbol jerárquico piramidal con nodo raíz y subcategorías"
                DiagramType.SMART_METRICS -> "gráfico comparativo con puntuación de relevancia para el examen"
            }

            val prompt = """
                Sos un generador de esquemas visuales para estudiantes.
                Crea un $typeDesc para el tema: "$topic".
                Devuelve ÚNICAMENTE un objeto JSON válido con este formato:
                {
                  "summary": "Breve resumen explicativo del esquema (2 líneas)",
                  "nodes": [
                    { "id": "1", "title": "Título corto (max 4 palabras)", "description": "Explicación del nodo para el examen", "level": 0, "category": "Tema Central", "score": 95 },
                    { "id": "2", "title": "Subtema A", "description": "Detalle...", "level": 1, "category": "Fase", "score": 85 }
                  ],
                  "connections": [
                    { "fromId": "1", "toId": "2", "label": "origina" }
                  ]
                }
                Incluye entre 5 y 8 nodos significativos.
            """.trimIndent()

            val responseText = callGeminiRaw(prompt)
            parseDiagramJson(topic, type, responseText)
        } catch (e: Exception) {
            Log.w("GeminiService", "Error consultando Gemini para diagrama, usando motor local: ${e.message}")
            generateLocalDiagram(topic, type)
        }
    }

    /**
     * 3. Genera un cuestionario interactivo de repaso con retroalimentación inmediata.
     */
    suspend fun generateQuiz(topic: String): QuizSession = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext generateLocalQuiz(topic)
        }

        try {
            val prompt = """
                Sos un profesor que prepara un simulacro de examen para Manuelito sobre el tema: "$topic".
                Crea un cuestionario interactivo de 4 preguntas de opción múltiple con 4 alternativas cada una.
                Devuelve ÚNICAMENTE un objeto JSON válido con este esquema exacto:
                {
                  "questions": [
                    {
                      "id": "q1",
                      "question": "¿Enunciado de la pregunta de examen?",
                      "difficulty": "Examen",
                      "options": [
                        { "id": "opt1", "text": "Opción A", "isCorrect": false, "feedback": "Incorrecto porque..." },
                        { "id": "opt2", "text": "Opción B correcta", "isCorrect": true, "feedback": "¡Excelente! Justificación..." },
                        { "id": "opt3", "text": "Opción C", "isCorrect": false, "feedback": "Incorrecto..." },
                        { "id": "opt4", "text": "Opción D", "isCorrect": false, "feedback": "Incorrecto..." }
                      ],
                      "explanation": "Explicación completa de todo el concepto para fijar el aprendizaje."
                    }
                  ]
                }
            """.trimIndent()

            val responseText = callGeminiRaw(prompt)
            parseQuizJson(topic, responseText)
        } catch (e: Exception) {
            Log.w("GeminiService", "Error consultando Gemini para cuestionario, usando motor local: ${e.message}")
            generateLocalQuiz(topic)
        }
    }

    /**
     * Ejecuta la llamada REST hacia Gemini 3.5 Flash.
     */
    private fun callGeminiRaw(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contents = JSONArray().apply {
                val userContent = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", parts)
                }
                put(userContent)
            }
            put("contents", contents)

            val generationConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.4)
            }
            put("generationConfig", generationConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("HTTP ${response.code}: ${response.message}")
        }

        val responseBody = response.body?.string().orEmpty()
        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates")
        val candidate = candidates?.optJSONObject(0)
        val content = candidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val firstPart = parts?.optJSONObject(0)
        return firstPart?.optString("text").orEmpty()
    }

    // =========================================================================
    // PARSERS JSON SEGUROS (Manejo de errores contra alucinaciones de formato)
    // =========================================================================

    private fun parseExplanationJson(topic: String, rawJson: String): StudyExplanation {
        val clean = cleanJson(rawJson)
        val json = JSONObject(clean)
        val keyPoints = mutableListOf<String>()
        val keyPointsArray = json.optJSONArray("keyPoints")
        if (keyPointsArray != null) {
            for (i in 0 until keyPointsArray.length()) {
                keyPoints.add(keyPointsArray.getString(i))
            }
        }
        val examTraps = mutableListOf<String>()
        val trapsArray = json.optJSONArray("examTraps")
        if (trapsArray != null) {
            for (i in 0 until trapsArray.length()) {
                examTraps.add(trapsArray.getString(i))
            }
        }

        return StudyExplanation(
            topic = topic,
            summary = json.optString("simpleDefinition", "Resumen preparado para el examen."),
            simpleDefinition = json.optString("simpleDefinition", "Concepto fundamental para el examen."),
            analogy = json.optString("analogy", "Imagina un sistema interconectado donde cada parte cumple su función."),
            keyPoints = if (keyPoints.isNotEmpty()) keyPoints else listOf("Comprender la definición base", "Identificar los componentes principales", "Relacionar con preguntas típicas"),
            examTraps = if (examTraps.isNotEmpty()) examTraps else listOf("No confundir causas con consecuencias", "Atención a excepciones teóricas"),
            checkQuestion = json.optString("checkQuestion", "¿Cuál es la idea central de este tema?"),
            checkAnswer = json.optString("checkAnswer", "Verifica si recordaste los elementos principales.")
        )
    }

    private fun parseDiagramJson(topic: String, type: DiagramType, rawJson: String): DiagramData {
        val clean = cleanJson(rawJson)
        val json = JSONObject(clean)
        val summary = json.optString("summary", "Esquema conceptual estructurado de $topic.")
        val nodes = mutableListOf<DiagramNode>()
        val nodesArray = json.optJSONArray("nodes")
        if (nodesArray != null) {
            for (i in 0 until nodesArray.length()) {
                val nodeObj = nodesArray.getJSONObject(i)
                nodes.add(
                    DiagramNode(
                        id = nodeObj.optString("id", (i + 1).toString()),
                        title = nodeObj.optString("title", "Concepto ${i + 1}"),
                        description = nodeObj.optString("description", "Detalle clave"),
                        level = nodeObj.optInt("level", if (i == 0) 0 else 1),
                        category = nodeObj.optString("category", "General"),
                        examImportanceScore = nodeObj.optInt("score", 85)
                    )
                )
            }
        }

        val connections = mutableListOf<DiagramConnection>()
        val connArray = json.optJSONArray("connections")
        if (connArray != null) {
            for (i in 0 until connArray.length()) {
                val cObj = connArray.getJSONObject(i)
                connections.add(
                    DiagramConnection(
                        fromId = cObj.optString("fromId", "1"),
                        toId = cObj.optString("toId", "2"),
                        label = cObj.optString("label").ifEmpty { null }
                    )
                )
            }
        }

        return if (nodes.isNotEmpty()) {
            DiagramData(topic, type, summary, nodes, connections)
        } else {
            generateLocalDiagram(topic, type)
        }
    }

    private fun parseQuizJson(topic: String, rawJson: String): QuizSession {
        val clean = cleanJson(rawJson)
        val json = JSONObject(clean)
        val questions = mutableListOf<QuizQuestion>()
        val qArray = json.optJSONArray("questions")
        if (qArray != null) {
            for (i in 0 until qArray.length()) {
                val qObj = qArray.getJSONObject(i)
                val options = mutableListOf<QuizOption>()
                val optArray = qObj.optJSONArray("options")
                if (optArray != null) {
                    for (j in 0 until optArray.length()) {
                        val optObj = optArray.getJSONObject(j)
                        options.add(
                            QuizOption(
                                id = optObj.optString("id", "opt_$j"),
                                text = optObj.optString("text", "Opción"),
                                isCorrect = optObj.optBoolean("isCorrect", j == 0),
                                feedback = optObj.optString("feedback", "Respuesta evaluada.")
                            )
                        )
                    }
                }
                questions.add(
                    QuizQuestion(
                        id = qObj.optString("id", "q_$i"),
                        question = qObj.optString("question", "¿Pregunta de examen?"),
                        difficulty = qObj.optString("difficulty", "Examen"),
                        options = options,
                        explanation = qObj.optString("explanation", "Explicación teórica de la respuesta correcta.")
                    )
                )
            }
        }
        return if (questions.isNotEmpty()) {
            QuizSession(topic, questions)
        } else {
            generateLocalQuiz(topic)
        }
    }

    private fun cleanJson(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        }
        if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    // =========================================================================
    // MOTOR PEDAGÓGICO DE RESPALDO (Sin dependencias externas, siempre disponible)
    // =========================================================================

    /**
     * Generador local que proporciona respuestas didácticas completas para Manuelito.
     */
    fun generateLocalExplanation(topic: String): StudyExplanation {
        val t = topic.lowercase().trim()
        return when {
            t.contains("célula") || t.contains("celula") || t.contains("mitosis") -> {
                StudyExplanation(
                    topic = topic,
                    summary = "La mitosis es el proceso biológico por el cual una célula madre eucariota se divide en dos células hijas genéticamente idénticas.",
                    simpleDefinition = "Es el mecanismo de multiplicación celular que usan los organismos para crecer y reparar tejidos, manteniendo intacto el ADN original.",
                    analogy = "Imagina que tienes un manual de instrucciones perfecto (ADN). Antes de remodelar la casa, sacas una fotocopia idéntica y le entregas un manual completo a cada obrero.",
                    keyPoints = listOf(
                        "Consta de 4 fases principales: Profase, Metafase, Anafase y Telofase (regla nemotécnica: 'PROMETE A TELO').",
                        "En la Metafase los cromosomas se alinean exactamente en el plano ecuatorial de la célula.",
                        "En la Anafase las cromátidas hermanas se separan hacia polos opuestos gracias al huso mitótico.",
                        "El resultado final son 2 células diploides (2n) idénticas entre sí."
                    ),
                    examTraps = listOf(
                        "¡OJO EN EL EXAMEN!: No confundir MITOSIS (2 células diploides idénticas para tejidos) con MEIOSIS (4 células haploides con variabilidad para gametos sexuales).",
                        "Los cromosomas no se duplican en la mitosis: se duplican antes, en la fase S de la interfase."
                    ),
                    checkQuestion = "¿En qué fase de la mitosis se alinean los cromosomas en el centro de la célula?",
                    checkAnswer = "En la Metafase (recuerda: M de Metafase = M de 'Medio/Centro')."
                )
            }
            t.contains("newton") || t.contains("fuerza") || t.contains("inercia") -> {
                StudyExplanation(
                    topic = topic,
                    summary = "Las 3 leyes de Newton son los pilares de la mecánica clásica que describen cómo interactúan las fuerzas con el movimiento de los cuerpos.",
                    simpleDefinition = "Son 3 reglas universales que explican por qué las cosas están quietas, cómo se aceleran cuando las empujas y por qué toda fuerza genera una respuesta igual en sentido contrario.",
                    analogy = "Cuando vas en colectivo y el conductor frena de golpe, tu cuerpo sigue yéndose hacia adelante: eso es la Primera Ley (inercia pura).",
                    keyPoints = listOf(
                        "1ª Ley (Inercia): Todo cuerpo permanece en reposo o velocidad constante a menos que una fuerza neta actúe sobre él.",
                        "2ª Ley (Fuerza fundamental): F = m · a. La aceleración es directamente proporcional a la fuerza e inversamente a la masa.",
                        "3ª Ley (Acción y Reacción): A toda acción le corresponde una reacción igual y en sentido opuesto, pero aplicadas en cuerpos distintos."
                    ),
                    examTraps = listOf(
                        "¡TRAMPA TÍPICA!: En la 3ª ley, las fuerzas de acción y reacción NUNCA se anulan entre sí porque actúan sobre cuerpos diferentes.",
                        "Masa (kg) no es lo mismo que peso (Newtons). El peso depende de la gravedad local (P = m · g)."
                    ),
                    checkQuestion = "Si empujas una pared con una fuerza de 50 N, ¿con qué fuerza te empuja la pared a ti?",
                    checkAnswer = "Con exactamente 50 N en sentido contrario (3ª Ley de Newton)."
                )
            }
            t.contains("revolución") || t.contains("revolucion") || t.contains("industrial") || t.contains("historia") -> {
                StudyExplanation(
                    topic = topic,
                    summary = "La Revolución Industrial fue el salto tecnológico y económico del trabajo manual y artesanal a la producción mecanizada en fábricas a gran escala.",
                    simpleDefinition = "El paso de una economía agraria y rural a una economía industrializada y urbana, iniciada en Gran Bretaña a mediados del siglo XVIII.",
                    analogy = "Pasar de coser un abrigo a mano durante una semana a una máquina que fabrica cien abrigos por hora: multiplicó la producción y cambió dónde vivía la gente.",
                    keyPoints = listOf(
                        "Motor principal: La máquina de vapor de James Watt (1769) alimentada por carbón mineral.",
                        "Sectores pioneros: Industria textil del algodón y siderurgia (hierro y ferrocarril).",
                        "Consecuencias sociales: Éxodo rural masivo a las ciudades, surgimiento de la burguesía industrial y del proletariado obrero.",
                        "Impacto global: Aparición del capitalismo moderno, nuevos sindicatos y jornadas laborales reguladas."
                    ),
                    examTraps = listOf(
                        "¡CUIDADO!: No confundir la 1ª Revolución (vapor, carbón y textil en Reino Unido) con la 2ª Revolución (electricidad, petróleo, acero y química a finales del siglo XIX).",
                        "No empezó de repente: fue precedida por una revolución agraria y demográfica previa."
                    ),
                    checkQuestion = "¿Cuál fue la fuente de energía fundamental de la Primera Revolución Industrial?",
                    checkAnswer = "El carbón mineral, que alimentaba las calderas de vapor."
                )
            }
            else -> {
                StudyExplanation(
                    topic = topic,
                    summary = "Guía sintética y conceptos indispensables para rendir con éxito el examen de '$topic'.",
                    simpleDefinition = "Es el tema central de tu estudio: comprende la base conceptual, sus componentes principales y sus aplicaciones prácticas.",
                    analogy = "Piensa en '$topic' como las piezas de un engranaje: cuando comprendes cómo se conectan las ideas básicas, los detalles complejos encajan solos.",
                    keyPoints = listOf(
                        "Concepto fundamental: Identifica con precisión qué es, cuál es su objetivo y cuándo se aplica.",
                        "Estructura o fases: Reconoce el orden lógico, las variables involucradas o las fechas/etapas clave.",
                        "Fórmulas o relaciones causales: Observa qué causa qué y cuáles son los factores determinantes.",
                        "Aplicación práctica: Cómo se traduce este concepto a un problema o caso real de examen."
                    ),
                    examTraps = listOf(
                        "¡ATENCIÓN EN EL EXAMEN!: Leer con cuidado los enunciados con 'SIEMPRE', 'NUNCA' o 'EXCEPTO', que suelen ocultar trampas.",
                        "Memorizar sin entender: Si el profesor cambia los números o el ejemplo, el razonamiento debe mantenerse sólido."
                    ),
                    checkQuestion = "¿Podrías resumir en una sola oración la idea principal de $topic?",
                    checkAnswer = "Revisa los 4 puntos clave anteriores para confirmar que no olvidaste ninguna parte esencial."
                )
            }
        }
    }

    /**
     * Generador local de diagramas y gráficos inteligentes interactivos.
     */
    fun generateLocalDiagram(topic: String, type: DiagramType): DiagramData {
        val t = topic.lowercase().trim()
        val isBio = t.contains("célula") || t.contains("celula") || t.contains("mitosis") || t.contains("biolog")
        val isPhys = t.contains("newton") || t.contains("física") || t.contains("fisica") || t.contains("fuerza")
        val isHist = t.contains("revolución") || t.contains("revolucion") || t.contains("industrial") || t.contains("historia")

        return when (type) {
            DiagramType.MIND_MAP -> {
                when {
                    isBio -> DiagramData(
                        topic = topic,
                        type = type,
                        summary = "Red conceptual de la mitosis y la división celular eucariota.",
                        nodes = listOf(
                            DiagramNode("1", "Ciclo Celular", "Proceso vital de replicación y división", level = 0, category = "Núcleo", examImportanceScore = 100),
                            DiagramNode("2", "Interfase (Fase S)", "Duplicación del ADN y preparación", level = 1, category = "Preparación", examImportanceScore = 80),
                            DiagramNode("3", "Profase", "Condensación de cromatina y huso", level = 1, category = "Fase 1", examImportanceScore = 85),
                            DiagramNode("4", "Metafase", "Alineación en placa ecuatorial", level = 1, category = "Fase 2", examImportanceScore = 95),
                            DiagramNode("5", "Anafase", "Separación de cromátidas a polos", level = 1, category = "Fase 3", examImportanceScore = 95),
                            DiagramNode("6", "Telofase", "Reconstrucción de envoltura nuclear", level = 1, category = "Fase 4", examImportanceScore = 85),
                            DiagramNode("7", "Citocinesis", "División física del citoplasma", level = 2, category = "Resultado", examImportanceScore = 90)
                        ),
                        connections = listOf(
                            DiagramConnection("1", "2", "inicia con"),
                            DiagramConnection("2", "3", "pasa a"),
                            DiagramConnection("3", "4", "prosigue a"),
                            DiagramConnection("4", "5", "desencadena"),
                            DiagramConnection("5", "6", "finaliza en"),
                            DiagramConnection("6", "7", "concluye con")
                        )
                    )
                    isPhys -> DiagramData(
                        topic = topic,
                        type = type,
                        summary = "Mapa conceptual interactivo de la Dinámica y Leyes de Newton.",
                        nodes = listOf(
                            DiagramNode("1", "Leyes de Newton", "Fundamentos de la Mecánica Clásica", level = 0, category = "Mecánica", examImportanceScore = 100),
                            DiagramNode("2", "1ª Ley: Inercia", "Resistencia al cambio de velocidad (ΣF = 0)", level = 1, category = "Principio", examImportanceScore = 90),
                            DiagramNode("3", "2ª Ley: Dinámica", "Fuerza neta = masa · aceleración (F = m·a)", level = 1, category = "Fórmula", examImportanceScore = 98),
                            DiagramNode("4", "3ª Ley: Acción-Reacción", "Fuerzas iguales y opuestas en pares", level = 1, category = "Principio", examImportanceScore = 92),
                            DiagramNode("5", "Masa vs Peso", "Masa es cantidad de materia; Peso es fuerza gravitatoria", level = 2, category = "Distinción", examImportanceScore = 95),
                            DiagramNode("6", "Fuerza de Rozamiento", "Oposición al movimiento por contacto", level = 2, category = "Aplicación", examImportanceScore = 85)
                        ),
                        connections = listOf(
                            DiagramConnection("1", "2", "define"),
                            DiagramConnection("1", "3", "formula"),
                            DiagramConnection("1", "4", "establece"),
                            DiagramConnection("3", "5", "requiere diferenciar"),
                            DiagramConnection("3", "6", "incorpora")
                        )
                    )
                    else -> DiagramData(
                        topic = topic,
                        type = type,
                        summary = "Esquema conceptual con relaciones directas para examen de '$topic'.",
                        nodes = listOf(
                            DiagramNode("1", topic, "Tema principal de estudio", level = 0, category = "Tema Central", examImportanceScore = 100),
                            DiagramNode("2", "Definición Base", "Fundamento teórico y conceptos clave", level = 1, category = "Teoría", examImportanceScore = 90),
                            DiagramNode("3", "Mecanismos / Fases", "Paso a paso del funcionamiento", level = 1, category = "Estructura", examImportanceScore = 95),
                            DiagramNode("4", "Casos y Ejemplos", "Situaciones prácticas típicas de examen", level = 1, category = "Práctica", examImportanceScore = 85),
                            DiagramNode("5", "Errores Frecuentes", "Puntos donde restan puntos en el test", level = 2, category = "Alerta", examImportanceScore = 92)
                        ),
                        connections = listOf(
                            DiagramConnection("1", "2", "se fundamenta en"),
                            DiagramConnection("1", "3", "se compone de"),
                            DiagramConnection("1", "4", "se demuestra con"),
                            DiagramConnection("3", "5", "evitar confusiones en")
                        )
                    )
                }
            }
            DiagramType.FLOWCHART -> {
                DiagramData(
                    topic = topic,
                    type = type,
                    summary = "Flujo secuencial cronológico y lógico de etapas para $topic.",
                    nodes = listOf(
                        DiagramNode("1", "Paso 1: Inicio / Entrada", "Condición inicial del sistema o causa disparadora", level = 0, category = "Entrada", examImportanceScore = 85),
                        DiagramNode("2", "Paso 2: Transformación", "Reacción, cálculo o cambio principal", level = 1, category = "Proceso", examImportanceScore = 95),
                        DiagramNode("3", "Paso 3: Verificación", "¿Se cumple la condición o equilibrio?", level = 1, category = "Decisión", examImportanceScore = 90),
                        DiagramNode("4", "Paso 4: Consolidación", "Fijación del nuevo estado", level = 2, category = "Etapa", examImportanceScore = 80),
                        DiagramNode("5", "Paso 5: Resultado Final", "Producto obtenido o estado de equilibrio", level = 2, category = "Salida", examImportanceScore = 100)
                    ),
                    connections = listOf(
                        DiagramConnection("1", "2", "desencadena"),
                        DiagramConnection("2", "3", "lleva a evaluación"),
                        DiagramConnection("3", "4", "si es correcto"),
                        DiagramConnection("4", "5", "obtiene")
                    )
                )
            }
            DiagramType.HIERARCHY_TREE -> {
                DiagramData(
                    topic = topic,
                    type = type,
                    summary = "Árbol jerárquico de clasificación y taxonomía para $topic.",
                    nodes = listOf(
                        DiagramNode("1", topic, "Categoría Superior / General", level = 0, category = "Raíz", examImportanceScore = 100),
                        DiagramNode("2", "Rama A: Fundamentos", "Leyes, postulados o hechos base", level = 1, category = "Rama 1", examImportanceScore = 90),
                        DiagramNode("3", "Rama B: Aplicaciones", "Métodos, cálculos y usos prácticos", level = 1, category = "Rama 2", examImportanceScore = 90),
                        DiagramNode("4", "Detalle A1: Concepto", "Definición puntual", level = 2, category = "Subnodo", examImportanceScore = 80),
                        DiagramNode("5", "Detalle A2: Demostración", "Prueba experimental", level = 2, category = "Subnodo", examImportanceScore = 75),
                        DiagramNode("6", "Detalle B1: Casos Reales", "Problemas de examen", level = 2, category = "Subnodo", examImportanceScore = 95)
                    ),
                    connections = listOf(
                        DiagramConnection("1", "2", "se divide en"),
                        DiagramConnection("1", "3", "se divide en"),
                        DiagramConnection("2", "4", "contiene"),
                        DiagramConnection("2", "5", "contiene"),
                        DiagramConnection("3", "6", "incluye")
                    )
                )
            }
            DiagramType.SMART_METRICS -> {
                DiagramData(
                    topic = topic,
                    type = type,
                    summary = "Matriz inteligente de importancia y frecuencia en exámenes para $topic.",
                    nodes = listOf(
                        DiagramNode("1", "Fórmulas / Definiciones Centrales", "Preguntado en el 95% de los exámenes", level = 0, category = "Alta Prioridad", examImportanceScore = 98),
                        DiagramNode("2", "Diferenciación Conceptual", "Preguntas trampa de opción múltiple", level = 1, category = "Alta Prioridad", examImportanceScore = 92),
                        DiagramNode("3", "Ejercicios Prácticos y Cálculos", "Desarrollo con procedimiento paso a paso", level = 1, category = "Prioridad Media", examImportanceScore = 85),
                        DiagramNode("4", "Antecedentes y Contexto", "Preguntas teóricas de introducción", level = 2, category = "Prioridad Secundaria", examImportanceScore = 70),
                        DiagramNode("5", "Casos Excepcionales", "Preguntas para alcanzar la nota máxima (10)", level = 2, category = "Nivel Experto", examImportanceScore = 88)
                    ),
                    connections = listOf(
                        DiagramConnection("1", "2", "se complementa con"),
                        DiagramConnection("1", "3", "se aplica en"),
                        DiagramConnection("3", "5", "se desafía en")
                    )
                )
            }
        }
    }

    /**
     * Generador local de cuestionarios interactivos de alta calidad pedagógica.
     */
    fun generateLocalQuiz(topic: String): QuizSession {
        val t = topic.lowercase().trim()
        val questions = when {
            t.contains("célula") || t.contains("celula") || t.contains("mitosis") -> listOf(
                QuizQuestion(
                    id = "q1",
                    question = "¿En qué fase específica de la mitosis los cromosomas se alinean a lo largo de la placa ecuatorial de la célula?",
                    options = listOf(
                        QuizOption("opt1", "Profase", false, "Incorrecto: En la profase la cromatina se condensa y desaparece el nucléolo, pero aún no se alinean."),
                        QuizOption("opt2", "Metafase", true, "¡Correcto! En la Metafase los cromosomas alcanzan su máxima condensación y se sitúan justo en el centro ecuatorial."),
                        QuizOption("opt3", "Anafase", false, "Incorrecto: En la anafase las cromátidas ya se están separando hacia los polos opuestos."),
                        QuizOption("opt4", "Telofase", false, "Incorrecto: En la telofase los cromosomas ya llegaron a los polos y se forman los nuevos núcleos.")
                    ),
                    explanation = "La Metafase es el momento clave en el que el huso mitótico alinea todos los cromosomas en el centro para asegurar que la división posterior sea equitativa."
                ),
                QuizQuestion(
                    id = "q2",
                    question = "¿Cuál es el resultado genético final de una división por mitosis en una célula somática humana (2n)?",
                    options = listOf(
                        QuizOption("opt1", "4 células haploides con variabilidad genética", false, "Incorrecto: Ese es el resultado de la Meiosis (formación de gametos)."),
                        QuizOption("opt2", "2 células diploides genéticamente idénticas a la progenitora", true, "¡Correcto! La mitosis preserva exactamente la información genética, generando 2 clones celulares diploides."),
                        QuizOption("opt3", "2 células haploides con la mitad de cromosomas", false, "Incorrecto: La mitosis mantiene el número cromosómico sin reducirlo."),
                        QuizOption("opt4", "1 célula tetraploide con doble material genético", false, "Incorrecto: La célula se divide físicamente en la citocinesis.")
                    ),
                    explanation = "La función primordial de la mitosis es el crecimiento tisular y la reparación celular, por lo que requiere copias exactas diploides (2n = 46 cromosomas en humanos)."
                ),
                QuizQuestion(
                    id = "q3",
                    question = "¿Qué estructura celular es responsable de tirar de las cromátidas hermanas hacia los polos durante la Anafase?",
                    options = listOf(
                        QuizOption("opt1", "El huso mitótico (microtúbulos cinetocóricos)", true, "¡Excelente! Los microtúbulos se anclan a los cinetocoros del centrómero y se acortan para arrastrar las cromátidas."),
                        QuizOption("opt2", "La membrana plasmática", false, "Incorrecto: La membrana solo delimita la célula y participa en la citocinesis estrangulando el citoplasma."),
                        QuizOption("opt3", "El retículo endoplásmico rugoso", false, "Incorrecto: El retículo participa en síntesis proteica, no en la tracción de cromosomas."),
                        QuizOption("opt4", "Las mitocondrias", false, "Incorrecto: Proveen ATP (energía), pero no constituyen el andamiaje mecánico de tracción.")
                    ),
                    explanation = "El huso acromático o mitótico formado por microtúbulos es el aparato mecánico que garantiza el reparto cromosómico exacto."
                ),
                QuizQuestion(
                    id = "q4",
                    question = "¿En qué momento del ciclo celular se duplica el ADN para que las células hijas puedan recibir copias completas?",
                    options = listOf(
                        QuizOption("opt1", "Durante la Profase de la mitosis", false, "Incorrecto: En la mitosis el ADN ya está duplicado y solo se reparte."),
                        QuizOption("opt2", "En la fase S de la Interfase", true, "¡Exacto! En la fase S (Síntesis) previa a la mitosis se replica con precisión todo el genoma."),
                        QuizOption("opt3", "En la fase G1", false, "Incorrecto: En G1 la célula crece y sintetiza enzimas, pero no duplica su ADN."),
                        QuizOption("opt4", "En la Citocinesis", false, "Incorrecto: La citocinesis es el corte final del citoplasma.")
                    ),
                    explanation = "Un error común de examen es creer que el ADN se duplica durante la mitosis. El ADN se replica exclusivamente durante la fase S de la interfase."
                )
            )
            t.contains("newton") || t.contains("fuerza") || t.contains("física") || t.contains("fisica") -> listOf(
                QuizQuestion(
                    id = "q1",
                    question = "Según la Segunda Ley de Newton (F = m · a), si duplicamos la fuerza neta aplicada a un objeto manteniendo constante su masa, ¿qué ocurre con su aceleración?",
                    options = listOf(
                        QuizOption("opt1", "Se reduce a la mitad", false, "Incorrecto: La relación entre fuerza y aceleración es directamente proporcional, no inversa."),
                        QuizOption("opt2", "Se duplica", true, "¡Correcto! Si duplicas F con la misma masa m, la aceleración 'a' se multiplica exactamente por 2."),
                        QuizOption("opt3", "Permanece constante", false, "Incorrecto: Una mayor fuerza neta necesariamente incrementa la aceleración."),
                        QuizOption("opt4", "Se cuadruplica", false, "Incorrecto: No hay relación cuadrática entre F y a.")
                    ),
                    explanation = "La fórmula F = m · a muestra proporcionalidad directa: a = F / m. Si F pasa a 2F, 'a' pasa a 2a."
                ),
                QuizQuestion(
                    id = "q2",
                    question = "Un libro reposa sobre una mesa horizontal. ¿Por qué la fuerza normal que ejerce la mesa sobre el libro y el peso del libro NO son un par de acción-reacción de la 3ª ley?",
                    options = listOf(
                        QuizOption("opt1", "Porque no tienen el mismo módulo", false, "Incorrecto: En reposo sí tienen el mismo módulo en este caso."),
                        QuizOption("opt2", "Porque ambas fuerzas actúan sobre el mismo cuerpo (el libro)", true, "¡Perfecto! Las fuerzas de acción y reacción actúan SIEMPRE sobre cuerpos distintos; el par del peso es la atracción que el libro ejerce sobre la Tierra."),
                        QuizOption("opt3", "Porque una de ellas no es una fuerza real", false, "Incorrecto: Tanto la normal como la gravitatoria son fuerzas reales."),
                        QuizOption("opt4", "Porque la gravedad no cumple las leyes de Newton", false, "Incorrecto: La gravedad sí cumple el principio de acción y reacción.")
                    ),
                    explanation = "¡Clásica trampa de examen! Para que sea un par de 3ª ley de Newton deben ser de la misma naturaleza e interactuar entre dos cuerpos distintos (A sobre B y B sobre A)."
                ),
                QuizQuestion(
                    id = "q3",
                    question = "Si una nave espacial viaja por el espacio interestelar libre de gravedad y fricción, y apaga sus motores, ¿qué sucederá según la Primera Ley de Newton?",
                    options = listOf(
                        QuizOption("opt1", "Se detendrá inmediatamente", false, "Incorrecto: Se necesita una fuerza externa para frenarla (concepto erróneo aristotélico)."),
                        QuizOption("opt2", "Continuará moviéndose en línea recta a velocidad constante indefinidamente", true, "¡Exacto! El principio de inercia establece que sin fuerza neta externa, la velocidad no cambia."),
                        QuizOption("opt3", "Describirá un círculo cerrado", false, "Incorrecto: Para curvar la trayectoria se requiere una fuerza centrípeta."),
                        QuizOption("opt4", "Irá frenando poco a poco hasta el reposo absoluto", false, "Incorrecto: En el vacío sin rozamiento no existe fuerza que disipe su energía cinética.")
                    ),
                    explanation = "La inercia mantiene el estado de movimiento rectilíneo uniforme a menos que una fuerza resultante distinta de cero intervenga."
                )
            )
            else -> listOf(
                QuizQuestion(
                    id = "q1",
                    question = "¿Cuál es el principio o concepto medular que define a '$topic'?",
                    options = listOf(
                        QuizOption("opt1", "La interrelación sistemática entre sus partes y variables fundamentales", true, "¡Correcto! Comprender la relación causa-efecto es lo que más valoran los correctores del examen."),
                        QuizOption("opt2", "Un conjunto de datos aislados sin conexión lógica", false, "Incorrecto: En los exámenes se evalúa la comprensión de relaciones, no datos sueltos."),
                        QuizOption("opt3", "Una regla que solo se cumple en condiciones de laboratorio ideales", false, "Incorrecto: Los conceptos tienen aplicación práctica en casos generales."),
                        QuizOption("opt4", "Una excepción teórica obsoleta", false, "Incorrecto: Es un pilar activo del programa de estudio.")
                    ),
                    explanation = "Dominar la definición esencial te permite deducir el resto de respuestas aunque te cambien el enunciado."
                ),
                QuizQuestion(
                    id = "q2",
                    question = "Al responder una pregunta de desarrollo o tipo test sobre '$topic', ¿qué aspecto NUNCA debes descuidar?",
                    options = listOf(
                        QuizOption("opt1", "Las unidades de medida, condiciones de validez y excepciones a la regla", true, "¡Excelente! La mayoría de puntos perdidos se deben a ignorar restricciones o unidades."),
                        QuizOption("opt2", "Copiar textualmente sin analizar", false, "Incorrecto: La memorización ciega suele fallar en preguntas aplicadas."),
                        QuizOption("opt3", "Asumir que todas las opciones 'Todas las anteriores' son correctas", false, "Incorrecto: Es una trampa clásica de redactores de exámenes."),
                        QuizOption("opt4", "Omitir la justificación del procedimiento", false, "Incorrecto: Justificar el razonamiento asegura el puntaje completo.")
                    ),
                    explanation = "La precisión en los límites del concepto demuestra dominio sólido ante cualquier tribunal examinador."
                ),
                QuizQuestion(
                    id = "q3",
                    question = "¿Cómo se relaciona la teoría de '$topic' con situaciones prácticas del mundo real?",
                    options = listOf(
                        QuizOption("opt1", "Permite predecir comportamientos, optimizar procesos y resolver problemas concretos", true, "¡Muy bien! Las preguntas de examen suelen plantear escenarios reales para verificar si puedes aplicar la teoría."),
                        QuizOption("opt2", "Es puramente abstracta y carece de utilidad experimental", false, "Incorrecto: Todos los temas del currículo tienen base empírica."),
                        QuizOption("opt3", "Solo es aplicable si no intervienen variables externas", false, "Incorrecto: Los modelos contemplan márgenes y factores externos."),
                        QuizOption("opt4", "Contradice los principios básicos de la disciplina", false, "Incorrecto: Se apoya de forma coherente en el cuerpo de conocimientos previo.")
                    ),
                    explanation = "Conectar la teoría con ejemplos tangibles es la mejor técnica para recordar conceptos bajo la presión del examen."
                )
            )
        }

        return QuizSession(topic = topic, questions = questions)
    }
}
