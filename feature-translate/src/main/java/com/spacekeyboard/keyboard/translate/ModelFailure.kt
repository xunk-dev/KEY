package com.spacekeyboard.keyboard.translate

import com.google.mlkit.common.MlKitException

enum class ModelFailure {
    NETWORK,
    STORAGE,
    OTHER;

    fun toTranslateFailure(): TranslateFailure = when (this) {
        NETWORK -> TranslateFailure.NETWORK
        STORAGE -> TranslateFailure.STORAGE
        OTHER -> TranslateFailure.MODEL
    }

    companion object {
        fun fromCode(code: Int?): ModelFailure = when (code) {
            MlKitException.NETWORK_ISSUE -> NETWORK
            MlKitException.NOT_ENOUGH_SPACE -> STORAGE
            else -> OTHER
        }

        fun of(error: Throwable?): ModelFailure {
            var cause = error
            while (cause != null) {
                if (cause is MlKitException) return fromCode(cause.errorCode)
                cause = cause.cause
            }
            return OTHER
        }
    }
}

sealed interface ModelStatus {
    data class Downloading(val since: Long) : ModelStatus
    data class Failed(val reason: ModelFailure) : ModelStatus
}

data class ModelsSnapshot(
    val installed: Set<String>? = null,
    val loadFailed: Boolean = false,
    val status: Map<String, ModelStatus> = emptyMap(),
    val removing: Set<String> = emptySet(),
) {
    fun isInstalled(code: String): Boolean = installed?.contains(code) == true

    fun downloading(code: String): Boolean = status[code] is ModelStatus.Downloading

    fun withStarted(code: String, now: Long): ModelsSnapshot =
        copy(status = status + (code to ModelStatus.Downloading(now)))

    fun withSuccess(code: String): ModelsSnapshot =
        copy(installed = (installed ?: emptySet()) + code, status = status - code)

    fun withFailure(code: String, reason: ModelFailure): ModelsSnapshot =
        copy(status = status + (code to ModelStatus.Failed(reason)))

    fun withLoaded(codes: Set<String>): ModelsSnapshot =
        copy(installed = codes, loadFailed = false, status = status.filterKeys { it !in codes })

    fun withRemoving(code: String, on: Boolean): ModelsSnapshot =
        copy(removing = if (on) removing + code else removing - code)

    fun withoutStatus(code: String): ModelsSnapshot = copy(status = status - code)
}
