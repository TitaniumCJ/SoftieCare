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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.softiecare.app.features.assessment.AssessmentScreen
import com.softiecare.app.features.assessment.PvtTestScreen
import com.softiecare.app.features.countermeasures.CountermeasureDetailScreen
import com.softiecare.app.features.dashboard.DashboardScreen
import com.softiecare.app.features.simulator.SimulatorSheet
import com.softiecare.app.ui.theme.SoftieCareTheme

// Rutas como texto para que rememberSaveable las conserve al girar la pantalla
private const val DASHBOARD = "dashboard"
private const val PVT = "pvt"
private const val ASSESSMENT = "assessment:"
private const val DETAIL = "detail:"

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
    var route by rememberSaveable { mutableStateOf(DASHBOARD) }
    var showSimulator by rememberSaveable { mutableStateOf(false) }
    val goBack = { route = DASHBOARD }

    // El boton atras del sistema regresa al Dashboard en lugar de cerrar la app
    BackHandler(enabled = route != DASHBOARD) { goBack() }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                route.startsWith(ASSESSMENT) ->
                    AssessmentScreen(indicatorId = route.removePrefix(ASSESSMENT), onBack = goBack)
                route.startsWith(DETAIL) ->
                    CountermeasureDetailScreen(indicatorId = route.removePrefix(DETAIL), onBack = goBack)
                route == PVT -> PvtTestScreen(onBack = goBack)
                else -> DashboardScreen(
                    onIndicatorClick = { id -> route = DETAIL + id },
                    onOpenAssessment = { id -> route = ASSESSMENT + id },
                    onOpenPvtTest = { route = PVT },
                    onOpenSimulator = { showSimulator = true }
                )
            }
        }
    }

    if (showSimulator) {
        SimulatorSheet(onDismiss = { showSimulator = false })
    }
}
