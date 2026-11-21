package com.spacekeyboard.keyboard.images

data class ImagesSnapshot(
    val open: Boolean = false,
    val allowed: Boolean = true,
    val items: List<ImageItem> = emptyList(),
    val filter: ImageFilter = ImageFilter.ALL,
) {
    val shown: List<ImageItem> get() = items.filter(filter::accepts)
}
