package com.flowscale.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SpinBox(
    value: Double,
    onValueChange: (Double) -> Unit,
    minValue: Double,
    maxValue: Double,
    step: Double,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier,
    ) {
        FilledTonalIconButton(
            onClick = { onValueChange((value - step).coerceAtLeast(minValue)) },
            enabled = value > minValue,
        ) {
            Text("−")
        }

        Text(
            text = formatRating(value),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp),
        )

        FilledTonalIconButton(
            onClick = { onValueChange((value + step).coerceAtMost(maxValue)) },
            enabled = value < maxValue,
        ) {
            Text("+")
        }
    }
}

fun formatRating(value: Double): String =
    if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toBigDecimal().stripTrailingZeros().toPlainString()
    }
