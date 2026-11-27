package com.spacekeyboard.keyboard.translate

data class GateInput(
    val allowed: Boolean,
    val requested: Boolean,
    val text: String,
    val route: TranslateRoute,
    val consent: Boolean,
    val configured: Boolean,
    val remaining: Int?,
)

sealed interface GateDecision {
    data class Go(val route: TranslateRoute, val fellBack: Boolean) : GateDecision
    data class Stop(val failure: TranslateFailure) : GateDecision
}

object TranslateGate {
    fun decide(input: GateInput): GateDecision = when {
        !input.allowed -> GateDecision.Stop(TranslateFailure.FIELD_BLOCKED)
        !input.requested -> GateDecision.Stop(TranslateFailure.NOT_REQUESTED)
        input.text.isBlank() -> GateDecision.Stop(TranslateFailure.EMPTY)
        input.text.length > TranslatePolicy.MAX_TEXT -> GateDecision.Stop(TranslateFailure.TOO_LONG)
        input.route == TranslateRoute.DEVICE -> GateDecision.Go(TranslateRoute.DEVICE, false)
        !input.consent -> GateDecision.Stop(TranslateFailure.NO_CONSENT)
        !input.configured || input.remaining == 0 -> GateDecision.Go(TranslateRoute.DEVICE, true)
        else -> GateDecision.Go(TranslateRoute.AI, false)
    }
}
