## Unreleased - Click sound: ready-made sounds

- See CHANGES-sound-presets.md. Customize > Sound has a Ready-made: calm sounds card and Ready-made: space sounds, eight each, that install the chosen WAV as the click sound and plays it. core ClickSoundStore.install with tests; app SoundPresets, SoundPresetsSection. Not compiled here.

## Unreleased - Click sound: tests, review fixes, trim sheet kept on rotation

- See CHANGES-click-sound.md. core/test: ClipMath, PeakBuilder, PcmClip, WavFile, PcmSample, StagedPeaks and ClickSoundStore tests. core: PcmSample (8, 16, 24, 32-bit and float PCM), StagedPeaks. ClickSoundPlayer waits for SoundPool before a test play; the trim sheet and its selection survive rotation; the Customize search entry matches sound; the tab bar is a little tighter for seven tabs. Not compiled here.

## Unreleased - Background: trim long videos, live photos

- See CHANGES-wallpaper.md. Videos over 15 s open a trim sheet (filmstrip, two handles, lossless cut); Motion Photos play as a live background with a switch. Not compiled here.

## Unreleased - Keyboard background: photo or video with transparency

- See CHANGES-wallpaper.md. Customize > Style has a Background card: pick a photo or a muted looping video, set transparency and darkening, remove it. Not compiled here.

## Unreleased - New Customize screen: six tabs over a live keyboard

- See CHANGES-customize-tabs.md. CustomizeScreen is now one screen with six tabs (Style, Colors, Keys, Size, Tools, Touch) and the real keyboard fixed at the bottom; the Keys tab makes it draggable. New SpaceIcon.VIBRATE. The dock is hidden on this screen. Not compiled here.
- Bottom-row key functions: KeyFn, KeyFnSection, KeyAction.EMOJI/CLIPBOARD/TRANSLATE.

## Unreleased - App screens reorganized: Library, Customize, Settings

- See CHANGES-screens-layout.md.
- Dock has three tabs: Home, Customize, Settings. Themes, emoji and fonts live in a Library section on Home (LibrarySection). SettingsScreen and KeyboardColorsScreen added; CustomizeScreen is a hub of step buttons; ThemesScreen removed. Back from Dictionary, Privacy and Backup returns to the screen that opened them. Not compiled here.

## Unreleased - Emoji suggestions, suggestions per app and field, stricter incognito

- See CHANGES-suggest-context.md.
- core: Personal, FieldKind, ScopedStats, ScopedStore; Suggester.suggest(personal). feature-emoji: EmojiHints, HintParser, assets/emoji/hints.tsv. ime: EmojiOffer, PersonalVocabulary, GlideStyle (moved out of SuggestionController).
- Fix: SuggestionController now keeps a strong reference to its preference listener (SharedPreferences holds listeners weakly), so Clear learned words reaches the running keyboard.
- Strings: typing_emoji_suggestions, typing_app_suggestions and their hints (en, ar); typing_incognito and typing_clear_learned reworded. Tests: FieldKindTest, ScopedStatsTest, ScopedStoreTest, SuggesterPersonalTest, EmojiHintsTest, HintParserTest, EmojiOfferTest. Not compiled here.

## Unreleased - Convert numbers and dates, fix Arabic spelling slips

- See CHANGES-convert-spelling.md.

## Unreleased - Quick diacritics bar

- core: text/Tashkeel. ime: TashkeelBar, SuggestionController marks state, ToolbarContent chip and bar. A chip at the end of the suggestion strip opens a bar of eight marks; marks type after the letter and can be stacked. See CHANGES-tashkeel.md.
- Strings: marks_open, marks_close (en, ar). Tests: TashkeelTest. Not compiled here.

## Unreleased - Dialect detection for Arabic suggestions

- core: Dialect, DialectBook, DialectTracker; Suggester.suggest(dialect) boosts the words and next-word pairs of the detected dialect (Levantine, Egyptian or Gulf) over the mixed seed list and lowers words of another dialect. See CHANGES-dialect.md.
- data: dialect_*.txt and pairs_*.txt in core/src/main/res/raw.
- ime: SuggestionController detects the dialect from the text before the cursor and from committed words (only while learning is on).
- Tests: DialectTest, DialectDataTest. Not compiled here.

## Unreleased - Verification code chip (OTP) in the top bar

- ime: when a verification code is on the clipboard, a chip with the code appears in the top bar (first priority, before autofill and other offers). One tap enters it. It works for a single field, for one box per digit with automatic focus moves (even slow ones), for apps that spread a pasted code over the boxes, for hidden fields with or without cursor reports (no repeated digits), for short fields (the field is cleared and a notice is shown), for a partly typed code (only the missing digits are typed), for stale digits (cleared only when the field is certainly a code field) and for raw TYPE_NULL fields (key events).
- ime: optional second source, notifications (OtpNotificationService, a NotificationListenerService). Off by default; the user turns it on in Settings > Typing and grants notification access in Android settings. Android 15 and later can hide the code from notification listeners; this is stated in the setting and in the docs.
- ime: the code is typed with commitText or key events through ImeOtpTarget, not through KeyTyper.insert, so automation recordings never hold it. ImeOtpTarget.fits() also requires that a field reached after an automatic focus move has the same package and the same input type as the field the fill started in.
- core: otp package (OtpCode, OtpInbox, OtpVocabulary, OtpText, OtpExtractor, OtpFieldRules with OtpFit and OtpGate, OtpTarget, OtpFiller, OtpNotificationGate). A code lives in memory for at most five minutes.
- core: TypingPrefs.otpOffers (on) and otpNotifications (off) with KeyboardPrefsStore setters, resetAll and tests.
- Manifest: BIND_NOTIFICATION_LISTENER_SERVICE is allowed only for .OtpNotificationService in the ime module; scripts/check-manifest-permissions.py still rejects it anywhere else and still rejects accessibility services.
- Docs: PRIVACY.md (Verification codes) and DATA_SAFETY.md (section 6).
- Strings: otp_* in ime, typing_otp_* in app, en and ar.
- Tests: OtpExtractorTest, OtpFillerTest (with FormModels: single field, six boxes with delayed focus, spread paste, short field, hidden fields, partial code, stale digits, blocked, fake restart, field ending after the last digit), OtpFieldRulesTest, OtpNotificationGateTest, KeyboardPrefsStoreOtpTest. Not run with Gradle (no Gradle or Android SDK here). A line-by-line Python port of OtpFiller with the same form models, and of OtpExtractor, passed every scenario and phrase of these tests; the Kotlin itself was not compiled.
- Files: core/otp/*, ime/OtpClipSource, OtpNotificationText, OtpNotificationService, ImeOtpTarget, OtpOffer, OtpOfferChip (new), TypingExtras, FieldSetup, SuggestionController, ToolbarContent, SpaceInputService, SpaceInputServiceViews, AndroidManifest, app/OtpSection (new), TypingSection, strings

## Unreleased - Import and export of your dictionary, and bigger built-in word lists

- app: My dictionary can now save your words to a text file (Export words to a file) and read them back (Import words from a file). The file is plain text, one word per line, UTF-8. It uses the same file picker as Backup (CreateDocument and OpenDocument). Export with an empty dictionary says so and creates no file
- core: SuggestionStore.exportText (words most used first, one per line) and importText (returns how many words were added). Lines that WordStats.clean refuses (under 2 or over 24 characters, spaces inside) are skipped, and so are lines with control characters or the replacement character, so picking a wrong binary file adds nothing. A first-line BOM is ignored. Only new words are counted; a word already in the dictionary keeps its count. A word on the block list comes off it, like adding it by hand
- core: one import adds at most 2000 new words (WordStats.MAX_IMPORT) and stops when the dictionary holds 4000 words, so an import never pushes out words learned from typing. Files above 300 KB are refused (shared reader with Backup: BackupFiles.readLimited now takes a limit)
- core: built-in lists words_ar.txt and words_en.txt are much bigger and no longer repeat words (the old lists repeated 6 Arabic and 6 English words). Arabic grew from 618 to 5951 lines and English from 313 to 2965. The old lines keep their place at the top, so their ranking does not change; new words follow, roughly from common to rare
- core: the new words were written for this project, not taken from a dictionary or frequency list (see NOTICE.md). They cover Modern Standard Arabic and Egyptian, Levantine, Gulf and Maghrebi words; daily life, work, study, technology, food, travel, health, home and nature; verbs with their common forms (past, I and you and we, imperative, and the b- forms of Egyptian and Levantine for the most used verbs); and the same nouns with the article. English adds common words, contractions, nouns by topic and verb forms (-s, -ing, -ed and irregular forms). A word that differs only by hamza, alef maqsura or ta marbuta from an earlier one was not added again
- Strings: dictionary_files_note, dictionary_import, dictionary_export, dictionary_imported, dictionary_imported_none, dictionary_exported, dictionary_export_empty, dictionary_file_large and dictionary_file_failed, en and ar; search_kw_dictionary has new words
- Tests: SuggestionStoreTest has 7 new tests (export order, skipped lines, only new words counted, block list, the two limits, round trip, empty text). Not compiled or run: this sandbox has no Kotlin compiler or Android SDK, so the tests and the screen (DictionaryScreen, DictionaryFiles) were checked by reading and by scripts/check-all.sh (passes). The word lists were checked by script (no repeats, only letters, old lines kept in order). Speed with the bigger lists was not measured on a phone
- Files: core/suggest/WordStats, SuggestionStore, core/res/raw/words_ar.txt, words_en.txt, app/DictionaryScreen, DictionaryFiles (new), BackupFiles, strings

## Unreleased - Type by swiping (glide typing)

- ime: slide your finger over the letters of a word and lift it on the last letter. The keyboard types the best match and a space, and puts the other guesses in the suggestion bar; tapping one replaces the word. A faint trail follows the finger. It works on letter keys of the normal layouts, with Shift (one-time or locked) and at the start of a sentence (capital letter). Words you glide are learned like typed words
- ime: when the switch is on, letter keys are typed when you lift your finger, not when you touch them (this is what lets a swipe start without a stray letter). Key sound, vibration and the preview still happen on touch. Turn the switch off to type on touch as before
- Not used (letters type on touch as before): password and other raw-key fields, the Arabizi layout, and the paired-letter Arabic layout (two letters on one key)
- core: GlideDecoder compares the swipe with the path through the letters of each candidate word (resampled to 32 points, key-size units), keeps only words whose first and last letters are near where the finger started and stopped, and checks that every letter was passed near. The built-in word list and your own words are the candidates; your words rank higher the more you use them. Removed (blocked) words are never offered
- app: Settings > Typing > Type by swiping (on by default, included in reset and backup with the other keyboard prefs)
- Strings: typing_glide and typing_glide_hint, en and ar
- Tests: GlideDecoderTest (8 tests, compiled and run here with the Kotlin compiler: they pass; on the built-in English list with noisy swipes top guess 97%, within the first three 100%, about 0.3 ms per swipe) and KeyboardPrefsStoreTest. The touch, trail and suggestion parts (KeyboardView, GlideTrail, SuggestionController) were not compiled: no Android SDK here. Only scripts/check-all.sh and reading
- Files: core/suggest/GlideDecoder (new), core/layout/KeyboardPrefs, KeyboardPrefsStore, ime/GlideTrail (new), KeyboardView, KeyboardViewTouch (digit swipe helpers moved here to stay under 300 lines), KeyTouch, KeyNeighborsBuilder, SuggestionController, SpaceInputServiceViews, FieldSetup, TypingExtras, LayoutController, app/TypingSection

## Unreleased - My dictionary (see, add and delete learned words)

- app: new My dictionary screen (Tools on the home screen, also found by settings search). It lists the words the keyboard learned from your typing, most used first, with a Delete button on each. One field adds a word (Add word) and also filters the list while you type. Words are 2 to 24 characters with no spaces; anything else shows a short message. At most 100 rows are drawn at once, the rest are reached by searching
- core: WordStats.add and listed, SuggestionStore.words, addWord and removeWord. Delete uses the same block list as holding a suggestion, so a deleted word is not learned again; adding it again takes it off the block list. An added word ranks above the built-in list. The running keyboard reloads the words by itself when the screen changes them
- app: My dictionary is also opened from Settings > Typing and Settings > Data (next to Clear learned words)
- Strings: action_dictionary, dictionary_* and search_kw_dictionary, en and ar
- Tests: SuggestionStoreTest (add, refused words, delete then add again, order, higher count kept). Not compiled or run: no Android SDK or network here, so the screen and tests were checked by reading and by scripts/check-all.sh
- Files: core/suggest/WordStats, SuggestionStore, app/DictionaryScreen (new), Screen, MainActivity, HomeEntries, strings

## Unreleased - Copied text chip in the top bar (paste or translate)

- ime: when you copy text, a chip with the start of it appears in the top bar where the suggestions are, for two minutes. Tap the text to paste it in the field, tap the translate icon to open the translate bar with that text (the translation goes into the field), hold the chip to hide it. It also shows when you copied the text a moment before opening the field
- ime: it uses the clipboard history, so it follows the history switch and never shows an item from a locked section. It is not shown in password and other sensitive fields, for a clip the source app marks as sensitive, for secret keys and tokens (ClipKind.TOKEN), for images, for texts above 5000 characters, or after the same text was pasted or hidden. The translate icon is hidden above 2000 characters or when translate is not allowed in the field. Typing a word shows the suggestions instead, and the chip comes back when the word is gone
- app: Settings > Typing has a new switch "Show copied text in the top bar" (on by default), with a note. It is part of the keyboard prefs, so reset and backup include it
- Strings: copy_offer_* (ime), typing_copy_offers and typing_copy_offers_hint (app), en and ar
- Tests: KeyboardPrefsStoreTest (copyOffers default, round trip, reset). Not compiled or run: no Android SDK here, so the chip, ClipTextOffer and toolbar parts were checked by reading and by scripts/check-all.sh (passes)
- Files: feature-clipboard/ClipLatest, ime/ClipTextOffer, TextOfferChip (new), SuggestionController, ToolbarContent, PanelToggles, TypingExtras, FieldSetup, SpaceInputService, SpaceInputServiceViews, core/layout/KeyboardPrefs, KeyboardPrefsStore, app/TypingSection

## Unreleased - Move the cursor on the space key, and variables in text shortcuts

- ime: press and hold the space key for a moment (a third of a second), then slide sideways: the cursor moves one character per small step, left or right, with a soft tick for each step. Releasing does not type a space. A quick swipe on space still switches the language, and a long hold still opens the mic or picker, as before (KeyboardViewTouch, KeyTouch, KeyBehavior)
- core: text shortcuts accept variables: {date}, {time}, {day} (day name) and {clipboard} (the newest text on the clipboard), in any letter case. They are filled in when the shortcut expands or when you tap Insert, using the phone language and clock. The clipboard is read only if {clipboard} is used. The undo on Backspace puts back the typed word. The Text shortcuts screen lists the variables (ShortcutVars, TypingExtras.variables)
- Strings: snip_vars (en, ar)
- Tests: ShortcutVarsTest (compiled and run here with the other shortcut tests, 19 pass). The cursor drag and the Android parts were not compiled (no Android SDK here); scripts/check-all.sh passes

## Unreleased - Text shortcuts in Tools (type a word, get the saved text)

- ime: new Text shortcuts tile in Tools (it can also sit on the top bar). It opens a list of your shortcuts, each with Insert (types it now) and Delete (asks once), and a New shortcut button
- ime: New shortcut closes the screen and shows a bar above the keyboard. Type the shortcut word (for example addr) and tick, then type or paste the text it becomes and tick again. Cancel goes back to the list. A word that already exists says it will replace the saved one
- ime: the clipboard item menu has Make a text shortcut. It opens the same bar with the item text already filled, so you only type the word. This works for any saved or pinned text
- ime: typing the word then space turns it into the saved text. A new shortcut works at once in the current field, without leaving it (FieldSetup.refreshShortcuts)
- core: the text of a shortcut can now be up to 2000 characters (was 500), so repeated replies fit. ShortcutStore gains check (names the reason an entry is refused), has and triggerFor
- ime: Backspace right after a shortcut expanded puts the typed word back, so an accidental expansion is one key to undo (KeyTyper)
- ime: each shortcut row has a pencil to edit the word or the text. Making a shortcut from a clipboard item that already has one says "Already saved as ...". Moving to another field closes the shortcut bar, so keys are never captured by mistake
- Not done: expansion still happens only on space, not on Enter or punctuation; the text typed in the bar is one line (paste keeps new lines)
- Strings: snip_* (ime) and clip_menu_shortcut (clipboard), en and ar
- Tests: ShortcutTest (check reasons, full list, has and triggerFor, long replies with new lines). The core shortcut tests were compiled and run here (15 tests pass). The Compose and Android parts were not compiled (no Android SDK in the sandbox), only checked with scripts/check-all.sh, which passes
- Files: core/shortcuts/ShortcutStore, ShortcutTiles, ime/SnippetController, SnippetBar, SnippetPanel (new), MacroBar, MacroPanel, PanelDeck, SearchKeyRouter, ShortcutsPanelContent, TileSpecs, FieldSetup, ImeClipActions, SpaceInputService, SpaceInputServiceViews, feature-clipboard/ui/ClipActions, ClipMenu

## Unreleased - Emoji hub: the search key stays in place on every tab

- emoji: on the Images tab (and Art) the search key used to disappear and the tabs grew to fill the space. The key now stays in the same spot, dimmed and inactive where search is not available, so the tab bar looks the same on every tab
- Files: feature-emoji/ui/HubTabs.kt. Not compiled or run here

## Unreleased - Automation: names, repeat count, live paste, encryption, quick play

- ime: a recording can be renamed from Tools > Automation (pencil on its row, a small screen with one field). A recording without a name stays "Recording N"
- ime: new Repeat choice next to Speed: Once, 3, 5, 10 or No end (No end is the default, and the choice is remembered like the speed). When the count is reached the playback stops and the bar closes. The bar shows "Round 2 of 5"
- ime: new step type, live paste. While recording, after you paste an item from the clipboard panel the bar shows a small "Record as live paste" key that turns that last step into a step that pastes the newest clipboard text when it plays. It skips the step when the clipboard history is off or empty, when the newest item is a large text or in a locked section, and in sensitive fields. Nothing is pasted from a locked section
- core: recordings are stored encrypted (AES-GCM, Android Keystore key space_keyboard_macros). AesGcmCipher and KeystoreKeys moved from feature-clipboard to core/vault so both features share them. Old plain recordings move into the encrypted store the first time they are read, and the plain copy is removed only after the encrypted copy reads back correctly. See docs/PRIVACY.md
- app: Privacy and data has a new Clear all automation recordings button with the same single confirmation as the other clear buttons
- ime: a recording can be pinned with the star on its row. The pinned one gets a small play button on the top bar that starts it at once. It is a bar button you can drag like the others, and it is shown only while a recording is pinned (the bar saved by older versions gets the slot at the end of the left side)
- Not added, as asked: recording delete word and cursor moves
- Strings: macro_repeat*, macro_playing_of, macro_paused_of, macro_live_paste, macro_favorite_*, macro_quick_play, macro_rename* (ime) and macro_clear_all (app), en and ar
- Tests: MacroStoreTest (name, encryption at rest, migration, no key, favorite, repeat, clear), MacroLiveClipTest, MacroPlaybackTest (round limit), BarArrangementTest (quick play slot)
- Built on the v21 automation screen (cards and step timeline): each card shows the name, a star, a pencil, trash and play, and the header has Repeat next to Speed. A live paste step shows as a "Live paste" chip. The Send step from v21 is kept
- Files: core/vault/* (moved, plus TextCipher), core/macro/MacroStore, MacroSerial, MacroRepeat, MacroRecorder, MacroCursor, MacroCodec, MacroStep, Macro, core/toolbar/BarArrangement, feature-clipboard/ClipLatest (new), ClipboardController (two members made internal), ime/MacroController (setters renamed chooseSpeed, chooseRepeat, changeMode to avoid clashing with the property setters), MacroBar, MacroPanel, MacroRenameActivity (new), ToolbarContent, ImeClipActions, KeyTyper, SpaceInputService, SpaceInputServiceViews, DataSettingsScreen
- Not compiled and not run here: the sandbox has no Gradle, no Kotlin compiler and no network, so the new code and its tests were checked by reading only

## Unreleased - Automation (record and loop what you type)

- ime: new Automation tile in Tools. It opens a small screen with New recording, a speed choice (Slow, Normal, Fast, Very fast) and the list of recordings, each with Play and Delete (Delete asks once)
- ime: New recording closes the screen and shows a bar above the keyboard with a red dot, the step count and a Stop key. Typed text, space, Enter, Delete, pasted clipboard items, symbols, voice text and emoji are recorded, and you can switch panels while recording. Typing a letter and deleting it leaves no trace in the recording
- ime: Play repeats the recording in a loop, with a pause between rounds. The bar shows the step and round, a Pause / Resume key, the speed (tap to change it while it runs) and an X to exit. It also stops when the keyboard closes or there is no text field
- ime: nothing is recorded in password and other sensitive fields. Recordings stay on the phone (up to 30, 200 steps each)
- Not recorded: moving the cursor, delete word, cut/copy/undo keys. Autocorrect and text shortcuts do not run while playing, the text goes in as it was typed
- Strings: macro_* (en, ar). Icons: AUTOMATION, PLAY, PAUSE, STOP
- Tests: MacroRecorderTest, MacroStoreTest, MacroPlaybackTest (compiled and run). The Android and Compose parts are not compiled here (no Gradle dependencies in the sandbox)
- Files: core/macro/* (new), ime/MacroController.kt, MacroBar.kt, MacroPanel.kt (new), ime/KeyTyper.kt, PanelDeck.kt, TileSpecs.kt, ShortcutsPanelContent.kt, SpaceInputService.kt, SpaceInputServiceViews.kt (createShake moved here to keep the service under 300 lines), ImeEmojiActions.kt

## Unreleased - Emoji: press and hold sends more

- emoji: press and hold an emoji and it keeps sending, faster the longer you hold, instead of a steady few per second. After the hold starts it sends 1 at a time, then 2, 3 and finally 5 per beat, with a soft tick each time it speeds up. It stops by itself at 40 emoji, with a longer buzz, so a field is never flooded
- emoji: a quick tap still sends one. Emoji with skin tones keep long press for the tone keys. It works in the grid, in recents and in the search results
- emoji: each burst is committed as one piece of text, so it is fast and the emoji is added to recents once
- Tests: EmojiBurstPlanTest (stages, cap, growth over time). Not compiled or run here (no Gradle dependencies in the sandbox)

## Unreleased - Clipboard: very large texts, progress and send as file

- clipboard: a large copy now shows a "Saving large text..." card at the top of Recent, with a progress bar (a moving bar when the size is unknown) and a stop button. It runs in the background, so the keyboard stays usable. Stopping removes the partial file
- clipboard: Insert on a text above 100000 characters opens a short confirmation: Send as file (opens the share sheet with a .txt file), Paste anyway, or Cancel. Smaller large texts paste at once
- clipboard: a paste in progress shows a strip with the percent and the characters pasted, and a Stop button. It also stops when the panel is closed or the keyboard moves to another field. The panel stays open until the paste ends
- clipboard: large items show their size (for example 2.3M chars) on the card, the full view says that only the first 5000 characters are shown, and Edit is hidden for them. Share becomes Send as file above 100000 characters
- clipboard: shared and copied links now end in a real file name (clip-xxxxxx.txt) and the provider answers the display name, and it rejects any id that is not a 32 character hex id
- Strings: clip_saving_title, clip_saving_wait, clip_busy_percent, clip_busy_chars, clip_pasting, clip_stop, clip_paste_stopped, clip_paste_large, clip_send_file, clip_paste_anyway, clip_view_preview, clip_item_size (en, ar)
- Tests: ClipBusyTest (progress meter, cancel leaves no files, labels, file names, paste confirmation, locked items, busy state). Not compiled or run here (no Gradle or Kotlin compiler in the sandbox); scripts/check-all.sh passes

## Unreleased - Cleanup for the build checks

- All comments removed, SpaceInputService, KeyboardView, FloatContent and PanelDeck brought under the file and function limits (new files SpaceInputServiceViews, KeyboardViewTouch, FloatPopover), unused imports removed

## Unreleased - Settings split into four short screens

- app: Settings is now a list of four screens (Look and size, Typing, Touch and shake, Privacy and data) instead of one long page. Rarely used options sit under "More options" on each screen
- app: incognito and the clear-data actions moved to Privacy and data, with one confirmation each. "Keep clipboard open after pasting" moved to the Clipboard screen
- app: the Settings list shows a live status line under each entry. Clearing clipboard, recent emoji, images and autofix data is also available from Privacy and data
- Not compiled in the authoring environment (no Gradle dependencies available)

## Unreleased - Floating glass dock

- Floating bottom dock (Home, Library, Settings) with a top beam, icon pop and a keyboard-try button that opens a typing sheet. Hides while the system keyboard is up. See CHANGES-dock.md

## Unreleased - Swipe the number row to swap digits

- Sideways swipe on the number row swaps Arabic/Persian and Latin digits. See CHANGES-digits-swipe.md

## Unreleased - Shake to open a tool

- Optional: shake the phone while the keyboard is shown to open the calculator or another chosen tool. Off by default, sensor read only while the keyboard is visible. See CHANGES-shake.md

## Unreleased - Autofix understands neighbouring keys

- Letter and touch rules are learned only from slips on neighbouring keys, taken from the real key positions of the current layout; far changes stay whole-word rules only. See CHANGES-neighbor-slips.md

## Unreleased - Top bar: language buttons on the right by default, long-press and drag any button

- ime: the top bar is now laid out left to right in every language. By default the tools, emoji, translate and clipboard buttons are on the left, and the globe, EN and the Arabic button are on the right, whatever the system direction
- ime: press and hold any button (language buttons, globe, tools, emoji, translate, clipboard) and drag it to any spot on the bar. The other buttons make room while you drag, so no gap is left. The order is saved and included in backups
- ime: while a word is being typed the language buttons hide and the suggestions fill the middle, with no gap
- ime: TalkBack actions "Move left" and "Move right" on every button
- core: BarArrangement (pure Kotlin, 12 tests run and passing on the JVM). "Reset toolbar order" and "Reset all" also restore the default bar
- Strings: toolbar_move_left, toolbar_move_right, editor_toolbar_bar_hint (en, ar)
- Not compiled with Android here: run the Gradle build, then check on a device that releasing a drag does not press the button, in RTL and LTR, all three looks

## Unreleased - Calculator: new tile in the tools panel (design 4, glass with side column)

- ime: new "Calculator" tile (calculator icon) in the tools panel. It opens a calculator in the keyboard area; the toolbar stays on top and its tools button turns into the keyboard button to go back
- ime: design 4 (glass with a side column): result display with a small expression line above it, a circular delete button at the top right, a 3 column pad (C, insert result, percent, digits, decimal point, thousands separator) and a side column (divide, multiply, minus, plus, and a glowing equals key). Colours follow the active look (galaxy, day, eclipse); the galaxy look also gets the soft glow backdrop
- ime: delete is on the right, next to the display. Hold it to repeat. The insert result key is in the pad where delete used to be: it types the result into the field, then returns to the keyboard (the calculation is kept for next time)
- ime: digits, decimal point and percent follow the "native digits" typing setting, the layout is always left to right so the side column stays on the right in Arabic and English
- core: new calc package. CalcEval (precedence, percent that is relative after plus and minus, 15 significant digits, divide by zero), CalcReducer (key rules, no double dots or leading zeros, 15 digit cap, equals then digit starts over, equals then operator continues), CalcFormat (digit style, thousands grouping, display and insert text)
- designsystem: CALCULATOR icon
- Strings: toolbar_calculator, calc_* (en, ar)
- Tests: CalcEvalTest, CalcStateTest, CalcFormatTest (40 tests, run and passing with the Kotlin compiler on the JVM)
- Not compiled with Android here: the three Compose files were written against APIs already used in this project. Run the Gradle build and :core:test :ime:test, then check on a device: all three looks, RTL and LTR, hold on delete, the glow on equals, insert result into a text field, opening other panels while the calculator is open (it must close), the keyboard height at small and large height settings

## Unreleased - Keyboard dimming (night and day), replaces the keyboard lighting idea

- ime: the "Keyboard lighting" tile (bulb) is now "Keyboard dimming" (moon icon). It opens a full-width bar in the toolbar slot above the number row: moon on the right, a night-to-day slider, sun, and a close button. Gold border, same look as the floating bar
- ime: the slider really dims the whole keyboard (keys, toolbar, symbols, emoji and the other panels) with a hardware layer colour filter (DimFilter). 100 percent is normal, the minimum is 20 percent. The bar itself stays bright so it can always be used; closing it keeps the chosen level
- ime: fills from the right in Arabic, TalkBack progress actions, thumb with a soft glow
- core: DimPrefs (level only) replaces LightingPrefs and is cleared by reset all; the old light_* settings are removed on reset. Key glow and colour dots are gone (KeyLight removed, KeyPainter no longer draws a glow)
- designsystem: MOON, SUN, CLOSE icons (BULB removed)
- Strings: toolbar_dim, dim_night, dim_day, dim_done, dim_level_value (en, ar)
- Tests: DimPrefsTest, DimLevelTest. DimPrefs logic was run on the JVM and passes; the rest needs the Gradle build
- Not compiled with Android here: run the build and :core:test :ime:test, then check on a device: dimming on all looks and panels, the bar in RTL and LTR, the filter on ComposeView hosts (layer type), and that the bar stays bright

## Unreleased - Emoji: fixed-height search and in-sync category tabs

- emoji: opening search no longer makes the keyboard taller. The tab bar is replaced by a search row (close button, search field, results) that grows out of the search button, the toolbar slot is used for it, and the keys stay below, so the total height equals the normal keyboard height
- emoji: results show in the same row, next to the field. Long-press an emoji with skin tones to see the tones in that row. The "Type with the keyboard below" text is gone; an empty field shows "Search emoji..." instead
- emoji: the category tabs follow the swipe at once. The pager is now the single source of truth: tab highlight reads the pager target page, a tab tap scrolls the pager directly, and the saved group is written only after the swipe settles. This removes the delay and the fight between the tabs and the swipe
- emoji: neighbouring pages are prepared ahead (beyondViewportPageCount 1) and the snap after a swipe is faster. The tab strip only scrolls when the selected tab is off screen and then centres it
- Strings: emoji_search_placeholder replaces emoji_search_hint (en, ar)
- Not compiled with Android here: run the Gradle build, then check on a device: open and close search (height must not change), type Arabic and English, long-press a skin-tone result, swipe fast across all groups while tapping tabs, rotate, RTL and LTR

## Unreleased - Floating keyboard: design A (top bar) finished and wired

- ime: the floating keyboard is now the "A - top bar" design. A slim gold bar sits above the keyboard with four controls: opacity (opens a slider), centre, a wide drag handle, and dock. Drag the handle to move the keyboard anywhere on the screen
- ime: two L-shaped corner marks at the bottom corners resize the keyboard. The left one changes width from the left side, the right one from the right side, and both change the height. Width, height, position and opacity are remembered
- ime: opacity slider (30 to 100 percent) in a small card above or below the bar, whichever has room. It follows the layout direction (fills from the right in Arabic) and has TalkBack progress actions
- ime: tools panel has a new "Floating keyboard" tile (new icon). Press it and the keyboard floats and lands in the centre of the screen; press it again to dock. The Mode tile still cycles through all modes, floating included
- ime: DockRoot is rebuilt on FloatFrame and FloatChrome. The old top handle (FloatHandle) is gone. Only the keyboard, the bar, the corner marks and the open slider take touches, the rest of the screen stays with the app. The app behind is not resized while the keyboard floats
- ime: the bar and corner marks start hidden, so they never show over the docked keyboard
- Tests: FloatOpacityTest (slider position to percent, left to right and right to left, clamping)
- Not compiled with Android here: run the Gradle build, :ime:test and scripts/check-all.sh (KeyPainter and ClipTab checks fail before this change). Check on a device: moving and resizing with the keyboard open in a text field, the slider in both RTL and LTR, rotation, and the touch region (taps outside the keyboard must reach the app)

## Unreleased - Clipboard: slim "history is off" strip on every tab

- clipboard: when history is off, a slim strip under the tabs says so and has a Turn on button. It shows on Recent, Pinned and Saved, so the notice no longer hides the page and no longer depends on which tab is first. The big centred "Turn on" view in Recent is removed; Recent shows its normal empty note
- Strings: clip_off_short, clip_enable_description (en, ar). clip_off stays for other uses
- Not compiled with Android here: check the strip on a device, in both LTR and RTL

## Unreleased - Clipboard: reorder the tabs, the first tab opens first

- clipboard: press and hold a tab (Recent, Pinned, Saved), drag it over another one and release to move it. The other tabs slide aside while you drag. Nothing is saved until you lift your finger; a system cancel puts everything back
- clipboard: the first tab of the order is the one the clipboard opens on, every time it is opened from the toolbar. Coming back from Edit, Share or the unlock prompt keeps the tab you were on. Default order is unchanged (Recent, Pinned, Saved)
- clipboard: swipe left or right on the list, the slide direction and the bar indicator all follow the new order. In Arabic the first tab is on the right
- clipboard: TalkBack gets "Move tab earlier / later" actions on each tab. Tapping still works and a tap that ends a drag does not switch tabs
- clipboard: the order is stored in ClipPrefsStore (tab_order). A damaged or incomplete value is repaired (unknown names dropped, missing tabs appended), so a tab can never disappear
- clipboard: one-time tip (toast) on the first manual tab switch, skipped once the user has reordered
- app: Clipboard settings has a Tab order section with a note and a "Restore default order" button, shown only when the order was changed
- Tests: ClipTabOrderTest, ClipSessionTest (opens on first tab), ClipPrefsStoreTest (persist, reset, damaged value). ClipSessionTest.everyTimeGrantEnds... now selects Saved after reopening, because reopening lands on the first tab
- Not compiled with Android here: run the Gradle build and :feature-clipboard:test before merging. The drag gesture (ClipTabBar.kt) needs a check on a device, in both LTR and RTL

## Unreleased - Keyboard size screen: centred width and a calmer layout

- core: KeyboardPrefs.widthPercent (50 to 100, default 100). In the normal mode the keyboard is centred at that width; the one-hand modes keep oneHandPercent
- ime: the left and right grips shrink the keyboard from both sides in the normal mode and no longer switch to one-hand mode. The centre handle still switches sides
- ime: the size screen shows a gold frame around the keyboard, slim edge grips, and one row of round buttons (reset, move, done) with a small width and height tag. The top bar and the wide bottom buttons are removed
- Tests: KeyboardGeometryTest (narrower normal width stays centred, no rail)
- Not compiled with Android here: run the Gradle build, the unit tests and scripts/check-*.sh before merging

## Unreleased - Stable keyboard: nothing changes size, shape or place on its own

Rule: a key, an icon or a toolbar button never changes size, shape or position because of the page, the language, the field or the text being typed. The user learns one size and one place.

- core: PageRows. Letters, numbers, symbols and edit pages all have the same number of rows (5 with the number row, 4 without). Before, ABC to 123 or the symbols page changed the keyboard height and the row height (rows = 5, 4, 5, 4), so every key resized and the app behind jumped. Without the number row the symbols page drops its digit row; with it the numbers page gets one extra symbol row and the edit page is rebuilt in five rows
- layouts: shared keys sit in the same place on every page and language. Enter is 11.8% of the row, Backspace 14% on the row above the bottom row, the Shift / symbols-toggle slot 14%, the bottom row is 10 / 10 / 8.2 / space / 8.2 / 11.8 (Persian keeps its extra key). Before, Enter was 11.8, 12, 20, 15 and 25% wide and Backspace 11.5 to 17% depending on the page
- ime: LayoutSnapshot.stableRows drives KeyboardView.onMeasure and PanelDeck, so every panel and the keyboard share one height
- ime: icons (Enter, Backspace, Shift, arrows, undo and redo) are sized from one shared unit per page (the narrowest icon key). Before each icon was sized from its own key width, so Enter was smaller than Backspace and the edit arrows were larger
- ime: label sizes are uniform. All single letters share one size and all function labels (EN, 123, ABC, Select, Copy...) share another, fixed at 70% of the letter size. Before each key shrank to fit by itself, so "123" and "Select" were smaller than "EN"
- ime: the letter hint (long-press character) is drawn in the corner and the letter is no longer pushed down when a key has a hint. Before letters with a hint sat lower than letters without, and turning the number row on or off moved them
- ime: Enter is always its icon. A field asking for Go, Search, Send, Next or Done no longer swaps the icon for a word; the word is read by accessibility (describeKey)
- ime: the space bar shows the mic at a fixed distance from its left edge and the language name centred, on every page. Before the pair moved with the width of the language name and the name disappeared on the numbers and edit pages
- ime: toolbar buttons keep their size while suggestions show (the compact 38dp mode is removed). A button that is not allowed in the current field (emoji, translate, clipboard in a password field) stays in place, dimmed, instead of disappearing and pushing the others
- designsystem: SpaceSteadyIndication. Keyboard panels dim a pressed control to 60% instead of shrinking it to 96.5% (SpaceTheme steadyPress). The app keeps its own press scale
- Tests: PageRowsTest (same rows on every page, Enter and Backspace widths, space bar edges)
- Not compiled with Android here: run the Gradle build, the unit tests and scripts/check-*.sh before merging. scripts/check-layouts.py already fails on the Arabic style files (id and letters checks) before this change

## Unreleased - Choice of Arabic letter layout (11 / 10 per row, iOS 11, paired, no dots)

- core: ArabicStyle with five layouts: ELEVEN (default, ض ص ث ق ف غ ع ه خ ح ج / ش س ي ب ل ا ت ن م ك ط / ذ ء ؤ ر ى ة و ز ظ د), TEN, IOS, PAIRED, PLAIN. Files: layout_ar.json (ELEVEN), layout_ar_ten.json, layout_ar_ios.json, layout_ar_pair.json, layout_ar_plain.json. Rows have equal total weight; every letter is reachable by a key or a long press
- core: paired keys (KeySpec.pair, JSON "pair": ["ض","ص"]) show two letters. Tapping the left half types the left letter, the right half the right letter; long press gives extra letters. The ime picks the letter in KeyboardView.picked from the touch offset
- core: the No dots layout types the dotless letters shown on the keys (ٮ ٯ ڡ ں); long press gives the dotted letters
- core: the choice is stored (arabic_style) and a saved key order for "ar" is dropped when the style changes. LayoutRepository.base/arranged take the style
- ime: when the number row is shown, digit hints are hidden on the letter keys (they showed 1 2 3 on the first letter row). Number keys offer the other numeral system on a long press, and a layout with 11 keys in the top row gets an eleventh ؟ key (hold: @) on the number row so the columns line up
- app: Settings > Customize has an Arabic letter layout section: five cards with a small preview of each layout and a radio button
- Tests: ArabicStylesTest, LayoutFilesTest (letters reachable by hold)
- Not compiled with Android here: run the Gradle build, the unit tests and scripts/check-*.sh before merging

## Unreleased - Keyboard size screen: the keyboard stays pinned while it is resized

- ime: while the size screen is open the keyboard window has one fixed height (like float mode), the keyboard is pinned to the bottom edge at full width and the overlay covers exactly the keyboard (FloatFrame.resizing). Before, every drag step resized the real input window under the finger and the app behind re-laid out each time
- ime: during resizing the insets are frozen at the height the keyboard had when resizing began, so the app behind does not jump; only the keyboard rectangle takes touches, taps above it reach the app (InputMethodService.Insets: contentTopInsets, visibleTopInsets, TOUCHABLE_INSETS_REGION)
- ime: the back-gesture exclusion on the size screen covers a 160dp band around the side grips instead of the full height
- core: the keyboard can be smaller (height 50%, was 70%) and one-hand width goes down to 50% (was 65%)
- Not compiled with Android here: run the Gradle build, the unit tests and scripts/check-*.sh before merging

## Unreleased - Curved-screen bottom row, mic on long press, number row choice

- core/ime: the bottom row is drawn as separate capsules (fully rounded, with a thin border when the theme has none). It is kept 12dp away from the left and right edges and 8dp above the bottom edge so curved screens no longer clip EN, 123 and Enter. GeometryInput has bottomInset and edgeInset; the keyboard is 8dp taller; touch cells still reach the screen edge
- ime: tapping the mic on the space bar now types a space. Voice starts only on a hold: 450 ms on the mic icon, 750 ms elsewhere on the space bar (KeyTiming.MIC_HOLD_MS)
- core/app: the number row is shown by default. Settings > Typing has three choices: English digits, Arabic digits, hide the row (DigitsMode). The old number_row and native_digits keys are kept, so saved choices still apply
- Tests: KeyboardGeometryTest (bottomRowKeepsClearOfCurvedEdges), ArabicGapsTest (digitsModeMapsToStoredPrefs)
- Not compiled with Android here: run the Gradle build, the unit tests and scripts/check-*.sh before merging

## Unreleased - Keyboard design B (compact): one 40dp strip, panels own the whole height

- ime: the 80dp suggestion + tools rows are one 40dp strip. It shows the tools row when idle and the suggestions while a word is being typed (SuggestionController.composing) or when autofill offers values. Next-word suggestions after a space no longer replace the tools
- ime: while a full panel is open (symbols, emoji, decorate, clipboard, images, expanded translate) the strip is hidden and the panel takes keyboard height + 40dp, so the total height stays the same as the typing view. Compact modes (emoji search, symbol search, clip naming, translate capsule) keep the strip. Every panel already has its own keyboard button
- emoji, symbols, images: the tab row is 36dp (was 46dp) and the dock is 46dp including its padding (was 56dp). The selected dock category is a highlighted circle with no label, its name stays in the accessibility description. The emoji grid shows about 3.8 rows instead of 2.3 at the default height
- ime: Galaxy keys draw no gold outline. Letter keys are 72% fill, function and space keys keep their tint. Enter keeps its edge and glow. Day and Eclipse are unchanged
- toolbar buttons are 34dp (Galaxy), 36dp (Day) and 40dp (Eclipse) so they fit the strip
- Tests: KeySkinTest (Galaxy outline)
- Not compiled with Android here: run the Gradle build, the unit tests and scripts/check-*.sh before merging

## Unreleased - Keyboard design B: smaller bottom row, language swipe, voice on space, three shift states

- layouts: the lam-alef key is gone from the Arabic layout (it is the first long-press key on lam). EN, 123, comma, period and Enter are narrower in the Arabic, Persian, English and Arabizi layouts; the space bar takes the room
- ime: swiping the space bar left or right switches to the next language (44dp), replacing the cursor swipe. Holding the space bar for 500 ms starts voice typing; the space bar shows a mic icon. The toolbar Voice item is removed (ToolbarItem.VOICE, toolbar_voice, ToolbarActions.onVoice)
- ime: Shift has three looks. Off is an outline arrow, once is a filled arrow on a lit key, locked is a filled arrow with a bar on the glow (Enter) key. One tap is a single capital, a second tap locks capitals (also when auto capitalization had already turned Shift on), a third tap turns it off. No timing window
- designsystem: SHIFT_ONCE and SHIFT_LOCK icons and a filled icon part (Fill)
- strings: onboarding cursor hint is now the language swipe hint; toolbar_voice removed
- Tests: ArabicGapsTest, KeyBehaviorTest, KeySkinTest, KeyboardPrefsTest, KeyboardPrefsStoreTest
- Not compiled with Android here: run the Gradle build, the unit tests and scripts/check-*.sh before merging

## Unreleased - Translate panel: one field, live preview, check to send

- feature-translate: the panel is now a compact strip above the keyboard (the keyboard stays visible). One capsule holds the target language chip, the text field, an options button and a check button. A dim line above it shows the live translation. The check translates and sends the text straight into the app field and closes the panel. Enter on the keyboard does the same
- feature-translate: typing is routed from the keyboard into the panel field through SearchKeyRouter, like emoji search. Backspace and delete word work. Paste appears in place of the check while the field is empty, so pasted text in any language is detected and translated
- feature-translate: the source language is never chosen by the user. Device translation detects it from the text, so Arabic, English and pasted text all work. Device translation runs 450 ms after each key. AI translation still waits for the check so nothing is sent without a press
- feature-translate: every control in the strip is a bordered rounded key like the keyboard keys, 48dp touch size with 8dp gaps, and uses the shared line icons (language, more, clipboard, check) instead of text glyphs. The send key is filled with the theme glow like the Enter key
- feature-translate: route, AI styles, privacy and report moved into an options view behind the dots button. The language list is a two column grid with a popular group. The panel follows the system layout direction
- app: TranslateTextActivity keeps working. It uses the same panel in full height mode and the check copies or returns the result
- strings: translate_input_hint, translate_paste, translate_send, translate_options, translate_preview_hint, translate_preview_ai, translate_popular_languages in English and Arabic
- Not compiled with Android here: run the Gradle build, the unit tests and scripts/check-*.sh before merging

## Unreleased - Decorate panel redesign: grid cards and floating dock

- feature-decorate: DecorPanel now matches the emoji panel. A two column grid of cards (the active style has a ring, favorites show a star, press and hold toggles a favorite) above the floating DockBar. The dock has the keyboard icon, category icons (all, favorites, frames, marks, stretch; fonts for English), a language button and a typing mode button. The permanent warning text is gone; the screen reader toast stays
- feature-decorate: DecorCategory sorts styles by id into all, fonts, frames, marks and stretch. Typing mode: with the pen on, a tap starts typing in that style instead of inserting
- icons: DECOR_FONTS, DECOR_MARKS, DECOR_STRETCH (SpaceIconsDecor.kt); PanelIcon PEN and LANGUAGE
- strings: new category, language, favorites and typing mode names in English and Arabic; removed decor_type, decor_typing_off and decor_warning
- Tests: DecorCategoryTest
- Not compiled with Android here: run the Gradle build, the unit tests and scripts/check-*.sh before merging

## Unreleased - Clipboard actions: copy, share, edit, full view

- feature-clipboard: four actions on every clipboard text. Copy puts it on the system clipboard (marked sensitive when it comes from a locked section; a short guard skips the listener echo so the copy neither duplicates nor loops). Share opens the system chooser (ACTION_SEND, FLAG_ACTIVITY_NEW_TASK). Edit opens a full screen editor. Long press shows the full text (scrollable) with Insert, Copy, Share and Edit; a normal tap still inserts
- feature-clipboard: ClipBook.edit, checkEdit and mergeTarget. Empty and over MAX_TEXT texts are refused; pin, section and time are kept. A text that becomes equal to another one is merged: the edited item stays, pinned if either was, keeps its own section (else the other's), newest time. A text in another locked section is never merged, so equality cannot be probed
- feature-clipboard: ClipSession payload, startView, startEdit, checkEdit and edit all go through reachable; nothing in a locked section can be copied, shared, viewed or edited. An edit lease is granted only after that check and ends when the editor closes, so the save still works after the unlock window ends
- ime: ClipEditActivity (Compose, multi line, counter, RTL, Save and Cancel, confirm before discarding). The draft survives rotation, process death (saved state) and closing (kept in memory until saved or discarded). Opening it from the keyboard keeps the clipboard panel open on return
- panel: the item menu has a fixed icon row (copy, share, edit, full view) above scrollable text rows, sized by ClipMenuMetrics so it never passes the panel
- icons: SHARE, PENCIL, FULL_VIEW, INSERT (SpaceIconsClip.kt)
- Tests: ClipEditTest, ClipSessionActionsTest, ClipMenuMetricsTest, ClipLabelsTest
- Not compiled with Android and not run on a device (no SDK here); the pure logic tests were compiled and run on the JVM

## Unreleased - Emoji tabs

- emoji: every tab now holds one kind of thing. Faces and expressions are their own tab; hands, gestures and body parts moved out of People into a new Hands and body parts tab; People keeps only people, roles, activities and families. Hearts and emotion symbols (heart, 💯, 💢) moved to Symbols, and cat and monkey faces moved to Animals next to the other mammals
- tools/data/emoji: the catalog is now sorted by the Unicode subgroup, not only the group (classify, MOVED, GROUP_ORDER in emoji_data.py). Regenerate the emoji data (build_data or fetch_emoji.py) so emoji.tsv carries the new body group; the dock shows the new tab only after that
- feature-emoji: new group id body (label, Arabic label, hand icon); People has a person icon. Tests: test_emoji_data.py, EmojiParserTest
- icons: the tab icons now follow the icon design rules (24 grid, 1.75 pen, round ends, one pixel margin) and live in the design system as SpaceIcon entries (RECENTS, GROUP_BODY to GROUP_FLAGS, in SpaceIconsEmoji.kt). Own drawn set: winking face, history clock, hand, two people, butterfly, burger, steering wheel, trophy, bulb with rays, heart with star, flag. Search, Delete and Keys come from SpaceIcon; the hand drawn IconPaths are gone
- Not compiled and Kotlin tests not run here (no SDK); the real Unicode file could not be downloaded here, so the split was tested on a sample

## Unreleased - New design, phase 5: help screens and final review

- app: Onboarding, Privacy, Backup and PasswordSection follow the look. Setup steps are cards with a check mark once done (not color only) and a status pill when ready; Enable, Pick, Next, Done and Export are SpacePrimaryButton with the design icons; the features list and the privacy notes sit in cards; backup switches use SettingsCard and the password fields use ToolField
- app: the status and navigation bar icons now follow the chosen look (ApplySystemBars). Before, picking Day while the phone was dark, or Galaxy while it was light, left the bar icons invisible against the screen
- app: screen titles are announced as headings; theme cards grow with the font size instead of clipping (fixed 68 dp height became a minimum); emoji skin tone buttons report their selected state
- Review by script: check-contrast.py, check-themes.py, check-fonts.sh, check-strings.py, check-layouts.py and the three source checks pass. RTL: no left or right specific modifiers in app, designsystem or the panels; the only absolute offset is the toolbar drag, which follows the finger. Touch targets: every clickable in app is 48 dp or larger
- Font: Readex Pro is not bundled. IBM Plex Sans Arabic stays for every look, so Day differs from the design only in letter shapes. Adding it needs the OFL font files, a NOTICE.md entry and a line in scripts/check-fonts.sh
- Needs a device: TalkBack pass, large font and display size, small and large screens, split screen, screenshot tests (no emulator here)
- Not compiled and tests not run here

## Unreleased - New design, phase 4: tool screens and keyboard panels

- app: Emoji, Clipboard, Translate settings, Auto fix and Shortcuts now use ScreenFrame like the phase 3 screens (look background, padding, back handling); each repeated page frame is gone. Decorate and Images keep their lazy lists but take the look background and padding
- app: shared ToolParts (ToolSection, ToolField, ConfirmAction). The ask, yes, no wipe pattern that Clipboard, Auto fix and Images each wrote by hand is now one ConfirmAction; text inputs in Shortcuts and Decorate use the look field fill, edge and radius
- app: section titles use SpaceSectionTitle; the main actions (add picture, add shortcut, clipboard history on or off) are SpacePrimaryButton with the Add icon where it fits; cards, rows and emoji set cards follow the look radius and fill; emoji set cards report their selected state to screen readers
- panels: PanelIcon (feature-emoji) now maps to the design icon set. Search and Delete come from SpaceIcon, and new Add, Clear all, Save, Done, Next, Copy, Cut and Select all are available. Clock, Close and Lock are not in the design set and stay drawn by hand. The symbols panel uses SpaceIcon for Search and Delete as well; its hand drawn paths are gone except Close
- panels: action keys show an icon before the label: Clipboard (Clear, Save, New section, Done), Images (Add), Emoji (Clear), Translate (Copy for read only fields, Done for the other results)
- Not changed: Next has no key yet; the keys that use Copy, Cut and Select all are in the keyboard layouts and keep their text labels
- Tests: PanelIconTest
- Not compiled and tests not run here

## Unreleased - New design, phase 3: appearance and customization

- app: App look picker on the Themes screen (Galaxy, Day, Eclipse, Automatic) with a small preview of each look. It writes to AppLookStore, which now holds its value as Compose state, so AppTheme re-themes every screen the moment you pick; Eclipse can be chosen from the UI for the first time. Automatic keeps following the phone (dark is Galaxy, light is Day)
- app: the app look is its own setting and does not change with the keyboard theme, which has its own section below it
- app: Themes, Customize and Rearrange screens now use ScreenFrame (look background, padding, back handling) with section titles, grouped setting cards, the look field radius on theme cards, slot tabs and chips, and a check mark on the chosen theme and look. ScreenFrame takes a scrollable flag so dragging keys does not scroll the page
- app: ColorEditor sits in a card; Customize sliders sit in a card; Edit layout is the primary button; switch rows are at least 48 dp high
- Accessibility: theme cards and color tabs now report their selected state to screen readers (they were plain clickables)
- Strings: app_look_title, app_look_galaxy, app_look_day, app_look_eclipse, app_look_auto, app_look_note, section_keyboard_theme, section_colors (English and Arabic); search keywords for Themes include the looks
- Tests: AppLookOptionsTest
- Not compiled and tests not run here

## Unreleased - New design, phase 2: the keyboard

- ime: toolbar and key icons now come from the shared design icon set. The hand drawn Pen (ToolbarIcons) and the path code in KeyIcons are gone; toolbar buttons use SpaceIconView and keys use the new SpaceIconCanvas (designsystem, same path data drawn on a Canvas). Word and line keys mirror one icon; the expand chevron mirrors with the rail side
- ime: three key looks picked from the theme (KeyStyle in core: Eclipse for eclipse, Day for the light themes, Galaxy for the rest). Galaxy has glass keys, tinted function keys, a glowing Enter; Day has white keys with a bottom edge, pill space and Enter; Eclipse has flat keys, bordered gold function keys, a gold Enter. Pressed keys keep the existing contrast rules
- ime: strip layout from the design, suggestions row (36 dp) above the tools row (44 dp), both always visible (80 dp in total, was one 48 dp row). Suggestions show per look (dividers in Galaxy, pills in Day, a dot on the first word in Eclipse). Active tool buttons glow in Galaxy, fill in Day and get a label and underline in Eclipse; an open panel shows its button as active
- ime: the show tools and show suggestions toggle is removed because both rows are always shown (two strings removed)
- core: Daylight and Eclipse theme colors match the design (Daylight background DFE5F0 instead of E3E8F2, Eclipse keys 1E1E22 instead of 18181B, a small step so keys stay 1.25:1 from the background); Nebula already matched Galaxy
- Not changed: Shift and Language keys keep text labels because they show state (caps lock, current language). Readex Pro for Day is still not bundled
- Tests: KeySkinTest (ink contrast for every theme, role and state, glyph mapping), KeyStyleTest; ThemeChoiceTest corrected to the 12 themes in the catalog
- Not compiled and tests not run here

## Unreleased - New design, phase 1: looks, icons, Home

- designsystem: three looks from the design file, Galaxy (dark purple, glass tiles), Day (light blue, grouped lists, circle badges) and Eclipse (black and gold, flat lists). SpaceLook picks one; SpaceTheme(look = ...) swaps colors and provides SpaceLookStyle (radii, fills, badge shape, hero kind, spacing). Without a look the theme works as before, so the keyboard panels are unchanged
- designsystem: unified icon set, 46 icons on a 24 grid with a 1.75 pen, drawn by SpaceIconView from path data (SpaceIcon). The chevron mirrors in left to right languages
- designsystem: LookCard, IconBadge, SpaceEntryGroup (tiles in Galaxy, a grouped list in Day and Eclipse), SpaceSectionTitle, SpacePrimaryButton, SpaceStatusPill
- app: Home rebuilt from the design (header, status hero, try-it field, search with icon, three groups); search results use the same rows; ScreenFrame takes the look background and padding. App look follows the phone (dark is Galaxy, light is Day); AppLookStore can hold Eclipse, the picker comes in phase 3
- Other screens only pick up the new colors for now; their layouts come in phases 3 to 5. See DESIGN_PHASES.md
- Checks: check-contrast.py covers the new palettes; SpaceLookTest added
- Day uses the Readex Pro font in the design; it is not bundled, IBM Plex Sans Arabic is used for every look
- Not compiled and tests not run here

## Unreleased - Feedback levels, wider ranges, animated stickers

- Feedback: vibration strength slider (10 to 100, 50 is the system default and uses the system haptic) and key sound volume slider (10 to 100, 100 is the system volume). The strength slider only shows on phones with adjustable vibration; other phones keep the system haptic. Uses the VIBRATE permission (normal, no prompt), touch usage so the system touch feedback setting still applies. Toolbar buttons keep the system haptic
- Ranges: key size 70 to 130 (it only changes the gap between keys, which is now clamped to 0.5 to 12 dp), keyboard height 70 to 160 (still capped at 45% of the screen), label size 80 to 150
- Images: animated WebP stickers up to 3 MB and 2048 pixels are kept as they are (still WebP is converted as before); tabs All, GIF, Stickers (stickers = transparent PNG and animated WebP); GIF badge; animated thumbnails on Android 9 and later, still thumbnails on older phones or when system animations are off
- Accessibility: saved pictures announce their kind and position (for example Saved GIF, 3 of 12); decorated text previews are described with the plain text instead of the styled letters, with button roles and a favorite label; the decoration warning stays pinned above the list; with a screen reader on, applying or typing with a style shows a short warning. Text already committed in another app cannot carry a description, so the warning is the only mitigation there
- Not done: GIF search (needs INTERNET in the keyboard and a third-party service), packs of built-in stickers
- Not compiled and tests not run here; new unit tests cover limits, gap clamp, feedback levels, WebP detection, kinds and filters

## Critical gaps

- Suggestions: strip in the toolbar area with prefix completion and next-word prediction. Seed list of about 600 common Arabic and English words plus words and word pairs learned from your own typing (not in password fields or incognito). Settings: switch and clear button in Customize.
- Voice typing: toolbar button opens the system speech recognizer in the current language and inserts the result.
- Inline autofill: password managers and autofill services show their suggestions in the toolbar area (Android 11 and later, adds androidx.autofill).
- Not done: glide typing. It needs a real dictionary and per-key geometry; the seed list is too small to decode paths reliably.
- Still not compiled or tested here.

## Gap fixes: Arabic and text editing

- Arabic: harakat popup on ظ, لا key, tatweel, Persian/Urdu letters by long press (پ چ گ ژ ڤ ی), Persian layout with half-space key, Arabizi language (Latin typing composes Arabic), optional number row with Arabic-Indic or Persian digits, language cycle limited to the languages enabled in Customize.
- Editing: edit layer from the toolbar (select all, copy, cut, paste, selection mode, character, word and line moves, up and down), double space inserts a period, text shortcuts expanded on space (screen in the app), incognito toggle, Enter key shows Go, Search, Send, Next or Done.
- Not compiled and tests not run: there is no Kotlin toolchain in the authoring environment. Layout and string checks pass.

# Changelog

## Unreleased - Panel keys at 48 dp

- feature-clipboard, feature-decorate, feature-emoji, feature-images, feature-symbols, feature-translate: panel keys, bars and symbol cells that were 36, 40 or 44 dp tall are now 48 dp (and the 40 dp minimum width of text keys is 48 dp), so they meet the Android touch target size. Panels fill the keyboard area, so the content area below the bars is 8 dp shorter. Check on a small phone with the lowest keyboard height (70 percent)
- Not done: emoji suggestions while typing. It needs the emoji search index built before the first word, a rule for where the emoji goes in the suggestion strip, and a different action when tapped (insert after the word instead of replacing it); this changes SuggestionController and EmojiController, so it should be built and tried on a device first
- Not compiled and tests not run here

## Unreleased - Gap fixes: predictive back, debug log

- app: android:enableOnBackInvokedCallback is on, so the system back gesture shows its preview and the BackHandler screens use it (Android 13 and later)
- app: DebugLog writes only the error class name, and only in debuggable builds, when a backup cannot be read or written; the message the user sees is unchanged and nothing is stored or sent
- Checked, not a gap: landscape keyboard height is already capped at 45 percent of the screen height (KeyboardPrefs.rowPx), and swiping on the space bar already moves the cursor
- Not done: bigger touch height for keyboard panel keys (36 to 40 dp, they sit edge to edge, so growing them needs a device test), emoji suggestions while typing (needs a decision on which words map to which emoji)
- Not compiled and tests not run here

## Unreleased - Gap fixes: rotation, large screens, tests

- app: the open screen survives rotation and being recreated by Android (MainActivity saves it; ScreenSave restores it, and never shows Setup again once it is done)
- app: settings screens are centered and capped at 840 dp wide through spaceMaxWidth, so lines stay readable on tablets and foldables (Home, Customize, Emoji, Clipboard, Translate, Learn from my mistakes, Shortcuts, and the shared ScreenFrame). The layout editor keeps its own side-by-side layout
- app: Backup has a Show the password switch; the password rules moved to BackupRules
- Tests: app and ime now have unit tests (ScreenSaveTest, BackupRulesTest, KeyBehaviorTest) and the CI test step runs :ime and :app
- Not compiled and tests not run here

## Unreleased - Search in settings

- app/core: search field on the home screen under the try-it field. It filters the twelve screens by their name and by extra keywords in the phone language (for example password, encrypt, restore for Backup), ignores case and Arabic spelling variants (SettingsSearch uses ArabicFold), and every typed word must match. Empty query shows the groups as before; no match shows a short message
- Tests: SettingsSearchTest
- Not done: searching inside each screen for single options (needs a registry of every option)
- Not compiled and tests not run here

## Unreleased - Clearer Learn from my mistakes, clipboard points, backup password

- app: Learn from my mistakes shows a worked example (type, delete and retype, the keyboard learns, next time it fixes), a plain sentence under the chosen sensitivity (Gentle, Balanced, Bold), and an explanation of the touch-area lines such as Key A (right) -> S when the list has any
- app: the clipboard disclosure paragraph is now an intro plus five short points with decorative icons (hidden from screen readers): stays on the phone, encrypted, 24 hours and pinned, password managers, off means nothing saved. The old clipboard_disclosure string is removed
- app: color editor labels are plainer: Hue is Color shade and Saturation is Color strength in English, Saturation is قوة اللون in Arabic, so both languages say the same thing
- core/app: optional backup password (BackupCrypto, PasswordSection). With a password the file is a JSON envelope encrypted with AES-256-GCM, key from PBKDF2 (SHA-256, SHA-1 on phones without it) with 200,000 rounds and a random salt and IV; hex encoded. At least 8 characters typed twice; restore of an encrypted file asks for the password first and a wrong password or damaged file changes nothing. Plain backups still restore. Read limit is now 4.5 MB because hex doubles the size
- Tests: BackupCryptoTest (round trip, wrong password, tampering, new salt each time, bad envelope, iteration cap, hex)
- Not done: password strength meter, show-password toggle, recovery of a lost password (not possible by design)
- Not compiled and tests not run: there is no Kotlin toolchain in the authoring environment. String, comment, line, function and manifest checks pass; the AES-GCM and PBKDF2 calls were checked on a plain JVM

## Unreleased - Home screen groups, settings button, automatic language

- app: the home screen groups its buttons under Look and feel, Typing tools and Help and data (headings are announced as headings), and the try-it field now sits right under the status instead of below twelve buttons
- core/ime: new Settings button in the keyboard toolbar (gear icon, last in the default order, movable in Customize). It opens the app on Customize and hides the keyboard. MainActivity is singleTop and reads the request in onCreate and onNewIntent. Existing saved toolbars get the button appended
- core/ime/app: automatic Arabic and English switching (ScriptGuess, AutoLanguage). When a field opens with text in it, or the cursor jumps more than 3 characters, the last 40 characters before the cursor decide: 80 percent or more Arabic script switches to Arabic (or Persian), 80 percent or more Latin switches to English, mixed text changes nothing. It needs at least 3 letters, only uses enabled languages, leaves Arabizi alone, does not run in password, raw-key or Latin-only fields, and stops for the rest of the field once you switch language yourself. Nothing is stored. Switch in Customize (on by default); Privacy screen explains it
- Tests: ScriptGuessTest, KeyboardPrefsStoreTest and KeyboardPrefsTest cover autoLanguage and the new toolbar item
- Not done: search inside the settings (twelve buttons in three groups fit on a screen); switching language in the middle of a sentence while typing (mixing languages in one sentence is normal, so the keyboard does not guess)
- Not compiled and tests not run: there is no Kotlin toolchain in the authoring environment. Comment, line, function, string and manifest checks pass

## Unreleased - Voice dialect, delivery, light app theme

- app: every settings screen now follows the light or dark mode of the phone (AppTheme with SpaceThemeMode.System); the window background and base theme have day and night versions (values-night); the slider thumb has a soft ring so it shows on light backgrounds
- core/ime/app: voice typing language comes from VoiceLanguage. Arabic uses the dialect chosen in Customize (Saudi and Gulf, Egyptian, Emirati, Kuwaiti, Qatari, Iraqi, Jordanian, Lebanese, Moroccan, Algerian, Tunisian) and Arabic and Persian also accept mixed English. Persian stays fa-IR and English en-US
- ime: voice text that arrives while the field is not ready yet is held and inserted when the keyboard is back on the field (flushPending), instead of being dropped after the fixed 450 ms wait
- app: Privacy screen explains that speech is handled by the phone's speech service and the keyboard never records audio
- Tests: VoiceLanguageTest
- Not done: dictation inside the keyboard without leaving the field. It needs the RECORD_AUDIO permission, which scripts/check-manifest-permissions.py does not allow, and it changes the privacy promise, so it needs a decision first
- Not compiled and tests not run: there is no Kotlin toolchain in the authoring environment. Comment, line, function, string and manifest checks pass

## Unreleased - Smarter suggestions

- core: Arabic spelling folding (ArabicFold) so suggestions match أ إ آ ا, ى ي, ة ه, diacritics, tatweel and Persian ک ی; typing احب suggests أحب and typing مدرسه suggests مدرسة
- core/ime: the word you typed is the first suggestion when the keyboard does not know it, so tapping it keeps your spelling and teaches it; not shown for one letter or for words it already knows
- core/ime: hold a suggestion to remove it (also a TalkBack custom action). Removed words are stored in WordStats (up to 500, encoded as b lines, old files still load) and never learned again. Customize has Restore removed suggestions; Clear learned words also clears the list
- core: next-word suggestions work from the first day through seed pairs (pairs_ar, pairs_en); learned pairs still rank first
- core: Arabic seed list grew from 288 to about 620 words, adding everyday words and common Egyptian, Levantine, Gulf and Maghrebi words
- Tests: SuggestTest (typed word, folding, blocked words, seed pairs), ArabicFoldTest
- Not done: glide typing (needs a dictionary of tens of thousands of words with frequencies and path geometry; the seed lists are original and small on purpose, see NOTICE.md)
- Not compiled and tests not run: there is no Kotlin toolchain in the authoring environment. Strings, line, function and comment checks pass

## Unreleased - UX gap fixes (setup, sliders, touch targets)

- app: Setup guide redesigned: progress dots and step count, Back button (and system Back) on every page, the enable page explains Android's standard keyboard warning before the steps, two step cards with Done or Not done yet, features as six short rows instead of one paragraph, and a try-it field on the last page
- app: HsvSlider has semantics (label, spoken value, range with steps, set progress) so TalkBack can adjust key size, height and colors; its touch height is 48dp. ValueSlider and ColorEditor pass the label and hide the duplicate visible label from screen readers
- app: shared BackLink and minTouchTarget (48dp) replace ten copies of the Back text; Shortcuts Delete, Learn from my mistakes Forget, Decorate star and Use, and the image delete button now meet 48dp; ChipGrid chips are at least 48dp high and use selectable so selected state is announced
- Not compiled and tests not run: there is no Kotlin toolchain in the authoring environment. Strings, line, function and comment checks pass

## Unreleased - UX improvements (second batch)

- core/ime: smart touch (TouchCorrector) favours the likely neighbouring letter near a key edge in ordinary fields; switch in Customize
- core/ime: Undo and Redo keys in the edit layer (English and Arabic), with icons and accessibility descriptions
- core/ime: external keyboards type Arabic and Persian through HardwareLayout (ime/HardwareKeys.kt); switch in Customize
- ime: the space bar shows the current language name; the toolbar toggles between suggestions and tools
- ime/core: the keyboard follows the system subtype (onCurrentInputMethodSubtypeChanged, LayoutCatalog.forSubtype) and offers new Persian and Arabizi subtypes
- ime/core: language per app (LanguageMemory, AppLanguageStore). Only the package name is stored, locally; not recorded in password fields or incognito; Forget remembered apps clears it
- core/app: backup and restore (PrefsBackup, BackupChecks, BackupScreen). Writes and reads a JSON file through the system file picker, 2 MB read limit, learned words optional and off by default, invalid files roll back to the previous settings. The suggestion controller reloads words after a restore
- app: new Setup guide (onboarding, shown on first launch), Privacy screen and Backup screen; the home screen opens screens through one Screen enum; MainActivity handles uiMode changes and refreshes the theme
- app: Themes screen has Follow the light or dark mode of the phone; Typing section has the smart touch, physical keyboard and remember language switches and the Forget remembered apps button
- app: the Add button in Shortcuts and the Clear learned words button pass onClick explicitly (SpaceButton takes modifier as its last parameter, so a trailing lambda does not compile)
- Tests: HardwareLayoutTest, LayoutSubtypeTest, TouchCorrectorTest, PrefsBackupTest, AppLanguageStoreTest, a new KeyboardPrefsStoreTest case, ArabicGapsTest covers UNDO and REDO; FakePreferences supports clear and getStringSet
- ime: picking the Persian or Arabizi keyboard from the system switcher now turns that language on and selects it (LayoutController.selectOrEnable); before, it did nothing unless the language was enabled. The Arabizi subtype uses the no-language locale zz so it is not a duplicate of Arabic
- core/app: backups also hold the Learn from my mistakes switches, recent emoji with skin tone and favorite decorations (checked by BackupFeatureChecks); BackupChecks is public and its when now has an else branch so it compiles
- core: LanguageMemory moved from ime to core and takes lambdas, so it is unit tested; HardwareLayout.textFor holds the key-to-text rule that HardwareKeys uses
- app: SwitchRow uses toggleable so TalkBack reads on or off; the backup result message is a polite live region
- Tests: LanguageMemoryTest, ThemeStoreTest, SuggestionStoreTest, KeyPriorTest, WordBoundaryTest, BackupChecksTest, more PrefsBackup and HardwareLayout cases. Still without direct tests: LayoutRepository (needs Android resources), and the ime classes that use Views (PanelDeck, FieldSetup, HardwareKeys)
- Not compiled and tests not run: there is no Kotlin toolchain in the authoring environment

## Unreleased - Gap fixes (input, layout, feedback, power)

- ime: editors with no input type (TYPE_NULL, such as terminals and some games) now get real key events. KeyTyper maps letters, digits and space through KeyCharacterMap and sends them with sendKeyEvent, Delete sends KEYCODE_DEL, and characters with no key mapping (Arabic letters) fall back to commitText. Autocorrect is skipped there, and translate and images are hidden. FieldPolicy.needsRawKeys decides this
- ime: symbols and clipboard insertion now go through KeyTyper.insert and KeyTyper.deletePlain so they also work in TYPE_NULL editors
- app: the home screen scrolls (verticalScroll plus imePadding) so the test field is never cut off on small screens, large fonts or landscape
- core/ime/app: new touch feedback settings in Customize: vibrate on key press, sound on key press (uses the system touch sounds setting), key preview bubble, and keep the clipboard open after pasting. Stored in KeyboardPrefs.feedback; reset with Reset all customization. The preview bubble is still never shown in password fields
- ime: key feedback moved out of KeyboardView into KeyFeedback so KeyboardView stays within the 300 line limit
- ime: the navigation inset now covers the left and right sides and the display cutout, not only the bottom, so keys no longer sit under the notch or a side navigation bar in landscape. The keyboard excludes 36dp strips on both edges from system gestures (setSystemGestureExclusionRects on Android 10 and later) so edge keys do not trigger Back
- ime: onUpdateSelection refreshes automatic capitals when the cursor or selection moves, so moving the cursor by touch, pasting from the clipboard or inserting an emoji no longer leaves Shift stale
- ime: the top bar is now 48dp, no longer shows the keyboard name, uses drawn icons (ToolbarIcons) with content descriptions, gives every button a 48dp touch target, scrolls sideways when the buttons do not fit and vibrates like the keys. The theme and mode buttons keep their current name as the accessibility description
- core/ime: email, address and password fields open on the English layout (FieldPolicy.prefersLatin). The choice is temporary: the saved language is not changed, and the language key goes back to the saved language first
- ime: the animated background draws at 20 frames per second and stops after 8 seconds without a touch, then continues from the same moment on the next touch. The preview in the app keeps animating
- ime: key rows shrink in landscape so the keyboard takes at most about 45 percent of the screen height (never below 34dp per row); the layout is recomputed on rotation (onConfigurationChanged). KeyboardPrefs.rowPx holds the rule and has unit tests
- Tests: FieldPolicyTest (prefersLatin, needsRawKeys), KeyboardPrefsStoreTest (feedback round trip and reset), KeyboardPrefsTest (row height cap)

## Unreleased - Typing experience fixes

- ime: KeyboardView handles several fingers at once (one touch per pointer id). Letters, Shift, 123, Enter and Delete are sent on touch down, so a slightly sliding finger no longer loses a key; keys with a hold list and the space and language keys are sent on release. Haptic feedback moved to touch down
- ime: hold Delete to repeat (accelerates), swipe on the space bar to move the cursor, key preview bubble, and long press on a key opens its alternatives (digits, accents, Arabic hamza forms, punctuation) that can be chosen by sliding. Long press on the language key switches to the next keyboard (or opens the picker)
- core/ime: new SHIFT and NUMBERS actions, a numbers and symbols layout (layout_num.json), the optional hold list in layout JSON, Shift once and locked, and automatic capital letters from getCursorCapsMode. Number, phone and date fields open on the numbers layout
- ime: accessibility through ExploreByTouchHelper (customview dependency) so TalkBack reads and presses keys; onEvaluateFullscreenMode returns false so landscape does not take over the screen; method.xml declares supportsSwitchingToNextInputMethod
- autofix: live per letter replacement is now off by default (stored choices are kept) and every word correction shows a short note with the old and new word; Backspace still undoes it
- Saved key orders for the English layout are ignored once because gap_shift became the shift key

## Unreleased - Saved images (send pictures from the keyboard)

- New module feature-images and a new Images toolbar button (reorderable with the other toolbar buttons, hidden in password fields). The panel shows the saved pictures as a grid; one tap sends the picture to the current chat or field and the panel stays open so several can be sent. ABC returns to the keys; Add opens the system photo picker from the keyboard (ImagePickActivity, translucent)
- Sending uses the keyboard content API (InputConnectionCompat.commitContent) with a FileProvider URI and a read grant for the receiving app (manual grantUriPermission on Android 7.0). When the field does not accept the image type, the picture goes to the clipboard as a content URI and a message asks the user to paste it
- Storage is the app's private files only (images and image_thumbs). Pictures are picked with the system photo picker, so no new permission is needed. Large pictures are decoded with a sample size, corrected for EXIF rotation, shrunk to 1024 pixels on the longest side and saved as PNG (with transparency) or JPEG; a 192 pixel thumbnail is saved beside each. GIFs up to 3 MB are copied unchanged so they keep their animation; animated WebP becomes a still picture. Limit 200 images; imports run on one background thread and write to a temporary file first
- app: new ImagesScreen (button on the home screen) with a privacy note, the photo picker, a grid with a delete button on each picture and Delete all with confirmation. English and Arabic strings
- ime: PanelToggles (the decorate, clipboard, translate and images toggles and the symbols button, moved out of SpaceInputService to keep it under 300 lines), ImageSender, ImeImagesActions and ImagesPanelContent. ToolbarItem has a new IMAGES entry at the end, so saved toolbar orders keep working
- Build, Release and Play Bundle run the feature-images unit tests (ImagePolicyTest: type mapping, sample size, fitted size, EXIF rotation, file names); core toolbar tests expect the new item; docs/PRIVACY.md has a saved images section
- Hardening: decode sample size uses the longest side and OutOfMemoryError is caught (panoramas); GIFs need a valid signature and at most 2048 pixels; EXIF mirrored orientations are handled; bitmaps are recycled; leftover temp files are removed; 150 MB total cap; duplicates (SHA-256 prefix in the file id) are skipped; the pick activity handles configuration changes so it cannot stay on screen as an invisible layer; a picture deleted in the app is not sent from a stale panel; the Android 7.0 read grant is revoked after two minutes
- Not compiled and the tests were not run: no Kotlin compiler or Gradle was available. The repository checks (line, function length, comments, strings, manifest permissions) were run and pass. Everything else was reviewed by hand only

## Unreleased - Autofix (learn from the user's mistakes)

- New module feature-autofix (plain Kotlin and SharedPreferences, no permissions, no network, no manifest). It notices a repeated slip (a wrong letter, deleted, then the right one typed) and learns it in three tables: letter context (word start plus pressed letter to intended letter), touch area inside the key (3x3 zones) and whole word. It then fixes the letter while typing and the word on space, punctuation or Enter. Delete right after a fix undoes it and lowers the rule's confidence; deleting the space and editing the word keeps learning
- Sensitivity Gentle, Balanced (default) and Bold. On by default. No learning or correcting in password fields and no learning when IME_FLAG_NO_PERSONALIZED_LEARNING is set
- ime: KeyboardView has an optional onKeyTouch callback with the ACTION_DOWN offset from the key cell centre (-0.5 to 0.5), turned into a Zone by the service. New KeyTyper (COMMIT, SPACE, DELETE and ENTER handling plus the decorated and sendEnter helpers, moved out of SpaceInputService) and InputConnectionField (TextField over InputConnection, edits inside beginBatchEdit and endBatchEdit, TextDeletion.deleteLast for plain deletes). An active decoration style bypasses the engine. beforeEnter runs before Enter is sent. The model is saved on window hidden and on destroy, and the word state is reset when any other panel opens
- core: FieldPolicy.mayAutoCorrect (plain text only: not password, email, web address or number fields, and not with TYPE_TEXT_FLAG_NO_SUGGESTIONS) with tests
- app: new AutoFixScreen (button on the home screen): master switch, switches for fixing letters while typing and for fixing the word at its end, sensitivity, the list of what was learned with a Forget button for each item, Forget everything with confirmation (ModelStore.clear then bumpEpoch, so the running keyboard reloads) and a privacy note. English and Arabic strings
- Build, Release and Play Bundle run the feature-autofix unit tests; docs/PRIVACY.md has an autofix section
- Test fix: repeatedUndosDropARemap now expects 4 recorded undos after 5 attempts, because the rule is already switched off before the fifth attempt, so the fifth press is not remapped and cannot be undone. It also checks that zoneFix returns null and that the key is no longer remapped
- Not compiled and the tests were not run: no Kotlin compiler or Gradle was available. The ime and app files and the new tests were reviewed by hand only

## Unreleased - Stage 9, part 1 (text selection menu)

- New Translate entry in the text selection menu of other apps (PROCESS_TEXT), handled by TranslateTextActivity in app. It reuses the keyboard translate panel with the same routes, styles, disclosure and daily limit
- The selected text is only shown; nothing is sent until the user taps Translate (and has agreed, for the AI route). Read-only text gets a Copy button; editable text is replaced through the PROCESS_TEXT result
- TranslatePanelHost takes an optional label for its first key (Close here, ABC in the keyboard); TranslateSource gains READONLY
- Screen capture and OCR (the rest of Stage 9) are not started: they need the feature-capture module, the mediaProjection foreground service and the Arabic Tesseract model from the Frame out project
- Not compiled; the activity and manifest entry were reviewed by hand only

## 0.7.0 - Stage 8

- New module feature-translate: translate panel opened from a new Translate toolbar button (reorderable with the other toolbar buttons); translation is blocked in password fields
- Two routes: AI through our own server, and on-device with ML Kit (translation and language detection). The on-device route needs no agreement and sends nothing
- AI route is off until the user reads the disclosure and taps Agree and use AI; text is sent only on a tap of Translate, never from password fields. Withdraw consent switches back to on-device and deletes the consent record and install ID
- Styles: neutral, formal, casual, chat (for example you to u), polite, concise. Language list is shown in full with a note that quality is lower for less common languages
- When the daily AI limit is used up the translation falls back to the device and says so; AI translations left today are shown
- Replace selection, replace all text or insert the result into the field
- New directory server: Firebase Cloud Function translate (Node 22). App Check verified on every request; the install ID is hashed and counted per day (30 per install, 20000 overall, both configurable); counters expire after 3 days; text is never stored or logged. Provider Anthropic, model claude-haiku-5-5; the key is a Secret Manager secret, never in the app
- INTERNET is now allowed in feature-translate only: check-manifest-permissions.py keeps every other module offline and the Wi-Fi permissions stay removed from the merged manifest
- Build, Release and Play Bundle run the feature-translate unit tests; Build also runs the server tests in a separate job. Release and Play Bundle read TRANSLATE_URL, FIREBASE_APP_ID, FIREBASE_API_KEY and FIREBASE_PROJECT_ID; when they are missing the build still succeeds and the app offers on-device translation only
- App Check uses Play Integrity in release builds and the debug provider in debug builds
- ime: ImeTranslateActions, TranslateInput, TranslatePanelContent and the service wiring; ToolbarPanels, ToolbarActions, DecorInput and ImeInsets extracted from SpaceInputService to stay under 300 lines
- core: ToolbarItem.TRANSLATE and its label
- 23 server tests (node --test) and 39 pure-logic tests in feature-translate run outside Gradle; Android, Compose and network files were never compiled (reviewed by hand only)
- docs/DATA_SAFETY.md: Play AI-content policy finding, provider training claim check and Data safety draft
- docs/PRIVACY.md translate section; docs/SETUP.md section 11: function deployment, CI secrets and manual verification of Stage 8
- New Translate screen in the app (button on the home screen): default route, consent status with Withdraw consent, and download or remove of on-device language models through ML Kit RemoteModelManager (OnDeviceModels in feature-translate). app now depends on feature-translate for this screen
- Report a problem key on AI translation results: opens the email app addressed to the support address with a fixed subject and no text attached (TranslateReport); a toast gives the address when no email app exists. Added as a hedge for the Play AI-generated content policy
- Review by hand of the feature-translate network, App Check, controller and Compose files and of the ime wiring: TranslateController no longer stays in the working state if the worker rejects the job; opening the panel forgets the in-memory daily count so AI is tried again after midnight; the toolbar translate action does nothing in a blocked field before reading any text
- versionCode 8, versionName 0.7.0

## 0.6.0 - Stage 7

- New module feature-clipboard: clipboard history with pinned items and saved sections, opened from a new Clipboard toolbar button (reorderable with the other toolbar buttons) and hidden in password fields
- History is off by default and is turned on only from the app, on the Clipboard screen, after a disclosure; it is never recorded in password fields or for clips flagged android.content.extra.IS_SENSITIVE
- Up to 50 items of up to 5000 characters; items that are not pinned or saved are deleted after 24 hours
- Sections are created from the keyboard itself: the panel shrinks to a name bar and the keyboard keys type into it
- Sections can be locked; locked items are hidden until the user unlocks with BiometricPrompt (API 29+, with device credential fallback) or the screen lock confirmation (before API 29). Locking is refused when the device has no screen lock
- Unlock window chosen in the app: every time (grant ends when the panel closes or the tab or section changes), 1 minute, 5 minutes, 15 minutes or 1 hour
- Storage is local only: clipboard.vault is encrypted with AES-GCM using an Android Keystore key without user authentication; locking is an app-level rule
- The transparent unlock activity hides the keyboard briefly; the controller keeps the panel state across that return
- Delete all data from the app wipes the file and resets the Keystore key
- No network: the module adds no permission; the only new permission is USE_BIOMETRIC, now on the allowed list of check-manifest-permissions.py
- Fixes found in review: the Keystore key now allows the caller-supplied IV that AesGcmCipher uses (otherwise every save failed on a device); the biometric callback is created only on API 29+ so API 24 to 28 do not load BiometricPrompt classes
- Pure logic (ClipBook, ClipCodec, ClipFilter, ClipSnapshot, ClipSession, AesGcmCipher, ClipStore, ClipPrefsStore) compiled with Kotlin 2.0.21 and its unit tests run outside Gradle: 40 passing; Android and Compose files were reviewed by hand only
- docs/SETUP.md section 10: manual verification of Stage 7; docs/PRIVACY.md
- versionCode 7, versionName 0.6.0

## 0.5.0 - Stages 5 and 6 (summary written from the code, not from the original notes)

- Stage 5, new module feature-emoji: emoji panel with search, recents (up to 60), skin tone choice in the app, Noto Color Emoji or system emoji set, and a glyph filter that hides emoji the device cannot draw. Shared panel pieces (PanelKey, PanelIcons) live here
- Stage 6, new module feature-decorate: text decoration with English and Arabic styles, Arabic letter joining, a renderability check, and a decoration panel and controller opened from the toolbar

## 0.4.0 - Stage 4

- New module feature-symbols: symbol panel and text faces library, opened from a new Symbols toolbar button (reorderable with the other toolbar buttons)
- Data pipeline in tools/data (Python, standard library only): UnicodeData, emoji-data and CLDR annotations (English and Arabic) are downloaded in CI and turned into symbols.tsv, featured.tsv, faces.tsv and face_categories.tsv; nothing generated is stored in the repository
- 20 symbol categories (currency, math, arrows, stars, hearts, shapes, marks, signs, punctuation, brackets, frames, Greek, circled, numbers, Arabic, games, music, misc, ornaments, technical) plus a hand-ordered Featured tab that starts with € { } ✓ ¶ π ® © ∆ ★ ✪ ❥ ♧ ° ● × √
- Emoji presentation characters are left out of symbols; they belong to Stage 5
- 3,600+ original text faces in 14 categories, including ^_^ ^o^ (◔‿◔), generated deterministically from curated lists and combinations
- Search by English or Arabic name, keyword, Unicode code (U+2605, 0x2605, 2605) or by pasting the symbol; Arabic search ignores diacritics and letter variants; faces are searched by text or mood words
- Search is typed with the keyboard itself: the panel shrinks to a search bar and a result strip above the keyboard, and the keyboard keys write into the search field
- Glyph filter: every symbol and every character of a face is checked with Paint.hasGlyph on a background thread and hidden if the device cannot draw it; results are cached per category
- Panel is drawn with Compose foundation; icons are drawn on Canvas, no icon characters
- New workflow Data pack and composite action generate-data; Build, Release and Play Bundle generate and verify the data before Gradle and run the feature-symbols unit tests
- Python tests for the pipeline and for category sync with the Kotlin enums; Kotlin tests for parsing, search, normalization and the generated files
- NOTICE.md: Unicode License v3 for the generated data
- versionCode 5, versionName 0.4.0

## 0.3.1 - Stage 3 completion

- Review of every Stage 3 UI file written without an Android build; no build result was available, so the Build workflow has to confirm it
- Delete, enter, expand and flip keys are drawn on Canvas (KeyIcons) instead of the characters ⌫ ⏎ ‹ › ↔, so no device can show empty squares
- Toolbar reorder in the app follows the finger in right-to-left layouts (absoluteOffset) and counts the gap between buttons
- The Rearrange screen stops page scrolling while a key is dragged and releases it on drop, cancel or detach
- KeyboardPrefsStore accepts a SharedPreferences directly; new unit tests with a fake store for read, write, clamping, orders per layout, reset and observers
- New geometry tests: one-key rows, split rows with no splittable key, wide keys across the middle, spacer-only rows, one-hand single key
- docs/SETUP.md section 8: manual verification of Stage 3
- versionCode 4, versionName 0.3.1

## 0.3.0 - Stage 3

- JSON keyboard layouts (Arabic, English) with weights, fixed keys and spacers; basic keys: space, delete, enter, language switch
- Customization: key size, keyboard height, one-hand mode (left/right with rail), split keyboard, label font, bold, italic and size
- Rearrange keys by dragging (swap), per language; reorder toolbar buttons by dragging; toolbar mode button
- Customize and Rearrange screens in the app; reset buttons
- New checks: check-layouts.py, check-strings.py; unit tests for layout, geometry and prefs
- Not included (Stage 1): Shift/Caps, long press, numbers, field types

## 0.2.0 - Stage 2

- Theme engine in core: ten space themes (Nebula, Galaxy, Black hole, Aurora, Eclipse, Dark neon, Mystic fog, Blood moon, Icy planet, Cosmic gold)
- Four editable colors per theme (background, keys, glow, border) with an HSV picker, stored per theme
- ThemeResolver keeps key text at 4.5:1 or better in normal and pressed states and separates keys, glow and border from the background
- Animated backdrops drawn on Canvas with prebuilt gradients, paused when the keyboard is hidden, in battery saver or when system animations are off
- Instant switching: the settings app, the keyboard toolbar button and the live preview all follow the same store
- Themes screen in the app with live keyboard preview, theme cards, color sliders, animation switch and reset
- New check: scripts/check-themes.py runs in Build, Release and Play Bundle
- Unit tests for color math, HSV, resolver and theme choice

## 0.1.0 - Stage 0

- Project skeleton: app, ime, core, designsystem
- designsystem ported from Frame out: theme, accents, glass, glow, motion, accessibility and IBM Plex Sans Arabic
- InputMethodService with a Canvas keyboard that commits one Arabic letter, one Latin letter and deletes
- Compose toolbar hosted inside the keyboard with a manual lifecycle owner
- FieldPolicy for password and no-learning fields, with unit tests
- Workflows: Build, Generate Keystore, Play Bundle, Release, with pinned action versions
- Checks: lines, comments, binaries, function length, contrast, fonts, permissions, merged manifest, 16KB alignment
