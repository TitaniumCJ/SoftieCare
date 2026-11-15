package com.softiecare.app.features.about

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softiecare.app.ui.components.BackPill
import com.softiecare.app.ui.theme.SoftieCareTheme
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.StatusWarning
import com.softiecare.app.ui.theme.SurfacePanel
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary

// About screen: purpose, how it works, offline design, medical disclaimer and team
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackPill(accent = StatusNormal, onClick = onBack)

        Column {
            Text("SoftieCare", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(
                "Health self-assessment for astronauts on long-duration missions",
                color = TextSecondary
            )
        }

        InfoCard("What it does") {
            Paragraph(
                "Crew members track 6 indicators (blood pressure, heart rate, glucose, body temperature, " +
                    "sleep and behavioral state) and see how they relate to the 5 hazards of NASA's " +
                    "Human Research Program: space radiation, isolation, distance from Earth, " +
                    "altered gravity and a hostile, closed environment."
            )
        }

        InfoCard("How it works") {
            listOf(
                "Measure each indicator with onboard instruments, following animated step-by-step guides.",
                "Each reading is classified as Stable, Not optimal or Critical.",
                "Indicators that need attention open a guided countermeasure plan.",
                "After the wait, a reminder asks for a new measurement to confirm the change."
            ).forEachIndexed { i, text -> NumberedLine(i + 1, text) }
        }

        InfoCard("Built for deep space") {
            Paragraph(
                "Far from Earth, communication delays make real-time telemedicine impossible. " +
                    "SoftieCare works 100% offline: every assessment happens on board, with no network."
            )
        }

        // Medical disclaimer (Blueprint 1.1)
        Surface(
            color = StatusWarning.copy(alpha = 0.10f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, StatusWarning),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Demo disclaimer", color = TextPrimary, fontWeight = FontWeight.Bold)
                Paragraph(
                    "SoftieCare is a hackathon prototype. Thresholds, countermeasure plans and the " +
                        "reaction test are heuristic demo content, not verified NASA clinical guidelines. " +
                        "It does not replace the judgment of a flight surgeon. Data is simulated."
                )
            }
        }

        InfoCard("Team Softies · NASA Space Apps Challenge") {
            listOf(
                "Carlos Alberto Jaime Mascareño",
                "Emir Alexander Alcantar Martinez",
                "Elias Tamayo Salcedo",
                "Arturo Rafael Cornejo Escobar"
            ).forEach { Paragraph(it) }
        }

        Text(
            "Icons: Material Symbols (Apache 2.0)",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Surface(color = SurfacePanel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun Paragraph(text: String) {
    Text(text, color = TextSecondary, lineHeight = 22.sp)
}

@Composable
private fun NumberedLine(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(StatusNormal.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("$number", color = StatusNormal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextSecondary, lineHeight = 22.sp)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0F19, heightDp = 1200)
@Composable
private fun AboutScreenPreview() {
    SoftieCareTheme { AboutScreen(onBack = {}) }
}
