package com.example.model

/**
 * Modelo de datos estructurado del [SELLO DE IA DE EstudIAnte].
 * Representa la certificación pedagógica emitida por la IA sobre un tema de estudio.
 * Consumido como datos discretos (métricas, códigos, niveles y listas), NO como texto libre.
 */
data class StudySeal(
    val topic: String,
    val masteryLevel: String, // "BASICO", "INTERMEDIO", "AVANZADO", "MAESTRIA"
    val score: Int, // 0 - 100
    val verifiedDate: String, // Formato "YYYY-MM-DD"
    val strengths: List<String>, // Lista de fortalezas académicas validadas
    val keyFormulaOrConcept: String, // Fórmula o principio axial del tema
    val examConfidencePercentage: Int, // Probabilidad estimada de aprobación (0 - 100)
    val sealCode: String, // Código criptográfico de verificación, ej: "ESTUD-IA-2026-F93A"
    val pedagogicalVerdict: String, // Dictamen docente: ej: "APTO PARA EXAMEN FINAL"
    val isLocalSimulation: Boolean = false // Indica si se generó con clave de prueba o sin internet
)
