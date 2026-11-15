package com.softiecare.app.features.assessment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.core.model.VitalIndicator
import com.softiecare.app.ui.components.AppIcons
import com.softiecare.app.ui.components.BackPill
import com.softiecare.app.ui.components.StatusBadge
import com.softiecare.app.ui.components.StepAnimation
import com.softiecare.app.ui.components.displayLevel
import com.softiecare.app.ui.components.indicatorAccent
import com.softiecare.app.ui.components.indicatorIcon
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusCritical
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.SurfacePanel
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary
import java.util.Locale

// Copy of the "Valid capture range" column in Blueprint table 4.3
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
            .padding(20.dp)
    ) {
        val accent = indicatorAccent(indicatorId)
        BackPill(accent = accent, onClick = onBack)
        Spacer(Modifier.height(16.dp))

        if (indicator == null) {
            Text("Indicator not found: $indicatorId", color = StatusCritical)
            return@Column
        }

        val guide = indicator.measurementGuide
        val inputStep = guide.size // the last step is always value entry
        val isMood = indicator.id == "MOOD"

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accent.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(indicatorIcon(indicator.id), contentDescription = null, tint = accent)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(indicator.name, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                // Blueprint 1.1: the mood numeric value is not shown
                if (!isMood) {
                    Text(
                        "Current reading: ${indicator.currentValue} ${indicator.unit}",
                        color = TextSecondary
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        if (saved) {
            SavedCard(indicator = indicator, accent = accent, showValue = !isMood)
            Spacer(Modifier.height(24.dp))
            BigButton(text = "Back to dashboard", icon = AppIcons.Back, accent = accent, onClick = onBack)
            return@Column
        }

        StepProgress(current = step, total = inputStep + 1, accent = accent)
        Spacer(Modifier.height(16.dp))

        Surface(color = SurfacePanel, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                // Animation for the current step
                StepAnimation(
                    indicatorId = indicator.id,
                    stepIndex = step,
                    accent = accent,
                    displayText = displayValue(indicator.normalValue, indicator.unit),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "STEP ${step + 1} OF ${inputStep + 1}",
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(6.dp))
                if (step < inputStep) {
                    Text(guide[step], color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp)
                } else {
                    Text(
                        "Enter the measured value (${indicator.unit})",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
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
                        textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = accent,
                            unfocusedBorderColor = TextSecondary,
                            focusedLabelColor = accent,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = accent,
                            errorBorderColor = StatusCritical,
                            errorLabelColor = StatusCritical
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, color = StatusCritical)
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { hasAttachedImage = true }) {
                        Text(
                            if (hasAttachedImage) "✓ Image attached" else "Attach device image",
                            color = if (hasAttachedImage) StatusNormal else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 0) {
                OutlinedButton(
                    onClick = { step-- },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, TextSecondary)
                ) {
                    Icon(AppIcons.Back, contentDescription = null, tint = TextPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text("Previous", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }

            val next = step < inputStep
            BigButton(
                text = if (next) "Next" else "Save reading",
                icon = if (next) AppIcons.Forward else AppIcons.CheckCircle,
                accent = accent,
                iconAtEnd = next,
                modifier = Modifier.weight(if (step > 0) 1.5f else 1f),
                onClick = {
                    if (next) {
                        step++
                    } else {
                        // Accepts a decimal comma from localized keyboards
                        val parsed = input.trim().replace(',', '.').toFloatOrNull()
                        val range = captureRanges[indicatorId]
                        error = when {
                            parsed == null -> "Not a number"
                            range == null -> "No capture range defined for this indicator"
                            parsed !in range -> // also rejects NaN and Infinity
                                "Out of range: ${range.start}–${range.endInclusive} ${indicator.unit}"
                            else -> null
                        }
                        if (error == null && parsed != null) {
                            HealthRepository.updateIndicatorValue(indicatorId, parsed)
                            println("Manual reading recorded for $indicatorId")
                            saved = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BigButton(
    text: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    iconAtEnd: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = SpaceBackground)
    ) {
        if (!iconAtEnd) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (iconAtEnd) {
            Spacer(Modifier.width(8.dp))
            Icon(icon, contentDescription = null)
        }
    }
}

// Segmented progress bar: one segment per step
@Composable
private fun StepProgress(current: Int, total: Int, accent: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(
                        if (i <= current) accent else TextSecondary.copy(alpha = 0.25f),
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
private fun SavedCard(indicator: VitalIndicator, accent: Color, showValue: Boolean) {
    Surface(color = SurfacePanel, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(AppIcons.CheckCircle, contentDescription = null, tint = accent, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(12.dp))
            Text("Reading saved", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            if (showValue) {
                Spacer(Modifier.height(8.dp))
                Text("${indicator.currentValue} ${indicator.unit}", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            StatusBadge(level = displayLevel(indicator))
        }
    }
}

private fun displayValue(v: Float, unit: String): String =
    if (v >= 100f) String.format(Locale.US, "%.0f %s", v, unit) else String.format(Locale.US, "%.1f %s", v, unit)

@Preview
@Composable
private fun AssessmentScreenPreview() {
    AssessmentScreen(indicatorId = "GLUCOSE", onBack = {})
}
