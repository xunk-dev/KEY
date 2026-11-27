package com.spacekeyboard.keyboard.core.layout

import android.content.SharedPreferences
import com.spacekeyboard.keyboard.core.R

enum class KeyFn(val id: String, val action: KeyAction, val labelRes: Int) {
    SYMBOLS("symbols", KeyAction.OPEN_SYMBOLS, R.string.keyfn_symbols),
    NUMBERS("numbers", KeyAction.NUMBERS, R.string.keyfn_numbers),
    LANGUAGE("language", KeyAction.LANGUAGE, R.string.keyfn_language),
    EMOJI("emoji", KeyAction.EMOJI, R.string.keyfn_emoji),
    CLIPBOARD("clipboard", KeyAction.CLIPBOARD, R.string.keyfn_clipboard),
    TRANSLATE("translate", KeyAction.TRANSLATE, R.string.keyfn_translate),
    COMMA("comma", KeyAction.COMMIT, R.string.keyfn_comma),
    PERIOD("period", KeyAction.COMMIT, R.string.keyfn_period);

    fun label(rtl: Boolean): String = when (this) {
        SYMBOLS -> "\u03A9"
        NUMBERS -> "123"
        COMMA -> if (rtl) "\u060C" else ","
        PERIOD -> "."
        else -> ""
    }

    fun replacing(slot: KeySpec, rtl: Boolean): KeySpec {
        val text = label(rtl)
        if (action == KeyAction.COMMIT && text == slot.label && slot.action == KeyAction.COMMIT) return slot
        return KeySpec(slot.id, text, action, slot.weight, slot.movable)
    }

    companion object {
        fun byId(id: String?): KeyFn? = entries.firstOrNull { it.id == id }

        fun isSlot(key: KeySpec): Boolean = key.action != KeyAction.SPACE && key.action != KeyAction.ENTER && key.action != KeyAction.SPACER && key.movable

        fun read(prefs: SharedPreferences, layoutId: String): Map<String, String> =
            prefs.getString(storeKey(layoutId), null).orEmpty().split(PAIR).mapNotNull { part ->
                val at = part.indexOf(EQ)
                if (at <= 0) null else part.substring(0, at) to part.substring(at + 1)
            }.toMap()

        fun write(prefs: SharedPreferences, layoutId: String, keyId: String, fn: KeyFn?) {
            val next = read(prefs, layoutId) - keyId + listOfNotNull(fn?.let { keyId to it.id })
            val editor = prefs.edit()
            if (next.isEmpty()) editor.remove(storeKey(layoutId)) else editor.putString(storeKey(layoutId), next.entries.joinToString(PAIR) { it.key + EQ + it.value })
            editor.apply()
        }

        fun clear(editor: SharedPreferences.Editor, layoutId: String) {
            editor.remove(storeKey(layoutId))
        }

        private fun storeKey(layoutId: String) = "keyfn.$layoutId"

        private const val PAIR = ";"
        private const val EQ = "="
    }
}
