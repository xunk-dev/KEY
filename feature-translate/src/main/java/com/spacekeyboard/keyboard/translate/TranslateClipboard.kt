package com.spacekeyboard.keyboard.translate

import android.content.ClipboardManager
import android.content.Context

object TranslateClipboard {
    fun read(context: Context): String {
        val clip = context.getSystemService(ClipboardManager::class.java)?.primaryClip ?: return ""
        if (clip.itemCount == 0) return ""
        return clip.getItemAt(0).coerceToText(context)?.toString().orEmpty()
    }
}
