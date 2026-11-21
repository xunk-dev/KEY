package com.spacekeyboard.keyboard.clipboard

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.annotation.RequiresApi

class ClipUnlockActivity : Activity() {
    private var done = false
    private var signal: CancellationSignal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val started = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) askBiometric() else askKeyguard()
        if (!started) {
            Toast.makeText(this, R.string.clip_unlock_failed, Toast.LENGTH_LONG).show()
            complete(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun askBiometric(): Boolean {
        val builder = BiometricPrompt.Builder(this)
            .setTitle(getString(R.string.clip_unlock_title))
            .setSubtitle(getString(R.string.clip_unlock_subtitle))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            builder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            )
        } else {
            @Suppress("DEPRECATION")
            builder.setDeviceCredentialAllowed(true)
        }
        val cancel = CancellationSignal().also { signal = it }
        return runCatching { builder.build().authenticate(cancel, mainExecutor, biometricCallback()) }.isSuccess
    }

    private fun askKeyguard(): Boolean {
        val manager = getSystemService(KeyguardManager::class.java)
        if (!manager.isDeviceSecure) return false
        val intent = manager.createConfirmDeviceCredentialIntent(
            getString(R.string.clip_unlock_title),
            getString(R.string.clip_unlock_subtitle),
        ) ?: return false
        startActivityForResult(intent, REQUEST)
        return true
    }

    @Deprecated("Platform activity result callback")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST) complete(resultCode == RESULT_OK)
    }

    override fun onDestroy() {
        signal?.cancel()
        if (!done) {
            done = true
            ClipUnlock.deliver(false)
        }
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun biometricCallback() = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) = complete(true)

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) = complete(false)
    }

    private fun complete(ok: Boolean) {
        if (done) return
        done = true
        ClipUnlock.deliver(ok)
        finish()
    }

    private companion object {
        const val REQUEST = 7
    }
}
