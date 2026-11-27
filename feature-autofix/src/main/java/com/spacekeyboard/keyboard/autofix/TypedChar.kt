package com.spacekeyboard.keyboard.autofix

class Applied(val viaContext: Boolean, val prefix: String)

class TypedChar(
    val ch: Char,
    val key: Char,
    val zone: Int,
    val retyped: Boolean = false,
    val applied: Applied? = null,
    val watch: String? = null,
) {
    val raw: Boolean get() = applied == null && !retyped
}
