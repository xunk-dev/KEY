package com.spacekeyboard.keyboard.translate

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TranslateController(context: Context) {
    private val app = context.applicationContext
    private val prefs = TranslatePrefsStore(app)
    private val config = TranslateConfig.fromBuild()
    private val client = HttpTranslateClient(config, AppCheckTokens(app, config)) { prefs.installId() }
    private val device = OnDeviceTranslator()
    private val worker: ExecutorService = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val listeners = LinkedHashSet<(TranslateState) -> Unit>()
    private var subscription: AutoCloseable? = null
    private var ticket = 0

    var state: TranslateState = fromPrefs(TranslateState())
        private set

    fun start() {
        if (subscription != null) return
        subscription = prefs.observe(::onPrefs)
        onPrefs()
    }

    fun stop() {
        subscription?.close()
        subscription = null
        ticket++
        main.removeCallbacksAndMessages(null)
        worker.shutdownNow()
    }

    fun subscribe(listener: (TranslateState) -> Unit): AutoCloseable {
        listeners += listener
        listener(state)
        return AutoCloseable { listeners -= listener }
    }

    fun open(text: String, kind: TranslateSource) = update { it.open(text, kind) }

    fun close() {
        ticket++
        update { it.close() }
    }

    fun setAllowed(value: Boolean) {
        if (!value) ticket++
        update { it.setAllowed(value) }
    }

    fun setTarget(code: String) = prefs.setTarget(code)

    fun setStyle(style: TranslateStyle) = prefs.setStyle(style)

    fun setRoute(route: TranslateRoute) = prefs.setRoute(route)

    fun grantConsent() = prefs.grantConsent()

    fun withdrawConsent() {
        ticket++
        prefs.withdrawConsent()
    }

    fun request() {
        val current = state
        val input = GateInput(
            current.allowed, true, current.source, current.route, current.consent, config.configured, current.remaining,
        )
        when (val decision = TranslateGate.decide(input)) {
            is GateDecision.Stop -> update { it.finish(TranslateOutcome.Failed(decision.failure), false) }
            is GateDecision.Go -> run(current, decision)
        }
    }

    private fun run(current: TranslateState, go: GateDecision.Go) {
        val mine = ++ticket
        update { it.begin() }
        val request = TranslateRequest(current.source, TranslatePolicy.AUTO, current.target, current.style)
        runCatching {
            worker.execute {
                val (outcome, fellBack) = translateNow(request, go)
                main.post { if (mine == ticket) update { it.finish(outcome, fellBack) } }
            }
        }.onFailure { update { it.finish(TranslateOutcome.Failed(TranslateFailure.SERVER), false) } }
    }

    private fun translateNow(request: TranslateRequest, go: GateDecision.Go): Pair<TranslateOutcome, Boolean> {
        if (go.route == TranslateRoute.DEVICE) return device.translate(request) to go.fellBack
        val online = client.translate(request)
        val spent = online is TranslateOutcome.Failed && online.failure == TranslateFailure.QUOTA
        if (!spent) return online to false
        return when (val offline = device.translate(request)) {
            is TranslateOutcome.Done -> offline.copy(remaining = 0) to true
            else -> offline to true
        }
    }

    private fun onPrefs() = update { fromPrefs(it) }

    private fun fromPrefs(base: TranslateState): TranslateState =
        base.withSettings(prefs.target(), prefs.style(), prefs.route(), prefs.consent())

    private fun update(change: (TranslateState) -> TranslateState) {
        val next = change(state)
        if (next == state) return
        state = next
        listeners.toList().forEach { it(next) }
    }
}
