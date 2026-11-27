package com.spacekeyboard.keyboard.ime

import android.content.Context
import android.os.Build
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InlineSuggestionsRequest
import android.view.inputmethod.InlineSuggestionsResponse
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi

@RequiresApi(Build.VERSION_CODES.R)
internal object InlineAutofill {
    private const val HEIGHT_DP = 40
    private const val MAX_COUNT = 6

    fun request(context: Context): InlineSuggestionsRequest {
        val metrics = context.resources.displayMetrics
        val height = (HEIGHT_DP * metrics.density).toInt()
        val style = InlineSuggestionUi.newStyleBuilder().build()
        val styles = UiVersions.newStylesBuilder().addStyle(style).build()
        val spec = InlinePresentationSpec.Builder(Size(1, height), Size(metrics.widthPixels, height))
            .setStyle(styles)
            .build()
        return InlineSuggestionsRequest.Builder(listOf(spec)).setMaxSuggestionCount(MAX_COUNT).build()
    }

    fun show(context: Context, response: InlineSuggestionsResponse, publish: (List<View>) -> Unit): Boolean {
        val suggestions = response.inlineSuggestions
        if (suggestions.isEmpty()) {
            publish(emptyList())
            return true
        }
        val views = arrayOfNulls<View>(suggestions.size)
        var pending = suggestions.size
        val size = Size(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        suggestions.forEachIndexed { index, suggestion ->
            suggestion.inflate(context, size, context.mainExecutor) { view ->
                views[index] = view
                pending -= 1
                if (pending == 0) publish(views.filterNotNull())
            }
        }
        return true
    }
}
