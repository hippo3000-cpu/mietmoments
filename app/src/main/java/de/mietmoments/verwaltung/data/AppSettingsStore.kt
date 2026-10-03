package de.mietmoments.verwaltung.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class AppSettings(
    val serverUrl: String = "https://mietmoments.de/verwaltung/",
    val token: String = "",
    val momoEnabled: Boolean = true,
    val animationsEnabled: Boolean = true
) {
    val configured: Boolean get() = serverUrl.startsWith("https://") && token.isNotBlank()
}

class AppSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("mietmoments_settings_v2", Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        serverUrl = prefs.getString("server_url", "https://mietmoments.de/verwaltung/") ?: "https://mietmoments.de/verwaltung/",
        token = decrypt(prefs.getString("token", "").orEmpty()),
        momoEnabled = prefs.getBoolean("momo_enabled", true),
        animationsEnabled = prefs.getBoolean("animations_enabled", true)
    )

    fun save(serverUrl: String, token: String, momoEnabled: Boolean, animationsEnabled: Boolean) {
        val normalized = serverUrl.trim().let { if (it.endsWith('/')) it else "$it/" }
        prefs.edit()
            .putString("server_url", normalized)
            .putString("token", encrypt(token.trim()))
            .putBoolean("momo_enabled", momoEnabled)
            .putBoolean("animations_enabled", animationsEnabled)
            .apply()
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(value: String): String {
        if (value.isBlank()) return ""
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, key())
            val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
            val body = Base64.encodeToString(cipher.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
            "$iv:$body"
        }.getOrDefault("")
    }

    private fun decrypt(value: String): String {
        if (!value.contains(':')) return ""
        return runCatching {
            val (iv64, body64) = value.split(':', limit = 2)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(iv64, Base64.NO_WRAP)))
            String(cipher.doFinal(Base64.decode(body64, Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrDefault("")
    }

    companion object { private const val KEY_ALIAS = "mietmoments_api_token_v2" }
}
