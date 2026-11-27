package com.spacekeyboard.keyboard.clipboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipClassifierTest {
    private fun kinds(text: String) = ClipClassifier.kinds(text)

    private fun only(kind: ClipKind, vararg samples: String) =
        samples.forEach { assertEquals(it, listOf(kind), kinds(it)) }

    @Test
    fun linksAreFoundAnywhereInsideLongText() {
        only(
            ClipKind.LINK,
            "https://example.com/a?b=1",
            "www.example.com",
            "t.me/zaid_dev",
            "example.com/path",
            "check this https://example.com/page and tell me what you think about it, it is a long text",
            "اقرأ هذا https://example.com/x ثم أخبرني",
        )
    }

    @Test
    fun emailsAreNotMistakenForLinksOrUsernames() {
        only(ClipKind.EMAIL, "zaid@gmail.com", "contact: zaid@gmail.com please")
    }

    @Test
    fun phoneNumbersAreRecognisedInManyShapesAndDigits() {
        only(
            ClipKind.PHONE,
            "+963 944 123 456",
            "0944123456",
            "+963944123456",
            "00963944123456",
            "٠٩٤٤١٢٣٤٥٦",
            "(555) 123-4567",
            "رقمي 0944 123 456 اتصل",
        )
    }

    @Test
    fun usernamesNeedTheAtSignAndASaneName() {
        only(ClipKind.USERNAME, "@zaid", "@zaid_dev", "follow @zaid and @ali_99 now")
        only(ClipKind.TEXT, "hi @ 5pm")
    }

    @Test
    fun tokensAndKeysAreRecognised() {
        only(
            ClipKind.TOKEN,
            "sk-abcdefghijklmnopqrstuvwxyz123456",
            "ghp_abcdefghijklmnopqrstuvwxyz0123456789",
            "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.abcdefghijklmnop",
            "123456789:AAEhBOweik6ad9r_QXMENQjcrGbqCr4K-Ak",
            "AKIAIOSFODNN7EXAMPLE",
            "3f2a9c1e5b7d4a8c9e0f1a2b3c4d5e6f",
            "550e8400-e29b-41d4-a716-446655440000",
            "api_key=abcd1234efgh",
            "password: hunter2hunter2",
            "a1B2c3D4e5F6g7H8i9J0",
            "Bearer abcdefghijklmnopqrstuvwx12345",
        )
    }

    @Test
    fun plainNumbersStayNumbers() {
        only(
            ClipKind.NUMBER,
            "123456",
            "٣٤٥٦٧٨",
            "1,234.50",
            "\$1,200",
            "2026-11-25",
            "1234567890",
            "4111 1111 1111 1111",
            "192.168.1.1",
            "12:30",
        )
    }

    @Test
    fun ordinaryTextIsNeverOverClassified() {
        only(
            ClipKind.TEXT,
            "hello world",
            "I have 5 apples and 12 oranges",
            "internationalization",
            "my-very-long-file-name-2024",
            "مرحبا كيف حالك",
            "getUserProfileDataFromServer",
            "main.sh",
            "see report.pdf",
            "meeting at 10:30 tomorrow",
            "order #12345 shipped",
        )
    }

    @Test
    fun mixedTextListsEveryKindInPriorityOrder() {
        assertEquals(listOf(ClipKind.LINK, ClipKind.USERNAME), kinds("Follow @zaid https://x.com/zaid"))
        assertEquals(listOf(ClipKind.EMAIL, ClipKind.PHONE), kinds("mail zaid@gmail.com or call +963 944 123 456"))
        assertEquals(listOf(ClipKind.LINK), kinds("https://x.com/@zaid"))
        assertEquals(
            listOf(ClipKind.LINK, ClipKind.TOKEN),
            kinds("https://api.telegram.org/bot123456789:AAEhBOweik6ad9r_QXMENQjcrGbqCr4K-Ak/getMe"),
        )
    }

    @Test
    fun primaryKindIsTheFirstOne() {
        assertEquals(ClipKind.LINK, ClipClassifier.primary("Follow @zaid https://x.com/zaid"))
        assertEquals(ClipKind.TEXT, ClipClassifier.primary("   "))
    }

    @Test
    fun hugeInputsStayFast() {
        val start = System.nanoTime()
        listOf("a".repeat(5000), "1".repeat(5000), "a.".repeat(2500), "1 ".repeat(2500), "x@".repeat(2500)).forEach { kinds(it) }
        assertTrue((System.nanoTime() - start) / 1_000_000 < 3000)
    }
}
