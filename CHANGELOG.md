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
