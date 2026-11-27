package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyBehaviorTest {
    private fun key(action: KeyAction, alts: List<String> = emptyList()) = KeySpec("k", "a", action, 1f, true, alts)

    @Test
    fun lettersDeleteAndEditCommandsFireOnDown() {
        assertTrue(key(KeyAction.COMMIT).firesOnDown())
        assertTrue(key(KeyAction.DELETE).firesOnDown())
        assertTrue(key(KeyAction.COPY).firesOnDown())
    }

    @Test
    fun spaceAndSpacerWaitForRelease() {
        assertFalse(key(KeyAction.SPACE).firesOnDown())
        assertFalse(key(KeyAction.SPACER).firesOnDown())
    }

    @Test
    fun longPressNeedsAlternativesOrLanguageKey() {
        assertTrue(key(KeyAction.COMMIT, listOf("b")).hasLongPress())
        assertFalse(key(KeyAction.COMMIT).hasLongPress())
        assertTrue(key(KeyAction.LANGUAGE).hasLongPress())
        assertTrue(key(KeyAction.SPACE).hasLongPress())
        assertFalse(key(KeyAction.DELETE).hasLongPress())
    }

    @Test
    fun numberRowKeysAreRecognisedByTheirId() {
        assertTrue(KeySpec("nr_1", "1", KeyAction.COMMIT, 1f, false).isNumberRowKey())
        assertTrue(KeySpec("nr_q", "\u061F", KeyAction.COMMIT, 1f, false).isNumberRowKey())
        assertFalse(key(KeyAction.COMMIT).isNumberRowKey())
    }
}
