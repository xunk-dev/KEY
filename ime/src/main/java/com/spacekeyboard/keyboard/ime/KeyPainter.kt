package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.spacekeyboard.keyboard.core.layout.Box
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.PlacedKey
import com.spacekeyboard.keyboard.core.layout.Placement
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.ime.layout.DragState
import com.spacekeyboard.keyboard.ime.layout.LabelTypefaces
import com.spacekeyboard.keyboard.ime.layout.LayoutSnapshot

class KeyPainter(context: Context) {
    private val density = context.resources.displayMetrics.density
    private val typefaces = LabelTypefaces(context)
    private val icons = KeyIcons()
    private var look = KeyStyle.GALAXY
    private var railLeft = false
    private val rect = RectF()
    private val halo = RectF()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = density
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.RIGHT }
    private var layout: LayoutSnapshot? = null

    fun draw(
        canvas: Canvas,
        placement: Placement,
        palette: ThemePalette,
        keyStyle: KeyStyle,
        snapshot: LayoutSnapshot,
        pressed: Collection<PlacedKey>,
        drag: DragState?,
    ) {
        look = keyStyle
        layout = snapshot
        val prefs = snapshot.prefs
        textPaint.typeface = typefaces.resolve(prefs.label)
        val height = placement.keys.firstOrNull()?.box?.height ?: return
        textPaint.textSize = minOf(BASE_TEXT_DP * density * prefs.labelScale(), height * MAX_TEXT_RATIO)
        railLeft = railOnLeft(placement)
        val dragging = drag?.takeIf { it.active }
        placement.keys.forEach { placed ->
            val lit = placed in pressed || snapshot.isActive(placed.key) || (dragging != null && placed.slot == dragging.target?.slot)
            val faded = dragging != null && placed.slot == dragging.from.slot
            drawKey(canvas, placed.box, placed.key, palette, lit, if (faded) FADED_ALPHA else 1f)
        }
        if (dragging != null) drawLifted(canvas, dragging, palette)
    }

    private fun drawLifted(canvas: Canvas, drag: DragState, palette: ThemePalette) {
        val size = drag.from.box
        val lifted = Box(
            drag.x - size.width / 2f,
            drag.y - size.height / 2f,
            drag.x + size.width / 2f,
            drag.y + size.height / 2f,
        )
        drawKey(canvas, lifted, drag.from.key, palette, true, 1f)
    }

    private fun drawKey(canvas: Canvas, box: Box, key: KeySpec, palette: ThemePalette, lit: Boolean, alpha: Float) {
        rect.set(box.left, box.top, box.right, box.bottom)
        val skin = KeySkins.of(look, keyRole(key.action), palette, lit)
        val radius = if (skin.pill) box.height / 2f else minOf(skin.radiusDp * density, box.height * RADIUS_RATIO)
        if (skin.halo) drawHalo(canvas, radius, palette.glow)
        if (skin.shadow != 0) drawShadow(canvas, radius, fade(skin.shadow, alpha))
        fillPaint.color = fade(skin.fill, alpha)
        canvas.drawRoundRect(rect, radius, radius, fillPaint)
        if (skin.border != 0) {
            borderPaint.color = fade(skin.border, alpha)
            canvas.drawRoundRect(rect, radius, radius, borderPaint)
        }
        drawContent(canvas, box, key, fade(skin.ink, alpha), alpha)
    }

    private fun drawContent(canvas: Canvas, box: Box, key: KeySpec, ink: Int, alpha: Float) {
        val current = layout
        val enterText = if (key.action == KeyAction.ENTER) current?.enterLabel else null
        if (enterText == null && icons.handles(key.action)) {
            icons.draw(canvas, key.action, box, ink, railLeft)
            return
        }
        val label = enterText ?: current?.shown(key) ?: key.label
        if (label.isEmpty()) return
        drawLabel(canvas, label, ink, alpha, key.action == KeyAction.SPACE)
        val hint = current?.hint(key)
        if (hint != null && box.height > HINT_MIN_DP * density) {
            hintPaint.typeface = textPaint.typeface
            hintPaint.textSize = textPaint.textSize * HINT_RATIO
            hintPaint.color = ColorMath.withAlpha(ink, HINT_ALPHA * alpha)
            canvas.drawText(hint, rect.right - HINT_PAD_DP * density, rect.top - hintPaint.ascent() + HINT_PAD_DP * density, hintPaint)
        }
    }

    private fun drawShadow(canvas: Canvas, radius: Float, color: Int) {
        val drop = SHADOW_DP * density
        halo.set(rect.left, rect.top + drop, rect.right, rect.bottom + drop)
        glowPaint.color = color
        canvas.drawRoundRect(halo, radius, radius, glowPaint)
    }

    private fun drawLabel(canvas: Canvas, label: String, ink: Int, alpha: Float, quiet: Boolean) {
        val size = textPaint.textSize
        if (quiet) textPaint.textSize = size * SPACE_LABEL_RATIO
        val baseline = -(textPaint.ascent() + textPaint.descent()) / 2f
        textPaint.color = if (quiet) ColorMath.withAlpha(ink, SPACE_LABEL_ALPHA * alpha) else ink
        canvas.drawText(label, rect.centerX(), rect.centerY() + baseline, textPaint)
        textPaint.textSize = size
    }

    private fun railOnLeft(placement: Placement): Boolean {
        val rail = placement.keys.firstOrNull { it.key.action == KeyAction.EXPAND } ?: return false
        val other = placement.keys.firstOrNull { it.slot.row >= 0 } ?: return false
        return rail.cell.centerX < other.cell.centerX
    }

    private fun drawHalo(canvas: Canvas, radius: Float, color: Int) {
        for (layer in HALO_LAYERS downTo 1) {
            val grow = layer * HALO_STEP_DP * density
            halo.set(rect.left - grow, rect.top - grow, rect.right + grow, rect.bottom + grow)
            glowPaint.color = ColorMath.withAlpha(color, HALO_ALPHA * (HALO_LAYERS + 1 - layer))
            canvas.drawRoundRect(halo, radius + grow, radius + grow, glowPaint)
        }
    }

    private companion object {
        const val BASE_TEXT_DP = 22f
        const val MAX_TEXT_RATIO = 0.5f
        const val RADIUS_RATIO = 0.3f
        const val SHADOW_DP = 1.5f
        const val FADED_ALPHA = 0.35f
        const val HALO_LAYERS = 3
        const val HALO_STEP_DP = 3f
        const val HALO_ALPHA = 0.07f
        const val SPACE_LABEL_RATIO = 0.62f
        const val SPACE_LABEL_ALPHA = 0.7f
        const val HINT_RATIO = 0.5f
        const val HINT_ALPHA = 0.6f
        const val HINT_PAD_DP = 3f
        const val HINT_MIN_DP = 30f
    }
}
