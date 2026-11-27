package com.spacekeyboard.keyboard

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot

@Composable
fun TouchSettingsScreen(layouts: LayoutController, onBack: () -> Unit) {
    val prefs = rememberLayoutSnapshot(layouts).prefs
    ScreenFrame(R.string.settings_touch_title, onBack) {
        FeedbackSection(prefs.feedback, layouts)
        ShakeSection()
        BodyText(R.string.feedback_note)
    }
}
