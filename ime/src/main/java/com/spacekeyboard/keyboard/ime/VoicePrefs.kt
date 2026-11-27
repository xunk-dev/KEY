package com.spacekeyboard.keyboard.ime

import android.content.Context
import com.spacekeyboard.keyboard.core.voice.VoiceLanguage

class VoicePrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    var arabicTag: String
        get() = VoiceLanguage.arabicOrDefault(prefs.getString(KEY_ARABIC, null))
        set(value) {
            prefs.edit().putString(KEY_ARABIC, VoiceLanguage.arabicOrDefault(value)).apply()
        }

    private companion object {
        const val FILE = "voice"
        const val KEY_ARABIC = "arabic_tag"
    }
}
