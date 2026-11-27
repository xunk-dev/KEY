package com.spacekeyboard.keyboard.designsystem.icons

internal val ToolbarIconParts: Map<SpaceIcon, List<IconPart>> = mapOf(
    SpaceIcon.THEME to listOf(
        p("M12 3a9 9 0 1 0 0 18c1.1 0 1.8-.8 1.8-1.7 0-.5-.2-.9-.5-1.3-.3-.4-.5-.8-.5-1.3 0-1 .8-1.7 1.8-1.7H17a4 4 0 0 0 4-4c0-4.4-4-8-9-8Z"),
        dot(7.5f, 11.0f, 0.9f),
        dot(10.0f, 7.3f, 0.9f),
        dot(14.5f, 7.3f, 0.9f),
    ),
    SpaceIcon.RESIZE to listOf(
        box(2.0f, 9.0f, 20.0f, 12.0f, 3.0f),
        p("M6 13h.01M10 13h.01M14 13h.01M18 13h.01"),
        p("M8 17h8"),
        acc(p("M12 7V3M9.5 5.5 12 3l2.5 2.5")),
    ),
    SpaceIcon.MODE to listOf(
        box(2.0f, 3.0f, 20.0f, 11.0f, 3.0f),
        p("M6 7h.01M10 7h.01M14 7h.01M18 7h.01"),
        p("M8 10h8"),
        acc(p("M8 17l-2 2 2 2M6 19h12M16 17l2 2-2 2")),
    ),
    SpaceIcon.MODE_NORMAL to listOf(
        box(2.0f, 4.0f, 20.0f, 16.0f, 3.0f),
        p("M6 8h.01M10 8h.01M14 8h.01M18 8h.01"),
        p("M8 12h.01M12 12h.01M16 12h.01"),
        p("M7 16h10"),
    ),
    SpaceIcon.MODE_ONE_RIGHT to listOf(
        soft(box(2.0f, 4.0f, 20.0f, 16.0f, 3.0f)),
        box(10.0f, 4.0f, 12.0f, 16.0f, 3.0f),
        p("M14 8h.01M18 8h.01"),
        p("M14 12h.01M18 12h.01"),
        p("M14.5 16h3"),
    ),
    SpaceIcon.MODE_ONE_LEFT to listOf(
        soft(box(2.0f, 4.0f, 20.0f, 16.0f, 3.0f)),
        box(2.0f, 4.0f, 12.0f, 16.0f, 3.0f),
        p("M6 8h.01M10 8h.01"),
        p("M6 12h.01M10 12h.01"),
        p("M6.5 16h3"),
    ),
    SpaceIcon.MODE_SPLIT to listOf(
        box(2.0f, 4.0f, 8.0f, 16.0f, 3.0f),
        box(14.0f, 4.0f, 8.0f, 16.0f, 3.0f),
        p("M4.5 8h.01M7.5 8h.01"),
        p("M4.5 12h.01M7.5 12h.01"),
        p("M5 16h2"),
        p("M16.5 8h.01M19.5 8h.01"),
        p("M16.5 12h.01M19.5 12h.01"),
        p("M17 16h2"),
    ),
    SpaceIcon.SYMBOLS to listOf(
        box(3.0f, 3.0f, 18.0f, 18.0f, 4.0f),
        acc(p("M6.5 8.5h4M8.5 6.5v4M13.5 8.5h4")),
        p("m6.8 14.3 3.4 3.4m0-3.4-3.4 3.4M13.5 16h4M15.5 14.3h.01M15.5 17.7h.01"),
    ),
    SpaceIcon.EMOJI to listOf(
        ring(12.0f, 12.0f, 9.0f),
        acc(p("M13.5 10H16M8.5 14.5c1.8 2.2 5.2 2.2 7 0")),
        dot(9.0f, 10.0f, 0.1f),
    ),
    SpaceIcon.DECORATE to listOf(
        p("M10.5 4.5 12.3 9.7 17.5 11.5 12.3 13.3 10.5 18.5 8.7 13.3 3.5 11.5 8.7 9.7 10.5 4.5Z"),
        p("M18.5 3.5v4M16.5 5.5h4M18 15.5v4M16 17.5h4"),
    ),
    SpaceIcon.CLIPBOARD to listOf(
        soft(box(8.0f, 3.0f, 12.0f, 14.0f, 3.0f)),
        box(4.0f, 7.0f, 12.0f, 14.0f, 3.0f),
        acc(p("M7.5 12.5h5M7.5 16h3")),
    ),
    SpaceIcon.TRANSLATE to listOf(
        box(3.0f, 3.0f, 11.0f, 8.0f, 3.0f),
        p("M6 11v3l3-3"),
        acc(box(10.0f, 13.0f, 11.0f, 8.0f, 3.0f)),
        acc(p("M18 13v-3l-3 3")),
        dot(8.5f, 7.0f, 0.1f),
    ),
    SpaceIcon.IMAGES to listOf(
        box(3.0f, 4.0f, 18.0f, 16.0f, 3.0f),
        ring(9.0f, 10.0f, 1.6f),
        p("M3.5 17.5 9 12.5l4 3.5 3-2.8 4.5 4"),
    ),
    SpaceIcon.VOICE to listOf(
        p("M6.5 10v4M17.5 9v6"),
        acc(p("M12 6v12")),
    ),
    SpaceIcon.EDIT to listOf(
        p("M10 4h4M10 20h4M12 4v16M7 9l-3 3 3 3M17 9l3 3-3 3"),
    ),
    SpaceIcon.INCOGNITO to listOf(
        p("M3 11.5h18M6.5 11.5l1.4-5.9A1.5 1.5 0 0 1 9.4 4.5h5.2a1.5 1.5 0 0 1 1.5 1.1l1.4 5.9"),
        ring(8.0f, 17.0f, 2.8f),
        ring(16.0f, 17.0f, 2.8f),
        p("M10.8 16.6h2.4"),
    ),
    SpaceIcon.SETTINGS to listOf(
        p("M4 7h8M18 7h2M4 17h2M12 17h8"),
        ring(15.0f, 7.0f, 2.5f),
        ring(9.0f, 17.0f, 2.5f),
    ),
    SpaceIcon.KEYS to listOf(
        box(2.5f, 5.0f, 19.0f, 14.0f, 3.0f),
        p("M6.5 9h.01M10 9h.01M14 9h.01M17.5 9h.01M6.5 12h.01M10 12h.01M14 12h.01M17.5 12h.01"),
        acc(p("M8 15.5h8")),
    ),
    SpaceIcon.NUMPAD to listOf(
        box(4.5f, 3.0f, 15.0f, 18.0f, 3.5f),
        p("M8.5 7.5h.01M12 7.5h.01M15.5 7.5h.01M8.5 11.5h.01M12 11.5h.01M15.5 11.5h.01M8.5 15.5h.01M15.5 15.5h.01"),
        acc(p("M12 15.5h.01")),
    ),
    SpaceIcon.TOOLS to listOf(
        box(4.0f, 4.0f, 6.5f, 6.5f, 2.0f),
        acc(ring(17.0f, 7.25f, 3.25f)),
        box(4.0f, 13.5f, 6.5f, 6.5f, 2.0f),
        box(13.5f, 13.5f, 6.5f, 6.5f, 2.0f),
    ),
    SpaceIcon.WORDS to listOf(
        p("M4 7h16M4 12h11M4 17h7"),
    ),
    SpaceIcon.FLOAT to listOf(
        box(3.5f, 4.5f, 17.0f, 10.0f, 2.5f),
        p("M8 8.5h.01M12 8.5h.01M16 8.5h.01M8.5 11.5h7M7 18.5h10M10 21.5h4"),
    ),
    SpaceIcon.OPACITY to listOf(
        ring(12.0f, 12.0f, 9.0f),
        fill("M12 3a9 9 0 0 1 0 18Z"),
    ),
    SpaceIcon.CENTER to listOf(
        ring(12.0f, 12.0f, 5.5f),
        dot(12.0f, 12.0f, 1.2f),
        p("M12 2.5v3M12 18.5v3M2.5 12h3M18.5 12h3"),
    ),
    SpaceIcon.DOCK to listOf(
        p("M8.5 5.5 12 9l3.5-3.5"),
        box(3.5f, 12.5f, 17.0f, 8.0f, 2.5f),
        p("M8 16.5h.01M12 16.5h.01M16 16.5h.01"),
    ),
    SpaceIcon.MOVE to listOf(
        p("M12 3v18M3 12h18M9 6l3-3 3 3M9 18l3 3 3-3M6 9l-3 3 3 3M18 9l3 3-3 3"),
    ),
    SpaceIcon.MOON to listOf(
        p("M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5Z"),
    ),
    SpaceIcon.SUN to listOf(
        ring(12.0f, 12.0f, 4.0f),
        p("M12 2.5v2.5M12 19v2.5M2.5 12H5M19 12h2.5M5.2 5.2l1.8 1.8M17 17l1.8 1.8M18.8 5.2 17 7M7 17l-1.8 1.8"),
    ),
    SpaceIcon.CLOSE to listOf(
        p("M6 6l12 12M18 6 6 18"),
    ),
    SpaceIcon.CALCULATOR to listOf(
        box(5.0f, 3.0f, 14.0f, 18.0f, 2.5f),
        p("M8 7h8M8.5 12h.01M12 12h.01M15.5 12h.01M8.5 16h.01M12 16h.01M15.5 16h.01"),
    ),
    SpaceIcon.AUTOMATION to listOf(
        p("M17 3l3 3-3 3M20 6H8a4 4 0 0 0-4 4M7 21l-3-3 3-3M4 18h12a4 4 0 0 0 4-4"),
        acc(dot(12.0f, 12.0f, 1.8f)),
    ),
    SpaceIcon.PLAY to listOf(
        fill("M8 5.5v13a.8.8 0 0 0 1.2.7l10.5-6.5a.8.8 0 0 0 0-1.4L9.2 4.8A.8.8 0 0 0 8 5.5Z"),
    ),
    SpaceIcon.PAUSE to listOf(
        fill("M7 5h3.2v14H7ZM13.8 5H17v14h-3.2Z"),
    ),
    SpaceIcon.STOP to listOf(
        fill("M7 7h10v10H7Z"),
    ),
)
