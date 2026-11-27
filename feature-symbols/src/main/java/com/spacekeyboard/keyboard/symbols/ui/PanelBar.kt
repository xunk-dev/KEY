package com.spacekeyboard.keyboard.symbols.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.symbols.R
import com.spacekeyboard.keyboard.symbols.state.PanelMetrics

@Composable
internal fun SearchBar(query: String, palette: ThemePalette, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(PanelMetrics.BAR_DP.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(palette.keyFill))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val text = query.ifEmpty { stringResource(R.string.panel_search_hint) }
            SpaceText(text = text, style = SpaceTheme.typography.body, color = Color(palette.keyText))
            if (query.isNotEmpty()) Cursor(Color(palette.keyText))
        }
        PanelIconButton(PanelIcon.CLOSE, palette, stringResource(R.string.panel_close_search), onClose)
    }
}

@Composable
private fun Cursor(color: Color) {
    Box(Modifier.padding(start = 2.dp).width(2.dp).height(18.dp).background(color))
}
