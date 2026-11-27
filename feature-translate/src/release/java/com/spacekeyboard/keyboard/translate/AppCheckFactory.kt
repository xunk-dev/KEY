package com.spacekeyboard.keyboard.translate

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

internal object AppCheckFactory {
    fun create(): AppCheckProviderFactory = PlayIntegrityAppCheckProviderFactory.getInstance()
}
