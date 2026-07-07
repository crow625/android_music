package com.example.androidmusic.data.mapper

import com.example.androidmusic.data.db.PlaylistEntryEntity
import com.example.androidmusic.data.db.PlaylistWithEntries
import com.example.androidmusic.domain.model.Playlist
import com.example.androidmusic.domain.model.PlaylistEntry

/** Read mapping: persisted playlist + its entries -> domain model (entries in position order). */
fun PlaylistWithEntries.toDomain(): Playlist = Playlist(
    id = playlist.id,
    name = playlist.name,
    entries = entries.sortedBy { it.position }.map { it.toDomain() },
)

fun PlaylistEntryEntity.toDomain(): PlaylistEntry = PlaylistEntry(
    id = id,
    trackId = trackId,
    trackTitle = trackTitle,
    trackArtist = trackArtist,
    trackAlbum = trackAlbum,
    filePath = filePath,
    position = position,
)
