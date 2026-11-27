# Remove single recents (design A)

- Emoji "Recent": the Clear key now enters edit mode. Each emoji gets a red minus badge and a tap removes just that one. The header shows "Clear all" (with the existing confirm) and "Done".
- Symbols, Faces and Text art tabs: a new "Recent" item is first in the bottom dock. Items are recorded when inserted (limit 40, kept per tab) and use the same edit mode.
- Recents from the Recent view itself are not reordered when tapped.

Files
- feature-emoji: model/Recents.kt, data/EmojiPrefsStore.kt, EmojiController.kt, ui/EmojiActions.kt, ui/EmojiGrid.kt (EmojiEditGrid), ui/EmojiPanel.kt, strings (en, ar), RecentsTest.kt, EmojiPrefsStoreTest.kt
- ime/ImeEmojiActions.kt
- feature-symbols: data/SymbolRecents.kt (new), state/PanelState.kt, state/SymbolPanelModel.kt, ui/RecentViews.kt (new), ui/ResultViews.kt, ui/SymbolPanel.kt, ui/SymbolDock.kt, strings (en, ar)

# Toolbar order editor (Settings > Layout)

- The 13 toolbar buttons were one non-wrapping row: only the dragged chip moved, the rest stayed, and the order changed only on release.
- Now a vertical list with a dotted drag handle, position number and 48dp rows. While dragging, the other rows slide aside live, the row lifts with a shadow, haptic ticks on each new slot, and the page scroll is locked until release.

Files: app/ToolbarEditor.kt, app/LayoutEditorScreen.kt, strings (en, ar)

# Mode list without Floating

- Floating is its own toolbar tile, so it is no longer part of the mode chips (Settings) or the mode cycle (Normal, One-hand right, One-hand left, Split).
- PanelMode.FLOAT stays for the floating feature itself; if the keyboard is floating, the mode key goes back to Normal.

Files: core/KeyboardPrefs.kt (PanelMode.selectable), app/LookSettingsScreen.kt, KeyboardPrefsTest.kt

# Mode and size icons

- New icons drawn on the 24 grid: MODE (keyboard with a swap arrow), MODE_NORMAL, MODE_ONE_RIGHT, MODE_ONE_LEFT (compact keyboard with a ghost of the full one), MODE_SPLIT, and a new RESIZE (keyboard with a grow arrow).
- The mode tile in the shortcuts panel now shows the icon of the current mode (ime/ToolbarIcons.kt PanelMode.icon, TileSpecs.kt).

Files: designsystem/icons/SpaceIcon.kt, SpaceIconsToolbar.kt, ime/ToolbarIcons.kt, ime/TileSpecs.kt
