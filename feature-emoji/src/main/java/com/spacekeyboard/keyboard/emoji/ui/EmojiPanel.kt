package com.spacekeyboard.keyboard.emoji.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.emoji.EmojiSnapshot
import com.spacekeyboard.keyboard.emoji.LoadState
import com.spacekeyboard.keyboard.emoji.R
import com.spacekeyboard.keyboard.emoji.RECENTS_GROUP
import com.spacekeyboard.keyboard.emoji.model.Emoji
import kotlinx.coroutines.launch

private const val SCRIM_ALPHA = 0.55f

@Composable
fun EmojiPanel(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions, tabs: List<HubTab>) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
            var holding by remember { mutableStateOf<Emoji?>(null) }
            val ids = remember(state.groups) { listOf(RECENTS_GROUP) + state.groups.map { it.id } }
            val pager = key(state.load, ids) {
                rememberPagerState(ids.indexOf(state.activeGroup).coerceAtLeast(0)) { ids.size }
            }
            val scope = rememberCoroutineScope()
            Column(modifier = Modifier.fillMaxSize()) {
                HubTabBar(tabs, HubTab.EMOJI, palette, actions::onTab, actions::onSearch, stringResource(R.string.emoji_search))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(palette.background).copy(alpha = SCRIM_ALPHA)),
                ) {
                    PanelBody(state, palette, actions, pager, ids) { holding = it }
                    holding?.let { EmojiToneSheet(it, state, palette, actions) { holding = null } }
                }
                EmojiDock(state, ids, { pager.shownIndex() }, palette, actions) { index ->
                    scope.launch { pager.animateScrollToPage(index) }
                }
            }
        }
    }
}

private fun PagerState.shownIndex(): Int = if (isScrollInProgress) targetPage else currentPage

@Composable
private fun PanelBody(
    state: EmojiSnapshot,
    palette: ThemePalette,
    actions: EmojiActions,
    pager: PagerState,
    ids: List<String>,
    onHold: (Emoji) -> Unit,
) {
    when (state.load) {
        LoadState.READY -> GroupPager(pager, ids, state, palette, actions, onHold)
        LoadState.FAILED -> CenterText(stringResource(R.string.emoji_failed), palette)
        else -> CenterText(stringResource(R.string.emoji_loading), palette)
    }
}

@Composable
private fun GroupPager(
    pager: PagerState,
    ids: List<String>,
    state: EmojiSnapshot,
    palette: ThemePalette,
    actions: EmojiActions,
    onHold: (Emoji) -> Unit,
) {
    val active by rememberUpdatedState(state.activeGroup)
    LaunchedEffect(pager, ids) {
        snapshotFlow { pager.settledPage }.collect { page ->
            val id = ids.getOrNull(page)
            if (id != null && id != active) actions.onGroup(id)
        }
    }
    HorizontalPager(
        state = pager,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 1,
        key = { ids[it] },
        flingBehavior = PagerDefaults.flingBehavior(
            state = pager,
            snapAnimationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        ),
    ) { page ->
        GroupPage(ids[page], state, palette, actions, onHold)
    }
}

@Composable
private fun GroupPage(id: String, state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions, onHold: (Emoji) -> Unit) {
    if (id == RECENTS_GROUP) {
        RecentsContent(state, palette, actions)
        return
    }
    val group = state.groups.firstOrNull { it.id == id } ?: return
    val texts = remember(group, state.tone) { group.emojis.map { it.forTone(state.tone) } }
    EmojiGrid(
        texts,
        state.typeface,
        actions::onPick,
        hasTones = { group.emojis.getOrNull(it)?.hasTones == true },
        onHold = { index -> group.emojis.getOrNull(index)?.let(onHold) },
        onBurst = actions::onPickMany,
    )
}

@Composable
private fun RecentsContent(state: EmojiSnapshot, palette: ThemePalette, actions: EmojiActions) {
    var confirming by remember(state.recents.isEmpty()) { mutableStateOf(false) }
    var editing by remember(state.recents.isEmpty()) { mutableStateOf(false) }
    if (state.recents.isEmpty()) {
        CenterText(stringResource(R.string.emoji_recents_empty), palette)
        return
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpaceText(
                stringResource(if (confirming) R.string.emoji_clear_confirm else R.string.emoji_recents),
                SpaceTheme.typography.bodySmall,
                color = Color(palette.label),
            )
            ClearButtons(
                editing, confirming, palette,
                onEdit = { editing = true },
                onDone = { editing = false; confirming = false },
                onAsk = { confirming = true },
                onNo = { confirming = false },
            ) {
                confirming = false
                actions.onClearRecents()
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            if (editing) {
                EmojiEditGrid(state.recents, state.typeface, actions::onRemoveRecent)
            } else {
                EmojiGrid(state.recents, state.typeface, actions::onPick, onBurst = actions::onPickMany)
            }
        }
    }
}

@Composable
private fun ClearButtons(
    editing: Boolean,
    confirming: Boolean,
    palette: ThemePalette,
    onEdit: () -> Unit,
    onDone: () -> Unit,
    onAsk: () -> Unit,
    onNo: () -> Unit,
    onYes: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        when {
            confirming -> {
                SmallKey(stringResource(R.string.emoji_clear_no), palette, onNo)
                SmallKey(stringResource(R.string.emoji_clear_yes), palette, onYes)
            }
            editing -> {
                SmallKey(stringResource(R.string.emoji_clear_yes), palette, onAsk, PanelIcon.CLEAR_ALL)
                SmallKey(stringResource(R.string.emoji_done), palette, onDone)
            }
            else -> SmallKey(stringResource(R.string.emoji_clear), palette, onEdit, PanelIcon.CLEAR_ALL)
        }
    }
}

@Composable
private fun SmallKey(text: String, palette: ThemePalette, onClick: () -> Unit, icon: PanelIcon? = null) {
    PanelKey(palette, text, Modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) PanelIconView(icon, Color(palette.keyText), Modifier.padding(start = 10.dp).size(16.dp))
            SpaceText(
                text,
                SpaceTheme.typography.bodySmall,
                color = Color(palette.keyText),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun CenterText(text: String, palette: ThemePalette) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.label))
    }
}
