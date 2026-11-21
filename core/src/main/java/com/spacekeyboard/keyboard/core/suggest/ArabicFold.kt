package com.spacekeyboard.keyboard.core.suggest

object ArabicFold {
    fun fold(text: String): String {
        val out = StringBuilder(text.length)
        for (ch in text.lowercase()) {
            when {
                ch in MARKS_START..MARKS_END || ch == SUPERSCRIPT_ALEF || ch == TATWEEL -> Unit
                ch == 'أ' || ch == 'إ' || ch == 'آ' || ch == 'ٱ' -> out.append('ا')
                ch == 'ى' || ch == 'ی' -> out.append('ي')
                ch == 'ة' -> out.append('ه')
                ch == 'ک' -> out.append('ك')
                else -> out.append(ch)
            }
        }
        return out.toString()
    }

    private const val MARKS_START = 'ً'
    private const val MARKS_END = 'ٟ'
    private const val SUPERSCRIPT_ALEF = 'ٰ'
    private const val TATWEEL = 'ـ'
}
