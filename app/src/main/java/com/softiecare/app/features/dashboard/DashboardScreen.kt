package com.softiecare.app.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.ui.components.PrimaryActionButton
import com.softiecare.app.ui.components.SpaceCard
import com.softiecare.app.ui.components.StatusBadge
import com.softiecare.app.ui.theme.SoftieCareTheme
import com.softiecare.app.ui.theme.TextPrimary

// Placeholder del Modulo 2: lo reemplaza el Integrante 2
@Composable
fun DashboardScreen(
    onIndicatorClick: (String) -> Unit,
    onOpenAssessment: (String) -> Unit,
    onOpenPvtTest: () -> Unit,
    onOpenSimulator: () -> Unit
) {
    val indicators by HealthRepository.indicators.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("SoftieCare - Dashboard (placeholder)", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
        }
        items(indicators, key = { it.id }) { indicator ->
            SpaceCard(statusLevel = indicator.level, onClick = { onIndicatorClick(indicator.id) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${indicator.name}: ${indicator.currentValue} ${indicator.unit}", color = TextPrimary)
                    StatusBadge(indicator.level)
                }
            }
        }
        item { PrimaryActionButton("Capturar BP_SYS") { onOpenAssessment("BP_SYS") } }
        item { PrimaryActionButton("Test PVT", onOpenPvtTest) }
        item { PrimaryActionButton("Simulador", onOpenSimulator) }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    SoftieCareTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DashboardScreen({}, {}, {}, {})
        }
    }
}
