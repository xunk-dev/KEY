package com.spacekeyboard.keyboard.dock

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val LocalDockInset = compositionLocalOf { 0.dp }

@Composable
internal fun dockContentPadding(base: Dp): PaddingValues =
    PaddingValues(start = base, top = base, end = base, bottom = base + LocalDockInset.current)
