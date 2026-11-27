package com.spacekeyboard.keyboard.core.theme

enum class WallpaperKind { NONE, IMAGE, VIDEO }

data class WallpaperChoice(
    val kind: WallpaperKind = WallpaperKind.NONE,
    val opacity: Int = DEFAULT_OPACITY,
    val dim: Int = DEFAULT_DIM,
    val stamp: Long = 0L,
    val live: Boolean = false,
    val animate: Boolean = true,
) {
    val active: Boolean get() = kind != WallpaperKind.NONE

    val alpha: Float get() = opacity / 100f

    val brightness: Float get() = 1f - dim / 100f

    companion object {
        const val MIN_OPACITY = 10
        const val MAX_OPACITY = 100
        const val DEFAULT_OPACITY = 80
        const val MIN_DIM = 0
        const val MAX_DIM = 80
        const val DEFAULT_DIM = 15
        val NONE = WallpaperChoice()
    }
}

object WallpaperMath {
    const val MAX_SIDE = 1280
    const val MAX_VIDEO_SIDE = 1920
    const val MAX_VIDEO_BYTES = 40L * 1024 * 1024

    fun opacity(value: Int): Int = value.coerceIn(WallpaperChoice.MIN_OPACITY, WallpaperChoice.MAX_OPACITY)

    fun dim(value: Int): Int = value.coerceIn(WallpaperChoice.MIN_DIM, WallpaperChoice.MAX_DIM)

    fun sampleSize(width: Int, height: Int, target: Int): Int {
        if (width <= 0 || height <= 0 || target <= 0) return 1
        var sample = 1
        val longest = maxOf(width, height)
        while (longest / (sample * 2) >= target) sample *= 2
        return sample
    }

    fun fit(width: Int, height: Int, max: Int): Pair<Int, Int> {
        val longest = maxOf(width, height)
        if (longest <= max || longest <= 0) return width to height
        val scale = max.toFloat() / longest
        return maxOf(1, (width * scale).toInt()) to maxOf(1, (height * scale).toInt())
    }
}
