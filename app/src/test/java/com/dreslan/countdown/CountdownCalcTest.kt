package com.dreslan.countdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CountdownCalcTest {
    @Test
    fun calculates_days_hours_minutes_seconds() {
        val target = Instant.parse("2026-04-22T16:30:00Z")
        val now = Instant.parse("2026-04-12T12:00:00Z")
        val result = calculateRemaining(target, now)

        assertEquals(10L, result.days)
        assertEquals(4L, result.hours)
        assertEquals(30L, result.minutes)
        assertEquals(0L, result.seconds)
        assertFalse(result.isComplete)
    }

    @Test
    fun returns_zero_when_target_is_past() {
        val target = Instant.parse("2026-04-10T12:00:00Z")
        val now = Instant.parse("2026-04-12T12:00:00Z")
        val result = calculateRemaining(target, now)

        assertEquals(0L, result.days)
        assertEquals(0L, result.hours)
        assertEquals(0L, result.minutes)
        assertEquals(0L, result.seconds)
        assertTrue(result.isComplete)
    }

    @Test
    fun returns_zero_when_target_equals_now() {
        val target = Instant.parse("2026-04-12T12:00:00Z")
        val now = Instant.parse("2026-04-12T12:00:00Z")
        val result = calculateRemaining(target, now)
        assertTrue(result.isComplete)
    }

    @Test
    fun handles_seconds_correctly() {
        val target = Instant.parse("2026-04-12T12:05:30Z")
        val now = Instant.parse("2026-04-12T12:00:00Z")
        val result = calculateRemaining(target, now)

        assertEquals(0L, result.days)
        assertEquals(0L, result.hours)
        assertEquals(5L, result.minutes)
        assertEquals(30L, result.seconds)
        assertFalse(result.isComplete)
    }

    @Test
    fun formats_as_display_string() {
        val result = CountdownTime(days = 10, hours = 4, minutes = 5, seconds = 3, isComplete = false)
        assertEquals("10:04:05:03", result.toDisplayString())
    }

    @Test
    fun formats_zero_state() {
        val result = CountdownTime(days = 0, hours = 0, minutes = 0, seconds = 0, isComplete = true)
        assertEquals("00:00:00:00", result.toDisplayString())
    }
}
