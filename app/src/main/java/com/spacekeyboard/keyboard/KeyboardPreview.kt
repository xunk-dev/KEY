package com.spacekeyboard.keyboard

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.spacekeyboard.keyboard.core.layout.SlotRef
import com.spacekeyboard.keyboard.ime.KeyboardView
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.ThemedFrame

@Composable
fun KeyboardPreview(
    theme: ThemeController,
    layouts: LayoutController,
    fixedLayoutId: String? = null,
    editing: Boolean = false,
    onSwap: (SlotRef, SlotRef) -> Unit = { _, _ -> },
    onDragging: (Boolean) -> Unit = {},
) {
    AndroidView(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        factory = { context ->
            ThemedFrame(context, theme, idlePause = false).apply {
                val keys = KeyboardView(context, theme, layouts, fixedLayoutId, editing, onSwap, onDragging = onDragging)
                addView(keys, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            }
        },
    )
}
