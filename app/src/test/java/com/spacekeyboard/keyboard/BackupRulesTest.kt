package com.spacekeyboard.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackupRulesTest {
    @Test
    fun noPasswordIsAllowed() {
        assertNull(BackupRules.exportProblem("", ""))
    }

    @Test
    fun shortPasswordIsRefused() {
        assertEquals(R.string.backup_password_short, BackupRules.exportProblem("abc", "abc"))
    }

    @Test
    fun differentRepeatIsRefused() {
        assertEquals(R.string.backup_password_mismatch, BackupRules.exportProblem("long enough", "long enougx"))
    }

    @Test
    fun matchingLongPasswordPasses() {
        assertNull(BackupRules.exportProblem("long enough", "long enough"))
    }
}
