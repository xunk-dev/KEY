package com.spacekeyboard.keyboard.core.sound

data class ClickSoundChoice(
    val name: String = "",
    val durationMs: Long = 0L,
    val stamp: Long = 0L,
) {
    val custom: Boolean get() = stamp != 0L

    companion object {
        val SYSTEM = ClickSoundChoice()
    }
}
