package com.spacekeyboard.keyboard.core.layout

import com.spacekeyboard.keyboard.core.R

enum class PanelMode(val id: String, val labelRes: Int) {
    NORMAL("normal", R.string.mode_normal),
    ONE_HAND_RIGHT("one_right", R.string.mode_one_right),
    ONE_HAND_LEFT("one_left", R.string.mode_one_left),
    SPLIT("split", R.string.mode_split);

    fun next(): PanelMode = PanelMode.entries[(ordinal + 1) % PanelMode.entries.size]

    companion object {
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
    VOICE("voice", R.string.toolbar_voice),
    EDIT("edit", R.string.toolbar_edit),
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
    const val HEIGHT_MIN = 70
    const val HEIGHT_MAX = 160
    const val ONE_HAND_MIN = 65
    const val ONE_HAND_MAX = 90
    const val SPLIT_MIN = 8
    const val SPLIT_MAX = 30
    const val LABEL_MIN = 80
    const val LABEL_MAX = 150
    const val LABEL_DEFAULT = 100
    const val GAP_BASE_DP = 6f
    const val GAP_STEP_DP = 0.2f
    const val GAP_MIN_DP = 0.5f
    const val GAP_MAX_DP = 12f
    const val STRENGTH_MIN = 10
    const val STRENGTH_MAX = 100
    const val STRENGTH_DEFAULT = 50
    const val VOLUME_MIN = 10
    const val VOLUME_MAX = 100
    const val VOLUME_DEFAULT = 100
    const val ROW_DP = 50f
    const val MAX_KEYS_RATIO = 0.45f
    const val MIN_ROW_DP = 34f
}

data class FeedbackPrefs(
    val vibrate: Boolean = true,
    val sound: Boolean = false,
    val vibrateStrength: Int = Limits.STRENGTH_DEFAULT,
    val soundVolume: Int = Limits.VOLUME_DEFAULT,
    val preview: Boolean = true,
    val clipKeepOpen: Boolean = false,
)

data class TypingPrefs(
    val numberRow: Boolean = false,
    val nativeDigits: Boolean = false,
    val doubleSpace: Boolean = true,
    val incognito: Boolean = false,
    val suggestions: Boolean = true,
    val rememberLanguage: Boolean = true,
    val touchAssist: Boolean = true,
    val hardwareKeys: Boolean = true,
    val autoLanguage: Boolean = true,
)

data class KeyboardPrefs(
    val keySizePercent: Int = 100,
    val heightPercent: Int = 100,
    val mode: PanelMode = PanelMode.NORMAL,
    val oneHandPercent: Int = 80,
    val splitGapPercent: Int = 16,
    val label: LabelStyle = LabelStyle(),
    val orders: Map<String, List<List<String>>> = emptyMap(),
    val toolbar: List<ToolbarItem> = ToolbarItem.entries,
    val language: String = LayoutCatalog.default,
    val feedback: FeedbackPrefs = FeedbackPrefs(),
    val languages: List<String> = LayoutCatalog.defaultLanguages,
    val typing: TypingPrefs = TypingPrefs(),
) {
    fun gapDp(): Float =
        (Limits.GAP_BASE_DP - (keySizePercent - 100) * Limits.GAP_STEP_DP).coerceIn(Limits.GAP_MIN_DP, Limits.GAP_MAX_DP)

    fun rowDp(): Float = Limits.ROW_DP * heightPercent / 100f

    fun rowPx(rows: Int, density: Float, screenPx: Int): Float {
        val capped = screenPx * Limits.MAX_KEYS_RATIO / rows.coerceAtLeast(1)
        return minOf(rowDp() * density, maxOf(capped, Limits.MIN_ROW_DP * density))
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
        splitGapPercent = splitGapPercent.coerceIn(Limits.SPLIT_MIN, Limits.SPLIT_MAX),
        label = label.copy(scalePercent = label.scalePercent.coerceIn(Limits.LABEL_MIN, Limits.LABEL_MAX)),
        feedback = feedback.copy(
            vibrateStrength = feedback.vibrateStrength.coerceIn(Limits.STRENGTH_MIN, Limits.STRENGTH_MAX),
            soundVolume = feedback.soundVolume.coerceIn(Limits.VOLUME_MIN, Limits.VOLUME_MAX),
        ),
    )
}
