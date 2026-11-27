package com.spacekeyboard.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InlineSuggestionsRequest
import android.view.inputmethod.InlineSuggestionsResponse
import androidx.annotation.RequiresApi

abstract class AutofillImeService : InputMethodService() {
    protected open fun onAutofill(views: List<View>) = Unit

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateInlineSuggestionsRequest(uiExtras: Bundle): InlineSuggestionsRequest = InlineAutofill.request(this)

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onInlineSuggestionsResponse(response: InlineSuggestionsResponse): Boolean =
        InlineAutofill.show(this, response, ::onAutofill)
}
