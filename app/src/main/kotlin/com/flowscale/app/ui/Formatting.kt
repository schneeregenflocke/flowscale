package com.flowscale.app.ui

import java.math.BigDecimal

fun formatRating(value: Double): String =
    BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
