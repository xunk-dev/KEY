package com.spacekeyboard.keyboard.symbols.model

import com.spacekeyboard.keyboard.symbols.R

enum class FaceCategory(val id: String, val labelRes: Int) {
    CLASSIC("classic", R.string.face_classic),
    HAPPY("happy", R.string.face_happy),
    LAUGH("laugh", R.string.face_laugh),
    LOVE("love", R.string.face_love),
    SHY("shy", R.string.face_shy),
    SAD("sad", R.string.face_sad),
    ANGRY("angry", R.string.face_angry),
    SURPRISED("surprised", R.string.face_surprised),
    SLEEPY("sleepy", R.string.face_sleepy),
    COOL("cool", R.string.face_cool),
    EVIL("evil", R.string.face_evil),
    CONFUSED("confused", R.string.face_confused),
    ANIMALS("animals", R.string.face_animals),
    ACTIONS("actions", R.string.face_actions);

    companion object {
        fun byId(id: String): FaceCategory? = entries.firstOrNull { it.id == id }
    }
}

data class FaceEntry(val category: FaceCategory, val text: String) {
    fun codePoints(): List<Int> = text.codePointList()
}
