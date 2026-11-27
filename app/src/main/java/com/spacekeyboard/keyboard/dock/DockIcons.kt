package com.spacekeyboard.keyboard.dock

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private class Frame(val x: Float, val y: Float, val w: Float, val h: Float)

private val HomeFrame = Frame(4f, 4f, 120f, 112f)
private val CustomizeFrame = Frame(4f, 2f, 120f, 112f)
private val SettingsFrame = Frame(4f, 14f, 120f, 112f)
private val TryFrame = Frame(6f, 8f, 116f, 116f)
private const val HOUSE = "M64 8C68 8 72 10 76 13L110 41Q116 46 116 54V90A18 18 0 0 1 98 108H30A18 18 0 0 1 12 90V54Q12 46 18 41L52 13C56 10 60 8 64 8Z"
private const val SPARKLE = "M64 20L66.6 26.4L73 29L66.6 31.6L64 38L61.4 31.6L55 29L61.4 26.4Z"
private const val DROP = "M104 8C104 8 92 21 92 29A12 12 0 0 0 116 29C116 21 104 8 104 8Z"
private const val DROP_SHINE = "M98 29A6 6 0 0 0 102 34.5"
private const val GEAR_CORE = "M101 69A11 11 0 1 1 79 69A11 11 0 1 1 101 69Z"

private val BodyBase = Color(0xFF232846)
private val KeyBottom = Color(0xFFD9D2FF)
private val KeyLip = Color(0xFF5B4BD0)
private val Violet = Color(0xFFB9A8FF)
private val KeyTints = listOf(
    Color(0xFFFF7AC8) to Color(0xFFFF4FA0),
    Color(0xFFFF9A5C) to Color(0xFFFF7A2E),
    Color(0xFFFFD25A) to Color(0xFFFFB020),
    Color(0xFF6EE7A0) to Color(0xFF2FCF7A),
    Color(0xFF5EC8FF) to Color(0xFF2F9BFF),
)

private fun rr(x: Float, y: Float, w: Float, h: Float, r: Float): String =
    "M${x + r} ${y}H${x + w - r}A$r $r 0 0 1 ${x + w} ${y + r}V${y + h - r}A$r $r 0 0 1 ${x + w - r} ${y + h}" +
        "H${x + r}A$r $r 0 0 1 $x ${y + h - r}V${y + r}A$r $r 0 0 1 ${x + r} ${y}Z"

private fun gearPath(cx: Float, cy: Float, outer: Float, inner: Float, teeth: Int): String {
    val step = (2 * PI / teeth).toFloat()
    val offsets = floatArrayOf(-0.30f, -0.20f, 0.20f, 0.30f)
    val sb = StringBuilder()
    for (k in 0 until teeth) {
        for (j in offsets.indices) {
            val angle = step * k + offsets[j] * step
            val radius = if (j == 1 || j == 2) outer else inner
            sb.append(if (k == 0 && j == 0) "M" else "L")
            sb.append(cx + radius * cos(angle)).append(' ').append(cy + radius * sin(angle))
        }
    }
    return sb.append('Z').toString()
}

private fun vertical(y0: Float, y1: Float, colors: List<Color>) =
    Brush.linearGradient(colors, Offset(0f, y0), Offset(0f, y1))

private fun diagonal(x0: Float, y0: Float, x1: Float, y1: Float, colors: List<Color>) =
    Brush.linearGradient(colors, Offset(x0, y0), Offset(x1, y1))

private fun ImageVector.Builder.fillPath(d: String, brush: Brush, alpha: Float = 1f) {
    addPath(PathParser().parsePathString(d).toNodes(), fill = brush, fillAlpha = alpha)
}

private fun ImageVector.Builder.fillPath(d: String, color: Color, alpha: Float = 1f) =
    fillPath(d, SolidColor(color), alpha)

private fun ImageVector.Builder.strokePath(d: String, brush: Brush, width: Float, alpha: Float = 1f) {
    addPath(
        PathParser().parsePathString(d).toNodes(),
        stroke = brush,
        strokeAlpha = alpha,
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
}

private fun ImageVector.Builder.glassBody(d: String, top: Float, bottom: Float, shadow: Float = 5f) {
    group(translationY = shadow) { fillPath(d, Color.Black, 0.22f) }
    fillPath(d, BodyBase, 0.92f)
    fillPath(d, vertical(top, bottom, listOf(Color.White.copy(0.30f), Color.White.copy(0.08f))))
    val rim = diagonal(10f, top, 118f, bottom, listOf(Color.White.copy(0.95f), Color.White.copy(0.25f), Violet.copy(0.6f)))
    strokePath(d, rim, 2.4f)
}

private fun ImageVector.Builder.key(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    top: Color = Color.White,
    bottom: Color = KeyBottom,
    lip: Color = KeyLip,
    radius: Float = 3.5f,
) {
    fillPath(rr(x, y + 2.4f, w, h, radius), lip, 0.55f)
    fillPath(rr(x, y, w, h, radius), vertical(y, y + h, listOf(top, bottom)))
}

private fun ImageVector.Builder.keyRow(count: Int, x: Float, y: Float, pitch: Float, w: Float, h: Float) {
    for (i in 0 until count) key(x + i * pitch, y, w, h)
}

private fun ImageVector.Builder.keyboardKeys() {
    keyRow(5, 20f, 46f, 18.5f, 14.5f, 12f)
    keyRow(4, 29f, 63f, 18.5f, 14.5f, 12f)
    key(34f, 80f, 60f, 12f)
}

private fun newBuilder(name: String, frame: Frame, height: Dp) =
    ImageVector.Builder(name, height * (frame.w / frame.h), height, frame.w, frame.h)

private fun ImageVector.Builder.framed(frame: Frame, block: ImageVector.Builder.() -> Unit): ImageVector.Builder {
    group(translationX = -frame.x, translationY = -frame.y, block = block)
    return this
}

private fun homeIcon(): ImageVector {
    val b = newBuilder("dock_home", HomeFrame, DockMetrics.IconHeight)
    b.framed(HomeFrame) {
        glassBody(HOUSE, 8f, 108f)
        keyRow(3, 35f, 50f, 21f, 15f, 12f)
        keyRow(4, 24f, 67f, 21f, 15f, 12f)
        key(34f, 84f, 60f, 12f)
        fillPath(SPARKLE, vertical(20f, 38f, listOf(Color(0xFFFFE08A), Color(0xFFFFB23F))))
    }
    return b.build()
}

private fun ImageVector.Builder.paintedKeys() {
    for (i in 0 until 5) {
        val (top, bottom) = KeyTints[i]
        key(20f + i * 18.5f, 46f, 14.5f, 12f, top, bottom, bottom)
    }
    for (i in 0 until 4) {
        val (top, bottom) = KeyTints[(i + 1) % 5]
        key(29f + i * 18.5f, 63f, 14.5f, 12f, top, bottom, bottom)
    }
    key(34f, 80f, 60f, 12f, Color(0xFFC4B5FF), Color(0xFF6A5BFF), Color(0xFF4A3FD0))
}

private fun ImageVector.Builder.paintDrop() {
    val pink = vertical(8f, 41f, listOf(Color(0xFFFF9AD5), Color(0xFFFF5FA8)))
    fillPath(DROP, pink)
    strokePath(DROP, SolidColor(Color.White), 2f, 0.85f)
    strokePath(DROP_SHINE, SolidColor(Color.White), 2.2f, 0.9f)
}

private fun customizeIcon(): ImageVector {
    val b = newBuilder("dock_customize", CustomizeFrame, DockMetrics.IconHeight)
    b.framed(CustomizeFrame) {
        glassBody(rr(10f, 32f, 108f, 72f, 22f), 32f, 104f)
        paintedKeys()
        paintDrop()
    }
    return b.build()
}

private fun ImageVector.Builder.gear() {
    val outline = gearPath(90f, 69f, 24.5f, 19f, 10)
    group(translationY = 4f) { fillPath(outline, Color.Black, 0.30f) }
    fillPath(outline, diagonal(66f, 45f, 114f, 93f, listOf(Color(0xFFD8CEFF), Color(0xFF7A6BFF))))
    strokePath(outline, SolidColor(Color.White), 2.2f, 0.9f)
    fillPath(GEAR_CORE, Color(0xFF2B2470), 0.55f)
    key(82.5f, 62.5f, 15f, 12.5f, radius = 4f)
}

private fun settingsIcon(): ImageVector {
    val b = newBuilder("dock_settings", SettingsFrame, DockMetrics.IconHeight)
    b.framed(SettingsFrame) {
        glassBody(rr(10f, 32f, 108f, 72f, 22f), 32f, 104f)
        keyRow(3, 18f, 44f, 18.5f, 14.5f, 12f)
        keyRow(2, 18f, 61f, 18.5f, 14.5f, 12f)
        key(18f, 78f, 38f, 12f)
        gear()
    }
    return b.build()
}

private fun ImageVector.Builder.textField() {
    glassBody(rr(14f, 12f, 100f, 32f, 16f), 12f, 44f, shadow = 4f)
    strokePath("M30 28H42", SolidColor(Color.White), 5f, 0.9f)
    strokePath("M48 28H62", SolidColor(Color.White), 5f, 0.9f)
    fillPath(rr(68f, 20f, 4f, 16f, 2f), vertical(20f, 36f, listOf(Color(0xFF9AF0FF), Color(0xFF2FC6FF))))
}

internal fun tryIcon(): ImageVector {
    val b = newBuilder("dock_try", TryFrame, DockMetrics.TryIconSize)
    b.framed(TryFrame) {
        textField()
        group(scaleX = 0.8f, scaleY = 0.8f, translationX = 12.8f, translationY = 33f) {
            glassBody(rr(10f, 32f, 108f, 72f, 22f), 32f, 104f)
            keyboardKeys()
        }
    }
    return b.build()
}

internal fun dockIcon(tab: DockTab): ImageVector = when (tab) {
    DockTab.HOME -> homeIcon()
    DockTab.CUSTOMIZE -> customizeIcon()
    DockTab.SETTINGS -> settingsIcon()
}
