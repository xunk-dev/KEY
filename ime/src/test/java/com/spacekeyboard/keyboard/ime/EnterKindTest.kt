package com.spacekeyboard.keyboard.ime

import android.view.inputmethod.EditorInfo
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EnterKindTest {
    @Test
    fun imeActionsMapToKinds() {
        assertEquals(EnterKind.SEARCH, EnterKind.of(EditorInfo.IME_ACTION_SEARCH))
        assertEquals(EnterKind.GO, EnterKind.of(EditorInfo.IME_ACTION_GO))
        assertEquals(EnterKind.SEND, EnterKind.of(EditorInfo.IME_ACTION_SEND))
        assertEquals(EnterKind.NEXT, EnterKind.of(EditorInfo.IME_ACTION_NEXT))
        assertEquals(EnterKind.DONE, EnterKind.of(EditorInfo.IME_ACTION_DONE))
        assertEquals(EnterKind.PREVIOUS, EnterKind.of(EditorInfo.IME_ACTION_PREVIOUS))
        assertEquals(EnterKind.ENTER, EnterKind.of(EditorInfo.IME_ACTION_NONE))
        assertEquals(EnterKind.ENTER, EnterKind.of(EditorInfo.IME_ACTION_UNSPECIFIED))
    }

    @Test
    fun noEnterActionFlagKeepsThePlainEnter() {
        val options = EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION
        assertEquals(EnterKind.ENTER, EnterKind.of(options))
    }

    @Test
    fun enterKeyIconFollowsTheKindAndMirrorsInRtl() {
        assertEquals(SpaceIcon.SEARCH, glyphFor(KeyAction.ENTER, true, false, EnterKind.SEARCH)?.icon)
        assertEquals(SpaceIcon.SEND, glyphFor(KeyAction.ENTER, true, false, EnterKind.SEND)?.icon)
        assertEquals(SpaceIcon.DONE, glyphFor(KeyAction.ENTER, true, false, EnterKind.DONE)?.icon)
        assertEquals(SpaceIcon.ENTER, glyphFor(KeyAction.ENTER, true)?.icon)
        assertFalse(glyphFor(KeyAction.ENTER, true, false, EnterKind.GO)?.mirror == true)
        assertTrue(glyphFor(KeyAction.ENTER, true, true, EnterKind.GO)?.mirror == true)
        assertTrue(glyphFor(KeyAction.ENTER, true, false, EnterKind.NEXT)?.mirror == true)
        assertFalse(glyphFor(KeyAction.ENTER, true, true, EnterKind.NEXT)?.mirror == true)
    }
}
