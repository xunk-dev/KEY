package com.spacekeyboard.keyboard.core.layout

import org.junit.Assert.assertEquals
import org.junit.Test

class KeyboardPrefsTest {
    @Test
    fun sanitizedClampsEveryRange() {
        val wild = KeyboardPrefs(
            keySizePercent = 500,
            heightPercent = 1,
            oneHandPercent = 5,
            splitGapPercent = 99,
            label = LabelStyle(scalePercent = 1000),
            language = "xx",
        ).sanitized()
        assertEquals(Limits.KEY_MAX, wild.keySizePercent)
        assertEquals(Limits.HEIGHT_MIN, wild.heightPercent)
        assertEquals(Limits.ONE_HAND_MIN, wild.oneHandPercent)
        assertEquals(Limits.SPLIT_MAX, wild.splitGapPercent)
        assertEquals(Limits.LABEL_MAX, wild.label.scalePercent)
        assertEquals(LayoutCatalog.default, wild.language)
    }

    @Test
    fun biggerKeysMeanSmallerGaps() {
        assertEquals(10f, KeyboardPrefs(keySizePercent = 80).gapDp(), 0.001f)
        assertEquals(6f, KeyboardPrefs().gapDp(), 0.001f)
        assertEquals(2f, KeyboardPrefs(keySizePercent = 120).gapDp(), 0.001f)
    }

    @Test
    fun rowHeightFollowsThePercent() {
        assertEquals(50f, KeyboardPrefs().rowDp(), 0.001f)
        assertEquals(35f, KeyboardPrefs(heightPercent = 70).rowDp(), 0.001f)
    }

    @Test
    fun modesCycleAndUnknownIdsFallBack() {
        assertEquals(PanelMode.ONE_HAND_RIGHT, PanelMode.NORMAL.next())
        assertEquals(PanelMode.NORMAL, PanelMode.SPLIT.next())
        assertEquals(PanelMode.NORMAL, PanelMode.byId("nope"))
        assertEquals(PanelMode.SPLIT, PanelMode.byId("split"))
        assertEquals(LabelFont.PLEX, LabelFont.byId(null))
    }

    @Test
    fun toolbarParseKeepsOrderDropsUnknownAndAppendsMissing() {
        assertEquals(listOf(ToolbarItem.MODE, ToolbarItem.THEME, ToolbarItem.SYMBOLS, ToolbarItem.EMOJI, ToolbarItem.DECORATE, ToolbarItem.CLIPBOARD, ToolbarItem.TRANSLATE, ToolbarItem.IMAGES, ToolbarItem.VOICE, ToolbarItem.EDIT, ToolbarItem.INCOGNITO, ToolbarItem.SETTINGS), ToolbarItem.parse("mode,ghost,theme,mode"))
        assertEquals(listOf(ToolbarItem.MODE, ToolbarItem.THEME, ToolbarItem.SYMBOLS, ToolbarItem.EMOJI, ToolbarItem.DECORATE, ToolbarItem.CLIPBOARD, ToolbarItem.TRANSLATE, ToolbarItem.IMAGES, ToolbarItem.VOICE, ToolbarItem.EDIT, ToolbarItem.INCOGNITO, ToolbarItem.SETTINGS), ToolbarItem.parse("mode"))
        assertEquals(ToolbarItem.entries, ToolbarItem.parse(null))
        assertEquals(ToolbarItem.entries, ToolbarItem.parse(""))
    }

    @Test
    fun languagesCycle() {
        assertEquals("en", LayoutCatalog.next("ar"))
        assertEquals("ar", LayoutCatalog.next("en"))
        assertEquals("ar", LayoutCatalog.next("unknown"))
    }

    @Test
    fun rowsKeepTheirHeightOnTallScreens() {
        assertEquals(137.5f, KeyboardPrefs().rowPx(5, 2.75f, 2400), 0.001f)
    }

    @Test
    fun rowsShrinkSoLandscapeKeepsRoomForTheApp() {
        assertEquals(97.2f, KeyboardPrefs().rowPx(5, 2.75f, 1080), 0.001f)
    }

    @Test
    fun rowsNeverShrinkBelowTheFloor() {
        assertEquals(93.5f, KeyboardPrefs().rowPx(5, 2.75f, 600), 0.001f)
    }

    @Test
    fun aShortPreferredRowIsNeverRaisedByTheCap() {
        assertEquals(96.25f, KeyboardPrefs(heightPercent = 70).rowPx(5, 2.75f, 2400), 0.001f)
    }

    @Test
    fun gapStaysInsideItsBoundsAtEveryKeySize() {
        (Limits.KEY_MIN..Limits.KEY_MAX).forEach { size ->
            val gap = KeyboardPrefs(keySizePercent = size).gapDp()
            assertEquals(true, gap >= Limits.GAP_MIN_DP && gap <= Limits.GAP_MAX_DP)
        }
        assertEquals(Limits.GAP_MIN_DP, KeyboardPrefs(keySizePercent = Limits.KEY_MAX).gapDp(), 0.001f)
        assertEquals(Limits.GAP_MAX_DP, KeyboardPrefs(keySizePercent = Limits.KEY_MIN).gapDp(), 0.001f)
    }

    @Test
    fun tallestRowIsCappedByTheScreen() {
        val tall = KeyboardPrefs(heightPercent = Limits.HEIGHT_MAX)
        assertEquals(80f, tall.rowDp(), 0.001f)
        assertEquals(80f * 2.5f, tall.rowPx(rows = 4, density = 2.5f, screenPx = 1000 * 5), 0.001f)
        assertEquals(2000f * 0.45f / 5f, tall.rowPx(rows = 5, density = 3f, screenPx = 2000), 0.001f)
    }

    @Test
    fun feedbackLevelsAreClamped() {
        val wild = KeyboardPrefs(feedback = FeedbackPrefs(vibrateStrength = 500, soundVolume = 0)).sanitized()
        assertEquals(Limits.STRENGTH_MAX, wild.feedback.vibrateStrength)
        assertEquals(Limits.VOLUME_MIN, wild.feedback.soundVolume)
    }
}
