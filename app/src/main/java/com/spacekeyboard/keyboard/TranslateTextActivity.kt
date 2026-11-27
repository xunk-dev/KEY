package com.spacekeyboard.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import com.spacekeyboard.keyboard.translate.TranslateClipboard
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.TranslateReport
import com.spacekeyboard.keyboard.translate.TranslateSource
import com.spacekeyboard.keyboard.translate.ui.TranslateActions
import com.spacekeyboard.keyboard.translate.ui.TranslatePanelHost
import com.spacekeyboard.keyboard.translate.R as TranslateR

class TranslateTextActivity : ComponentActivity(), TranslateActions {
    private lateinit var theme: ThemeController
    private lateinit var translate: TranslateController
    private var readOnly = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
        readOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
        theme = ThemeController(this).also { it.start() }
        translate = TranslateController(this).also {
            it.sink = ::deliver
            it.start()
            it.open(text, if (readOnly) TranslateSource.READONLY else TranslateSource.SELECTION)
        }
        setContent {
            val palette = rememberThemeSnapshot(theme).palette
            Box(Modifier.fillMaxSize().background(Color(palette.background)).systemBarsPadding()) {
                TranslatePanelHost(translate, palette, this@TranslateTextActivity, compact = false)
            }
        }
    }

    override fun onDestroy() {
        translate.stop()
        theme.stop()
        super.onDestroy()
    }

    override fun onKeys() = finish()

    override fun onPaste() = translate.type(TranslateClipboard.read(this))

    private fun deliver(text: String) {
        if (readOnly) {
            getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(null, text))
            Toast.makeText(this, getString(TranslateR.string.translate_copied), Toast.LENGTH_SHORT).show()
        } else {
            setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, text))
        }
        finish()
    }

    override fun onReport() {
        val subject = getString(TranslateR.string.translate_report_subject)
        if (TranslateReport.open(this, subject)) return
        Toast.makeText(this, getString(TranslateR.string.translate_report_no_mail, TranslateReport.SUPPORT_EMAIL), Toast.LENGTH_LONG).show()
    }
}
