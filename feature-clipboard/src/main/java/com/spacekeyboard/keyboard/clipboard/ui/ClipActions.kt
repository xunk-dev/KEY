package com.spacekeyboard.keyboard.clipboard.ui

import com.spacekeyboard.keyboard.clipboard.BusyKind
import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.ClipKind
import com.spacekeyboard.keyboard.clipboard.EditStatus
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.R

interface ClipActions {
    fun onKeys()
    fun onPick(item: ClipItem)
    fun onTab(tab: ClipTab)
    fun onTabOrder(order: List<ClipTab>)
    fun onEnable()
    fun onSmart()
    fun onKind(kind: ClipKind?)
    fun onSection(name: String)
    fun onPin(id: Long)
    fun onDelete(id: Long)
    fun onUndo()
    fun onUndoDismiss()
    fun onAssignStart(id: Long)
    fun onAssign(name: String?)
    fun onAssignCancel()
    fun onClear()
    fun onConfirm(yes: Boolean)
    fun onNewSection()
    fun onTitle(id: Long)
    fun onTitleRemove(id: Long)
    fun onNameOk()
    fun onNameSuggest(name: String)
    fun onNameCancel()
    fun onLockSection(name: String)
    fun onDeleteSection(name: String)
    fun onUnlock()
    fun onCopy(id: Long)
    fun onShortcut(item: ClipItem)
    fun onShare(id: Long)
    fun onEdit(id: Long)
    fun onView(id: Long)
    fun onViewClose()
    fun onPasteLarge(id: Long)
    fun onBusyStop(kind: BusyKind)
}

object ClipLabels {
    private const val PREVIEW_MAX = 90
    private const val THOUSAND = 1_000L
    private const val MILLION = 1_000_000L
    private const val BILLION = 1_000_000_000L

    fun tab(tab: ClipTab): Int = when (tab) {
        ClipTab.CURRENT -> R.string.clip_tab_current
        ClipTab.PINNED -> R.string.clip_tab_pinned
        ClipTab.SAVED -> R.string.clip_tab_saved
    }

    fun kind(kind: ClipKind): Int = when (kind) {
        ClipKind.LINK -> R.string.clip_kind_link
        ClipKind.EMAIL -> R.string.clip_kind_email
        ClipKind.PHONE -> R.string.clip_kind_phone
        ClipKind.USERNAME -> R.string.clip_kind_username
        ClipKind.TOKEN -> R.string.clip_kind_token
        ClipKind.NUMBER -> R.string.clip_kind_number
        ClipKind.TEXT -> R.string.clip_kind_text
    }

    fun preview(text: String): String {
        val flat = text.replace(Regex("\\s+"), " ").trim()
        return if (flat.length > PREVIEW_MAX) flat.take(PREVIEW_MAX) + "…" else flat
    }

    fun compact(count: Long): String = when {
        count >= BILLION -> scaled(count, BILLION, "B")
        count >= MILLION -> scaled(count, MILLION, "M")
        count >= THOUSAND -> scaled(count, THOUSAND, "K")
        else -> count.toString()
    }

    private fun scaled(count: Long, unit: Long, suffix: String): String {
        val tenths = count * 10 / unit
        val whole = tenths / 10
        val fraction = tenths % 10
        return if (fraction == 0L || whole >= 100) "$whole$suffix" else "$whole.$fraction$suffix"
    }

    fun problem(status: EditStatus?): Int? = when (status) {
        EditStatus.EMPTY -> R.string.clip_edit_empty
        EditStatus.TOO_LONG -> R.string.clip_edit_too_long
        EditStatus.MISSING -> R.string.clip_edit_missing
        EditStatus.LOCKED -> R.string.clip_edit_locked
        EditStatus.UNAVAILABLE -> R.string.clip_edit_unavailable
        EditStatus.OK, EditStatus.UNCHANGED, null -> null
    }
}
