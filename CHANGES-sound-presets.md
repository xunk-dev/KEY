# Ready-made click sounds (Customize > Sound tab)

- Two new cards under the click sound card: "Ready-made: calm sounds" (Marimba, Cotton, Droplet, Thock, Kalimba, Felt Piano, Bamboo, Glass) and "Ready-made: space sounds" (Glyph, Chirp, Sonar, Ion, Pulsar, Crystal, Warp, Moon). Tap a row: the sound becomes the click sound and plays once so the choice is heard at once. The selected row is highlighted. "Back to system sound" and "Choose from device" work as before.
- The sixteen sounds are WAV files in app/src/main/res/raw (click_calm_*, click_space_*): mono, 16-bit, 44.1 kHz, 60 to 150 ms, 184 KB in all. Calm sounds: no energy above 4 kHz, low sharpness, one short event per key. Space sounds are brighter (Chirp, Pulsar and Crystal the brightest) and Sonar and Pulsar are the two with a second event (an echo, a second pulse). Loudness is within about 2 dB across each group and about 1.5 dB between the groups. They are copied into the same click.wav the imported sounds use, so ClickSoundPlayer and KeyFeedback are unchanged and a preset is stored, reloaded and cleared exactly like a picked file.
- Code: core ClickSoundStore.install(stream, label, durationMs) copies through a temp file and commits only after the rename, so a failed install keeps the earlier choice. app SoundPresets.kt (CALM_PRESETS, SPACE_PRESETS, installPreset), SoundPresetsSection.kt (two cards and rows), one line in SoundSection.kt. Strings: sound_presets_calm, sound_presets_space, sound_presets_hint and sound_preset_<name>, sound_preset_<name>_note in values and values-ar.
- Known limit: the selected row is found by the saved label, so after switching the phone language the highlight is gone until a row is tapped again. The sound keeps playing.

## Tests (core/src/test/.../core/sound/ClickSoundStoreTest)
installCopiesTheClipAndMakesItTheChoice, installReplacesAnEarlierClip, aFailedInstallKeepsTheEarlierChoice.

## Optional later
In ClickSoundPlayer.fire the last argument of pool.play is 1f. For tonal presets a random rate from 1.0, 1.125, 1.25, 1.5 keeps overlapping taps in tune; for Cotton and Thock use 0.97 to 1.03.

## Status
- Not compiled and the tests have not been run (no Android SDK or Kotlin compiler here). check-strings, check-function-lines and check-binaries pass; the new files add no comment or line-count failure. The comment and line failures in KeyboardView, KeyboardPlacement, KeyPainter, SymbolRail and KeyboardGeometry were there before.
- Run: ./gradlew :core:test :app:assembleDebug
- By hand: Customize > Sound, scroll to both cards, tap each preset (it plays, the row highlights, the card above shows its name and length), type with sound on, Back to system sound clears the highlight, pick a file after a preset replaces it.
