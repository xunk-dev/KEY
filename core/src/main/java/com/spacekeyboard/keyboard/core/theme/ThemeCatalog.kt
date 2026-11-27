package com.spacekeyboard.keyboard.core.theme

import com.spacekeyboard.keyboard.core.R

enum class BackdropKind { NONE }

data class ThemeDef(val id: String, val nameRes: Int, val backdrop: BackdropKind, val colors: ThemeColors, val light: Boolean = false)

object ThemeCatalog {
    val all: List<ThemeDef> = listOf(
        ThemeDef("glass", R.string.theme_glass, BackdropKind.NONE, ThemeColors(0xFF090A0E.toInt(), 0xFF23272F.toInt(), 0xFF8FB8FF.toInt(), 0xFF404759.toInt())),
        ThemeDef("eclipse", R.string.theme_eclipse, BackdropKind.NONE, ThemeColors(0xFF000000.toInt(), 0xFF1E1E22.toInt(), 0xFFFFD27A.toInt(), 0xFF8C7A52.toInt())),
        ThemeDef("daylight", R.string.theme_daylight, BackdropKind.NONE, ThemeColors(0xFFDFE5F0.toInt(), 0xFFFFFFFF.toInt(), 0xFF1F5FE0.toInt(), 0xFF8795B3.toInt()), light = true),
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
