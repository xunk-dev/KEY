package com.spacekeyboard.keyboard.core.macro

class Macro(val id: Long, val steps: List<MacroStep>, val name: String? = null) {
    fun preview(limit: Int = PREVIEW_CHARS): String {
        val out = StringBuilder()
        for (step in steps) {
            when (step) {
                is MacroStep.Text -> out.append(step.text.replace('\n', ' '))
                MacroStep.Enter -> out.append(ENTER_MARK)
                MacroStep.Send -> out.append(SEND_MARK)
                MacroStep.LiveClip -> out.append(CLIP_MARK)
                MacroStep.Delete -> out.append(DELETE_MARK)
            }
            if (out.length > limit) break
        }
        return if (out.length > limit) out.substring(0, limit).trimEnd() + ELLIPSIS else out.toString()
    }

    private companion object {
        const val PREVIEW_CHARS = 48
        const val ENTER_MARK = " ⏎ "
        const val SEND_MARK = " ➤ "
        const val CLIP_MARK = " 📋 "
        const val DELETE_MARK = " ⌫ "
        const val ELLIPSIS = "…"
    }
}
