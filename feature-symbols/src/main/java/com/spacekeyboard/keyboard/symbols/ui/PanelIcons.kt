package com.spacekeyboard.keyboard.symbols.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView

internal enum class PanelIcon { SEARCH, BACKSPACE, CLOSE }

@Composable
internal fun PanelIconButton(icon: PanelIcon, palette: ThemePalette, description: String, onClick: () -> Unit) {
    val tap = rememberTap()
    val ink = Color(palette.keyText)
    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(palette.keyFill))
            .clickable(role = Role.Button) {
                tap()
                onClick()
            }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        PanelGlyph(icon, ink)
    }
}

@Composable
private fun PanelGlyph(icon: PanelIcon, ink: Color) {
    when (icon) {
        PanelIcon.SEARCH -> SpaceIconView(SpaceIcon.SEARCH, ink, size = 20.dp)
        PanelIcon.BACKSPACE -> SpaceIconView(SpaceIcon.DELETE, ink, size = 20.dp)
        PanelIcon.CLOSE -> Canvas(Modifier.size(20.dp)) { close(ink) }
    }
}

private fun DrawScope.line(color: Color, from: Offset, to: Offset) {
    drawLine(color, from, to, strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
}

private fun DrawScope.close(color: Color) {
    val w = size.width
    line(color, Offset(w * 0.2f, w * 0.2f), Offset(w * 0.8f, w * 0.8f))
    line(color, Offset(w * 0.2f, w * 0.8f), Offset(w * 0.8f, w * 0.2f))
}
