package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.dock.DockTab

internal val sharedScreens = setOf(Screen.DICTIONARY, Screen.PRIVACY, Screen.BACKUP)

internal fun Screen.dockTab(origin: Screen = Screen.HOME): DockTab = when (this) {
    Screen.CUSTOMIZE, Screen.KEYBOARD_COLORS, Screen.KEYBOARD_LOOK, Screen.EDITOR -> DockTab.CUSTOMIZE
    Screen.SETTINGS, Screen.SETTINGS_TYPING, Screen.SETTINGS_TOUCH, Screen.SETTINGS_DATA -> DockTab.SETTINGS
    in sharedScreens -> origin.dockTab()
    else -> DockTab.HOME
}

internal fun DockTab.screen(): Screen = when (this) {
    DockTab.HOME -> Screen.HOME
    DockTab.CUSTOMIZE -> Screen.CUSTOMIZE
    DockTab.SETTINGS -> Screen.SETTINGS
}

internal fun Screen.parent(origin: Screen): Screen = when (this) {
    Screen.KEYBOARD_COLORS, Screen.KEYBOARD_LOOK, Screen.EDITOR -> Screen.CUSTOMIZE
    Screen.SETTINGS_TYPING, Screen.SETTINGS_TOUCH, Screen.SETTINGS_DATA -> Screen.SETTINGS
    in sharedScreens -> origin
    else -> Screen.HOME
}

internal fun Screen.showsDock(): Boolean = this != Screen.SETUP && this != Screen.EDITOR && this != Screen.CUSTOMIZE
