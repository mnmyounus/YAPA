package com.mnmyounus.yala.data.local.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mnmyounus.yala.domain.model.LockCredential
import com.mnmyounus.yala.domain.model.LockType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backed by [EncryptedSharedPreferences], whose master key lives in the
 * Android Keystore (hardware-backed on most devices) — so even a rooted
 * device with file access only sees ciphertext, never raw credential hashes
 * in the clear on disk without the keystore key.
 */
@Singleton
class SecurePreferencesDataSource @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "yala_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val onboardedFlow = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDED, false))

    fun observeOnboarded() = onboardedFlow.asStateFlow()

    fun setOnboarded(complete: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDED, complete).apply()
        onboardedFlow.value = complete
    }

    fun saveGlobalCredential(credential: LockCredential) =
        prefs.edit().putString(KEY_GLOBAL_CREDENTIAL, credential.toJson()).apply()

    fun getGlobalCredential(): LockCredential? =
        prefs.getString(KEY_GLOBAL_CREDENTIAL, null)?.let { LockCredential.fromJson(it) }

    fun saveAppCredential(packageName: String, credential: LockCredential) =
        prefs.edit().putString(KEY_APP_CREDENTIAL_PREFIX + packageName, credential.toJson()).apply()

    fun getAppCredential(packageName: String): LockCredential? =
        prefs.getString(KEY_APP_CREDENTIAL_PREFIX + packageName, null)?.let { LockCredential.fromJson(it) }

    fun saveRecoveryKey(hash: String, salt: String) {
        prefs.edit()
            .putString(KEY_RECOVERY_HASH, hash)
            .putString(KEY_RECOVERY_SALT, salt)
            .apply()
    }

    fun getRecoveryHash(): String? = prefs.getString(KEY_RECOVERY_HASH, null)
    fun getRecoverySalt(): String? = prefs.getString(KEY_RECOVERY_SALT, null)

    private companion object {
        const val KEY_ONBOARDED = "onboarded"
        const val KEY_GLOBAL_CREDENTIAL = "global_credential"
        const val KEY_APP_CREDENTIAL_PREFIX = "app_credential_"
        const val KEY_RECOVERY_HASH = "recovery_hash"
        const val KEY_RECOVERY_SALT = "recovery_salt"
    }
}

private fun LockCredential.toJson(): String = JSONObject().apply {
    put("type", type.id)
    put("hint", hintText ?: "")
    put("hash", credentialHash)
    put("salt", salt)
    put("images", JSONArray(imagePoolUris))
    put("seqLen", secretSequenceLength)
}.toString()

private fun LockCredential.Companion.fromJson(json: String): LockCredential {
    val o = JSONObject(json)
    val images = mutableListOf<String>()
    val arr = o.optJSONArray("images")
    if (arr != null) for (i in 0 until arr.length()) images.add(arr.getString(i))
    return LockCredential(
        type = LockType.fromId(o.getInt("type")),
        hintText = o.optString("hint").ifEmpty { null },
        credentialHash = o.getString("hash"),
        salt = o.getString("salt"),
        imagePoolUris = images,
        secretSequenceLength = o.optInt("seqLen", 0)
    )
}
