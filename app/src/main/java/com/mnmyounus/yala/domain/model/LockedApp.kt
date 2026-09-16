package com.mnmyounus.yala.domain.model

/** A single installed app (user app or system app) as shown in the app picker. */
data class InstalledApp(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean,
    val isLocked: Boolean = false
)

/**
 * Persisted record of a locked app. When [scope] is GLOBAL, [credentialOverride]
 * is null and the app defers to the single global [LockCredential].
 */
data class LockedApp(
    val packageName: String,
    val label: String,
    val scope: LockScope,
    val credentialOverride: LockCredential?,
    val lockedAtEpochMillis: Long
)
