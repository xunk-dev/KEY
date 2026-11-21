package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.spacekeyboard.keyboard.images.ImageItem
import com.spacekeyboard.keyboard.images.ImagePicker
import com.spacekeyboard.keyboard.images.ImagesController
import com.spacekeyboard.keyboard.images.ui.ImagesActions

class ImeImagesActions(
    private val context: Context,
    private val images: ImagesController,
    private val connection: () -> InputConnection?,
    private val editor: () -> EditorInfo?,
) : ImagesActions {
    private val sender = ImageSender(context, images.store)

    override fun onKeys() = images.close()

    override fun onAdd() {
        images.close()
        ImagePicker.launch(context)
    }

    override fun onPick(item: ImageItem) {
        if (images.store.exists(item)) sender.send(item, connection(), editor()) else images.refresh()
    }
}
