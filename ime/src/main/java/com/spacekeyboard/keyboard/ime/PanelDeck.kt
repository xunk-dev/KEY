package com.spacekeyboard.keyboard.ime

import android.content.res.Resources
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.spacekeyboard.keyboard.clipboard.ClipMode
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.EmojiMode
import com.spacekeyboard.keyboard.core.layout.Limits
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.LayoutSnapshot
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.symbols.state.PanelMetrics
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel
import com.spacekeyboard.keyboard.translate.TranslateController

internal const val TOOLBAR_DP = 46
private const val TOOLBAR_SCALE_MIN = 0.7f
private const val TOOLBAR_SCALE_MAX = 1.35f

internal fun toolbarScale(snapshot: LayoutSnapshot, density: Float, screenPx: Int): Float {
    val nominal = Limits.ROW_DP * density
    val row = snapshot.rowPx(snapshot.stableRows, density, screenPx)
    return (row / nominal).coerceIn(TOOLBAR_SCALE_MIN, TOOLBAR_SCALE_MAX)
}

internal fun toolbarPx(snapshot: LayoutSnapshot, density: Float, screenPx: Int): Int =
    (TOOLBAR_DP * density * toolbarScale(snapshot, density, screenPx)).toInt()
private const val TRANSLATE_DP = 64

internal class PanelViews(
    val toolbar: View,
    val symbols: View,
    val emoji: View,
    val decor: View,
    val clip: View,
    val translate: View,
    val images: View,
    val shortcuts: View,
    val calculator: View,
    val dim: View,
    val macroBar: View,
    val keyboard: KeyboardStack,
)

internal class PanelDeck(
    private val resources: Resources,
    private val layouts: LayoutController,
    private val panel: SymbolPanelModel,
    private val emoji: EmojiController,
    private val decor: DecorController,
    private val clip: ClipboardController,
    private val translate: TranslateController,
    private val images: ImagesController,
    private val shortcuts: ShortcutsController,
    private val calculator: CalculatorController,
    private val dim: DimController,
    private val macro: MacroController,
    private val snippets: SnippetController,
    private val settle: () -> Unit,
) {
    private var views: PanelViews? = null
    private var applied: List<Any>? = null
    private val dimFilter = DimFilter()

    fun attach(next: PanelViews?) {
        views = next
        applied = null
    }

    fun reset() {
        applied = null
    }

    fun onPanelChanged() {
        if (panel.snapshot.open) {
            settle()
            shortcuts.close()
            calculator.close()
            dim.close()
        }
        apply()
    }

    fun onOtherChanged() {
        val clipOpen = clip.snapshot.open
        val translateOpen = translate.state.open
        val imagesOpen = images.snapshot.open
        val other = emoji.snapshot.open || decor.snapshot.open || clipOpen || translateOpen || imagesOpen
        if (other) settle()
        if (other && shortcuts.open) shortcuts.close()
        if (other && dim.open) dim.close()
        if (other && calculator.open) calculator.close()
        if (other && panel.snapshot.open) panel.close()
        if (emoji.snapshot.open && decor.snapshot.open) decor.close()
        if (clipOpen && (emoji.snapshot.open || decor.snapshot.open)) clip.close()
        if (translateOpen && (emoji.snapshot.open || decor.snapshot.open || clipOpen)) translate.close()
        if (imagesOpen && (emoji.snapshot.open || decor.snapshot.open || clipOpen || translateOpen)) images.close()
        apply()
    }

    private fun setHeight(view: View, height: Int) {
        (view.layoutParams as? LinearLayout.LayoutParams)?.let {
            if (it.height != height) {
                it.height = height
                view.layoutParams = it
            }
        }
    }

    fun apply() {
        val target = views ?: return
        val state = panel.snapshot
        dimFilter.apply(layouts.snapshot.prefs.dim.brightness, listOf(target.toolbar, target.keyboard, target.symbols, target.emoji, target.decor, target.clip, target.translate, target.images, target.shortcuts, target.calculator))
        val mode = emoji.snapshot.mode
        val density = resources.displayMetrics.density
        val keys = layouts.snapshot.keyboardHeightPx(density, resources.displayMetrics.heightPixels)
        val bar = toolbarPx(layouts.snapshot, density, resources.displayMetrics.heightPixels)
        val full = keys + bar
        val height = if (state.searching) dp(PanelMetrics.SEARCH_DP) else full
        val emojiSearching = mode == EmojiMode.SEARCH
        val emojiHeight = if (emojiSearching) bar else full
        val decorOpen = decor.snapshot.open
        val clipMode = clip.snapshot.mode
        val clipHeight = if (clipMode == ClipMode.NAMING) bar else full
        val translateOpen = translate.state.open
        val translateExpanded = translateOpen && translate.state.expanded
        val translateHeight = if (translateExpanded) full else dp(TRANSLATE_DP)
        val (imagesOpen, shortcutsOpen) = images.snapshot.open to shortcuts.open
        val (dimOpen, calculatorOpen) = dim.open to calculator.open
        val now = listOf(state.open, state.searching, height, mode, emojiHeight, decorOpen, clipMode, clipHeight, translateOpen, translateHeight, imagesOpen, shortcutsOpen, dimOpen, calculatorOpen, full, layouts.snapshot.digitsOnTop, bar, macro.active, snippets.barShown)
        if (now == applied) return
        applied = now
        val hideKeys = (state.open && !state.searching) || mode == EmojiMode.BROWSE || decorOpen ||
            clipMode == ClipMode.BROWSE || translateExpanded || imagesOpen
        setHeight(target.toolbar, bar)
        target.toolbar.visibility = if (hideKeys || emojiSearching || clipMode == ClipMode.NAMING || dimOpen) View.GONE else View.VISIBLE
        target.keyboard.visibility = if (hideKeys || shortcutsOpen || calculatorOpen) View.GONE else View.VISIBLE
        size(target.symbols, state.open, height)
        size(target.emoji, mode != EmojiMode.CLOSED, emojiHeight)
        size(target.decor, decorOpen, full)
        size(target.clip, clipMode != ClipMode.CLOSED, clipHeight)
        size(target.translate, translateOpen, translateHeight)
        size(target.images, imagesOpen, full)
        size(target.shortcuts, shortcutsOpen, keys)
        size(target.calculator, calculatorOpen, keys)
        size(target.dim, dimOpen, bar)
        size(target.macroBar, macro.active || snippets.barShown, dp(MACRO_BAR_DP))
        sizeResults(target.keyboard, emojiSearching || clipMode == ClipMode.NAMING)
    }

    private fun sizeResults(stack: KeyboardStack, searching: Boolean) {
        val view = stack.results
        val snapshot = layouts.snapshot
        val metrics = resources.displayMetrics
        val height = snapshot.rowPx(snapshot.stableRows, metrics.density, metrics.heightPixels).toInt()
        val shown = searching && snapshot.digitsOnTop
        view.visibility = if (shown) View.VISIBLE else View.GONE
        stack.coverTopRow(shown, height)
        view.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, height, Gravity.TOP)
    }

    private fun size(view: View, visible: Boolean, height: Int) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
        view.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, height)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
