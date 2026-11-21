package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.core.theme.ThemePalette

internal enum class KeyRole { LETTER, FUNCTION, PRIMARY, QUIET }

internal class KeySkin(
    val fill: Int,
    val border: Int,
    val shadow: Int,
    val ink: Int,
    val radiusDp: Float,
    val pill: Boolean,
    val halo: Boolean,
)

internal fun keyRole(action: KeyAction): KeyRole = when (action) {
    KeyAction.ENTER -> KeyRole.PRIMARY
    KeyAction.SPACE -> KeyRole.QUIET
    KeyAction.COMMIT, KeyAction.SPACER -> KeyRole.LETTER
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

    fun of(style: KeyStyle, role: KeyRole, palette: ThemePalette, lit: Boolean): KeySkin = when (style) {
        KeyStyle.GALAXY -> galaxy(role, palette, lit)
        KeyStyle.DAY -> day(role, palette, lit)
        KeyStyle.ECLIPSE -> eclipse(role, palette, lit)
    }

    private fun ink(fill: Int, vararg wanted: Int): Int =
        ColorMath.readableOn(fill, wanted.toList() + listOf(DARK_INK, LIGHT_INK))

    private fun galaxy(role: KeyRole, p: ThemePalette, lit: Boolean): KeySkin {
        if (lit) return KeySkin(p.keyPressed, ColorMath.withAlpha(p.glow, 0.85f), NONE, p.keyTextPressed, 12f, false, true)
        return when (role) {
            KeyRole.LETTER -> KeySkin(ColorMath.withAlpha(p.keyFill, 0.92f), ColorMath.withAlpha(p.border, 0.85f), NONE, p.keyText, 12f, false, false)
            KeyRole.FUNCTION -> {
                val fill = ColorMath.mix(p.background, p.glow, 0.16f)
                KeySkin(fill, ColorMath.withAlpha(p.glow, 0.4f), NONE, ink(fill, p.keyText), 12f, false, false)
            }
            KeyRole.PRIMARY -> KeySkin(p.glow, ColorMath.mix(p.glow, ColorMath.WHITE, 0.35f), NONE, ink(p.glow), 12f, false, true)
            KeyRole.QUIET -> {
                val fill = ColorMath.mix(p.background, p.keyFill, 0.6f)
                KeySkin(fill, ColorMath.withAlpha(p.border, 0.5f), NONE, p.keyText, 12f, false, false)
            }
        }
    }

    private fun day(role: KeyRole, p: ThemePalette, lit: Boolean): KeySkin {
        val edge = ColorMath.withAlpha(p.border, 0.6f)
        val pill = role == KeyRole.PRIMARY || role == KeyRole.QUIET
        if (lit) return KeySkin(p.keyPressed, NONE, edge, p.keyTextPressed, 13f, pill, false)
        return when (role) {
            KeyRole.LETTER, KeyRole.QUIET -> KeySkin(p.keyFill, NONE, edge, p.keyText, 13f, pill, false)
            KeyRole.FUNCTION -> {
                val fill = ColorMath.mix(p.keyFill, p.border, 0.45f)
                KeySkin(fill, NONE, ColorMath.withAlpha(p.border, 0.8f), ink(fill, p.keyText), 13f, false, false)
            }
            KeyRole.PRIMARY -> KeySkin(p.glow, NONE, ColorMath.mix(p.glow, ColorMath.BLACK, 0.3f), ink(p.glow, LIGHT_INK), 13f, true, false)
        }
    }

    private fun eclipse(role: KeyRole, p: ThemePalette, lit: Boolean): KeySkin {
        if (lit) {
            val fill = ColorMath.mix(p.keyFill, p.glow, 0.25f)
            return KeySkin(fill, NONE, NONE, ink(fill, p.glow, p.keyTextPressed), 9f, false, false)
        }
        return when (role) {
            KeyRole.LETTER, KeyRole.QUIET -> KeySkin(p.keyFill, NONE, NONE, p.keyText, 9f, false, false)
            KeyRole.FUNCTION -> {
                val fill = ColorMath.mix(p.background, p.keyFill, 0.6f)
                KeySkin(fill, ColorMath.mix(p.background, p.border, 0.35f), NONE, ink(fill, p.glow, p.keyText), 9f, false, false)
            }
            KeyRole.PRIMARY -> KeySkin(p.glow, NONE, NONE, ink(p.glow), 9f, false, false)
        }
    }
}
