package com.spacekeyboard.keyboard.emoji.data

import android.content.Context
import android.content.SharedPreferences
import com.spacekeyboard.keyboard.emoji.model.Recents
import com.spacekeyboard.keyboard.emoji.model.SkinTone

class EmojiPrefsStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun recents(): List<String> = Recents.decode(prefs.getString(RECENTS, null))

    fun record(text: String) {
        val next = Recents.record(recents(), text)
        prefs.edit().putString(RECENTS, Recents.encode(next)).apply()
    }

    fun clearRecents() {
        prefs.edit().remove(RECENTS).apply()
    }

    fun tone(): SkinTone = SkinTone.byId(prefs.getString(TONE, null))

    fun setTone(tone: SkinTone) {
        prefs.edit().putString(TONE, tone.id).apply()
    }

    fun set(): EmojiSet = EmojiSet.byId(prefs.getString(SET, null))

    fun setSet(set: EmojiSet) {
        prefs.edit().putString(SET, set.id).apply()
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object {
        const val NAME = "space_keyboard_emoji"
        const val RECENTS = "recents"
        const val TONE = "tone"
        const val SET = "set"
    }
}
