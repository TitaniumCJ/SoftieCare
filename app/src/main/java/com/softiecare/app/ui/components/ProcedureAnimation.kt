package com.softiecare.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

// Busca app/src/main/assets/lottie/<id en minusculas>.json (ej. bp_sys.json)
// Si el archivo no existe, la composicion queda en null y no se dibuja nada
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
