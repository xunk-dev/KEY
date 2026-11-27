package com.spacekeyboard.keyboard.clipboard

enum class EditStatus { OK, UNCHANGED, EMPTY, TOO_LONG, MISSING, LOCKED, UNAVAILABLE }

data class EditOutcome(val status: EditStatus, val merged: Boolean = false)

data class ClipPayload(
    val text: String,
    val sensitive: Boolean,
    val blob: String? = null,
    val length: Long = text.length.toLong(),
)

object ClipEditing {
    fun problem(text: String): EditStatus? = when {
        text.isBlank() -> EditStatus.EMPTY
        text.length > ClipPolicy.MAX_TEXT -> EditStatus.TOO_LONG
        else -> null
    }

    fun canSave(text: String, original: String): Boolean = problem(text) == null && text != original
}

class SelfCopyGuard(private val clock: () -> Long) {
    private var text: String? = null
    private var until = 0L

    fun mark(value: String) {
        text = value
        until = clock() + WINDOW_MS
    }

    fun clear() {
        text = null
    }

    fun consume(value: String): Boolean {
        val hit = text == value && clock() < until
        if (hit || clock() >= until) text = null
        return hit
    }

    private companion object {
        const val WINDOW_MS = 2_000L
    }
}
