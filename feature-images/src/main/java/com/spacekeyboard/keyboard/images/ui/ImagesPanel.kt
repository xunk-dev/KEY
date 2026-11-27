package com.spacekeyboard.keyboard.images.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.emoji.ui.DockAction
import com.spacekeyboard.keyboard.emoji.ui.DockBar
import com.spacekeyboard.keyboard.emoji.ui.DockCategory
import com.spacekeyboard.keyboard.emoji.ui.HubTab
import com.spacekeyboard.keyboard.emoji.ui.HubTabBar
import com.spacekeyboard.keyboard.emoji.ui.PanelKey
import com.spacekeyboard.keyboard.images.ImageFilter
import com.spacekeyboard.keyboard.images.ImageItem
import com.spacekeyboard.keyboard.images.ImageKind
import com.spacekeyboard.keyboard.images.ImageStore
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.images.ImagesSnapshot
import com.spacekeyboard.keyboard.images.R
import com.spacekeyboard.keyboard.images.rememberImagesSnapshot

private const val SCRIM_ALPHA = 0.55f
private const val BADGE_ALPHA = 0.8f
private val CellSize = 76.dp

@Composable
fun ImagesPanelHost(controller: ImagesController, palette: ThemePalette, actions: ImagesActions, tabs: List<HubTab>) {
    val state = rememberImagesSnapshot(controller)
    if (!state.open) return
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        SpaceTheme(mode = SpaceThemeMode.Dark, steadyPress = true) {
            ImagesPanel(state, controller, palette, actions, tabs)
        }
    }
}

@Composable
private fun ImagesPanel(
    state: ImagesSnapshot,
    controller: ImagesController,
    palette: ThemePalette,
    actions: ImagesActions,
    tabs: List<HubTab>,
) {
    val store = controller.store
    Column(modifier = Modifier.fillMaxSize()) {
        HubTabBar(tabs, HubTab.IMAGES, palette, actions::onTab, onSearch = null, searchDescription = "")
        Box(Modifier.weight(1f).fillMaxWidth().background(Color(palette.background).copy(alpha = SCRIM_ALPHA))) {
            when {
                state.items.isEmpty() -> Empty(stringResource(R.string.images_empty), palette)
                state.shown.isEmpty() -> Empty(stringResource(R.string.images_empty_filtered), palette)
                else -> Grid(state.shown, store, palette, actions)
            }
        }
        ImagesDock(state.filter, palette, actions, controller)
    }
}

@Composable
private fun ImagesDock(filter: ImageFilter, palette: ThemePalette, actions: ImagesActions, controller: ImagesController) {
    DockBar(palette) {
        DockAction(SpaceIcon.KEYS, stringResource(R.string.images_keys), palette, actions::onKeys)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterItem(R.string.images_filter_all, SpaceIcon.FILTER_ALL, ImageFilter.ALL, filter, palette, controller)
            FilterItem(R.string.images_filter_gif, SpaceIcon.FILTER_GIF, ImageFilter.GIF, filter, palette, controller)
            FilterItem(R.string.images_filter_stickers, SpaceIcon.FILTER_STICKER, ImageFilter.STICKER, filter, palette, controller)
        }
        DockAction(SpaceIcon.ADD, stringResource(R.string.images_add), palette, actions::onAdd)
    }
}

@Composable
private fun FilterItem(label: Int, icon: SpaceIcon, target: ImageFilter, current: ImageFilter, palette: ThemePalette, controller: ImagesController) {
    DockCategory(icon, stringResource(label), palette, target == current) { controller.setFilter(target) }
}

@Composable
private fun Empty(text: String, palette: ThemePalette) {
    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        SpaceText(text, SpaceTheme.typography.bodySmall, color = Color(palette.label))
    }
}

@Composable
private fun GifBadge(palette: ThemePalette, modifier: Modifier) {
    SpaceText(
        text = stringResource(R.string.images_filter_gif),
        style = SpaceTheme.typography.bodySmall,
        color = Color(palette.keyText),
        modifier = modifier.background(Color(palette.keyFill).copy(alpha = BADGE_ALPHA)).padding(horizontal = 4.dp),
    )
}

@Composable
private fun kindLabel(kind: ImageKind): String = stringResource(
    when (kind) {
        ImageKind.GIF -> R.string.images_label_gif
        ImageKind.STICKER -> R.string.images_label_sticker
        ImageKind.PHOTO -> R.string.images_label
    },
)

@Composable
private fun Grid(items: List<ImageItem>, store: ImageStore, palette: ThemePalette, actions: ImagesActions) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(CellSize),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
            val label = stringResource(R.string.images_item_desc, kindLabel(item.kind), index + 1, items.size)
            PanelKey(palette, label, Modifier.aspectRatio(1f), onClick = { actions.onPick(item) }) {
                ImageThumb(store, item, Modifier.fillMaxSize().padding(4.dp))
                if (item.kind == ImageKind.GIF) GifBadge(palette, Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}
