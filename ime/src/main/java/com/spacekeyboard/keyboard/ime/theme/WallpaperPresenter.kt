package com.spacekeyboard.keyboard.ime.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.spacekeyboard.keyboard.core.theme.WallpaperChoice
import com.spacekeyboard.keyboard.core.theme.WallpaperKind
import com.spacekeyboard.keyboard.core.theme.WallpaperMath
import java.io.File
import java.util.concurrent.Executors

internal class WallpaperPresenter(private val frame: ViewGroup, private val controller: ThemeController) {
    private val context: Context = frame.context
    private val main = Handler(Looper.getMainLooper())
    private val still = PinnedImage(context)
    private var video: PinnedVideo? = null
    private var shown = true
    private var visible = false
    private var choice = WallpaperChoice.NONE
    private var motion = true
    private var loaded = Loaded(WallpaperKind.NONE, 0L)
    private var generation = 0

    private data class Loaded(val kind: WallpaperKind, val stamp: Long)

    fun render(snapshot: ThemeSnapshot) {
        choice = snapshot.wallpaper
        motion = snapshot.motion
        if (!choice.active) {
            clear()
            return
        }
        mount()
        applyLook()
        val wanted = Loaded(choice.kind, choice.stamp)
        if (wanted != loaded) {
            loaded = wanted
            dropVideo()
            loadStill(wanted)
        }
        syncVideo()
    }

    fun setShown(value: Boolean) {
        shown = value
        syncVideo()
    }

    fun setVisible(value: Boolean) {
        visible = value
        syncVideo()
    }

    fun release() = clear()

    private fun clear() {
        generation++
        loaded = Loaded(WallpaperKind.NONE, 0L)
        dropVideo()
        if (still.parent != null) frame.removeView(still)
        still.setImageDrawable(null)
    }

    private fun dropVideo() {
        video?.let {
            frame.removeView(it)
            it.release()
        }
        video = null
    }

    private fun mount() {
        if (still.parent == null) frame.addView(still, 0, ViewGroup.LayoutParams(0, 0))
    }

    private fun applyLook() {
        val level = (choice.brightness * 255).toInt().coerceIn(0, 255)
        val filter = PorterDuffColorFilter(Color.rgb(level, level, level), PorterDuff.Mode.MULTIPLY)
        still.alpha = choice.alpha
        still.setColorFilter(filter)
        video?.let {
            it.level = choice.alpha
            it.setLayerPaint(Paint().apply { colorFilter = filter })
        }
    }

    private fun loadStill(wanted: Loaded) {
        val file = controller.wallpaperStill() ?: return
        val token = ++generation
        val target = context.resources.displayMetrics.widthPixels.coerceAtLeast(MIN_TARGET)
        IO.execute {
            val bitmap = decode(file, target)
            main.post { if (token == generation && loaded == wanted) still.setImageBitmap(bitmap) }
        }
    }

    private fun syncVideo() {
        val file = controller.wallpaperVideo()
        val play = choice.kind == WallpaperKind.VIDEO && choice.animate && motion && file != null
        if (!play) {
            dropVideo()
            return
        }
        val view = video ?: PinnedVideo(context, file!!).also {
            video = it
            frame.addView(it, 1, ViewGroup.LayoutParams(0, 0))
            applyLook()
        }
        view.playing = shown && visible
    }

    private fun decode(file: File, target: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        val options = BitmapFactory.Options().apply {
            inSampleSize = WallpaperMath.sampleSize(bounds.outWidth, bounds.outHeight, target)
        }
        return BitmapFactory.decodeFile(file.path, options)
    }

    private companion object {
        const val MIN_TARGET = 720
        val IO = Executors.newSingleThreadExecutor { task -> Thread(task, "wallpaper-decode").apply { isDaemon = true } }
    }
}

private class PinnedImage(context: Context) : ImageView(context) {
    init {
        scaleType = ScaleType.CENTER_CROP
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable = false
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        val host = parent as? View
        if (host == null) super.layout(l, t, r, b) else super.layout(0, 0, host.width, host.height)
    }
}

private class PinnedVideo(context: Context, private val file: File) : TextureView(context), TextureView.SurfaceTextureListener {
    private var player: MediaPlayer? = null
    private var surface: Surface? = null
    private var prepared = false
    private var failed = false
    private var videoWidth = 0
    private var videoHeight = 0

    var playing = false
        set(value) {
            field = value
            sync()
        }

    var level = 1f
        set(value) {
            field = value
            if (revealed) {
                animate().cancel()
                alpha = value
            }
        }

    private var revealed = false

    init {
        isOpaque = false
        alpha = 0f
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        surfaceTextureListener = this
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        val host = parent as? View
        if (host == null) super.layout(l, t, r, b) else super.layout(0, 0, host.width, host.height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        crop()
    }

    fun release() {
        surfaceTextureListener = null
        stop()
        surface?.release()
        surface = null
    }

    override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
        surface = Surface(texture)
        sync()
    }

    override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) = crop()

    override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
        stop()
        surface?.release()
        surface = null
        return true
    }

    override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit

    private fun sync() {
        if (failed) return
        if (!playing) {
            player?.takeIf { prepared && it.isPlaying }?.pause()
            return
        }
        val target = surface ?: return
        val current = player ?: open(target) ?: return
        if (prepared && !current.isPlaying) current.start()
    }

    private fun open(target: Surface): MediaPlayer? = try {
        MediaPlayer().also { mp ->
            player = mp
            mp.setSurface(target)
            mp.setDataSource(file.path)
            mp.isLooping = true
            mp.setVolume(0f, 0f)
            mp.setOnVideoSizeChangedListener { _, w, h ->
                videoWidth = w
                videoHeight = h
                crop()
            }
            mp.setOnInfoListener { _, what, _ ->
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) reveal()
                false
            }
            mp.setOnErrorListener { _, _, _ ->
                failed = true
                stop()
                true
            }
            mp.setOnPreparedListener {
                prepared = true
                if (playing) it.start()
            }
            mp.prepareAsync()
        }
    } catch (_: Exception) {
        failed = true
        stop()
        null
    }

    private fun reveal() {
        revealed = true
        animate().alpha(level).setDuration(FADE_MS).start()
    }

    private fun stop() {
        revealed = false
        prepared = false
        player?.let {
            it.setOnPreparedListener(null)
            it.setOnInfoListener(null)
            it.setOnErrorListener(null)
            it.setOnVideoSizeChangedListener(null)
            runCatching { it.stop() }
            it.release()
        }
        player = null
        animate().cancel()
        alpha = 0f
    }

    private fun crop() {
        if (width == 0 || height == 0 || videoWidth == 0 || videoHeight == 0) return
        val scale = maxOf(width.toFloat() / videoWidth, height.toFloat() / videoHeight)
        val matrix = Matrix()
        matrix.setScale(scale * videoWidth / width, scale * videoHeight / height, width / 2f, height / 2f)
        setTransform(matrix)
    }

    private companion object {
        const val FADE_MS = 260L
    }
}
