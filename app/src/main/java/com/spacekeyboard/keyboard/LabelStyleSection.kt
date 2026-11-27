package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.LabelFont
import com.spacekeyboard.keyboard.core.layout.LabelStyle
import com.spacekeyboard.keyboard.core.layout.Limits
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.layout.LayoutController

@Composable
fun LabelStyleSection(style: LabelStyle, layouts: LayoutController) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpaceText(stringResource(R.string.section_labels), SpaceTheme.typography.title)
        SpaceText(
            text = stringResource(R.string.labels_note),
            style = SpaceTheme.typography.bodySmall,
            color = SpaceTheme.colors.onSurfaceVariant,
        )
        ChipGrid(LabelFont.entries, style.font, { it.labelRes }, layouts::setFont)
        SwitchRow(stringResource(R.string.label_bold), style.bold, layouts::setBold)
        SwitchRow(stringResource(R.string.label_italic), style.italic, layouts::setItalic)
        ValueSlider(
            label = stringResource(R.string.label_size),
            value = style.scalePercent,
            min = Limits.LABEL_MIN,
            max = Limits.LABEL_MAX,
            onChange = layouts::setLabelScale,
        )
    }
}
