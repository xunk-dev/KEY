package com.spacekeyboard.keyboard.autofix

class Episode(val peak: List<TypedChar>, var cut: Int) {
    val typed = ArrayList<TypedChar>()
    var counted = false

    val removed: List<TypedChar> get() = peak.subList(cut, peak.size)

    val prefix: String get() = String(CharArray(cut) { peak[it].ch })
}
