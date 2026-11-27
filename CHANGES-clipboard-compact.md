# Compact clipboard

- The bottom bar is gone. The keys button now floats over the bottom corner of the list (start side) and the list scrolls past it.
- Clearing recent texts is a small trash icon at the end of the tabs row (only on Recent, only when there is something to clear, hidden while a confirm is showing).
- Lock and delete section are small icons at the end of the sections row instead of wide footer keys.
- Cards are tighter: pin and more keys are 40dp.

# Edit text

- The editor no longer raises the keyboard on open (windowSoftInputMode stateHidden, no auto focus). Tapping anywhere in the text box focuses it and shows the keyboard. A hint under the box says so until it has focus.
- Leaving the editor (save, cancel, discard) restarts the 60 second window that keeps the clipboard open in the app you return to, so a long edit no longer closes it.
- The pencil is hidden in the item menu for very large texts (it only showed a toast before).

# Gaps fixed

- Save to section > + Section: the text is now placed in the new section (before, the section was created empty and the text stayed where it was). Cancelling the name returns to the section picker.
- Deleting a text shows a "Text deleted / Undo" pill beside the keys button for 5 seconds. Undo restores pin, section and time.
- With history off and nothing recorded, the empty note no longer says copied texts appear here.

Files

- feature-clipboard/ui/ClipChrome.kt (new), ClipPanel.kt, ClipDesign.kt, ClipBody.kt, ClipMenu.kt, ClipEditor.kt, ClipActions.kt, ClipTabBar.kt
- feature-clipboard/ClipSession.kt, ClipSnapshot.kt, ClipBook.kt, ClipboardController.kt (toggle removed, holdOpen internal), ClipEdit.kt, strings (en, ar), ClipUndoTest.kt (new)
- ime/ImeClipActions.kt, ClipEditActivity.kt, AndroidManifest.xml
