# Implementation Plan

**Android Music Player · phased delivery roadmap**

This document refines [`technical-spec.md`](technical-spec.md) §14 into an
execution plan. It is the working reference for sequencing the build.

## How phases are structured

Every phase produces a **deliverable of one of two kinds**:

- **🧠 Logical Core** — a significant, self-contained chunk of logic that lands
  with its full unit-test suite. Not yet visible in the app, but provably correct
  and the foundation everything else builds on.
- **📱 Integrated Feature** — a vertical slice cutting through data → player → UI
  that you can run and exercise in the app by hand, on top of its automated tests.

Phases 0–1 are foundational (skeleton + the pure brain). Phases 2–7 are vertical
feature slices: each one is demoable in the running app and leaves the app in a
shippable-if-incomplete state. Order can flex, but each phase assumes the ones
before it.

Each phase below lists its **Goal**, **Deliverable**, **Scope**, **Tests**, and a
**Definition of Done** (the concrete check that says the phase is finished).

---

## Phase 0 — Foundation & Test Harness

**Goal:** A buildable, runnable skeleton with the entire quality/observability
toolchain proven end-to-end before any feature code exists.

**Deliverable:** 📱 *(infra)* The app launches to a placeholder screen; CI runs a
passing unit test and a passing screenshot golden; lint and leak/strict tooling
are active.

**Scope**
- Gradle with version catalog (`libs.versions.toml`); `:domain` **pure-Kotlin**
  module + `:app` Android module; Hilt wired.
- Single-Activity shell: `NavHost` with one placeholder destination, base theme.
- Cross-cutting seams implemented first: `Logger` (Timber), `DiagnosticReporter`
  (`diagnostic_event` Room table + rotating log file in `filesDir/logs/`),
  uncaught-exception crash handler, `Clock`, `DispatcherProvider`.
- Debug-only StrictMode + LeakCanary; detekt with the **no-silent-catch** rule.
- Roborazzi wired and proven on one trivial composable golden.

**Tests**
- One JVM unit test (e.g. `FakeLogger` + a `DiagnosticReporter` round-trip).
- One Roborazzi golden on the placeholder composable.
- detekt and the build are green.

**Definition of Done**
- `./gradlew :app:assembleDebug` produces an APK that launches to the placeholder.
- `:domain` compiles as a pure-Kotlin module (zero Android dependencies).
- Unit test + golden + detekt all pass locally and in CI.

---

## Phase 1 — Domain Core

**Goal:** The complete decision-making "brain" of the app as pure Kotlin, fully
tested, with zero Android dependencies.

**Deliverable:** 🧠 *Logical Core* — all domain logic and interfaces, every path
covered by fast JVM tests.

**Scope**
- All domain models incl. `MediaUri`, `PlayQueue` (with `originalOrder`),
  `RepeatMode`, diagnostics models, stats models, `QueueSource`.
- `PlayQueue` operations: shuffle + restore (current track preserved),
  skip-previous 3-second rule, repeat-mode transitions, add-to-next / add-to-end,
  clear-queue-keeps-current.
- `BuildQueueUseCase` (one branch per `QueueSource`).
- Stable-ID strategy: MusicBrainz Recording ID → SHA-1 fallback with duration
  bucketing; metadata normalization helpers.
- `FocusPolicy` (focus event → action).
- `StatPeriod` → (from, to) bounds, timezone-aware via `Clock`.
- All repository/player/infrastructure **interfaces** + their test fakes
  (`FakePlaybackController`, `FakeAudioPlayer`, `FakeAudioFileRepository`,
  `FakeLogger`, …). No implementations yet.

**Tests**
- Exhaustive JVM unit tests for every operation above (see spec §12.1), incl.
  shuffle/restore, skip-previous, repeat edges, stable-ID determinism &
  collisions, `FocusPolicy` per transition, `StatPeriod` month/midnight boundaries.

**Definition of Done**
- Every domain logic path is exercised by a passing JVM test; suite runs in
  seconds; `:domain` still has no Android imports.

---

## Phase 2 — Library Ingest & Browse

**Goal:** Point the app at folders, scan them, and browse your tracks.

**Deliverable:** 📱 *Integrated Feature* — add/remove source folders and see your
music appear as an All-Tracks list, with scan results explained.

**Scope**
- SAF folder picker; `takePersistableUriPermission`; `MediaUri ↔ android.net.Uri`
  mappers.
- `MetadataReader` (wraps `MediaMetadataRetriever`); `track` + `source_folder`
  Room tables + DAOs.
- `MediaStoreAudioRepository`: recursive scan via `DocumentFile`, upsert with
  stable ID, soft-delete stale tracks, produce a `ScanReport`.
- Minimal app scaffold (bottom-nav frame) + **All Tracks** (Library) screen and
  **Folder Sources** screen. `observeLibrary()` drives the list.
- Scan failures/problem files reported via diagnostics and shown in a scan summary.

**Tests**
- In-memory Room DAO tests (track/source queries).
- JVM tests for scan/dedup/soft-delete logic and `ScanReport` tallying; mapper tests.
- Library + Sources ViewModel tests (Turbine).
- Roborazzi goldens: Library (empty / loading / loaded / error), Folder Sources.

**Definition of Done (demo)**
- Launch → add a folder via SAF → tracks appear in All Tracks → scan summary
  shows counts/problems → remove the folder → tracks disappear (files on disk
  untouched). Survives app restart (persisted permission + Room).

---

## Phase 3 — Playback Engine & Now Playing

**Goal:** Tap a track and hear it, with full transport controls and well-behaved
background/system audio.

**Deliverable:** 📱 *Integrated Feature* — real gapless playback through the
service, mini-player, Now Playing, and an editable queue.

> This is the largest phase. It may be split into **3a** (engine: service,
> play/pause/skip/seek, notification, audio focus — minimal UI) and **3b**
> (Now Playing + Queue UI, shuffle/repeat) if a smaller increment is preferred.

**Scope**
- `MediaLibraryService` + `ExoAudioPlayer` (`AudioPlayer`); `MediaController`-backed
  `PlaybackController` with `connection` state; Hilt wiring.
- `PlayFromSourceUseCase` / `AddToQueueUseCase` driving the controller; gapless via
  `setMediaItems()` on the whole queue; shuffle resolved in the domain.
- Foreground service + media-style notification + `MediaSession`.
- `AudioFocusManager` applying `FocusPolicy` (pause/duck/resume).
- UI: persistent mini-player bar; Now Playing screen (full art, seek bar w/
  elapsed/remaining, play/pause, next/prev, shuffle, repeat); Queue bottom sheet
  (drag-reorder, swipe-remove, tap-to-jump, clear-queue). `PlayerViewModel`
  nav-graph-scoped and shared.

**Tests**
- `ExoAudioPlayer` smoke test on device (load/play/pause against a test asset).
- Service connection / `PlaybackController` integration test.
- `PlayerViewModel` tests with `FakePlaybackController` (Turbine).
- Goldens: mini-player, Now Playing (playing/paused/error), Queue sheet states.

**Definition of Done (demo)**
- Tap a track → it plays; mini-player appears; expand → Now Playing; seek / skip /
  shuffle / repeat all work; queue is editable. Audio continues with screen off
  and when backgrounded; notification controls work; an incoming call pauses and
  resumes; a notification sound ducks then restores.

---

## Phase 4 — Albums, Artists, Folders, Search & Sort

**Goal:** Browse the library the way listeners actually navigate it.

**Deliverable:** 📱 *Integrated Feature* — albums grid, artist pages, folder view,
plus search and sort, all able to start playback.

**Scope**
- Albums grid + album detail (disc/track order, play button); Artists list +
  artist detail (albums + tracks); Folders view mirroring source hierarchy.
- Sort options on All Tracks (title/artist/album); library search.
- Album-art loading/caching; play buttons feed `PlayFromSourceUseCase`.

**Tests**
- DAO tests for album/artist grouping & ordering.
- ViewModel tests for each screen; search/sort logic tests.
- Goldens for albums grid, album detail, artists, artist detail, folders, search.

**Definition of Done (demo)**
- Browse albums → open one → play the whole album in track order; open an artist;
  browse folders matching your sources; search narrows the list; sort reorders it.

---

## Phase 5 — Playlists

**Goal:** Create and manage playlists that survive file moves/renames.

**Deliverable:** 📱 *Integrated Feature* — full playlist CRUD with drag-reorder and
resilient resolution.

**Scope**
- `playlist` + `playlist_entry` tables + DAOs; `PlaylistRepository` impl.
- Create / rename / delete; add-to-playlist from any track context menu; remove;
  drag-to-reorder; play playlist.
- `ResolvePlaylistUseCase`: metadata match → path fallback → unresolvable
  (deterministic ordering); unresolved entries greyed with a warning icon.
- Playlists screen + playlist detail.

**Tests**
- Repo integration tests (CRUD, reorder, persistence).
- Resolution-path tests (mostly already in `:domain` from Phase 1) + integration.
- ViewModel tests; goldens incl. the unresolved-entry state.

**Definition of Done (demo)**
- Create a playlist → add tracks from several views → drag-reorder → play →
  rename/delete. Move a file on disk and rescan → entry still resolves; make one
  genuinely unresolvable → it shows greyed with a warning.

---

## Phase 6 — Playback Statistics

**Goal:** Passively record listening and surface summaries and recaps.

**Deliverable:** 📱 *Integrated Feature* — a Stats screen with totals, top
tracks/artists/albums, history, and month/year recaps.

**Scope**
- Play-event recording in the service: ≥ 5s threshold, actual
  `duration_listened_ms`, timestamp from `Clock`; `play_event` table.
- `StatsRepository` + `GetStatsSummaryUseCase`.
- Stats screen: total listening time, top tracks/artists/albums filterable by
  month / year / all-time, daily/monthly history (heatmap or bar chart),
  "top of the month/year" recap. Local-timezone bucketing.

**Tests**
- DAO aggregation tests incl. `localtime` bucketing and midnight/month boundary.
- Recording-rule tests (5s threshold, skip vs natural end).
- `StatPeriod` bounds (domain, from Phase 1); ViewModel tests; goldens.

**Definition of Done (demo)**
- Play several tracks → events recorded → Stats screen shows correct totals and
  rankings; switch month/year/all-time; history chart and recap render.

---

## Phase 7 — System Integration & Hardening

**Goal:** Production-grade behavior at the edges: hardware controls, resilience,
settings, and user-facing diagnostics.

**Deliverable:** 📱 *Integrated Feature* — headset/Bluetooth handling, background
rescan, state restoration, Settings, and the Diagnostics screen.

**Scope**
- Headset buttons: single/double/triple → play-pause/next/previous.
- `BluetoothMonitor`: pause on disconnect; optional resume on reconnect
  (user-configurable). Reconnect decision is pure/tested.
- `ScanScheduler` real impl: WorkManager periodic (daily) + foreground-after-1h.
- Last-queue restore on cold start (DataStore); persisted shuffle/repeat.
- Settings screen (theme, scan frequency, Bluetooth auto-resume).
- Diagnostics screen (recent errors, last `ScanReport`, share/export log) +
  next-launch crash surfacing. Drawer navigation for secondary destinations.

**Tests**
- `BluetoothMonitor` reconnect-decision tests (domain); settings repo tests;
  queue-restore logic tests; Diagnostics ViewModel tests.
- Goldens for Settings and Diagnostics screens.

**Definition of Done (demo)**
- Headset buttons work; unplugging Bluetooth pauses and replugging resumes (when
  enabled); kill + relaunch restores the queue and shuffle/repeat; dropping a new
  file in a source folder gets picked up by the periodic scan; settings persist;
  forcing a crash surfaces a report offer on next launch; the Diagnostics screen
  lists errors and exports the log.

---

## Phase dependency summary

```
Phase 0 ─► Phase 1 ─► Phase 2 ─► Phase 3 ─► Phase 4
                                   │           │
                                   ├──────────►├─► Phase 5
                                   └──────────────► Phase 6
Phase 7 builds on all of the above.
```

Phases 4, 5, and 6 all depend on the library (Phase 2) and playback (Phase 3) but
are largely independent of each other, so their order can be swapped to suit
priorities.
