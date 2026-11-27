# Customize screen: tabs over a live keyboard

Source design: design/reference/customize-v4.html.

- CustomizeScreen: header (back, title, Done), scrolling tab content, glass pill tab bar, and the real KeyboardPreview fixed at the bottom of every tab (always full, including the bottom row).
- Tabs (CustomizeTab): Style = theme grid and follow-system; Colors = slot tabs and HSV sliders; Keys = language chips, drag-to-swap on the bottom keyboard (editing mode only on this tab), reset layout, Arabic letter arrangement; Size = mode tiles, key size, height, letters; Tools = toolbar order, number row, suggestions; Touch = vibration, sound, key bubble.
- DockRoutes: the dock is hidden on CUSTOMIZE (Back and Done return Home). Old KEYBOARD_COLORS, KEYBOARD_LOOK and EDITOR screens remain but are no longer linked from Customize.
- SpaceIcon.VIBRATE added (path from the design).
- Key functions (Keys tab, KeyFnSection): pick a bottom-row key and give it a function: Symbols, Numbers, Language, Emoji, Clipboard, Translate, Comma or Period. core: KeyFn, LayoutSpec.withFunctions, KeyboardPrefs.keyFns (stored as keyfn.<layout>), new KeyActions EMOJI, CLIPBOARD, TRANSLATE (KeyAction.isTool). ime: KeyActions.openTool (ToolKeys.kt), icons, chip role and square shape for the new actions. The choice follows the key when it is dragged; Reset layout clears it. Test: KeyFnTest.
- Not in this step: adding or removing keys, per-key width, undo, hold-to-compare, glass/flat/soft/paper/mech/neon key styles, hex field and color wheel.
- Not compiled here (no Android SDK).
