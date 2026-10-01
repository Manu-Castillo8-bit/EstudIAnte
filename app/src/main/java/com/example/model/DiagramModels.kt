package com.example.model

/**
 * Tipos de diagramas y gráficos inteligentes soportados para el repaso de exámenes.
 */
enum class DiagramType(val title: String, val description: String) {
    MIND_MAP(
        title = "Mapa Conceptual",
        description = "Nodo central con ramificaciones conectadas por relaciones clave."
    ),
    FLOWCHART(
        title = "Diagrama de Flujo",
        description = "Secuencia paso a paso de fases, causas, efectos y procesos."
    ),
    HIERARCHY_TREE(
        title = "Árbol Jerárquico",
        description = "Estructura piramidal de categorías principales y subconceptos."
    ),
    SMART_METRICS(
        title = "Gráfico Inteligente",
        description = "Matriz de relevancia, peso en examen y comparación visual."
    )
}

/**
 * Nodo individual dentro del lienzo interactivo.
 *
 * PRECAUCIÓN DE DISEÑO:
 * No hardcodear coordenadas fijas en píxeles. El lienzo de Compose escala
 * y posiciona automáticamente en función de `level` y el orden de los nodos.
 */
data class DiagramNode(
    val id: String,
    val title: String,
    val description: String,
    val level: Int = 0, // 0 = Raíz / Central, 1 = Principal, 2 = Detalle
    val category: String = "Concepto",
    val examImportanceScore: Int = 85 // Porcentaje de relevancia estimado para examen (0-100)
)

/**
 * Conexión dirigida o relación entre dos nodos del diagrama.
 */
data class DiagramConnection(
    val fromId: String,
    val toId: String,
    val label: String? = null
)

/**
 * Contenedor completo de datos para renderizar un diagrama inteligente.
 */
data class DiagramData(
    val topic: String,
    val type: DiagramType,
    val summary: String,
    val nodes: List<DiagramNode>,
    val connections: List<DiagramConnection>
)
