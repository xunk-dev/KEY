package com.spacekeyboard.keyboard.designsystem.icons

internal val EmojiIconParts: Map<SpaceIcon, List<IconPart>> = mapOf(
    SpaceIcon.RECENTS to listOf(
        p("M3.5 12a8.5 8.5 0 1 0 2.7-6.2L3.5 8.4"),
        p("M3.5 4v4.4H7.9"),
        p("M12 7.6V12l3 1.9"),
    ),
    SpaceIcon.GROUP_FACES to listOf(
        ring(12.0f, 12.0f, 9.0f),
        dot(8.7f, 10.0f, 0.9f),
        p("M14 10.3c.7-1 1.9-1 2.6 0"),
        p("M8 14.2s1.5 2.8 4 2.8 4-2.8 4-2.8"),
    ),
    SpaceIcon.GROUP_BODY to listOf(
        p("M8 12V6.5a1.5 1.5 0 0 1 3 0V11"),
        p("M11 10.5V4.5a1.5 1.5 0 0 1 3 0V11"),
        p("M14 11V6.5a1.5 1.5 0 0 1 3 0V13"),
        p("M17 9.5a1.5 1.5 0 0 1 3 0v4.5a7 7 0 0 1-7 7h-1.2a7 7 0 0 1-5-2.1L3.8 15.3A1.6 1.6 0 0 1 6 13l2 2"),
    ),
    SpaceIcon.GROUP_PEOPLE to listOf(
        ring(9.5f, 8.0f, 3.2f),
        p("M3.5 20.5v-1.2a4.8 4.8 0 0 1 4.8-4.8h2.4a4.8 4.8 0 0 1 4.8 4.8v1.2"),
        p("M15.4 4.9a3.2 3.2 0 0 1 0 6.2"),
        p("M18.4 14.9a4.6 4.6 0 0 1 2.7 4.2v1.4"),
    ),
    SpaceIcon.GROUP_ANIMALS to listOf(
        p("M11.4 11.2C11.4 7.4 8.2 4.2 4.6 4.6c-.4 3.4 1.6 6.4 6.8 6.6Z"),
        p("M11.4 12.6c-3.8 0-6.7 2.1-6.2 4.9.4 2 3.1 2.6 4.8 1 .9-.8 1.4-3.2 1.4-5.9Z"),
        p("M12.6 11.2C12.6 7.4 15.8 4.2 19.4 4.6c.4 3.4-1.6 6.4-6.8 6.6Z"),
        p("M12.6 12.6c3.8 0 6.7 2.1 6.2 4.9-.4 2-3.1 2.6-4.8 1-.9-.8-1.4-3.2-1.4-5.9Z"),
        p("M12 7.5V18.5"),
        p("M12 7.5 10.4 4.8M12 7.5l1.6-2.7"),
    ),
    SpaceIcon.GROUP_FOOD to listOf(
        p("M4.5 10.5a7.5 6.5 0 0 1 15 0Z"),
        p("M3.5 13.5c1.4 1.3 2.85 1.3 4.25 0s2.85-1.3 4.25 0 2.85 1.3 4.25 0 2.85-1.3 4.25 0"),
        p("M4.5 16.7h15v.8a3 3 0 0 1-3 3h-9a3 3 0 0 1-3-3Z"),
    ),
    SpaceIcon.GROUP_TRAVEL to listOf(
        ring(12.0f, 12.0f, 9.0f),
        ring(12.0f, 12.0f, 2.3f),
        p("M3.2 10.4c3 .2 5.2.8 6.6 1.6M20.8 10.4c-3 .2-5.2.8-6.6 1.6M12 14.3V21"),
    ),
    SpaceIcon.GROUP_ACTIVITIES to listOf(
        p("M7.5 3.5h9v5.5a4.5 4.5 0 0 1-9 0Z"),
        p("M7.5 5.5H5a1 1 0 0 0-1 1V7a3 3 0 0 0 3 3h.5M16.5 5.5H19a1 1 0 0 1 1 1V7a3 3 0 0 1-3 3h-.5"),
        p("M12 13.5V17M8.5 20.5V19a2 2 0 0 1 2-2h3a2 2 0 0 1 2 2v1.5Z"),
    ),
    SpaceIcon.GROUP_OBJECTS to listOf(
        p("M12 7a4.5 4.5 0 0 0-2.7 8.1c.5.4.8.9.8 1.6h3.8c0-.7.3-1.2.8-1.6A4.5 4.5 0 0 0 12 7Z"),
        p("M10 19h4M10.8 21h2.4"),
        p("M12 2.5V4M5.2 5.2l1 1M18.8 5.2l-1 1M3 11.5h1.5M19.5 11.5H21"),
    ),
    SpaceIcon.GROUP_SYMBOLS to listOf(
        p("M8.3 20.3s-5-2.9-5-6.4a2.8 2.8 0 0 1 5-1.7 2.8 2.8 0 0 1 5 1.7c0 3.5-5 6.4-5 6.4Z"),
        p("M17.6 2.8l1.2 2.5 2.7.4-2 1.9.5 2.7-2.4-1.3-2.4 1.3.5-2.7-2-1.9 2.7-.4Z"),
    ),
    SpaceIcon.GROUP_FLAGS to listOf(
        p("M5 21.5V3.5"),
        p("M5 4.6c3-1.8 5.5-1.8 8 0s5 1.8 6.5 0V14c-1.5 1.8-3.5 1.8-6.5 0s-5-1.8-8 0"),
    ),
)
