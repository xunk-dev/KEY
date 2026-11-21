package com.spacekeyboard.keyboard.core.theme

enum class KeyStyle {
    GALAXY, DAY, ECLIPSE;

    companion object {
        private val DAY_IDS = setOf("daylight", "paper")
        private val ECLIPSE_IDS = setOf("eclipse")

        fun of(theme: ThemeDef): KeyStyle = when (theme.id) {
            in DAY_IDS -> DAY
            in ECLIPSE_IDS -> ECLIPSE
            else -> GALAXY
        }
    }
}
