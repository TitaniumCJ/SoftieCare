package com.softiecare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

// Animated emoji from Noto Emoji Animation (Google, CC BY 4.0), bundled in assets/lottie/emoji/<codepoint>.json
@Composable
fun AnimatedEmoji(codepoint: String, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("lottie/emoji/$codepoint.json"))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}

object Emoji {
    const val RELIEVED = "1f60c"
    const val FLEXED_ARM = "1f4aa"
    const val STETHOSCOPE = "1fa7a"
    const val WRITING_HAND = "270d_fe0f"
    const val EXHALING = "1f62e_200d_1f4a8"
    const val POINTING_FINGER = "1f449"
    const val ANATOMICAL_HEART = "1fac0"
    const val HOURGLASS = "23f3"
    const val BLOOD_DROP = "1fa78"
    const val CHART_UP = "1f4c8"
    const val ALARM_CLOCK = "23f0"
    const val THERMOMETER_FACE = "1f912"
    const val THERMOMETER = "1f321_fe0f"
    const val SLEEPING = "1f634"
    const val SUNRISE = "1f305"
    const val FULL_BATTERY = "1f50b"
    const val SMILE = "1f642"
    const val THUMBS_UP = "1f44d"
    const val CHECK = "2705"
}

// One animation per step of each measurement guide; stepIndex == guide size is the value entry step
private fun emojiFor(id: String, step: Int): String = when (id) {
    "BP_SYS" -> listOf(Emoji.RELIEVED, Emoji.FLEXED_ARM, Emoji.STETHOSCOPE)
    "HEART_RATE" -> listOf(Emoji.EXHALING, Emoji.POINTING_FINGER, Emoji.ANATOMICAL_HEART)
    "GLUCOSE" -> listOf(Emoji.HOURGLASS, Emoji.BLOOD_DROP, Emoji.CHART_UP)
    "TEMP" -> listOf(Emoji.ALARM_CLOCK, Emoji.THERMOMETER_FACE, Emoji.THERMOMETER)
    "SLEEP" -> listOf(Emoji.SLEEPING, Emoji.SUNRISE, Emoji.FULL_BATTERY)
    else -> listOf(Emoji.EXHALING, Emoji.SMILE, Emoji.THUMBS_UP)
}.getOrElse(step) { Emoji.WRITING_HAND }

@Composable
fun StepAnimation(
    indicatorId: String,
    stepIndex: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(190.dp)
                .background(accent.copy(alpha = 0.14f), CircleShape)
        )
        AnimatedEmoji(codepoint = emojiFor(indicatorId, stepIndex), modifier = Modifier.size(150.dp))
    }
}
