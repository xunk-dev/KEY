# Emoji search layout

- The search strip keeps only the close button and the query pill.
- Results now sit in the number row, with the same height, so the keyboard does not grow.
- The last visible result fades at the edge so a cut-off key reads as scrollable.
- With an empty query the row shows recent emoji.
- Long press on a result with skin tones opens the tone keys in the same row.
- Without a number row, or on the numbers and symbols pages, the old inline results are used.

Files

- feature-emoji/ui/EmojiResultsRow.kt (new)
- feature-emoji/ui/EmojiSearchPanel.kt (resultsBelow parameter)
- ime/KeyboardStack.kt (new)
- ime/EmojiPanelContent.kt, ime/PanelDeck.kt, ime/SpaceInputService.kt, ime/layout/LayoutSnapshot.kt

# Section naming (clipboard)

- While naming a section the toolbar is hidden and the naming bar takes its 46dp slot, so the keyboard keeps its normal height (before: toolbar plus a fixed 90dp panel).
- Naming bar: name field with a folder icon, a done key and a cancel key.
- Suggested names (Work, Personal, Links, Numbers, Addresses, Prayers) sit in the number row; names that already exist as sections are hidden. Tapping one fills the field.
- Without a number row, or on the numbers and symbols pages, only the naming bar shows.

Files

- feature-clipboard/ui/ClipNameBar.kt, ClipNameSuggestions.kt (new)
- feature-clipboard/ui/ClipPanel.kt, ClipActions.kt, ClipSession.kt (suggest), ClipboardController.kt, strings (en, ar), ClipSessionTest.kt
- feature-emoji/ui/EmojiResultsRow.kt (edgeFade is now public)
- ime/KeyRowContent.kt (new, replaces EmojiResultsContent), ImeClipActions.kt, PanelDeck.kt, SpaceInputService.kt

# Number row no longer shows through result strips

- While emoji search or clipboard section naming puts its strip over the number row, the keyboard now clips that row out (KeyboardView.topRowClipPx), so no digits or key shapes appear under the emoji or the suggested names.
- Touches on the hidden row are ignored, so nothing can be typed through the strip.
- Digits stay available from the numbers page (123).

Files: ime/KeyboardView.kt, ime/KeyboardStack.kt, ime/PanelDeck.kt

# Number pad on the 123 key

- The 123 key now only opens a number pad (label is plain "123" in every language; no more "?123").
- Pad layout: symbol column on the left (+  -  ×  and the symbols key), digits 3x3, a right column with delete, dot, comma and enter, and a bottom row with the symbols key, ABC (back to letters), 0 and space.
- Long press on the symbol keys gives more (± = − – _ ÷ * /).
- Digits follow the number-row choice (Latin or native) and keep the other style on long press.
- The symbols key opens the symbols page, which now holds the common symbols (@ # $ % & ( ) / = * " ' : ; ! ? ~ [ ] { } < >) with the rest on long press (currencies, © ® ™, √ π Δ, « »). Its "123" key returns to the pad.
- The pad keeps four rows; with the number row on it simply grows taller (no extra symbol row).

Files: core/res/raw/layout_num.json, layout_sym.json, core/layout/PageLabels.kt, PageRows.kt, ime/layout/LayoutController.kt, tests PageRowsTest.kt, PageLabelsTest.kt

# Free movement between the top bar and the tiles page

- Every tile (mode, theme, decorate, edit, resize, floating keyboard, dim, calculator, incognito, app settings) plus emoji, clipboard and translate can live either on the top bar next to the emoji button or on the tiles page, and move back at any time.
- Tiles page: the "Arrange" chip opens an arrange view with two sections, "Top bar" and "Tiles". Tapping a tile moves it to the other place (up/down arrow badge). Hold and drag still reorders. Dragging on the bar itself still reorders bar buttons.
- The same spec feeds the large square (icon + name) in the tiles page and the compact icon button in the bar, so state (selected, open) and behaviour are identical in both places.
- Guards: the tools button (opens the tiles page) and the globe and language buttons always stay on the bar; the bar holds at most 6 movable items (a message shows when full); a tile that is hidden gets restored when it is moved back to the page; if every tile is on the bar the page explains that and still shows the Arrange chip; old saved bar layouts keep working (three-part encoding only appears once something is moved off the bar).

Files: core/shortcuts/ShortcutTiles.kt (EMOJI, CLIPBOARD, TRANSLATE tiles), core/toolbar/BarArrangement.kt (bring, sendOff, off list, MAX_MOVABLE), ime/TileSpecs.kt (new), ime/ShortcutsPanelContent.kt, ime/ToolbarContent.kt, ime/SpaceInputService.kt, ime/SpaceInputServiceViews.kt, strings (en, ar), BarArrangementTest.kt

# Decorate opens in the current language

- Opening decorate now starts in Arabic when an Arabic (or Persian) keyboard is active, and in English otherwise. Before, it always reopened in whichever language was used last (English by default).
- If the selected text or the word before the cursor is clearly in the other script, the style set follows the text so the styles actually apply.
- The language toggle inside the panel still works.

Files: feature-decorate/DecorController.kt (open takes a language), ime/PanelToggles.kt, ime/SpaceInputService.kt
