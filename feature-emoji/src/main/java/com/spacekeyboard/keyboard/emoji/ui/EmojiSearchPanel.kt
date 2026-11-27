package com.spacekeyboard.keyboard.emoji.ui

import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.emoji.EmojiSnapshot
import com.spacekeyboard.keyboard.emoji.R
import com.spacekeyboard.keyboard.emoji.model.Emoji
import com.spacekeyboard.keyboard.emoji.model.SkinTone

private const val CARET = "|"
private const val ELLIPSIS = "…"
private const val TAIL_CHARS = 9
private const val QUERY_ALPHA = 0.45f
private const val HINT_ALPHA = 0.6f
private const val PILL_SHARE = 0.42f
private const val MIN_WEIGHT = 0.001f
private const val START_SCALE = 0.2f
private const val ENTER_MS = 240
private const val SHARE_MS = 200

private typealias Reveal = Animatable<Float, AnimationVector1D>

@Composable
fun EmojiSearchPanel(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions, resultsBelow: Boolean = false) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
            val reveal = rememberReveal()
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DockButton(
                    PanelIcon.CLOSE,
                    stringResource(R.string.emoji_search_close),
                    palette,
                    false,
                    Modifier.size(DockKey).graphicsLayer { alpha = reveal.value },
                    actions::onCloseSearch,
                )
                SearchField(state, palette, actions, reveal, resultsBelow, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun rememberReveal(): Reveal {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) { reveal.animateTo(1f, tween(ENTER_MS, easing = FastOutSlowInEasing)) }
    return reveal
}

@Composable
private fun SearchField(
    state: EmojiSnapshot,
    palette: ThemePalette,
    actions: EmojiActions,
    reveal: Reveal,
    resultsBelow: Boolean,
    modifier: Modifier,
) {
    var holding by remember(state.query) { mutableStateOf<Emoji?>(null) }
    val wide = state.query.isBlank() || resultsBelow
    val share by animateFloatAsState(if (wide) 1f else PILL_SHARE, tween(SHARE_MS), label = "pillShare")
    val originX = if (LocalConfiguration.current.layoutDirection == View.LAYOUT_DIRECTION_RTL) 0f else 1f
    Row(
        modifier = modifier.fillMaxHeight().graphicsLayer {
            alpha = reveal.value
            scaleX = START_SCALE + (1f - START_SCALE) * reveal.value
            transformOrigin = TransformOrigin(originX, 0.5f)
        },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QueryPill(state.query, palette, Modifier.weight(share.coerceAtLeast(MIN_WEIGHT)).height(DockKey))
        Box(Modifier.weight((1f - share).coerceAtLeast(MIN_WEIGHT)).fillMaxHeight(), contentAlignment = Alignment.CenterStart) {
            if (!wide) Results(state, palette, actions, holding) { holding = it }
        }
    }
}

@Composable
private fun QueryPill(query: String, palette: ThemePalette, modifier: Modifier) {
    val blank = query.isEmpty()
    Row(
        modifier = modifier.clip(CircleShape).background(Color(palette.background).copy(alpha = QUERY_ALPHA)).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PanelIconView(PanelIcon.SEARCH, Color(palette.label))
        SpaceText(
            text = if (blank) stringResource(R.string.emoji_search_placeholder) else visibleTail(query) + CARET,
            style = SpaceTheme.typography.body,
            color = Color(palette.keyText).copy(alpha = if (blank) HINT_ALPHA else 1f),
            maxLines = 1,
        )
    }
}

private fun visibleTail(query: String): String =
    if (query.length > TAIL_CHARS) ELLIPSIS + query.takeLast(TAIL_CHARS) else query

@Composable
private fun Results(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions, holding: Emoji?, onHold: (Emoji?) -> Unit) {
    when {
        holding != null -> ToneStrip(holding, state, palette, actions) { onHold(null) }
        !state.searchReady -> Notice(stringResource(R.string.emoji_search_loading), palette)
        state.results.isEmpty() -> Notice(stringResource(R.string.emoji_search_none), palette)
        else -> ResultStrip(state, actions, onHold)
    }
}

@Composable
private fun Notice(text: String, palette: ThemePalette) {
    SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.label), maxLines = 1)
}

@Composable
private fun ResultStrip(state: EmojiSnapshot, actions: EmojiActions, onHold: (Emoji?) -> Unit) {
    val texts = remember(state.results, state.tone) { state.results.map { it.forTone(state.tone) } }
    val listState = remember(state.query) { LazyListState() }
    LazyRow(state = listState, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
        items(texts.size) { index ->
            val emoji = state.results[index]
            EmojiCellView(
                texts[index],
                state.typeface,
                onHold = if (emoji.hasTones) ({ onHold(emoji) }) else null,
                onBurst = if (emoji.hasTones) null else ({ count -> actions.onPickMany(texts[index], count) }),
            ) {
                actions.onPick(texts[index])
            }
        }
    }
}

@Composable
private fun ToneStrip(emoji: Emoji, state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions, onDismiss: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
        DockButton(PanelIcon.CLOSE, stringResource(R.string.emoji_search_close), palette, false, Modifier.size(DockKey), onDismiss)
        LazyRow(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).fillMaxHeight()) {
            items(SkinTone.entries.size) { index ->
                val tone = SkinTone.entries[index]
                EmojiCellView(emoji.forTone(tone), state.typeface) {
                    actions.onTone(tone)
                    actions.onPick(emoji.forTone(tone))
                    onDismiss()
                }
            }
        }
    }
}
