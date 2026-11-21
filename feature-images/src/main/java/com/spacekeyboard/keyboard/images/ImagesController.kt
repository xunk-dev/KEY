package com.spacekeyboard.keyboard.images

import android.content.Context

class ImagesController(context: Context) {
    val store = ImageStore(context)
    private val listeners = LinkedHashSet<(ImagesSnapshot) -> Unit>()

    var snapshot: ImagesSnapshot = ImagesSnapshot()
        private set

    fun subscribe(listener: (ImagesSnapshot) -> Unit): AutoCloseable {
        listeners += listener
        listener(snapshot)
        return AutoCloseable { listeners -= listener }
    }

    fun open() = update { it.copy(open = it.allowed, items = store.list()) }

    fun close() = update { it.copy(open = false) }

    fun setFilter(filter: ImageFilter) = update { it.copy(filter = filter) }

    fun toggle() = if (snapshot.open) close() else open()

    fun refresh() = update { it.copy(items = store.list()) }

    fun setAllowed(value: Boolean) = update { it.copy(allowed = value, open = it.open && value) }

    private fun update(change: (ImagesSnapshot) -> ImagesSnapshot) {
        val next = change(snapshot)
        if (next == snapshot) return
        snapshot = next
        listeners.toList().forEach { it(next) }
    }
}
