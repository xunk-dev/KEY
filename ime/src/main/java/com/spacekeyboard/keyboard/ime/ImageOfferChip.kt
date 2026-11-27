package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ImageOfferChip(offer: ImageOffer, palette: ThemePalette, onPaste: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val fill = ColorMath.mix(palette.keyFill, palette.glow, 0.18f)
    val ink = suggestionInk(palette, KeyStyle.DAY, true, fill)
    val shape = RoundedCornerShape(50)
    val label = stringResource(R.string.clip_image_offer)
    Box(modifier = modifier.fillMaxHeight().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape)
                .background(Color(fill))
                .border(1.dp, Color(ColorMath.withAlpha(palette.glow, 0.4f)), shape)
                .combinedClickable(
                    role = Role.Button,
                    onLongClickLabel = stringResource(R.string.clip_image_dismiss),
                    onLongClick = onDismiss,
                    onClick = onPaste,
                )
                .padding(start = 6.dp, end = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                bitmap = offer.thumb,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)),
            )
            SpaceText(label, SpaceTheme.typography.body.copy(fontWeight = FontWeight.SemiBold), color = Color(ink))
        }
    }
}
