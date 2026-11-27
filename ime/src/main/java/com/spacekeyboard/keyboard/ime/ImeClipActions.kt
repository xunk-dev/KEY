package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.content.Intent
import com.spacekeyboard.keyboard.clipboard.BusyKind
import com.spacekeyboard.keyboard.clipboard.ClipEdit
import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.ClipLatest
import com.spacekeyboard.keyboard.clipboard.ClipKind
import com.spacekeyboard.keyboard.clipboard.ClipPolicy
import com.spacekeyboard.keyboard.clipboard.ClipTab
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.clipboard.ui.ClipActions

class ImeClipActions(
    private val context: Context,
    private val clip: ClipboardController,
    private val insert: (String) -> Unit,
    private val pasted: (() -> Unit) -> Unit,
    private val makeShortcut: (String) -> Unit,
    private val keepOpen: () -> Boolean,
) : ClipActions {
    override fun onKeys() = clip.close()

    override fun onPick(item: ClipItem) {
        when {
            item.blob == null -> {
                if (ClipLatest.livePasteable(clip.snapshot.sections, item)) pasted { insert(item.text) } else insert(item.text)
                if (keepOpen()) clip.closeView() else clip.close()
            }
            item.length > ClipPolicy.PASTE_CONFIRM -> clip.askPaste(item.id)
            else -> onPasteLarge(item.id)
        }
    }

    override fun onPasteLarge(id: Long) = clip.pasteLarge(id, insert, !keepOpen())

    override fun onBusyStop(kind: BusyKind) = clip.stopBusy(kind)

    override fun onShortcut(item: ClipItem) {
        clip.close()
        makeShortcut(item.text)
    }

    override fun onTab(tab: ClipTab) = clip.selectTab(tab)

    override fun onTabOrder(order: List<ClipTab>) = clip.setTabOrder(order)

    override fun onEnable() = clip.enable()

    override fun onSmart() = clip.toggleSmart()

    override fun onKind(kind: ClipKind?) = clip.selectKind(kind)

    override fun onSection(name: String) = clip.selectSection(name)

    override fun onPin(id: Long) = clip.togglePin(id)

    override fun onDelete(id: Long) = clip.delete(id)

    override fun onUndo() = clip.undoDelete()

    override fun onUndoDismiss() = clip.dismissUndo()

    override fun onAssignStart(id: Long) = clip.startAssign(id)

    override fun onAssign(name: String?) = clip.assign(name)

    override fun onAssignCancel() = clip.cancelAssign()

    override fun onClear() = clip.askClear()

    override fun onConfirm(yes: Boolean) = clip.answer(yes)

    override fun onNewSection() = clip.startNaming()

    override fun onTitle(id: Long) = clip.startTitle(id)

    override fun onTitleRemove(id: Long) = clip.removeTitle(id)

    override fun onNameOk() = clip.confirmName()

    override fun onNameSuggest(name: String) = clip.suggest(name)

    override fun onNameCancel() = clip.cancelNaming()

    override fun onLockSection(name: String) = clip.toggleSectionLock(name)

    override fun onDeleteSection(name: String) = clip.askDeleteSection(name)

    override fun onUnlock() = clip.requestUnlock()

    override fun onCopy(id: Long) = clip.copy(id)

    override fun onShare(id: Long) = clip.share(id)

    override fun onView(id: Long) = clip.view(id)

    override fun onViewClose() = clip.closeView()

    override fun onEdit(id: Long) {
        if (!clip.beginEdit(id)) return
        val intent = Intent(context, ClipEditActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(ClipEdit.EXTRA_ID, id)
        if (runCatching { context.startActivity(intent) }.isFailure) clip.endEdit(id)
    }
}
