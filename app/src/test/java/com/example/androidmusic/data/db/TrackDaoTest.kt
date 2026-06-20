package com.example.androidmusic.data.db

import androidx.room.Room
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TrackDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TrackDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.trackDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `observeActive excludes stale tracks and sorts by title case-insensitively`() = runTest {
        dao.upsert(
            listOf(
                track(id = "1", title = "Banana"),
                track(id = "2", title = "apple"),
                track(id = "3", title = "Cherry", stale = true),
            ),
        )

        val active = dao.observeActive().first()
        assertEquals(listOf("2", "1"), active.map { it.id })
    }

    @Test
    fun `upsert replaces an existing row by id`() = runTest {
        dao.upsert(listOf(track(id = "1", title = "Old")))
        dao.upsert(listOf(track(id = "1", title = "New")))

        val active = dao.observeActive().first()
        assertEquals(1, active.size)
        assertEquals("New", active.first().title)
    }

    @Test
    fun `markStale hides tracks from the active view`() = runTest {
        dao.upsert(listOf(track(id = "1"), track(id = "2")))
        dao.markStale(listOf("1"))

        assertEquals(listOf("2"), dao.observeActive().first().map { it.id })
    }

    @Test
    fun `idsForFolder and deleteByFolder operate per source folder`() = runTest {
        dao.upsert(
            listOf(
                track(id = "1", folder = "tree://a"),
                track(id = "2", folder = "tree://a"),
                track(id = "3", folder = "tree://b"),
            ),
        )
        assertEquals(setOf("1", "2"), dao.idsForFolder("tree://a").toSet())

        dao.deleteByFolder("tree://a")
        assertEquals(listOf("3"), dao.observeActive().first().map { it.id })
    }

    private fun track(
        id: String,
        title: String = id,
        folder: String = "tree://f",
        stale: Boolean = false,
    ) = TrackEntity(
        id = id,
        title = title,
        artist = "Artist",
        album = "Album",
        albumArtist = "Artist",
        trackNumber = 1,
        discNumber = 1,
        durationMs = 1_000,
        filePath = "/music/$id.mp3",
        uri = "file:///music/$id.mp3",
        folderUri = folder,
        albumArtUri = null,
        mbRecordingId = null,
        isStale = stale,
        dateIndexed = 0,
    )
}
