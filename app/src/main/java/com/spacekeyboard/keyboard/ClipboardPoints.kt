package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private val points = listOf(
    "📱" to R.string.clipboard_point_local,
    "🔒" to R.string.clipboard_point_encrypted,
    "⏱️" to R.string.clipboard_point_expiry,
    "🔑" to R.string.clipboard_point_sensitive,
    "⏸️" to R.string.clipboard_point_off,
)

@Composable
internal fun ClipboardPoints() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpaceText(stringResource(R.string.clipboard_intro), SpaceTheme.typography.body)
        SpaceText(
            text = stringResource(R.string.clipboard_points_title),
            style = SpaceTheme.typography.title,
            modifier = Modifier.semantics { heading() },
        )
        points.forEach { (icon, text) -> PointRow(icon, text) }
    }
}

@Composable
private fun PointRow(icon: String, text: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceText(
            text = icon,
            style = SpaceTheme.typography.title.copy(textAlign = TextAlign.Center),
            modifier = Modifier.width(32.dp).clearAndSetSemantics {},
        )
        SpaceText(stringResource(text), SpaceTheme.typography.bodySmall, Modifier.weight(1f), color = SpaceTheme.colors.onSurfaceVariant)
    }
}
