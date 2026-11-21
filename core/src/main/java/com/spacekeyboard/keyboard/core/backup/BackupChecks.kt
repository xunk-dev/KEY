package com.spacekeyboard.keyboard.core.backup

import android.content.SharedPreferences
import com.spacekeyboard.keyboard.core.apps.AppLanguageStore
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefsStore
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutStore
import com.spacekeyboard.keyboard.core.suggest.SuggestionStore
import com.spacekeyboard.keyboard.core.theme.ThemeStore

object BackupChecks {
    fun passes(name: String, prefs: SharedPreferences): Boolean = runCatching {
        when (name) {
            PrefsBackup.THEME -> ThemeStore(prefs).read()
            PrefsBackup.KEYBOARD -> KeyboardPrefsStore(prefs).read()
            PrefsBackup.SHORTCUTS -> ShortcutStore(prefs).read()
            PrefsBackup.APPS -> AppLanguageStore(prefs).size()
            PrefsBackup.WORDS -> SuggestionStore(prefs).load()
            else -> Unit
        }
    }.isSuccess
}
