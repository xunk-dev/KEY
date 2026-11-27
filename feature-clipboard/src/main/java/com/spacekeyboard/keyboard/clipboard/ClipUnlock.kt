package com.spacekeyboard.keyboard.clipboard

import android.content.Context
import android.content.Intent

object ClipUnlock {
    private var pending: ((Boolean) -> Unit)? = null

    fun request(context: Context, onResult: (Boolean) -> Unit) {
        pending = onResult
        val intent = Intent(context, ClipUnlockActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (runCatching { context.startActivity(intent) }.isFailure) deliver(false)
    }

    fun cancel() {
        pending = null
    }

    internal fun deliver(ok: Boolean) {
        val callback = pending
        pending = null
        callback?.invoke(ok)
    }
}
