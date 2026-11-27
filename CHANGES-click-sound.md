# Custom click sound (Customize > Sound tab)

- New SOUND tab: sound switch and volume (moved from Touch), choose an audio file, trim the part (waveform, handles, zoom, preview), test, back to system sound.
- Formats: anything Android decodes (MP3, WAV, OGG, M4A/AAC, FLAC, Opus, AMR, 3GP, audio of MP4). Source up to 64 MB and 10 min; copied to cache, decoded to a mono 16-bit WAV (max 3 s, fades, peak normalised) in app storage, played with SoundPool.
- Code: core/sound (ClipMath, PeakBuilder, PcmClip, WavFile, PcmDecoder, PcmSample, StagedPeaks, ClickSoundImport, ClickSoundStore), ime ClickSoundPlayer + KeyFeedback, app Sound*.kt, SpaceIcon.SOUND.

## v48: review fixes
- Decoding: PcmSample reads 8-bit, 16-bit, 24-bit packed, 32-bit and float PCM; any other encoding fails the import instead of playing noise. The end-of-stream test in DecodePump is parenthesised.
- Import: an empty file now reports "could not be read" instead of "too big".
- ClickSoundPlayer: loading, ready and system states are explicit. The Test button waits for SoundPool to finish loading and then plays, so the first test after saving is no longer silent. A pending test is dropped when the sound is unloaded.
- ClipPreview: any RuntimeException from setDataSource ends the preview; a clip that starts at 0 plays without waiting for a seek callback.
- SoundImportState: a second pick or save is ignored while one is running; `::errorText` replaced by a lambda; the state variable in SoundSection no longer uses the soft keyword `import` as a name.

## v48: improvements
- The trim sheet survives rotation and process death: the staged file stays in cache, its peaks are kept next to it (click_stage.bin.peaks), and the selection and zoom are saved with rememberSaveable. A pick that is still being analysed when the screen turns is lost and has to be picked again.
- Settings search: the Customize entry now matches sound, click and audio (en) and صوت نقر نقرة (ar).
- Tab bar: with seven tabs the selected tab is weight 2 (was 2.3) and the side padding is 8 dp outer and 4 dp inner (was 12 and 5), so a 360 dp screen gives each unselected tab about 40 dp (it was about 38 dp with seven tabs, 43 dp with six). To go back, restore SELECTED_WEIGHT = 2.3f and the old paddings in CustomizeTab.kt.

## Tests (core/src/test/.../core/sound)
ClipMathInitialTest, ClipMathEditTest, ClipMathViewTest, PeakBuilderTest, PcmClipTest, WavFileTest, PcmSampleTest, StagedPeaksTest, ClickSoundStoreTest (with FakePreferences).

## Status
- Still not compiled and the tests have not been run: the build environment has no Android SDK, Kotlin compiler or network. The expected values of ClipMath and PcmClip tests were checked against a Python port of the same formulas.
- scripts: check-function-lines, check-strings, check-layouts, check-themes, check-manifest-permissions and check-binaries pass. check-comments and check-lines fail only on KeyboardView, KeyboardPlacement, KeyPainter, SymbolRail, KeyboardGeometry (comments) and KeyPainter (319 lines), as before these changes.
- Run: ./gradlew :core:test :ime:test :app:assembleDebug
