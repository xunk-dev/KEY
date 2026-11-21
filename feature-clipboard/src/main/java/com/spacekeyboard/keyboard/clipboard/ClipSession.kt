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
    val assigning: Long? = null,
    val confirm: ClipConfirm? = null,
) {
    fun isOpen(now: Long): Boolean =
        window.isOpen(unlockedAt, now) || (window == UnlockWindow.EVERY_TIME && grant)

    fun relockAt(now: Long): Long? {
        val start = unlockedAt ?: return null
        return if (window.millis > 0 && isOpen(now)) start + window.millis else null
    }

    fun open(now: Long): ClipSession = if (allowed) copy(mode = ClipMode.BROWSE, book = book.prune(now)) else this

    fun close(): ClipSession =
        copy(mode = ClipMode.CLOSED, grant = false, naming = "", assigning = null, confirm = null)

    fun setAllowed(value: Boolean): ClipSession = if (value) copy(allowed = true) else close().copy(allowed = false)

    fun withSettings(enabled: Boolean, window: UnlockWindow): ClipSession =
        copy(enabled = enabled, window = window, grant = grant && window == UnlockWindow.EVERY_TIME)

    fun selectTab(tab: ClipTab): ClipSession = reset().copy(tab = tab)

    fun selectSection(name: String): ClipSession =
        if (book.sections.any { it.name == name }) reset().copy(section = name) else this

    fun record(text: String, sensitive: Boolean, now: Long): ClipSession =
        if (ClipFilter.shouldRecord(enabled && allowed, sensitive, text)) copy(book = book.add(text, now)) else this

    fun togglePin(id: Long, now: Long): ClipSession =
        reachable(id, now)?.let { copy(book = book.setPinned(id, !it.pinned)) } ?: this

    fun delete(id: Long, now: Long): ClipSession =
        reachable(id, now)?.let { copy(book = book.delete(id), assigning = null) } ?: this

    fun startAssign(id: Long, now: Long): ClipSession = reachable(id, now)?.let { copy(assigning = id) } ?: this

    fun assign(name: String?): ClipSession {
        val id = assigning ?: return this
        return copy(book = book.moveToSection(id, name), assigning = null)
    }

    fun cancelAssign(): ClipSession = copy(assigning = null)

    fun askClear(): ClipSession = copy(confirm = ClipConfirm.ClearCurrent)

    fun askDeleteSection(name: String): ClipSession = copy(confirm = ClipConfirm.DeleteSection(name))

    fun answer(yes: Boolean): ClipSession {
        val pending = confirm
        val base = copy(confirm = null)
        if (!yes) return base
        return when (pending) {
            ClipConfirm.ClearCurrent -> base.copy(book = book.clearCurrent())
            is ClipConfirm.DeleteSection ->
                base.copy(book = book.deleteSection(pending.name), section = section.takeIf { it != pending.name })
            null -> base
        }
    }

    fun toggleSectionLock(name: String, now: Long): ClipSession {
        val target = book.sections.firstOrNull { it.name == name } ?: return this
        if (target.locked && !isOpen(now)) return this
        val next = copy(book = book.setSectionLocked(name, !target.locked))
        return if (target.locked) next else next.copy(grant = false, unlockedAt = null)
    }

    fun startNaming(): ClipSession =
        if (book.sections.size < ClipPolicy.MAX_SECTIONS) copy(mode = ClipMode.NAMING, naming = "") else this

    fun type(text: String): ClipSession =
        if (mode == ClipMode.NAMING) copy(naming = (naming + text).take(ClipPolicy.MAX_NAME)) else this

    fun backspace(): ClipSession {
        val cut = if (naming.lastOrNull()?.isLowSurrogate() == true) 2 else 1
        return if (mode == ClipMode.NAMING) copy(naming = naming.dropLast(cut)) else this
    }

    fun cancelNaming(): ClipSession = copy(mode = ClipMode.BROWSE, naming = "")

    fun confirmName(): ClipSession {
        val clean = naming.trim()
        if (clean.isEmpty()) return cancelNaming()
        return reset().copy(book = book.addSection(clean), mode = ClipMode.BROWSE, naming = "", tab = ClipTab.SAVED, section = clean)
    }

    fun beginUnlock(): ClipSession = if (unlocking) this else copy(unlocking = true)

    fun finishUnlock(ok: Boolean, now: Long): ClipSession =
        copy(unlocking = false, unlockedAt = if (ok) now else unlockedAt, grant = ok)

    fun snapshot(now: Long): ClipSnapshot {
        val selected = effectiveSection()
        val hidden = hiddenSections(now)
        val locked = tab == ClipTab.SAVED && selected != null && selected in hidden
        val items = when (tab) {
            ClipTab.CURRENT -> book.current
            ClipTab.PINNED -> book.pinned.filter { it.section == null || it.section !in hidden }
            ClipTab.SAVED -> if (selected == null || locked) emptyList() else book.saved(selected)
        }
        return ClipSnapshot(
            mode, allowed, enabled, tab, book.sections, selected, items, locked, unlocking, naming,
            assigning?.let { id -> items.firstOrNull { it.id == id } }, confirm,
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

    private fun reset(): ClipSession = copy(grant = false, assigning = null, confirm = null)
}
