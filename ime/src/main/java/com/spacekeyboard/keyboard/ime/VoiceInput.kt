package com.spacekeyboard.keyboard.ime

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.spacekeyboard.keyboard.core.voice.VoiceLanguage
import java.util.UUID

internal class VoiceInput(
    private val context: Context,
    private val prefs: VoicePrefs,
    private val onText: (String) -> Boolean,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var token = ""
    private var pending: String? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(received: Context, intent: Intent) {
            val text = intent.getStringExtra(EXTRA_TEXT) ?: return
            if (token.isEmpty() || intent.getStringExtra(EXTRA_TOKEN) != token) return
            token = ""
            handler.postDelayed({ deliver(text) }, DELIVERY_DELAY_MS)
        }
    }

    fun register() {
        val filter = IntentFilter(ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }

    fun unregister() {
        runCatching { context.unregisterReceiver(receiver) }
    }

    fun start(language: String) {
        token = UUID.randomUUID().toString()
        val intent = Intent(context, VoiceActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(EXTRA_LANGUAGE, VoiceLanguage.tagFor(language, prefs.arabicTag))
            .putExtra(EXTRA_TOKEN, token)
        context.startActivity(intent)
    }

    fun flushPending() {
        val text = pending ?: return
        pending = null
        deliver(text)
    }

    private fun deliver(text: String) {
        if (!onText(text)) pending = text
    }

    companion object {
        const val ACTION = "com.spacekeyboard.keyboard.ime.VOICE_RESULT"
        const val EXTRA_TEXT = "text"
        const val EXTRA_TOKEN = "token"
        const val EXTRA_LANGUAGE = "language"
        private const val DELIVERY_DELAY_MS = 450L
    }
}
