package com.flowscale.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "intensity_records")
data class IntensityRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val intensity: Double,
    val recordedAt: Long = Instant.now().toEpochMilli(),
)
