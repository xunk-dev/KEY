package com.spacekeyboard.keyboard.translate

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslateConfigTest {
    private val full = TranslateConfig("https://example.test/translate", "1:2:android:3", "key", "project")

    @Test
    fun fullHttpsConfigIsConfigured() {
        assertTrue(full.configured)
    }

    @Test
    fun anyMissingPartMeansNotConfigured() {
        assertFalse(full.copy(url = "").configured)
        assertFalse(full.copy(appId = " ").configured)
        assertFalse(full.copy(apiKey = "").configured)
        assertFalse(full.copy(projectId = "").configured)
    }

    @Test
    fun plainHttpIsNeverAccepted() {
        assertFalse(full.copy(url = "http://example.test/translate").configured)
    }
}
