package com.spacekeyboard.keyboard.core.vault

interface TextCipher {
    fun encrypt(plain: String): String

    fun decrypt(encoded: String): String?
}
