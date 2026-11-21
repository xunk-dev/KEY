package com.spacekeyboard.keyboard.designsystem.icons

internal val KeyIconParts: Map<SpaceIcon, List<IconPart>> = mapOf(
    SpaceIcon.DELETE to listOf(
        p("M20 5H9.3a2 2 0 0 0-1.5.7L3 12l4.8 6.3a2 2 0 0 0 1.5.7H20a1 1 0 0 0 1-1V6a1 1 0 0 0-1-1Z"),
        p("m12.5 9.5 5 5M17.5 9.5l-5 5"),
    ),
    SpaceIcon.ENTER to listOf(
        p("M19 5v6a2 2 0 0 1-2 2H5M9 9l-4 4 4 4"),
    ),
    SpaceIcon.SHIFT to listOf(
        p("M12 4 4.5 12.5H9V19h6v-6.5h4.5L12 4Z"),
    ),
    SpaceIcon.LANGUAGE to listOf(
        ring(12.0f, 12.0f, 9.0f),
        p("M3 12h18M12 3c2.4 2.5 3.6 5.5 3.6 9s-1.2 6.5-3.6 9c-2.4-2.5-3.6-5.5-3.6-9S9.6 5.5 12 3Z"),
    ),
    SpaceIcon.SWAP_SIDE to listOf(
        p("m7 8-4 4 4 4M17 8l4 4-4 4M3 12h18"),
    ),
    SpaceIcon.EXPAND to listOf(
        p("m15 6-6 6 6 6"),
    ),
    SpaceIcon.UP to listOf(
        p("M12 19V5M6 11l6-6 6 6"),
    ),
    SpaceIcon.DOWN to listOf(
        p("M12 5v14M6 13l6 6 6-6"),
    ),
    SpaceIcon.LEFT to listOf(
        p("M19 12H5M11 6l-6 6 6 6"),
    ),
    SpaceIcon.RIGHT to listOf(
        p("M5 12h14M13 6l6 6-6 6"),
    ),
    SpaceIcon.WORD_BACK to listOf(
        p("m11 7-5 5 5 5M18 7l-5 5 5 5"),
    ),
    SpaceIcon.LINE_START to listOf(
        p("M20 5v14M15 12H5M10 7l-5 5 5 5"),
    ),
    SpaceIcon.UNDO to listOf(
        p("M9 14 4 9l5-5M4 9h10.5a5.5 5.5 0 0 1 0 11H11"),
    ),
    SpaceIcon.REDO to listOf(
        p("m15 14 5-5-5-5M20 9H9.5a5.5 5.5 0 0 0 0 11H13"),
    ),
    SpaceIcon.SEARCH to listOf(
        ring(11.0f, 11.0f, 6.5f),
        p("m16 16 4.5 4.5"),
    ),
)
