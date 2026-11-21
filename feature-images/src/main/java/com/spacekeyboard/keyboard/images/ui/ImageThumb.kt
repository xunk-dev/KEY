package com.spacekeyboard.keyboard.images.ui

import android.os.Build
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.spacekeyboard.keyboard.designsystem.a11y.isMotionReduced
import com.spacekeyboard.keyboard.images.ImageItem
import com.spacekeyboard.keyboard.images.ImageStore

private const val CACHE_ENTRIES = 100

private val cache = LruCache<String, ImageBitmap>(CACHE_ENTRIES)

private fun load(store: ImageStore, item: ImageItem): ImageBitmap? {
    cache.get(item.id)?.let { return it }
    val bitmap = store.thumbnail(item)?.asImageBitmap() ?: return null
    cache.put(item.id, bitmap)
    return bitmap
}

@Composable
fun ImageThumb(store: ImageStore, item: ImageItem, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val still = remember(context) { isMotionReduced(context) }
    if (item.animated && !still && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val drawable = remember(item.id) { decodeAnimated(store.fileOf(item)) }
        if (drawable != null) {
            AnimatedThumb(drawable, modifier)
            return
        }
    }
    val bitmap = remember(item.id) { load(store, item) } ?: return
    Image(bitmap = bitmap, contentDescription = null, modifier = modifier, contentScale = ContentScale.Fit)
}
