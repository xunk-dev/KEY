package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.macro.Macro
import com.spacekeyboard.keyboard.core.macro.MacroCursor
import com.spacekeyboard.keyboard.core.macro.MacroRecorder
import com.spacekeyboard.keyboard.core.macro.MacroRepeat
import com.spacekeyboard.keyboard.core.macro.MacroSpeed
import com.spacekeyboard.keyboard.core.macro.MacroStep
import com.spacekeyboard.keyboard.core.macro.MacroStore

enum class MacroMode { IDLE, RECORDING, PLAYING, PAUSED }

class MacroController(
    context: Context,
    private val perform: (MacroStep) -> Unit,
    private val ready: () -> Boolean,
    private val sensitive: () -> Boolean,
    private val clipText: () -> String?,
) {
    private val app = context.applicationContext
    private val store = MacroStore(context)
    private val handler = Handler(Looper.getMainLooper())
    private var recorder = MacroRecorder()
    private var cursor = MacroCursor(emptyList())
    private var limit = 0
    private var pendingPaste = false
    private val tick = Runnable { step() }

    var onChange: (() -> Unit)? = null
    var mode by mutableStateOf(MacroMode.IDLE)
        private set
    var recorded by mutableIntStateOf(0)
        private set
    var position by mutableIntStateOf(0)
        private set
    var total by mutableIntStateOf(0)
        private set
    var round by mutableIntStateOf(1)
        private set
    var rounds by mutableIntStateOf(0)
        private set
    var macros by mutableStateOf(store.all())
        private set
    var favorite by mutableStateOf(store.favorite())
        private set
    var speed by mutableStateOf(store.speed)
        private set
    var repeat by mutableStateOf(store.repeat)
        private set
    var manager by mutableStateOf(false)
        private set
    var full by mutableStateOf(false)
        private set
    var canLive by mutableStateOf(false)
        private set

    val active: Boolean get() = mode != MacroMode.IDLE

    fun reload() {
        macros = store.all()
        favorite = store.favorite()
    }

    fun openManager() {
        reload()
        manager = true
    }

    fun closeManager() {
        manager = false
    }

    fun tools(open: Boolean) {
        if (!open) manager = false
    }

    fun markPaste(block: () -> Unit) {
        pendingPaste = mode == MacroMode.RECORDING
        try {
            block()
        } finally {
            pendingPaste = false
        }
    }

    fun note(step: MacroStep) {
        if (mode != MacroMode.RECORDING || sensitive()) return
        val text = (step as? MacroStep.Text)?.text
        val pasted = pendingPaste && text != null
        val added = if (pasted && text != null) recorder.addPasted(text) else recorder.add(step)
        canLive = added && pasted && text.orEmpty().isNotEmpty()
        if (added) recorded = recorder.size else full = true
    }

    fun noteText(text: String) = note(MacroStep.Text(text))

    fun makeLive() {
        if (canLive && recorder.liveLast()) canLive = false
    }

    fun startRecording() {
        halt()
        recorder = MacroRecorder()
        recorded = 0
        full = false
        canLive = false
        manager = false
        changeMode(MacroMode.RECORDING)
    }

    fun finishRecording() {
        if (mode != MacroMode.RECORDING) return
        store.add(recorder.steps())
        canLive = false
        reload()
        changeMode(MacroMode.IDLE)
    }

    fun play(macro: Macro) {
        if (!ready()) return
        halt()
        cursor = MacroCursor(macro.steps)
        limit = repeat.rounds
        rounds = limit
        total = macro.steps.size
        position = 0
        round = 1
        manager = false
        changeMode(MacroMode.PLAYING)
        handler.postDelayed(tick, START_DELAY_MS)
    }

    fun playFavorite() {
        val target = favorite ?: return
        if (!active) play(target)
    }

    fun pause() {
        if (mode != MacroMode.PLAYING) return
        handler.removeCallbacks(tick)
        changeMode(MacroMode.PAUSED)
    }

    fun resume() {
        if (mode != MacroMode.PAUSED) return
        changeMode(MacroMode.PLAYING)
        handler.postDelayed(tick, START_DELAY_MS)
    }

    fun cycleSpeed() = chooseSpeed(speed.next())

    fun chooseSpeed(value: MacroSpeed) {
        speed = value
        store.speed = value
    }

    fun chooseRepeat(value: MacroRepeat) {
        repeat = value
        store.repeat = value
    }

    fun delete(id: Long) {
        store.delete(id)
        reload()
    }

    fun toggleFavorite(id: Long) {
        store.setFavorite(if (store.favoriteId == id) null else id)
        reload()
    }

    fun askRename(id: Long) {
        val intent = Intent(app, MacroRenameActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(MacroRenameActivity.EXTRA_ID, id)
        runCatching { app.startActivity(intent) }
    }

    fun halt() {
        handler.removeCallbacks(tick)
        if (mode == MacroMode.RECORDING) finishRecording()
        if (mode != MacroMode.IDLE) changeMode(MacroMode.IDLE)
    }

    private fun step() {
        if (mode != MacroMode.PLAYING) return
        val next = cursor.next()
        if (next == null || !ready()) return halt()
        execute(next)
        position = cursor.index
        if (cursor.finished(limit)) return halt()
        round = cursor.round
        handler.postDelayed(tick, if (cursor.atRoundStart) speed.roundMs else speed.stepMs)
    }

    private fun execute(step: MacroStep) {
        if (step != MacroStep.LiveClip) return perform(step)
        if (sensitive()) return
        val text = clipText()
        if (!text.isNullOrEmpty()) perform(MacroStep.Text(text))
    }

    private fun changeMode(value: MacroMode) {
        mode = value
        onChange?.invoke()
    }

    private companion object {
        const val START_DELAY_MS = 350L
    }
}
