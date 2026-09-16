package com.mnmyounus.yala.domain.usecase

import com.mnmyounus.yala.data.local.crypto.CryptoManager
import com.mnmyounus.yala.domain.model.LockCredential
import com.mnmyounus.yala.domain.model.LockScope
import com.mnmyounus.yala.domain.model.LockType
import com.mnmyounus.yala.domain.model.LockedApp
import com.mnmyounus.yala.domain.repository.AppRepository
import com.mnmyounus.yala.domain.repository.LockRepository
import javax.inject.Inject

/** Hashes and persists a new credential for a single app, or globally. */
class SetupLockUseCase @Inject constructor(
    private val lockRepository: LockRepository,
    private val appRepository: AppRepository,
    private val crypto: CryptoManager
) {
    suspend fun setGlobal(
        type: LockType,
        plainSecret: String,
        hint: String?,
        imagePoolUris: List<String> = emptyList(),
        secretSequenceLength: Int = 0
    ) {
        val credential = buildCredential(type, plainSecret, hint, imagePoolUris, secretSequenceLength)
        lockRepository.saveGlobalCredential(credential)
    }

    suspend fun setForApp(
        packageName: String,
        label: String,
        scope: LockScope,
        type: LockType,
        plainSecret: String,
        hint: String?,
        imagePoolUris: List<String> = emptyList(),
        secretSequenceLength: Int = 0
    ) {
        val credential = buildCredential(type, plainSecret, hint, imagePoolUris, secretSequenceLength)
        if (scope == LockScope.INDIVIDUAL) {
            lockRepository.saveAppCredential(packageName, credential)
        }
        appRepository.lockApp(
            LockedApp(
                packageName = packageName,
                label = label,
                scope = scope,
                credentialOverride = if (scope == LockScope.INDIVIDUAL) credential else null,
                lockedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    private fun buildCredential(
        type: LockType,
        plainSecret: String,
        hint: String?,
        imagePoolUris: List<String>,
        secretSequenceLength: Int
    ): LockCredential {
        val salt = crypto.generateSalt()
        val hash = crypto.hash(plainSecret, salt)
        return LockCredential(
            type = type,
            hintText = hint,
            credentialHash = hash,
            salt = salt,
            imagePoolUris = imagePoolUris,
            secretSequenceLength = secretSequenceLength
        )
    }
}

sealed class VerifyResult {
    data object Success : VerifyResult()
    data object Failure : VerifyResult()
    data object NoCredentialConfigured : VerifyResult()
}

/** Checks a user-entered PIN/password/pattern/image-sequence against the stored hash for an app. */
class VerifyLockUseCase @Inject constructor(
    private val lockRepository: LockRepository,
    private val crypto: CryptoManager
) {
    suspend operator fun invoke(packageName: String, plainAttempt: String): VerifyResult {
        val credential = lockRepository.getCredentialFor(packageName)
            ?: return VerifyResult.NoCredentialConfigured
        val matches = crypto.verify(plainAttempt, credential.salt, credential.credentialHash)
        return if (matches) VerifyResult.Success else VerifyResult.Failure
    }
}

/** Generates and persists (hashed) a fresh 12-character recovery key; returns the ONE plaintext copy for the user to save. */
class GenerateRecoveryKeyUseCase @Inject constructor(
    private val lockRepository: LockRepository,
    private val crypto: CryptoManager
) {
    suspend operator fun invoke(): String {
        val key = crypto.generateRecoveryKey()
        val salt = crypto.generateSalt()
        val hash = crypto.hash(key, salt)
        lockRepository.saveRecoveryKeyHash(hash, salt)
        return key
    }
}

/** Verifies a recovery key entered during an "I forgot my lock" flow. */
class VerifyRecoveryKeyUseCase @Inject constructor(
    private val lockRepository: LockRepository
) {
    suspend operator fun invoke(plainKey: String): Boolean = lockRepository.verifyRecoveryKey(plainKey)
}
