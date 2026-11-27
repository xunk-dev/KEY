package com.spacekeyboard.keyboard.ime

import android.view.View
import android.widget.FrameLayout
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

internal class SuggestionActions(val onPick: (String) -> Unit, val onRemove: (String) -> Unit)

@Composable
internal fun SuggestionBar(
    items: List<String>,
    palette: ThemePalette,
    style: KeyStyle,
    actions: SuggestionActions,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    val gap = if (style == KeyStyle.DAY) 6.dp else 0.dp
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
        items.forEachIndexed { index, word ->
            if (index > 0 && style == KeyStyle.GALAXY) {
                Box(Modifier.width(1.dp).height(18.dp).background(Color(ColorMath.withAlpha(palette.label, 0.14f))))
            }
            SuggestionItem(word, index == 0, palette, style, actions, Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuggestionItem(
    word: String,
    first: Boolean,
    palette: ThemePalette,
    style: KeyStyle,
    actions: SuggestionActions,
    modifier: Modifier,
) {
    val removeLabel = stringResource(R.string.suggestion_remove_action)
    val fill = suggestionFill(palette, style, first)
    val ink = suggestionInk(palette, style, first, fill)
    val shape = if (style == KeyStyle.DAY) RoundedCornerShape(50) else RoundedCornerShape(0.dp)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = if (style == KeyStyle.DAY) 1.dp else 0.dp)
            .clip(shape)
            .background(Color(fill))
            .combinedClickable(
                role = Role.Button,
                onLongClickLabel = removeLabel,
                onLongClick = { actions.onRemove(word) },
                onClick = { actions.onPick(word) },
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (style == KeyStyle.ECLIPSE && first) Box(Modifier.size(5.dp).clip(CircleShape).background(Color(palette.glow)))
            val body = SpaceTheme.typography.body
            SpaceText(word, if (first) body.copy(fontWeight = FontWeight.SemiBold) else body, color = Color(ink))
        }
    }
}

private fun suggestionFill(palette: ThemePalette, style: KeyStyle, first: Boolean): Int = when {
    style != KeyStyle.DAY -> 0
    first -> ColorMath.mix(palette.keyFill, palette.glow, 0.18f)
    else -> palette.keyFill
}

internal fun suggestionInk(palette: ThemePalette, style: KeyStyle, first: Boolean, fill: Int): Int {
    val base = if (style == KeyStyle.DAY) fill else palette.background
    val emphasis = if (style == KeyStyle.DAY) ColorMath.mix(palette.glow, ColorMath.BLACK, 0.35f) else palette.glow
    val wanted = when {
        first && style == KeyStyle.GALAXY -> palette.label
        first -> emphasis
        else -> ColorMath.withAlpha(palette.label, 0.85f)
    }
    return if (ColorMath.contrast(ColorMath.opaque(wanted), ColorMath.opaque(base)) >= ColorMath.TEXT_MINIMUM) wanted else ColorMath.readableOn(ColorMath.opaque(base), listOf(palette.keyText, palette.label))
}

@Composable
internal fun AutofillBar(views: List<View>) {
    Row(
        modifier = Modifier.fillMaxSize().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        views.forEach { view ->
            AndroidView(factory = { context -> FrameLayout(context).apply { addView(view) } })
        }
    }
}
