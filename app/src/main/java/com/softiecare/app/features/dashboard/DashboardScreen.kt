package com.softiecare.app.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.core.model.BodyZone
import com.softiecare.app.core.model.SpaceEnvironment
import com.softiecare.app.core.model.StatusLevel
import com.softiecare.app.core.model.VitalIndicator
import com.softiecare.app.ui.components.PrimaryActionButton
import com.softiecare.app.ui.components.SpaceCard
import com.softiecare.app.ui.components.StatusBadge
import com.softiecare.app.ui.components.statusColor
import com.softiecare.app.ui.theme.SoftieCareTheme
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.SurfacePanel
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    onIndicatorClick: (String) -> Unit,
    onOpenAssessment: (String) -> Unit,
    onOpenPvtTest: () -> Unit,
    onOpenSimulator: () -> Unit
) {
    val indicators by HealthRepository.indicators.collectAsState()
    val environment by HealthRepository.environment.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
            .padding(16.dp)
    ) {
        Header(environment = environment, onOpenSimulator = onOpenSimulator)
        Spacer(Modifier.height(16.dp))

        // Dos columnas en pantallas anchas y una sola columna en telefono vertical
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth >= 600.dp) {
                Row(Modifier.fillMaxSize()) {
                    BodyPanel(
                        indicators = indicators,
                        onIndicatorClick = onIndicatorClick,
                        modifier = Modifier
                            .weight(0.4f)
                            .verticalScroll(rememberScrollState())
                    )
                    Spacer(Modifier.width(16.dp))
                    IndicatorList(
                        indicators = indicators,
                        onIndicatorClick = onIndicatorClick,
                        onOpenAssessment = onOpenAssessment,
                        onOpenPvtTest = onOpenPvtTest,
                        modifier = Modifier
                            .weight(0.6f)
                            .verticalScroll(rememberScrollState())
                    )
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    BodyPanel(
                        indicators = indicators,
                        onIndicatorClick = onIndicatorClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                    IndicatorList(
                        indicators = indicators,
                        onIndicatorClick = onIndicatorClick,
                        onOpenAssessment = onOpenAssessment,
                        onOpenPvtTest = onOpenPvtTest,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(environment: SpaceEnvironment, onOpenSimulator: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SoftieCare",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onOpenSimulator) {
                Text(text = "Simulador", color = TextPrimary)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Selector de entorno: solo contexto visual (Blueprint 1.1, RF-03)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpaceEnvironment.values().forEach { env ->
                FilterChip(
                    selected = env == environment,
                    onClick = { HealthRepository.setEnvironment(env) },
                    label = { Text(environmentLabel(env)) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SurfacePanel,
                        labelColor = TextSecondary,
                        selectedContainerColor = SurfacePanel,
                        selectedLabelColor = StatusNormal
                    )
                )
            }
        }
    }
}

@Composable
private fun BodyPanel(
    indicators: List<VitalIndicator>,
    onIndicatorClick: (String) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .background(SurfacePanel, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ZoneBlock(BodyZone.HEAD_MIND, indicators, CircleShape, 80.dp, 80.dp, onIndicatorClick)
        ZoneBlock(BodyZone.THORAX_CARDIO, indicators, RoundedCornerShape(24.dp), 150.dp, 120.dp, onIndicatorClick)
        ZoneBlock(BodyZone.METABOLIC_ENDOCRINE, indicators, RoundedCornerShape(20.dp), 130.dp, 100.dp, onIndicatorClick)
    }
}

@Composable
private fun ZoneBlock(
    zone: BodyZone,
    indicators: List<VitalIndicator>,
    shape: Shape,
    width: Dp,
    height: Dp,
    onIndicatorClick: (String) -> Unit
) {
    // El enum esta ordenado NORMAL < WARNING < CRITICAL, por eso el maximo es el peor estado de la zona
    val worst = indicators.filter { it.bodyZone == zone }.maxByOrNull { it.level }
    val level = worst?.level ?: StatusLevel.NORMAL
    val color = statusColor(level)

    Box(
        modifier = Modifier
            .size(width, height)
            .clip(shape)
            .background(color.copy(alpha = 0.2f))
            .border(2.dp, color, shape)
            .clickable(enabled = worst != null) { worst?.let { onIndicatorClick(it.id) } },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = zoneLabel(zone),
            color = TextPrimary,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun IndicatorList(
    indicators: List<VitalIndicator>,
    onIndicatorClick: (String) -> Unit,
    onOpenAssessment: (String) -> Unit,
    onOpenPvtTest: () -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        indicators.forEach { indicator ->
            SpaceCard(statusLevel = indicator.level, onClick = { onIndicatorClick(indicator.id) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(text = indicator.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${indicator.currentValue} ${indicator.unit}",
                            color = TextSecondary
                        )
                    }
                    StatusBadge(level = indicator.level)
                    TextButton(onClick = { onOpenAssessment(indicator.id) }) {
                        Text(text = "Medir", color = TextPrimary)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        PrimaryActionButton(text = "Prueba de reacción (PVT)", onClick = onOpenPvtTest)
    }
}

private fun environmentLabel(env: SpaceEnvironment): String = when (env) {
    SpaceEnvironment.TRANSIT_MARS_0G -> "Tránsito a Marte 0 g"
    SpaceEnvironment.LUNAR_BASE -> "Base lunar"
    SpaceEnvironment.PRESSURIZED_ROVER -> "Rover presurizado"
}

private fun zoneLabel(zone: BodyZone): String = when (zone) {
    BodyZone.HEAD_MIND -> "Cabeza / Mente"
    BodyZone.THORAX_CARDIO -> "Tórax / Cardio"
    BodyZone.METABOLIC_ENDOCRINE -> "Metabólico / Endocrino"
}

@Preview(name = "Telefono", showBackground = true, widthDp = 400, heightDp = 860)
@Preview(name = "Tablet", showBackground = true, widthDp = 1000, heightDp = 600)
@Composable
private fun DashboardScreenPreview() {
    SoftieCareTheme {
        DashboardScreen({}, {}, {}, {})
    }
}
