package com.spacekeyboard.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.spacekeyboard.keyboard.designsystem.components.SpaceEntry
import com.spacekeyboard.keyboard.designsystem.icons.SpaceIcon
import com.spacekeyboard.keyboard.designsystem.theme.BadgeTone

internal class HomeGroup(
    val title: Int,
    val tone: BadgeTone,
    val asList: Boolean,
    val entries: List<Pair<Int, Screen>>,
)

internal val keywordRes = mapOf(
    Screen.THEMES to R.string.search_kw_themes,
    Screen.CUSTOMIZE to R.string.search_kw_customize,
    Screen.EMOJI to R.string.search_kw_emoji,
    Screen.DECORATE to R.string.search_kw_decorate,
    Screen.CLIPBOARD to R.string.search_kw_clipboard,
    Screen.TRANSLATE to R.string.search_kw_translate,
    Screen.AUTOFIX to R.string.search_kw_autofix,
    Screen.IMAGES to R.string.search_kw_images,
    Screen.SHORTCUTS to R.string.search_kw_shortcuts,
    Screen.SETUP to R.string.search_kw_setup,
    Screen.PRIVACY to R.string.search_kw_privacy,
    Screen.BACKUP to R.string.search_kw_backup,
)

internal val homeGroups = listOf(
    HomeGroup(
        R.string.home_group_look,
        BadgeTone.Look,
        false,
        listOf(R.string.action_themes to Screen.THEMES, R.string.action_customize to Screen.CUSTOMIZE),
    ),
    HomeGroup(
        R.string.home_group_tools,
        BadgeTone.Tool,
        false,
        listOf(
            R.string.action_emoji to Screen.EMOJI,
            R.string.action_decorate to Screen.DECORATE,
            R.string.action_clipboard to Screen.CLIPBOARD,
            R.string.action_translate to Screen.TRANSLATE,
            R.string.action_autofix to Screen.AUTOFIX,
            R.string.action_images to Screen.IMAGES,
            R.string.action_shortcuts to Screen.SHORTCUTS,
        ),
    ),
    HomeGroup(
        R.string.home_group_help,
        BadgeTone.Help,
        true,
        listOf(
            R.string.action_setup to Screen.SETUP,
            R.string.action_privacy to Screen.PRIVACY,
            R.string.action_backup to Screen.BACKUP,
        ),
    ),
)

internal val homeEntries: List<Pair<Int, Screen>> = homeGroups.flatMap { it.entries }

internal fun Screen.icon(): SpaceIcon = when (this) {
    Screen.HOME -> SpaceIcon.KEYS
    Screen.THEMES -> SpaceIcon.THEME
    Screen.CUSTOMIZE, Screen.EDITOR -> SpaceIcon.CUSTOMIZE
    Screen.EMOJI -> SpaceIcon.EMOJI
    Screen.DECORATE -> SpaceIcon.DECORATE
    Screen.CLIPBOARD -> SpaceIcon.CLIPBOARD
    Screen.TRANSLATE -> SpaceIcon.TRANSLATE
    Screen.AUTOFIX -> SpaceIcon.AUTOFIX
    Screen.IMAGES -> SpaceIcon.IMAGES
    Screen.SHORTCUTS -> SpaceIcon.SHORTCUTS
    Screen.SETUP -> SpaceIcon.SETUP
    Screen.PRIVACY -> SpaceIcon.PRIVACY
    Screen.BACKUP -> SpaceIcon.BACKUP
}

internal fun Screen.tone(): BadgeTone = homeGroups.firstOrNull { group -> group.entries.any { it.second == this } }?.tone ?: BadgeTone.Tool

@Composable
internal fun HomeGroup.spaceEntries(onOpen: (Screen) -> Unit): List<SpaceEntry> =
    entries.map { (label, screen) -> SpaceEntry(stringResource(label), screen.icon(), tone) { onOpen(screen) } }
