# Unified bottom row and icons

- en, az, fa: the LANGUAGE key at the bottom-left is now OPEN_SYMBOLS, like every Arabic layout. Language switching stays in the toolbar chips.
- Icons: SYMBOLS = math symbols box, KEYS = three-row keyboard, NUMPAD (new) = number pad.
- NUMBERS key draws NUMPAD on letter layouts and KEYS (back to letters) on the numbers and symbols pages. SYMBOLS key ("#=") draws the symbols icon.
- Tests updated: LayoutFilesTest and ArabicGapsTest now expect OPEN_SYMBOLS.

## Pages

- Icons depend on the page (PagePlace): letters show NUMPAD on the numbers key and the symbols icon on the symbols key; the numbers page shows KEYS on the back key and the symbols icon on "#="; the symbols page shows NUMPAD on its "123" key and KEYS on the back key.
- Numbers page: left column, Enter and Delete are now 14% like the other pages (columns 14 / 24 / 24 / 24 / 14).
- New test NumbersPageSizesTest.
