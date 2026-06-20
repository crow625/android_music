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
class SourceFolderDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SourceFolderDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.sourceFolderDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `insert is idempotent and delete removes`() = runTest {
        dao.insert(SourceFolderEntity("tree://a", addedAt = 1))
        dao.insert(SourceFolderEntity("tree://a", addedAt = 2)) // ignored duplicate
        dao.insert(SourceFolderEntity("tree://b", addedAt = 3))

        assertEquals(listOf("tree://a", "tree://b"), dao.observeAll().first().map { it.uri })

        dao.delete("tree://a")
        assertEquals(listOf("tree://b"), dao.observeAll().first().map { it.uri })
    }
}
