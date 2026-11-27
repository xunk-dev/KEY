package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

internal class MenuAnchor(val item: ClipItem, val top: Float)

private class MenuEntry(val icon: SpaceIcon, val label: String, val danger: Boolean = false, val run: () -> Unit)

private const val SCRIM = 0.4f
private const val MENU_WIDTH = 220

@Composable
internal fun ItemMenu(anchor: MenuAnchor, palette: ThemePalette, actions: ClipActions, onDismiss: () -> Unit) {
    val entries = menuEntries(anchor.item, actions)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = SCRIM))
                .pointerInput(Unit) { detectTapGestures { onDismiss() } },
        )
        val available = maxHeight.value.toInt()
        val anchorTop = with(LocalDensity.current) { anchor.top.toDp() }.value.toInt()
        val height = ClipMenuMetrics.height(entries.size, available)
        val top = ClipMenuMetrics.top(anchorTop + ClipMenuMetrics.BELOW_CARD, height, available)
        Column(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp)
                .offset(y = top.dp)
                .width(MENU_WIDTH.dp)
                .height(height.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(palette.keyFill)),
        ) {
            MenuIconRow(anchor.item, palette, actions, onDismiss)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                entries.forEach { MenuRow(it, palette, onDismiss) }
            }
        }
    }
}

@Composable
private fun menuEntries(item: ClipItem, actions: ClipActions): List<MenuEntry> {
    val delete = MenuEntry(SpaceIcon.TRASH, stringResource(R.string.clip_delete), danger = true) { actions.onDelete(item.id) }
    val titled = item.title != null
    val title = MenuEntry(SpaceIcon.PENCIL, stringResource(if (titled) R.string.clip_menu_title_edit else R.string.clip_menu_title)) { actions.onTitle(item.id) }
    val untitle = MenuEntry(SpaceIcon.BROOM, stringResource(R.string.clip_menu_title_remove)) { actions.onTitleRemove(item.id) }
    val shortcut = MenuEntry(SpaceIcon.SHORTCUTS, stringResource(R.string.clip_menu_shortcut)) { actions.onShortcut(item) }
    val naming = (if (titled) listOf(title, untitle) else listOf(title)) + listOfNotNull(shortcut.takeIf { item.blob == null })
    if (item.section == null) {
        return naming + listOf(MenuEntry(SpaceIcon.BOOKMARK, stringResource(R.string.clip_menu_save)) { actions.onAssignStart(item.id) }, delete)
    }
    return naming + listOf(
        MenuEntry(SpaceIcon.FOLDER_ADD, stringResource(R.string.clip_menu_move)) { actions.onAssignStart(item.id) },
        MenuEntry(SpaceIcon.BOOKMARK, stringResource(R.string.clip_remove_section)) {
            actions.onAssignStart(item.id)
            actions.onAssign(null)
        },
        delete,
    )
}

@Composable
private fun MenuIconRow(item: ClipItem, palette: ThemePalette, actions: ClipActions, onDismiss: () -> Unit) {
    val id = item.id
    val line = Color(palette.border).copy(alpha = LINE_ALPHA)
    Row(
        Modifier
            .fillMaxWidth()
            .height(ClipMenuMetrics.ICON_ROW.dp)
            .drawBehind { drawRect(line, Offset(0f, size.height - 1.dp.toPx()), Size(size.width, 1.dp.toPx())) },
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MenuIcon(SpaceIcon.COPY, R.string.clip_copy, palette, onDismiss) { actions.onCopy(id) }
        MenuIcon(SpaceIcon.SHARE, R.string.clip_share, palette, onDismiss) { actions.onShare(id) }
        if (item.blob == null) MenuIcon(SpaceIcon.PENCIL, R.string.clip_edit, palette, onDismiss) { actions.onEdit(id) }
        MenuIcon(SpaceIcon.FULL_VIEW, R.string.clip_view, palette, onDismiss) { actions.onView(id) }
    }
}

@Composable
private fun MenuIcon(icon: SpaceIcon, label: Int, palette: ThemePalette, onDismiss: () -> Unit, run: () -> Unit) {
    ClipSpaceKey(icon, stringResource(label), palette) {
        onDismiss()
        run()
    }
}

@Composable
private fun MenuRow(entry: MenuEntry, palette: ThemePalette, onDismiss: () -> Unit) {
    val tint = if (entry.danger) ClipDanger else Color(palette.keyText)
    Row(
        Modifier
            .fillMaxWidth()
            .height(ClipMenuMetrics.ROW.dp)
            .clickable {
                onDismiss()
                entry.run()
            }
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClipIcon(entry.icon, tint, 22.dp)
        SpaceText(entry.label, SpaceTheme.typography.body, color = tint)
    }
}
