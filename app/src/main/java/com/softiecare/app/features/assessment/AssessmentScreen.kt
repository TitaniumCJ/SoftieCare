package com.softiecare.app.features.assessment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.ui.components.HazardTag
import com.softiecare.app.ui.components.PrimaryActionButton
import com.softiecare.app.ui.components.SpaceCard
import com.softiecare.app.ui.components.StatusBadge
import com.softiecare.app.ui.components.displayLevel
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusCritical
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary

// Copia de la columna "Rango valido de captura" de la tabla 4.3 del blueprint
private val captureRanges: Map<String, ClosedFloatingPointRange<Float>> = mapOf(
    "BP_SYS" to 50.0f..250.0f,
    "HEART_RATE" to 30.0f..220.0f,
    "GLUCOSE" to 30.0f..400.0f,
    "TEMP" to 32.0f..43.0f,
    "SLEEP" to 0.0f..24.0f,
    "MOOD" to 1.0f..5.0f
)

@Composable
fun AssessmentScreen(indicatorId: String, onBack: () -> Unit) {
    val indicators by HealthRepository.indicators.collectAsState()
    val indicator = indicators.firstOrNull { it.id == indicatorId }

    var step by remember(indicatorId) { mutableIntStateOf(0) }
    var input by remember(indicatorId) { mutableStateOf("") }
    var error by remember(indicatorId) { mutableStateOf<String?>(null) }
    var saved by remember(indicatorId) { mutableStateOf(false) }
    var hasAttachedImage by remember(indicatorId) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Volver", color = TextSecondary)
        }

        if (indicator == null) {
            Text("Indicador no encontrado: $indicatorId", color = StatusCritical)
            return@Column
        }

        val guide = indicator.measurementGuide
        val inputStep = guide.size // el ultimo paso siempre es la captura

        Text(indicator.name, color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HazardTag(hazard = indicator.primaryHazard)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Lectura actual: ${indicator.currentValue} ${indicator.unit}",
            color = TextSecondary
        )
        Spacer(Modifier.height(24.dp))

        if (saved) {
            SpaceCard(statusLevel = indicator.level, onClick = null) {
                Column(Modifier.padding(16.dp)) {
                    Text("Lectura registrada", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("${indicator.currentValue} ${indicator.unit}", color = TextPrimary, fontSize = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    StatusBadge(level = displayLevel(indicator))
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryActionButton(text = "Volver al panel", onClick = onBack)
            return@Column
        }

        Text(
            "Paso ${step + 1} de ${inputStep + 1}",
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))

        SpaceCard(statusLevel = indicator.level, onClick = null) {
            Column(Modifier.padding(16.dp)) {
                if (step < inputStep) {
                    Text(guide[step], color = TextPrimary, fontSize = 18.sp)
                } else {
                    Text(
                        "Valor medido (${indicator.unit})",
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = input,
                        onValueChange = {
                            input = it
                            error = null
                        },
                        singleLine = true,
                        isError = error != null,
                        label = { Text(indicator.unit) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = StatusNormal,
                            unfocusedBorderColor = TextSecondary,
                            focusedLabelColor = StatusNormal,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = StatusNormal,
                            errorBorderColor = StatusCritical,
                            errorLabelColor = StatusCritical
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = StatusCritical)
                    }
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { hasAttachedImage = true }) {
                        Text(
                            if (hasAttachedImage) "✓ Imagen adjuntada" else "Adjuntar imagen del dispositivo",
                            color = if (hasAttachedImage) StatusNormal else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (step > 0) {
                TextButton(onClick = { step-- }) {
                    Text("Anterior", color = TextSecondary)
                }
            } else {
                Spacer(Modifier)
            }

            if (step < inputStep) {
                PrimaryActionButton(text = "Siguiente", onClick = { step++ })
            } else {
                PrimaryActionButton(text = "Guardar lectura", onClick = {
                    // Acepta coma decimal del teclado en espanol
                    val parsed = input.trim().replace(',', '.').toFloatOrNull()
                    val range = captureRanges[indicatorId]
                    error = when {
                        parsed == null -> "Valor no numérico"
                        range == null -> "Indicador sin rango de captura definido"
                        parsed !in range -> // tambien rechaza NaN e Infinity
                            "Fuera de rango: ${range.start}–${range.endInclusive} ${indicator.unit}"
                        else -> null
                    }
                    if (error == null && parsed != null) {
                        HealthRepository.updateIndicatorValue(indicatorId, parsed)
                        println("Lectura manual registrada para $indicatorId")
                        saved = true
                    }
                })
            }
        }
    }
}

@Preview
@Composable
private fun AssessmentScreenPreview() {
    AssessmentScreen(indicatorId = "GLUCOSE", onBack = {})
}
