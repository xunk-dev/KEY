package com.spacekeyboard.keyboard

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spacekeyboard.keyboard.core.KeyboardStatus
import com.spacekeyboard.keyboard.core.KeyboardStatusReader
import com.spacekeyboard.keyboard.decorate.DecorController
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.ime.SettingsLink
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.theme.ThemeController

class MainActivity : ComponentActivity() {
    private var status by mutableStateOf(KeyboardStatus.DISABLED)
    private var screen by mutableStateOf(Screen.HOME)
    private lateinit var onboarding: OnboardingStore
    private lateinit var theme: ThemeController
    private lateinit var layouts: LayoutController
    private lateinit var emoji: EmojiController
    private lateinit var decor: DecorController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onboarding = OnboardingStore(this)
        if (!onboarding.isDone()) screen = Screen.SETUP
        ScreenSave.restore(savedInstanceState?.getString(ScreenSave.KEY), onboarding.isDone())?.let { screen = it }
        openRequestedScreen(intent)
        status = KeyboardStatusReader.read(this)
        theme = ThemeController(this).also { it.start() }
        layouts = LayoutController(this).also { it.start() }
        emoji = EmojiController(this).also { it.start() }
        decor = DecorController(this).also { it.start() }
        setContent { Content() }
    }

    @Composable
    private fun Content() {
        val home = { screen = Screen.HOME }
        when (screen) {
            Screen.SETUP -> OnboardingScreen(status, ::openInputSettings, ::showPicker, onFinish = ::finishSetup)
            Screen.HOME -> HomeScreen(status, ::openInputSettings, ::showPicker, onOpen = { screen = it })
            Screen.THEMES -> ThemesScreen(theme, layouts, onBack = home)
            Screen.CUSTOMIZE -> CustomizeScreen(theme, layouts, onEditor = { screen = Screen.EDITOR }, onBack = home)
            Screen.EDITOR -> LayoutEditorScreen(theme, layouts, onBack = { screen = Screen.CUSTOMIZE })
            Screen.EMOJI -> EmojiScreen(emoji, onBack = home)
            Screen.DECORATE -> DecorateScreen(decor, onBack = home)
            Screen.CLIPBOARD -> ClipboardScreen(onBack = home)
            Screen.TRANSLATE -> TranslateScreen(onBack = home)
            Screen.AUTOFIX -> AutoFixScreen(onBack = home)
            Screen.IMAGES -> ImagesScreen(onBack = home)
            Screen.SHORTCUTS -> ShortcutsScreen(onBack = home)
            Screen.PRIVACY -> PrivacyScreen(onBack = home)
            Screen.BACKUP -> BackupScreen(onBack = home)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ScreenSave.KEY, screen.name)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openRequestedScreen(intent)
    }

    private fun openRequestedScreen(request: Intent?) {
        if (request?.getBooleanExtra(SettingsLink.EXTRA_OPEN_CUSTOMIZE, false) != true) return
        request.removeExtra(SettingsLink.EXTRA_OPEN_CUSTOMIZE)
        if (onboarding.isDone()) screen = Screen.CUSTOMIZE
    }

    override fun onResume() {
        super.onResume()
        status = KeyboardStatusReader.read(this)
        theme.refresh()
        layouts.refresh()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        theme.refresh()
    }

    override fun onDestroy() {
        decor.stop()
        emoji.stop()
        layouts.stop()
        theme.stop()
        super.onDestroy()
    }

    private fun finishSetup() {
        onboarding.markDone()
        screen = Screen.HOME
    }

    private fun openInputSettings() {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }

    private fun showPicker() {
        getSystemService(InputMethodManager::class.java).showInputMethodPicker()
    }
}
