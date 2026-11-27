# Swipe the number row to swap Arabic and Latin digits

- Swipe sideways on the number row (Arabic and Persian layouts): ١٢٣ <-> 123. The choice is the same setting as Customize > Typing > Number row, so the calculator and the settings screen agree. A short tick and a note with the new digits confirm it
- It counts as a swipe only when the finger travels about one key and a bit (at least 36dp), mostly sideways (height change under 60% of the width), and no long-press popup is open
- Digit keys now type on release instead of on touch-down, but only where the swipe is available (Arabic/Persian layout, number row on, not a password field). This is what stops a swipe from typing a stray digit first. Everywhere else typing is unchanged
- Long-press on a digit still offers the other style. The release handling keeps the old result in every case (slide to the alternative, release on it untouched, slide away)
- No swipe in password fields, on the English layout (it has no second digit style, so nothing would change), on the numbers/symbols pages, or in the layout editor
- After the swap the keyboard rebuilds and cancels the touch, so no digit is typed by the swipe
- TalkBack users keep the setting in Customize (a swipe gesture is not offered to them)
- Per-app memory of the digit style was left out on purpose
- Code: NumberRow.hasNative, LayoutController.canSwapDigits/swapDigits, KeyboardView.swipedDigits, KeyTouch.deferred, KeyBehavior.isNumberRowKey. Tests: NumberRowSwapTest, KeyBehaviorTest. Not compiled or run here (no Gradle in the sandbox)
