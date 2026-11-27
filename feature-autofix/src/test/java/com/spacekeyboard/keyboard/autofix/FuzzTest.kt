package com.spacekeyboard.keyboard.autofix

import java.util.Random
import org.junit.Assert.assertEquals
import org.junit.Test

class FuzzTest {
    private val letters = "كتابيسمنهو"

    private fun step(random: Random, session: Typing, reference: StringBuilder) {
        when (random.nextInt(10)) {
            in 0..5 -> letters[random.nextInt(letters.length)].let { session.zones(it to random.nextInt(Zone.COUNT)); reference.append(it) }
            6, 7 -> if (reference.isNotEmpty()) { session.back(); reference.setLength(reference.length - 1) } else session.back()
            8 -> { session.space(); reference.append(' ') }
            else -> { session.engine.reset() }
        }
    }

    @Test
    fun withoutRulesTheTextIsNeverAltered() {
        repeat(40) { seed ->
            val random = Random(seed.toLong())
            val session = Typing(model = ErrorModel(), mayLearn = false, mayFix = true)
            val reference = StringBuilder()
            repeat(300) { step(random, session, reference) }
            assertEquals("seed $seed", reference.toString(), session.text)
        }
    }

    @Test
    fun randomSessionsWithLearningNeverCrashAndNeverGrowTablesWithoutBound() {
        val model = ErrorModel()
        repeat(60) { seed ->
            val random = Random(1000L + seed)
            val session = Typing(AutoFixConfig(level = AutoFixLevel.BOLD), model)
            repeat(400) { step(random, session, StringBuilder()) }
        }
        model.trim(Typing.DAY)
        assertEquals(true, model.contexts.size <= ErrorModel.MAX_CONTEXTS)
        assertEquals(true, model.words.size <= ErrorModel.MAX_WORDS)
        ModelCodec.decode(ModelCodec.encode(model))
    }
}
