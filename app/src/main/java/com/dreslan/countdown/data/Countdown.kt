package com.dreslan.countdown.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

enum class CountdownTheme {
    CLEAN,
    MEDIEVAL
}

@Entity(tableName = "countdowns")
data class Countdown(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateTime: Instant,
    val timeZone: String,
    val theme: CountdownTheme = CountdownTheme.CLEAN,
    val zeroMessage: String? = null,
    val videoUrl: String? = null,
    val createdAt: Instant = Instant.now()
)
