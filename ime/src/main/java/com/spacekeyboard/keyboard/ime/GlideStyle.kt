package com.spacekeyboard.keyboard.ime

internal object GlideStyle {
    private const val SENTENCE_ENDS = ".!?؟"

    fun styled(word: String, capital: Boolean, all: Boolean): String = when {
        all -> word.uppercase()
        capital -> word.replaceFirstChar { it.uppercase() }
        else -> word
    }

    fun startsSentence(before: String): Boolean =
        before.trimEnd().let { it.isEmpty() || it.last() in SENTENCE_ENDS }
}
