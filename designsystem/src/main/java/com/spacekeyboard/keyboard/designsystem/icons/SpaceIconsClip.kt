package com.spacekeyboard.keyboard.designsystem.icons

private const val LOCK_BODY =
    "M6 11h12a1 1 0 0 1 1 1v7.5a1 1 0 0 1-1 1H6a1 1 0 0 1-1-1V12a1 1 0 0 1 1-1ZM12 15v2"

internal val ClipIconParts: Map<SpaceIcon, List<IconPart>> = mapOf(
    SpaceIcon.PIN to listOf(
        p("M9 3.5h6M10 3.5 9.5 9 7 12.5h10L14.5 9 14 3.5M12 12.5V21"),
    ),
    SpaceIcon.BOOKMARK to listOf(
        p("M7 3.5h10a1 1 0 0 1 1 1V21l-6-4.2L6 21V4.5a1 1 0 0 1 1-1ZM12 7.5v5M9.5 10h5"),
    ),
    SpaceIcon.TRASH to listOf(
        p("M4.5 7h15M9.5 7V4.5h5V7M6.5 7l.8 12.5a1 1 0 0 0 1 .9h7.4a1 1 0 0 0 1-.9L17.5 7M10 11v6M14 11v6"),
    ),
    SpaceIcon.BROOM to listOf(
        p("M3 8h5M3 12h3M3 16h4M20 3.5 13.2 12.2M11 11.5l5.5 3.5-1 5H8.5Z"),
    ),
    SpaceIcon.FOLDER_ADD to listOf(
        p("M3.5 7A1.5 1.5 0 0 1 5 5.5h4l2 2.5h8A1.5 1.5 0 0 1 20.5 9.5V18A1.5 1.5 0 0 1 19 19.5H5A1.5 1.5 0 0 1 3.5 18ZM12 11.5v5M9.5 14h5"),
    ),
    SpaceIcon.LOCK to listOf(
        p(LOCK_BODY),
        p("M8.5 11V8a3.5 3.5 0 0 1 7 0v3"),
    ),
    SpaceIcon.UNLOCK to listOf(
        p(LOCK_BODY),
        p("M8.5 11V8a3.5 3.5 0 0 1 6.7-1.4"),
    ),
    SpaceIcon.MORE to listOf(
        dot(12.0f, 5.5f, 1.1f),
        dot(12.0f, 12.0f, 1.1f),
        dot(12.0f, 18.5f, 1.1f),
    ),
    SpaceIcon.CLIP_EMPTY to listOf(
        p("M8 5H6.5A1.5 1.5 0 0 0 5 6.5v13A1.5 1.5 0 0 0 6.5 21h11a1.5 1.5 0 0 0 1.5-1.5V12"),
        p("M9.5 3h5a1 1 0 0 1 1 1v1.5a1 1 0 0 1-1 1h-5a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1Z"),
        p("M19.5 2.5l.8 1.9 1.9.8-1.9.8-.8 1.9-.8-1.9-1.9-.8 1.9-.8Z"),
    ),
    SpaceIcon.SHARE to listOf(
        p("M12 15V4M8.5 7.5 12 4l3.5 3.5M6 11H5.5a1 1 0 0 0-1 1v7a1 1 0 0 0 1 1h13a1 1 0 0 0 1-1v-7a1 1 0 0 0-1-1H18"),
    ),
    SpaceIcon.PENCIL to listOf(
        p("M4 20l1-4L16.5 4.5a2.2 2.2 0 0 1 3 3L8 19l-4 1ZM14.5 6.5l3 3"),
    ),
    SpaceIcon.FULL_VIEW to listOf(
        p("M9 4H5.5A1.5 1.5 0 0 0 4 5.5V9M15 4h3.5A1.5 1.5 0 0 1 20 5.5V9M9 20H5.5A1.5 1.5 0 0 1 4 18.5V15M15 20h3.5a1.5 1.5 0 0 0 1.5-1.5V15"),
        p("M8.5 10.5h7M8.5 13.5h4.5"),
    ),
    SpaceIcon.INSERT to listOf(
        p("M12 4v11M7.5 10.5 12 15l4.5-4.5M5 20h14"),
    ),
    SpaceIcon.SMART to listOf(
        p("M4 7h8M4 12h6M4 17h8"),
        acc(p("M17.5 9l1.3 3.2 3.2 1.3-3.2 1.3-1.3 3.2-1.3-3.2-3.2-1.3 3.2-1.3Z")),
    ),
    SpaceIcon.KIND_ALL to listOf(
        box(4.0f, 4.0f, 6.5f, 6.5f, 1.5f),
        soft(box(13.5f, 4.0f, 6.5f, 6.5f, 1.5f)),
        soft(box(4.0f, 13.5f, 6.5f, 6.5f, 1.5f)),
        box(13.5f, 13.5f, 6.5f, 6.5f, 1.5f),
    ),
    SpaceIcon.KIND_LINK to listOf(
        p("M10 13a5 5 0 0 0 7.5.5l3-3a5 5 0 0 0-7.1-7.1l-1.7 1.7"),
        acc(p("M14 11a5 5 0 0 0-7.5-.5l-3 3a5 5 0 0 0 7.1 7.1l1.7-1.7")),
    ),
    SpaceIcon.KIND_EMAIL to listOf(
        box(3.0f, 5.5f, 18.0f, 13.0f, 2.5f),
        acc(p("M3.5 8l8.5 6 8.5-6")),
    ),
    SpaceIcon.KIND_PHONE to listOf(
        p("M6.5 3.5h3l1.5 4-2 1.3a11 11 0 0 0 5.2 5.2l1.3-2 4 1.5v3a2 2 0 0 1-2 2A15.5 15.5 0 0 1 4.5 5.5a2 2 0 0 1 2-2Z"),
    ),
    SpaceIcon.KIND_USERNAME to listOf(
        acc(ring(12.0f, 12.0f, 4.0f)),
        p("M16 12v1.5a2.5 2.5 0 0 0 5 0V12a9 9 0 1 0-3.6 7.2"),
    ),
    SpaceIcon.KIND_TOKEN to listOf(
        acc(ring(7.5f, 16.5f, 3.5f)),
        p("M10 14 20 4M16.5 7.5 19 10M13.5 10.5 15.5 12.5"),
    ),
    SpaceIcon.KIND_NUMBER to listOf(
        p("M9 4 7 20M17 4l-2 16"),
        acc(p("M4.5 9h16M3.5 15h16")),
    ),
    SpaceIcon.KIND_TEXT to listOf(
        p("M4 6h16M4 12h16"),
        acc(p("M4 18h10")),
    ),
)
