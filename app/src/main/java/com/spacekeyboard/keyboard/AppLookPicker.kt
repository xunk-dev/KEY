package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceLook
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.lookColorScheme

internal val appLookOptions: List<SpaceLook?> = listOf(SpaceLook.Galaxy, SpaceLook.Day, SpaceLook.Eclipse, null)

internal fun appLookLabel(look: SpaceLook?): Int = when (look) {
    SpaceLook.Galaxy -> R.string.app_look_galaxy
    SpaceLook.Day -> R.string.app_look_day
    SpaceLook.Eclipse -> R.string.app_look_eclipse
    null -> R.string.app_look_auto
}

@Composable
internal fun AppLookPicker() {
    val context = LocalContext.current
    val saved by AppLookStore.current(context)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        appLookOptions.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { option -> LookOption(option, option == saved) { AppLookStore.write(context, option) } }
            }
        }
    }
}

@Composable
private fun RowScope.LookOption(look: SpaceLook?, selected: Boolean, onSelect: () -> Unit) {
    val style = SpaceTheme.look
    val shape = RoundedCornerShape(style.cardRadius.coerceAtMost(18.dp))
    val edge = if (selected) SpaceTheme.colors.primary else SpaceTheme.colors.outline
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(style.card)
            .border(if (selected) 2.dp else 1.dp, edge, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Swatch(look)
        OptionLabel(look, selected)
    }
}

@Composable
private fun OptionLabel(look: SpaceLook?, selected: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceText(stringResource(appLookLabel(look)), SpaceTheme.typography.titleSmall, Modifier.weight(1f))
        if (selected) SpaceIconView(SpaceIcon.CHECK, SpaceTheme.colors.primary, size = 18.dp)
    }
}

@Composable
private fun Swatch(look: SpaceLook?) {
    val shown = if (look == null) listOf(SpaceLook.Galaxy, SpaceLook.Day) else listOf(look)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(10.dp)),
    ) {
        shown.forEach { SwatchHalf(it, Modifier.weight(1f)) }
    }
}

@Composable
private fun SwatchHalf(look: SpaceLook, modifier: Modifier) {
    val scheme = lookColorScheme(look)
    Box(modifier.fillMaxHeight().background(scheme.background), contentAlignment = Alignment.Center) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(width = 22.dp, height = 14.dp).clip(RoundedCornerShape(5.dp)).background(scheme.surface))
            Box(Modifier.size(14.dp).clip(CircleShape).background(scheme.primary))
        }
    }
}
