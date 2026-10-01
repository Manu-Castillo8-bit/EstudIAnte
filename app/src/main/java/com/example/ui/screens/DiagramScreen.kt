package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiagramData
import com.example.model.DiagramType
import com.example.ui.components.InteractiveDiagramCanvas

/**
 * Pantalla de Generación y Visualización de Diagramas y Gráficos Inteligentes.
 *
 * SOPORTA TODO TIPO DE DIAGRAMAS:
 * 1. Mapa Conceptual (Mind Map)
 * 2. Diagrama de Flujo (Flowchart)
 * 3. Árbol Jerárquico (Hierarchy Tree)
 * 4. Gráfico Inteligente de Relevancia (Smart Metrics)
 */
@Composable
fun DiagramScreen(
    topic: String,
    currentDiagram: DiagramData?,
    selectedType: DiagramType,
    isGenerating: Boolean,
    onGenerateDiagram: (String, DiagramType) -> Unit,
    onSwitchType: (DiagramType) -> Unit,
    onExplainConcept: (String) -> Unit,
    onQuizConcept: (String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // BackHandler para navegación limpia
    BackHandler {
        onNavigateBack()
    }

    var topicInput by remember(topic) { mutableStateOf(topic) }
    var isEditingTopic by remember { mutableStateOf(false) }

    // Generar automáticamente al abrir si no hay diagrama cargado
    LaunchedEffect(topic, selectedType) {
        if (currentDiagram == null || currentDiagram.topic != topic || currentDiagram.type != selectedType) {
            onGenerateDiagram(topic, selectedType)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("diagram_screen")
    ) {
        // Barra superior con tema de estudio y botón de regenerar
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Diagrama de estudio:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = topic,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { onGenerateDiagram(topic, selectedType) },
                        modifier = Modifier.testTag("regenerate_diagram_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerar diagrama",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Selector de tipo de diagrama
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiagramTypeChip(
                        type = DiagramType.MIND_MAP,
                        icon = Icons.Default.Hub,
                        isSelected = selectedType == DiagramType.MIND_MAP,
                        onSelect = { onSwitchType(DiagramType.MIND_MAP) }
                    )
                    DiagramTypeChip(
                        type = DiagramType.FLOWCHART,
                        icon = Icons.Default.Timeline,
                        isSelected = selectedType == DiagramType.FLOWCHART,
                        onSelect = { onSwitchType(DiagramType.FLOWCHART) }
                    )
                    DiagramTypeChip(
                        type = DiagramType.HIERARCHY_TREE,
                        icon = Icons.Default.AccountTree,
                        isSelected = selectedType == DiagramType.HIERARCHY_TREE,
                        onSelect = { onSwitchType(DiagramType.HIERARCHY_TREE) }
                    )
                    DiagramTypeChip(
                        type = DiagramType.SMART_METRICS,
                        icon = Icons.Default.BarChart,
                        isSelected = selectedType == DiagramType.SMART_METRICS,
                        onSelect = { onSwitchType(DiagramType.SMART_METRICS) }
                    )
                }
            }
        }

        // Subtítulo explicativo del tipo de diagrama actual
        currentDiagram?.let { diagram ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💡 ${diagram.summary} (Arrastra para mover, pellizca o usa +/- para zoom, toca nodos para inspeccionar)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Área central interactiva
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isGenerating) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Generando estructura visual para $topic...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Calculando relaciones y pesos de examen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (currentDiagram != null) {
                InteractiveDiagramCanvas(
                    diagramData = currentDiagram,
                    onExplainConcept = onExplainConcept,
                    onQuizConcept = onQuizConcept,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = "Presiona 'Regenerar' para visualizar el diagrama.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DiagramTypeChip(
    type: DiagramType,
    icon: ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelect,
        label = {
            Text(text = type.title, fontSize = 12.sp)
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        ),
        modifier = Modifier.testTag("chip_diagram_${type.name}")
    )
}
