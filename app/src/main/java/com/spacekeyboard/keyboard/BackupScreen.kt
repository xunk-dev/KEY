package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.components.SpacePrimaryButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.spacekeyboard.keyboard.designsystem.components.SpaceButton
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
internal fun BackupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var includeWords by rememberSaveable { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var repeat by rememberSaveable { mutableStateOf("") }
    var shown by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf(0) }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) message = BackupFiles.write(context, uri, includeWords, password.toCharArray())
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) message = BackupFiles.read(context, uri, password.toCharArray())
    }
    ScreenFrame(R.string.backup_title, onBack) {
        BodyText(R.string.backup_note)
        SettingsCard { SwitchRow(stringResource(R.string.backup_words), includeWords) { includeWords = it } }
        BodyText(R.string.backup_words_note)
        PasswordSection(password, repeat, shown, { password = it }, { repeat = it }, { shown = it })
        SpacePrimaryButton(stringResource(R.string.backup_export), {
            val problem = BackupRules.exportProblem(password, repeat)
            message = problem ?: 0
            if (problem == null) exporter.launch(BACKUP_FILE)
        }, icon = SpaceIcon.SAVE)
        SpaceButton(stringResource(R.string.backup_import), { importer.launch(BACKUP_TYPES) })
        if (message != 0) {
            SpaceText(
                text = stringResource(message),
                style = SpaceTheme.typography.title,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                color = SpaceTheme.colors.primary,
            )
        }
    }
}

private const val BACKUP_FILE = "space-keyboard-backup.json"
private val BACKUP_TYPES = arrayOf("application/json", "text/plain", "application/octet-stream")
