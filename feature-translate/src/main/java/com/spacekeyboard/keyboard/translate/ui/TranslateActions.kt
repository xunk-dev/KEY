package com.spacekeyboard.keyboard.translate.ui

import com.spacekeyboard.keyboard.translate.TranslateSource

interface TranslateActions {
    fun onKeys()
    fun onUse(text: String, kind: TranslateSource)
    fun onReport()
}
