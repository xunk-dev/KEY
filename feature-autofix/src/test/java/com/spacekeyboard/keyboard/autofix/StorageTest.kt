package com.spacekeyboard.keyboard.autofix

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StorageTest {
    private fun sample(): ErrorModel {
        val model = ErrorModel()
        repeat(3) { Typing(AutoFixConfig(live = false, words = false), model).zones('ك' to 4, 'ت' to 4, 'ا' to 4, 'ي' to 8).back().type("ب").space() }
        return model
    }

    @Test
    fun modelSurvivesAnEncodeDecodeRoundTrip() {
        val model = sample()
        val copy = ModelCodec.decode(ModelCodec.encode(model))
        assertEquals(model.contexts.entries(), copy.contexts.entries())
        assertEquals(model.zones.entries(), copy.zones.entries())
        assertEquals(model.words.entries(), copy.words.entries())
        assertEquals(model.presses, copy.presses)
    }

    @Test
    fun damagedLinesAreSkippedAndGoodOnesKept() {
        val text = listOf("v1", "c\tكتا\tي\tب\t1\t0\t0\t0\t5", "c\tbroken", "z\tk\t99\tl\t1\t0\t0\t0\t5", "w\tكتاي\tكتاب\tx\t0\t0\t0\t0", "w\tteh\tthe\t2\t0\t0\t0\t7")
        val model = ModelCodec.decode(text.joinToString("\n"))
        assertEquals(1, model.contexts.size)
        assertEquals(0, model.zones.size)
        assertEquals(2, model.words.rule("teh", "the")?.fixed)
        assertNull(model.words.rule("كتاي", "كتاب"))
    }

    @Test
    fun unknownOrMissingDataGivesAnEmptyModel() {
        assertTrue(ModelCodec.decode(null).isEmpty())
        assertTrue(ModelCodec.decode("v9\nw\ta\tb\t1\t0\t0\t0\t1").isEmpty())
    }

    @Test
    fun storeSavesLoadsAndClears() {
        val fake = FakePreferences()
        val store = ModelStore(fake.prefs)
        store.save(sample())
        assertFalse(store.load().isEmpty())
        store.clear()
        assertTrue(store.load().isEmpty())
    }

    @Test
    fun settingsDefaultToOnAndBalanced() {
        val store = AutoFixPrefsStore(FakePreferences().prefs)
        assertEquals(AutoFixConfig(), store.config())
    }

    @Test
    fun settingsPersistAndNotifyObservers() {
        val fake = FakePreferences()
        val store = AutoFixPrefsStore(fake.prefs)
        var calls = 0
        val subscription = store.observe { calls++ }
        store.setEnabled(false)
        store.setLevel(AutoFixLevel.BOLD)
        store.setLive(false)
        store.setWords(false)
        store.bumpEpoch()
        assertEquals(AutoFixConfig(false, AutoFixLevel.BOLD, false, false), AutoFixPrefsStore(fake.prefs).config())
        assertEquals(1, store.epoch())
        assertEquals(5, calls)
        subscription.close()
        store.setEnabled(true)
        assertEquals(5, calls)
    }

    @Test
    fun trimDropsOldWeakRulesAndKeepsStrongOnes() {
        val model = ErrorModel()
        model.words.update("weak", "x") { Rule(fixed = 1, day = 1) }
        model.words.update("strong", "y") { Rule(fixed = 9, day = 1) }
        model.trim(today = 400)
        assertNull(model.words.rule("weak", "x"))
        assertEquals(9, model.words.rule("strong", "y")?.fixed)
    }

    @Test
    fun trimCapsTheTableSize() {
        val model = ErrorModel()
        repeat(ErrorModel.MAX_WORDS + 50) { i -> model.words.update("w$i", "x") { Rule(fixed = 1 + i % 5, day = 100) } }
        model.trim(100)
        assertEquals(ErrorModel.MAX_WORDS, model.words.size)
    }

    @Test
    fun learnedListShowsStrongestFirstAndForgetRemovesIt() {
        val model = sample()
        val list = LearnedView.list(model, AutoFixLevel.BALANCED)
        assertTrue(list.any { it.kind == Kind.WORD && it.from == "كتاي" && it.to == "كتاب" })
        assertTrue(list.any { it.kind == Kind.LETTER && it.from == "كتاي" && it.to == "كتاب" })
        val item = list.first { it.kind == Kind.WORD }
        LearnedView.forget(model, item)
        assertNull(model.words.rule(item.from, item.to))
        list.filter { it.kind != Kind.WORD }.forEach { LearnedView.forget(model, it) }
        assertTrue(model.isEmpty())
    }
}
