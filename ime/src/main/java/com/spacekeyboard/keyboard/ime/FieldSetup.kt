package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.text.InputType
import android.view.inputmethod.EditorInfo
import com.spacekeyboard.keyboard.autofix.AutoFixController
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.core.FieldPolicy
import com.spacekeyboard.keyboard.core.apps.AppLanguageStore
import com.spacekeyboard.keyboard.core.apps.LanguageMemory
import com.spacekeyboard.keyboard.core.otp.FieldTraits
import com.spacekeyboard.keyboard.core.otp.OtpFieldRules
import com.spacekeyboard.keyboard.core.otp.OtpFit
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutBook
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutStore
import com.spacekeyboard.keyboard.core.suggest.FieldKind
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
    private var appScoped = true
    private var packageName: String? = null
    private var plainField = false
    private var otpFit = OtpFit.NONE

    init {
        layouts.subscribe(::onLayout)
    }

    fun start(info: EditorInfo?, restarting: Boolean) {
        inputType = info?.inputType ?: 0
        options = info?.imeOptions ?: 0
        packageName = info?.packageName
        otpFit = info?.let { OtpFieldRules.fit(traitsOf(it)) } ?: OtpFit.NONE
        typer.extras.fieldEpoch++
        typer.extras.cursor = null
        val sensitive = FieldPolicy.isSensitive(inputType)
        val rich = !sensitive && !FieldPolicy.needsRawKeys(inputType)
        emoji.setAllowed(FieldPolicy.acceptsEmoji(inputType))
        decor.setAllowed(FieldPolicy.acceptsEmoji(inputType))
        clip.setAllowed(!sensitive)
        clip.setEditorField(info?.packageName == context.packageName)
        translate.setAllowed(rich)
        images.setAllowed(rich)
        showPreviews(!sensitive)
        applyField()
        memory.begin(info?.packageName, !sensitive && !incognito)
        layouts.setEnter(enterLabel(info), EnterKind.of(info?.imeOptions ?: 0))
        if (!restarting) openFresh(inputType)
    }

    fun end() = memory.end()

    private fun applyField() {
        val typing = layouts.snapshot.prefs.typing
        incognito = typing.incognito
        appScoped = typing.appSuggestions
        val learns = !incognito && FieldPolicy.mayLearn(inputType, options)
        val fixes = FieldPolicy.mayAutoCorrect(inputType)
        emoji.setLearning(learns)
        autofix.setField(learns, fixes)
        val expands = fixes && !FieldPolicy.needsRawKeys(inputType)
        plainField = expands
        typer.extras.learning = learns
        val kind = if (appScoped) FieldKind.of(inputType, options) else FieldKind.PROSE
        typer.extras.kind = kind
        typer.extras.scope = if (appScoped) kind.scope(packageName) else null
        typer.extras.emojiSuggestions = typing.emojiSuggestions && expands
        typer.extras.suggestions = typing.suggestions && expands
        typer.extras.imageOffers = typing.imageSuggestions && expands
        typer.extras.copyOffers = typing.copyOffers && expands
        typer.extras.touchAssist = typing.touchAssist && expands
        typer.extras.glide = typing.glide && expands && layouts.snapshot.language != AZ
        typer.extras.convertOffers = typing.convertOffers && expands
        typer.extras.spellFix = typing.spellFix && expands
        typer.extras.otpFit = otpFit
        typer.extras.otpOffers = typing.otpOffers
        refreshShortcuts()
    }

    fun refreshShortcuts() {
        typer.extras.shortcuts = if (plainField) shortcuts.book() else ShortcutBook.EMPTY
    }

    private fun onLayout(snapshot: LayoutSnapshot) {
        val composing = snapshot.language == AZ && !snapshot.numbers && !snapshot.edit
        if (typer.extras.arabizi && !composing) typer.settle()
        typer.extras.arabizi = composing
        typer.extras.doubleSpace = snapshot.prefs.typing.doubleSpace
        typer.extras.suggestions = snapshot.prefs.typing.suggestions && plainField
        typer.extras.imageOffers = snapshot.prefs.typing.imageSuggestions && plainField
        typer.extras.copyOffers = snapshot.prefs.typing.copyOffers && plainField
        typer.extras.touchAssist = snapshot.prefs.typing.touchAssist && plainField
        typer.extras.glide = snapshot.prefs.typing.glide && plainField && snapshot.language != AZ
        typer.extras.convertOffers = snapshot.prefs.typing.convertOffers && plainField
        typer.extras.spellFix = snapshot.prefs.typing.spellFix && plainField
        typer.extras.otpOffers = snapshot.prefs.typing.otpOffers
        typer.extras.emojiSuggestions = snapshot.prefs.typing.emojiSuggestions && plainField
        memory.onLanguage(snapshot.prefs.language)
        if (snapshot.prefs.typing.incognito != incognito || snapshot.prefs.typing.appSuggestions != appScoped) applyField()
    }

    private fun traitsOf(info: EditorInfo) = FieldTraits(
        info.inputType,
        info.imeOptions,
        listOf(info.hintText?.toString(), info.label?.toString(), info.fieldName, info.privateImeOptions, info.actionLabel?.toString()),
    )

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
