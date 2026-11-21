package com.spacekeyboard.keyboard.designsystem.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.theme.SpaceLook
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun SpaceSectionTitle(text: String, modifier: Modifier = Modifier) {
    val look = SpaceTheme.look
    val style = SpaceTheme.typography.title.copy(fontSize = look.headingSize, fontWeight = look.headingWeight)
    val inset = if (look.look == SpaceLook.Galaxy) 0.dp else 6.dp
    SpaceText(
        text = text,
        style = style,
        color = look.heading,
        modifier = modifier.padding(horizontal = inset).semantics { heading() },
    )
}
