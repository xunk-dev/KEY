package com.spacekeyboard.keyboard.translate

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import java.util.concurrent.TimeUnit

internal class AppCheckTokens(context: Context, private val config: TranslateConfig) {
    private val app = context.applicationContext
    private val check: FirebaseAppCheck? by lazy { create() }

    fun token(): String? {
        val client = check ?: return null
        return runCatching { Tasks.await(client.getAppCheckToken(false), TIMEOUT_SECONDS, TimeUnit.SECONDS).token }.getOrNull()
    }

    private fun create(): FirebaseAppCheck? {
        if (!config.configured) return null
        return runCatching {
            val firebase = FirebaseApp.getApps(app).firstOrNull { it.name == NAME } ?: FirebaseApp.initializeApp(app, options(), NAME)
            FirebaseAppCheck.getInstance(firebase).also { it.installAppCheckProviderFactory(AppCheckFactory.create()) }
        }.getOrNull()
    }

    private fun options(): FirebaseOptions = FirebaseOptions.Builder()
        .setApplicationId(config.appId)
        .setApiKey(config.apiKey)
        .setProjectId(config.projectId)
        .build()

    private companion object {
        const val NAME = "translate"
        const val TIMEOUT_SECONDS = 10L
    }
}
