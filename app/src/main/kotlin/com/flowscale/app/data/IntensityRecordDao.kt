package com.flowscale.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IntensityRecordDao {

    @Insert
    suspend fun insert(record: IntensityRecord)

    @Query("SELECT * FROM intensity_records ORDER BY recordedAt ASC")
    fun getAll(): Flow<List<IntensityRecord>>

    @Query("SELECT * FROM intensity_records ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatest(): IntensityRecord?

    @Query("SELECT COUNT(*) FROM intensity_records")
    fun getCount(): Flow<Long>
}
