package com.dreslan.countdown.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dreslan.countdown.data.Note
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NoteFeed(
    notes: List<Note>,
    timeZone: ZoneId,
    dotColor: Color,
    lineColor: Color,
    dateColor: Color,
    textColor: Color,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d").withZone(timeZone)

    Column(modifier = modifier) {
        notes.forEachIndexed { index, note ->
            val isLast = index == notes.lastIndex
            NoteRow(
                note = note,
                isLast = isLast,
                dateFormatter = dateFormatter,
                dotColor = dotColor,
                lineColor = lineColor,
                dateColor = dateColor,
                textColor = textColor,
                onClick = { onNoteClick(note) }
            )
        }
    }
}

@Composable
private fun NoteRow(
    note: Note,
    isLast: Boolean,
    dateFormatter: DateTimeFormatter,
    dotColor: Color,
    lineColor: Color,
    dateColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        // Left column: dot + vertical line
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight()
                .drawBehind {
                    val dotRadius = 5.dp.toPx()
                    val dotY = 10.dp.toPx()
                    val centerX = size.width / 2f

                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = Offset(centerX, dotY)
                    )

                    if (!isLast) {
                        val lineStartY = 16.dp.toPx()
                        drawLine(
                            color = lineColor,
                            start = Offset(centerX, lineStartY),
                            end = Offset(centerX, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                }
        )

        // Right column: date + text
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, bottom = 8.dp)
        ) {
            Text(
                text = dateFormatter.format(note.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = dateColor
            )
            Text(
                text = note.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}
