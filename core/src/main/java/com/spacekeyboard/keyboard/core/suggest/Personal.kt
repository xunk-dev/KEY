package com.spacekeyboard.keyboard.core.suggest

class Personal(val global: Boolean = true, val local: Map<String, Int> = emptyMap()) {
    companion object {
        val DEFAULT = Personal()
        val NONE = Personal(global = false)
    }
}
