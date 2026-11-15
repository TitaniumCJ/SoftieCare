package com.softiecare.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.softiecare.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.model.SpaceHazard
import com.softiecare.app.core.model.StatusLevel
import com.softiecare.app.core.model.VitalIndicator
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.StatusCritical
import com.softiecare.app.ui.theme.StatusCriticalStrong
import com.softiecare.app.ui.theme.StatusNormal
import com.softiecare.app.ui.theme.StatusNormalStrong
import com.softiecare.app.ui.theme.StatusWarning
import com.softiecare.app.ui.theme.StatusWarningStrong
import com.softiecare.app.ui.theme.SurfacePanel
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary
import java.util.Locale

// Bright color for lines and charts on a dark background
fun statusColor(level: StatusLevel): Color = when (level) {
    StatusLevel.NORMAL -> StatusNormal
    StatusLevel.WARNING -> StatusWarning
    StatusLevel.CRITICAL -> StatusCritical
}

// Badge background used with white text
fun statusContainerColor(level: StatusLevel): Color = when (level) {
    StatusLevel.NORMAL -> StatusNormalStrong
    StatusLevel.WARNING -> StatusWarningStrong
    StatusLevel.CRITICAL -> StatusCriticalStrong
}

fun statusLabel(level: StatusLevel): String = when (level) {
    StatusLevel.NORMAL -> "STABLE"
    StatusLevel.WARNING -> "NOT OPTIMAL"
    StatusLevel.CRITICAL -> "CRITICAL"
}

// Level shown on screen (Blueprint 1.1): MOOD is never presented as critical
fun displayLevel(indicator: VitalIndicator): StatusLevel =
    if (indicator.id == "MOOD" && indicator.level == StatusLevel.CRITICAL) StatusLevel.WARNING
    else indicator.level

private val PanelBorder = BorderStroke(1.dp, TextSecondary.copy(alpha = 0.25f))

private fun formatValue(v: Float): String = String.format(Locale.US, "%.1f", v)

// Neutral border: status is communicated only by StatusBadge (Blueprint 6)
@Composable
fun SpaceCard(
    statusLevel: StatusLevel,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfacePanel,
        shape = RoundedCornerShape(16.dp),
        border = PanelBorder
    ) {
        // clickable inside the Surface so the ripple respects the corners
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun StatusBadge(level: StatusLevel) {
    Surface(
        color = statusContainerColor(level),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = statusLabel(level),
            color = TextPrimary,
            maxLines = 1,
            softWrap = false,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// Readable HRP hazard name (the enum name is never shown)
fun hazardLabel(hazard: SpaceHazard): String = when (hazard) {
    SpaceHazard.RADIATION -> "Space radiation"
    SpaceHazard.ISOLATION -> "Isolation"
    SpaceHazard.DISTANCE -> "Distance from Earth"
    SpaceHazard.GRAVITY -> "Altered gravity"
    SpaceHazard.HOSTILE_ENVIRONMENT -> "Hostile, closed environment"
}

@Composable
fun HazardTag(hazard: SpaceHazard) {
    Surface(
        color = SpaceBackground,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, TextSecondary)
    ) {
        Text(
            text = hazardLabel(hazard),
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PrimaryActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = StatusNormal,
            contentColor = SpaceBackground
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}

// Icon and accent color per indicator (decorative only; status comes from StatusBadge)
fun indicatorIcon(id: String): ImageVector = when (id) {
    "BP_SYS" -> AppIcons.MonitorHeart
    "HEART_RATE" -> AppIcons.Heart
    "GLUCOSE" -> AppIcons.Drop
    "TEMP" -> AppIcons.Thermostat
    "SLEEP" -> AppIcons.Bedtime
    else -> AppIcons.Psychology
}

fun indicatorAccent(id: String): Color = when (id) {
    "BP_SYS" -> Color(0xFF60A5FA)
    "HEART_RATE" -> Color(0xFFF87171)
    "GLUCOSE" -> Color(0xFFA78BFA)
    "TEMP" -> Color(0xFFFBBF24)
    "SLEEP" -> Color(0xFF818CF8)
    else -> Color(0xFF2DD4BF)
}

// Small trend chart without axes
@Composable
fun Sparkline(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val minV = values.min()
        val span = (values.max() - minV).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (values.size - 1)
        val path = Path().apply {
            values.forEachIndexed { i, v ->
                val x = i * stepX
                val y = size.height - (v - minV) / span * size.height
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

// Large indicator tile: icon, name, big value, trend and status badge
@Composable
fun IndicatorTile(
    indicator: VitalIndicator,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMeasure: (() -> Unit)? = null
) {
    val accent = indicatorAccent(indicator.id)
    Surface(
        modifier = modifier,
        color = SurfacePanel,
        shape = RoundedCornerShape(20.dp),
        border = PanelBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(accent.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = indicatorIcon(indicator.id),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = indicator.name,
                    color = TextSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            if (indicator.id == "MOOD") {
                // Blueprint 1.1: the mood numeric value is not highlighted
                Text(
                    text = "Reaction test",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Open for details",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatValue(indicator.currentValue),
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = indicator.unit,
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Sparkline(
                    values = indicator.history7Days,
                    color = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            StatusBadge(displayLevel(indicator))
            // "Measure" on its own full-width row so the text never wraps
            if (onMeasure != null) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onMeasure,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent.copy(alpha = 0.18f),
                        contentColor = TextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(AppIcons.Add, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Measure", maxLines = 1, softWrap = false, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Visible back button: pill with arrow and border in the context color
@Composable
fun BackPill(accent: Color, text: String = "Back", onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = accent.copy(alpha = 0.22f), contentColor = TextPrimary),
        border = BorderStroke(1.5.dp, accent),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Icon(AppIcons.Back, contentDescription = null, tint = accent)
        Spacer(Modifier.width(8.dp))
        Text(text, fontWeight = FontWeight.Bold)
    }
}

// SoftieCare logo: the launcher icon foreground (orbit and heart) cropped to a circle
@Composable
fun AppLogo(size: Dp = 48.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(SpaceBackground)
            .border(1.dp, TextSecondary.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // The 108dp foreground keeps its art inside the central ~62dp, so it is drawn larger and clipped
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = "SoftieCare logo",
            modifier = Modifier.requiredSize(size * 1.6f)
        )
    }
}
