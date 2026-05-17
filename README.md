# Mini Video Journal

[![CI](https://github.com/tomekdz/video-journal/actions/workflows/ci.yml/badge.svg)](https://github.com/tomekdz/video-journal/actions/workflows/ci.yml)

A video journaling Android app built as a take-home assignment. Record short clips, add a description, and browse them in a scrollable feed.

## Features

- **Record** short video clips with the device camera (rear or front)
- **Review & describe** before saving — discard or add an optional text note
- **Feed** of recorded clips, latest first, in a continuous vertical scroll (LazyColumn with card-per-entry layout)
- **Inline playback** — tap a card to play via ExoPlayer, tap again to stop
- **Thumbnail previews** — first frame extracted via Coil's `VideoFrameDecoder`
- **Share** videos to other apps via system share sheet
- **Delete** entries directly from the feed
- **Animations** — card insert/remove, crossfade thumbnail↔player, pulsating record button

## Tech Stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose (Material 3) |
| Navigation | Navigation 3 |
| DI | Koin 4 |
| Database | SQLDelight 2 |
| Camera | CameraX 1.4 |
| Playback | Media3 ExoPlayer |
| Thumbnails | Coil 3 (VideoFrameDecoder) |
| Async | Kotlin Coroutines + Flow |

## Architecture

Multi-module clean architecture:

```
:app
 ├── :feature-feed        → :core-domain, :core-ui
 ├── :feature-camera      → :core-domain, :core-ui
 ├── :core-domain         (pure Kotlin/JVM — model, repository interface, use cases)
 ├── :core-data           → :core-domain, :core-database
 ├── :core-database       (SQLDelight)
 ├── :core-ui             (Compose theme + shared components)
 └── :core-testing        (KoinTestRunner, FakeVideoEntryRepository)
```

`:core-domain` has zero Android dependencies. Feature modules depend on `:core-domain`, never directly on `:core-data`. Only `:app` wires all Koin modules together.

## Build & Run

**Requirements:** JDK 17, Android SDK 36.

```bash
git clone https://github.com/tomekdz/video-journal.git
cd video-journal
./gradlew assembleDebug      # build debug APK
./gradlew testDebug          # run unit tests
./gradlew connectedCheck     # run instrumented tests (needs emulator/device)
```

Install on a connected device:

```bash
./gradlew installDebug
```

## CI

GitHub Actions runs on every push and pull request to `main`. The pipeline builds a debug APK and runs all JVM unit tests. Test reports are uploaded as artifacts on every run (including failures). Instrumented tests are not in CI — they require a connected device and are run locally via `./gradlew connectedCheck`.

## Permissions

- `CAMERA` — video recording
- `RECORD_AUDIO` — audio capture

No storage permissions are requested. Videos are written to the app's internal storage (`context.filesDir`), which requires no permission on any supported API level.

## Notes

### What I found interesting

Navigation 3's `EntryProvider` composition model is a meaningful step forward over the older `NavHost` approach — having each feature module declare its own `EntryProviderScope` extension keeps nav logic co-located with the feature without leaking destination logic into `:app`. SQLDelight's compile-time query verification was also satisfying to work with: schema changes immediately surface as type errors in the data layer, which makes refactoring noticeably safer than runtime-checked alternatives.

### Challenges

AGP 9.x drops the `kotlin-android` plugin and bundles Kotlin support directly, which required adjusting the module setup to distinguish `:core-domain` (pure JVM, `java-library`) from Android library modules — easy to get wrong silently. `stateIn(WhileSubscribed(...))` in unit tests requires an active collector to start the upstream; tests that read `.value` directly without subscribing always see the initial `Loading` state. Fixed by launching a no-op collector with `UnconfinedTestDispatcher` before asserting, but the failure mode is subtle enough that it's worth calling out.

### What I'd improve with more time

- Add a confirmation dialog before deleting an entry, and clean up orphaned video files when the user navigates back mid-recording without saving — currently those files accumulate silently.
- Expand instrumented test coverage to cover the full record → describe → save → feed golden path in `:test-app`, and add SQLDelight integration tests against a real in-memory driver (currently the repository layer is tested only via the fake).
- Introduce pagination or a `PagingSource` for the feed — the current `LazyColumn` loads all entries at once, which is fine for a journal but would degrade on large datasets.
- Add playback progress indicators and a seek bar to the inline player for a more complete media experience.
