package com.spacekeyboard.keyboard.ime

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.core.FieldPolicy
import com.spacekeyboard.keyboard.core.otp.OtpFit
import com.spacekeyboard.keyboard.core.otp.OtpTarget

internal class ImeOtpTarget(
    private val connection: () -> InputConnection?,
    private val editor: () -> EditorInfo?,
    private val extras: TypingExtras,
    private val handler: Handler = Handler(Looper.getMainLooper()),
) : OtpTarget {
    private val startPackage = editor()?.packageName
    private val startType = editor()?.inputType
    private val raw = startType?.let(FieldPolicy::needsRawKeys) == true
    private val keyMap by lazy { KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD) }

    override fun epoch(): Int = extras.fieldEpoch

    override fun alive(): Boolean = connection() != null && editor() != null

    override fun fits(): Boolean {
        val info = editor() ?: return false
        val sameField = info.packageName == startPackage && info.inputType == startType
        return connection() != null && sameField && extras.otpFit != OtpFit.NONE
    }

    override fun read(): String? = connection()?.let(::fieldText)

    override fun cursor(): Int? = extras.cursor

    override fun commit(text: String) {
        val target = connection() ?: return
        if (raw) typeRaw(target, text) else target.commitText(text, 1)
    }

    override fun erase(count: Int) {
        val target = connection() ?: return
        if (raw) repeat(count) { press(target, KeyEvent.KEYCODE_DEL) } else target.deleteSurroundingText(count, 0)
    }

    override fun wipe() {
        val target = connection() ?: return
        if (raw) {
            press(target, KeyEvent.KEYCODE_MOVE_END)
            repeat(RAW_WIPE) { press(target, KeyEvent.KEYCODE_DEL) }
            return
        }
        val before = target.getTextBeforeCursor(WINDOW, 0)?.length ?: 0
        val after = target.getTextAfterCursor(WINDOW, 0)?.length ?: 0
        target.deleteSurroundingText(before, after)
    }

    override fun moveToEnd() {
        connection()?.let { press(it, KeyEvent.KEYCODE_MOVE_END) }
    }

    override fun later(delayMs: Long, block: () -> Unit) {
        handler.postDelayed({ block() }, delayMs)
    }

    private fun typeRaw(target: InputConnection, text: String) {
        val events = keyMap.getEvents(text.toCharArray())
        if (events == null) target.commitText(text, 1) else events.forEach { target.sendKeyEvent(it) }
    }

    private fun press(target: InputConnection, code: Int) {
        val now = SystemClock.uptimeMillis()
        target.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, code, 0))
        target.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, code, 0))
    }

    companion object {
        private const val WINDOW = 64
        private const val RAW_WIPE = 16

        fun fieldText(target: InputConnection): String? {
            val before = target.getTextBeforeCursor(WINDOW, 0)
            val after = target.getTextAfterCursor(WINDOW, 0)
            if (before == null && after == null) return null
            return before?.toString().orEmpty() + after?.toString().orEmpty()
        }
    }
}
