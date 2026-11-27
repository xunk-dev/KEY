package com.spacekeyboard.keyboard.core.layout

import android.content.Context
import android.content.SharedPreferences
import com.spacekeyboard.keyboard.core.toolbar.BarArrangement

class KeyboardPrefsStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE),
    )

    fun read(): KeyboardPrefs = KeyboardPrefs(
        keySizePercent = prefs.getInt(KEY_SIZE, 100),
        heightPercent = prefs.getInt(HEIGHT, 100),
        mode = PanelMode.byId(prefs.getString(MODE, null)),
        oneHandPercent = prefs.getInt(ONE_HAND, 80),
        widthPercent = prefs.getInt(WIDTH, Limits.WIDTH_MAX),
        splitGapPercent = prefs.getInt(SPLIT_GAP, 16),
        floatWidthPercent = prefs.getInt(FLOAT_WIDTH, Limits.FLOAT_WIDTH_DEFAULT),
        floatHeightPercent = prefs.getInt(FLOAT_HEIGHT, Limits.FLOAT_HEIGHT_DEFAULT),
        floatOpacityPercent = prefs.getInt(FLOAT_OPACITY, Limits.FLOAT_OPACITY_MAX),
        floatLeft = prefs.getInt(FLOAT_LEFT, Limits.centeredLeft(Limits.FLOAT_WIDTH_DEFAULT)),
        floatTop = prefs.getInt(FLOAT_TOP, Limits.FLOAT_START_TOP),
        label = LabelStyle(
            font = LabelFont.byId(prefs.getString(FONT, null)),
            bold = prefs.getBoolean(BOLD, false),
            italic = prefs.getBoolean(ITALIC, false),
            scalePercent = prefs.getInt(LABEL_SCALE, Limits.LABEL_DEFAULT),
        ),
        orders = LayoutCatalog.ids.mapNotNull { id -> readOrder(id)?.let { id to it } }.toMap(),
        keyFns = LayoutCatalog.ids.associateWith { KeyFn.read(prefs, it) }.filterValues { it.isNotEmpty() },
        toolbar = ToolbarItem.parse(prefs.getString(TOOLBAR, null)),
        language = prefs.getString(LANGUAGE, LayoutCatalog.default) ?: LayoutCatalog.default,
        feedback = FeedbackPrefs(
            vibrate = prefs.getBoolean(VIBRATE, true),
            sound = prefs.getBoolean(SOUND, false),
            vibrateStrength = prefs.getInt(VIBRATE_STRENGTH, Limits.STRENGTH_DEFAULT),
            soundVolume = prefs.getInt(SOUND_VOLUME, Limits.VOLUME_DEFAULT),
            preview = prefs.getBoolean(PREVIEW, false),
            clipKeepOpen = prefs.getBoolean(CLIP_KEEP_OPEN, false),
        ),
        languages = prefs.getString(LANGUAGES, null)?.split(COL)?.filter { it.isNotEmpty() } ?: LayoutCatalog.defaultLanguages,
        arabicStyle = ArabicStyle.byId(prefs.getString(ARABIC_STYLE, null)),
        dim = DimPrefs(levelPercent = prefs.getInt(DIM_LEVEL, DimLimits.LEVEL_DEFAULT)),
        barLayout = prefs.getString(BAR, null),
        typing = TypingPrefs(
            numberRow = prefs.getBoolean(NUMBER_ROW, true),
            nativeDigits = prefs.getBoolean(NATIVE_DIGITS, false),
            doubleSpace = prefs.getBoolean(DOUBLE_SPACE, true),
            incognito = prefs.getBoolean(INCOGNITO, false),
            suggestions = prefs.getBoolean(SUGGESTIONS, true),
            imageSuggestions = prefs.getBoolean(IMAGE_SUGGESTIONS, true),
            emojiSuggestions = prefs.getBoolean(EMOJI_SUGGESTIONS, true),
            appSuggestions = prefs.getBoolean(APP_SUGGESTIONS, true),
            copyOffers = prefs.getBoolean(COPY_OFFERS, true),
            otpOffers = prefs.getBoolean(OTP_OFFERS, true),
            otpNotifications = prefs.getBoolean(OTP_NOTIFICATIONS, false),
            rememberLanguage = prefs.getBoolean(REMEMBER_LANGUAGE, true),
            touchAssist = prefs.getBoolean(TOUCH_ASSIST, true),
            glide = prefs.getBoolean(GLIDE, true),
            convertOffers = prefs.getBoolean(CONVERT_OFFERS, true),
            spellFix = prefs.getBoolean(SPELL_FIX, true),
            hardwareKeys = prefs.getBoolean(HARDWARE_KEYS, true),
            autoLanguage = prefs.getBoolean(AUTO_LANGUAGE, true),
            autoCapitalize = prefs.getBoolean(AUTO_CAPITALIZE, true),
        ),
    ).sanitized()

    fun setKeySize(value: Int) = saveInt(KEY_SIZE, value.coerceIn(Limits.KEY_MIN, Limits.KEY_MAX))

    fun setHeight(value: Int) = saveInt(HEIGHT, value.coerceIn(Limits.HEIGHT_MIN, Limits.HEIGHT_MAX))

    fun setOneHand(value: Int) = saveInt(ONE_HAND, value.coerceIn(Limits.ONE_HAND_MIN, Limits.ONE_HAND_MAX))

    fun setWidth(value: Int) = saveInt(WIDTH, value.coerceIn(Limits.WIDTH_MIN, Limits.WIDTH_MAX))

    fun setSplitGap(value: Int) = saveInt(SPLIT_GAP, value.coerceIn(Limits.SPLIT_MIN, Limits.SPLIT_MAX))

    fun setFloatWidth(value: Int) = saveInt(FLOAT_WIDTH, value.coerceIn(Limits.FLOAT_WIDTH_MIN, Limits.FLOAT_WIDTH_MAX))

    fun setFloatHeight(value: Int) = saveInt(FLOAT_HEIGHT, value.coerceIn(Limits.FLOAT_HEIGHT_MIN, Limits.FLOAT_HEIGHT_MAX))

    fun setFloatOpacity(value: Int) = saveInt(FLOAT_OPACITY, value.coerceIn(Limits.FLOAT_OPACITY_MIN, Limits.FLOAT_OPACITY_MAX))

    fun setDimLevel(value: Int) = saveInt(DIM_LEVEL, value.coerceIn(DimLimits.LEVEL_MIN, DimLimits.LEVEL_MAX))

    fun setFloatPosition(left: Int, top: Int) = edit {
        putInt(FLOAT_LEFT, left.coerceIn(0, Limits.FLOAT_POS_MAX))
        putInt(FLOAT_TOP, top.coerceIn(0, Limits.FLOAT_POS_MAX))
    }

    fun setFloatFrame(width: Int, height: Int, left: Int, top: Int) = edit {
        putInt(FLOAT_WIDTH, width.coerceIn(Limits.FLOAT_WIDTH_MIN, Limits.FLOAT_WIDTH_MAX))
        putInt(FLOAT_HEIGHT, height.coerceIn(Limits.FLOAT_HEIGHT_MIN, Limits.FLOAT_HEIGHT_MAX))
        putInt(FLOAT_LEFT, left.coerceIn(0, Limits.FLOAT_POS_MAX))
        putInt(FLOAT_TOP, top.coerceIn(0, Limits.FLOAT_POS_MAX))
    }

    fun enterFloat() = edit {
        val width = prefs.getInt(FLOAT_WIDTH, Limits.FLOAT_WIDTH_DEFAULT).coerceIn(Limits.FLOAT_WIDTH_MIN, Limits.FLOAT_WIDTH_MAX)
        putString(MODE, PanelMode.FLOAT.id)
        putInt(FLOAT_LEFT, Limits.centeredLeft(width))
        putInt(FLOAT_TOP, Limits.FLOAT_START_TOP)
    }

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

    fun setArabicStyle(style: ArabicStyle) = edit {
        putString(ARABIC_STYLE, style.id)
        remove(orderKey("ar"))
    }

    fun setDigitsMode(mode: DigitsMode) = edit {
        putBoolean(NUMBER_ROW, mode != DigitsMode.HIDDEN)
        putBoolean(NATIVE_DIGITS, mode == DigitsMode.NATIVE)
    }

    fun setDoubleSpace(value: Boolean) = edit { putBoolean(DOUBLE_SPACE, value) }

    fun setIncognito(value: Boolean) = edit { putBoolean(INCOGNITO, value) }

    fun setSuggestions(value: Boolean) = edit { putBoolean(SUGGESTIONS, value) }

    fun setImageSuggestions(value: Boolean) = edit { putBoolean(IMAGE_SUGGESTIONS, value) }

    fun setEmojiSuggestions(value: Boolean) = edit { putBoolean(EMOJI_SUGGESTIONS, value) }

    fun setAppSuggestions(value: Boolean) = edit { putBoolean(APP_SUGGESTIONS, value) }

    fun setCopyOffers(value: Boolean) = edit { putBoolean(COPY_OFFERS, value) }

    fun setOtpOffers(value: Boolean) = edit { putBoolean(OTP_OFFERS, value) }

    fun setOtpNotifications(value: Boolean) = edit { putBoolean(OTP_NOTIFICATIONS, value) }

    fun otpNotificationsOn(): Boolean = prefs.getBoolean(OTP_NOTIFICATIONS, false)

    fun setRememberLanguage(value: Boolean) = edit { putBoolean(REMEMBER_LANGUAGE, value) }

    fun setTouchAssist(value: Boolean) = edit { putBoolean(TOUCH_ASSIST, value) }

    fun setGlide(value: Boolean) = edit { putBoolean(GLIDE, value) }

    fun setConvertOffers(value: Boolean) = edit { putBoolean(CONVERT_OFFERS, value) }

    fun setSpellFix(value: Boolean) = edit { putBoolean(SPELL_FIX, value) }

    fun setHardwareKeys(value: Boolean) = edit { putBoolean(HARDWARE_KEYS, value) }

    fun setAutoLanguage(value: Boolean) = edit { putBoolean(AUTO_LANGUAGE, value) }

    fun setAutoCapitalize(value: Boolean) = edit { putBoolean(AUTO_CAPITALIZE, value) }

    fun setToolbar(items: List<ToolbarItem>) = edit { putString(TOOLBAR, items.joinToString(",") { it.id }) }

    fun setOrder(layoutId: String, order: List<List<String>>) =
        edit { putString(orderKey(layoutId), order.joinToString(ROW) { it.joinToString(COL) }) }

    fun resetOrder(layoutId: String) = edit {
        remove(orderKey(layoutId))
        KeyFn.clear(this, layoutId)
    }

    fun setKeyFn(layoutId: String, keyId: String, fn: KeyFn?) = KeyFn.write(prefs, layoutId, keyId, fn)

    fun setBar(value: BarArrangement) = edit { putString(BAR, value.encode()) }

    fun resetToolbar() = edit {
        remove(TOOLBAR)
        remove(BAR)
    }

    fun resetSize() = edit {
        remove(HEIGHT)
        if (PanelMode.byId(prefs.getString(MODE, null)) in SIZED_MODES) remove(MODE)
        remove(ONE_HAND)
        remove(WIDTH)
        remove(FLOAT_WIDTH)
        remove(FLOAT_HEIGHT)
        remove(FLOAT_LEFT)
        remove(FLOAT_TOP)
    }

    fun resetAll() = edit {
        listOf(
            KEY_SIZE, HEIGHT, MODE, ONE_HAND, WIDTH, SPLIT_GAP, FLOAT_WIDTH, FLOAT_HEIGHT, FLOAT_OPACITY, FLOAT_LEFT, FLOAT_TOP, FONT, BOLD, ITALIC, LABEL_SCALE, TOOLBAR, BAR,
            VIBRATE, SOUND, VIBRATE_STRENGTH, SOUND_VOLUME, PREVIEW, CLIP_KEEP_OPEN, LANGUAGES, NUMBER_ROW, NATIVE_DIGITS, DOUBLE_SPACE, INCOGNITO, SUGGESTIONS, IMAGE_SUGGESTIONS, EMOJI_SUGGESTIONS, APP_SUGGESTIONS, COPY_OFFERS, OTP_OFFERS, OTP_NOTIFICATIONS, REMEMBER_LANGUAGE,
            TOUCH_ASSIST, GLIDE, CONVERT_OFFERS, SPELL_FIX, HARDWARE_KEYS, AUTO_LANGUAGE, AUTO_CAPITALIZE, ARABIC_STYLE, DIM_LEVEL, "light_on", "light_level", "light_color",
        ).forEach { remove(it) }
        LayoutCatalog.ids.forEach {
            remove(orderKey(it))
            KeyFn.clear(this, it)
        }
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
        val SIZED_MODES = setOf(PanelMode.ONE_HAND_LEFT, PanelMode.ONE_HAND_RIGHT)
        const val NAME = "space_keyboard"
        const val KEY_SIZE = "key_size"
        const val HEIGHT = "height"
        const val MODE = "mode"
        const val ONE_HAND = "one_hand"
        const val WIDTH = "keyboard_width"
        const val SPLIT_GAP = "split_gap"
        const val FLOAT_WIDTH = "float_width"
        const val FLOAT_HEIGHT = "float_height"
        const val FLOAT_OPACITY = "float_opacity"
        const val DIM_LEVEL = "dim_level"
        const val FLOAT_LEFT = "float_left"
        const val FLOAT_TOP = "float_top"
        const val FONT = "label_font"
        const val BOLD = "label_bold"
        const val ITALIC = "label_italic"
        const val LABEL_SCALE = "label_scale"
        const val TOOLBAR = "toolbar"
        const val BAR = "top_bar"
        const val LANGUAGE = "language"
        const val VIBRATE = "feedback_vibrate"
        const val SOUND = "feedback_sound"
        const val VIBRATE_STRENGTH = "feedback_vibrate_strength"
        const val SOUND_VOLUME = "feedback_sound_volume"
        const val PREVIEW = "feedback_preview"
        const val CLIP_KEEP_OPEN = "clip_keep_open"
        const val LANGUAGES = "languages"
        const val ARABIC_STYLE = "arabic_style"
        const val NUMBER_ROW = "number_row"
        const val NATIVE_DIGITS = "native_digits"
        const val DOUBLE_SPACE = "double_space"
        const val INCOGNITO = "incognito"
        const val SUGGESTIONS = "suggestions"
        const val IMAGE_SUGGESTIONS = "image_suggestions"
        const val EMOJI_SUGGESTIONS = "emoji_suggestions"
        const val APP_SUGGESTIONS = "app_suggestions"
        const val COPY_OFFERS = "copy_offers"
        const val OTP_OFFERS = "otp_offers"
        const val OTP_NOTIFICATIONS = "otp_notifications"
        const val REMEMBER_LANGUAGE = "remember_language"
        const val TOUCH_ASSIST = "touch_assist"
        const val GLIDE = "glide_typing"
        const val CONVERT_OFFERS = "convert_offers"
        const val SPELL_FIX = "spell_fix"
        const val HARDWARE_KEYS = "hardware_keys"
        const val AUTO_LANGUAGE = "auto_language"
        const val AUTO_CAPITALIZE = "auto_capitalize"
        const val ROW = ";"
        const val COL = ","
    }
}
