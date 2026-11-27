package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette

internal object GlassSkins {
    private const val RADIUS_DP = 11f
    private const val WIDE_RADIUS_DP = 14f
    private const val NONE = 0
    private const val DROP = 0x80000000.toInt()
    private const val LIGHT_DROP = 0x3A000000
    private const val LIGHT_BACKGROUND = 0.4
    private const val ENTER_INK = 0xFF0A1330.toInt()
    private const val LIGHT_INK = 0xFFF4F6FF.toInt()
    private const val LIFT = 0.035f
    private const val SINK = 0.17f
    private const val FUNCTION_DEPTH = 0.2f
    private const val QUIET_DEPTH = 0.45f
    private const val ENTER_SINK = 0.18f
    private const val PRESS_BLEND = 0.3f
    private const val PRESS_RING = 0.6f

    fun of(role: KeyRole, p: ThemePalette, lit: Boolean): KeySkin {
        if (lit) return pressed(role, p)
        val drop = if (ColorMath.luminance(p.background) > LIGHT_BACKGROUND) LIGHT_DROP else DROP
        return when (role) {
            KeyRole.LETTER -> raised(p.keyFill, p.keyText, p.glow, RADIUS_DP, drop)
            KeyRole.FUNCTION -> {
                val base = ColorMath.mix(p.keyFill, p.background, FUNCTION_DEPTH)
                raised(base, softInk(base, p), p.glow, RADIUS_DP, drop)
            }
            KeyRole.CHIP -> {
                val base = ColorMath.mix(p.keyFill, p.background, QUIET_DEPTH)
                val ink = ColorMath.readableOn(surface(base).to, listOf(p.glow, p.keyText))
                raised(base, ink, ink, RADIUS_DP, NONE)
            }
            KeyRole.QUIET -> {
                val base = ColorMath.mix(p.keyFill, p.background, QUIET_DEPTH)
                raised(base, softInk(base, p), p.glow, WIDE_RADIUS_DP, drop)
            }
            KeyRole.PRIMARY -> primary(p)
        }
    }

    fun dim(p: ThemePalette): Int = ColorMath.mix(ColorMath.mix(p.keyText, p.keyFill, 0.45f), p.glow, 0.15f)

    private fun softInk(base: Int, p: ThemePalette): Int =
        ColorMath.readableOn(surface(base).to, listOf(ColorMath.mix(dim(p), p.keyText, 0.2f), p.keyText))

    private fun surface(base: Int) = KeyGradient(
        ColorMath.mix(base, ColorMath.WHITE, LIFT),
        ColorMath.mix(base, ColorMath.BLACK, SINK),
        false,
    )

    private fun raised(base: Int, ink: Int, accent: Int, radius: Float, shadow: Int): KeySkin {
        val gradient = surface(base)
        return KeySkin(gradient.to, NONE, shadow, ink, radius, false, false, accent, gradient)
    }

    private fun primary(p: ThemePalette): KeySkin {
        val end = ColorMath.mix(p.glow, ColorMath.BLACK, ENTER_SINK)
        val ink = ColorMath.readableOn(end, listOf(ENTER_INK, LIGHT_INK))
        return KeySkin(end, NONE, NONE, ink, WIDE_RADIUS_DP, false, false, ink, KeyGradient(p.glow, end, true))
    }

    private fun pressed(role: KeyRole, p: ThemePalette): KeySkin {
        val fill = ColorMath.mix(p.keyFill, p.glow, PRESS_BLEND)
        val ink = ColorMath.readableOn(fill, listOf(p.keyText, LIGHT_INK, ENTER_INK))
        val radius = if (role == KeyRole.QUIET || role == KeyRole.PRIMARY) WIDE_RADIUS_DP else RADIUS_DP
        return KeySkin(fill, ColorMath.withAlpha(p.glow, PRESS_RING), NONE, ink, radius, false, false, ink)
    }
}
