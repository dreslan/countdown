package com.dreslan.countdown

import java.time.Duration
import java.time.Instant

data class CountdownTime(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isComplete: Boolean
) {
    fun toDisplayString(): String {
        return "%02d:%02d:%02d:%02d".format(days, hours, minutes, seconds)
    }
}

fun calculateRemaining(target: Instant, now: Instant): CountdownTime {
    val duration = Duration.between(now, target)
    if (duration.isZero || duration.isNegative) {
        return CountdownTime(0, 0, 0, 0, isComplete = true)
    }
    val totalSeconds = duration.seconds
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return CountdownTime(days, hours, minutes, seconds, isComplete = false)
}
