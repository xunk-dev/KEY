package com.spacekeyboard.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.KeyboardStatus
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import com.spacekeyboard.keyboard.designsystem.components.SpacePrimaryButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceStatusPill
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.HeroKind
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private class HeroState(val title: Int, val hint: Int?, val action: Int?, val onAction: () -> Unit, val ready: Boolean)

private fun heroState(status: KeyboardStatus, onEnable: () -> Unit, onPick: () -> Unit) = when (status) {
    KeyboardStatus.DISABLED -> HeroState(R.string.status_disabled, null, R.string.action_enable, onEnable, false)
    KeyboardStatus.ENABLED -> HeroState(R.string.status_enabled, null, R.string.action_pick, onPick, false)
    KeyboardStatus.SELECTED -> HeroState(R.string.hero_active, R.string.hero_active_hint, R.string.action_pick, onPick, true)
}

private fun statusMessage(status: KeyboardStatus): Int = when (status) {
    KeyboardStatus.DISABLED -> R.string.status_disabled
    KeyboardStatus.ENABLED -> R.string.status_enabled
    KeyboardStatus.SELECTED -> R.string.status_selected
}

@Composable
internal fun HomeHeader(status: KeyboardStatus) {
    val look = SpaceTheme.look
    Column(
        modifier = Modifier.padding(horizontal = if (look.hero == HeroKind.Card) 0.dp else 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SpaceText(stringResource(R.string.app_name), SpaceTheme.typography.display, Modifier.semantics { heading() })
        val message = stringResource(statusMessage(status))
        val muted = SpaceTheme.colors.onSurfaceVariant
        when (look.hero) {
            HeroKind.Card -> SpaceText(message, SpaceTheme.typography.body, color = muted)
            HeroKind.Plain -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(SpaceTheme.colors.primary, CircleShape))
                SpaceText(message, SpaceTheme.typography.body, color = muted)
            }
            HeroKind.Banner -> Unit
        }
    }
}

@Composable
internal fun HomeHero(status: KeyboardStatus, onEnable: () -> Unit, onPick: () -> Unit) {
    val state = heroState(status, onEnable, onPick)
    when (SpaceTheme.look.hero) {
        HeroKind.Card -> CardHero(state)
        HeroKind.Banner -> BannerHero(state, status)
        HeroKind.Plain -> if (!state.ready) state.action?.let { SpacePrimaryButton(stringResource(it), state.onAction) }
    }
}

@Composable
private fun CardHero(state: HeroState) {
    val look = SpaceTheme.look
    LookCard(Modifier.fillMaxWidth(), radius = look.heroRadius, glow = true) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                HeroIcon(banner = false)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    SpaceText(stringResource(state.title), SpaceTheme.typography.title.copy(fontSize = 17.sp))
                    state.hint?.let { SpaceText(stringResource(it), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant) }
                }
                if (state.ready) SpaceStatusPill(stringResource(R.string.hero_pill_on))
            }
            state.action?.let { SpacePrimaryButton(stringResource(it), state.onAction, icon = SpaceIcon.SETUP) }
        }
    }
}

@Composable
private fun BannerHero(state: HeroState, status: KeyboardStatus) {
    val look = SpaceTheme.look
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(look.heroRadius))
            .background(look.heroFill)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeroIcon(banner = true)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SpaceText(stringResource(statusMessage(status)), SpaceTheme.typography.titleSmall)
            if (state.ready) SpaceText(stringResource(R.string.hero_active), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
        }
        state.action?.let { SpacePrimaryButton(stringResource(if (state.ready) R.string.hero_switch else it), state.onAction, compact = true) }
    }
}

@Composable
private fun HeroIcon(banner: Boolean) {
    val look = SpaceTheme.look
    val shape = if (banner) CircleShape else RoundedCornerShape(15.dp)
    val fill = if (banner) Modifier.background(look.primary) else Modifier.background(SpaceTheme.colors.primary.copy(alpha = 0.2f))
    Box(
        modifier = Modifier.size(if (banner) 52.dp else 48.dp).clip(shape).then(fill),
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(SpaceIcon.KEYS, if (banner) look.onPrimary else SpaceTheme.colors.primary, size = 26.dp)
    }
}
