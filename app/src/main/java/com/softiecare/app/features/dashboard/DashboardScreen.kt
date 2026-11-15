package com.softiecare.app.features.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.core.model.SpaceEnvironment
import com.softiecare.app.core.model.StatusLevel
import com.softiecare.app.core.model.VitalIndicator
import com.softiecare.app.ui.components.AppIcons
import com.softiecare.app.ui.components.AppLogo
import com.softiecare.app.ui.components.EnvironmentScene
import com.softiecare.app.ui.components.PrimaryActionButton
import com.softiecare.app.ui.components.StatusBadge
import com.softiecare.app.ui.components.displayLevel
import com.softiecare.app.ui.components.environmentLabel
import com.softiecare.app.ui.components.indicatorAccent
import com.softiecare.app.ui.components.indicatorIcon
import com.softiecare.app.ui.components.statusColor
import com.softiecare.app.ui.components.statusLabel
import com.softiecare.app.ui.theme.SoftieCareTheme
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.SurfacePanel
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary

// "Today" tab: environment, overall status and only what needs attention
@Composable
fun DashboardScreen(
    onIndicatorClick: (String) -> Unit,
    onOpenPvtTest: () -> Unit,
    onOpenSimulator: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val indicators by HealthRepository.indicators.collectAsState()
    val environment by HealthRepository.environment.collectAsState()
    val attention = indicators.filter { displayLevel(it) != StatusLevel.NORMAL }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header(onOpenSimulator, onOpenAbout)
        EnvironmentScene(environment)
        EnvironmentSelector(environment)
        OverallStatusCard(indicators)

        SectionTitle(if (attention.isEmpty()) "No alerts" else "Needs attention")
        if (attention.isEmpty()) {
            InfoCard("All indicators are within the stable range.")
        } else {
            attention.forEach { AttentionRow(it) { onIndicatorClick(it.id) } }
        }

        PrimaryActionButton(text = "Reaction test (PVT)", onClick = onOpenPvtTest)
        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun Header(onOpenSimulator: () -> Unit, onOpenAbout: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        AppLogo(size = 48.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Crew health monitoring", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "SoftieCare",
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        TextButton(onClick = onOpenSimulator) {
            Icon(AppIcons.Tune, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Simulator", color = TextPrimary)
        }
        IconButton(onClick = onOpenAbout) {
            Icon(AppIcons.Info, contentDescription = "About SoftieCare", tint = TextPrimary)
        }
    }
}

// Environment selector: visual context only (Blueprint 1.1, RF-03)
@Composable
private fun EnvironmentSelector(environment: SpaceEnvironment) {
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
                    selectedContainerColor = StatusNormal.copy(alpha = 0.18f),
                    selectedLabelColor = StatusNormal
                )
            )
        }
    }
}

// Ring with the share of stable indicators and the worst displayed level
@Composable
private fun OverallStatusCard(indicators: List<VitalIndicator>) {
    val total = indicators.size.coerceAtLeast(1)
    val stable = indicators.count { displayLevel(it) == StatusLevel.NORMAL }
    val overall = indicators.maxOfOrNull { displayLevel(it) } ?: StatusLevel.NORMAL
    val color = statusColor(overall)

    Surface(color = SurfacePanel, shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 10.dp.toPx()
                    drawArc(
                        color = TextSecondary.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(stroke)
                    )
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * stable / total,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                Text("$stable/$total", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Overall status", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = statusLabel(overall),
                    color = color,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$stable of $total indicators stable",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun AttentionRow(indicator: VitalIndicator, onClick: () -> Unit) {
    val accent = indicatorAccent(indicator.id)
    Surface(color = SurfacePanel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(indicatorIcon(indicator.id), contentDescription = null, tint = accent)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(indicator.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("View countermeasure", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            StatusBadge(displayLevel(indicator))
        }
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TextPrimary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
internal fun InfoCard(text: String) {
    Surface(color = SurfacePanel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(text, color = TextSecondary, modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "Phone", showBackground = true, backgroundColor = 0xFF0B0F19, widthDp = 400, heightDp = 860)
@Composable
private fun DashboardScreenPreview() {
    SoftieCareTheme {
        Box(Modifier.background(SpaceBackground)) {
            DashboardScreen({}, {}, {}, {})
        }
    }
}
