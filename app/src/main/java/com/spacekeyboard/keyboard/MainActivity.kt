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
import com.spacekeyboard.keyboard.dock.DockHost
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.ime.SettingsLink
import com.spacekeyboard.keyboard.ime.layout.LayoutController
import com.spacekeyboard.keyboard.ime.theme.ThemeController

class MainActivity : ComponentActivity() {
    private var status by mutableStateOf(KeyboardStatus.DISABLED)
    private var screen by mutableStateOf(Screen.HOME)
    private var origin by mutableStateOf(Screen.HOME)
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
        setContent { DockHost(screen.dockTab(origin), screen.showsDock(), onSelect = { screen = it.screen() }) { Content() } }
    }

    private fun navigate(target: Screen) {
        if (target in sharedScreens) origin = screen
        screen = target
    }

    @Composable
    private fun Content() {
        val back = { screen = screen.parent(origin) }
        when (screen) {
            Screen.SETUP -> OnboardingScreen(status, ::openInputSettings, ::showPicker, onFinish = ::finishSetup)
            Screen.HOME -> HomeScreen(status, theme, layouts, ::openInputSettings, ::showPicker, onOpen = ::navigate)
            Screen.CUSTOMIZE -> CustomizeScreen(theme, layouts, onBack = back)
            Screen.KEYBOARD_COLORS -> KeyboardColorsScreen(theme, layouts, onBack = back)
            Screen.KEYBOARD_LOOK -> LookSettingsScreen(theme, layouts, onBack = back)
            Screen.EDITOR -> LayoutEditorScreen(theme, layouts, onBack = back)
            Screen.SETTINGS -> SettingsScreen(layouts, onOpen = ::navigate, onBack = back)
            Screen.SETTINGS_TYPING -> TypingSettingsScreen(layouts, onOpen = ::navigate, onBack = back)
            Screen.SETTINGS_TOUCH -> TouchSettingsScreen(layouts, onBack = back)
            Screen.SETTINGS_DATA -> DataSettingsScreen(layouts, emoji, onOpen = ::navigate, onBack = back)
            Screen.EMOJI -> EmojiScreen(emoji, onBack = back)
            Screen.DECORATE -> DecorateScreen(decor, onBack = back)
            Screen.CLIPBOARD -> ClipboardScreen(layouts, onBack = back)
            Screen.TRANSLATE -> TranslateScreen(onBack = back)
            Screen.AUTOFIX -> AutoFixScreen(onBack = back)
            Screen.IMAGES -> ImagesScreen(onBack = back)
            Screen.SHORTCUTS -> ShortcutsScreen(onBack = back)
            Screen.DICTIONARY -> DictionaryScreen(onBack = back)
            Screen.PRIVACY -> PrivacyScreen(onBack = back)
            Screen.BACKUP -> BackupScreen(onBack = back)
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
