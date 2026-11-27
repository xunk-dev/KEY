package com.spacekeyboard.keyboard.designsystem.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.launch

object SpacePressIndication : IndicationNodeFactory {
    private const val PRESSED_SCALE = 0.965f

    override fun create(interactionSource: InteractionSource): DelegatableNode = PressNode(interactionSource, PRESSED_SCALE, 1f)

    override fun equals(other: Any?): Boolean = other === SpacePressIndication

    override fun hashCode(): Int = 7211
}

object SpaceSteadyIndication : IndicationNodeFactory {
    private const val PRESSED_ALPHA = 0.6f

    override fun create(interactionSource: InteractionSource): DelegatableNode = PressNode(interactionSource, 1f, PRESSED_ALPHA)

    override fun equals(other: Any?): Boolean = other === SpaceSteadyIndication

    override fun hashCode(): Int = 7212
}

private class PressNode(
    private val source: InteractionSource,
    private val pressedScale: Float,
    private val pressedAlpha: Float,
) : Modifier.Node(), DrawModifierNode {
    private val press = Animatable(0f)
    private val layerPaint = Paint()

    override fun onAttach() {
        coroutineScope.launch {
            val active = HashSet<PressInteraction.Press>()
            source.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> active.add(interaction)
                    is PressInteraction.Release -> active.remove(interaction.press)
                    is PressInteraction.Cancel -> active.remove(interaction.press)
                    else -> return@collect
                }
                launch {
                    if (active.isNotEmpty()) press.animateTo(1f, tween(110, easing = FastOutSlowInEasing))
                    else press.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val factor = 1f - (1f - pressedScale) * press.value
        val alpha = 1f - (1f - pressedAlpha) * press.value
        when {
            alpha < 1f -> {
                layerPaint.alpha = alpha
                drawContext.canvas.saveLayer(Rect(0f, 0f, size.width, size.height), layerPaint)
                this@draw.drawContent()
                drawContext.canvas.restore()
            }
            factor == 1f -> drawContent()
            else -> scale(factor, factor) { this@draw.drawContent() }
        }
    }
}
