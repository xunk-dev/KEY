package com.spacekeyboard.keyboard.decorate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.decorate.DecorCatalog
import com.spacekeyboard.keyboard.decorate.DecorSnapshot
import com.spacekeyboard.keyboard.decorate.R
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon
import com.spacekeyboard.keyboard.emoji.ui.PanelIconView

private val BarHeight = 44.dp
private const val ACTIVE_FILL = 0.18f

@Composable
fun DecorTopBar(
    state: DecorSnapshot,
    sample: String,
    typeMode: Boolean,
    palette: ThemePalette,
    actions: DecorActions,
    onMode: (Boolean) -> Unit,
) {
    val active = state.active?.let(DecorCatalog::byId)
    if (active != null) ActiveStrip(active.apply(sample), palette, actions) else ModeBar(typeMode, palette, onMode)
}

@Composable
private fun ModeBar(typeMode: Boolean, palette: ThemePalette, onMode: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp).height(BarHeight - 8.dp)
            .clip(CircleShape).background(Color(palette.keyFill)).padding(2.dp),
    ) {
        Segment(stringResource(R.string.decor_mode_apply), !typeMode, palette, Modifier.weight(1f)) { onMode(false) }
        Segment(stringResource(R.string.decor_type_mode), typeMode, palette, Modifier.weight(1f)) { onMode(true) }
    }
}

@Composable
private fun Segment(text: String, selected: Boolean, palette: ThemePalette, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.fillMaxWidth().height(BarHeight - 12.dp).clip(CircleShape)
            .background(if (selected) Color(palette.keyPressed) else Color.Transparent)
            .clickable(role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(if (selected) palette.keyTextPressed else palette.keyText), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ActiveStrip(sample: String, palette: ThemePalette, actions: DecorActions) {
    val shape = RoundedCornerShape(16.dp)
    val stop = stringResource(R.string.decor_stop)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp).height(BarHeight)
            .clip(shape).background(Color(palette.glow).copy(alpha = ACTIVE_FILL)).border(1.5.dp, Color(palette.glow), shape),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.height(BarHeight).clip(shape).semantics { contentDescription = stop }
                .clickable(role = Role.Button, onClick = actions::onClearActive).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PanelIconView(PanelIcon.CLOSE, Color(palette.keyText))
            SpaceText(stop, SpaceTheme.typography.bodySmall, color = Color(palette.keyText), maxLines = 1)
        }
        SpaceText(sample, SpaceTheme.typography.title, Modifier.weight(1f), Color(palette.keyText), maxLines = 1, overflow = TextOverflow.Ellipsis)
        SpaceText(stringResource(R.string.decor_active_label), SpaceTheme.typography.bodySmall, Modifier.padding(end = 12.dp), Color(palette.glow), maxLines = 1)
    }
}
