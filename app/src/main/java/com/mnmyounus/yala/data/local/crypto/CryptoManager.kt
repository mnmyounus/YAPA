package com.mnmyounus.yala.data.local.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * All credential material (PIN digits, password chars, pattern node sequence,
 * or image-sequence indices) is normalized to a String and salted+hashed here
 * with PBKDF2WithHmacSHA256 before it ever touches disk. The plaintext is
 * discarded immediately after hashing — it is never persisted, logged, or
 * transmitted (there is no network stack in this app at all).
 */
@Singleton
class CryptoManager @Inject constructor() {

    companion object {
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH_BITS = 256
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val RECOVERY_KEY_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no O/0/I/1 ambiguity
    }

    fun generateSalt(): String {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hash(plainText: String, saltB64: String): String {
        val salt = Base64.decode(saltB64, Base64.NO_WRAP)
        val spec = PBEKeySpec(plainText.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hashBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    /** Constant-time-ish comparison via re-hash; avoids storing/returning plaintext anywhere. */
    fun verify(plainText: String, saltB64: String, expectedHashB64: String): Boolean {
        val candidate = hash(plainText, saltB64)
        return constantTimeEquals(candidate, expectedHashB64)
    }

    /** Generates a random 12-character alphanumeric recovery key for the user to save. */
    fun generateRecoveryKey(): String {
        val random = SecureRandom()
        return (1..12)
            .map { RECOVERY_KEY_ALPHABET[random.nextInt(RECOVERY_KEY_ALPHABET.length)] }
            .joinToString("")
    }

    /** Normalizes a pattern (list of grid-node indices, e.g. [0,4,8,6]) into a hashable string. */
    fun serializePattern(nodeIndices: List<Int>): String = nodeIndices.joinToString(",")

    /** Normalizes an image-sequence choice (indices into the 5-image pool) into a hashable string. */
    fun serializeImageSequence(poolIndices: List<Int>): String = poolIndices.joinToString(",")

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].code xor b[i].code)
        return result == 0
    }
}
