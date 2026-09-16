package com.mnmyounus.yala.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ---------- Entities ----------

@Entity(tableName = "locked_apps")
data class LockedAppEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val scope: String,               // "INDIVIDUAL" | "GLOBAL"
    val credentialJson: String?,     // null when scope == GLOBAL
    val lockedAtEpochMillis: Long
)

@Entity(tableName = "intruder_captures")
data class IntruderCaptureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val packageNameAttempted: String,
    val outcome: String,             // "SUCCESS" | "FAILURE"
    val timestampEpochMillis: Long
)

// ---------- DAOs ----------

@Dao
interface LockedAppDao {
    @Query("SELECT * FROM locked_apps ORDER BY label ASC")
    fun observeAll(): Flow<List<LockedAppEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM locked_apps WHERE packageName = :pkg)")
    suspend fun isLocked(pkg: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LockedAppEntity)

    @Query("DELETE FROM locked_apps WHERE packageName = :pkg")
    suspend fun delete(pkg: String)
}

@Dao
interface IntruderCaptureDao {
    @Query("SELECT * FROM intruder_captures WHERE outcome = :outcome ORDER BY timestampEpochMillis DESC")
    fun observeByOutcome(outcome: String): Flow<List<IntruderCaptureEntity>>

    @Insert
    suspend fun insert(entity: IntruderCaptureEntity)

    @Query("DELETE FROM intruder_captures WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM intruder_captures")
    suspend fun clearAll()
}

// ---------- Database ----------

@Database(
    entities = [LockedAppEntity::class, IntruderCaptureEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lockedAppDao(): LockedAppDao
    abstract fun intruderCaptureDao(): IntruderCaptureDao
}
