package com.example.androidmusic.domain.playlist

import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.PlaylistEntry
import com.example.androidmusic.domain.model.ResolvedEntry

/**
 * Resolves stored [PlaylistEntry] rows against the current library so playlists
 * survive files moving or being renamed.
 *
 * Strategy, per entry (technical-spec §9):
 * 1. **Metadata match** — the entry's captured [PlaylistEntry.trackId] is the
 *    stable, metadata-derived id (see `StableTrackId`); if a library track still
 *    carries that id, the file is the same recording regardless of where it moved.
 * 2. **Path fallback** — otherwise, a library track at the same [PlaylistEntry.filePath]
 *    is treated as the entry (covers a re-tag that changed the metadata id but not
 *    the location).
 * 3. **Unresolvable** — no match; surfaced to the UI as a greyed entry.
 *
 * Ordering is deterministic: entries come back sorted by [PlaylistEntry.position]
 * (ties broken by [PlaylistEntry.id]) so the queue is stable across resolutions.
 */
object PlaylistResolution {

    fun resolve(entries: List<PlaylistEntry>, library: List<AudioFile>): List<ResolvedEntry> {
        val byId: Map<String, AudioFile> = library.associateBy { it.id }
        // A path can legitimately be shared only transiently during a move; last-writer
        // wins is fine here because the id match above already handled the stable case.
        val byPath: Map<String, AudioFile> = library.associateBy { it.filePath }

        return entries
            .sortedWith(compareBy({ it.position }, { it.id }))
            .map { entry ->
                val match = entry.trackId?.let { byId[it] } ?: byPath[entry.filePath]
                ResolvedEntry(entry = entry, resolvedFile = match)
            }
    }
}
