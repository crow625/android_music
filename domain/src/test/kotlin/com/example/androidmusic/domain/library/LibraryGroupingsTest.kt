package com.example.androidmusic.domain.library

import com.example.androidmusic.domain.testing.audioFile
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryGroupingsTest {

    @Test
    fun `albums groups tracks by album and album artist, sorted by title`() {
        val tracks = listOf(
            audioFile(id = "1", album = "Bravado", artist = "X"),
            audioFile(id = "2", album = "Bravado", artist = "X"),
            audioFile(id = "3", album = "Anthem", artist = "Y"),
        )
        val albums = LibraryGroupings.albums(tracks)

        assertEquals(listOf("Anthem", "Bravado"), albums.map { it.title })
        assertEquals(2, albums.first { it.title == "Bravado" }.trackCount)
    }

    @Test
    fun `same album title by different album artists are distinct albums`() {
        val tracks = listOf(
            audioFile(id = "1", album = "Greatest Hits", artist = "A", albumArtist = "A"),
            audioFile(id = "2", album = "Greatest Hits", artist = "B", albumArtist = "B"),
        )
        assertEquals(2, LibraryGroupings.albums(tracks).size)
    }

    @Test
    fun `artists groups by performing artist with album and track counts`() {
        val tracks = listOf(
            audioFile(id = "1", artist = "X", album = "One"),
            audioFile(id = "2", artist = "X", album = "One"),
            audioFile(id = "3", artist = "X", album = "Two"),
            audioFile(id = "4", artist = "Y", album = "Three"),
        )
        val artists = LibraryGroupings.artists(tracks)

        val x = artists.first { it.name == "X" }
        assertEquals(2, x.albumCount)
        assertEquals(3, x.trackCount)
        assertEquals(listOf("X", "Y"), artists.map { it.name })
    }

    @Test
    fun `albumTracks are ordered by disc then track`() {
        val key = LibraryGroupings.albumKey(audioFile(album = "A", artist = "X"))
        val tracks = listOf(
            audioFile(id = "d2t1", album = "A", artist = "X", discNumber = 2, trackNumber = 1),
            audioFile(id = "d1t2", album = "A", artist = "X", discNumber = 1, trackNumber = 2),
            audioFile(id = "d1t1", album = "A", artist = "X", discNumber = 1, trackNumber = 1),
        )
        assertEquals(
            listOf("d1t1", "d1t2", "d2t1"),
            LibraryGroupings.albumTracks(tracks, key).map { it.id },
        )
    }

    @Test
    fun `folders group by parent folder uri with track counts`() {
        val tracks = listOf(
            audioFile(id = "1", parentFolderUri = "tree://a"),
            audioFile(id = "2", parentFolderUri = "tree://a"),
            audioFile(id = "3", parentFolderUri = "tree://b"),
        )
        val folders = LibraryGroupings.folders(tracks)

        assertEquals(listOf("tree://a", "tree://b"), folders.map { it.uri })
        assertEquals(2, folders.first { it.uri == "tree://a" }.trackCount)
    }

    @Test
    fun `albumKey is case- and whitespace-insensitive`() {
        val a = LibraryGroupings.albumKey(audioFile(album = "Toxicity", artist = "SOAD"))
        val b = LibraryGroupings.albumKey(audioFile(album = " toxicity ", artist = "soad"))
        assertEquals(a, b)
    }
}
