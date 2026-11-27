package com.spacekeyboard.keyboard.emoji.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptic
import com.spacekeyboard.keyboard.designsystem.haptics.SpaceHaptics

class HubTabOrderStore(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE))

    fun read(): List<HubTab> {
        val known = prefs.getString(KEY, null).orEmpty().split(',').mapNotNull { name -> HubTab.entries.firstOrNull { it.name == name || (name == "SHAPES" && it == HubTab.FACES) } }
        return (known + HubTab.entries).distinct()
    }

    fun save(order: List<HubTab>) {
        prefs.edit().putString(KEY, order.joinToString(",") { it.name }).apply()
    }

    fun observe(onChange: () -> Unit): AutoCloseable {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> onChange() }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        return AutoCloseable { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private companion object {
        const val NAME = "space_hub_tabs"
        const val KEY = "order"
    }
}

@Composable
fun rememberHubTabOrder(): List<HubTab> {
    val context = LocalContext.current
    val store = remember { HubTabOrderStore(context) }
    var order by remember { mutableStateOf(store.read()) }
    DisposableEffect(store) {
        val subscription = store.observe { order = store.read() }
        onDispose { subscription.close() }
    }
    return order
}

@Stable
internal class HubReorder {
    var held by mutableStateOf<HubTab?>(null)
        private set
    var preview by mutableStateOf<List<HubTab>>(emptyList())
        private set
    private var left by mutableFloatStateOf(0f)
    private var width = 0f

    val active: Boolean get() = held != null

    fun start(order: List<HubTab>, x: Float, barWidth: Float, rtl: Boolean): Boolean {
        if (order.size < 2 || barWidth <= 0f) return false
        val slot = HubTabOrder.slotAt(x, barWidth, order.size, rtl)
        held = order[slot]
        preview = order
        width = barWidth
        left = HubTabOrder.slotLeft(slot, barWidth / order.size, order.size, rtl)
        return true
    }

    fun move(dx: Float, rtl: Boolean): Boolean {
        val tab = held ?: return false
        val count = preview.size
        val cell = width / count
        left = (left + dx).coerceIn(0f, width - cell)
        val target = HubTabOrder.slotAt(left + cell / 2f, width, count, rtl)
        if (target == preview.indexOf(tab)) return false
        preview = HubTabOrder.moveTo(preview, tab, target)
        return true
    }

    fun offsetOf(tab: HubTab, rtl: Boolean): Float {
        if (tab != held) return 0f
        val count = preview.size
        return left - HubTabOrder.slotLeft(preview.indexOf(tab), width / count, count, rtl)
    }

    fun finish(current: List<HubTab>, save: (List<HubTab>) -> Unit) {
        if (held == null) return
        val result = preview
        held = null
        if (result != current) save(result)
    }
}

internal fun Modifier.hubReorderGesture(
    reorder: HubReorder,
    rtl: Boolean,
    haptics: SpaceHaptics,
    current: () -> List<HubTab>,
    save: (List<HubTab>) -> Unit,
): Modifier = pointerInput(rtl) {
    detectDragGesturesAfterLongPress(
        onDragStart = { at ->
            if (reorder.start(current(), at.x, size.width.toFloat(), rtl)) haptics.perform(SpaceHaptic.LongPress)
        },
        onDrag = { change, amount ->
            if (reorder.active) {
                change.consume()
                if (reorder.move(amount.x, rtl)) haptics.perform(SpaceHaptic.Tick)
            }
        },
        onDragEnd = {
            if (reorder.active) haptics.perform(SpaceHaptic.DragEnd)
            reorder.finish(current(), save)
        },
        onDragCancel = { reorder.finish(current(), save) },
    )
}
