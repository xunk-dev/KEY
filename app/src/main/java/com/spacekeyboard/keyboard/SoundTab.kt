package com.spacekeyboard.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.spacekeyboard.keyboard.core.sound.ClickSoundChoice
import com.spacekeyboard.keyboard.core.sound.ClickSoundStore
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.layout.rememberLayoutSnapshot

@Composable
internal fun SoundTab(layouts: LayoutController) {
    val context = LocalContext.current
    val feedback = rememberLayoutSnapshot(layouts).prefs.feedback
    val store = remember(context) { ClickSoundStore(context) }
    val choice = rememberClickSound(store)
    SettingsCard { SoundBasics(feedback, layouts) }
    if (!feedback.sound) Notice(R.string.sound_off_note)
    ClickSoundSection(store, choice, feedback.soundVolume)
    BodyText(R.string.sound_volume_note)
}

@Composable
private fun rememberClickSound(store: ClickSoundStore): ClickSoundChoice {
    var choice by remember(store) { mutableStateOf(store.read()) }
    DisposableEffect(store) {
        val subscription = store.observe { choice = store.read() }
        onDispose { subscription.close() }
    }
    return choice
}
