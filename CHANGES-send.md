# Send from the keyboard

Long-pressing Enter in a field where Enter is only a newline (chat apps) now sends the message.
It tries the editor's SEND action first, then Ctrl+Enter as a fallback.
Where Enter already shows a Send/Go/Search action, tap still runs it and long-press still inserts a newline.

Macros record this as a new `Send` step (codec token `s`, preview mark ➤) and replay it through the same path.

Limit: an input method cannot tap another app's send button. Apps that ignore both SEND and Ctrl+Enter still need their own button.
