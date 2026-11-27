package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.spacekeyboard.keyboard.core.shake.ShakeAction
import com.spacekeyboard.keyboard.core.shake.ShakeDetector
import com.spacekeyboard.keyboard.core.shake.ShakePrefsStore

class ShakeController(context: Context, private val onShake: (ShakeAction) -> Unit) : SensorEventListener {
    private val store = ShakePrefsStore(context)
    private val sensors = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensors?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val detector = ShakeDetector()
    private var prefs = store.read()
    private var subscription: AutoCloseable? = null
    private var visible = false
    private var listening = false

    fun start() {
        if (subscription != null) return
        subscription = store.observe {
            prefs = store.read()
            sync()
        }
    }

    fun show() {
        visible = true
        sync()
    }

    fun hide() {
        visible = false
        sync()
    }

    fun stop() {
        hide()
        subscription?.close()
        subscription = null
    }

    private fun sync() {
        detector.threshold = prefs.sensitivity.threshold
        val want = visible && prefs.enabled && accelerometer != null
        if (want == listening) return
        listening = want
        if (want) {
            detector.reset()
            sensors?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        } else {
            sensors?.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val v = event.values
        if (v.size >= 3 && detector.feed(v[0], v[1], v[2], event.timestamp / NANOS_PER_MS)) onShake(prefs.action)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private companion object {
        const val NANOS_PER_MS = 1_000_000L
    }
}
