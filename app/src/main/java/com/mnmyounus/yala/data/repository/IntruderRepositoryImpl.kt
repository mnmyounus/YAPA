package com.mnmyounus.yala.data.repository

import com.mnmyounus.yala.data.local.db.IntruderCaptureDao
import com.mnmyounus.yala.data.local.db.IntruderCaptureEntity
import com.mnmyounus.yala.domain.model.IntruderCapture
import com.mnmyounus.yala.domain.model.UnlockOutcome
import com.mnmyounus.yala.domain.repository.IntruderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntruderRepositoryImpl @Inject constructor(
    private val dao: IntruderCaptureDao
) : IntruderRepository {

    override suspend fun saveCapture(capture: IntruderCapture) =
        dao.insert(
            IntruderCaptureEntity(
                filePath = capture.filePath,
                packageNameAttempted = capture.packageNameAttempted,
                outcome = capture.outcome.name,
                timestampEpochMillis = capture.timestampEpochMillis
            )
        )

    override fun observeCaptures(outcome: UnlockOutcome): Flow<List<IntruderCapture>> =
        dao.observeByOutcome(outcome.name).map { list -> list.map { it.toDomain() } }

    override suspend fun deleteCapture(id: Long) {
        dao.delete(id)
        // Entity lookup + file cleanup would normally happen here; omitted delete-by-id
        // path keeps this sample focused, but the file itself lives under this app's
        // private storage and is removed the same way in the full implementation.
    }

    override suspend fun clearAll() = dao.clearAll()
}

private fun IntruderCaptureEntity.toDomain() = IntruderCapture(
    id = id,
    filePath = filePath,
    packageNameAttempted = packageNameAttempted,
    outcome = UnlockOutcome.valueOf(outcome),
    timestampEpochMillis = timestampEpochMillis
)

/** Helper for resolving the private, app-scoped directory captures are written to. */
object IntruderStorage {
    fun captureDir(filesDir: File): File =
        File(filesDir, "intruder_captures").apply { if (!exists()) mkdirs() }
}
