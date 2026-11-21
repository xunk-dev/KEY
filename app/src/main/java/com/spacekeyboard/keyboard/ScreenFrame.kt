package com.spacekeyboard.keyboard

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.spaceMaxWidth

@Composable
internal fun ScreenFrame(
    title: Int,
    onBack: (() -> Unit)?,
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onBack != null) BackHandler(onBack = onBack)
    AppTheme {
        val look = SpaceTheme.look
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(look.canvas)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState(), enabled = scrollable)
                .spaceMaxWidth()
                .padding(look.screenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (onBack != null) {
                BackLink(onBack)
            }
            SpaceText(stringResource(title), SpaceTheme.typography.display, Modifier.semantics { heading() })
            content()
        }
    }
}

@Composable
internal fun BodyText(res: Int) {
    SpaceText(stringResource(res), SpaceTheme.typography.body, color = SpaceTheme.colors.onSurfaceVariant)
}

@Composable
internal fun BackLink(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable(role = Role.Button, onClick = onBack)
            .minTouchTarget(),
        contentAlignment = Alignment.CenterStart,
    ) {
        SpaceText(
            text = stringResource(R.string.themes_back),
            style = SpaceTheme.typography.titleSmall,
            color = SpaceTheme.colors.primary,
        )
    }
}

internal fun Modifier.minTouchTarget(): Modifier = sizeIn(minWidth = 48.dp, minHeight = 48.dp).wrapContentSize(Alignment.Center)
