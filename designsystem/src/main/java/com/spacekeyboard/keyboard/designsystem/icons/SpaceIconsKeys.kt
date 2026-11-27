package com.spacekeyboard.keyboard.designsystem.icons

internal val KeyIconParts: Map<SpaceIcon, List<IconPart>> = mapOf(
    SpaceIcon.DELETE to listOf(
        acc(p("M10 7 5 12l5 5")),
        p("M5 12h9"),
        dot(17.6f, 12.0f, 0.2f),
        soft(dot(21.0f, 12.0f, 0.1f)),
    ),
    SpaceIcon.ENTER to listOf(
        p("M18 8v2.5a3.5 3.5 0 0 1-3.5 3.5H6"),
        acc(p("M9.5 10.5 6 14l3.5 3.5")),
        acc(dot(18.0f, 4.6f, 0.3f)),
    ),
    SpaceIcon.SEND to listOf(
        acc(p("M10 14 21 3")),
        p("M21 3l-6.5 18a.55.55 0 0 1-1 0l-3.5-7-7-3.5a.55.55 0 0 1 0-1L21 3"),
    ),
    SpaceIcon.SHIFT to listOf(
        acc(p("M5 15l7-7 7 7")),
    ),
    SpaceIcon.SHIFT_ONCE to listOf(
        acc(fill("M5 15l7-7 7 7Z")),
    ),
    SpaceIcon.SHIFT_LOCK to listOf(
        acc(fill("M5 13l7-7 7 7Z")),
        p("M6 19h12"),
    ),
    SpaceIcon.LANGUAGE to listOf(
        ring(9.0f, 12.0f, 5.5f),
        acc(ring(15.0f, 12.0f, 5.5f)),
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
