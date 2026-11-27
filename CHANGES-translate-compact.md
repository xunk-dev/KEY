# Compact translate panel (design D)

- `TranslateCapsule.kt`: `TranslateMain` rebuilt. A slim rail (close, options) sits at the start; the card has an accent bar, a source line tagged `AUTO` and a result line tagged with the target code (`EN ▾`). Tap the tag for the language list, long-press for the quick list. The action (paste / insert / AI translate) is one round button at the end.
- `PanelDeck.kt`: `TRANSLATE_DP` 120 -> 64.
- Full-screen translate (`compact = false`) still scrolls and shows the whole result.
