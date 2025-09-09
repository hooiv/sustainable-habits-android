package com.example.myapplication.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {
    /**
     * Quick check: can we authenticate with strong biometrics on this device right now?
     */
    fun canUseBiometrics(context: Context): Boolean {
        val bm = BiometricManager.from(context)
        val result = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Broader check: allow device credential fallback if available.
     */
    fun canUseBiometricOrCredential(context: Context): Boolean {
        val bm = BiometricManager.from(context)
        val result = bm.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Create a BiometricPrompt with simple success/error callbacks.
     * The caller decides when to call authenticate(promptInfo).
     */
    fun createBiometricPrompt(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (code: Int, message: String) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errorCode, errString.toString())
            }
            override fun onAuthenticationFailed() {
                // Treat as a soft error; don't invoke onError to allow retries.
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    /**
     * Build a PromptInfo based on whether you want to allow device credential fallback.
     * If allowDeviceCredential = false, a negative button text is required by the API.
     */
    fun buildPromptInfo(
        title: String,
        subtitle: String? = null,
        description: String? = null,
        allowDeviceCredential: Boolean = false,
        negativeButtonText: String = "Cancel"
    ): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
        subtitle?.let { builder.setSubtitle(it) }
        description?.let { builder.setDescription(it) }
        if (allowDeviceCredential) {
            builder.setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
        } else {
            builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            builder.setNegativeButtonText(negativeButtonText)
        }
        return builder.build()
    }
}

