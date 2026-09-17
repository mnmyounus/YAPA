package com.mnmyounus.yala.domain.repository

import com.mnmyounus.yala.domain.model.*
import kotlinx.coroutines.flow.Flow

interface LockRepository {
    suspend fun saveGlobalCredential(credential: LockCredential)
    suspend fun getGlobalCredential(): LockCredential?
    suspend fun saveAppCredential(packageName: String, credential: LockCredential)
    suspend fun getCredentialFor(packageName: String): LockCredential?
    suspend fun saveRecoveryKeyHash(hash: String, salt: String)
    suspend fun verifyRecoveryKey(plainKey: String): Boolean
    fun isOnboarded(): Flow<Boolean>
    suspend fun setOnboarded(complete: Boolean)
    fun observeThemeMode(): Flow<Int>
    suspend fun setThemeMode(ordinal: Int)
}

interface AppRepository {
    suspend fun getInstalledApps(includeSystemApps: Boolean): List<InstalledApp>
    fun observeLockedApps(): Flow<List<LockedApp>>
    suspend fun lockApp(app: LockedApp)
    suspend fun unlockApp(packageName: String)
    suspend fun isAppLocked(packageName: String): Boolean
}

interface IntruderRepository {
    suspend fun saveCapture(capture: IntruderCapture)
    fun observeCaptures(outcome: UnlockOutcome): Flow<List<IntruderCapture>>
    suspend fun deleteCapture(id: Long)
    suspend fun clearAll()
}
