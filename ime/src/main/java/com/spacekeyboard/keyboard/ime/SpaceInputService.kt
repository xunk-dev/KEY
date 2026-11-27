package com.spacekeyboard.keyboard.ime

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import kotlin.math.abs
import android.widget.LinearLayout
import androidx.compose.ui.platform.ComposeView
import com.spacekeyboard.keyboard.autofix.AutoFixController
import com.spacekeyboard.keyboard.autofix.Zone
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.core.FieldPolicy
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.NumberRow
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.ThemedFrame
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel
import com.spacekeyboard.keyboard.translate.TranslateController

class SpaceInputService : AutofillImeService() {
    private val lifecycleOwner = ImeLifecycleOwner()
    internal lateinit var theme: ThemeController
    internal lateinit var layouts: LayoutController
    internal lateinit var panel: SymbolPanelModel
    internal lateinit var emoji: EmojiController
    internal lateinit var decor: DecorController
    internal lateinit var clip: ClipboardController
    internal lateinit var translate: TranslateController
    internal lateinit var autofix: AutoFixController
    internal lateinit var images: ImagesController
    internal lateinit var shortcuts: ShortcutsController
    internal lateinit var macro: MacroController
    internal lateinit var snippets: SnippetController
    internal val dim = DimController()
    internal val calculator = CalculatorController()
    internal lateinit var resize: ResizeController
    internal lateinit var dock: DockRoot
    internal lateinit var toggles: PanelToggles
    internal lateinit var typer: KeyTyper
    internal lateinit var suggest: SuggestionController
    internal lateinit var voice: VoiceInput
    internal lateinit var autoLanguage: AutoLanguage
    internal lateinit var router: SearchKeyRouter
    internal lateinit var actions: KeyActions
    internal lateinit var fields: FieldSetup
    internal lateinit var deck: PanelDeck
    internal lateinit var hardware: HardwareKeys
    internal lateinit var touchAssist: TouchAssist
    internal lateinit var shake: ShakeController
    private var frame: ThemedFrame? = null
    private var keyboardView: KeyboardView? = null
    internal var subscriptions = ArrayList<AutoCloseable>()
    internal var touchZone = Zone.NONE

    override fun onCreate() {
        super.onCreate()
        lifecycleOwner.create()
        lifecycleOwner.attachTo(window.window!!.decorView)
        theme = ThemeController(this).also { it.start() }
        layouts = LayoutController(this).also { it.start() }
        panel = SymbolPanelModel(this)
        decor = DecorController(this).also { it.start() }
        emoji = EmojiController(this).also {
            it.start()
            it.preload()
        }
        clip = ClipboardController(this).also { it.start() }
        translate = TranslateController(this).also { it.start() }
        autofix = AutoFixController(this).also { it.start() }
        images = ImagesController(this)
        shortcuts = ShortcutsController()
        resize = ResizeController()
        dock = DockRoot(this, theme, layouts, resize)
        toggles = PanelToggles(
            panel, emoji, decor, clip, translate, images, shortcuts, calculator,
            connection = { currentInputConnection },
            keyboardLanguage = { layouts.snapshot.language },
        )
        typer = KeyTyper(
            decor, autofix, { currentInputConnection }, { currentInputEditorInfo?.imeOptions ?: 0 },
            { sendDownUpKeyEvents(it) }, ::needsRawKeys,
        )
        macro = createMacro()
        snippets = createSnippets()
        createInputParts()
    }

    private fun createInputParts() {
        suggest = SuggestionController(
            this, { currentInputConnection }, typer::settle, { currentInputEditorInfo }, createTextOffer(), createOtpOffer(),
            typer.extras, emoji::hint, emoji::record,
        )
        typer.extras.spelling = suggest::spellFix
        voice = VoiceInput(this, VoicePrefs(this)) { text -> typer.ready().also { if (it) typer.insert(text) } }.also { it.register() }
        fields = FieldSetup(layouts, emoji, decor, clip, translate, images, autofix, typer, this) { keyboardView?.showPreviews = it }
        deck = PanelDeck(resources, layouts, panel, emoji, decor, clip, translate, images, shortcuts, calculator, dim, macro, snippets, typer::settle)
        shortcuts.onChange = { macro.tools(shortcuts.open); snippets.tools(shortcuts.open); deck.onOtherChanged() }
        touchAssist = TouchAssist(suggest, { currentInputConnection }, { typer.extras.touchAssist })
        autoLanguage = AutoLanguage(layouts, { currentInputConnection }, ::mayAutoSwitch)
        router = SearchKeyRouter(panel, emoji, clip, translate, snippets)
        actions = createActions()
        hardware = HardwareKeys(layouts, actions::key, typer::settle) { currentInputConnection != null && !needsRawKeys() }
        bindAutofixNote()
        createShake()
    }

    private fun createActions() = KeyActions(
        layouts, router, typer, autofix, suggest,
        { currentInputConnection }, { currentInputEditorInfo }, { touchZone },
        ::switchKeyboard, { voice.start(layouts.snapshot.language) }, { toggles.openTool(it) },
    )

    private fun bindAutofixNote() {
        autofix.onFixed = { original, corrected ->
            keyboardView?.showNote("$original \u2192 $corrected \u00B7 ${getString(R.string.autofix_hint_undo)}")
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onComputeInsets(outInsets: InputMethodService.Insets) {
        super.onComputeInsets(outInsets)
        dock.fillInsets(outInsets, window.window!!.decorView.height)
    }

    override fun onAutofill(views: List<View>) {
        suggest.autofill = views
    }

    override fun onCreateInputView(): View {
        closeSubscriptions()
        val toolbar = createToolbar()
        val panelHost = ComposeView(this).apply { setContent { PanelContent(theme, panel, emoji, images, toggles::showTab, typer::insert, typer::deletePlain) } }
        val emojiActions = ImeEmojiActions(emoji, { currentInputConnection }, toggles::showTab, macro::noteText)
        val emojiHost = ComposeView(this).apply { setContent { EmojiPanelContent(theme, emoji, images, layouts, emojiActions) } }
        val shortcutsHost = ComposeView(this).apply { setContent { ShortcutsPanelContent(theme, layouts, decor, emoji, clip, translate, shortcuts, tileCommands()) } }
        val calculatorHost = calculatorHost(this, theme, layouts, calculator, typer::insert)
        val dimHost = ComposeView(this).apply { setContent { DimBarContent(theme, layouts, dim) } }
        val macroHost = ComposeView(this).apply { setContent { MacroBarContent(theme, macro, snippets) } }
        val keyboard = createKeyboard()
        val decorActions = ImeDecorActions(this, decor) { currentInputConnection }
        val decorHost = ComposeView(this).apply { setContent { DecorPanelContent(theme, decor, decorActions) } }
        val clipActions = ImeClipActions(this, clip, typer::insert, macro::markPaste, snippets::start) { layouts.snapshot.prefs.feedback.clipKeepOpen }
        val stack = KeyboardStack(this, keyboard, ComposeView(this).apply { setContent { KeyRowContent(theme, emoji, clip, emojiActions, clipActions) } })
        val clipHost = ComposeView(this).apply { setContent { ClipPanelContent(theme, clip, clipActions) } }
        val translateActions = ImeTranslateActions(this, translate) { currentInputConnection }
        val translateHost = ComposeView(this).apply { setContent { TranslatePanelContent(theme, translate, translateActions) } }
        val imagesActions = ImeImagesActions(this, images, { currentInputConnection }, { currentInputEditorInfo }, toggles::showTab)
        val imagesHost = ComposeView(this).apply { setContent { ImagesPanelContent(theme, emoji, images, imagesActions) } }
        deck.attach(PanelViews(toolbar, panelHost, emojiHost, decorHost, clipHost, translateHost, imagesHost, shortcutsHost, calculatorHost, dimHost, macroHost, stack))
        keyboardView = keyboard
        val themed = ThemedFrame(this, theme).also {
            frame = it
            it.addView(translateHost, LinearLayout.LayoutParams(MATCH_PARENT, 0))
            it.addView(toolbar, LinearLayout.LayoutParams(MATCH_PARENT, dp(TOOLBAR_DP)))
            listOf(macroHost, panelHost, emojiHost, decorHost, clipHost, imagesHost, shortcutsHost, calculatorHost, dimHost).forEach { host ->
                it.addView(host, LinearLayout.LayoutParams(MATCH_PARENT, 0))
            }
            it.addView(stack, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            applyNavigationInset(it)
            subscribeAll()
        }
        return dock.build(themed)
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleOwner.show()
        frame?.setShown(true)
        macro.reload()
        shake.show()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (!restarting) snippets.reset()
        fields.start(info, restarting)
        suggest.beginOtp()
        if (!restarting) autoLanguage.onFieldStart()
        voice.flushPending()
        actions.updateAutoCap()
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int,
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        typer.extras.cursor = newSelStart.takeIf { it >= 0 && newSelStart == newSelEnd }
        if (oldSelStart != newSelStart || oldSelEnd != newSelEnd) actions.updateAutoCap()
        val jumped = candidatesStart < 0 && newSelStart == newSelEnd && abs(newSelStart - oldSelStart) > CURSOR_JUMP
        if (jumped) autoLanguage.onCursorMoved()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        theme.refresh()
        deck.reset()
        keyboardView?.requestLayout()
        deck.apply()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean = hardware.down(event) || super.onKeyDown(keyCode, event)

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean = hardware.up(event) || super.onKeyUp(keyCode, event)

    override fun onCurrentInputMethodSubtypeChanged(subtype: InputMethodSubtype?) {
        super.onCurrentInputMethodSubtypeChanged(subtype)
        val id = LayoutCatalog.forSubtype(subtype?.languageTag, subtype?.extraValue) ?: return
        layouts.selectOrEnable(id)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        fields.end()
        macro.halt()
        snippets.reset()
        typer.settle()
        suggest.hide()
        autofix.flush()
        shake.hide()
        lifecycleOwner.hide()
        frame?.setShown(false)
        panel.close()
        emoji.close()
        decor.close()
        translate.close()
        images.close()
        shortcuts.close()
        calculator.close()
        dim.close()
        resize.close()
        if (!clip.shouldSurvive()) clip.close()
    }

    override fun onDestroy() {
        shake.stop()
        voice.unregister()
        closeSubscriptions()
        autofix.reset()
        autofix.stop()
        translate.stop()
        clip.stop()
        emoji.stop()
        decor.stop()
        panel.release()
        layouts.stop()
        theme.stop()
        lifecycleOwner.destroy()
        super.onDestroy()
    }

    internal fun switchKeyboard() {
        val switched = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            shouldOfferSwitchingToNextInputMethod() &&
            switchToNextInputMethod(false)
        if (!switched) (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
    }

    internal fun canSwapDigits(): Boolean =
        layouts.canSwapDigits && !FieldPolicy.isSensitive(currentInputEditorInfo?.inputType ?: 0)

    internal fun swapDigits() {
        val native = layouts.swapDigits()
        keyboardView?.showNote(NumberRow.digits(layouts.snapshot.activeId, native).take(SAMPLE_DIGITS).joinToString(""))
    }

    internal fun openSettings() {
        if (SettingsLink.open(this)) requestHideSelf(0)
    }

    private fun mayAutoSwitch(): Boolean = currentInputEditorInfo?.inputType?.let {
        !FieldPolicy.isSensitive(it) && !FieldPolicy.needsRawKeys(it) && !FieldPolicy.prefersLatin(it)
    } == true

    private fun needsRawKeys(): Boolean = currentInputEditorInfo?.let { FieldPolicy.needsRawKeys(it.inputType) } == true

    private fun closeSubscriptions() {
        subscriptions.forEach { it.close() }
        subscriptions.clear()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}

private const val CURSOR_JUMP = 3
private const val SAMPLE_DIGITS = 3
