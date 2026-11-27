package com.spacekeyboard.keyboard.core.macro

import java.net.URLDecoder
import java.net.URLEncoder

object MacroCodec {
    private const val CHARSET = "UTF-8"
    private const val TEXT = 't'
    private const val ENTER = "e"
    private const val SEND = "s"
    private const val CLIP = "c"
    private const val DELETE = "d"

    fun encode(steps: List<MacroStep>): String = steps.joinToString(",") {
        when (it) {
            is MacroStep.Text -> TEXT + URLEncoder.encode(it.text, CHARSET)
            MacroStep.Enter -> ENTER
            MacroStep.Send -> SEND
            MacroStep.LiveClip -> CLIP
            MacroStep.Delete -> DELETE
        }
    }

    fun decode(raw: String): List<MacroStep> = raw.split(',').mapNotNull { token ->
        when {
            token == ENTER -> MacroStep.Enter
            token == SEND -> MacroStep.Send
            token == CLIP -> MacroStep.LiveClip
            token == DELETE -> MacroStep.Delete
            token.length > 1 && token[0] == TEXT -> runCatching { MacroStep.Text(URLDecoder.decode(token.substring(1), CHARSET)) }.getOrNull()
            else -> null
        }
    }
}
