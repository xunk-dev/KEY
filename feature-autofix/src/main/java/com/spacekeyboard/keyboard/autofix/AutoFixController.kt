package com.spacekeyboard.keyboard.autofix

import android.content.Context

class AutoFixController(context: Context, private val clock: () -> Long = System::currentTimeMillis) {
    private val app = context.applicationContext
    private val prefs = AutoFixPrefsStore(app)
    private val store = ModelStore(app)
    private val model = ErrorModel()
    private val engine = AutoFixEngine(model, prefs.config(), ::today)
    private val typer = AutoFixTyper(engine)
    private var subscription: AutoCloseable? = null
    private var epoch = prefs.epoch()
    private var saved = 0

    fun start() {
        if (subscription != null) return
        store.load(model)
        saved = model.revision
        subscription = prefs.observe(::onPrefs)
        onPrefs()
    }

    fun stop() {
        flush()
        subscription?.close()
        subscription = null
    }

    var onFixed: ((original: String, corrected: String) -> Unit)?
        get() = typer.onFixed
        set(value) {
            typer.onFixed = value
        }

    fun setField(mayLearn: Boolean, mayFix: Boolean) = engine.setField(mayLearn, mayFix)

    fun type(label: String, zone: Int, field: TextField) {
        typer.type(label, zone, field)
        autosave()
    }

    fun space(field: TextField) {
        typer.boundary(" ", field)
        autosave()
    }

    fun beforeEnter(field: TextField) = typer.beforeEnter(field)

    fun delete(field: TextField) = typer.delete(field)

    fun reset() = typer.reset()

    fun flush() {
        if (model.revision == saved) return
        model.trim(today())
        store.save(model)
        saved = model.revision
    }

    private fun autosave() {
        if (model.revision - saved >= SAVE_EVERY) flush()
    }

    private fun onPrefs() {
        engine.config = prefs.config()
        if (epoch == prefs.epoch()) return
        epoch = prefs.epoch()
        model.clear()
        store.load(model)
        saved = model.revision
        engine.reset()
    }

    private fun today(): Int = (clock() / DAY_MS).toInt()

    private companion object {
        const val DAY_MS = 86_400_000L
        const val SAVE_EVERY = 120
    }
}
