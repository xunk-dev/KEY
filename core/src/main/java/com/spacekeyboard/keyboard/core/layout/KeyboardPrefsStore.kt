package com.spacekeyboard.keyboard.core.layout

import android.content.Context
import android.content.SharedPreferences

class KeyboardPrefsStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun read(): KeyboardPrefs = KeyboardPrefs(
        keySizePercent = prefs.getInt(KEY_SIZE, 100),
        heightPercent = prefs.getInt(HEIGHT, 100),
        mode = PanelMode.byId(prefs.getString(MODE, null)),
        oneHandPercent = prefs.getInt(ONE_HAND, 80),
        splitGapPercent = prefs.getInt(SPLIT_GAP, 16),
        label = LabelStyle(
            font = LabelFont.byId(prefs.getString(FONT, null)),
            bold = prefs.getBoolean(BOLD, false),
            italic = prefs.getBoolean(ITALIC, false),
            scalePercent = prefs.getInt(LABEL_SCALE, Limits.LABEL_DEFAULT),
        ),
        orders = LayoutCatalog.ids.mapNotNull { id -> readOrder(id)?.let { id to it } }.toMap(),
        toolbar = ToolbarItem.parse(prefs.getString(TOOLBAR, null)),
        language = prefs.getString(LANGUAGE, LayoutCatalog.default) ?: LayoutCatalog.default,
        feedback = FeedbackPrefs(
            vibrate = prefs.getBoolean(VIBRATE, true),
            sound = prefs.getBoolean(SOUND, false),
            vibrateStrength = prefs.getInt(VIBRATE_STRENGTH, Limits.STRENGTH_DEFAULT),
            soundVolume = prefs.getInt(SOUND_VOLUME, Limits.VOLUME_DEFAULT),
            preview = prefs.getBoolean(PREVIEW, true),
            clipKeepOpen = prefs.getBoolean(CLIP_KEEP_OPEN, false),
        ),
        languages = prefs.getString(LANGUAGES, null)?.split(COL)?.filter { it.isNotEmpty() } ?: LayoutCatalog.defaultLanguages,
        typing = TypingPrefs(
            numberRow = prefs.getBoolean(NUMBER_ROW, false),
            nativeDigits = prefs.getBoolean(NATIVE_DIGITS, false),
            doubleSpace = prefs.getBoolean(DOUBLE_SPACE, true),
            incognito = prefs.getBoolean(INCOGNITO, false),
            suggestions = prefs.getBoolean(SUGGESTIONS, true),
            rememberLanguage = prefs.getBoolean(REMEMBER_LANGUAGE, true),
            touchAssist = prefs.getBoolean(TOUCH_ASSIST, true),
            hardwareKeys = prefs.getBoolean(HARDWARE_KEYS, true),
            autoLanguage = prefs.getBoolean(AUTO_LANGUAGE, true),
        ),
    ).sanitized()

    fun setKeySize(value: Int) = saveInt(KEY_SIZE, value.coerceIn(Limits.KEY_MIN, Limits.KEY_MAX))

    fun setHeight(value: Int) = saveInt(HEIGHT, value.coerceIn(Limits.HEIGHT_MIN, Limits.HEIGHT_MAX))

    fun setOneHand(value: Int) = saveInt(ONE_HAND, value.coerceIn(Limits.ONE_HAND_MIN, Limits.ONE_HAND_MAX))

    fun setSplitGap(value: Int) = saveInt(SPLIT_GAP, value.coerceIn(Limits.SPLIT_MIN, Limits.SPLIT_MAX))

    fun setLabelScale(value: Int) = saveInt(LABEL_SCALE, value.coerceIn(Limits.LABEL_MIN, Limits.LABEL_MAX))

    fun setMode(mode: PanelMode) = edit { putString(MODE, mode.id) }

    fun setFont(font: LabelFont) = edit { putString(FONT, font.id) }

    fun setBold(value: Boolean) = edit { putBoolean(BOLD, value) }

    fun setItalic(value: Boolean) = edit { putBoolean(ITALIC, value) }

    fun setVibrate(value: Boolean) = edit { putBoolean(VIBRATE, value) }

    fun setSound(value: Boolean) = edit { putBoolean(SOUND, value) }

    fun setVibrateStrength(value: Int) = saveInt(VIBRATE_STRENGTH, value.coerceIn(Limits.STRENGTH_MIN, Limits.STRENGTH_MAX))

    fun setSoundVolume(value: Int) = saveInt(SOUND_VOLUME, value.coerceIn(Limits.VOLUME_MIN, Limits.VOLUME_MAX))

    fun setPreview(value: Boolean) = edit { putBoolean(PREVIEW, value) }

    fun setClipKeepOpen(value: Boolean) = edit { putBoolean(CLIP_KEEP_OPEN, value) }

    fun setLanguage(id: String) = edit { putString(LANGUAGE, if (id in LayoutCatalog.ids) id else LayoutCatalog.default) }

    fun setLanguageEnabled(id: String, on: Boolean) {
        if (id !in LayoutCatalog.optional) return
        val now = read().languages.filter { it != id }
        val next = if (on) LayoutCatalog.ids.filter { it in now || it == id } else now
        edit { putString(LANGUAGES, next.joinToString(COL)) }
    }

    fun setNumberRow(value: Boolean) = edit { putBoolean(NUMBER_ROW, value) }

    fun setNativeDigits(value: Boolean) = edit { putBoolean(NATIVE_DIGITS, value) }

    fun setDoubleSpace(value: Boolean) = edit { putBoolean(DOUBLE_SPACE, value) }

    fun setIncognito(value: Boolean) = edit { putBoolean(INCOGNITO, value) }

    fun setSuggestions(value: Boolean) = edit { putBoolean(SUGGESTIONS, value) }

    fun setRememberLanguage(value: Boolean) = edit { putBoolean(REMEMBER_LANGUAGE, value) }

    fun setTouchAssist(value: Boolean) = edit { putBoolean(TOUCH_ASSIST, value) }

    fun setHardwareKeys(value: Boolean) = edit { putBoolean(HARDWARE_KEYS, value) }

    fun setAutoLanguage(value: Boolean) = edit { putBoolean(AUTO_LANGUAGE, value) }

    fun setToolbar(items: List<ToolbarItem>) = edit { putString(TOOLBAR, items.joinToString(",") { it.id }) }

    fun setOrder(layoutId: String, order: List<List<String>>) =
        edit { putString(orderKey(layoutId), order.joinToString(ROW) { it.joinToString(COL) }) }

    fun resetOrder(layoutId: String) = edit { remove(orderKey(layoutId)) }

    fun resetToolbar() = edit { remove(TOOLBAR) }

    fun resetAll() = edit {
        listOf(
            KEY_SIZE, HEIGHT, MODE, ONE_HAND, SPLIT_GAP, FONT, BOLD, ITALIC, LABEL_SCALE, TOOLBAR,
            VIBRATE, SOUND, VIBRATE_STRENGTH, SOUND_VOLUME, PREVIEW, CLIP_KEEP_OPEN, LANGUAGES, NUMBER_ROW, NATIVE_DIGITS, DOUBLE_SPACE, INCOGNITO, SUGGESTIONS, REMEMBER_LANGUAGE,
            TOUCH_ASSIST, HARDWARE_KEYS, AUTO_LANGUAGE,
        ).forEach { remove(it) }
        LayoutCatalog.ids.forEach { remove(orderKey(it)) }
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun readOrder(layoutId: String): List<List<String>>? =
        prefs.getString(orderKey(layoutId), null)?.takeIf { it.isNotEmpty() }
            ?.split(ROW)?.map { it.split(COL) }

    private fun saveInt(key: String, value: Int) = edit { putInt(key, value) }

    private fun edit(block: SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
    }

    private fun orderKey(layoutId: String) = "order.$layoutId"

    private companion object {
        const val NAME = "space_keyboard"
        const val KEY_SIZE = "key_size"
        const val HEIGHT = "height"
        const val MODE = "mode"
        const val ONE_HAND = "one_hand"
        const val SPLIT_GAP = "split_gap"
        const val FONT = "label_font"
        const val BOLD = "label_bold"
        const val ITALIC = "label_italic"
        const val LABEL_SCALE = "label_scale"
        const val TOOLBAR = "toolbar"
        const val LANGUAGE = "language"
        const val VIBRATE = "feedback_vibrate"
        const val SOUND = "feedback_sound"
        const val VIBRATE_STRENGTH = "feedback_vibrate_strength"
        const val SOUND_VOLUME = "feedback_sound_volume"
        const val PREVIEW = "feedback_preview"
        const val CLIP_KEEP_OPEN = "clip_keep_open"
        const val LANGUAGES = "languages"
        const val NUMBER_ROW = "number_row"
        const val NATIVE_DIGITS = "native_digits"
        const val DOUBLE_SPACE = "double_space"
        const val INCOGNITO = "incognito"
        const val SUGGESTIONS = "suggestions"
        const val REMEMBER_LANGUAGE = "remember_language"
        const val TOUCH_ASSIST = "touch_assist"
        const val HARDWARE_KEYS = "hardware_keys"
        const val AUTO_LANGUAGE = "auto_language"
        const val ROW = ";"
        const val COL = ","
    }
}
