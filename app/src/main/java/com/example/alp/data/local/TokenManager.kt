package com.example.alp.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.alp.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

    private val keyAlias = "ALP_Keystore_AES_Key"

    init {
        getOrCreateSecretKey()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (keyStore.containsAlias(keyAlias)) {
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
            if (entry != null) return entry.secretKey
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encrypt(input: String): String? {
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(input.toByteArray(Charsets.UTF_8))
            val combined = iv + encryptedBytes
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun decrypt(encryptedStr: String): String? {
        return try {
            val combined = Base64.decode(encryptedStr, Base64.NO_WRAP)
            if (combined.size < 12) return null
            val iv = combined.copyOfRange(0, 12)
            val encryptedBytes = combined.copyOfRange(12, combined.size)

            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            clear()
            null
        }
    }

    fun saveToken(token: String) {
        val encrypted = encrypt(token) ?: token
        prefs.edit().putString(Constants.KEY_JWT_TOKEN, encrypted).apply()
    }

    fun getToken(): String? {
        val encrypted = prefs.getString(Constants.KEY_JWT_TOKEN, null) ?: return null
        return decrypt(encrypted)
    }

    fun saveUserInfo(email: String, timezone: String) {
        prefs.edit()
            .putString(Constants.KEY_USER_EMAIL, email)
            .putString(Constants.KEY_USER_TIMEZONE, timezone)
            .apply()
    }

    fun getTimezone(): String {
        return prefs.getString(Constants.KEY_USER_TIMEZONE, "Asia/Jakarta") ?: "Asia/Jakarta"
    }

    fun saveReminderSettings(enabled: Boolean, breakfast: String, lunch: String, dinner: String) {
        prefs.edit()
            .putBoolean("reminder_enabled", enabled)
            .putString("reminder_breakfast", breakfast)
            .putString("reminder_lunch", lunch)
            .putString("reminder_dinner", dinner)
            .apply()
    }

    fun getReminderEnabled(): Boolean = prefs.getBoolean("reminder_enabled", true)
    fun getBreakfastTime(): String = prefs.getString("reminder_breakfast", "08:00") ?: "08:00"
    fun getLunchTime(): String = prefs.getString("reminder_lunch", "13:00") ?: "13:00"
    fun getDinnerTime(): String = prefs.getString("reminder_dinner", "19:00") ?: "19:00"

    fun hasAskedNotificationPermission(): Boolean = prefs.getBoolean("notification_asked", false)
    fun setAskedNotificationPermission(asked: Boolean = true) {
        prefs.edit().putBoolean("notification_asked", asked).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty()
    }
}
