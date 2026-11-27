package com.spacekeyboard.keyboard

import android.content.Context
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.spacekeyboard.keyboard.core.sound.ClickSoundStore

internal class ClickPreset(
    @RawRes val raw: Int,
    @StringRes val label: Int,
    @StringRes val note: Int,
    val durationMs: Long,
)

internal val CALM_PRESETS = listOf(
    ClickPreset(R.raw.click_calm_marimba, R.string.sound_preset_calm_marimba, R.string.sound_preset_calm_marimba_note, 130L),
    ClickPreset(R.raw.click_calm_cotton, R.string.sound_preset_calm_cotton, R.string.sound_preset_calm_cotton_note, 80L),
    ClickPreset(R.raw.click_calm_droplet, R.string.sound_preset_calm_droplet, R.string.sound_preset_calm_droplet_note, 95L),
    ClickPreset(R.raw.click_calm_thock, R.string.sound_preset_calm_thock, R.string.sound_preset_calm_thock_note, 100L),
    ClickPreset(R.raw.click_calm_kalimba, R.string.sound_preset_calm_kalimba, R.string.sound_preset_calm_kalimba_note, 150L),
    ClickPreset(R.raw.click_calm_felt_piano, R.string.sound_preset_calm_felt_piano, R.string.sound_preset_calm_felt_piano_note, 135L),
    ClickPreset(R.raw.click_calm_bamboo, R.string.sound_preset_calm_bamboo, R.string.sound_preset_calm_bamboo_note, 100L),
    ClickPreset(R.raw.click_calm_glass, R.string.sound_preset_calm_glass, R.string.sound_preset_calm_glass_note, 115L),
)

internal val SPACE_PRESETS = listOf(
    ClickPreset(R.raw.click_space_glyph, R.string.sound_preset_space_glyph, R.string.sound_preset_space_glyph_note, 95L),
    ClickPreset(R.raw.click_space_chirp, R.string.sound_preset_space_chirp, R.string.sound_preset_space_chirp_note, 70L),
    ClickPreset(R.raw.click_space_sonar, R.string.sound_preset_space_sonar, R.string.sound_preset_space_sonar_note, 150L),
    ClickPreset(R.raw.click_space_ion, R.string.sound_preset_space_ion, R.string.sound_preset_space_ion_note, 70L),
    ClickPreset(R.raw.click_space_pulsar, R.string.sound_preset_space_pulsar, R.string.sound_preset_space_pulsar_note, 60L),
    ClickPreset(R.raw.click_space_crystal, R.string.sound_preset_space_crystal, R.string.sound_preset_space_crystal_note, 150L),
    ClickPreset(R.raw.click_space_warp, R.string.sound_preset_space_warp, R.string.sound_preset_space_warp_note, 85L),
    ClickPreset(R.raw.click_space_moon, R.string.sound_preset_space_moon, R.string.sound_preset_space_moon_note, 95L),
)

internal fun installPreset(context: Context, store: ClickSoundStore, preset: ClickPreset): Boolean =
    runCatching {
        context.resources.openRawResource(preset.raw).use {
            store.install(it, context.getString(preset.label), preset.durationMs)
        }
    }.getOrDefault(false)
