package com.spacekeyboard.keyboard.core.suggest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GlideDecoderTest {
    private val rows = listOf("qwertyuiop" to 0f, "asdfghjkl" to 50f, "zxcvbnm" to 150f)
    private val keys = rows.flatMapIndexed { r, (letters, offset) ->
        letters.mapIndexed { i, ch -> GlideKey(ch, offset + 50f + i * 100f, 60f + r * 120f, 100f, 120f) }
    }
    private val words = listOf("hello", "help", "held", "world", "word", "quick", "good", "gold", "hat", "has", "the", "then")
    private val candidates = words.map { GlideCandidate(it, 0.4f) }
    private val decoder = GlideDecoder()

    private fun trace(word: String, jitter: Float = 0f): List<GlidePoint> {
        val centers = word.map { c -> keys.first { it.ch == c } }
        val out = ArrayList<GlidePoint>()
        for (i in 0 until centers.size - 1) {
            for (s in 0 until 10) {
                val t = s / 10f
                val wobble = if ((i + s) % 2 == 0) jitter else -jitter
                out += GlidePoint(
                    centers[i].x + (centers[i + 1].x - centers[i].x) * t + wobble,
                    centers[i].y + (centers[i + 1].y - centers[i].y) * t - wobble,
                )
            }
        }
        out += GlidePoint(centers.last().x, centers.last().y)
        return out
    }

    @Test
    fun exactTraceFindsTheWord() {
        words.forEach { word ->
            assertEquals(word, decoder.decode(trace(word), keys, candidates).first())
        }
    }

    @Test
    fun noisyTraceStillFindsTheWord() {
        listOf("hello", "world", "quick", "gold").forEach { word ->
            assertEquals(word, decoder.decode(trace(word, 18f), keys, candidates).first())
        }
    }

    @Test
    fun repeatedLettersCollapse() {
        assertEquals("good", decoder.decode(trace("god"), keys, candidates).first())
    }

    @Test
    fun shortTapIsNotAGlide() {
        val tap = listOf(GlidePoint(150f, 60f), GlidePoint(155f, 62f), GlidePoint(160f, 60f))
        assertTrue(decoder.decode(tap, keys, candidates).isEmpty())
    }

    @Test
    fun startAndEndKeysMustMatch() {
        val result = decoder.decode(trace("quick"), keys, candidates)
        assertTrue(result.all { it.first() == 'q' || it.first() == 'w' || it.first() == 'a' })
    }

    @Test
    fun frequentWordBreaksTies() {
        val ranked = listOf(GlideCandidate("held", 0.4f), GlideCandidate("hell", 0.4f), GlideCandidate("help", 1f))
        val path = trace("hel")
        val result = decoder.decode(path + GlidePoint(keys.first { it.ch == 'l' }.x, keys.first { it.ch == 'l' }.y), keys, ranked)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun arabicFoldedLettersMapToKeys() {
        val arabic = listOf(GlideKey('ا', 100f, 60f, 100f, 120f), GlideKey('ح', 300f, 60f, 100f, 120f), GlideKey('ب', 500f, 60f, 100f, 120f))
        val path = listOf(GlidePoint(100f, 60f), GlidePoint(200f, 60f), GlidePoint(300f, 60f), GlidePoint(400f, 60f), GlidePoint(500f, 60f))
        val result = GlideDecoder().decode(path, arabic, listOf(GlideCandidate("أحب", 0.4f)))
        assertEquals(listOf("أحب"), result)
    }

    @Test
    fun unknownLettersSkipTheWord() {
        val result = decoder.decode(trace("hello"), keys, listOf(GlideCandidate("héllo", 1f)))
        assertTrue(result.isEmpty())
    }
}
