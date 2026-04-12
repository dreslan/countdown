# Braveheart Timer — Design Spec

## Overview

A multi-countdown Android app with homescreen widgets. Users create countdowns to future events, each with an optional zero-state message and optional video embed. Two visual themes available per countdown. Widgets come in 4x1 (compact) and 4x2 (with video play button) sizes.

## Data Model

Each countdown record contains:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| id | Long (auto-generated) | Yes | Primary key |
| title | String | Yes | Display name (e.g., "Catheter Removal") |
| targetDateTime | Instant (stored as Long epoch millis via TypeConverter) | Yes | Target date and time |
| timeZone | String | Yes | IANA timezone (e.g., "America/New_York") |
| theme | Enum (CLEAN, MEDIEVAL) | Yes | Visual theme, defaults to CLEAN |
| zeroMessage | String? | No | Message shown at zero (e.g., "FREEDOM!") |
| videoUrl | String? | No | YouTube URL for embed |
| createdAt | Instant (stored as Long epoch millis via TypeConverter) | Yes | Creation timestamp |

Stored in a local Room database. No cloud sync. Room TypeConverters map `Instant` ↔ `Long` (epoch millis).

## Themes

### Clean & Minimal
- Background: dark gray gradient (`#0d1117` → `#161b22` → `#21262d`)
- Countdown text: `#f0f6fc`, thin weight, sans-serif
- Label text: `#8b949e` ("Freedom in" / title)
- Unit labels: `#484f58`
- Play button: white at 60% opacity on subtle circular background

### Medieval / Rustic
- Background: warm brown gradient (`#2d1b00` → `#4a2800` → `#6b3a00`)
- Countdown text: `#f0e6d0`, bold weight, serif (Georgia)
- Label text: `#d4a855` (gold)
- Unit labels: `#8a6d3b`
- Play button: gold at 70% opacity with gold border circle
- Subtle diagonal texture overlay at 5% opacity

## App Screens

### Countdown List
- Primary screen on app launch
- Shows all countdowns as cards, each displaying:
  - Title
  - Live countdown (`DD:HH:MM:SS`)
  - Theme-appropriate styling
- Completed countdowns show zero message (if set) or `00:00:00:00`
- FAB (floating action button) to create new countdown
- Tap a card to open detail view

### Create / Edit Countdown
- Form fields:
  - Title (text input, required)
  - Date picker + time picker (required)
  - Timezone selector (defaults to device timezone)
  - Theme toggle (Clean / Medieval)
  - Zero message (text input, optional, placeholder: e.g., "FREEDOM!")
  - Video URL (text input, optional, placeholder: "YouTube URL")
- Save and Cancel actions
- Validation: title required, date/time required

### Countdown Detail
- Full-screen countdown display matching the countdown's theme
- Large countdown numbers (`DD:HH:MM:SS`) with unit labels
- Title displayed above countdown
- If video URL is configured: embedded YouTube player via WebView below the countdown
  - Playable anytime (before, during, and after countdown completes)
- Edit button (opens edit screen)
- Delete button (with confirmation dialog)

### Zero State (countdown complete)
- Countdown numbers replaced with:
  - Custom zero message if set (e.g., "FREEDOM!")
  - `00:00:00:00` if no message set
- Video auto-play behavior (app-side only, widgets cannot trigger playback):
  - Auto-plays **only on the natural zero crossing** — when the detail screen is open and the countdown reaches zero in real time
  - Does **not** auto-play when navigating to a detail screen of an already-completed countdown
  - User can always manually play the video at any time
- Widget shows the zero message

## Widgets

### 4x1 Widget
- Compact countdown display
- Shows: title label above, `DD:HH:MM:SS`, unit labels below
- Themed background matching the countdown's theme setting
- Tap opens countdown detail screen in app
- No play button (not enough space)

### 4x2 Widget
- Full countdown display with play button
- Left side: title label, `DD:HH:MM:SS`, unit labels
- Right side: circular play button (only visible if video URL is configured)
- Themed background matching the countdown's theme setting
- Tap countdown area → opens detail screen
- Tap play button → opens detail screen and auto-plays video

### Widget Configuration
- When user adds a widget to homescreen, Android shows a configuration activity
- `CountdownWidgetConfigActivity` is a standalone `Activity` (not a Compose nav destination) declared in `AndroidManifest.xml` with `android.appwidget.action.APPWIDGET_CONFIGURE`
- Configuration activity shows a list of existing countdowns to choose from
- If no countdowns exist: shows "No countdowns yet" message with a "Create One" button that deep-links into the app's create screen. Widget placement is deferred until a countdown is selected.
- Selected countdown is bound to that widget instance
- If the bound countdown is deleted, widget shows "Countdown deleted — tap to reconfigure"

### Widget Updates
- Per-minute updates via `ACTION_TIME_TICK` BroadcastReceiver (system broadcast fires every minute). The receiver calls `GlanceAppWidget.updateAll()` to refresh all widget instances.
- WorkManager cannot achieve 1-minute intervals (15-minute minimum), so `ACTION_TIME_TICK` is the correct mechanism.
- On zero crossing: the tick handler detects completion and triggers an immediate widget update to show the zero state. No stale positive counts.
- Widget update is lightweight — reads countdown from Room, computes remaining time, re-renders.

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Widget Framework | Glance (Jetpack) |
| Database | Room |
| Video Playback | WebView (YouTube embed) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |
| Build System | Gradle (Kotlin DSL) |

## Project Structure

```
app/
  src/main/
    java/com/dreslan/countdown/
      data/
        Countdown.kt          # Entity
        CountdownDao.kt       # DAO
        CountdownDatabase.kt  # Room database
      ui/
        theme/
          Theme.kt            # Compose theme definitions
          Color.kt            # Color constants for both themes
          Type.kt             # Typography
        list/
          CountdownListScreen.kt
          CountdownListViewModel.kt
        detail/
          CountdownDetailScreen.kt
          CountdownDetailViewModel.kt
        edit/
          EditCountdownScreen.kt
          EditCountdownViewModel.kt
      widget/
        CountdownWidget.kt         # Glance widget (4x2)
        CountdownWidgetSmall.kt    # Glance widget (4x1)
        CountdownWidgetConfigActivity.kt  # Standalone config Activity (APPWIDGET_CONFIGURE)
        WidgetTickReceiver.kt             # ACTION_TIME_TICK receiver, calls updateAll()
      MainActivity.kt
      CountdownApp.kt              # Application class
    res/
      xml/
        countdown_widget_info.xml
        countdown_widget_small_info.xml
```

## Navigation

```
CountdownList → CreateCountdown
CountdownList → CountdownDetail → EditCountdown
Widget tap → CountdownDetail
Widget config (standalone Activity) → picker list → select countdown
Widget config (no countdowns) → "Create One" → app CreateCountdown → return to config
```

## Edge Cases

- **Countdown deleted while widget exists**: Widget shows "Countdown deleted — tap to reconfigure"
- **No countdowns exist**: List screen shows empty state with prompt to create first countdown
- **Invalid video URL**: Validate that the URL is a recognized YouTube format (`youtube.com/watch?v=`, `youtu.be/`, `youtube.com/shorts/`). Normalize to `youtube.com/embed/{videoId}` for WebView. Show error toast on save if URL doesn't match any recognized format; don't persist invalid URL.
- **Timezone changes**: Countdown uses stored IANA timezone, unaffected by device timezone changes
- **Past target dates**: Intentionally allowed. Users may want to keep completed countdowns around. Detail screen and widget show zero state immediately.
- **Device reboot**: Glance widgets survive reboots via AppWidgetProvider. `ACTION_TIME_TICK` receiver re-registers on boot via `BOOT_COMPLETED` receiver.
