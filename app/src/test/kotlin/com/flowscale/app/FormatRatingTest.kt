package com.flowscale.app

import com.flowscale.app.ui.formatRating
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatRatingTest {

    @Test
    fun wholeNumbersDisplayWithoutDecimal() {
        assertEquals("0", formatRating(0.0))
        assertEquals("1", formatRating(1.0))
        assertEquals("5", formatRating(5.0))
        assertEquals("10", formatRating(10.0))
    }

    @Test
    fun tenthStepsDisplayCorrectly() {
        assertEquals("0.1", formatRating(0.1))
        assertEquals("0.2", formatRating(0.2))
        assertEquals("0.3", formatRating(0.3))
        assertEquals("1.1", formatRating(1.1))
        assertEquals("9.9", formatRating(9.9))
    }

    @Test
    fun trailingZerosAreStripped() {
        assertEquals("2.5", formatRating(2.50))
        assertEquals("3.25", formatRating(3.250))
    }
}
