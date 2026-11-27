package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.layout.GeometryInput
import com.spacekeyboard.keyboard.core.layout.KeyboardGeometry
import com.spacekeyboard.keyboard.core.layout.KeyboardPrefs
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.LayoutSpec
import com.spacekeyboard.keyboard.core.layout.Limits
import com.spacekeyboard.keyboard.core.layout.PanelMode
import com.spacekeyboard.keyboard.core.layout.Placement

internal fun computePlacement(
    spec: LayoutSpec,
    prefs: KeyboardPrefs,
    density: Float,
    width: Float,
    height: Float,
    editing: Boolean,
): Placement {
    val mode = if (editing) PanelMode.NORMAL else prefs.mode
    val input = GeometryInput(
        width, height, prefs.gapDp() * density, mode, prefs.oneHandPercent, prefs.splitGapPercent,
        bottomInset = if (editing) 0f else prefs.bottomSafePx(density),
        edgeInset = if (editing || spec.id == LayoutCatalog.numbers.id) 0f else Limits.EDGE_SAFE_DP * density,
        widthPercent = if (editing) Limits.WIDTH_MAX else prefs.widthPercent,
    )
    return KeyboardGeometry.place(spec, input)
}
