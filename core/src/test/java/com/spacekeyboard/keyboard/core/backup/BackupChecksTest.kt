package com.spacekeyboard.keyboard.core.backup

import com.spacekeyboard.keyboard.core.layout.FakePreferences
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupChecksTest {
    @Test
    fun emptyFilesPassForEveryKnownName() {
        PrefsBackup.ALL.forEach { assertTrue(it, BackupChecks.passes(it, FakePreferences().prefs)) }
    }

    @Test
    fun unknownNamesAreNotChecked() {
        assertTrue(BackupChecks.passes("other", FakePreferences().prefs))
    }

    @Test
    fun wrongTypedValuesFailTheirOwnStore() {
        val themes = FakePreferences()
        themes.prefs.edit().putString("animated", "yes").apply()
        assertFalse(BackupChecks.passes(PrefsBackup.THEME, themes.prefs))
        val shortcuts = FakePreferences()
        shortcuts.prefs.edit().putBoolean("entries", true).apply()
        assertFalse(BackupChecks.passes(PrefsBackup.SHORTCUTS, shortcuts.prefs))
        val words = FakePreferences()
        words.prefs.edit().putInt("stats", 3).apply()
        assertFalse(BackupChecks.passes(PrefsBackup.WORDS, words.prefs))
    }
}
