package com.spacekeyboard.keyboard.symbols.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.symbols.model.FaceEntry
import com.spacekeyboard.keyboard.symbols.model.SymbolEntry

@Composable
internal fun SymbolCell(
    entry: SymbolEntry,
    palette: ThemePalette,
    arabic: Boolean,
    modifier: Modifier = Modifier,
    onInsert: (String) -> Unit,
) {
    val tap = rememberTap()
    val label = entry.label(arabic)
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(palette.keyFill))
            .clickable(role = Role.Button) {
                tap()
                onInsert(entry.text)
            }
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = entry.text,
            style = TextStyle(
                color = Color(palette.keyText),
                fontSize = 22.sp,
                fontFamily = FontFamily.Default,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
internal fun FaceCell(
    entry: FaceEntry,
    palette: ThemePalette,
    modifier: Modifier = Modifier,
    onInsert: (String) -> Unit,
) {
    val tap = rememberTap()
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(palette.keyFill))
            .clickable(role = Role.Button) {
                tap()
                onInsert(entry.text)
            }
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = entry.text,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            style = TextStyle(
                color = Color(palette.keyText),
                fontSize = 16.sp,
                fontFamily = FontFamily.Default,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
