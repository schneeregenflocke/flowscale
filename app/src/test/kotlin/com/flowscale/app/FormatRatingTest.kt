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
    fun quarterStepsDisplayCorrectly() {
        assertEquals("0.25", formatRating(0.25))
        assertEquals("0.5", formatRating(0.5))
        assertEquals("0.75", formatRating(0.75))
        assertEquals("1.25", formatRating(1.25))
        assertEquals("9.75", formatRating(9.75))
    }

    @Test
    fun trailingZerosAreStripped() {
        assertEquals("2.5", formatRating(2.50))
        assertEquals("3.25", formatRating(3.250))
    }
}
