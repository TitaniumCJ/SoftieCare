package com.softiecare.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.model.SpaceEnvironment
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary
import kotlin.random.Random

fun environmentLabel(env: SpaceEnvironment): String = when (env) {
    SpaceEnvironment.TRANSIT_MARS_0G -> "Ship to Mars"
    SpaceEnvironment.LUNAR_BASE -> "Lunar base"
    SpaceEnvironment.PRESSURIZED_ROVER -> "Mars rover"
}

private fun environmentTitle(env: SpaceEnvironment): String = when (env) {
    SpaceEnvironment.TRANSIT_MARS_0G -> "Spacecraft in transit to Mars"
    SpaceEnvironment.LUNAR_BASE -> "Lunar base"
    SpaceEnvironment.PRESSURIZED_ROVER -> "Pressurized rover"
}

private fun environmentSubtitle(env: SpaceEnvironment): String = when (env) {
    SpaceEnvironment.TRANSIT_MARS_0G -> "Microgravity · 0 g"
    SpaceEnvironment.LUNAR_BASE -> "Lunar surface · 0.17 g"
    SpaceEnvironment.PRESSURIZED_ROVER -> "Martian surface · 0.38 g"
}

// App background tint per environment (always dark to keep contrast)
fun environmentTint(env: SpaceEnvironment): Color = when (env) {
    SpaceEnvironment.TRANSIT_MARS_0G -> Color(0xFF0E1C3A)
    SpaceEnvironment.LUNAR_BASE -> Color(0xFF1C2029)
    SpaceEnvironment.PRESSURIZED_ROVER -> Color(0xFF2E1610)
}

fun environmentBrush(env: SpaceEnvironment): Brush =
    Brush.verticalGradient(listOf(environmentTint(env), SpaceBackground, SpaceBackground))

// Illustrated banner that makes the crew location clear
@Composable
fun EnvironmentScene(env: SpaceEnvironment, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            when (env) {
                SpaceEnvironment.TRANSIT_MARS_0G -> drawShipScene()
                SpaceEnvironment.LUNAR_BASE -> drawMoonScene()
                SpaceEnvironment.PRESSURIZED_ROVER -> drawMarsScene()
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .background(Color(0x99000000), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = environmentTitle(env),
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = environmentSubtitle(env),
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun DrawScope.drawStars(seed: Int, count: Int, maxY: Float) {
    val rnd = Random(seed)
    repeat(count) {
        drawCircle(
            color = Color.White.copy(alpha = 0.3f + rnd.nextFloat() * 0.6f),
            radius = 1f + rnd.nextFloat() * 2f,
            center = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * maxY)
        )
    }
}

private fun DrawScope.drawShipScene() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF050814), Color(0xFF13234A))))
    drawStars(seed = 1, count = 70, maxY = size.height)

    // Mars in the distance
    val mars = Offset(size.width * 0.82f, size.height * 0.30f)
    drawCircle(Color(0xFFC1440E), radius = size.height * 0.16f, center = mars)
    drawCircle(Color(0xFF8E2F08), radius = size.height * 0.05f, center = mars + Offset(-12f, 10f))
    drawCircle(Color(0xFFE07A4A), radius = size.height * 0.03f, center = mars + Offset(14f, -14f))

    // Ship: solar panels, body, window and thruster
    val cx = size.width * 0.42f
    val cy = size.height * 0.48f
    val h = size.height * 0.22f
    val bodyW = h * 2.6f
    val panel = Color(0xFF2B5BB5)
    val panelW = h * 1.4f
    drawRect(Color(0xFF9AA4B2), Offset(cx - bodyW / 2 - panelW, cy - 2f), Size(panelW * 2 + bodyW, 4f))
    drawRect(panel, Offset(cx - bodyW / 2 - panelW, cy - h * 0.9f), Size(panelW * 0.8f, h * 1.8f))
    drawRect(panel, Offset(cx + bodyW / 2 + panelW * 0.2f, cy - h * 0.9f), Size(panelW * 0.8f, h * 1.8f))
    drawPath(
        Path().apply {
            moveTo(cx - bodyW / 2 - h * 0.6f, cy - h * 0.35f)
            lineTo(cx - bodyW / 2 - h * 1.1f, cy)
            lineTo(cx - bodyW / 2 - h * 0.6f, cy + h * 0.35f)
            close()
        },
        Color(0xFFFF9F43)
    )
    drawRoundRect(
        Color(0xFFE6EAF0),
        topLeft = Offset(cx - bodyW / 2, cy - h / 2),
        size = Size(bodyW, h),
        cornerRadius = CornerRadius(h / 2, h / 2)
    )
    drawRect(Color(0xFF8A94A6), Offset(cx - bodyW / 2 - h * 0.6f, cy - h * 0.35f), Size(h * 0.6f, h * 0.7f))
    drawCircle(Color(0xFF38BDF8), radius = h * 0.22f, center = Offset(cx + bodyW / 2 - h * 0.6f, cy))
    drawCircle(Color(0xFF0B3A5A), radius = h * 0.12f, center = Offset(cx + bodyW / 2 - h * 0.6f, cy))
}

private fun DrawScope.drawMoonScene() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF02030A), Color(0xFF151A26))))
    drawStars(seed = 2, count = 60, maxY = size.height * 0.6f)

    // Earth in the lunar sky
    val earth = Offset(size.width * 0.80f, size.height * 0.26f)
    val r = size.height * 0.13f
    drawCircle(Color(0xFF2563EB), radius = r, center = earth)
    drawCircle(Color(0xFF22A06B), radius = r * 0.45f, center = earth + Offset(-r * 0.3f, -r * 0.2f))
    drawCircle(Color(0xFF22A06B), radius = r * 0.3f, center = earth + Offset(r * 0.4f, r * 0.35f))
    drawCircle(Color.White.copy(alpha = 0.5f), radius = r * 0.25f, center = earth + Offset(r * 0.2f, -r * 0.55f))

    // Lunar ground with craters
    val ground = size.height * 0.68f
    drawRect(Color(0xFF8B8F98), Offset(0f, ground), Size(size.width, size.height - ground))
    drawOval(Color(0xFF9EA2AA), Offset(-size.width * 0.1f, ground - 24f), Size(size.width * 1.2f, 60f))
    drawOval(Color(0xFF6E727B), Offset(size.width * 0.62f, ground + 30f), Size(70f, 18f))
    drawOval(Color(0xFF6E727B), Offset(size.width * 0.12f, ground + 45f), Size(50f, 14f))

    // Base: dome, module and antenna
    val bx = size.width * 0.32f
    val dome = size.height * 0.17f
    drawArc(
        Color(0xFFE6EAF0),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(bx - dome, ground - dome + 6f),
        size = Size(dome * 2, dome * 2)
    )
    drawRect(Color(0xFFCBD2DC), Offset(bx + dome * 0.8f, ground - dome * 0.55f + 6f), Size(dome * 1.4f, dome * 0.55f))
    drawCircle(Color(0xFF38BDF8), radius = dome * 0.14f, center = Offset(bx + dome * 1.2f, ground - dome * 0.3f + 6f))
    drawRect(Color(0xFF9AA4B2), Offset(bx - 2f, ground - dome * 1.7f), Size(4f, dome * 0.8f))
    drawCircle(Color(0xFFFF5252), radius = 5f, center = Offset(bx, ground - dome * 1.7f))
}

private fun DrawScope.drawMarsScene() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF3A1A12), Color(0xFFB4673E))))
    drawStars(seed = 3, count = 20, maxY = size.height * 0.35f)

    // Martian hills and ground
    val ground = size.height * 0.70f
    drawPath(
        Path().apply {
            moveTo(0f, ground)
            quadraticTo(size.width * 0.2f, ground - size.height * 0.25f, size.width * 0.45f, ground - 10f)
            quadraticTo(size.width * 0.7f, ground - size.height * 0.18f, size.width, ground - 20f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        },
        Color(0xFF8E3B1E)
    )
    drawRect(Color(0xFFA9512A), Offset(0f, ground), Size(size.width, size.height - ground))
    drawCircle(Color(0xFF6E2A12), radius = 6f, center = Offset(size.width * 0.15f, ground + 30f))
    drawCircle(Color(0xFF6E2A12), radius = 4f, center = Offset(size.width * 0.85f, ground + 40f))

    // Pressurized rover: cabin, windows and wheels
    val rx = size.width * 0.38f
    val w = size.height * 0.62f
    val h = size.height * 0.24f
    val top = ground - h - 4f
    drawRoundRect(
        Color(0xFFE6EAF0),
        topLeft = Offset(rx - w / 2, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(h * 0.35f, h * 0.35f)
    )
    drawRoundRect(
        Color(0xFF38BDF8),
        topLeft = Offset(rx + w * 0.18f, top + h * 0.18f),
        size = Size(w * 0.24f, h * 0.36f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        Color(0xFF0B3A5A),
        topLeft = Offset(rx - w * 0.3f, top + h * 0.22f),
        size = Size(w * 0.36f, h * 0.24f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRect(Color(0xFF9AA4B2), Offset(rx - w * 0.35f, top - h * 0.35f), Size(3f, h * 0.35f))
    listOf(-0.32f, 0f, 0.32f).forEach { k ->
        val c = Offset(rx + w * k, ground + 2f)
        drawCircle(Color(0xFF2B2B2B), radius = h * 0.26f, center = c)
        drawCircle(Color(0xFF8A94A6), radius = h * 0.11f, center = c)
    }
}
