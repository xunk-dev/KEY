package com.spacekeyboard.keyboard.autofix

object EditDistance {
    fun between(a: String, b: String): Int {
        val rows = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) rows[i][0] = i
        for (j in 0..b.length) rows[0][j] = j
        for (i in 1..a.length) for (j in 1..b.length) rows[i][j] = cell(rows, a, b, i, j)
        return rows[a.length][b.length]
    }

    private fun cell(rows: Array<IntArray>, a: String, b: String, i: Int, j: Int): Int {
        val cost = if (a[i - 1] == b[j - 1]) 0 else 1
        var best = minOf(rows[i - 1][j] + 1, rows[i][j - 1] + 1, rows[i - 1][j - 1] + cost)
        if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) best = minOf(best, rows[i - 2][j - 2] + 1)
        return best
    }
}
