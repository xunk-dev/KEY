package com.spacekeyboard.keyboard.designsystem

import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.parts
import com.spacekeyboard.keyboard.designsystem.theme.BadgeTone
import com.spacekeyboard.keyboard.designsystem.theme.SpaceLook
import com.spacekeyboard.keyboard.designsystem.theme.lookColorScheme
import com.spacekeyboard.keyboard.designsystem.theme.lookStyleFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpaceLookTest {
    @Test
    fun everyIconHasDrawingParts() {
        SpaceIcon.entries.forEach { assertTrue(it.name, it.parts().isNotEmpty()) }
    }

    @Test
    fun keysRoundTrip() {
        SpaceLook.entries.forEach { assertEquals(it, SpaceLook.fromKey(it.key)) }
    }

    @Test
    fun unknownOrMissingKeyIsNull() {
        assertNull(SpaceLook.fromKey("zzz"))
        assertNull(SpaceLook.fromKey(null))
    }

    @Test
    fun systemPicksGalaxyOrDay() {
        assertEquals(SpaceLook.Galaxy, SpaceLook.forSystem(true))
        assertEquals(SpaceLook.Day, SpaceLook.forSystem(false))
    }

    @Test
    fun schemeBrightnessFollowsLook() {
        SpaceLook.entries.forEach { assertEquals(it.name, it.dark, lookColorScheme(it).isDark) }
    }

    @Test
    fun everyLookStyleCoversAllBadgeTones() {
        SpaceLook.entries.forEach { look ->
            val style = lookStyleFor(look)
            assertEquals(look, style.look)
            BadgeTone.entries.forEach { assertTrue("$look $it", style.badges.containsKey(it)) }
        }
    }
}
