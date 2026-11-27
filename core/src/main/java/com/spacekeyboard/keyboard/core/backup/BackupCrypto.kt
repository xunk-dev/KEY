package com.spacekeyboard.keyboard.core.backup

import java.security.GeneralSecurityException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONObject

object BackupCrypto {
    const val MIN_PASSWORD = 8
    private const val FORMAT = "space-keyboard-backup-encrypted"
    private const val VERSION = 1
    private const val SHA256 = "PBKDF2WithHmacSHA256"
    private const val SHA1 = "PBKDF2WithHmacSHA1"
    private const val ITERATIONS = 200_000
    private const val MAX_ITERATIONS = 1_000_000
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val KEY_BITS = 256
    private const val TAG_BITS = 128
    private const val CIPHER = "AES/GCM/NoPadding"

    sealed interface Opened {
        data class Text(val value: String) : Opened
        data object WrongPassword : Opened
        data object Invalid : Opened
    }

    fun isEncrypted(text: String): Boolean {
        val root = runCatching { JSONObject(text) }.getOrNull() ?: return false
        return root.optString("format") == FORMAT
    }

    fun encrypt(plain: String, password: CharArray): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val kdf = availableKdf()
        val key = derive(password, salt, ITERATIONS, kdf)
        val cipher = Cipher.getInstance(CIPHER)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        val data = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("kdf", kdf)
            .put("iterations", ITERATIONS)
            .put("salt", Hex.encode(salt))
            .put("iv", Hex.encode(iv))
            .put("data", Hex.encode(data))
            .toString()
    }

    fun decrypt(text: String, password: CharArray): Opened {
        val root = runCatching { JSONObject(text) }.getOrNull() ?: return Opened.Invalid
        if (root.optString("format") != FORMAT || root.optInt("version", -1) != VERSION) return Opened.Invalid
        val kdf = root.optString("kdf")
        val iterations = root.optInt("iterations", 0)
        val salt = Hex.decode(root.optString("salt"))
        val iv = Hex.decode(root.optString("iv"))
        val data = Hex.decode(root.optString("data"))
        val valid = kdf in setOf(SHA256, SHA1) && iterations in 1..MAX_ITERATIONS &&
            salt != null && salt.size == SALT_BYTES && iv != null && iv.size == IV_BYTES && data != null
        if (!valid) return Opened.Invalid
        return open(password, kdf, iterations, salt, iv, data)
    }

    private fun open(password: CharArray, kdf: String, iterations: Int, salt: ByteArray, iv: ByteArray, data: ByteArray): Opened {
        val key = runCatching { derive(password, salt, iterations, kdf) }.getOrNull() ?: return Opened.Invalid
        return try {
            val cipher = Cipher.getInstance(CIPHER)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
            Opened.Text(String(cipher.doFinal(data), Charsets.UTF_8))
        } catch (_: GeneralSecurityException) {
            Opened.WrongPassword
        }
    }

    private fun availableKdf(): String = try {
        SecretKeyFactory.getInstance(SHA256)
        SHA256
    } catch (_: NoSuchAlgorithmException) {
        SHA1
    }

    private fun derive(password: CharArray, salt: ByteArray, iterations: Int, kdf: String): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        try {
            val bytes = SecretKeyFactory.getInstance(kdf).generateSecret(spec).encoded
            return SecretKeySpec(bytes, "AES")
        } finally {
            spec.clearPassword()
        }
    }
}
