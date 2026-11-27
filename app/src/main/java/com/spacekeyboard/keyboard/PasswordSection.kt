package com.spacekeyboard.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.spacekeyboard.keyboard.designsystem.components.SpaceSectionTitle
import com.spacekeyboard.keyboard.designsystem.components.SpaceText
import com.spacekeyboard.keyboard.designsystem.theme.SpaceTheme

@Composable
internal fun PasswordSection(
    password: String,
    repeat: String,
    shown: Boolean,
    onPassword: (String) -> Unit,
    onRepeat: (String) -> Unit,
    onShown: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SpaceSectionTitle(stringResource(R.string.backup_password_title))
        BodyText(R.string.backup_password_note)
        PasswordField(R.string.backup_password_label, password, shown, onPassword)
        PasswordField(R.string.backup_password_repeat, repeat, shown, onRepeat)
        SettingsCard { SwitchRow(stringResource(R.string.backup_password_show), shown, onShown) }
    }
}

@Composable
private fun PasswordField(label: Int, value: String, shown: Boolean, onChange: (String) -> Unit) {
    val description = stringResource(label)
    val focus = remember { FocusRequester() }
    ToolField(Modifier.tapToFocus(focus)) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            textStyle = SpaceTheme.typography.body.copy(color = SpaceTheme.colors.onSurface),
            cursorBrush = SolidColor(SpaceTheme.colors.primary),
            visualTransformation = if (shown) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
            decorationBox = { inner ->
                if (value.isEmpty()) SpaceText(description, SpaceTheme.typography.body, color = SpaceTheme.colors.onSurfaceVariant)
                inner()
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(16.dp)
                .focusRequester(focus)
                .semantics { contentDescription = description },
        )
    }
}
