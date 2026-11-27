package com.spacekeyboard.keyboard.ime

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.spacekeyboard.keyboard.clipboard.ClipEdit
import com.spacekeyboard.keyboard.clipboard.EditStatus
import com.spacekeyboard.keyboard.clipboard.ui.ClipEditorActions
import com.spacekeyboard.keyboard.clipboard.ui.ClipEditorHost
import com.spacekeyboard.keyboard.clipboard.ui.ClipEditorModel
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import com.spacekeyboard.keyboard.clipboard.R as ClipR

class ClipEditActivity : ComponentActivity(), ClipEditorActions {
    private var theme: ThemeController? = null
    private var id = NONE
    private var original = ""
    private var current = ""
    private var finished = false
    private var status by mutableStateOf<EditStatus?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = intent.getLongExtra(ClipEdit.EXTRA_ID, NONE)
        val source = ClipEdit.original(id) ?: savedInstanceState?.getString(KEY_ORIGINAL)
        if (source == null) {
            Toast.makeText(this, ClipR.string.clip_edit_closed, Toast.LENGTH_LONG).show()
            finished = true
            finish()
            return
        }
        original = source
        val stashed = ClipEdit.draftFor(id)
        current = stashed ?: source
        val model = ClipEditorModel(source, current, stashed != null)
        val controller = ThemeController(this).also { it.start() }
        theme = controller
        setContent {
            val palette = rememberThemeSnapshot(controller).palette
            Box(Modifier.fillMaxSize().background(Color(palette.background)).systemBarsPadding().imePadding()) {
                ClipEditorHost(palette, model, status, this@ClipEditActivity)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_ORIGINAL, original)
    }

    override fun onStop() {
        if (!finished) ClipEdit.keepDraft(id, current, original)
        super.onStop()
    }

    override fun onDestroy() {
        if (isFinishing) {
            if (!finished) ClipEdit.keepDraft(id, current, original)
            ClipEdit.finish(id)
        }
        theme?.stop()
        super.onDestroy()
    }

    override fun onChange(text: String) {
        current = text
        status = null
    }

    override fun onSave(text: String) {
        val outcome = ClipEdit.save(id, text)
        when (outcome.status) {
            EditStatus.OK -> done(if (outcome.merged) ClipR.string.clip_edit_merged else ClipR.string.clip_edit_saved)
            EditStatus.UNCHANGED -> done(null)
            else -> status = outcome.status
        }
    }

    override fun onClose() = done(null)

    private fun done(message: Int?) {
        finished = true
        ClipEdit.dropDraft(id)
        ClipEdit.leaving()
        message?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        finish()
    }

    private companion object {
        const val NONE = -1L
        const val KEY_ORIGINAL = "clip_edit_original"
    }
}
