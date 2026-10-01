package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BackupManager
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.StudyTopicEntity
import com.example.ui.viewmodel.AppScreen
import kotlinx.coroutines.launch

/**
 * Pantalla Principal de Repaso IA con accesibilidad estricta y diseño ergonómico para celular:
 * 1. Optimizado desde 320 px de ancho, operable con una sola mano y sin zoom.
 * 2. Alto contraste apto para luz solar directa; NINGÚN texto menor a 16 px.
 * 3. Todos los campos de entrada cuentan con etiqueta fija y visible.
 * 4. Un solo botón principal (Filled) por pantalla; todos los demás son secundarios (Outlined/Tonal).
 * 5. Estado vacío claro cuando no hay temas, invitando con frase motivadora a la primera acción.
 * 6. Mensajes de éxito y error en español natural sin tecnicismos.
 */
@Composable
fun HomeScreen(
    selectedTopic: String,
    topics: List<StudyTopicEntity>,
    quizAttempts: List<QuizAttemptEntity>,
    diagramCount: Int,
    onNavigateTo: (AppScreen) -> Unit,
    onTopicSelected: (String) -> Unit,
    onAddTopic: (String, String) -> Unit,
    onEditTopic: (Long, String, String, String) -> Unit,
    onDeleteTopic: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Diálogos de interacción
    var showAddDialog by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<StudyTopicEntity?>(null) }
    var topicToDelete by remember { mutableStateOf<StudyTopicEntity?>(null) }
    var topicToStudy by remember { mutableStateOf<StudyTopicEntity?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }

    // Mensaje de notificación visible (Éxito / Estado)
    var userNotificationMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // MENSAJE VISIBLE DE ÉXITO O AVISO EN ESPAÑOL CLARO
        userNotificationMessage?.let { notification ->
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = notification,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = { userNotificationMessage = null }) {
                            Text("×", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 1. BANNER HERO DE BIENVENIDA A MANUELITO
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "¡Hola, Manuelito! 👋",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Tutor inteligente de estudio",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.95f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "¿Qué examen preparás hoy? ¿Qué te gustaría hacer?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // EL ÚNICO BOTÓN PRINCIPAL (FILLED) DE ESTA PANTALLA
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_add_new_topic")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "+ Agregar Tema de Examen",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }

        // 2. ENCABEZADO DE LA LISTA
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tus Temas Guardados (${topics.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tocá un tema para pedir explicaciones, ver diagramas o hacer el cuestionario:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3. ESTADO VACÍO (CUANDO TODAVÍA NO HAY NINGÚN DATO)
        if (topics.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_state_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "¡Todo listo para empezar a repasar!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // FRASE QUE INVITA A LA PRIMERA ACCIÓN
                        Text(
                            text = "Todavía no agregaste ningún tema para tu examen. Tocá el botón de abajo para sumar tu primera materia y la IA te preparará resúmenes, diagramas y cuestionarios de inmediato.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Botón secundario ya que el banner superior tiene el principal
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sumar mi primer tema ahora",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        } else {
            // LISTA DE TEMAS (LEGIBILIDAD AL SOL, TEXTO >= 16PX)
            items(topics, key = { it.id }) { topic ->
                TopicSummaryCard(
                    topic = topic,
                    isSelected = topic.title.equals(selectedTopic, ignoreCase = true),
                    onClick = {
                        onTopicSelected(topic.title)
                        topicToStudy = topic
                    },
                    onEdit = { topicToEdit = topic },
                    onDelete = { topicToDelete = topic }
                )
            }
        }

        // 4. ESTADÍSTICAS Y BOTÓN SECUNDARIO DE RESPALDO
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tu progreso de estudio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(number = "${topics.size}", label = "Temas")
                        StatItem(number = "$diagramCount", label = "Diagramas")
                        StatItem(number = "${quizAttempts.size}", label = "Tests")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BOTÓN SECUNDARIO (OUTLINED) DE RESPALDO
                    OutlinedButton(
                        onClick = { showBackupDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_export_backup"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Respaldar o Exportar Mis Datos",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIÁLOGOS (CAMPOS CON ETIQUETA VISIBLE Y BOTÓN PRINCIPAL ÚNICO)
    // =========================================================================

    // DIÁLOGO 1: AGREGAR NUEVO TEMA
    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newSubject by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Agregar Tema de Examen",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Completá los datos para que el tutor arme las explicaciones y diagramas:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    errorMessage?.let { err ->
                        Text(
                            text = err,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    // CAMPO CON ETIQUETA FIJA Y VISIBLE
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = {
                            newTitle = it
                            errorMessage = null
                        },
                        label = { Text("Nombre del tema o concepto (obligatorio)", style = MaterialTheme.typography.bodyMedium) },
                        placeholder = { Text("Ej: Termodinámica, Mitosis...", style = MaterialTheme.typography.bodyMedium) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_topic_title")
                    )

                    // CAMPO CON ETIQUETA FIJA Y VISIBLE
                    OutlinedTextField(
                        value = newSubject,
                        onValueChange = { newSubject = it },
                        label = { Text("Materia o asignatura (opcional)", style = MaterialTheme.typography.bodyMedium) },
                        placeholder = { Text("Ej: Física, Biología, Historia...", style = MaterialTheme.typography.bodyMedium) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_topic_subject")
                    )
                }
            },
            // UN SOLO BOTÓN PRINCIPAL
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onAddTopic(newTitle.trim(), newSubject.ifBlank { "General" }.trim())
                            onTopicSelected(newTitle.trim())
                            showAddDialog = false
                            userNotificationMessage = "¡Tema '${newTitle.trim()}' guardado con éxito!"
                        } else {
                            errorMessage = "Por favor escribí el nombre del tema antes de guardar."
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_add_topic"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Tema", style = MaterialTheme.typography.titleSmall)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    // DIÁLOGO 2: EDITAR TEMA
    topicToEdit?.let { topic ->
        var editTitle by remember(topic) { mutableStateOf(topic.title) }
        var editSubject by remember(topic) { mutableStateOf(topic.subject) }
        var editSummary by remember(topic) { mutableStateOf(topic.summary) }

        AlertDialog(
            onDismissRequest = { topicToEdit = null },
            title = {
                Text(
                    text = "Modificar Tema",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // CAMPO CON ETIQUETA VISIBLE
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Nombre del tema", style = MaterialTheme.typography.bodyMedium) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // CAMPO CON ETIQUETA VISIBLE
                    OutlinedTextField(
                        value = editSubject,
                        onValueChange = { editSubject = it },
                        label = { Text("Materia", style = MaterialTheme.typography.bodyMedium) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // CAMPO CON ETIQUETA VISIBLE
                    OutlinedTextField(
                        value = editSummary,
                        onValueChange = { editSummary = it },
                        label = { Text("Resumen de estudio", style = MaterialTheme.typography.bodyMedium) },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTitle.isNotBlank()) {
                            onEditTopic(topic.id, editTitle.trim(), editSubject.trim(), editSummary.trim())
                            topicToEdit = null
                            userNotificationMessage = "¡Cambios guardados con éxito!"
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Cambios", style = MaterialTheme.typography.titleSmall)
                }
            },
            dismissButton = {
                TextButton(onClick = { topicToEdit = null }) {
                    Text("Cancelar", style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    // DIÁLOGO 3: CONFIRMAR BORRADO
    topicToDelete?.let { topic ->
        AlertDialog(
            onDismissRequest = { topicToDelete = null },
            title = {
                Text(
                    text = "¿Eliminar este tema?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Querés borrar '${topic.title}' de tu lista de examen? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTopic(topic.id)
                        topicToDelete = null
                        userNotificationMessage = "¡El tema fue eliminado de tu lista!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sí, Eliminar", style = MaterialTheme.typography.titleSmall)
                }
            },
            dismissButton = {
                TextButton(onClick = { topicToDelete = null }) {
                    Text("Cancelar", style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    // DIÁLOGO 4: PANEL DE ACCIONES DEL TEMA SELECCIONADO
    topicToStudy?.let { topic ->
        AlertDialog(
            onDismissRequest = { topicToStudy = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = topic.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Materia: ${topic.subject}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = topic.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = "¿Qué querés hacer ahora, Manuelito?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // OPCIÓN 1: EXPLICADOR IA
                    OutlinedButton(
                        onClick = {
                            topicToStudy = null
                            onTopicSelected(topic.title)
                            onNavigateTo(AppScreen.EXPLAIN)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. Explicar con Tutor IA",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // OPCIÓN 2: DIAGRAMA
                    OutlinedButton(
                        onClick = {
                            topicToStudy = null
                            onTopicSelected(topic.title)
                            onNavigateTo(AppScreen.DIAGRAM)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Hub, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Diagrama Creativo",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // EL ÚNICO BOTÓN PRINCIPAL FILLED DENTRO DEL DIÁLOGO
                    Button(
                        onClick = {
                            topicToStudy = null
                            onTopicSelected(topic.title)
                            onNavigateTo(AppScreen.QUIZ)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Quiz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3. Cuestionario de Repaso",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { topicToStudy = null }) {
                    Text("Cerrar", style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }

    // DIÁLOGO 5: INFORMACIÓN Y EXPORTACIÓN DE RESPALDO
    if (showBackupDialog) {
        val dbPath = BackupManager.getDatabaseFilePath(context)
        var isExporting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Respaldo de Datos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Tus datos se guardan permanentemente en la memoria interna de tu celular en la base local SQLite.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Archivo en tu celular:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "repaso_ia.db",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dbPath,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Podés generar una copia de respaldo para enviarla a tu correo, WhatsApp o guardarla en Google Drive:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // BOTÓN PRINCIPAL ÚNICO DENTRO DEL DIÁLOGO DE RESPALDO
                    Button(
                        onClick = {
                            isExporting = true
                            coroutineScope.launch {
                                val json = BackupManager.generateBackupJson(context)
                                BackupManager.shareBackup(context, json)
                                isExporting = false
                                showBackupDialog = false
                                userNotificationMessage = "¡Respaldo preparado y listo para compartir!"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isExporting,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isExporting) "Preparando respaldo..." else "Compartir Respaldo",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Cerrar", style = MaterialTheme.typography.bodyMedium)
                }
            }
        )
    }
}

/**
 * Tarjeta individual de resumen de tema con contraste para exteriores y tipografía >= 16px.
 */
@Composable
private fun TopicSummaryCard(
    topic: StudyTopicEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("card_topic_${topic.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 3.dp else 2.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Text(
                        text = topic.subject,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_edit_topic_${topic.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar tema",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_delete_topic_${topic.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Borrar tema",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = topic.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tocá para estudiar este tema →",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
