package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.spacekeyboard.keyboard.core.sound.ClickSoundStore
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

class ClickSoundPlayer(context: Context) {
    private val store = ClickSoundStore(context)
    private val audio = context.getSystemService(AudioManager::class.java)
    private val results = ConcurrentHashMap<Int, Int>()
    private val pending = AtomicReference<Float?>(null)

    @Volatile
    private var pool: SoundPool? = null

    @Volatile
    private var soundId = 0
    private var loadedStamp = 0L

    fun play(level: Float): Boolean = when (state()) {
        State.SYSTEM -> false
        State.LOADING -> true
        State.READY -> {
            fire(level)
            true
        }
    }

    fun preview(level: Float) {
        when (state()) {
            State.SYSTEM -> audio?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
            State.READY -> fire(level)
            State.LOADING -> {
                pending.set(level)
                if (results[soundId] == LOADED) flushPending()
            }
        }
    }

    fun release() {
        unload()
        pool?.release()
        pool = null
    }

    private fun state(): State {
        val stamp = store.activeStamp()
        if (stamp == 0L) {
            unload()
            return State.SYSTEM
        }
        if (stamp != loadedStamp) load(stamp)
        return when (results[soundId]) {
            null -> if (soundId != 0) State.LOADING else State.SYSTEM
            LOADED -> State.READY
            else -> State.SYSTEM
        }
    }

    private fun fire(level: Float) {
        pool?.play(soundId, level, level, 1, 0, 1f)
    }

    private fun flushPending() {
        val level = pending.getAndSet(null) ?: return
        fire(level)
    }

    private fun load(stamp: Long) {
        val file = store.clipFile
        val active = pool ?: newPool().also { pool = it }
        unload()
        loadedStamp = stamp
        soundId = if (file.isFile) active.load(file.path, 1) else 0
    }

    private fun unload() {
        pending.set(null)
        if (soundId != 0) pool?.unload(soundId)
        results.remove(soundId)
        soundId = 0
        loadedStamp = 0L
    }

    private fun newPool(): SoundPool {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        return SoundPool.Builder().setMaxStreams(MAX_STREAMS).setAudioAttributes(attributes).build().also {
            it.setOnLoadCompleteListener { _, id, status ->
                results[id] = status
                if (id == soundId && status == LOADED) flushPending()
            }
        }
    }

    private enum class State { SYSTEM, LOADING, READY }

    private companion object {
        const val MAX_STREAMS = 4
        const val LOADED = 0
    }
}
