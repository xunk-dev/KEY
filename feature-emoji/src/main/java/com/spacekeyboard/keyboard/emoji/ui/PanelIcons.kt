package com.spacekeyboard.keyboard.emoji.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView

enum class PanelIcon(val space: SpaceIcon? = null) {
    SEARCH(SpaceIcon.SEARCH),
    BACKSPACE(SpaceIcon.DELETE),
    ADD(SpaceIcon.ADD),
    CLEAR_ALL(SpaceIcon.CLEAR_ALL),
    SAVE(SpaceIcon.SAVE),
    DONE(SpaceIcon.DONE),
    NEXT(SpaceIcon.NEXT),
    COPY(SpaceIcon.COPY),
    CUT(SpaceIcon.CUT),
    SELECT_ALL(SpaceIcon.SELECT_ALL),
    CLOCK(SpaceIcon.RECENTS),
    KEYBOARD(SpaceIcon.KEYS),
    PEN(SpaceIcon.PENCIL),
    LANGUAGE(SpaceIcon.LANGUAGE),
    SMILEYS(SpaceIcon.GROUP_FACES),
    BODY(SpaceIcon.GROUP_BODY),
    PEOPLE(SpaceIcon.GROUP_PEOPLE),
    ANIMALS(SpaceIcon.GROUP_ANIMALS),
    FOOD(SpaceIcon.GROUP_FOOD),
    TRAVEL(SpaceIcon.GROUP_TRAVEL),
    ACTIVITIES(SpaceIcon.GROUP_ACTIVITIES),
    OBJECTS(SpaceIcon.GROUP_OBJECTS),
    SYMBOLS(SpaceIcon.GROUP_SYMBOLS),
    FLAGS(SpaceIcon.GROUP_FLAGS),
    CLOSE,
    LOCK,
}

fun panelIconForGroup(id: String): PanelIcon = when (id) {
    "smileys" -> PanelIcon.SMILEYS
    "body" -> PanelIcon.BODY
    "people" -> PanelIcon.PEOPLE
    "animals" -> PanelIcon.ANIMALS
    "food" -> PanelIcon.FOOD
    "travel" -> PanelIcon.TRAVEL
    "activities" -> PanelIcon.ACTIVITIES
    "objects" -> PanelIcon.OBJECTS
    "symbols" -> PanelIcon.SYMBOLS
    "flags" -> PanelIcon.FLAGS
    else -> PanelIcon.CLOCK
}

@Composable
fun PanelIconView(icon: PanelIcon, color: Color, modifier: Modifier = Modifier) {
    val shared = icon.space
    if (shared != null) {
        SpaceIconView(shared, color, modifier, size = 20.dp)
        return
    }
    Canvas(modifier.size(20.dp)) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (icon) {
            PanelIcon.CLOSE -> close(color, stroke)
            PanelIcon.LOCK -> lock(color, stroke)
            else -> Unit
        }
    }
}

private fun DrawScope.at(x: Float, y: Float) = Offset(size.width * x, size.height * y)

private fun DrawScope.line(color: Color, a: Offset, b: Offset, stroke: Stroke) =
    drawLine(color, a, b, stroke.width, StrokeCap.Round)

private fun DrawScope.close(color: Color, stroke: Stroke) {
    line(color, at(0.2f, 0.2f), at(0.8f, 0.8f), stroke)
    line(color, at(0.2f, 0.8f), at(0.8f, 0.2f), stroke)
}

private fun DrawScope.lock(color: Color, stroke: Stroke) {
    drawRoundRect(
        color,
        topLeft = at(0.2f, 0.45f),
        size = Size(size.width * 0.6f, size.height * 0.45f),
        cornerRadius = CornerRadius(size.minDimension * 0.1f),
        style = stroke,
    )
    drawArc(color, 180f, 180f, false, at(0.32f, 0.1f), Size(size.width * 0.36f, size.height * 0.4f), style = stroke)
    line(color, at(0.32f, 0.3f), at(0.32f, 0.45f), stroke)
    line(color, at(0.68f, 0.3f), at(0.68f, 0.45f), stroke)
}
