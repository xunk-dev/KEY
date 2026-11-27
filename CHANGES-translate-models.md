# Translate: reliable language downloads

- `OnDeviceModels` is now a process-wide state holder (`ModelsSnapshot`). Downloads use ML Kit task listeners: no blocked threads, no 180 s timeout that reports a failure while the download is still running. State survives rotation, back and reopening the screen.
- Failures carry a reason (`NETWORK`, `STORAGE`, `OTHER`) read from `MlKitException`, shown per language with a Try again button.
- Settings > Translate: one row per language (status line, indeterminate bar, Download / Remove / Try again). Remove needs a second tap (reverts after 4 s). Several languages can download at once. A failed model list shows Check again instead of loading forever.
- New "Download on Wi-Fi only" switch (default on, `TranslatePrefsStore.modelsWifiOnly`). After 20 s the row says it is waiting for Wi-Fi or is slow.
- Keyboard: `OnDeviceTranslator` no longer downloads silently on the single worker. A missing model returns `NEEDS_MODEL`; the panel shows the language names and a Download button, then translates again by itself (`TranslateController.downloadModels`). Failed `NETWORK`/`SERVER`/`MODEL`/`STORAGE` now show a Try again button (it was never wired).
- `TranslateController` recreates its worker after `stop()` and closes the model watcher.

- Completion notification (`ModelNotifier`): "X is ready" or "Could not download X" when a download ends while the settings screen is not showing it, including downloads started from the keyboard. `POST_NOTIFICATIONS` is asked once, in context, when the person first taps Download; without it nothing is posted and everything else works.
- English is now handled explicitly. Every pair goes through English, so `missing()` always requires it and the prompt names it when absent. A model also counts as present once a download of it finished here (remembered in `space_keyboard_models`, cleared on Remove), so a bundled English that ML Kit never lists cannot cause a prompt loop. The silent 60 s `downloadModelIfNeeded` in the translate path is gone, and English now appears in the settings list.

Not changed: ML Kit has no cancel or progress-percent API, so downloads cannot be cancelled and the bar is indeterminate. If the process is killed mid-download, no notification is posted; reopening the screen shows the real state.
