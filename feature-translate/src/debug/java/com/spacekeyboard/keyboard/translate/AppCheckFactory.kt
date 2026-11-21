package com.spacekeyboard.keyboard.translate

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

internal object AppCheckFactory {
    fun create(): AppCheckProviderFactory = DebugAppCheckProviderFactory.getInstance()
}
