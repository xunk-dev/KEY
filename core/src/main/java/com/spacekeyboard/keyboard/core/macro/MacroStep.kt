package com.spacekeyboard.keyboard.core.macro

sealed interface MacroStep {
    data class Text(val text: String) : MacroStep

    object Enter : MacroStep {
        override fun toString() = "Enter"
    }

    object Send : MacroStep {
        override fun toString() = "Send"
    }

    object LiveClip : MacroStep {
        override fun toString() = "LiveClip"
    }

    object Delete : MacroStep {
        override fun toString() = "Delete"
    }
}
