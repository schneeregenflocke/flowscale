package com.flowscale.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IntensityRecordDao {

    @Insert
    suspend fun insert(record: IntensityRecord)

    @Query("SELECT * FROM intensity_records ORDER BY recordedAt DESC")
    fun getAll(): Flow<List<IntensityRecord>>

    @Query(
        "SELECT * FROM intensity_records " +
            "WHERE recordedAt >= :sinceMillis " +
            "OR id = (SELECT id FROM intensity_records WHERE recordedAt < :sinceMillis ORDER BY recordedAt DESC LIMIT 1) " +
            "ORDER BY recordedAt ASC",
    )
    fun getSince(sinceMillis: Long): Flow<List<IntensityRecord>>

    @Query("SELECT * FROM intensity_records ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatest(): IntensityRecord?
}
