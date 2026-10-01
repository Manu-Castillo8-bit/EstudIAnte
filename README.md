# 📱 Repaso IA — Tutor Inteligente de Exámenes

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room%20(SQLite)-FFA000.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![AI Engine](https://img.shields.io/badge/AI-Gemini%203.5%20Flash-0091FF.svg?style=flat&logo=google)](https://ai.google.dev/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**Repaso IA** es una aplicación móvil nativa para Android diseñada específicamente para estudiantes que preparan exámenes rigurosos. Desarrollada 100% en **Kotlin** y **Jetpack Compose (Material Design 3)**, integra un tutor pedagógico potenciado por **Google Gemini 3.5 Flash** con persistencia local robusta en **Room (SQLite)** y un motor didáctico de respaldo totalmente funcional sin conexión a internet.

---

## ✨ Características Principales

### 🧠 1. Tutor Pedagógico con IA (Orientado a Exámenes)
- Explicaciones claras que combinan definiciones rigurosas con analogías cotidianas para fijar el conocimiento.
- **Detección de trampas típicas de examen**: alertando sobre los errores frecuentes que suelen costar puntos en las evaluaciones.
- Preguntas de autoevaluación guiadas con respuestas justificadas.

### 📊 2. Diagramas y Esquemas Conceptuales Visuales
- Generación de esquemas dinámicos para memorización espacial:
  - **Mapas Conceptuales Radiales**: con relaciones causales y nodales.
  - **Diagramas de Flujo Paso a Paso**: ideales para procesos biológicos, históricos o algoritmos.
  - **Árboles Jerárquicos**: para taxonomías y categorías.
  - **Matrices de Relevancia**: priorizando los temas más evaluados en los exámenes.

### 📝 3. Simulacros y Cuestionarios Interactivos
- Evaluaciones tipo test con 4 opciones por pregunta.
- Retroalimentación formativa inmediata en cada respuesta (explicando por qué una opción es correcta o incorrecta).
- Registro persistente de notas y porcentajes de acierto para seguimiento temporal.

### 🛡️ 4. [SELLO DE IA DE EstudIAnte] (Auditoría Pedagógica)
- Evaluación formal del dominio del tema mediante llamadas a la API de Gemini con **`responseSchema` estricto en formato JSON**.
- La aplicación consume y presenta los datos de forma estructurada (no como texto plano ni párrafos):
  - **Código único de verificación** criptográfica (ej: `ESTUD-IA-2026-F93A`).
  - **Nivel de maestría** (`BÁSICO`, `INTERMEDIO`, `AVANZADO`, `MAESTRÍA`).
  - **Puntaje numérico** y porcentaje de confianza para el examen.
  - **Fórmula o concepto axial** destacado.
  - **Píldoras de fortalezas validadas** con tildes de verificación.
  - **Dictamen pedagógico del auditor**.

### 💾 5. Persistencia Local 100% Offline con Room (SQLite)
- Todos los temas, cuestionarios y esquemas se almacenan en la base de datos local de SQLite en el dispositivo.
- Operaciones completas de **CRUD** (Crear, Leer, Editar y Borrar temas con confirmación de seguridad).
- **Herramienta de Exportación y Respaldo**: permite exportar la base de datos completa a formato JSON legible para compartir en WhatsApp, Google Drive o correo electrónico.

### 📴 6. Motor Didáctico Local de Respaldo (Tolerancia Total a Fallos)
- Si no hay conexión a internet, hay pérdida de paquetes o no se ha configurado una clave de API, la aplicación **nunca se bloquea**:
  - Activa de forma transparente el motor pedagógico local en memoria con contenido estructurado en español.
  - Permite estudiar en cualquier lugar sin gastar llamadas de API ni depender de servidores externos.

---

## 🏛️ Arquitectura del Software

El proyecto sigue los principios de **Clean Architecture** y el patrón **MVVM (Model-View-ViewModel)** recomendado por Google para Android moderno:

```
┌────────────────────────────────────────────────────────┐
│                   Capa de UI (Compose)                 │
│   HomeScreen • ExplainScreen • DiagramScreen • Quiz    │
└───────────────────────────▲────────────────────────────┘
                            │ Flujos reactivos (StateFlow)
┌───────────────────────────┴────────────────────────────┐
│              Capa de Estado (ViewModel)                │
│                    RepasoViewModel                     │
└───────────────────────────▲────────────────────────────┘
                            │ Corrutinas (suspend functions)
┌───────────────────────────┴────────────────────────────┐
│            Capa de Repositorio (Single Source)         │
│                    StudyRepository                     │
└───────────────▲────────────────────────▲───────────────┘
                │                        │
┌───────────────┴──────────────┐ ┌───────┴───────────────┐
│     Fuente Local (Room)      │ │   Fuente Remota (IA)  │
│   AppDatabase • StudyDao     │ │     GeminiService     │
│  SQLite Local Persistence    │ │  Gemini REST API /    │
│                              │ │  Motor Didáctico Local│
└──────────────────────────────┘ └───────────────────────┘
```

- **UDF (Unidirectional Data Flow):** Los eventos suben desde los composables hacia el ViewModel y el estado baja a través de flujos inmutables `StateFlow` con `collectAsStateWithLifecycle()`.
- **Inyección de Dependencias por Constructor:** Ligera, mantenible y sin frameworks pesados.
- **Manejo Estricto de Corrutinas:** Todas las operaciones de I/O de base de datos y de red se ejecutan en `Dispatchers.IO`, garantizando una interfaz fluida a 60/120 FPS sin bloqueos en el hilo principal.

---

## 🎨 Accesibilidad y Ergonomía Móvil

Diseñada siguiendo directrices de usabilidad para estudiantes en dispositivos reales:
- **Operación con una sola mano:** Controles principales ubicados en la mitad inferior de la pantalla.
- **Legibilidad garantizada:** Ningún texto en pantalla es inferior a `16.sp`, optimizado para uso prolongado o bajo luz solar directa.
- **Jerarquía de botones:** Exactamente un solo botón de acción principal (`Filled Button`) por pantalla/diálogo, con botones secundarios en estilo `Outlined` para evitar fatiga cognitiva.
- **Manejo Edge-to-Edge:** Adaptación nativa a las barras de navegación y estado con `enableEdgeToEdge()`.

---

## 🛠️ Tecnologías y Librerías

| Componente | Tecnología / Librería | Versión |
| :--- | :--- | :--- |
| **Lenguaje** | Kotlin | 2.1.0 |
| **UI Framework** | Jetpack Compose con Material 3 | BOM 2025.01.00 |
| **Persistencia** | AndroidX Room (SQLite) + KSP | 2.6.1 |
| **Motor de IA** | Google Gemini 3.5 Flash | REST API v1beta |
| **Cliente HTTP** | OkHttp 3 con timeouts de 60s | 4.12.0 |
| **Gestión de Secretos** | Secrets Gradle Plugin | 2.0.1 |
| **Procesador de Anotaciones** | KSP (Kotlin Symbol Processing) | 2.1.0-1.0.29 |

---

## 📂 Estructura del Proyecto

```text
app/src/main/java/com/example/
├── MainActivity.kt                # Punto de entrada de la aplicación y navegación
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         # Definición de Room Database
│   │   ├── BackupManager.kt       # Utilidad para exportar y respaldar datos en JSON
│   │   ├── StudyDao.kt            # Data Access Object con consultas SQL
│   │   └── StudyEntities.kt       # Entidades Room (Temas, Cuestionarios, Diagramas)
│   ├── remote/
│   │   └── GeminiService.kt       # Cliente REST hacia Gemini con responseSchema y motor local
│   └── repository/
│       └── StudyRepository.kt     # Única fuente de verdad de datos
├── model/
│   ├── ChatMessage.kt             # Modelos para chat interactivo
│   ├── DiagramModels.kt           # Nodos y conexiones para diagramas
│   ├── QuizModels.kt              # Sesiones, preguntas y opciones de examen
│   ├── StudyExplanation.kt        # Modelo de explicación pedagógica
│   └── StudySeal.kt               # Modelo estructurado del [SELLO DE IA]
└── ui/
    ├── components/
    │   ├── CommonComponents.kt    # Barras de navegación, tarjetas y estadísticas
    │   └── StudySealDialog.kt     # Diálogo para visualizar el Sello de IA estructurado
    ├── screens/
    │   ├── DiagramScreen.kt       # Pantalla del generador visual de diagramas
    │   ├── ExplainScreen.kt       # Pantalla del tutor interactivo
    │   ├── HomeScreen.kt          # Pantalla principal con CRUD de temas y respaldos
    │   └── QuizScreen.kt           # Pantalla de cuestionarios con retroalimentación
    ├── theme/
    │   ├── Color.kt               # Paleta de colores M3
    │   ├── Theme.kt               # Configuración del tema claro y oscuro
    │   └── Type.kt                # Tipografía accesible
    └── viewmodel/
        └── RepasoViewModel.kt     # ViewModel central que administra el estado reactivo
```

---

## 🔑 Configuración de la API Key de Gemini

La clave de API nunca debe hardcodearse en el código fuente. Se administra mediante variables de entorno a través del **Secrets Gradle Plugin**:

1. Copia el archivo `.env.example` como `.env`:
   ```bash
   cp .env.example .env
   ```
2. Abre `.env` y coloca tu clave de API obtenida en [Google AI Studio](https://aistudio.google.com/):
   ```properties
   GEMINI_API_KEY=AIzaSyTuClaveDeApiRealAqui
   ```
3. La clave se inyectará de forma segura en `BuildConfig.GEMINI_API_KEY` en tiempo de compilación.

> **Nota:** Si no configuras ninguna clave de API, la aplicación funcionará de todas maneras utilizando el **Motor Didáctico Local de Respaldo** sin lanzar errores.

---

## 🚀 Compilación y Ejecución

### Requisitos Previos:
- **Android Studio:** Koala / Ladybug o superior.
- **JDK:** Java 17 o superior.
- **Android SDK:** `minSdk = 24` (Android 7.0+) y `compileSdk / targetSdk = 35` (Android 15).

### Pasos para compilar desde la terminal:

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/repaso-ia.git
   cd repaso-ia
   ```

2. **Compilar el archivo APK en modo Debug:**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Ubicación del APK compilado:**
   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

4. **Instalación en dispositivo físico o emulador con ADB:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 🧪 Pruebas Unitarias

Para ejecutar las pruebas unitarias y de arquitectura:
```bash
./gradlew testDebugUnitTest
```

---

## 📄 Licencia

Este proyecto se distribuye bajo la licencia **MIT**. Consulta el archivo `LICENSE` para más información.
