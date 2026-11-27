package com.spacekeyboard.keyboard.images.ui

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.spacekeyboard.keyboard.images.ImagePolicy
import java.io.File

@RequiresApi(Build.VERSION_CODES.P)
internal fun decodeAnimated(file: File): Drawable? = runCatching {
    ImageDecoder.decodeDrawable(ImageDecoder.createSource(file)) { decoder, info, _ ->
        val (width, height) = ImagePolicy.fitted(info.size.width, info.size.height, ImagePolicy.THUMB_SIDE)
        decoder.setTargetSize(width, height)
    }
}.getOrNull()

@RequiresApi(Build.VERSION_CODES.P)
@Composable
internal fun AnimatedThumb(drawable: Drawable, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                setImageDrawable(drawable)
            }
        },
        modifier = modifier,
    )
    DisposableEffect(drawable) {
        (drawable as? AnimatedImageDrawable)?.start()
        onDispose { (drawable as? AnimatedImageDrawable)?.stop() }
    }
}
