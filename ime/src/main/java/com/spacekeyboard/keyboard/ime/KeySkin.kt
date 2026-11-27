package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.core.theme.ThemePalette

internal enum class KeyRole { LETTER, FUNCTION, PRIMARY, QUIET, CHIP }

internal class KeyGradient(val from: Int, val to: Int, val diagonal: Boolean)

internal class KeySkin(
    val fill: Int,
    val border: Int,
    val shadow: Int,
    val ink: Int,
    val radiusDp: Float,
    val pill: Boolean,
    val halo: Boolean,
    val accent: Int = ink,
    val gradient: KeyGradient? = null,
)

internal fun KeySkin.tinted(accent: Int) = KeySkin(fill, border, shadow, ink, radiusDp, pill, halo, accent, gradient)

internal fun KeySkin.lifted(toward: Int, amount: Float): KeySkin {
    fun mixed(c: Int) = (c and 0xFF000000.toInt()) or (ColorMath.mix(c, toward, amount) and 0x00FFFFFF)
    return KeySkin(mixed(fill), border, shadow, ink, radiusDp, pill, halo, accent, gradient?.let { KeyGradient(mixed(it.from), mixed(it.to), it.diagonal) })
}

internal fun keyRole(action: KeyAction): KeyRole = when (action) {
    KeyAction.ENTER -> KeyRole.PRIMARY
    KeyAction.SPACE -> KeyRole.QUIET
    KeyAction.COMMIT, KeyAction.SPACER -> KeyRole.LETTER
    KeyAction.LANGUAGE, KeyAction.NUMBERS, KeyAction.SYMBOLS, KeyAction.OPEN_SYMBOLS, KeyAction.RAIL -> KeyRole.CHIP
    KeyAction.EMOJI, KeyAction.CLIPBOARD, KeyAction.TRANSLATE -> KeyRole.CHIP
    else -> KeyRole.FUNCTION
}

internal fun fade(argb: Int, alpha: Float): Int {
    val value = ((argb ushr 24) * alpha).toInt().coerceIn(0, 255)
    return (value shl 24) or (argb and 0x00FFFFFF)
}

internal object KeySkins {
    private const val LIGHT_INK = 0xFFF4F6FF.toInt()
    private const val DARK_INK = 0xFF10131A.toInt()
    private const val NONE = 0
    private const val RADIUS_DP = 14f
    private const val ACCENT_CONTRAST = 3.0

    fun of(style: KeyStyle, role: KeyRole, palette: ThemePalette, lit: Boolean): KeySkin = when (style) {
        KeyStyle.GLASS -> GlassSkins.of(role, palette, lit)
        KeyStyle.GALAXY -> accented(galaxy(role, palette, lit), role, palette, lit)
        KeyStyle.DAY -> accented(day(role, palette, lit), role, palette, lit)
        KeyStyle.ECLIPSE -> accented(eclipse(role, palette, lit), role, palette, lit)
    }

    private fun accented(skin: KeySkin, role: KeyRole, p: ThemePalette, lit: Boolean): KeySkin {
        if (lit || role == KeyRole.PRIMARY || ColorMath.contrast(p.glow, skin.fill) < ACCENT_CONTRAST) return skin
        return skin.tinted(p.glow)
    }

    private fun ink(fill: Int, vararg wanted: Int): Int =
        ColorMath.readableOn(fill, wanted.toList() + listOf(DARK_INK, LIGHT_INK))

    private fun galaxy(role: KeyRole, p: ThemePalette, lit: Boolean): KeySkin {
        val ring = ColorMath.withAlpha(p.glow, 0.7f)
        if (lit) return KeySkin(p.keyPressed, ring, NONE, p.keyTextPressed, RADIUS_DP, false, false)
        return when (role) {
            KeyRole.LETTER -> KeySkin(ColorMath.withAlpha(p.keyFill, 0.94f), NONE, NONE, p.keyText, RADIUS_DP, false, false)
            KeyRole.FUNCTION -> {
                val fill = ColorMath.mix(p.keyFill, p.glow, 0.2f)
                KeySkin(fill, NONE, NONE, ink(fill, p.keyText), RADIUS_DP, false, false)
            }
            KeyRole.PRIMARY -> KeySkin(p.glow, NONE, NONE, ink(p.glow), RADIUS_DP, true, false)
            KeyRole.QUIET -> {
                val fill = ColorMath.mix(p.background, p.keyFill, 0.7f)
                KeySkin(fill, ColorMath.withAlpha(p.border, 0.45f), NONE, p.keyText, RADIUS_DP, true, false)
            }
            KeyRole.CHIP -> {
                val fill = ColorMath.mix(p.background, p.keyFill, 0.5f)
                KeySkin(fill, ColorMath.withAlpha(p.glow, 0.42f), NONE, ink(fill, p.glow, p.keyText), RADIUS_DP, false, false)
            }
        }
    }

    private fun day(role: KeyRole, p: ThemePalette, lit: Boolean): KeySkin {
        val edge = ColorMath.withAlpha(p.border, 0.6f)
        val pill = role == KeyRole.PRIMARY || role == KeyRole.QUIET
        if (lit) return KeySkin(p.keyPressed, NONE, edge, p.keyTextPressed, RADIUS_DP, pill, false)
        return when (role) {
            KeyRole.LETTER, KeyRole.QUIET -> KeySkin(p.keyFill, NONE, edge, p.keyText, RADIUS_DP, pill, false)
            KeyRole.FUNCTION -> {
                val fill = ColorMath.mix(p.keyFill, p.border, 0.45f)
                KeySkin(fill, NONE, ColorMath.withAlpha(p.border, 0.8f), ink(fill, p.keyText), RADIUS_DP, false, false)
            }
            KeyRole.PRIMARY -> KeySkin(p.glow, NONE, ColorMath.mix(p.glow, ColorMath.BLACK, 0.3f), ink(p.glow, LIGHT_INK), RADIUS_DP, true, false)
            KeyRole.CHIP -> KeySkin(p.keyFill, ColorMath.withAlpha(p.glow, 0.5f), edge, ink(p.keyFill, p.glow, p.keyText), RADIUS_DP, false, false)
        }
    }

    private fun eclipse(role: KeyRole, p: ThemePalette, lit: Boolean): KeySkin {
        if (lit) {
            val fill = ColorMath.mix(p.keyFill, p.glow, 0.25f)
            return KeySkin(fill, NONE, NONE, ink(fill, p.glow, p.keyTextPressed), 10f, false, false)
        }
        return when (role) {
            KeyRole.LETTER, KeyRole.QUIET -> KeySkin(p.keyFill, NONE, NONE, p.keyText, 10f, false, false)
            KeyRole.FUNCTION -> {
                val fill = ColorMath.mix(p.background, p.keyFill, 0.6f)
                KeySkin(fill, ColorMath.mix(p.background, p.border, 0.35f), NONE, ink(fill, p.glow, p.keyText), 10f, false, false)
            }
            KeyRole.PRIMARY -> KeySkin(p.glow, NONE, NONE, ink(p.glow), 10f, false, false)
            KeyRole.CHIP -> {
                val fill = ColorMath.mix(p.background, p.keyFill, 0.6f)
                KeySkin(fill, ColorMath.mix(p.background, p.glow, 0.45f), NONE, ink(fill, p.glow, p.keyText), 10f, false, false)
            }
        }
    }
}
