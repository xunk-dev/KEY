package com.spacekeyboard.keyboard.autofix

class FakeField : TextField {
    val text = StringBuilder()
    var selected = false

    override fun before(length: Int): String = text.takeLast(length).toString()

    override fun after(length: Int): String = ""

    override fun commit(text: String) {
        this.text.append(text)
    }

    override fun deleteBefore(length: Int) {
        text.setLength((text.length - length).coerceAtLeast(0))
    }

    override fun deleteLast() {
        deleteBefore(1)
    }

    override fun hasSelection(): Boolean = selected

    override fun batch(block: () -> Unit) = block()
}

class Typing(
    config: AutoFixConfig = AutoFixConfig(),
    val model: ErrorModel = ErrorModel(),
    mayLearn: Boolean = true,
    mayFix: Boolean = true,
) {
    val pad = FakeField()
    val engine = AutoFixEngine(model, config) { DAY }
    private val typer = AutoFixTyper(engine)

    init {
        engine.setField(mayLearn, mayFix)
    }

    val text: String get() = pad.text.toString()

    fun type(letters: String, zone: Int = 4): Typing {
        letters.forEach { typer.type(it.toString(), zone, pad) }
        return this
    }

    fun zones(vararg pairs: Pair<Char, Int>): Typing {
        pairs.forEach { typer.type(it.first.toString(), it.second, pad) }
        return this
    }

    fun back(count: Int = 1): Typing {
        repeat(count) { typer.delete(pad) }
        return this
    }

    fun space(): Typing {
        typer.boundary(" ", pad)
        return this
    }

    fun enter(): Typing {
        typer.beforeEnter(pad)
        pad.commit("\n")
        return this
    }

    companion object {
        const val DAY = 100
    }
}
