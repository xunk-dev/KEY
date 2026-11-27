package com.spacekeyboard.keyboard.ime

import android.content.ClipboardManager
import androidx.compose.ui.platform.ComposeView
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutVars
import java.util.Date
import java.util.Locale
import android.view.HapticFeedbackConstants
import com.spacekeyboard.keyboard.autofix.Zone
import com.spacekeyboard.keyboard.clipboard.latestText
import com.spacekeyboard.keyboard.core.FieldPolicy

internal fun SpaceInputService.createKeyboard() = KeyboardView(
    this,
    theme,
    layouts,
    onKey = actions::key,
    onKeyTouch = { dx, dy -> touchZone = if (dx.isNaN() || dy.isNaN()) Zone.NONE else Zone.of(dx, dy) },
    onNeighbors = { autofix.keys(it) },
    canSwapDigits = this::canSwapDigits,
    glideOn = { typer.extras.glide },
    onGlide = { path, keys ->
        suggest.glide(path, keys, layouts.snapshot.shift)
        layouts.consumeShift()
    },
    onSwapDigits = this::swapDigits,
    onKeyLongPress = actions::longPress,
    onResolve = touchAssist::resolve,
)

internal fun SpaceInputService.createToolbar(): ComposeView = ComposeView(this).apply {
    setContent {
        val panels = ToolbarPanels(emoji, decor, clip, translate, images, panel, shortcuts, calculator)
        val actions = ToolbarActions(
            toggles::toggleDecor, toggles::toggleClip, toggles::toggleTranslate, toggles::toggleImages,
            onSettings = this@createToolbar::openSettings,
            onSelectLanguage = { id ->
                toggles.showKeyboard()
                layouts.selectLanguage(id)
            },
            onSwitchKeyboard = this@createToolbar::switchKeyboard,
        )
        ToolbarContent(theme, layouts, panels, actions, suggest, tileCommands())
    }
}

internal fun SpaceInputService.subscribeAll() {
    dim.onChange = { deck.apply() }
    macro.onChange = { deck.apply() }
    subscriptions += clip.subscribe { suggest.textOffer?.refresh() }
    snippets.onChange = { deck.apply() }
    calculator.onChange = { deck.onOtherChanged() }
    subscriptions += panel.subscribe { deck.onPanelChanged() }
    subscriptions += layouts.subscribe { deck.apply() }
    subscriptions += layouts.subscribe(dock::apply)
    subscriptions += emoji.subscribe { deck.onOtherChanged() }
    subscriptions += decor.subscribe { deck.onOtherChanged() }
    subscriptions += clip.subscribe { deck.onOtherChanged() }
    subscriptions += translate.subscribe { deck.onOtherChanged() }
    subscriptions += images.subscribe { deck.onOtherChanged() }
}

internal fun SpaceInputService.tileCommands() = TileCommands(
    macro = macro,
    snippets = snippets,
    onDecorate = toggles::toggleDecor,
    onSettings = this::openSettings,
    onResize = dock::startResize,
    onFloat = dock::toggleFloat,
    onDim = dim::show,
    onCalculator = toggles::showCalculator,
    onEmoji = emoji::toggle,
    onClipboard = toggles::toggleClip,
    onTranslate = toggles::toggleTranslate,
    onAutomation = {
        macro.openManager()
        shortcuts.show()
    },
    onSnippets = {
        snippets.openManager()
        shortcuts.show()
    },
)

internal fun SpaceInputService.createTextOffer() =
    ClipTextOffer(this, clip, { typer.insert(it) }, { toggles.translateText(it) }, { translate.state.allowed })

internal fun SpaceInputService.createOtpOffer() = OtpOffer(
    this,
    typer.extras,
    typer::settle,
    { currentInputConnection },
    { ImeOtpTarget({ currentInputConnection }, { currentInputEditorInfo }, typer.extras) },
)

internal fun SpaceInputService.createSnippets(): SnippetController {
    typer.extras.variables = { resolveVariables(it) }
    return SnippetController(this, shortcuts, typer::insert, { resolveVariables(it) }) { fields.refreshShortcuts() }
}

private fun SpaceInputService.resolveVariables(text: String): String =
    ShortcutVars.expand(text, Date(), Locale.getDefault()) {
        getSystemService(ClipboardManager::class.java)?.primaryClip
            ?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(this)?.toString().orEmpty()
    }

internal fun SpaceInputService.createMacro() =
    MacroController(
        this, typer::play, typer::ready,
        { FieldPolicy.isSensitive(currentInputEditorInfo?.inputType ?: 0) },
        { clip.latestText() },
    ).also { typer.onStep = it::note }

internal fun SpaceInputService.createShake() {
    val shakeRouter = ShakeRouter(
        toggles, calculator, dock, { voice.start(layouts.snapshot.language) },
        { currentInputConnection != null }, typer::settle,
    )
    shake = ShakeController(this) {
        window.window?.decorView?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        shakeRouter.run(it)
    }.also { it.start() }
}
