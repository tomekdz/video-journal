# Mini Video Journal

A video journaling Android app built as a take-home assignment. Record short clips, add a description, and browse them in a scrollable feed.

## Features

- **Record** short video clips with the device camera (rear or front)
- **Review & describe** before saving — discard or add an optional text note
- **Feed** of recorded clips, latest first, with thumbnail previews
- **Inline playback** — tap a card to play via ExoPlayer, tap again to stop
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
./gradlew assembleDebug      # build debug APK
./gradlew testDebug          # run unit tests
./gradlew connectedCheck     # run instrumented tests (needs emulator/device)
```

Install on a connected device:

```bash
./gradlew installDebug
```

## Permissions

- `CAMERA` — video recording
- `RECORD_AUDIO` — audio capture

No storage permissions are requested. Videos are written to the app's internal storage (`context.filesDir`), which requires no permission on any supported API level.

## Notes

### What I found interesting
Navigation 3's `EntryProvider` composition model is a nice step forward — having each feature module declare its own `EntryProviderScope` extension keeps nav logic co-located with the feature without leaking into `:app`.

### Challenges
- AGP 9.x drops the `kotlin-android` plugin and bundles Kotlin support directly, which required adjusting the module setup for `:core-domain` (pure JVM) vs Android library modules.
- `stateIn(WhileSubscribed(...))` in unit tests requires an active collector to start the upstream — tests that read `.value` directly without subscribing will always see the initial `Loading` state. Fixed by launching a collector with `UnconfinedTestDispatcher` before asserting.

### What I'd improve with more time
- Replace `AndroidView(PreviewView)` with the `camera-viewfinder-compose` `Viewfinder` composable for a fully Compose-native camera surface (no `AndroidView` wrapper needed, no z-ordering workaround).
- Add a confirmation dialog before deleting a video entry.
- Clean up orphaned video files when the user navigates back mid-recording.
- Offline-first sync if a remote backend were added.
