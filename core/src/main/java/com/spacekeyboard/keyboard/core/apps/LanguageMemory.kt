package com.spacekeyboard.keyboard.core.apps

class LanguageMemory(
    private val store: AppLanguageStore,
    private val current: () -> String,
    private val enabled: () -> Boolean,
    private val select: (String) -> Boolean,
) {
    private var app: String? = null
    private var recording = false
    private var applying = false
    private var last = current()

    fun begin(packageName: String?, record: Boolean) {
        app = packageName?.takeIf { it.isNotEmpty() }
        recording = record
    }

    fun end() {
        app = null
        recording = false
    }

    fun restore() {
        val name = app ?: return
        if (!enabled()) return
        val saved = store.get(name) ?: return
        applying = true
        select(saved)
        applying = false
        last = current()
    }

    fun onLanguage(language: String) {
        if (language == last) return
        last = language
        val name = app ?: return
        if (!applying && recording && enabled()) store.put(name, language)
    }
}
