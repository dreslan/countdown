package com.dreslan.countdown.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant

class InstantConverter {
    @TypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilli()

    @TypeConverter
    fun toInstant(epochMilli: Long): Instant = Instant.ofEpochMilli(epochMilli)
}

class ThemeConverter {
    @TypeConverter
    fun fromTheme(theme: CountdownTheme): String = theme.name

    @TypeConverter
    fun toTheme(name: String): CountdownTheme = CountdownTheme.valueOf(name)
}

@Database(entities = [Countdown::class], version = 1)
@TypeConverters(InstantConverter::class, ThemeConverter::class)
abstract class CountdownDatabase : RoomDatabase() {
    abstract fun countdownDao(): CountdownDao

    companion object {
        @Volatile
        private var INSTANCE: CountdownDatabase? = null

        fun getInstance(context: Context): CountdownDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CountdownDatabase::class.java,
                    "countdown_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
