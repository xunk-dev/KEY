package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.clipboard.ui.ClipActions

class ImeClipActions(
    private val clip: ClipboardController,
    private val insert: (String) -> Unit,
    private val keepOpen: () -> Boolean,
) : ClipActions {
    override fun onKeys() = clip.close()

    override fun onPick(item: ClipItem) {
        insert(item.text)
        if (!keepOpen()) clip.close()
    }

    override fun onTab(tab: ClipTab) = clip.selectTab(tab)

    override fun onSection(name: String) = clip.selectSection(name)

    override fun onPin(id: Long) = clip.togglePin(id)

    override fun onDelete(id: Long) = clip.delete(id)

    override fun onAssignStart(id: Long) = clip.startAssign(id)

    override fun onAssign(name: String?) = clip.assign(name)

    override fun onAssignCancel() = clip.cancelAssign()

    override fun onClear() = clip.askClear()

    override fun onConfirm(yes: Boolean) = clip.answer(yes)

    override fun onNewSection() = clip.startNaming()

    override fun onNameOk() = clip.confirmName()

    override fun onNameCancel() = clip.cancelNaming()

    override fun onLockSection(name: String) = clip.toggleSectionLock(name)

    override fun onDeleteSection(name: String) = clip.askDeleteSection(name)

    override fun onUnlock() = clip.requestUnlock()
}
