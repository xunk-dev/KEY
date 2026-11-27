package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.suggest.FieldKind
import com.spacekeyboard.keyboard.core.suggest.Personal
import com.spacekeyboard.keyboard.core.suggest.ScopedStats
import com.spacekeyboard.keyboard.core.suggest.ScopedStore

internal class PersonalVocabulary(private val store: ScopedStore, private val live: TypingExtras) {
    private var stats: ScopedStats = store.load()
    private var unsaved = 0
    private var saving = false
    private val watch: AutoCloseable = store.observe(::reload)

    fun personal(): Personal {
        if (!live.learning) return Personal.NONE
        val local = live.scope?.let { stats.counts(it) }.orEmpty()
        return Personal(live.kind == FieldKind.PROSE, local)
    }

    fun learn(word: String) {
        val scope = live.scope ?: return
        stats.learn(scope, word)
        unsaved++
        if (unsaved >= SAVE_EVERY) flush()
    }

    fun remove(word: String) {
        stats.remove(word)
        unsaved++
        flush()
    }

    fun flush() {
        if (unsaved == 0) return
        saving = true
        store.save(stats)
        saving = false
        unsaved = 0
    }

    fun close() = watch.close()

    private fun reload() {
        if (saving) return
        stats = store.load()
        unsaved = 0
    }

    private companion object {
        const val SAVE_EVERY = 25
    }
}
