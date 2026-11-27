package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.Hsv
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun ColorEditor(argb: Int, onChange: (Int) -> Unit) {
    val holder = remember { arrayOf(Hsv.fromArgb(argb)) }
    if (holder[0].toArgb() != ColorMath.opaque(argb)) holder[0] = Hsv.fromArgb(argb)
    val hsv = holder[0]
    val hueStops = remember { List(7) { Color(Hsv(it * 60f, 1f, 1f).toArgb()) } }
    val saturationStops = listOf(Color(hsv.with(saturation = 0f).toArgb()), Color(hsv.with(saturation = 1f).toArgb()))
    val valueStops = listOf(Color(hsv.with(value = 0f).toArgb()), Color(hsv.with(value = 1f).toArgb()))
    val thumb = Color(argb)
    fun update(next: Hsv) {
        holder[0] = next
        onChange(next.toArgb())
    }
    LookCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Labeled(R.string.color_hue)
            HsvSlider(hsv.hue / 360f, hueStops, thumb, { update(hsv.with(hue = it * 360f)) }, label = stringResource(R.string.color_hue))
            Labeled(R.string.color_saturation)
            HsvSlider(hsv.saturation, saturationStops, thumb, { update(hsv.with(saturation = it)) }, label = stringResource(R.string.color_saturation))
            Labeled(R.string.color_brightness)
            HsvSlider(hsv.value, valueStops, thumb, { update(hsv.with(value = it)) }, label = stringResource(R.string.color_brightness))
        }
    }
}

@Composable
private fun Labeled(label: Int) {
    SpaceText(
        text = stringResource(label),
        style = SpaceTheme.typography.bodySmall,
        color = SpaceTheme.colors.onSurfaceVariant,
        modifier = Modifier.clearAndSetSemantics { },
    )
}
