package com.softiecare.app.features.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.core.model.StatusLevel
import com.softiecare.app.ui.components.SpaceCard
import com.softiecare.app.ui.components.indicatorAccent
import com.softiecare.app.ui.components.indicatorIcon
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary

// "Guides" tab: step-by-step measurement procedures and the PVT
@Composable
fun GuidesScreen(
    onOpenAssessment: (String) -> Unit,
    onOpenPvtTest: () -> Unit
) {
    val indicators by HealthRepository.indicators.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Measurement guides",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select a procedure to start the guided measurement.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        indicators.filter { it.id != "MOOD" }.forEach { indicator ->
            GuideRow(
                icon = indicatorIcon(indicator.id),
                accent = indicatorAccent(indicator.id),
                title = indicator.name,
                subtitle = "${indicator.measurementGuide.size} steps · ${indicator.unit}",
                onClick = { onOpenAssessment(indicator.id) }
            )
        }
        GuideRow(
            icon = indicatorIcon("MOOD"),
            accent = indicatorAccent("MOOD"),
            title = "Reaction test (PVT)",
            subtitle = "Alertness check · 1 attempt",
            onClick = onOpenPvtTest
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun GuideRow(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    SpaceCard(statusLevel = StatusLevel.NORMAL, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accent.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
            Text("Start", color = accent, fontWeight = FontWeight.Bold)
        }
    }
}
