# Shake the phone to open a tool

- core/shake: ShakeAction (calculator, emoji, clipboard, translate, shortcuts, voice typing, symbols, images, text styles, floating keyboard), ShakeSensitivity, ShakePrefs/ShakePrefsStore (off by default), ShakeDetector (gravity filtered out; needs 4 hard pushes in 0.8 s with 3 direction changes; 1.5 s cooldown, so walking, a bump or putting the phone down do not trigger it)
- ime: ShakeController reads the accelerometer only while the keyboard is shown and the feature is on, then unregisters. ShakeRouter runs the chosen tool (the calculator closes on a second shake). A short haptic tick confirms the shake
- app: ShakeSection in Customize (switch, tool, sensitivity), English and Arabic strings
- Tests: ShakeDetectorTest. Not compiled or run here (no Gradle in the sandbox)
- Not added to PrefsBackup on purpose (it would change the backup file list); say if you want it
