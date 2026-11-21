package com.spacekeyboard.keyboard.decorate

import android.content.Context
import android.graphics.Paint

enum class SourceKind { NONE, SELECTION, BEFORE }

data class DecorSnapshot(
    val open: Boolean = false,
    val allowed: Boolean = true,
    val language: Language = Language.EN,
    val favoritesOnly: Boolean = false,
    val favorites: Set<String> = emptySet(),
    val active: String? = null,
    val source: String = "",
    val kind: SourceKind = SourceKind.NONE,
    val styles: List<DecorStyle> = emptyList(),
)

class DecorController(context: Context) {
    private val store = DecorPrefsStore(context)
    private val listeners = LinkedHashSet<(DecorSnapshot) -> Unit>()
    private val paint = Paint()
    private val renderable = HashMap<String, Boolean>()
    private var subscription: AutoCloseable? = null

    var snapshot: DecorSnapshot = build(DecorSnapshot())
        private set

    fun start() {
        if (subscription != null) return
        subscription = store.observe { update { it } }
    }

    fun stop() {
        subscription?.close()
        subscription = null
    }

    fun subscribe(listener: (DecorSnapshot) -> Unit): AutoCloseable {
        listeners += listener
        listener(snapshot)
        return AutoCloseable { listeners -= listener }
    }

    fun open(source: String, kind: SourceKind) = update { it.copy(open = it.allowed, source = source, kind = kind) }

    fun close() = update { it.copy(open = false) }

    fun setAllowed(value: Boolean) = update { it.copy(allowed = value, open = it.open && value) }

    fun setLanguage(language: Language) = update { it.copy(language = language) }

    fun toggleFavoritesOnly() = update { it.copy(favoritesOnly = !it.favoritesOnly) }

    fun toggleFavorite(id: String) = store.toggleFavorite(id)

    fun toggleActive(id: String) = store.setActive(if (store.active() == id) null else id)

    fun clearActive() = store.setActive(null)

    fun activeStyle(): DecorStyle? = snapshot.active?.let(DecorCatalog::byId)

    private fun update(change: (DecorSnapshot) -> DecorSnapshot) {
        val next = build(change(snapshot))
        if (next == snapshot) return
        snapshot = next
        listeners.toList().forEach { it(snapshot) }
    }

    private fun build(base: DecorSnapshot): DecorSnapshot {
        val favorites = store.favorites()
        val active = store.active()
        val visible = DecorCatalog.forLanguage(base.language).filter(::isRenderable)
        val shown = if (base.favoritesOnly) visible.filter { it.id in favorites } else visible
        val ordered = shown.sortedBy { if (it.id in favorites) 0 else 1 }
        return base.copy(favorites = favorites, active = active, styles = ordered)
    }

    private fun isRenderable(style: DecorStyle): Boolean = renderable.getOrPut(style.id) {
        Renderability.ok(style.apply(Renderability.probe(style.language))) { paint.hasGlyph(String(Character.toChars(it))) }
    }
}
