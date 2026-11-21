package com.spacekeyboard.keyboard.clipboard

import android.app.KeyguardManager
import android.content.ClipDescription
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
    private val listeners = LinkedHashSet<(ClipSnapshot) -> Unit>()
    private val relock = Runnable { publish() }
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener { onClip() }
    private var subscription: AutoCloseable? = null
    private var store: ClipStore? = null
    private var broken = false
    private var returnUntil = 0L
    private var session = ClipSession().withSettings(prefs.enabled(), prefs.window())

    var snapshot: ClipSnapshot = session.snapshot(clock())
        private set

    fun start() {
        if (subscription != null) return
        subscription = prefs.observe(::onPrefs)
        clipboard?.addPrimaryClipChangedListener(clipListener)
        onPrefs()
    }

    fun stop() {
        subscription?.close()
        subscription = null
        clipboard?.removePrimaryClipChangedListener(clipListener)
        main.removeCallbacksAndMessages(null)
        ClipUnlock.cancel()
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

    fun close() = update { it.close() }

    fun toggle() = if (snapshot.open) close() else open()

    fun setAllowed(value: Boolean) = update { it.setAllowed(value) }

    fun selectTab(tab: ClipTab) = update { it.selectTab(tab) }

    fun selectSection(name: String) = update { it.selectSection(name) }

    fun togglePin(id: Long) = update { it.togglePin(id, clock()) }

    fun delete(id: Long) = update { it.delete(id, clock()) }

    fun startAssign(id: Long) = update { it.startAssign(id, clock()) }

    fun assign(name: String?) = update { it.assign(name) }

    fun cancelAssign() = update { it.cancelAssign() }

    fun askClear() = update { it.askClear() }

    fun askDeleteSection(name: String) = update { it.askDeleteSection(name) }

    fun answer(yes: Boolean) = update { it.answer(yes) }

    fun startNaming() = update { it.startNaming() }

    fun cancelNaming() = update { it.cancelNaming() }

    fun type(text: String) = update { it.type(text) }

    fun backspace() = update { it.backspace() }

    fun confirmName() = update { it.confirmName() }

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
        returnUntil = clock() + RETURN_WINDOW_MS
        update { it.beginUnlock() }
        ClipUnlock.request(app) { ok -> update { it.finishUnlock(ok, clock()) } }
    }

    private fun deviceIsSecure(): Boolean = app.getSystemService(KeyguardManager::class.java)?.isDeviceSecure == true

    private fun onPrefs() {
        if (prefs.wipeRequested()) {
            store = null
            broken = false
            session = session.copy(book = ClipBook())
            prefs.clearWipeRequest()
        }
        update { it.withSettings(prefs.enabled(), prefs.window()) }
    }

    private fun onClip() {
        if (!session.enabled || !session.allowed) return
        val clip = runCatching { clipboard?.primaryClip }.getOrNull() ?: return
        val description = clip.description
        val textual = description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
            description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
        if (!textual || clip.itemCount == 0) return
        val text = clip.getItemAt(0).coerceToText(app)?.toString().orEmpty()
        ensureLoaded()
        if (!broken) update { it.record(text, isSensitive(description), clock()) }
    }

    private fun isSensitive(description: ClipDescription): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && description.extras?.getBoolean(SENSITIVE, false) == true

    private fun ensureLoaded() {
        if (store != null || broken) return
        val opened = ClipStoreFactory.open(app)
        if (opened == null) {
            broken = true
            return
        }
        store = opened.store
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
        runCatching { worker.execute { target.save(book) } }
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
        const val SENSITIVE = "android.content.extra.IS_SENSITIVE"
        const val RELOCK_SLACK_MS = 50L
        const val RETURN_WINDOW_MS = 60_000L
    }
}
