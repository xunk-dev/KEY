package com.spacekeyboard.keyboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.WallpaperChoice
import com.spacekeyboard.keyboard.core.theme.WallpaperImportResult
import com.spacekeyboard.keyboard.core.theme.TrimRange
import com.spacekeyboard.keyboard.core.theme.WallpaperKind
import com.spacekeyboard.keyboard.core.theme.WallpaperProbe
import com.spacekeyboard.keyboard.designsystem.components.LookCard
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

private const val THUMB_TARGET = 640
private const val TRANSPARENCY_SCALE = 100

private class PendingTrim(val uri: Uri, val durationMs: Long)

@Composable
internal fun WallpaperSection(theme: ThemeController) {
    val snapshot = rememberThemeSnapshot(theme)
    val wallpaper = snapshot.wallpaper
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }
    var pending by remember { mutableStateOf<PendingTrim?>(null) }
    val start = { uri: Uri, trim: TrimRange? ->
        busy = true
        error = null
        importInBackground(theme, uri, trim) {
            error = it
            busy = false
        }
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onPicked(theme, uri, { busy = it }, { pending = it }, start)
    }
    pending?.let { request ->
        WallpaperTrimSheet(request.uri, request.durationMs, onConfirm = {
            pending = null
            start(request.uri, it)
        }, onCancel = { pending = null })
    }
    SpaceText(stringResource(R.string.wallpaper_title), SpaceTheme.typography.titleSmall)
    LookCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            WallpaperPreview(theme, snapshot.palette.background, wallpaper, busy) { theme.clearWallpaper() }
            PickRow(wallpaper.kind, busy) { picker.launch(PickVisualMediaRequest(it)) }
            if (wallpaper.active) WallpaperControls(theme, wallpaper, snapshot.motion)
            error?.let { Notice(it) }
            if (!wallpaper.active) BodyText(R.string.wallpaper_hint)
        }
    }
}

private fun onPicked(
    theme: ThemeController,
    uri: Uri,
    onBusy: (Boolean) -> Unit,
    onTrim: (PendingTrim) -> Unit,
    onImport: (Uri, TrimRange?) -> Unit,
) {
    onBusy(true)
    probeInBackground(theme, uri) { probe ->
        if (probe.needsTrim) {
            onTrim(PendingTrim(uri, probe.durationMs))
            onBusy(false)
        } else {
            onImport(uri, null)
        }
    }
}

@Composable
private fun PickRow(kind: WallpaperKind, busy: Boolean, onPick: (ActivityResultContracts.PickVisualMedia.VisualMediaType) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PickTile(SpaceIcon.IMAGES, R.string.wallpaper_photo, kind == WallpaperKind.IMAGE, busy) {
            onPick(ActivityResultContracts.PickVisualMedia.ImageOnly)
        }
        PickTile(SpaceIcon.PLAY, R.string.wallpaper_video, kind == WallpaperKind.VIDEO, busy) {
            onPick(ActivityResultContracts.PickVisualMedia.VideoOnly)
        }
    }
}

@Composable
private fun RowScope.PickTile(icon: SpaceIcon, label: Int, selected: Boolean, busy: Boolean, onClick: () -> Unit) {
    val colors = SpaceTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .weight(1f)
            .clip(shape)
            .background(if (selected) colors.primaryContainer else SpaceTheme.look.field)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outline, shape)
            .clickable(enabled = !busy, role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceIconView(icon, if (selected) colors.primary else colors.onSurface, size = 22.dp)
        SpaceText(stringResource(label), SpaceTheme.typography.label, maxLines = 1)
    }
}

@Composable
private fun WallpaperControls(theme: ThemeController, wallpaper: WallpaperChoice, motion: Boolean) {
    val transparency = TRANSPARENCY_SCALE - wallpaper.opacity
    ValueSlider(
        stringResource(R.string.wallpaper_transparency),
        transparency,
        TRANSPARENCY_SCALE - WallpaperChoice.MAX_OPACITY,
        TRANSPARENCY_SCALE - WallpaperChoice.MIN_OPACITY,
    ) { theme.setWallpaperOpacity(TRANSPARENCY_SCALE - it) }
    ValueSlider(stringResource(R.string.wallpaper_dim), wallpaper.dim, WallpaperChoice.MIN_DIM, WallpaperChoice.MAX_DIM, theme::setWallpaperDim)
    if (wallpaper.live) SwitchRow(stringResource(R.string.wallpaper_live_play), wallpaper.animate, theme::setWallpaperAnimate)
    if (wallpaper.kind == WallpaperKind.VIDEO) {
        BodyText(if (wallpaper.live) R.string.wallpaper_live_note else R.string.wallpaper_video_note)
        if (!motion) Notice(R.string.wallpaper_battery)
    }
    SpaceButton(stringResource(R.string.wallpaper_remove), theme::clearWallpaper)
}

private fun probeInBackground(theme: ThemeController, uri: Uri, done: (WallpaperProbe) -> Unit) {
    Thread({ done(theme.probeWallpaper(uri)) }, "wallpaper-probe").start()
}

private fun importInBackground(theme: ThemeController, uri: Uri, trim: TrimRange?, done: (Int?) -> Unit) {
    Thread({
        val message = when (theme.importWallpaper(uri, trim)) {
            WallpaperImportResult.OK -> null
            WallpaperImportResult.TOO_LARGE -> R.string.wallpaper_err_large
            WallpaperImportResult.UNSUPPORTED -> R.string.wallpaper_err_unsupported
        }
        done(message)
    }, "wallpaper-import").start()
}
