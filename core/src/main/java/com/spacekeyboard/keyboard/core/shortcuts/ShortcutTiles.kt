package com.spacekeyboard.keyboard.core.shortcuts

import android.content.Context
import android.content.SharedPreferences

enum class ShortcutTile(val id: String, val removable: Boolean = true) {
    MODE("mode"), THEME("theme"), DECORATE("decorate"), EDIT("edit"), RESIZE("resize"),
    FLOAT("float"), DIM("dim"), CALCULATOR("calculator"), INCOGNITO("incognito"), SETTINGS("settings", removable = false),
    EMOJI("emoji"), CLIPBOARD("clipboard"), TRANSLATE("translate"), AUTOMATION("automation"),
    TEXT("text");

    companion object {
        fun byId(id: String): ShortcutTile? = entries.firstOrNull { it.id == id }
    }
}

class TileArrangement private constructor(val visible: List<ShortcutTile>, val hidden: List<ShortcutTile>) {

    fun move(tile: ShortcutTile, delta: Int, shown: (ShortcutTile) -> Boolean = { true }): TileArrangement {
        val list = visible.toMutableList()
        val from = list.indexOf(tile)
        if (from < 0 || delta == 0) return this
        val step = if (delta > 0) 1 else -1
        var to = from + step
        while (to in list.indices && !shown(list[to])) to += step
        if (to !in list.indices) return this
        java.util.Collections.swap(list, from, to)
        return TileArrangement(list, hidden)
    }

    fun remove(tile: ShortcutTile): TileArrangement =
        if (!tile.removable || tile !in visible) this else TileArrangement(visible - tile, hidden + tile)

    fun add(tile: ShortcutTile): TileArrangement =
        if (tile !in hidden) this else TileArrangement(visible + tile, hidden - tile)

    fun canMove(tile: ShortcutTile, delta: Int, shown: (ShortcutTile) -> Boolean = { true }): Boolean = move(tile, delta, shown) !== this

    companion object {
        val DEFAULT = resolve(null, null)

        fun resolve(order: String?, hiddenIds: String?): TileArrangement {
            val ordered = parse(order)
            val hidden = parse(hiddenIds).filter { it !in ordered && it.removable }
            val fresh = ShortcutTile.entries.filter { it !in ordered && it !in hidden }
            return TileArrangement(ordered + fresh, hidden)
        }

        internal fun parse(raw: String?): List<ShortcutTile> =
            raw.orEmpty().split(',').mapNotNull { ShortcutTile.byId(it) }.distinct()
    }

    fun encodeVisible(): String = visible.joinToString(",") { it.id }

    fun encodeHidden(): String = hidden.joinToString(",") { it.id }
}

class ShortcutTilesStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE))

    fun arrangement(): TileArrangement = TileArrangement.resolve(prefs.getString(ORDER, null), prefs.getString(HIDDEN, null))

    fun save(value: TileArrangement) {
        prefs.edit().putString(ORDER, value.encodeVisible()).putString(HIDDEN, value.encodeHidden()).apply()
    }

    fun reset() {
        prefs.edit().remove(ORDER).remove(HIDDEN).apply()
    }

    private companion object {
        const val NAME = "space_shortcut_tiles"
        const val ORDER = "order"
        const val HIDDEN = "hidden"
    }
}
