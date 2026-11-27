package com.spacekeyboard.keyboard.emoji.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmojiHintsTest {
    private val grin = Emoji("😀")
    private val cat = Emoji("🐱")
    private val pizza = Emoji("🍕")
    private val heart = Emoji("❤️")
    private val thumbs = Emoji("👍")

    private val english = mapOf(
        grin.text to EmojiName("grinning face", listOf("face", "grin", "smile")),
        cat.text to EmojiName("cat face", listOf("pet", "kitten", "face")),
        pizza.text to EmojiName("pizza", listOf("food", "slice")),
        heart.text to EmojiName("red heart", listOf("love")),
        thumbs.text to EmojiName("thumbs up", listOf("yes")),
    )
    private val arabic = mapOf(
        pizza.text to EmojiName("بيتزا", listOf("طعام")),
        heart.text to EmojiName("قلب أحمر", listOf("حب", "قلب")),
    )
    private val all = listOf(grin, cat, pizza, heart, thumbs)

    private fun hints(curated: Map<String, List<String>> = emptyMap()) = EmojiHints(all, listOf(english, arabic), curated)

    @Test
    fun aSingleWordNameMatchesWhatItNames() {
        assertEquals(pizza, hints().find("pizza"))
        assertEquals(pizza, hints().find("  Pizza "))
        assertEquals(pizza, hints().find("بيتزا"))
    }

    @Test
    fun aKeywordOnlyOneEmojiHasMatches() {
        assertEquals(cat, hints().find("kitten"))
        assertEquals(heart, hints().find("حب"))
    }

    @Test
    fun ambiguousWordsGiveNothing() {
        assertNull(hints().find("face"))
    }

    @Test
    fun unknownTooShortOrTooLongWordsGiveNothing() {
        assertNull(hints().find("zebra"))
        assertNull(hints().find(""))
        assertNull(hints().find("a".repeat(40)))
    }

    @Test
    fun shortLatinKeywordsAreNotMatchedAutomatically() {
        val short = EmojiHints(listOf(thumbs), listOf(mapOf(thumbs.text to EmojiName("thumbs up", listOf("ok")))))
        assertNull(short.find("ok"))
    }

    @Test
    fun curatedWordsWinAndMayBeShort() {
        val curated = mapOf("ok" to listOf("👍"), "face" to listOf("😀"), "pizza" to listOf("🐱"))
        val found = hints(curated)
        assertEquals(thumbs, found.find("ok"))
        assertEquals(grin, found.find("face"))
        assertEquals(cat, found.find("pizza"))
    }

    @Test
    fun curatedEmojiMatchWithoutTheVariationSelector() {
        assertEquals(heart, hints(mapOf("love" to listOf("❤"))).find("love"))
    }

    @Test
    fun curatedEmojiMissingFromTheCatalogAreSkipped() {
        val found = hints(mapOf("party" to listOf("🥳", "🐱")))
        assertEquals(cat, found.find("party"))
        assertNull(hints(mapOf("party" to listOf("🥳"))).find("party"))
    }

    @Test
    fun arabicFormsAreNormalisedAndTheArticleIsDropped() {
        val found = hints(mapOf("اسد" to listOf("🐱")))
        assertEquals(cat, found.find("أسد"))
        assertEquals(cat, found.find("الاسد"))
        assertEquals(heart, hints().find("الحب"))
    }
}
