package com.spacekeyboard.keyboard.emoji.ui

import android.graphics.Typeface
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp

@Composable
fun EmojiGlyph(text: String, typeface: Typeface?, size: Dp, modifier: Modifier = Modifier) {
    val family = remember(typeface) { if (typeface == null) FontFamily.Default else FontFamily(typeface) }
    val fontSize = with(LocalDensity.current) { size.toSp() }
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            fontFamily = family,
            fontSize = fontSize,
            lineHeight = fontSize * LINE_FACTOR,
            textAlign = TextAlign.Center,
        ),
        maxLines = 1,
        softWrap = false,
    )
}

private const val LINE_FACTOR = 1.25f
