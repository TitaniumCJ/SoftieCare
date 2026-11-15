package com.softiecare.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.softiecare.app.core.model.SpaceEnvironment
import com.softiecare.app.ui.theme.SpaceBackground
import com.softiecare.app.ui.theme.TextPrimary
import com.softiecare.app.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.sin
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

// Illustrated banner that makes the crew location clear: painted backdrop + animated emoji (Noto, CC BY 4.0)
@Composable
fun EnvironmentScene(env: SpaceEnvironment, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scene")
    // t loops 0..1 for twinkling stars, blinking lights and drifting dust
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )
    // Gentle floating movement for the rocket
    val bob by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "bob"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            when (env) {
                SpaceEnvironment.TRANSIT_MARS_0G -> drawDeepSpace(t)
                SpaceEnvironment.LUNAR_BASE -> drawLunarBase(t)
                SpaceEnvironment.PRESSURIZED_ROVER -> drawMarsSurface(t)
            }
        }
        when (env) {
            SpaceEnvironment.TRANSIT_MARS_0G -> {
                AnimatedEmoji(
                    codepoint = Emoji.ROCKET,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 36.dp, y = (bob - 14f).dp)
                        .size(110.dp)
                )
                AnimatedEmoji(
                    codepoint = Emoji.SPARKLES,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 10.dp, y = 14.dp)
                        .size(34.dp)
                )
            }
            SpaceEnvironment.LUNAR_BASE -> AnimatedEmoji(
                codepoint = Emoji.EARTH,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-26).dp, y = 14.dp)
                    .size(62.dp)
            )
            SpaceEnvironment.PRESSURIZED_ROVER -> AnimatedEmoji(
                codepoint = Emoji.SPARKLES,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 24.dp, y = 16.dp)
                    .size(28.dp)
            )
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

// Stars with individual twinkle phases
private fun DrawScope.drawStars(seed: Int, count: Int, maxY: Float, t: Float) {
    val rnd = Random(seed)
    repeat(count) {
        val phase = rnd.nextFloat()
        val base = 0.25f + rnd.nextFloat() * 0.5f
        val twinkle = (sin((t + phase) * 2f * PI.toFloat()) + 1f) / 2f
        val r = 0.8f + rnd.nextFloat() * 1.8f
        val c = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * maxY)
        drawCircle(Color.White.copy(alpha = (base + 0.4f * twinkle).coerceAtMost(1f)), r.dp.toPx() / 2f, c)
    }
}

// Soft radial glow used for nebulae, atmospheres and lights
private fun DrawScope.glow(color: Color, center: Offset, radius: Float) {
    drawCircle(
        Brush.radialGradient(listOf(color, Color.Transparent), center = center, radius = radius),
        radius = radius,
        center = center
    )
}

// Shaded planet: base gradient, surface patches clipped to the disc, polar cap and night-side shadow
private fun DrawScope.drawPlanet(
    center: Offset,
    r: Float,
    light: Color,
    dark: Color,
    patch: Color,
    seed: Int,
    polarCap: Boolean
) {
    glow(light.copy(alpha = 0.35f), center, r * 1.45f)
    drawCircle(
        Brush.radialGradient(listOf(light, dark), center = center - Offset(r * 0.35f, r * 0.35f), radius = r * 1.5f),
        radius = r,
        center = center
    )
    val disc = Path().apply { addOval(androidx.compose.ui.geometry.Rect(center, r)) }
    clipPath(disc) {
        val rnd = Random(seed)
        repeat(9) {
            val c = center + Offset((rnd.nextFloat() - 0.5f) * r * 1.8f, (rnd.nextFloat() - 0.5f) * r * 1.8f)
            drawOval(
                patch.copy(alpha = 0.18f + rnd.nextFloat() * 0.2f),
                topLeft = c - Offset(r * 0.3f, r * 0.12f),
                size = Size(r * (0.3f + rnd.nextFloat() * 0.5f), r * (0.12f + rnd.nextFloat() * 0.18f))
            )
        }
        if (polarCap) {
            drawOval(Color.White.copy(alpha = 0.85f), center + Offset(-r * 0.35f, -r * 1.02f), Size(r * 0.7f, r * 0.32f))
        }
        // Night side
        drawCircle(
            Brush.radialGradient(
                listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)),
                center = center - Offset(r * 0.45f, r * 0.45f),
                radius = r * 2.1f
            ),
            radius = r,
            center = center
        )
    }
}

private fun DrawScope.drawDeepSpace(t: Float) {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF040716), Color(0xFF101C44), Color(0xFF1B1240))))
    // Nebulae
    glow(Color(0xFF7C3AED).copy(alpha = 0.30f), Offset(size.width * 0.25f, size.height * 0.2f), size.height * 0.9f)
    glow(Color(0xFF0EA5E9).copy(alpha = 0.18f), Offset(size.width * 0.6f, size.height * 0.9f), size.height * 0.8f)
    drawStars(seed = 1, count = 80, maxY = size.height, t = t)
    drawPlanet(
        center = Offset(size.width * 0.80f, size.height * 0.40f),
        r = size.height * 0.26f,
        light = Color(0xFFF08A4B),
        dark = Color(0xFF8C2D0E),
        patch = Color(0xFF5B1A06),
        seed = 4,
        polarCap = true
    )
}

private fun DrawScope.drawLunarBase(t: Float) {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF02030A), Color(0xFF0D1426), Color(0xFF1A2236))))
    drawStars(seed = 2, count = 70, maxY = size.height * 0.6f, t = t)

    // Curved lunar horizon with soft shading
    val horizon = size.height * 0.62f
    val ground = Path().apply {
        moveTo(0f, horizon + 18f)
        quadraticTo(size.width * 0.5f, horizon - 22f, size.width, horizon + 12f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(ground, Brush.verticalGradient(listOf(Color(0xFFB9BEC8), Color(0xFF6B7180)), startY = horizon - 22f, endY = size.height))
    // Craters: dark bowl with a lit rim
    listOf(Triple(0.12f, 0.86f, 34f), Triple(0.62f, 0.80f, 44f), Triple(0.88f, 0.92f, 26f), Triple(0.45f, 0.95f, 20f)).forEach { (x, y, w) ->
        val c = Offset(size.width * x, size.height * y)
        drawOval(Color(0xFF555B68), c - Offset(w, w * 0.32f), Size(w * 2, w * 0.64f))
        drawOval(Color(0xFF7D8391), c - Offset(w * 0.8f, w * 0.22f), Size(w * 1.6f, w * 0.44f))
        drawArc(
            Color.White.copy(alpha = 0.35f), 200f, 140f, false,
            topLeft = c - Offset(w, w * 0.32f), size = Size(w * 2, w * 0.64f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(2.5f)
        )
    }

    // Habitat: shaded dome, connector module, glowing windows and a blinking beacon
    val bx = size.width * 0.36f
    val dome = size.height * 0.2f
    val base = horizon + 6f
    drawRoundRect(
        Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)), startY = base - dome * 0.55f, endY = base),
        topLeft = Offset(bx + dome * 0.7f, base - dome * 0.55f),
        size = Size(dome * 1.7f, dome * 0.55f),
        cornerRadius = CornerRadius(dome * 0.27f, dome * 0.27f)
    )
    drawArc(
        Brush.radialGradient(
            listOf(Color.White, Color(0xFFCBD5E1), Color(0xFF64748B)),
            center = Offset(bx - dome * 0.3f, base - dome * 0.7f),
            radius = dome * 1.6f
        ),
        startAngle = 180f, sweepAngle = 180f, useCenter = true,
        topLeft = Offset(bx - dome, base - dome),
        size = Size(dome * 2, dome * 2)
    )
    listOf(-0.45f, 0f, 0.45f).forEach { k ->
        val w = Offset(bx + dome * k, base - dome * 0.35f)
        glow(Color(0xFFFCD34D).copy(alpha = 0.6f), w, dome * 0.22f)
        drawCircle(Color(0xFFFDE68A), dome * 0.08f, w)
    }
    listOf(1.05f, 1.45f, 1.85f).forEach { k ->
        val w = Offset(bx + dome * k, base - dome * 0.28f)
        drawCircle(Color(0xFF38BDF8), dome * 0.07f, w)
    }
    drawLine(Color(0xFF94A3B8), Offset(bx, base - dome), Offset(bx, base - dome * 1.6f), 3f, StrokeCap.Round)
    val beacon = Offset(bx, base - dome * 1.6f)
    val on = sin(t * 4f * PI.toFloat()) > 0f
    if (on) glow(Color(0xFFFF5252).copy(alpha = 0.8f), beacon, 14f)
    drawCircle(if (on) Color(0xFFFF5252) else Color(0xFF7F1D1D), 4.5f, beacon)
}

private fun DrawScope.drawMarsSurface(t: Float) {
    // Dusty butterscotch sky with a small, pale sun
    drawRect(Brush.verticalGradient(listOf(Color(0xFF3B1C14), Color(0xFF9A5A3A), Color(0xFFD4A27A))))
    drawStars(seed = 3, count = 18, maxY = size.height * 0.3f, t = t)
    val sun = Offset(size.width * 0.82f, size.height * 0.24f)
    glow(Color(0xFFFFF1D6).copy(alpha = 0.7f), sun, size.height * 0.22f)
    drawCircle(Color(0xFFFFF7E8), size.height * 0.05f, sun)

    // Layered dunes: far and light to near and dark
    fun dune(y: Float, amp: Float, top: Color, bottom: Color) {
        val p = Path().apply {
            moveTo(0f, y)
            quadraticTo(size.width * 0.25f, y - amp, size.width * 0.5f, y - amp * 0.2f)
            quadraticTo(size.width * 0.75f, y + amp * 0.6f, size.width, y - amp * 0.5f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(p, Brush.verticalGradient(listOf(top, bottom), startY = y - amp, endY = size.height))
    }
    dune(size.height * 0.58f, size.height * 0.16f, Color(0xFFC67B4E), Color(0xFF9C4F2C))
    dune(size.height * 0.70f, size.height * 0.10f, Color(0xFFB0582F), Color(0xFF7E3418))
    dune(size.height * 0.84f, size.height * 0.06f, Color(0xFF8E3E1D), Color(0xFF5E2410))

    // Rover: solar panel, shaded cabin, window band, antenna dish and wheels with hubs
    val ground = size.height * 0.80f
    val rx = size.width * 0.40f
    val w = size.height * 0.70f
    val h = size.height * 0.24f
    val top = ground - h - h * 0.25f
    // Dust kicked up behind the rover
    repeat(4) { i ->
        val k = (t + i / 4f) % 1f
        drawCircle(
            Color(0xFFE0B08A).copy(alpha = 0.35f * (1f - k)),
            h * (0.18f + k * 0.35f),
            Offset(rx - w * 0.55f - k * w * 0.5f, ground - h * 0.1f - k * h * 0.4f)
        )
    }
    drawRoundRect(
        Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)), startY = top - h * 0.18f, endY = top),
        topLeft = Offset(rx - w * 0.42f, top - h * 0.18f),
        size = Size(w * 0.6f, h * 0.14f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFA8B3C2)), startY = top, endY = top + h),
        topLeft = Offset(rx - w / 2, top),
        size = Size(w, h),
        cornerRadius = CornerRadius(h * 0.4f, h * 0.4f)
    )
    drawRoundRect(
        Brush.horizontalGradient(listOf(Color(0xFF0B3A5A), Color(0xFF1D6FA3), Color(0xFF0B3A5A)), startX = rx - w * 0.1f, endX = rx + w * 0.45f),
        topLeft = Offset(rx - w * 0.05f, top + h * 0.18f),
        size = Size(w * 0.48f, h * 0.32f),
        cornerRadius = CornerRadius(h * 0.16f, h * 0.16f)
    )
    drawLine(Color.White.copy(alpha = 0.6f), Offset(rx + w * 0.02f, top + h * 0.26f), Offset(rx + w * 0.16f, top + h * 0.26f), 3f, StrokeCap.Round)
    drawLine(Color(0xFF94A3B8), Offset(rx - w * 0.36f, top), Offset(rx - w * 0.36f, top - h * 0.55f), 3f)
    drawArc(Color(0xFFE2E8F0), 200f, 140f, true, Offset(rx - w * 0.36f - h * 0.22f, top - h * 0.75f), Size(h * 0.44f, h * 0.3f))
    listOf(-0.32f, 0f, 0.32f).forEach { k ->
        val c = Offset(rx + w * k, ground - h * 0.06f)
        drawCircle(Color(0xFF1F2937), h * 0.27f, c)
        drawCircle(Color(0xFF374151), h * 0.19f, c)
        drawCircle(Color(0xFF9CA3AF), h * 0.08f, c)
    }
}
