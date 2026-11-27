package com.spacekeyboard.keyboard.core.theme

import com.spacekeyboard.keyboard.core.layout.FakePreferences
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WallpaperTest {
    private val fake = FakePreferences()
    private val dir: File = Files.createTempDirectory("wallpaper").toFile()
    private val store = WallpaperStore(fake.prefs, dir)

    private fun put(file: File) {
        dir.mkdirs()
        file.writeText("x")
    }

    @Test
    fun emptyStoreHasNoWallpaperAndDefaultLook() {
        val choice = store.read()
        assertFalse(choice.active)
        assertEquals(WallpaperChoice.DEFAULT_OPACITY, choice.opacity)
        assertEquals(WallpaperChoice.DEFAULT_DIM, choice.dim)
    }

    @Test
    fun committedImageIsActiveAndDropsTheVideoFiles() {
        put(store.videoFile)
        put(store.posterFile)
        put(store.imageFile)
        store.commit(WallpaperKind.IMAGE)
        assertEquals(WallpaperKind.IMAGE, store.read().kind)
        assertFalse(store.videoFile.exists())
        assertFalse(store.posterFile.exists())
    }

    @Test
    fun videoNeedsBothTheClipAndItsStill() {
        put(store.videoFile)
        store.commit(WallpaperKind.VIDEO)
        assertEquals(WallpaperKind.NONE, store.read().kind)
        put(store.posterFile)
        assertEquals(WallpaperKind.VIDEO, store.read().kind)
    }

    @Test
    fun missingFileFallsBackToNone() {
        put(store.imageFile)
        store.commit(WallpaperKind.IMAGE)
        store.imageFile.delete()
        assertFalse(store.read().active)
    }

    @Test
    fun everyCommitChangesTheStamp() {
        put(store.imageFile)
        store.commit(WallpaperKind.IMAGE)
        val first = store.read().stamp
        store.commit(WallpaperKind.IMAGE)
        assertNotEquals(first, store.read().stamp)
    }

    @Test
    fun clearRemovesEveryFile() {
        put(store.imageFile)
        put(store.videoFile)
        put(store.posterFile)
        store.clear()
        assertFalse(store.imageFile.exists() || store.videoFile.exists() || store.posterFile.exists())
        assertFalse(store.read().active)
    }

    @Test
    fun opacityAndDimAreClamped() {
        store.setOpacity(0)
        store.setDim(500)
        val choice = store.read()
        assertEquals(WallpaperChoice.MIN_OPACITY, choice.opacity)
        assertEquals(WallpaperChoice.MAX_DIM, choice.dim)
        store.setOpacity(999)
        assertEquals(WallpaperChoice.MAX_OPACITY, store.read().opacity)
    }

    @Test
    fun sampleSizeKeepsTheLongestSideAboveTheTarget() {
        assertEquals(1, WallpaperMath.sampleSize(1000, 800, 1280))
        assertEquals(2, WallpaperMath.sampleSize(4000, 3000, 1280))
        assertEquals(4, WallpaperMath.sampleSize(6000, 4000, 1280))
        assertEquals(1, WallpaperMath.sampleSize(0, 0, 1280))
    }

    @Test
    fun fitNeverUpscalesAndKeepsTheRatio() {
        assertEquals(800 to 600, WallpaperMath.fit(800, 600, 1280))
        assertEquals(1280 to 720, WallpaperMath.fit(2560, 1440, 1280))
        assertEquals(720 to 1280, WallpaperMath.fit(1440, 2560, 1280))
    }

    @Test
    fun brightnessFollowsTheDimLevel() {
        assertEquals(1f, WallpaperChoice(dim = 0).brightness, 0.001f)
        assertEquals(0.2f, WallpaperChoice(dim = 80).brightness, 0.001f)
        assertTrue(WallpaperChoice(opacity = 50).alpha == 0.5f)
    }

    @Test
    fun liveFlagSurvivesOnlyForVideoAndResetsOnNextCommit() {
        put(store.videoFile)
        put(store.posterFile)
        store.commit(WallpaperKind.VIDEO, live = true)
        assertTrue(store.read().live)
        store.commit(WallpaperKind.VIDEO)
        assertFalse(store.read().live)
        store.commit(WallpaperKind.VIDEO, live = true)
        store.videoFile.delete()
        assertFalse(store.read().live)
    }

    @Test
    fun motionPlaysByDefaultAndCanBeSwitchedOff() {
        assertTrue(store.read().animate)
        store.setAnimate(false)
        assertFalse(store.read().animate)
    }
}
