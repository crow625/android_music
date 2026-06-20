package com.example.androidmusic.domain.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class StableTrackIdTest {

    @Test
    fun `uses MusicBrainz recording id when present`() {
        val id = StableTrackId.compute(
            title = "Song",
            artist = "Artist",
            album = "Album",
            durationMs = 200_000,
            musicBrainzRecordingId = "mbid-123",
        )
        assertEquals("mbid-123", id)
    }

    @Test
    fun `is deterministic and case- and whitespace-insensitive`() {
        val a = StableTrackId.compute("Song", "Artist", "Album", 200_000)
        val b = StableTrackId.compute("  song ", "ARTIST", "album", 200_000)
        assertEquals(a, b)
    }

    @Test
    fun `near-identical durations bucket to the same id`() {
        val a = StableTrackId.compute("Song", "Artist", "Album", 180_000)
        val b = StableTrackId.compute("Song", "Artist", "Album", 179_000)
        assertEquals(a, b)
    }

    @Test
    fun `clearly different durations produce different ids`() {
        val a = StableTrackId.compute("Song", "Artist", "Album", 180_000)
        val b = StableTrackId.compute("Song", "Artist", "Album", 182_000)
        assertNotEquals(a, b)
    }

    @Test
    fun `different album produces a different id`() {
        val a = StableTrackId.compute("Song", "Artist", "Album One", 200_000)
        val b = StableTrackId.compute("Song", "Artist", "Album Two", 200_000)
        assertNotEquals(a, b)
    }

    @Test
    fun `duration bucketing rounds to nearest two seconds`() {
        assertEquals(180_000L, StableTrackId.bucketDuration(179_500))
        assertEquals(0L, StableTrackId.bucketDuration(0))
        assertEquals(0L, StableTrackId.bucketDuration(-5))
    }
}
