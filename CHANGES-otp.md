# Verification code chip (OTP)

What changed
- A chip in the top bar shows a verification code from the clipboard (always) or from notifications (optional, off by default). Tapping it enters the code in the focused field, including fields split into one box per digit.
- Settings > Typing has two switches (Offer verification codes in the top bar: on; Also read codes from notifications: off) and a button that opens Android's notification access settings.
- The code stays in memory only, at most five minutes. It is not written to disk, to the clipboard history or to automation recordings.
- Android 15 and later can hide the code from notification listeners. Copying the code always works.

Rules kept
- No comments in code, files up to 300 lines, functions up to 40 lines, English and Arabic strings match, allowBackup=false.
- BIND_NOTIFICATION_LISTENER_SERVICE is allowed only for ime/.OtpNotificationService (scripts/check-manifest-permissions.py).

Not tried here (no Gradle, Android SDK or Kotlin compiler)
- Compiling the Kotlin, running any unit test, and any run on a device or emulator.
- Real apps: the filler was checked only against the form models in OtpFormModels.kt. Real fields with unusual focus timing may still need the timing constants in OtpFiller adjusted.
- The notification path (including the Android 15 redaction) and the clipboard timestamp behaviour.

Known limits
- A clip marked IS_SENSITIVE is ignored, so a code copied by an app that marks it sensitive is not offered.
- In a field that does not report the cursor and cannot be read, the filler assumes the code was accepted after one commit.
