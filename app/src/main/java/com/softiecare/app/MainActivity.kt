package com.softiecare.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.core.notifications.RemeasureReminder
import com.softiecare.app.features.about.AboutScreen
import com.softiecare.app.features.assessment.AssessmentScreen
import com.softiecare.app.features.assessment.PvtTestScreen
import com.softiecare.app.features.countermeasures.CountermeasureDetailScreen
import com.softiecare.app.features.countermeasures.CountermeasurePlanScreen
import com.softiecare.app.features.dashboard.DashboardScreen
import com.softiecare.app.features.dashboard.GuidesScreen
import com.softiecare.app.features.dashboard.HistoryScreen
import com.softiecare.app.features.dashboard.VitalsScreen
import com.softiecare.app.features.simulator.SimulatorSheet
import com.softiecare.app.ui.components.AppIcons
import com.softiecare.app.ui.components.environmentBrush
import com.softiecare.app.ui.theme.SoftieCareTheme
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.SurfacePanel
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary

// Routes as strings so rememberSaveable keeps them across screen rotation
private const val DASHBOARD = "dashboard"
private const val VITALS = "vitals"
private const val HISTORY = "history"
private const val GUIDES = "guides"
private const val PVT = "pvt"
private const val ASSESSMENT = "assessment:"
private const val DETAIL = "detail:"
private const val PLAN = "plan:"
private const val ABOUT = "about"

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(DASHBOARD, "Today", AppIcons.Home),
    Tab(VITALS, "Vitals", AppIcons.Vitals),
    Tab(HISTORY, "History", AppIcons.Chart),
    Tab(GUIDES, "Guides", AppIcons.Guide)
)

class MainActivity : ComponentActivity() {
    // Indicator to re-measure when the app is opened from a reminder notification
    private val remeasureRequest = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) {
            remeasureRequest.value = intent.getStringExtra(RemeasureReminder.EXTRA_INDICATOR_ID)
        }
        setContent {
            SoftieCareTheme {
                SoftieCareApp(
                    remeasureRequest = remeasureRequest.value,
                    onRemeasureHandled = { remeasureRequest.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(RemeasureReminder.EXTRA_INDICATOR_ID)?.let { remeasureRequest.value = it }
    }
}

@Composable
private fun SoftieCareApp(remeasureRequest: String?, onRemeasureHandled: () -> Unit) {
    var route by rememberSaveable { mutableStateOf(DASHBOARD) }
    // Tab to return to when leaving a detail screen
    var lastTab by rememberSaveable { mutableStateOf(DASHBOARD) }
    var showSimulator by rememberSaveable { mutableStateOf(false) }
    val environment by HealthRepository.environment.collectAsState()

    LaunchedEffect(remeasureRequest) {
        if (remeasureRequest != null) {
            lastTab = DASHBOARD
            route = ASSESSMENT + remeasureRequest
            onRemeasureHandled()
        }
    }

    val isTab = tabs.any { it.route == route }
    val goBack = { route = lastTab }
    val openTab = { tab: String -> lastTab = tab; route = tab }

    // Back: from a detail screen to its tab, and from another tab to "Today"
    BackHandler(enabled = route != DASHBOARD) {
        if (isTab) openTab(DASHBOARD) else goBack()
    }

    // The background tint changes with the operating environment
    Box(
        Modifier
            .fillMaxSize()
            .background(environmentBrush(environment))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (isTab) {
                    NavigationBar(containerColor = SurfacePanel) {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                selected = route == tab.route,
                                onClick = { openTab(tab.route) },
                                icon = { Icon(tab.icon, contentDescription = null) },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SpaceBackground,
                                    selectedTextColor = TextPrimary,
                                    indicatorColor = StatusNormal,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (route == DASHBOARD) {
                    ExtendedFloatingActionButton(
                        onClick = { openTab(GUIDES) },
                        icon = { Icon(AppIcons.Add, contentDescription = null) },
                        text = { Text("Measure") },
                        containerColor = StatusNormal,
                        contentColor = SpaceBackground
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val openDetail = { id: String -> route = DETAIL + id }
                val openAssessment = { id: String -> route = ASSESSMENT + id }
                val openPvt = { route = PVT }
                when {
                    route.startsWith(ASSESSMENT) ->
                        AssessmentScreen(indicatorId = route.removePrefix(ASSESSMENT), onBack = goBack)
                    route.startsWith(DETAIL) -> {
                        val id = route.removePrefix(DETAIL)
                        CountermeasureDetailScreen(
                            indicatorId = id,
                            onBack = goBack,
                            onStartCountermeasure = { route = PLAN + id }
                        )
                    }
                    // The plan ends with a new measurement of the same indicator
                    route.startsWith(PLAN) -> {
                        val id = route.removePrefix(PLAN)
                        CountermeasurePlanScreen(
                            indicatorId = id,
                            onBack = { route = DETAIL + id },
                            onRemeasure = { route = ASSESSMENT + id }
                        )
                    }
                    route == PVT -> PvtTestScreen(onBack = goBack)
                    route == ABOUT -> AboutScreen(onBack = goBack)
                    route == VITALS -> VitalsScreen(onIndicatorClick = openDetail, onOpenAssessment = openAssessment)
                    route == HISTORY -> HistoryScreen(onIndicatorClick = openDetail)
                    route == GUIDES -> GuidesScreen(onOpenAssessment = openAssessment, onOpenPvtTest = openPvt)
                    else -> DashboardScreen(
                        onIndicatorClick = openDetail,
                        onOpenPvtTest = openPvt,
                        onOpenSimulator = { showSimulator = true },
                        onOpenAbout = { route = ABOUT }
                    )
                }
            }
        }
    }

    if (showSimulator) {
        SimulatorSheet(onDismiss = { showSimulator = false })
    }
}
