package com.spacekeyboard.keyboard.core.layout

object DimLimits {
    const val LEVEL_MIN = 20
    const val LEVEL_MAX = 100
    const val LEVEL_DEFAULT = 100
}

data class DimPrefs(val levelPercent: Int = DimLimits.LEVEL_DEFAULT) {
    val brightness: Float get() = levelPercent / PERCENT
    val dimmed: Boolean get() = levelPercent < DimLimits.LEVEL_MAX

    fun sanitized(): DimPrefs = copy(levelPercent = levelPercent.coerceIn(DimLimits.LEVEL_MIN, DimLimits.LEVEL_MAX))

    private companion object {
        const val PERCENT = 100f
    }
}
