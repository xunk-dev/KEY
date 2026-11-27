package com.spacekeyboard.keyboard

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.spacekeyboard.keyboard.core.theme.TrimMath
import com.spacekeyboard.keyboard.core.theme.TrimRange
import com.spacekeyboard.keyboard.core.theme.WallpaperFrames
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

private const val FRAME_COUNT = 10
private const val STRIP_SIDE = 160
private const val PREVIEW_SIDE = 640
private const val START = 0
private const val END = 1
private val HANDLE_WIDTH = 18.dp
private val STRIP_HEIGHT = 56.dp

@Composable
internal fun WallpaperTrimSheet(uri: Uri, durationMs: Long, onConfirm: (TrimRange) -> Unit, onCancel: () -> Unit) {
    var range by remember { mutableStateOf(TrimMath.initial(durationMs)) }
    var active by remember { mutableIntStateOf(START) }
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }
    val strip = remember(uri) { mutableStateListOf<ImageBitmap?>().apply { repeat(FRAME_COUNT) { add(null) } } }
    val resolver = LocalContext.current.contentResolver
    val frames = remember(uri) { WallpaperFrames(resolver, uri) }
    DisposableEffect(frames) {
        frames.strip(durationMs, FRAME_COUNT, STRIP_SIDE) { index, bitmap -> strip[index] = bitmap.asImageBitmap() }
        onDispose { frames.close() }
    }
    val position = if (active == START) range.startMs else range.endMs
    LaunchedEffect(position, active) {
        frames.preview(position, PREVIEW_SIDE, active == END) { preview = it.asImageBitmap() }
    }
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { onCancel() } },
            contentAlignment = Alignment.BottomCenter,
        ) {
            SheetBody(range, durationMs, strip, preview, active, { active = it }, { range = it }, onConfirm, onCancel)
        }
    }
}

@Composable
private fun SheetBody(
    range: TrimRange,
    durationMs: Long,
    strip: List<ImageBitmap?>,
    preview: ImageBitmap?,
    active: Int,
    onActive: (Int) -> Unit,
    onRange: (TrimRange) -> Unit,
    onConfirm: (TrimRange) -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(SpaceTheme.colors.surface)
            .pointerInput(Unit) { detectTapGestures { } }
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SpaceText(stringResource(R.string.wallpaper_trim_title), SpaceTheme.typography.titleSmall)
        FramePreview(preview)
        Timeline(range, durationMs, strip, onActive, onRange)
        val seconds = ((range.lengthMs + 500L) / 1000L).toInt()
        SpaceText(
            stringResource(R.string.wallpaper_trim_selected, TrimMath.label(range.startMs), TrimMath.label(range.endMs), seconds),
            SpaceTheme.typography.bodySmall,
            color = SpaceTheme.colors.onSurfaceVariant,
        )
        SpaceText(
            stringResource(R.string.wallpaper_trim_hint, (TrimMath.MAX_CLIP_MS / 1000L).toInt()),
            SpaceTheme.typography.bodySmall,
            color = SpaceTheme.colors.onSurfaceVariant,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SpaceButton(stringResource(R.string.wallpaper_trim_cancel), onCancel, Modifier.weight(1f))
            SpaceButton(stringResource(R.string.wallpaper_trim_use), { onConfirm(range) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FramePreview(frame: ImageBitmap?) {
    Box(
        Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (frame != null) Image(frame, null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
    }
}

@Composable
private fun Timeline(range: TrimRange, durationMs: Long, strip: List<ImageBitmap?>, onActive: (Int) -> Unit, onRange: (TrimRange) -> Unit) {
    val latest by rememberUpdatedState(range)
    var widthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val handlePx = with(density) { HANDLE_WIDTH.roundToPx() }
    val startX = widthPx * TrimMath.fraction(range.startMs, durationMs)
    val endX = widthPx * TrimMath.fraction(range.endMs, durationMs)
    fun deltaMs(dx: Float): Long = if (widthPx == 0) 0L else (dx / widthPx * durationMs).toLong()
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(Modifier.fillMaxWidth().height(STRIP_HEIGHT)) {
            Box(Modifier.fillMaxSize().padding(horizontal = HANDLE_WIDTH).onSizeChanged { widthPx = it.width }) {
                Filmstrip(strip)
                Shade(startX, endX)
                Box(
                    Modifier
                        .offset { IntOffset(startX.toInt(), 0) }
                        .width(with(density) { (endX - startX).toDp() })
                        .fillMaxHeight()
                        .pointerInput(durationMs, widthPx) {
                            detectHorizontalDragGestures(onDragStart = { onActive(START) }) { change, dx ->
                                change.consume()
                                onRange(TrimMath.shift(latest, deltaMs(dx), durationMs))
                            }
                        },
                )
            }
            Handle(startX.toInt()) { dx ->
                onActive(START)
                onRange(TrimMath.moveStart(latest, latest.startMs + deltaMs(dx), durationMs))
            }
            Handle(endX.toInt() + handlePx) { dx ->
                onActive(END)
                onRange(TrimMath.moveEnd(latest, latest.endMs + deltaMs(dx), durationMs))
            }
        }
    }
}

@Composable
private fun Filmstrip(strip: List<ImageBitmap?>) {
    Row(Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)).background(SpaceTheme.colors.containerHigh)) {
        strip.forEach { frame ->
            Box(Modifier.weight(1f).fillMaxHeight()) {
                if (frame != null) Image(frame, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
    }
}

@Composable
private fun Shade(startX: Float, endX: Float) {
    val edge = SpaceTheme.colors.primary
    Canvas(Modifier.fillMaxSize()) {
        val veil = Color.Black.copy(alpha = 0.62f)
        drawRect(veil, Offset.Zero, Size(startX, size.height))
        drawRect(veil, Offset(endX, 0f), Size(size.width - endX, size.height))
        val line = 2.dp.toPx()
        drawRect(edge, Offset(startX, 0f), Size(endX - startX, line))
        drawRect(edge, Offset(startX, size.height - line), Size(endX - startX, line))
    }
}

@Composable
private fun Handle(x: Int, onDrag: (Float) -> Unit) {
    val colors = SpaceTheme.colors
    Box(
        modifier = Modifier
            .offset { IntOffset(x, 0) }
            .width(HANDLE_WIDTH)
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.primary)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dx ->
                    change.consume()
                    onDrag(dx)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.width(3.dp).height(22.dp).clip(RoundedCornerShape(2.dp)).background(colors.onPrimary))
    }
}
