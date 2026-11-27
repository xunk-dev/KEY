package com.spacekeyboard.keyboard.translate

import android.content.Context
import android.content.SharedPreferences
import java.util.Locale
import java.util.UUID

class TranslatePrefsStore(private val prefs: SharedPreferences, private val locale: Locale = Locale.getDefault()) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun consent(): Boolean = prefs.getInt(CONSENT, 0) == TranslatePolicy.CONSENT_VERSION

    fun grantConsent() {
        prefs.edit().putInt(CONSENT, TranslatePolicy.CONSENT_VERSION).putString(ROUTE, TranslateRoute.AI.id).apply()
    }

    fun withdrawConsent() {
        prefs.edit().remove(CONSENT).remove(INSTALL).putString(ROUTE, TranslateRoute.DEVICE.id).apply()
    }

    fun route(): TranslateRoute = TranslateRoute.byId(prefs.getString(ROUTE, null))

    fun setRoute(route: TranslateRoute) {
        if (route == TranslateRoute.AI && !consent()) return
        prefs.edit().putString(ROUTE, route.id).apply()
    }

    fun target(): String {
        val saved = prefs.getString(TARGET, null)
        return if (saved != null && TranslateLanguages.isSupported(saved)) saved else TranslateLanguages.defaultTarget(locale)
    }

    fun setTarget(code: String) {
        if (!TranslateLanguages.isSupported(code)) return
        val recent = (listOf(code) + recent().filter { it != code }).take(MAX_RECENT)
        prefs.edit().putString(TARGET, code).putString(RECENT, recent.joinToString(",")).apply()
    }

    fun pinned(): List<String> = list(PINNED) ?: DEFAULT_PINNED

    fun togglePin(code: String) {
        if (!TranslateLanguages.isSupported(code)) return
        val now = pinned()
        val next = if (code in now) now - code else (now + code).takeLast(MAX_PINNED)
        prefs.edit().putString(PINNED, next.joinToString(",")).apply()
    }

    fun recent(): List<String> = list(RECENT) ?: emptyList()

    private fun list(key: String): List<String>? =
        prefs.getString(key, null)?.split(',')?.filter { TranslateLanguages.isSupported(it) }

    fun style(): TranslateStyle = TranslateStyle.byId(prefs.getString(STYLE, null))

    fun setStyle(style: TranslateStyle) {
        prefs.edit().putString(STYLE, style.id).apply()
    }

    fun modelsWifiOnly(): Boolean = prefs.getBoolean(WIFI_ONLY, true)

    fun setModelsWifiOnly(value: Boolean) {
        prefs.edit().putBoolean(WIFI_ONLY, value).apply()
    }

    fun notifyAsked(): Boolean = prefs.getBoolean(NOTIFY_ASKED, false)

    fun setNotifyAsked() {
        prefs.edit().putBoolean(NOTIFY_ASKED, true).apply()
    }

    fun installId(): String {
        prefs.getString(INSTALL, null)?.let { return it }
        return UUID.randomUUID().toString().also { prefs.edit().putString(INSTALL, it).apply() }
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object {
        const val NAME = "space_keyboard_translate"
        const val CONSENT = "consent_version"
        const val ROUTE = "route"
        const val TARGET = "target"
        const val STYLE = "style"
        const val INSTALL = "install_id"
        const val WIFI_ONLY = "models_wifi_only"
        const val NOTIFY_ASKED = "models_notify_asked"
        const val PINNED = "pinned_languages"
        const val RECENT = "recent_languages"
        const val MAX_PINNED = 8
        const val MAX_RECENT = 4
        val DEFAULT_PINNED = listOf("en", "ar", "fr", "es")
    }
}
