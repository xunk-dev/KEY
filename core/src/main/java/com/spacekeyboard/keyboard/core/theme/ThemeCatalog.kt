package com.spacekeyboard.keyboard.core.theme

import com.spacekeyboard.keyboard.core.R

enum class BackdropKind { NEBULA, GALAXY, BLACK_HOLE, AURORA, ECLIPSE, NEON_GRID, FOG, BLOOD_MOON, ICE, GOLD_DUST, NONE }

data class ThemeDef(val id: String, val nameRes: Int, val backdrop: BackdropKind, val colors: ThemeColors, val light: Boolean = false)

object ThemeCatalog {
    val all: List<ThemeDef> = listOf(
        ThemeDef("nebula", R.string.theme_nebula, BackdropKind.NEBULA, ThemeColors(0xFF0B0716.toInt(), 0xFF2B2048.toInt(), 0xFFB26BFF.toInt(), 0xFF7A5BC9.toInt())),
        ThemeDef("galaxy", R.string.theme_galaxy, BackdropKind.GALAXY, ThemeColors(0xFF060B1F.toInt(), 0xFF18264F.toInt(), 0xFF5CA8FF.toInt(), 0xFF4A6FD0.toInt())),
        ThemeDef("blackhole", R.string.theme_blackhole, BackdropKind.BLACK_HOLE, ThemeColors(0xFF020202.toInt(), 0xFF261F19.toInt(), 0xFFFF8A2B.toInt(), 0xFF8A5A2B.toInt())),
        ThemeDef("aurora", R.string.theme_aurora, BackdropKind.AURORA, ThemeColors(0xFF04141A.toInt(), 0xFF0F2E33.toInt(), 0xFF4DFFB5.toInt(), 0xFF2FA37E.toInt())),
        ThemeDef("eclipse", R.string.theme_eclipse, BackdropKind.ECLIPSE, ThemeColors(0xFF000000.toInt(), 0xFF1E1E22.toInt(), 0xFFFFD27A.toInt(), 0xFF8C7A52.toInt())),
        ThemeDef("neon", R.string.theme_neon, BackdropKind.NEON_GRID, ThemeColors(0xFF08000F.toInt(), 0xFF2D1547.toInt(), 0xFFFF2BD6.toInt(), 0xFF00E5FF.toInt())),
        ThemeDef("fog", R.string.theme_fog, BackdropKind.FOG, ThemeColors(0xFF10151A.toInt(), 0xFF232C34.toInt(), 0xFF9FB4C8.toInt(), 0xFF66788A.toInt())),
        ThemeDef("bloodmoon", R.string.theme_bloodmoon, BackdropKind.BLOOD_MOON, ThemeColors(0xFF120405.toInt(), 0xFF42171C.toInt(), 0xFFFF3B3B.toInt(), 0xFF9C3A3A.toInt())),
        ThemeDef("icy", R.string.theme_icy, BackdropKind.ICE, ThemeColors(0xFF06161F.toInt(), 0xFF133544.toInt(), 0xFF9BE7FF.toInt(), 0xFF5BB6D6.toInt())),
        ThemeDef("gold", R.string.theme_gold, BackdropKind.GOLD_DUST, ThemeColors(0xFF0D0A04.toInt(), 0xFF332714.toInt(), 0xFFFFC94D.toInt(), 0xFF9A7A2E.toInt())),
        ThemeDef("daylight", R.string.theme_daylight, BackdropKind.FOG, ThemeColors(0xFFDFE5F0.toInt(), 0xFFFFFFFF.toInt(), 0xFF1F5FE0.toInt(), 0xFF8795B3.toInt()), light = true),
        ThemeDef("paper", R.string.theme_paper, BackdropKind.NONE, ThemeColors(0xFFFBF5E9.toInt(), 0xFFE6D8BC.toInt(), 0xFFB4541B.toInt(), 0xFF9C8660.toInt()), light = true),
    )

    val default: ThemeDef = all.first()

    val defaultLight: ThemeDef = all.first { it.light }

    fun byId(id: String?): ThemeDef = all.firstOrNull { it.id == id } ?: default

    fun next(id: String, sameKind: Boolean = false): ThemeDef {
        val light = byId(id).light
        val pool = if (sameKind) all.filter { it.light == light } else all
        return pool[(pool.indexOfFirst { it.id == id } + 1).mod(pool.size)]
    }
}
