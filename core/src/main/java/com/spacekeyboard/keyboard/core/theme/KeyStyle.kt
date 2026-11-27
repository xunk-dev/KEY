package com.spacekeyboard.keyboard.core.theme

enum class KeyStyle {
    GALAXY, DAY, ECLIPSE, GLASS;

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun of(theme: ThemeDef): KeyStyle = GLASS
    }
}
