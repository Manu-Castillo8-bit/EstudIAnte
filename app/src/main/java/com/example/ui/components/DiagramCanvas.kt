package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiagramData
import com.example.model.DiagramNode
import com.example.model.DiagramType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Lienzo interactivo en Jetpack Compose para visualizar todo tipo de diagramas:
 * Mapas conceptuales, diagramas de flujo, árboles de decisión y gráficos inteligentes.
 *
 * PRECAUCIÓN DE CÓDIGO CRÍTICA:
 * 1. Transformación de Coordenadas: Al detectar toques (tap) o arrastres (drag), se debe aplicar
 *    la inversa del zoom (escala) y del desplazamiento (offset) acumulado para mapear el toque
 *    de la pantalla a la coordenada lógica del nodo. Si se omite esto, al hacer zoom o pan
 *    el usuario no podrá tocar los nodos correctamente.
 * 2. Rendimiento en Canvas: No instanciar objetos Paint pesados dentro del loop de dibujo `onDraw`.
 */
@Composable
fun InteractiveDiagramCanvas(
    diagramData: DiagramData,
    onExplainConcept: (String) -> Unit,
    onQuizConcept: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var selectedNode by remember { mutableStateOf<DiagramNode?>(null) }

    // Colores del esquema para dibujar nodos y enlaces
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val textPrimaryColor = MaterialTheme.colorScheme.onSurface

    // Calculamos las posiciones lógicas de los nodos según el tipo de diagrama
    val nodePositions = remember(diagramData, diagramData.type) {
        calculateNodePositions(diagramData)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .testTag("diagram_canvas_container")
    ) {
        // Lienzo de dibujo interactivo
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(diagramData) {
                    // Detección de arrastre para mover el diagrama
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffset += dragAmount
                    }
                }
                .pointerInput(diagramData, scale, panOffset, nodePositions) {
                    // Detección de toques para seleccionar un nodo específico
                    detectTapGestures { tapOffset ->
                        // Convertir toque de pantalla a coordenadas del lienzo (zoom + pan)
                        val logicalTapX = (tapOffset.x - panOffset.x) / scale
                        val logicalTapY = (tapOffset.y - panOffset.y) / scale

                        // Buscar el nodo más cercano en radio de toque (45dp ~= 90px aprox)
                        var clickedNode: DiagramNode? = null
                        for (node in diagramData.nodes) {
                            val pos = nodePositions[node.id] ?: continue
                            val dx = logicalTapX - pos.x
                            val dy = logicalTapY - pos.y
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist <= 60f) {
                                clickedNode = node
                                break
                            }
                        }
                        selectedNode = clickedNode
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Centro del canvas más desplazamiento del usuario
            val centerX = canvasWidth / 2f + panOffset.x
            val centerY = canvasHeight / 2f + panOffset.y

            // 1. Dibujar conexiones (Líneas y Curvas Bezier entre nodos)
            for (conn in diagramData.connections) {
                val fromPos = nodePositions[conn.fromId] ?: continue
                val toPos = nodePositions[conn.toId] ?: continue

                val startX = centerX + fromPos.x * scale
                val startY = centerY + fromPos.y * scale
                val endX = centerX + toPos.x * scale
                val endY = centerY + toPos.y * scale

                // Dibujar línea conectora con suave curvatura
                val path = Path().apply {
                    moveTo(startX, startY)
                    val controlX = (startX + endX) / 2f
                    val controlY = (startY + endY) / 2f - 20f * scale
                    quadraticTo(controlX, controlY, endX, endY)
                }

                drawPath(
                    path = path,
                    color = outlineColor,
                    style = Stroke(
                        width = 3.dp.toPx() * scale.coerceIn(0.7f, 1.5f),
                        cap = StrokeCap.Round
                    )
                )

                // Etiqueta de la relación si existe
                if (!conn.label.isNullOrBlank() && scale >= 0.75f) {
                    val labelX = (startX + endX) / 2f
                    val labelY = (startY + endY) / 2f - 10f * scale
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 28f * scale.coerceIn(0.8f, 1.2f)
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText(conn.label, labelX, labelY, paint)
                    }
                }
            }

            // 2. Dibujar nodos
            for (node in diagramData.nodes) {
                val pos = nodePositions[node.id] ?: continue
                val nodeX = centerX + pos.x * scale
                val nodeY = centerY + pos.y * scale

                val isSelected = selectedNode?.id == node.id
                val radius = when (node.level) {
                    0 -> 48.dp.toPx() * scale // Nodo central
                    1 -> 38.dp.toPx() * scale // Nodos de primer nivel
                    else -> 32.dp.toPx() * scale // Nodos de detalle
                }

                val nodeBgColor = when (node.level) {
                    0 -> primaryColor
                    1 -> secondaryColor
                    else -> tertiaryColor
                }

                // Halo de selección si el usuario tocó este nodo
                if (isSelected) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.25f),
                        radius = radius + 12.dp.toPx() * scale,
                        center = Offset(nodeX, nodeY)
                    )
                }

                // Círculo del nodo
                drawCircle(
                    color = nodeBgColor,
                    radius = radius,
                    center = Offset(nodeX, nodeY)
                )

                // Borde fino
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = radius,
                    center = Offset(nodeX, nodeY),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Texto del título dentro del nodo (máx 2 líneas)
                val paintTitle = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = (if (node.level == 0) 30f else 24f) * scale.coerceIn(0.6f, 1.2f)
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                // Truncar si es muy largo
                val displayText = if (node.title.length > 16) {
                    node.title.take(14) + "..."
                } else {
                    node.title
                }
                drawContext.canvas.nativeCanvas.drawText(
                    displayText,
                    nodeX,
                    nodeY + 8f * scale,
                    paintTitle
                )
            }
        }

        // 3. Controles flotantes de Zoom y Recentrado en la esquina superior derecha
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { scale = (scale + 0.2f).coerceAtMost(2.5f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Acercar zoom")
                }
                IconButton(
                    onClick = { scale = (scale - 0.2f).coerceAtLeast(0.5f) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Alejar zoom")
                }
                IconButton(
                    onClick = {
                        scale = 1.0f
                        panOffset = Offset.Zero
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Restablecer vista")
                }
            }
        }

        // 4. Panel de Inspección del Nodo Seleccionado (Flotante inferior)
        selectedNode?.let { node ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("node_detail_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = node.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Relevancia en examen: ${node.examImportanceScore}% • ${node.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = { selectedNode = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar detalle")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = node.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Acciones directas para Manuelito
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onExplainConcept(node.title) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Explicar con IA", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { onQuizConcept(node.title) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cuestionario", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Distribuye las posiciones relativas (en dp/px lógicos) de los nodos según el tipo de diagrama.
 */
private fun calculateNodePositions(diagramData: DiagramData): Map<String, Offset> {
    val positions = mutableMapOf<String, Offset>()
    val nodes = diagramData.nodes
    if (nodes.isEmpty()) return positions

    when (diagramData.type) {
        DiagramType.MIND_MAP -> {
            // Disposición radial: nodo central en (0,0), ramas en círculo
            val root = nodes.firstOrNull { it.level == 0 } ?: nodes.first()
            positions[root.id] = Offset(0f, 0f)

            val branchNodes = nodes.filter { it.id != root.id }
            val count = branchNodes.size
            val radius = 220f

            branchNodes.forEachIndexed { index, node ->
                val angle = (2 * Math.PI / count) * index - Math.PI / 2
                val x = (radius * cos(angle)).toFloat()
                val y = (radius * sin(angle)).toFloat()
                positions[node.id] = Offset(x, y)
            }
        }
        DiagramType.FLOWCHART -> {
            // Disposición vertical secuencial de flujo
            val startY = -((nodes.size - 1) * 110f) / 2f
            nodes.forEachIndexed { index, node ->
                positions[node.id] = Offset(0f, startY + index * 115f)
            }
        }
        DiagramType.HIERARCHY_TREE -> {
            // Disposición en árbol (niveles 0, 1, 2)
            val levels = nodes.groupBy { it.level }
            val levelHeight = 130f
            val maxLevel = levels.keys.maxOrNull() ?: 1
            val topY = -(maxLevel * levelHeight) / 2f

            levels.forEach { (level, levelNodes) ->
                val y = topY + level * levelHeight
                val count = levelNodes.size
                val spacing = 160f
                val startX = -((count - 1) * spacing) / 2f

                levelNodes.forEachIndexed { index, node ->
                    positions[node.id] = Offset(startX + index * spacing, y)
                }
            }
        }
        DiagramType.SMART_METRICS -> {
            // Matriz comparativa en cuadrícula 2 columnas
            nodes.forEachIndexed { index, node ->
                val col = index % 2
                val row = index / 2
                val x = if (col == 0) -140f else 140f
                val y = (row - 1) * 120f
                positions[node.id] = Offset(x, y)
            }
        }
    }

    return positions
}
