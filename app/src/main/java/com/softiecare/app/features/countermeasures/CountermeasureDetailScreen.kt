package com.softiecare.app.features.countermeasures

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.core.model.BodyZone
import com.softiecare.app.core.model.SpaceHazard
import com.softiecare.app.core.model.StatusLevel
import com.softiecare.app.core.model.VitalIndicator
import com.softiecare.app.ui.components.HazardTag
import com.softiecare.app.ui.components.PrimaryActionButton
import com.softiecare.app.ui.components.SpaceCard
import com.softiecare.app.ui.components.StatusBadge
import com.softiecare.app.ui.components.statusColor
import com.softiecare.app.ui.theme.SoftieCareTheme
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusCritical
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun CountermeasureDetailScreen(indicatorId: String, onBack: () -> Unit) {
    val indicators by HealthRepository.indicators.collectAsState()
    val indicator = indicators.find { it.id == indicatorId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Volver al panel", color = TextSecondary)
        }

        if (indicator == null) {
            Text("Indicador no encontrado: $indicatorId", color = StatusCritical)
            return@Column
        }

        HeaderSection(indicator)
        HazardSection(indicator)
        TrendSection(indicator)
        CountermeasureSection(indicator)
    }
}

@Composable
private fun HeaderSection(indicator: VitalIndicator) {
    SpaceCard(statusLevel = indicator.level, onClick = null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = indicator.name,
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(level = indicator.level)
            }
            Text(
                text = "${formatValue(indicator.currentValue)} ${indicator.unit}",
                color = statusColor(indicator.level),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Valor nominal: ${formatValue(indicator.normalValue)} ${indicator.unit}",
                color = TextSecondary
            )
            Text(
                text = "Zona corporal: ${zoneLabel(indicator.bodyZone)}",
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun HazardSection(indicator: VitalIndicator) {
    SpaceCard(statusLevel = indicator.level, onClick = null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Peligro HRP asociado",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HazardTag(hazard = indicator.primaryHazard)
            Text(text = hazardDescription(indicator.primaryHazard), color = TextSecondary)
        }
    }
}

@Composable
private fun TrendSection(indicator: VitalIndicator) {
    val values = indicator.history7Days
    SpaceCard(statusLevel = indicator.level, onClick = null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Tendencia — últimas ${values.size} lecturas",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TrendLineChart(
                indicatorId = indicator.id,
                values = values,
                normalValue = indicator.normalValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            if (values.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Más antigua", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = "Mín ${formatValue(values.min())} · Máx ${formatValue(values.max())} ${indicator.unit}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text("Más reciente", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(
                text = "Línea punteada: valor nominal",
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CountermeasureSection(indicator: VitalIndicator) {
    SpaceCard(statusLevel = indicator.level, onClick = null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Protocolo de contramedida",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = indicator.countermeasureProtocol, color = TextPrimary)

            // Se oculta en NORMAL para no insertar lecturas nominales repetidas en history7Days
            if (indicator.level != StatusLevel.NORMAL) {
                PrimaryActionButton(
                    text = "Confirmar Contramedida Ejecutada",
                    onClick = { HealthRepository.applyCountermeasure(indicator.id) }
                )
            } else {
                Text(text = "Indicador en rango nominal", color = StatusNormal, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TrendLineChart(
    indicatorId: String,
    values: List<Float>,
    normalValue: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas

        val minV = minOf(values.min(), normalValue)
        val maxV = maxOf(values.max(), normalValue)
        val span = if (maxV - minV > 0f) maxV - minV else 1f
        val padX = 10.dp.toPx()
        val padY = 12.dp.toPx()
        val chartW = size.width - 2 * padX
        val chartH = size.height - 2 * padY

        fun xOf(i: Int): Float =
            if (values.size == 1) size.width / 2f else padX + chartW * i / (values.size - 1)

        fun yOf(v: Float): Float = padY + (1f - (v - minV) / span) * chartH

        val normalY = yOf(normalValue)
        drawLine(
            color = TextSecondary,
            start = Offset(padX, normalY),
            end = Offset(size.width - padX, normalY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
        )

        val path = Path().apply {
            values.forEachIndexed { i, v ->
                if (i == 0) moveTo(xOf(i), yOf(v)) else lineTo(xOf(i), yOf(v))
            }
        }
        val lastLevel = HealthRepository.evaluateLevel(indicatorId, values.last())
        drawPath(
            path = path,
            color = statusColor(lastLevel),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Cada punto se colorea con su propio nivel para ver en que lectura inicio la alerta
        values.forEachIndexed { i, v ->
            drawCircle(
                color = statusColor(HealthRepository.evaluateLevel(indicatorId, v)),
                radius = 5.dp.toPx(),
                center = Offset(xOf(i), yOf(v))
            )
        }
    }
}

private fun formatValue(v: Float): String = String.format(Locale.US, "%.1f", v)

private fun zoneLabel(zone: BodyZone): String = when (zone) {
    BodyZone.HEAD_MIND -> "Cabeza / Mente"
    BodyZone.THORAX_CARDIO -> "Tórax / Cardiovascular"
    BodyZone.METABOLIC_ENDOCRINE -> "Metabólico / Endocrino"
}

private fun hazardDescription(hazard: SpaceHazard): String = when (hazard) {
    SpaceHazard.RADIATION ->
        "La radiación espacial puede alterar el sistema inmune y provocar cambios en la temperatura corporal."
    SpaceHazard.ISOLATION ->
        "El aislamiento y confinamiento prolongados afectan la calidad del sueño, el estado de ánimo y el desempeño conductual."
    SpaceHazard.DISTANCE ->
        "La distancia a la Tierra impide la telemedicina en tiempo real, por lo que la tripulación debe actuar de forma autónoma."
    SpaceHazard.GRAVITY ->
        "Los cambios de gravedad redistribuyen los fluidos corporales y alteran la regulación cardiovascular."
    SpaceHazard.HOSTILE_ENVIRONMENT ->
        "El hábitat cerrado altera la alimentación y el metabolismo, con impacto en la regulación de glucosa."
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0F19, heightDp = 900)
@Composable
private fun CountermeasureDetailScreenPreview() {
    SoftieCareTheme {
        CountermeasureDetailScreen(indicatorId = "TEMP", onBack = {})
    }
}
