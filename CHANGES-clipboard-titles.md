# Clipboard: titles + swipe actions
- Swipe a card right: name it (title shown instead of the text; the text becomes a small subtitle).
- Swipe a card left: pin / unpin.
- 3-dot menu: Add title / Edit title / Remove title.
- Title is stored in the saved book (extra column, old data still loads). Max 40 chars.
- Clearing the title field and pressing Done removes the title.
- Files: ClipBook, ClipCodec, ClipSession, ClipSnapshot, ClipboardController, ui/ClipSwipe (new), ui/ClipBody, ui/ClipMenu, ui/ClipNameBar, ui/ClipNameSuggestions, ui/ClipActions, ImeClipActions, strings (en/ar), ClipTitleTest (new).

# Number row: deliberate digit swap
- Swap Arabic/English digits now needs a long, flat, unhurried swipe (>= 1.5 keys / 64dp, nearly horizontal, held >= 150ms).
- A tick shows when armed; the swap happens on release. Dragging back before release cancels (no digit typed, no swap).
- Tunables: KeyTiming.SWIPE_DIGITS_* in KeyBehavior.kt.

# Enter key follows the field + emoji in raw-key fields (Termux)
- Enter key icon now follows the field's IME action: Search, Go, Send, Next, Done, Previous; plain Enter for multiline / no action.
- Arrows and plane mirror automatically in RTL languages.
- Emoji and fancy text are now allowed in every field type (no restrictions). Clipboard history still stays off in password fields.
- Files: EnterKind (new), FieldSetup, KeyIcons, KeyPainter, LayoutSnapshot/Controller, SpaceIcon(+SEND), FieldPolicy.

# Emoji/symbols stay in place when tapped
- Tapping an emoji in the Recents list (or the empty-search recents strip) no longer moves it to the front.
- The emoji panel no longer jumps to Recents after the first pick: the shown group is pinned before the pick is recorded.
- Symbols already skipped recording while their Recents page is open.
