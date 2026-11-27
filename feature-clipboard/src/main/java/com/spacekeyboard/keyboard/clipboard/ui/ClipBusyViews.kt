package com.spacekeyboard.keyboard.clipboard.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.clipboard.BusyKind
import com.spacekeyboard.keyboard.clipboard.ClipBusy
import com.spacekeyboard.keyboard.clipboard.ClipConfirm
import com.spacekeyboard.keyboard.clipboard.R
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.ui.PanelIcon
import java.text.NumberFormat

private const val CARD_ALPHA = 0.55f
private const val TRACK_ALPHA = 0.18f
private const val SWEEP_WIDTH = 0.35f
private const val SWEEP_MS = 1100

@Composable
internal fun BusyBar(percent: Int, palette: ThemePalette, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    val fill = Color(palette.keyText)
    Box(modifier.fillMaxWidth().height(4.dp).clip(shape).background(Color(palette.label).copy(alpha = TRACK_ALPHA))) {
        if (percent >= 0) {
            Box(Modifier.fillMaxWidth(percent / 100f).fillMaxHeight().background(fill))
        } else {
            val sweep = rememberInfiniteTransition(label = "clip-busy")
            val shift by sweep.animateFloat(0f, 1f, infiniteRepeatable(tween(SWEEP_MS, easing = LinearEasing)), label = "clip-busy-shift")
            Box(Modifier.align(BiasAlignment(-1f + 2f * shift, 0f)).fillMaxWidth(SWEEP_WIDTH).fillMaxHeight().background(fill))
        }
    }
}

@Composable
internal fun SavingCard(busy: ClipBusy, palette: ThemePalette, actions: ClipActions) {
    val label = Color(palette.label)
    val detail = if (busy.percent >= 0) stringResource(R.string.clip_busy_percent, busy.percent) else stringResource(R.string.clip_saving_wait)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(palette.keyFill).copy(alpha = CARD_ALPHA))
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SpaceText(stringResource(R.string.clip_saving_title), SpaceTheme.typography.body, color = label)
            SpaceText(detail, SpaceTheme.typography.bodySmall, color = label.copy(alpha = 0.6f))
            BusyBar(busy.percent, palette)
        }
        ClipIconKey(PanelIcon.CLOSE, stringResource(R.string.clip_stop), palette) { actions.onBusyStop(BusyKind.SAVING) }
    }
}

@Composable
internal fun PasteStrip(busy: ClipBusy, palette: ThemePalette, actions: ClipActions) {
    val count = stringResource(R.string.clip_busy_chars, ClipLabels.compact(busy.chars))
    val progress = if (busy.percent >= 0) stringResource(R.string.clip_busy_percent, busy.percent) + "  ·  " + count else count
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(palette.keyFill).copy(alpha = CARD_ALPHA))
            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SpaceText(stringResource(R.string.clip_pasting, progress), SpaceTheme.typography.bodySmall, color = Color(palette.label))
            BusyBar(busy.percent, palette)
        }
        ClipKey(stringResource(R.string.clip_stop), palette) { actions.onBusyStop(BusyKind.PASTING) }
    }
}

@Composable
internal fun PasteLargeView(confirm: ClipConfirm.PasteLarge, palette: ThemePalette, actions: ClipActions) {
    val count = NumberFormat.getInstance().format(confirm.length)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SpaceText(stringResource(R.string.clip_paste_large, count), SpaceTheme.typography.bodySmall, color = Color(palette.label))
        ClipKey(stringResource(R.string.clip_send_file), palette, Modifier.fillMaxWidth(), selected = true, spaceIcon = SpaceIcon.SHARE) {
            actions.onShare(confirm.id)
            actions.onConfirm(false)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ClipKey(stringResource(R.string.clip_paste_anyway), palette) { actions.onPasteLarge(confirm.id) }
            ClipKey(stringResource(R.string.clip_cancel), palette) { actions.onConfirm(false) }
        }
    }
}
