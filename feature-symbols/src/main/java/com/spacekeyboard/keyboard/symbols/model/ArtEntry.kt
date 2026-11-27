package com.spacekeyboard.keyboard.symbols.model

import com.spacekeyboard.keyboard.symbols.R

enum class ArtCategory(val id: String, val labelRes: Int) {
    HEARTS("hearts", R.string.art_hearts),
    FACES("faces", R.string.art_faces),
    SMALL("small", R.string.art_small),
    ANIMALS("animals", R.string.art_animals),
    NATURE("nature", R.string.art_nature),
    FOOD("food", R.string.art_food),
    VEHICLES("vehicles", R.string.art_vehicles),
    SPACE("space", R.string.art_space),
    SPORTS("sports", R.string.art_sports),
    OCCASIONS("occasions", R.string.art_occasions),
    SKULLS("skulls", R.string.art_skulls),
    OBJECTS("objects", R.string.art_objects),
    BLOCKS("blocks", R.string.art_blocks),
    LINES("lines", R.string.art_lines),
    FRAMES("frames", R.string.art_frames),
    DIVIDERS("dividers", R.string.art_dividers);

    companion object {
        fun byId(id: String): ArtCategory? = entries.firstOrNull { it.id == id }
    }
}

data class ArtEntry(val category: ArtCategory, val text: String) {
    val lines: List<String> get() = text.split('\n')

    val rowCount: Int get() = lines.size

    fun widest(): Float = lines.maxOf(::visualWidth)

    private fun visualWidth(line: String): Float {
        var total = 0f
        var index = 0
        while (index < line.length) {
            val point = line.codePointAt(index)
            total += if (point < NARROW_LIMIT) NARROW else WIDE
            index += Character.charCount(point)
        }
        return total
    }

    private companion object {
        const val NARROW_LIMIT = 0x2000
        const val NARROW = 0.62f
        const val WIDE = 1.05f
    }
}
