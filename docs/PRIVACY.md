# Privacy

Space Keyboard sends nothing over the network while you type. The only feature that uses the network is Translate (Stage 8), and its AI route sends text only after you agree to a disclosure and tap Translate. Everything else (typing, themes, symbols, emoji, decoration, clipboard, saved images, learning from your mistakes) works without network access.

## Clipboard history (Stage 7)

- Off by default. It starts only after the user reads the disclosure on the Clipboard screen in the app and taps the turn-on button. Turning it off stops recording at once.
- Stored locally only, in the app's private files, in a file named clipboard.vault. The file is encrypted with AES-GCM using a key held in the Android Keystore. There is no network access, no sync and no backup (allowBackup is false).
- Items that are not pinned or saved in a section are deleted after 24 hours. The history keeps at most 50 unpinned items; pinned items and items saved in sections are kept until the user deletes them. Texts up to 5000 characters are stored inside clipboard.vault. A longer text is stored as its own encrypted file in a private folder (clipblobs) in independent 64 KB segments, so a removed, reordered or changed segment is detected when it is read; the vault keeps only a 5000 character preview, the character count and a SHA-256 fingerprint used to spot repeated copies. The limit is the free space on the device (the keyboard leaves 256 MB free) and 8 billion characters in total; beyond that the oldest unpinned large text is deleted. If there is not enough space the text is not saved and a notice is shown.
- Nothing is recorded in password fields, and nothing is recorded when the copied content carries the android.content.extra.IS_SENSITIVE flag, which password managers use on Android 13 and later.
- Sections can be locked. A locked section is hidden until the user authenticates with a fingerprint, face or the screen lock. Locking is an app rule: the encryption key does not require authentication, so the lock is not a defence against someone with root access to the device.
- Delete all data in the app removes the file and the key, including pinned and locked items.
- Only text is read. Images and other content types are ignored.
- Copy, Share and Edit work only on texts that are visible: a locked section must be unlocked first. Copy places the text on the system clipboard, where other apps can read it; for texts from a locked section the copy is marked sensitive so Android 13 and later hides its preview and the history does not record it again. Share hands the text to the app the user picks in the system chooser. The editor keeps an unsaved draft in memory only, never on disk, until it is saved or discarded. Large texts cannot be edited. Copy places a large text directly on the system clipboard up to 100000 characters; above that it places a content link that the receiving app reads through a read-only provider of this app, which decrypts the file while it is read. Share works the same way and offers the text as a .txt file. The link works only for the app that Android grants access to, and the provider is not exported. Pasting a text above 100000 characters asks first and offers to send it as a file instead; a paste in progress shows its progress and can be stopped, and it stops when the keyboard moves to another field. A large text copied by another app as a content link is read the same way and stored encrypted.

## Verification codes (OTP)

- On by default for copied codes (Customize, Typing: Offer verification codes in the top bar). Reading codes from notifications is a separate switch and is off by default.
- Clipboard source: while the keyboard is open it reads the newest text on the system clipboard directly, looks for a verification code in it, and offers the code as a chip in the top bar. It does not use the clipboard history, so it works when the history is off. Text marked with the android.content.extra.IS_SENSITIVE flag is ignored. Only text is read.
- Notification source: off until the user turns on Read codes from notifications and grants notification access to Space Keyboard in Android settings (the service OtpNotificationService, declared with BIND_NOTIFICATION_LISTENER_SERVICE; this is the only place the app uses that permission). Only the title and text of each new notification are looked at, to find a code. Ongoing notifications, notifications of this app and notifications older than five minutes are skipped. Nothing else about a notification is read, kept or sent. The user can withdraw access at any time in Android settings.
- Android 15 and later can hide the text of a notification that carries a one-time code from apps that listen to notifications. Where that happens the code cannot be read from the notification even when access is on; copying the code still works.
- Retention: a code is kept in memory only, for at most five minutes (less when the message states a shorter validity) and for no more than one code at a time. It is never written to disk, to the clipboard history, to a backup or to an automation recording, and it is never sent over the network. It is dropped when the process ends. A code that was entered or hidden is not offered again for ten minutes.
- Entering a code: tapping the chip types the code into the focused field with the same calls the keyboard uses for any text, but without going through the recorder, so automation recordings never contain it. For fields split into one box per digit it types digit by digit and follows the focus as the app moves it, only while the new field belongs to the same app and has the same input type. If a field is too short, already holds other text or stops accepting input, the keyboard stops and leaves the field as it found it.
- Where it appears: never in password fields, email, web address, message or multi-line fields, and not in fields whose name suggests another kind of code (postal, promo, invite and similar). In a field whose name says it expects a code it always appears; in a plain number field it appears when the field is empty or holds the start of the same code; in other fields it appears only for a code that came inside a message and only for ninety seconds.
- If the user turns the clipboard history on, copying a code is recorded by the history like any other copied text under that feature's own rules; the OTP feature adds nothing to it.

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
- While incognito is on, suggestions come only from the built-in lists and nothing you type is learned.

## Suggestions for each app and field

- When enabled (Customize, Typing: Adapt suggestions to each app and field), the words you type are also counted for the app you typed them in, so that app suggests them first. Search boxes, name fields and address fields keep one shared list of their own instead of feeding the normal suggestions.
- What is stored is the app's package name (or the field kind), the word and how many times it was used, in the app's private preferences, in a file named space_scoped_words. It stays on the device: no network, no sync, no upload, and it is not part of the backup file. At most 40 apps or field kinds and 300 words each are kept, and the least recently used are dropped first.
- Nothing is recorded in password fields, while incognito is on, or in fields that ask for no personalised learning. While incognito is on, suggestions are not built from the learned words either.
- Clear learned words in Customize deletes this list too. Turning the switch off stops recording and using it. Clearing the app's storage in Android settings removes everything.

## Emoji suggestions

- When the word you are typing matches an emoji, the keyboard shows that emoji in the suggestion bar. The match is made on the phone against a built-in list; the text you type is not sent anywhere. Choosing the emoji adds it to your recent emoji unless incognito is on or the field is a password field.

## Automation recordings

- A recording keeps the text you typed, pasted or picked as emoji, plus Enter and Delete steps, so it can be played again, and the optional name you give it. It is stored only on the phone, in the app's private preferences (space_macros), and is not sent anywhere and is not part of the backup file.
- Recordings and their names are encrypted with AES-GCM using a key held in the Android Keystore (alias space_keyboard_macros), the same method as the clipboard vault. Only the playback speed, the repeat choice, the number of the pinned recording and the counter used for new numbers are stored as plain values. Recordings made by an older version were stored as plain text; they are encrypted automatically the first time they are read, and the plain copy is removed only after the encrypted copy has been written and read back. If the Keystore is not available, old recordings are left as they are, and new ones are not saved.
- A live paste step stores no text. It pastes the newest text from the keyboard's clipboard history when it plays. It does nothing when the clipboard history is off or empty, when the newest item is a large text or sits in a locked section, and in password and other sensitive fields. Text is never taken from a locked section, even when it is unlocked at that moment. The text pasted at play time is not saved in the recording.
- Nothing is recorded in password and other sensitive fields. A pasted text that is not turned into a live paste is saved as text inside the recording (encrypted), so do not record pasting secrets. Each recording can be renamed, pinned for quick play or deleted from Tools > Automation, and Clear all automation recordings in the app under Privacy and data removes every recording and the pin after one confirmation.
- The pinned recording shows a small play button on the keyboard's top bar. The button holds no text; it only starts that recording.
