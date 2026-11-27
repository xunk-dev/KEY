package com.spacekeyboard.keyboard.dock

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer

@Stable
internal class Backdrop(val layer: GraphicsLayer)

internal val blurSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

private const val GLASS_SATURATION = 1.9f
private const val GLASS_BRIGHTNESS = 1.06f

@androidx.annotation.RequiresApi(Build.VERSION_CODES.S)
private fun glassEffect(radiusPx: Float): androidx.compose.ui.graphics.RenderEffect {
    val colors = ColorMatrix().apply { setSaturation(GLASS_SATURATION) }
    val lift = ColorMatrix(
        floatArrayOf(
            GLASS_BRIGHTNESS, 0f, 0f, 0f, 0f,
            0f, GLASS_BRIGHTNESS, 0f, 0f, 0f,
            0f, 0f, GLASS_BRIGHTNESS, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        ),
    )
    colors.postConcat(lift)
    val blur = RenderEffect.createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
    return RenderEffect.createColorFilterEffect(ColorMatrixColorFilter(colors), blur).asComposeRenderEffect()
}

internal fun Modifier.recordBackdrop(backdrop: Backdrop): Modifier = drawWithContent {
    backdrop.layer.record { this@drawWithContent.drawContent() }
    drawLayer(backdrop.layer)
}

internal fun Modifier.backdropBlur(backdrop: Backdrop, origin: () -> Offset, radiusPx: Float): Modifier =
    graphicsLayer {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) renderEffect = glassEffect(radiusPx)
    }.drawBehind {
        val shift = origin()
        translate(-shift.x, -shift.y) { drawLayer(backdrop.layer) }
    }
