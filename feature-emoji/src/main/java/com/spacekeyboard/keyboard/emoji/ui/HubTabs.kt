package com.spacekeyboard.keyboard.emoji.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import kotlin.math.roundToInt
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.a11y.spaceHoldRepeat
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.R

enum class HubTab(val icon: SpaceIcon) {
    EMOJI(SpaceIcon.EMOJI),
    SYMBOLS(SpaceIcon.SYMBOLS),
    FACES(SpaceIcon.FACE_CLASSIC),
    ART(SpaceIcon.ART),
    IMAGES(SpaceIcon.IMAGES),
}

@Composable
private fun HubTab.label(): String = stringResource(
    when (this) {
        HubTab.EMOJI -> R.string.hub_tab_emoji
        HubTab.SYMBOLS -> R.string.hub_tab_symbols
        HubTab.FACES -> R.string.hub_tab_faces
        HubTab.ART -> R.string.hub_tab_art
        HubTab.IMAGES -> R.string.hub_tab_images
    },
)

private val BarHeight = 36.dp
private val UnderlineHeight = 3.dp
private const val IDLE_ALPHA = 0.72f
private const val GLOW_ALPHA = 0.35f

@Composable
fun HubTabBar(
    tabs: List<HubTab>,
    current: HubTab,
    palette: ThemePalette,
    onTab: (HubTab) -> Unit,
    onSearch: (() -> Unit)?,
    searchDescription: String,
) {
    val direction = if (LocalConfiguration.current.layoutDirection == android.view.View.LAYOUT_DIRECTION_RTL) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }
    val rtl = direction == LayoutDirection.Rtl
    val context = LocalContext.current
    val store = remember { HubTabOrderStore(context) }
    val reorder = remember { HubReorder() }
    val haptics = rememberSpaceHaptics()
    val latest by rememberUpdatedState(tabs)
    val gesture = Modifier.hubReorderGesture(reorder, rtl, haptics, { latest }) { store.save(HubTabOrder.merge(store.read(), it)) }
    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        Row(
            modifier = Modifier.fillMaxWidth().height(BarHeight).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(modifier = Modifier.weight(1f).fillMaxHeight().then(gesture)) {
                val shown = if (reorder.active) reorder.preview else tabs
                shown.forEach { tab ->
                    key(tab) {
                        val held = reorder.held == tab
                        val lift = Modifier.weight(1f).zIndex(if (held) 1f else 0f).absoluteOffset { IntOffset(reorder.offsetOf(tab, rtl).roundToInt(), 0) }
                        HubTabItem(tab, tab == current, palette, lift) { if (!reorder.active) onTab(tab) }
                    }
                }
            }
            if (onSearch != null) {
                DockButton(PanelIcon.SEARCH, searchDescription, palette, false, Modifier.size(DockKey), onSearch)
            } else {
                SearchPlaceholder(palette)
            }
        }
    }
}

@Composable
private fun HubTabItem(tab: HubTab, selected: Boolean, palette: ThemePalette, modifier: Modifier, onClick: () -> Unit) {
    val label = tab.label()
    val ink = Color(if (selected) palette.label else palette.keyText)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .alpha(if (selected) 1f else IDLE_ALPHA)
            .semantics {
                contentDescription = label
                this.selected = selected
            }
            .clickable(role = Role.Tab, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            SpaceIconView(tab.icon, ink, size = 18.dp)
            SpaceText(label, SpaceTheme.typography.bodySmall, color = ink)
        }
        if (selected) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .fillMaxWidth(UNDERLINE_SHARE)
                    .height(UnderlineHeight)
                    .clip(CircleShape)
                    .background(Color(palette.glow)),
            )
        }
    }
}

private const val UNDERLINE_SHARE = 0.64f
private const val PLACEHOLDER_ALPHA = 0.3f

@Composable
private fun SearchPlaceholder(palette: ThemePalette) {
    Box(Modifier.size(DockKey).alpha(PLACEHOLDER_ALPHA), contentAlignment = Alignment.Center) {
        PanelIconView(PanelIcon.SEARCH, Color(palette.keyText))
    }
}

@Composable
fun DockCategory(icon: SpaceIcon, label: String, palette: ThemePalette, selected: Boolean, onClick: () -> Unit) {
    val ink = Color(if (selected) palette.keyTextPressed else palette.keyText)
    Row(
        modifier = Modifier
            .height(DockKey)
            .widthIn(min = DockKey)
            .clip(CircleShape)
            .background(if (selected) Color(palette.keyPressed) else Color.Transparent)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceIconView(icon, ink, size = 20.dp)
    }
}

@Composable
fun DockAction(icon: SpaceIcon, description: String, palette: ThemePalette, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(DockKey)
            .clip(CircleShape)
            .semantics { contentDescription = description }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(icon, Color(palette.keyText), size = 20.dp)
    }
}

@Composable
fun DockDelete(palette: ThemePalette, description: String, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .size(DockKey)
            .clip(CircleShape)
            .semantics { contentDescription = description }
            .spaceHoldRepeat(onDelete),
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(SpaceIcon.DELETE, Color(palette.keyText), size = 20.dp)
    }
}
