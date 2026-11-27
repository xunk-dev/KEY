package com.spacekeyboard.keyboard.translate

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage as MlLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.concurrent.TimeoutException

internal class OnDeviceTranslator {
    fun translate(request: TranslateRequest): TranslateOutcome =
        runCatching { run(request) }.getOrElse { TranslateOutcome.Failed(failureOf(it)) }

    private fun run(request: TranslateRequest): TranslateOutcome {
        val source = detect(request.text) ?: return TranslateOutcome.Failed(TranslateFailure.UNSUPPORTED)
        val target = MlLanguage.fromLanguageTag(request.target) ?: return TranslateOutcome.Failed(TranslateFailure.UNSUPPORTED)
        if (source == target) return TranslateOutcome.Done(request.text, null, TranslateRoute.DEVICE)
        val missing = OnDeviceModels.missing(listOf(source, target)) ?: return TranslateOutcome.Failed(TranslateFailure.MODEL)
        if (missing.isNotEmpty()) return TranslateOutcome.Failed(TranslateFailure.NEEDS_MODEL, missing.joinToString(","))
        val options = TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(target).build()
        val translator = Translation.getClient(options)
        try {
            return TranslateOutcome.Done(await(translator.translate(request.text), TRANSLATE_SECONDS), null, TranslateRoute.DEVICE)
        } finally {
            translator.close()
        }
    }

    private fun failureOf(error: Throwable): TranslateFailure = when {
        error is TimeoutException -> TranslateFailure.NETWORK
        else -> ModelFailure.of(error).toTranslateFailure()
    }

    private fun detect(text: String): String? {
        val identifier = LanguageIdentification.getClient()
        try {
            val tag = await(identifier.identifyLanguage(text), DETECT_SECONDS)
            return if (tag == UNDETERMINED) null else MlLanguage.fromLanguageTag(tag)
        } finally {
            identifier.close()
        }
    }

    private fun <T> await(task: Task<T>, seconds: Long): T = Tasks.await(task, seconds, java.util.concurrent.TimeUnit.SECONDS)

    private companion object {
        const val UNDETERMINED = "und"
        const val DETECT_SECONDS = 10L
        const val TRANSLATE_SECONDS = 30L
    }
}
