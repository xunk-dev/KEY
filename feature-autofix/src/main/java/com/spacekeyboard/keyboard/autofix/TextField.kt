package com.spacekeyboard.keyboard.autofix

interface Surround {
    fun before(length: Int): String

    fun after(length: Int): String
}

interface TextField : Surround {
    fun commit(text: String)

    fun deleteBefore(length: Int)

    fun deleteLast()

    fun hasSelection(): Boolean

    fun batch(block: () -> Unit)
}

data class Edit(val delete: Int, val insert: String)
