package com.spacekeyboard.keyboard.ime

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.autofix.AutoFixController
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeyLabels
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.Shift

internal class KeyActions(
    private val layouts: LayoutController,
    private val router: SearchKeyRouter,
    private val typer: KeyTyper,
    private val autofix: AutoFixController,
    private val suggest: SuggestionController,
    private val connection: () -> InputConnection?,
    private val editor: () -> EditorInfo?,
    private val zone: () -> Int,
    private val switchKeyboard: () -> Unit,
    private val startVoice: () -> Unit,
    private val openTool: (KeyAction) -> Unit,
) {
    private val edits = EditCommands(layouts, connection)

    private var voiceSession = false

    fun key(key: KeySpec) {
        voiceSession = false
        val typed = key.action == KeyAction.COMMIT
        val resolved = if (typed) key.copy(label = KeyLabels.output(shownText(key))) else key
        if (router.route(resolved)) {
            if (typed) layouts.consumeShift()
            return
        }
        perform(key, resolved)
        if (typed) layouts.consumeShift()
        suggest.update(typer.extras, key.action == KeyAction.SPACE || key.action == KeyAction.ENTER)
        if (key.action == KeyAction.DELETE || key.action == KeyAction.DELETE_WORD) return
        if (typed || key.action in EDITING) updateAutoCap()
    }

    fun longPress(key: KeySpec): Boolean = when (key.action) {
        KeyAction.LANGUAGE -> true.also { switchKeyboard() }
        KeyAction.SPACE -> true.also {
            voiceSession = true
            startVoice()
        }
        KeyAction.ENTER -> (!router.capturing() && typer.ready()).also {
            if (!it) return@also
            if (layouts.snapshot.enterKind == EnterKind.ENTER) typer.send() else typer.newline(isMultiline())
        }
        else -> false
    }

    private fun isMultiline(): Boolean =
        ((editor()?.inputType ?: 0) and InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0

    private fun shownText(key: KeySpec): String =
        if (key.id == KeyTiming.ALT_ID) key.label else layouts.snapshot.shown(key)

    fun updateAutoCap() {
        suggest.update(typer.extras, false)
        if (voiceSession) return
        val snapshot = layouts.snapshot
        if (!snapshot.prefs.typing.autoCapitalize) return
        if (snapshot.numbers || snapshot.language != "en" || snapshot.shift == Shift.LOCKED) return
        val info = editor() ?: return
        val mode = connection()?.getCursorCapsMode(info.inputType) ?: 0
        layouts.setShift(if (mode != 0) Shift.ONCE else Shift.OFF)
    }

    private fun perform(key: KeySpec, resolved: KeySpec) {
        when (key.action) {
            KeyAction.COMMIT -> typer.commit(resolved.label, zone())
            KeyAction.SPACE -> typer.space()
            KeyAction.DELETE -> typer.delete()
            KeyAction.DELETE_WORD -> if (key.id == KeyTiming.REPLACE_ID) typer.replaceLast(key.label) else typer.deleteWord()
            KeyAction.ENTER -> typer.enter()
            KeyAction.LANGUAGE -> layouts.switchLanguage()
            KeyAction.EXPAND -> layouts.setMode(PanelMode.NORMAL)
            KeyAction.FLIP -> layouts.flipSide()
            KeyAction.SHIFT -> layouts.tapShift()
            KeyAction.NUMBERS -> layouts.toggleNumbers()
            KeyAction.SYMBOLS -> layouts.toggleSymbols()
            KeyAction.OPEN_SYMBOLS -> layouts.openSymbols()
            KeyAction.EDIT -> toggleEdit()
            KeyAction.EMOJI, KeyAction.CLIPBOARD, KeyAction.TRANSLATE -> openTool(key.action)
            KeyAction.SPACER -> Unit
            else -> editCommand(key.action)
        }
    }

    private fun toggleEdit() {
        typer.settle()
        layouts.toggleEdit()
    }

    private fun editCommand(action: KeyAction) {
        if (!action.isEditCommand) return
        typer.settle()
        edits.run(action)
    }

    private companion object {
        val EDITING = setOf(KeyAction.SPACE, KeyAction.DELETE, KeyAction.DELETE_WORD, KeyAction.ENTER)
    }
}
