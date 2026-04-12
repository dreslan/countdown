package com.dreslan.countdown.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = Countdown::class,
        parentColumns = ["id"],
        childColumns = ["countdownId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("countdownId")]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val countdownId: Long,
    val text: String,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
