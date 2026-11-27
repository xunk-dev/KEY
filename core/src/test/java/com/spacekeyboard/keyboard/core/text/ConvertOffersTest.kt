package com.spacekeyboard.keyboard.core.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertOffersTest {
    private fun texts(before: String) = ConvertOffers.forTail(before).map { it.text }

    @Test
    fun westernNumberOffersArabicIndic() {
        assertEquals(listOf("١٢٣٤"), texts("الرقم 1234"))
        assertEquals(4, ConvertOffers.forTail("الرقم 1234").single().remove)
    }

    @Test
    fun arabicIndicNumberOffersWestern() {
        assertEquals(listOf("1234"), texts("سعره ١٢٣٤"))
    }

    @Test
    fun persianAndMixedNumbersOfferBoth() {
        assertEquals(listOf("123", "١٢٣"), texts("۱۲۳"))
        assertEquals(listOf("123", "١٢٣"), texts("1٢3"))
    }

    @Test
    fun separatorsStayAndTheWholeNumberIsReplaced() {
        assertEquals(listOf("١٢,٥٠٠.٧٥"), texts("12,500.75"))
        assertEquals(listOf("١٠:٣٠"), texts("10:30"))
    }

    @Test
    fun nothingOfferedAwayFromNumbers() {
        assertTrue(texts("").isEmpty())
        assertTrue(texts("مرحبا").isEmpty())
        assertTrue(texts("1234 ").isEmpty())
        assertTrue(texts("abc123").isEmpty())
        assertTrue(texts("ص10").isEmpty())
    }

    @Test
    fun gregorianDateOffersHijriThenDigits() {
        assertEquals(
            listOf("16/06/1448هـ", "١٦/٠٦/١٤٤٨هـ", "٢٦/١١/٢٠٢٦"),
            texts("اليوم 26/11/2026"),
        )
        assertEquals(10, ConvertOffers.forTail("26/11/2026").first().remove)
    }

    @Test
    fun hijriDateOffersGregorian() {
        assertEquals(
            listOf("27/10/2026م", "٢٧/١٠/٢٠٢٦م", "١٦/٠٥/١٤٤٨"),
            texts("16/05/1448"),
        )
    }

    @Test
    fun arabicDigitsKeepTheirScriptFirst() {
        assertEquals(
            listOf("١٦/٠٦/١٤٤٨هـ", "16/06/1448هـ", "26/11/2026"),
            texts("٢٦/١١/٢٠٢٦"),
        )
    }

    @Test
    fun markerIsReplacedAndSwitches() {
        val offers = ConvertOffers.forTail("موعد 1448/5/16هـ")
        assertEquals("2026/10/27م", offers.first().text)
        assertEquals("1448/5/16هـ".length, offers.first().remove)
        assertEquals("2026/10/27م", texts("1448/5/16 هـ").first())
        assertEquals("1448-06-16هـ", texts("2026-11-26 م").first())
    }

    @Test
    fun yearFirstOrderIsKept() {
        assertEquals("1448/06/16هـ", texts("2026/11/26").first())
        assertEquals("1448-06-16هـ", texts("2026-11-26").first())
    }

    @Test
    fun unpaddedInputStaysUnpadded() {
        assertEquals("25/5/1448هـ", texts("5/11/2026").first())
    }

    @Test
    fun impossibleOrAmbiguousDatesGetNoCalendarOffer() {
        assertEquals(listOf("٣٠/٠٢/٢٠٢٦"), texts("30/02/2026"))
        assertEquals(listOf("٢٦/١١/٢٦"), texts("26/11/26"))
        assertEquals(listOf("٠١/٠١/١٨٠٠"), texts("01/01/1800"))
    }
}
