# Technical Blueprint — SoftieCare

| Field | Value | 
| ----- | ----- | 
| **Version** | v2.2.0 | 
| **Status** | Active | 
| **Last updated** | 2026-11-14 | 
| **Author** | Carlos Alberto Jaime Mascareño | 

## Change history

| Version | Date | Author | Changes | 
| ----- | ----- | ----- | ----- | 
| v1.0.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Initial technical blueprint for Android Studio (Kotlin + Jetpack Compose), split into 4 modules, data contract and schedule. | 
| v1.1.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Removed the redundant `CountermeasureLog` entity, state-based routing instead of an external library, exact screen signatures in section 4.2. | 
| v1.2.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | `currentValue: Float`, `BodyZone` reduced to 3 zones with explicit mapping and thresholds in 4.3, `enum class Scenario`, `recordPvtResult`, removal of `requiresFlightSurgeonAlert`, rename to `StatusNormal`. | 
| v1.3.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | `evaluateLevel` with continuous cutoffs, input range validation, `SpaceEnvironment` as visual context, architectural role of `SpaceHazard.DISTANCE`, hazard reassignment for `TEMP` and `GLUCOSE`. | 
| v1.4.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | `recordPvtResult` never improves `MOOD`, scenarios aligned with their `SpaceHazard`, varied seed data, PVT false start handling. | 
| v1.4.1 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Guard against duplicate readings in `recordPvtResult`; full path in the `git restore` command. | 
| v1.4.2 | 2026-11-14 | Carlos Alberto Jaime Mascareño | `targetSdk` and `compileSdk` aligned to 37. | 
| v1.5.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | New sections 1.1 (Scope and assumptions), 1.2 (Requirements) and 1.3 (Future work). Impersonal UI wording. `Main` and `feature/Modulo<N>` branches. | 
| v1.5.1 – v1.5.3 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Module owners, removal of time slots in section 8, readable cutoff table in 4.3. | 
| v1.6.0 – v1.6.1 | 2026-11-14 | Carlos Alberto Jaime Mascareño | `ProcedureAnimation` (Lottie) component; level presentation rule (`MOOD` never shown as critical) with `statusLabel` and `displayLevel`. | 
| v1.7.0 – v1.7.1 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Visual redesign (`IndicatorTile`, `StatusBadge` on `Status*Strong`), `HealthRepository` unit tests, Phase 3 integration in `Main`, navigation that survives rotation, dark launch theme and app icon. | 
| v1.8.0 – v1.8.1 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Bottom navigation with 4 tabs, removal of the body silhouette (RF-02 became the overall status), environment illustration (`EnvironmentScene`), icons (`AppIcons`), per-step animations (`StepAnimation`) and readable HRP hazard names (`hazardLabel`). | 
| v1.9.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | The countermeasure no longer fixes the indicator instantly: guided plan (`CountermeasurePlanScreen`) ending with a mandatory new measurement. `applyCountermeasure` removed. Shared `BackPill` button. | 
| v2.0.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Whole app, code comments and documentation in English (the challenge is presented to NASA). Re-measure reminder notification (RF-12, `RemeasureReminder`). `docs/guia_datos.md` renamed to `docs/data_guide.md`. | 
| v2.1.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | About screen with the demo disclaimer and team (FR-13, `features.about`), opened from the info button on the Today tab. Countermeasure plan steps announced as checkboxes by screen readers. | 
| v2.2.0 | 2026-11-14 | Carlos Alberto Jaime Mascareño | Step animations replaced by Noto Emoji Animation Lottie files (CC BY 4.0, credited on the About screen); `ProcedureAnimation` removed. App logo (`AppLogo`) on the Today header and the About screen. | 

## 0. Introduction

This document is the single source of truth for building **SoftieCare**: what is built, with which Android technologies, how the repository is organized and which conventions the whole team follows during the hackathon. Every team member reads it in full before writing code.

Once each team member identifies their module in section 2, they work only inside their assigned package to avoid merge conflicts. Screens communicate only through the data contract in section 4.1, the signatures in section 4.2 and the business rules in section 4.3.

## 1. Project summary

**SoftieCare** is a native, *Offline-First* Android app for medical monitoring and self-assessment for astronauts on long-duration missions. It lets the crew assess their physical and behavioral state against the 5 hazards of NASA's *Human Research Program (HRP)* through the 6 indicators named in the challenge description: blood pressure, heart rate, blood glucose (`mg/dL`), body temperature, sleep hours and mood.

**Team split (4 people in total):**

* Each of the 4 team members owns one of the 4 modules in section 2.

* Carlos Alberto Jaime Mascareño (owner of Module 1) also creates the initial repository scaffolding and does the final integration in `MainActivity.kt`.

### 1.1 Scope and assumptions

* **Self-assessment with manual entry:** The current version is a self-assessment tool. The crew member measures each indicator with onboard instruments and enters the value manually. Integration with sensors and wearables is out of scope by explicit decision (see 1.3).

* **Heuristic thresholds:** The `NORMAL` / `WARNING` / `CRITICAL` cutoffs (table and cutoffs in section 4.3) are heuristic values for a software demo. They are not verified NASA clinical guidelines and do not replace the judgment of a flight surgeon.

* **Behavioral state and PVT:** The `MOOD` indicator represents the crew member's behavioral state (mood and alertness) on a `1.0` to `5.0` scale. The reaction test (PVT) is a single attempt with demo thresholds for a touch screen; it is not equivalent to the 10-minute clinical PVT and can only worsen the recorded behavioral state (rules in 4.3).

* **Countermeasure:** A countermeasure does not fix the indicator by itself. The crew member follows a guided plan (steps, what to expect and warning signs), waits the indicated time and measures again; the level changes only with that new reading. In the demo the wait is accelerated to a few seconds and the plan content is heuristic.

* **Operating environment:** The selected environment (transit to Mars in 0 g, lunar base or pressurized rover) is visual operating context. In this version it does not change the classification thresholds (see 1.3).

* **Level presentation:** The interface shows levels as "STABLE" (`NORMAL`), "NOT OPTIMAL" (`WARNING`) and "CRITICAL" (`CRITICAL`). To avoid reinforcing a low mood, `MOOD` is never shown as "CRITICAL": its `CRITICAL` level is shown as "NOT OPTIMAL" and its numeric value is not highlighted. "CRITICAL" is reserved for physical indicators. The internal classification in section 4.3 does not change.

* **Offline-First because of the `DISTANCE` hazard:** Far from Earth, communication suffers delays of several minutes on a Mars mission and temporary link losses, so real-time telemedicine cannot be relied on. For that reason the system works 100% without a network and the assessment happens autonomously on board.

### 1.2 Requirements

This section defines what the system does. How it is built on Android is defined from section 3 onward.

| ID | Requirement | 
| ----- | ----- | 
| FR-01 | The system shows the 6 indicators with their current value, unit and level (`NORMAL`, `WARNING`, `CRITICAL`). | 
| FR-02 | The system shows an overall status: the worst displayed level and how many indicators are stable. | 
| FR-03 | The system lets the crew select the operating environment and shows it with an illustration of the location (spacecraft, lunar base or Mars rover) as visual context (see 1.1). | 
| FR-04 | The crew member manually records an indicator value following a 3-step guide with an animation per step. The system rejects non-numeric values or values outside the valid capture range (table in 4.3). | 
| FR-05 | The crew member can mark an image attachment as evidence; the system only shows a visual confirmation and does not store the image. | 
| FR-06 | The system classifies each reading with the cutoffs in section 4.3. | 
| FR-07 | The system keeps the last 7 readings of each indicator and shows their trend. | 
| FR-08 | The crew member takes a single-attempt reaction test. A tap before the stimulus is a false start and restarts the attempt without recording a result. A slow result lowers the behavioral state (rules in 4.3). | 
| FR-09 | The system shows the detail of each indicator: associated HRP hazard, trend and countermeasure protocol. | 
| FR-10 | The system can inject demo scenarios: nominal, radiation spike and isolation fatigue. | 
| FR-11 | The crew member follows a countermeasure plan with checkable steps, what to expect, warning signs and a wait time; at the end the system asks for a new measurement of the indicator (see 1.1). | 
| FR-12 | When the countermeasure wait ends, the system shows a local notification reminding the crew member to measure again; opening it starts the guided measurement of that indicator. | 
| FR-13 | The system shows an About screen with the app purpose, how it works, the offline design, the demo disclaimer (see 1.1) and the team. | 
| NFR-01 | The system works without a network connection (see 1.1). | 
| NFR-02 | The interface uses a high-contrast dark mode (see section 7). | 
| NFR-03 | All interface text and project documentation are in English. | 

### 1.3 Future work

* Automatic capture from sensors and wearables.

* Classification thresholds adjusted to the operating environment.

* Full clinical-length PVT with multiple stimuli.

## 2. Modules and owners

| \# | Module / Interface | Package (`com.softiecare.app...`) | Owner | Main technical scope | 
| ----- | ----- | ----- | ----- | ----- | 
| 1 | Core, Clinical Engine & Simulator | `core.model`, `core.data`, `core.notifications`, `features.simulator`, `features.about` | Carlos Alberto Jaime Mascareño | Initial scaffolding, base entities, `evaluateLevel` in `HealthRepository`, reactive state (`StateFlow`), scenario injection dialog (`SimulatorSheet`), re-measure reminders (`RemeasureReminder`) and the About screen. | 
| 2 | Main HUD & Navigation | `features.dashboard` (+ `res/drawable`) | Member 2: Emir Alexander Alcantar Martinez | Today tab (environment, overall status and alerts), Vitals (grid of the 6 indicators), History (trends) and Guides (procedures). Sole owner of new resources in `res/drawable`. | 
| 3 | Guided Measurement & PVT | `features.assessment` | Member 3: Elias Tamayo Salcedo | Step-by-step guided form with numeric and range validation, simulated image attachment confirmation and the reaction time mini-game (PVT). | 
| 4 | Diagnosis, Countermeasures & Charts | `features.countermeasures` | Member 4: Arturo Rafael Cornejo Escobar | Clinical detail view: link to HRP hazards, chart of the last 7 readings in `Canvas` and guided countermeasure plan (`CountermeasurePlanScreen`). | 

## 3. Technology stack

| Layer | Technology | Rationale | 
| ----- | ----- | ----- | 
| **Android OS / SDK** | `minSdk = 26` (Android 8.0), `targetSdk = 37` | `minSdk 26` supports `java.time` natively without desugaring. Check that `compileSdk = 37` is installed in the local SDK Manager. | 
| **Language and UI** | Kotlin and Jetpack Compose (Material 3) | With Kotlin 2.0+, check that the `org.jetbrains.kotlin.plugin.compose` plugin is declared in `build.gradle.kts` (included by default in the recent *Empty Activity* template). | 
| **Routing / Navigation** | State-based navigation (`when (route)`) + Material 3 `NavigationBar` with 4 tabs + `showSimulator` boolean in `MainActivity.kt` | No external navigation dependency and no route errors when integrating the 4 branches. `SimulatorSheet` is an `AlertDialog` controlled by its own `Boolean` (keeping the `SimulatorSheet` name so the agreed signature does not break). | 
| **State and persistence** | `StateFlow` in `HealthRepository` (in-memory singleton with seed data) | Fast development and zero SQL migration failures during the demo. | 
| **Visualization — Charts** | Native Jetpack Compose `Canvas` | Draws the `history7Days` trend without external libraries. | 
| **Visualization — Environment and icons** | Compose `Canvas` illustrations; Material Symbols icons (Apache 2.0) defined as `ImageVector` in code | No new dependencies or asset files; works offline. | 
| **Notifications** | `AlarmManager` (inexact alarm) + `BroadcastReceiver` + `NotificationCompat`; `POST_NOTIFICATIONS` permission on Android 13+ | Local reminders without a network or extra libraries. The inexact alarm needs no special permission; Android may deliver it a few seconds late. | 
| **Step animations** | `com.airbnb.android:lottie-compose` 6.7.1 + Noto Emoji Animation Lottie files in `app/src/main/assets/lottie/emoji/` | Polished animated emoji by Google under CC BY 4.0 (credit on the About screen and in `app/src/main/assets/lottie/README.md`). Bundled in the app, offline. | 
| **Backend / Network / Auth** | None (100% Offline-First) | Architectural requirement derived from `SpaceHazard.DISTANCE` (see section 1.1). | 

## 4. Repository structure

```
softiecare/
├── app/
│   ├── src/main/
│   │   ├── java/com/softiecare/app/
│   │   │   ├── MainActivity.kt              # Entry point, screen state, tabs and simulator overlay
│   │   │   ├── ui/
│   │   │   │   ├── theme/                   # Color.kt, Theme.kt, Type.kt
│   │   │   │   └── components/              # SharedComponents.kt, AppIcons.kt, EnvironmentScene.kt, StepAnimation.kt
│   │   │   ├── core/
│   │   │   │   ├── model/                   # HealthModels.kt
│   │   │   │   ├── data/                    # HealthRepository.kt
│   │   │   │   └── notifications/           # RemeasureReminder.kt
│   │   │   └── features/
│   │   │       ├── dashboard/               # Module 2: DashboardScreen, VitalsScreen, HistoryScreen, GuidesScreen
│   │   │       ├── assessment/              # Module 3: AssessmentScreen.kt, PvtTestScreen.kt
│   │   │       ├── countermeasures/         # Module 4: CountermeasureDetailScreen, CountermeasurePlanScreen, CountermeasurePlans
│   │   │       ├── simulator/               # Module 1: SimulatorSheet.kt
│   │   │       └── about/                   # Module 1: AboutScreen.kt
│   │   └── res/
│   │       └── drawable/                    # Module 2 only after the initial template commit
│   └── build.gradle.kts
├── docs/
└── README.md

```

**Ownership boundaries, to minimize merge conflicts while working in parallel:**

* Each member works only inside their own `features/<module-name>/` folder.

* `res/drawable/` includes `ic_launcher_background.xml` and `ic_launcher_foreground.xml` from the initial scaffolding commit; after that commit only Member 2 modifies it. Exception: `ic_notification.xml` was added by the lead during integration for FR-12.

* `core/`, `ui/theme/`, `ui/components/` and `MainActivity.kt` come from the initial scaffolding and are edited only by Carlos Alberto Jaime Mascareño.

### 4.1 Data contract between modules (`core/model/HealthModels.kt`)

All indicator values are numeric (`Float`) for uniform validation with `toFloatOrNull()` and direct charting from `history7Days`. `history7Days` stores the last 7 recorded readings; it starts with a seed list of exactly 7 elements per indicator with small variations inside the `NORMAL` range (to avoid a flat initial line), the seventh element being equal to `normalValue`.

* **Blood pressure (`BP_SYS`):** Records only the systolic pressure in `mmHg`.

* **Behavioral state (`MOOD`):** Mood and alertness (see 1.1) on a numeric scale from `1.0` (critical) to `5.0` (optimal) in `pts`.

| Type / State | Who writes / calls | Who reads (`collectAsState`) | Where it lives | 
| ----- | ----- | ----- | ----- | 
| `VitalIndicator` | Module 1 (`injectScenario`), Module 3 (`updateIndicatorValue`, `recordPvtResult`) | Module 2 (Today, Vitals, History and Guides tabs), Module 3 (guide and current input) and Module 4 (detail, chart and plan) | `core/model/HealthModels.kt` | 
| `SpaceEnvironment` | Module 2 (`setEnvironment`) | Module 2 (environment banner and selector), `MainActivity` (background tint) | `core/model/HealthModels.kt` | 

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

`BodyZone` remains in the model as data only; the interface no longer shows body zones (FR-02).

### 4.2 Exact screen signatures and `HealthRepository` methods

**Public methods in `core/data/HealthRepository.kt`:**

```
object HealthRepository {
    val environment: StateFlow<SpaceEnvironment>
    val indicators: StateFlow<List<VitalIndicator>>

    fun setEnvironment(env: SpaceEnvironment)
    fun evaluateLevel(id: String, v: Float): StatusLevel
    fun updateIndicatorValue(id: String, newValue: Float)
    fun recordPvtResult(reactionTimeMs: Long)
    fun injectScenario(scenario: Scenario)
}

```

**Re-measure reminder in `core/notifications/RemeasureReminder.kt`:**

```
object RemeasureReminder {
    const val EXTRA_INDICATOR_ID: String
    fun schedule(context: Context, indicatorId: String, indicatorName: String, delayMs: Long)
}

```

**`@Composable` signatures per module:**

```
// Module 1: features/simulator/SimulatorSheet.kt and features/about/AboutScreen.kt
@Composable
fun SimulatorSheet(onDismiss: () -> Unit)

@Composable
fun AboutScreen(onBack: () -> Unit)

// Module 2: features/dashboard/DashboardScreen.kt
@Composable
fun DashboardScreen(
    onIndicatorClick: (String) -> Unit,
    onOpenPvtTest: () -> Unit,
    onOpenSimulator: () -> Unit,
    onOpenAbout: () -> Unit
)

// Module 2: features/dashboard/VitalsScreen.kt, HistoryScreen.kt and GuidesScreen.kt
@Composable
fun VitalsScreen(onIndicatorClick: (String) -> Unit, onOpenAssessment: (String) -> Unit)

@Composable
fun HistoryScreen(onIndicatorClick: (String) -> Unit)

@Composable
fun GuidesScreen(onOpenAssessment: (String) -> Unit, onOpenPvtTest: () -> Unit)

// Module 3: features/assessment/AssessmentScreen.kt and PvtTestScreen.kt
@Composable
fun AssessmentScreen(indicatorId: String, onBack: () -> Unit)

@Composable
fun PvtTestScreen(onBack: () -> Unit)

// Module 4: features/countermeasures/CountermeasureDetailScreen.kt and CountermeasurePlanScreen.kt
@Composable
fun CountermeasureDetailScreen(indicatorId: String, onBack: () -> Unit, onStartCountermeasure: () -> Unit)

@Composable
fun CountermeasurePlanScreen(indicatorId: String, onBack: () -> Unit, onRemeasure: () -> Unit)

```

### 4.3 Indicator mapping, continuous cutoffs (`evaluateLevel`) and business rules

*(The following cutoffs are heuristic; see 1.1.)*

**Coverage of the 5 HRP hazards (`SpaceHazard`):**

* `GRAVITY`, `RADIATION`, `HOSTILE_ENVIRONMENT` and `ISOLATION` are assigned directly to the 6 indicators as `primaryHazard`:

  * `TEMP` is linked to `RADIATION` (fever / immune changes induced by space radiation).

  * `GLUCOSE` is linked to `HOSTILE_ENVIRONMENT` (metabolic and nutritional changes in a closed habitat).

* `DISTANCE` (distance from Earth) is not assigned to an indicator: it is the cross-cutting hazard that justifies the *Offline-First* architecture (see 1.1).

* The interface shows each hazard with a readable name from `hazardLabel` (section 6), never the enum name.

| `id` | Name | Zone (`BodyZone`) | Hazard (`SpaceHazard`) | `normalValue` (`unit`) | Valid capture range (`min..max`) | 
| ----- | ----- | ----- | ----- | ----- | ----- | 
| `"BP_SYS"` | Systolic Blood Pressure | `THORAX_CARDIO` | `GRAVITY` | `120.0f` (`mmHg`) | `50.0f..250.0f` | 
| `"HEART_RATE"` | Heart Rate | `THORAX_CARDIO` | `GRAVITY` | `72.0f` (`bpm`) | `30.0f..220.0f` | 
| `"GLUCOSE"` | Fasting Glucose | `METABOLIC_ENDOCRINE` | `HOSTILE_ENVIRONMENT` | `90.0f` (`mg/dL`) | `30.0f..400.0f` | 
| `"TEMP"` | Body Temperature | `METABOLIC_ENDOCRINE` | `RADIATION` | `36.6f` (`°C`) | `32.0f..43.0f` | 
| `"SLEEP"` | Sleep Hours | `HEAD_MIND` | `ISOLATION` | `7.5f` (`hrs`) | `0.0f..24.0f` | 
| `"MOOD"` | Behavioral State | `HEAD_MIND` | `ISOLATION` | `4.5f` (`pts` 1-5) | `1.0f..5.0f` | 

The capture range only rejects implausible entries (typos or instrument errors); it is not a clinical scale. For example, `TEMP` accepts `32.0` to `43.0` °C because values outside that range are not compatible with a valid reading.

**Continuous cutoff rule without decimal gaps (`evaluateLevel`):**

To keep intermediate decimal values (e.g. `94.95f` or `9.5f`) from falling outside every range, `evaluateLevel` evaluates strict inequalities in order (`CRITICAL` -> `WARNING` -> `else NORMAL`):

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

**Reading the cutoffs** (same rule as the code above; `v` = entered value):

| `id` | `NORMAL` (stable) | `WARNING` (caution) | `CRITICAL` (bad) | 
| ----- | ----- | ----- | ----- | 
| `BP_SYS` | 95 ≤ v < 130 | 90 ≤ v < 95 or 130 ≤ v < 140 | v < 90 or v ≥ 140 | 
| `HEART_RATE` | 60 ≤ v ≤ 90 | 50 ≤ v < 60 or 90 < v ≤ 105 | v < 50 or v > 105 | 
| `GLUCOSE` | 70 ≤ v < 100 | 60 ≤ v < 70 or 100 ≤ v ≤ 125 | v < 60 or v > 125 | 
| `TEMP` | 36.1 ≤ v < 37.3 | 35.5 ≤ v < 36.1 or 37.3 ≤ v < 38.0 | v < 35.5 or v ≥ 38.0 | 
| `SLEEP` | 7.0 ≤ v ≤ 9.0 | 5.5 ≤ v < 7.0 or 9.0 < v ≤ 10.0 | v < 5.5 or v > 10.0 | 
| `MOOD` | v ≥ 4.0 | 2.5 ≤ v < 4.0 | v < 2.5 | 

Test data for each level is in `docs/data_guide.md`.

**Exact behavior of the `HealthRepository` methods:**

1. **`updateIndicatorValue(id, newValue)`:** Sets `currentValue = newValue`, assigns `level = evaluateLevel(id, newValue)` and rotates the window of the last 7 readings in `history7Days` by dropping the first element (`drop(1)`) and appending `newValue` (`+ newValue`).

2. **Countermeasure:** There is no method that restores the indicator. The new reading after the plan is recorded with `updateIndicatorValue` (see 1.1).

3. **`recordPvtResult(reactionTimeMs)`:** The demo PVT (see 1.1) measures a single touch reaction time in milliseconds and affects the behavioral indicator `"MOOD"` only if the result is lower than the current `currentMood` (it never improves the state nor inserts repeated readings in `history7Days`):

   * If `reactionTimeMs > 500L` and `currentMood > 2.0f`: calls `updateIndicatorValue("MOOD", 2.0f)` (`CRITICAL`).

   * If `reactionTimeMs in 381L..500L` and `currentMood > 3.0f`: calls `updateIndicatorValue("MOOD", 3.0f)` (at least `WARNING`, or keeps `CRITICAL` if it was already at or below `2.0f`).

   * If `reactionTimeMs <= 380L` (or `currentMood` was already at or below the target): state and `history7Days` stay unchanged.

   * If the user taps before the green stimulus appears, `PvtTestScreen` shows `"False start — retry"` and restarts the attempt without calling `recordPvtResult`.

4. **`injectScenario(scenario)`:** Each scenario changes only the indicators linked to its `SpaceHazard`:

   * `Scenario.NOMINAL`: Restores the 6 indicators to their full initial seed state (`currentValue = normalValue`, `level = StatusLevel.NORMAL` and `history7Days` restored to the original seed list of 7 varied nominal readings).

   * `Scenario.RADIATION_SPIKE`: Calls only `updateIndicatorValue("TEMP", 38.4f)` (`CRITICAL`, `SpaceHazard.RADIATION`).

   * `Scenario.ISOLATION_FATIGUE`: Calls only `updateIndicatorValue("SLEEP", 4.5f)` (`CRITICAL`, `SpaceHazard.ISOLATION`) and `updateIndicatorValue("MOOD", 2.0f)` (`CRITICAL`, `SpaceHazard.ISOLATION`).

5. **`setEnvironment(env)`:** Updates only the `StateFlow<SpaceEnvironment>` to show the current operating context. It does not affect `evaluateLevel` (see 1.1).

6. **Image attachment in Module 3 (`AssessmentScreen`):** Purely local visual state (`var hasAttachedImage by remember { mutableStateOf(false) }`) that shows an on-screen confirmation; it does not modify `VitalIndicator`.

7. **Re-measure reminder (FR-12):** When all plan steps are checked, `CountermeasurePlanScreen` requests the `POST_NOTIFICATIONS` permission (Android 13+) and calls `RemeasureReminder.schedule` with the demo wait. `RemeasureReceiver` posts the notification; tapping it opens `MainActivity` with `EXTRA_INDICATOR_ID`, which navigates to `AssessmentScreen` for that indicator. Without the permission, no notification is shown and the in-app countdown still works.

## 5. Code conventions

* **`.kt` files, `@Composable` functions and classes:** `PascalCase` (e.g. function `DashboardScreen()`, class `VitalIndicator`).

* **Regular functions, properties and variables:** `camelCase`.

* **Package names:** `lowercase`, dot-separated (e.g. `com.softiecare.app.features.countermeasures`).

* **Language:** Interface text, logs, code comments, test names and documentation are written in English.

* **Logs and console output (`println` / `Log.d`):** No trailing punctuation inside printed strings (e.g. `println("Updating glucose reading")`).

* **Comments:** Short and only where the logic is not obvious.

* **Implementation priority:** Functional, direct and standard code over premature optimization.

## 6. Shared base components (`ui/components/`)

Delivered with the scaffolding and integration to keep the visual design consistent:

* `SpaceCard(statusLevel: StatusLevel, onClick: (() -> Unit)?, content: @Composable () -> Unit)`: Container with `SurfacePanel` background and neutral border. Status is communicated only by `StatusBadge`; `statusLevel` is kept so the signature does not break.

* `StatusBadge(level: StatusLevel)`: Semantic badge with the text of `statusLabel(level)` ("STABLE", "NOT OPTIMAL" or "CRITICAL") in white on `statusContainerColor(level)`. The text never wraps.

* `IndicatorTile(indicator: VitalIndicator, onClick: () -> Unit, modifier: Modifier = Modifier, onMeasure: (() -> Unit)? = null)`: Large tile with icon, name, big value with unit, small chart (`Sparkline`), `StatusBadge(displayLevel(indicator))` and a full-width "Measure" button. For `MOOD` it shows neither the numeric value nor the chart (1.1). Used by the Vitals tab.

* `indicatorIcon(id: String): ImageVector` and `indicatorAccent(id: String): Color`: Decorative icon (from `AppIcons`) and color per indicator. Status is communicated only by `StatusBadge`.

* `EnvironmentScene(env: SpaceEnvironment, modifier: Modifier = Modifier)` (`EnvironmentScene.kt`): Illustrated banner of the environment (spacecraft with Mars in the distance, lunar base with Earth, rover on Mars). `environmentBrush(env)` provides the app background tint.

* `displayLevel(indicator: VitalIndicator): StatusLevel`: Level shown on screen according to the presentation rule in 1.1 (`MOOD` in `CRITICAL` is shown as `WARNING`). Used for badges, colors and the overall status instead of `indicator.level`.

* `HazardTag(hazard: SpaceHazard)`: Informational chip showing which of the 5 HRP hazards is linked to the indicator, with the readable name from `hazardLabel(hazard)`.

* `StepAnimation(indicatorId: String, stepIndex: Int, accent: Color, modifier: Modifier = Modifier)` and `AnimatedEmoji(codepoint: String, modifier: Modifier = Modifier)` (`StepAnimation.kt`): Looping Noto animated emoji for each step of `measurementGuide` on a circle in the indicator color; `stepIndex` equal to the guide size is the value entry step.

* `AppLogo(size: Dp = 48.dp, modifier: Modifier = Modifier)`: SoftieCare logo (launcher icon foreground) cropped to a circle. Used on the Today header and the About screen.

* `BackPill(accent: Color, text: String = "Back", onClick: () -> Unit)`: Pill-shaped back button with an arrow and a border in the indicator color.

* `PrimaryActionButton(text: String, onClick: () -> Unit)`: High-contrast button to confirm measurements or medical protocols.

## 7. Design guide (design tokens)

The whole interface uses a strict high-contrast dark mode. No file inside `features/` declares hard-coded hex colors (`Color(0xFF...)`); they all use the constants in `ui/theme/Color.kt` (aligned with the `StatusLevel` names):

* `SpaceBackground` (`0xFF0B0F19`): General app background.

* `SurfacePanel` (`0xFF161F30`): Background of cards, panels and dialogs.

* `StatusNormal` (`0xFF00E676`): Nominal green (`StatusLevel.NORMAL`).

* `StatusWarning` (`0xFFFFEA00`): Caution yellow (`StatusLevel.WARNING`).

* `StatusCritical` (`0xFFFF1744`): Critical alert red (`StatusLevel.CRITICAL`).

* `TextPrimary` (`0xFFF8FAFC`): Off-white primary text.

* `TextSecondary` (`0xFF94A3B8`): Secondary labels and units.

* `StatusNormalStrong` (`0xFF15803D`), `StatusWarningStrong` (`0xFFB45309`), `StatusCriticalStrong` (`0xFFC62828`): `StatusBadge` backgrounds with white text (contrast of at least 5:1). The bright `Status*` colors are used for lines, charts and the overall status ring.

## 8. Work phases

### Phase 1: Alignment and scaffolding

* **Carlos Alberto Jaime Mascareño (Module 1):** Creates the Android Studio project (`minSdk 26`), sets up GitHub and pushes the full scaffolding: `Color.kt`, `Theme.kt`, `Type.kt`, `SharedComponents.kt`, `HealthModels.kt`, `HealthRepository.kt` (with 7 varied seed readings inside the normal range in each `history7Days`), a `SimulatorSheet.kt` stub and the `MainActivity.kt` skeleton. Checks that a simple `@Preview` renders while consuming `HealthRepository`.

* **Member 2 (Module 2):** Defines the main screen layout of `DashboardScreen.kt`.

* **Member 3 (Module 3):** Builds the numeric entry form with range validation in `AssessmentScreen.kt` and the single-attempt state machine (including false start detection) in `PvtTestScreen.kt`.

* **Member 4 (Module 4):** Designs the `TrendLineChart` component with `Canvas` and the `CountermeasureDetailScreen.kt` view.

### Phase 2: Module development in isolation

* Each member develops their screen on their module's `feature/Modulo<N>` branch (branch table in `README.md`).

* **Isolation rule:** Nobody commits changes to `MainActivity.kt` during this phase. Each screen is tested with `@Preview` in Android Studio (or, if `MainActivity.kt` is edited temporarily to test on the emulator, it is reverted before committing by running from the repository root: `git restore app/src/main/java/com/softiecare/app/MainActivity.kt`).

### Phase 3: Integration of the "Evaluate & Act" loop

* The 4 branches are merged and Carlos Alberto Jaime Mascareño connects the screens in `MainActivity.kt`.

* The full flow is verified:

  1. Open the Simulator (`showSimulator = true`) and inject `Scenario.RADIATION_SPIKE`, and also enter a manual value in `AssessmentScreen` for `"BP_SYS"` or `"HEART_RATE"` (for example `145.0f` mmHg, to also trigger the `GRAVITY` hazard during the demo).

  2. Check that the overall status and the "Needs attention" section of the Today tab change instantly.

  3. Tap the indicator in alert to open `CountermeasureDetailScreen` and see the updated chart of the last 7 readings.

  4. Press "Start countermeasure", check the plan steps, wait, and confirm that the re-measure notification appears. Press "Measure again" (or the notification), enter an in-range value and check that the level changes to `NORMAL`.

### Phase 4: Polish, optional extensions and demo freeze

* **Before submission:** Full code freeze, backup installable APK and demo video recording.

## 9. Common technical requirements (checklist for the 4 modules)

Each delivered module meets the following before a Pull Request to `Main`:

* Reads reactive state from `HealthRepository` with `collectAsState()` and writes only through the public `HealthRepository` methods (section 4.2).

* Uses only the colors in `ui/theme/Color.kt` and the components in `ui/components/`.

* Follows the function signatures in section 4.2 exactly.

* Every on-screen level badge or color uses `displayLevel(indicator)` (section 6), never `indicator.level` directly.

* Input validation in `AssessmentScreen`: convert with `toFloatOrNull()` and check that the value is inside the valid capture range in table 4.3 (e.g. no negative numbers, `"MOOD"` limited to `1.0f..5.0f` and `"SLEEP"` to `0.0f..24.0f`) before calling `HealthRepository.updateIndicatorValue(id, floatValue)`.

* All interface text is in English (NFR-03).

## 10. Definition of done and deployment

**Minimum viable product (MVP):** The app builds without errors in Android Studio, runs on the emulator or a physical device and meets the requirements in section 1.2.
