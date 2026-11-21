package com.spacekeyboard.keyboard.autofix

class AutoFixTyper(private val engine: AutoFixEngine) {
    var onFixed: ((original: String, corrected: String) -> Unit)? = null

    fun type(label: String, zone: Int, field: TextField) {
        val key = label.singleOrNull()
        when {
            key != null && Words.isLetter(key) -> field.commit(engine.letter(key, zone, field).toString())
            key != null && Words.isDigit(key) -> {
                engine.opaque()
                field.commit(label)
            }
            else -> boundary(label, field)
        }
    }

    fun boundary(text: String, field: TextField) {
        apply(engine.boundary(text, field), field, true)
        if (text.isNotEmpty()) field.commit(text)
    }

    fun beforeEnter(field: TextField) {
        apply(engine.boundary("", field), field, true)
    }

    fun delete(field: TextField) {
        if (field.hasSelection()) {
            engine.reset()
            field.deleteLast()
            return
        }
        val undo = engine.delete(field)
        if (undo == null) field.deleteLast() else apply(undo, field, false)
    }

    fun reset() = engine.reset()

    private fun apply(edit: Edit?, field: TextField, announce: Boolean) {
        edit ?: return
        val original = if (announce) field.before(edit.delete) else ""
        field.batch {
            field.deleteBefore(edit.delete)
            field.commit(edit.insert)
        }
        if (announce && original != edit.insert) onFixed?.invoke(original, edit.insert)
    }
}
