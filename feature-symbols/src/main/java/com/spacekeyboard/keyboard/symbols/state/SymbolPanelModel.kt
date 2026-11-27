package com.spacekeyboard.keyboard.symbols.state

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.spacekeyboard.keyboard.symbols.data.SymbolAssets
import com.spacekeyboard.keyboard.symbols.data.SymbolData
import com.spacekeyboard.keyboard.symbols.data.SymbolRecents
import com.spacekeyboard.keyboard.symbols.glyph.GlyphFilter
import com.spacekeyboard.keyboard.symbols.glyph.PaintGlyphFilter
import com.spacekeyboard.keyboard.symbols.model.ArtCategory
import com.spacekeyboard.keyboard.symbols.model.ArtEntry
import com.spacekeyboard.keyboard.symbols.model.FaceCategory
import com.spacekeyboard.keyboard.symbols.model.FaceEntry
import com.spacekeyboard.keyboard.symbols.model.SymbolCategory
import com.spacekeyboard.keyboard.symbols.model.SymbolEntry
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SymbolPanelModel(context: Context, private val glyphs: GlyphFilter = PaintGlyphFilter()) {
    private val appContext = context.applicationContext
    private val recentStore = SymbolRecents(appContext)
    private val main = Handler(Looper.getMainLooper())
    private val listeners = LinkedHashSet<(PanelState) -> Unit>()
    private val symbolCache = HashMap<SymbolCategory, List<SymbolEntry>>()
    private val faceCache = HashMap<FaceCategory, List<FaceEntry>>()
    private var executor: ExecutorService? = null
    private var data: SymbolData? = null
    private var ticket = 0

    var snapshot: PanelState = PanelState()
        private set

    fun subscribe(listener: (PanelState) -> Unit): AutoCloseable {
        listeners += listener
        listener(snapshot)
        return AutoCloseable { listeners -= listener }
    }

    fun release() {
        executor?.shutdownNow()
        executor = null
        listeners.clear()
    }

    fun open() = change { copy(open = true, tab = PanelTab.SYMBOLS, recentsOpen = false, editing = false) }

    fun openFaces() = change { copy(open = true, tab = PanelTab.FACES, searching = false, query = "", recentsOpen = false, editing = false) }

    fun openArt() = change { copy(open = true, tab = PanelTab.ART, searching = false, query = "", recentsOpen = false, editing = false) }

    fun close() = change { copy(open = false, searching = false, query = "", editing = false) }

    fun selectTab(tab: PanelTab) = change { copy(tab = tab, searching = false, query = "", recentsOpen = false, editing = false) }

    fun selectCategory(category: SymbolCategory) =
        change { copy(category = category, searching = false, query = "", recentsOpen = false, editing = false) }

    fun selectFaceCategory(category: FaceCategory) =
        change { copy(faceCategory = category, searching = false, query = "", recentsOpen = false, editing = false) }

    fun selectArtCategory(category: ArtCategory) =
        change { copy(artCategory = category, searching = false, query = "", recentsOpen = false, editing = false) }

    fun showRecents() = change { copy(recentsOpen = true, searching = false, query = "", editing = false) }

    fun setEditing(value: Boolean) = change { copy(editing = value) }

    fun record(text: String) {
        recentStore.record(snapshot.tab, text)
    }

    fun removeRecent(text: String) {
        recentStore.remove(snapshot.tab, text)
        change { this }
    }

    fun clearRecents() {
        recentStore.clear(snapshot.tab)
        change { this }
    }

    fun startSearch() = change { copy(searching = true, query = "") }

    fun endSearch() = change { copy(searching = false, query = "") }

    fun type(text: String) = change {
        if (searching) copy(query = (query + text).take(MAX_QUERY)) else this
    }

    fun backspace() = change {
        if (searching && query.isNotEmpty()) copy(query = query.dropLast(1)) else this
    }

    private fun change(block: PanelState.() -> PanelState) {
        val moved = snapshot.block()
        val next = moved.copy(recents = recentStore.list(moved.tab), editing = moved.editing && moved.recentsOpen)
        if (next == snapshot) return
        snapshot = next
        publish()
        compute()
    }

    private fun publish() {
        listeners.toList().forEach { it(snapshot) }
    }

    private fun worker(): ExecutorService =
        executor ?: Executors.newSingleThreadExecutor().also { executor = it }

    private fun compute() {
        val state = snapshot
        if (!state.open) return
        val current = ++ticket
        worker().execute {
            val loaded = data ?: SymbolAssets.load(appContext).also { data = it }
            val symbols = if (state.tab == PanelTab.SYMBOLS) symbolsFor(loaded, state) else emptyList()
            val faces = if (state.tab == PanelTab.FACES) facesFor(loaded, state) else emptyList()
            val art = if (state.tab == PanelTab.ART) loaded.art.category(state.artCategory) else emptyList()
            main.post { deliver(current, symbols, faces, art) }
        }
    }

    private fun deliver(current: Int, symbols: List<SymbolEntry>, faces: List<FaceEntry>, art: List<ArtEntry>) {
        if (current != ticket) return
        snapshot = snapshot.copy(loaded = true, symbols = symbols, faces = faces, art = art)
        publish()
    }

    private fun symbolsFor(loaded: SymbolData, state: PanelState): List<SymbolEntry> {
        if (state.searching) {
            return loaded.symbols.search(state.query).filter { glyphs.has(it.codePoint) }
        }
        return symbolCache.getOrPut(state.category) {
            loaded.symbols.category(state.category).filter { glyphs.has(it.codePoint) }
        }
    }

    private fun facesFor(loaded: SymbolData, state: PanelState): List<FaceEntry> {
        if (state.searching) return loaded.faces.search(state.query).filter(::renderable)
        return faceCache.getOrPut(state.faceCategory) {
            loaded.faces.category(state.faceCategory).filter(::renderable)
        }
    }

    private fun renderable(face: FaceEntry): Boolean = face.codePoints().all { glyphs.has(it) }

    private companion object {
        const val MAX_QUERY = 40
    }
}
