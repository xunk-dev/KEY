package com.spacekeyboard.keyboard.images.ui

import com.spacekeyboard.keyboard.images.ImageItem

interface ImagesActions {
    fun onKeys()
    fun onAdd()
    fun onPick(item: ImageItem)
}
