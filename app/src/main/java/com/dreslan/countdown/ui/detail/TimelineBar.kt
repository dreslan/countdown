package com.dreslan.countdown.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Instant

@Composable
fun TimelineBar(
    startDate: Instant,
    targetDate: Instant,
    noteTimestamps: List<Instant>,
    progressColor: Color,
    trackColor: Color,
    dotColor: Color,
    labelColor: Color,
    startLabel: String,
    targetLabel: String,
    modifier: Modifier = Modifier
) {
    val now = Instant.now()
    val totalDuration = targetDate.toEpochMilli() - startDate.toEpochMilli()
    val elapsed = now.toEpochMilli() - startDate.toEpochMilli()
    val progress = if (totalDuration > 0) (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 1f
    val percentText = "${(progress * 100).toInt()}%"

    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = startLabel,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = percentText,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
            Text(
                text = targetLabel,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(vertical = 5.dp)
        ) {
            val barHeight = 6.dp.toPx()
            val barY = (size.height - barHeight) / 2
            val cornerRadius = barHeight / 2

            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, barY),
                size = Size(size.width, barHeight),
                cornerRadius = CornerRadius(cornerRadius)
            )

            val filledWidth = size.width * progress
            if (filledWidth > 0f) {
                drawRoundRect(
                    color = progressColor,
                    topLeft = Offset(0f, barY),
                    size = Size(filledWidth, barHeight),
                    cornerRadius = CornerRadius(cornerRadius)
                )
            }

            val dotRadius = 6.dp.toPx()
            val bgRadius = dotRadius + 2.dp.toPx()
            val centerY = size.height / 2
            for (timestamp in noteTimestamps) {
                val noteElapsed = timestamp.toEpochMilli() - startDate.toEpochMilli()
                val noteProgress = if (totalDuration > 0) (noteElapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f
                val x = size.width * noteProgress
                drawCircle(
                    color = Color(0xFF1A1A1A),
                    radius = bgRadius,
                    center = Offset(x, centerY)
                )
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, centerY)
                )
            }
        }
    }
}
