package com.example.androidmusic.domain.metadata

import com.example.androidmusic.domain.model.MediaUri

/** Raw tag data read from a file, before it is combined into an [AudioFile]. */
data class TrackMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val trackNumber: Int,
    val discNumber: Int,
    val durationMs: Long,
    val musicBrainzRecordingId: String?,
    val albumArtUri: MediaUri?,
)

/** Seam over the platform metadata extractor (MediaMetadataRetriever). */
interface MetadataReader {
    /** Returns parsed metadata, or null if the file is unreadable/unsupported. */
    suspend fun read(uri: MediaUri): TrackMetadata?
}
