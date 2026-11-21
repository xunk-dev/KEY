package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.theme.SpaceLook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLookOptionsTest {
    @Test
    fun everyLookIsOfferedOnce() {
        SpaceLook.entries.forEach { look ->
            assertEquals(1, appLookOptions.count { it == look })
        }
    }

    @Test
    fun automaticComesLast() {
        assertNull(appLookOptions.last())
        assertEquals(SpaceLook.entries.size + 1, appLookOptions.size)
    }

    @Test
    fun everyOptionHasItsOwnLabel() {
        val labels = appLookOptions.map { appLookLabel(it) }
        assertEquals(labels.size, labels.toSet().size)
        assertTrue(labels.all { it != 0 })
    }
}
