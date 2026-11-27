package com.spacekeyboard.keyboard.translate.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.rememberSpaceHaptics
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.translate.R
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.TranslateFailure
import com.spacekeyboard.keyboard.translate.TranslateLanguages
import com.spacekeyboard.keyboard.translate.TranslatePolicy
import com.spacekeyboard.keyboard.translate.TranslateRoute
import com.spacekeyboard.keyboard.translate.TranslateState
import com.spacekeyboard.keyboard.translate.TranslateStatus

private const val DIM_ALPHA = 0.7f
private const val SHOWN_TAIL = 48
private const val CARD_EDGE_ALPHA = 0.10f
private const val TAG_FILL_ALPHA = 0.18f
private const val CARD_LIFT = 0.035f
private const val CARD_SINK = 0.17f
private const val ORB_TINT = 0.2f
private const val ORB_INK = 0xFF0A1330.toInt()

private enum class TranslateMenu { NONE, LANGUAGES, QUICK, OPTIONS }

private val CardShape = RoundedCornerShape(16.dp)
private val TagShape = RoundedCornerShape(7.dp)
private val CardMin = 56.dp
private val RailWidth = 36.dp
private val RailButtonHeight = 28.dp
private val LineMin = 22.dp
private val OrbSize = 36.dp
private val BarHeight = 30.dp

@Composable
internal fun TranslateMain(
    state: TranslateState,
    palette: ThemePalette,
    controller: TranslateController,
    actions: TranslateActions,
    show: (TranslateView) -> Unit,
    compact: Boolean,
) {
    var menu by remember { mutableStateOf(TranslateMenu.NONE) }
    val close = { menu = TranslateMenu.NONE }
    val frame = if (compact) Modifier.fillMaxSize() else Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    Row(
        frame.padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = if (compact) Alignment.CenterVertically else Alignment.Top,
    ) {
        Column(Modifier.width(RailWidth), horizontalAlignment = Alignment.CenterHorizontally) {
            RailButton(SpaceIcon.CLOSE, palette, stringResource(R.string.translate_close), actions::onKeys)
            Box {
                RailButton(SpaceIcon.MORE, palette, stringResource(R.string.translate_options)) {
                    menu = if (menu == TranslateMenu.OPTIONS) TranslateMenu.NONE else TranslateMenu.OPTIONS
                }
                if (menu == TranslateMenu.OPTIONS) OptionsMenu(state, palette, controller, actions, show, close)
            }
        }
        TranslateCard(state, palette, controller, actions, compact, menu, { menu = it }, Modifier.weight(1f))
    }
}

@Composable
private fun RailButton(icon: SpaceIcon, palette: ThemePalette, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(RailWidth, RailButtonHeight).clip(CircleShape).semantics { contentDescription = description }.clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(icon, Color(palette.label).copy(alpha = DIM_ALPHA), Modifier, size = 18.dp)
    }
}

@Composable
private fun TranslateCard(
    state: TranslateState,
    palette: ThemePalette,
    controller: TranslateController,
    actions: TranslateActions,
    compact: Boolean,
    menu: TranslateMenu,
    setMenu: (TranslateMenu) -> Unit,
    modifier: Modifier,
) {
    val locale = LocalConfiguration.current.locales.get(0)
    val flat = state.source.replace('\n', ' ')
    val shown = if (flat.length > SHOWN_TAIL) "\u2026" + flat.takeLast(SHOWN_TAIL) else flat
    val done = state.status == TranslateStatus.DONE && state.source.isNotBlank()
    val surface = Brush.verticalGradient(
        listOf(Color(ColorMath.mix(palette.keyFill, ColorMath.WHITE, CARD_LIFT)), Color(ColorMath.mix(palette.keyFill, ColorMath.BLACK, CARD_SINK))),
    )
    val bar = Brush.verticalGradient(
        listOf(Color(ColorMath.mix(palette.glow, ColorMath.WHITE, 0.4f)), Color(palette.glow), Color(ColorMath.mix(palette.glow, ColorMath.BLACK, 0.25f))),
    )
    Row(
        modifier.heightIn(min = CardMin).clip(CardShape).background(surface).border(1.dp, Color(palette.glow).copy(alpha = CARD_EDGE_ALPHA), CardShape)
            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(3.dp).height(BarHeight).clip(RoundedCornerShape(2.dp)).background(bar))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(Modifier.heightIn(min = LineMin), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                LangTag("AUTO", palette, false, stringResource(R.string.translate_auto), null, null)
                SpaceText(
                    if (flat.isEmpty()) stringResource(R.string.translate_input_hint) else shown,
                    SpaceTheme.typography.bodySmall,
                    Modifier.weight(1f),
                    Color(palette.label).copy(alpha = if (flat.isEmpty()) DIM_ALPHA else 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(Modifier.heightIn(min = LineMin), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box {
                    LangTag(
                        state.target.uppercase() + " \u25BE", palette, true, TranslateLanguages.displayName(state.target, locale),
                        { setMenu(if (menu == TranslateMenu.LANGUAGES) TranslateMenu.NONE else TranslateMenu.LANGUAGES) },
                        { setMenu(TranslateMenu.QUICK) },
                    )
                    val close = { setMenu(TranslateMenu.NONE) }
                    when (menu) {
                        TranslateMenu.LANGUAGES -> LanguageMenu(state, palette, controller, close)
                        TranslateMenu.QUICK -> QuickLanguageMenu(state, palette, controller, close)
                        else -> Unit
                    }
                }
                SpaceText(
                    previewText(state, locale),
                    SpaceTheme.typography.body,
                    Modifier.weight(1f),
                    if (done) Color(palette.glow) else Color(palette.label).copy(alpha = DIM_ALPHA),
                    maxLines = if (compact) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        when {
            state.status == TranslateStatus.FETCHING -> Unit
            state.failure == TranslateFailure.NEEDS_MODEL && state.status == TranslateStatus.FAILED ->
                ActionOrb(SpaceIcon.DOWN, palette, stringResource(R.string.translate_download), controller::downloadModels)
            state.status == TranslateStatus.FAILED && state.failure?.let(TranslateText::canRetry) == true ->
                ActionOrb(SpaceIcon.REDO, palette, stringResource(R.string.translate_retry), controller::request)
            state.source.isBlank() -> ActionOrb(SpaceIcon.CLIPBOARD, palette, stringResource(R.string.translate_paste), actions::onPaste)
            done -> ActionOrb(SpaceIcon.INSERT, palette, stringResource(R.string.translate_insert), controller::submit)
            state.route == TranslateRoute.AI && state.status != TranslateStatus.WORKING ->
                ActionOrb(SpaceIcon.DONE, palette, stringResource(R.string.translate_action), controller::submit)
            else -> Unit
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LangTag(text: String, palette: ThemePalette, accent: Boolean, description: String, onClick: (() -> Unit)?, onLongClick: (() -> Unit)?) {
    val fill = if (accent) Color(palette.glow).copy(alpha = TAG_FILL_ALPHA) else Color(palette.keyFill)
    val ink = if (accent) Color(palette.glow) else Color(palette.label).copy(alpha = 0.75f)
    val tap = if (onClick == null) Modifier else Modifier.combinedClickable(role = Role.Button, onClick = onClick, onLongClick = onLongClick)
    Box(Modifier.heightIn(min = LineMin).semantics { contentDescription = description }.then(tap), contentAlignment = Alignment.Center) {
        Box(Modifier.clip(TagShape).background(fill).padding(horizontal = 6.dp, vertical = 2.dp)) {
            SpaceText(text, SpaceTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp), color = ink, maxLines = 1)
        }
    }
}

@Composable
private fun ActionOrb(icon: SpaceIcon, palette: ThemePalette, description: String, onClick: () -> Unit) {
    val haptics = rememberSpaceHaptics()
    val face = Brush.linearGradient(
        listOf(Color(ColorMath.mix(palette.glow, ColorMath.WHITE, ORB_TINT)), Color(ColorMath.mix(palette.glow, ColorMath.BLACK, ORB_TINT))),
    )
    val ink = Color(ColorMath.readableOn(palette.glow, listOf(ORB_INK, ColorMath.WHITE)))
    Box(
        Modifier.size(OrbSize).clip(CircleShape).background(face).semantics { contentDescription = description }
            .clickable(role = Role.Button) {
                haptics.perform(SpaceHaptic.Tap)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(icon, ink, Modifier, size = 20.dp)
    }
}

@Composable
private fun previewText(state: TranslateState, locale: java.util.Locale): String = when {
    state.source.isBlank() -> stringResource(R.string.translate_preview_hint)
    state.status == TranslateStatus.FETCHING -> stringResource(R.string.translate_fetching)
    state.status == TranslateStatus.FAILED && state.missingModels.isNotEmpty() ->
        stringResource(R.string.translate_needs_model, state.missingModels.joinToString("، ") { TranslateLanguages.displayName(it, locale) })
    state.status == TranslateStatus.DONE -> state.result
    state.status == TranslateStatus.WORKING -> stringResource(workingText(state))
    state.status == TranslateStatus.FAILED ->
        stringResource(TranslateText.failure(state.failure ?: TranslateFailure.SERVER), TranslatePolicy.MAX_TEXT)
    state.route == TranslateRoute.AI -> stringResource(R.string.translate_preview_ai)
    else -> ""
}

private fun workingText(state: TranslateState): Int =
    if (state.route == TranslateRoute.AI) R.string.translate_working_ai else R.string.translate_working_device
