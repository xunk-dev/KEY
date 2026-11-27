package com.spacekeyboard.keyboard.ime.theme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.content.res.Resources
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.spacekeyboard.keyboard.core.theme.ThemeCatalog
import com.spacekeyboard.keyboard.core.theme.ThemeChoice
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.core.theme.ThemeSlot
import com.spacekeyboard.keyboard.core.theme.ThemeStore
import android.net.Uri
import com.spacekeyboard.keyboard.core.theme.WallpaperChoice
import com.spacekeyboard.keyboard.core.theme.WallpaperImport
import com.spacekeyboard.keyboard.core.theme.WallpaperImportResult
import com.spacekeyboard.keyboard.core.theme.TrimRange
import com.spacekeyboard.keyboard.core.theme.WallpaperKind
import com.spacekeyboard.keyboard.core.theme.WallpaperProbe
import com.spacekeyboard.keyboard.core.theme.WallpaperStore
import java.io.File

data class ThemeSnapshot(
    val choice: ThemeChoice,
    val palette: ThemePalette,
    val motion: Boolean,
    val wallpaper: WallpaperChoice = WallpaperChoice.NONE,
)

class ThemeController(context: Context) {
    private val context = context.applicationContext
    private val store = ThemeStore(this.context)
    private val wallpaperStore = WallpaperStore(this.context)
    private var wallpaperSubscription: AutoCloseable? = null
    private val listeners = LinkedHashSet<(ThemeSnapshot) -> Unit>()
    private var subscription: AutoCloseable? = null
    private var receiver: BroadcastReceiver? = null

    var snapshot: ThemeSnapshot = build()
        private set

    fun start() {
        if (subscription != null) return
        subscription = store.observe(::refresh)
        wallpaperSubscription = wallpaperStore.observe(::refresh)
        val saver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = refresh()
        }
        ContextCompat.registerReceiver(
            context,
            saver,
            IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        receiver = saver
        refresh()
    }

    fun stop() {
        subscription?.close()
        subscription = null
        wallpaperSubscription?.close()
        wallpaperSubscription = null
        receiver?.let { context.unregisterReceiver(it) }
        receiver = null
    }

    fun subscribe(listener: (ThemeSnapshot) -> Unit): AutoCloseable {
        listeners += listener
        listener(snapshot)
        return AutoCloseable { listeners -= listener }
    }

    fun refresh() {
        snapshot = build()
        listeners.toList().forEach { it(snapshot) }
    }

    fun select(id: String) = store.selectTheme(id)

    fun selectNext() = store.selectTheme(ThemeCatalog.next(snapshot.choice.theme.id, snapshot.choice.followSystem).id)

    fun setFollowSystem(value: Boolean) = store.setFollowSystem(value)

    fun setColor(slot: ThemeSlot, argb: Int) = store.setOverride(snapshot.choice.theme.id, slot, argb)

    fun resetColors() = store.resetTheme(snapshot.choice.theme.id)

    fun setAnimated(animated: Boolean) = store.setAnimated(animated)

    fun setWallpaperOpacity(percent: Int) = wallpaperStore.setOpacity(percent)

    fun setWallpaperDim(percent: Int) = wallpaperStore.setDim(percent)

    fun clearWallpaper() = wallpaperStore.clear()

    fun wallpaperStill(): File? = wallpaperStore.stillFile(snapshot.wallpaper.kind)?.takeIf { it.isFile }

    fun setWallpaperAnimate(enabled: Boolean) = wallpaperStore.setAnimate(enabled)

    fun probeWallpaper(uri: Uri): WallpaperProbe =
        WallpaperImport(context.contentResolver, wallpaperStore).probe(uri)

    fun importWallpaper(uri: Uri, trim: TrimRange? = null): WallpaperImportResult =
        WallpaperImport(context.contentResolver, wallpaperStore).import(uri, trim)

    fun wallpaperVideo(): File? = wallpaperStore.videoFile.takeIf { snapshot.wallpaper.kind == WallpaperKind.VIDEO && it.isFile }

    private fun build(): ThemeSnapshot {
        val choice = store.read().copy(night = systemIsDark())
        return ThemeSnapshot(choice, choice.palette(), motion = !powerSaving(), wallpaper = wallpaperStore.read())
    }

    private fun powerSaving(): Boolean =
        context.getSystemService(PowerManager::class.java)?.isPowerSaveMode == true

    private fun systemIsDark(): Boolean =
        (Resources.getSystem().configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}
