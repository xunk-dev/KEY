package com.spacekeyboard.keyboard.translate

import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage as MlLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import java.util.concurrent.TimeUnit

object OnDeviceModels {
    private const val LIST_SECONDS = 10L
    private const val DOWNLOAD_SECONDS = 180L
    private const val DELETE_SECONDS = 30L
    private const val ENGLISH = "en"

    private val manager: RemoteModelManager get() = RemoteModelManager.getInstance()

    fun languages(): List<String> =
        TranslateLanguages.ordered.filter { it != ENGLISH && MlLanguage.fromLanguageTag(it) != null }

    fun downloaded(): Set<String>? = runCatching {
        Tasks.await(manager.getDownloadedModels(TranslateRemoteModel::class.java), LIST_SECONDS, TimeUnit.SECONDS)
            .map { it.language }
            .toSet()
    }.getOrNull()

    fun download(code: String): Boolean = runCatching {
        Tasks.await(manager.download(model(code), DownloadConditions.Builder().build()), DOWNLOAD_SECONDS, TimeUnit.SECONDS)
        true
    }.getOrDefault(false)

    fun delete(code: String): Boolean = runCatching {
        Tasks.await(manager.deleteDownloadedModel(model(code)), DELETE_SECONDS, TimeUnit.SECONDS)
        true
    }.getOrDefault(false)

    private fun model(code: String): TranslateRemoteModel =
        TranslateRemoteModel.Builder(MlLanguage.fromLanguageTag(code) ?: code).build()
}
