package com.spacekeyboard.keyboard.translate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.translate.R
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.TranslateLanguages
import com.spacekeyboard.keyboard.translate.TranslateRoute
import com.spacekeyboard.keyboard.translate.TranslateState
import com.spacekeyboard.keyboard.translate.TranslateStyle

private val MenuShape = RoundedCornerShape(16.dp)
private const val PIN_DIM = 0.45f
private const val SELECTED_FILL = 0.25f

private object BelowAnchor : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val x = if (layoutDirection == LayoutDirection.Rtl) anchorBounds.right - popupContentSize.width else anchorBounds.left
        return IntOffset(x.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0)), anchorBounds.bottom + 6)
    }
}

@Composable
internal fun TranslatePopup(palette: ThemePalette, onDismiss: () -> Unit, width: Dp = 240.dp, content: @Composable () -> Unit) {
    Popup(
        popupPositionProvider = BelowAnchor,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false, dismissOnClickOutside = true, clippingEnabled = false),
    ) {
        Column(
            Modifier.width(width).clip(MenuShape).background(Color(palette.background)).border(1.5.dp, Color(palette.border), MenuShape).padding(6.dp),
        ) { content() }
    }
}

@Composable
internal fun LanguageMenu(state: TranslateState, palette: ThemePalette, controller: TranslateController, onDone: () -> Unit) {
    val locale = LocalConfiguration.current.locales.get(0)
    val pinned = state.pinned
    val recent = state.recent.filter { it !in pinned }
    val rest = TranslateLanguages.ordered.filter { it !in pinned && it !in recent }
    val firstWeak = rest.firstOrNull { !TranslateLanguages.isStrong(it) }
    TranslatePopup(palette, onDone) {
        LazyColumn(Modifier.heightIn(max = 300.dp)) {
            if (pinned.isNotEmpty()) item { Section(stringResource(R.string.translate_pinned), palette) }
            items(pinned.size) { LanguageRow(pinned[it], state, palette, controller, locale, onDone) }
            if (recent.isNotEmpty()) item { Section(stringResource(R.string.translate_recent), palette) }
            items(recent.size) { LanguageRow(recent[it], state, palette, controller, locale, onDone) }
            item { Section(stringResource(R.string.translate_all_languages), palette) }
            items(rest.size) { index ->
                if (rest[index] == firstWeak) Section(stringResource(R.string.translate_other_languages), palette)
                LanguageRow(rest[index], state, palette, controller, locale, onDone)
            }
        }
    }
}

@Composable
private fun Section(text: String, palette: ThemePalette) {
    SpaceText(text, SpaceTheme.typography.bodySmall, Modifier.padding(horizontal = 10.dp, vertical = 6.dp).alpha(PIN_DIM + 0.2f), Color(palette.label))
}

@Composable
private fun LanguageRow(code: String, state: TranslateState, palette: ThemePalette, controller: TranslateController, locale: java.util.Locale, onDone: () -> Unit) {
    val picked = code == state.target
    val pinned = code in state.pinned
    val pinLabel = stringResource(R.string.translate_pin)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(if (picked) Color(palette.glow).copy(alpha = SELECTED_FILL) else Color.Transparent)
            .clickable(role = Role.Button) {
                controller.setTarget(code)
                onDone()
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceText(
            TranslateLanguages.displayName(code, locale) + if (picked) "  ✓" else "",
            SpaceTheme.typography.bodySmall,
            Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 11.dp),
            Color(if (picked) palette.glow else palette.keyText),
            maxLines = 1,
        )
        Box(
            Modifier.semantics { contentDescription = pinLabel }.clickable(role = Role.Checkbox) { controller.togglePin(code) }.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            SpaceText("📌", SpaceTheme.typography.bodySmall, Modifier.alpha(if (pinned) 1f else PIN_DIM))
        }
    }
}

@Composable
internal fun QuickLanguageMenu(state: TranslateState, palette: ThemePalette, controller: TranslateController, onDone: () -> Unit) {
    val locale = LocalConfiguration.current.locales.get(0)
    val codes = (listOf(state.target) + state.recent + state.pinned).distinct().take(4)
    TranslatePopup(palette, onDone, width = 300.dp) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(TranslateGap)) {
            items(codes.size) { index ->
                val code = codes[index]
                TranslateChip(TranslateLanguages.displayName(code, locale), palette, selected = code == state.target) {
                    controller.setTarget(code)
                    onDone()
                }
            }
        }
    }
}

@Composable
internal fun OptionsMenu(
    state: TranslateState,
    palette: ThemePalette,
    controller: TranslateController,
    actions: TranslateActions,
    show: (TranslateView) -> Unit,
    onDone: () -> Unit,
) {
    TranslatePopup(palette, onDone, width = 280.dp) {
        Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(TranslateGap)) {
            Row(horizontalArrangement = Arrangement.spacedBy(TranslateGap)) {
                TranslateChip(stringResource(R.string.translate_route_device), palette, selected = !state.aiReady) {
                    controller.setRoute(TranslateRoute.DEVICE)
                }
                TranslateChip(stringResource(R.string.translate_route_ai), palette, selected = state.aiReady) {
                    if (state.consent) controller.setRoute(TranslateRoute.AI) else show(TranslateView.DISCLOSURE).also { onDone() }
                }
            }
            if (state.aiReady) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(TranslateGap)) {
                    items(TranslateStyle.entries.size) { index ->
                        val style = TranslateStyle.entries[index]
                        TranslateChip(stringResource(TranslateText.style(style)), palette, selected = style == state.style) { controller.setStyle(style) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TranslateGap)) {
                TranslateChip(stringResource(R.string.translate_privacy), palette) {
                    show(TranslateView.DISCLOSURE)
                    onDone()
                }
                if (state.aiReady) TranslateChip(stringResource(R.string.translate_report), palette) {
                    actions.onReport()
                    onDone()
                }
            }
        }
    }
}
