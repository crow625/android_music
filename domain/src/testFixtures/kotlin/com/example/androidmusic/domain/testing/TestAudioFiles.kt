package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.MediaUri

/** Builds an [AudioFile] with sensible defaults; override only what a test cares about. */
@Suppress("LongParameterList")
fun audioFile(
    id: String = "id-1",
    title: String = "Title",
    artist: String = "Artist",
    album: String = "Album",
    albumArtist: String = artist,
    trackNumber: Int = 1,
    discNumber: Int = 1,
    durationMs: Long = 180_000L,
    filePath: String = "/music/$id.mp3",
    uri: MediaUri = MediaUri("file://$filePath"),
    albumArtUri: MediaUri? = null,
): AudioFile = AudioFile(
    id = id,
    uri = uri,
    filePath = filePath,
    title = title,
    artist = artist,
    album = album,
    albumArtist = albumArtist,
    trackNumber = trackNumber,
    discNumber = discNumber,
    durationMs = durationMs,
    albumArtUri = albumArtUri,
)

/** A list of [count] simple tracks with ids `t1`..`t{count}`. */
fun audioFiles(count: Int): List<AudioFile> =
    (1..count).map { audioFile(id = "t$it", title = "Track $it") }
