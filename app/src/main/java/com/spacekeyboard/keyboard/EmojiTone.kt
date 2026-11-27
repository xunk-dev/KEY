package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import androidx.compose.foundation.selection.selectable
import android.graphics.Typeface
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.model.SkinTone
import com.spacekeyboard.keyboard.emoji.ui.EmojiGlyph
import com.spacekeyboard.keyboard.emoji.ui.EmojiLabels

private const val WAVING_HAND = 0x1F44B

@Composable
fun ToneRow(current: SkinTone, typeface: Typeface?, onSelect: (SkinTone) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        SkinTone.entries.forEach { tone ->
            val shape = RoundedCornerShape(12.dp)
            val outline = if (tone == current) SpaceTheme.colors.primary else SpaceTheme.colors.outline
            val description = stringResource(EmojiLabels.tone(tone))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape)
                    .border(if (tone == current) 2.dp else 1.dp, outline, shape)
                    .semantics { contentDescription = description }
                    .selectable(selected = tone == current, role = Role.RadioButton) { onSelect(tone) },
                contentAlignment = Alignment.Center,
            ) {
                val modifier = if (tone == SkinTone.NONE) "" else String(Character.toChars(tone.modifier))
                EmojiGlyph(String(Character.toChars(WAVING_HAND)) + modifier, typeface, 26.dp)
            }
        }
    }
}

@Composable
fun RecentsSection(count: Int, onClear: () -> Unit) {
    var asking by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SpaceSectionTitle(stringResource(R.string.emoji_recents_title))
        when {
            count == 0 -> SpaceText(
                stringResource(R.string.emoji_recents_none),
                SpaceTheme.typography.bodySmall,
                color = SpaceTheme.colors.onSurfaceVariant,
            )
            asking -> {
                SpaceText(stringResource(R.string.emoji_settings_clear_ask), SpaceTheme.typography.body)
                SpaceButton(stringResource(R.string.emoji_settings_clear_yes), { asking = false; onClear() })
                SpaceButton(stringResource(R.string.emoji_settings_clear_no), { asking = false })
            }
            else -> SpaceButton(stringResource(R.string.emoji_clear_recents), { asking = true })
        }
    }
}
