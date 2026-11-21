package com.spacekeyboard.keyboard

internal object ScreenSave {
    const val KEY = "space_screen"

    fun restore(saved: String?, setupDone: Boolean): Screen? {
        val screen = Screen.entries.firstOrNull { it.name == saved } ?: return null
        return if (screen == Screen.SETUP && setupDone) Screen.HOME else screen
    }
}
