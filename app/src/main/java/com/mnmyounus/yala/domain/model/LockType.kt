package com.mnmyounus.yala.domain.model

/**
 * The four custom lock mechanisms YALA supports. Deliberately NOT tied to
 * BiometricPrompt or KeyguardManager — everything here is app-owned.
 */
sealed class LockType(val id: Int) {
    data object Pin : LockType(0)
    data object Password : LockType(1)
    data object Pattern : LockType(2)
    data object ImageSequence : LockType(3)

    companion object {
        fun fromId(id: Int): LockType = when (id) {
            0 -> Pin
            1 -> Password
            2 -> Pattern
            3 -> ImageSequence
            else -> error("Unknown LockType id=$id")
        }
    }
}

/** How a lock is scoped across the apps the user has selected. */
enum class LockScope { INDIVIDUAL, GLOBAL }

/**
 * A fully-configured credential, already hashed by [CryptoManager] — the
 * plaintext PIN/password/pattern/image-order is never stored.
 */
data class LockCredential(
    val type: LockType,
    val hintText: String?,
    val credentialHash: String,   // Base64 PBKDF2 hash
    val salt: String,             // Base64 salt, unique per credential
    /** For [LockType.ImageSequence]: local content-uri strings of the 5 chosen images, in setup order. */
    val imagePoolUris: List<String> = emptyList(),
    /** For [LockType.ImageSequence]: indices into [imagePoolUris] representing the secret sequence (3 or 4 long). */
    val secretSequenceLength: Int = 0
) {
    companion object
}
