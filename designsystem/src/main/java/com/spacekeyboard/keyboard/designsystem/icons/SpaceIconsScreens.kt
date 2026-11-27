package com.spacekeyboard.keyboard.designsystem.icons

internal val ScreenIconParts: Map<SpaceIcon, List<IconPart>> = mapOf(
    SpaceIcon.VIBRATE to listOf(
        box(8.0f, 4.0f, 8.0f, 16.0f, 2.5f),
        acc(p("M4.5 9v6M19.5 9v6M2 11v2M22 11v2")),
    ),
    SpaceIcon.SOUND to listOf(
        p("M4 9.5v5h3.5l4.5 4v-13l-4.5 4H4Z"),
        acc(p("M15.5 9.2a4 4 0 0 1 0 5.6M18.2 6.6a7.7 7.7 0 0 1 0 10.8")),
    ),
    SpaceIcon.CUSTOMIZE to listOf(
        box(3.0f, 3.0f, 18.0f, 18.0f, 3.0f),
        p("M3 9h18M9 21V9"),
    ),
    SpaceIcon.AUTOFIX to listOf(
        p("M4 15 8 5l4 10M5.5 11.5h5M14 17l2.5 2.5L21 13"),
    ),
    SpaceIcon.SHORTCUTS to listOf(
        p("M13 3 5 13.5h6L10 21l8-10.5h-6L13 3Z"),
    ),
    SpaceIcon.SETUP to listOf(
        p("M12 3v8M7.1 6.6a8 8 0 1 0 9.8 0"),
    ),
    SpaceIcon.PRIVACY to listOf(
        p("M12 3 19.5 6v5.5c0 4.5-3.2 8.2-7.5 9.5-4.3-1.3-7.5-5-7.5-9.5V6L12 3Z"),
        p("m9 12 2.2 2.2L15.5 10"),
    ),
    SpaceIcon.BACKUP to listOf(
        p("M7 18.5a4.5 4.5 0 0 1-.6-8.96A6 6 0 0 1 18 10.8a3.9 3.9 0 0 1-.5 7.7"),
        p("M12 20v-7M9.5 15.5 12 13l2.5 2.5"),
    ),
    SpaceIcon.NEXT to listOf(
        p("m15 6-6 6 6 6"),
    ),
    SpaceIcon.DONE to listOf(
        p("m5 12.5 4.5 4.5L19 7.5"),
    ),
    SpaceIcon.COPY to listOf(
        box(9.0f, 9.0f, 11.0f, 11.0f, 2.5f),
        p("M15 9V6.5A2.5 2.5 0 0 0 12.5 4h-6A2.5 2.5 0 0 0 4 6.5v6A2.5 2.5 0 0 0 6.5 15H9"),
    ),
    SpaceIcon.CUT to listOf(
        ring(6.0f, 6.5f, 2.5f),
        ring(6.0f, 17.5f, 2.5f),
        p("m8 8 12 9M8 16 20 7"),
    ),
    SpaceIcon.SELECT_ALL to listOf(
        box(4.0f, 4.0f, 16.0f, 16.0f, 3.0f, dashed = true),
    ),
    SpaceIcon.ADD to listOf(
        p("M12 5v14M5 12h14"),
    ),
    SpaceIcon.CLEAR_ALL to listOf(
        p("M4 7h16M9 7V4.5h6V7M6.5 7l1 13h9l1-13M10 11v5M14 11v5"),
    ),
    SpaceIcon.SAVE to listOf(
        p("M7 4h10a1 1 0 0 1 1 1v15l-6-4-6 4V5a1 1 0 0 1 1-1Z"),
    ),
    SpaceIcon.CHEVRON to listOf(
        p("m15 6-6 6 6 6"),
    ),
    SpaceIcon.CHECK to listOf(
        p("m5 12.5 4.5 4.5L19 7.5"),
    ),
)
