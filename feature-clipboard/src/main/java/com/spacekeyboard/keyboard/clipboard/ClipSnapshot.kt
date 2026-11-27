package com.spacekeyboard.keyboard.clipboard

enum class ClipTab { CURRENT, PINNED, SAVED }

enum class ClipMode { CLOSED, BROWSE, NAMING }

sealed interface ClipConfirm {
    data object ClearCurrent : ClipConfirm
    data class DeleteSection(val name: String) : ClipConfirm
    data class PasteLarge(val id: Long, val length: Long) : ClipConfirm
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
    val counts: Map<ClipTab, Int> = emptyMap(),
    val viewing: ClipItem? = null,
    val order: List<ClipTab> = ClipTabOrder.DEFAULT,
    val busy: ClipBusy? = null,
    val undo: ClipItem? = null,
    val smart: Boolean = false,
    val kind: ClipKind? = null,
    val kindCounts: Map<ClipKind, Int> = emptyMap(),
    val grouped: Boolean = false,
    val titling: ClipItem? = null,
) {
    val open: Boolean get() = mode != ClipMode.CLOSED

    val showKinds: Boolean get() = smart && kindCounts.size > 1

    val sectionLocked: Boolean get() = sections.firstOrNull { it.name == section }?.locked == true
}
