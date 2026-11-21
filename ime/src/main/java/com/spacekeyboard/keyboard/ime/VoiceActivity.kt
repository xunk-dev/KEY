package com.spacekeyboard.keyboard.ime

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import com.spacekeyboard.keyboard.core.voice.VoiceLanguage

class VoiceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return
        val tag = intent.getStringExtra(VoiceInput.EXTRA_LANGUAGE) ?: VoiceLanguage.ENGLISH
        val request = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, tag)
            .putExtra(EXTRA_ADDITIONAL_LANGUAGES, VoiceLanguage.extraLanguages(tag).toTypedArray())
            .putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        try {
            startActivityForResult(request, REQUEST)
        } catch (missing: ActivityNotFoundException) {
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val text = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (resultCode == RESULT_OK && !text.isNullOrBlank()) {
            val result = Intent(VoiceInput.ACTION)
                .setPackage(packageName)
                .putExtra(VoiceInput.EXTRA_TEXT, text)
                .putExtra(VoiceInput.EXTRA_TOKEN, intent.getStringExtra(VoiceInput.EXTRA_TOKEN))
            sendBroadcast(result)
        }
        finish()
    }

    private companion object {
        const val REQUEST = 7
        const val EXTRA_ADDITIONAL_LANGUAGES = "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES"
    }
}
