package com.spacekeyboard.keyboard.ime

import com.spacekeyboard.keyboard.core.otp.OtpFit
import com.spacekeyboard.keyboard.core.shortcuts.ShortcutBook
import com.spacekeyboard.keyboard.core.suggest.FieldKind
import com.spacekeyboard.keyboard.core.text.SpellFix

class TypingExtras {
    var arabizi: Boolean = false
    var doubleSpace: Boolean = true
    var suggestions: Boolean = false
    var imageOffers: Boolean = false
    var copyOffers: Boolean = false
    var otpOffers: Boolean = false
    var otpFit: OtpFit = OtpFit.NONE
    var fieldEpoch: Int = 0
    var cursor: Int? = null
    var learning: Boolean = false
    var kind: FieldKind = FieldKind.PROSE
    var scope: String? = null
    var emojiSuggestions: Boolean = false
    var touchAssist: Boolean = false
    var glide: Boolean = false
    var convertOffers: Boolean = false
    var spellFix: Boolean = false
    var spelling: (String) -> SpellFix? = { null }
    var shortcuts: ShortcutBook = ShortcutBook.EMPTY
    var variables: (String) -> String = { it }
}
