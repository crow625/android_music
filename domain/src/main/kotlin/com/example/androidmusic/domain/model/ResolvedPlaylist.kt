package com.example.androidmusic.domain.model

/**
 * A playlist with each stored entry paired to the library track it currently
 * resolves to (see `PlaylistResolution`). Drives the playlist detail UI, where
 * unresolvable entries are shown greyed.
 */
data class ResolvedPlaylist(
    val id: Long,
    val name: String,
    val entries: List<ResolvedEntry>,
) {
    /** Library tracks for the resolvable entries, in playlist order (the playable queue). */
    val playableTracks: List<AudioFile> get() = entries.mapNotNull { it.resolvedFile }
}
