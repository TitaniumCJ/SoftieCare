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

    // Cortes continuos de la seccion 4.3: CRITICAL -> WARNING -> NORMAL
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

    fun applyCountermeasure(id: String) {
        updateById(id) {
            it.copy(
                currentValue = it.normalValue,
                level = StatusLevel.NORMAL,
                history7Days = it.history7Days.drop(1) + it.normalValue
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
        // Solo empeora el animo y evita lecturas repetidas en history7Days
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

    // Semilla: 7 lecturas dentro del rango NORMAL, la ultima igual a normalValue
    private fun seedIndicators(): List<VitalIndicator> = listOf(
        VitalIndicator(
            id = "BP_SYS",
            name = "Presion Sistolica",
            currentValue = 120.0f,
            normalValue = 120.0f,
            unit = "mmHg",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.THORAX_CARDIO,
            primaryHazard = SpaceHazard.GRAVITY,
            history7Days = listOf(118.0f, 122.0f, 119.0f, 124.0f, 117.0f, 121.0f, 120.0f),
            measurementGuide = listOf(
                "Permanecer sentado en reposo durante 5 minutos",
                "Colocar el brazalete en el brazo a la altura del corazon",
                "Registrar solo el valor sistolico en mmHg"
            ),
            countermeasureProtocol = "Reposo sentado 10 minutos, hidratacion con 500 ml de agua y nueva medicion. Si persiste, usar prenda de compresion en piernas y notificar al oficial medico de a bordo."
        ),
        VitalIndicator(
            id = "HEART_RATE",
            name = "Frecuencia Cardiaca",
            currentValue = 72.0f,
            normalValue = 72.0f,
            unit = "bpm",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.THORAX_CARDIO,
            primaryHazard = SpaceHazard.GRAVITY,
            history7Days = listOf(70.0f, 74.0f, 71.0f, 76.0f, 69.0f, 73.0f, 72.0f),
            measurementGuide = listOf(
                "Permanecer en reposo durante 5 minutos",
                "Colocar el sensor de pulso en el dedo indice",
                "Registrar el valor estable en bpm"
            ),
            countermeasureProtocol = "Respiracion controlada durante 5 minutos, hidratacion y nueva medicion en 15 minutos. Suspender ejercicio intenso hasta normalizar."
        ),
        VitalIndicator(
            id = "GLUCOSE",
            name = "Glucosa en Ayunas",
            currentValue = 90.0f,
            normalValue = 90.0f,
            unit = "mg/dL",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.METABOLIC_ENDOCRINE,
            primaryHazard = SpaceHazard.HOSTILE_ENVIRONMENT,
            history7Days = listOf(88.0f, 92.0f, 87.0f, 94.0f, 89.0f, 91.0f, 90.0f),
            measurementGuide = listOf(
                "Confirmar al menos 8 horas de ayuno",
                "Limpiar el dedo y usar una lanceta nueva",
                "Colocar la gota en la tira y registrar el valor en mg/dL"
            ),
            countermeasureProtocol = "Valor bajo: ingerir 15 g de carbohidratos de rapida absorcion y medir en 15 minutos. Valor alto: hidratacion y revision de la racion de dieta del habitat."
        ),
        VitalIndicator(
            id = "TEMP",
            name = "Temperatura Corporal",
            currentValue = 36.6f,
            normalValue = 36.6f,
            unit = "°C",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.METABOLIC_ENDOCRINE,
            primaryHazard = SpaceHazard.RADIATION,
            history7Days = listOf(36.5f, 36.7f, 36.4f, 36.8f, 36.5f, 36.7f, 36.6f),
            measurementGuide = listOf(
                "No comer ni beber durante los 15 minutos previos",
                "Colocar el termometro bajo la lengua",
                "Esperar la senal y registrar el valor en °C"
            ),
            countermeasureProtocol = "Reposo en el area con mayor blindaje del habitat, hidratacion y control de temperatura cada 30 minutos."
        ),
        VitalIndicator(
            id = "SLEEP",
            name = "Horas de Sueño",
            currentValue = 7.5f,
            normalValue = 7.5f,
            unit = "hrs",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.HEAD_MIND,
            primaryHazard = SpaceHazard.ISOLATION,
            history7Days = listOf(7.3f, 7.8f, 7.1f, 8.0f, 7.4f, 7.6f, 7.5f),
            measurementGuide = listOf(
                "Revisar la hora de inicio del sueño",
                "Revisar la hora de despertar",
                "Registrar el total de horas dormidas"
            ),
            countermeasureProtocol = "Ajustar la iluminacion del modulo a luz calida, posponer tareas no criticas y programar un periodo de descanso de 8 horas."
        ),
        VitalIndicator(
            id = "MOOD",
            name = "Estado Conductual",
            currentValue = 4.5f,
            normalValue = 4.5f,
            unit = "pts",
            level = StatusLevel.NORMAL,
            bodyZone = BodyZone.HEAD_MIND,
            primaryHazard = SpaceHazard.ISOLATION,
            history7Days = listOf(4.3f, 4.6f, 4.2f, 4.8f, 4.4f, 4.7f, 4.5f),
            measurementGuide = listOf(
                "Ubicarse en un lugar tranquilo del modulo",
                "Evaluar animo y nivel de alerta de 1 (muy bajo) a 5 (optimo)",
                "Registrar el valor que mejor describa el estado actual"
            ),
            countermeasureProtocol = "Programar un mensaje diferido con familia, 20 minutos de ejercicio y una actividad recreativa del plan de tripulacion."
        )
    )
}
