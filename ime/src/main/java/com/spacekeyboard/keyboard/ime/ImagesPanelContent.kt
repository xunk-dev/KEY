package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.ime.theme.ThemeController
import com.spacekeyboard.keyboard.ime.theme.rememberThemeSnapshot
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.images.ui.ImagesActions
import com.spacekeyboard.keyboard.images.ui.ImagesPanelHost

@Composable
fun ImagesPanelContent(theme: ThemeController, emoji: EmojiController, images: ImagesController, actions: ImagesActions) {
    ImagesPanelHost(images, rememberThemeSnapshot(theme).palette, actions, rememberHubTabs(emoji, images))
}
