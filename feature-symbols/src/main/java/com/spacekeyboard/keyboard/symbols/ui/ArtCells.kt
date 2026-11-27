package com.spacekeyboard.keyboard.symbols.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.symbols.model.ArtEntry

private val CellPadding = 8.dp
private val MinHeight = 52.dp
private val MaxHeight = 150.dp
private const val MIN_SIZE = 5f
private const val MAX_SIZE = 20f
private const val LINE_FACTOR = 1.14f

internal fun artFontSize(entry: ArtEntry, width: Dp, height: Dp): Float {
    val byWidth = (width.value - CellPadding.value * 2) / entry.widest()
    val byHeight = (height.value - CellPadding.value * 2) / (entry.rowCount * LINE_FACTOR)
    return minOf(byWidth, byHeight).coerceIn(MIN_SIZE, MAX_SIZE)
}

@Composable
internal fun ArtCell(
    entry: ArtEntry,
    palette: ThemePalette,
    description: String,
    modifier: Modifier = Modifier,
    onInsert: (String) -> Unit,
) {
    val tap = rememberTap()
    BoxWithConstraints(
        modifier = modifier
            .heightIn(min = MinHeight, max = MaxHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(palette.keyFill))
            .clickable(role = Role.Button) {
                tap()
                onInsert(entry.text)
            }
            .semantics { contentDescription = description }
            .padding(CellPadding),
        contentAlignment = Alignment.Center,
    ) {
        val size = artFontSize(entry, maxWidth, maxHeight)
        BasicText(
            text = entry.text,
            maxLines = entry.rowCount,
            softWrap = false,
            overflow = TextOverflow.Clip,
            style = TextStyle(
                color = Color(palette.keyText),
                fontSize = size.sp,
                lineHeight = (size * LINE_FACTOR).sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
