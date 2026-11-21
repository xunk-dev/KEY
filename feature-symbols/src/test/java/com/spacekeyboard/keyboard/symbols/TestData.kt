package com.spacekeyboard.keyboard.symbols

import com.spacekeyboard.keyboard.symbols.data.SymbolData

object TestData {
    val symbolLines = listOf(
        "#space-keyboard-symbols 1",
        "20AC\tcurrency\teuro sign\teuro\tcurrency|euro\tيورو\tعملة|يورو",
        "2605\tstars\tblack star\tblack star\tfavorite|star\tنجمة سوداء\tنجمة|مفضل",
        "2606\tstars\twhite star\twhite star\tstar\t\t",
        "2765\thearts\trotated heavy black heart bullet\t\t\t\t",
        "2190\tarrows\tleftwards arrow\tleftwards arrow\tarrow|left\tسهم لليسار\tسهم",
        "3C0\tgreek\tgreek small letter pi\tpi\t\t\t",
        "2022\tpunctuation\tbullet\tbullet\t\t\t",
        "bad line",
        "ZZZ\tstars\tx\t\t\t\t",
        "2607\tnocategory\tx\t\t\t\t",
    )
    val featuredLines = listOf("#space-keyboard-featured 1", "2605", "20AC", "FFFFFF", "9999999")
    val faceLines = listOf(
        "#space-keyboard-faces 1",
        "classic\t^_^",
        "happy\t(◔‿◔)",
        "sad\t(╥﹏╥)",
        "mystery\t:-)",
        "sad\t",
    )
    val categoryLines = listOf(
        "#space-keyboard-face-categories 1",
        "happy\thappy smile joy\tسعيد ابتسامة",
        "sad\tsad cry\tحزين بكاء",
        "classic\tclassic emoticon smile\tكلاسيكي",
    )

    fun data(): SymbolData = SymbolData.parse(symbolLines, featuredLines, faceLines, categoryLines)
}
