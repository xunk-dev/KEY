package com.spacekeyboard.keyboard.ime

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputContentInfoCompat
import java.util.concurrent.Executors

class ImageOffer(val uri: Uri, val mime: String, val thumb: ImageBitmap)

class ClipImageOffer(
    private val context: Context,
    private val connection: () -> InputConnection?,
    private val editor: () -> EditorInfo?,
) {
    private val manager = context.getSystemService(ClipboardManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private val listener = ClipboardManager.OnPrimaryClipChangedListener { refresh() }
    private val expire = Runnable { dismiss() }
    private var handled: String? = null
    private var pending: String? = null
    private var registered = false
    private var on by mutableStateOf(false)
    private var accepted by mutableStateOf<List<String>>(emptyList())

    var offer by mutableStateOf<ImageOffer?>(null)
        private set

    val visible: Boolean
        get() = on && offer?.let { current -> accepted.any { ClipDescription.compareMimeTypes(current.mime, it) } } == true

    fun sync(value: Boolean) {
        val next = value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
        val types = editor()?.let { EditorInfoCompat.getContentMimeTypes(it).toList() }.orEmpty()
        if (next == on && types == accepted) return
        on = next
        accepted = types
        if (!on) {
            stop()
            return
        }
        if (!registered) manager?.addPrimaryClipChangedListener(listener)
        registered = true
        refresh()
    }

    fun stop() {
        if (registered) manager?.removePrimaryClipChangedListener(listener)
        registered = false
        on = false
        clear()
    }

    fun refresh() {
        if (!on) return
        val clip = runCatching { manager?.primaryClip }.getOrNull()
        val found = clip?.let(::imageOf)
        if (found == null) {
            clear()
            return
        }
        val key = found.first.toString()
        if (key == handled || key == pending || key == offer?.uri?.toString()) return
        pending = key
        load(found.first, found.second)
    }

    fun send() {
        val current = offer ?: return
        val target = connection()
        val info = editor()
        if (target == null || info == null || !commit(current, target, info)) {
            Toast.makeText(context, R.string.clip_image_unsupported, Toast.LENGTH_LONG).show()
            return
        }
        dismiss()
    }

    fun dismiss() {
        handled = offer?.uri?.toString() ?: handled
        clear()
    }

    private fun commit(current: ImageOffer, target: InputConnection, info: EditorInfo): Boolean {
        val description = ClipDescription(context.getString(R.string.clip_image_label), arrayOf(current.mime))
        val content = InputContentInfoCompat(current.uri, description, null)
        val flags = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION
        return InputConnectionCompat.commitContent(target, info, content, flags, null)
    }

    private fun imageOf(clip: ClipData): Pair<Uri, String>? {
        if (clip.itemCount == 0 || isSensitive(clip.description)) return null
        val uri = clip.getItemAt(0).uri ?: return null
        val mime = clip.description.filterMimeTypes(IMAGE)?.firstOrNull() ?: return null
        return uri to mime
    }

    private fun isSensitive(description: ClipDescription): Boolean =
        description.extras?.getBoolean(SENSITIVE, false) == true

    private fun load(uri: Uri, mime: String) {
        val key = uri.toString()
        worker.execute {
            val thumb = decode(uri)
            handler.post { if (pending == key) show(key, thumb?.let { ImageOffer(uri, mime, it) }) }
        }
    }

    private fun show(key: String, next: ImageOffer?) {
        pending = null
        if (!on || next == null) {
            handled = key
            return
        }
        offer = next
        handler.removeCallbacks(expire)
        handler.postDelayed(expire, LIFETIME_MS)
    }

    private fun clear() {
        handler.removeCallbacks(expire)
        pending = null
        offer = null
    }

    private fun decode(uri: Uri): ImageBitmap? = runCatching {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleFor(maxOf(bounds.outWidth, bounds.outHeight)) }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }?.asImageBitmap()
    }.getOrNull()

    private fun sampleFor(side: Int): Int {
        var sample = 1
        while (side / (sample * 2) >= THUMB_PX) sample *= 2
        return sample
    }

    private companion object {
        const val IMAGE = "image/*"
        const val SENSITIVE = "android.content.extra.IS_SENSITIVE"
        const val LIFETIME_MS = 120_000L
        const val THUMB_PX = 96
    }
}
