package com.spacekeyboard.keyboard.emoji.ui

import android.graphics.Typeface
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
fun EmojiGrid(texts: List<String>, typeface: Typeface?, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    val haptics = rememberSpaceHaptics()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(EmojiCell),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
    ) {
        items(texts.size) { index ->
            val text = texts[index]
            EmojiCellView(text, typeface) {
                haptics.perform(SpaceHaptic.Tap)
                onPick(text)
            }
        }
    }
}

@Composable
fun EmojiCellView(text: String, typeface: Typeface?, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(EmojiCell).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        EmojiGlyph(text, typeface, EmojiGlyphSize)
    }
}
