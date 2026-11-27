package com.spacekeyboard.keyboard.core.otp

import android.text.InputType
import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpFieldRulesTest {
    private fun fit(inputType: Int, vararg labels: String?, options: Int = 0) =
        OtpFieldRules.fit(FieldTraits(inputType, options, labels.toList()))

    private fun code(bare: Boolean = false, receivedAt: Long = NOW) =
        OtpCode(CODE, receivedAt, 300_000L, OtpSource.CLIPBOARD, bare)

    @Test
    fun aCodeLabelMakesTheFieldCertain() {
        assertEquals(OtpFit.CERTAIN, fit(InputType.TYPE_CLASS_TEXT, "Enter OTP"))
        assertEquals(OtpFit.CERTAIN, fit(InputType.TYPE_CLASS_NUMBER, null, "Verification code"))
        assertEquals(OtpFit.CERTAIN, fit(InputType.TYPE_CLASS_TEXT, "أدخل رمز التحقق"))
    }

    @Test
    fun otherCodesAndPasswordsAreRuledOutByName() {
        assertEquals(OtpFit.NONE, fit(InputType.TYPE_CLASS_NUMBER, "Postal code"))
        assertEquals(OtpFit.NONE, fit(InputType.TYPE_CLASS_TEXT, "Promo code"))
        assertEquals(OtpFit.NONE, fit(InputType.TYPE_CLASS_TEXT, "Password"))
    }

    @Test
    fun withoutANameTheInputTypeDecides() {
        assertEquals(OtpFit.LIKELY, fit(InputType.TYPE_CLASS_NUMBER))
        assertEquals(OtpFit.NONE, fit(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL))
        assertEquals(OtpFit.MAYBE, fit(InputType.TYPE_CLASS_PHONE))
        assertEquals(OtpFit.MAYBE, fit(InputType.TYPE_CLASS_TEXT, null, ""))
        assertEquals(OtpFit.NONE, fit(InputType.TYPE_CLASS_DATETIME))
    }

    @Test
    fun textFieldsThatCannotTakeACodeAreRuledOut() {
        assertEquals(OtpFit.NONE, fit(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        assertEquals(OtpFit.NONE, fit(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS))
        assertEquals(OtpFit.NONE, fit(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE))
        assertEquals(OtpFit.NONE, fit(InputType.TYPE_CLASS_TEXT, options = EditorInfo.IME_ACTION_SEND))
    }

    @Test
    fun certainAndLikelyFieldsAcceptAFreshCode() {
        assertTrue(OtpGate.allows(OtpFit.CERTAIN, code(), NOW, ""))
        assertTrue(OtpGate.allows(OtpFit.LIKELY, code(bare = true), NOW, ""))
        assertTrue(OtpGate.allows(OtpFit.LIKELY, code(), NOW, "483"))
        assertFalse(OtpGate.allows(OtpFit.LIKELY, code(), NOW, "999"))
        assertTrue(OtpGate.allows(OtpFit.CERTAIN, code(), NOW, "999"))
    }

    @Test
    fun aFieldHoldingTheWholeCodeOrNoneRejectsIt() {
        assertFalse(OtpGate.allows(OtpFit.CERTAIN, code(), NOW, CODE))
        assertFalse(OtpGate.allows(OtpFit.NONE, code(), NOW, ""))
    }

    @Test
    fun anUncertainFieldOnlyTakesAMessageCodeForAShortTime() {
        assertTrue(OtpGate.allows(OtpFit.MAYBE, code(), NOW, ""))
        assertFalse(OtpGate.allows(OtpFit.MAYBE, code(bare = true), NOW, ""))
        assertFalse(OtpGate.allows(OtpFit.MAYBE, code(), NOW, "4"))
        assertFalse(OtpGate.allows(OtpFit.MAYBE, code(receivedAt = NOW - OtpGate.MAYBE_WINDOW_MS - 1), NOW, ""))
    }

    @Test
    fun anExpiredCodeIsNeverAllowed() {
        assertFalse(OtpGate.allows(OtpFit.CERTAIN, code(receivedAt = NOW - 300_001L), NOW, ""))
        assertFalse(OtpGate.allows(OtpFit.CERTAIN, code(receivedAt = NOW + 1L), NOW, ""))
    }

    @Test
    fun theNextChangeComesFromTheShorterLimit() {
        val held = code()
        assertEquals(held.receivedAt + OtpGate.MAYBE_WINDOW_MS, OtpGate.nextChange(OtpFit.MAYBE, held))
        assertEquals(held.expiresAt, OtpGate.nextChange(OtpFit.CERTAIN, held))
    }

    private companion object {
        const val CODE = "483920"
        const val NOW = 1_000_000L
    }
}
