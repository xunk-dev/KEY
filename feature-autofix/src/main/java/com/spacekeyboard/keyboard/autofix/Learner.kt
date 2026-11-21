package com.spacekeyboard.keyboard.autofix

class Learner(private val model: ErrorModel, private val today: () -> Int) {
    fun finish(tracker: WordTracker) {
        if (tracker.opaque || tracker.chars.isEmpty()) return
        val final = tracker.chars.toList()
        val slips = tracker.episodes.mapNotNull { EpisodeReader.read(it, final) }
        val fresh = slips.filter { !it.episode.counted }
        fresh.forEach(::learnSlip)
        if (fresh.isNotEmpty()) learnWord(String(CharArray(final.size) { final[it].ch }), slips)
        tracker.episodes.forEach { it.counted = true }
        settle(final, tracker.episodes.isEmpty())
    }

    private fun learnSlip(slip: Slip) {
        if (slip !is Swap) return
        val wrong = slip.wrong
        model.learnSlip(slip.episode.prefix, wrong.key, slip.intended, wrong.zone, today())
    }

    private fun learnWord(word: String, slips: List<Slip>) {
        val raw = RawWord.rebuild(word, slips) ?: return
        if (raw.length >= ErrorModel.MIN_WORD && word.length >= ErrorModel.MIN_WORD) model.learnWord(raw, word, today())
    }

    private fun settle(final: List<TypedChar>, clean: Boolean) {
        final.forEach { tc ->
            tc.applied?.let { applied(tc, it) }
            if (tc.applied == null && tc.watch != null) keep(tc.watch, tc.key)
        }
        val word = String(CharArray(final.size) { final[it].ch })
        if (clean && model.words.candidates(word).isNotEmpty()) model.words.updateAll(word) { it.withKept() }
        model.touch()
    }

    private fun keep(prefix: String, key: Char) {
        model.contexts.updateAll(Spot(prefix, key)) { it.withKept() }
    }

    private fun applied(tc: TypedChar, how: Applied) {
        if (how.viaContext) {
            model.contexts.update(Spot(how.prefix, tc.key), tc.ch) { it.withApplied(today()) }
        } else {
            model.zones.update(Cell(tc.key, tc.zone), tc.ch) { it.withApplied(today()) }
        }
    }
}
