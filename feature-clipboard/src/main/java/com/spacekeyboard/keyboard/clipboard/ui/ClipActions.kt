package com.spacekeyboard.keyboard.clipboard.ui

import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.R

interface ClipActions {
    fun onKeys()
    fun onPick(item: ClipItem)
    fun onTab(tab: ClipTab)
    fun onSection(name: String)
    fun onPin(id: Long)
    fun onDelete(id: Long)
    fun onAssignStart(id: Long)
    fun onAssign(name: String?)
    fun onAssignCancel()
    fun onClear()
    fun onConfirm(yes: Boolean)
    fun onNewSection()
    fun onNameOk()
    fun onNameCancel()
    fun onLockSection(name: String)
    fun onDeleteSection(name: String)
    fun onUnlock()
}

object ClipLabels {
    private const val PREVIEW_MAX = 90

    fun tab(tab: ClipTab): Int = when (tab) {
        ClipTab.CURRENT -> R.string.clip_tab_current
        ClipTab.PINNED -> R.string.clip_tab_pinned
        ClipTab.SAVED -> R.string.clip_tab_saved
    }

    fun preview(text: String): String {
        val flat = text.replace(Regex("\\s+"), " ").trim()
        return if (flat.length > PREVIEW_MAX) flat.take(PREVIEW_MAX) + "…" else flat
    }
}
