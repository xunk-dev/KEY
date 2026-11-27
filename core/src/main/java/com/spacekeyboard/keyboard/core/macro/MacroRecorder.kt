package com.spacekeyboard.keyboard.core.macro

import java.text.BreakIterator

class MacroRecorder {
    private val items = ArrayList<MacroStep>()
    private var chars = 0
    private var pastedAt = NONE
    private var pastedText = ""

    val size: Int get() = items.size

    fun steps(): List<MacroStep> = items.toList()

    fun add(step: MacroStep): Boolean = when (step) {
        is MacroStep.Text -> addText(step.text)
        MacroStep.Delete -> addDelete()
        MacroStep.Enter, MacroStep.Send, MacroStep.LiveClip -> addStep(step)
    }

    fun addPasted(text: String): Boolean {
        if (text.isEmpty()) return true
        if (chars + text.length > MAX_CHARS || items.size >= MAX_STEPS) return false
        items.add(MacroStep.Text(text))
        chars += text.length
        pastedAt = items.lastIndex
        pastedText = text
        return true
    }

    fun liveLast(): Boolean {
        val last = items.lastOrNull() as? MacroStep.Text ?: return false
        if (pastedAt != items.lastIndex || last.text != pastedText) return false
        chars -= last.text.length
        items[items.lastIndex] = MacroStep.LiveClip
        pastedAt = NONE
        return true
    }

    private fun addText(text: String): Boolean {
        if (text.isEmpty()) return true
        if (chars + text.length > MAX_CHARS) return false
        val last = items.lastOrNull()
        if (last is MacroStep.Text && last.text.length + text.length <= MAX_JOIN) {
            items[items.lastIndex] = MacroStep.Text(last.text + text)
            chars += text.length
            return true
        }
        if (items.size >= MAX_STEPS) return false
        items.add(MacroStep.Text(text))
        chars += text.length
        return true
    }

    private fun addDelete(): Boolean {
        val last = items.lastOrNull()
        if (last !is MacroStep.Text) return addStep(MacroStep.Delete)
        val kept = last.text.substring(0, lastCharStart(last.text))
        chars -= last.text.length - kept.length
        if (kept.isEmpty()) items.removeAt(items.lastIndex) else items[items.lastIndex] = MacroStep.Text(kept)
        return true
    }

    private fun addStep(step: MacroStep): Boolean {
        if (items.size >= MAX_STEPS) return false
        items.add(step)
        return true
    }

    private fun lastCharStart(text: String): Int {
        val breaks = BreakIterator.getCharacterInstance()
        breaks.setText(text)
        val start = breaks.preceding(text.length)
        return if (start == BreakIterator.DONE) 0 else start
    }

    companion object {
        const val MAX_STEPS = 200
        const val MAX_CHARS = 20_000
        const val MAX_JOIN = 2_000
        private const val NONE = -1
    }
}
