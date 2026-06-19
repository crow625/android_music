# Feature Specification

**Version 1.0 · Android Music Player**

---

## 1. Overview

This spec describes a local-first Android music player that plays audio files stored on the device or on mounted storage. It organises music by album, artist, and user-created playlist, offers a full playback queue with gapless transitions, and integrates with the Android audio system for well-behaved, system-wide audio management including Bluetooth streaming and headset controls.

The app contains no streaming service, no accounts, and no network dependency for core playback.

---

## 2. Navigation

Navigation uses a bottom navigation bar with a persistent mini-player bar sitting immediately above it. A slide-out drawer provides access to secondary destinations.

### 2.1 Bottom Navigation (primary destinations)

| Destination | Description                                |
| ----------- | ------------------------------------------ |
| Library     | All tracks, browsable and searchable       |
| Albums      | Grid of albums, tappable to album detail   |
| Artists     | List of artists, tappable to artist detail |
| Playlists   | User-created playlists                     |

### 2.2 Navigation Drawer (secondary destinations)

| Destination    | Description                              |
| -------------- | ---------------------------------------- |
| Folder Sources | Add or remove source folders             |
| Playback Stats | Listening history and summary statistics |
| Settings       | App preferences                          |

### 2.3 Mini-Player

A persistent mini-player bar is always visible above the bottom nav when a track is loaded. It displays the current track title, artist, and album art thumbnail, with play/pause and skip-next controls. Tapping it expands to the full Now Playing screen.

---

## 3. Music Library

### 3.1 Folder Sources

The user selects one or more folders to include as music sources. The app scans those folders recursively for supported audio files and indexes them into the local library.

- Folder selection uses the Android Storage Access Framework (SAF) folder picker.
- Multiple roots are supported (e.g. internal storage and an SD card simultaneously).
- Source folders can be added or removed at any time from the Folder Sources screen.
- Removing a source folder removes those tracks from the library but does not delete the files.

### 3.2 Supported Formats

MP3, AAC, FLAC, OGG Vorbis, WAV, ALAC (m4a), OPUS. Format support is determined by the Android media framework and may vary by device.

### 3.3 Metadata

Track metadata is read from embedded file tags (ID3v2 for MP3, Vorbis comments for FLAC/OGG, MP4 atoms for AAC/ALAC). The following fields are indexed:

- Title
- Artist
- Album
- Album Artist
- Track number and disc number
- Genre
- Year
- Duration
- Embedded album art

### 3.4 Library Views

| View       | Description                                                          |
| ---------- | -------------------------------------------------------------------- |
| All Tracks | Flat list of all indexed tracks, sortable by title, artist, or album |
| Albums     | Grid view; tapping an album shows its track list in disc/track order |
| Artists    | List view; tapping an artist shows their albums and tracks           |
| Playlists  | List of user playlists; tapping shows the playlist track list        |
| Folders    | File-system folder view mirroring the source folder hierarchy        |

---

## 4. Playlists

### 4.1 Creating and Editing Playlists

- Users can create a named playlist from any library view.
- Tracks can be added to a playlist from any track context menu.
- Tracks can be removed from a playlist via the playlist detail screen.
- Track order within a playlist can be changed by drag-to-reorder.
- Playlists can be renamed or deleted.

### 4.2 Playlist Resilience

Playlists are resilient to file moves and renames. Rather than storing only a file path, each playlist entry stores a composite identity derived from the track's embedded metadata:

| Field       | Value                                                                                                                                       |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| Primary key | Title + Artist + Album (normalised, case-insensitive)                                                                                       |
| Fallback    | File path (used only when metadata match fails)                                                                                             |
| Resolution  | At load time, each entry is resolved against the current library index; unresolvable entries are surfaced as greyed-out with a warning icon |

---

## 5. Playback

### 5.1 Starting Playback

Playback can be initiated from any of the following contexts:

| Context                    | Behaviour                                                                                                  |
| -------------------------- | ---------------------------------------------------------------------------------------------------------- |
| Track tap                  | Plays the tapped track; the surrounding list (album, artist, folder, or all tracks) becomes the play queue |
| Album / Artist play button | Plays the entire album or artist discography from the first track                                          |
| Playlist play button       | Plays the entire playlist from the first track                                                             |
| Queue: Add to next         | Inserts the selected track immediately after the currently playing track                                   |
| Queue: Add to end          | Appends the selected track to the end of the current queue                                                 |

### 5.2 Playback Controls

| Control       | Behaviour                                                                                          |
| ------------- | -------------------------------------------------------------------------------------------------- |
| Play / Pause  | Toggle playback                                                                                    |
| Skip Next     | Advance to the next track in the queue                                                             |
| Skip Previous | If more than 3 seconds into a track, restart the current track; otherwise go to the previous track |
| Seek          | Scrub to any position within the current track via a seek bar                                      |
| Shuffle       | Randomise the queue order; toggling off restores the original order                                |
| Repeat        | Off / Repeat Queue / Repeat One                                                                    |

### 5.3 Gapless Playback

Consecutive tracks in the queue transition without audible gaps. The player pre-buffers the next track before the current track ends, enabling seamless album playback where tracks are intended to flow together.

### 5.4 Now Playing Screen

- Album art (full-size)
- Track title, artist, album
- Seek bar with elapsed and remaining time
- Play/pause, skip next, skip previous controls
- Shuffle and repeat toggles
- Queue button — opens the current play queue in a bottom sheet

---

## 6. Play Queue

- The current queue is displayed in a scrollable bottom sheet accessible from the Now Playing screen.
- The currently playing track is highlighted and the list scrolls to it automatically.
- Tracks can be reordered by drag handle.
- Tracks can be removed from the queue by swipe-to-dismiss or context menu.
- Tapping a queued track jumps to it immediately.
- "Clear queue" removes all tracks except the currently playing one.

---

## 7. Audio Behaviour

### 7.1 Audio Focus

The app requests and manages Android audio focus correctly:

- Pauses on transient audio focus loss (e.g. incoming notification sound).
- Pauses on sustained focus loss (e.g. incoming phone call); resumes on focus regain.
- Ducks (lowers volume) when another app requests brief audio focus with CAN_DUCK; restores volume on regain.

### 7.2 Background Playback

Playback continues when the screen is off or when the user switches to another app. A persistent foreground service notification is displayed with playback controls and track information.

### 7.3 Bluetooth

- Audio is routed to the active Bluetooth A2DP device automatically by the Android audio system.
- Playback pauses automatically when the Bluetooth device disconnects.
- Playback can optionally auto-resume when the Bluetooth device reconnects (user-configurable).

### 7.4 Headset & Media Controls

- Single button press: play/pause.
- Double button press: skip next.
- Triple button press: skip previous.
- Lock screen and notification media controls are always available during playback.
- Android Auto and Wear OS media integration via MediaSession.

---

## 8. Playback Statistics

The app passively tracks playback activity and surfaces summary statistics on the Stats screen.

### 8.1 Tracked Data

| Scope          | Data                                                    |
| -------------- | ------------------------------------------------------- |
| Per play event | Track, artist, album, timestamp, duration listened (ms) |
| Granularity    | Raw events are stored; summaries are computed on demand |

### 8.2 Summary Views

| View                    | Description                                                                   |
| ----------------------- | ----------------------------------------------------------------------------- |
| Total listening time    | Lifetime and per-period                                                       |
| Top tracks              | By play count and by total time listened; filterable by month, year, all-time |
| Top artists             | By total time listened; filterable by month, year, all-time                   |
| Top albums              | By total time listened; filterable by month, year, all-time                   |
| Daily / monthly history | Calendar heatmap or bar chart of listening time over time                     |

A "top of the month" and "top of the year" summary is available, mirroring the style of year-end streaming recaps.

---

## 9. Out of Scope (v1)

The following are explicitly out of scope for the initial version:

- Streaming from network sources or cloud storage
- Audio equaliser or effects
- Lyrics display
- MusicBrainz or online metadata lookup
- Podcast support
- Crossfade between tracks (gapless is in scope; crossfade is a separate feature)
- Lock-screen album art on Android versions below API 26

---

_End of Feature Specification._
