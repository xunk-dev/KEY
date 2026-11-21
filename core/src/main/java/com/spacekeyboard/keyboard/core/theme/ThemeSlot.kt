package com.spacekeyboard.keyboard.core.theme

enum class ThemeSlot(val key: String) {
    BACKGROUND("background"),
    KEYS("keys"),
    GLOW("glow"),
    BORDER("border"),
}

data class ThemeColors(val background: Int, val keys: Int, val glow: Int, val border: Int) {
    operator fun get(slot: ThemeSlot): Int = when (slot) {
        ThemeSlot.BACKGROUND -> background
        ThemeSlot.KEYS -> keys
        ThemeSlot.GLOW -> glow
        ThemeSlot.BORDER -> border
    }

    fun with(slot: ThemeSlot, argb: Int): ThemeColors = when (slot) {
        ThemeSlot.BACKGROUND -> copy(background = argb)
        ThemeSlot.KEYS -> copy(keys = argb)
        ThemeSlot.GLOW -> copy(glow = argb)
        ThemeSlot.BORDER -> copy(border = argb)
    }
}
