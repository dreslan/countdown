package com.dreslan.countdown.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Database(entities = [Countdown::class, Note::class], version = 5, exportSchema = false)
@TypeConverters(InstantConverter::class, ThemeConverter::class)
abstract class CountdownDatabase : RoomDatabase() {
    abstract fun countdownDao(): CountdownDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: CountdownDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE countdowns ADD COLUMN showProgress INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE countdowns ADD COLUMN backgroundImagePath TEXT DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE countdowns ADD COLUMN startDate INTEGER DEFAULT NULL")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE notes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        countdownId INTEGER NOT NULL,
                        text TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY (countdownId) REFERENCES countdowns(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX index_notes_countdownId ON notes(countdownId)")
            }
        }

        fun getInstance(context: Context): CountdownDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CountdownDatabase::class.java,
                    "countdown_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { INSTANCE = it }
            }
        }
    }
}
