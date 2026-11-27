package com.spacekeyboard.keyboard.core.layout

import com.spacekeyboard.keyboard.core.R
import com.spacekeyboard.keyboard.core.toolbar.BarArrangement

enum class PanelMode(val id: String, val labelRes: Int) {
    NORMAL("normal", R.string.mode_normal),
    ONE_HAND_RIGHT("one_right", R.string.mode_one_right),
    ONE_HAND_LEFT("one_left", R.string.mode_one_left),
    SPLIT("split", R.string.mode_split),
    FLOAT("float", R.string.mode_float);

    fun next(): PanelMode {
        val index = selectable.indexOf(this)
        return if (index < 0) NORMAL else selectable[(index + 1) % selectable.size]
    }

    companion object {
        val selectable: List<PanelMode> = entries.filter { it != FLOAT }

        fun byId(id: String?): PanelMode = PanelMode.entries.firstOrNull { it.id == id } ?: NORMAL
    }
}

enum class LabelFont(val id: String, val labelRes: Int) {
    PLEX("plex", R.string.font_plex),
    SANS("sans", R.string.font_sans),
    SERIF("serif", R.string.font_serif),
    MONO("mono", R.string.font_mono),
    CURSIVE("cursive", R.string.font_cursive),
    CASUAL("casual", R.string.font_casual);

    companion object {
        fun byId(id: String?): LabelFont = LabelFont.entries.firstOrNull { it.id == id } ?: PLEX
    }
}

enum class ToolbarItem(val id: String, val labelRes: Int) {
    THEME("theme", R.string.toolbar_theme),
    MODE("mode", R.string.toolbar_mode),
    SYMBOLS("symbols", R.string.toolbar_symbols),
    EMOJI("emoji", R.string.toolbar_emoji),
    DECORATE("decorate", R.string.toolbar_decorate),
    CLIPBOARD("clipboard", R.string.toolbar_clipboard),
    TRANSLATE("translate", R.string.toolbar_translate),
    IMAGES("images", R.string.toolbar_images),
    EDIT("edit", R.string.toolbar_edit),
    RESIZE("resize", R.string.toolbar_resize),
    INCOGNITO("incognito", R.string.toolbar_incognito),
    SETTINGS("settings", R.string.toolbar_settings);

    companion object {
        fun parse(raw: String?): List<ToolbarItem> {
            val saved = raw.orEmpty().split(',').mapNotNull { id -> ToolbarItem.entries.firstOrNull { it.id == id } }.distinct()
            return saved + ToolbarItem.entries.filter { it !in saved }
        }
    }
}

data class LabelStyle(
    val font: LabelFont = LabelFont.PLEX,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val scalePercent: Int = Limits.LABEL_DEFAULT,
)

object Limits {
    const val KEY_MIN = 70
    const val KEY_MAX = 130
    const val HEIGHT_MIN = 50
    const val HEIGHT_MAX = 160
    const val ONE_HAND_MIN = 50
    const val ONE_HAND_MAX = 90
    const val WIDTH_MIN = 50
    const val WIDTH_MAX = 100
    const val SPLIT_MIN = 8
    const val SPLIT_MAX = 30
    const val FLOAT_WIDTH_MIN = 60
    const val FLOAT_WIDTH_MAX = 100
    const val FLOAT_WIDTH_DEFAULT = 85
    const val FLOAT_HEIGHT_MIN = 50
    const val FLOAT_HEIGHT_MAX = 130
    const val FLOAT_HEIGHT_DEFAULT = 80
    const val FLOAT_OPACITY_MIN = 30
    const val FLOAT_OPACITY_MAX = 100
    const val FLOAT_POS_MAX = 1000
    const val FLOAT_START_TOP = 300
    const val FLOAT_BAR_DP = 40
    const val LABEL_MIN = 80
    const val LABEL_MAX = 150
    const val LABEL_DEFAULT = 100
    const val GAP_BASE_DP = 4f
    const val GAP_STEP_DP = 0.2f
    const val GAP_MIN_DP = 0.5f
    const val GAP_MAX_DP = 12f
    const val STRENGTH_MIN = 10
    const val STRENGTH_MAX = 100
    const val STRENGTH_DEFAULT = 50
    const val VOLUME_MIN = 10
    const val VOLUME_MAX = 100
    const val VOLUME_DEFAULT = 100
    const val ROW_DP = 60f
    const val MAX_KEYS_RATIO = 0.45f
    const val MIN_ROW_DP = 34f
    const val BOTTOM_SAFE_DP = 8f
    const val EDGE_SAFE_DP = 12f

    fun centeredLeft(widthPercent: Int): Int = (100 - widthPercent) * FLOAT_POS_MAX / 200
}

data class FeedbackPrefs(
    val vibrate: Boolean = true,
    val sound: Boolean = false,
    val vibrateStrength: Int = Limits.STRENGTH_DEFAULT,
    val soundVolume: Int = Limits.VOLUME_DEFAULT,
    val preview: Boolean = false,
    val clipKeepOpen: Boolean = false,
)

enum class ArabicStyle(val id: String, val labelRes: Int, val rawRes: Int) {
    ELEVEN("eleven", R.string.arabic_style_eleven, R.raw.layout_ar),
    TEN("ten", R.string.arabic_style_ten, R.raw.layout_ar_ten),
    IOS("ios", R.string.arabic_style_ios, R.raw.layout_ar_ios),
    PAIRED("paired", R.string.arabic_style_paired, R.raw.layout_ar_pair),
    PLAIN("plain", R.string.arabic_style_plain, R.raw.layout_ar_plain);

    companion object {
        fun byId(id: String?): ArabicStyle = entries.firstOrNull { it.id == id } ?: ELEVEN
    }
}

enum class DigitsMode(val id: String, val labelRes: Int) {
    LATIN("latin", R.string.digits_latin),
    NATIVE("native", R.string.digits_native),
    HIDDEN("hidden", R.string.digits_hidden),
}

data class TypingPrefs(
    val numberRow: Boolean = true,
    val nativeDigits: Boolean = false,
    val doubleSpace: Boolean = true,
    val incognito: Boolean = false,
    val suggestions: Boolean = true,
    val imageSuggestions: Boolean = true,
    val emojiSuggestions: Boolean = true,
    val appSuggestions: Boolean = true,
    val copyOffers: Boolean = true,
    val otpOffers: Boolean = true,
    val otpNotifications: Boolean = false,
    val rememberLanguage: Boolean = true,
    val touchAssist: Boolean = true,
    val glide: Boolean = true,
    val convertOffers: Boolean = true,
    val spellFix: Boolean = true,
    val hardwareKeys: Boolean = true,
    val autoLanguage: Boolean = true,
    val autoCapitalize: Boolean = true,
) {
    val digits: DigitsMode get() = when {
        !numberRow -> DigitsMode.HIDDEN
        nativeDigits -> DigitsMode.NATIVE
        else -> DigitsMode.LATIN
    }
}

data class KeyboardPrefs(
    val keySizePercent: Int = 100,
    val heightPercent: Int = 100,
    val mode: PanelMode = PanelMode.NORMAL,
    val oneHandPercent: Int = 80,
    val splitGapPercent: Int = 16,
    val floatWidthPercent: Int = Limits.FLOAT_WIDTH_DEFAULT,
    val floatHeightPercent: Int = Limits.FLOAT_HEIGHT_DEFAULT,
    val floatOpacityPercent: Int = Limits.FLOAT_OPACITY_MAX,
    val floatLeft: Int = Limits.centeredLeft(Limits.FLOAT_WIDTH_DEFAULT),
    val floatTop: Int = Limits.FLOAT_START_TOP,
    val label: LabelStyle = LabelStyle(),
    val orders: Map<String, List<List<String>>> = emptyMap(),
    val toolbar: List<ToolbarItem> = ToolbarItem.entries,
    val language: String = LayoutCatalog.default,
    val feedback: FeedbackPrefs = FeedbackPrefs(),
    val languages: List<String> = LayoutCatalog.defaultLanguages,
    val typing: TypingPrefs = TypingPrefs(),
    val arabicStyle: ArabicStyle = ArabicStyle.ELEVEN,
    val widthPercent: Int = Limits.WIDTH_MAX,
    val dim: DimPrefs = DimPrefs(),
    val barLayout: String? = null,
    val keyFns: Map<String, Map<String, String>> = emptyMap(),
) {
    fun bar(): BarArrangement = BarArrangement.resolve(barLayout, languages)

    fun gapDp(): Float =
        (Limits.GAP_BASE_DP - (keySizePercent - 100) * Limits.GAP_STEP_DP).coerceIn(Limits.GAP_MIN_DP, Limits.GAP_MAX_DP)

    fun bottomSafePx(density: Float): Float = Limits.BOTTOM_SAFE_DP * density

    fun shownHeightPercent(): Int = if (mode == PanelMode.FLOAT) floatHeightPercent else heightPercent

    fun rowDp(): Float = Limits.ROW_DP * shownHeightPercent() / 100f

    fun rowPx(rows: Int, density: Float, screenPx: Int): Float {
        val capped = screenPx * Limits.MAX_KEYS_RATIO / rows.coerceAtLeast(1)
        return minOf(rowDp() * density, maxOf(capped, Limits.MIN_ROW_DP * density))
    }

    fun maxHeightPercent(rows: Int, density: Float, screenPx: Int): Int {
        val capped = maxOf(screenPx * Limits.MAX_KEYS_RATIO / rows.coerceAtLeast(1), Limits.MIN_ROW_DP * density)
        return (capped / (Limits.ROW_DP * density) * 100f).toInt().coerceIn(Limits.HEIGHT_MIN, Limits.HEIGHT_MAX)
    }

    fun labelScale(): Float = label.scalePercent / 100f

    fun sanitized(): KeyboardPrefs {
        val enabled = languages.filter { it in LayoutCatalog.ids }.distinct().ifEmpty { LayoutCatalog.defaultLanguages }
        return clamped().copy(
            languages = enabled,
            language = if (language in enabled) language else enabled.first(),
        )
    }

    private fun clamped(): KeyboardPrefs = copy(
        keySizePercent = keySizePercent.coerceIn(Limits.KEY_MIN, Limits.KEY_MAX),
        heightPercent = heightPercent.coerceIn(Limits.HEIGHT_MIN, Limits.HEIGHT_MAX),
        oneHandPercent = oneHandPercent.coerceIn(Limits.ONE_HAND_MIN, Limits.ONE_HAND_MAX),
        widthPercent = widthPercent.coerceIn(Limits.WIDTH_MIN, Limits.WIDTH_MAX),
        splitGapPercent = splitGapPercent.coerceIn(Limits.SPLIT_MIN, Limits.SPLIT_MAX),
        floatWidthPercent = floatWidthPercent.coerceIn(Limits.FLOAT_WIDTH_MIN, Limits.FLOAT_WIDTH_MAX),
        floatHeightPercent = floatHeightPercent.coerceIn(Limits.FLOAT_HEIGHT_MIN, Limits.FLOAT_HEIGHT_MAX),
        floatOpacityPercent = floatOpacityPercent.coerceIn(Limits.FLOAT_OPACITY_MIN, Limits.FLOAT_OPACITY_MAX),
        floatLeft = floatLeft.coerceIn(0, Limits.FLOAT_POS_MAX),
        floatTop = floatTop.coerceIn(0, Limits.FLOAT_POS_MAX),
        dim = dim.sanitized(),
        label = label.copy(scalePercent = label.scalePercent.coerceIn(Limits.LABEL_MIN, Limits.LABEL_MAX)),
        feedback = feedback.copy(
            vibrateStrength = feedback.vibrateStrength.coerceIn(Limits.STRENGTH_MIN, Limits.STRENGTH_MAX),
            soundVolume = feedback.soundVolume.coerceIn(Limits.VOLUME_MIN, Limits.VOLUME_MAX),
        ),
    )
}
