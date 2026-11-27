package com.spacekeyboard.keyboard.autofix

private class Fix(val original: String, val corrected: String, val boundary: String)

class AutoFixEngine(
    private val model: ErrorModel,
    var config: AutoFixConfig = AutoFixConfig(),
    private val today: () -> Int = { 0 },
) {
    private val tracker = WordTracker()
    var neighbors: KeyNeighbors = KeyNeighbors.Unknown
    private val learner = Learner(model, today) { neighbors }
    private var learn = false
    private var fix = false
    private var parked: Parked? = null
    private var pending: Fix? = null

    private val learning: Boolean get() = config.enabled && learn
    private val fixing: Boolean get() = config.enabled && fix

    fun setField(mayLearn: Boolean, mayFix: Boolean) {
        reset()
        learn = mayLearn
        fix = mayFix
    }

    fun reset() {
        settle()
        tracker.clear()
        parked = null
    }

    fun opaque() {
        reset()
        tracker.opaque = true
    }

    fun letter(key: Char, zone: Int, around: Surround): Char {
        settle()
        parked = null
        if (!config.enabled || tracker.opaque) return key
        var prefix = tracker.word()
        if (hasRules(prefix, key, zone) && !matches(prefix, around, true)) {
            tracker.clear()
            prefix = ""
        }
        val applied = if (fixing && config.live) choose(prefix, key, zone) else null
        if (learning && zone != Zone.NONE) model.press(key, zone)
        return track(prefix, key, zone, applied)
    }

    fun boundary(text: String, around: Surround): Edit? {
        settle()
        val had = tracker.chars.isNotEmpty() && !tracker.opaque
        var edit: Edit? = null
        if (had && matches(tracker.word(), around, true)) {
            edit = if (fixing && config.words) wordFix(text) else null
            if (edit == null && learning) learner.finish(tracker)
            parked = if (edit == null && text.length == 1) tracker.park(text) else null
        } else {
            parked = null
        }
        tracker.clear()
        return edit
    }

    fun delete(around: Surround): Edit? {
        val last = pending
        pending = null
        if (last != null) return undoFix(last, around)
        val back = parked
        parked = null
        if (back != null && tracker.chars.isEmpty() && learning) {
            tracker.restore(back)
            return null
        }
        forgetLast(around)
        return null
    }

    private fun hasRules(prefix: String, key: Char, zone: Int): Boolean =
        model.contexts.candidates(Spot(prefix, key)).isNotEmpty() || model.zones.candidates(Cell(key, zone)).isNotEmpty()

    private fun choose(prefix: String, key: Char, zone: Int): Pair<Char, Applied>? {
        val veto = tracker.veto
        if (veto != null && veto.key == key && veto.size == tracker.size) return null
        val viaContext = model.contextFix(prefix, key, config.level)
        val target = viaContext ?: model.zoneFix(key, zone, config.level) ?: return null
        if (target == key || !Words.isLetter(target)) return null
        return target to Applied(viaContext != null, prefix)
    }

    private fun track(prefix: String, key: Char, zone: Int, applied: Pair<Char, Applied>?): Char {
        val watch = if (model.contexts.candidates(Spot(prefix, key)).isNotEmpty()) prefix else null
        val shown = applied?.first ?: key
        val tc = TypedChar(shown, key, zone, tracker.correcting, applied?.second, watch)
        tracker.add(tc)
        tracker.veto = null
        tracker.justApplied = if (applied != null) tc else null
        return shown
    }

    private fun wordFix(text: String): Edit? {
        if (tracker.episodes.isNotEmpty()) return null
        val word = tracker.word()
        val right = model.wordFix(word, config.level)?.takeIf { it != word } ?: return null
        pending = Fix(word, right, text)
        return Edit(word.length, right)
    }

    private fun undoFix(done: Fix, around: Surround): Edit? {
        val tail = done.corrected + done.boundary
        if (done.boundary.isEmpty() || !around.before(tail.length).endsWith(tail)) {
            accept(done)
            return null
        }
        if (learning) model.words.update(done.original, done.corrected) { it.withUndone(today()) }
        model.touch()
        tracker.restore(Parked(done.original.map { TypedChar(it, it, Zone.NONE, retyped = true) }, emptyList(), ""))
        return Edit(tail.length, done.original)
    }

    private fun forgetLast(around: Surround) {
        if (tracker.chars.isEmpty() || tracker.opaque) return
        if (!matches(tracker.word(), around, true)) {
            tracker.clear()
            return
        }
        val gone = tracker.pop() ?: return
        if (gone === tracker.justApplied) undoLive(gone)
        tracker.justApplied = null
    }

    private fun undoLive(gone: TypedChar) {
        val how = gone.applied ?: return
        if (learning) {
            if (how.viaContext) {
                model.contexts.update(Spot(how.prefix, gone.key), gone.ch) { it.withUndone(today()) }
            } else {
                model.zones.update(Cell(gone.key, gone.zone), gone.ch) { it.withUndone(today()) }
            }
            model.touch()
        }
        tracker.veto = Veto(gone.key, tracker.size)
    }

    private fun matches(word: String, around: Surround, checkAfter: Boolean): Boolean {
        if (word.isEmpty()) return true
        val text = around.before(word.length + 1)
        if (!text.endsWith(word) || Words.joins(text, word)) return false
        return !checkAfter || around.after(1).firstOrNull()?.let(Words::isLetter) != true
    }

    private fun settle() {
        val last = pending ?: return
        pending = null
        accept(last)
    }

    private fun accept(done: Fix) {
        if (!learning) return
        model.words.update(done.original, done.corrected) { it.withApplied(today()) }
        model.touch()
    }
}
