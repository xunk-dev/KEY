package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme
import com.spacekeyboard.keyboard.translate.OnDeviceModels
import com.spacekeyboard.keyboard.translate.TranslateLanguages
import kotlin.concurrent.thread

private class ModelsState {
    var downloaded by mutableStateOf<Set<String>?>(null)
    var busy by mutableStateOf(false)
    var failed by mutableStateOf(false)

    fun refresh() {
        thread { downloaded = OnDeviceModels.downloaded() }
    }

    fun toggle(code: String) {
        if (busy) return
        val installed = downloaded?.contains(code) == true
        busy = true
        failed = false
        thread {
            val done = if (installed) OnDeviceModels.delete(code) else OnDeviceModels.download(code)
            downloaded = OnDeviceModels.downloaded()
            failed = !done
            busy = false
        }
    }
}

@Composable
internal fun TranslateModelsSection() {
    val state = remember { ModelsState() }
    LaunchedEffect(Unit) { state.refresh() }
    val locale = LocalConfiguration.current.locales.get(0)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SpaceText(stringResource(R.string.settings_translate_models_title), SpaceTheme.typography.title)
        TranslateNote(R.string.settings_translate_models_note)
        ModelsStatus(state)
        val installed = state.downloaded
        if (installed != null) {
            OnDeviceModels.languages().forEach { code ->
                val name = TranslateLanguages.displayName(code, locale)
                val label = if (code in installed) R.string.settings_translate_models_remove else R.string.settings_translate_models_get
                SpaceButton(stringResource(label, name), { state.toggle(code) })
            }
        }
    }
}

@Composable
private fun ModelsStatus(state: ModelsState) {
    when {
        state.busy -> TranslateNote(R.string.settings_translate_models_working)
        state.failed -> TranslateNote(R.string.settings_translate_models_failed)
        state.downloaded == null -> TranslateNote(R.string.settings_translate_models_loading)
    }
}
