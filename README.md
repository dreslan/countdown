# Braveheart Timer

A multi-countdown Android app with themed homescreen widgets. Create countdowns to events, each with an optional zero-state message, YouTube video link, background image, and progress bar.

## Features

- **Multiple countdowns** — create, edit, delete countdowns with custom titles, descriptions, and target dates
- **Two themes** — Clean (dark/minimal) and Medieval (warm browns/gold/serif)
- **Homescreen widget** — 4x1 widget showing countdown name, coarse time remaining (days/hours/minutes), and target date
- **Background images** — pick an image from your gallery for the widget and in-app display
- **YouTube integration** — link a YouTube video to any countdown, play it from the widget or detail screen
- **Progress bar** — optional visual progress from start date to target date
- **Timeline notes** — attach dated notes to any countdown, displayed as a chronological feed
- **Export** — share countdowns as Markdown or JSON
- **Tap to refresh** — widget refresh button updates the countdown instantly

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Widgets:** Glance (Jetpack)
- **Database:** Room
- **Background updates:** WorkManager (15-minute periodic refresh)
- **Screenshot testing:** Paparazzi
- **Build:** Gradle (Kotlin DSL)
- **Min SDK:** 26 (Android 8.0) · **Target SDK:** 35

## Quick Start

### Prerequisites

- Java 17 (`brew install openjdk@17`)
- Android SDK (platform 35, build-tools 34)
- [uv](https://docs.astral.sh/uv/) (for the deploy CLI)
- `adb` (`brew install android-platform-tools`)

### Build & Deploy

```bash
# Build + install on device (handles pairing/connecting automatically)
uv run deploy.py push

# Build + install with clean build
uv run deploy.py push --clean

# Build APK only
uv run deploy.py build

# Run tests
uv run deploy.py test

# Manual connection (if needed)
uv run deploy.py pair           # pair with a new device
uv run deploy.py connect        # connect to a paired device
uv run deploy.py devices        # list connected devices
```

`push` is all-in-one: if no device is connected it walks you through wireless debugging setup, if multiple devices are connected it lets you pick one, then builds and installs.

### Docker

```bash
docker compose up -d
docker compose exec dev ./gradlew assembleDebug
```

## Project Structure

```
app/src/main/java/com/dreslan/countdown/
├── data/           # Room entity, DAO, database, YouTube URL utils
├── ui/
│   ├── theme/      # Clean & Medieval color schemes, typography
│   ├── components/ # Shared CountdownDisplay composable
│   ├── list/       # Countdown list screen
│   ├── edit/       # Create/edit countdown screen
│   └── detail/     # Countdown detail screen with video
├── widget/         # Glance widget, config activity, refresh/play actions
├── CountdownApp.kt # Application class with WorkManager registration
├── CountdownCalc.kt # Countdown time calculation
└── MainActivity.kt # Single-activity with Compose Navigation
```
