package com.spacekeyboard.keyboard.emoji.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.emoji.EmojiSnapshot
import com.spacekeyboard.keyboard.emoji.model.Emoji
import com.spacekeyboard.keyboard.emoji.model.SkinTone

private const val SHEET_SCRIM = 0.5f

@Composable
fun EmojiToneSheet(emoji: Emoji, state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = SHEET_SCRIM)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color(palette.keyFill))
                .padding(8.dp)
                .clickable(enabled = false, onClick = {}),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SkinTone.entries.forEach { tone ->
                ToneChoice(emoji.forTone(tone), tone, state, palette) {
                    actions.onTone(tone)
                    actions.onPick(emoji.forTone(tone))
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun ToneChoice(text: String, tone: SkinTone, state: EmojiSnapshot, palette: ThemePalette, onClick: () -> Unit) {
    val label = stringResource(EmojiLabels.tone(tone))
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (tone == state.tone) Color(palette.keyPressed) else Color.Transparent)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        EmojiGlyph(text, state.typeface, 30.dp)
    }
}
