package com.spacekeyboard.keyboard.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex
import kotlin.math.abs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutTile
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutTilesStore
import com.spacekeyboard.keyboard.core.shortcuts.TileArrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.theme.ColorMath
import com.spacekeyboard.keyboard.core.theme.KeyStyle
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.designsystem.theme.SpaceThemeMode
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.clipboard.ClipboardController
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

private const val COLUMNS = 4

@Composable
internal fun ShortcutsPanelContent(
    theme: ThemeController,
    layouts: LayoutController,
    decor: DecorController,
    emoji: EmojiController,
    clip: ClipboardController,
    translate: TranslateController,
    shortcuts: ShortcutsController,
    commands: TileCommands,
) {
    SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
        val themeSnapshot = rememberThemeSnapshot(theme)
        val layoutSnapshot = rememberLayoutSnapshot(layouts)
        val palette = themeSnapshot.palette
        val look = ButtonLook(palette, KeyStyle.of(themeSnapshot.choice.theme), layoutSnapshot.prefs.feedback.vibrate)
        val context = LocalContext.current
        val store = remember(context) { ShortcutTilesStore(context) }
        var arrangement by remember { mutableStateOf(store.arrangement()) }
        var arranging by remember { mutableStateOf(false) }
        var full by remember { mutableStateOf(false) }
        var dragging by remember { mutableStateOf<ShortcutTile?>(null) }
        var dragOffset by remember { mutableStateOf(Offset.Zero) }
        val bounds = remember { mutableMapOf<ShortcutTile, Rect>() }
        val haptic = LocalHapticFeedback.current
        val specs = rememberTileSpecs(theme, layouts, decor, emoji, clip, translate, shortcuts, commands)
        val prefs = layoutSnapshot.prefs
        val bar = remember(prefs.barLayout, prefs.languages) { prefs.bar() }
        val onScreen = { tile: ShortcutTile -> tile in specs && tile.id !in bar.all }
        val shown = arrangement.visible.filter(onScreen)
        val onBar = bar.all.mapNotNull { ShortcutTile.byId(it) }.filter { it in specs }
        fun toBar(tile: ShortcutTile) {
            if (bar.canBring(tile.id)) {
                full = false
                layouts.setBar(bar.bring(tile.id))
            } else {
                full = true
            }
        }
        fun toPage(tile: ShortcutTile) {
            full = false
            if (tile in arrangement.hidden) {
                arrangement = arrangement.add(tile)
                store.save(arrangement)
            }
            layouts.setBar(bar.sendOff(tile.id))
        }
        val drop = {
            val from = dragging
            val origin = from?.let { bounds[it] }
            if (from != null && origin != null) {
                val point = origin.center + dragOffset
                val target = shown.firstOrNull { it != from && bounds[it]?.contains(point) == true }
                if (target != null) {
                    val next = arrangement.moveTo(from, target, onScreen)
                    arrangement = next
                    store.save(next)
                }
            }
            dragging = null
            dragOffset = Offset.Zero
        }
        val onDrop by rememberUpdatedState(drop)
        val pageItems = shown.mapNotNull { id -> specs[id]?.let { id to (if (arranging) it.withClick { toBar(id) } else it) } }
        val barItems = onBar.mapNotNull { id -> specs[id]?.let { id to it.withClick { toPage(id) } } }
        if (commands.macro.manager) {
            MacroManagerContent(commands.macro, look) {
                commands.macro.startRecording()
                shortcuts.close()
            }
        } else if (commands.snippets.manager) {
            SnippetManagerContent(commands.snippets, look)
        } else Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ArrangeChip(if (arranging) stringResource(R.string.shortcuts_done) else stringResource(R.string.shortcuts_arrange), arranging, look) {
                arranging = !arranging
                full = false
            }
            if (arranging) {
                SectionLabel(stringResource(R.string.shortcuts_bar_title), look)
                if (barItems.isEmpty()) {
                    SectionLabel(stringResource(R.string.shortcuts_bar_empty), look, dim = true)
                } else {
                    TileGrid(barItems, look, "\u2193", null, Offset.Zero, false, bounds, {}, {}, {}, {})
                }
                SectionLabel(stringResource(R.string.shortcuts_page_title), look)
                SectionLabel(stringResource(if (full) R.string.shortcuts_bar_full else R.string.shortcuts_arrange_hint), look, dim = !full)
            } else if (pageItems.isEmpty()) {
                SectionLabel(stringResource(R.string.shortcuts_empty), look, dim = true)
            }
            TileGrid(
                pageItems, look, if (arranging) "\u2191" else null, dragging, dragOffset, true, bounds,
                onDragStart = {
                    dragging = it
                    dragOffset = Offset.Zero
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDrag = { dragOffset += it },
                onDrop = { onDrop() },
                onCancel = {
                    dragging = null
                    dragOffset = Offset.Zero
                },
            )
        }
    }
}

private fun Tile.withClick(click: () -> Unit) = Tile(icon, label, selected, click)

@Composable
private fun TileGrid(
    items: List<Pair<ShortcutTile, Tile>>,
    look: ButtonLook,
    badge: String?,
    dragging: ShortcutTile?,
    dragOffset: Offset,
    draggable: Boolean,
    bounds: MutableMap<ShortcutTile, Rect>,
    onDragStart: (ShortcutTile) -> Unit,
    onDrag: (Offset) -> Unit,
    onDrop: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(COLUMNS).forEach { row ->
            Row(
                Modifier.fillMaxWidth().zIndex(if (row.any { it.first == dragging }) 1f else 0f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { (id, tile) ->
                    val lifted = dragging == id
                    var tileModifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { bounds[id] = it.boundsInRoot() }
                        .graphicsLayer {
                            if (lifted) {
                                translationX = dragOffset.x
                                translationY = dragOffset.y
                                scaleX = 1.06f
                                scaleY = 1.06f
                                alpha = 0.9f
                            }
                        }
                    if (draggable) {
                        tileModifier = tileModifier.pointerInput(id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart(id) },
                                onDrag = { change, delta ->
                                    change.consume()
                                    onDrag(delta)
                                },
                                onDragEnd = onDrop,
                                onDragCancel = onCancel,
                            )
                        }
                    }
                    TileView(tile, look, tileModifier, badge)
                }
                repeat(COLUMNS - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ArrangeChip(text: String, active: Boolean, look: ButtonLook, onClick: () -> Unit) {
    val p = look.palette
    val shape = RoundedCornerShape(50)
    val fill = Color(ColorMath.mix(p.background, p.keyFill, if (active) 1f else 0.75f))
    var box = Modifier.clip(shape).background(fill)
    if (active) box = box.border(1.dp, Color(p.glow), shape)
    Box(box.clickable(role = Role.Button, onClick = onClick).padding(horizontal = 18.dp, vertical = 8.dp)) {
        BasicText(text, style = TextStyle(color = Color(if (active) p.glow else p.label), fontSize = 13.sp))
    }
}

@Composable
private fun SectionLabel(text: String, look: ButtonLook, dim: Boolean = false) {
    val p = look.palette
    BasicText(
        text,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(color = Color(p.label).copy(alpha = if (dim) 0.65f else 1f), fontSize = 12.sp),
    )
}

private fun TileArrangement.moveTo(tile: ShortcutTile, target: ShortcutTile, shown: (ShortcutTile) -> Boolean): TileArrangement {
    val order = visible.filter(shown)
    val from = order.indexOf(tile)
    val to = order.indexOf(target)
    if (from < 0 || to < 0 || from == to) return this
    var result = this
    repeat(abs(to - from)) { result = result.move(tile, if (to > from) 1 else -1, shown) }
    return result
}

@Composable
private fun TileView(tile: Tile, look: ButtonLook, modifier: Modifier, badge: String? = null) {
    val p = look.palette
    val ink = Color(if (tile.selected) p.glow else p.label)
    val fill = Color(ColorMath.mix(p.background, p.keyFill, if (tile.selected) 1f else 0.75f))
    val shape = RoundedCornerShape(12.dp)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var box = Modifier.fillMaxWidth().aspectRatio(1.35f).clip(shape).background(fill)
        if (tile.selected) box = box.border(1.dp, Color(p.glow), shape)
        Box(
            modifier = box.clickable(role = Role.Button) { tile.onClick() },
            contentAlignment = Alignment.Center,
        ) {
            SpaceIconView(tile.icon, ink, Modifier, 28.dp)
            if (badge != null) {
                Box(Modifier.fillMaxSize().padding(6.dp), contentAlignment = Alignment.TopEnd) {
                    BasicText(badge, style = TextStyle(color = Color(p.glow), fontSize = 14.sp))
                }
            }
        }
        BasicText(
            text = tile.label,
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(color = ink, fontSize = 12.sp, textAlign = TextAlign.Center),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
