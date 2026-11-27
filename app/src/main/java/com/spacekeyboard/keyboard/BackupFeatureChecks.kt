package com.spacekeyboard.keyboard

import android.content.SharedPreferences
import com.spacekeyboard.keyboard.autofix.AutoFixPrefsStore
import com.spacekeyboard.keyboard.core.backup.BackupChecks
import com.spacekeyboard.keyboard.core.backup.PrefsBackup
import com.spacekeyboard.keyboard.decorate.DecorPrefsStore
import com.spacekeyboard.keyboard.emoji.data.EmojiPrefsStore

internal object BackupFeatureChecks {
    fun passes(name: String, prefs: SharedPreferences): Boolean =
        BackupChecks.passes(name, prefs) && runCatching { read(name, prefs) }.isSuccess

    private fun read(name: String, prefs: SharedPreferences) {
        when (name) {
            PrefsBackup.AUTOFIX -> AutoFixPrefsStore(prefs).apply { config(); epoch() }
            PrefsBackup.EMOJI -> EmojiPrefsStore(prefs).apply { recents(); tone(); set() }
            PrefsBackup.DECORATE -> DecorPrefsStore(prefs).apply { favorites(); active() }
            else -> Unit
        }
    }
}
