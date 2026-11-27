package com.spacekeyboard.keyboard.ime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacekeyboard.keyboard.core.macro.MacroStore
import com.spacekeyboard.keyboard.core.theme.ThemePalette
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot

class MacroRenameActivity : ComponentActivity() {
    private var theme: ThemeController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getLongExtra(EXTRA_ID, NONE)
        val store = MacroStore(this)
        val current = store.all().firstOrNull { it.id == id }
        if (current == null) return finish()
        val controller = ThemeController(this).also { it.start() }
        theme = controller
        setContent {
            val palette = rememberThemeSnapshot(controller).palette
            Box(Modifier.fillMaxSize().background(Color(palette.background)).systemBarsPadding().imePadding()) {
                RenameForm(palette, current.name.orEmpty(), { store.rename(id, it); finish() }, { finish() })
            }
        }
    }

    override fun onDestroy() {
        theme?.stop()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_ID = "com.spacekeyboard.keyboard.ime.MACRO_ID"
        private const val NONE = -1L
    }
}

@Composable
private fun RenameForm(palette: ThemePalette, initial: String, onSave: (String) -> Unit, onCancel: () -> Unit) {
    var text by rememberSaveable { mutableStateOf(initial) }
    val focus = remember { FocusRequester() }
    val ink = Color(palette.label)
    val shape = RoundedCornerShape(14.dp)
    LaunchedEffect(Unit) { focus.requestFocus() }
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BasicText(stringResource(R.string.macro_rename), style = TextStyle(color = ink, fontSize = 18.sp))
        BasicTextField(
            value = text,
            onValueChange = { text = it.take(MacroStore.MAX_NAME) },
            modifier = Modifier.fillMaxWidth().clip(shape).border(1.dp, Color(palette.glow), shape).padding(14.dp).focusRequester(focus),
            textStyle = TextStyle(color = ink, fontSize = 16.sp),
            cursorBrush = SolidColor(Color(palette.glow)),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSave(text) }),
            decorationBox = { field ->
                Box {
                    if (text.isEmpty()) BasicText(stringResource(R.string.macro_rename_hint), style = TextStyle(color = ink.copy(alpha = 0.5f), fontSize = 16.sp))
                    field()
                }
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RenameButton(stringResource(R.string.macro_rename_save), Color(palette.glow)) { onSave(text) }
            RenameButton(stringResource(R.string.macro_rename_cancel), ink, onCancel)
        }
    }
}

@Composable
private fun RenameButton(label: String, tint: Color, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(Modifier.clip(shape).background(tint.copy(alpha = 0.16f)).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 22.dp, vertical = 10.dp)) {
        BasicText(label, style = TextStyle(color = tint, fontSize = 15.sp), maxLines = 1)
    }
}
