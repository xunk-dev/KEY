package com.spacekeyboard.keyboard.designsystem.icons

internal val ToolbarIconParts: Map<SpaceIcon, List<IconPart>> = mapOf(
    SpaceIcon.THEME to listOf(
        p("M12 3a9 9 0 1 0 0 18c1.1 0 1.8-.8 1.8-1.7 0-.5-.2-.9-.5-1.3-.3-.4-.5-.8-.5-1.3 0-1 .8-1.7 1.8-1.7H17a4 4 0 0 0 4-4c0-4.4-4-8-9-8Z"),
        dot(7.5f, 11.0f, 0.9f),
        dot(10.0f, 7.3f, 0.9f),
        dot(14.5f, 7.3f, 0.9f),
    ),
    SpaceIcon.MODE to listOf(
        box(3.0f, 5.0f, 18.0f, 14.0f, 3.0f),
        p("M12 5v14M7 10h.01M7 14h2.5M15 10h.01M15 14h2.5"),
    ),
    SpaceIcon.SYMBOLS to listOf(
        p("M4 19h4.5v-1.7a7 7 0 1 1 7 0V19H20"),
    ),
    SpaceIcon.EMOJI to listOf(
        ring(12.0f, 12.0f, 9.0f),
        p("M8.5 14.3s1.3 2.2 3.5 2.2 3.5-2.2 3.5-2.2M9 9.8h.01M15 9.8h.01"),
    ),
    SpaceIcon.DECORATE to listOf(
        p("M10.5 4.5 12.3 9.7 17.5 11.5 12.3 13.3 10.5 18.5 8.7 13.3 3.5 11.5 8.7 9.7 10.5 4.5Z"),
        p("M18.5 3.5v4M16.5 5.5h4M18 15.5v4M16 17.5h4"),
    ),
    SpaceIcon.CLIPBOARD to listOf(
        box(5.0f, 4.0f, 14.0f, 17.0f, 2.5f),
        p("M9 4v-.5A1.5 1.5 0 0 1 10.5 2h3A1.5 1.5 0 0 1 15 3.5V4M9 12h6M9 16h4"),
    ),
    SpaceIcon.TRANSLATE to listOf(
        p("M4 5h9M8.5 3v2M6 5c.6 3.3 2.7 6 5.5 7.5M12 5c-.6 3.3-2.8 6-6 7.5M12.5 20l4-9 4 9M14 17h5"),
    ),
    SpaceIcon.IMAGES to listOf(
        box(3.0f, 4.0f, 18.0f, 16.0f, 3.0f),
        ring(9.0f, 10.0f, 1.6f),
        p("M3.5 17.5 9 12.5l4 3.5 3-2.8 4.5 4"),
    ),
    SpaceIcon.VOICE to listOf(
        box(9.0f, 3.0f, 6.0f, 11.0f, 3.0f),
        p("M5 11a7 7 0 0 0 14 0M12 18v3M9 21h6"),
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
        box(2.5f, 6.0f, 19.0f, 12.0f, 3.0f),
        p("M7 10h.01M11 10h.01M15 10h.01M17.5 10h.01M8 14h8"),
    ),
    SpaceIcon.TOOLS to listOf(
        box(4.0f, 4.0f, 6.5f, 6.5f, 2.0f),
        box(13.5f, 4.0f, 6.5f, 6.5f, 2.0f),
        box(4.0f, 13.5f, 6.5f, 6.5f, 2.0f),
        box(13.5f, 13.5f, 6.5f, 6.5f, 2.0f),
    ),
    SpaceIcon.WORDS to listOf(
        p("M4 7h16M4 12h11M4 17h7"),
    ),
)
