package com.spacekeyboard.keyboard.core

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

enum class KeyboardStatus { DISABLED, ENABLED, SELECTED }

object KeyboardStatusReader {
    fun read(context: Context): KeyboardStatus {
        val manager = context.getSystemService(InputMethodManager::class.java)
        val packageName = context.packageName
        val enabled = manager.enabledInputMethodList.any { it.packageName == packageName }
        if (!enabled) return KeyboardStatus.DISABLED
        val current = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD,
        )
        val selected = current?.startsWith("$packageName/") == true
        return if (selected) KeyboardStatus.SELECTED else KeyboardStatus.ENABLED
    }
}
