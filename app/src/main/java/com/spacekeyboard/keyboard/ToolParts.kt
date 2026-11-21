package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
internal fun ToolSection(title: Int, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SpaceSectionTitle(stringResource(title))
        content()
    }
}

@Composable
internal fun ToolField(content: @Composable () -> Unit) {
    val look = SpaceTheme.look
    val shape = RoundedCornerShape(look.fieldRadius.coerceAtMost(18.dp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(look.field)
            .border(look.fieldEdgeWidth, look.fieldEdge, shape),
    ) {
        content()
    }
}

@Composable
internal fun ConfirmAction(ask: Int, yes: Int, no: Int, action: Int, onConfirm: () -> Unit) {
    var asking by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (asking) {
            SpaceText(stringResource(ask), SpaceTheme.typography.body)
            SpaceButton(stringResource(yes), { asking = false; onConfirm() })
            SpaceButton(stringResource(no), { asking = false })
        } else {
            SpaceButton(stringResource(action), { asking = true })
        }
    }
}
