package com.spacekeyboard.keyboard.symbols.state

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.spacekeyboard.keyboard.symbols.data.SymbolAssets
import com.spacekeyboard.keyboard.symbols.data.SymbolData
import com.spacekeyboard.keyboard.symbols.glyph.GlyphFilter
import com.spacekeyboard.keyboard.symbols.glyph.PaintGlyphFilter
import com.spacekeyboard.keyboard.symbols.model.FaceCategory
import com.spacekeyboard.keyboard.symbols.model.FaceEntry
import com.spacekeyboard.keyboard.symbols.model.SymbolCategory
import com.spacekeyboard.keyboard.symbols.model.SymbolEntry
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SymbolPanelModel(context: Context, private val glyphs: GlyphFilter = PaintGlyphFilter()) {
    private val appContext = context.applicationContext
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

    fun open() = change { copy(open = true) }

    fun close() = change { copy(open = false, searching = false, query = "") }

    fun selectTab(tab: PanelTab) = change { copy(tab = tab, searching = false, query = "") }

    fun selectCategory(category: SymbolCategory) =
        change { copy(category = category, searching = false, query = "") }

    fun selectFaceCategory(category: FaceCategory) =
        change { copy(faceCategory = category, searching = false, query = "") }

    fun startSearch() = change { copy(searching = true, query = "") }

    fun endSearch() = change { copy(searching = false, query = "") }

    fun type(text: String) = change {
        if (searching) copy(query = (query + text).take(MAX_QUERY)) else this
    }

    fun backspace() = change {
        if (searching && query.isNotEmpty()) copy(query = query.dropLast(1)) else this
    }

    private fun change(block: PanelState.() -> PanelState) {
        val next = snapshot.block()
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
            main.post { deliver(current, symbols, faces) }
        }
    }

    private fun deliver(current: Int, symbols: List<SymbolEntry>, faces: List<FaceEntry>) {
        if (current != ticket) return
        snapshot = snapshot.copy(loaded = true, symbols = symbols, faces = faces)
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
