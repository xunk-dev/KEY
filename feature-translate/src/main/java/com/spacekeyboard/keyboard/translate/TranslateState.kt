package com.spacekeyboard.keyboard.translate

enum class TranslateStatus { IDLE, WORKING, DONE, FAILED }

data class TranslateState(
    val open: Boolean = false,
    val allowed: Boolean = true,
    val source: String = "",
    val sourceKind: TranslateSource = TranslateSource.NONE,
    val target: String = "en",
    val style: TranslateStyle = TranslateStyle.NEUTRAL,
    val route: TranslateRoute = TranslateRoute.DEVICE,
    val consent: Boolean = false,
    val status: TranslateStatus = TranslateStatus.IDLE,
    val result: String = "",
    val failure: TranslateFailure? = null,
    val remaining: Int? = null,
    val usedRoute: TranslateRoute? = null,
    val fellBack: Boolean = false,
) {
    val canUseResult: Boolean get() = status == TranslateStatus.DONE && result.isNotBlank()

    val aiReady: Boolean get() = route == TranslateRoute.AI && consent

    fun open(text: String, kind: TranslateSource): TranslateState =
        if (allowed) cleared().copy(open = true, source = text, sourceKind = kind, remaining = null) else this

    fun close(): TranslateState = cleared().copy(open = false, source = "", sourceKind = TranslateSource.NONE)

    fun setAllowed(value: Boolean): TranslateState = if (value) copy(allowed = true) else close().copy(allowed = false)

    fun withSettings(target: String, style: TranslateStyle, route: TranslateRoute, consent: Boolean): TranslateState {
        val changed = target != this.target || style != this.style || route != this.route || consent != this.consent
        val next = copy(target = target, style = style, route = route, consent = consent)
        return if (changed && status != TranslateStatus.WORKING) next.cleared() else next
    }

    fun setTarget(code: String): TranslateState =
        if (TranslateLanguages.isSupported(code) && code != target) copy(target = code).cleared() else this

    fun setStyle(value: TranslateStyle): TranslateState = if (value != style) copy(style = value).cleared() else this

    fun begin(): TranslateState =
        if (status == TranslateStatus.WORKING) this else cleared().copy(status = TranslateStatus.WORKING)

    fun finish(outcome: TranslateOutcome, fellBack: Boolean): TranslateState = when (outcome) {
        is TranslateOutcome.Done -> copy(
            status = TranslateStatus.DONE,
            result = outcome.text,
            remaining = outcome.remaining ?: remaining,
            usedRoute = outcome.route,
            fellBack = fellBack,
            failure = null,
        )
        is TranslateOutcome.Failed -> cleared().copy(status = TranslateStatus.FAILED, failure = outcome.failure)
    }

    private fun cleared(): TranslateState = copy(
        status = TranslateStatus.IDLE,
        result = "",
        failure = null,
        usedRoute = null,
        fellBack = false,
    )
}
