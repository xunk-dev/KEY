package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.view.inputmethod.InputConnection
import android.widget.Toast
import com.spacekeyboard.keyboard.translate.R
import com.spacekeyboard.keyboard.translate.TranslateReport
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.TranslateClipboard
import com.spacekeyboard.keyboard.translate.TranslateSource
import com.spacekeyboard.keyboard.translate.ui.TranslateActions

class ImeTranslateActions(
    private val context: Context,
    private val translate: TranslateController,
    private val connection: () -> InputConnection?,
) : TranslateActions {
    override fun onKeys() = translate.close()

    init {
        translate.sink = ::deliver
    }

    override fun onPaste() = translate.type(TranslateClipboard.read(context))

    private fun deliver(text: String) {
        val target = connection() ?: return
        target.commitText(text, 1)
        translate.close()
    }

    override fun onReport() {
        val subject = context.getString(R.string.translate_report_subject)
        if (TranslateReport.open(context, subject)) return
        Toast.makeText(context, context.getString(R.string.translate_report_no_mail, TranslateReport.SUPPORT_EMAIL), Toast.LENGTH_LONG).show()
    }
}

internal object TranslateInput {
    fun read(connection: InputConnection?): Pair<String, TranslateSource> {
        val selected = connection?.getSelectedText(0)?.toString().orEmpty()
        return if (selected.isNotEmpty()) selected to TranslateSource.SELECTION else "" to TranslateSource.NONE
    }
}
