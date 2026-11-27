package com.spacekeyboard.keyboard.emoji.ui

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.spacekeyboard.keyboard.emoji.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics

val EmojiCell = 44.dp
val EmojiGlyphSize = 28.dp

@Composable
fun EmojiGrid(
    texts: List<String>,
    typeface: Typeface?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
    hasTones: (Int) -> Boolean = { false },
    onHold: (Int) -> Unit = {},
    onBurst: ((String, Int) -> Unit)? = null,
) {
    val haptics = rememberSpaceHaptics()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(EmojiCell),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
    ) {
        items(texts.size) { index ->
            val text = texts[index]
            val tones = hasTones(index)
            EmojiCellView(
                text,
                typeface,
                onHold = if (tones) ({ haptics.perform(SpaceHaptic.Tap); onHold(index) }) else null,
                onBurst = if (!tones && onBurst != null) ({ count -> onBurst(text, count) }) else null,
            ) {
                onPick(text)
            }
        }
    }
}

@Composable
fun EmojiCellView(text: String, typeface: Typeface?, onHold: (() -> Unit)? = null, onBurst: ((Int) -> Unit)? = null, onClick: () -> Unit) {
    val haptics = rememberSpaceHaptics()
    Box(
        modifier = Modifier.size(EmojiCell).emojiPress(
            onTap = { if (onBurst == null) haptics.perform(SpaceHaptic.Tap); onClick() },
            onHold = onHold,
            onBurst = onBurst,
        ),
        contentAlignment = Alignment.Center,
    ) {
        EmojiGlyph(text, typeface, EmojiGlyphSize)
    }
}

private val RemoveBadge = Color(0xFFD92D3A)

@Composable
fun EmojiEditGrid(texts: List<String>, typeface: Typeface?, onRemove: (String) -> Unit, modifier: Modifier = Modifier) {
    val haptics = rememberSpaceHaptics()
    val description = stringResource(R.string.emoji_remove)
    LazyVerticalGrid(
        columns = GridCells.Adaptive(EmojiCell),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
    ) {
        items(texts, key = { it }) { text ->
            Box(
                modifier = Modifier
                    .size(EmojiCell)
                    .semantics { contentDescription = description }
                    .clickable(role = Role.Button) {
                        haptics.perform(SpaceHaptic.Tap)
                        onRemove(text)
                    },
                contentAlignment = Alignment.Center,
            ) {
                EmojiGlyph(text, typeface, EmojiGlyphSize)
                RemoveMark(Modifier.align(Alignment.TopStart))
            }
        }
    }
}

@Composable
private fun RemoveMark(modifier: Modifier) {
    Box(modifier = modifier.offset(x = 1.dp, y = 1.dp).size(18.dp).background(RemoveBadge, CircleShape), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(width = 9.dp, height = 2.dp).background(Color.White, CircleShape))
    }
}
