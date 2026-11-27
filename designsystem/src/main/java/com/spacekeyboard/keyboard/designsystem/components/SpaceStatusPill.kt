package com.spacekeyboard.keyboard.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private const val PILL_ALPHA = 0.14f

@Composable
fun SpaceStatusPill(text: String, modifier: Modifier = Modifier) {
    val tone = SpaceTheme.colors.success
    Row(
        modifier = modifier
            .background(tone.copy(alpha = PILL_ALPHA), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceIconView(SpaceIcon.CHECK, tone, size = 14.dp)
        SpaceText(text, SpaceTheme.typography.label, color = tone)
    }
}
