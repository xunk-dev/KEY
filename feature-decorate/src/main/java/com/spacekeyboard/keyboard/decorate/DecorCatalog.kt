package com.spacekeyboard.keyboard.decorate

object DecorCatalog {
    val all: List<DecorStyle> = EnglishStyles.all + ArabicStyles.all

    fun forLanguage(language: Language): List<DecorStyle> = all.filter { it.language == language }

    fun byId(id: String): DecorStyle? = all.firstOrNull { it.id == id }
}
