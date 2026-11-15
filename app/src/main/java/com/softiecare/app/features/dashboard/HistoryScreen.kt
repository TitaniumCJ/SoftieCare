package com.softiecare.app.features.dashboard

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.features.countermeasures.TrendLineChart
import com.softiecare.app.ui.components.SpaceCard
import com.softiecare.app.ui.components.StatusBadge
import com.softiecare.app.ui.components.displayLevel
import com.softiecare.app.ui.components.indicatorAccent
import com.softiecare.app.ui.components.indicatorIcon
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary

// "History" tab: last 7 readings per indicator (MOOD is omitted, Blueprint 1.1)
@Composable
fun HistoryScreen(onIndicatorClick: (String) -> Unit) {
    val indicators by HealthRepository.indicators.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "History",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Last 7 readings. The dashed line marks the nominal value.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        indicators.filter { it.id != "MOOD" }.forEach { indicator ->
            SpaceCard(statusLevel = indicator.level, onClick = { onIndicatorClick(indicator.id) }) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = indicatorIcon(indicator.id),
                            contentDescription = null,
                            tint = indicatorAccent(indicator.id),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = indicator.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        StatusBadge(displayLevel(indicator))
                    }
                    Spacer(Modifier.height(8.dp))
                    TrendLineChart(
                        indicatorId = indicator.id,
                        values = indicator.history7Days,
                        normalValue = indicator.normalValue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
