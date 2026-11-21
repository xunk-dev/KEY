package com.spacekeyboard.keyboard.decorate

class Frame(val prefix: String, val suffix: String)

class Marks(val mark: String) {
    fun apply(text: String): String {
        val builder = StringBuilder()
        text.forEach { c ->
            builder.append(c)
            if (!c.isWhitespace() && !c.isLowSurrogate() && !c.isHighSurrogate()) builder.append(mark)
        }
        return builder.toString()
    }
}

class ComposedTransform(
    private val alphabet: Alphabet?,
    private val marks: Marks?,
    private val frame: Frame?,
) : Transform {
    override fun text(source: String): String {
        val mapped = alphabet?.text(source) ?: source
        val marked = marks?.apply(mapped) ?: mapped
        return if (frame == null) marked else frame.prefix + marked + frame.suffix
    }

    override fun typed(before: String, input: String): String {
        val mapped = alphabet?.text(input) ?: input
        return marks?.apply(mapped) ?: mapped
    }
}

object EnglishStyles {
    private val marks = listOf("̶", "̲", "̳", "̸", "̅", "̇", "̃", "͓")
    private val framed = listOf("bold", "italic", "script", "fraktur", "double", "mono", "circled", "smallcaps", "fullwidth")
    val frames: List<Frame> = listOf(
        Frame("★ ", " ★"), Frame("✦ ", " ✦"), Frame("【", "】"),
        Frame("『", "』"), Frame("«", "»"), Frame("꧁", "꧂"),
        Frame("♥ ", " ♥"), Frame("░", "░"), Frame("▌", "▐"),
        Frame("(•", "•)"), Frame("─ ", " ─"), Frame("☾ ", " ☽"),
    )

    val all: List<DecorStyle> = buildList {
        add(style("en_plain", null, null, null))
        EnglishAlphabets.all.forEach { add(style("en_${it.id}", it, null, null)) }
        marks.forEachIndexed { i, m -> add(style("en_mark$i", null, Marks(m), null)) }
        frames.forEachIndexed { f, frame ->
            add(style("en_frame$f", null, null, frame))
            framed.forEach { add(style("en_${it}_frame$f", EnglishAlphabets.byId(it), null, frame)) }
        }
    }

    private fun style(id: String, alphabet: Alphabet?, marks: Marks?, frame: Frame?) =
        DecorStyle(id, Language.EN, ComposedTransform(alphabet, marks, frame))
}
