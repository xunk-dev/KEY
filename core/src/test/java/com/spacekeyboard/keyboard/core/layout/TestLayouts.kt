package com.spacekeyboard.keyboard.core.layout

fun letter(id: String, weight: Float = 1f) = KeySpec(id, id, KeyAction.COMMIT, weight, true)

fun fixed(id: String, action: KeyAction, weight: Float) = KeySpec(id, "", action, weight, false)

fun sampleLayout() = LayoutSpec(
    "t",
    listOf(
        listOf(letter("a"), letter("b"), letter("c"), letter("d")),
        listOf(fixed("pad", KeyAction.SPACER, 1f), letter("e"), letter("f"), letter("g", 2f)),
        listOf(letter("h"), fixed("space", KeyAction.SPACE, 4f), KeySpec("del", "x", KeyAction.DELETE, 2f, true)),
    ),
)

fun inputOf(
    mode: PanelMode,
    width: Float = 1000f,
    height: Float = 300f,
    oneHand: Int = 80,
    splitGap: Int = 20,
) = GeometryInput(width, height, 0f, mode, oneHand, splitGap)
