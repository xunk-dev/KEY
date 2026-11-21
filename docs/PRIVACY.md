# Privacy

Space Keyboard sends nothing over the network while you type. The only feature that uses the network is Translate (Stage 8), and its AI route sends text only after you agree to a disclosure and tap Translate. Everything else (typing, themes, symbols, emoji, decoration, clipboard, saved images, learning from your mistakes) works without network access.

## Clipboard history (Stage 7)

- Off by default. It starts only after the user reads the disclosure on the Clipboard screen in the app and taps the turn-on button. Turning it off stops recording at once.
- Stored locally only, in the app's private files, in a file named clipboard.vault. The file is encrypted with AES-GCM using a key held in the Android Keystore. There is no network access, no sync and no backup (allowBackup is false).
- Items that are not pinned or saved in a section are deleted after 24 hours. The history keeps at most 50 unpinned items of up to 5000 characters each; pinned items and items saved in sections are kept until the user deletes them.
- Nothing is recorded in password fields, and nothing is recorded when the copied content carries the android.content.extra.IS_SENSITIVE flag, which password managers use on Android 13 and later.
- Sections can be locked. A locked section is hidden until the user authenticates with a fingerprint, face or the screen lock. Locking is an app rule: the encryption key does not require authentication, so the lock is not a defence against someone with root access to the device.
- Delete all data in the app removes the file and the key, including pinned and locked items.
- Only text is read. Images and other content types are ignored.

## Translate (Stage 8)

Translate has two routes. You choose one in the translate panel. The AI route is off until you agree; the on-device route needs no agreement.

### AI route

- What is sent: only the text you chose to translate (the selection, or all text in the field), up to 2000 characters, together with the source language (or auto), the target language, the style (neutral, formal, casual, chat, polite or concise), a random install ID, and an App Check token that proves the request comes from the genuine app. No account, name, email, advertising ID or device identifier is sent.
- When: only after you read the disclosure and tap Agree and use AI, and then only when you tap Translate. Nothing is sent while you type, when you open the panel, or when you change a language or style.
- Never from password fields: translation is turned off in password fields and no text from them is read or sent.
- Where it goes: to our translation server (a Firebase Cloud Function), which forwards the text to an AI service to produce the translation and returns the result. The service key lives only on the server, never in the app.
- What the server stores: it does not store your text or the translation, and its logs hold only the request status. For usage limiting it keeps a counter per install: the install ID is hashed (SHA-256) and the document holds a number and an expiry date. It also keeps one overall daily counter. Counters expire automatically after 3 days.
- Daily limit: 30 AI translations per install per day, and an overall daily cap to control cost. When your limit is used up, the translation is done on your phone instead.
- Withdraw consent: use Withdraw consent in the translate panel. It switches the route to on-device, deletes the local consent record and the install ID, and nothing more is sent. A new random install ID is created if you agree again.

### On-device route

- Translation and language detection run on your phone with Google ML Kit. Your text is not sent anywhere.
- The first translation into or out of a language downloads a language model from Google. That download contains no text from you, only a request for the model.

### App Check

- The AI route asks Google Play Integrity, through Firebase App Check, for a token that proves the app is genuine. This exchange with Google contains no text from you.

### Local data

- Translate keeps in the app's private preferences: whether you agreed (and which version of the disclosure), the chosen route, target language, style and the install ID. Nothing else is stored, and allowBackup is false.
- Withdraw consent removes the consent record and install ID; clearing the app's storage in Android settings removes everything.

### AI service

- The text is forwarded from our server to Anthropic's API (model claude-haiku-5-5) only to produce the translation.
- Per Anthropic's published policies checked on 20 November 2026: commercial inputs and outputs are not used to train models by default, and API conversation content is not retained by default for models that are not on its Covered Models list (this model is not on that list at the time of checking). Exceptions the provider states: content flagged by its safety systems may be retained (up to 2 years), data may be kept where the law requires, and feedback explicitly submitted to the provider may be used. We submit no feedback.
- These are the provider's terms, not ours, and they can change. Recheck them before each release and whenever the model name changes in server/src/config or TRANSLATE_MODEL.

## Translate from the text selection menu (Stage 9)

- Other apps can offer Translate in their text selection menu. Choosing it hands the selected text to Space Keyboard, which shows it and sends nothing.
- The same rules as the keyboard apply: on-device by default, and the AI route only after the disclosure and a tap on Translate.
- Space Keyboard does not read other apps' text on its own. It receives only the text the user selected and chose to translate.
- For editable text the translation is returned to the app that sent it; for read-only text it is copied to the clipboard only when the user taps Copy.

## Learning from your mistakes (autofix)

- On by default, and it can be turned off in the app on the Learn from my mistakes screen. Turning it off stops learning and correcting at once.
- What it learns: when you repeat the same slip, for example you press a wrong letter, delete it and type the right one, it keeps a count for that slip. It stores three kinds of counts: the start of a word plus the letter you pressed, the part of a key you tend to touch, and whole words you retype (the mistyped form and the corrected form). These entries can therefore contain words you typed, such as names. Each entry also keeps its counters and the day it was last updated, which is used to age out old entries. It never stores sentences, the app or field you typed in, or any identifier.
- Stored locally only, in the app's private preferences, in a file named space_keyboard_autofix_model. The model is not encrypted, unlike the clipboard vault. There is no network access, no sync and no backup (allowBackup is false). Old and weak entries are removed automatically and the model has a fixed size limit.
- Nothing is learned and nothing is corrected in password fields, and nothing is learned in fields that set the no personalized learning flag (IME_FLAG_NO_PERSONALIZED_LEARNING). Nothing is corrected in email, web address or number fields, or in fields that ask for no suggestions.
- You can see what it has learned on the same screen, forget one entry, or tap Forget everything to delete the whole model. A correction can also be undone by pressing delete right after it, and each undo lowers the confidence in that rule.
- Clearing the app's storage in Android settings removes everything.

## Saved images

- Only images the user picks on purpose are saved, with the system photo picker (no storage or photos permission is requested). Nothing is read from the gallery otherwise.
- Pictures are copied into the app's private files (images and image_thumbs). They are not uploaded, not synced and not backed up (allowBackup is false). Large pictures are shrunk to 1024 pixels on the longest side; GIFs and animated WebP stickers up to 3 MB and 2048 pixels are kept as they are (the file signature is checked; a still WebP is converted like any other picture). At most 200 images and 150 MB are kept, and a picture that is already saved is not saved twice. After sending on Android 7.0 the read grant is revoked after two minutes.
- A picture leaves the app only when the user taps it on the keyboard. It is then handed to the app that owns the text field through a content URI that grants read access to that one image (the keyboard content API). If that app does not accept images, the picture is placed on the clipboard as a content URI instead and the user pastes it.
- Images are turned off in password fields.
- Delete all images in the app removes every saved image and thumbnail. Clearing the app's storage in Android settings removes everything.

## Language for each app

- When enabled (Customize, Typing: Remember the language for each app), the keyboard remembers which language you last used in each app and restores it the next time you start typing there.
- The only thing stored is the app's package name (for example com.example.chat) and the language code, in the app's private preferences, in a file named space_app_languages. It stays on the device: no network, no sync, no upload. At most 200 apps are kept and the oldest are dropped first.
- Nothing is recorded in password fields or while incognito is on. Choosing the Persian or Arabizi keyboard from the system keyboard switcher turns that language on in Customize.
- Forget remembered apps in Customize deletes the list. Turning the switch off stops recording and restoring. Clearing the app's storage in Android settings removes everything.

## Backup and restore

- Backup is a file you create on purpose from the Backup and restore screen and save where you choose, using the system file picker. Nothing is sent anywhere by Space Keyboard.
- The file is not encrypted unless you type a password before saving it (at least 8 characters, typed twice). With a password the file is encrypted with AES-256-GCM using a key derived from the password (PBKDF2, 200,000 rounds, random salt), and the password is never stored. A lost password cannot be recovered. The file holds your themes and colors, layout and typing settings, text shortcuts, the remembered app languages (package names), the Learn from my mistakes switches, recent emoji with the chosen skin tone, and your favorite decorations. Without a password anyone who can open the file can read it, so keep it somewhere you trust.
- Learned words are left out unless you turn on Include learned words, because they can contain names and private words. The switch is off by default and is off again each time the screen is opened.
- Never part of a backup: the translate consent and install identifier, the learned model of the autofix, and the clipboard vault.
- Restore reads a file you pick, up to about 4.5 MB (an encrypted file is larger than the settings inside it). For an encrypted file, type its password on the screen first; a wrong password changes nothing. The file is checked first; if it is damaged, has the wrong format or does not load, everything is rolled back and your current settings stay as they were.

## Physical keyboards

- When an external keyboard is attached, Space Keyboard can map its keys to Arabic or Persian letters (Customize, Typing: use the keyboard in an attached physical keyboard). It only translates the key that was pressed into a character for the focused field.
- Nothing is stored or sent. Hardware key presses are not logged, not learned as words in password fields, and never leave the device.

## Suggestions (removed words)

- Holding a suggestion removes it. The removed words are kept on the phone only, in the same private file as the learned words, up to 500 words. Clear learned words in Customize deletes them too, and Restore removed suggestions brings the suggestions back without deleting learned words.
- Tapping the first suggestion keeps the exact word you typed and adds it to the learned words, unless incognito is on or the field is a password field.
