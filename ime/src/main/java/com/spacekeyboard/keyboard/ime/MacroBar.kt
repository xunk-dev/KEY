package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

internal const val MACRO_BAR_DP = 42
internal val RecordRed = Color(0xFFE5484D)

@Composable
internal fun MacroBarContent(theme: ThemeController, macro: MacroController, snippets: SnippetController) {
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        val palette = rememberThemeSnapshot(theme).palette
        if (snippets.barShown) SnippetBar(palette, snippets) else if (macro.mode != MacroMode.IDLE) BarRow(palette, macro)
    }
}

@Composable
private fun BarRow(palette: ThemePalette, macro: MacroController) {
    val ink = Color(palette.label)
    val fill = Color(ColorMath.mix(palette.background, palette.keyFill, 0.5f))
    Row(
        Modifier.fillMaxSize().padding(vertical = 3.dp).clip(RoundedCornerShape(14.dp)).background(fill).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val recording = macro.mode == MacroMode.RECORDING
        Box(Modifier.size(10.dp).background(if (recording) RecordRed else Color(palette.glow), CircleShape))
        BasicText(
            barText(macro),
            modifier = Modifier.weight(1f),
            style = TextStyle(color = ink, fontSize = 13.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (recording) {
            if (macro.canLive) BarChip(stringResource(R.string.macro_live_paste), Color(palette.glow), palette, macro::makeLive)
            MacroIconButton(SpaceIcon.STOP, stringResource(R.string.macro_stop), RecordRed, ink) { macro.finishRecording() }
        } else {
            PlayControls(macro, palette, ink)
        }
    }
}

@Composable
private fun PlayControls(macro: MacroController, palette: ThemePalette, ink: Color) {
    val paused = macro.mode == MacroMode.PAUSED
    val glow = Color(palette.glow)
    MacroIconButton(
        if (paused) SpaceIcon.PLAY else SpaceIcon.PAUSE,
        stringResource(if (paused) R.string.macro_resume else R.string.macro_pause),
        glow,
        ink,
    ) { if (paused) macro.resume() else macro.pause() }
    BarChip(stringResource(macro.speed.labelRes), glow, palette, macro::cycleSpeed)
    MacroIconButton(SpaceIcon.CLOSE, stringResource(R.string.macro_exit), ink, ink) { macro.halt() }
}

@Composable
private fun BarChip(text: String, tint: Color, palette: ThemePalette, onClick: () -> Unit) {
    BasicText(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(ColorMath.withAlpha(palette.glow, 0.22f)))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        style = TextStyle(color = tint, fontSize = 12.sp),
        maxLines = 1,
    )
}

@Composable
private fun barText(macro: MacroController): String = when (macro.mode) {
    MacroMode.RECORDING -> stringResource(if (macro.full) R.string.macro_full else R.string.macro_recording, macro.recorded)
    MacroMode.PLAYING -> playText(macro, R.string.macro_playing, R.string.macro_playing_of)
    MacroMode.PAUSED -> playText(macro, R.string.macro_paused, R.string.macro_paused_of)
    MacroMode.IDLE -> ""
}

@Composable
private fun playText(macro: MacroController, plain: Int, counted: Int): String =
    if (macro.rounds > 0) {
        stringResource(counted, macro.position, macro.total, macro.round, macro.rounds)
    } else {
        stringResource(plain, macro.position, macro.total, macro.round)
    }

@Composable
internal fun MacroIconButton(icon: SpaceIcon, description: String, tint: Color, ink: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(ink.copy(alpha = 0.12f))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) { SpaceIconView(icon, tint, Modifier, 20.dp) }
}
