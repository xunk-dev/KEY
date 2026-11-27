package com.spacekeyboard.keyboard.decorate

object EnglishAlphabets {
    private val script = mapOf(
        'B' to 0x212C, 'E' to 0x2130, 'F' to 0x2131, 'H' to 0x210B, 'I' to 0x2110, 'L' to 0x2112,
        'M' to 0x2133, 'R' to 0x211B, 'e' to 0x212F, 'g' to 0x210A, 'o' to 0x2134,
    )
    private val fraktur = mapOf('C' to 0x212D, 'H' to 0x210C, 'I' to 0x2111, 'R' to 0x211C, 'Z' to 0x2128)
    private val double = mapOf(
        'C' to 0x2102, 'H' to 0x210D, 'N' to 0x2115, 'P' to 0x2119, 'Q' to 0x211A, 'R' to 0x211D, 'Z' to 0x2124,
    )
    private val circledDigits = ('1'..'9').associateWith { codePointString(0x2460 + (it - '1')) } +
        ('0' to codePointString(0x24EA))
    private val smallCaps = "abcdefghijklmnopqrstuvwxyz".zip("ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ").associate { it.first to it.second.toString() }
    private val upsideDown = "abcdefghijklmnopqrstuvwxyz".zip("ɐqɔpǝɟƃɥᴉɾʞlɯuodbɹsʇnʌʍxʎz").associate { it.first to it.second.toString() }

    val all: List<Alphabet> = listOf(
        Alphabet("bold", 0x1D400, 0x1D41A, 0x1D7CE),
        Alphabet("italic", 0x1D434, 0x1D44E, null, mapOf('h' to 0x210E)),
        Alphabet("bolditalic", 0x1D468, 0x1D482),
        Alphabet("script", 0x1D49C, 0x1D4B6, null, script),
        Alphabet("boldscript", 0x1D4D0, 0x1D4EA),
        Alphabet("fraktur", 0x1D504, 0x1D51E, null, fraktur),
        Alphabet("boldfraktur", 0x1D56C, 0x1D586),
        Alphabet("double", 0x1D538, 0x1D552, 0x1D7D8, double),
        Alphabet("sans", 0x1D5A0, 0x1D5BA, 0x1D7E2),
        Alphabet("sansbold", 0x1D5D4, 0x1D5EE, 0x1D7EC),
        Alphabet("sansitalic", 0x1D608, 0x1D622, 0x1D7E2),
        Alphabet("sansbolditalic", 0x1D63C, 0x1D656, 0x1D7EC),
        Alphabet("mono", 0x1D670, 0x1D68A, 0x1D7F6),
        Alphabet("circled", 0x24B6, 0x24D0, null, custom = circledDigits),
        Alphabet("negcircled", 0x1F150, 0x1F150, null, foldCase = true),
        Alphabet("squared", 0x1F130, 0x1F130, null, foldCase = true),
        Alphabet("negsquared", 0x1F170, 0x1F170, null, foldCase = true),
        Alphabet("fullwidth", 0xFF21, 0xFF41, 0xFF10, custom = mapOf(' ' to "　")),
        Alphabet("smallcaps", custom = smallCaps, foldCase = true),
        Alphabet("upsidedown", custom = upsideDown, foldCase = true, reversed = true),
    )

    fun byId(id: String): Alphabet = all.first { it.id == id }
}
