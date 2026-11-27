package com.spacekeyboard.keyboard

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.sound.ClickImportError
import com.spacekeyboard.keyboard.core.sound.ClickSoundImport
import com.spacekeyboard.keyboard.core.sound.ClipRange
import com.spacekeyboard.keyboard.core.sound.StageResult
import com.spacekeyboard.keyboard.core.sound.StagedSound

@Stable
internal class SoundImportState(private val importer: ClickSoundImport) {
    var busy by mutableStateOf(false)
        private set
    var error by mutableStateOf<Int?>(null)
        private set
    var staged by mutableStateOf<StagedSound?>(null)
        private set

    fun pick(uri: Uri) {
        if (busy) return
        busy = true
        error = null
        Thread({ finishStage(importer.stage(uri)) }, "click-sound-stage").start()
    }

    fun confirm(range: ClipRange) {
        val sound = staged ?: return
        if (busy) return
        busy = true
        Thread({ finishSave(sound, importer.save(sound, range)) }, "click-sound-save").start()
    }

    fun cancel() {
        staged?.let(importer::discard)
        staged = null
        error = null
    }

    fun clearError() {
        error = null
    }

    private fun finishStage(result: StageResult) {
        when (result) {
            is StageResult.Ready -> staged = result.sound
            is StageResult.Failed -> error = errorText(result.error)
        }
        busy = false
    }

    private fun finishSave(sound: StagedSound, failure: ClickImportError?) {
        error = failure?.let { errorText(it) }
        if (failure != ClickImportError.SILENT_PART) {
            importer.discard(sound)
            staged = null
        }
        busy = false
    }

    private fun reopen(saved: List<Any>) {
        val name = saved.getOrNull(0) as? String ?: return
        val duration = saved.getOrNull(1) as? Long ?: return
        staged = importer.restore(name, duration)
    }

    private fun errorText(error: ClickImportError): Int = when (error) {
        ClickImportError.TOO_LARGE -> R.string.sound_err_large
        ClickImportError.TOO_LONG -> R.string.sound_err_long
        ClickImportError.UNSUPPORTED -> R.string.sound_err_unsupported
        ClickImportError.SILENT -> R.string.sound_err_silent
        ClickImportError.SILENT_PART -> R.string.sound_err_silent_part
    }

    companion object {
        fun saver(importer: ClickSoundImport): Saver<SoundImportState, Any> = listSaver<SoundImportState, Any>(
            save = { state -> state.staged?.let { listOf(it.name, it.durationMs) } ?: emptyList() },
            restore = { saved -> SoundImportState(importer).also { it.reopen(saved) } },
        )
    }
}
