package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
internal fun AutoFixExample() {
    LookCard(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SpaceSectionTitle(stringResource(R.string.autofix_example_title))
            SpaceText(stringResource(R.string.autofix_example_step1), SpaceTheme.typography.body)
            SpaceText(stringResource(R.string.autofix_example_step2), SpaceTheme.typography.body)
            SpaceText(stringResource(R.string.autofix_example_step3), SpaceTheme.typography.body)
        }
    }
}
