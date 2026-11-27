package com.spacekeyboard.keyboard.core.backup

import com.spacekeyboard.keyboard.core.apps.AppLanguageStore
import com.spacekeyboard.keyboard.core.layout.FakePreferences
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefsStore
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutStore
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrefsBackupTest {
    private class Device {
        val files = PrefsBackup.ALL.associateWith { FakePreferences() }
        val backup = PrefsBackup({ files.getValue(it).prefs })
    }

    private fun filled(): Device {
        val device = Device()
        KeyboardPrefsStore(device.files.getValue(PrefsBackup.KEYBOARD).prefs).apply {
            setKeySize(120)
            setLanguageEnabled("fa", true)
        }
        ShortcutStore(device.files.getValue(PrefsBackup.SHORTCUTS).prefs).add("brb", "be right back")
        AppLanguageStore(device.files.getValue(PrefsBackup.APPS).prefs).put("com.a", "en")
        return device
    }

    @Test
    fun exportAndImportRoundTrip() {
        val source = filled()
        val target = Device()
        assertEquals(PrefsBackup.Result.OK, target.backup.import(source.backup.export(false)))
        val keyboard = KeyboardPrefsStore(target.files.getValue(PrefsBackup.KEYBOARD).prefs).read()
        assertEquals(120, keyboard.keySizePercent)
        assertTrue("fa" in keyboard.languages)
        assertEquals(mapOf("brb" to "be right back"), ShortcutStore(target.files.getValue(PrefsBackup.SHORTCUTS).prefs).read())
        assertEquals("en", AppLanguageStore(target.files.getValue(PrefsBackup.APPS).prefs).get("com.a"))
    }

    @Test
    fun everyValueTypeSurvivesTheTrip() {
        val source = Device()
        source.files.getValue(PrefsBackup.THEME).prefs.edit()
            .putBoolean("b", true)
            .putInt("i", 7)
            .putLong("l", 9_000_000_000L)
            .putFloat("f", 1.5f)
            .putString("s", "نص")
            .putStringSet("a", setOf("x", "y"))
            .apply()
        val target = Device()
        assertEquals(PrefsBackup.Result.OK, target.backup.import(source.backup.export(false)))
        assertEquals(source.files.getValue(PrefsBackup.THEME).prefs.all, target.files.getValue(PrefsBackup.THEME).prefs.all)
    }

    @Test
    fun featureSettingsAreBackedUpToo() {
        val source = Device()
        source.files.getValue(PrefsBackup.AUTOFIX).prefs.edit().putBoolean("enabled", false).apply()
        source.files.getValue(PrefsBackup.EMOJI).prefs.edit().putString("tone", "dark").apply()
        source.files.getValue(PrefsBackup.DECORATE).prefs.edit().putString("favorites", "a,b").apply()
        val target = Device()
        assertEquals(PrefsBackup.Result.OK, target.backup.import(source.backup.export(false)))
        listOf(PrefsBackup.AUTOFIX, PrefsBackup.EMOJI, PrefsBackup.DECORATE).forEach {
            assertEquals(source.files.getValue(it).prefs.all, target.files.getValue(it).prefs.all)
        }
    }

    @Test
    fun theTranslateConsentAndTheLearnedModelAreNeverPartOfABackup() {
        assertFalse("space_keyboard_translate" in PrefsBackup.ALL)
        assertFalse("space_keyboard_autofix_model" in PrefsBackup.ALL)
        assertFalse("space_keyboard_clipboard" in PrefsBackup.ALL)
    }

    @Test
    fun wordsAreOnlyExportedOnRequest() {
        val device = filled()
        assertFalse(JSONObject(device.backup.export(false)).getJSONObject("files").has(PrefsBackup.WORDS))
        assertTrue(JSONObject(device.backup.export(true)).getJSONObject("files").has(PrefsBackup.WORDS))
    }

    @Test
    fun importLeavesWordsAloneWhenTheyAreNotInTheFile() {
        val target = Device()
        target.files.getValue(PrefsBackup.WORDS).prefs.edit().putString("keep", "me").apply()
        target.backup.import(filled().backup.export(false))
        assertTrue("keep" in target.files.getValue(PrefsBackup.WORDS).keys())
    }

    @Test
    fun importReplacesStaleKeys() {
        val target = Device()
        target.files.getValue(PrefsBackup.KEYBOARD).prefs.edit().putBoolean("stale", true).apply()
        target.backup.import(filled().backup.export(false))
        assertFalse("stale" in target.files.getValue(PrefsBackup.KEYBOARD).keys())
    }

    @Test
    fun wrongTypedValueIsRejectedAndEverythingRollsBack() {
        val target = Device()
        target.files.getValue(PrefsBackup.KEYBOARD).prefs.edit().putBoolean("mine", true).apply()
        AppLanguageStore(target.files.getValue(PrefsBackup.APPS).prefs).put("com.old", "en")
        val bad = """{"version":1,"files":{"space_keyboard":{"zzz":{"t":"b","v":true}},"space_app_languages":{"entries":{"t":"b","v":true}}}}"""
        assertEquals(PrefsBackup.Result.INVALID, target.backup.import(bad))
        val keyboard = target.files.getValue(PrefsBackup.KEYBOARD)
        assertTrue("mine" in keyboard.keys())
        assertFalse("zzz" in keyboard.keys())
        assertEquals("en", AppLanguageStore(target.files.getValue(PrefsBackup.APPS).prefs).get("com.old"))
    }

    @Test
    fun malformedFilesAreInvalid() {
        val device = Device()
        assertEquals(PrefsBackup.Result.INVALID, device.backup.import("not json"))
        assertEquals(PrefsBackup.Result.INVALID, device.backup.import("""{"version":2,"files":{}}"""))
        assertEquals(PrefsBackup.Result.INVALID, device.backup.import("""{"version":1,"files":{}}"""))
        assertEquals(PrefsBackup.Result.INVALID, device.backup.import("""{"version":1,"files":{"space_theme":{"k":{"t":"?","v":1}}}}"""))
    }

    @Test
    fun oversizedTextIsRejectedBeforeParsing() {
        assertEquals(PrefsBackup.Result.TOO_LARGE, Device().backup.import("x".repeat(2_000_001)))
    }
}
