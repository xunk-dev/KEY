package com.spacekeyboard.keyboard

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log

internal object DebugLog {
    fun failure(context: Context, where: String, error: Throwable) {
        val debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        if (debuggable) Log.w("SpaceKeyboard", "$where failed: ${error.javaClass.simpleName}")
    }
}
