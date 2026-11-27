package com.spacekeyboard.keyboard.ime

import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutCheck
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutStore

enum class SnippetStep { OFF, TRIGGER, TEXT, DONE }

class SnippetController(
    private val context: Context,
    private val toolsPanel: ShortcutsController,
    private val insert: (String) -> Unit,
    private val resolve: (String) -> String,
    private val changed: () -> Unit,
) {
    private val store = ShortcutStore(context)
    private val handler = Handler(Looper.getMainLooper())
    private val finish = Runnable { moveTo(SnippetStep.OFF) }
    private var fromManager = false
    private var editing: String? = null

    var onChange: (() -> Unit)? = null
    var items by mutableStateOf(store.read().toList())
        private set
    var manager by mutableStateOf(false)
        private set
    var step by mutableStateOf(SnippetStep.OFF)
        private set
    var trigger by mutableStateOf("")
        private set
    var text by mutableStateOf("")
        private set
    var problem by mutableStateOf<ShortcutCheck?>(null)
        private set
    var existing by mutableStateOf<String?>(null)
        private set

    val composing: Boolean get() = step == SnippetStep.TRIGGER || step == SnippetStep.TEXT

    val barShown: Boolean get() = step != SnippetStep.OFF

    val replaces: Boolean get() = step == SnippetStep.TRIGGER && trigger.isNotEmpty() && store.cleanTrigger(trigger) != editing && store.has(trigger)

    fun openManager() {
        items = store.read().toList()
        manager = true
    }

    fun closeManager() {
        manager = false
    }

    fun tools(open: Boolean) {
        if (!open) manager = false
    }

    fun startNew() = begin("", true)

    fun start(prefill: String) = begin(prefill, false)

    fun edit(key: String, value: String) = begin(value, true, key)

    fun insertItem(value: String) {
        toolsPanel.close()
        insert(resolve(value))
    }

    fun remove(key: String) {
        store.remove(key)
        items = store.read().toList()
        changed()
    }

    fun type(value: String) {
        problem = null
        when (step) {
            SnippetStep.TRIGGER -> store.cleanTrigger(trigger + value).takeIf { it.length <= ShortcutStore.MAX_TRIGGER }?.let { trigger = it }
            SnippetStep.TEXT -> (text + value).takeIf { it.length <= ShortcutStore.MAX_TEXT }?.let { text = it }
            else -> Unit
        }
    }

    fun backspace() {
        problem = null
        when (step) {
            SnippetStep.TRIGGER -> trigger = trigger.dropLast(cut(trigger))
            SnippetStep.TEXT -> text = text.dropLast(cut(text))
            else -> Unit
        }
    }

    fun paste() {
        if (step != SnippetStep.TEXT) return
        val clip = context.getSystemService(ClipboardManager::class.java)?.primaryClip
        val pasted = clip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
        if (pasted.isEmpty()) return
        problem = null
        text += pasted
    }

    fun confirm() {
        when (step) {
            SnippetStep.TRIGGER -> if (trigger.isNotEmpty()) afterTrigger()
            SnippetStep.TEXT -> save()
            else -> Unit
        }
    }

    fun cancel() {
        val back = fromManager && step != SnippetStep.OFF
        reset()
        if (back) openManagerAgain()
    }

    fun reset() {
        handler.removeCallbacks(finish)
        trigger = ""
        text = ""
        problem = null
        existing = null
        editing = null
        fromManager = false
        moveTo(SnippetStep.OFF)
    }

    private fun begin(prefill: String, viaManager: Boolean, name: String = "") {
        handler.removeCallbacks(finish)
        fromManager = viaManager
        editing = name.ifEmpty { null }
        existing = if (name.isEmpty()) store.triggerFor(prefill) else null
        trigger = name
        text = prefill
        problem = if (prefill.length > ShortcutStore.MAX_TEXT) ShortcutCheck.TEXT_TOO_LONG else null
        manager = false
        moveTo(SnippetStep.TRIGGER)
        toolsPanel.close()
    }

    private fun afterTrigger() {
        if (text.isBlank() || editing != null) {
            problem = null
            moveTo(SnippetStep.TEXT)
        } else {
            save()
        }
    }

    private fun save() {
        val verdict = store.check(trigger, text)
        problem = verdict.takeIf { it != ShortcutCheck.OK }
        if (problem != null || !store.add(trigger, text)) return
        editing?.takeIf { it != store.cleanTrigger(trigger) }?.let(store::remove)
        items = store.read().toList()
        changed()
        if (fromManager) {
            reset()
            openManagerAgain()
        } else {
            moveTo(SnippetStep.DONE)
            handler.postDelayed(finish, DONE_MS)
        }
    }

    private fun openManagerAgain() {
        manager = true
        toolsPanel.show()
    }

    private fun moveTo(value: SnippetStep) {
        step = value
        onChange?.invoke()
    }

    private fun cut(value: String): Int = if (value.lastOrNull()?.isLowSurrogate() == true) 2 else 1

    private companion object {
        const val DONE_MS = 1800L
    }
}
