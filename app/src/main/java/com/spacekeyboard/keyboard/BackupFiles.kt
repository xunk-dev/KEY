package com.spacekeyboard.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.spacekeyboard.keyboard.core.backup.BackupCrypto
import com.spacekeyboard.keyboard.core.backup.PrefsBackup
import java.io.ByteArrayOutputStream
import java.io.InputStream

internal object BackupFiles {
    private const val LIMIT = 4_500_000
    private const val CHUNK = 8192

    fun write(context: Context, uri: Uri, includeWords: Boolean, password: CharArray): Int = runCatching {
        val plain = backup(context).export(includeWords)
        val text = if (password.isEmpty()) plain else BackupCrypto.encrypt(plain, password)
        val stream = context.contentResolver.openOutputStream(uri, "wt") ?: error("no stream")
        stream.use { it.write(text.toByteArray(Charsets.UTF_8)) }
        if (password.isEmpty()) R.string.backup_ok_export else R.string.backup_ok_export_locked
    }.getOrElse { DebugLog.failure(context, "backup write", it); R.string.backup_failed }

    fun read(context: Context, uri: Uri, password: CharArray): Int = runCatching {
        val stream = context.contentResolver.openInputStream(uri) ?: error("no stream")
        val bytes = stream.use { readLimited(it) }
        if (bytes == null) R.string.backup_large else restore(context, bytes.toString(Charsets.UTF_8), password)
    }.getOrElse { DebugLog.failure(context, "backup read", it); R.string.backup_failed }

    private fun restore(context: Context, text: String, password: CharArray): Int {
        if (!BackupCrypto.isEncrypted(text)) return verdict(backup(context).import(text))
        if (password.isEmpty()) return R.string.backup_need_password
        return when (val opened = BackupCrypto.decrypt(text, password)) {
            is BackupCrypto.Opened.Text -> verdict(backup(context).import(opened.value))
            BackupCrypto.Opened.WrongPassword -> R.string.backup_wrong_password
            BackupCrypto.Opened.Invalid -> R.string.backup_invalid
        }
    }

    private fun backup(context: Context): PrefsBackup = PrefsBackup(
        open = { name: String -> context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE) },
        readable = { name: String, prefs: SharedPreferences -> BackupFeatureChecks.passes(name, prefs) },
    )

    private fun verdict(result: PrefsBackup.Result): Int = when (result) {
        PrefsBackup.Result.OK -> R.string.backup_ok_import
        PrefsBackup.Result.INVALID -> R.string.backup_invalid
        PrefsBackup.Result.TOO_LARGE -> R.string.backup_large
    }

    fun readLimited(input: InputStream, limit: Int = LIMIT): ByteArray? {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(CHUNK)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) return out.toByteArray()
            out.write(buffer, 0, count)
            if (out.size() > limit) return null
        }
    }
}
