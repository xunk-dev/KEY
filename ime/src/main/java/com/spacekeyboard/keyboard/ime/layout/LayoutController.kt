package com.spacekeyboard.keyboard.ime.layout

import android.content.Context
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefs
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefsStore
import com.spacekeyboard.keyboard.core.layout.LabelFont
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.LayoutRepository
import com.spacekeyboard.keyboard.core.layout.LayoutSpec
import com.spacekeyboard.keyboard.core.layout.NumberRow
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.core.layout.SlotRef

enum class Shift { OFF, ONCE, LOCKED }

data class LayoutSnapshot(
    val prefs: KeyboardPrefs,
    val layouts: Map<String, LayoutSpec>,
    val numbers: Boolean = false,
    val shift: Shift = Shift.OFF,
    val latinOnly: Boolean = false,
    val edit: Boolean = false,
    val selecting: Boolean = false,
    val enterLabel: String? = null,
) {
    val language: String get() = if (latinOnly) LATIN else prefs.language

    val activeId: String get() = when {
        edit -> LayoutCatalog.edit.id
        numbers -> LayoutCatalog.numbers.id
        else -> language
    }

    val active: LayoutSpec get() = layouts.getValue(activeId)

    private val capsOn: Boolean get() = shift != Shift.OFF && !numbers && !edit && language == LATIN

    private fun languageLabel(): String {
        val target = if (latinOnly && prefs.language != language) prefs.language else LayoutCatalog.next(prefs.language, prefs.languages)
        return LayoutCatalog.short(target).ifEmpty { LayoutCatalog.short(LayoutCatalog.default) }
    }

    fun spaceLabel(): String = if (numbers || edit) "" else LayoutCatalog.title(language)

    fun shown(key: KeySpec): String = when {
        key.action == KeyAction.SPACE -> spaceLabel()
        key.action == KeyAction.SHIFT -> if (shift == Shift.LOCKED) "\u21EA" else "\u21E7"
        key.action == KeyAction.LANGUAGE -> languageLabel()
        key.action == KeyAction.COMMIT && capsOn -> key.label.uppercase()
        else -> key.label
    }

    fun alts(key: KeySpec): List<String> = if (capsOn) key.alts.map { it.uppercase() } else key.alts

    fun hint(key: KeySpec): String? = if (key.action == KeyAction.COMMIT) alts(key).firstOrNull() else null

    fun isActive(key: KeySpec): Boolean =
        (key.action == KeyAction.SHIFT && shift != Shift.OFF) || (key.action == KeyAction.SELECT_MODE && selecting)

    fun rowPx(rows: Int, density: Float, screenPx: Int): Float = prefs.rowPx(rows, density, screenPx)

    fun keyboardHeightPx(density: Float, screenPx: Int): Int {
        val rows = active.rows.size
        return (rows * rowPx(rows, density, screenPx)).toInt()
    }

    private companion object {
        const val LATIN = "en"
    }
}

class LayoutController(context: Context) {
    private val store = KeyboardPrefsStore(context)
    private val repository = LayoutRepository(context)
    private val listeners = LinkedHashSet<(LayoutSnapshot) -> Unit>()
    private var subscription: AutoCloseable? = null
    private var numbers = false
    private var shift = Shift.OFF
    private var latinOnly = false
    private var edit = false
    private var selecting = false
    private var enterLabel: String? = null
    private var lastShiftTap = 0L

    var snapshot: LayoutSnapshot = build()
        private set

    fun start() {
        if (subscription != null) return
        subscription = store.observe(::refresh)
        refresh()
    }

    fun stop() {
        subscription?.close()
        subscription = null
    }

    fun subscribe(listener: (LayoutSnapshot) -> Unit): AutoCloseable {
        listeners += listener
        listener(snapshot)
        return AutoCloseable { listeners -= listener }
    }

    fun refresh() {
        snapshot = build()
        listeners.toList().forEach { it(snapshot) }
    }

    fun setKeySize(value: Int) = store.setKeySize(value)

    fun setHeight(value: Int) = store.setHeight(value)

    fun setOneHand(value: Int) = store.setOneHand(value)

    fun setSplitGap(value: Int) = store.setSplitGap(value)

    fun setLabelScale(value: Int) = store.setLabelScale(value)

    fun setMode(mode: PanelMode) = store.setMode(mode)

    fun setVibrate(value: Boolean) = store.setVibrate(value)
    fun setSound(value: Boolean) = store.setSound(value)
    fun setVibrateStrength(value: Int) = store.setVibrateStrength(value)
    fun setSoundVolume(value: Int) = store.setSoundVolume(value)
    fun setPreview(value: Boolean) = store.setPreview(value)
    fun setClipKeepOpen(value: Boolean) = store.setClipKeepOpen(value)

    fun setNumberRow(value: Boolean) = store.setNumberRow(value)

    fun setNativeDigits(value: Boolean) = store.setNativeDigits(value)

    fun setDoubleSpace(value: Boolean) = store.setDoubleSpace(value)

    fun setIncognito(value: Boolean) = store.setIncognito(value)

    fun setTouchAssist(value: Boolean) = store.setTouchAssist(value)

    fun setHardwareKeys(value: Boolean) = store.setHardwareKeys(value)

    fun setRememberLanguage(value: Boolean) = store.setRememberLanguage(value)

    fun setSuggestions(value: Boolean) = store.setSuggestions(value)

    fun toggleIncognito() = store.setIncognito(!snapshot.prefs.typing.incognito)

    fun setLanguageEnabled(id: String, on: Boolean) = store.setLanguageEnabled(id, on)

    fun setFont(font: LabelFont) = store.setFont(font)

    fun setBold(value: Boolean) = store.setBold(value)

    fun setItalic(value: Boolean) = store.setItalic(value)

    fun cycleMode() = store.setMode(snapshot.prefs.mode.next())

    fun flipSide() {
        val flipped = when (snapshot.prefs.mode) {
            PanelMode.ONE_HAND_LEFT -> PanelMode.ONE_HAND_RIGHT
            PanelMode.ONE_HAND_RIGHT -> PanelMode.ONE_HAND_LEFT
            else -> return
        }
        store.setMode(flipped)
    }

    fun switchLanguage() {
        numbers = false
        edit = false
        shift = Shift.OFF
        val wasLatinOnly = latinOnly
        latinOnly = false
        if (!wasLatinOnly || snapshot.prefs.language == snapshot.language) {
            store.setLanguage(LayoutCatalog.next(snapshot.prefs.language, snapshot.prefs.languages))
        }
        refresh()
    }

    fun selectLanguage(id: String): Boolean {
        if (id !in snapshot.prefs.languages) return false
        numbers = false
        edit = false
        selecting = false
        shift = Shift.OFF
        latinOnly = false
        if (snapshot.prefs.language != id) store.setLanguage(id)
        refresh()
        return true
    }

    fun selectOrEnable(id: String): Boolean {
        if (id !in snapshot.prefs.languages) {
            if (id !in LayoutCatalog.optional) return false
            store.setLanguageEnabled(id, true)
            refresh()
        }
        return selectLanguage(id)
    }

    fun setLatinOnly(on: Boolean) {
        if (latinOnly == on) return
        latinOnly = on
        shift = Shift.OFF
        refresh()
    }

    fun setNumbers(on: Boolean) {
        if (numbers == on && !(on && edit)) return
        numbers = on
        if (on) edit = false
        refresh()
    }

    fun setEdit(on: Boolean) {
        if (edit == on && !(on && numbers)) return
        edit = on
        selecting = false
        if (on) numbers = false
        refresh()
    }

    fun toggleEdit() = setEdit(!edit)

    fun toggleSelecting(): Boolean {
        selecting = !selecting
        refresh()
        return selecting
    }

    fun clearSelecting() {
        if (!selecting) return
        selecting = false
        refresh()
    }

    fun setEnterLabel(label: String?) {
        if (enterLabel == label) return
        enterLabel = label
        refresh()
    }

    fun toggleNumbers() = setNumbers(!numbers)

    fun setShift(next: Shift) {
        if (shift == next) return
        shift = next
        refresh()
    }

    fun tapShift(now: Long) {
        val next = when (shift) {
            Shift.OFF -> Shift.ONCE
            Shift.ONCE -> if (now - lastShiftTap <= DOUBLE_TAP_MS) Shift.LOCKED else Shift.OFF
            Shift.LOCKED -> Shift.OFF
        }
        lastShiftTap = now
        setShift(next)
    }

    fun consumeShift() {
        if (shift == Shift.ONCE) setShift(Shift.OFF)
    }

    fun swap(layoutId: String, a: SlotRef, b: SlotRef) {
        val shift = if (snapshot.prefs.typing.numberRow) 1 else 0
        val base = snapshot.layouts[layoutId]?.withoutNumberRow() ?: return
        val order = base.swapped(SlotRef(a.row - shift, a.col), SlotRef(b.row - shift, b.col)) ?: return
        store.setOrder(layoutId, order)
    }

    fun moveToolbar(from: Int, to: Int) {
        val items = snapshot.prefs.toolbar.toMutableList()
        if (from !in items.indices || to !in items.indices || from == to) return
        items.add(to, items.removeAt(from))
        store.setToolbar(items)
    }

    fun resetOrder(layoutId: String) = store.resetOrder(layoutId)

    fun resetToolbar() = store.resetToolbar()

    fun resetAll() = store.resetAll()

    private fun build(): LayoutSnapshot {
        val prefs = store.read()
        val typing = prefs.typing
        val layouts = LayoutCatalog.ids.associateWith { id ->
            val arranged = repository.arranged(id, prefs.orders[id])
            if (typing.numberRow) arranged.withNumberRow(NumberRow.digits(id, typing.nativeDigits)) else arranged
        } + (LayoutCatalog.numbers.id to repository.base(LayoutCatalog.numbers.id)) +
            (LayoutCatalog.edit.id to repository.base(LayoutCatalog.edit.id))
        return LayoutSnapshot(prefs, layouts, numbers, shift, latinOnly, edit, selecting, enterLabel)
    }

    private companion object {
        const val DOUBLE_TAP_MS = 400L
    }
}
