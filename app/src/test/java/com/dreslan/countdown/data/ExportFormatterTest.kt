package com.dreslan.countdown.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ExportFormatterTest {
    private val countdown = Countdown(
        id = 1,
        title = "FREEDOM!",
        targetDateTime = Instant.parse("2026-04-22T16:30:00Z"),
        timeZone = "America/New_York",
        theme = CountdownTheme.MEDIEVAL,
        startDate = Instant.parse("2026-03-30T17:30:00Z"),
        createdAt = Instant.parse("2026-03-30T17:30:00Z"),
        showProgress = true
    )

    private val notes = listOf(
        Note(id = 1, countdownId = 1, text = "Day 1 post-op", createdAt = Instant.parse("2026-03-31T12:00:00Z")),
        Note(id = 2, countdownId = 1, text = "Feeling better", createdAt = Instant.parse("2026-04-04T15:00:00Z"))
    )

    @Test
    fun markdownContainsTitleAsHeading() {
        val md = ExportFormatter.toMarkdown(countdown, notes)
        assertTrue(md.startsWith("# FREEDOM!"))
    }

    @Test
    fun markdownContainsTargetDate() {
        val md = ExportFormatter.toMarkdown(countdown, notes)
        assertTrue(md.contains("**Target:**"))
        assertTrue(md.contains("April 22, 2026"))
    }

    @Test
    fun markdownContainsStartDate() {
        val md = ExportFormatter.toMarkdown(countdown, notes)
        assertTrue(md.contains("**Started:**"))
        assertTrue(md.contains("March 30, 2026"))
    }

    @Test
    fun markdownContainsNotes() {
        val md = ExportFormatter.toMarkdown(countdown, notes)
        assertTrue(md.contains("Day 1 post-op"))
        assertTrue(md.contains("Feeling better"))
    }

    @Test
    fun markdownHandlesEmptyNotes() {
        val md = ExportFormatter.toMarkdown(countdown, emptyList())
        assertTrue(!md.contains("## Notes"))
    }

    @Test
    fun jsonContainsTitle() {
        val json = ExportFormatter.toJson(countdown, notes)
        assertTrue(json.contains("\"title\": \"FREEDOM!\""))
    }

    @Test
    fun jsonContainsNotes() {
        val json = ExportFormatter.toJson(countdown, notes)
        assertTrue(json.contains("\"text\": \"Day 1 post-op\""))
        assertTrue(json.contains("\"text\": \"Feeling better\""))
    }

    @Test
    fun jsonHandlesEmptyNotes() {
        val json = ExportFormatter.toJson(countdown, emptyList())
        assertTrue(json.contains("\"notes\": []"))
    }
}
