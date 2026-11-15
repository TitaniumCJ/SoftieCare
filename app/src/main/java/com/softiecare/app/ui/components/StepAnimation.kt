package com.softiecare.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.softiecare.app.ui.theme.SurfacePanel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// Simple Canvas animations: one per step of each measurement guide
private enum class Scene {
    REST_SEATED, CUFF, READ_DISPLAY, BREATHE, FINGER_SENSOR, HEARTBEAT,
    FASTING_CLOCK, LANCET, DROP_STRIP, NO_FOOD, THERMO_MOUTH, THERMO_RISE,
    MOON, SUNRISE, RATING, TYPE_VALUE
}

// stepIndex == guide size is the value entry step
private fun sceneFor(id: String, step: Int): Scene = when (id) {
    "BP_SYS" -> listOf(Scene.REST_SEATED, Scene.CUFF, Scene.READ_DISPLAY)
    "HEART_RATE" -> listOf(Scene.BREATHE, Scene.FINGER_SENSOR, Scene.HEARTBEAT)
    "GLUCOSE" -> listOf(Scene.FASTING_CLOCK, Scene.LANCET, Scene.DROP_STRIP)
    "TEMP" -> listOf(Scene.NO_FOOD, Scene.THERMO_MOUTH, Scene.THERMO_RISE)
    "SLEEP" -> listOf(Scene.MOON, Scene.SUNRISE, Scene.READ_DISPLAY)
    else -> listOf(Scene.BREATHE, Scene.RATING, Scene.TYPE_VALUE)
}.getOrElse(step) { Scene.TYPE_VALUE }

private val Skin = Color(0xFFE0A87E)
private val Metal = Color(0xFFCBD2DC)
private val Screen = Color(0xFF0B3A5A)
private val Red = Color(0xFFF87171)
private val White = Color(0xFFF8FAFC)
private val Muted = Color(0xFF64748B)

@Composable
fun StepAnimation(
    indicatorId: String,
    stepIndex: Int,
    accent: Color,
    displayText: String,
    modifier: Modifier = Modifier
) {
    val scene = sceneFor(indicatorId, stepIndex)
    val transition = rememberInfiniteTransition(label = "step")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )
    val tm = rememberTextMeasurer()

    Canvas(modifier) {
        when (scene) {
            Scene.REST_SEATED -> restSeated(t, accent, tm)
            Scene.CUFF -> cuff(t, accent)
            Scene.READ_DISPLAY -> readDisplay(t, accent, tm, displayText)
            Scene.BREATHE -> breathe(t, accent, tm)
            Scene.FINGER_SENSOR -> fingerSensor(t)
            Scene.HEARTBEAT -> heartbeat(t, accent)
            Scene.FASTING_CLOCK -> clock(t, accent, tm, "8 h")
            Scene.LANCET -> lancet(t)
            Scene.DROP_STRIP -> dropStrip(t, accent)
            Scene.NO_FOOD -> noFood(t, accent, tm)
            Scene.THERMO_MOUTH -> thermoMouth(t)
            Scene.THERMO_RISE -> thermoRise(t, accent)
            Scene.MOON -> moon(t, tm)
            Scene.SUNRISE -> sunrise(t)
            Scene.RATING -> rating(t, accent, tm)
            Scene.TYPE_VALUE -> typeValue(t, accent)
        }
    }
}

// Smooth 0..1..0 for back-and-forth movement
private fun pingPong(t: Float): Float = (1f - cos(t * 2f * PI.toFloat())) / 2f

private fun DrawScope.label(tm: TextMeasurer, text: String, center: Offset, color: Color, size: Float = 16f) {
    val layout = tm.measure(text, TextStyle(color = color, fontSize = size.sp, fontWeight = FontWeight.Bold))
    drawText(layout, topLeft = center - Offset(layout.size.width / 2f, layout.size.height / 2f))
}

private fun DrawScope.timerArc(t: Float, center: Offset, r: Float, accent: Color) {
    drawCircle(Muted, r, center, style = Stroke(r * 0.18f))
    drawArc(
        accent, -90f, 360f * t, false,
        topLeft = center - Offset(r, r), size = Size(r * 2, r * 2),
        style = Stroke(r * 0.18f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.restSeated(t: Float, accent: Color, tm: TextMeasurer) {
    val u = min(size.width, size.height) / 10f
    val cx = size.width * 0.38f
    val base = size.height * 0.85f
    // Chair
    drawLine(Metal, Offset(cx - u * 2, base - u * 3), Offset(cx + u * 1.5f, base - u * 3), u * 0.4f, StrokeCap.Round)
    drawLine(Metal, Offset(cx - u * 2, base - u * 3), Offset(cx - u * 2, base - u * 7), u * 0.4f, StrokeCap.Round)
    drawLine(Metal, Offset(cx - u * 1.6f, base - u * 3), Offset(cx - u * 1.6f, base), u * 0.3f)
    drawLine(Metal, Offset(cx + u * 1.2f, base - u * 3), Offset(cx + u * 1.2f, base), u * 0.3f)
    // Seated person breathing (torso rises and falls)
    val breath = pingPong(t) * u * 0.3f
    val hip = Offset(cx - u * 1.2f, base - u * 3.5f)
    val shoulder = Offset(cx - u * 1.2f, base - u * 6.5f - breath)
    drawLine(White, hip, shoulder, u * 0.7f, StrokeCap.Round)
    drawCircle(White, u * 0.9f, shoulder - Offset(0f, u * 1.4f))
    drawLine(White, hip, Offset(cx + u * 1.6f, base - u * 3.5f), u * 0.6f, StrokeCap.Round)
    drawLine(White, Offset(cx + u * 1.6f, base - u * 3.5f), Offset(cx + u * 1.6f, base), u * 0.6f, StrokeCap.Round)
    drawLine(White, shoulder + Offset(0f, u * 0.6f), Offset(cx + u * 0.6f, base - u * 3.9f), u * 0.45f, StrokeCap.Round)
    // Rest timer
    val c = Offset(size.width * 0.75f, size.height * 0.42f)
    timerArc(t, c, u * 2f, accent)
    label(tm, "5 min", c, White, 14f)
}

private fun DrawScope.cuff(t: Float, accent: Color) {
    val u = min(size.width, size.height) / 10f
    val y = size.height * 0.45f
    // Horizontal arm
    drawRoundRect(Skin, Offset(size.width * 0.12f, y - u * 0.9f), Size(size.width * 0.62f, u * 1.8f), CornerRadius(u, u))
    // Inflating cuff
    val inflate = pingPong(t) * u * 0.5f
    drawRoundRect(
        accent, Offset(size.width * 0.22f, y - u * 1.3f - inflate),
        Size(size.width * 0.2f, u * 2.6f + inflate * 2), CornerRadius(u * 0.4f, u * 0.4f)
    )
    // Tube and inflation bulb
    val bulb = Offset(size.width * 0.32f, size.height * 0.85f)
    drawLine(Muted, Offset(size.width * 0.32f, y + u * 1.3f + inflate), bulb - Offset(0f, u), u * 0.25f)
    drawOval(Red, bulb - Offset(u * (0.9f - inflate / u * 0.3f), u), Size(u * (1.8f - inflate / u * 0.6f), u * 2f))
    // Heart at arm level
    val h = Offset(size.width * 0.86f, y)
    drawCircle(Red, u * 0.55f, h + Offset(-u * 0.4f, -u * 0.2f))
    drawCircle(Red, u * 0.55f, h + Offset(u * 0.4f, -u * 0.2f))
    drawPath(Path().apply {
        moveTo(h.x - u * 0.95f, h.y); lineTo(h.x + u * 0.95f, h.y); lineTo(h.x, h.y + u); close()
    }, Red)
    drawLine(Muted, Offset(size.width * 0.12f, y), Offset(size.width * 0.95f, y), 2f, alpha = 0.5f)
}

private fun DrawScope.readDisplay(t: Float, accent: Color, tm: TextMeasurer, text: String) {
    val w = size.width * 0.5f
    val h = size.height * 0.62f
    val tl = Offset((size.width - w) / 2, (size.height - h) / 2)
    drawRoundRect(Metal, tl, Size(w, h), CornerRadius(24f, 24f))
    drawRoundRect(Screen, tl + Offset(w * 0.1f, h * 0.12f), Size(w * 0.8f, h * 0.5f), CornerRadius(12f, 12f))
    // First "measuring", then the value blinks
    val screenCenter = tl + Offset(w / 2, h * 0.37f)
    if (t < 0.45f) {
        val dots = ".".repeat(1 + (t * 9).toInt() % 3)
        label(tm, dots, screenCenter, White, 22f)
    } else {
        val alpha = if (t > 0.6f && ((t * 10).toInt() % 2 == 0)) 0.4f else 1f
        label(tm, text, screenCenter, accent.copy(alpha = alpha), 20f)
    }
    drawCircle(accent, h * 0.09f, tl + Offset(w / 2, h * 0.8f))
}

private fun DrawScope.breathe(t: Float, accent: Color, tm: TextMeasurer) {
    val c = center
    val r = min(size.width, size.height) * (0.22f + 0.12f * pingPong(t))
    drawCircle(accent.copy(alpha = 0.15f), r * 1.3f, c)
    drawCircle(accent.copy(alpha = 0.35f), r, c)
    label(tm, if (t < 0.5f) "Inhale" else "Exhale", c, White)
}

private fun DrawScope.fingerSensor(t: Float) {
    val u = min(size.width, size.height) / 10f
    val cx = size.width / 2
    // The finger slides into the sensor in the first half
    val slide = (min(t, 0.5f) / 0.5f)
    val tipY = size.height * 0.95f - slide * size.height * 0.5f
    drawRoundRect(Skin, Offset(cx - u, tipY), Size(u * 2, size.height), CornerRadius(u, u))
    // Clip sensor
    val top = size.height * 0.25f
    drawRoundRect(Metal, Offset(cx - u * 2f, top), Size(u * 4f, u * 1.4f), CornerRadius(u * 0.6f, u * 0.6f))
    drawRoundRect(Metal, Offset(cx - u * 2f, top + u * 3.6f), Size(u * 4f, u * 1.4f), CornerRadius(u * 0.6f, u * 0.6f))
    if (t > 0.55f && ((t * 12).toInt() % 2 == 0)) drawCircle(Red, u * 0.45f, Offset(cx, top + u * 0.7f))
}

private fun DrawScope.heartbeat(t: Float, accent: Color) {
    val y = size.height * 0.55f
    val pts = listOf(0f to 0f, 0.3f to 0f, 0.36f to -0.15f, 0.42f to 0.35f, 0.48f to -0.6f, 0.54f to 0.2f, 0.6f to 0f, 1f to 0f)
    val path = Path()
    pts.forEachIndexed { i, (x, dy) ->
        val p = Offset(size.width * 0.05f + x * size.width * 0.9f, y + dy * size.height * 0.4f)
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    drawPath(path, Muted, style = Stroke(3f))
    // Dot traveling along the line
    val x = t
    val seg = pts.zipWithNext().first { x <= it.second.first }
    val k = (x - seg.first.first) / (seg.second.first - seg.first.first)
    val dy = seg.first.second + (seg.second.second - seg.first.second) * k
    val head = Offset(size.width * 0.05f + x * size.width * 0.9f, y + dy * size.height * 0.4f)
    drawCircle(accent.copy(alpha = 0.3f), 18f, head)
    drawCircle(accent, 9f, head)
}

private fun DrawScope.clock(t: Float, accent: Color, tm: TextMeasurer, text: String) {
    val r = min(size.width, size.height) * 0.32f
    val c = Offset(size.width * 0.42f, center.y)
    drawCircle(White, r, c, style = Stroke(r * 0.1f))
    rotate(360f * t, c) { drawLine(accent, c, c - Offset(0f, r * 0.75f), r * 0.08f, StrokeCap.Round) }
    rotate(30f * t, c) { drawLine(White, c, c - Offset(0f, r * 0.5f), r * 0.1f, StrokeCap.Round) }
    label(tm, text, Offset(size.width * 0.82f, center.y), accent, 22f)
}

private fun DrawScope.lancet(t: Float) {
    val u = min(size.width, size.height) / 10f
    val cx = size.width * 0.45f
    val tip = Offset(cx, size.height * 0.4f)
    drawRoundRect(Skin, Offset(cx - u, tip.y), Size(u * 2, size.height), CornerRadius(u, u))
    if (t < 0.45f) {
        // Cleaning pad moving back and forth
        val dx = sin(t / 0.45f * 4f * PI.toFloat()) * u
        drawRoundRect(White, Offset(cx - u * 0.8f + dx, tip.y - u * 0.6f), Size(u * 1.6f, u), CornerRadius(u * 0.3f, u * 0.3f))
    } else {
        val k = (t - 0.45f) / 0.55f
        val lx = cx + u * 3f - min(k * 3f, 1f) * u * 2f
        drawPath(Path().apply {
            moveTo(lx, tip.y - u * 0.3f); lineTo(lx + u * 2.5f, tip.y - u * 1f); lineTo(lx + u * 2.5f, tip.y + u * 0.4f); close()
        }, Metal)
        if (k > 0.4f) drawCircle(Red, u * 0.5f * min((k - 0.4f) / 0.4f, 1f), tip - Offset(0f, u * 0.5f))
    }
}

private fun DrawScope.dropStrip(t: Float, accent: Color) {
    val u = min(size.width, size.height) / 10f
    val meterTl = Offset(size.width * 0.55f, size.height * 0.35f)
    drawRoundRect(Metal, meterTl, Size(u * 4f, u * 5.5f), CornerRadius(u * 0.6f, u * 0.6f))
    drawRoundRect(Screen, meterTl + Offset(u * 0.5f, u * 0.6f), Size(u * 3f, u * 2.2f), CornerRadius(u * 0.3f, u * 0.3f))
    // Strip inserted in the meter
    val stripY = size.height * 0.62f
    drawRect(White, Offset(size.width * 0.18f, stripY), Size(meterTl.x - size.width * 0.18f, u * 0.8f))
    // Drop falling onto the strip
    val dropX = size.width * 0.25f
    val fall = min(t / 0.6f, 1f)
    if (fall < 1f) {
        val y = size.height * 0.1f + fall * (stripY - size.height * 0.1f - u * 0.5f)
        drawCircle(Red, u * 0.5f, Offset(dropX, y))
    } else {
        drawOval(Red, Offset(dropX - u * 0.6f, stripY - u * 0.1f), Size(u * 1.2f, u * 0.6f))
        // Check mark on the meter screen
        val s = meterTl + Offset(u * 2f, u * 1.7f)
        drawLine(accent, s + Offset(-u * 0.8f, 0f), s + Offset(-u * 0.2f, u * 0.5f), u * 0.3f, StrokeCap.Round)
        drawLine(accent, s + Offset(-u * 0.2f, u * 0.5f), s + Offset(u * 0.8f, -u * 0.6f), u * 0.3f, StrokeCap.Round)
    }
}

private fun DrawScope.noFood(t: Float, accent: Color, tm: TextMeasurer) {
    val u = min(size.width, size.height) / 10f
    val c = Offset(size.width * 0.32f, center.y)
    // Glass with a prohibition line
    drawPath(Path().apply {
        moveTo(c.x - u * 1.5f, c.y - u * 2f); lineTo(c.x + u * 1.5f, c.y - u * 2f)
        lineTo(c.x + u * 1.1f, c.y + u * 2f); lineTo(c.x - u * 1.1f, c.y + u * 2f); close()
    }, White)
    drawCircle(Red, u * 3f, c, style = Stroke(u * 0.4f))
    drawLine(Red, c + Offset(-u * 2.1f, -u * 2.1f), c + Offset(u * 2.1f, u * 2.1f), u * 0.4f)
    val tc = Offset(size.width * 0.74f, center.y)
    timerArc(t, tc, u * 2f, accent)
    label(tm, "15 min", tc, White, 13f)
}

private fun DrawScope.thermoMouth(t: Float) {
    val u = min(size.width, size.height) / 10f
    val face = Offset(size.width * 0.35f, center.y)
    drawCircle(Skin, u * 3.5f, face)
    drawCircle(Screen, u * 0.35f, face + Offset(-u * 1.2f, -u * 1f))
    drawCircle(Screen, u * 0.35f, face + Offset(u * 1.2f, -u * 1f))
    drawOval(Color(0xFF7F1D1D), face + Offset(-u * 1f, u * 0.9f), Size(u * 2f, u * 1.1f))
    // Thermometer going into the mouth
    val k = pingPong(t)
    val start = Offset(size.width * 0.95f - k * u * 2.5f, face.y + u * 1.45f)
    drawLine(White, start, start - Offset(u * 4.5f, 0f), u * 0.5f, StrokeCap.Round)
    drawCircle(Red, u * 0.45f, start - Offset(u * 4.5f, 0f))
}

private fun DrawScope.thermoRise(t: Float, accent: Color) {
    val u = min(size.width, size.height) / 10f
    val cx = size.width * 0.4f
    val top = size.height * 0.1f
    val bottom = size.height * 0.8f
    drawRoundRect(White, Offset(cx - u * 0.6f, top), Size(u * 1.2f, bottom - top), CornerRadius(u * 0.6f, u * 0.6f))
    drawCircle(White, u * 1.1f, Offset(cx, bottom))
    drawCircle(Red, u * 0.8f, Offset(cx, bottom))
    val level = min(t / 0.7f, 1f)
    val y = bottom - level * (bottom - top - u)
    drawRect(Red, Offset(cx - u * 0.3f, y), Size(u * 0.6f, bottom - y))
    // "Beep" waves at the end
    if (t > 0.7f) {
        val r = (t - 0.7f) / 0.3f
        drawCircle(accent.copy(alpha = 1f - r), u * (1.5f + r * 2.5f), Offset(size.width * 0.72f, size.height * 0.3f), style = Stroke(4f))
        drawCircle(accent, u * 0.6f, Offset(size.width * 0.72f, size.height * 0.3f))
    }
}

private fun DrawScope.moon(t: Float, tm: TextMeasurer) {
    val u = min(size.width, size.height) / 10f
    val c = Offset(size.width * 0.35f, center.y)
    drawCircle(Color(0xFFFDE68A), u * 3f, c)
    drawCircle(SurfacePanel, u * 2.6f, c + Offset(u * 1.4f, -u * 0.8f))
    // Z letters rising and fading
    for (i in 0..2) {
        val k = (t + i / 3f) % 1f
        label(tm, "z", Offset(size.width * (0.6f + i * 0.1f), size.height * (0.75f - k * 0.55f)), White.copy(alpha = 1f - k), 16f + i * 4f)
    }
}

private fun DrawScope.sunrise(t: Float) {
    val horizon = size.height * 0.72f
    val r = min(size.width, size.height) * 0.2f
    val rise = pingPong(t)
    val c = Offset(center.x, horizon + r * 0.6f - rise * r * 1.6f)
    for (i in 0 until 8) {
        val a = i * PI.toFloat() / 4f
        val dir = Offset(cos(a), sin(a))
        drawLine(Color(0xFFFBBF24), c + dir * (r * 1.25f), c + dir * (r * 1.6f), 6f, StrokeCap.Round)
    }
    drawCircle(Color(0xFFFBBF24), r, c)
    drawRect(SurfacePanel, Offset(0f, horizon), Size(size.width, size.height - horizon))
    drawLine(Muted, Offset(0f, horizon), Offset(size.width, horizon), 4f)
}

private fun DrawScope.rating(t: Float, accent: Color, tm: TextMeasurer) {
    val n = 5
    val gap = size.width / (n + 1)
    val sel = min((t / 0.7f * n).toInt(), n - 1)
    for (i in 0 until n) {
        val c = Offset(gap * (i + 1), center.y)
        val on = i == sel
        drawCircle(if (on) accent else Muted, if (on) gap * 0.36f else gap * 0.28f, c)
        label(tm, "${i + 1}", c, if (on) SurfacePanel else White, if (on) 20f else 16f)
    }
}

private fun DrawScope.typeValue(t: Float, accent: Color) {
    val u = min(size.width, size.height) / 10f
    val tl = Offset(size.width * 0.2f, size.height * 0.3f)
    val w = size.width * 0.6f
    drawRoundRect(Muted, tl, Size(w, u * 2.2f), CornerRadius(u * 0.5f, u * 0.5f), style = Stroke(4f))
    // Digits appear one by one, then the check mark
    val digits = min((t / 0.6f * 4).toInt(), 4)
    for (i in 0 until digits) drawCircle(White, u * 0.3f, tl + Offset(u * 1.2f + i * u * 1.1f, u * 1.1f))
    if (t > 0.65f) {
        val c = Offset(center.x, size.height * 0.78f)
        drawCircle(accent, u * 1.2f, c)
        drawLine(SurfacePanel, c + Offset(-u * 0.55f, 0f), c + Offset(-u * 0.1f, u * 0.45f), u * 0.25f, StrokeCap.Round)
        drawLine(SurfacePanel, c + Offset(-u * 0.1f, u * 0.45f), c + Offset(u * 0.6f, -u * 0.4f), u * 0.25f, StrokeCap.Round)
    }
}
