package com.spacekeyboard.keyboard.core.macro

import java.net.URLDecoder
import java.net.URLEncoder

internal object MacroSerial {
    private const val CHARSET = "UTF-8"
    private const val FIELD = '\t'

    fun write(macro: Macro): String {
        val name = URLEncoder.encode(macro.name.orEmpty(), CHARSET)
        return "${macro.id}$FIELD$name$FIELD${MacroCodec.encode(macro.steps)}"
    }

    fun parse(line: String): Macro? {
        val parts = line.split(FIELD, limit = 3)
        if (parts.size != 3) return null
        val id = parts[0].toLongOrNull() ?: return null
        val name = runCatching { URLDecoder.decode(parts[1], CHARSET) }.getOrNull()?.ifEmpty { null }
        val steps = MacroCodec.decode(parts[2])
        return if (steps.isEmpty()) null else Macro(id, steps, name)
    }

    fun parseLegacy(line: String): Macro? {
        val at = line.indexOf(FIELD)
        if (at <= 0) return null
        val id = line.substring(0, at).toLongOrNull() ?: return null
        val steps = MacroCodec.decode(line.substring(at + 1))
        return if (steps.isEmpty()) null else Macro(id, steps)
    }
}
