package com.spacekeyboard.keyboard.core.otp

import java.util.PriorityQueue

internal abstract class FormModel : OtpTarget {
    private class Task(val at: Long, val order: Long, val block: () -> Unit)

    private val tasks = PriorityQueue<Task>(compareBy<Task>({ it.at }, { it.order }))
    private var order = 0L
    var clock = 0L
        private set
    var epochValue = 0
    var isAlive = true
    var commits = 0
        private set
    var committedChars = 0
        private set

    protected fun schedule(delay: Long, block: () -> Unit) {
        tasks.add(Task(clock + delay, order++, block))
    }

    fun runAll() {
        var guard = 0
        while (guard++ < GUARD) {
            val next = tasks.poll() ?: return
            clock = next.at
            next.block()
        }
    }

    override fun epoch(): Int = epochValue

    override fun alive(): Boolean = isAlive

    override fun later(delayMs: Long, block: () -> Unit) = schedule(delayMs, block)

    override fun commit(text: String) {
        commits++
        committedChars += text.length
        put(text)
    }

    override fun moveToEnd() = Unit

    protected abstract fun put(text: String)

    abstract fun actual(): String

    private companion object {
        const val GUARD = 100_000
    }
}

internal class SingleField(
    private val max: Int? = null,
    private val rejectLong: Boolean = false,
    private val readable: Boolean = true,
    private val masked: Boolean = false,
    private val reportsCursor: Boolean = true,
    private val spurious: Boolean = false,
    initial: String = "",
) : FormModel() {
    private val content = StringBuilder(initial)

    override fun fits(): Boolean = true

    override fun read(): String? = when {
        !readable -> null
        masked -> "\u2022".repeat(content.length)
        else -> content.toString()
    }

    override fun cursor(): Int? = if (reportsCursor) content.length else null

    override fun put(text: String) {
        val limit = max
        if (rejectLong && limit != null && content.length + text.length > limit) return
        val incoming = if (limit == null) text else text.take((limit - content.length).coerceAtLeast(0))
        content.append(incoming)
        if (spurious) schedule(SPURIOUS_DELAY) { epochValue++ }
    }

    override fun erase(count: Int) {
        content.setLength((content.length - count).coerceAtLeast(0))
    }

    override fun wipe() = content.setLength(0)

    override fun actual(): String = content.toString()

    private companion object {
        const val SPURIOUS_DELAY = 50L
    }
}

internal class BoxRow(
    count: Int,
    private val delay: Long,
    private val spreads: Boolean = false,
    private val endsAfterLast: Boolean = false,
    private val fitsBelow: Int = Int.MAX_VALUE,
    private val stuckAt: Int = -1,
) : FormModel() {
    private val boxes = MutableList(count) { "" }
    private var current = 0

    override fun read(): String? = boxes[current]

    override fun cursor(): Int? = boxes[current].length

    override fun fits(): Boolean = current < fitsBelow

    override fun put(text: String) {
        if (spreads && text.length > 1) spread(text) else enter(text)
    }

    private fun spread(text: String) {
        val used = minOf(text.length, boxes.size)
        for (i in 0 until used) boxes[i] = text.substring(i, i + 1)
        schedule(delay) {
            current = used - 1
            epochValue++
        }
    }

    private fun enter(text: String) {
        if (boxes[current].isNotEmpty() || current == stuckAt) return
        boxes[current] = text.substring(0, 1)
        val at = current
        when {
            at < boxes.size - 1 -> schedule(delay) {
                current = at + 1
                epochValue++
            }
            endsAfterLast -> schedule(delay) {
                isAlive = false
                epochValue++
            }
        }
    }

    override fun erase(count: Int) {
        boxes[current] = boxes[current].dropLast(count)
    }

    override fun wipe() {
        boxes[current] = ""
    }

    override fun actual(): String = boxes.joinToString("")
}
