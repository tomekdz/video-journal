# AGENTS.md

Mini Video Journal — multi-module Android app.
**Stack:** Kotlin, Jetpack Compose (Material 3), Koin DI, SQLDelight, CameraX, Media3 ExoPlayer, Navigation 3, Coroutines/Flow.
**Package:** `co.ynd.interview.tomek` | Min SDK 26 | Compile/target SDK 36 | JVM 17

---

## Build & Verify

```bash
./gradlew assembleDebug          # compile check
./gradlew testDebug              # all JVM unit tests
./gradlew :feature-feed:testDebug   # single-module tests
./gradlew connectedCheck         # instrumented tests (requires running emulator)
```

Dependencies are declared in `gradle/libs.versions.toml` (Gradle version catalog).

---

## Module Map

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
├──→ feature-feed-navigation      (NavKey only)
├──→ feature-camera-navigation    (NavKey only)
├──→ core-data, core-database     (Koin module wiring)
└──→ core-ui
```

| Module | Role |
|--------|------|
| `:app` | Entry point, Koin init, `MainActivity`, `MainNavigation` |
| `:core-domain` | Pure JVM — `VideoEntry` model, repository interface, use cases |
| `:core-data` | `DefaultVideoEntryRepository` (SQLDelight), `FakeVideoEntryRepository` (tests) |
| `:core-database` | SQLDelight schema, `DatabaseDriverFactory`, `databaseModule` |
| `:core-ui` | Compose theme, shared composables (`VideoPlayer`, `VideoThumbnail`, etc.) |
| `:core-testing` | `KoinTestRunner`, `testDataModule` (swaps in the fake repository) |
| `:feature-feed` | Feed screen, `FeedViewModel`, inline playback, share/delete |
| `:feature-camera` | CameraX recording, review/describe flow, permission handling |
| `:feature-*-navigation` | NavKey `data object` only — no logic |
| `:test-app` | Instrumented end-to-end tests targeting `:app` |

---

## Architecture Rules

- **Navigation 3**: `NavDisplay` + `NavBackStack` + `EntryProvider`. NavKeys are `@Serializable data object`s implementing `NavKey`.
- **DI**: Koin only. Use `viewModel { }` DSL; inject in Compose with `koinViewModel()`.
- **Repository pattern**: interface in `:core-domain`, implementation in `:core-data`, fake in `:core-data`.
- **ViewModel**: use cases injected via Koin; state exposed as `StateFlow<UiState>` with `stateIn(WhileSubscribed(5000))`.
- **Domain layer**: `:core-domain` is pure Kotlin/JVM — zero Android imports allowed.

---

## Definition of Done

A change is complete when:

1. `./gradlew assembleDebug` compiles with zero errors.
2. `./gradlew testDebug` is fully green — no tests deleted or skipped to pass.
3. Every new public use case or repository method has a unit test.
4. The module dependency graph above is updated if a new inter-module edge was added.
5. UI changes are manually verified on a device or emulator.

---

## Testing Strategy

| Layer | Approach |
|-------|----------|
| `:core-domain` use cases | JVM unit tests only; no Android deps |
| `:core-data` repository | Unit tests using `FakeVideoEntryRepository` |
| Feature ViewModels | JVM unit tests; use `testDataModule` from `:core-testing` |
| Compose UI / navigation | Instrumented tests in `:test-app` |

- Never mock SQLDelight queries or the database driver — use `FakeVideoEntryRepository`.
- Use `KoinTestRunner` from `:core-testing` for ViewModel tests.
- Do not test layout details (colors, padding); test state and behavior.

---

## Do Not Generate

| Prohibited | Reason |
|------------|--------|
| Hilt / Dagger (`@HiltViewModel`, `@Inject`) | DI is Koin |
| `ViewModelProvider.Factory` | Koin handles ViewModel instantiation |
| `kotlin-android` plugin declarations | AGP 9.x provides it automatically |
| `kotlin { compilerOptions { } }` in Android modules | Use `android { compileOptions { } }` |
| Room / raw SQLite | Persistence is SQLDelight only |
| `LiveData` | Use `StateFlow` / `Flow` |
| XML layouts | UI is 100% Jetpack Compose |
| New modules without updating the dependency graph | Keeps the map accurate |
| Descriptive code comments | Only comment non-obvious *why*, never *what* |
