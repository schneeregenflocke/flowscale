package com.flowscale.app

import com.flowscale.app.data.IntensityRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun recentSelectionKeepsPreviousPointForChartContinuity() {
        val records = listOf(
            IntensityRecord(id = 1, intensity = 1.0, recordedAt = 1_000),
            IntensityRecord(id = 2, intensity = 2.0, recordedAt = 2_000),
            IntensityRecord(id = 3, intensity = 3.0, recordedAt = 3_000),
        )

        val result = selectRecentRecords(records, windowStartMillis = 2_500)

        assertEquals(listOf(records[1], records[2]), result)
    }

    @Test
    fun recentSelectionFallsBackToLatestPointWhenWindowIsAfterAllRecords() {
        val records = listOf(
            IntensityRecord(id = 1, intensity = 1.0, recordedAt = 1_000),
            IntensityRecord(id = 2, intensity = 2.0, recordedAt = 2_000),
        )

        val result = selectRecentRecords(records, windowStartMillis = 3_000)

        assertEquals(listOf(records.last()), result)
    }
}
