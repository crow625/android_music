package com.example.androidmusic.data.repository

import androidx.room.Room
import com.example.androidmusic.data.db.AppDatabase
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.testing.FakeAudioFileRepository
import com.example.androidmusic.domain.testing.FakeClock
import com.example.androidmusic.domain.testing.audioFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RoomPlaylistRepositoryTest {

    private lateinit var db: AppDatabase
    private val files = FakeAudioFileRepository()
    private lateinit var repository: RoomPlaylistRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomPlaylistRepository(db, db.playlistDao(), files, FakeClock())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `create rename and delete are observable`() = runTest {
        val created = repository.createPlaylist("Chill")
        assertEquals(listOf("Chill"), repository.getPlaylists().first().map { it.name })

        repository.renamePlaylist(created.id, "Focus")
        assertEquals(listOf("Focus"), repository.getPlaylists().first().map { it.name })

        repository.deletePlaylist(created.id)
        assertTrue(repository.getPlaylists().first().isEmpty())
    }

    @Test
    fun `adding tracks appends contiguous positions`() = runTest {
        val id = repository.createPlaylist("Mix").id
        repository.addTrack(id, audioFile(id = "a"))
        repository.addTrack(id, audioFile(id = "b"))
        repository.addTrack(id, audioFile(id = "c"))

        val entries = repository.getPlaylists().first().single().entries
        assertEquals(listOf("a", "b", "c"), entries.map { it.trackId })
        assertEquals(listOf(0, 1, 2), entries.map { it.position })
    }

    @Test
    fun `removing a track closes the position gap`() = runTest {
        val id = repository.createPlaylist("Mix").id
        listOf("a", "b", "c").forEach { repository.addTrack(id, audioFile(id = it)) }
        val middle = repository.getPlaylists().first().single().entries[1]

        repository.removeTrack(id, middle.id)

        val entries = repository.getPlaylists().first().single().entries
        assertEquals(listOf("a", "c"), entries.map { it.trackId })
        assertEquals(listOf(0, 1), entries.map { it.position })
    }

    @Test
    fun `reorder moves an entry and reindexes positions`() = runTest {
        val id = repository.createPlaylist("Mix").id
        listOf("a", "b", "c").forEach { repository.addTrack(id, audioFile(id = it)) }

        repository.reorderTrack(id, from = 0, to = 2)

        val entries = repository.getPlaylists().first().single().entries
        assertEquals(listOf("b", "c", "a"), entries.map { it.trackId })
        assertEquals(listOf(0, 1, 2), entries.map { it.position })
    }

    @Test
    fun `deleting a playlist cascades to its entries`() = runTest {
        val id = repository.createPlaylist("Mix").id
        repository.addTrack(id, audioFile(id = "a"))

        repository.deletePlaylist(id)

        // A fresh playlist that reuses no ids must not inherit orphaned entries.
        val newId = repository.createPlaylist("Mix2").id
        assertTrue(repository.getPlaylists().first().single { it.id == newId }.entries.isEmpty())
    }

    @Test
    fun `resolveEntries resolves against the live library and greys the missing`() = runTest {
        files.library = Library(listOf(audioFile(id = "a"), audioFile(id = "b")))
        val id = repository.createPlaylist("Mix").id
        repository.addTrack(id, audioFile(id = "a"))
        repository.addTrack(id, audioFile(id = "gone"))

        val resolved = repository.resolveEntries(id)

        assertEquals("a", resolved[0].resolvedFile?.id)
        assertTrue(resolved[0].isResolved)
        assertNull(resolved[1].resolvedFile)
        assertFalse(resolved[1].isResolved)
    }
}
