package com.dreslan.countdown.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CountdownDao {
    @Query("SELECT * FROM countdowns ORDER BY targetDateTime ASC")
    fun getAll(): Flow<List<Countdown>>

    @Query("SELECT * FROM countdowns ORDER BY targetDateTime ASC")
    suspend fun getAllOnce(): List<Countdown>

    @Query("SELECT * FROM countdowns WHERE id = :id")
    suspend fun getById(id: Long): Countdown?

    @Query("SELECT * FROM countdowns WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<Countdown?>

    @Insert
    suspend fun insert(countdown: Countdown): Long

    @Update
    suspend fun update(countdown: Countdown)

    @Delete
    suspend fun delete(countdown: Countdown)

    @Query("DELETE FROM countdowns WHERE id = :id")
    suspend fun deleteById(id: Long)
}
