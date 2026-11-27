package com.spacekeyboard.keyboard.core.theme

import android.content.Context
import android.content.SharedPreferences
import java.io.File

class WallpaperStore(private val prefs: SharedPreferences, private val dir: File) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
        File(context.applicationContext.filesDir, FOLDER),
    )

    val imageFile: File get() = File(dir, "image.jpg")
    val videoFile: File get() = File(dir, "video.mp4")
    val posterFile: File get() = File(dir, "poster.jpg")

    fun stillFile(kind: WallpaperKind): File? = when (kind) {
        WallpaperKind.IMAGE -> imageFile
        WallpaperKind.VIDEO -> posterFile
        WallpaperKind.NONE -> null
    }

    fun read(): WallpaperChoice {
        val kind = runCatching { WallpaperKind.valueOf(prefs.getString(KIND, null).orEmpty()) }
            .getOrDefault(WallpaperKind.NONE)
        val usable = when (kind) {
            WallpaperKind.NONE -> WallpaperKind.NONE
            WallpaperKind.IMAGE -> if (imageFile.isFile) kind else WallpaperKind.NONE
            WallpaperKind.VIDEO -> if (videoFile.isFile && posterFile.isFile) kind else WallpaperKind.NONE
        }
        return WallpaperChoice(
            kind = usable,
            opacity = WallpaperMath.opacity(prefs.getInt(OPACITY, WallpaperChoice.DEFAULT_OPACITY)),
            dim = WallpaperMath.dim(prefs.getInt(DIM, WallpaperChoice.DEFAULT_DIM)),
            stamp = prefs.getLong(STAMP, 0L),
            live = usable == WallpaperKind.VIDEO && prefs.getBoolean(LIVE, false),
            animate = prefs.getBoolean(ANIMATE, true),
        )
    }

    fun workDir(): File = dir.also { it.mkdirs() }

    fun commit(kind: WallpaperKind, live: Boolean = false) {
        val stale = when (kind) {
            WallpaperKind.IMAGE -> listOf(videoFile, posterFile)
            WallpaperKind.VIDEO -> listOf(imageFile)
            WallpaperKind.NONE -> listOf(imageFile, videoFile, posterFile)
        }
        prefs.edit()
            .putString(KIND, kind.name)
            .putBoolean(LIVE, live)
            .putLong(STAMP, System.nanoTime())
            .commit()
        stale.forEach { it.delete() }
    }

    fun clear() = commit(WallpaperKind.NONE)

    fun setOpacity(value: Int) {
        prefs.edit().putInt(OPACITY, WallpaperMath.opacity(value)).apply()
    }

    fun setAnimate(value: Boolean) {
        prefs.edit().putBoolean(ANIMATE, value).apply()
    }

    fun setDim(value: Int) {
        prefs.edit().putInt(DIM, WallpaperMath.dim(value)).apply()
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object {
        const val NAME = "space_wallpaper"
        const val FOLDER = "wallpaper"
        const val KIND = "kind"
        const val OPACITY = "opacity"
        const val DIM = "dim"
        const val STAMP = "stamp"
        const val LIVE = "live"
        const val ANIMATE = "animate"
    }
}
