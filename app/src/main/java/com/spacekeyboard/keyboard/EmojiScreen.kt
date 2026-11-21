package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.data.EmojiSet
import com.spacekeyboard.keyboard.emoji.data.EmojiTypefaces
import com.spacekeyboard.keyboard.emoji.rememberEmojiSnapshot
import com.spacekeyboard.keyboard.emoji.ui.EmojiGlyph

private val OLD_SAMPLE = emojiOf(0x1F600, 0x1F60D, 0x1F973, 0x1F60E, 0x1F525, 0x1F680, 0x1F319, 0x1F431)
private val NEW_SAMPLE = emojiOf(0x1FAE0, 0x1FAE8, 0x1FAE9, 0x1FAC6, 0x1FADC, 0x1FABE)

fun emojiOf(vararg codePoints: Int): String = codePoints.joinToString("") { String(Character.toChars(it)) }

@Composable
fun EmojiScreen(controller: EmojiController, onBack: () -> Unit) {
    val state = rememberEmojiSnapshot(controller)
    val context = LocalContext.current
    val typeface = remember(state.set) { EmojiTypefaces.load(context, state.set) }
    ScreenFrame(R.string.emoji_title, onBack) {
        StyleSection(state.set, controller::setSet)
        SpaceSectionTitle(stringResource(R.string.emoji_tone_title))
        ToneRow(state.tone, typeface, controller::setTone)
        RecentsSection(state.recents.size, controller::clearRecents)
        Note(R.string.emoji_other_apps)
        Note(R.string.emoji_license)
    }
}

@Composable
private fun Note(res: Int) {
    SpaceText(stringResource(res), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
}

@Composable
private fun StyleSection(current: EmojiSet, onSelect: (EmojiSet) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SpaceSectionTitle(stringResource(R.string.emoji_style_title))
        Note(R.string.emoji_style_note)
        EmojiSet.entries.forEach { set -> SetCard(set, set == current) { onSelect(set) } }
    }
}

@Composable
private fun SetCard(set: EmojiSet, selected: Boolean, onSelect: () -> Unit) {
    val context = LocalContext.current
    val typeface = remember(set) { EmojiTypefaces.load(context, set) }
    val shape = RoundedCornerShape(SpaceTheme.look.cardRadius.coerceAtMost(18.dp))
    val outline = if (selected) SpaceTheme.colors.primary else SpaceTheme.colors.outline
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SpaceTheme.look.card)
            .border(if (selected) 2.dp else 1.dp, outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SpaceText(stringResource(set.labelRes), SpaceTheme.typography.titleSmall)
        EmojiGlyph(OLD_SAMPLE, typeface, 26.dp)
        EmojiGlyph(NEW_SAMPLE, typeface, 26.dp)
    }
}
