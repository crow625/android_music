package com.example.androidmusic.domain.library

import com.example.androidmusic.domain.model.Album
import com.example.androidmusic.domain.model.Artist
import com.example.androidmusic.domain.model.AudioFile

/**
 * Pure grouping of a flat track list into albums and artists. Album identity is
 * (album title + album artist) so same-named albums by different artists stay
 * distinct; artist identity is the performing artist. The same key functions
 * back `BuildQueueUseCase`'s album/artist sources so browsing and playback agree.
 */
object LibraryGroupings {

    fun artistKey(file: AudioFile): String = Normalize.key(file.artist)

    /**
     * Collision-free composite key. Length-prefixing the album part means the
     * boundary between album and artist is unambiguous regardless of content,
     * so no separator character can clash with real metadata.
     */
    fun albumKey(file: AudioFile): String {
        val album = Normalize.key(file.album)
        val artist = Normalize.key(albumArtistOf(file))
        return "${album.length}:$album:$artist"
    }

    fun albums(tracks: List<AudioFile>): List<Album> =
        tracks.groupBy(::albumKey)
            .map { (key, group) ->
                val first = group.first()
                Album(
                    id = key,
                    title = first.album,
                    artist = albumArtistOf(first),
                    trackCount = group.size,
                    artworkUri = group.firstNotNullOfOrNull { it.albumArtUri?.value },
                )
            }
            .sortedBy { Normalize.key(it.title) }

    fun artists(tracks: List<AudioFile>): List<Artist> =
        tracks.groupBy(::artistKey)
            .map { (key, group) ->
                Artist(
                    id = key,
                    name = group.first().artist,
                    albumCount = group.map(::albumKey).distinct().size,
                    trackCount = group.size,
                )
            }
            .sortedBy { Normalize.key(it.name) }

    /** Tracks for an album, in disc then track order. */
    fun albumTracks(tracks: List<AudioFile>, albumId: String): List<AudioFile> =
        tracks.filter { albumKey(it) == albumId }
            .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))

    /** Tracks for an artist, grouped by album then disc/track order. */
    fun artistTracks(tracks: List<AudioFile>, artistId: String): List<AudioFile> =
        tracks.filter { artistKey(it) == artistId }
            .sortedWith(compareBy({ Normalize.key(it.album) }, { it.discNumber }, { it.trackNumber }))

    fun artistAlbums(tracks: List<AudioFile>, artistId: String): List<Album> =
        albums(tracks.filter { artistKey(it) == artistId })

    private fun albumArtistOf(file: AudioFile): String = file.albumArtist.ifBlank { file.artist }
}
