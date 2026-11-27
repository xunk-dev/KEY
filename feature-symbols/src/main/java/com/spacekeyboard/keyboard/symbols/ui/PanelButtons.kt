package com.spacekeyboard.keyboard.symbols.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
internal fun rememberTap(): () -> Unit {
    val view = LocalView.current
    return remember(view) {
        val action: () -> Unit = { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP) }
        action
    }
}

@Composable
internal fun PanelButton(text: String, palette: ThemePalette, selected: Boolean, onClick: () -> Unit) {
    val fill = if (selected) palette.keyPressed else palette.keyFill
    val ink = if (selected) palette.keyTextPressed else palette.keyText
    val tap = rememberTap()
    Box(
        modifier = Modifier
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(fill))
            .clickable(role = Role.Button) {
                tap()
                onClick()
            }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        SpaceText(text = text, style = SpaceTheme.typography.bodySmall, color = Color(ink))
    }
}
