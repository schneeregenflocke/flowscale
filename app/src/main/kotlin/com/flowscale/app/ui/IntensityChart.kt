package com.flowscale.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowscale.app.R
import com.flowscale.app.data.IntensityRecord

private const val Y_MIN = 0.0
private const val Y_MAX = 10.0
private const val POINT_RADIUS = 5f

@Composable
fun IntensityChart(
    records: List<IntensityRecord>,
    nowMillis: Long,
    windowMillis: Long,
    currentIntensity: Double,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val chartDescription = stringResource(R.string.chart_description)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .semantics {
                contentDescription = chartDescription
            },
    ) {
        val windowStart = nowMillis - windowMillis

        val paddingLeft = 36.dp.toPx()
        val paddingRight = 8.dp.toPx()
        val paddingTop = 8.dp.toPx()
        val paddingBottom = 4.dp.toPx()

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        fun xFor(timeMillis: Long): Float {
            val fraction = ((timeMillis - windowStart).toFloat() / windowMillis).coerceIn(0f, 1f)
            return paddingLeft + fraction * chartWidth
        }

        fun yFor(intensity: Double): Float {
            val fraction = ((intensity - Y_MIN) / (Y_MAX - Y_MIN)).coerceIn(0.0, 1.0)
            return paddingTop + chartHeight - (fraction * chartHeight).toFloat()
        }

        drawGridLines(
            gridColor = gridColor,
            labelColor = labelColor,
            textMeasurer = textMeasurer,
            paddingLeft = paddingLeft,
            paddingTop = paddingTop,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            ::yFor,
        )

        val visible = records.filter { it.recordedAt >= windowStart }
        val preWindowRecord = records.lastOrNull { it.recordedAt < windowStart }

        // Live point at the right edge (now) for current intensity
        val livePoint = Offset(xFor(nowMillis), yFor(currentIntensity))

        // Compute left-edge interpolated point
        val leftEdgePoint: Offset? = if (preWindowRecord != null) {
            if (visible.isNotEmpty()) {
                val first = visible.first()
                val timeDelta = first.recordedAt - preWindowRecord.recordedAt
                if (timeDelta > 0) {
                    val fraction = (windowStart - preWindowRecord.recordedAt).toDouble() / timeDelta
                    val interpolated = preWindowRecord.intensity + fraction * (first.intensity - preWindowRecord.intensity)
                    Offset(xFor(windowStart), yFor(interpolated))
                } else {
                    Offset(xFor(windowStart), yFor(preWindowRecord.intensity))
                }
            } else {
                val timeDelta = nowMillis - preWindowRecord.recordedAt
                if (timeDelta > 0) {
                    val fraction = (windowStart - preWindowRecord.recordedAt).toDouble() / timeDelta
                    val interpolated = preWindowRecord.intensity + fraction * (currentIntensity - preWindowRecord.intensity)
                    Offset(xFor(windowStart), yFor(interpolated))
                } else {
                    Offset(xFor(windowStart), yFor(preWindowRecord.intensity))
                }
            }
        } else {
            null
        }

        if (visible.isEmpty() && leftEdgePoint == null) {
            // No recorded points at all — just draw the live point
            drawCircle(
                color = pointColor,
                radius = POINT_RADIUS,
                center = livePoint,
            )
            return@Canvas
        }

        // Build complete list of points for drawing
        val allPoints = buildList {
            leftEdgePoint?.let { add(it) }
            addAll(visible.map { Offset(xFor(it.recordedAt), yFor(it.intensity)) })
        }

        // Draw lines between all points (including left edge → first visible)
        for (i in 0 until allPoints.size - 1) {
            drawLine(
                color = lineColor,
                start = allPoints[i],
                end = allPoints[i + 1],
                strokeWidth = 3f,
            )
        }

        // Line from last point to live point
        if (allPoints.isNotEmpty()) {
            drawLine(
                color = lineColor,
                start = allPoints.last(),
                end = livePoint,
                strokeWidth = 3f,
            )
        }

        // Draw all points
        for (point in allPoints) {
            drawCircle(
                color = pointColor,
                radius = POINT_RADIUS,
                center = point,
            )
        }

        // Draw live point
        drawCircle(
            color = pointColor,
            radius = POINT_RADIUS,
            center = livePoint,
        )
    }
}

private fun DrawScope.drawGridLines(
    gridColor: Color,
    labelColor: Color,
    textMeasurer: TextMeasurer,
    paddingLeft: Float,
    paddingTop: Float,
    chartWidth: Float,
    chartHeight: Float,
    yFor: (Double) -> Float,
) {
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
    val labelStyle = TextStyle(color = labelColor, fontSize = 10.sp)

    for (tick in 0..10) {
        val y = yFor(tick.toDouble())
        drawLine(
            color = gridColor,
            start = Offset(paddingLeft, y),
            end = Offset(paddingLeft + chartWidth, y),
            strokeWidth = 1f,
            pathEffect = dashEffect,
        )
        val label = tick.toString()
        val measured = textMeasurer.measure(label, labelStyle)
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(paddingLeft - measured.size.width - 4f, y - measured.size.height / 2f),
        )
    }
}
