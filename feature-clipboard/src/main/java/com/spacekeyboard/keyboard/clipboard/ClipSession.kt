package com.spacekeyboard.keyboard.clipboard

data class ClipSession(
    val book: ClipBook = ClipBook(),
    val mode: ClipMode = ClipMode.CLOSED,
    val allowed: Boolean = true,
    val enabled: Boolean = false,
    val window: UnlockWindow = UnlockWindow.EVERY_TIME,
    val tab: ClipTab = ClipTab.CURRENT,
    val section: String? = null,
    val unlockedAt: Long? = null,
    val grant: Boolean = false,
    val unlocking: Boolean = false,
    val naming: String = "",
    val titling: Long? = null,
    val assigning: Long? = null,
    val confirm: ClipConfirm? = null,
    val viewing: Long? = null,
    val editing: Long? = null,
    val inEditor: Boolean = false,
    val order: List<ClipTab> = ClipTabOrder.DEFAULT,
    val busy: ClipBusy? = null,
    val undo: ClipItem? = null,
    val smart: Boolean = false,
    val kind: ClipKind? = null,
) {
    fun isOpen(now: Long): Boolean =
        window.isOpen(unlockedAt, now) || (window == UnlockWindow.EVERY_TIME && grant)

    fun relockAt(now: Long): Long? {
        val start = unlockedAt ?: return null
        return if (window.millis > 0 && isOpen(now)) start + window.millis else null
    }

    fun open(now: Long): ClipSession =
        if (allowed) copy(mode = ClipMode.BROWSE, tab = order.first(), book = book.prune(now), kind = null) else this

    fun close(): ClipSession =
        copy(mode = ClipMode.CLOSED, grant = false, naming = "", titling = null, assigning = null, confirm = null, viewing = null, undo = null)

    fun setAllowed(value: Boolean): ClipSession = if (value) copy(allowed = true) else close().copy(allowed = false)

    fun withSettings(enabled: Boolean, window: UnlockWindow): ClipSession =
        copy(enabled = enabled, window = window, grant = grant && window == UnlockWindow.EVERY_TIME)

    fun withSmart(value: Boolean): ClipSession = if (smart == value) this else copy(smart = value, kind = null)

    fun selectKind(value: ClipKind?): ClipSession = if (!smart || kind == value) this else copy(kind = value)

    fun withOrder(value: List<ClipTab>): ClipSession = copy(order = ClipTabOrder.normalize(value))

    fun withBusy(value: ClipBusy?): ClipSession = if (busy == value) this else copy(busy = value)

    fun selectTab(tab: ClipTab): ClipSession = reset().copy(tab = tab)

    fun selectSection(name: String): ClipSession =
        if (book.sections.any { it.name == name }) reset().copy(section = name) else this

    fun record(text: String, sensitive: Boolean, now: Long): ClipSession =
        if (ClipFilter.shouldRecord(enabled && allowed, sensitive, text)) copy(book = book.add(text, now)) else this

    fun recordLarge(preview: String, total: Long, blob: String, digest: String, now: Long): ClipSession =
        if (enabled && allowed) copy(book = book.addLarge(preview, total, blob, digest, now)) else this

    fun togglePin(id: Long, now: Long): ClipSession =
        reachable(id, now)?.let { copy(book = book.setPinned(id, !it.pinned)) } ?: this

    fun delete(id: Long, now: Long): ClipSession =
        reachable(id, now)?.let { copy(book = book.delete(id), assigning = null, viewing = null, undo = it) } ?: this

    fun restore(): ClipSession = undo?.let { copy(book = book.restore(it), undo = null) } ?: this

    fun dismissUndo(): ClipSession = if (undo == null) this else copy(undo = null)

    fun startAssign(id: Long, now: Long): ClipSession = reachable(id, now)?.let { copy(assigning = id) } ?: this

    fun assign(name: String?): ClipSession {
        val id = assigning ?: return this
        return copy(book = book.moveToSection(id, name), assigning = null)
    }

    fun cancelAssign(): ClipSession = copy(assigning = null)

    fun payload(id: Long, now: Long): ClipPayload? = reachable(id, now)?.let { ClipPayload(it.text, isLocked(it), it.blob, it.length) }

    fun startView(id: Long, now: Long): ClipSession = reachable(id, now)?.let { copy(viewing = id) } ?: this

    fun closeView(): ClipSession = copy(viewing = null)

    fun startEdit(id: Long, now: Long): ClipSession =
        reachable(id, now)?.takeIf { it.blob == null }?.let { copy(editing = id, viewing = null) } ?: this

    fun endEdit(id: Long): ClipSession = if (editing == id) copy(editing = null) else this

    fun editableText(id: Long): String? =
        if (editing == id) book.items.firstOrNull { it.id == id }?.text else null

    fun checkEdit(id: Long, text: String): EditStatus = when {
        book.items.none { it.id == id } -> EditStatus.MISSING
        editing != id -> EditStatus.LOCKED
        else -> book.checkEdit(id, text)
    }

    fun edit(id: Long, text: String): ClipSession =
        if (checkEdit(id, text) == EditStatus.OK) copy(book = book.edit(id, text)) else this

    fun askClear(): ClipSession = copy(confirm = ClipConfirm.ClearCurrent)

    fun askPaste(id: Long, now: Long): ClipSession {
        val item = reachable(id, now)?.takeIf { it.blob != null } ?: return this
        return copy(confirm = ClipConfirm.PasteLarge(item.id, item.length), viewing = null)
    }

    fun askDeleteSection(name: String): ClipSession = copy(confirm = ClipConfirm.DeleteSection(name))

    fun answer(yes: Boolean): ClipSession {
        val pending = confirm
        val base = copy(confirm = null)
        if (!yes) return base
        return when (pending) {
            ClipConfirm.ClearCurrent -> base.copy(book = book.clearCurrent())
            is ClipConfirm.DeleteSection ->
                base.copy(book = book.deleteSection(pending.name), section = section.takeIf { it != pending.name })
            is ClipConfirm.PasteLarge, null -> base
        }
    }

    fun toggleSectionLock(name: String, now: Long): ClipSession {
        val target = book.sections.firstOrNull { it.name == name } ?: return this
        if (target.locked && !isOpen(now)) return this
        val next = copy(book = book.setSectionLocked(name, !target.locked), viewing = null)
        return if (target.locked) next else next.copy(grant = false, unlockedAt = null)
    }

    fun startNaming(): ClipSession =
        if (book.sections.size < ClipPolicy.MAX_SECTIONS) copy(mode = ClipMode.NAMING, naming = "") else this

    fun startTitle(id: Long, now: Long): ClipSession {
        val item = reachable(id, now) ?: return this
        return copy(mode = ClipMode.NAMING, naming = item.title.orEmpty(), titling = id, assigning = null)
    }

    fun setTitle(id: Long, title: String?, now: Long): ClipSession =
        reachable(id, now)?.let { copy(book = book.setTitle(id, title)) } ?: this

    private fun nameLimit(): Int = if (titling != null) ClipPolicy.MAX_TITLE else ClipPolicy.MAX_NAME

    fun type(text: String): ClipSession =
        if (mode == ClipMode.NAMING) copy(naming = (naming + text).take(nameLimit())) else this

    fun backspace(): ClipSession {
        val cut = if (naming.lastOrNull()?.isLowSurrogate() == true) 2 else 1
        return if (mode == ClipMode.NAMING) copy(naming = naming.dropLast(cut)) else this
    }

    fun suggest(text: String): ClipSession =
        if (mode == ClipMode.NAMING) copy(naming = text.take(nameLimit())) else this

    fun cancelNaming(): ClipSession = copy(mode = ClipMode.BROWSE, naming = "", titling = null)

    fun confirmName(): ClipSession {
        titling?.let { id ->
            val named = book.setTitle(id, naming)
            return copy(book = named, mode = ClipMode.BROWSE, naming = "", titling = null)
        }
        val clean = naming.trim()
        if (clean.isEmpty()) return cancelNaming()
        val created = book.addSection(clean)
        val placed = assigning?.let { created.moveToSection(it, clean) } ?: created
        return reset().copy(book = placed, mode = ClipMode.BROWSE, naming = "", tab = ClipTab.SAVED, section = clean)
    }

    fun beginUnlock(): ClipSession = if (unlocking) this else copy(unlocking = true)

    fun finishUnlock(ok: Boolean, now: Long): ClipSession =
        copy(unlocking = false, unlockedAt = if (ok) now else unlockedAt, grant = ok, viewing = null)

    fun snapshot(now: Long): ClipSnapshot {
        val selected = effectiveSection()
        val hidden = hiddenSections(now)
        val locked = tab == ClipTab.SAVED && selected != null && selected in hidden
        val base = when (tab) {
            ClipTab.CURRENT -> book.current
            ClipTab.PINNED -> book.pinned.filter { it.section == null || it.section !in hidden }
            ClipTab.SAVED -> if (selected == null || locked) emptyList() else book.saved(selected)
        }
        val view = if (smart) ClipSmart.arrange(base, kind) else ClipSmart.off(base)
        val items = view.items
        val counts = mapOf(
            ClipTab.CURRENT to book.current.size,
            ClipTab.PINNED to book.pinned.count { it.section == null || it.section !in hidden },
            ClipTab.SAVED to book.items.count { it.section != null && it.section !in hidden },
        )
        return ClipSnapshot(
            if (editing != null && inEditor) ClipMode.CLOSED else mode, allowed, enabled, tab, book.sections, selected, items, locked, unlocking, naming,
            assigning?.let { id -> base.firstOrNull { it.id == id } }, confirm, counts,
            viewing?.let { id -> base.firstOrNull { it.id == id } },
            order,
            busy,
            undo,
            smart,
            view.kind,
            view.counts,
            view.grouped,
            titling?.let { id -> book.items.firstOrNull { it.id == id } },
        )
    }

    private fun effectiveSection(): String? =
        section?.takeIf { name -> book.sections.any { it.name == name } } ?: book.sections.firstOrNull()?.name

    private fun hiddenSections(now: Long): Set<String> =
        if (isOpen(now)) emptySet() else book.sections.filter { it.locked }.map { it.name }.toSet()

    private fun reachable(id: Long, now: Long): ClipItem? {
        val item = book.items.firstOrNull { it.id == id } ?: return null
        val target = book.sections.firstOrNull { it.name == item.section }
        return if (target?.locked == true && !isOpen(now)) null else item
    }

    private fun isLocked(item: ClipItem): Boolean = book.sections.firstOrNull { it.name == item.section }?.locked == true

    private fun reset(): ClipSession = copy(grant = false, assigning = null, confirm = null, viewing = null)
}
