package com.example.androidmusic.domain.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryScanMergeTest {

    @Test
    fun `stale ids are existing ids not seen in the latest scan`() {
        val stale = LibraryScanMerge.staleTrackIds(
            existingIds = listOf("a", "b", "c"),
            foundIds = listOf("a", "c"),
        )
        assertEquals(listOf("b"), stale)
    }

    @Test
    fun `nothing is stale when everything was found`() {
        val stale = LibraryScanMerge.staleTrackIds(
            existingIds = listOf("a", "b"),
            foundIds = listOf("a", "b", "x"),
        )
        assertTrue(stale.isEmpty())
    }

    @Test
    fun `empty existing yields no stale ids`() {
        assertTrue(LibraryScanMerge.staleTrackIds(emptyList(), listOf("a")).isEmpty())
    }
}
