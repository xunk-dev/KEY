package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.layout.KeyLabels
import com.spacekeyboard.keyboard.core.text.Tashkeel
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
internal fun MarksBar(suggest: SuggestionController, look: ButtonLook, modifier: Modifier = Modifier.fillMaxSize()) {
    val actions = SuggestionActions({ suggest.mark(KeyLabels.output(it)) }, {})
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        SuggestionBar(Tashkeel.LABELS, look.palette, look.style, actions, Modifier.weight(1f).fillMaxHeight())
        MarksChip(true, look, suggest::toggleMarks)
    }
}

@Composable
internal fun MarksChip(open: Boolean, look: ButtonLook, onClick: () -> Unit) {
    val description = stringResource(if (open) R.string.marks_close else R.string.marks_open)
    Box(
        modifier = Modifier.fillMaxHeight().width(40.dp).clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SpaceText(Tashkeel.CHIP, SpaceTheme.typography.body, color = Color(look.palette.label))
    }
}
