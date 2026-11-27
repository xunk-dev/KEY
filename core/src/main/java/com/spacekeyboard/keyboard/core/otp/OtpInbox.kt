package com.spacekeyboard.keyboard.core.otp

import java.util.concurrent.CopyOnWriteArrayList

class OtpInbox(private val now: () -> Long = System::currentTimeMillis) {
    private val listeners = CopyOnWriteArrayList<() -> Unit>()
    private val spent = LinkedHashMap<String, Long>()
    private var latest: OtpCode? = null

    fun offer(code: OtpCode): Boolean {
        val changed = synchronized(this) {
            prune()
            val same = latest?.value == code.value
            val blocked = spent.containsKey(code.value) || code.expiresAt <= now()
            if (same || blocked) false else {
                latest = code
                true
            }
        }
        if (changed) notifyListeners()
        return changed
    }

    @Synchronized
    fun current(): OtpCode? {
        val held = latest ?: return null
        if (held.expiresAt > now()) return held
        latest = null
        return null
    }

    fun consume(code: OtpCode) = retire(code)

    fun dismiss(code: OtpCode) = retire(code)

    fun clear() {
        val had = synchronized(this) { (latest != null).also { latest = null } }
        if (had) notifyListeners()
    }

    fun subscribe(listener: () -> Unit): AutoCloseable {
        listeners += listener
        return AutoCloseable { listeners -= listener }
    }

    private fun retire(code: OtpCode) {
        val changed = synchronized(this) {
            spent[code.value] = now()
            if (latest?.value == code.value) {
                latest = null
                true
            } else {
                false
            }
        }
        if (changed) notifyListeners()
    }

    private fun prune() {
        val limit = now() - SPENT_MS
        spent.entries.removeAll { it.value < limit }
        while (spent.size > SPENT_MAX) spent.remove(spent.keys.first())
    }

    private fun notifyListeners() = listeners.forEach { it() }

    companion object {
        const val SPENT_MS = 600_000L
        const val SPENT_MAX = 16
        val shared = OtpInbox()
    }
}
