# Quick diacritics bar (tashkeel)

- Long press: already in every Arabic layout (ar, ar_pair, ar_ten, ar_plain) on the ظ key: shadda is not first there, the nine marks are typed bare (no tatweel) so they attach to the letter before the cursor. Unchanged
- New quick bar: while suggestions are on and the character before the cursor is an Arabic letter or mark, a small ـَ chip sits at the end of the suggestion strip. One tap swaps the strip for eight marks (shadda, fatha, damma, kasra, sukun, three tanween). Each tap types the mark after the letter; the bar stays open so marks can be stacked (shadda then fatha). The chip closes it. It closes by itself after a space, a digit, a Latin letter or punctuation
- core/text/Tashkeel: the eight marks, the labels (tatweel + mark, so they are visible), canMark(before)
- ime: TashkeelBar (MarksBar, MarksChip), SuggestionController (marksAvailable, marksOpen, toggleMarks, mark), ToolbarContent (WordsRow, marks branch)
- Strings: marks_open, marks_close in ime, en and ar
- Tests: TashkeelTest. Not compiled or run here (no Gradle in the sandbox); scripts/check-all.sh passes
- Known limits: the chip needs a visible suggestion strip (it is hidden when suggestions are off or the strip is empty); word completion restarts after a mark because a mark ends the word (same as typing marks from the ظ key)
