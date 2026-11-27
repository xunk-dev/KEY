package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import com.spacekeyboard.keyboard.designsystem.components.SpacePrimaryButton
import androidx.compose.ui.unit.coerceAtMost
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.dock.dockContentPadding
import com.spacekeyboard.keyboard.images.ImageItem
import com.spacekeyboard.keyboard.images.ImageMessages
import com.spacekeyboard.keyboard.images.ImagePolicy
import com.spacekeyboard.keyboard.images.ImageStore
import com.spacekeyboard.keyboard.images.ui.ImageThumb

@Composable
fun ImagesScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val store = remember { ImageStore(context) }
    var items by remember { mutableStateOf(store.list()) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        if (uris.isNotEmpty()) {
            store.addAsync(uris) { result ->
                ImageMessages.show(context, result)
                items = store.list()
            }
        }
    }
    val onAdd = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
    AppTheme {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(96.dp),
            modifier = Modifier.fillMaxSize().background(SpaceTheme.look.canvas).systemBarsPadding(),
            contentPadding = dockContentPadding(SpaceTheme.look.screenPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            header(items.size, onBack, onAdd) { store.clear(); items = store.list() }
            items(items, key = { it.id }) { item ->
                ImageCell(store, item) { store.remove(item.id); items = store.list() }
            }
        }
    }
}

private fun LazyGridScope.header(count: Int, onBack: () -> Unit, onAdd: () -> Unit, onClear: () -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BackLink(onBack)
            SpaceText(stringResource(R.string.images_screen_title), SpaceTheme.typography.display)
            ImagesNote(R.string.images_disclosure)
            SpaceSectionTitle(stringResource(R.string.images_count, count, ImagePolicy.LIMIT))
            SpacePrimaryButton(stringResource(R.string.images_add_button), onAdd, icon = SpaceIcon.ADD)
            if (count > 0) ClearSection(onClear)
        }
    }
}

@Composable
private fun ImagesNote(res: Int) {
    SpaceText(stringResource(res), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
}

@Composable
private fun ClearSection(onClear: () -> Unit) {
    ConfirmAction(R.string.images_clear_ask, R.string.images_clear_yes, R.string.images_clear_no, R.string.images_clear, onClear)
}

@Composable
private fun ImageCell(store: ImageStore, item: ImageItem, onDelete: () -> Unit) {
    val delete = stringResource(R.string.images_delete)
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(SpaceTheme.look.fieldRadius.coerceAtMost(16.dp)))
            .background(SpaceTheme.colors.container),
    ) {
        ImageThumb(store, item, Modifier.fillMaxSize().padding(4.dp))
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .semantics { contentDescription = delete }
                .clickable(role = Role.Button, onClick = onDelete)
                .minTouchTarget(),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(SpaceTheme.colors.containerHigh)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                SpaceText("×", SpaceTheme.typography.titleSmall, color = SpaceTheme.colors.error)
            }
        }
    }
}
