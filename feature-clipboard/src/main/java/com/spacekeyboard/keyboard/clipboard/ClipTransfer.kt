package com.spacekeyboard.keyboard.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle

object ClipTransfer {
    private const val SENSITIVE = "android.content.extra.IS_SENSITIVE"
    private const val PLAIN = "text/plain"

    fun copy(context: Context, text: String, sensitive: Boolean): Boolean =
        copy(context, ClipData.newPlainText(null, text), sensitive)

    fun copy(context: Context, clip: ClipData, sensitive: Boolean): Boolean {
        val manager = context.getSystemService(ClipboardManager::class.java) ?: return false
        if (sensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            clip.description.extras = PersistableBundle().apply { putBoolean(SENSITIVE, true) }
        }
        return runCatching { manager.setPrimaryClip(clip) }.isSuccess
    }

    fun acceptable(description: ClipDescription): Boolean {
        val textual = description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
            description.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)
        val sensitive = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && description.extras?.getBoolean(SENSITIVE, false) == true
        return textual && !sensitive
    }

    fun shareUri(context: Context, uri: Uri): Boolean {
        val send = Intent(Intent.ACTION_SEND).setType(PLAIN).putExtra(Intent.EXTRA_STREAM, uri)
        send.clipData = ClipData.newRawUri(null, uri)
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooser = Intent.createChooser(send, context.getString(R.string.clip_share_title))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return runCatching { context.startActivity(chooser) }.isSuccess
    }

    fun share(context: Context, text: String): Boolean {
        val send = Intent(Intent.ACTION_SEND).setType(PLAIN).putExtra(Intent.EXTRA_TEXT, text)
        val chooser = Intent.createChooser(send, context.getString(R.string.clip_share_title))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(chooser) }.isSuccess
    }
}
