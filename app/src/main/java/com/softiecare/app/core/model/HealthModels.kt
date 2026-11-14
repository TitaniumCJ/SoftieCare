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
