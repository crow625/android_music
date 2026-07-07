package com.example.androidmusic.data.mapper

import com.example.androidmusic.data.db.TrackEntity
import com.example.androidmusic.domain.metadata.TrackMetadata
import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.MediaUri

/** Read mapping: persisted row -> domain model. */
fun TrackEntity.toAudioFile(): AudioFile = AudioFile(
    id = id,
    uri = MediaUri(uri),
    filePath = filePath,
    parentFolderUri = parentFolderUri,
    title = title,
    artist = artist,
    album = album,
    albumArtist = albumArtist,
    trackNumber = trackNumber,
    discNumber = discNumber,
    durationMs = durationMs,
    albumArtUri = albumArtUri?.let { MediaUri(it) },
)

/**
 * Write mapping: parsed [TrackMetadata] for a file -> a persistable row, given
 * the stable [id], its source [folderUri], the file [uri]/[filePath], and the
 * scan timestamp [dateIndexed].
 */
@Suppress("LongParameterList")
fun TrackMetadata.toEntity(
    id: String,
    uri: MediaUri,
    filePath: String,
    folderUri: MediaUri,
    parentFolderUri: String,
    dateIndexed: Long,
): TrackEntity = TrackEntity(
    id = id,
    title = title,
    artist = artist,
    album = album,
    albumArtist = albumArtist,
    trackNumber = trackNumber,
    discNumber = discNumber,
    durationMs = durationMs,
    filePath = filePath,
    uri = uri.value,
    folderUri = folderUri.value,
    parentFolderUri = parentFolderUri,
    albumArtUri = albumArtUri?.value,
    mbRecordingId = musicBrainzRecordingId,
    isStale = false,
    dateIndexed = dateIndexed,
)
