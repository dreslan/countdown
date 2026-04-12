package com.dreslan.countdown.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class CountdownDaoTest {
    private lateinit var database: CountdownDatabase
    private lateinit var dao: CountdownDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            CountdownDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.countdownDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveCountdown() = runTest {
        val countdown = Countdown(
            title = "Test",
            targetDateTime = Instant.parse("2026-04-22T16:30:00Z"),
            timeZone = "America/New_York"
        )
        val id = dao.insert(countdown)
        val retrieved = dao.getById(id)

        assertNotNull(retrieved)
        assertEquals("Test", retrieved!!.title)
        assertEquals("America/New_York", retrieved.timeZone)
        assertEquals(CountdownTheme.CLEAN, retrieved.theme)
    }

    @Test
    fun getAllReturnsOrderedByTargetDate() = runTest {
        val later = Countdown(
            title = "Later",
            targetDateTime = Instant.parse("2027-01-01T00:00:00Z"),
            timeZone = "UTC"
        )
        val earlier = Countdown(
            title = "Earlier",
            targetDateTime = Instant.parse("2026-06-01T00:00:00Z"),
            timeZone = "UTC"
        )
        dao.insert(later)
        dao.insert(earlier)

        val all = dao.getAll().first()
        assertEquals(2, all.size)
        assertEquals("Earlier", all[0].title)
        assertEquals("Later", all[1].title)
    }

    @Test
    fun updateCountdown() = runTest {
        val countdown = Countdown(
            title = "Original",
            targetDateTime = Instant.parse("2026-04-22T16:30:00Z"),
            timeZone = "America/New_York"
        )
        val id = dao.insert(countdown)
        val saved = dao.getById(id)!!
        dao.update(saved.copy(title = "Updated", theme = CountdownTheme.MEDIEVAL))

        val updated = dao.getById(id)!!
        assertEquals("Updated", updated.title)
        assertEquals(CountdownTheme.MEDIEVAL, updated.theme)
    }

    @Test
    fun deleteCountdown() = runTest {
        val countdown = Countdown(
            title = "ToDelete",
            targetDateTime = Instant.parse("2026-04-22T16:30:00Z"),
            timeZone = "America/New_York"
        )
        val id = dao.insert(countdown)
        dao.deleteById(id)
        assertNull(dao.getById(id))
    }

    @Test
    fun optionalFieldsStoredCorrectly() = runTest {
        val countdown = Countdown(
            title = "WithExtras",
            targetDateTime = Instant.parse("2026-04-22T16:30:00Z"),
            timeZone = "America/New_York",
            theme = CountdownTheme.MEDIEVAL,
            zeroMessage = "FREEDOM!",
            videoUrl = "https://www.youtube.com/embed/lLCEUpIg8rE"
        )
        val id = dao.insert(countdown)
        val retrieved = dao.getById(id)!!

        assertEquals("FREEDOM!", retrieved.zeroMessage)
        assertEquals("https://www.youtube.com/embed/lLCEUpIg8rE", retrieved.videoUrl)
        assertEquals(CountdownTheme.MEDIEVAL, retrieved.theme)
    }
}
