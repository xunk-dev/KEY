package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private const val PREVIEW_MAX = 80
private val SPACES = Regex("""\s+""")

internal fun previewOf(text: String): String = text.trim().replace(SPACES, " ").take(PREVIEW_MAX)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TextOfferChip(offer: ClipTextOffer, palette: ThemePalette, modifier: Modifier = Modifier) {
    val item = offer.offer ?: return
    val fill = ColorMath.mix(palette.keyFill, palette.glow, 0.18f)
    val ink = Color(suggestionInk(palette, KeyStyle.DAY, true, fill))
    val shape = RoundedCornerShape(50)
    val preview = remember(item.id, item.copiedAt) { previewOf(item.text) }
    Box(modifier = modifier.fillMaxHeight().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape)
                .background(Color(fill))
                .border(1.dp, Color(ColorMath.withAlpha(palette.glow, 0.4f)), shape),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxHeight()
                    .combinedClickable(
                        role = Role.Button,
                        onLongClickLabel = stringResource(R.string.copy_offer_hide),
                        onLongClick = offer::dismiss,
                        onClick = offer::paste,
                    )
                    .padding(start = 14.dp, end = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                SpaceText(
                    preview,
                    SpaceTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                    color = ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (offer.canTranslate) TranslateButton(ink, offer::translateCopied)
        }
    }
}

@Composable
private fun TranslateButton(ink: Color, onClick: () -> Unit) {
    val description = stringResource(R.string.copy_offer_translate)
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(start = 6.dp, end = 12.dp),
        contentAlignment = Alignment.Center,
    ) { SpaceIconView(SpaceIcon.TRANSLATE, ink, Modifier, 20.dp) }
}
