package com.spacekeyboard.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScreenSaveTest {
    @Test
    fun everyScreenRestoresFromItsName() {
        Screen.entries.filter { it != Screen.SETUP }.forEach {
            assertEquals(it, ScreenSave.restore(it.name, true))
        }
    }

    @Test
    fun missingOrUnknownNameGivesNothing() {
        assertNull(ScreenSave.restore(null, true))
        assertNull(ScreenSave.restore("GONE", true))
    }

    @Test
    fun setupOnlyRestoresWhileItIsNotDone() {
        assertEquals(Screen.SETUP, ScreenSave.restore("SETUP", false))
        assertEquals(Screen.HOME, ScreenSave.restore("SETUP", true))
    }
}
