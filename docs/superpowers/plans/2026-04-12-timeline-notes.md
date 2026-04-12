# Timeline Notes & Detail Screen Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add journal-style notes to countdowns with a timeline visualization, retro TV video button, bigger widget buttons, and export.

**Architecture:** New `Note` Room entity with FK to `Countdown`. Detail screen replaces `VideoPlayButton` with `RetroTvButton` composable, `LinearProgressIndicator` with `TimelineBar`, and adds a `NoteFeed` + `NoteDialog`. Export formats countdown + notes as Markdown/JSON via Android share sheet.

**Tech Stack:** Room (data), Jetpack Compose (UI), Glance (widget), Paparazzi (screenshot tests)

---

## File Structure

### New Files
- `data/Note.kt` — Note Room entity
- `data/NoteDao.kt` — DAO for note CRUD
- `data/ExportFormatter.kt` — Markdown and JSON formatting
- `ui/detail/RetroTvButton.kt` — Retro TV composable
- `ui/detail/TimelineBar.kt` — Progress bar with note dots and labels
- `ui/detail/NoteFeed.kt` — Vertical note list with edit callbacks
- `ui/detail/NoteDialog.kt` — Add/edit note dialog
- `tests/data/ExportFormatterTest.kt` — Export formatting tests

### Modified Files
- `data/CountdownDatabase.kt` — Add Note entity, NoteDao, migration v4→v5
- `ui/detail/CountdownDetailViewModel.kt` — Add notes state, note CRUD, export
- `ui/detail/CountdownDetailScreen.kt` — Replace video button + progress bar, add FAB + notes
- `ui/list/CountdownListScreen.kt` — Add note count to cards
- `ui/list/CountdownListViewModel.kt` — Add note counts flow
- `widget/CountdownWidgetSmall.kt` — Bump button sizes

---

### Task 1: Note Entity and DAO

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/data/Note.kt`
- Create: `app/src/main/java/com/dreslan/countdown/data/NoteDao.kt`
- Modify: `app/src/main/java/com/dreslan/countdown/data/CountdownDatabase.kt`

- [ ] **Step 1: Create Note entity**

```kotlin
// app/src/main/java/com/dreslan/countdown/data/Note.kt
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
```

- [ ] **Step 2: Create NoteDao**

```kotlin
// app/src/main/java/com/dreslan/countdown/data/NoteDao.kt
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
```

- [ ] **Step 3: Update CountdownDatabase — add Note entity, NoteDao, migration v4→v5**

In `CountdownDatabase.kt`:

1. Change `@Database` annotation:
```kotlin
@Database(entities = [Countdown::class, Note::class], version = 5, exportSchema = false)
```

2. Add abstract DAO method:
```kotlin
abstract fun noteDao(): NoteDao
```

3. Add migration:
```kotlin
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
```

4. Register migration in builder:
```kotlin
.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
```

- [ ] **Step 4: Build to verify compilation**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/data/Note.kt \
       app/src/main/java/com/dreslan/countdown/data/NoteDao.kt \
       app/src/main/java/com/dreslan/countdown/data/CountdownDatabase.kt
git commit -m "feat: add Note entity, NoteDao, and migration v4→v5"
```

---

### Task 2: Widget Button Sizing

**Files:**
- Modify: `app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetSmall.kt:204-240`

- [ ] **Step 1: Increase play button size**

Change the play button Box from `.size(44.dp)` to `.size(56.dp)` and the Text fontSize from `26.sp` to `32.sp`:

```kotlin
// Line 206-224, change:
Box(
    modifier = GlanceModifier
        .size(56.dp)
        .clickable(
            actionRunCallback<PlayVideoAction>(
                androidx.glance.action.actionParametersOf(
                    PlayVideoAction.VIDEO_URL_KEY to videoUrl
                )
            )
        ),
    contentAlignment = Alignment.Center
) {
    Text(
        text = "\u25B6",
        style = TextStyle(
            color = ColorProvider(playColor),
            fontSize = 32.sp
        )
    )
}
```

- [ ] **Step 2: Increase refresh button size**

Change the refresh button Box from `.size(28.dp)` to `.size(40.dp)` and the Text fontSize from `16.sp` to `22.sp`:

```kotlin
// Line 227-240, change:
Box(
    modifier = GlanceModifier
        .size(40.dp)
        .clickable(actionRunCallback<RefreshAction>()),
    contentAlignment = Alignment.Center
) {
    Text(
        text = "\u21BB",
        style = TextStyle(
            color = ColorProvider(labelColor),
            fontSize = 22.sp
        )
    )
}
```

- [ ] **Step 3: Build and verify**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/widget/CountdownWidgetSmall.kt
git commit -m "fix: increase widget play and refresh button tap targets"
```

---

### Task 3: RetroTvButton Composable

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/ui/detail/RetroTvButton.kt`

- [ ] **Step 1: Create RetroTvButton composable**

```kotlin
// app/src/main/java/com/dreslan/countdown/ui/detail/RetroTvButton.kt
package com.dreslan.countdown.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun RetroTvButton(
    embedUrl: String,
    bodyColor: Color,
    borderColor: Color,
    screenColor: Color = Color(0xFF111111),
    playIconColor: Color,
    modifier: Modifier = Modifier
) {
    val videoId = embedUrl.substringAfterLast("/").substringBefore("?")
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .clickable {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/watch?v=$videoId")
                    )
                    context.startActivity(intent)
                }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Antennas
                Canvas(
                    modifier = Modifier
                        .width(200.dp)
                        .height(30.dp)
                ) {
                    val centerX = size.width / 2
                    val antennaSpread = 50.dp.toPx()

                    // Left antenna
                    drawLine(
                        color = borderColor,
                        start = Offset(centerX - 15.dp.toPx(), size.height),
                        end = Offset(centerX - antennaSpread, 6.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = borderColor,
                        radius = 4.dp.toPx(),
                        center = Offset(centerX - antennaSpread, 4.dp.toPx())
                    )

                    // Right antenna
                    drawLine(
                        color = borderColor,
                        start = Offset(centerX + 15.dp.toPx(), size.height),
                        end = Offset(centerX + antennaSpread, 6.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawCircle(
                        color = borderColor,
                        radius = 4.dp.toPx(),
                        center = Offset(centerX + antennaSpread, 4.dp.toPx())
                    )
                }

                // TV body
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bodyColor)
                        .padding(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Screen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(screenColor)
                                .drawBehind {
                                    // Scanlines
                                    var y = 0f
                                    while (y < size.height) {
                                        drawLine(
                                            color = Color.White.copy(alpha = 0.04f),
                                            start = Offset(0f, y),
                                            end = Offset(size.width, y),
                                            strokeWidth = 1f
                                        )
                                        y += 3.dp.toPx()
                                    }
                                    // Static noise dots
                                    val step = 4.dp.toPx()
                                    var nx = 0f
                                    while (nx < size.width) {
                                        var ny = 0f
                                        while (ny < size.height) {
                                            val noise = sin(nx * 127.1 + ny * 311.7).let {
                                                (it * 43758.5453).let { v -> v - v.toLong() }
                                            }
                                            if (noise > 0.7f) {
                                                drawCircle(
                                                    color = Color.White.copy(alpha = 0.06f),
                                                    radius = 0.5.dp.toPx(),
                                                    center = Offset(nx, ny)
                                                )
                                            }
                                            ny += step
                                        }
                                        nx += step
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Play button
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play video",
                                    tint = playIconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Knobs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(borderColor, CircleShape)
                            )
                            Box(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(borderColor, CircleShape)
                            )
                        }
                    }
                }

                // Pedestal feet
                Row(
                    modifier = Modifier.width(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(80.dp, Alignment.CenterHorizontally)
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(bodyColor)
                    )
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(bodyColor)
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Build to verify compilation**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/ui/detail/RetroTvButton.kt
git commit -m "feat: add RetroTvButton composable with antennas, static, and pedestal feet"
```

---

### Task 4: TimelineBar Composable

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/ui/detail/TimelineBar.kt`

- [ ] **Step 1: Create TimelineBar composable**

```kotlin
// app/src/main/java/com/dreslan/countdown/ui/detail/TimelineBar.kt
package com.dreslan.countdown.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.time.Instant

@Composable
fun TimelineBar(
    startDate: Instant,
    targetDate: Instant,
    noteTimestamps: List<Instant>,
    progressColor: Color,
    trackColor: Color,
    dotColor: Color,
    labelColor: Color,
    startLabel: String,
    targetLabel: String,
    modifier: Modifier = Modifier
) {
    val now = Instant.now()
    val totalDuration = targetDate.toEpochMilli() - startDate.toEpochMilli()
    val elapsed = now.toEpochMilli() - startDate.toEpochMilli()
    val progress = if (totalDuration > 0) (elapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 1f
    val percentText = "${(progress * 100).toInt()}%"

    Column(modifier = modifier.fillMaxWidth()) {
        // Labels row
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = startLabel,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = percentText,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
            Text(
                text = targetLabel,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }

        // Bar with dots
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .padding(vertical = 5.dp)
        ) {
            val barHeight = 6.dp.toPx()
            val barY = (size.height - barHeight) / 2
            val cornerRadius = barHeight / 2

            // Track
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(0f, barY),
                size = Size(size.width, barHeight),
                cornerRadius = CornerRadius(cornerRadius)
            )

            // Filled portion
            val filledWidth = size.width * progress
            if (filledWidth > 0f) {
                drawRoundRect(
                    color = progressColor,
                    topLeft = Offset(0f, barY),
                    size = Size(filledWidth, barHeight),
                    cornerRadius = CornerRadius(cornerRadius)
                )
            }

            // Note dots
            val dotRadius = 6.dp.toPx()
            val bgRadius = dotRadius + 2.dp.toPx()
            val centerY = size.height / 2
            for (timestamp in noteTimestamps) {
                val noteElapsed = timestamp.toEpochMilli() - startDate.toEpochMilli()
                val noteProgress = if (totalDuration > 0) (noteElapsed.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f) else 0f
                val x = size.width * noteProgress
                // Background circle for contrast
                drawCircle(
                    color = Color(0xFF1A1A1A),
                    radius = bgRadius,
                    center = Offset(x, centerY)
                )
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, centerY)
                )
            }
        }
    }
}
```

- [ ] **Step 2: Build to verify compilation**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/ui/detail/TimelineBar.kt
git commit -m "feat: add TimelineBar composable with progress and note dots"
```

---

### Task 5: NoteFeed and NoteDialog Composables

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/ui/detail/NoteFeed.kt`
- Create: `app/src/main/java/com/dreslan/countdown/ui/detail/NoteDialog.kt`

- [ ] **Step 1: Create NoteFeed composable**

```kotlin
// app/src/main/java/com/dreslan/countdown/ui/detail/NoteFeed.kt
package com.dreslan.countdown.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dreslan.countdown.data.Note
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NoteFeed(
    notes: List<Note>,
    timeZone: ZoneId,
    dotColor: Color,
    lineColor: Color,
    dateColor: Color,
    textColor: Color,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    Column(modifier = modifier) {
        notes.forEachIndexed { index, note ->
            val isLast = index == notes.lastIndex
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .clickable { onNoteClick(note) }
                    .padding(vertical = 4.dp)
            ) {
                // Dot + vertical line
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight()
                        .drawBehind {
                            val centerX = size.width / 2
                            // Dot
                            drawCircle(
                                color = dotColor,
                                radius = 5.dp.toPx(),
                                center = Offset(centerX, 10.dp.toPx())
                            )
                            // Line below dot (unless last item)
                            if (!isLast) {
                                drawLine(
                                    color = lineColor,
                                    start = Offset(centerX, 16.dp.toPx()),
                                    end = Offset(centerX, size.height),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                )

                // Note content
                Column(modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)) {
                    Text(
                        text = note.createdAt.atZone(timeZone).format(dateFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = dateColor
                    )
                    Text(
                        text = note.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Create NoteDialog composable**

```kotlin
// app/src/main/java/com/dreslan/countdown/ui/detail/NoteDialog.kt
package com.dreslan.countdown.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dreslan.countdown.data.Note
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    existingNote: Note?,
    timeZone: ZoneId,
    onSave: (text: String, createdAt: Instant) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val initialZdt = (existingNote?.createdAt ?: Instant.now()).atZone(timeZone)
    var text by remember { mutableStateOf(existingNote?.text ?: "") }
    var date by remember { mutableStateOf(initialZdt.toLocalDate()) }
    var time by remember { mutableStateOf(initialZdt.toLocalTime()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingNote != null) "Edit Note" else "Add Note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(date.format(dateFormatter))
                    }
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(time.format(timeFormatter))
                    }
                }

                if (onDelete != null) {
                    TextButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    ) {
                        Text("Delete note", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (text.isNotBlank()) {
                        val instant = date.atTime(time).atZone(timeZone).toInstant()
                        onSave(text.trim(), instant)
                        onDismiss()
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val epochMillis = date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = epochMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}
```

- [ ] **Step 3: Build to verify compilation**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/ui/detail/NoteFeed.kt \
       app/src/main/java/com/dreslan/countdown/ui/detail/NoteDialog.kt
git commit -m "feat: add NoteFeed and NoteDialog composables"
```

---

### Task 6: Detail ViewModel — Notes Support

**Files:**
- Modify: `app/src/main/java/com/dreslan/countdown/ui/detail/CountdownDetailViewModel.kt`

- [ ] **Step 1: Add notes to DetailState and ViewModel**

Replace the entire file:

```kotlin
package com.dreslan.countdown.ui.detail

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dreslan.countdown.data.Countdown
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.data.Note
import com.dreslan.countdown.data.ExportFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

data class DetailState(
    val countdown: Countdown? = null,
    val notes: List<Note> = emptyList(),
    val isDeleted: Boolean = false
)

class CountdownDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CountdownDatabase.getInstance(application)
    private val countdownDao = db.countdownDao()
    private val noteDao = db.noteDao()
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state

    fun loadCountdown(id: Long) {
        viewModelScope.launch {
            countdownDao.getByIdFlow(id).collect { countdown ->
                if (!_state.value.isDeleted) {
                    _state.value = _state.value.copy(countdown = countdown)
                }
            }
        }
        viewModelScope.launch {
            noteDao.getNotesForCountdown(id).collect { notes ->
                _state.value = _state.value.copy(notes = notes)
            }
        }
    }

    fun addNote(text: String, createdAt: Instant) {
        val countdown = _state.value.countdown ?: return
        viewModelScope.launch {
            noteDao.insert(
                Note(
                    countdownId = countdown.id,
                    text = text,
                    createdAt = createdAt
                )
            )
        }
    }

    fun updateNote(note: Note, text: String, createdAt: Instant) {
        viewModelScope.launch {
            noteDao.update(
                note.copy(
                    text = text,
                    createdAt = createdAt,
                    updatedAt = Instant.now()
                )
            )
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            noteDao.deleteById(noteId)
        }
    }

    fun deleteCountdown() {
        viewModelScope.launch {
            val countdown = _state.value.countdown ?: return@launch
            _state.value = _state.value.copy(isDeleted = true)
            countdownDao.delete(countdown)
        }
    }

    fun exportMarkdown(): Intent {
        val s = _state.value
        val text = ExportFormatter.toMarkdown(s.countdown!!, s.notes)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, s.countdown.title)
        }
    }

    fun exportJson(): Intent {
        val s = _state.value
        val text = ExportFormatter.toJson(s.countdown!!, s.notes)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, s.countdown.title)
        }
    }
}
```

- [ ] **Step 2: This will not compile yet — ExportFormatter doesn't exist. That's Task 7. Proceed to Task 7 before building.**

---

### Task 7: ExportFormatter

**Files:**
- Create: `app/src/main/java/com/dreslan/countdown/data/ExportFormatter.kt`
- Create: `app/src/test/java/com/dreslan/countdown/data/ExportFormatterTest.kt`

- [ ] **Step 1: Write failing tests for ExportFormatter**

```kotlin
// app/src/test/java/com/dreslan/countdown/data/ExportFormatterTest.kt
package com.dreslan.countdown.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ExportFormatterTest {
    private val countdown = Countdown(
        id = 1,
        title = "FREEDOM!",
        targetDateTime = Instant.parse("2026-04-22T16:30:00Z"),
        timeZone = "America/New_York",
        theme = CountdownTheme.MEDIEVAL,
        startDate = Instant.parse("2026-03-30T17:30:00Z"),
        createdAt = Instant.parse("2026-03-30T17:30:00Z"),
        showProgress = true
    )

    private val notes = listOf(
        Note(id = 1, countdownId = 1, text = "Day 1 post-op", createdAt = Instant.parse("2026-03-31T12:00:00Z")),
        Note(id = 2, countdownId = 1, text = "Feeling better", createdAt = Instant.parse("2026-04-04T15:00:00Z"))
    )

    @Test
    fun markdownContainsTitleAsHeading() {
        val md = ExportFormatter.toMarkdown(countdown, notes)
        assertTrue(md.startsWith("# FREEDOM!"))
    }

    @Test
    fun markdownContainsTargetDate() {
        val md = ExportFormatter.toMarkdown(countdown, notes)
        assertTrue(md.contains("**Target:**"))
        assertTrue(md.contains("April 22, 2026"))
    }

    @Test
    fun markdownContainsStartDate() {
        val md = ExportFormatter.toMarkdown(countdown, notes)
        assertTrue(md.contains("**Started:**"))
        assertTrue(md.contains("March 30, 2026"))
    }

    @Test
    fun markdownContainsNotes() {
        val md = ExportFormatter.toMarkdown(countdown, notes)
        assertTrue(md.contains("Day 1 post-op"))
        assertTrue(md.contains("Feeling better"))
    }

    @Test
    fun markdownHandlesEmptyNotes() {
        val md = ExportFormatter.toMarkdown(countdown, emptyList())
        assertTrue(!md.contains("## Notes"))
    }

    @Test
    fun jsonContainsTitle() {
        val json = ExportFormatter.toJson(countdown, notes)
        assertTrue(json.contains("\"title\": \"FREEDOM!\""))
    }

    @Test
    fun jsonContainsNotes() {
        val json = ExportFormatter.toJson(countdown, notes)
        assertTrue(json.contains("\"text\": \"Day 1 post-op\""))
        assertTrue(json.contains("\"text\": \"Feeling better\""))
    }

    @Test
    fun jsonHandlesEmptyNotes() {
        val json = ExportFormatter.toJson(countdown, emptyList())
        assertTrue(json.contains("\"notes\": []"))
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --tests "com.dreslan.countdown.data.ExportFormatterTest"`
Expected: Compilation failure — `ExportFormatter` not found

- [ ] **Step 3: Implement ExportFormatter**

```kotlin
// app/src/main/java/com/dreslan/countdown/data/ExportFormatter.kt
package com.dreslan.countdown.data

import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ExportFormatter {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a")
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
    private val noteDateFormatter = DateTimeFormatter.ofPattern("MMM d")

    fun toMarkdown(countdown: Countdown, notes: List<Note>): String {
        val zone = ZoneId.of(countdown.timeZone)
        val targetFormatted = countdown.targetDateTime.atZone(zone).format(dateTimeFormatter)
        val origin = countdown.startDate ?: countdown.createdAt
        val startFormatted = origin.atZone(zone).format(dateTimeFormatter)

        return buildString {
            appendLine("# ${countdown.title}")
            appendLine()
            appendLine("**Target:** $targetFormatted")
            appendLine("**Started:** $startFormatted")

            if (notes.isNotEmpty()) {
                appendLine()
                appendLine("## Notes")
                appendLine()
                for (note in notes) {
                    val dateStr = note.createdAt.atZone(zone).format(noteDateFormatter)
                    appendLine("- **$dateStr:** ${note.text}")
                }
            }
        }.trimEnd()
    }

    fun toJson(countdown: Countdown, notes: List<Note>): String {
        val zone = ZoneId.of(countdown.timeZone)
        val origin = countdown.startDate ?: countdown.createdAt

        return buildString {
            appendLine("{")
            appendLine("  \"title\": \"${countdown.title}\",")
            appendLine("  \"targetDateTime\": \"${countdown.targetDateTime.atZone(zone)}\",")
            appendLine("  \"startDate\": \"${origin.atZone(zone)}\",")
            appendLine("  \"createdAt\": \"${countdown.createdAt.atZone(zone)}\",")
            appendLine("  \"theme\": \"${countdown.theme.name}\",")
            if (notes.isEmpty()) {
                appendLine("  \"notes\": []")
            } else {
                appendLine("  \"notes\": [")
                notes.forEachIndexed { index, note ->
                    val comma = if (index < notes.lastIndex) "," else ""
                    appendLine("    {")
                    appendLine("      \"text\": \"${note.text}\",")
                    appendLine("      \"createdAt\": \"${note.createdAt.atZone(zone)}\"")
                    appendLine("    }$comma")
                }
                appendLine("  ]")
            }
            appendLine("}")
        }.trimEnd()
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew :app:testDebugUnitTest --tests "com.dreslan.countdown.data.ExportFormatterTest"`
Expected: All 8 tests PASS

- [ ] **Step 5: Build full project**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (Task 6 ViewModel changes can now compile)

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/data/ExportFormatter.kt \
       app/src/test/java/com/dreslan/countdown/data/ExportFormatterTest.kt \
       app/src/main/java/com/dreslan/countdown/ui/detail/CountdownDetailViewModel.kt
git commit -m "feat: add ExportFormatter with markdown/JSON output and tests"
```

---

### Task 8: Rewire Detail Screen

**Files:**
- Modify: `app/src/main/java/com/dreslan/countdown/ui/detail/CountdownDetailScreen.kt`

This is the largest change. Replace `LinearProgressIndicator` with `TimelineBar`, replace `VideoPlayButton` with `RetroTvButton`, add `NoteFeed`, `NoteDialog`, FAB, and export menu.

- [ ] **Step 1: Rewrite CountdownDetailScreen**

Replace the entire file with:

```kotlin
package com.dreslan.countdown.ui.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.dreslan.countdown.data.CountdownTheme
import com.dreslan.countdown.data.Note
import com.dreslan.countdown.ui.components.CountdownDisplay
import com.dreslan.countdown.ui.theme.CleanColors
import com.dreslan.countdown.ui.theme.CountdownItemTheme
import com.dreslan.countdown.ui.theme.MedievalColors
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    var showNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }
    var showExportMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(countdownId) {
        viewModel.loadCountdown(countdownId)
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onNavigateBack()
        }
    }

    val countdown = state.countdown ?: return

    val backgroundBrush = when (countdown.theme) {
        CountdownTheme.CLEAN -> Brush.verticalGradient(
            listOf(CleanColors.backgroundStart, CleanColors.backgroundMid, CleanColors.backgroundEnd)
        )
        CountdownTheme.MEDIEVAL -> Brush.verticalGradient(
            listOf(MedievalColors.backgroundStart, MedievalColors.backgroundMid, MedievalColors.backgroundEnd)
        )
    }

    val themeColors = when (countdown.theme) {
        CountdownTheme.CLEAN -> CleanColors
        CountdownTheme.MEDIEVAL -> MedievalColors
    }

    val topBarColors = when (countdown.theme) {
        CountdownTheme.CLEAN -> Triple(CleanColors.backgroundStart, CleanColors.countdownText, CleanColors.labelText)
        CountdownTheme.MEDIEVAL -> Triple(MedievalColors.backgroundStart, MedievalColors.countdownText, MedievalColors.labelText)
    }
    val (topBarBg, topBarContent, topBarAction) = topBarColors

    CountdownItemTheme(theme = countdown.theme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = topBarContent
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEditClick(countdown.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = topBarAction)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = topBarAction)
                        }
                        if (state.notes.isNotEmpty()) {
                            Box {
                                IconButton(onClick = { showExportMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = topBarAction)
                                }
                                DropdownMenu(
                                    expanded = showExportMenu,
                                    onDismissRequest = { showExportMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Export as Markdown") },
                                        onClick = {
                                            showExportMenu = false
                                            context.startActivity(Intent.createChooser(viewModel.exportMarkdown(), "Export"))
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Export as JSON") },
                                        onClick = {
                                            showExportMenu = false
                                            context.startActivity(Intent.createChooser(viewModel.exportJson(), "Export"))
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarBg)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editingNote = null
                        showNoteDialog = true
                    },
                    containerColor = themeColors.labelText
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add note")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            val bgBitmap = countdown.backgroundImagePath?.let { path ->
                if (path.isNotBlank()) {
                    try { BitmapFactory.decodeFile(path)?.asImageBitmap() } catch (_: Exception) { null }
                } else null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(padding)
            ) {
                if (bgBitmap != null) {
                    Image(
                        bitmap = bgBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(modifier = Modifier.matchParentSize().background(Color(0xAA000000)))
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = countdown.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val zone = ZoneId.of(countdown.timeZone)
                    val zdt = countdown.targetDateTime.atZone(zone)
                    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, h:mm a")
                    Text(
                        text = zdt.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )

                    CountdownDisplay(
                        targetDateTime = countdown.targetDateTime,
                        zeroMessage = countdown.zeroMessage,
                        countdownStyle = MaterialTheme.typography.displayLarge,
                        labelStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Timeline bar
                    if (countdown.showProgress) {
                        val origin = countdown.startDate ?: countdown.createdAt
                        val startFormatter = DateTimeFormatter.ofPattern("MMM d")
                        val startLabel = origin.atZone(zone).format(startFormatter)
                        val targetLabel = countdown.targetDateTime.atZone(zone).format(startFormatter)

                        TimelineBar(
                            startDate = origin,
                            targetDate = countdown.targetDateTime,
                            noteTimestamps = state.notes.map { it.createdAt },
                            progressColor = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface,
                            dotColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            startLabel = startLabel,
                            targetLabel = targetLabel,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Note feed
                    if (state.notes.isNotEmpty()) {
                        NoteFeed(
                            notes = state.notes,
                            timeZone = ZoneId.of(countdown.timeZone),
                            dotColor = MaterialTheme.colorScheme.primary,
                            lineColor = MaterialTheme.colorScheme.surface,
                            dateColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            textColor = MaterialTheme.colorScheme.onBackground,
                            onNoteClick = { note ->
                                editingNote = note
                                showNoteDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Retro TV video button
                    val videoUrl = countdown.videoUrl
                    if (!videoUrl.isNullOrBlank()) {
                        val tvColors = when (countdown.theme) {
                            CountdownTheme.CLEAN -> Triple(
                                CleanColors.backgroundEnd,
                                CleanColors.unitText,
                                CleanColors.playButton
                            )
                            CountdownTheme.MEDIEVAL -> Triple(
                                MedievalColors.backgroundEnd,
                                MedievalColors.unitText,
                                MedievalColors.playButton
                            )
                        }
                        RetroTvButton(
                            embedUrl = videoUrl,
                            bodyColor = tvColors.first,
                            borderColor = tvColors.second,
                            playIconColor = tvColors.third
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete countdown?") },
            text = { Text("\"${countdown.title}\" will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCountdown()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showNoteDialog) {
        NoteDialog(
            existingNote = editingNote,
            timeZone = ZoneId.of(countdown.timeZone),
            onSave = { text, createdAt ->
                val existing = editingNote
                if (existing != null) {
                    viewModel.updateNote(existing, text, createdAt)
                } else {
                    viewModel.addNote(text, createdAt)
                }
            },
            onDelete = editingNote?.let { note -> { viewModel.deleteNote(note.id) } },
            onDismiss = {
                showNoteDialog = false
                editingNote = null
            }
        )
    }
}
```

- [ ] **Step 2: Remove the old VideoPlayButton** — it's no longer referenced. The old `VideoPlayButton` composable (lines 262-300 of the original file) is now replaced by `RetroTvButton`. Since we're replacing the entire file, it's already gone.

- [ ] **Step 3: Build to verify compilation**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/ui/detail/CountdownDetailScreen.kt
git commit -m "feat: rewire detail screen with timeline, note feed, retro TV, and export"
```

---

### Task 9: List Screen — Note Count on Cards

**Files:**
- Modify: `app/src/main/java/com/dreslan/countdown/ui/list/CountdownListViewModel.kt`
- Modify: `app/src/main/java/com/dreslan/countdown/ui/list/CountdownListScreen.kt`

- [ ] **Step 1: Update CountdownListViewModel to provide note counts**

Replace the entire file:

```kotlin
package com.dreslan.countdown.ui.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dreslan.countdown.data.CountdownDatabase
import com.dreslan.countdown.data.Countdown
import kotlinx.coroutines.flow.Flow

class CountdownListViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CountdownDatabase.getInstance(application)
    val countdowns: Flow<List<Countdown>> = db.countdownDao().getAll()

    fun getNoteCount(countdownId: Long): Flow<Int> = db.noteDao().getNoteCountForCountdown(countdownId)
}
```

- [ ] **Step 2: Update CountdownCard to show note count**

In `CountdownListScreen.kt`, add note count display after the progress bar. Change the `CountdownCard` composable signature and the call site.

Update the call site in `CountdownListScreen` (around line 103-107):

```kotlin
items(countdowns, key = { it.id }) { countdown ->
    val noteCount by viewModel.getNoteCount(countdown.id).collectAsState(initial = 0)
    CountdownCard(
        countdown = countdown,
        noteCount = noteCount,
        onClick = { onCountdownClick(countdown.id) }
    )
}
```

Update the `CountdownCard` signature and add note count after progress bar:

```kotlin
@Composable
private fun CountdownCard(countdown: Countdown, noteCount: Int, onClick: () -> Unit) {
```

After the `LinearProgressIndicator` block (around line 197), add:

```kotlin
if (countdown.showProgress && noteCount > 0) {
    Text(
        text = "$noteCount note${if (noteCount != 1) "s" else ""}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = Modifier.padding(top = 4.dp)
    )
}
```

- [ ] **Step 3: Build to verify compilation**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/dreslan/countdown/ui/list/CountdownListViewModel.kt \
       app/src/main/java/com/dreslan/countdown/ui/list/CountdownListScreen.kt
git commit -m "feat: show note count on list screen cards"
```

---

### Task 10: Full Build, Test, and Verify

- [ ] **Step 1: Run full test suite**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew :app:testDebugUnitTest`
Expected: All tests PASS

- [ ] **Step 2: Verify Paparazzi screenshots**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew verifyPaparazziDebug`
Expected: BUILD SUCCESSFUL (or record new goldens if screenshots changed)

If Paparazzi fails due to changed screenshots:
Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew recordPaparazziDebug`
Then verify the new golden images look correct.

- [ ] **Step 3: Build release APK**

Run: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit any remaining changes**

```bash
git add -A
git commit -m "chore: update golden screenshots for timeline redesign"
```
