package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
internal fun CustomizeHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackLink(onBack)
        SpaceText(
            text = stringResource(R.string.customize_title),
            style = SpaceTheme.typography.title,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        DoneButton(onBack)
    }
}

@Composable
private fun DoneButton(onClick: () -> Unit) {
    val colors = SpaceTheme.colors
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(colors.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceIconView(SpaceIcon.CHECK, colors.onPrimary, size = 18.dp)
        SpaceText(stringResource(R.string.customize_done), SpaceTheme.typography.label, color = colors.onPrimary)
    }
}

@Composable
internal fun TabIntro(tab: CustomizeTab) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        SpaceText(stringResource(tab.label), SpaceTheme.typography.titleSmall)
        SpaceText(stringResource(tab.hint), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
    }
}
