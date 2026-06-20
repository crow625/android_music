package com.example.androidmusic.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val entries: List<PlaylistEntry>,
)

/**
 * A playlist entry stores resilient identity (metadata captured at add-time)
 * so it can be re-resolved against the library after files move or are renamed.
 */
data class PlaylistEntry(
    val id: Long,
    val trackId: String?,
    val trackTitle: String,
    val trackArtist: String,
    val trackAlbum: String,
    val filePath: String,
    val position: Int,
)

/** An entry paired with the library track it resolved to ([resolvedFile] == null ⇒ unresolvable). */
data class ResolvedEntry(
    val entry: PlaylistEntry,
    val resolvedFile: AudioFile?,
) {
    val isResolved: Boolean get() = resolvedFile != null
}
