package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.components.LookCard
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable

private val notes = listOf(
    R.string.privacy_typing,
    R.string.privacy_passwords,
    R.string.privacy_network,
    R.string.privacy_voice,
    R.string.privacy_auto_language,
    R.string.privacy_apps,
    R.string.privacy_backup,
    R.string.privacy_delete,
)

@Composable
internal fun PrivacyScreen(onBack: () -> Unit) {
    ScreenFrame(R.string.privacy_title, onBack) {
        LookCard(Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                notes.forEach { BodyText(it) }
            }
        }
    }
}
