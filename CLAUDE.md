# CLAUDE.md

## Project Overview

Mini Video Journal — multi-module Android app. Kotlin, Jetpack Compose (Material 3), Koin DI, SQLDelight database, CameraX recording, Media3 ExoPlayer playback, Navigation 3, Coroutines/Flow. Package: `co.ynd.interview.tomek`. Min SDK 26, compile/target SDK 36, JVM 17.

## Build & Test Commands

```bash
./gradlew assembleDebug                      # Build debug APK
./gradlew testDebug                          # All local (JVM) unit tests
./gradlew :feature-feed:testDebug            # Single module's unit tests
./gradlew connectedCheck                     # All instrumented tests (needs emulator)
```

Dependencies are managed in `gradle/libs.versions.toml` (version catalog).

## Module Dependency Graph

```
app ──→ feature-feed ──→ core-domain ← core-data ──→ core-database
│           │                              │
│           ↓                              ↓
│       core-ui                    SQLDelight (VideoEntryQueries)
│
├──→ feature-camera ──→ core-domain
│           │
│           ↓
│       core-ui
│
├──→ core-navigation              (NavKeys: FeedDestination, CameraDestination)
├──→ core-data, core-database     (Koin module wiring)
└──→ core-ui

feature-feed  ──→ core-navigation
feature-camera ──→ core-navigation
```

- **`:app`** — Application entry point, `VideoJournalApp` (Koin init), `MainActivity`, `MainNavigation`. Wires all Koin modules and feature entry providers into `NavDisplay`.
- **`:core-domain`** — Pure Kotlin/JVM module (no Android deps). Domain model (`VideoEntry`), repository interface, use cases (`GetVideoEntriesUseCase`, `SaveVideoEntryUseCase`, `DeleteVideoEntryUseCase`), `VideoFileCleaner` interface, Koin `domainModule`.
- **`:core-data`** — `DefaultVideoEntryRepository` backed by SQLDelight queries, `FakeVideoEntryRepository` for tests, `FileSystemVideoFileCleaner`, mapper, Koin `dataModule`.
- **`:core-database`** — SQLDelight `VideoJournalDatabase`, `VideoEntry.sq` schema, `DatabaseDriverFactory`, Koin `databaseModule`.
- **`:core-navigation`** — `FeedDestination` and `CameraDestination` NavKeys only. No Android logic.
- **`:core-ui`** — Compose theme, shared components (`VideoPlayer`, `VideoThumbnail`, `LoadingIndicator`, `ErrorMessage`, `PermissionRationaleDialog`), utility functions.
- **`:core-testing`** — `KoinTestRunner`, `testDataModule` that swaps in `FakeVideoEntryRepository` and a no-op `VideoFileCleaner`.
- **`:feature-feed`** — Feed screen with `LazyColumn` of video cards, inline playback, share/delete actions. `FeedViewModel` + Koin `feedModule`.
- **`:feature-camera`** — CameraX recording screen, review/describe flow, permission handling. `CameraViewModel` owns all recording state via `VideoRecorder`/`VideoFileOperations` abstractions. Koin `cameraModule`.
- **`:test-app`** — Instrumented test module targeting `:app`.

## Architecture Patterns

- **Navigation 3**: `NavDisplay` + `NavBackStack` + `EntryProvider` pattern. Navigation keys are `@Serializable data object`s implementing `NavKey`.
- **DI**: Koin modules in each layer. `viewModel { }` DSL for ViewModels, `koinViewModel()` in Compose. No Hilt.
- **Repository pattern**: Interface in `:core-domain`, default implementation in `:core-data` backed by SQLDelight, fake in `:core-data` for tests.
- **ViewModel**: Constructor-injected use cases via Koin. Exposes `StateFlow<UiState>` using `stateIn(WhileSubscribed(5000))`.
- **Domain layer**: Explicit use case classes in `:core-domain` (pure Kotlin, no Android).

## AGP 9.x Notes

- No `kotlin-android` plugin — AGP 9.x bundles Kotlin support.
- No `kotlin { compilerOptions }` blocks in Android modules — JVM target set via `android { compileOptions }`.
- SQLDelight must be 2.3.x+ for AGP 9.x compatibility.

## Definition of Done

A change is complete when all of the following pass:

```bash
./gradlew assembleDebug          # must compile with zero errors
./gradlew testDebug              # all JVM unit tests green
```

For changes that touch UI or navigation, also verify manually on a device or emulator — type checking alone does not confirm feature correctness.

Do not mark a task done if:
- Any existing test is deleted or disabled to make the suite pass.
- A new public API or use case has no accompanying unit test.
- The module dependency graph has gained a new edge not reflected in this file.

## Testing Strategy

| Layer | What to test | How |
|-------|-------------|-----|
| `:core-domain` use cases | Business logic, edge cases | JVM unit tests, no Android deps |
| `:core-data` repository | Correct mapping, delegation to queries | Unit tests with `FakeVideoEntryRepository` |
| Feature ViewModels | State transitions, error/loading states | JVM unit tests; inject fakes via Koin `testDataModule` |
| UI / Compose screens | Golden-path flow | Instrumented tests in `:test-app` using `connectedCheck` |

**Rules:**
- Use `FakeVideoEntryRepository` (`:core-data`) — never mock the SQLDelight queries or the database driver.
- `:core-testing` provides `KoinTestRunner` and `testDataModule`; always use them for ViewModel tests instead of constructing Koin manually.
- `:core-domain` is pure JVM — tests there must not import any `android.*` class.
- Do not write tests for `@Composable` layout details (pixel positions, colors); test behavior and state.

## Do Not Generate

- **Hilt / Dagger** — DI is Koin. Never add `@HiltViewModel`, `@Inject`, or any Hilt/Dagger annotation.
- **ViewModel factory boilerplate** — Koin handles instantiation; no `ViewModelProvider.Factory` needed.
- **`kotlin-android` plugin declarations** — AGP 9.x provides this automatically.
- **`kotlin { compilerOptions { } }` blocks in Android modules** — use `android { compileOptions { } }` instead.
- **New modules** without updating the dependency graph in this file.
- **Raw SQLite / Room** — persistence is SQLDelight only.
- **`LiveData`** — use `StateFlow` / `Flow` throughout.
- **XML layouts** — UI is 100% Jetpack Compose.
- **Comments that describe what the code does** — only add a comment when the *why* is non-obvious (hidden constraint, workaround, subtle invariant).
