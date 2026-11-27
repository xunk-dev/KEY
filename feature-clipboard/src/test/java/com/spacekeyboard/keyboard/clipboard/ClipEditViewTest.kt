package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ClipEditViewTest {
    private fun ready() = ClipSession().withSettings(true, UnlockWindow.FIVE_MINUTES).open(0).record("some long text", false, 1)

    private fun idOf(s: ClipSession) = s.book.items.first().id

    @Test
    fun startingAnEditLeavesTheFullView() {
        val base = ready()
        val id = idOf(base)
        val viewing = base.startView(id, 2)
        assertNotNull(viewing.snapshot(2).viewing)
        assertNull(viewing.startEdit(id, 3).snapshot(3).viewing)
    }

    @Test
    fun panelIsHiddenOnlyInsideTheEditorField() {
        val base = ready()
        val id = idOf(base)
        val editing = base.startEdit(id, 2)
        assertEquals(ClipMode.BROWSE, editing.snapshot(2).mode)
        assertEquals(ClipMode.CLOSED, editing.copy(inEditor = true).snapshot(2).mode)
        assertEquals(ClipMode.BROWSE, editing.endEdit(id).copy(inEditor = true).snapshot(3).mode)
    }
}
