package com.spacekeyboard.keyboard.core.otp

internal object OtpText {
    private const val HIDDEN = "​‌‍‎‏‪‫‬‭‮⁦⁧⁨⁩﻿؜"
    private const val SPACES = "      "
    private val URL = Regex("(?i)\\b(?:https?://|www\\.)\\S+")
    private val DOMAIN = Regex("(?i)\\b[a-z0-9-]+(?:\\.[a-z0-9-]+)*\\.(?:com|net|org|io|co|me|app|ly|gl|link|sa|ae|ps|il|eg|jo|tr|uk)(?:/\\S*)?")
    private val EMAIL = Regex("\\S+@\\S+")
    private val RETRIEVER = Regex("(?s)^\\s*<#>(.*?)\\s+[A-Za-z0-9+/=]{11}\\s*$")

    fun fold(text: String): String = buildString(text.length) {
        for (ch in text) {
            when {
                ch in '٠'..'٩' -> append('0' + (ch - '٠'))
                ch in '۰'..'۹' -> append('0' + (ch - '۰'))
                ch in '０'..'９' -> append('0' + (ch - '０'))
                HIDDEN.indexOf(ch) >= 0 -> Unit
                SPACES.indexOf(ch) >= 0 -> append(' ')
                else -> append(ch)
            }
        }
    }

    fun clean(raw: String): String {
        var text = fold(raw)
        RETRIEVER.find(text)?.let { text = it.groupValues[1] }
        return text.replace(URL, " ").replace(EMAIL, " ").replace(DOMAIN, " ")
    }

    fun validity(text: String, fallback: Long, min: Long, max: Long): Long {
        val hit = OtpVocabulary.VALIDITY.find(text) ?: return fallback
        val amount = hit.groupValues[1].toLongOrNull() ?: return fallback
        val unit = hit.groupValues[2].lowercase()
        val ms = when {
            unit.startsWith("sec") || unit.startsWith("ثاني") || unit == "ثوان" -> amount * 1_000L
            unit.startsWith("hour") || unit.startsWith("hr") || unit.startsWith("heure") || unit.startsWith("ساع") -> amount * 3_600_000L
            else -> amount * 60_000L
        }
        return ms.coerceIn(min, max)
    }

    fun flat(text: String): String = text.filter(Char::isLetterOrDigit)
}
