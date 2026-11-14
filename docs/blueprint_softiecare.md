# Blueprint Técnico — SoftieCare

| Metadato | Valor | 
| ----- | ----- | 
| **Versión** | v1.5.2 | 
| **Estado** | Activo | 
| **Última actualización** | 2026-11-14 | 
| **Autor** | Carlos Alberto Jaime Mascareño | 

## Historial de cambios

| Versión | Fecha | Autor | Cambios | 
| ----- | ----- | ----- | ----- | 
| v1.0.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Versión inicial del Blueprint técnico para Android Studio (Kotlin + Jetpack Compose), división en 4 módulos, contrato de datos y cronograma de ejecución. | 
| v1.1.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Eliminación de entidad redundante `CountermeasureLog`, simplificación de ruteo por estado en lugar de librería externa, adición de firmas exactas de pantallas en sección 4.2 y ajuste de horario del sábado. | 
| v1.2.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Cambio de `value` a `currentValue: Float` (Presión Sistólica y Ánimo 1–5), reducción de `BodyZone` a 3 zonas con mapeo y umbrales explícitos en sección 4.3, `enum class Scenario`, método `recordPvtResult`, eliminación de `requiresFlightSurgeonAlert`, renombrado a `StatusNormal`, ajuste de Fase 1 (10:00–10:45 AM), uso de `@Preview` en Fase 2 y hora límite de Fase 4. | 
| v1.3.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Reemplazo de intervalos con huecos por función `evaluateLevel` con cortes continuos en `when`, validación de rangos de entrada en sección 9, clarificación de `SpaceEnvironment` como contexto visual, rol arquitectónico de `SpaceHazard.DISTANCE`, reasignación de `TEMP` (`RADIATION`) y `GLUCOSE` (`HOSTILE_ENVIRONMENT`), ajuste de `recordPvtResult` sobre `MOOD` sin alterar horas de sueño, comportamiento de `history7Days` en `Scenario.NOMINAL` y excepción de archivos de plantilla en `res/drawable/`. | 
| v1.4.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Uso de `minOf(currentMood, ...)` en `recordPvtResult` para evitar mejorar `MOOD` con un resultado lento, alineación de `Scenario.RADIATION_SPIKE` (`TEMP`) e `ISOLATION_FATIGUE` (`SLEEP`, `MOOD`) con sus respectivos `SpaceHazard`, eliminación de redundancia de `DISTANCE` en sección 3, variación nominal en datos semilla de `history7Days` y manejo de salida en falso en `PvtTestScreen`. | 
| v1.4.1 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Corrección de ruta completa en comando `git restore` de Fase 2, guarda `if (currentMood > target)` en `recordPvtResult` para evitar lecturas duplicadas en `history7Days` y nota de prueba manual para `THORAX_CARDIO` en Fase 3. | 
| v1.4.2 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Alineacion de `targetSdk` y `compileSdk` a 37 en la seccion 3 segun el proyecto generado por la plantilla de Android Studio. | 
| v1.5.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Nuevas secciones 1.1 (Alcance y supuestos), 1.2 (Requisitos) y 1.3 (Trabajo futuro): captura manual, umbrales heuristicos, `MOOD` como estado conductual, PVT de un intento, contramedida como simplificacion, entorno sin efecto en umbrales y justificacion Offline-First por `DISTANCE`. Las secciones 3, 4.1, 4.3 y 10 referencian 1.1 y 1.2 en lugar de repetir el contenido. Redaccion impersonal en textos de interfaz. Ramas `Main` y `feature/Modulo<N>` en secciones 8 y 9. | 
| v1.5.1 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Nombres de los responsables de los Modulos 2, 3 y 4 en la seccion 2. | 
| v1.5.2 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Eliminacion de horarios en la seccion 8 (renombrada a Fases de trabajo). | 

## 0. Introducción

Este documento es la fuente única de verdad para construir **SoftieCare**: qué se va a construir, con qué tecnologías de Android, cómo se organiza el repositorio y qué convenciones sigue todo el equipo durante el hackathon. Cada integrante debe leerlo completo antes de escribir código.

Una vez que cada integrante identifique su módulo en la sección 2, trabajará únicamente dentro de su paquete asignado para evitar conflictos de *merge*. La comunicación entre las interfaces se realiza exclusivamente a través del contrato de datos definido en la sección 4.1, las firmas de la sección 4.2 y las reglas de negocio de la sección 4.3.

## 1. Resumen del proyecto

**SoftieCare** es una aplicación nativa en Android de monitoreo y autoevaluación médica *Offline-First* para astronautas en misiones de larga duración. Permite a la tripulación evaluar su estado físico y conductual frente a los 5 peligros del *Human Research Program (HRP)* de la NASA a través de los 6 indicadores mencionados en la descripción del reto: presión arterial, frecuencia cardíaca, glucosa en sangre (`mg/dL`), temperatura corporal, horas de sueño y estado de ánimo.

**División de trabajo (Equipo de 4 personas en total):**

* El equipo consta de 4 integrantes; cada uno es responsable de uno de los 4 módulos de la sección 2.

* Carlos Alberto Jaime Mascareño (responsable del Módulo 1) realiza además el *scaffolding* inicial del repositorio y la integración final en `MainActivity.kt`.

### 1.1 Alcance y supuestos

* **Autoevaluacion con captura manual:** La version actual es una herramienta de autoevaluacion. El tripulante mide cada indicador con el instrumental de a bordo y registra el valor manualmente. La integracion con sensores y wearables queda fuera de alcance por decision explicita (ver 1.3).

* **Umbrales heuristicos:** Los cortes de clasificacion `NORMAL` / `WARNING` / `CRITICAL` (tabla y cortes de la seccion 4.3) son heuristicos para demostracion de software. No constituyen guias clinicas verificadas de la NASA ni sustituyen el criterio de un medico de vuelo.

* **Estado conductual y PVT:** El indicador `MOOD` representa el estado conductual del tripulante (animo y alerta) en una escala de `1.0` a `5.0`. La prueba de reaccion (PVT) es de un solo intento con umbrales demostrativos de interfaz tactil; no equivale al PVT clinico de 10 minutos y solo puede empeorar el estado conductual registrado (reglas en 4.3).

* **Contramedida:** Confirmar la ejecucion de una contramedida restaura el indicador a su valor nominal como simplificacion de la demostracion. En uso real, el tripulante vuelve a medir el indicador despues de ejecutar el protocolo y el sistema clasifica la nueva lectura.

* **Entorno operativo:** El entorno seleccionado (transito a Marte en 0 g, base lunar o rover presurizado) es contexto operativo visual. En esta version no modifica los umbrales de clasificacion (ver 1.3).

* **Offline-First por el peligro `DISTANCE`:** Lejos de la Tierra, la comunicacion sufre retrasos de varios minutos en una mision a Marte y perdidas temporales de enlace, por lo que no se puede depender de telemedicina en tiempo real. Por esa razon el sistema opera al 100% sin red y la evaluacion se realiza de forma autonoma a bordo.

### 1.2 Requisitos

Esta seccion define que hace el sistema. El como se construye en Android se define a partir de la seccion 3.

| ID | Requisito | 
| ----- | ----- | 
| RF-01 | El sistema muestra los 6 indicadores con su valor actual, unidad y nivel (`NORMAL`, `WARNING`, `CRITICAL`). | 
| RF-02 | El sistema representa 3 zonas corporales (cabeza/mente, torax/cardiovascular, metabolico/endocrino) coloreadas segun el nivel de sus indicadores. | 
| RF-03 | El sistema permite seleccionar el entorno operativo como contexto visual (ver 1.1). | 
| RF-04 | El tripulante registra manualmente el valor de un indicador siguiendo una guia de 3 pasos. El sistema rechaza valores no numericos o fuera del rango valido de captura (tabla de 4.3). | 
| RF-05 | El tripulante puede marcar un adjunto de imagen como evidencia; el sistema solo muestra una confirmacion visual y no almacena la imagen. | 
| RF-06 | El sistema clasifica cada lectura con los cortes de la seccion 4.3. | 
| RF-07 | El sistema conserva las ultimas 7 lecturas de cada indicador y muestra su tendencia. | 
| RF-08 | El tripulante realiza una prueba de reaccion de un intento. Un toque antes del estimulo se trata como salida en falso y reinicia el intento sin registrar resultado. Un resultado lento reduce el estado conductual (reglas en 4.3). | 
| RF-09 | El sistema muestra el detalle de cada indicador: peligro del HRP asociado, tendencia y protocolo de contramedida. El tripulante confirma la ejecucion de la contramedida (ver 1.1). | 
| RF-10 | El sistema permite inyectar escenarios de demostracion: nominal, pico de radiacion y fatiga por aislamiento. | 
| RNF-01 | El sistema funciona sin conexion de red (ver 1.1). | 
| RNF-02 | La interfaz usa modo oscuro de alto contraste (ver seccion 7). | 

### 1.3 Trabajo futuro

* Captura automatica desde sensores y wearables.

* Ajuste de los umbrales de clasificacion segun el entorno operativo.

* Verificacion de la contramedida mediante una nueva medicion obligatoria.

* PVT completo de duracion clinica con multiples estimulos.

## 2. Módulos y responsables

| \# | Módulo / Interfaz | Paquete (`com.softiecare.app...`) | Responsable | Alcance técnico principal | 
| ----- | ----- | ----- | ----- | ----- | 
| 1 | Core, Motor Clínico & Simulador | `core.model`, `core.data`, `features.simulator` | Carlos Alberto Jaime Mascareño | Scaffolding inicial, entidades base, función `evaluateLevel` en `HealthRepository`, estado reactivo (`StateFlow`) y diálogo de inyección de escenarios (`SimulatorSheet`). | 
| 2 | Anatomía Interactiva & HUD Principal | `features.dashboard` (+ `res/drawable`) | Integrante 2: Emir Alexander Alcantar Martinez | Pantalla principal dividida: silueta humana en 3 zonas interactivas, lista lateral de los 6 indicadores y selector visual de entorno. Único responsable de agregar recursos nuevos en `res/drawable`. | 
| 3 | Captura Guiada & Test PVT | `features.assessment` | Integrante 3: Elias Tamayo Salcedo | Formulario guiado paso a paso con validación numérica y de rango, confirmación visual simulada de adjunto de imagen y minijuego de tiempo de reacción (PVT). | 
| 4 | Diagnóstico, Contramedidas & Gráficas | `features.countermeasures` | Integrante 4: Arturo Rafael Cornejo Escobar | Vista de detalle clínico: relación con los peligros del HRP, gráfica de las últimas 7 lecturas en `Canvas` y ejecución de contramedida. | 

## 3. Stack tecnológico

| Capa | Tecnología | Justificación | 
| ----- | ----- | ----- | 
| **SO / SDK Android** | `minSdk = 26` (Android 8.0), `targetSdk = 37` | `minSdk 26` soporta `java.time` nativo sin configurar *desugaring*. Verificar que `compileSdk = 37` esté instalado en el SDK Manager local. | 
| **Lenguaje y UI** | Kotlin y Jetpack Compose (Material 3) | Si se usa Kotlin 2.0+, verificar que el plugin `org.jetbrains.kotlin.plugin.compose` esté declarado en `build.gradle.kts` (incluido por defecto en la plantilla *Empty Activity* de Android Studio reciente). | 
| **Ruteo / Navegación** | Navegación por estado (`when (currentScreen)`) + booleano `showSimulator` en `MainActivity.kt` | Elimina dependencias externas de navegación y evita errores de rutas al integrar las 4 ramas. `SimulatorSheet` se implementa como un `AlertDialog` controlado por un `Boolean` independiente (conservando el nombre `SimulatorSheet` para no romper la firma acordada). | 
| **Estado y Persistencia** | `StateFlow` en `HealthRepository` (Singleton en memoria con *Seed Data*) | Garantiza velocidad de desarrollo y cero fallos de migración SQL durante la demostración. | 
| **Visualización — Gráficas** | `Canvas` nativo de Jetpack Compose | Dibuja la tendencia de `history7Days` sin librerías externas. | 
| **Visualización — Cuerpo** | Bloques táctiles en Compose (`Box`/`Canvas`) o 3 `VectorDrawable` separados | Un vector único no permite clic ni coloreado por región fácilmente; se usan 3 contenedores interactivos (uno por `BodyZone`) coloreados con `StatusNormal`, `StatusWarning` o `StatusCritical`. | 
| **Backend / Red / Auth** | Ninguno (100% Offline-First) | Requisito arquitectónico derivado de `SpaceHazard.DISTANCE` (ver sección 1.1). | 

## 4. Estructura del repositorio

```
softiecare/
├── app/
│   ├── src/main/
│   │   ├── java/com/softiecare/app/
│   │   │   ├── MainActivity.kt              # Punto de entrada, estado de pantalla y overlay de simulador
│   │   │   ├── ui/
│   │   │   │   ├── theme/                   # Color.kt, Theme.kt, Type.kt
│   │   │   │   └── components/              # SharedComponents.kt
│   │   │   ├── core/
│   │   │   │   ├── model/                   # HealthModels.kt
│   │   │   │   └── data/                    # HealthRepository.kt
│   │   │   └── features/
│   │   │       ├── dashboard/               # Módulo 2: DashboardScreen.kt
│   │   │       ├── assessment/              # Módulo 3: AssessmentScreen.kt, PvtTestScreen.kt
│   │   │       ├── countermeasures/         # Módulo 4: CountermeasureDetailScreen.kt
│   │   │       └── simulator/               # Módulo 1: SimulatorSheet.kt
│   │   └── res/
│   │       └── drawable/                    # Exclusivo de Módulo 2 tras el commit inicial de plantilla
│   └── build.gradle.kts
├── docs/
└── README.md

```

**Límites de propiedad, para minimizar conflictos de merge trabajando en paralelo:**

* Cada integrante trabaja únicamente dentro de su carpeta en `features/<nombre-modulo>/`.

* `res/drawable/` incluye `ic_launcher_background.xml` e `ic_launcher_foreground.xml` en el commit inicial del *scaffolding*; después de ese commit inicial, solo puede ser modificado por el Integrante 2.

* `core/model/`, `core/data/`, `ui/theme/`, `ui/components/` y `MainActivity.kt` los entrega el *scaffolding* inicial y solo los edita Carlos Alberto Jaime Mascareño.

### 4.1 Contrato de datos entre módulos (`core/model/HealthModels.kt`)

Todos los valores de indicadores son numéricos (`Float`) para permitir validación uniforme con `toFloatOrNull()` y graficado directo en `history7Days` (que almacena las últimas 7 lecturas registradas, iniciando con una lista semilla de exactamente 7 elementos por indicador con ligeras variaciones dentro del rango `NORMAL` para evitar una línea plana inicial, siendo el séptimo elemento igual a `normalValue`):

* **Presión Arterial (`BP_SYS`):** Registra únicamente la presión sistólica en `mmHg`.

* **Estado Conductual (`MOOD`):** Animo y alerta (ver 1.1) en escala numérica de `1.0` (crítico) a `5.0` (óptimo) en `pts`.

| Tipo / Estado | Quién escribe / invoca | Quién lee (`collectAsState`) | Dónde vive | 
| ----- | ----- | ----- | ----- | 
| `VitalIndicator` | Módulo 1 (`injectScenario`), Módulo 3 (`updateIndicatorValue`, `recordPvtResult`) y Módulo 4 (`applyCountermeasure`) | Módulo 2 (HUD y Silueta), Módulo 3 (Guía e input actual) y Módulo 4 (Detalle y Gráfica) | `core/model/HealthModels.kt` | 
| `SpaceEnvironment` | Módulo 2 (`setEnvironment`) | Módulo 2 (Chip de entorno activo en barra superior) | `core/model/HealthModels.kt` | 

```
package com.softiecare.app.core.model

enum class StatusLevel { NORMAL, WARNING, CRITICAL }
enum class SpaceEnvironment { TRANSIT_MARS_0G, LUNAR_BASE, PRESSURIZED_ROVER }
enum class SpaceHazard { RADIATION, ISOLATION, DISTANCE, GRAVITY, HOSTILE_ENVIRONMENT }
enum class BodyZone { HEAD_MIND, THORAX_CARDIO, METABOLIC_ENDOCRINE }
enum class Scenario { NOMINAL, RADIATION_SPIKE, ISOLATION_FATIGUE }

data class VitalIndicator(
    val id: String,
    val name: String,
    val currentValue: Float,
    val normalValue: Float,
    val unit: String,
    val level: StatusLevel,
    val bodyZone: BodyZone,
    val primaryHazard: SpaceHazard,
    val history7Days: List<Float>,
    val measurementGuide: List<String>,
    val countermeasureProtocol: String
)

```

### 4.2 Firmas exactas de pantallas y métodos de `HealthRepository`

**Métodos públicos en `core/data/HealthRepository.kt`:**

```
object HealthRepository {
    val environment: StateFlow<SpaceEnvironment>
    val indicators: StateFlow<List<VitalIndicator>>

    fun setEnvironment(env: SpaceEnvironment)
    fun evaluateLevel(id: String, v: Float): StatusLevel
    fun updateIndicatorValue(id: String, newValue: Float)
    fun recordPvtResult(reactionTimeMs: Long)
    fun applyCountermeasure(id: String)
    fun injectScenario(scenario: Scenario)
}

```

**Firmas `@Composable` de cada módulo:**

```
// Módulo 1: features/simulator/SimulatorSheet.kt
@Composable
fun SimulatorSheet(onDismiss: () -> Unit)

// Módulo 2: features/dashboard/DashboardScreen.kt
@Composable
fun DashboardScreen(
    onIndicatorClick: (String) -> Unit,
    onOpenAssessment: (String) -> Unit,
    onOpenPvtTest: () -> Unit,
    onOpenSimulator: () -> Unit
)

// Módulo 3: features/assessment/AssessmentScreen.kt y PvtTestScreen.kt
@Composable
fun AssessmentScreen(indicatorId: String, onBack: () -> Unit)

@Composable
fun PvtTestScreen(onBack: () -> Unit)

// Módulo 4: features/countermeasures/CountermeasureDetailScreen.kt
@Composable
fun CountermeasureDetailScreen(indicatorId: String, onBack: () -> Unit)

```

### 4.3 Mapeo de indicadores, cortes continuos (`evaluateLevel`) y reglas de negocio

*(Los cortes siguientes son heurísticos; ver 1.1).*

**Cobertura de los 5 Peligros del HRP (`SpaceHazard`):**

* `GRAVITY`, `RADIATION`, `HOSTILE_ENVIRONMENT` e `ISOLATION` se asignan directamente a los 6 indicadores como `primaryHazard`:

  * `TEMP` se asocia a `RADIATION` (fiebre/alteración del sistema inmune inducida por radiación espacial).

  * `GLUCOSE` se asocia a `HOSTILE_ENVIRONMENT` (alteración metabólica y nutricional en hábitat cerrado).

* `DISTANCE` (distancia de la Tierra) no se asigna a un indicador: es el peligro transversal que justifica la arquitectura *Offline-First* (ver 1.1).

| `id` | Nombre | Zona (`BodyZone`) | Peligro (`SpaceHazard`) | `normalValue` (`unit`) | Rango válido de captura (`min..max`) | 
| ----- | ----- | ----- | ----- | ----- | ----- | 
| `"BP_SYS"` | Presión Sistólica | `THORAX_CARDIO` | `GRAVITY` | `120.0f` (`mmHg`) | `50.0f..250.0f` | 
| `"HEART_RATE"` | Frecuencia Cardíaca | `THORAX_CARDIO` | `GRAVITY` | `72.0f` (`bpm`) | `30.0f..220.0f` | 
| `"GLUCOSE"` | Glucosa en Ayunas | `METABOLIC_ENDOCRINE` | `HOSTILE_ENVIRONMENT` | `90.0f` (`mg/dL`) | `30.0f..400.0f` | 
| `"TEMP"` | Temperatura Corporal | `METABOLIC_ENDOCRINE` | `RADIATION` | `36.6f` (`°C`) | `32.0f..43.0f` | 
| `"SLEEP"` | Horas de Sueño | `HEAD_MIND` | `ISOLATION` | `7.5f` (`hrs`) | `0.0f..24.0f` | 
| `"MOOD"` | Estado Conductual | `HEAD_MIND` | `ISOLATION` | `4.5f` (`pts` 1-5) | `1.0f..5.0f` | 

**Regla de corte continuo sin huecos decimales (`evaluateLevel`):**

Para evitar que valores decimales intermedios (ej. `94.95f` o `9.5f`) queden fuera de rango, `evaluateLevel` evalúa desigualdades estrictas en orden (`CRITICAL` -> `WARNING` -> `else NORMAL`):

```
fun evaluateLevel(id: String, v: Float): StatusLevel = when (id) {
    "BP_SYS" -> when {
        v < 90f || v >= 140f -> StatusLevel.CRITICAL
        v < 95f || v >= 130f -> StatusLevel.WARNING
        else -> StatusLevel.NORMAL
    }
    "HEART_RATE" -> when {
        v < 50f || v > 105f -> StatusLevel.CRITICAL
        v < 60f || v > 90f -> StatusLevel.WARNING
        else -> StatusLevel.NORMAL
    }
    "GLUCOSE" -> when {
        v < 60f || v > 125f -> StatusLevel.CRITICAL
        v < 70f || v >= 100f -> StatusLevel.WARNING
        else -> StatusLevel.NORMAL
    }
    "TEMP" -> when {
        v < 35.5f || v >= 38.0f -> StatusLevel.CRITICAL
        v < 36.1f || v >= 37.3f -> StatusLevel.WARNING
        else -> StatusLevel.NORMAL
    }
    "SLEEP" -> when {
        v < 5.5f || v > 10.0f -> StatusLevel.CRITICAL
        v < 7.0f || v > 9.0f -> StatusLevel.WARNING
        else -> StatusLevel.NORMAL
    }
    "MOOD" -> when {
        v < 2.5f -> StatusLevel.CRITICAL
        v < 4.0f -> StatusLevel.WARNING
        else -> StatusLevel.NORMAL
    }
    else -> StatusLevel.NORMAL
}

```

**Comportamiento exacto de los métodos en `HealthRepository`:**

1. **`updateIndicatorValue(id, newValue)`:** Actualiza `currentValue = newValue`, asigna `level = evaluateLevel(id, newValue)`, y rota la ventana de las últimas 7 lecturas en `history7Days` eliminando el primer elemento (`drop(1)`) y agregando `newValue` al final (`+ newValue`).

2. **`applyCountermeasure(id)`:** Restaura el indicador a su estado nominal: asigna `currentValue = normalValue`, `level = StatusLevel.NORMAL`, y agrega `normalValue` al final de `history7Days` (`drop(1) + normalValue`). Es la simplificacion descrita en 1.1.

3. **`recordPvtResult(reactionTimeMs)`:** El PVT demostrativo (ver 1.1) mide un único tiempo de reacción táctil en milisegundos e impacta el indicador conductual `"MOOD"` únicamente si el resultado es más bajo que el `currentMood` actual (evitando mejorar el estado o insertar lecturas repetidas en `history7Days`):

   * Si `reactionTimeMs > 500L` y `currentMood > 2.0f`: invoca `updateIndicatorValue("MOOD", 2.0f)` (`CRITICAL`).

   * Si `reactionTimeMs in 381L..500L` y `currentMood > 3.0f`: invoca `updateIndicatorValue("MOOD", 3.0f)` (quedando en al menos `WARNING`, o manteniendo `CRITICAL` si ya era menor o igual a `2.0f`).

   * Si `reactionTimeMs <= 380L` (o si `currentMood` ya era menor o igual al valor objetivo): mantiene el estado y `history7Days` sin cambios.

   * Si el usuario toca la pantalla antes de que aparezca el estímulo verde, `PvtTestScreen` muestra en pantalla el aviso `"Salida en falso — reintentar"` y reinicia el intento sin invocar `recordPvtResult`.

4. **`injectScenario(scenario)`:** Cada escenario modifica exclusivamente los indicadores asociados a su `SpaceHazard`:

   * `Scenario.NOMINAL`: Restaura los 6 indicadores a su estado semilla inicial completo (`currentValue = normalValue`, `level = StatusLevel.NORMAL` y `history7Days` restaurado a la lista semilla original de 7 lecturas nominales variadas).

   * `Scenario.RADIATION_SPIKE`: Invoca únicamente `updateIndicatorValue("TEMP", 38.4f)` (`CRITICAL`, `SpaceHazard.RADIATION`).

   * `Scenario.ISOLATION_FATIGUE`: Invoca únicamente `updateIndicatorValue("SLEEP", 4.5f)` (`CRITICAL`, `SpaceHazard.ISOLATION`) y `updateIndicatorValue("MOOD", 2.0f)` (`CRITICAL`, `SpaceHazard.ISOLATION`).

5. **`setEnvironment(env)`:** Actualiza únicamente el `StateFlow<SpaceEnvironment>` para reflejar visualmente el contexto operativo actual en el encabezado del Dashboard. No interviene en `evaluateLevel` (ver 1.1).

6. **Adjunto de imagen en Módulo 3 (`AssessmentScreen`):** Es exclusivamente un estado visual local (`var hasAttachedImage by remember { mutableStateOf(false) }`) que muestra confirmación en pantalla al usuario; no modifica `VitalIndicator`.

## 5. Convenciones de código

* **Archivos `.kt`, funciones `@Composable` y Clases:** `PascalCase` (ej. función `DashboardScreen()`, clase `VitalIndicator`).

* **Funciones regulares, propiedades y variables:** `camelCase`.

* **Nombres de paquete:** `lowercase` separados por punto (ej. `com.softiecare.app.features.countermeasures`).

* **Logs y textos en consola (`println` / `Log.d`):** No usar tildes ni signos de puntuación finales dentro de las cadenas de impresión (ej. `println("Actualizando lectura de glucosa")`).

* **Comentarios:** Cortos y únicamente cuando ocurra lógica no evidente.

* **Prioridad de implementación:** Código funcional, directo y estándar antes que sobre-optimizado.

## 6. Componentes base compartidos (`ui/components/SharedComponents.kt`)

Entregados en el *scaffolding* inicial para mantener consistencia visual:

* `SpaceCard(statusLevel: StatusLevel, onClick: (() -> Unit)?, content: @Composable () -> Unit)`: Contenedor con fondo `SurfacePanel` y borde coloreado según el estado.

* `StatusBadge(level: StatusLevel)`: Etiqueta semántica (`NORMAL`, `WARNING`, `CRITICAL`).

* `HazardTag(hazard: SpaceHazard)`: Chip informativo que muestra cuál de los 5 peligros del HRP está asociado al indicador.

* `PrimaryActionButton(text: String, onClick: () -> Unit)`: Botón de alto contraste para confirmar mediciones o protocolos médicos.

## 7. Guía de diseño (Design Tokens)

Toda la interfaz utiliza modo oscuro estricto de alto contraste. Ningún archivo dentro de `features/` debe declarar colores hexadecimales quemados (`Color(0xFF...)`); todos deben consumir las constantes de `ui/theme/Color.kt` (alineadas exactamente con los nombres de `StatusLevel`):

* `SpaceBackground` (`0xFF0B0F19`): Fondo general de la aplicación.

* `SurfacePanel` (`0xFF161F30`): Fondo de tarjetas, paneles laterales y diálogos modales.

* `StatusNormal` (`0xFF00E676`): Verde indicador nominal (`StatusLevel.NORMAL`).

* `StatusWarning` (`0xFFFFEA00`): Amarillo precaución (`StatusLevel.WARNING`).

* `StatusCritical` (`0xFFFF1744`): Rojo alerta crítica (`StatusLevel.CRITICAL`).

* `TextPrimary` (`0xFFF8FAFC`): Texto principal blanco hueso.

* `TextSecondary` (`0xFF94A3B8`): Etiquetas secundarias y unidades de medida.

## 8. Fases de trabajo

### Fase 1: Alineación y Scaffolding

* **Carlos Alberto Jaime Mascareño (Módulo 1):** Inicializa el proyecto en Android Studio (`minSdk 26`), configura GitHub y sube el *scaffolding* completo: `Color.kt`, `Theme.kt`, `Type.kt`, `SharedComponents.kt`, `HealthModels.kt`, `HealthRepository.kt` (con 7 elementos semilla variados dentro del rango normal en cada `history7Days`), un stub de `SimulatorSheet.kt` y el esqueleto de `MainActivity.kt`. Verifica que una `@Preview` simple renderice correctamente consumiendo `HealthRepository`.

* **Integrante 2 (Módulo 2):** Define el diseño de las 3 zonas de `BodyZone` (`HEAD_MIND`, `THORAX_CARDIO`, `METABOLIC_ENDOCRINE`) y la estructura de dos columnas de `DashboardScreen.kt`.

* **Integrante 3 (Módulo 3):** Estructura el formulario de captura numérica con validación de rango en `AssessmentScreen.kt` y la máquina de estados de intento único (incluyendo detección de salida en falso) en `PvtTestScreen.kt`.

* **Integrante 4 (Módulo 4):** Diseña el componente `TrendLineChart` con `Canvas` y la vista `CountermeasureDetailScreen.kt`.

### Fase 2: Desarrollo de Módulos en Aislamiento

* Cada integrante desarrolla su pantalla en la rama `feature/Modulo<N>` de su módulo (tabla de ramas en `README.md`).

* **Regla de aislamiento:** Nadie hace commit de cambios en `MainActivity.kt` durante esta fase. Para probar cada pantalla se utiliza `@Preview` en Android Studio (o, si se edita temporalmente `MainActivity.kt` para probar en el emulador, se debe revertir antes de hacer commit ejecutando desde la raíz del repositorio: `git restore app/src/main/java/com/softiecare/app/MainActivity.kt`).

### Fase 3: Integración del Ciclo "Evaluate & Act"

* Se hace *merge* de las 4 ramas y Carlos Alberto Jaime Mascareño conecta las pantallas en `MainActivity.kt`.

* Se verifica el flujo completo:

  1. Abrir el Simulador (`showSimulator = true`) e inyectar `Scenario.RADIATION_SPIKE` (activa `METABOLIC_ENDOCRINE`), y además capturar un dato manual en `AssessmentScreen` para `"BP_SYS"` o `"HEART_RATE"` (por ejemplo `145.0f` mmHg, para activar también la zona `THORAX_CARDIO` y el peligro `GRAVITY` durante la demostración).

  2. Observar que la zona correspondiente de la silueta y la fila en `DashboardScreen` cambien a color Amarillo/Rojo al instante.

  3. Tocar el indicador en alerta para abrir `CountermeasureDetailScreen` y ver la gráfica actualizada de las últimas 7 lecturas.

  4. Presionar "Confirmar Contramedida Ejecutada" y comprobar que el valor regrese a `normalValue` y el color a `StatusNormal`.

### Fase 4: Pulido, Expansiones Opcionales y Demo Freeze

* **Antes de la entrega:** Congelación total de código (*Code Freeze*), generación del APK instalable de respaldo y grabación del video de demostración.

## 9. Requisitos técnicos comunes (Checklist para los 4 módulos)

Cada módulo entregado debe cumplir con lo siguiente antes de hacer Pull Request a `Main`:

* Lectura de estado reactivo desde `HealthRepository` mediante `collectAsState()`, y escritura invocando exclusivamente los métodos públicos de `HealthRepository` (sección 4.2).

* Uso exclusivo de los colores de `ui/theme/Color.kt` (`StatusNormal`, `StatusWarning`, `StatusCritical`) y componentes de `ui/components/SharedComponents.kt`.

* Respeto exacto a las firmas de funciones definidas en la sección 4.2.

* Validación de entrada en `AssessmentScreen`: convertir con `toFloatOrNull()` y verificar que el valor esté dentro del rango válido de captura de la tabla 4.3 (ej. no permitir números negativos, y restringir `"MOOD"` a `1.0f..5.0f` y `"SLEEP"` a `0.0f..24.0f`) antes de invocar `HealthRepository.updateIndicatorValue(id, floatValue)`.

## 10. Nivel de "terminado" y despliegue

**Mínimo indispensable (MVP):** La aplicación compila sin errores en Android Studio, corre en el emulador o dispositivo físico y cumple los requisitos de la sección 1.2.