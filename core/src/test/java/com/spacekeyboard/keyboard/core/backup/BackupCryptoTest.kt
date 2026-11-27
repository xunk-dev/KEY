package com.spacekeyboard.keyboard.core.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCryptoTest {
    private val password = "correct horse".toCharArray()

    @Test
    fun roundTripKeepsArabicText() {
        val sealed = BackupCrypto.encrypt("{\"نص\":1}", password)
        assertEquals(BackupCrypto.Opened.Text("{\"نص\":1}"), BackupCrypto.decrypt(sealed, password))
    }

    @Test
    fun sealedTextIsMarkedAndHidesTheContent() {
        val sealed = BackupCrypto.encrypt("secret words", password)
        assertTrue(BackupCrypto.isEncrypted(sealed))
        assertFalse(sealed.contains("secret"))
    }

    @Test
    fun plainBackupIsNotEncrypted() {
        assertFalse(BackupCrypto.isEncrypted("{\"version\":1,\"files\":{}}"))
        assertFalse(BackupCrypto.isEncrypted("not json"))
    }

    @Test
    fun wrongPasswordIsReported() {
        val sealed = BackupCrypto.encrypt("data", password)
        assertEquals(BackupCrypto.Opened.WrongPassword, BackupCrypto.decrypt(sealed, "other password".toCharArray()))
    }

    @Test
    fun tamperedDataIsRejected() {
        val sealed = BackupCrypto.encrypt("data", password)
        val flipped = sealed.replace("\"data\":\"", "\"data\":\"00")
        assertNotEquals(BackupCrypto.Opened.Text("data"), BackupCrypto.decrypt(flipped, password))
    }

    @Test
    fun eachFileUsesNewSaltAndIv() {
        assertNotEquals(BackupCrypto.encrypt("same", password), BackupCrypto.encrypt("same", password))
    }

    @Test
    fun brokenEnvelopeIsInvalid() {
        assertEquals(BackupCrypto.Opened.Invalid, BackupCrypto.decrypt("{}", password))
        assertEquals(BackupCrypto.Opened.Invalid, BackupCrypto.decrypt("nope", password))
        val sealed = BackupCrypto.encrypt("data", password)
        assertEquals(BackupCrypto.Opened.Invalid, BackupCrypto.decrypt(sealed.replace("\"iterations\":200000", "\"iterations\":99999999"), password))
    }

    @Test
    fun hexRoundTripAndRejectsBadInput() {
        val bytes = byteArrayOf(0, 1, 127, -1, -128)
        assertEquals(bytes.toList(), Hex.decode(Hex.encode(bytes))!!.toList())
        assertNull(Hex.decode("abc"))
        assertNull(Hex.decode("zz"))
    }
}
