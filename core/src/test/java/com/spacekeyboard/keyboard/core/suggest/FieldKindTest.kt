package com.spacekeyboard.keyboard.core.suggest

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FieldKindTest {
    private val text = InputType.TYPE_CLASS_TEXT

    @Test
    fun personNameFieldIsName() {
        assertEquals(FieldKind.NAME, FieldKind.of(text or InputType.TYPE_TEXT_VARIATION_PERSON_NAME, 0))
    }

    @Test
    fun postalAddressFieldIsAddress() {
        assertEquals(FieldKind.ADDRESS, FieldKind.of(text or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS, 0))
    }

    @Test
    fun searchActionMakesASearchField() {
        assertEquals(FieldKind.SEARCH, FieldKind.of(text, EditorInfo.IME_ACTION_SEARCH))
    }

    @Test
    fun ordinaryAndMessageFieldsAreProse() {
        assertEquals(FieldKind.PROSE, FieldKind.of(text, EditorInfo.IME_ACTION_SEND))
        assertEquals(FieldKind.PROSE, FieldKind.of(text or InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE, 0))
        assertEquals(FieldKind.PROSE, FieldKind.of(text or InputType.TYPE_TEXT_FLAG_MULTI_LINE, 0))
    }

    @Test
    fun nonTextClassesAreProse() {
        assertEquals(FieldKind.PROSE, FieldKind.of(InputType.TYPE_CLASS_NUMBER, EditorInfo.IME_ACTION_SEARCH))
    }

    @Test
    fun proseScopeFollowsThePackage() {
        assertEquals("app:com.example.chat", FieldKind.PROSE.scope("com.example.chat"))
        assertNull(FieldKind.PROSE.scope(null))
        assertNull(FieldKind.PROSE.scope(""))
    }

    @Test
    fun otherKindsShareOneScopeAcrossApps() {
        assertEquals("kind:name", FieldKind.NAME.scope("com.a"))
        assertEquals("kind:name", FieldKind.NAME.scope("com.b"))
        assertEquals("kind:search", FieldKind.SEARCH.scope(null))
    }
}
