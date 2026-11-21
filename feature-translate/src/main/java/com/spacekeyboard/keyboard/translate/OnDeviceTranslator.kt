package com.spacekeyboard.keyboard.translate

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage as MlLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.concurrent.TimeUnit

internal class OnDeviceTranslator {
    fun translate(request: TranslateRequest): TranslateOutcome =
        runCatching { run(request) }.getOrElse { TranslateOutcome.Failed(TranslateFailure.MODEL) }

    private fun run(request: TranslateRequest): TranslateOutcome {
        val source = detect(request.text) ?: return TranslateOutcome.Failed(TranslateFailure.UNSUPPORTED)
        val target = MlLanguage.fromLanguageTag(request.target) ?: return TranslateOutcome.Failed(TranslateFailure.UNSUPPORTED)
        if (source == target) return TranslateOutcome.Done(request.text, null, TranslateRoute.DEVICE)
        val options = TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(target).build()
        val translator = Translation.getClient(options)
        try {
            await(translator.downloadModelIfNeeded(DownloadConditions.Builder().build()), DOWNLOAD_SECONDS)
            return TranslateOutcome.Done(await(translator.translate(request.text), TRANSLATE_SECONDS), null, TranslateRoute.DEVICE)
        } finally {
            translator.close()
        }
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

    private fun <T> await(task: Task<T>, seconds: Long): T = Tasks.await(task, seconds, TimeUnit.SECONDS)

    private companion object {
        const val UNDETERMINED = "und"
        const val DETECT_SECONDS = 10L
        const val DOWNLOAD_SECONDS = 180L
        const val TRANSLATE_SECONDS = 30L
    }
}
