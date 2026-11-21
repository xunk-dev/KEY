package com.spacekeyboard.keyboard.decorate.ui

import com.spacekeyboard.keyboard.decorate.DecorStyle
import com.spacekeyboard.keyboard.decorate.Language

interface DecorActions {
    fun onKeys()
    fun onApply(style: DecorStyle)
    fun onLanguage(language: Language)
    fun onFavorite(id: String)
    fun onActive(id: String)
    fun onFavoritesOnly()
    fun onClearActive()
}
