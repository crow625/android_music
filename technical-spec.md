# Technical Specification

**Version 1.1 · Android Music Player**

> **v1.1 changelog:** Split `:domain` into a pure-Kotlin module; introduced `MediaUri`;
> split the player seam into `AudioPlayer` (service) + `PlaybackController` (client);
> promoted audio focus, Bluetooth, metadata reading, time, and logging to explicit seams;
> added the Observability & Diagnostics layer; revised the stable-track-ID strategy;
> made stats timezone-aware and playlist resolution deterministic; adopted the
> stateless-composable + Roborazzi-golden UI/preview strategy.

---

## 1. Overview

This document describes the architecture, key APIs, data model, and module boundaries for the Android music player. It is the authoritative reference for implementation decisions. The companion Feature Specification defines what the app does; this document defines how it is built.

The two overriding engineering principles are:

1. **Clean interface seams at every external-API boundary.** Anywhere the app touches an external API — the player SDK, the audio system, the database, the framework, or the UI toolkit — it does so through an interface with a test fake. Core logic runs independently of the UI and the Android framework.
2. **No error is ever swallowed silently.** Every failure is either an explicitly-handled known condition or is reported through the diagnostics seam, surfaced to the user, and persisted.

---

## 2. Technology Stack

| Concern         | Choice                                               |
| --------------- | ---------------------------------------------------- |
| Language        | Kotlin                                               |
| Min SDK         | API 26 (Android 8.0 Oreo)                            |
| Target SDK      | API 35                                               |
| UI toolkit      | Jetpack Compose                                      |
| Architecture    | MVVM + Use Cases (Clean Architecture lite)           |
| DI              | Hilt                                                 |
| Async           | Kotlin Coroutines + Flow                             |
| Navigation      | Jetpack Navigation Compose                           |
| Database        | Room                                                 |
| Preferences     | DataStore                                            |
| Audio           | androidx.media3 (ExoPlayer)                          |
| Logging         | Timber (behind a `Logger` seam)                      |
| Screenshot test | Roborazzi (JVM, reuses `@Preview`)                   |
| Build           | Gradle with version catalog (libs.versions.toml)     |

---

## 3. Module Structure

The project is split into a **pure-Kotlin `:domain` module** and an **Android `:app` module**. The split is deliberate: it makes the compiler — not code review — enforce that domain logic has zero Android dependencies and runs on a plain JVM.

| Module / Package | Type            | Contents                                                                              |
| ---------------- | --------------- | ------------------------------------------------------------------------------------- |
| `:domain`        | Kotlin library  | Domain models, repository + player interfaces, use cases, `PlayQueue` logic, `FocusPolicy`, normalization/stable-ID, all diagnostics interfaces & models. **No Android imports — enforced by the build.** |
| `:app`           | Android app     | Everything Android, organised into the packages below.                                |
| `:app › ui/`     | package         | Compose screens (stateless), ViewModels, state mappers, navigation                    |
| `:app › data/`   | package         | Repository implementations, Room database, SAF/MediaStore access, DataStore, `MediaUri ↔ Uri` mappers |
| `:app › player/` | package         | `ExoAudioPlayer`, `MediaLibraryService`, `PlaybackController` impl, `AudioFocusManager`, `BluetoothMonitor` — the only code that touches ExoPlayer or `android.media.*` |
| `:app › stats/`  | package         | `StatsRepository` impl, Room DAOs for the playback event log                          |
| `:app › diagnostics/` | package    | `Logger`/`DiagnosticReporter` impls, crash handler, log-file sink, Diagnostics screen |

**Dependency rule:** `ui/` → `domain/` ← `data/`, `player/`, `stats/`, `diagnostics/`. Nothing imports from a layer above it. `:domain` depends on nothing Android.

> The `:app` internal packages remain enforced by review. They map cleanly onto further Gradle modules (`:data`, `:player`, …) if the project grows; the pure-Kotlin `:domain` split is done up-front because it is cheap now and near-impossible to retrofit.

---

## 4. Architecture Overview

### 4.1 Layer Summary

| Layer                        | Responsibility                                                                                               |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------ |
| UI (Compose + ViewModel)     | Stateless composables observe a `UiState` and emit events. ViewModels map domain → `UiState`. No business logic. |
| Domain (Use Cases)           | Orchestrates repository and player calls. Pure Kotlin. No Android imports.                                   |
| Data (Repositories)          | Implements domain interfaces. Talks to Room, MediaStore, SAF, DataStore.                                     |
| Player (Service + ExoPlayer) | Implements `AudioPlayer`. Runs in `MediaLibraryService`. Only layer that imports ExoPlayer or `android.media.*`. |

### 4.2 Key Interfaces (Testability Seams)

All inter-layer communication crosses an interface. Implementations are injected by Hilt; tests use fakes. **Every external API listed in §4.3 sits behind one of these.**

#### `MediaUri`

The domain never imports `android.net.Uri`. A URI is represented by a value-class wrapper that makes intent explicit:

```kotlin
@JvmInline
value class MediaUri(val value: String)
```

`data/` and `player/` convert `MediaUri ↔ android.net.Uri` at their boundaries.

#### `AudioPlayer` (service-side)

Implemented by `ExoAudioPlayer` inside `MediaLibraryService`. This is the wrapper over the real ExoPlayer.

```kotlin
interface AudioPlayer {
    fun setQueue(queue: PlayQueue)       // Replaces queue; player pre-buffers adjacent items
    fun playIndex(index: Int)            // Jump to and play a specific queue position
    fun play()
    fun pause()
    fun stop()
    fun skipToNext()
    fun skipToPrevious()
    fun seekTo(positionMs: Long)
    val state: StateFlow<PlaybackState>
}
```

#### `PlaybackController` (client-side)

The player lives in the `MediaLibraryService` process; ViewModels cannot hold the `AudioPlayer` directly. They depend on `PlaybackController`, backed by a Media3 `MediaController` that connects to the service asynchronously — so it carries an explicit connection state.

```kotlin
interface PlaybackController {
    val connection: StateFlow<ConnectionState>   // Connecting / Connected / Disconnected
    val state: StateFlow<PlaybackState>
    fun playSource(source: QueueSource, startIndex: Int = 0)
    fun play()
    fun pause()
    fun skipToNext()
    fun skipToPrevious()
    fun seekTo(positionMs: Long)
    fun toggleShuffle()
    fun cycleRepeatMode()
}

enum class ConnectionState { Connecting, Connected, Disconnected }
```

#### `AudioFileRepository`

```kotlin
interface AudioFileRepository {
    fun observeLibrary(): Flow<Library>
    suspend fun listAudioFiles(folderUri: MediaUri): List<AudioFile>
    suspend fun scanSources(sourceUris: List<MediaUri>): ScanReport
}
```

#### `PlaylistRepository`

```kotlin
interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String): Playlist
    suspend fun addTrack(playlistId: Long, track: AudioFile)
    suspend fun removeTrack(playlistId: Long, entryId: Long)
    suspend fun reorderTrack(playlistId: Long, from: Int, to: Int)
    suspend fun resolveEntries(playlistId: Long): List<ResolvedEntry>
}
```

#### `StatsRepository`

```kotlin
interface StatsRepository {
    suspend fun recordPlayEvent(event: PlayEvent)
    suspend fun querySummary(period: StatPeriod): StatsSummary
    fun observeHistory(): Flow<List<DailyListening>>
}
```

#### Diagnostics & infrastructure seams

```kotlin
interface Logger {
    fun log(level: LogLevel, tag: String, message: String, error: Throwable? = null)
}

interface DiagnosticReporter {
    suspend fun report(error: AppError)
    fun observeRecent(limit: Int = 100): Flow<List<AppError>>
    suspend fun exportLog(): MediaUri          // writes/returns a shareable log file
}

interface AudioFocusManager {                  // wraps AudioFocusRequest / AudioManager
    fun requestFocus(): Boolean
    fun abandonFocus()
    val focusEvents: Flow<FocusEvent>
}

interface BluetoothMonitor {                   // wraps AudioDeviceCallback + A2DP receiver
    val events: Flow<BtEvent>                  // Connected(device) / Disconnected(device)
}

interface MetadataReader {                     // wraps MediaMetadataRetriever
    suspend fun read(uri: MediaUri): TrackMetadata?
}

interface SettingsRepository {                 // wraps DataStore
    val settings: Flow<AppSettings>
    suspend fun update(transform: (AppSettings) -> AppSettings)
}

interface ScanScheduler {                      // wraps WorkManager
    fun schedulePeriodic()
    fun requestImmediateScan()
}

interface Clock {                              // wraps system time + zone
    fun now(): Instant
    val zone: ZoneId
}

interface DispatcherProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}
```

### 4.3 External-API → Seam Map

| External API                           | Seam interface                         | Pure logic extracted                |
| -------------------------------------- | -------------------------------------- | ----------------------------------- |
| ExoPlayer                              | `AudioPlayer` (service)                | queue → `MediaItem` mapping         |
| Media3 `MediaController` / service     | `PlaybackController` (client)          | —                                   |
| `AudioManager` / `AudioFocusRequest`   | `AudioFocusManager`                    | **`FocusPolicy`** (event → action)  |
| Bluetooth callbacks / A2DP receiver    | `BluetoothMonitor`                     | reconnect-resume decision           |
| SAF `DocumentFile` / MediaStore        | `AudioFileRepository`                  | scan / dedup logic, `ScanReport`    |
| `MediaMetadataRetriever`               | `MetadataReader`                       | tag → `AudioFile`, stable-ID hashing|
| Room                                   | repository interfaces                  | —                                   |
| DataStore                              | `SettingsRepository`                   | —                                   |
| WorkManager                            | `ScanScheduler`                        | —                                   |
| System time / zone                     | `Clock`                                | `StatPeriod` → (from, to) bounds    |
| Coroutine dispatchers                  | `DispatcherProvider`                   | —                                   |
| `android.util.Log` / Timber            | `Logger`                               | —                                   |
| Jetpack Compose                        | stateless composable + `UiState` + `(Event) -> Unit` | domain → `UiState` mappers |

---

## 5. Domain Models

### 5.1 Core Models

```kotlin
data class AudioFile(
    val id: String,              // Stable ID — see §7.2
    val uri: MediaUri,
    val filePath: String,        // Mutable attribute; not the identity
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val trackNumber: Int,
    val discNumber: Int,
    val durationMs: Long,
    val albumArtUri: MediaUri?,
)

data class PlayQueue(
    val items: List<AudioFile>,
    val currentIndex: Int,
    val shuffled: Boolean = false,
    val originalOrder: List<AudioFile> = items,   // retained so shuffle can be undone
) {
    val current get() = items.getOrNull(currentIndex)
    val hasNext  get() = currentIndex < items.lastIndex
    val hasPrev  get() = currentIndex > 0
}

sealed class PlaybackState {
    object Idle : PlaybackState()
    data class Playing(val index: Int, val positionMs: Long) : PlaybackState()
    data class Paused (val index: Int, val positionMs: Long) : PlaybackState()
    data class Error  (val index: Int, val message: String)  : PlaybackState()
}

data class Playlist(val id: Long, val name: String, val entries: List<PlaylistEntry>)

data class PlaylistEntry(
    val id: Long,
    val trackId: String?,        // Stable track ID; null if track not in current library
    val trackTitle: String,      // Stored at add-time for resilient resolution
    val trackArtist: String,
    val trackAlbum: String,
    val filePath: String,        // Fallback
    val position: Int,
)

data class ResolvedEntry(val entry: PlaylistEntry, val resolvedFile: AudioFile?)
// resolvedFile == null means unresolvable; surface in UI with warning icon

enum class RepeatMode { Off, RepeatQueue, RepeatOne }
```

> **Shuffle is owned by the domain `PlayQueue`, not ExoPlayer.** ExoPlayer's own `shuffleModeEnabled` stays off so there is a single source of truth that the queue bottom-sheet and playback always agree on, and so the shuffle/restore logic is unit-testable. Toggling shuffle off restores `originalOrder` while keeping the current track current.

### 5.2 Stats Models

```kotlin
data class PlayEvent(
    val trackId: String,
    val trackTitle: String,
    val artistName: String,
    val albumName: String,
    val startedAt: Instant,
    val durationListenedMs: Long,
)

sealed class StatPeriod {
    data class Month(val year: Int, val month: Int) : StatPeriod()
    data class Year(val year: Int) : StatPeriod()
    object AllTime : StatPeriod()
}

data class StatsSummary(
    val topTracks: List<RankedItem>,
    val topArtists: List<RankedItem>,
    val topAlbums: List<RankedItem>,
    val totalListeningMs: Long,
)

data class RankedItem(val name: String, val totalMs: Long, val playCount: Int)
data class DailyListening(val date: LocalDate, val totalMs: Long)
```

### 5.3 QueueSource

`BuildQueueUseCase` takes a `QueueSource` to construct a `PlayQueue` from any origin:

```kotlin
sealed class QueueSource {
    data class FromAlbum(val albumId: String) : QueueSource()
    data class FromArtist(val artistId: String) : QueueSource()
    data class FromPlaylist(val playlistId: Long) : QueueSource()
    data class FromFolder(val folderUri: MediaUri) : QueueSource()
    data class FromLibrary(val sortOrder: SortOrder) : QueueSource()
    data class FromSingleTrack(val file: AudioFile, val contextList: List<AudioFile>) : QueueSource()
}
```

### 5.4 Diagnostics Models

```kotlin
enum class LogLevel { Debug, Info, Warn, Error }
enum class Severity { Warn, Error, Fatal }
enum class ErrorCategory { Playback, Scan, Metadata, Database, Permission, Unknown }

data class AppError(
    val category: ErrorCategory,
    val severity: Severity,
    val message: String,
    val stackTrace: String?,
    val context: Map<String, String>,   // e.g. trackId, fileUri, focus state
    val occurredAt: Instant,
)

data class ScanReport(
    val indexed: Int,
    val skippedUnsupported: Int,
    val metadataFailed: Int,
    val unreadable: Int,
    val problems: List<ScanProblem>,
)
data class ScanProblem(val uri: MediaUri, val reason: String)
```

---

## 6. Audio Stack

### 6.1 MediaLibraryService & the client/service split

ExoPlayer lives inside a `MediaLibraryService` subclass (`androidx.media3`). This is mandatory for background playback, system media controls (lock screen, notification shade, Wear OS, Android Auto), automatic Bluetooth A2DP routing, and an Activity-independent process lifecycle.

`ExoAudioPlayer` wraps the ExoPlayer instance and implements **`AudioPlayer`**. It is instantiated inside the service, not in the application Hilt graph, so its lifetime is always bound to the service.

Because the player runs in the service, **the UI never references `AudioPlayer` directly**. ViewModels depend on **`PlaybackController`**, whose implementation is backed by a Media3 `MediaController`. The controller connects asynchronously, so `PlaybackController.connection` exposes `Connecting / Connected / Disconnected`; the UI renders a sensible state while connecting and surfaces a diagnostic if the connection fails.

### 6.2 Gapless Playback

Gapless playback is achieved by passing the entire `PlayQueue` to ExoPlayer as a playlist via `setMediaItems()`. ExoPlayer pre-buffers adjacent items automatically — the app must not attempt to manage cross-track buffering manually.

Whenever the queue changes (reorder, add, remove, shuffle), `setMediaItems()` is called with the updated list and current index. ExoPlayer reconciles the update without interrupting playback of the current track where possible. The order passed to ExoPlayer is always the domain `PlayQueue.items` order (shuffle is resolved in the domain — see §5.1).

### 6.3 Audio Focus

Audio focus is split into a **pure policy** and a **thin Android binding**:

- **`FocusPolicy`** (pure, in `:domain`) maps a `FocusEvent` + current playback state to a `FocusAction` (`Pause`, `Resume`, `Duck`, `Restore`). Fully unit-tested.
- **`AudioFocusManager`** (in `player/`) wraps `AudioFocusRequest` (API 26+), emits `FocusEvent`s, and applies the `FocusAction` returned by the policy.

| Focus change                         | `FocusPolicy` action                                       |
| ------------------------------------ | ---------------------------------------------------------- |
| `AUDIOFOCUS_GAIN`                    | Resume if paused for focus loss; restore volume if ducked  |
| `AUDIOFOCUS_LOSS`                    | Pause, abandon focus                                       |
| `AUDIOFOCUS_LOSS_TRANSIENT`          | Pause; resume on GAIN                                      |
| `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK` | Lower volume to 20%; restore on GAIN                       |

### 6.4 Bluetooth

A2DP routing is handled by the Android audio system once audio focus is held. The two explicit cases sit behind **`BluetoothMonitor`**, which emits `Connected`/`Disconnected` events from an `AudioDeviceCallback` and a `BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED` receiver:

- **Disconnect:** pause playback when the active output device is removed.
- **Reconnect:** optionally resume when the previously active device reconnects (user-configurable via `SettingsRepository`).

The reconnect-resume *decision* is pure logic and unit-tested; only the event source is Android.

### 6.5 Headset Buttons

`MediaSession` (via Media3) handles headset button routing automatically once the session is active. The app implements `MediaSession.Callback`:

| Key event                                  | Action                       |
| ------------------------------------------ | ---------------------------- |
| `KEYCODE_MEDIA_PLAY_PAUSE`                 | Toggle play/pause            |
| `KEYCODE_MEDIA_NEXT`                       | Skip next                    |
| `KEYCODE_MEDIA_PREVIOUS`                   | Restart or skip previous     |
| `KEYCODE_HEADSETHOOK` single/double/triple | Play/pause / next / previous |

---

## 7. Persistence

### 7.1 What Goes Where

| Store         | Contents                                                                                               | Rationale                                                  |
| ------------- | ------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------- |
| **Room**      | Track index, playlists, playlist entries, play events, source folders, **diagnostic events**           | Relational data with foreign keys, queries, and stable IDs |
| **DataStore** | Shuffle/repeat state, last queue (track ID list + index), Bluetooth auto-resume, theme, scan frequency | Scalar settings; no SQL needed; survives process death     |
| **Log file**  | Rotating verbose text log in app-private storage (`filesDir/logs/`)                                    | Full human-readable traces, exportable as a single file    |

The current in-memory play queue lives in `MediaLibraryService`. On cold start, the last queue is restored from DataStore (track IDs looked up against Room).

### 7.2 Room Database

Database name: `android_music.db`

#### Tables

| Table             | Key columns                                                                                                                                                              |
| ----------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `track`           | `id TEXT PK` (stable ID), `title`, `artist`, `album`, `album_artist`, `track_number`, `disc_number`, `duration_ms`, `file_path`, `folder_uri`, `mb_recording_id`, `date_indexed` |
| `playlist`        | `id INTEGER PK AUTOINCREMENT`, `name`, `created_at`, `updated_at`                                                                                                        |
| `playlist_entry`  | `id`, `playlist_id FK`, `track_id FK` (nullable), `track_title`, `track_artist`, `track_album`, `file_path`, `position`                                                  |
| `play_event`      | `id`, `track_id`, `track_title`, `artist_name`, `album_name`, `started_at INTEGER` (epoch ms), `duration_listened_ms`                                                    |
| `source_folder`   | `uri TEXT PK`, `added_at`                                                                                                                                                |
| `diagnostic_event`| `id`, `category`, `severity`, `message`, `stack_trace`, `context_json`, `occurred_at INTEGER`                                                                            |

#### Stable Track ID

The `track.id` is resolved in priority order, so genuinely-distinct recordings stay distinct when their files are tagged for it:

1. **Embedded MusicBrainz Recording ID** (`MUSICBRAINZ_TRACKID` tag), if present. This is a globally-unique recording identifier read **locally, with no network**, and cleanly distinguishes remasters/versions.
2. Otherwise, a deterministic SHA-1 (hex) of `LOWER(title) + "§" + LOWER(artist) + "§" + LOWER(album) + "§" + durationBucket`, where `durationBucket` is `durationMs` rounded to the nearest 2 seconds (absorbs minor re-encode jitter while separating obviously different recordings).

The file path is a mutable attribute, updated on rescan. Playlist entries and play events therefore reference the track stably across file moves and renames.

> **Known residual:** two *untagged* files that are genuinely different recordings but share title/artist/album/duration will still collide. This is documented and accepted for v1; the MusicBrainz path is the recommended mitigation, and a future version could add manual split/merge.

#### Indices

- `track`: `(artist)`, `(album)`, `(folder_uri)`
- `play_event`: `(started_at)`, `(artist_name)`, `(track_id)`
- `playlist_entry`: `(playlist_id, position)`
- `diagnostic_event`: `(occurred_at)`, `(category)`

### 7.3 Folder Scanning

`MediaStoreAudioRepository` implements `AudioFileRepository`, delegating tag reads to the `MetadataReader` seam. Scanning strategy:

- For each source URI, use `DocumentFile.fromTreeUri()` to enumerate files recursively.
- For each audio file, extract metadata via `MetadataReader` (wrapping `MediaMetadataRetriever`).
- Compute the stable track ID; upsert into the `track` table (update `file_path` if moved).
- Tracks whose file no longer exists under any source are marked stale (soft-deleted).
- Use `ContentResolver.takePersistableUriPermission()` on each source root to survive restarts.
- Every per-file outcome (indexed / skipped-unsupported / metadata-failed / unreadable) is tallied into a **`ScanReport`** so the user can be told *why* a file is missing. Failures are reported via the diagnostics seam, never swallowed.

A `WorkManager` `PeriodicWorkRequest` (behind `ScanScheduler`) triggers a background rescan once per day and on app foreground after 1+ hour away.

### 7.4 Playlist Resolution

When a playlist is loaded, each `PlaylistEntry` is resolved against the library:

1. Query `track`: `LOWER(title) = LOWER(entry.trackTitle) AND LOWER(artist) = LOWER(entry.trackArtist) AND LOWER(album) = LOWER(entry.trackAlbum)`, **`ORDER BY id ASC`** for determinism.
2. Exactly one match → resolved.
3. Zero matches → fall back to matching `entry.filePath` against `track.file_path`.
4. Still no match → `ResolvedEntry(entry, resolvedFile = null)`; surface in UI with a warning icon.
5. Multiple matches → use the first row of the deterministic ordering (stable and testable).

---

## 8. Statistics

Play events are recorded by `MediaLibraryService` whenever a track plays for at least 5 seconds. The raw event log is never aggregated in place — summaries are always computed by Room queries, preserving full historical fidelity for any future query shape.

### 8.1 Recording

The service tracks state transitions. When a track ends naturally or is skipped after ≥ 5 seconds of play, a `play_event` row is inserted with the actual `duration_listened_ms` (not the track duration). The timestamp comes from the injected `Clock`.

### 8.2 Timezone

All day/month bucketing is done in the **device's local timezone**, sourced from `Clock.zone`. SQLite date functions use the `'localtime'` modifier so listening near midnight lands on the correct local day. A boundary test covers the midnight/month-edge case.

### 8.3 Example DAO Queries

```kotlin
@Query("""
    SELECT artist_name AS name,
           SUM(duration_listened_ms) AS totalMs,
           COUNT(*) AS playCount
    FROM play_event
    WHERE started_at >= :from AND started_at < :to
    GROUP BY artist_name
    ORDER BY totalMs DESC
    LIMIT 10
""")
fun topArtists(from: Long, to: Long): Flow<List<RankedItem>>

@Query("""
    SELECT DATE(started_at / 1000, 'unixepoch', 'localtime') AS date,
           SUM(duration_listened_ms) AS totalMs
    FROM play_event
    GROUP BY date
    ORDER BY date ASC
""")
fun dailyHistory(): Flow<List<DailyListening>>
```

`StatPeriod` maps to `(from, to)` epoch-ms bounds computed in `StatsRepository` using `Clock` (so period boundaries respect the local zone).

---

## 9. UI Architecture

### 9.1 Stateless composables + UiState + event sink

Every screen is a **stateless composable** driven by an immutable `UiState` and a single event lambda:

```kotlin
@Composable
fun LibraryScreen(state: LibraryUiState, onEvent: (LibraryEvent) -> Unit) { /* dumb UI */ }
```

A thin stateful wrapper connects the ViewModel:

```kotlin
@Composable
fun LibraryRoute(vm: LibraryViewModel = hiltViewModel()) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    LibraryScreen(state, vm::onEvent)
}
```

The domain → `UiState` mapping lives in the ViewModel (or a pure mapper function) and is unit-tested without Compose. Composables contain no business logic and no fake data.

#### Error surfacing (never silent)

- **Recoverable/expected** errors (decode failure, missing file, revoked permission) are carried as one-shot events in `UiState` and shown as a Snackbar/inline message. Playback errors flow `PlaybackState.Error` → nav-graph-scoped `PlayerViewModel` → a shared Snackbar host at the `Scaffold` level, so they surface from any screen.
- **Unexpected** errors and fatals are reported to the diagnostics layer and surfaced via a notification / Diagnostics-screen badge.

### 9.2 Previews & screenshot goldens

- All `@Preview` functions and their fake `UiState` (`PreviewParameterProvider`s) live in a **dedicated debug source set**, physically separated from production composables. Production code never imports preview/fake code.
- **Roborazzi** reuses those `@Preview` functions as JVM screenshot tests: each preview is simultaneously the IDE/demo visual reference and a golden regression test — one artifact, two purposes.
- Every significant component has previews covering its meaningful states (empty, loading, error, long titles, light/dark).

### 9.3 Navigation Graph

Single-Activity. `NavHost` at the top level. Bottom nav manages four main destinations; the drawer overlay is handled outside the NavHost.

| Destination    | Route                                       |
| -------------- | ------------------------------------------- |
| Library        | `/library`                                  |
| Albums         | `/albums`, `/albums/{albumId}`              |
| Artists        | `/artists`, `/artists/{artistId}`           |
| Playlists      | `/playlists`, `/playlists/{playlistId}`     |
| Now Playing    | `/now-playing` (also via mini-player tap)   |
| Queue          | Bottom sheet overlay, not a nav destination |
| Stats          | `/stats` (from drawer)                      |
| Folder Sources | `/sources` (from drawer)                    |
| Settings       | `/settings` (from drawer)                   |
| Diagnostics    | `/diagnostics` (from Settings)              |

### 9.4 ViewModel Responsibilities

ViewModels hold no Android framework state except `ApplicationContext` where unavoidable (injected by Hilt). Each screen has a dedicated ViewModel injected with the use cases it needs. Use cases are the only entry point to the domain layer from ViewModels.

### 9.5 Shared Player State

`PlayerViewModel` is scoped to the `NavGraph` (not to any individual screen) and shared across all screens via the `NavBackStackEntry`. This ensures the mini-player bar and Now Playing screen always observe the same `PlaybackState` (via `PlaybackController`), and that state survives navigation between tabs.

---

## 10. Use Cases

| Use Case                 | Responsibility                                                                          |
| ------------------------ | --------------------------------------------------------------------------------------- |
| `BuildQueueUseCase`      | Takes a `QueueSource`, returns a `PlayQueue`. Pure; no Android deps.                    |
| `PlayFromSourceUseCase`  | Calls `BuildQueueUseCase` then drives `PlaybackController`.                             |
| `AddToQueueUseCase`      | Inserts a track at the given position; updates the queue.                              |
| `ToggleShuffleUseCase`   | Applies/undoes shuffle on the `PlayQueue` (pure), preserving the current track.        |
| `ScanLibraryUseCase`     | Triggers rescan of all source folders; returns a `ScanReport`; reports failures.       |
| `CreatePlaylistUseCase`  | Validates name uniqueness; calls `PlaylistRepository.createPlaylist`.                   |
| `ResolvePlaylistUseCase` | Calls `PlaylistRepository.resolveEntries`; returns resolved list with warnings.         |
| `RecordPlayEventUseCase` | Called by `MediaLibraryService`; wraps `StatsRepository.recordPlayEvent`.               |
| `GetStatsSummaryUseCase` | Takes a `StatPeriod`; returns `StatsSummary` from `StatsRepository`.                    |

---

## 11. Observability & Diagnostics

On-device only — there is no network reporter (consistent with the app's local-first design). Diagnostics may be **exported/shared** by the user.

### 11.1 Logging

All logging goes through the **`Logger`** seam (`:domain`). `:app` provides a Logcat tree (debug builds) and a file/Room sink (always), backed by Timber. `:domain` cannot call `android.util.Log`, which keeps logging testable (`FakeLogger`).

### 11.2 Structured error reporting

`DiagnosticReporter` persists `AppError`s to the `diagnostic_event` Room table (queryable, aggregatable) **and** appends to the rotating log file (full traces, exportable as one file). Because reporting is a seam, use-case tests can assert that a failure path actually reported.

### 11.3 Crash capture

A `Thread.setDefaultUncaughtExceptionHandler` writes the fatal stack trace to the store before the process dies. On next launch the unsent fatal is detected and the user is offered "the app crashed last time — view / share report."

### 11.4 User-visible Diagnostics screen (v1)

Reached from Settings. Lists recent `AppError`s with stack traces and the most recent `ScanReport`, and offers **share/export** of the log file. Built with the same stateless-composable + previews + golden pattern as every other screen.

### 11.5 No-silent-catch policy

Every `catch` either handles a documented, expected condition or reports via `DiagnosticReporter`. A detekt/lint rule flags empty or swallowing catch blocks at build time.

### 11.6 Debug-only instrumentation

Enabled in debug builds only: **StrictMode** (main-thread I/O detection), **LeakCanary** (memory-leak detection). (A Media3 `AnalyticsListener` for verbose playback tracing is intentionally *not* enabled by default — playback errors are still captured via `Player.Listener.onPlayerError` — but can be added later.)

---

## 12. Testing Strategy

### 12.1 Unit Tests (JVM, no Android runtime)

Run in `:domain` and against `:app` ViewModels/mappers:

- All use cases: inject fake repositories and `FakePlaybackController`.
- All ViewModels and domain → `UiState` mappers: assert `StateFlow` emissions with [Turbine](https://github.com/cashapp/turbine).
- `PlayQueue` mutations: reorder, **shuffle on/off restoring original order with current track preserved**, repeat-mode transitions, add-to-next/end, clear-queue-keeps-current.
- **Skip-previous 3-second rule.**
- `FocusPolicy`: one test per focus transition.
- Stable-ID resolution: MusicBrainz path, hash path, duration bucketing, determinism.
- Playlist resolution: metadata match, path fallback, unresolvable, multiple-match determinism.
- `StatPeriod` → (from, to) bounds incl. local-timezone month/midnight boundaries (`Clock` faked).
- `BuildQueueUseCase`: one test per `QueueSource` variant.
- Diagnostics: assert failure paths call `DiagnosticReporter.report`.

### 12.2 Screenshot / Golden Tests (JVM, Roborazzi)

- Every significant composable, driven by its `@Preview` cases (empty/loading/error/edge states, light & dark). Goldens committed; CI diffs on every change.

### 12.3 Integration Tests (Android emulator / device)

- Room DAOs: in-memory Room; test all queries including stat aggregations and `localtime` bucketing.
- `MediaStoreAudioRepository` / `MetadataReader`: against a real `ContentResolver` and sample files on the emulator; assert `ScanReport` contents.
- `ExoAudioPlayer` + `PlaybackController`: smoke test connect/load/play/pause against a real audio file in test assets.

### 12.4 Fakes

```kotlin
class FakePlaybackController : PlaybackController {
    private val _conn  = MutableStateFlow(ConnectionState.Connected)
    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val connection = _conn
    override val state = _state
    var lastSource: QueueSource? = null
    override fun playSource(source: QueueSource, startIndex: Int) { lastSource = source; _state.value = PlaybackState.Playing(startIndex, 0) }
    override fun play() {}
    override fun pause() {}
    override fun skipToNext() {}
    override fun skipToPrevious() {}
    override fun seekTo(positionMs: Long) {}
    override fun toggleShuffle() {}
    override fun cycleRepeatMode() {}
}

class FakeAudioPlayer : AudioPlayer {
    var lastQueue: PlayQueue? = null
    var lastPlayedIndex: Int? = null
    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val state: StateFlow<PlaybackState> = _state
    override fun setQueue(queue: PlayQueue)     { lastQueue = queue }
    override fun playIndex(index: Int)          { lastPlayedIndex = index; _state.value = PlaybackState.Playing(index, 0) }
    override fun play()                         { }
    override fun pause()                        { }
    override fun stop()                         { _state.value = PlaybackState.Idle }
    override fun skipToNext()                   { }
    override fun skipToPrevious()               { }
    override fun seekTo(positionMs: Long)       { }
}

class FakeAudioFileRepository(
    private val files: List<AudioFile> = emptyList()
) : AudioFileRepository {
    override fun observeLibrary() = flowOf(Library(files))
    override suspend fun listAudioFiles(folderUri: MediaUri) = files
    override suspend fun scanSources(sourceUris: List<MediaUri>) = ScanReport(files.size, 0, 0, 0, emptyList())
}

class FakeLogger : Logger {
    val entries = mutableListOf<Triple<LogLevel, String, Throwable?>>()
    override fun log(level: LogLevel, tag: String, message: String, error: Throwable?) {
        entries += Triple(level, message, error)
    }
}
```

---

## 13. Key Gradle Dependencies

| Artifact                                        | Purpose                                  |
| ----------------------------------------------- | ---------------------------------------- |
| `androidx.media3:media3-exoplayer`              | ExoPlayer core                           |
| `androidx.media3:media3-session`                | MediaSession + MediaLibraryService       |
| `androidx.media3:media3-ui`                     | Optional: stock player UI components     |
| `androidx.room:room-runtime` / `room-ktx`       | Room database + coroutine/Flow ext.      |
| `androidx.datastore:datastore-preferences`      | DataStore for scalar preferences         |
| `androidx.hilt:hilt-android`                    | Dependency injection                     |
| `androidx.navigation:navigation-compose`        | Navigation for Compose                   |
| `androidx.work:work-runtime-ktx`                | WorkManager for background library scans |
| `androidx.lifecycle:lifecycle-runtime-compose`  | `collectAsStateWithLifecycle`            |
| `com.jakewharton.timber:timber`                 | Logging backend (behind `Logger`)        |
| `io.github.takahirom.roborazzi:*`               | JVM screenshot/golden tests from previews|
| `org.robolectric:robolectric`                   | JVM Android runtime for Roborazzi        |
| `com.squareup.leakcanary:leakcanary-android`    | Debug-only leak detection                |
| `io.gitlab.arturbosch.detekt`                   | Lint incl. no-silent-catch rule          |
| `app.cash.turbine:turbine`                      | Flow testing in unit tests               |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | `TestScope` / `runTest`                  |

---

## 14. Build Order

1. **Phase 0 — Scaffold.** Modules (`:domain` pure-Kotlin + `:app`), version catalog, Hilt, Roborazzi harness proven on one trivial golden, the diagnostics seams (`Logger`/`DiagnosticReporter`/crash handler), StrictMode/LeakCanary/detekt wiring.
2. **Phase 1 — Pure domain.** Models, `PlayQueue` logic, `BuildQueueUseCase`, stable-ID/normalization, `FocusPolicy`, `Clock`-based `StatPeriod` math. Fully unit-tested.
3. **Phase 2 — Data.** Room schema + DAOs, repository impls, `MetadataReader`, scanning + `ScanReport`. In-memory Room integration tests + JVM tests for mappers/scan logic.
4. **Phase 3 — Player.** `MediaLibraryService`, `ExoAudioPlayer`, `MediaController`-backed `PlaybackController`, `AudioFocusManager`, `BluetoothMonitor`.
5. **Phase 4 — UI slices.** One feature at a time (Library → Albums → Artists → Playlists → Now Playing + mini-player → Queue → Stats → Sources → Settings → Diagnostics): `UiState` + stateless screen + ViewModel + previews + goldens + ViewModel tests.
6. **Phase 5 — Cross-cutting.** Foreground notification, headset buttons, Bluetooth auto-resume, WorkManager rescan, last-queue restore.

---

_End of Technical Specification._
