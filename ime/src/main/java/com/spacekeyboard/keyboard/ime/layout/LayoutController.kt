package com.spacekeyboard.keyboard.ime.layout

import android.content.Context
import com.spacekeyboard.keyboard.core.layout.ArabicStyle
import com.spacekeyboard.keyboard.core.layout.KeyFn
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefsStore
import com.spacekeyboard.keyboard.core.layout.LabelFont
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.LayoutRepository
import com.spacekeyboard.keyboard.core.layout.DigitsMode
import com.spacekeyboard.keyboard.core.layout.NumberRow
import com.spacekeyboard.keyboard.core.layout.PageLabels
import com.spacekeyboard.keyboard.core.layout.PageRows
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.core.layout.SlotRef
import com.spacekeyboard.keyboard.core.toolbar.BarArrangement
import com.spacekeyboard.keyboard.designsystem.icons.IconDirection

class LayoutController(context: Context) {
    private val store = KeyboardPrefsStore(context)
    private val repository = LayoutRepository(context)
    private val listeners = LinkedHashSet<(LayoutSnapshot) -> Unit>()
    private var subscription: AutoCloseable? = null
    private var numbers = false
    private var symbols = false
    private var shift = Shift.OFF
    private var latinOnly = false
    private var edit = false
    private var selecting = false
    private var enterLabel: String? = null
    private var enterKind = com.spacekeyboard.keyboard.ime.EnterKind.ENTER

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
        IconDirection.rtl = LayoutCatalog.isRtl(snapshot.language)
        listeners.toList().forEach { it(snapshot) }
    }

    fun setKeySize(value: Int) = store.setKeySize(value)
    fun setHeight(value: Int) = store.setHeight(value)
    fun setOneHand(value: Int) = store.setOneHand(value)
    fun setWidth(value: Int) = store.setWidth(value)
    fun resetSize() = store.resetSize()
    fun setSplitGap(value: Int) = store.setSplitGap(value)
    fun setFloatWidth(value: Int) = store.setFloatWidth(value)
    fun setFloatHeight(value: Int) = store.setFloatHeight(value)
    fun setFloatOpacity(value: Int) = store.setFloatOpacity(value)
    fun setDimLevel(value: Int) = store.setDimLevel(value)
    fun setFloatPosition(left: Int, top: Int) = store.setFloatPosition(left, top)
    fun setFloatFrame(width: Int, height: Int, left: Int, top: Int) = store.setFloatFrame(width, height, left, top)
    fun enterFloat() = store.enterFloat()
    fun dockFloat() = store.setMode(PanelMode.NORMAL)
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
    fun setDigitsMode(mode: DigitsMode) = store.setDigitsMode(mode)

    val canSwapDigits: Boolean get() = snapshot.prefs.typing.numberRow && NumberRow.hasNative(snapshot.activeId)

    fun swapDigits(): Boolean {
        val native = !snapshot.prefs.typing.nativeDigits
        setDigitsMode(if (native) DigitsMode.NATIVE else DigitsMode.LATIN)
        return native
    }

    fun setArabicStyle(style: ArabicStyle) = store.setArabicStyle(style)
    fun setDoubleSpace(value: Boolean) = store.setDoubleSpace(value)
    fun setAutoCapitalize(value: Boolean) = store.setAutoCapitalize(value)
    fun setIncognito(value: Boolean) = store.setIncognito(value)
    fun setTouchAssist(value: Boolean) = store.setTouchAssist(value)

    fun setGlide(value: Boolean) = store.setGlide(value)

    fun setConvertOffers(value: Boolean) = store.setConvertOffers(value)

    fun setSpellFix(value: Boolean) = store.setSpellFix(value)
    fun setHardwareKeys(value: Boolean) = store.setHardwareKeys(value)
    fun setRememberLanguage(value: Boolean) = store.setRememberLanguage(value)
    fun setSuggestions(value: Boolean) = store.setSuggestions(value)
    fun setEmojiSuggestions(value: Boolean) = store.setEmojiSuggestions(value)
    fun setAppSuggestions(value: Boolean) = store.setAppSuggestions(value)
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
        symbols = false
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
        symbols = false
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
        if (!on) symbols = false
        if (on) edit = false
        refresh()
    }

    fun toggleSymbols() {
        if (!numbers) return
        symbols = !symbols
        refresh()
    }

    fun openSymbols() {
        if (numbers && symbols && !edit) return
        numbers = true
        symbols = true
        edit = false
        selecting = false
        refresh()
    }

    fun setEdit(on: Boolean) {
        if (edit == on && !(on && numbers)) return
        edit = on
        selecting = false
        if (on) {
            numbers = false
            symbols = false
        }
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

    fun setEnter(label: String?, kind: com.spacekeyboard.keyboard.ime.EnterKind) {
        if (enterLabel == label && enterKind == kind) return
        enterLabel = label
        enterKind = kind
        refresh()
    }

    fun toggleNumbers() = setNumbers(!numbers)

    fun setShift(next: Shift) {
        if (shift == next) return
        shift = next
        refresh()
    }

    fun tapShift() {
        setShift(
            when (shift) {
                Shift.OFF -> Shift.ONCE
                Shift.ONCE -> Shift.LOCKED
                Shift.LOCKED -> Shift.OFF
            },
        )
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

    fun setBar(value: BarArrangement) = store.setBar(value)

    fun setKeyFn(layoutId: String, keyId: String, fn: KeyFn?) = store.setKeyFn(layoutId, keyId, fn)

    fun resetOrder(layoutId: String) = store.resetOrder(layoutId)

    fun resetToolbar() = store.resetToolbar()

    fun resetAll() = store.resetAll()

    private fun build(): LayoutSnapshot {
        val prefs = store.read()
        val typing = prefs.typing
        val layouts = LayoutCatalog.ids.associateWith { id ->
            val arranged = repository.arranged(id, prefs.orders[id], prefs.arabicStyle)
                .withFunctions(prefs.keyFns[id].orEmpty(), LayoutCatalog.isRtl(id))
            val withDigits = if (typing.numberRow) {
                arranged.withNumberRow(
                    NumberRow.digits(id, typing.nativeDigits),
                    NumberRow.alternates(id, typing.nativeDigits),
                    NumberRow.extra(id, arranged.rows.first().size),
                )
            } else {
                arranged
            }
            PageLabels.letters(withDigits, id)
        }
        val pageLanguage = if (latinOnly) LayoutSnapshot.LATIN else prefs.language
        val pages = mapOf(
            LayoutCatalog.numbers.id to PageLabels.page(PageRows.numbers(repository.base(LayoutCatalog.numbers.id), typing.numberRow), pageLanguage, true),
            LayoutCatalog.symbols.id to PageLabels.page(PageRows.symbols(repository.base(LayoutCatalog.symbols.id), typing.numberRow), pageLanguage, true),
            LayoutCatalog.edit.id to PageLabels.page(PageRows.edit(repository.base(LayoutCatalog.edit.id), typing.numberRow), pageLanguage, true),
        )
        return LayoutSnapshot(prefs, layouts + pages, numbers, symbols, shift, latinOnly, edit, selecting, enterLabel, enterKind)
    }
}
