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
    init { OnDeviceModels.attach(app) }
    private val client = HttpTranslateClient(config, AppCheckTokens(app, config)) { prefs.installId() }
    private val device = OnDeviceTranslator()
    private var executor: ExecutorService? = null
    private var modelWatch: AutoCloseable? = null
    private val main = Handler(Looper.getMainLooper())
    private val listeners = LinkedHashSet<(TranslateState) -> Unit>()
    private var subscription: AutoCloseable? = null
    private var ticket = 0
    private var pendingSend = false
    private val live = Runnable { request() }

    var sink: ((String) -> Unit)? = null

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
        pendingSend = false
        main.removeCallbacksAndMessages(null)
        modelWatch?.close()
        modelWatch = null
        executor?.shutdownNow()
        executor = null
    }

    private fun worker(): ExecutorService =
        executor?.takeUnless { it.isShutdown } ?: Executors.newSingleThreadExecutor().also { executor = it }

    fun subscribe(listener: (TranslateState) -> Unit): AutoCloseable {
        listeners += listener
        listener(state)
        return AutoCloseable { listeners -= listener }
    }

    fun open(text: String, kind: TranslateSource) {
        update { it.open(text, kind) }
        scheduleLive()
    }

    fun close() {
        ticket++
        pendingSend = false
        main.removeCallbacks(live)
        update { it.close() }
    }

    fun type(text: String) = edit { it.type(text) }

    fun backspace() = edit { it.backspace() }

    fun deleteWord() = edit { it.deleteWord() }

    fun setExpanded(value: Boolean) = update { it.setExpanded(value) }

    fun submit() {
        val current = state
        if (!current.open || current.source.isBlank()) return
        if (current.canUseResult) return deliver(current.result)
        main.removeCallbacks(live)
        pendingSend = true
        if (current.status != TranslateStatus.WORKING) request()
        if (state.status != TranslateStatus.WORKING) pendingSend = false
    }

    fun setAllowed(value: Boolean) {
        if (!value) ticket++
        update { it.setAllowed(value) }
    }

    fun setTarget(code: String) = prefs.setTarget(code)

    fun togglePin(code: String) = prefs.togglePin(code)

    fun setStyle(style: TranslateStyle) = prefs.setStyle(style)

    fun setRoute(route: TranslateRoute) = prefs.setRoute(route)

    fun grantConsent() = prefs.grantConsent()

    fun withdrawConsent() {
        ticket++
        prefs.withdrawConsent()
    }

    fun downloadModels() {
        val codes = state.missingModels
        if (codes.isEmpty()) return request()
        val mine = ++ticket
        pendingSend = false
        update { it.fetching() }
        val wifiOnly = prefs.modelsWifiOnly()
        codes.forEach(OnDeviceModels::dismissFailure)
        modelWatch?.close()
        var finished = false
        modelWatch = OnDeviceModels.subscribe { snap ->
            if (finished) return@subscribe
            if (mine != ticket) { finished = true; return@subscribe }
            val failed = codes.firstNotNullOfOrNull { (snap.status[it] as? ModelStatus.Failed)?.reason }
            when {
                failed != null -> {
                    finished = true
                    codes.forEach(OnDeviceModels::dismissFailure)
                    update { it.finish(TranslateOutcome.Failed(failed.toTranslateFailure()), false) }
                }
                codes.all(snap::isInstalled) -> { finished = true; request() }
            }
        }
        codes.forEach { OnDeviceModels.download(it, wifiOnly) }
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
            worker().execute {
                val (outcome, fellBack) = translateNow(request, go)
                main.post { if (mine == ticket) finishRun(outcome, fellBack) }
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

    private fun finishRun(outcome: TranslateOutcome, fellBack: Boolean) {
        update { it.finish(outcome, fellBack) }
        val send = pendingSend
        pendingSend = false
        if (send && state.canUseResult) deliver(state.result)
    }

    private fun deliver(text: String) {
        pendingSend = false
        sink?.invoke(text)
    }

    private fun edit(change: (TranslateState) -> TranslateState) {
        ticket++
        pendingSend = false
        update(change)
        scheduleLive()
    }

    private fun scheduleLive() {
        main.removeCallbacks(live)
        val current = state
        val ready = current.open && current.route == TranslateRoute.DEVICE && current.source.isNotBlank()
        if (ready) main.postDelayed(live, LIVE_DELAY_MS)
    }

    private fun onPrefs() {
        update { fromPrefs(it) }
        scheduleLive()
    }

    private fun fromPrefs(base: TranslateState): TranslateState =
        base.withSettings(prefs.target(), prefs.style(), prefs.route(), prefs.consent())
            .withLists(prefs.pinned(), prefs.recent())

    private fun update(change: (TranslateState) -> TranslateState) {
        val next = change(state)
        if (next == state) return
        state = next
        listeners.toList().forEach { it(next) }
    }
}

private const val LIVE_DELAY_MS = 450L
