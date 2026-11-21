package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.clipboard.rememberClipSnapshot
import com.spacekeyboard.keyboard.core.layout.ToolbarItem
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.decorate.rememberDecorSnapshot
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.rememberEmojiSnapshot
import com.spacekeyboard.keyboard.emoji.R as EmojiR
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.images.rememberImagesSnapshot
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.rememberTranslateState
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

@Composable
fun ToolbarContent(
    theme: ThemeController,
    layouts: LayoutController,
    panels: ToolbarPanels,
    actions: ToolbarActions,
    suggest: SuggestionController,
    onSymbols: () -> Unit,
) {
    SpaceTheme(mode = SpaceThemeMode.Dark) {
        val snapshot = rememberThemeSnapshot(theme)
        val style = KeyStyle.of(snapshot.choice.theme)
        val look = ButtonLook(snapshot.palette, style, rememberLayoutSnapshot(layouts).prefs.feedback.vibrate)
        Column(modifier = Modifier.fillMaxSize()) {
            SuggestionStrip(suggest, look, Modifier.fillMaxWidth().height(SUGGESTION_DP.dp))
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(TOOLS_DP.dp).toolsFrame(look)) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .horizontalScroll(rememberScrollState())
                        .widthIn(min = maxWidth),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToolbarButtons(theme, layouts, panels, actions, onSymbols)
                }
            }
        }
    }
}

@Composable
private fun SuggestionStrip(suggest: SuggestionController, look: ButtonLook, modifier: Modifier) {
    Box(modifier = modifier.padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
        when {
            suggest.autofill.isNotEmpty() -> AutofillBar(suggest.autofill)
            suggest.items.isNotEmpty() ->
                SuggestionBar(suggest.items, look.palette, look.style, SuggestionActions(suggest::accept, suggest::remove), Modifier.fillMaxSize())
        }
    }
}

private fun Modifier.toolsFrame(look: ButtonLook): Modifier = drawBehind {
    val line = 1.dp.toPx()
    val palette = look.palette
    when (look.style) {
        KeyStyle.GALAXY -> {
            val color = Color(ColorMath.withAlpha(palette.glow, 0.18f))
            drawRect(color, Offset.Zero, Size(size.width, line))
            drawRect(color, Offset(0f, size.height - line), Size(size.width, line))
        }
        KeyStyle.ECLIPSE -> drawRect(Color(ColorMath.mix(palette.background, palette.border, 0.35f)), Offset(0f, size.height - line), Size(size.width, line))
        KeyStyle.DAY -> Unit
    }
}

class ToolbarPanels(
    val emoji: EmojiController,
    val decor: DecorController,
    val clip: ClipboardController,
    val translate: TranslateController,
    val images: ImagesController,
)

class ToolbarActions(
    val onDecorate: () -> Unit,
    val onClipboard: () -> Unit,
    val onTranslate: () -> Unit,
    val onImages: () -> Unit,
    val onVoice: () -> Unit,
    val onSettings: () -> Unit,
)

@Composable
private fun ToolbarButtons(
    theme: ThemeController,
    layouts: LayoutController,
    panels: ToolbarPanels,
    actions: ToolbarActions,
    onSymbols: () -> Unit,
) {
    val themeSnapshot = rememberThemeSnapshot(theme)
    val layoutSnapshot = rememberLayoutSnapshot(layouts)
    val emojiSnapshot = rememberEmojiSnapshot(panels.emoji)
    val decorSnapshot = rememberDecorSnapshot(panels.decor)
    val clipSnapshot = rememberClipSnapshot(panels.clip)
    val translateState = rememberTranslateState(panels.translate)
    val imagesSnapshot = rememberImagesSnapshot(panels.images)
    val look = ButtonLook(themeSnapshot.palette, KeyStyle.of(themeSnapshot.choice.theme), layoutSnapshot.prefs.feedback.vibrate)
    layoutSnapshot.prefs.toolbar.forEach { item ->
        when (item) {
            ToolbarItem.THEME ->
                ToolbarButton(item.glyph(), stringResource(themeSnapshot.choice.theme.nameRes), look, theme::selectNext)
            ToolbarItem.MODE ->
                ToolbarButton(item.glyph(), stringResource(layoutSnapshot.prefs.mode.labelRes), look, layouts::cycleMode)
            ToolbarItem.SYMBOLS ->
                ToolbarButton(item.glyph(), stringResource(item.labelRes), look, onSymbols)
            ToolbarItem.DECORATE ->
                PanelButton(decorSnapshot.allowed, decorSnapshot.open, item, look, actions.onDecorate, decorSnapshot.active != null)
            ToolbarItem.EMOJI ->
                PanelButton(emojiSnapshot.allowed, emojiSnapshot.open, item, look, panels.emoji::toggle)
            ToolbarItem.CLIPBOARD ->
                PanelButton(clipSnapshot.allowed, clipSnapshot.open, item, look, actions.onClipboard)
            ToolbarItem.TRANSLATE ->
                PanelButton(translateState.allowed, translateState.open, item, look, actions.onTranslate)
            ToolbarItem.IMAGES ->
                PanelButton(imagesSnapshot.allowed, imagesSnapshot.open, item, look, actions.onImages)
            ToolbarItem.VOICE ->
                ToolbarButton(item.glyph(), stringResource(item.labelRes), look, actions.onVoice)
            ToolbarItem.EDIT ->
                ToolbarButton(item.glyph(), stringResource(item.labelRes), look, layouts::toggleEdit, layoutSnapshot.edit)
            ToolbarItem.SETTINGS ->
                ToolbarButton(item.glyph(), stringResource(item.labelRes), look, actions.onSettings)
            ToolbarItem.INCOGNITO ->
                ToolbarButton(item.glyph(), stringResource(item.labelRes), look, layouts::toggleIncognito, layoutSnapshot.prefs.typing.incognito)
        }
    }
}

@Composable
private fun PanelButton(
    allowed: Boolean,
    open: Boolean,
    item: ToolbarItem,
    look: ButtonLook,
    onClick: () -> Unit,
    selected: Boolean = false,
) {
    if (!allowed) return
    val glyph = if (open) SpaceIcon.KEYS else item.glyph()
    val label = if (open) EmojiR.string.emoji_keys else item.labelRes
    ToolbarButton(glyph, stringResource(label), look, onClick, selected || open)
}
