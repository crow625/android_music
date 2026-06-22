package com.example.androidmusic.domain.library

import com.example.androidmusic.domain.model.SortOrder
import com.example.androidmusic.domain.testing.audioFile
import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryQueryTest {

    private val tracks = listOf(
        audioFile(id = "1", title = "Aerials", artist = "System of a Down", album = "Toxicity"),
        audioFile(id = "2", title = "Teardrop", artist = "Massive Attack", album = "Mezzanine"),
        audioFile(id = "3", title = "Karma Police", artist = "Radiohead", album = "OK Computer"),
    )

    @Test
    fun `blank query returns all tracks sorted by title`() {
        val result = LibraryQuery.apply(tracks, query = "", sort = SortOrder.Title)
        assertEquals(listOf("Aerials", "Karma Police", "Teardrop"), result.map { it.title })
    }

    @Test
    fun `query matches title, artist or album case-insensitively`() {
        assertEquals(listOf("1"), LibraryQuery.apply(tracks, "aerial", SortOrder.Title).map { it.id })
        assertEquals(listOf("2"), LibraryQuery.apply(tracks, "massive", SortOrder.Title).map { it.id })
        assertEquals(listOf("3"), LibraryQuery.apply(tracks, "ok computer", SortOrder.Title).map { it.id })
    }

    @Test
    fun `no matches yields an empty list`() {
        assertEquals(emptyList<String>(), LibraryQuery.apply(tracks, "zzz", SortOrder.Title).map { it.id })
    }

    @Test
    fun `sort by artist orders by artist name`() {
        val result = LibraryQuery.apply(tracks, query = "", sort = SortOrder.Artist)
        assertEquals(listOf("Massive Attack", "Radiohead", "System of a Down"), result.map { it.artist })
    }

    @Test
    fun `sort by album orders by album title`() {
        val result = LibraryQuery.apply(tracks, query = "", sort = SortOrder.Album)
        assertEquals(listOf("Mezzanine", "OK Computer", "Toxicity"), result.map { it.album })
    }
}
