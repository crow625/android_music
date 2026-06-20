package com.example.androidmusic.domain.model

/**
 * A single indexed audio track.
 *
 * [id] is the stable identity (see `StableTrackId`); [uri] and [filePath] are
 * mutable attributes that may change as files move, so they are never used as
 * identity. All fields are plain Kotlin/JVM types — no Android imports.
 */
data class AudioFile(
    val id: String,
    val uri: MediaUri,
    val filePath: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val trackNumber: Int,
    val discNumber: Int,
    val durationMs: Long,
    val albumArtUri: MediaUri?,
)
