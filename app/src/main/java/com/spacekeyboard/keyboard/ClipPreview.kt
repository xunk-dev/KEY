package com.spacekeyboard.keyboard

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.spacekeyboard.keyboard.core.sound.ClipRange
import java.io.IOException

internal class ClipPreview {
    private val handler = Handler(Looper.getMainLooper())
    private var player: MediaPlayer? = null
    private var stopper: Runnable? = null

    fun play(path: String, range: ClipRange, onDone: () -> Unit) {
        stop()
        val next = MediaPlayer()
        player = next
        try {
            next.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            next.setDataSource(path)
            next.setOnErrorListener { _, _, _ ->
                finish(next, onDone)
                true
            }
            next.setOnCompletionListener { finish(next, onDone) }
            next.setOnPreparedListener { begin(it, range, onDone) }
            next.prepareAsync()
        } catch (_: IOException) {
            finish(next, onDone)
        } catch (_: RuntimeException) {
            finish(next, onDone)
        }
    }

    fun stop() {
        val current = player ?: return
        player = null
        drop(current)
    }

    fun release() = stop()

    private fun begin(ready: MediaPlayer, range: ClipRange, onDone: () -> Unit) {
        if (range.startMs <= 0L) {
            launch(ready, range, onDone)
            return
        }
        ready.setOnSeekCompleteListener { launch(it, range, onDone) }
        seekTo(ready, range.startMs)
    }

    private fun launch(ready: MediaPlayer, range: ClipRange, onDone: () -> Unit) {
        if (player !== ready) return
        ready.setOnSeekCompleteListener(null)
        ready.start()
        val job = Runnable { finish(ready, onDone) }
        stopper = job
        handler.postDelayed(job, range.lengthMs)
    }

    private fun seekTo(ready: MediaPlayer, positionMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ready.seekTo(positionMs, MediaPlayer.SEEK_CLOSEST)
        } else {
            ready.seekTo(positionMs.toInt())
        }
    }

    private fun finish(target: MediaPlayer, onDone: () -> Unit) {
        if (player !== target) return
        player = null
        drop(target)
        onDone()
    }

    private fun drop(target: MediaPlayer) {
        stopper?.let(handler::removeCallbacks)
        stopper = null
        runCatching { target.stop() }
        target.release()
    }
}
