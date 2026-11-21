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
    private lateinit var theme: ThemeController
    private lateinit var layouts: LayoutController
    private lateinit var panel: SymbolPanelModel
    private lateinit var emoji: EmojiController
    private lateinit var decor: DecorController
    private lateinit var clip: ClipboardController
    private lateinit var translate: TranslateController
    private lateinit var autofix: AutoFixController
    private lateinit var images: ImagesController
    private lateinit var toggles: PanelToggles
    private lateinit var typer: KeyTyper
    private lateinit var suggest: SuggestionController
    private lateinit var voice: VoiceInput
    private lateinit var autoLanguage: AutoLanguage
    private lateinit var router: SearchKeyRouter
    private lateinit var actions: KeyActions
    private lateinit var fields: FieldSetup
    private lateinit var deck: PanelDeck
    private lateinit var hardware: HardwareKeys
    private lateinit var touchAssist: TouchAssist
    private var frame: ThemedFrame? = null
    private var keyboardView: KeyboardView? = null
    private var subscriptions = ArrayList<AutoCloseable>()
    private var touchZone = Zone.NONE

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
        toggles = PanelToggles(panel, emoji, decor, clip, translate, images) { currentInputConnection }
        typer = KeyTyper(
            decor, autofix, { currentInputConnection }, { currentInputEditorInfo?.imeOptions ?: 0 },
            { sendDownUpKeyEvents(it) }, ::needsRawKeys,
        )
        suggest = SuggestionController(this, { currentInputConnection }, typer::settle)
        voice = VoiceInput(this, VoicePrefs(this)) { text -> typer.ready().also { if (it) typer.insert(text) } }.also { it.register() }
        fields = FieldSetup(layouts, emoji, decor, clip, translate, images, autofix, typer, this) { keyboardView?.showPreviews = it }
        deck = PanelDeck(resources, layouts, panel, emoji, decor, clip, translate, images, typer::settle)
        touchAssist = TouchAssist(suggest, { currentInputConnection }, { typer.extras.touchAssist })
        autoLanguage = AutoLanguage(layouts, { currentInputConnection }, ::mayAutoSwitch)
        router = SearchKeyRouter(panel, emoji, clip)
        actions = KeyActions(
            layouts, router, typer, autofix, suggest,
            { currentInputConnection }, { currentInputEditorInfo }, { touchZone },
            { sendDownUpKeyEvents(it) }, ::switchKeyboard,
        )
        hardware = HardwareKeys(layouts, actions::key, typer::settle) { currentInputConnection != null && !needsRawKeys() }
        autofix.onFixed = { original, corrected ->
            keyboardView?.showNote("$original \u2192 $corrected \u00B7 ${getString(R.string.autofix_hint_undo)}")
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onAutofill(views: List<View>) {
        suggest.autofill = views
    }

    override fun onCreateInputView(): View {
        closeSubscriptions()
        val toolbar = createToolbar()
        val panelHost = ComposeView(this).apply {
            setContent { PanelContent(theme, panel, typer::insert, typer::deletePlain) }
        }
        val emojiActions = ImeEmojiActions(emoji) { currentInputConnection }
        val emojiHost = ComposeView(this).apply { setContent { EmojiPanelContent(theme, emoji, emojiActions) } }
        val keyboard = createKeyboard()
        val decorActions = ImeDecorActions(this, decor) { currentInputConnection }
        val decorHost = ComposeView(this).apply { setContent { DecorPanelContent(theme, decor, decorActions) } }
        val clipActions = ImeClipActions(clip, typer::insert) { layouts.snapshot.prefs.feedback.clipKeepOpen }
        val clipHost = ComposeView(this).apply { setContent { ClipPanelContent(theme, clip, clipActions) } }
        val translateActions = ImeTranslateActions(this, translate) { currentInputConnection }
        val translateHost = ComposeView(this).apply { setContent { TranslatePanelContent(theme, translate, translateActions) } }
        val imagesActions = ImeImagesActions(this, images, { currentInputConnection }, { currentInputEditorInfo })
        val imagesHost = ComposeView(this).apply { setContent { ImagesPanelContent(theme, images, imagesActions) } }
        deck.attach(PanelViews(toolbar, panelHost, emojiHost, decorHost, clipHost, translateHost, imagesHost, keyboard))
        keyboardView = keyboard
        return ThemedFrame(this, theme).also {
            frame = it
            it.addView(toolbar, LinearLayout.LayoutParams(MATCH_PARENT, dp(TOOLBAR_DP)))
            it.addView(panelHost, LinearLayout.LayoutParams(MATCH_PARENT, 0))
            it.addView(emojiHost, LinearLayout.LayoutParams(MATCH_PARENT, 0))
            it.addView(decorHost, LinearLayout.LayoutParams(MATCH_PARENT, 0))
            it.addView(clipHost, LinearLayout.LayoutParams(MATCH_PARENT, 0))
            it.addView(translateHost, LinearLayout.LayoutParams(MATCH_PARENT, 0))
            it.addView(imagesHost, LinearLayout.LayoutParams(MATCH_PARENT, 0))
            it.addView(keyboard, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            applyNavigationInset(it)
            subscribeAll()
        }
    }

    private fun createKeyboard() = KeyboardView(
        this,
        theme,
        layouts,
        onKey = actions::key,
        onKeyTouch = { dx, dy -> touchZone = if (dx.isNaN() || dy.isNaN()) Zone.NONE else Zone.of(dx, dy) },
        onCursorMove = actions::moveCursor,
        onKeyLongPress = actions::longPress,
        onResolve = touchAssist::resolve,
    )

    private fun createToolbar(): ComposeView = ComposeView(this).apply {
        setContent {
            val panels = ToolbarPanels(emoji, decor, clip, translate, images)
            val actions = ToolbarActions(
                toggles::toggleDecor, toggles::toggleClip, toggles::toggleTranslate, toggles::toggleImages,
                onVoice = { voice.start(layouts.snapshot.language) },
                onSettings = ::openSettings,
            )
            ToolbarContent(theme, layouts, panels, actions, suggest, toggles::showSymbols)
        }
    }

    private fun subscribeAll() {
        subscriptions += panel.subscribe { deck.onPanelChanged() }
        subscriptions += layouts.subscribe { deck.apply() }
        subscriptions += emoji.subscribe { deck.onOtherChanged() }
        subscriptions += decor.subscribe { deck.onOtherChanged() }
        subscriptions += clip.subscribe { deck.onOtherChanged() }
        subscriptions += translate.subscribe { deck.onOtherChanged() }
        subscriptions += images.subscribe { deck.onOtherChanged() }
    }

    override fun onWindowShown() {
        super.onWindowShown()
        lifecycleOwner.show()
        frame?.setShown(true)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        fields.start(info, restarting)
        if (!restarting) autoLanguage.onFieldStart()
        voice.flushPending()
        actions.updateAutoCap()
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int, candidatesStart: Int, candidatesEnd: Int,
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
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
        typer.settle()
        suggest.flush()
        autofix.flush()
        lifecycleOwner.hide()
        frame?.setShown(false)
        panel.close()
        emoji.close()
        decor.close()
        translate.close()
        images.close()
        if (!clip.shouldSurvive()) clip.close()
    }

    override fun onDestroy() {
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

    private fun switchKeyboard() {
        val switched = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            shouldOfferSwitchingToNextInputMethod() &&
            switchToNextInputMethod(false)
        if (!switched) (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
    }

    private fun openSettings() {
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
