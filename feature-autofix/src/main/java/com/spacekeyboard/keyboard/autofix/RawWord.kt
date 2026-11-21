package com.spacekeyboard.keyboard.autofix

object RawWord {
    fun rebuild(final: String, slips: List<Slip>): String? {
        if (slips.isEmpty()) return null
        val letters = final.toMutableList()
        for (slip in slips.sortedByDescending { it.pos }) if (!undo(letters, slip)) return null
        val raw = String(letters.toCharArray())
        return raw.takeIf { EditDistance.between(it, final) == slips.size }
    }

    private fun undo(letters: MutableList<Char>, slip: Slip): Boolean {
        val pos = slip.pos
        return when (slip) {
            is Swap -> pos < letters.size && letters.set(pos, slip.wrong.ch).let { true }
            is Twist -> pos + 1 < letters.size && swap(letters, pos)
            is Extra -> pos <= letters.size && letters.add(pos, slip.ch).let { true }
            is Missing -> pos < letters.size && letters.removeAt(pos).let { true }
        }
    }

    private fun swap(letters: MutableList<Char>, pos: Int): Boolean {
        val first = letters[pos]
        letters[pos] = letters[pos + 1]
        letters[pos + 1] = first
        return true
    }
}
