package com.spacekeyboard.keyboard.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.glass.GlassSurface
import com.spacekeyboard.keyboard.designsystem.theme.SpaceShapes
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun SpaceButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
        shape = SpaceShapes.md,
        glow = SpaceTheme.colors.primary,
    ) {
        SpaceText(
            text = text,
            style = SpaceTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
        )
    }
}
