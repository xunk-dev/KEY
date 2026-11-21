package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.view.inputmethod.InputConnection
import android.widget.Toast
import com.spacekeyboard.keyboard.translate.R
import com.spacekeyboard.keyboard.translate.TranslateReport
import com.spacekeyboard.keyboard.translate.TranslateController
import com.spacekeyboard.keyboard.translate.TranslatePolicy
import com.spacekeyboard.keyboard.translate.TranslateSource
import com.spacekeyboard.keyboard.translate.ui.TranslateActions

class ImeTranslateActions(
    private val context: Context,
    private val translate: TranslateController,
    private val connection: () -> InputConnection?,
) : TranslateActions {
    override fun onKeys() = translate.close()

    override fun onUse(text: String, kind: TranslateSource) {
        val target = connection() ?: return
        target.beginBatchEdit()
        if (kind == TranslateSource.ALL) {
            val before = target.getTextBeforeCursor(WINDOW, 0)?.length ?: 0
            val after = target.getTextAfterCursor(WINDOW, 0)?.length ?: 0
            target.deleteSurroundingText(before, after)
        }
        target.commitText(text, 1)
        target.endBatchEdit()
        translate.close()
    }

    override fun onReport() {
        val subject = context.getString(R.string.translate_report_subject)
        if (TranslateReport.open(context, subject)) return
        Toast.makeText(context, context.getString(R.string.translate_report_no_mail, TranslateReport.SUPPORT_EMAIL), Toast.LENGTH_LONG).show()
    }

    private companion object {
        const val WINDOW = TranslatePolicy.MAX_TEXT + 1
    }
}

internal object TranslateInput {
    fun read(connection: InputConnection?): Pair<String, TranslateSource> {
        val selected = connection?.getSelectedText(0)?.toString().orEmpty()
        if (selected.isNotEmpty()) return selected to TranslateSource.SELECTION
        val window = TranslatePolicy.MAX_TEXT + 1
        val before = connection?.getTextBeforeCursor(window, 0)?.toString().orEmpty()
        val after = connection?.getTextAfterCursor(window, 0)?.toString().orEmpty()
        val all = before + after
        return if (all.isNotBlank()) all to TranslateSource.ALL else "" to TranslateSource.NONE
    }
}
