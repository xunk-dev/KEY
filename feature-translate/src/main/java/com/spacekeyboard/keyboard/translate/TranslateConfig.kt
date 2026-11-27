package com.spacekeyboard.keyboard.translate

data class TranslateConfig(val url: String, val appId: String, val apiKey: String, val projectId: String) {
    val configured: Boolean
        get() = url.startsWith("https://") && appId.isNotBlank() && apiKey.isNotBlank() && projectId.isNotBlank()

    companion object {
        fun fromBuild(): TranslateConfig = TranslateConfig(
            BuildConfig.TRANSLATE_URL,
            BuildConfig.FIREBASE_APP_ID,
            BuildConfig.FIREBASE_API_KEY,
            BuildConfig.FIREBASE_PROJECT_ID,
        )
    }
}
