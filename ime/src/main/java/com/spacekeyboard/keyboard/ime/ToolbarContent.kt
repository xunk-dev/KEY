package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.toolbar.BarArrangement
import com.spacekeyboard.keyboard.core.toolbar.BarSlots
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutTile
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.clipboard.rememberClipSnapshot
import com.spacekeyboard.keyboard.core.layout.ToolbarItem
import com.spacekeyboard.keyboard.core.macro.Macro
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.rememberEmojiSnapshot
import com.spacekeyboard.keyboard.emoji.R as EmojiR
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.symbols.state.SymbolPanelModel
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.rememberTranslateState
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

@Composable
internal fun ToolbarContent(
    theme: ThemeController,
    layouts: LayoutController,
    panels: ToolbarPanels,
    actions: ToolbarActions,
    suggest: SuggestionController,
    commands: TileCommands,
) {
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        val snapshot = rememberThemeSnapshot(theme)
        val style = KeyStyle.of(snapshot.choice.theme)
        val look = ButtonLook(snapshot.palette, style, rememberLayoutSnapshot(layouts).prefs.feedback.vibrate)
        val words = suggest.otpVisible || suggest.autofill.isNotEmpty() || suggest.offerVisible || suggest.textVisible || (suggest.composing && suggest.items.isNotEmpty()) || suggest.converting || suggest.marksOpen
        val layout = rememberLayoutSnapshot(layouts)
        val metrics = LocalContext.current.resources.displayMetrics
        val scale = toolbarScale(layout, metrics.density, metrics.heightPixels)
        val base = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(base.density * scale, base.fontScale)) {
            Box(modifier = Modifier.fillMaxSize().toolsFrame(look)) {
                MainRow(theme, layouts, panels, actions, suggest, commands, look, words)
            }
        }
    }
}

@Composable
private fun MainRow(
    theme: ThemeController,
    layouts: LayoutController,
    panels: ToolbarPanels,
    actions: ToolbarActions,
    suggest: SuggestionController,
    commands: TileCommands,
    look: ButtonLook,
    words: Boolean,
) {
    val prefs = rememberLayoutSnapshot(layouts).prefs
    val bar = remember(prefs.barLayout, prefs.languages) { prefs.bar() }
    val specs = rememberTileSpecs(theme, layouts, panels.decor, panels.emoji, panels.clip, panels.translate, panels.shortcuts, commands)
    val buttons = barButtons(panels, actions, layouts, bar, specs, words, commands.macro)
    val direction = LocalLayoutDirection.current
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        ToolbarBar(bar, buttons, words, look, layouts::setBar, Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                SuggestionStrip(suggest, look, Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun barButtons(
    panels: ToolbarPanels,
    actions: ToolbarActions,
    layouts: LayoutController,
    bar: BarArrangement,
    specs: Map<ShortcutTile, Tile>,
    words: Boolean,
    macro: MacroController,
): Map<String, BarButton> {
    val emoji = rememberEmojiSnapshot(panels.emoji)
    val clip = rememberClipSnapshot(panels.clip)
    val translate = rememberTranslateState(panels.translate)
    val snapshot = rememberLayoutSnapshot(layouts)
    return buildMap {
        put(BarSlots.TOOLS, toolsButton(panels))
        if (BarSlots.EMOJI in bar.all) put(BarSlots.EMOJI, panelButton(emoji.allowed, emoji.open, ToolbarItem.EMOJI, panels.emoji::toggle))
        if (BarSlots.TRANSLATE in bar.all) put(BarSlots.TRANSLATE, panelButton(translate.allowed, translate.open, ToolbarItem.TRANSLATE, actions.onTranslate))
        if (BarSlots.CLIPBOARD in bar.all) put(BarSlots.CLIPBOARD, panelButton(clip.allowed, clip.open, ToolbarItem.CLIPBOARD, actions.onClipboard))
        macro.favorite?.let { put(BarSlots.MACRO_PLAY, quickPlayButton(it, macro)) }
        bar.all.mapNotNull { ShortcutTile.byId(it) }.filter { it !in PANEL_BUTTONS }.forEach { tile ->
            specs[tile]?.let { put(tile.id, BarButton(it.icon, "", it.label, it.selected, true, it.onClick)) }
        }
        if (!words) {
            val switch = stringResource(R.string.toolbar_switch_keyboard)
            put(BarSlots.GLOBE, BarButton(SpaceIcon.LANGUAGE, "", switch, false, true, actions.onSwitchKeyboard))
            snapshot.prefs.languages.forEach { id ->
                val on = id == snapshot.language
                put(BarSlots.language(id), BarButton(null, LayoutCatalog.short(id), LayoutCatalog.title(id), on, true) { actions.onSelectLanguage(id) })
            }
        }
    }
}

@Composable
private fun quickPlayButton(favorite: Macro, macro: MacroController): BarButton {
    val name = favorite.name ?: stringResource(R.string.macro_default_name, favorite.id.toInt())
    return BarButton(SpaceIcon.PLAY, "", stringResource(R.string.macro_quick_play, name), false, !macro.active, macro::playFavorite)
}

@Composable
private fun SuggestionStrip(suggest: SuggestionController, look: ButtonLook, modifier: Modifier) {
    Box(modifier = modifier.padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
        when {
            suggest.otpVisible -> suggest.otpOffer?.let { OtpOfferChip(it, look.palette) }
            suggest.autofill.isNotEmpty() -> AutofillBar(suggest.autofill)
            suggest.offerVisible -> suggest.imageOffer.offer?.let { ImageOfferChip(it, look.palette, suggest.imageOffer::send, suggest.imageOffer::dismiss) }
            suggest.textVisible -> suggest.textOffer?.let { TextOfferChip(it, look.palette) }
            suggest.marksOpen -> MarksBar(suggest, look)
            suggest.items.isNotEmpty() -> WordsRow(suggest, look)
        }
    }
}

@Composable
private fun WordsRow(suggest: SuggestionController, look: ButtonLook) {
    Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        val actions = SuggestionActions(suggest::accept, suggest::remove)
        SuggestionBar(suggest.items, look.palette, look.style, actions, Modifier.weight(1f).fillMaxHeight())
        if (suggest.marksAvailable) MarksChip(false, look, suggest::toggleMarks)
    }
}

private fun Modifier.toolsFrame(look: ButtonLook): Modifier = drawBehind {
    val palette = look.palette
    val inset = 3.dp.toPx()
    val topLeft = Offset(inset, inset)
    val body = Size(size.width - 2 * inset, size.height - 2 * inset)
    val radius = CornerRadius(body.height / 2f)
    val fill = when (look.style) {
        KeyStyle.DAY -> Color(ColorMath.mix(palette.keyFill, palette.border, 0.2f))
        KeyStyle.GLASS -> Color(ColorMath.mix(palette.background, palette.keyFill, 0.3f))
        else -> Color(ColorMath.mix(palette.background, palette.keyFill, 0.55f))
    }
    drawRoundRect(fill, topLeft, body, radius)
    drawRoundRect(Color(ColorMath.withAlpha(palette.border, 0.35f)), topLeft, body, radius, style = Stroke(1.dp.toPx()))
}

private val PANEL_BUTTONS = setOf(ShortcutTile.EMOJI, ShortcutTile.CLIPBOARD, ShortcutTile.TRANSLATE)

class ToolbarPanels(
    val emoji: EmojiController,
    val decor: DecorController,
    val clip: ClipboardController,
    val translate: TranslateController,
    val images: ImagesController,
    val symbols: SymbolPanelModel,
    val shortcuts: ShortcutsController,
    val calculator: CalculatorController,
)

class ToolbarActions(
    val onDecorate: () -> Unit,
    val onClipboard: () -> Unit,
    val onTranslate: () -> Unit,
    val onImages: () -> Unit,
    val onSettings: () -> Unit,
    val onSelectLanguage: (String) -> Unit,
    val onSwitchKeyboard: () -> Unit = {},
)

@Composable
private fun panelButton(allowed: Boolean, open: Boolean, item: ToolbarItem, onClick: () -> Unit): BarButton {
    val label = stringResource(if (open) EmojiR.string.emoji_keys else item.labelRes)
    return BarButton(if (open) SpaceIcon.KEYS else item.glyph(), "", label, open, allowed || open, onClick)
}

@Composable
private fun toolsButton(panels: ToolbarPanels): BarButton =
    if (panels.calculator.open) {
        BarButton(SpaceIcon.KEYS, "", stringResource(EmojiR.string.emoji_keys), true, true, panels.calculator::close)
    } else {
        BarButton(SpaceIcon.TOOLS, "", stringResource(R.string.toolbar_shortcuts), panels.shortcuts.open, true, panels.shortcuts::toggle)
    }
