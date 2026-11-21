package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.spacekeyboard.keyboard.autofix.AutoFixController
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.core.FieldPolicy
import com.spacekeyboard.keyboard.core.apps.AppLanguageStore
import com.spacekeyboard.keyboard.core.apps.LanguageMemory
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutBook
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutStore
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.LayoutSnapshot
import com.spacekeyboard.keyboard.ime.layout.Shift
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.translate.TranslateController

internal class FieldSetup(
    private val layouts: LayoutController,
    private val emoji: EmojiController,
    private val decor: DecorController,
    private val clip: ClipboardController,
    private val translate: TranslateController,
    private val images: ImagesController,
    private val autofix: AutoFixController,
    private val typer: KeyTyper,
    private val context: Context,
    private val showPreviews: (Boolean) -> Unit,
) {
    private val shortcuts = ShortcutStore(context)
    private val memory = LanguageMemory(
        AppLanguageStore(context),
        { layouts.snapshot.prefs.language },
        { layouts.snapshot.prefs.typing.rememberLanguage },
        layouts::selectLanguage,
    )
    private var inputType = 0
    private var options = 0
    private var incognito = false
    private var plainField = false

    init {
        layouts.subscribe(::onLayout)
    }

    fun start(info: EditorInfo?, restarting: Boolean) {
        inputType = info?.inputType ?: 0
        options = info?.imeOptions ?: 0
        val sensitive = FieldPolicy.isSensitive(inputType)
        val rich = !sensitive && !FieldPolicy.needsRawKeys(inputType)
        emoji.setAllowed(FieldPolicy.acceptsEmoji(inputType))
        decor.setAllowed(!sensitive && FieldPolicy.acceptsEmoji(inputType))
        clip.setAllowed(!sensitive)
        translate.setAllowed(rich)
        images.setAllowed(rich)
        showPreviews(!sensitive)
        applyField()
        memory.begin(info?.packageName, !sensitive && !incognito)
        layouts.setEnterLabel(enterLabel(info))
        if (!restarting) openFresh(inputType)
    }

    fun end() = memory.end()

    private fun applyField() {
        incognito = layouts.snapshot.prefs.typing.incognito
        val learns = !incognito && FieldPolicy.mayLearn(inputType, options)
        val fixes = FieldPolicy.mayAutoCorrect(inputType)
        emoji.setLearning(learns)
        autofix.setField(learns, fixes)
        val expands = fixes && !FieldPolicy.needsRawKeys(inputType)
        plainField = expands
        typer.extras.learning = learns
        val typing = layouts.snapshot.prefs.typing
        typer.extras.suggestions = typing.suggestions && expands
        typer.extras.touchAssist = typing.touchAssist && expands
        typer.extras.shortcuts = if (expands) shortcuts.book() else ShortcutBook.EMPTY
    }

    private fun onLayout(snapshot: LayoutSnapshot) {
        val composing = snapshot.language == AZ && !snapshot.numbers && !snapshot.edit
        if (typer.extras.arabizi && !composing) typer.settle()
        typer.extras.arabizi = composing
        typer.extras.doubleSpace = snapshot.prefs.typing.doubleSpace
        typer.extras.suggestions = snapshot.prefs.typing.suggestions && plainField
        typer.extras.touchAssist = snapshot.prefs.typing.touchAssist && plainField
        memory.onLanguage(snapshot.prefs.language)
        if (snapshot.prefs.typing.incognito != incognito) applyField()
    }

    private fun enterLabel(info: EditorInfo?): String? {
        if (info == null || (info.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) return null
        val res = when (info.imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_GO -> R.string.enter_go
            EditorInfo.IME_ACTION_SEARCH -> R.string.enter_search
            EditorInfo.IME_ACTION_SEND -> R.string.enter_send
            EditorInfo.IME_ACTION_NEXT -> R.string.enter_next
            EditorInfo.IME_ACTION_DONE -> R.string.enter_done
            EditorInfo.IME_ACTION_PREVIOUS -> R.string.enter_previous
            else -> return null
        }
        return context.getString(res)
    }

    private fun openFresh(inputType: Int) {
        val kind = inputType and InputType.TYPE_MASK_CLASS
        layouts.setEdit(false)
        layouts.setNumbers(
            kind == InputType.TYPE_CLASS_NUMBER || kind == InputType.TYPE_CLASS_PHONE || kind == InputType.TYPE_CLASS_DATETIME,
        )
        memory.restore()
        layouts.setLatinOnly(FieldPolicy.prefersLatin(inputType))
        layouts.setShift(Shift.OFF)
        emoji.close()
        translate.close()
        images.close()
        if (clip.shouldSurvive()) clip.settle() else clip.close()
    }

    private companion object {
        const val AZ = "az"
    }
}
