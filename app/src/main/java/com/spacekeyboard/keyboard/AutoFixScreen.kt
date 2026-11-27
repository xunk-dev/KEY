package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.autofix.AutoFixLevel
import com.spacekeyboard.keyboard.autofix.AutoFixPrefsStore
import com.spacekeyboard.keyboard.autofix.Learned
import com.spacekeyboard.keyboard.autofix.LearnedView
import com.spacekeyboard.keyboard.autofix.ModelStore
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
fun AutoFixScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { AutoFixPrefsStore(context) }
    val store = remember { ModelStore(context) }
    var config by remember { mutableStateOf(prefs.config()) }
    var learned by remember { mutableStateOf(loadLearned(store, config.level)) }
    ScreenFrame(R.string.autofix_title, onBack) {
        AutoFixNote(R.string.autofix_intro)
        AutoFixExample()
        SwitchRow(R.string.autofix_main_title, null, config.enabled) { prefs.setEnabled(it); config = prefs.config() }
        if (config.enabled) {
            SwitchRow(R.string.autofix_live_title, R.string.autofix_live_note, config.live) { prefs.setLive(it); config = prefs.config() }
            SwitchRow(R.string.autofix_words_title, R.string.autofix_words_note, config.words) { prefs.setWords(it); config = prefs.config() }
            LevelSection(config.level) { prefs.setLevel(it); config = prefs.config(); learned = loadLearned(store, it) }
        }
        LearnedSection(learned) { forget(store, prefs, it); learned = loadLearned(store, config.level) }
        ForgetAllSection { store.clear(); prefs.bumpEpoch(); learned = emptyList() }
        AutoFixNote(R.string.autofix_privacy)
    }
}

private fun loadLearned(store: ModelStore, level: AutoFixLevel): List<Learned> = LearnedView.list(store.load(), level)

private fun forget(store: ModelStore, prefs: AutoFixPrefsStore, item: Learned) {
    val model = store.load()
    LearnedView.forget(model, item)
    store.save(model)
    prefs.bumpEpoch()
}

@Composable
internal fun AutoFixNote(res: Int) {
    SpaceText(stringResource(res), SpaceTheme.typography.bodySmall, color = SpaceTheme.colors.onSurfaceVariant)
}

private fun onOffLabel(value: Boolean): Int = if (value) R.string.autofix_on else R.string.autofix_off

@Composable
private fun SwitchRow(title: Int, note: Int?, value: Boolean, onChange: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SpaceSectionTitle(stringResource(title))
        if (note != null) AutoFixNote(note)
        ChipGrid(listOf(true, false), value, ::onOffLabel, onChange)
    }
}

private fun levelLabel(level: AutoFixLevel): Int = when (level) {
    AutoFixLevel.GENTLE -> R.string.autofix_level_gentle
    AutoFixLevel.BALANCED -> R.string.autofix_level_balanced
    AutoFixLevel.BOLD -> R.string.autofix_level_bold
}

private fun levelNote(level: AutoFixLevel): Int = when (level) {
    AutoFixLevel.GENTLE -> R.string.autofix_level_gentle_note
    AutoFixLevel.BALANCED -> R.string.autofix_level_balanced_note
    AutoFixLevel.BOLD -> R.string.autofix_level_bold_note
}

@Composable
private fun LevelSection(level: AutoFixLevel, onPick: (AutoFixLevel) -> Unit) {
    ToolSection(R.string.autofix_level_title) {
        AutoFixNote(R.string.autofix_level_note)
        ChipGrid(AutoFixLevel.entries, level, ::levelLabel, onPick)
        AutoFixNote(levelNote(level))
    }
}

@Composable
private fun ForgetAllSection(onForgetAll: () -> Unit) {
    ConfirmAction(
        R.string.autofix_wipe_ask, R.string.autofix_wipe_yes, R.string.autofix_wipe_no, R.string.autofix_wipe, onForgetAll,
    )
}
