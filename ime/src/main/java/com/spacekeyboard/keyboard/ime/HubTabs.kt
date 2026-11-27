package com.spacekeyboard.keyboard.ime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.spacekeyboard.keyboard.emoji.EmojiController
import com.spacekeyboard.keyboard.emoji.rememberEmojiSnapshot
import com.spacekeyboard.keyboard.emoji.ui.HubTab
import com.spacekeyboard.keyboard.emoji.ui.HubTabOrder
import com.spacekeyboard.keyboard.emoji.ui.rememberHubTabOrder
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.images.rememberImagesSnapshot

@Composable
internal fun rememberHubTabs(emoji: EmojiController, images: ImagesController): List<HubTab> {
    val emojiAllowed = rememberEmojiSnapshot(emoji).allowed
    val imagesAllowed = rememberImagesSnapshot(images).allowed
    val order = rememberHubTabOrder()
    return remember(emojiAllowed, imagesAllowed, order) {
        val available = buildList {
            if (emojiAllowed) add(HubTab.EMOJI)
            add(HubTab.SYMBOLS)
            add(HubTab.FACES)
            add(HubTab.ART)
            if (imagesAllowed) add(HubTab.IMAGES)
        }
        HubTabOrder.arrange(order, available)
    }
}
