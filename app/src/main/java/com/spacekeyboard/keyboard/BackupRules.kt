package com.spacekeyboard.keyboard

import com.spacekeyboard.keyboard.core.backup.BackupCrypto

internal object BackupRules {
    fun exportProblem(password: String, repeat: String): Int? = when {
        password.isEmpty() -> null
        password.length < BackupCrypto.MIN_PASSWORD -> R.string.backup_password_short
        password != repeat -> R.string.backup_password_mismatch
        else -> null
    }
}
