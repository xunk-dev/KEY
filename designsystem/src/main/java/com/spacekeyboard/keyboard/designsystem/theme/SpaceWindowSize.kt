package com.spacekeyboard.keyboard.designsystem.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SpaceWindowSize { Compact, Medium, Expanded }

fun spaceWindowSize(width: Dp): SpaceWindowSize = when {
    width >= 840.dp -> SpaceWindowSize.Expanded
    width >= 600.dp -> SpaceWindowSize.Medium
    else -> SpaceWindowSize.Compact
}
