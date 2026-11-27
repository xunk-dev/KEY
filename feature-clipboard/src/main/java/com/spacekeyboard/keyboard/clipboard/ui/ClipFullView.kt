package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.ClipPolicy
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon
import java.text.NumberFormat

private const val VIEW_ALPHA = 0.55f

@Composable
fun ClipFullView(item: ClipItem, palette: ThemePalette, actions: ClipActions) {
    Column(Modifier.fillMaxSize().background(Color(palette.background).copy(alpha = VIEW_ALPHA))) {
        ViewHeader(item, palette, actions)
        if (item.blob != null) PreviewNote(item, palette)
        val style = SpaceTheme.typography.body.copy(textDirection = TextDirection.Content)
        val toolbar = remember { SelectionToolbar() }
        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp)) {
            CompositionLocalProvider(LocalTextToolbar provides toolbar) {
                SelectionContainer {
                    SpaceText(
                        item.text,
                        style,
                        color = Color(palette.label),
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    )
                }
            }
            SelectionBar(toolbar, palette, Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
        }
        ViewActions(item, palette, actions)
    }
}

@Composable
private fun ViewHeader(item: ClipItem, palette: ThemePalette, actions: ClipActions) {
    Row(
        Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val count = NumberFormat.getInstance().format(item.length)
        SpaceText(
            stringResource(R.string.clip_view_length, count),
            SpaceTheme.typography.bodySmall,
            color = Color(palette.label).copy(alpha = 0.7f),
            modifier = Modifier.weight(1f),
        )
        ClipIconKey(PanelIcon.CLOSE, stringResource(R.string.clip_view_close), palette, actions::onViewClose)
    }
}

@Composable
private fun PreviewNote(item: ClipItem, palette: ThemePalette) {
    SpaceText(
        stringResource(R.string.clip_view_preview, NumberFormat.getInstance().format(item.text.length)),
        SpaceTheme.typography.bodySmall,
        color = Color(palette.label).copy(alpha = 0.7f),
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
    )
}

@Composable
private fun ViewActions(item: ClipItem, palette: ThemePalette, actions: ClipActions) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val weight = Modifier.weight(1f)
        ClipCapsuleKey(stringResource(R.string.clip_insert), palette, weight, primary = true, spaceIcon = SpaceIcon.INSERT) { actions.onPick(item) }
        ClipCapsuleKey(stringResource(R.string.clip_copy), palette, weight, spaceIcon = SpaceIcon.COPY) { actions.onCopy(item.id) }
        val shareText = if (item.length > ClipPolicy.DIRECT_COPY) R.string.clip_send_file else R.string.clip_share
        ClipCapsuleKey(stringResource(shareText), palette, weight, spaceIcon = SpaceIcon.SHARE) { actions.onShare(item.id) }
        if (item.blob == null) {
            ClipCapsuleKey(stringResource(R.string.clip_edit), palette, weight, spaceIcon = SpaceIcon.PENCIL) { actions.onEdit(item.id) }
        }
    }
}

@Stable
internal class SelectionToolbar : TextToolbar {
    var copy by mutableStateOf<(() -> Unit)?>(null)
        private set
    var selectAll by mutableStateOf<(() -> Unit)?>(null)
        private set
    override var status by mutableStateOf(TextToolbarStatus.Hidden)
        private set

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {
        copy = onCopyRequested
        selectAll = onSelectAllRequested
        status = TextToolbarStatus.Shown
    }

    override fun hide() {
        copy = null
        selectAll = null
        status = TextToolbarStatus.Hidden
    }
}

@Composable
private fun SelectionBar(toolbar: SelectionToolbar, palette: ThemePalette, modifier: Modifier) {
    if (toolbar.status != TextToolbarStatus.Shown) return
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        toolbar.copy?.let { copy ->
            ClipCapsuleKey(stringResource(R.string.clip_copy_selection), palette, primary = true, spaceIcon = SpaceIcon.COPY) { copy() }
        }
        toolbar.selectAll?.let { all ->
            ClipCapsuleKey(stringResource(R.string.clip_select_all), palette) { all() }
        }
    }
}
