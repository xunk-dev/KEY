package com.spacekeyboard.keyboard.translate

import com.google.mlkit.common.MlKitException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsSnapshotTest {
    @Test
    fun errorCodesMapToReasons() {
        assertEquals(ModelFailure.NETWORK, ModelFailure.fromCode(MlKitException.NETWORK_ISSUE))
        assertEquals(ModelFailure.STORAGE, ModelFailure.fromCode(MlKitException.NOT_ENOUGH_SPACE))
        assertEquals(ModelFailure.OTHER, ModelFailure.fromCode(null))
        assertEquals(ModelFailure.OTHER, ModelFailure.of(IllegalStateException()))
    }

    @Test
    fun failuresBecomeTranslateFailures() {
        assertEquals(TranslateFailure.NETWORK, ModelFailure.NETWORK.toTranslateFailure())
        assertEquals(TranslateFailure.STORAGE, ModelFailure.STORAGE.toTranslateFailure())
        assertEquals(TranslateFailure.MODEL, ModelFailure.OTHER.toTranslateFailure())
    }

    @Test
    fun aDownloadRunsThenLands() {
        val started = ModelsSnapshot(installed = emptySet()).withStarted("ar", 10L)
        assertTrue(started.downloading("ar"))
        assertFalse(started.isInstalled("ar"))
        val done = started.withSuccess("ar")
        assertTrue(done.isInstalled("ar"))
        assertFalse(done.downloading("ar"))
    }

    @Test
    fun aFailureKeepsItsReasonUntilDismissed() {
        val failed = ModelsSnapshot(installed = emptySet()).withStarted("fr", 1L).withFailure("fr", ModelFailure.NETWORK)
        assertEquals(ModelStatus.Failed(ModelFailure.NETWORK), failed.status["fr"])
        assertNull(failed.withoutStatus("fr").status["fr"])
    }

    @Test
    fun reloadingTheListKeepsRunningDownloadsOfOtherLanguages() {
        val running = ModelsSnapshot().withStarted("de", 1L).withStarted("es", 2L)
        val loaded = running.withLoaded(setOf("es"))
        assertTrue(loaded.downloading("de"))
        assertFalse(loaded.downloading("es"))
        assertTrue(loaded.isInstalled("es"))
        assertFalse(loaded.loadFailed)
    }

    @Test
    fun unknownInstalledListIsNotInstalled() {
        assertFalse(ModelsSnapshot().isInstalled("ar"))
    }
}
