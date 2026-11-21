package com.spacekeyboard.keyboard.core.theme

data class ThemeChoice(
    val themeId: String = ThemeCatalog.default.id,
    val overrides: Map<String, Map<ThemeSlot, Int>> = emptyMap(),
    val animated: Boolean = true,
    val followSystem: Boolean = false,
    val night: Boolean = true,
) {
    val effectiveId: String get() {
        val picked = ThemeCatalog.byId(themeId)
        return when {
            !followSystem || picked.light != night -> picked.id
            night -> ThemeCatalog.default.id
            else -> ThemeCatalog.defaultLight.id
        }
    }

    val theme: ThemeDef get() = ThemeCatalog.byId(effectiveId)

    fun requested(): ThemeColors {
        val custom = overrides[theme.id].orEmpty()
        return ThemeSlot.entries.fold(theme.colors) { colors, slot ->
            custom[slot]?.let { colors.with(slot, ColorMath.opaque(it)) } ?: colors
        }
    }

    fun palette(): ThemePalette = ThemeResolver.resolve(requested())

    fun hasOverrides(): Boolean = overrides[theme.id].orEmpty().isNotEmpty()

    fun withOverride(slot: ThemeSlot, argb: Int): ThemeChoice {
        val custom = overrides[theme.id].orEmpty() + (slot to ColorMath.opaque(argb))
        return copy(overrides = overrides + (theme.id to custom))
    }

    fun withoutOverrides(): ThemeChoice = copy(overrides = overrides - theme.id)
}
