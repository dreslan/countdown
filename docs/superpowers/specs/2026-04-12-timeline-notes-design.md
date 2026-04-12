# Timeline Notes & Detail Screen Redesign

## Summary

Add journal-style notes to countdowns, displayed as a timeline on the detail screen. Redesign the detail screen's video button as a retro TV, upgrade the progress bar to a timeline with note dots, and bump widget button sizes.

## Detail Screen Layout (top to bottom)

1. **Title** — left-aligned (unchanged)
2. **Target date** — centered below title (unchanged)
3. **Countdown display** — centered (unchanged)
4. **Timeline progress bar**
   - 6dp thick bar with rounded ends
   - Labels: start date (left), percentage (center), target date (right)
   - Note dots plotted at chronological position between start and target
   - Colors: theme-aware (Clean: `#8B949E`, Medieval: `#D4A855`)
5. **Note feed**
   - Vertical list with left border line (2dp), newest first
   - Each note: colored dot on the border line, date label, text
   - Tapping a note opens edit dialog (text + date editable, delete option)
6. **Retro TV video button** (only when `videoUrl` is set)
   - Old-school TV shape: rabbit-ear antennas with ball tips, rounded-rect body with border, screen area with static/scanline effect, two control knobs below screen, pedestal feet
   - Compact: ~120dp tall total (including antennas and feet), centered horizontally
   - Tapping opens YouTube via Intent (same behavior as current)
   - Static effect via layered drawable or Canvas draw (horizontal scanlines + noise pattern)
7. **FAB** — "+" button anchored bottom-right to add a new note

## Widget Changes

- Play button: hit target 44dp → 56dp, icon font 26sp → 32sp
- Refresh button: hit target 28dp → 40dp, icon font 16sp → 22sp

## List Screen Cards

- When `showProgress` is on and notes exist: show progress bar + note count text (e.g., "3 notes") beside or below the bar
- No other changes to card layout

## Data Model

### New Entity: `Note`

```kotlin
@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = Countdown::class,
        parentColumns = ["id"],
        childColumns = ["countdownId"],
        onDelete = ForeignKey.CASCADE
    )]
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

- `createdAt` determines position on timeline bar
- `updatedAt` tracks edits
- `CASCADE` delete: notes are removed when their countdown is deleted

### New DAO: `NoteDao`

- `getNotesForCountdown(countdownId: Long): Flow<List<Note>>` — ordered by `createdAt DESC`
- `getNoteCountForCountdown(countdownId: Long): Flow<Int>` — for list screen badge
- `insert(note: Note): Long`
- `update(note: Note)`
- `deleteById(id: Long)`

### Migration v4 → v5

```sql
CREATE TABLE notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    countdownId INTEGER NOT NULL,
    text TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY (countdownId) REFERENCES countdowns(id) ON DELETE CASCADE
)
```

## Add/Edit Note UX

- **Add**: FAB opens dialog with text field, stamped to now. Save inserts note.
- **Edit**: Tap note in feed → same dialog, pre-filled. Date is editable (for backdating corrections). Delete option available.
- Dialog fields: text (multiline), date (date picker), time (time picker)

## Export

- Menu option in detail screen top bar (alongside edit/delete actions)
- Two formats: Markdown and JSON
- Uses Android share sheet (`Intent.ACTION_SEND`) so user can send to any app

### Markdown format

```markdown
# FREEDOM!
**Target:** April 22, 2026 12:30 PM
**Started:** March 30, 2026 1:30 PM
**Progress:** 43%

## Notes
- **Apr 8:** Trouble sleeping, took pain meds but incision looks good
- **Apr 4:** Some mild pain today but nothing too bad
- **Mar 31:** Day 1 post-op, feeling groggy but okay
```

### JSON format

```json
{
  "title": "FREEDOM!",
  "targetDateTime": "2026-04-22T12:30:00-04:00",
  "startDate": "2026-03-30T13:30:00-04:00",
  "createdAt": "2026-03-30T13:30:00-04:00",
  "theme": "MEDIEVAL",
  "notes": [
    {
      "text": "Trouble sleeping, took pain meds but incision looks good",
      "createdAt": "2026-04-08T21:30:00-04:00"
    }
  ]
}
```

## Scope Boundaries

- No changes to edit screen (beyond what was already shipped for start date/time)
- No changes to widget layout beyond button sizing
- No animated static on the TV (static is a static drawable/canvas pattern, not a running animation)
- Export does not include background image — text data only
