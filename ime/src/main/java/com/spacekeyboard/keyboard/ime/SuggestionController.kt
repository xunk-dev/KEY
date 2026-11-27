package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.suggest.DialectTracker
import com.spacekeyboard.keyboard.core.suggest.FieldKind
import com.spacekeyboard.keyboard.core.suggest.GlideCandidate
import com.spacekeyboard.keyboard.core.suggest.GlideDecoder
import com.spacekeyboard.keyboard.core.suggest.GlideKey
import com.spacekeyboard.keyboard.core.suggest.GlidePoint
import com.spacekeyboard.keyboard.core.suggest.KeyPrior
import com.spacekeyboard.keyboard.core.suggest.ScopedStore
import com.spacekeyboard.keyboard.core.suggest.Suggester
import com.spacekeyboard.keyboard.core.suggest.SuggestionStore
import com.spacekeyboard.keyboard.core.suggest.TextContext
import com.spacekeyboard.keyboard.core.text.ArabicSpelling
import com.spacekeyboard.keyboard.core.text.ConvertOffers
import com.spacekeyboard.keyboard.core.text.Conversion
import com.spacekeyboard.keyboard.core.text.SpellFix
import com.spacekeyboard.keyboard.core.text.Tashkeel
import com.spacekeyboard.keyboard.ime.layout.Shift

class SuggestionController(
    private val context: Context,
    private val connection: () -> InputConnection?,
    private val settle: () -> Unit,
    editor: () -> EditorInfo? = { null },
    val textOffer: ClipTextOffer? = null,
    val otpOffer: OtpOffer? = null,
    private val live: TypingExtras = TypingExtras(),
    emojiHint: (String) -> String? = { null },
    private val onEmoji: (String) -> Unit = {},
) {
    private val store = SuggestionStore(context)
    private val stats = store.load()
    private val seed = store.seed()
    private val dialects = store.dialects()
    private val tracker = DialectTracker(dialects)
    private val suggester = Suggester(seed, stats, store.seedPairs(), dialects)
    private val keyPrior = KeyPrior(seed, stats)
    private val decoder = GlideDecoder()
    private val seedCandidates by lazy { seed.map { GlideCandidate(it.lowercase(), GlideDecoder.BASE_WEIGHT) } }
    private val baseWords: Set<String> = HashSet(seed).also { it.addAll(dialects.words()) }
    private val spelling = ArabicSpelling { word -> !stats.isBlocked(word) && (word in baseWords || stats.words.containsKey(word)) }
    private var glided: String? = null
    private var glideAlternatives: List<String> = emptyList()
    private var unsaved = 0
    private var saving = false
    private val vocab = PersonalVocabulary(ScopedStore(context), live)
    private val emojis = EmojiOffer(emojiHint)

    val imageOffer = ClipImageOffer(context, connection, editor)

    var items by mutableStateOf<List<String>>(emptyList())
        private set

    var composing by mutableStateOf(false)
        private set

    var autofill by mutableStateOf<List<View>>(emptyList())

    var converts by mutableStateOf<List<Conversion>>(emptyList())
        private set

    val converting: Boolean
        get() = converts.isNotEmpty()

    var marksAvailable by mutableStateOf(false)
        private set

    var marksOpen by mutableStateOf(false)
        private set

    val offerVisible: Boolean
        get() = imageOffer.visible && !composing && autofill.isEmpty()

    val textVisible: Boolean
        get() = textOffer?.offer != null && !composing && autofill.isEmpty()

    val otpVisible: Boolean
        get() = otpOffer?.code != null

    private val watch: AutoCloseable = store.observe(::onStoreChanged)

    fun spellFix(before: String): SpellFix? = spelling.fix(before)

    fun beginOtp() = otpOffer?.begin()

    fun toggleMarks() {
        marksOpen = !marksOpen
    }

    fun mark(mark: String) {
        val target = connection() ?: return
        settle()
        target.commitText(mark, 1)
    }

    private fun syncMarks(before: String) {
        marksAvailable = Tashkeel.canMark(before)
        if (!marksAvailable) marksOpen = false
    }

    fun hide() {
        flush()
        emojis.clear()
        tracker.reset()
        imageOffer.stop()
        textOffer?.sync(false)
        otpOffer?.stop()
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
        otpOffer?.refresh()
        val target = connection()
        if (offerConversions(extras, target)) return
        imageOffer.sync(extras.imageOffers && target != null)
        textOffer?.sync(extras.copyOffers && target != null)
        if (!extras.suggestions || target == null) {
            items = emptyList()
            emojis.clear()
            composing = false
            syncMarks("")
            return
        }
        val before = target.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty()
        syncMarks(before)
        if (keepGlideChoices(before)) return
        if (boundary && live.learning) learnLast(before)
        val parsed = TextContext.parse(before)
        composing = parsed.prefix.isNotEmpty()
        val base = suggester.suggest(parsed, includeTyped = true, dialect = tracker.detect(before), personal = vocab.personal())
        items = emojis.merge(base, parsed.prefix, extras.emojiSuggestions && atWordEnd(target))
    }

    private fun atWordEnd(target: InputConnection): Boolean =
        target.getTextAfterCursor(1, 0)?.firstOrNull()?.let { !TextContext.isWordChar(it) } ?: true

    fun remove(word: String) {
        if (converts.any { it.text == word } || emojis.owns(word)) return
        stats.block(word)
        vocab.remove(word)
        unsaved++
        flush()
        val before = connection()?.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty()
        items = suggester.suggest(TextContext.parse(before), includeTyped = true, dialect = tracker.detect(before), personal = vocab.personal())
        Toast.makeText(context, context.getString(R.string.suggestion_removed, word), Toast.LENGTH_SHORT).show()
    }

    fun glide(path: List<GlidePoint>, keys: List<GlideKey>, shift: Shift = Shift.OFF) {
        val target = connection() ?: return
        val words = decoder.decode(path, keys, glideCandidates())
        val best = words.firstOrNull() ?: return
        settle()
        val before = target.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty()
        val context = TextContext.parse(before)
        val lead = if (before.isNotEmpty() && before.last().isLetterOrDigit()) " " else ""
        val capital = shift != Shift.OFF || GlideStyle.startsSentence(before)
        val text = GlideStyle.styled(best, capital, shift == Shift.LOCKED)
        target.commitText("$lead$text ", 1)
        if (live.learning) remember(context.prefix.ifEmpty { context.previous }, best)
        glided = text
        glideAlternatives = words.drop(1).map { GlideStyle.styled(it, capital, shift == Shift.LOCKED) }
        composing = false
        items = glideAlternatives
    }

    private fun glideCandidates(): List<GlideCandidate> {
        val own = if (vocab.personal().global) stats.words.filterKeys { !stats.isBlocked(it) }.map { (word, n) -> GlideCandidate(word, GlideDecoder.weightOf(n)) } else emptyList()
        return seedCandidates.filter { !stats.isBlocked(it.word) } + own
    }

    private fun keepGlideChoices(before: String): Boolean {
        val word = glided ?: return false
        if (!before.endsWith("$word ")) {
            glided = null
            return false
        }
        composing = false
        items = glideAlternatives
        return true
    }

    private fun offerConversions(extras: TypingExtras, target: InputConnection?): Boolean {
        converts = emptyList()
        if (!extras.convertOffers || target == null) return false
        val before = target.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty()
        val found = ConvertOffers.forTail(before)
        if (found.isEmpty()) return false
        converts = found
        items = found.map { it.text }
        composing = false
        syncMarks(before)
        return true
    }

    private fun convert(choice: Conversion) {
        val target = connection() ?: return
        settle()
        target.beginBatchEdit()
        target.deleteSurroundingText(choice.remove, 0)
        target.commitText(choice.text, 1)
        target.endBatchEdit()
        converts = emptyList()
        items = emptyList()
        composing = false
    }

    fun accept(word: String) {
        converts.firstOrNull { it.text == word }?.let { return convert(it) }
        if (emojis.owns(word)) return acceptEmoji(word)
        val target = connection() ?: return
        settle()
        val text = target.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty()
        val swap = glided?.takeIf { text.endsWith("$it ") }
        val context = TextContext.parse(if (swap == null) text else text.dropLast(swap.length + 1))
        glided = null
        target.beginBatchEdit()
        target.deleteSurroundingText(context.prefix.length + (swap?.let { it.length + 1 } ?: 0), 0)
        target.commitText("$word ", 1)
        target.endBatchEdit()
        if (live.learning) remember(context.previous, word)
        composing = false
        items = suggester.suggest(TextContext("", word), dialect = tracker.detect(word), personal = vocab.personal())
    }

    private fun acceptEmoji(emoji: String) {
        val target = connection() ?: return
        settle()
        val context = TextContext.parse(target.getTextBeforeCursor(WINDOW, 0)?.toString().orEmpty())
        target.commitText(" $emoji ", 1)
        if (live.learning && context.prefix.isNotEmpty()) remember(context.previous, context.prefix)
        onEmoji(emoji)
        composing = false
        items = emptyList()
        emojis.clear()
    }

    fun priorWeight(prefix: String, letter: String): Int = keyPrior.weight(prefix, letter)

    fun flush() {
        vocab.flush()
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
        vocab.learn(word)
        if (live.kind != FieldKind.PROSE) return
        stats.learn(previous, word)
        tracker.observe(word)
        unsaved++
        if (unsaved >= SAVE_EVERY) flush()
    }

    private companion object {
        const val WINDOW = 48
        const val SAVE_EVERY = 25
    }
}
