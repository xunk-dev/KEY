package com.spacekeyboard.keyboard.decorate.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.spacekeyboard.keyboard.decorate.DecorCategory
import com.spacekeyboard.keyboard.decorate.R
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon

internal fun DecorCategory.icon(): SpaceIcon = when (this) {
    DecorCategory.ALL -> SpaceIcon.FILTER_ALL
    DecorCategory.FONTS -> SpaceIcon.DECOR_FONTS
    DecorCategory.FRAMES -> SpaceIcon.SYM_FRAMES
    DecorCategory.MARKS -> SpaceIcon.DECOR_MARKS
    DecorCategory.STRETCH -> SpaceIcon.DECOR_STRETCH
}

@Composable
internal fun DecorCategory.label(): String = stringResource(
    when (this) {
        DecorCategory.ALL -> R.string.decor_cat_all
        DecorCategory.FONTS -> R.string.decor_cat_fonts
        DecorCategory.FRAMES -> R.string.decor_cat_frames
        DecorCategory.MARKS -> R.string.decor_cat_marks
        DecorCategory.STRETCH -> R.string.decor_cat_stretch
    },
)
