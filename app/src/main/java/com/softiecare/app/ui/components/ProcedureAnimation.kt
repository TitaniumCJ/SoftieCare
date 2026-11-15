package com.softiecare.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

// Loads app/src/main/assets/lottie/<lowercase id>.json (e.g. bp_sys.json)
// If the file does not exist, the composition stays null and nothing is drawn
@Composable
fun ProcedureAnimation(indicatorId: String, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("lottie/${indicatorId.lowercase()}.json")
    )
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}
