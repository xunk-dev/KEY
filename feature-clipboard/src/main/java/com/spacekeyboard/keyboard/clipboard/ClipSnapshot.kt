package com.spacekeyboard.keyboard.clipboard

enum class ClipTab { CURRENT, PINNED, SAVED }

enum class ClipMode { CLOSED, BROWSE, NAMING }

sealed interface ClipConfirm {
    data object ClearCurrent : ClipConfirm
    data class DeleteSection(val name: String) : ClipConfirm
}

data class ClipSnapshot(
    val mode: ClipMode = ClipMode.CLOSED,
    val allowed: Boolean = true,
    val enabled: Boolean = false,
    val tab: ClipTab = ClipTab.CURRENT,
    val sections: List<Section> = emptyList(),
    val section: String? = null,
    val items: List<ClipItem> = emptyList(),
    val locked: Boolean = false,
    val unlocking: Boolean = false,
    val naming: String = "",
    val assigning: ClipItem? = null,
    val confirm: ClipConfirm? = null,
) {
    val open: Boolean get() = mode != ClipMode.CLOSED

    val sectionLocked: Boolean get() = sections.firstOrNull { it.name == section }?.locked == true
}
