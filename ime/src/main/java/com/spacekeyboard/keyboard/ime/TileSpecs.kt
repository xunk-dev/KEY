package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.clipboard.rememberClipSnapshot
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.core.layout.ToolbarItem
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutTile
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.decorate.rememberDecorSnapshot
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.rememberEmojiSnapshot
import com.spacekeyboard.keyboard.emoji.R as EmojiR
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.rememberTranslateState

internal class Tile(val icon: SpaceIcon, val label: String, val selected: Boolean, val onClick: () -> Unit)

internal class TileCommands(
    val macro: MacroController,
    val snippets: SnippetController,
    val onDecorate: () -> Unit,
    val onSettings: () -> Unit,
    val onResize: () -> Unit,
    val onFloat: () -> Unit,
    val onDim: () -> Unit,
    val onCalculator: () -> Unit,
    val onEmoji: () -> Unit,
    val onClipboard: () -> Unit,
    val onTranslate: () -> Unit,
    val onAutomation: () -> Unit,
    val onSnippets: () -> Unit,
)

@Composable
internal fun rememberTileSpecs(
    theme: ThemeController,
    layouts: LayoutController,
    decor: DecorController,
    emoji: EmojiController,
    clip: ClipboardController,
    translate: TranslateController,
    shortcuts: ShortcutsController,
    commands: TileCommands,
): Map<ShortcutTile, Tile> {
    val themeSnapshot = rememberThemeSnapshot(theme)
    val layoutSnapshot = rememberLayoutSnapshot(layouts)
    val decorSnapshot = rememberDecorSnapshot(decor)
    val emojiSnapshot = rememberEmojiSnapshot(emoji)
    val clipSnapshot = rememberClipSnapshot(clip)
    val translateState = rememberTranslateState(translate)
    val prefs = layoutSnapshot.prefs
    fun closing(action: () -> Unit): () -> Unit = { shortcuts.close(); action() }
    return buildMap {
        put(ShortcutTile.MODE, Tile(prefs.mode.icon(), stringResource(prefs.mode.labelRes), false, layouts::cycleMode))
        put(ShortcutTile.THEME, Tile(ToolbarItem.THEME.glyph(), stringResource(themeSnapshot.choice.theme.nameRes), false, theme::selectNext))
        if (decorSnapshot.allowed) {
            put(ShortcutTile.DECORATE, Tile(ToolbarItem.DECORATE.glyph(), stringResource(ToolbarItem.DECORATE.labelRes), decorSnapshot.active != null, closing(commands.onDecorate)))
        }
        put(ShortcutTile.EDIT, Tile(ToolbarItem.EDIT.glyph(), stringResource(ToolbarItem.EDIT.labelRes), layoutSnapshot.edit, layouts::toggleEdit))
        put(ShortcutTile.RESIZE, Tile(ToolbarItem.RESIZE.glyph(), stringResource(ToolbarItem.RESIZE.labelRes), false, closing(commands.onResize)))
        put(ShortcutTile.FLOAT, Tile(SpaceIcon.FLOAT, stringResource(R.string.toolbar_float), prefs.mode == PanelMode.FLOAT, closing(commands.onFloat)))
        put(ShortcutTile.DIM, Tile(SpaceIcon.MOON, stringResource(R.string.toolbar_dim), prefs.dim.dimmed, closing(commands.onDim)))
        put(ShortcutTile.CALCULATOR, Tile(SpaceIcon.CALCULATOR, stringResource(R.string.toolbar_calculator), false, closing(commands.onCalculator)))
        put(ShortcutTile.INCOGNITO, Tile(ToolbarItem.INCOGNITO.glyph(), stringResource(ToolbarItem.INCOGNITO.labelRes), prefs.typing.incognito, layouts::toggleIncognito))
        put(ShortcutTile.AUTOMATION, Tile(SpaceIcon.AUTOMATION, stringResource(R.string.macro_title), commands.macro.active, if (commands.macro.active) commands.macro::halt else commands.onAutomation))
        put(ShortcutTile.TEXT, Tile(SpaceIcon.SHORTCUTS, stringResource(R.string.snip_title), commands.snippets.manager, commands.onSnippets))
        put(ShortcutTile.SETTINGS, Tile(ToolbarItem.SETTINGS.glyph(), stringResource(R.string.shortcuts_app_settings), false, closing(commands.onSettings)))
        if (emojiSnapshot.allowed || emojiSnapshot.open) {
            val open = emojiSnapshot.open
            put(ShortcutTile.EMOJI, Tile(if (open) SpaceIcon.KEYS else ToolbarItem.EMOJI.glyph(), stringResource(if (open) EmojiR.string.emoji_keys else ToolbarItem.EMOJI.labelRes), open, closing(commands.onEmoji)))
        }
        if (clipSnapshot.allowed || clipSnapshot.open) {
            val open = clipSnapshot.open
            put(ShortcutTile.CLIPBOARD, Tile(if (open) SpaceIcon.KEYS else ToolbarItem.CLIPBOARD.glyph(), stringResource(if (open) EmojiR.string.emoji_keys else ToolbarItem.CLIPBOARD.labelRes), open, closing(commands.onClipboard)))
        }
        if (translateState.allowed || translateState.open) {
            val open = translateState.open
            put(ShortcutTile.TRANSLATE, Tile(if (open) SpaceIcon.KEYS else ToolbarItem.TRANSLATE.glyph(), stringResource(if (open) EmojiR.string.emoji_keys else ToolbarItem.TRANSLATE.labelRes), open, closing(commands.onTranslate)))
        }
    }
}
