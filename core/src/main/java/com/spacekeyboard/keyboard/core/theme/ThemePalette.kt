package com.spacekeyboard.keyboard.core.theme

data class ThemePalette(
    val background: Int,
    val keyFill: Int,
    val keyPressed: Int,
    val keyText: Int,
    val keyTextPressed: Int,
    val glow: Int,
    val border: Int,
    val label: Int,
    val adjusted: Boolean,
)

object ThemeResolver {
    const val KEY_SEPARATION = 1.25
    const val DECOR = 2.0
    private const val PRESS_BLEND = 0.6f
    private val lightText = 0xFFF4F6FF.toInt()
    private val darkText = 0xFF10131A.toInt()

    fun resolve(requested: ThemeColors): ThemePalette {
        val background = ColorMath.opaque(requested.background)
        val keys = ColorMath.ensureContrast(requested.keys, background, KEY_SEPARATION)
        val glow = ColorMath.ensureContrast(requested.glow, background, DECOR)
        val border = ColorMath.ensureContrast(requested.border, background, DECOR)
        val pressed = ColorMath.mix(keys, glow, PRESS_BLEND)
        val text = listOf(lightText, darkText)
        return ThemePalette(
            background = background,
            keyFill = keys,
            keyPressed = pressed,
            keyText = ColorMath.readableOn(keys, text),
            keyTextPressed = ColorMath.readableOn(pressed, text),
            glow = glow,
            border = border,
            label = ColorMath.readableOn(background, text),
            adjusted = keys != ColorMath.opaque(requested.keys) ||
                glow != ColorMath.opaque(requested.glow) ||
                border != ColorMath.opaque(requested.border),
        )
    }
}
