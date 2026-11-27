package com.spacekeyboard.keyboard.autofix

data class AutoFixConfig(
    val enabled: Boolean = true,
    val level: AutoFixLevel = AutoFixLevel.BALANCED,
    val live: Boolean = true,
    val words: Boolean = true,
)
