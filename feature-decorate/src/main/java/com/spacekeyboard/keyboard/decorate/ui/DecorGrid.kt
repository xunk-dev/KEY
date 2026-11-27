package com.spacekeyboard.keyboard.decorate.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.decorate.DecorSnapshot
import com.spacekeyboard.keyboard.decorate.DecorStyle
import com.spacekeyboard.keyboard.decorate.R
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private val CardHeight = 64.dp
private val CardShape = RoundedCornerShape(18.dp)
private val ActiveRing = 1.5.dp

@Composable
fun DecorGrid(
    styles: List<DecorStyle>,
    sample: String,
    state: DecorSnapshot,
    palette: ThemePalette,
    onPick: (DecorStyle) -> Unit,
    onFavorite: (String) -> Unit,
) {
    if (styles.isEmpty()) {
        EmptyNote(palette)
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(styles, key = { it.id }) { style ->
            val favorite = style.id in state.favorites
            DecorCard(style.apply(sample), sample, favorite, style.id == state.active, palette, { onPick(style) }) { onFavorite(style.id) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DecorCard(
    text: String,
    sample: String,
    favorite: Boolean,
    active: Boolean,
    palette: ThemePalette,
    onTap: () -> Unit,
    onHold: () -> Unit,
) {
    val haptics = rememberSpaceHaptics()
    val apply = stringResource(R.string.decor_apply_desc, sample)
    val holdLabel = stringResource(R.string.decor_favorite_desc)
    val ring = if (active) Modifier.border(ActiveRing, Color(palette.glow), CardShape) else Modifier
    fun press(action: () -> Unit) {
        haptics.perform(SpaceHaptic.Tap)
        action()
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(CardHeight)
            .clip(CardShape)
            .background(Color(if (active) palette.keyPressed else palette.keyFill))
            .then(ring)
            .semantics { contentDescription = apply }
            .combinedClickable(role = Role.Button, onLongClickLabel = holdLabel, onLongClick = { press(onHold) }, onClick = { press(onTap) }),
        contentAlignment = Alignment.Center,
    ) {
        CardText(text, active, palette)
        val starLabel = stringResource(R.string.decor_favorite_desc)
        Box(
            Modifier.align(Alignment.TopStart).size(36.dp).clip(CircleShape)
                .semantics { contentDescription = starLabel }
                .clickable(role = Role.Checkbox) { press(onHold) },
            contentAlignment = Alignment.Center,
        ) {
            SpaceIconView(SpaceIcon.SYM_STARS, Color(if (favorite) palette.glow else palette.label).copy(alpha = if (favorite) 1f else 0.45f), size = 16.dp)
        }
    }
}

@Composable
private fun CardText(text: String, active: Boolean, palette: ThemePalette) {
    SpaceText(
        text,
        SpaceTheme.typography.title,
        color = Color(if (active) palette.keyTextPressed else palette.keyText),
        modifier = Modifier.padding(horizontal = 12.dp).clearAndSetSemantics { },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun EmptyNote(palette: ThemePalette) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        SpaceText(
            stringResource(R.string.decor_empty_favorites),
            SpaceTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
            color = Color(palette.label),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
        )
    }
}
