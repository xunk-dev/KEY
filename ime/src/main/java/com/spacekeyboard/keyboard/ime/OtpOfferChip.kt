package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun OtpOfferChip(offer: OtpOffer, palette: ThemePalette, modifier: Modifier = Modifier) {
    val item = offer.code ?: return
    val fill = ColorMath.mix(palette.keyFill, palette.glow, 0.18f)
    val ink = Color(suggestionInk(palette, KeyStyle.DAY, true, fill))
    val shape = RoundedCornerShape(50)
    val action = stringResource(R.string.otp_fill_action, item.spoken())
    Box(modifier = modifier.fillMaxHeight().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .clip(shape)
                .background(Color(fill))
                .border(1.dp, Color(ColorMath.withAlpha(palette.glow, 0.4f)), shape)
                .combinedClickable(
                    role = Role.Button,
                    onLongClickLabel = stringResource(R.string.otp_hide),
                    onLongClick = offer::dismiss,
                    onClick = offer::use,
                )
                .semantics { contentDescription = action }
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpaceIconView(SpaceIcon.KIND_TOKEN, ink, Modifier, 18.dp)
            SpaceText(
                item.shown(),
                SpaceTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                color = ink,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
