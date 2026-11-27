package com.spacekeyboard.keyboard.core.otp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpExtractorTest {
    private fun code(text: String): String? = OtpExtractor.extract(text)?.code

    @Test
    fun englishMessages() {
        assertEquals("483920", code("Your verification code is 483920. Do not share it."))
        assertEquals("123456", code("123456 is your Facebook confirmation code"))
        assertEquals("4821", code("Your Uber code is 4821. Never share this code."))
        assertEquals("123456", code("G-123456 is your Google verification code."))
        assertEquals("987654", code("Use 987654 as your Microsoft security code"))
        assertEquals("1234567", code("Your account security code is 1234567"))
        assertEquals("12345678", code("Login code: 12345678"))
        assertEquals("246810", code("Your one-time password is 246810"))
    }

    @Test
    fun groupedAndSpacedCodes() {
        assertEquals("123456", code("Your verification code is 123-456"))
        assertEquals("123456", code("Your WhatsApp code: 123 456"))
        assertEquals("123456", code("123-456 is your WhatsApp code. Don't share it."))
        assertEquals("123456", code("Verification code: 12 34 56"))
        assertEquals("123456", code("Your code is 1 2 3 4 5 6"))
        assertEquals("12345678", code("Your code is 1234-5678"))
    }

    @Test
    fun arabicPersianAndOtherLanguages() {
        assertEquals("456789", code("رمز التحقق الخاص بك هو ٤٥٦٧٨٩"))
        assertEquals("456789", code("456789 هو رمز التحقق الخاص بك"))
        assertEquals("5678", code("استخدم 5678 كرمز تحقق لتسجيل الدخول"))
        assertEquals("123456", code("كود التفعيل: ١٢٣٤٥٦ لا تشاركه مع أحد"))
        assertEquals("112233", code("رمزك هو 112233"))
        assertEquals("654321", code("کد تایید شما: ۶۵۴۳۲۱"))
        assertEquals("112233", code("Doğrulama kodunuz: 112233"))
        assertEquals("778899", code("Votre code de vérification est 778899"))
        assertEquals("334455", code("Tu código de verificación es 334455"))
        assertEquals("998877", code("Ihr Bestätigungscode lautet 998877"))
        assertEquals("445566", code("Ваш код подтверждения: 445566"))
    }

    @Test
    fun bankMessagesPickTheCodeNotTheAmount() {
        assertEquals("483920", code("Your OTP for txn of INR 5,000.00 at Amazon is 483920. Valid for 10 mins."))
        assertEquals("729104", code("OTP 729104 to pay SAR 1500 at STORE. Never share your OTP."))
        assertEquals("556677", code("رمز التحقق 556677 لعملية شراء بمبلغ 2500 ريال. لا تشاركه"))
        assertNull(code("You paid USD 123456 to Ref 998877 on 12/05/2026"))
        assertNull(code("Your balance is 123456 SAR"))
        assertEquals("334422", code("Card ending 4455: use code 334422 to confirm"))
    }

    @Test
    fun ignoresNumbersThatAreNotCodes() {
        assertNull(code("Your order 123456 has shipped"))
        assertNull(code("Meeting at 12:30 on 2026-11-26"))
        assertNull(code("Call +1 800 123 4567 for support"))
        assertNull(code("Promo code 7788 gives 20% off"))
        assertNull(code("رقم الطلب 556677 قيد التجهيز"))
        assertNull(code("Happy new year 2026 from the team"))
        assertNull(code("Tracking number 1234567 is on its way"))
    }

    @Test
    fun codeAmongOtherNumbers() {
        assertEquals("246135", code("Call 800123 if this wasn't you. Your code is 246135."))
        assertEquals("246135", code("Your code 246135 expires in 5 minutes"))
        assertEquals("1357", code("Your PIN is 1357, valid until 2026"))
    }

    @Test
    fun smsRetrieverHashIsIgnored() {
        assertEquals("246810", code("<#> Your ExampleApp code is 246810 FA+9qCX9VSu"))
    }

    @Test
    fun urlsAndEmailsAreIgnored() {
        assertEquals("246810", code("Code 246810. Sign in: https://example.com/login?c=998877"))
        assertNull(code("Write to support1234@example.com or visit www.site5678.com"))
    }

    @Test
    fun alphanumericCodesNeedAKeywordNearby() {
        assertEquals("A1B2C3", code("Your verification code is A1B2C3"))
        assertNull(code("Order ABC123 shipped yesterday"))
        assertNull(code("iPhone15 deals this week"))
    }

    @Test
    fun invisibleMarksAndSpacesAreHandled() {
        assertEquals("123456", code("‏رمز التحقق: ١٢٣‎٤٥٦"))
        assertEquals("123456", code("Your code is 123456"))
    }

    @Test
    fun bareCodesFromTheClipboard() {
        val plain = OtpExtractor.extract("483920")
        assertEquals("483920", plain?.code)
        assertTrue(plain?.bare == true)
        assertEquals("123456", OtpExtractor.extract(" 123 456 ")?.code)
        assertEquals("123456", OtpExtractor.extract("١٢٣٤٥٦")?.code)
        assertNull(OtpExtractor.extract("2026"))
        assertNull(OtpExtractor.extract("123"))
        assertNull(OtpExtractor.extract("123456789"))
        assertNull(OtpExtractor.extract("hello"))
        assertNull(OtpExtractor.extract("   "))
    }

    @Test
    fun messageCodesAreNotBare() {
        assertFalse(OtpExtractor.extract("Your code is 483920")?.bare == true)
    }

    @Test
    fun validityShortensTheLifetime() {
        assertEquals(120_000L, OtpExtractor.extract("Your code is 483920. It is valid for 2 minutes.")?.ttlMs)
        assertEquals(OtpExtractor.MAX_TTL_MS, OtpExtractor.extract("Your code is 483920. Valid for 30 minutes.")?.ttlMs)
        assertEquals(OtpExtractor.MIN_TTL_MS, OtpExtractor.extract("Your code is 483920. Expires in 30 seconds.")?.ttlMs)
        assertEquals(OtpExtractor.DEFAULT_TTL_MS, OtpExtractor.extract("Your code is 483920")?.ttlMs)
        assertEquals(180_000L, OtpExtractor.extract("رمز التحقق 483920 صالح لمدة ٣ دقائق")?.ttlMs)
    }

    @Test
    fun hugeTextIsRejected() {
        assertNull(OtpExtractor.extract("code 123456 ".repeat(400)))
    }
}
