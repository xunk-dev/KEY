package com.spacekeyboard.keyboard

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.core.theme.WallpaperChoice
import com.spacekeyboard.keyboard.core.theme.WallpaperKind
import com.spacekeyboard.keyboard.core.theme.WallpaperMath
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIconView
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

private const val THUMB_TARGET = 640

@Composable
internal fun WallpaperPreview(theme: ThemeController, base: Int, wallpaper: WallpaperChoice, busy: Boolean, onRemove: () -> Unit) {
    val thumb = rememberThumb(theme, wallpaper)
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(PREVIEW_RATIO)
            .clip(shape)
            .background(if (wallpaper.active) Color(base) else SpaceTheme.look.field)
            .border(1.dp, SpaceTheme.colors.outline, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (wallpaper.active && thumb != null) {
            val level = (wallpaper.brightness * 255).toInt().coerceIn(0, 255)
            Image(
                bitmap = thumb,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = wallpaper.alpha,
                colorFilter = ColorFilter.tint(Color(level, level, level), BlendMode.Modulate),
            )
        } else if (!wallpaper.active) {
            EmptyPreview()
        }
        if (wallpaper.kind == WallpaperKind.VIDEO) {
            VideoBadge(Modifier.align(Alignment.TopStart), if (wallpaper.live) R.string.wallpaper_live else R.string.wallpaper_video)
        }
        if (wallpaper.active) RemoveButton(Modifier.align(Alignment.TopEnd), onRemove)
        if (busy) BusyVeil()
    }
}

private const val PREVIEW_RATIO = 16f / 7f

@Composable
private fun EmptyPreview() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SpaceIconView(SpaceIcon.IMAGES, SpaceTheme.colors.onSurfaceVariant, size = 28.dp)
        SpaceText(stringResource(R.string.wallpaper_empty), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun VideoBadge(modifier: Modifier, label: Int) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SpaceIconView(SpaceIcon.PLAY, Color.White, size = 14.dp)
        SpaceText(stringResource(label), SpaceTheme.typography.label, color = Color.White, maxLines = 1)
    }
}

@Composable
private fun RemoveButton(modifier: Modifier, onRemove: () -> Unit) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.wallpaper_remove), onClick = onRemove),
        contentAlignment = Alignment.Center,
    ) {
        SpaceIconView(SpaceIcon.CLOSE, Color.White, size = 16.dp)
    }
}

@Composable
private fun BusyVeil() {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
        SpaceText(stringResource(R.string.wallpaper_importing), SpaceTheme.typography.label, color = Color.White)
    }
}

@Composable
private fun rememberThumb(theme: ThemeController, wallpaper: WallpaperChoice): ImageBitmap? {
    var thumb by remember { mutableStateOf<ImageBitmap?>(null) }
    DisposableEffect(wallpaper.kind, wallpaper.stamp) {
        val alive = AtomicBoolean(true)
        val file = theme.wallpaperStill()
        if (file == null) {
            thumb = null
        } else {
            Thread({
                val bitmap = decodeThumb(file)
                if (alive.get()) thumb = bitmap
            }, "wallpaper-thumb").start()
        }
        onDispose { alive.set(false) }
    }
    return thumb
}

private fun decodeThumb(file: File): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    val options = BitmapFactory.Options().apply {
        inSampleSize = WallpaperMath.sampleSize(bounds.outWidth, bounds.outHeight, THUMB_TARGET)
    }
    return BitmapFactory.decodeFile(file.path, options)?.asImageBitmap()
}
