package com.spacekeyboard.keyboard.emoji

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import com.spacekeyboard.keyboard.emoji.data.EmojiPrefsStore
import com.spacekeyboard.keyboard.emoji.data.EmojiRepository
import com.spacekeyboard.keyboard.emoji.data.EmojiSet
import com.spacekeyboard.keyboard.emoji.data.EmojiTypefaces
import com.spacekeyboard.keyboard.emoji.model.EmojiGroup
import com.spacekeyboard.keyboard.emoji.model.EmojiHints
import com.spacekeyboard.keyboard.emoji.model.EmojiSearch
import com.spacekeyboard.keyboard.emoji.model.Recents
import com.spacekeyboard.keyboard.emoji.model.SkinTone
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EmojiController(context: Context) {
    private val app = context.applicationContext
    private val store = EmojiPrefsStore(app)
    private val repository = EmojiRepository(app)
    private val worker: ExecutorService = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val listeners = LinkedHashSet<(EmojiSnapshot) -> Unit>()
    private var subscription: AutoCloseable? = null
    private var search: EmojiSearch? = null
    private var searchBuilding = false
    private var hints: EmojiHints? = null
    private var hintsBuilding = false
    private var generation = 0
    private var learning = true

    var snapshot: EmojiSnapshot = EmojiSnapshot(recents = store.recents(), tone = store.tone(), set = store.set())
        private set

    fun start() {
        if (subscription != null) return
        subscription = store.observe(::onPrefs)
        onPrefs()
    }

    fun stop() {
        subscription?.close()
        subscription = null
        worker.shutdownNow()
    }

    fun subscribe(listener: (EmojiSnapshot) -> Unit): AutoCloseable {
        listeners += listener
        listener(snapshot)
        return AutoCloseable { listeners -= listener }
    }

    fun preload() {
        if (snapshot.load == LoadState.IDLE || snapshot.load == LoadState.FAILED) load()
    }

    fun open() {
        if (!snapshot.allowed) return
        update(snapshot.copy(mode = EmojiMode.BROWSE))
        preload()
    }

    fun close() = update(snapshot.copy(mode = EmojiMode.CLOSED, query = "", results = emptyList()))

    fun toggle() = if (snapshot.open) close() else open()

    fun startSearch() {
        update(snapshot.copy(mode = EmojiMode.SEARCH, query = "", results = emptyList(), searchReady = search != null))
        preload()
        buildSearch()
    }

    fun stopSearch() = update(snapshot.copy(mode = EmojiMode.BROWSE, query = "", results = emptyList()))

    fun type(text: String) = setQuery(snapshot.query + text)

    fun backspace() {
        if (snapshot.query.isNotEmpty()) setQuery(snapshot.query.dropLast(1))
    }

    fun selectGroup(id: String) = update(snapshot.copy(group = id))

    fun setTone(tone: SkinTone) = store.setTone(tone)

    fun setSet(set: EmojiSet) = store.setSet(set)

    fun clearRecents() = store.clearRecents()

    fun removeRecent(text: String) = store.removeRecent(text)

    fun record(text: String) {
        pinGroup()
        if (!learning) return
        if (Recents.keepsOrder(recentsShown(), snapshot.recents, text)) return
        store.record(text)
    }

    private fun recentsShown(): Boolean =
        if (snapshot.mode == EmojiMode.SEARCH) snapshot.query.isEmpty() else snapshot.activeGroup == RECENTS_GROUP

    private fun pinGroup() {
        val shown = snapshot.activeGroup
        if (snapshot.group != shown) update(snapshot.copy(group = shown))
    }

    fun hint(word: String): String? = hints?.find(word)?.forTone(snapshot.tone)

    fun setLearning(value: Boolean) {
        learning = value
    }

    fun setAllowed(value: Boolean) {
        val mode = if (value) snapshot.mode else EmojiMode.CLOSED
        update(snapshot.copy(allowed = value, mode = mode))
    }

    private fun setQuery(query: String) =
        update(snapshot.copy(query = query, results = search?.find(query).orEmpty()))

    private fun onPrefs() {
        val set = store.set()
        val changed = set != snapshot.set
        update(snapshot.copy(recents = store.recents(), tone = store.tone(), set = set))
        if (changed && snapshot.load != LoadState.IDLE) load()
    }

    private fun load() {
        val token = ++generation
        val set = snapshot.set
        update(snapshot.copy(load = LoadState.LOADING))
        worker.execute {
            val result = runCatching {
                val typeface = EmojiTypefaces.load(app, set)
                Loaded(typeface, repository.groups(typeface))
            }
            main.post { if (token == generation) finishLoad(result.getOrNull()) }
        }
    }

    private fun finishLoad(loaded: Loaded?) {
        search = null
        hints = null
        update(
            snapshot.copy(
                load = if (loaded == null) LoadState.FAILED else LoadState.READY,
                groups = loaded?.groups.orEmpty(),
                typeface = loaded?.typeface,
                results = emptyList(),
                searchReady = false,
            ),
        )
        if (snapshot.mode == EmojiMode.SEARCH) buildSearch()
        buildHints()
    }

    private fun buildHints() {
        if (hintsBuilding || snapshot.load != LoadState.READY) return
        hintsBuilding = true
        val token = generation
        val emojis = snapshot.groups.flatMap { it.emojis }
        worker.execute {
            val built = runCatching { EmojiHints(emojis, repository.names(), repository.hints()) }.getOrNull()
            main.post {
                hintsBuilding = false
                if (token != generation) {
                    buildHints()
                } else if (built != null) {
                    hints = built
                }
            }
        }
    }

    private fun buildSearch() {
        if (search != null || searchBuilding || snapshot.load != LoadState.READY) return
        searchBuilding = true
        val token = generation
        val emojis = snapshot.groups.flatMap { it.emojis }
        worker.execute {
            val built = runCatching { EmojiSearch(emojis, repository.names()) }.getOrNull()
            main.post {
                searchBuilding = false
                if (token == generation && built != null) {
                    search = built
                    update(snapshot.copy(searchReady = true, results = built.find(snapshot.query)))
                } else if (token != generation && snapshot.mode == EmojiMode.SEARCH) {
                    buildSearch()
                }
            }
        }
    }

    private fun update(next: EmojiSnapshot) {
        if (next == snapshot) return
        snapshot = next
        listeners.toList().forEach { it(snapshot) }
    }

    private class Loaded(val typeface: Typeface?, val groups: List<EmojiGroup>)
}
