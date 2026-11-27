package com.spacekeyboard.keyboard.ime

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.clipboard.ClipItem
import com.spacekeyboard.keyboard.clipboard.ClipKind
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.clipboard.latestItem
import com.spacekeyboard.keyboard.translate.TranslatePolicy

class ClipTextOffer(
    context: Context,
    private val clip: ClipboardController,
    private val insert: (String) -> Unit,
    private val translate: (String) -> Unit,
    private val translateAllowed: () -> Boolean,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val manager = context.getSystemService(ClipboardManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val expire = Runnable { refresh() }
    private var handled: Pair<Long, Long>? = null
    private var on = false

    var offer by mutableStateOf<ClipItem?>(null)
        private set

    val canTranslate: Boolean
        get() = offer?.let { it.text.length <= TranslatePolicy.MAX_TEXT } == true && translateAllowed()

    fun sync(value: Boolean) {
        if (value == on) return
        on = value
        refresh()
    }

    fun refresh() {
        handler.removeCallbacks(expire)
        val item = if (on && currentIsText()) clip.latestItem()?.takeIf(::qualifies) else null
        offer = item
        if (item != null) handler.postDelayed(expire, item.copiedAt + LIFETIME_MS - now() + GRACE_MS)
    }

    fun paste() {
        val current = offer ?: return
        dismiss()
        insert(current.text)
    }

    fun translateCopied() {
        val current = offer ?: return
        if (!canTranslate) return
        dismiss()
        translate(current.text)
    }

    fun dismiss() {
        handled = offer?.let { it.id to it.copiedAt } ?: handled
        offer = null
        handler.removeCallbacks(expire)
    }

    private fun qualifies(item: ClipItem): Boolean {
        val age = now() - item.copiedAt
        return item.blob == null && item.text.isNotBlank() && item.text.length <= MAX_CHARS &&
            age in 0..LIFETIME_MS && (item.id to item.copiedAt) != handled && ClipKind.TOKEN !in item.kinds
    }

    private fun currentIsText(): Boolean {
        val description = runCatching { manager?.primaryClipDescription }.getOrNull() ?: return false
        val sensitive = description.extras?.getBoolean(SENSITIVE, false) == true
        return !sensitive && description.filterMimeTypes(TEXT) != null
    }

    private companion object {
        const val TEXT = "text/*"
        const val SENSITIVE = "android.content.extra.IS_SENSITIVE"
        const val LIFETIME_MS = 120_000L
        const val GRACE_MS = 50L
        const val MAX_CHARS = 5000
    }
}
