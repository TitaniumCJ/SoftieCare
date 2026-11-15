package com.softiecare.app.features.countermeasures

import com.softiecare.app.core.model.VitalIndicator

// Countermeasure plan: what to do, what to expect, when to ask for help and how long to wait before re-measuring.
// Heuristic demo content (Blueprint 1.1); it does not replace the flight surgeon.
data class CountermeasurePlan(
    val title: String,
    val steps: List<String>,
    val expect: List<String>,
    val alarms: List<String>,
    val waitMinutes: Int
)

// The plan depends on whether the value is above or below the nominal value
fun planFor(indicator: VitalIndicator): CountermeasurePlan {
    val high = indicator.currentValue > indicator.normalValue
    return when (indicator.id) {
        "BP_SYS" -> if (high) CountermeasurePlan(
            title = "High blood pressure",
            steps = listOf(
                "Sit in a quiet area of the module for 10 minutes.",
                "Breathe slowly: inhale for 4 seconds and exhale for 6 seconds, for 5 minutes.",
                "Drink water and avoid caffeine.",
                "Stop intense exercise until the next measurement."
            ),
            expect = listOf(
                "Blood pressure usually drops gradually within 15 to 30 minutes.",
                "Some tension or a mild headache may be felt."
            ),
            alarms = listOf("Chest pain", "Severe headache", "Blurred vision or confusion"),
            waitMinutes = 30
        ) else CountermeasurePlan(
            title = "Low blood pressure",
            steps = listOf(
                "Lie down and raise the legs.",
                "Drink 500 ml of water.",
                "Put on the compression garment on the legs.",
                "Get up slowly, sitting first."
            ),
            expect = listOf(
                "Dizziness usually eases within 10 to 20 minutes.",
                "In microgravity, fluid shifts make this change more common."
            ),
            alarms = listOf("Fainting", "Confusion", "Very fast or weak pulse"),
            waitMinutes = 20
        )

        "HEART_RATE" -> if (high) CountermeasurePlan(
            title = "High heart rate",
            steps = listOf(
                "Stop the current physical activity.",
                "Breathe slowly for 5 minutes.",
                "Drink water.",
                "Stay in a cool area of the module."
            ),
            expect = listOf("Heart rate usually drops after 10 to 15 minutes of rest."),
            alarms = listOf("Irregular palpitations", "Chest pain", "Shortness of breath"),
            waitMinutes = 15
        ) else CountermeasurePlan(
            title = "Low heart rate",
            steps = listOf(
                "Stay seated.",
                "Drink water.",
                "Get up slowly."
            ),
            expect = listOf("A low resting heart rate can be normal; a new measurement confirms it."),
            alarms = listOf("Dizziness", "Fainting", "Extreme fatigue"),
            waitMinutes = 15
        )

        "GLUCOSE" -> if (high) CountermeasurePlan(
            title = "High glucose",
            steps = listOf(
                "Drink water.",
                "Do 10 to 15 minutes of light activity if there are no symptoms.",
                "Review the last ration of the habitat diet plan."
            ),
            expect = listOf("Glucose drops gradually over the next hour."),
            alarms = listOf("Extreme thirst", "Nausea or vomiting", "Confusion"),
            waitMinutes = 60
        ) else CountermeasurePlan(
            title = "Low glucose",
            steps = listOf(
                "Take 15 g of fast-acting carbohydrates (juice or glucose tablets).",
                "Stay seated during the wait.",
                "After the new measurement, eat a snack from the ration."
            ),
            expect = listOf(
                "Shaking and sweating usually ease within about 15 minutes.",
                "If the new measurement is still low, repeat the 15 g of carbohydrates."
            ),
            alarms = listOf("Confusion", "Shaking that does not stop", "Severe drowsiness"),
            waitMinutes = 15
        )

        "TEMP" -> if (high) CountermeasurePlan(
            title = "High temperature",
            steps = listOf(
                "Move to the most shielded area of the habitat.",
                "Rest and drink water often.",
                "Wear light clothing and lower the module temperature."
            ),
            expect = listOf(
                "Temperature drops slowly; check it every 30 minutes.",
                "Tiredness and chills may occur."
            ),
            alarms = listOf("Above 39 °C", "Difficulty breathing", "Confusion or stiff neck"),
            waitMinutes = 30
        ) else CountermeasurePlan(
            title = "Low temperature",
            steps = listOf(
                "Put on layers of dry clothing.",
                "Have a warm drink.",
                "Raise the module temperature."
            ),
            expect = listOf("The body recovers its temperature within 20 to 30 minutes."),
            alarms = listOf("Intense shivering", "Confusion", "Drowsiness"),
            waitMinutes = 30
        )

        "SLEEP" -> CountermeasurePlan(
            title = "Insufficient rest",
            steps = listOf(
                "Set the module lighting to warm light.",
                "Postpone non-critical tasks.",
                "Avoid screens one hour before sleeping.",
                "Schedule an 8-hour rest period."
            ),
            expect = listOf("Focus improves after one or two full sleep periods."),
            alarms = listOf("Drowsiness during critical tasks", "Repeated errors in procedures"),
            waitMinutes = 480
        )

        else -> CountermeasurePlan(
            title = "Time for yourself",
            steps = listOf(
                "Schedule a delayed message with family.",
                "Do 20 minutes of exercise.",
                "Choose a recreational activity from the crew plan."
            ),
            expect = listOf("Feeling better can take a few hours; this is normal on long missions."),
            alarms = listOf("Discomfort that lasts several days"),
            waitMinutes = 60
        )
    }
}
