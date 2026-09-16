package com.mnmyounus.yala.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.mnmyounus.yala.data.local.db.LockedAppDao
import com.mnmyounus.yala.data.local.db.LockedAppEntity
import com.mnmyounus.yala.domain.model.InstalledApp
import com.mnmyounus.yala.domain.model.LockScope
import com.mnmyounus.yala.domain.model.LockedApp
import com.mnmyounus.yala.domain.repository.AppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: LockedAppDao
) : AppRepository {

    private val pm: PackageManager get() = context.packageManager

    override suspend fun getInstalledApps(includeSystemApps: Boolean): List<InstalledApp> =
        withContext(Dispatchers.IO) {
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            apps
                .asSequence()
                .filter { it.packageName != context.packageName }
                .filter { includeSystemApps || (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .map {
                    InstalledApp(
                        packageName = it.packageName,
                        label = pm.getApplicationLabel(it).toString(),
                        isSystemApp = (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                }
                .sortedBy { it.label.lowercase() }
                .toList()
        }

    override fun observeLockedApps(): Flow<List<LockedApp>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun lockApp(app: LockedApp) = dao.upsert(app.toEntity())

    override suspend fun unlockApp(packageName: String) = dao.delete(packageName)

    override suspend fun isAppLocked(packageName: String): Boolean = dao.isLocked(packageName)
}

private fun LockedAppEntity.toDomain(): LockedApp = LockedApp(
    packageName = packageName,
    label = label,
    scope = LockScope.valueOf(scope),
    credentialOverride = null, // resolved separately via LockRepository.getCredentialFor()
    lockedAtEpochMillis = lockedAtEpochMillis
)

private fun LockedApp.toEntity(): LockedAppEntity = LockedAppEntity(
    packageName = packageName,
    label = label,
    scope = scope.name,
    credentialJson = credentialOverride?.let { JSONObject().put("type", it.type.id).toString() },
    lockedAtEpochMillis = lockedAtEpochMillis
)
