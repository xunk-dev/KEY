package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.spacekeyboard.keyboard.core.layout.Box
import com.spacekeyboard.keyboard.core.layout.KeyAction
import com.spacekeyboard.keyboard.core.layout.LayoutCatalog
import com.spacekeyboard.keyboard.core.layout.KeySpec
import com.spacekeyboard.keyboard.core.layout.PlacedKey
import com.spacekeyboard.keyboard.core.layout.Placement
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.ime.layout.DragState
import com.spacekeyboard.keyboard.ime.layout.LabelTypefaces
import com.spacekeyboard.keyboard.ime.layout.LayoutSnapshot
import com.spacekeyboard.keyboard.ime.layout.Shift

internal class KeyPainter(context: Context) {
    private val density = context.resources.displayMetrics.density
    private val typefaces = LabelTypefaces(context)
    private val icons = KeyIcons()
    private var look = KeyStyle.GALAXY
    private var railLeft = false
    private var baseSize = 0f
    private var letterScale = 1f
    private var functionScale = 1f
    private var iconUnit = 0f
    private var squareSide = 0f
    private var squareWidthLimit = 0f
    private val rect = RectF()
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val effects = KeyEffects(density)
    private val hints = HintPainter(density)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = density
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private var layout: LayoutSnapshot? = null
    private val micAreas = ArrayList<RectF>()
    private var pressedIds: Set<String> = emptySet()

    fun micAt(x: Float, y: Float): Boolean = micAreas.any { it.contains(x, y) }

    fun draw(
        canvas: Canvas,
        placement: Placement,
        palette: ThemePalette,
        keyStyle: KeyStyle,
        snapshot: LayoutSnapshot,
        pressed: Collection<PlacedKey>,
        drag: DragState?,
        rail: SymbolRail? = null,
    ) {
        look = keyStyle
        layout = snapshot
        micAreas.clear()
        pressedIds = pressed.mapTo(HashSet()) { it.key.id }
        val prefs = snapshot.prefs
        textPaint.typeface = typefaces.resolve(prefs.label)
        val height = placement.keys.firstOrNull()?.box?.height ?: return
        baseSize = minOf(BASE_TEXT_DP * density * prefs.labelScale(), height * MAX_TEXT_RATIO)
        textPaint.textSize = baseSize
        measureSizes(placement, snapshot)
        iconUnit = iconUnitOf(placement, icons)
        measureSquares(placement)
        railLeft = railOnLeft(placement)
        val dragging = drag?.takeIf { it.active }
        val labelled = spaceLabelOwners(placement)
        placement.keys.forEach { placed ->
            if (placed.key.action == KeyAction.RAIL) return@forEach
            val lit = placed in pressed || snapshot.isActive(placed.key) || (dragging != null && placed.slot == dragging.target?.slot)
            val faded = dragging != null && placed.slot == dragging.from.slot
            val unit = if (placed.slot.row < 0) minOf(placed.box.width, placed.box.height) else iconUnit
            drawKey(canvas, placed.box, placed.key, palette, lit, if (faded) FADED_ALPHA else 1f, placed in labelled, unit)
        }
        if (rail != null && rail.active) drawRail(canvas, rail, palette)
        if (dragging != null) drawLifted(canvas, dragging, palette)
    }

    private fun drawRail(canvas: Canvas, rail: SymbolRail, palette: ThemePalette) {
        val area = rail.clip() ?: return
        val saved = canvas.save()
        canvas.clipRect(area)
        rail.entries().forEach { placed ->
            val lit = pressedIds.contains(placed.key.id)
            drawKey(canvas, placed.box, placed.key.copy(action = KeyAction.RAIL), palette, lit, 1f, false, iconUnit)
        }
        rail.thumb(THUMB_DP * density, THUMB_INSET_DP * density)?.let { thumb ->
            fillPaint.color = ColorMath.withAlpha(palette.keyText, THUMB_ALPHA)
            canvas.drawRoundRect(thumb, thumb.width() / 2f, thumb.width() / 2f, fillPaint)
        }
        canvas.restoreToCount(saved)
    }

    private fun drawLifted(canvas: Canvas, drag: DragState, palette: ThemePalette) {
        val size = drag.from.box
        val lifted = Box(
            drag.x - size.width / 2f,
            drag.y - size.height / 2f,
            drag.x + size.width / 2f,
            drag.y + size.height / 2f,
        )
        drawKey(canvas, lifted, drag.from.key, palette, true, 1f, true, iconUnit)
    }

    private fun roundEnter(key: KeySpec) = key.action == KeyAction.ENTER && look != KeyStyle.GLASS

    private fun squareKind(key: KeySpec) = key.action == KeyAction.OPEN_SYMBOLS || key.action == KeyAction.NUMBERS

    // Symbols, 123 and a round Enter share one side, so they read as one family on every page and in every
    // language. A key that spans two columns (ABC on the symbols page) is not a square: it fills its cell.
    private fun measureSquares(placement: Placement) {
        val rows = placement.keys.filter { it.slot.row >= 0 }
        val widths = rows.filter { it.key.action == KeyAction.COMMIT }.map { it.box.width }.sorted()
        val letter = widths.getOrNull(widths.size / 2) ?: 0f
        squareWidthLimit = if (letter > 0f) letter * WIDE_RATIO else Float.MAX_VALUE
        squareSide = rows
            .filter { (squareKind(it.key) || roundEnter(it.key)) && it.box.width <= squareWidthLimit }
            .minOfOrNull { minOf(it.box.width, it.box.height) } ?: 0f
    }

    private fun drawKey(canvas: Canvas, box: Box, key: KeySpec, palette: ThemePalette, lit: Boolean, alpha: Float, labelled: Boolean, unit: Float) {
        val round = roundEnter(key)
        val wide = box.width > squareWidthLimit
        val squared = squareKind(key) && !wide
        if ((round || squared) && squareSide > 0f) {
            val side = minOf(squareSide, box.width, box.height)
            rect.set(box.centerX - side / 2f, box.centerY - side / 2f, box.centerX + side / 2f, box.centerY + side / 2f)
        } else if (round || squared) {
            val side = minOf(box.width, box.height)
            rect.set(box.centerX - side / 2f, box.centerY - side / 2f, box.centerX + side / 2f, box.centerY + side / 2f)
        } else {
            rect.set(box.left, box.top, box.right, box.bottom)
        }
        val locked = key.action == KeyAction.SHIFT && layout?.shift == Shift.LOCKED
        val padKey = layout?.activeId == LayoutCatalog.numbers.id && key.action == KeyAction.COMMIT
        val padDigit = padKey && key.label.length == 1 && key.label[0].isDigit()
        val role = when {
            locked -> KeyRole.PRIMARY
            padKey && !padDigit -> KeyRole.CHIP
            else -> keyRole(key.action)
        }
        val base = KeySkins.of(look, role, palette, lit && !locked)
        val skin = if (padDigit && !lit) base.lifted(palette.keyText, DIGIT_LIFT) else base
        val radius = if (skin.pill || round) rect.height() / 2f else if (squared) rect.height() * SQUARE_RADIUS_RATIO else minOf(skin.radiusDp * density, box.height * RADIUS_RATIO)
        if (skin.halo) effects.halo(canvas, rect, radius, palette.glow)
        if (skin.shadow != 0) effects.shadow(canvas, rect, radius, fade(skin.shadow, alpha))
        paintFill(canvas, skin, radius, alpha)
        val edge = skin.border
        if (edge != 0) {
            borderPaint.color = fade(edge, alpha)
            canvas.drawRoundRect(rect, radius, radius, borderPaint)
        }
        if (round) effects.orbit(canvas, rect, fade(skin.ink, alpha))
        drawContent(canvas, box, key, fade(skin.ink, alpha), fade(skin.accent, alpha), alpha, labelled, unit)
    }

    private fun paintFill(canvas: Canvas, skin: KeySkin, radius: Float, alpha: Float) {
        val gradient = skin.gradient
        if (gradient == null) {
            fillPaint.color = fade(skin.fill, alpha)
        } else {
            val endX = if (gradient.diagonal) rect.right else rect.left
            fillPaint.color = ColorMath.WHITE
            fillPaint.shader = LinearGradient(rect.left, rect.top, endX, rect.bottom, fade(gradient.from, alpha), fade(gradient.to, alpha), Shader.TileMode.CLAMP)
        }
        canvas.drawRoundRect(rect, radius, radius, fillPaint)
        fillPaint.shader = null
    }

    private fun drawContent(canvas: Canvas, box: Box, key: KeySpec, ink: Int, accent: Int, alpha: Float, labelled: Boolean, unit: Float) {
        val current = layout
        if (key.action == KeyAction.SHIFT) {
            icons.drawShift(canvas, current?.shift ?: Shift.OFF, box, ink, accent, unit)
            return
        }
        if (icons.handles(key.action)) {
            icons.draw(canvas, key.action, box, ink, accent, railLeft, unit, LayoutCatalog.isRtl(current?.language.orEmpty()), current?.enterKind ?: EnterKind.ENTER, pagePlaceOf(current?.activeId))
            if (key.action == KeyAction.ENTER && current?.enterKind != EnterKind.ENTER) hints.newline(canvas, box, ink, unit)
            return
        }
        if (key.action == KeyAction.SPACE && !labelled) return
        val label = current?.shown(key) ?: key.label
        if (label.isEmpty() && key.action != KeyAction.SPACE) return
        if (key.pair) {
            drawPair(canvas, key.letters, ink, alpha, box)
            return
        }
        if (key.action == KeyAction.SPACE) {
            drawSpaceContent(canvas, label, ink, accent, alpha, box.width)
            return
        }
        val padDigit = current?.activeId == LayoutCatalog.numbers.id && key.action == KeyAction.COMMIT && label.length == 1 && label[0].isDigit()
        val size = sizeFor(key, label) * if (padDigit) DIGIT_SCALE else 1f
        val fitted = drawLabel(canvas, label, ink, alpha, size, box.width, rect.centerX())
        val hint = current?.hint(key)
        if (hint != null && box.height > HINT_MIN_DP * density) hints.draw(canvas, rect, textPaint, baseSize, hint, label, fitted, if (padDigit) accent else ink, alpha, box)
    }

    private fun sizeFor(key: KeySpec, label: String): Float = when {
        key.action == KeyAction.RAIL -> baseSize * RAIL_RATIO
        isLetterKey(key, label) -> baseSize * letterScale
        key.action == KeyAction.COMMIT -> baseSize
        else -> baseSize * FUNCTION_RATIO * functionScale
    }

    private fun measureSizes(placement: Placement, snapshot: LayoutSnapshot) {
        letterScale = 1f
        functionScale = 1f
        placement.keys.forEach { placed ->
            val key = placed.key
            if (placed.slot.row < 0 || key.pair || key.action == KeyAction.SPACE || key.action == KeyAction.SHIFT || icons.handles(key.action)) return@forEach
            val label = snapshot.shown(key)
            if (label.isEmpty()) return@forEach
            val room = placed.box.width * FIT_RATIO
            when {
                isLetterKey(key, label) -> letterScale = minOf(letterScale, fitOf(label, baseSize, room))
                key.action != KeyAction.COMMIT -> functionScale = minOf(functionScale, fitOf(label, baseSize * FUNCTION_RATIO, room))
            }
        }
        textPaint.textSize = baseSize
    }

    private fun fitOf(label: String, size: Float, room: Float): Float {
        textPaint.textSize = size
        val wide = textPaint.measureText(label)
        return if (wide > room && wide > 0f) room / wide else 1f
    }

    private fun drawPair(canvas: Canvas, letters: List<String>, ink: Int, alpha: Float, box: Box) {
        val quarter = box.width / 4f
        letters.forEachIndexed { i, letter ->
            val centre = rect.centerX() + if (i == 0) -quarter else quarter
            drawLabel(canvas, letter, ink, alpha, baseSize, box.width / 2f, centre)
        }
        borderPaint.color = ColorMath.withAlpha(ink, PAIR_DIVIDER_ALPHA * alpha)
        canvas.drawLine(rect.centerX(), rect.top + rect.height() * 0.28f, rect.centerX(), rect.bottom - rect.height() * 0.28f, borderPaint)
    }

    private fun drawSpaceContent(canvas: Canvas, label: String, ink: Int, accent: Int, alpha: Float, keyWidth: Float) {
        effects.ruler(canvas, rect, ink, alpha)
        val size = baseSize * SPACE_LABEL_RATIO
        val micSize = size * MIC_RATIO
        val inset = MIC_INSET_DP * density
        val color = ColorMath.withAlpha(ink, SPACE_LABEL_ALPHA * alpha)
        val micFits = keyWidth >= micSize + 2f * inset
        if (micFits) {
            val centre = rect.left + inset + micSize / 2f
            icons.drawIcon(canvas, SpaceIcon.VOICE, centre, rect.centerY(), micSize, color, accent)
            val half = maxOf(micSize, MIC_TAP_DP * density) / 2f
            micAreas += RectF(centre - half, rect.top, centre + half, rect.bottom)
        }
        val clear = if (micFits) inset + micSize + MIC_GAP_DP * density else 0f
        val room = 2f * (keyWidth / 2f - clear)
        if (room <= 0f || label.isEmpty()) return
        var fitted = size
        textPaint.textSize = fitted
        val wide = textPaint.measureText(label)
        if (wide > room) {
            fitted *= room / wide
            textPaint.textSize = fitted
        }
        if (fitted >= MIN_LABEL_DP * density) {
            val baseline = -(textPaint.ascent() + textPaint.descent()) / 2f
            textPaint.color = color
            canvas.drawText(label, rect.centerX(), rect.centerY() + baseline, textPaint)
        }
        textPaint.textSize = baseSize
    }

    private fun drawLabel(canvas: Canvas, label: String, ink: Int, alpha: Float, size: Float, keyWidth: Float, centerX: Float): Float {
        var fitted = size
        textPaint.textSize = fitted
        val room = keyWidth * FIT_RATIO
        val wide = textPaint.measureText(label)
        if (wide > room && wide > 0f) {
            fitted *= room / wide
            textPaint.textSize = fitted
        }
        val baseline = -(textPaint.ascent() + textPaint.descent()) / 2f
        textPaint.color = ink
        canvas.drawText(label, centerX, rect.centerY() + baseline, textPaint)
        textPaint.textSize = baseSize
        return fitted
    }

    private companion object {
        const val BASE_TEXT_DP = 22f
        const val MAX_TEXT_RATIO = 0.5f
        const val RADIUS_RATIO = 0.32f
        const val SQUARE_RADIUS_RATIO = 0.3f
        const val WIDE_RATIO = 1.8f
        const val PAIR_DIVIDER_ALPHA = 0.18f
        const val FADED_ALPHA = 0.35f
        const val SPACE_LABEL_RATIO = 0.62f
        const val SPACE_LABEL_ALPHA = 0.85f
        const val HINT_MIN_DP = 30f
        const val FIT_RATIO = 0.8f
        const val MIC_RATIO = 1.5f
        const val MIC_GAP_DP = 6f
        const val MIN_LABEL_DP = 7f
        const val MIC_TAP_DP = 44f
        const val FUNCTION_RATIO = 0.7f
        const val RAIL_RATIO = 0.9f
        const val DIGIT_SCALE = 1.2f
        const val DIGIT_LIFT = 0.1f
        const val THUMB_DP = 2.5f
        const val THUMB_INSET_DP = 6f
        const val THUMB_ALPHA = 0.35f
        const val MIC_INSET_DP = 10f
    }
}
