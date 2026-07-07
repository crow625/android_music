package com.example.androidmusic.domain.playlist

import com.example.androidmusic.domain.model.PlaylistEntry
import com.example.androidmusic.domain.testing.audioFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaylistResolutionTest {

    private fun entry(
        id: Long,
        position: Int,
        trackId: String? = "id-$id",
        filePath: String = "/music/$id.mp3",
    ) = PlaylistEntry(
        id = id,
        trackId = trackId,
        trackTitle = "Title $id",
        trackArtist = "Artist",
        trackAlbum = "Album",
        filePath = filePath,
        position = position,
    )

    @Test
    fun `resolves by stable id even when the file path changed`() {
        val entry = entry(id = 1, position = 0, filePath = "/old/place.mp3")
        val moved = audioFile(id = "id-1", filePath = "/new/place.mp3")

        val resolved = PlaylistResolution.resolve(listOf(entry), listOf(moved))

        assertEquals(moved, resolved.single().resolvedFile)
        assertTrue(resolved.single().isResolved)
    }

    @Test
    fun `falls back to file path when the metadata id no longer matches`() {
        // File was re-tagged: its stable id changed, but it sits at the same path.
        val entry = entry(id = 1, position = 0, trackId = "old-id", filePath = "/music/song.mp3")
        val retagged = audioFile(id = "brand-new-id", filePath = "/music/song.mp3")

        val resolved = PlaylistResolution.resolve(listOf(entry), listOf(retagged))

        assertEquals(retagged, resolved.single().resolvedFile)
    }

    @Test
    fun `marks entry unresolvable when neither id nor path match`() {
        val entry = entry(id = 1, position = 0, trackId = "gone", filePath = "/deleted.mp3")
        val other = audioFile(id = "other", filePath = "/present.mp3")

        val resolved = PlaylistResolution.resolve(listOf(entry), listOf(other)).single()

        assertNull(resolved.resolvedFile)
        assertFalse(resolved.isResolved)
        // The entry is still returned so the UI can show it greyed with a warning.
        assertEquals(1L, resolved.entry.id)
    }

    @Test
    fun `id match takes precedence over a colliding path`() {
        val entry = entry(id = 1, position = 0, trackId = "id-1", filePath = "/shared.mp3")
        val byId = audioFile(id = "id-1", filePath = "/moved.mp3")
        val byPath = audioFile(id = "someone-else", filePath = "/shared.mp3")

        val resolved = PlaylistResolution.resolve(listOf(entry), listOf(byPath, byId)).single()

        assertEquals(byId, resolved.resolvedFile)
    }

    @Test
    fun `returns entries ordered by position then id regardless of input order`() {
        val entries = listOf(
            entry(id = 3, position = 2),
            entry(id = 1, position = 0),
            entry(id = 2, position = 1),
        )
        val library = listOf(audioFile(id = "id-1"), audioFile(id = "id-2"), audioFile(id = "id-3"))

        val order = PlaylistResolution.resolve(entries, library).map { it.entry.id }

        assertEquals(listOf(1L, 2L, 3L), order)
    }

    @Test
    fun `empty library leaves every entry unresolved but preserved`() {
        val entries = listOf(entry(id = 1, position = 0), entry(id = 2, position = 1))

        val resolved = PlaylistResolution.resolve(entries, emptyList())

        assertEquals(2, resolved.size)
        assertTrue(resolved.none { it.isResolved })
    }
}
