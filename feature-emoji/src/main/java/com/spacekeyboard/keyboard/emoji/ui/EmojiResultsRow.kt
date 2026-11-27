package com.spacekeyboard.keyboard.emoji.ui

import android.graphics.Typeface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.emoji.EmojiSnapshot
import com.spacekeyboard.keyboard.emoji.R
import com.spacekeyboard.keyboard.emoji.model.Emoji
import com.spacekeyboard.keyboard.emoji.model.SkinTone

private val Gap = 4.dp
private val SidePad = 2.dp
private val KeyInset = 3.dp
private val FadeWidth = 28.dp
private val KeyGlyph = 24.dp
private const val SLOTS = 11

@Composable
fun EmojiResultsRow(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions) {
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        var holding by remember(state.query) { mutableStateOf<Emoji?>(null) }
        val hold = holding
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                hold != null -> ToneKeys(hold, state, palette, actions) { holding = null }
                state.query.isBlank() -> RecentKeys(state, palette, actions)
                !state.searchReady -> Notice(stringResource(R.string.emoji_search_loading), palette)
                state.results.isEmpty() -> Notice(stringResource(R.string.emoji_search_none), palette)
                else -> ResultKeys(state, palette, actions) { holding = it }
            }
        }
    }
}

@Composable
private fun Notice(text: String, palette: ThemePalette) {
    SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.label), maxLines = 1)
}

@Composable
private fun RecentKeys(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions) {
    val texts = state.recents
    KeyStrip(texts, state.typeface, palette, null, { index, count -> actions.onPickMany(texts[index], count) }) { actions.onPick(texts[it]) }
}

@Composable
private fun ResultKeys(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions, onHold: (Emoji?) -> Unit) {
    val texts = remember(state.results, state.tone) { state.results.map { it.forTone(state.tone) } }
    val hold: (Int) -> Unit = { index -> onHold(state.results[index]) }
    val tones: (Int) -> Boolean = { index -> state.results[index].hasTones }
    KeyStrip(texts, state.typeface, palette, hold, { index, count -> actions.onPickMany(texts[index], count) }, tones) { actions.onPick(texts[it]) }
}

@Composable
private fun ToneKeys(emoji: Emoji, state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions, onDismiss: () -> Unit) {
    val texts = remember(emoji) { SkinTone.entries.map { emoji.forTone(it) } }
    Row(Modifier.fillMaxSize().padding(horizontal = SidePad), verticalAlignment = Alignment.CenterVertically) {
        DockButton(PanelIcon.CLOSE, stringResource(R.string.emoji_search_close), palette, false, Modifier.size(DockKey), onDismiss)
        Box(Modifier.weight(1f).fillMaxHeight()) {
            KeyStrip(texts, state.typeface, palette, null, null) { index ->
                val tone = SkinTone.entries[index]
                actions.onTone(tone)
                actions.onPick(texts[index])
                onDismiss()
            }
        }
    }
}

@Composable
private fun KeyStrip(
    texts: List<String>,
    typeface: Typeface?,
    palette: ThemePalette,
    onHold: ((Int) -> Unit)?,
    onBurst: ((Int, Int) -> Unit)?,
    hasTones: (Int) -> Boolean = { false },
    onPick: (Int) -> Unit,
) {
    val haptics = rememberSpaceHaptics()
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val width = (maxWidth - SidePad * 2 - Gap * (SLOTS - 1)) / SLOTS
        LazyRow(
            modifier = Modifier.fillMaxSize().edgeFade(rtl, texts.size > SLOTS),
            contentPadding = PaddingValues(horizontal = SidePad),
            horizontalArrangement = Arrangement.spacedBy(Gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(texts.size) { index ->
                val tones = hasTones(index)
                val hold: (() -> Unit)? = if (tones) onHold?.let { handler -> { handler(index) } } else null
                val burst: ((Int) -> Unit)? = if (tones) null else onBurst?.let { handler -> { count -> handler(index, count) } }
                Box(
                    modifier = Modifier
                        .width(width)
                        .fillMaxHeight()
                        .padding(vertical = KeyInset)
                        .panelKeyLook(palette, false)
                        .emojiPress(
                            onTap = {
                                if (burst == null) haptics.perform(SpaceHaptic.Tap)
                                onPick(index)
                            },
                            onHold = hold,
                            onBurst = burst,
                        ),
                    contentAlignment = Alignment.Center,
                ) { EmojiGlyph(texts[index], typeface, KeyGlyph) }
            }
        }
    }
}

fun Modifier.edgeFade(rtl: Boolean, enabled: Boolean): Modifier {
    if (!enabled) return this
    return graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
        drawContent()
        val fade = FadeWidth.toPx()
        val brush = if (rtl) {
            Brush.horizontalGradient(listOf(Color.Transparent, Color.Black), 0f, fade)
        } else {
            Brush.horizontalGradient(listOf(Color.Black, Color.Transparent), size.width - fade, size.width)
        }
        drawRect(brush, blendMode = BlendMode.DstIn)
    }
}
