package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.ToolbarItem
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import kotlin.math.roundToInt

private val ChipGap = 8.dp

@Composable
fun ToolbarEditor(items: List<ToolbarItem>, onMove: (Int, Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ChipGap)) {
        items.forEachIndexed { index, item ->
            key(item) { ToolbarChip(item, index, items.size, onMove) }
        }
    }
}

@Composable
private fun ToolbarChip(item: ToolbarItem, index: Int, count: Int, onMove: (Int, Int) -> Unit) {
    var shift by remember { mutableFloatStateOf(0f) }
    val mirrored = LocalLayoutDirection.current == LayoutDirection.Rtl
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .absoluteOffset { IntOffset(shift.roundToInt(), 0) }
            .clip(shape)
            .background(SpaceTheme.colors.containerHigh)
            .border(1.dp, SpaceTheme.colors.primary, shape)
            .pointerInput(index, count, mirrored) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val pitch = size.width + ChipGap.toPx()
                        val steps = (shift / pitch).roundToInt() * if (mirrored) -1 else 1
                        shift = 0f
                        onMove(index, (index + steps).coerceIn(0, count - 1))
                    },
                    onDragCancel = { shift = 0f },
                ) { change, amount ->
                    change.consume()
                    shift += amount
                }
            }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        SpaceText(stringResource(item.labelRes), SpaceTheme.typography.bodySmall)
    }
}
