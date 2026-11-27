package com.spacekeyboard.keyboard.core.sound

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StagedPeaksTest {
    private val dir: File = Files.createTempDirectory("peaks").toFile()
    private val file = File(dir, "click_stage.bin")

    @Test
    fun theSidecarSitsNextToTheFile() {
        assertEquals(File(dir, "click_stage.bin.peaks"), StagedPeaks.sidecar(file))
    }

    @Test
    fun peaksRoundTrip() {
        val peaks = floatArrayOf(0f, 0.5f, 1f, 0.125f)
        StagedPeaks.write(file, peaks)
        assertArrayEquals(peaks, StagedPeaks.read(file), 0f)
    }

    @Test
    fun aMissingSidecarReadsAsNull() {
        assertNull(StagedPeaks.read(file))
    }

    @Test
    fun anEmptySidecarReadsAsNull() {
        StagedPeaks.sidecar(file).writeBytes(ByteArray(0))
        assertNull(StagedPeaks.read(file))
    }

    @Test
    fun aTruncatedSidecarReadsAsNull() {
        StagedPeaks.sidecar(file).writeBytes(ByteArray(6))
        assertNull(StagedPeaks.read(file))
    }

    @Test
    fun writingAgainReplacesThePeaks() {
        StagedPeaks.write(file, floatArrayOf(1f, 1f, 1f))
        StagedPeaks.write(file, floatArrayOf(0.25f))
        assertArrayEquals(floatArrayOf(0.25f), StagedPeaks.read(file), 0f)
    }

    @Test
    fun deleteRemovesTheSidecar() {
        StagedPeaks.write(file, floatArrayOf(1f))
        assertTrue(StagedPeaks.sidecar(file).exists())
        StagedPeaks.delete(file)
        assertFalse(StagedPeaks.sidecar(file).exists())
        assertNull(StagedPeaks.read(file))
    }

    @Test
    fun deleteWithoutASidecarIsHarmless() {
        StagedPeaks.delete(file)
        assertNull(StagedPeaks.read(file))
    }
}
