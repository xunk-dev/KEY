# Convert numbers and dates, fix Arabic spelling slips

- Suggestion bar: after a number it offers the other digits (123 or ١٢٣, Persian too). After a date (26/11/2026, 2026-11-26, 1448/6/16هـ) it offers the Hijri or Gregorian date in the same and the other digit style, then the digit swap. Tap replaces the whole token; order, separator, padding and the هـ / م marker are kept or switched.
- Hijri uses the Umm al-Qura table for 1318 to 1500 AH (1900 to 2077). Checked day by day against the JDK. Outside that range, or for impossible dates, only digits are offered. Local moon sighting can differ by a day.
- Spelling: on space, an Arabic word (3+ letters, no marks, not in the dictionary or your learned words) is replaced when exactly one spelling is known after at most two changes: final ه/ة, ى/ي, hamza forms (أ إ آ ا ؤ ئ ء). Prefixes (و ف ب ل ك ال وال بال لل) are kept. Delete right after restores the word and it is not fixed again.
- Settings > Typing: "Convert numbers and dates", "Fix Arabic spelling slips" (both on).
- Code: core/calendar, core/text (Digits, ConvertOffers, ArabicSpelling), ime/SpellingStep, KeyTyper, SuggestionController, FieldSetup, TypingExtras, ToolbarContent, KeyboardPrefs(+Store), app/TypingSection, strings (en, ar).
- Tests: HijriCalendarTest, ConvertOffersTest, ArabicSpellingTest, KeyboardPrefsStoreConvertTest. The core tests were compiled and run (31 pass). ime and app code were not compiled here (no Android SDK); scripts/check-all.sh passes.
