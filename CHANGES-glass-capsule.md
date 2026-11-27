# Glass capsule buttons + selectable text in the clipboard full view
- ClipKeys.kt: new ClipCapsuleKey (glass capsule, theme-aware, glow on the primary button).
- ClipFullView.kt: Insert / Copy / Share / Edit use capsules; the text is now selectable (SelectionContainer). The keyboard window has no system selection menu, so a custom TextToolbar shows "Copy selection" and "Select all" capsules over the text.
- EditCommands.kt (ime): in select mode a real Shift key press now wraps the arrow key so editors extend the selection.
- Strings: clip_copy_selection, clip_select_all (en, ar). Not compiled or run here.

# Edit text: full view no longer opens over the editor keyboard
- Cause: opening the editor (Edit button in the full view) left `viewing` set and the clipboard panel stayed open in the editor's own keyboard, so the full view covered the keys while selecting/typing.
- ClipSession.startEdit clears `viewing`; new `inEditor` flag hides the panel (snapshot mode CLOSED) only while editing and typing in the editor field. Back in the other app the panel returns to the list.
- FieldSetup tells the controller when the field belongs to this app (ClipboardController.setEditorField).
- Test: ClipEditViewTest.
