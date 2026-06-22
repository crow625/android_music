package com.example.androidmusic.domain.usecase

import com.example.androidmusic.domain.library.LibraryGroupings
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.PlaylistEntry
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.model.ResolvedEntry
import com.example.androidmusic.domain.model.SortOrder
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakePlaylistRepository
import com.example.androidmusic.domain.testing.audioFile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildQueueUseCaseTest {

    private val albumA1 = audioFile(id = "a1", album = "Album A", discNumber = 1, trackNumber = 2)
    private val albumA2 = audioFile(id = "a2", album = "Album A", discNumber = 1, trackNumber = 1)
    private val albumB1 = audioFile(id = "b1", album = "Album B", artist = "Other")

    private fun useCase(
        library: Library = Library(),
        files: FakeAudioFileRepository = FakeAudioFileRepository(library = library),
        playlists: FakePlaylistRepository = FakePlaylistRepository(),
    ) = BuildQueueUseCase(files, playlists)

    @Test
    fun `FromAlbum returns only that album, in disc-track order`() = runTest {
        val queue = useCase(Library(listOf(albumA1, albumA2, albumB1)))
            .invoke(QueueSource.FromAlbum(LibraryGroupings.albumKey(albumA1)))

        assertEquals(listOf("a2", "a1"), queue.items.map { it.id })
        assertEquals(0, queue.currentIndex)
    }

    @Test
    fun `FromArtist returns that artist, ordered by album then track`() = runTest {
        val x1 = audioFile(id = "x1", artist = "Artist X", album = "Z", trackNumber = 1)
        val x2 = audioFile(id = "x2", artist = "Artist X", album = "A", trackNumber = 2)
        val x3 = audioFile(id = "x3", artist = "Artist X", album = "A", trackNumber = 1)

        val queue = useCase(Library(listOf(x1, x2, x3, albumB1)))
            .invoke(QueueSource.FromArtist(LibraryGroupings.artistKey(x1)))

        assertEquals(listOf("x3", "x2", "x1"), queue.items.map { it.id })
    }

    @Test
    fun `FromLibrary sorts by title`() = runTest {
        val banana = audioFile(id = "1", title = "Banana")
        val apple = audioFile(id = "2", title = "apple")
        val cherry = audioFile(id = "3", title = "Cherry")

        val queue = useCase(Library(listOf(banana, apple, cherry)))
            .invoke(QueueSource.FromLibrary(SortOrder.Title))

        assertEquals(listOf("2", "1", "3"), queue.items.map { it.id })
    }

    @Test
    fun `FromFolder returns the folder's files`() = runTest {
        val folder = MediaUri("tree://music/rock")
        val files = FakeAudioFileRepository(
            filesByFolder = mapOf(folder.value to listOf(albumA1, albumB1)),
        )
        val queue = useCase(files = files).invoke(QueueSource.FromFolder(folder))

        assertEquals(listOf("a1", "b1"), queue.items.map { it.id })
    }

    @Test
    fun `FromPlaylist includes only resolved entries`() = runTest {
        fun entry(id: Long) = PlaylistEntry(id, null, "t", "a", "al", "/p", id.toInt())
        val playlists = FakePlaylistRepository(
            resolvedByPlaylist = mapOf(
                7L to listOf(
                    ResolvedEntry(entry(1), albumA1),
                    ResolvedEntry(entry(2), resolvedFile = null),
                    ResolvedEntry(entry(3), albumB1),
                ),
            ),
        )
        val queue = useCase(playlists = playlists).invoke(QueueSource.FromPlaylist(7L))

        assertEquals(listOf("a1", "b1"), queue.items.map { it.id })
    }

    @Test
    fun `FromSingleTrack starts at the track within its context list`() = runTest {
        val context = listOf(albumA2, albumA1, albumB1)
        val queue = useCase().invoke(QueueSource.FromSingleTrack(albumA1, context))

        assertEquals(listOf("a2", "a1", "b1"), queue.items.map { it.id })
        assertEquals(1, queue.currentIndex)
        assertEquals("a1", queue.current?.id)
    }

    @Test
    fun `FromSingleTrack with empty context plays just the track`() = runTest {
        val queue = useCase().invoke(QueueSource.FromSingleTrack(albumA1, emptyList()))

        assertEquals(listOf("a1"), queue.items.map { it.id })
        assertEquals(0, queue.currentIndex)
    }
}
