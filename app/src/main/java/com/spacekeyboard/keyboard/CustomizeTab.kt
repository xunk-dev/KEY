package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

internal enum class CustomizeTab(val icon: SpaceIcon, val label: Int, val hint: Int) {
    STYLE(SpaceIcon.THEME, R.string.ctab_style, R.string.ctab_style_hint),
    COLORS(SpaceIcon.OPACITY, R.string.ctab_colors, R.string.ctab_colors_hint),
    KEYS(SpaceIcon.KEYS, R.string.ctab_keys, R.string.ctab_keys_hint),
    SIZE(SpaceIcon.RESIZE, R.string.ctab_size, R.string.ctab_size_hint),
    TOOLS(SpaceIcon.TOOLS, R.string.ctab_tools, R.string.ctab_tools_hint),
    TOUCH(SpaceIcon.VIBRATE, R.string.ctab_touch, R.string.ctab_touch_hint),
    SOUND(SpaceIcon.SOUND, R.string.ctab_sound, R.string.ctab_sound_hint),
}

private const val TAB_BAR_ALPHA = 0.86f
private const val SELECTED_WEIGHT = 2f

@Composable
internal fun CustomizeTabBar(selected: CustomizeTab, onSelect: (CustomizeTab) -> Unit) {
    val colors = SpaceTheme.colors
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(shape)
            .background(colors.containerHigh.copy(alpha = TAB_BAR_ALPHA))
            .border(1.dp, colors.outline, shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        CustomizeTab.entries.forEach { TabItem(it, it == selected, onSelect) }
    }
}

@Composable
private fun RowScope.TabItem(tab: CustomizeTab, selected: Boolean, onSelect: (CustomizeTab) -> Unit) {
    val colors = SpaceTheme.colors
    val ink = if (selected) colors.onPrimary else colors.onSurfaceVariant
    val label = stringResource(tab.label)
    Row(
        modifier = Modifier
            .weight(if (selected) SELECTED_WEIGHT else 1f)
            .height(42.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) colors.primary else Color.Transparent)
            .selectable(selected = selected, role = Role.Tab) { onSelect(tab) },
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceIconView(tab.icon, ink, size = 20.dp)
        if (selected) SpaceText(label, SpaceTheme.typography.label, color = ink, maxLines = 1)
    }
}
