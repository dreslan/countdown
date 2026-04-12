# Braveheart Timer Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a multi-countdown Android app with themed homescreen widgets (4x1/4x2), optional zero-state messages, and optional YouTube video embeds.

**Architecture:** Kotlin + Jetpack Compose for UI, Glance for widgets, Room for persistence. Single-activity architecture with Compose Navigation. Widgets update per-minute via ACTION_TIME_TICK registered in Application class, with a 30-minute fallback via updatePeriodMillis.

**Tech Stack:** Kotlin 2.1.0, AGP 8.7.3, Compose BOM 2024.12.01, Room 2.6.1 (KSP), Glance 1.1.1, Navigation Compose 2.8.5, Min SDK 26, Target SDK 35.

---

## File Map

```
countdown/
├── settings.gradle.kts
├── build.gradle.kts                          # Root build file
├── gradle.properties
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts                      # App module build file
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/com/dreslan/countdown/
│       │       ├── CountdownApp.kt                         # Application class, tick registration
│       │       ├── CountdownCalc.kt                        # CountdownTime data class + calculateRemaining()
│       │       ├── MainActivity.kt                         # Single activity, hosts NavHost
│       │       ├── data/
│       │       │   ├── Countdown.kt                        # Room entity + Theme enum
│       │       │   ├── CountdownDao.kt                     # DAO interface
│       │       │   ├── CountdownDatabase.kt                # Room database + TypeConverters
│       │       │   └── YoutubeUrl.kt                       # URL normalization utility
│       │       ├── ui/
│       │       │   ├── theme/
│       │       │   │   ├── Color.kt                        # Color constants for both themes
│       │       │   │   ├── Type.kt                         # Typography definitions
│       │       │   │   └── Theme.kt                        # Compose theme wiring
│       │       │   ├── components/
│       │       │   │   └── CountdownDisplay.kt             # Shared countdown renderer
│       │       │   ├── list/
│       │       │   │   ├── CountdownListScreen.kt
│       │       │   │   └── CountdownListViewModel.kt
│       │       │   ├── edit/
│       │       │   │   ├── EditCountdownScreen.kt
│       │       │   │   └── EditCountdownViewModel.kt
│       │       │   └── detail/
│       │       │       ├── CountdownDetailScreen.kt
│       │       │       └── CountdownDetailViewModel.kt
│       │       └── widget/
│       │           ├── CountdownWidget.kt                  # 4x2 Glance widget
│       │           ├── CountdownWidgetReceiver.kt          # 4x2 receiver
│       │           ├── CountdownWidgetSmall.kt             # 4x1 Glance widget
│       │           ├── CountdownWidgetSmallReceiver.kt     # 4x1 receiver
│       │           ├── CountdownWidgetConfigActivity.kt    # Config picker Activity
│       │           ├── WidgetPreferences.kt                # Widget-to-countdown binding storage
│       │           └── WidgetTickReceiver.kt               # Boot receiver for tick re-registration
│       │   └── res/
│       │       ├── xml/
│       │       │   ├── countdown_widget_info.xml           # 4x2 widget metadata
│       │       │   └── countdown_widget_small_info.xml     # 4x1 widget metadata
│       │       └── values/
│       │           └── strings.xml
│       └── test/
│           └── java/com/dreslan/countdown/
│               ├── data/
│               │   └── YoutubeUrlTest.kt
│               └── CountdownCalcTest.kt
│       └── androidTest/
│           └── java/com/dreslan/countdown/
│               └── data/
│                   └── CountdownDaoTest.kt
```

---

### Task 1: Project Scaffolding

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Create Gradle wrapper properties**

```properties
# gradle/wrapper/gradle-wrapper.properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Step 2: Create root settings.gradle.kts**

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "countdown"
include(":app")
```

- [ ] **Step 3: Create root build.gradle.kts**

```kotlin
// build.gradle.kts
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
}
```

- [ ] **Step 4: Create gradle.properties**

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 5: Create app/build.gradle.kts**

```kotlin
// app/build.gradle.kts
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.dreslan.countdown"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dreslan.countdown"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Glance (widgets)
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    // WebView (for YouTube embed)
    implementation("androidx.webkit:webkit:1.12.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

- [ ] **Step 6: Create AndroidManifest.xml (minimal, will be expanded later)**

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- app/src/main/AndroidManifest.xml -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:name=".CountdownApp"
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@android:style/Theme.Material.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 7: Create strings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<!-- app/src/main/res/values/strings.xml -->
<resources>
    <string name="app_name">Braveheart Timer</string>
    <string name="widget_description_large">4x2 countdown widget with play button</string>
    <string name="widget_description_small">4x1 compact countdown widget</string>
</resources>
```

- [ ] **Step 8: Create placeholder Application class and MainActivity**

`app/src/main/java/com/dreslan/countdown/CountdownApp.kt`:
```kotlin
package com.dreslan.countdown

import android.app.Application

class CountdownApp : Application()
```

`app/src/main/java/com/dreslan/countdown/MainActivity.kt`:
```kotlin
package com.dreslan.countdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Navigation will be wired in Task 8
        }
    }
}
```

- [ ] **Step 9: Generate Gradle wrapper and verify build**

Run: `gradle wrapper --gradle-version 8.9` (if `gradle` CLI is available), or download wrapper JAR manually.

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "feat: scaffold Android project with dependencies"
```

---

### Task 2: Data Layer

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/data/Countdown.kt`
- Create: `app/src/main/java/com/dreslan/countdown/data/CountdownDao.kt`
- Create: `app/src/main/java/com/dreslan/countdown/data/CountdownDatabase.kt`
- Create: `app/src/androidTest/java/com/dreslan/countdown/data/CountdownDaoTest.kt`

- [ ] **Step 1: Write the Room entity and Theme enum**

`app/src/main/java/com/dreslan/countdown/data/Countdown.kt`:
```kotlin
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
```

- [ ] **Step 2: Write the DAO**

`app/src/main/java/com/dreslan/countdown/data/CountdownDao.kt`:
```kotlin
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

    @Insert
    suspend fun insert(countdown: Countdown): Long

    @Update
    suspend fun update(countdown: Countdown)

    @Delete
    suspend fun delete(countdown: Countdown)

    @Query("DELETE FROM countdowns WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

- [ ] **Step 3: Write the database with TypeConverters**

`app/src/main/java/com/dreslan/countdown/data/CountdownDatabase.kt`:
```kotlin
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
```

- [ ] **Step 4: Write DAO instrumented tests**

`app/src/androidTest/java/com/dreslan/countdown/data/CountdownDaoTest.kt`:
```kotlin
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
```

- [ ] **Step 5: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/data/ app/src/androidTest/
git commit -m "feat: add Room data layer with entity, DAO, database, and tests"
```

---

### Task 3: Utility Functions

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/data/YoutubeUrl.kt`
- Create: `app/src/main/java/com/dreslan/countdown/ui/components/CountdownDisplay.kt` (calc function only, UI later)
- Create: `app/src/test/java/com/dreslan/countdown/data/YoutubeUrlTest.kt`
- Create: `app/src/test/java/com/dreslan/countdown/CountdownCalcTest.kt`

- [ ] **Step 1: Write YouTube URL normalization tests**

`app/src/test/java/com/dreslan/countdown/data/YoutubeUrlTest.kt`:
```kotlin
package com.dreslan.countdown.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class YoutubeUrlTest {

    @Test
    fun normalizes_watch_url() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("https://www.youtube.com/watch?v=lLCEUpIg8rE")
        )
    }

    @Test
    fun normalizes_short_url() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("https://youtu.be/lLCEUpIg8rE")
        )
    }

    @Test
    fun normalizes_shorts_url() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("https://www.youtube.com/shorts/lLCEUpIg8rE")
        )
    }

    @Test
    fun passes_through_embed_url() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("https://www.youtube.com/embed/lLCEUpIg8rE")
        )
    }

    @Test
    fun handles_url_without_https() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("youtube.com/watch?v=lLCEUpIg8rE")
        )
    }

    @Test
    fun handles_watch_url_with_extra_params() {
        assertEquals(
            "https://www.youtube.com/embed/lLCEUpIg8rE",
            normalizeYoutubeUrl("https://www.youtube.com/watch?v=lLCEUpIg8rE&t=120")
        )
    }

    @Test
    fun returns_null_for_non_youtube_url() {
        assertNull(normalizeYoutubeUrl("https://vimeo.com/123456"))
    }

    @Test
    fun returns_null_for_garbage_input() {
        assertNull(normalizeYoutubeUrl("not a url"))
    }

    @Test
    fun returns_null_for_empty_string() {
        assertNull(normalizeYoutubeUrl(""))
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test`
Expected: FAIL — `normalizeYoutubeUrl` not found

- [ ] **Step 3: Implement YouTube URL normalization**

`app/src/main/java/com/dreslan/countdown/data/YoutubeUrl.kt`:
```kotlin
package com.dreslan.countdown.data

private val YOUTUBE_PATTERNS = listOf(
    Regex("""(?:https?://)?(?:www\.)?youtube\.com/watch\?v=([a-zA-Z0-9_-]+)"""),
    Regex("""(?:https?://)?youtu\.be/([a-zA-Z0-9_-]+)"""),
    Regex("""(?:https?://)?(?:www\.)?youtube\.com/shorts/([a-zA-Z0-9_-]+)"""),
    Regex("""(?:https?://)?(?:www\.)?youtube\.com/embed/([a-zA-Z0-9_-]+)"""),
)

fun normalizeYoutubeUrl(url: String): String? {
    for (pattern in YOUTUBE_PATTERNS) {
        val match = pattern.find(url)
        if (match != null) {
            return "https://www.youtube.com/embed/${match.groupValues[1]}"
        }
    }
    return null
}
```

- [ ] **Step 4: Run YouTube URL tests to verify they pass**

Run: `./gradlew test --tests "com.dreslan.countdown.data.YoutubeUrlTest"`
Expected: ALL PASS

- [ ] **Step 5: Write countdown calculation tests**

`app/src/test/java/com/dreslan/countdown/CountdownCalcTest.kt`:
```kotlin
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
```

- [ ] **Step 6: Run tests to verify they fail**

Run: `./gradlew test --tests "com.dreslan.countdown.CountdownCalcTest"`
Expected: FAIL — `calculateRemaining` and `CountdownTime` not found

- [ ] **Step 7: Implement countdown calculation**

`app/src/main/java/com/dreslan/countdown/CountdownCalc.kt`:
```kotlin
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
```

- [ ] **Step 8: Run all tests to verify they pass**

Run: `./gradlew test`
Expected: ALL PASS

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/data/YoutubeUrl.kt \
       app/src/main/java/com/dreslan/countdown/CountdownCalc.kt \
       app/src/test/
git commit -m "feat: add YouTube URL normalization and countdown calculation with tests"
```

---

### Task 4: Theme System

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/ui/theme/Color.kt`
- Create: `app/src/main/java/com/dreslan/countdown/ui/theme/Type.kt`
- Create: `app/src/main/java/com/dreslan/countdown/ui/theme/Theme.kt`

- [ ] **Step 1: Define color constants**

`app/src/main/java/com/dreslan/countdown/ui/theme/Color.kt`:
```kotlin
package com.dreslan.countdown.ui.theme

import androidx.compose.ui.graphics.Color

object CleanColors {
    val backgroundStart = Color(0xFF0D1117)
    val backgroundMid = Color(0xFF161B22)
    val backgroundEnd = Color(0xFF21262D)
    val countdownText = Color(0xFFF0F6FC)
    val labelText = Color(0xFF8B949E)
    val unitText = Color(0xFF484F58)
    val playButton = Color(0x99FFFFFF) // white at 60%
    val playButtonBg = Color(0x14FFFFFF) // white at 8%
}

object MedievalColors {
    val backgroundStart = Color(0xFF2D1B00)
    val backgroundMid = Color(0xFF4A2800)
    val backgroundEnd = Color(0xFF6B3A00)
    val countdownText = Color(0xFFF0E6D0)
    val labelText = Color(0xFFD4A855)
    val unitText = Color(0xFF8A6D3B)
    val playButton = Color(0xB3D4A855) // gold at 70%
    val playButtonBg = Color(0x26D4A855) // gold at 15%
    val playButtonBorder = Color(0x4DD4A855) // gold at 30%
}
```

- [ ] **Step 2: Define typography**

`app/src/main/java/com/dreslan/countdown/ui/theme/Type.kt`:
```kotlin
package com.dreslan.countdown.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val CleanTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 48.sp,
        letterSpacing = 3.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 2.sp
    )
)

val MedievalTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        letterSpacing = 3.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 2.sp
    )
)
```

- [ ] **Step 3: Wire up Compose theme**

`app/src/main/java/com/dreslan/countdown/ui/theme/Theme.kt`:
```kotlin
package com.dreslan.countdown.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.dreslan.countdown.data.CountdownTheme

private val CleanColorScheme = darkColorScheme(
    background = CleanColors.backgroundStart,
    surface = CleanColors.backgroundMid,
    onBackground = CleanColors.countdownText,
    onSurface = CleanColors.countdownText,
    primary = CleanColors.labelText,
    onPrimary = CleanColors.countdownText,
)

private val MedievalColorScheme = darkColorScheme(
    background = MedievalColors.backgroundStart,
    surface = MedievalColors.backgroundMid,
    onBackground = MedievalColors.countdownText,
    onSurface = MedievalColors.countdownText,
    primary = MedievalColors.labelText,
    onPrimary = MedievalColors.countdownText,
)

@Composable
fun CountdownAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CleanColorScheme,
        typography = CleanTypography,
        content = content
    )
}

@Composable
fun CountdownItemTheme(
    theme: CountdownTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        CountdownTheme.CLEAN -> CleanColorScheme
        CountdownTheme.MEDIEVAL -> MedievalColorScheme
    }
    val typography = when (theme) {
        CountdownTheme.CLEAN -> CleanTypography
        CountdownTheme.MEDIEVAL -> MedievalTypography
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
```

- [ ] **Step 4: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/ui/theme/
git commit -m "feat: add dual theme system with Clean and Medieval color schemes"
```

---

### Task 5: Countdown List Screen

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/ui/components/CountdownDisplay.kt`
- Create: `app/src/main/java/com/dreslan/countdown/ui/list/CountdownListViewModel.kt`
- Create: `app/src/main/java/com/dreslan/countdown/ui/list/CountdownListScreen.kt`

- [ ] **Step 1: Create the shared CountdownDisplay composable**

`app/src/main/java/com/dreslan/countdown/ui/components/CountdownDisplay.kt`:
```kotlin
package com.dreslan.countdown.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.dreslan.countdown.CountdownTime
import com.dreslan.countdown.calculateRemaining
import kotlinx.coroutines.delay
import java.time.Instant

@Composable
fun CountdownDisplay(
    targetDateTime: Instant,
    zeroMessage: String?,
    countdownStyle: TextStyle = MaterialTheme.typography.displayLarge,
    labelStyle: TextStyle = MaterialTheme.typography.labelSmall,
    modifier: Modifier = Modifier,
    onZeroCrossing: (() -> Unit)? = null
) {
    var time by remember { mutableStateOf(calculateRemaining(targetDateTime, Instant.now())) }
    var wasComplete by remember { mutableStateOf(time.isComplete) }

    LaunchedEffect(targetDateTime) {
        while (true) {
            time = calculateRemaining(targetDateTime, Instant.now())
            if (time.isComplete && !wasComplete) {
                wasComplete = true
                onZeroCrossing?.invoke()
            }
            delay(1000L)
        }
    }

    Column(modifier = modifier) {
        if (time.isComplete && !zeroMessage.isNullOrBlank()) {
            Text(
                text = zeroMessage,
                style = countdownStyle,
                color = MaterialTheme.colorScheme.onBackground
            )
        } else {
            Text(
                text = time.toDisplayString(),
                style = countdownStyle,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row {
                UnitLabel("DAYS", labelStyle)
                Spacer(Modifier.width(12.dp))
                UnitLabel("HRS", labelStyle)
                Spacer(Modifier.width(12.dp))
                UnitLabel("MIN", labelStyle)
                Spacer(Modifier.width(12.dp))
                UnitLabel("SEC", labelStyle)
            }
        }
    }
}

@Composable
private fun UnitLabel(text: String, style: TextStyle) {
    Text(
        text = text,
        style = style,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    )
}
```

- [ ] **Step 2: Create the ViewModel**

`app/src/main/java/com/dreslan/countdown/ui/list/CountdownListViewModel.kt`:
```kotlin
package com.dreslan.countdown.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dreslan.countdown.data.CountdownDatabase
import kotlinx.coroutines.flow.Flow
import com.dreslan.countdown.data.Countdown

class CountdownListViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = CountdownDatabase.getInstance(application).countdownDao()
    val countdowns: Flow<List<Countdown>> = dao.getAll()
}
```

- [ ] **Step 3: Create the list screen**

`app/src/main/java/com/dreslan/countdown/ui/list/CountdownListScreen.kt`:
```kotlin
package com.dreslan.countdown.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.ui.components.CountdownDisplay
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.CountdownItemTheme
import com.dreslan.countdown.ui.theme.MedievalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownListScreen(
    onCountdownClick: (Long) -> Unit,
    onCreateClick: () -> Unit,
    viewModel: CountdownListViewModel = viewModel()
) {
    val countdowns by viewModel.countdowns.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Braveheart Timer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanColors.backgroundStart,
                    titleContentColor = CleanColors.countdownText
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = CleanColors.labelText
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create countdown")
            }
        },
        containerColor = CleanColors.backgroundStart
    ) { padding ->
        if (countdowns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No countdowns yet.\nTap + to create one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = CleanColors.labelText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(countdowns, key = { it.id }) { countdown ->
                    CountdownCard(
                        countdown = countdown,
                        onClick = { onCountdownClick(countdown.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CountdownCard(countdown: Countdown, onClick: () -> Unit) {
    val bgBrush = when (countdown.theme) {
        CountdownTheme.CLEAN -> Brush.linearGradient(
            listOf(CleanColors.backgroundMid, CleanColors.backgroundEnd)
        )
        CountdownTheme.MEDIEVAL -> Brush.linearGradient(
            listOf(MedievalColors.backgroundMid, MedievalColors.backgroundEnd)
        )
    }

    CountdownItemTheme(theme = countdown.theme) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bgBrush)
                .clickable(onClick = onClick)
                .padding(20.dp)
        ) {
            Text(
                text = countdown.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            CountdownDisplay(
                targetDateTime = countdown.targetDateTime,
                zeroMessage = countdown.zeroMessage,
                countdownStyle = MaterialTheme.typography.displayLarge.copy(
                    fontSize = MaterialTheme.typography.displayLarge.fontSize * 0.6
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
```

- [ ] **Step 4: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/ui/components/ \
       app/src/main/java/com/dreslan/countdown/ui/list/
git commit -m "feat: add countdown list screen with themed cards and live countdown"
```

---

### Task 6: Create/Edit Countdown Screen

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/ui/edit/EditCountdownViewModel.kt`
- Create: `app/src/main/java/com/dreslan/countdown/ui/edit/EditCountdownScreen.kt`

- [ ] **Step 1: Create the ViewModel**

`app/src/main/java/com/dreslan/countdown/ui/edit/EditCountdownViewModel.kt`:
```kotlin
package com.dreslan.countdown.ui.edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.data.normalizeYoutubeUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class EditState(
    val title: String = "",
    val date: LocalDate = LocalDate.now().plusDays(1),
    val time: LocalTime = LocalTime.NOON,
    val timeZone: ZoneId = ZoneId.systemDefault(),
    val theme: CountdownTheme = CountdownTheme.CLEAN,
    val zeroMessage: String = "",
    val videoUrl: String = "",
    val isEditing: Boolean = false,
    val editId: Long = 0,
    val videoUrlError: String? = null,
    val titleError: String? = null,
    val isSaved: Boolean = false,
)

class EditCountdownViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = CountdownDatabase.getInstance(application).countdownDao()
    private val _state = MutableStateFlow(EditState())
    val state: StateFlow<EditState> = _state

    fun loadCountdown(id: Long) {
        viewModelScope.launch {
            val countdown = dao.getById(id) ?: return@launch
            val zone = ZoneId.of(countdown.timeZone)
            val zdt = countdown.targetDateTime.atZone(zone)
            _state.value = EditState(
                title = countdown.title,
                date = zdt.toLocalDate(),
                time = zdt.toLocalTime(),
                timeZone = zone,
                theme = countdown.theme,
                zeroMessage = countdown.zeroMessage ?: "",
                videoUrl = countdown.videoUrl ?: "",
                isEditing = true,
                editId = countdown.id
            )
        }
    }

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(title = title, titleError = null)
    }

    fun updateDate(date: LocalDate) {
        _state.value = _state.value.copy(date = date)
    }

    fun updateTime(time: LocalTime) {
        _state.value = _state.value.copy(time = time)
    }

    fun updateTimeZone(zone: ZoneId) {
        _state.value = _state.value.copy(timeZone = zone)
    }

    fun updateTheme(theme: CountdownTheme) {
        _state.value = _state.value.copy(theme = theme)
    }

    fun updateZeroMessage(message: String) {
        _state.value = _state.value.copy(zeroMessage = message)
    }

    fun updateVideoUrl(url: String) {
        _state.value = _state.value.copy(videoUrl = url, videoUrlError = null)
    }

    fun save() {
        val s = _state.value
        if (s.title.isBlank()) {
            _state.value = s.copy(titleError = "Title is required")
            return
        }

        val normalizedVideoUrl = if (s.videoUrl.isBlank()) {
            null
        } else {
            val normalized = normalizeYoutubeUrl(s.videoUrl)
            if (normalized == null) {
                _state.value = s.copy(videoUrlError = "Not a valid YouTube URL")
                return
            }
            normalized
        }

        val targetInstant = s.date.atTime(s.time)
            .atZone(s.timeZone)
            .toInstant()

        viewModelScope.launch {
            if (s.isEditing) {
                val existing = dao.getById(s.editId) ?: return@launch
                dao.update(
                    existing.copy(
                        title = s.title.trim(),
                        targetDateTime = targetInstant,
                        timeZone = s.timeZone.id,
                        theme = s.theme,
                        zeroMessage = s.zeroMessage.trim().ifBlank { null },
                        videoUrl = normalizedVideoUrl
                    )
                )
            } else {
                dao.insert(
                    Countdown(
                        title = s.title.trim(),
                        targetDateTime = targetInstant,
                        timeZone = s.timeZone.id,
                        theme = s.theme,
                        zeroMessage = s.zeroMessage.trim().ifBlank { null },
                        videoUrl = normalizedVideoUrl
                    )
                )
            }
            _state.value = _state.value.copy(isSaved = true)
        }
    }
}
```

- [ ] **Step 2: Create the edit screen**

`app/src/main/java/com/dreslan/countdown/ui/edit/EditCountdownScreen.kt`:
```kotlin
package com.dreslan.countdown.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.ui.theme.CleanColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCountdownScreen(
    countdownId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: EditCountdownViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(countdownId) {
        if (countdownId != null && countdownId > 0) {
            viewModel.loadCountdown(countdownId)
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Countdown" else "New Countdown") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanColors.backgroundStart,
                    titleContentColor = CleanColors.countdownText,
                    navigationIconContentColor = CleanColors.countdownText
                )
            )
        },
        containerColor = CleanColors.backgroundStart
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("Title") },
                placeholder = { Text("e.g., Catheter Removal") },
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Date: ${state.date}")
                }
                Button(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Time: %02d:%02d".format(state.time.hour, state.time.minute))
                }
            }

            Text("Theme", style = MaterialTheme.typography.labelLarge, color = CleanColors.labelText)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.theme == CountdownTheme.CLEAN,
                    onClick = { viewModel.updateTheme(CountdownTheme.CLEAN) },
                    label = { Text("Clean") }
                )
                FilterChip(
                    selected = state.theme == CountdownTheme.MEDIEVAL,
                    onClick = { viewModel.updateTheme(CountdownTheme.MEDIEVAL) },
                    label = { Text("Medieval") }
                )
            }

            OutlinedTextField(
                value = state.zeroMessage,
                onValueChange = viewModel::updateZeroMessage,
                label = { Text("Zero Message (optional)") },
                placeholder = { Text("e.g., FREEDOM!") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.videoUrl,
                onValueChange = viewModel::updateVideoUrl,
                label = { Text("YouTube URL (optional)") },
                placeholder = { Text("e.g., https://youtu.be/...") },
                isError = state.videoUrlError != null,
                supportingText = state.videoUrlError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isEditing) "Save Changes" else "Create Countdown")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        viewModel.updateDate(selected)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.time.hour,
            initialMinute = state.time.minute
        )
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/ui/edit/
git commit -m "feat: add create/edit countdown screen with validation"
```

---

### Task 7: Countdown Detail Screen

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/ui/detail/CountdownDetailViewModel.kt`
- Create: `app/src/main/java/com/dreslan/countdown/ui/detail/CountdownDetailScreen.kt`

- [ ] **Step 1: Create the ViewModel**

`app/src/main/java/com/dreslan/countdown/ui/detail/CountdownDetailViewModel.kt`:
```kotlin
package com.dreslan.countdown.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DetailState(
    val countdown: Countdown? = null,
    val isDeleted: Boolean = false
)

class CountdownDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = CountdownDatabase.getInstance(application).countdownDao()
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state

    fun loadCountdown(id: Long) {
        viewModelScope.launch {
            _state.value = DetailState(countdown = dao.getById(id))
        }
    }

    fun deleteCountdown() {
        viewModelScope.launch {
            val countdown = _state.value.countdown ?: return@launch
            dao.delete(countdown)
            _state.value = _state.value.copy(isDeleted = true)
        }
    }
}
```

- [ ] **Step 2: Create the detail screen with YouTube WebView**

`app/src/main/java/com/dreslan/countdown/ui/detail/CountdownDetailScreen.kt`:
```kotlin
package com.dreslan.countdown.ui.detail

import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.ui.components.CountdownDisplay
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.CountdownItemTheme
import com.dreslan.countdown.ui.theme.MedievalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownDetailScreen(
    countdownId: Long,
    autoPlayVideo: Boolean = false,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: CountdownDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var shouldAutoPlay by remember { mutableStateOf(autoPlayVideo) }

    LaunchedEffect(countdownId) {
        viewModel.loadCountdown(countdownId)
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onNavigateBack()
    }

    val countdown = state.countdown
    if (countdown == null) return

    val colors = when (countdown.theme) {
        CountdownTheme.CLEAN -> CleanColors
        CountdownTheme.MEDIEVAL -> MedievalColors
    }

    val bgBrush = when (countdown.theme) {
        CountdownTheme.CLEAN -> Brush.verticalGradient(
            listOf(CleanColors.backgroundStart, CleanColors.backgroundMid, CleanColors.backgroundEnd)
        )
        CountdownTheme.MEDIEVAL -> Brush.verticalGradient(
            listOf(MedievalColors.backgroundStart, MedievalColors.backgroundMid, MedievalColors.backgroundEnd)
        )
    }

    CountdownItemTheme(theme = countdown.theme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEditClick(countdown.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.backgroundStart,
                        navigationIconContentColor = colors.countdownText,
                        actionIconContentColor = colors.countdownText
                    )
                )
            },
            containerColor = colors.backgroundStart
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(bgBrush)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(48.dp))

                Text(
                    text = countdown.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))

                CountdownDisplay(
                    targetDateTime = countdown.targetDateTime,
                    zeroMessage = countdown.zeroMessage,
                    onZeroCrossing = {
                        if (countdown.videoUrl != null) {
                            shouldAutoPlay = true
                        }
                    }
                )

                if (countdown.videoUrl != null) {
                    Spacer(Modifier.height(48.dp))

                    YoutubePlayer(
                        embedUrl = countdown.videoUrl!!,
                        autoPlay = shouldAutoPlay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Countdown") },
            text = { Text("Delete \"${countdown.title}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCountdown()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun YoutubePlayer(
    embedUrl: String,
    autoPlay: Boolean,
    modifier: Modifier = Modifier
) {
    val url = if (autoPlay) "$embedUrl?autoplay=1" else embedUrl

    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    webChromeClient = WebChromeClient()
                    settings.javaScriptEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = !autoPlay
                    settings.cacheMode = WebSettings.LOAD_DEFAULT
                    loadUrl(url)
                }
            },
            update = { webView ->
                webView.loadUrl(url)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/ui/detail/
git commit -m "feat: add countdown detail screen with YouTube embed and zero-crossing auto-play"
```

---

### Task 8: Navigation + MainActivity

**Files:**
- Modify: `app/src/main/java/com/dreslan/countdown/MainActivity.kt`

- [ ] **Step 1: Wire up navigation in MainActivity**

Replace the content of `app/src/main/java/com/dreslan/countdown/MainActivity.kt`:
```kotlin
package com.dreslan.countdown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dreslan.countdown.ui.detail.CountdownDetailScreen
import com.dreslan.countdown.ui.edit.EditCountdownScreen
import com.dreslan.countdown.ui.list.CountdownListScreen
import com.dreslan.countdown.ui.theme.CountdownAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CountdownAppTheme {
                CountdownNavHost()
            }
        }
    }
}

@Composable
private fun CountdownNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            CountdownListScreen(
                onCountdownClick = { id -> navController.navigate("detail/$id") },
                onCreateClick = { navController.navigate("edit/0") }
            )
        }

        composable(
            "detail/{countdownId}?autoPlay={autoPlay}",
            arguments = listOf(
                navArgument("countdownId") { type = NavType.LongType },
                navArgument("autoPlay") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val countdownId = backStackEntry.arguments?.getLong("countdownId") ?: return@composable
            val autoPlay = backStackEntry.arguments?.getBoolean("autoPlay") ?: false
            CountdownDetailScreen(
                countdownId = countdownId,
                autoPlayVideo = autoPlay,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { id -> navController.navigate("edit/$id") }
            )
        }

        composable(
            "edit/{countdownId}",
            arguments = listOf(
                navArgument("countdownId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val countdownId = backStackEntry.arguments?.getLong("countdownId") ?: 0L
            EditCountdownScreen(
                countdownId = if (countdownId > 0) countdownId else null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/MainActivity.kt
git commit -m "feat: wire up Compose Navigation with list, detail, and edit routes"
```

---

### Task 9: Glance Widgets

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/widget/WidgetPreferences.kt`
- Create: `app/src/main/java/com/dreslan/countdown/widget/CountdownWidget.kt`
- Create: `app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetReceiver.kt`
- Create: `app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetSmall.kt`
- Create: `app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetSmallReceiver.kt`
- Create: `app/src/main/res/xml/countdown_widget_info.xml`
- Create: `app/src/main/res/xml/countdown_widget_small_info.xml`

- [ ] **Step 1: Create widget preferences helper (stores widget-to-countdown binding)**

`app/src/main/java/com/dreslan/countdown/widget/WidgetPreferences.kt`:
```kotlin
package com.dreslan.countdown.widget

import android.content.Context

private const val PREFS_NAME = "countdown_widget_prefs"
private const val KEY_PREFIX = "widget_countdown_"

fun saveWidgetCountdownId(context: Context, appWidgetId: Int, countdownId: Long) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putLong("$KEY_PREFIX$appWidgetId", countdownId)
        .apply()
}

fun getWidgetCountdownId(context: Context, appWidgetId: Int): Long? {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val key = "$KEY_PREFIX$appWidgetId"
    return if (prefs.contains(key)) prefs.getLong(key, -1L) else null
}

fun removeWidgetCountdownId(context: Context, appWidgetId: Int) {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .remove("$KEY_PREFIX$appWidgetId")
        .apply()
}
```

- [ ] **Step 2: Create the 4x2 Glance widget**

`app/src/main/java/com/dreslan/countdown/widget/CountdownWidget.kt`:
```kotlin
package com.dreslan.countdown.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dreslan.countdown.MainActivity
import com.dreslan.countdown.calculateRemaining
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.MedievalColors
import java.time.Instant

class CountdownWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val countdownId = getWidgetCountdownId(context, appWidgetId)
        val countdown = countdownId?.let {
            CountdownDatabase.getInstance(context).countdownDao().getById(it)
        }

        provideContent {
            if (countdown == null) {
                DeletedWidgetContent()
            } else {
                val time = calculateRemaining(countdown.targetDateTime, Instant.now())
                WidgetContent(
                    title = countdown.title,
                    displayText = if (time.isComplete && !countdown.zeroMessage.isNullOrBlank()) {
                        countdown.zeroMessage!!
                    } else {
                        time.toDisplayString()
                    },
                    showUnits = !(time.isComplete && !countdown.zeroMessage.isNullOrBlank()),
                    theme = countdown.theme,
                    hasVideo = countdown.videoUrl != null,
                    isLarge = true
                )
            }
        }
    }
}

@Composable
fun WidgetContent(
    title: String,
    displayText: String,
    showUnits: Boolean,
    theme: CountdownTheme,
    hasVideo: Boolean,
    isLarge: Boolean
) {
    val bgColor = when (theme) {
        CountdownTheme.CLEAN -> CleanColors.backgroundMid
        CountdownTheme.MEDIEVAL -> MedievalColors.backgroundMid
    }
    val textColor = when (theme) {
        CountdownTheme.CLEAN -> CleanColors.countdownText
        CountdownTheme.MEDIEVAL -> MedievalColors.countdownText
    }
    val labelColor = when (theme) {
        CountdownTheme.CLEAN -> CleanColors.labelText
        CountdownTheme.MEDIEVAL -> MedievalColors.labelText
    }
    val unitColor = when (theme) {
        CountdownTheme.CLEAN -> CleanColors.unitText
        CountdownTheme.MEDIEVAL -> MedievalColors.unitText
    }
    val playColor = when (theme) {
        CountdownTheme.CLEAN -> CleanColors.playButton
        CountdownTheme.MEDIEVAL -> MedievalColors.playButton
    }
    val fontFamily = when (theme) {
        CountdownTheme.CLEAN -> FontFamily.SansSerif
        CountdownTheme.MEDIEVAL -> FontFamily.Serif
    }
    val fontWeight = when (theme) {
        CountdownTheme.CLEAN -> FontWeight.Normal
        CountdownTheme.MEDIEVAL -> FontWeight.Bold
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity<MainActivity>())
            .padding(horizontal = 20.dp, vertical = if (isLarge) 16.dp else 8.dp)
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = title,
                    style = TextStyle(
                        color = ColorProvider(labelColor),
                        fontSize = 10.sp,
                        fontFamily = fontFamily
                    )
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = displayText,
                    style = TextStyle(
                        color = ColorProvider(textColor),
                        fontSize = if (isLarge) 28.sp else 22.sp,
                        fontWeight = fontWeight,
                        fontFamily = fontFamily
                    )
                )
                if (showUnits) {
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text = "DAYS    HRS     MIN     SEC",
                        style = TextStyle(
                            color = ColorProvider(unitColor),
                            fontSize = 8.sp,
                            fontFamily = fontFamily
                        )
                    )
                }
            }

            if (isLarge && hasVideo) {
                Spacer(GlanceModifier.width(12.dp))
                Box(
                    modifier = GlanceModifier
                        .size(48.dp)
                        .background(ColorProvider(bgColor))
                        .clickable(actionStartActivity<MainActivity>()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u25B6",
                        style = TextStyle(
                            color = ColorProvider(playColor),
                            fontSize = 24.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DeletedWidgetContent() {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(CleanColors.backgroundMid)
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Countdown deleted\nTap to reconfigure",
            style = TextStyle(
                color = ColorProvider(CleanColors.labelText),
                fontSize = 12.sp
            )
        )
    }
}
```

- [ ] **Step 3: Create the 4x2 receiver**

`app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetReceiver.kt`:
```kotlin
package com.dreslan.countdown.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}
```

- [ ] **Step 4: Create the 4x1 Glance widget**

`app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetSmall.kt`:
```kotlin
package com.dreslan.countdown.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import com.dreslan.countdown.calculateRemaining
import com.dreslan.countdown.data.CountdownDatabase
import java.time.Instant

class CountdownWidgetSmall : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val countdownId = getWidgetCountdownId(context, appWidgetId)
        val countdown = countdownId?.let {
            CountdownDatabase.getInstance(context).countdownDao().getById(it)
        }

        provideContent {
            if (countdown == null) {
                DeletedWidgetContent()
            } else {
                val time = calculateRemaining(countdown.targetDateTime, Instant.now())
                WidgetContent(
                    title = countdown.title,
                    displayText = if (time.isComplete && !countdown.zeroMessage.isNullOrBlank()) {
                        countdown.zeroMessage!!
                    } else {
                        time.toDisplayString()
                    },
                    showUnits = !(time.isComplete && !countdown.zeroMessage.isNullOrBlank()),
                    theme = countdown.theme,
                    hasVideo = countdown.videoUrl != null,
                    isLarge = false
                )
            }
        }
    }
}
```

- [ ] **Step 5: Create the 4x1 receiver**

`app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetSmallReceiver.kt`:
```kotlin
package com.dreslan.countdown.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class CountdownWidgetSmallReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidgetSmall()
}
```

- [ ] **Step 6: Create widget info XML files**

`app/src/main/res/xml/countdown_widget_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="110dp"
    android:targetCellWidth="4"
    android:targetCellHeight="2"
    android:updatePeriodMillis="1800000"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:configure="com.dreslan.countdown.widget.CountdownWidgetConfigActivity"
    android:description="@string/widget_description_large" />
```

`app/src/main/res/xml/countdown_widget_small_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="40dp"
    android:targetCellWidth="4"
    android:targetCellHeight="1"
    android:updatePeriodMillis="1800000"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:configure="com.dreslan.countdown.widget.CountdownWidgetConfigActivity"
    android:description="@string/widget_description_small" />
```

- [ ] **Step 7: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (config activity not yet created — will be added next task, but XML reference is forward-declared)

Note: If the build fails because `CountdownWidgetConfigActivity` is referenced but doesn't exist yet, create a stub:
```kotlin
// app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetConfigActivity.kt
package com.dreslan.countdown.widget
import androidx.activity.ComponentActivity
class CountdownWidgetConfigActivity : ComponentActivity()
```

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/widget/ \
       app/src/main/res/xml/
git commit -m "feat: add 4x1 and 4x2 Glance widgets with themed rendering"
```

---

### Task 10: Widget Configuration Activity

**Files:**
- Create (or replace stub): `app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetConfigActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create the widget configuration activity**

`app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetConfigActivity.kt`:
```kotlin
package com.dreslan.countdown.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.CountdownAppTheme
import kotlinx.coroutines.launch

class CountdownWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set CANCELED result by default — if user backs out, widget won't be placed
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val dao = CountdownDatabase.getInstance(this).countdownDao()

        setContent {
            CountdownAppTheme {
                val countdowns by dao.getAll().collectAsState(initial = emptyList())
                ConfigScreen(
                    countdowns = countdowns,
                    onSelect = { countdown -> selectCountdown(countdown.id) },
                    onCreateClick = {
                        // Launch main app to create screen
                        val intent = Intent(this@CountdownWidgetConfigActivity, com.dreslan.countdown.MainActivity::class.java)
                        intent.putExtra("navigate_to", "edit/0")
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun selectCountdown(countdownId: Long) {
        saveWidgetCountdownId(this, appWidgetId, countdownId)

        // Determine widget type and update
        lifecycleScope.launch {
            val manager = GlanceAppWidgetManager(this@CountdownWidgetConfigActivity)
            // Try updating as both types — only the correct one will match
            try {
                val glanceId = manager.getGlanceIdBy(appWidgetId)
                CountdownWidget().update(this@CountdownWidgetConfigActivity, glanceId)
            } catch (_: Exception) {
                try {
                    val glanceId = manager.getGlanceIdBy(appWidgetId)
                    CountdownWidgetSmall().update(this@CountdownWidgetConfigActivity, glanceId)
                } catch (_: Exception) { }
            }
        }

        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigScreen(
    countdowns: List<Countdown>,
    onSelect: (Countdown) -> Unit,
    onCreateClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Countdown") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanColors.backgroundStart,
                    titleContentColor = CleanColors.countdownText
                )
            )
        },
        containerColor = CleanColors.backgroundStart
    ) { padding ->
        if (countdowns.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No countdowns yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = CleanColors.labelText
                    )
                    Button(
                        onClick = onCreateClick,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Create One")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(countdowns, key = { it.id }) { countdown ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(countdown) },
                        colors = CardDefaults.cardColors(
                            containerColor = CleanColors.backgroundMid
                        )
                    ) {
                        Text(
                            text = countdown.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = CleanColors.countdownText,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Update AndroidManifest.xml with widget receivers and config activity**

Replace `app/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:name=".CountdownApp"
        android:allowBackup="true"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@android:style/Theme.Material.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Widget config activity -->
        <activity
            android:name=".widget.CountdownWidgetConfigActivity"
            android:exported="true"
            android:theme="@android:style/Theme.Material.NoActionBar" />

        <!-- 4x2 widget -->
        <receiver
            android:name=".widget.CountdownWidgetReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/countdown_widget_info" />
        </receiver>

        <!-- 4x1 widget -->
        <receiver
            android:name=".widget.CountdownWidgetSmallReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/countdown_widget_small_info" />
        </receiver>

        <!-- Boot receiver for widget tick re-registration -->
        <receiver
            android:name=".widget.WidgetTickReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetConfigActivity.kt \
       app/src/main/AndroidManifest.xml
git commit -m "feat: add widget configuration activity and manifest declarations"
```

---

### Task 11: Widget Updates (Tick + Boot Receivers)

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/widget/WidgetTickReceiver.kt`
- Modify: `app/src/main/java/com/dreslan/countdown/CountdownApp.kt`

- [ ] **Step 1: Create the boot receiver**

`app/src/main/java/com/dreslan/countdown/widget/WidgetTickReceiver.kt`:
```kotlin
package com.dreslan.countdown.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetTickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-register the tick listener via Application
            // The Application class handles tick registration in onCreate
            // On boot, the system will create the Application, which registers the tick
        }
    }
}
```

- [ ] **Step 2: Update CountdownApp to register ACTION_TIME_TICK and update widgets**

Replace `app/src/main/java/com/dreslan/countdown/CountdownApp.kt`:
```kotlin
package com.dreslan.countdown

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.dreslan.countdown.widget.CountdownWidget
import com.dreslan.countdown.widget.CountdownWidgetSmall

class CountdownApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val tickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_TIME_TICK) {
                appScope.launch {
                    CountdownWidget().updateAll(context)
                    CountdownWidgetSmall().updateAll(context)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(tickReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
    }
}
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/CountdownApp.kt \
       app/src/main/java/com/dreslan/countdown/widget/WidgetTickReceiver.kt
git commit -m "feat: add per-minute widget updates via ACTION_TIME_TICK and boot receiver"
```

---

### Task 12: Final Integration and Verification

**Files:**
- All files from previous tasks

- [ ] **Step 1: Run full build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run unit tests**

Run: `./gradlew test`
Expected: ALL PASS (YoutubeUrlTest, CountdownCalcTest)

- [ ] **Step 3: Run lint check**

Run: `./gradlew lint`
Expected: No errors (warnings acceptable)

- [ ] **Step 4: Verify APK is generated**

Run: `ls -la app/build/outputs/apk/debug/`
Expected: `app-debug.apk` exists

- [ ] **Step 5: Commit any final fixes**

If any issues were found and fixed in previous steps:
```bash
git add -A
git commit -m "fix: resolve build/lint issues from integration"
```

- [ ] **Step 6: Tag initial release**

```bash
git tag v1.0.0
```
