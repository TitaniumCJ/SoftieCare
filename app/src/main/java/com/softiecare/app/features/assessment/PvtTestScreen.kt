package com.softiecare.app.features.assessment

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.ui.components.PrimaryActionButton
import com.softiecare.app.ui.components.StatusBadge
import com.softiecare.app.ui.components.displayLevel
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusCritical
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlin.random.Random

private sealed interface PvtPhase {
    object Ready : PvtPhase
    object Waiting : PvtPhase
    data class Stimulus(val shownAtMs: Long) : PvtPhase
    data class Result(val reactionTimeMs: Long) : PvtPhase
    object FalseStart : PvtPhase
}

@Composable
fun PvtTestScreen(onBack: () -> Unit) {
    var phase by remember { mutableStateOf<PvtPhase>(PvtPhase.Ready) }
    val indicators by HealthRepository.indicators.collectAsState()
    val mood = indicators.firstOrNull { it.id == "MOOD" }

    // Cada cambio de fase cancela la espera anterior (un toque anticipado corta el delay)
    LaunchedEffect(phase) {
        when (phase) {
            PvtPhase.Waiting -> {
                delay(Random.nextLong(2000L, 5001L))
                phase = PvtPhase.Stimulus(SystemClock.uptimeMillis())
            }
            PvtPhase.FalseStart -> {
                delay(1500L)
                phase = PvtPhase.Waiting
            }
            else -> Unit
        }
    }

    val background = when (phase) {
        is PvtPhase.Stimulus -> StatusNormal
        PvtPhase.FalseStart -> StatusCritical
        else -> SpaceBackground
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .pointerInput(phase) {
                // onPress mide al contacto, no al soltar el dedo
                detectTapGestures(onPress = {
                    when (val p = phase) {
                        PvtPhase.Waiting -> phase = PvtPhase.FalseStart
                        is PvtPhase.Stimulus -> {
                            val ms = SystemClock.uptimeMillis() - p.shownAtMs
                            HealthRepository.recordPvtResult(ms)
                            println("Resultado PVT registrado")
                            phase = PvtPhase.Result(ms)
                        }
                        else -> Unit
                    }
                })
            }
    ) {
        if (phase == PvtPhase.Ready || phase is PvtPhase.Result) {
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
                Text("← Volver", color = TextSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val p = phase) {
                PvtPhase.Ready -> {
                    Text(
                        "Test de Vigilancia Psicomotora",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Al ponerse verde la pantalla, tocarla lo más rápido posible. " +
                            "Un toque antes de tiempo reinicia el intento.",
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(32.dp))
                    PrimaryActionButton(text = "Iniciar prueba", onClick = { phase = PvtPhase.Waiting })
                }

                PvtPhase.Waiting -> Text("Espera el verde...", color = TextPrimary, fontSize = 28.sp)

                is PvtPhase.Stimulus -> Text(
                    "¡TOCA!",
                    color = SpaceBackground,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )

                PvtPhase.FalseStart -> Text(
                    "Salida en falso — reintentar",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                is PvtPhase.Result -> {
                    Text(
                        "${p.reactionTimeMs} ms",
                        color = TextPrimary,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    if (mood != null) {
                        // Blueprint 1.1: sin valor numerico del animo y nunca "CRITICO"
                        Text(mood.name, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        StatusBadge(level = displayLevel(mood))
                    }
                    Spacer(Modifier.height(32.dp))
                    PrimaryActionButton(text = "Repetir prueba", onClick = { phase = PvtPhase.Waiting })
                }
            }
        }
    }
}

@Preview
@Composable
private fun PvtTestScreenPreview() {
    PvtTestScreen(onBack = {})
}
