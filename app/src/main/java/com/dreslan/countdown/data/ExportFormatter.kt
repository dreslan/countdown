package com.dreslan.countdown.data

import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ExportFormatter {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a")
    private val noteDateFormatter = DateTimeFormatter.ofPattern("MMM d")

    private fun String.escapeJson(): String =
        replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")

    fun toMarkdown(countdown: Countdown, notes: List<Note>): String {
        val zone = ZoneId.of(countdown.timeZone)
        val targetFormatted = countdown.targetDateTime.atZone(zone).format(dateTimeFormatter)
        val origin = countdown.startDate ?: countdown.createdAt
        val startFormatted = origin.atZone(zone).format(dateTimeFormatter)

        val now = java.time.Instant.now()
        val totalDuration = countdown.targetDateTime.toEpochMilli() - origin.toEpochMilli()
        val elapsed = now.toEpochMilli() - origin.toEpochMilli()
        val progress = if (totalDuration > 0) ((elapsed.toFloat() / totalDuration.toFloat()) * 100).toInt().coerceIn(0, 100) else 100

        return buildString {
            appendLine("# ${countdown.title}")
            appendLine()
            appendLine("**Target:** $targetFormatted")
            appendLine("**Started:** $startFormatted")
            appendLine("**Progress:** $progress%")

            if (notes.isNotEmpty()) {
                appendLine()
                appendLine("## Notes")
                appendLine()
                for (note in notes) {
                    val dateStr = note.createdAt.atZone(zone).format(noteDateFormatter)
                    appendLine("- **$dateStr:** ${note.text}")
                }
            }
        }.trimEnd()
    }

    fun toJson(countdown: Countdown, notes: List<Note>): String {
        val zone = ZoneId.of(countdown.timeZone)
        val origin = countdown.startDate ?: countdown.createdAt

        return buildString {
            appendLine("{")
            appendLine("  \"title\": \"${countdown.title.escapeJson()}\",")
            appendLine("  \"targetDateTime\": \"${countdown.targetDateTime.atZone(zone)}\",")
            appendLine("  \"startDate\": \"${origin.atZone(zone)}\",")
            appendLine("  \"createdAt\": \"${countdown.createdAt.atZone(zone)}\",")
            appendLine("  \"theme\": \"${countdown.theme.name}\",")
            if (notes.isEmpty()) {
                appendLine("  \"notes\": []")
            } else {
                appendLine("  \"notes\": [")
                notes.forEachIndexed { index, note ->
                    val comma = if (index < notes.lastIndex) "," else ""
                    appendLine("    {")
                    appendLine("      \"text\": \"${note.text.escapeJson()}\",")
                    appendLine("      \"createdAt\": \"${note.createdAt.atZone(zone)}\"")
                    appendLine("    }$comma")
                }
                appendLine("  ]")
            }
            appendLine("}")
        }.trimEnd()
    }
}
