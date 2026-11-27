package com.spacekeyboard.keyboard.autofix

sealed interface Slip {
    val pos: Int
    val episode: Episode
}

class Swap(override val pos: Int, override val episode: Episode, val wrong: TypedChar, val intended: Char) : Slip

class Twist(override val pos: Int, override val episode: Episode) : Slip

class Extra(override val pos: Int, override val episode: Episode, val ch: Char) : Slip

class Missing(override val pos: Int, override val episode: Episode, val ch: Char) : Slip
