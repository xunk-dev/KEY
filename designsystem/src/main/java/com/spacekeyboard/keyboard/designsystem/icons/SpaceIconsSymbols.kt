package com.spacekeyboard.keyboard.designsystem.icons

private fun face(vararg parts: IconPart): List<IconPart> = listOf(ring(12.0f, 12.0f, 9.0f)) + parts

internal val SymbolIconParts: Map<SpaceIcon, List<IconPart>> = mapOf(
    SpaceIcon.SYM_FEATURED to listOf(p("M12 3l2.2 6.3L21 12l-6.8 2.7L12 21l-2.2-6.3L3 12l6.8-2.7Z")),
    SpaceIcon.SYM_CURRENCY to listOf(
        ring(12.0f, 12.0f, 9.0f),
        p("M14.5 9.5c-.5-1-1.5-1.5-2.5-1.5-1.5 0-2.5.8-2.5 2s1 1.7 2.5 2 2.5.8 2.5 2-1 2-2.5 2c-1 0-2-.5-2.5-1.5"),
        p("M12 6V8M12 16v2"),
    ),
    SpaceIcon.SYM_MATH to listOf(p("M12 4v8M8 8h8M8 18h8")),
    SpaceIcon.SYM_ARROWS to listOf(p("M4 12h16M14 6l6 6-6 6")),
    SpaceIcon.SYM_STARS to listOf(p("M12 3.5l2.6 5.4 5.9.8-4.3 4.1 1.1 5.9L12 16.9l-5.3 2.8 1.1-5.9L3.5 9.7l5.9-.8Z")),
    SpaceIcon.SYM_HEARTS to listOf(p("M12 20s-8-4.6-8-10.2A4.3 4.3 0 0 1 12 7.6a4.3 4.3 0 0 1 8 2.2C20 15.4 12 20 12 20Z")),
    SpaceIcon.SYM_SHAPES to listOf(
        p("M7 3.5l4 7H3Z"),
        ring(17.0f, 7.0f, 3.5f),
        box(3.5f, 13.5f, 7.0f, 7.0f, 1.5f),
        p("M17 13.5l3.5 7h-7Z"),
    ),
    SpaceIcon.SYM_MARKS to listOf(p("M4 12.5l4.5 4.5L20 6.5")),
    SpaceIcon.SYM_SIGNS to listOf(ring(12.0f, 12.0f, 9.0f), p("M14.8 9.6a3.5 3.5 0 1 0 0 4.8")),
    SpaceIcon.SYM_PUNCTUATION to listOf(p("M9 9a3 3 0 1 1 4.2 2.7c-.8.4-1.2 1-1.2 1.8M12 17h.01")),
    SpaceIcon.SYM_BRACKETS to listOf(p("M9 4H6v16h3M15 4h3v16h-3")),
    SpaceIcon.SYM_FRAMES to listOf(box(3.0f, 3.0f, 18.0f, 18.0f, 3.0f), box(7.0f, 7.0f, 10.0f, 10.0f, 1.5f)),
    SpaceIcon.SYM_GREEK to listOf(p("M18 5H6l6 7-6 7h12")),
    SpaceIcon.SYM_CIRCLED to listOf(ring(12.0f, 12.0f, 9.0f), p("M10.5 9.5 12 8v8")),
    SpaceIcon.SYM_NUMERALS to listOf(p("M9 4 7 20M17 4l-2 16M4.5 9h16M3.5 15h16")),
    SpaceIcon.SYM_ARABIC to listOf(p("M20 14.5A8.5 8.5 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5Z")),
    SpaceIcon.SYM_GAMES to listOf(
        box(4.0f, 4.0f, 16.0f, 16.0f, 3.0f),
        dot(9.0f, 9.0f, 1.1f), dot(15.0f, 9.0f, 1.1f), dot(12.0f, 12.0f, 1.1f), dot(9.0f, 15.0f, 1.1f), dot(15.0f, 15.0f, 1.1f),
    ),
    SpaceIcon.SYM_MUSIC to listOf(p("M9 18V6l10-2v12"), ring(6.5f, 18.0f, 2.5f), ring(16.5f, 16.0f, 2.5f)),
    SpaceIcon.SYM_MISC to listOf(dot(5.5f, 12.0f, 1.6f), dot(12.0f, 12.0f, 1.6f), dot(18.5f, 12.0f, 1.6f)),
    SpaceIcon.SYM_ORNAMENTS to listOf(p("M3.5 12c2.5-5 5-5 8.5 0s6 5 8.5 0")),
    SpaceIcon.SYM_TECHNICAL to listOf(
        p("M9 9V6.5A2.5 2.5 0 1 0 6.5 9H9Zm0 0h6m-6 0v6m6-6V6.5A2.5 2.5 0 1 1 17.5 9H15Zm0 0v6m0 0h2.5a2.5 2.5 0 1 1-2.5 2.5V15Zm0 0H9m0 0H6.5A2.5 2.5 0 1 0 9 17.5V15Z"),
    ),
    SpaceIcon.ART to listOf(p("M5 6h5v2h4V6h5v6h-2v2h-2v2h-2v2h-2v-2H9v-2H7v-2H5Z")),
    SpaceIcon.FACE_CLASSIC to face(dot(8.8f, 10.0f, 0.9f), dot(15.2f, 10.0f, 0.9f), p("M8.5 14.5q3.5 2.5 7 0")),
    SpaceIcon.FACE_HAPPY to face(p("M7.5 10.5q1.5-2 3 0M13.5 10.5q1.5-2 3 0"), p("M8 14s1.5 3 4 3 4-3 4-3")),
    SpaceIcon.FACE_LAUGH to face(p("M7.5 9.5l3 1-3 1M16.5 9.5l-3 1 3 1"), p("M8 14h8c0 2.5-1.8 4-4 4s-4-1.5-4-4Z")),
    SpaceIcon.FACE_LOVE to face(
        p("M9 12.2s-2.2-1.3-2.2-2.9a1.1 1.1 0 0 1 2.2-.3 1.1 1.1 0 0 1 2.2.3c0 1.6-2.2 2.9-2.2 2.9Z"),
        p("M15 12.2s-2.2-1.3-2.2-2.9a1.1 1.1 0 0 1 2.2-.3 1.1 1.1 0 0 1 2.2.3c0 1.6-2.2 2.9-2.2 2.9Z"),
        p("M8.5 15s1.3 2.2 3.5 2.2 3.5-2.2 3.5-2.2"),
    ),
    SpaceIcon.FACE_SHY to face(p("M7.5 10.5q1.5-2 3 0M13.5 10.5q1.5-2 3 0"), p("M5.8 13.8h.01M18.2 13.8h.01"), p("M10.5 15.5h3")),
    SpaceIcon.FACE_SAD to face(dot(8.8f, 10.0f, 0.9f), dot(15.2f, 10.0f, 0.9f), p("M8.5 16.5s1.2-2.5 3.5-2.5 3.5 2.5 3.5 2.5")),
    SpaceIcon.FACE_ANGRY to face(p("M7 8.5l3.5 1.5M17 8.5l-3.5 1.5"), dot(9.0f, 11.8f, 0.9f), dot(15.0f, 11.8f, 0.9f), p("M8.5 16.5s1.2-2.5 3.5-2.5 3.5 2.5 3.5 2.5")),
    SpaceIcon.FACE_SURPRISED to face(dot(8.8f, 10.0f, 0.9f), dot(15.2f, 10.0f, 0.9f), ring(12.0f, 15.5f, 1.8f)),
    SpaceIcon.FACE_SLEEPY to face(p("M7.5 10.5h3M13.5 10.5h3"), ring(12.0f, 15.5f, 1.2f), p("M16.5 3.2h3l-3 3h3")),
    SpaceIcon.FACE_COOL to face(
        p("M6 9.5h5v2.5a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2Z"),
        p("M13 9.5h5v2.5a2 2 0 0 1-2 2h-1a2 2 0 0 1-2-2Z"),
        p("M11 10.5h2"),
        p("M8.5 16.5s1.3 1.8 3.5 1.8 3.5-1.8 3.5-1.8"),
    ),
    SpaceIcon.FACE_EVIL to face(p("M5 6.5l2.5 2M19 6.5l-2.5 2"), dot(8.8f, 11.0f, 0.9f), dot(15.2f, 11.0f, 0.9f), p("M8 14.5s1.5 2.5 4 2.5 4-2.5 4-2.5")),
    SpaceIcon.FACE_CONFUSED to face(dot(8.8f, 10.0f, 0.9f), dot(15.2f, 10.0f, 0.9f), p("M8.5 16c1-1 2-1 3.5 0s2.5 1 3.5 0")),
    SpaceIcon.FACE_ANIMALS to listOf(
        p("M5 9V4.5l4 2.5h6l4-2.5V9a7 7 0 0 1-14 0Z"),
        dot(9.5f, 11.0f, 0.9f), dot(14.5f, 11.0f, 0.9f), p("M11 14h2"),
    ),
    SpaceIcon.FACE_ACTIONS to EmojiIconParts.getValue(SpaceIcon.GROUP_BODY),
    SpaceIcon.FILTER_ALL to listOf(
        box(4.0f, 4.0f, 6.5f, 6.5f, 2.0f), box(13.5f, 4.0f, 6.5f, 6.5f, 2.0f),
        box(4.0f, 13.5f, 6.5f, 6.5f, 2.0f), box(13.5f, 13.5f, 6.5f, 6.5f, 2.0f),
    ),
    SpaceIcon.FILTER_GIF to listOf(box(2.5f, 5.0f, 19.0f, 14.0f, 3.5f), p("M10 9.5v5l4.5-2.5Z")),
    SpaceIcon.FILTER_STICKER to listOf(
        p("M4 6a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v7l-7 7H6a2 2 0 0 1-2-2ZM20 13h-5a2 2 0 0 0-2 2v5"),
        p("M9 9.5h.01M15 9.5h.01M8.8 13s1 1.8 3.2 1.8"),
    ),
)
