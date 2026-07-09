package com.example.androidmusic.ui.playlists

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistReorderTest {

    private fun entries(vararg ids: Long): List<PlaylistEntryUi> =
        ids.map { PlaylistEntryUi(entryId = it, title = "t$it", subtitle = "s", isResolved = true) }

    @Test
    fun `dragging a row down yields a move from its old to its new index`() {
        val move = reorderMove(
            draggedKey = 1L,
            persisted = entries(1, 2, 3),
            reordered = entries(2, 3, 1),
        )
        assertEquals(PlaylistDetailEvent.Move(from = 0, to = 2), move)
    }

    @Test
    fun `dragging a row up yields the reverse move`() {
        val move = reorderMove(
            draggedKey = 3L,
            persisted = entries(1, 2, 3),
            reordered = entries(3, 1, 2),
        )
        assertEquals(PlaylistDetailEvent.Move(from = 2, to = 0), move)
    }

    @Test
    fun `an adjacent swap is a single-step move`() {
        val move = reorderMove(
            draggedKey = 1L,
            persisted = entries(1, 2, 3),
            reordered = entries(2, 1, 3),
        )
        assertEquals(PlaylistDetailEvent.Move(from = 0, to = 1), move)
    }

    // Guards the original bug: a drag that ends where it started (or a stale index)
    // must NOT persist a no-op move.
    @Test
    fun `no net change persists nothing`() {
        val order = entries(1, 2, 3)
        assertNull(reorderMove(draggedKey = 2L, persisted = order, reordered = order))
    }

    @Test
    fun `a null drag key persists nothing`() {
        assertNull(reorderMove(draggedKey = null, persisted = entries(1, 2), reordered = entries(2, 1)))
    }

    @Test
    fun `an unknown key persists nothing`() {
        assertNull(reorderMove(draggedKey = 99L, persisted = entries(1, 2), reordered = entries(2, 1)))
    }
}
