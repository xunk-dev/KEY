package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import kotlin.math.roundToInt

@Composable
fun ValueSlider(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    val colors = SpaceTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth().clearAndSetSemantics { }, horizontalArrangement = Arrangement.SpaceBetween) {
            SpaceText(label, SpaceTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            SpaceText("$value%", SpaceTheme.typography.bodySmall)
        }
        HsvSlider(
            fraction = (value - min).toFloat() / (max - min),
            stops = listOf(colors.containerHigh, colors.primary),
            thumb = colors.primary,
            onChange = { onChange(min + (it * (max - min)).roundToInt()) },
            label = label,
            valueText = "$value%",
            steps = (max - min - 1).coerceAtLeast(0),
        )
    }
}
