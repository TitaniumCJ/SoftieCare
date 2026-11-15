package com.softiecare.app.core.data

import com.softiecare.app.core.model.BodyZone
import com.softiecare.app.core.model.Scenario
import com.softiecare.app.core.model.SpaceEnvironment
import com.softiecare.app.core.model.SpaceHazard
import com.softiecare.app.core.model.StatusLevel
import com.softiecare.app.core.model.VitalIndicator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object HealthRepository {

    private val _environment = MutableStateFlow(SpaceEnvironment.TRANSIT_MARS_0G)
    val environment: StateFlow<SpaceEnvironment> = _environment.asStateFlow()

    private val _indicators = MutableStateFlow(seedIndicators())
    val indicators: StateFlow<List<VitalIndicator>> = _indicators.asStateFlow()

    fun setEnvironment(env: SpaceEnvironment) {
        _environment.value = env
    }

    // Continuous cutoffs from section 4.3: CRITICAL -> WARNING -> NORMAL
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

    fun updateIndicatorValue(id: String, newValue: Float) {
        updateById(id) {
            it.copy(
                currentValue = newValue,
                level = evaluateLevel(id, newValue),
                history7Days = it.history7Days.drop(1) + newValue
            )
        }
    }

    fun recordPvtResult(reactionTimeMs: Long) {
        val target = when {
            reactionTimeMs > 500L -> 2.0f
            reactionTimeMs >= 381L -> 3.0f
            else -> return
        }
        val currentMood = _indicators.value.firstOrNull { it.id == "MOOD" }?.currentValue ?: return
        // Only worsens mood and avoids repeated readings in history7Days
        if (currentMood > target) {
            updateIndicatorValue("MOOD", target)
        }
    }

    fun injectScenario(scenario: Scenario) {
        when (scenario) {
            Scenario.NOMINAL -> _indicators.value = seedIndicators()
            Scenario.RADIATION_SPIKE -> updateIndicatorValue("TEMP", 38.4f)
            Scenario.ISOLATION_FATIGUE -> {
                updateIndicatorValue("SLEEP", 4.5f)
                updateIndicatorValue("MOOD", 2.0f)
            }
        }
    }

    private fun updateById(id: String, transform: (VitalIndicator) -> VitalIndicator) {
        _indicators.update { list -> list.map { if (it.id == id) transform(it) else it } }
    }

    // Seed: 7 readings within the NORMAL range, the last one equal to normalValue
    private fun seedIndicators(): List<VitalIndicator> = listOf(
        VitalIndicator(
            id = "BP_SYS",
            name = "Systolic Blood Pressure",
            currentValue = 120.0f,
            normalValue = 120.0f,
            unit = "mmHg",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.THORAX_CARDIO,
            primaryHazard = SpaceHazard.GRAVITY,
            history7Days = listOf(118.0f, 122.0f, 119.0f, 124.0f, 117.0f, 121.0f, 120.0f),
            measurementGuide = listOf(
                "Sit and rest for 5 minutes",
                "Place the cuff on the upper arm at heart level",
                "Record only the systolic value in mmHg"
            ),
            countermeasureProtocol = "Seated rest for 10 minutes, drink 500 ml of water and measure again. If it persists, use the leg compression garment and notify the onboard medical officer."
        ),
        VitalIndicator(
            id = "HEART_RATE",
            name = "Heart Rate",
            currentValue = 72.0f,
            normalValue = 72.0f,
            unit = "bpm",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.THORAX_CARDIO,
            primaryHazard = SpaceHazard.GRAVITY,
            history7Days = listOf(70.0f, 74.0f, 71.0f, 76.0f, 69.0f, 73.0f, 72.0f),
            measurementGuide = listOf(
                "Rest for 5 minutes",
                "Place the pulse sensor on the index finger",
                "Record the stable value in bpm"
            ),
            countermeasureProtocol = "Controlled breathing for 5 minutes, hydration and a new measurement in 15 minutes. Avoid intense exercise until it normalizes."
        ),
        VitalIndicator(
            id = "GLUCOSE",
            name = "Fasting Glucose",
            currentValue = 90.0f,
            normalValue = 90.0f,
            unit = "mg/dL",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.METABOLIC_ENDOCRINE,
            primaryHazard = SpaceHazard.HOSTILE_ENVIRONMENT,
            history7Days = listOf(88.0f, 92.0f, 87.0f, 94.0f, 89.0f, 91.0f, 90.0f),
            measurementGuide = listOf(
                "Confirm at least 8 hours of fasting",
                "Clean the finger and use a new lancet",
                "Place the drop on the strip and record the value in mg/dL"
            ),
            countermeasureProtocol = "Low value: take 15 g of fast-acting carbohydrates and measure again in 15 minutes. High value: hydration and review of the habitat diet ration."
        ),
        VitalIndicator(
            id = "TEMP",
            name = "Body Temperature",
            currentValue = 36.6f,
            normalValue = 36.6f,
            unit = "°C",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.METABOLIC_ENDOCRINE,
            primaryHazard = SpaceHazard.RADIATION,
            history7Days = listOf(36.5f, 36.7f, 36.4f, 36.8f, 36.5f, 36.7f, 36.6f),
            measurementGuide = listOf(
                "Do not eat or drink during the previous 15 minutes",
                "Place the thermometer under the tongue",
                "Wait for the beep and record the value in °C"
            ),
            countermeasureProtocol = "Rest in the most shielded area of the habitat, hydration and temperature checks every 30 minutes."
        ),
        VitalIndicator(
            id = "SLEEP",
            name = "Sleep Hours",
            currentValue = 7.5f,
            normalValue = 7.5f,
            unit = "hrs",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.HEAD_MIND,
            primaryHazard = SpaceHazard.ISOLATION,
            history7Days = listOf(7.3f, 7.8f, 7.1f, 8.0f, 7.4f, 7.6f, 7.5f),
            measurementGuide = listOf(
                "Check the time sleep started",
                "Check the wake-up time",
                "Record the total hours slept"
            ),
            countermeasureProtocol = "Set the module lighting to warm light, postpone non-critical tasks and schedule an 8-hour rest period."
        ),
        VitalIndicator(
            id = "MOOD",
            name = "Behavioral State",
            currentValue = 4.5f,
            normalValue = 4.5f,
            unit = "pts",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.HEAD_MIND,
            primaryHazard = SpaceHazard.ISOLATION,
            history7Days = listOf(4.3f, 4.6f, 4.2f, 4.8f, 4.4f, 4.7f, 4.5f),
            measurementGuide = listOf(
                "Find a quiet place in the module",
                "Rate mood and alertness from 1 (very low) to 5 (optimal)",
                "Record the value that best describes the current state"
            ),
            countermeasureProtocol = "Schedule a delayed message with family, 20 minutes of exercise and a recreational activity from the crew plan."
        )
    )
}
