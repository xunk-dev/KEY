package com.spacekeyboard.keyboard.dock

import androidx.annotation.StringRes
import com.spacekeyboard.keyboard.R

internal enum class DockTab(@StringRes val label: Int) {
    HOME(R.string.dock_home),
    CUSTOMIZE(R.string.dock_customize),
    SETTINGS(R.string.dock_settings),
}
