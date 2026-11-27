package com.spacekeyboard.keyboard.core.sound

import android.content.Context
import android.content.SharedPreferences
import java.io.File
import java.io.IOException
import java.io.InputStream

class ClickSoundStore(private val prefs: SharedPreferences, private val dir: File) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
        File(context.applicationContext.filesDir, FOLDER),
    )

    val clipFile: File get() = File(dir, "click.wav")

    fun read(): ClickSoundChoice {
        val stamp = prefs.getLong(STAMP, 0L)
        if (stamp == 0L || !clipFile.isFile) return ClickSoundChoice.SYSTEM
        return ClickSoundChoice(
            name = prefs.getString(LABEL, "").orEmpty(),
            durationMs = prefs.getLong(DURATION, 0L),
            stamp = stamp,
        )
    }

    fun activeStamp(): Long = prefs.getLong(STAMP, 0L)

    fun workDir(): File = dir.also { it.mkdirs() }

    fun commit(label: String, durationMs: Long) {
        prefs.edit()
            .putString(LABEL, label)
            .putLong(DURATION, durationMs)
            .putLong(STAMP, System.currentTimeMillis())
            .commit()
    }

    fun install(source: InputStream, label: String, durationMs: Long): Boolean {
        val temp = File(workDir(), TEMP)
        return try {
            temp.outputStream().use { source.copyTo(it) }
            clipFile.delete()
            val moved = temp.renameTo(clipFile)
            if (moved) commit(label, durationMs)
            moved
        } catch (_: IOException) {
            false
        } finally {
            temp.delete()
        }
    }

    fun clear() {
        prefs.edit().putLong(STAMP, 0L).commit()
        clipFile.delete()
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object {
        const val NAME = "space_click_sound"
        const val FOLDER = "click_sound"
        const val LABEL = "label"
        const val DURATION = "duration"
        const val STAMP = "stamp"
        const val TEMP = "preset.tmp"
    }
}
