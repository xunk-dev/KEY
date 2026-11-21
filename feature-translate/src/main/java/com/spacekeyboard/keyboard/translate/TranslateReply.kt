package com.spacekeyboard.keyboard.translate

object TranslateReply {
    fun from(status: Int, body: String): TranslateOutcome = when {
        status == HTTP_OK -> parse(body)
        status == HTTP_TOO_MANY -> TranslateOutcome.Failed(TranslateFailure.QUOTA)
        status in HTTP_CLIENT_MIN..HTTP_CLIENT_MAX -> TranslateOutcome.Failed(TranslateFailure.REJECTED)
        else -> TranslateOutcome.Failed(TranslateFailure.SERVER)
    }

    private fun parse(body: String): TranslateOutcome {
        val fields = TranslateJson.parseFlat(body)
        val text = (fields?.get("text") as? String).orEmpty()
        if (text.isBlank()) return TranslateOutcome.Failed(TranslateFailure.SERVER)
        val remaining = (fields?.get("remaining") as? Number)?.toInt()
        return TranslateOutcome.Done(text, remaining, TranslateRoute.AI)
    }

    private const val HTTP_OK = 200
    private const val HTTP_TOO_MANY = 429
    private const val HTTP_CLIENT_MIN = 400
    private const val HTTP_CLIENT_MAX = 499
}
