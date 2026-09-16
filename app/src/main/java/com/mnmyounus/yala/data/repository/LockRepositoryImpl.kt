package com.mnmyounus.yala.data.repository

import com.mnmyounus.yala.data.local.crypto.CryptoManager
import com.mnmyounus.yala.data.local.datastore.SecurePreferencesDataSource
import com.mnmyounus.yala.domain.model.LockCredential
import com.mnmyounus.yala.domain.repository.LockRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockRepositoryImpl @Inject constructor(
    private val prefs: SecurePreferencesDataSource,
    private val crypto: CryptoManager
) : LockRepository {

    override suspend fun saveGlobalCredential(credential: LockCredential) =
        prefs.saveGlobalCredential(credential)

    override suspend fun getGlobalCredential(): LockCredential? = prefs.getGlobalCredential()

    override suspend fun saveAppCredential(packageName: String, credential: LockCredential) =
        prefs.saveAppCredential(packageName, credential)

    override suspend fun getCredentialFor(packageName: String): LockCredential? =
        prefs.getAppCredential(packageName) ?: prefs.getGlobalCredential()

    override suspend fun saveRecoveryKeyHash(hash: String, salt: String) =
        prefs.saveRecoveryKey(hash, salt)

    override suspend fun verifyRecoveryKey(plainKey: String): Boolean {
        val hash = prefs.getRecoveryHash() ?: return false
        val salt = prefs.getRecoverySalt() ?: return false
        return crypto.verify(plainKey.uppercase(), salt, hash)
    }

    override fun isOnboarded(): Flow<Boolean> = prefs.observeOnboarded()

    override suspend fun setOnboarded(complete: Boolean) = prefs.setOnboarded(complete)
}
