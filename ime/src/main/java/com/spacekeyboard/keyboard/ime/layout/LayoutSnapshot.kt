package com.spacekeyboard.keyboard.ime.layout

import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefs
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.LayoutSpec
import com.spacekeyboard.keyboard.core.layout.PageRows

enum class Shift { OFF, ONCE, LOCKED }

data class LayoutSnapshot(
    val prefs: KeyboardPrefs,
    val layouts: Map<String, LayoutSpec>,
    val numbers: Boolean = false,
    val symbols: Boolean = false,
    val shift: Shift = Shift.OFF,
    val latinOnly: Boolean = false,
    val edit: Boolean = false,
    val selecting: Boolean = false,
    val enterLabel: String? = null,
    val enterKind: com.spacekeyboard.keyboard.ime.EnterKind = com.spacekeyboard.keyboard.ime.EnterKind.ENTER,
) {
    val language: String get() = if (latinOnly) LATIN else prefs.language

    val activeId: String get() = when {
        edit -> LayoutCatalog.edit.id
        numbers && symbols -> LayoutCatalog.symbols.id
        numbers -> LayoutCatalog.numbers.id
        else -> language
    }

    val active: LayoutSpec get() = layouts.getValue(activeId)

    private val capsOn: Boolean get() = shift != Shift.OFF && !numbers && !edit && language == LATIN

    private fun languageLabel(): String {
        val target = if (latinOnly && prefs.language != language) prefs.language else LayoutCatalog.next(prefs.language, prefs.languages)
        return LayoutCatalog.short(target).ifEmpty { LayoutCatalog.short(LayoutCatalog.default) }
    }

    fun spaceLabel(): String = LayoutCatalog.title(language)

    fun shown(key: KeySpec): String = when {
        key.action == KeyAction.SPACE -> spaceLabel()
        key.action == KeyAction.SHIFT -> if (shift == Shift.LOCKED) "\u21EA" else "\u21E7"
        key.action == KeyAction.LANGUAGE -> languageLabel()
        key.action == KeyAction.COMMIT && capsOn -> key.label.uppercase()
        else -> key.label
    }

    val digitsOnTop: Boolean get() = prefs.typing.numberRow && activeId in LayoutCatalog.ids

    fun alts(key: KeySpec): List<String> {
        val own = if (digitsOnTop && !key.id.startsWith(LayoutSpec.NUMBER_ROW_PREFIX)) {
            key.alts.filterNot { it.isNotEmpty() && it.all(Char::isDigit) }
        } else {
            key.alts
        }
        return if (capsOn) own.map { it.uppercase() } else own
    }

    fun hint(key: KeySpec): String? = if (key.action == KeyAction.COMMIT) alts(key).firstOrNull() else null

    fun isActive(key: KeySpec): Boolean =
        (key.action == KeyAction.SHIFT && shift != Shift.OFF) || (key.action == KeyAction.SELECT_MODE && selecting)

    val stableRows: Int get() = PageRows.count(prefs.typing.numberRow)

    fun rowPx(rows: Int, density: Float, screenPx: Int): Float = prefs.rowPx(rows, density, screenPx)

    fun keyboardHeightPx(density: Float, screenPx: Int): Int {
        val rows = stableRows
        return (rows * rowPx(rows, density, screenPx) + prefs.bottomSafePx(density)).toInt()
    }

    companion object {
        const val LATIN = "en"
    }
}
