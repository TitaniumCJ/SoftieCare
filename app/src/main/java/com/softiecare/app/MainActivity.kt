package com.softiecare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.softiecare.app.features.assessment.AssessmentScreen
import com.softiecare.app.features.assessment.PvtTestScreen
import com.softiecare.app.features.countermeasures.CountermeasureDetailScreen
import com.softiecare.app.features.dashboard.DashboardScreen
import com.softiecare.app.features.simulator.SimulatorSheet
import com.softiecare.app.ui.theme.SoftieCareTheme

private sealed interface Screen {
    object Dashboard : Screen
    data class Assessment(val indicatorId: String) : Screen
    object PvtTest : Screen
    data class CountermeasureDetail(val indicatorId: String) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoftieCareTheme {
                SoftieCareApp()
            }
        }
    }
}

@Composable
private fun SoftieCareApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var showSimulator by remember { mutableStateOf(false) }
    val goBack = { currentScreen = Screen.Dashboard }

    // El boton atras del sistema regresa al Dashboard en lugar de cerrar la app
    BackHandler(enabled = currentScreen != Screen.Dashboard) { goBack() }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                Screen.Dashboard -> DashboardScreen(
                    onIndicatorClick = { id -> currentScreen = Screen.CountermeasureDetail(id) },
                    onOpenAssessment = { id -> currentScreen = Screen.Assessment(id) },
                    onOpenPvtTest = { currentScreen = Screen.PvtTest },
                    onOpenSimulator = { showSimulator = true }
                )
                is Screen.Assessment -> AssessmentScreen(indicatorId = screen.indicatorId, onBack = goBack)
                Screen.PvtTest -> PvtTestScreen(onBack = goBack)
                is Screen.CountermeasureDetail -> CountermeasureDetailScreen(indicatorId = screen.indicatorId, onBack = goBack)
            }
        }
    }

    if (showSimulator) {
        SimulatorSheet(onDismiss = { showSimulator = false })
    }
}
