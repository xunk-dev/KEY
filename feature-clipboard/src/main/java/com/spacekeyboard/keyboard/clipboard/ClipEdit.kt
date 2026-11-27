package com.spacekeyboard.keyboard.clipboard

object ClipEdit {
    const val EXTRA_ID = "com.spacekeyboard.keyboard.clipboard.EDIT_ID"

    private var host: ClipboardController? = null
    private val drafts = HashMap<Long, String>()

    internal fun attach(controller: ClipboardController) {
        host = controller
    }

    internal fun detach(controller: ClipboardController) {
        if (host !== controller) return
        host = null
        drafts.clear()
    }

    fun original(id: Long): String? = host?.editText(id)

    fun save(id: Long, text: String): EditOutcome = host?.edit(id, text) ?: EditOutcome(EditStatus.UNAVAILABLE)

    fun leaving() {
        host?.holdOpen()
    }

    fun finish(id: Long) {
        host?.endEdit(id)
    }

    fun draftFor(id: Long): String? = drafts[id]

    fun keepDraft(id: Long, text: String, original: String) {
        if (text == original) drafts.remove(id) else drafts[id] = text
    }

    fun dropDraft(id: Long) {
        drafts.remove(id)
    }
}
