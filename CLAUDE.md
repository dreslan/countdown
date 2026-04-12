# Braveheart Timer — Claude Instructions

## Build & Deploy

```bash
uv run deploy.py push         # build + install (auto-pairs/connects if needed)
uv run deploy.py push --clean # clean build + install
uv run deploy.py build        # build APK only
```

Or directly: `JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew assembleDebug`

## Test

```bash
JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew :app:testDebugUnitTest
```

Paparazzi screenshot tests:
```bash
JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew recordPaparazziDebug   # record golden screenshots
JAVA_HOME=/usr/local/opt/openjdk@17 ./gradlew verifyPaparazziDebug   # verify against goldens
```

## Architecture

- Single-activity app with Compose Navigation (list → detail → edit)
- Room database with TypeConverters for Instant and CountdownTheme
- Glance widget (4x1) with WorkManager periodic updates (15 min)
- Widget actions via ActionCallback (RefreshAction, PlayVideoAction)

## Key Files

- `data/Countdown.kt` — Room entity + CountdownTheme enum
- `data/CountdownDatabase.kt` — Room DB with migrations (currently v3)
- `widget/CountdownWidgetSmall.kt` — Glance widget rendering
- `ui/theme/Color.kt` — CleanColors and MedievalColors constants
- `deploy.py` — CLI tool for build/test/deploy (run with `uv run`)

## Conventions

- Java 17 required (system Java may be newer, always set JAVA_HOME)
- Package: `com.dreslan.countdown`
- Two themes: CLEAN (dark/minimal/sans-serif) and MEDIEVAL (warm brown/gold/serif)
- Background images stored as files in app internal storage, paths in Room
- YouTube URLs normalized to embed format, opened via Intent to YouTube app
