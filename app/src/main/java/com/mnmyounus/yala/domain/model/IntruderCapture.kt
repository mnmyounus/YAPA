package com.mnmyounus.yala.domain.model

enum class UnlockOutcome { SUCCESS, FAILURE }

/**
 * A single front-camera capture taken at an unlock attempt. Stored only in
 * this app's private storage (`filesDir/intruder_captures/`) — never uploaded,
 * never shared, never synced. Visible only inside YALA's own gallery screens.
 */
data class IntruderCapture(
    val id: Long = 0,
    val filePath: String,
    val packageNameAttempted: String,
    val outcome: UnlockOutcome,
    val timestampEpochMillis: Long
)
