package com.softiecare.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.model.SpaceHazard
import com.softiecare.app.core.model.StatusLevel
import com.softiecare.app.core.model.VitalIndicator
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusCritical
import com.softiecare.app.ui.theme.StatusCriticalStrong
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.StatusNormalStrong
import com.softiecare.app.ui.theme.StatusWarning
import com.softiecare.app.ui.theme.StatusWarningStrong
import com.softiecare.app.ui.theme.SurfacePanel
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary
import java.util.Locale

// Color brillante para lineas, graficas y zonas sobre fondo oscuro
fun statusColor(level: StatusLevel): Color = when (level) {
    StatusLevel.NORMAL -> StatusNormal
    StatusLevel.WARNING -> StatusWarning
    StatusLevel.CRITICAL -> StatusCritical
}

// Fondo de etiqueta con texto blanco
fun statusContainerColor(level: StatusLevel): Color = when (level) {
    StatusLevel.NORMAL -> StatusNormalStrong
    StatusLevel.WARNING -> StatusWarningStrong
    StatusLevel.CRITICAL -> StatusCriticalStrong
}

fun statusLabel(level: StatusLevel): String = when (level) {
    StatusLevel.NORMAL -> "ESTABLE"
    StatusLevel.WARNING -> "NO OPTIMO"
    StatusLevel.CRITICAL -> "CRITICO"
}

// Nivel que se muestra en pantalla (Blueprint 1.1): MOOD nunca se presenta como critico
fun displayLevel(indicator: VitalIndicator): StatusLevel =
    if (indicator.id == "MOOD" && indicator.level == StatusLevel.CRITICAL) StatusLevel.WARNING
    else indicator.level

private val PanelBorder = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.25f))

private fun formatValue(v: Float): String = String.format(Locale.US, "%.1f", v)

// El borde es neutro: el estado se comunica solo con StatusBadge (Blueprint 6)
@Composable
fun SpaceCard(
    statusLevel: StatusLevel,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfacePanel,
        shape = RoundedCornerShape(16.dp),
        border = PanelBorder
    ) {
        // clickable dentro del Surface para que el ripple respete las esquinas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun StatusBadge(level: StatusLevel) {
    Surface(
        color = statusContainerColor(level),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = statusLabel(level),
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun HazardTag(hazard: SpaceHazard) {
    Surface(
        color = SpaceBackground,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, TextSecondary)
    ) {
        Text(
            text = hazard.name,
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PrimaryActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StatusNormal,
            contentColor = SpaceBackground
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

// Cuadro grande de un indicador: nombre, valor en grande y etiqueta de estado
@Composable
fun IndicatorTile(
    indicator: VitalIndicator,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMeasure: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        color = SurfacePanel,
        shape = RoundedCornerShape(16.dp),
        border = PanelBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = indicator.name,
                color = TextSecondary,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            if (indicator.id == "MOOD") {
                // Blueprint 1.1: no se resalta el valor numerico del animo
                Text(
                    text = "Toca para ver detalle",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatValue(indicator.currentValue),
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = indicator.unit,
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(displayLevel(indicator))
                Spacer(Modifier.weight(1f))
                if (onMeasure != null) {
                    TextButton(onClick = onMeasure) {
                        Text("Medir", color = TextPrimary)
                    }
                }
            }
        }
    }
}
