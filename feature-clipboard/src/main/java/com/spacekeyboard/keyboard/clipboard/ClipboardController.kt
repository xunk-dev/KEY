package com.spacekeyboard.keyboard.clipboard

import android.app.KeyguardManager
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ClipboardController(context: Context, private val clock: () -> Long = System::currentTimeMillis) {
    private val app = context.applicationContext
    private val prefs = ClipPrefsStore(app)
    private val clipboard = app.getSystemService(ClipboardManager::class.java)
    private val worker: ExecutorService = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val large = ClipLarge(app, worker, main, ::onBusy)
    private val listeners = LinkedHashSet<(ClipSnapshot) -> Unit>()
    private val relock = Runnable { publish() }
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener { onClip() }
    private var subscription: AutoCloseable? = null
    private var store: ClipStore? = null
    private var broken = false
    private var returnUntil = 0L
    private val selfCopy = SelfCopyGuard(clock)
    internal var session = ClipSession().withSettings(prefs.enabled(), prefs.window()).withOrder(prefs.tabOrder()).withSmart(prefs.smart())

    var snapshot: ClipSnapshot = session.snapshot(clock())
        private set

    fun start() {
        if (subscription != null) return
        subscription = prefs.observe(::onPrefs)
        ClipEdit.attach(this)
        clipboard?.addPrimaryClipChangedListener(clipListener)
        onPrefs()
    }

    fun stop() {
        subscription?.close()
        subscription = null
        ClipEdit.detach(this)
        clipboard?.removePrimaryClipChangedListener(clipListener)
        main.removeCallbacksAndMessages(null)
        ClipUnlock.cancel()
        large.shutdown()
        worker.shutdown()
    }

    fun subscribe(listener: (ClipSnapshot) -> Unit): AutoCloseable {
        listeners += listener
        listener(snapshot)
        return AutoCloseable { listeners -= listener }
    }

    fun open() {
        ensureLoaded()
        update { it.open(clock()) }
    }

    fun close() {
        large.cancel()
        update { it.close() }
    }

    fun enable() = prefs.setEnabled(true)

    fun toggleSmart() = prefs.setSmart(!session.smart)

    fun selectKind(kind: ClipKind?) = update { it.selectKind(kind) }

    fun setEditorField(on: Boolean) {
        if (session.inEditor != on) update { it.copy(inEditor = on) }
    }

    fun setAllowed(value: Boolean) {
        large.cancel()
        update { it.setAllowed(value) }
    }

    fun selectTab(tab: ClipTab) {
        update { it.selectTab(tab) }
        if (!prefs.tabHintShown()) {
            prefs.markTabHintShown()
            toast(R.string.clip_tab_hint, Toast.LENGTH_LONG)
        }
    }

    fun setTabOrder(order: List<ClipTab>) {
        val clean = ClipTabOrder.normalize(order)
        prefs.setTabOrder(clean)
        update { it.withOrder(clean) }
    }

    fun selectSection(name: String) = update { it.selectSection(name) }

    fun togglePin(id: Long) = update { it.togglePin(id, clock()) }

    fun delete(id: Long) = update { it.delete(id, clock()) }

    fun undoDelete() = update { it.restore() }

    fun dismissUndo() = update { it.dismissUndo() }

    fun startAssign(id: Long) = update { it.startAssign(id, clock()) }

    fun assign(name: String?) = update { it.assign(name) }

    fun cancelAssign() = update { it.cancelAssign() }

    fun askClear() = update { it.askClear() }

    fun askDeleteSection(name: String) = update { it.askDeleteSection(name) }

    fun answer(yes: Boolean) = update { it.answer(yes) }

    fun startNaming() = update { it.startNaming() }

    fun startTitle(id: Long) = update { it.startTitle(id, clock()) }

    fun removeTitle(id: Long) = update { it.setTitle(id, null, clock()) }

    fun cancelNaming() = update { it.cancelNaming() }

    fun type(text: String) = update { it.type(text) }

    fun backspace() = update { it.backspace() }

    fun suggest(text: String) = update { it.suggest(text) }

    fun confirmName() = update { it.confirmName() }

    fun copy(id: Long) {
        val payload = session.payload(id, clock()) ?: return
        val offer = large.offer(payload)
        if (offer == null) {
            toast(R.string.clip_copy_failed, Toast.LENGTH_LONG)
            return
        }
        selfCopy.mark(offer.key)
        if (ClipTransfer.copy(app, offer.data, payload.sensitive)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) toast(R.string.clip_copied, Toast.LENGTH_SHORT)
        } else {
            selfCopy.clear()
            toast(R.string.clip_copy_failed, Toast.LENGTH_LONG)
        }
    }

    fun share(id: Long) {
        val payload = session.payload(id, clock()) ?: return
        holdOpen()
        if (!large.share(payload)) toast(R.string.clip_share_failed, Toast.LENGTH_LONG)
    }

    fun askPaste(id: Long) = update { it.askPaste(id, clock()) }

    fun pasteLarge(id: Long, sink: (String) -> Unit, closeWhenDone: Boolean) {
        val payload = session.payload(id, clock()) ?: return
        val blob = payload.blob ?: return
        update { it.answer(false).closeView() }
        large.stream(blob, payload.length, sink) { complete ->
            if (!complete) toast(R.string.clip_paste_stopped, Toast.LENGTH_SHORT) else if (closeWhenDone) close() else closeView()
        }
    }

    fun stopBusy(kind: BusyKind) = large.halt(kind)

    fun view(id: Long) = update { it.startView(id, clock()) }

    fun closeView() = update { it.closeView() }

    fun beginEdit(id: Long): Boolean {
        update { it.startEdit(id, clock()) }
        val granted = session.editing == id
        if (granted) holdOpen() else if (session.book.items.any { it.id == id && it.blob != null }) toast(R.string.clip_edit_large, Toast.LENGTH_SHORT)
        return granted
    }

    fun editText(id: Long): String? = session.editableText(id)

    fun edit(id: Long, text: String): EditOutcome {
        val status = session.checkEdit(id, text)
        if (status != EditStatus.OK) return EditOutcome(status)
        val merged = session.book.mergeTarget(id, text) != null
        update { it.edit(id, text) }
        return EditOutcome(EditStatus.OK, merged)
    }

    fun endEdit(id: Long) = update { it.endEdit(id) }

    fun toggleSectionLock(name: String) {
        val locking = session.book.sections.firstOrNull { it.name == name }?.locked == false
        if (locking && !deviceIsSecure()) {
            Toast.makeText(app, R.string.clip_need_lock, Toast.LENGTH_LONG).show()
            return
        }
        update { it.toggleSectionLock(name, clock()) }
    }

    fun shouldSurvive(): Boolean = session.unlocking || clock() < returnUntil

    fun settle() {
        returnUntil = 0L
    }

    fun requestUnlock() {
        if (session.unlocking) return
        holdOpen()
        update { it.beginUnlock() }
        ClipUnlock.request(app) { ok -> update { it.finishUnlock(ok, clock()) } }
    }

    private fun onBusy(value: ClipBusy?) = update { it.withBusy(value) }

    internal fun holdOpen() {
        returnUntil = clock() + RETURN_WINDOW_MS
    }

    private fun toast(message: Int, length: Int) = Toast.makeText(app, message, length).show()

    private fun deviceIsSecure(): Boolean = app.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

    private fun onPrefs() {
        if (prefs.wipeRequested()) {
            store = null
            large.detach()
            broken = false
            session = session.copy(book = ClipBook())
            prefs.clearWipeRequest()
        }
        update { it.withSettings(prefs.enabled(), prefs.window()).withOrder(prefs.tabOrder()).withSmart(prefs.smart()) }
    }

    private fun onClip() {
        if (!session.enabled || !session.allowed) return
        val clip = runCatching { clipboard?.primaryClip }.getOrNull() ?: return
        if (!ClipTransfer.acceptable(clip.description) || clip.itemCount == 0) return
        val item = clip.getItemAt(0)
        val key = item.uri?.toString() ?: item.text?.toString()
        if (key != null && selfCopy.consume(key)) return
        ensureLoaded()
        if (!broken) large.ingest(item, ::recorded)
    }

    private fun recorded(result: Intake) {
        when (result) {
            is Intake.Inline -> update { it.record(result.text, false, clock()) }
            is Intake.Large -> {
                update { it.recordLarge(result.preview, result.total, result.blob, result.digest, clock()) }
                large.discardIfUnused(result.blob, session.book)
            }
            Intake.TooLarge -> toast(R.string.clip_too_large, Toast.LENGTH_LONG)
            Intake.Blank, Intake.Failed, Intake.Cancelled -> Unit
        }
    }

    internal fun ensureLoaded() {
        if (store != null || broken) return
        val opened = ClipStoreFactory.open(app) ?: run { broken = true; return }
        store = opened.store
        large.blobs = opened.blobs
        session = session.copy(book = opened.book)
    }

    private fun update(change: (ClipSession) -> ClipSession) {
        val before = session
        session = change(before)
        if (session.book != before.book) persist(session.book)
        publish()
    }

    private fun persist(book: ClipBook) {
        val target = store ?: return
        val pool = large.blobs
        val keep = book.items.mapNotNull { it.blob }.toSet()
        runCatching {
            worker.execute {
                target.save(book)
                pool?.sweep(keep, clock())
            }
        }
    }

    private fun publish() {
        val now = clock()
        main.removeCallbacks(relock)
        session.relockAt(now)?.let { main.postDelayed(relock, it - now + RELOCK_SLACK_MS) }
        val next = session.snapshot(now)
        if (next == snapshot) return
        snapshot = next
        listeners.toList().forEach { it(next) }
    }

    private companion object {
        const val RELOCK_SLACK_MS = 50L
        const val RETURN_WINDOW_MS = 60_000L
    }
}
