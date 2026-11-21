package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.suggest.KeyPrior
import com.spacekeyboard.keyboard.core.suggest.Suggester
import com.spacekeyboard.keyboard.core.suggest.SuggestionStore
import com.spacekeyboard.keyboard.core.suggest.TextContext
import com.spacekeyboard.keyboard.core.suggest.WordStats

class SuggestionController(
    private val context: Context,
    private val connection: () -> InputConnection?,
    private val settle: () -> Unit,
) {
    private val store = SuggestionStore(context)
    private val stats = store.load()
    private val seed = store.seed()
    private val suggester = Suggester(seed, stats, store.seedPairs())
    private val keyPrior = KeyPrior(seed, stats)
    private var unsaved = 0
    private var learning = false
    private var saving = false

    var items by mutableStateOf<List<String>>(emptyList())
        private set

    var autofill by mutableStateOf<List<View>>(emptyList())

    init {
        store.observe(::onStoreChanged)
    }

    private fun onStoreChanged() {
        if (saving) return
        stats.forget()
        if (store.has()) store.load().let { loaded ->
            stats.words.putAll(loaded.words)
            stats.pairs.putAll(loaded.pairs)
            stats.blocked.addAll(loaded.blocked)
        }
        unsaved = 0
    }

    fun update(extras: TypingExtras, boundary: Boolean) {
        learning = extras.learning
        val target = connection()
        if (!extras.suggestions || target == null) {
            items = emptyList()
            return
        }
        val before = target.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty()
        if (boundary && learning) learnLast(before)
        items = suggester.suggest(TextContext.parse(before), includeTyped = true)
    }

    fun remove(word: String) {
        stats.block(word)
        unsaved++
        flush()
        val before = connection()?.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty()
        items = suggester.suggest(TextContext.parse(before), includeTyped = true)
        Toast.makeText(context, context.getString(R.string.suggestion_removed, word), Toast.LENGTH_SHORT).show()
    }

    fun accept(word: String) {
        val target = connection() ?: return
        settle()
        val context = TextContext.parse(target.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty())
        target.beginBatchEdit()
        target.deleteSurroundingText(context.prefix.length, 0)
        target.commitText("$word ", 1)
        target.endBatchEdit()
        if (learning) remember(context.previous, word)
        items = suggester.suggest(TextContext("", word))
    }

    fun priorWeight(prefix: String, letter: String): Int = keyPrior.weight(prefix, letter)

    fun flush() {
        if (unsaved == 0) return
        saving = true
        store.save(stats)
        saving = false
        unsaved = 0
    }

    private fun learnLast(before: String) {
        val context = TextContext.parse(before.trimEnd())
        if (context.prefix.isNotEmpty()) remember(context.previous, context.prefix)
    }

    private fun remember(previous: String?, word: String) {
        stats.learn(previous, word)
        unsaved++
        if (unsaved >= SAVE_EVERY) flush()
    }

    private companion object {
        const val WINDOW = 48
        const val SAVE_EVERY = 25
    }
}
