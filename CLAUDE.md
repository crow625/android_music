# CLAUDE.md

Working guide for the **AndroidMusic** player. Authoritative detail lives in
[`feature-spec.md`](feature-spec.md) (what it does) and
[`technical-spec.md`](technical-spec.md) (how it's built). This file is the
short list of rules that are easy to get wrong — read it before changing code.

## What this is

A local-first Android music player (no network, no accounts) that plays audio
from user-selected folders. Kotlin · Jetpack Compose · Media3/ExoPlayer · Room ·
DataStore · Hilt · Coroutines/Flow. Min SDK 26, target 35.

## Two non-negotiable principles

1. **Clean interface seams at every external-API boundary.** ExoPlayer, the
   audio system, Room, DataStore, MediaStore/SAF, WorkManager, system time, and
   even Compose are each reached only through an interface with a test fake.
   Core logic runs on a plain JVM, independent of the UI and framework.
2. **No error is ever swallowed.** Every `catch` either handles a documented
   expected condition or calls `DiagnosticReporter.report`. Genuine errors are
   surfaced to the user (Snackbar/notification) **and** persisted. A detekt rule
   flags empty/swallowing catches.

## Module & layering rules

- `:domain` is a **pure-Kotlin module — no Android imports, ever.** The build
  enforces this. If you reach for `android.*` in `:domain`, you're in the wrong
  layer. URIs in the domain are `MediaUri` (a `value class` over `String`), not
  `android.net.Uri`; convert at the `data/`/`player/` boundary.
- `:app` packages: `ui/`, `data/`, `player/`, `stats/`, `diagnostics/`.
  Dependency rule: `ui/ → domain/ ← data/ , player/ , stats/ , diagnostics/`.
  Never import from a layer above you.
- `player/` is the **only** place allowed to touch ExoPlayer or `android.media.*`.

## Key seams (don't bypass them)

| Concern | Interface | Notes |
| ------- | --------- | ----- |
| Player (service) | `AudioPlayer` | wraps ExoPlayer, lives in `MediaLibraryService` |
| Player (client/UI) | `PlaybackController` | MediaController-backed; has `connection` state. **ViewModels use this, never `AudioPlayer`.** |
| Audio focus | `AudioFocusManager` + pure `FocusPolicy` | policy is unit-tested |
| Bluetooth | `BluetoothMonitor` | |
| Tag reading | `MetadataReader` | wraps `MediaMetadataRetriever` |
| Time | `Clock` | carries `ZoneId`; never call `Instant.now()` directly |
| Dispatchers | `DispatcherProvider` | inject, don't hardcode `Dispatchers.IO` |
| Logging | `Logger` | never call `android.util.Log` directly |
| Errors | `DiagnosticReporter` | |
| Settings | `SettingsRepository` | DataStore |
| Scan scheduling | `ScanScheduler` | WorkManager |

## UI conventions

- Screens are **stateless composables**: `fun XScreen(state: XUiState, onEvent: (XEvent) -> Unit)`.
  A thin `XRoute` wrapper binds the ViewModel. Composables hold no business logic
  and no fake data. Domain → `UiState` mapping is in the ViewModel/pure mapper
  and is unit-tested without Compose.
- **Previews + fake `UiState` live only in the dedicated debug source set**, never
  alongside production composables. Production code never imports preview/fake code.
- **Roborazzi reuses `@Preview` functions as goldens** — one `@Preview` is both the
  IDE/demo reference and the regression test. Cover empty/loading/error/edge +
  light/dark. Commit goldens.
- `PlayerViewModel` is nav-graph-scoped and shared, so mini-player and Now Playing
  observe the same state.

## Domain decisions to respect

- **Shuffle is owned by `PlayQueue`**, not ExoPlayer (`shuffleModeEnabled` stays
  off). Toggling off restores `originalOrder` and keeps the current track current.
- **Stable track ID**: embedded MusicBrainz Recording ID if present, else
  `SHA-1(lower(title)+§+lower(artist)+§+lower(album)+§+durationBucket)` where
  `durationBucket` = duration rounded to nearest 2s. Untagged genuine-duplicate
  collisions are a documented, accepted v1 limitation.
- **Playlist resolution** orders matches `BY id ASC` for determinism (metadata
  match → path fallback → unresolvable).
- **Stats bucket in local timezone** via `Clock.zone`; DAOs use SQLite `'localtime'`.

## Diagnostics

On-device only (no network reporter). Errors go to the `diagnostic_event` Room
table **and** a rotating exportable log file in `filesDir/logs/`. Uncaught
exceptions are captured and surfaced on next launch. A user-visible Diagnostics
screen (under Settings) lists recent errors + the last `ScanReport` and can
share the log. Debug builds enable StrictMode and LeakCanary.

## Testing

- **JVM unit tests** (`:domain` + ViewModels/mappers): use cases with fakes,
  `StateFlow` via Turbine, `PlayQueue`/shuffle/skip-previous/repeat logic,
  `FocusPolicy`, stable-ID, playlist resolution, `StatPeriod` boundaries (faked
  `Clock`), and assertions that failure paths report diagnostics.
- **Roborazzi goldens** for every significant composable (from its previews).
- **Instrumented tests** (emulator): Room DAOs (incl. `localtime` aggregation),
  `MediaStoreAudioRepository`/`MetadataReader`, and an `ExoAudioPlayer` smoke test.

Prefer writing the JVM test alongside the domain/use-case code (test-first where
practical). Don't push logic into the player/UI layers just to avoid a seam.

## Build order

Phase 0 scaffold (modules, version catalog, Hilt, Roborazzi harness, diagnostics
seams, StrictMode/LeakCanary/detekt) → 1 pure domain → 2 data → 3 player →
4 UI slices → 5 cross-cutting (notification, headset, Bluetooth resume, periodic
scan, last-queue restore). See `technical-spec.md` §14.
