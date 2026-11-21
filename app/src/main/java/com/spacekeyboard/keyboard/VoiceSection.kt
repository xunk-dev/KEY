package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.VoicePrefs

private val dialects = listOf(
    "ar-SA" to R.string.voice_dialect_sa,
    "ar-EG" to R.string.voice_dialect_eg,
    "ar-AE" to R.string.voice_dialect_ae,
    "ar-KW" to R.string.voice_dialect_kw,
    "ar-QA" to R.string.voice_dialect_qa,
    "ar-IQ" to R.string.voice_dialect_iq,
    "ar-JO" to R.string.voice_dialect_jo,
    "ar-LB" to R.string.voice_dialect_lb,
    "ar-MA" to R.string.voice_dialect_ma,
    "ar-DZ" to R.string.voice_dialect_dz,
    "ar-TN" to R.string.voice_dialect_tn,
)

@Composable
fun VoiceSection() {
    val prefs = remember { VoicePrefs(LocalContext.current) }
    var selected by remember { mutableStateOf(prefs.arabicTag) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpaceText(stringResource(R.string.section_voice), SpaceTheme.typography.title)
        BodyText(R.string.voice_hint)
        ChipGrid(dialects, dialects.first { it.first == selected }, { it.second }) {
            selected = it.first
            prefs.arabicTag = it.first
        }
    }
}
