package com.dreslan.countdown.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE countdownId = :countdownId ORDER BY createdAt DESC")
    fun getNotesForCountdown(countdownId: Long): Flow<List<Note>>

    @Query("SELECT COUNT(*) FROM notes WHERE countdownId = :countdownId")
    fun getNoteCountForCountdown(countdownId: Long): Flow<Int>

    @Insert
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
