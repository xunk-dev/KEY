package com.spacekeyboard.keyboard.dock

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.R
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.tapToFocus

private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val FieldShape = RoundedCornerShape(20.dp)
private const val SCRIM_ALPHA = 0.5f
private val SheetMinHeight = 280.dp

@Composable
internal fun BoxScope.TrySheet(visible: Boolean, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    BackHandler(enabled = visible, onBack = onDismiss)
    AnimatedVisibility(visible, Modifier.fillMaxSize(), enter = fadeIn(), exit = fadeOut()) {
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = SCRIM_ALPHA)).pointerInput(Unit) { detectTapGestures { onDismiss() } })
    }
    AnimatedVisibility(
        visible = visible,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(spring(0.8f, 400f)) { it },
        exit = slideOutVertically { it },
    ) {
        SheetBody(text) { text = it }
    }
}

@Composable
private fun SheetBody(text: String, onChange: (String) -> Unit) {
    val colors = SpaceTheme.colors
    val title = stringResource(R.string.try_title)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SheetShape)
            .background(colors.surface)
            .border(1.dp, colors.divider, SheetShape)
            .navigationBarsPadding()
            .imePadding()
            .padding(20.dp)
            .heightIn(min = SheetMinHeight)
            .semantics { paneTitle = title },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SpaceText(title, SpaceTheme.typography.title)
        SpaceText(stringResource(R.string.try_hint), SpaceTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        TryField(text, onChange)
    }
}

@Composable
private fun TryField(text: String, onChange: (String) -> Unit) {
    val colors = SpaceTheme.colors
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focus.requestFocus()
        keyboard?.show()
    }
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .clip(FieldShape)
            .background(colors.containerHigh)
            .tapToFocus(focus)
            .padding(16.dp),
    ) {
        if (text.isEmpty()) {
            SpaceText(stringResource(R.string.try_placeholder), SpaceTheme.typography.body, color = colors.onSurfaceVariant)
        }
        BasicTextField(
            value = text,
            onValueChange = onChange,
            textStyle = SpaceTheme.typography.body.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
        )
    }
}
