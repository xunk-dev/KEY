package com.spacekeyboard.keyboard.core.macro

import android.content.Context
import android.content.SharedPreferences
import com.spacekeyboard.keyboard.core.vault.AesGcmCipher
import com.spacekeyboard.keyboard.core.vault.KeystoreKeys
import com.spacekeyboard.keyboard.core.vault.TextCipher

class MacroStore(private val prefs: SharedPreferences, private val cipher: TextCipher?) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
        runCatching { AesGcmCipher(KeystoreKeys.getOrCreate(ALIAS)) }.getOrNull(),
    )

    var speed: MacroSpeed
        get() = MacroSpeed.byId(prefs.getString(SPEED, null))
        set(value) {
            prefs.edit().putString(SPEED, value.id).apply()
        }

    var repeat: MacroRepeat
        get() = MacroRepeat.byId(prefs.getString(REPEAT, null))
        set(value) {
            prefs.edit().putString(REPEAT, value.id).apply()
        }

    val favoriteId: Long? get() = prefs.getLong(FAVORITE, 0L).takeIf { it > 0L }

    fun all(): List<Macro> = load()

    fun favorite(): Macro? = favoriteId?.let { id -> load().firstOrNull { it.id == id } }

    fun add(steps: List<MacroStep>, name: String? = null): Macro? {
        if (steps.isEmpty() || cipher == null) return null
        val current = load()
        if (current.size >= MAX_MACROS) return null
        val id = prefs.getLong(SEQ, 0L) + 1
        val macro = Macro(id, steps, clean(name))
        prefs.edit().putLong(SEQ, id).apply()
        return if (save(current + macro)) macro else null
    }

    fun rename(id: Long, name: String?) {
        val renamed = load().map { if (it.id == id) Macro(it.id, it.steps, clean(name)) else it }
        save(renamed)
    }

    fun setFavorite(id: Long?) {
        val edit = prefs.edit()
        if (id != null && load().any { it.id == id }) edit.putLong(FAVORITE, id) else edit.remove(FAVORITE)
        edit.apply()
    }

    fun delete(id: Long) {
        save(load().filter { it.id != id })
        if (favoriteId == id) prefs.edit().remove(FAVORITE).apply()
    }

    fun clear() {
        prefs.edit().remove(ITEMS).remove(VAULT).remove(FAVORITE).apply()
    }

    private fun clean(name: String?): String? = name?.trim()?.take(MAX_NAME)?.ifEmpty { null }

    private fun load(): List<Macro> {
        val sealed = prefs.getString(VAULT, null)
        if (sealed != null) return open(sealed)
        val legacy = prefs.getString(ITEMS, null) ?: return emptyList()
        val list = legacy.split(SEP).mapNotNull(MacroSerial::parseLegacy)
        migrate(list)
        return list
    }

    private fun open(sealed: String): List<Macro> {
        val plain = cipher?.decrypt(sealed) ?: return emptyList()
        return plain.split(SEP).mapNotNull(MacroSerial::parse)
    }

    private fun migrate(list: List<Macro>) {
        if (list.isEmpty()) return prefs.edit().remove(ITEMS).apply()
        if (save(list)) prefs.edit().remove(ITEMS).apply()
    }

    private fun save(list: List<Macro>): Boolean {
        val box = cipher ?: return false
        val plain = list.joinToString(SEP.toString(), transform = MacroSerial::write)
        val sealed = runCatching { box.encrypt(plain) }.getOrNull() ?: return false
        if (box.decrypt(sealed) != plain) return false
        prefs.edit().putString(VAULT, sealed).apply()
        return true
    }

    companion object {
        const val MAX_MACROS = 30
        const val MAX_NAME = 40
        private const val NAME = "space_macros"
        private const val ALIAS = "space_keyboard_macros"
        private const val ITEMS = "items"
        private const val VAULT = "vault"
        private const val SEQ = "seq"
        private const val SPEED = "speed"
        private const val REPEAT = "repeat"
        private const val FAVORITE = "favorite"
        private const val SEP = '\n'
    }
}
