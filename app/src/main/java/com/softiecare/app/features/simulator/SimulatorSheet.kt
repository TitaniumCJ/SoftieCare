package com.softiecare.app.features.simulator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.core.model.Scenario
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusCritical
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.StatusWarning
import com.softiecare.app.ui.theme.SurfacePanel
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary

@Composable
fun SimulatorSheet(onDismiss: () -> Unit) {
    fun inject(scenario: Scenario) {
        HealthRepository.injectScenario(scenario)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfacePanel,
        title = { Text("Simulador de escenarios", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Selecciona un escenario para la demostracion", color = TextSecondary)
                ScenarioButton("Nominal (restaurar)", StatusNormal) { inject(Scenario.NOMINAL) }
                ScenarioButton("Pico de radiacion", StatusCritical) { inject(Scenario.RADIATION_SPIKE) }
                ScenarioButton("Fatiga por aislamiento", StatusWarning) { inject(Scenario.ISOLATION_FATIGUE) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun ScenarioButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = SpaceBackground)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}
