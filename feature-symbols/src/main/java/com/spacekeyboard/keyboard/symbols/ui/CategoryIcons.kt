package com.spacekeyboard.keyboard.symbols.ui

import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.symbols.model.ArtCategory
import com.spacekeyboard.keyboard.symbols.model.FaceCategory
import com.spacekeyboard.keyboard.symbols.model.SymbolCategory

internal fun SymbolCategory.icon(): SpaceIcon = when (this) {
    SymbolCategory.FEATURED -> SpaceIcon.SYM_FEATURED
    SymbolCategory.CURRENCY -> SpaceIcon.SYM_CURRENCY
    SymbolCategory.MATH -> SpaceIcon.SYM_MATH
    SymbolCategory.ARROWS -> SpaceIcon.SYM_ARROWS
    SymbolCategory.STARS -> SpaceIcon.SYM_STARS
    SymbolCategory.HEARTS -> SpaceIcon.SYM_HEARTS
    SymbolCategory.SHAPES -> SpaceIcon.SYM_SHAPES
    SymbolCategory.MARKS -> SpaceIcon.SYM_MARKS
    SymbolCategory.SIGNS -> SpaceIcon.SYM_SIGNS
    SymbolCategory.PUNCTUATION -> SpaceIcon.SYM_PUNCTUATION
    SymbolCategory.BRACKETS -> SpaceIcon.SYM_BRACKETS
    SymbolCategory.FRAMES -> SpaceIcon.SYM_FRAMES
    SymbolCategory.GREEK -> SpaceIcon.SYM_GREEK
    SymbolCategory.CIRCLED -> SpaceIcon.SYM_CIRCLED
    SymbolCategory.NUMERALS -> SpaceIcon.SYM_NUMERALS
    SymbolCategory.ARABIC -> SpaceIcon.SYM_ARABIC
    SymbolCategory.GAMES -> SpaceIcon.SYM_GAMES
    SymbolCategory.MUSIC -> SpaceIcon.SYM_MUSIC
    SymbolCategory.MISC -> SpaceIcon.SYM_MISC
    SymbolCategory.ORNAMENTS -> SpaceIcon.SYM_ORNAMENTS
    SymbolCategory.TECHNICAL -> SpaceIcon.SYM_TECHNICAL
}

internal fun FaceCategory.icon(): SpaceIcon = when (this) {
    FaceCategory.CLASSIC -> SpaceIcon.FACE_CLASSIC
    FaceCategory.HAPPY -> SpaceIcon.FACE_HAPPY
    FaceCategory.LAUGH -> SpaceIcon.FACE_LAUGH
    FaceCategory.LOVE -> SpaceIcon.FACE_LOVE
    FaceCategory.SHY -> SpaceIcon.FACE_SHY
    FaceCategory.SAD -> SpaceIcon.FACE_SAD
    FaceCategory.ANGRY -> SpaceIcon.FACE_ANGRY
    FaceCategory.SURPRISED -> SpaceIcon.FACE_SURPRISED
    FaceCategory.SLEEPY -> SpaceIcon.FACE_SLEEPY
    FaceCategory.COOL -> SpaceIcon.FACE_COOL
    FaceCategory.EVIL -> SpaceIcon.FACE_EVIL
    FaceCategory.CONFUSED -> SpaceIcon.FACE_CONFUSED
    FaceCategory.ANIMALS -> SpaceIcon.FACE_ANIMALS
    FaceCategory.ACTIONS -> SpaceIcon.FACE_ACTIONS
}

internal fun ArtCategory.icon(): SpaceIcon = when (this) {
    ArtCategory.HEARTS -> SpaceIcon.SYM_HEARTS
    ArtCategory.FACES -> SpaceIcon.FACE_CLASSIC
    ArtCategory.SMALL -> SpaceIcon.FACE_LOVE
    ArtCategory.ANIMALS -> SpaceIcon.FACE_ANIMALS
    ArtCategory.NATURE -> SpaceIcon.SUN
    ArtCategory.FOOD -> SpaceIcon.GROUP_FOOD
    ArtCategory.VEHICLES -> SpaceIcon.GROUP_TRAVEL
    ArtCategory.SPACE -> SpaceIcon.MOON
    ArtCategory.SPORTS -> SpaceIcon.GROUP_ACTIVITIES
    ArtCategory.OCCASIONS -> SpaceIcon.SYM_STARS
    ArtCategory.SKULLS -> SpaceIcon.FACE_EVIL
    ArtCategory.OBJECTS -> SpaceIcon.GROUP_OBJECTS
    ArtCategory.BLOCKS -> SpaceIcon.SYM_SHAPES
    ArtCategory.LINES -> SpaceIcon.PENCIL
    ArtCategory.FRAMES -> SpaceIcon.SYM_FRAMES
    ArtCategory.DIVIDERS -> SpaceIcon.SYM_ORNAMENTS
}
