package com.spacekeyboard.keyboard.ime

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import com.spacekeyboard.keyboard.clipboard.ClipMode
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.EmojiMode
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.symbols.state.PanelMetrics
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel
import com.spacekeyboard.keyboard.translate.TranslateController

internal const val SUGGESTION_DP = 36
internal const val TOOLS_DP = 44
internal const val TOOLBAR_DP = SUGGESTION_DP + TOOLS_DP
private const val EMOJI_SEARCH_DP = 100
private const val CLIP_NAME_DP = 90

internal class PanelViews(
    val toolbar: View,
    val symbols: View,
    val emoji: View,
    val decor: View,
    val clip: View,
    val translate: View,
    val images: View,
    val keyboard: View,
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
    private val settle: () -> Unit,
) {
    private var views: PanelViews? = null
    private var applied: List<Any>? = null

    fun attach(next: PanelViews?) {
        views = next
        applied = null
    }

    fun reset() {
        applied = null
    }

    fun onPanelChanged() {
        if (panel.snapshot.open) settle()
        apply()
    }

    fun onOtherChanged() {
        val clipOpen = clip.snapshot.open
        val translateOpen = translate.state.open
        val imagesOpen = images.snapshot.open
        val other = emoji.snapshot.open || decor.snapshot.open || clipOpen || translateOpen || imagesOpen
        if (other) settle()
        if (other && panel.snapshot.open) panel.close()
        if (emoji.snapshot.open && decor.snapshot.open) decor.close()
        if (clipOpen && (emoji.snapshot.open || decor.snapshot.open)) clip.close()
        if (translateOpen && (emoji.snapshot.open || decor.snapshot.open || clipOpen)) translate.close()
        if (imagesOpen && (emoji.snapshot.open || decor.snapshot.open || clipOpen || translateOpen)) images.close()
        apply()
    }

    fun apply() {
        val target = views ?: return
        val state = panel.snapshot
        val mode = emoji.snapshot.mode
        val density = resources.displayMetrics.density
        val keys = layouts.snapshot.keyboardHeightPx(density, resources.displayMetrics.heightPixels)
        val height = if (state.searching) dp(PanelMetrics.SEARCH_DP) else dp(TOOLBAR_DP) + keys
        val emojiHeight = if (mode == EmojiMode.SEARCH) dp(EMOJI_SEARCH_DP) else keys
        val decorOpen = decor.snapshot.open
        val clipMode = clip.snapshot.mode
        val clipHeight = if (clipMode == ClipMode.NAMING) dp(CLIP_NAME_DP) else keys
        val translateOpen = translate.state.open
        val imagesOpen = images.snapshot.open
        val now = listOf(state.open, state.searching, height, mode, emojiHeight, decorOpen, clipMode, clipHeight, translateOpen, imagesOpen)
        if (now == applied) return
        applied = now
        target.toolbar.visibility = if (state.open) View.GONE else View.VISIBLE
        val hideKeys = (state.open && !state.searching) || mode == EmojiMode.BROWSE || decorOpen ||
            clipMode == ClipMode.BROWSE || translateOpen || imagesOpen
        target.keyboard.visibility = if (hideKeys) View.GONE else View.VISIBLE
        size(target.symbols, state.open, height)
        size(target.emoji, mode != EmojiMode.CLOSED, emojiHeight)
        size(target.decor, decorOpen, keys)
        size(target.clip, clipMode != ClipMode.CLOSED, clipHeight)
        size(target.translate, translateOpen, keys)
        size(target.images, imagesOpen, keys)
    }

    private fun size(view: View, visible: Boolean, height: Int) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
        view.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, height)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
