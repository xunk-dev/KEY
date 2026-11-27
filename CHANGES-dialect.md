# Dialect detection for Arabic suggestions

- core/suggest: Dialect (SHAMI, MASRI, KHALEEJI), DialectBook (word lists and next-word pairs per dialect; a word is a marker only if it belongs to exactly one dialect and sits above the == line), DialectTracker (counts marker words in the text before the cursor plus a decaying memory of committed words; it answers only when one dialect clearly leads, and forgets after about 13 neutral words)
- Suggester.suggest takes dialect (default null, so every existing call behaves as before). With a dialect: its words rank above the generic seed but below words the user learned, its next-word pairs are added, and seed words that belong to another dialect drop below plain words instead of being hidden
- SuggestionStore.dialects() loads the lists; SuggestionController feeds the tracker from committed words only while learning is on (nothing is stored; reset when the keyboard hides)
- Data: raw/dialect_shami.txt, dialect_masri.txt, dialect_khaleeji.txt (one word per line, words after == only boost), raw/pairs_shami.txt, pairs_masri.txt, pairs_khaleeji.txt (same format as pairs_ar.txt)
- Words that are also plain Arabic (أبي, دول, طب, قوي, هما, حاجة) are never markers, so formal text is not mistaken for a dialect
- Tests: DialectTest (markers, detection, fading, ranking, penalty, learned words win, pairs), DialectDataTest (reads the real lists: at least 60 marker words per dialect, no repeats, single words, sample sentences, formal text left alone)
- Not compiled or run here (no Gradle in the sandbox). The ranking and detection scenarios of the tests were checked with a line-by-line Python port and all passed; the repo checks check-comments, check-function-lines and check-lines pass
- No settings switch yet (detection is automatic); say if you want one in Settings > Typing
