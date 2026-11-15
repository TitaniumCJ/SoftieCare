package com.softiecare.app.features.countermeasures

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softiecare.app.core.data.HealthRepository
import com.softiecare.app.core.notifications.RemeasureReminder
import com.softiecare.app.ui.components.AppIcons
import com.softiecare.app.ui.components.BackPill
import com.softiecare.app.ui.components.StatusBadge
import com.softiecare.app.ui.components.displayLevel
import com.softiecare.app.ui.components.indicatorAccent
import com.softiecare.app.ui.components.indicatorIcon
import com.softiecare.app.ui.theme.SoftieCareTheme
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusCritical
import com.softiecare.app.ui.theme.SurfacePanel
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

// Demo wait length: the real plan time is compressed to these seconds
private const val DEMO_WAIT_SECONDS = 15

// Guided countermeasure plan: checkable steps, what to expect, warning signs and a wait before re-measuring.
// The value is not corrected automatically: the indicator changes with the new measurement (Blueprint 1.1).
@Composable
fun CountermeasurePlanScreen(indicatorId: String, onBack: () -> Unit, onRemeasure: () -> Unit) {
    val indicators by HealthRepository.indicators.collectAsState()
    val indicator = indicators.find { it.id == indicatorId }
    val accent = indicatorAccent(indicatorId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BackPill(accent = accent, onClick = onBack)

        if (indicator == null) {
            Text("Indicator not found: $indicatorId", color = StatusCritical)
            return@Column
        }

        // The plan is fixed when the screen opens so it does not change while running
        val plan = remember(indicatorId) { planFor(indicator) }
        var checked by rememberSaveable(indicatorId) { mutableStateOf(List(plan.steps.size) { false }) }
        var elapsed by rememberSaveable(indicatorId) { mutableIntStateOf(0) }
        val allDone = checked.all { it }
        val waitDone = elapsed >= DEMO_WAIT_SECONDS

        val context = LocalContext.current
        val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

        LaunchedEffect(allDone) {
            // When the wait starts, ask for notification permission (Android 13+) and schedule the reminder
            if (allDone && elapsed == 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                RemeasureReminder.schedule(context, indicator.id, indicator.name, DEMO_WAIT_SECONDS * 1000L)
            }
            while (allDone && elapsed < DEMO_WAIT_SECONDS) {
                delay(1000)
                elapsed++
            }
        }

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(accent.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(indicatorIcon(indicator.id), contentDescription = null, tint = accent)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Countermeasure plan", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Text(plan.title, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            StatusBadge(displayLevel(indicator))
        }

        // Checkable steps
        SectionTitle("What to do", "${checked.count { it }} of ${plan.steps.size}")
        plan.steps.forEachIndexed { i, text ->
            StepRow(
                number = i + 1,
                text = text,
                done = checked[i],
                accent = accent,
                onToggle = { checked = checked.toMutableList().also { it[i] = !it[i] } }
            )
        }

        // What to expect
        SectionTitle("What to expect", null)
        Surface(color = SurfacePanel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                plan.expect.forEach { Bullet(it, accent) }
            }
        }

        // Warning signs
        Surface(
            color = StatusCritical.copy(alpha = 0.12f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, StatusCritical),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Notify the medical officer if there is:", color = TextPrimary, fontWeight = FontWeight.Bold)
                plan.alarms.forEach { Bullet(it, StatusCritical) }
            }
        }

        // Wait and new measurement
        WaitCard(
            allDone = allDone,
            progress = elapsed / DEMO_WAIT_SECONDS.toFloat(),
            waitMinutes = plan.waitMinutes,
            accent = accent
        )
        Button(
            onClick = onRemeasure,
            enabled = allDone && waitDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = SpaceBackground,
                disabledContainerColor = SurfacePanel,
                disabledContentColor = TextSecondary
            )
        ) {
            Icon(AppIcons.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Measure again", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTitle(text: String, trailing: String?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, color = TextSecondary)
    }
}

@Composable
private fun StepRow(number: Int, text: String, done: Boolean, accent: Color, onToggle: () -> Unit) {
    Surface(
        color = if (done) accent.copy(alpha = 0.12f) else SurfacePanel,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, if (done) accent else TextSecondary.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            // toggleable with Role.Checkbox so screen readers announce the step as a checkbox
            modifier = Modifier
                .toggleable(value = done, role = Role.Checkbox, onValueChange = { onToggle() })
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Numbered circle; turns into a check mark when done
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .then(
                        if (done) Modifier.background(accent, CircleShape)
                        else Modifier.border(2.dp, accent, CircleShape)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (done) {
                    Icon(AppIcons.CheckCircle, contentDescription = null, tint = SpaceBackground, modifier = Modifier.size(22.dp))
                } else {
                    Text("$number", color = accent, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = text,
                color = if (done) TextSecondary else TextPrimary,
                fontSize = 16.sp,
                textDecoration = if (done) TextDecoration.LineThrough else null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Bullet(text: String, color: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .padding(top = 7.dp)
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, color = TextPrimary)
    }
}

@Composable
private fun WaitCard(allDone: Boolean, progress: Float, waitMinutes: Int, accent: Color) {
    val waitText = if (waitMinutes >= 60) "${waitMinutes / 60} h" else "$waitMinutes min"
    val remainingMinutes = (waitMinutes * (1f - progress.coerceIn(0f, 1f))).toInt()
    Surface(color = SurfacePanel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawArc(TextSecondary.copy(alpha = 0.2f), -90f, 360f, false, style = Stroke(8.dp.toPx()))
                    drawArc(accent, -90f, 360f * progress.coerceIn(0f, 1f), false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                }
                Text(
                    text = if (progress >= 1f) "Done" else if (allDone) "$remainingMinutes'" else waitText,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    text = when {
                        progress >= 1f -> "Wait time complete"
                        allDone -> "Waiting before measuring again"
                        else -> "Wait $waitText before measuring again"
                    },
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (allDone) "Time accelerated for the demo. A notification will remind when to measure." else "The wait starts when all steps are done.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0F19, heightDp = 1000)
@Composable
private fun CountermeasurePlanScreenPreview() {
    SoftieCareTheme {
        CountermeasurePlanScreen(indicatorId = "TEMP", onBack = {}, onRemeasure = {})
    }
}
