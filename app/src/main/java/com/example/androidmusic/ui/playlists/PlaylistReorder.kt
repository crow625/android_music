package com.example.androidmusic.ui.playlists

/**
 * Pure translation of a drag-to-reorder result into a single persistable move.
 *
 * A drag reorders an optimistic local copy of the list; on drop we must persist
 * one [PlaylistDetailEvent.Move] describing where the dragged row started (its
 * index in the [persisted] order) and where it ended up (its index in the
 * [reordered] order). Kept separate from the Composable so it is unit-testable —
 * the drag gesture itself isn't, but this decision is where regressions hide
 * (e.g. a stale index yielding from == to, which silently persists nothing).
 *
 * Returns `null` when there is nothing to persist: no active drag, the key is
 * missing from either list, or the position did not change.
 */
fun reorderMove(
    draggedKey: Long?,
    persisted: List<PlaylistEntryUi>,
    reordered: List<PlaylistEntryUi>,
): PlaylistDetailEvent.Move? {
    val key = draggedKey ?: return null
    val from = persisted.indexOfFirst { it.entryId == key }
    val to = reordered.indexOfFirst { it.entryId == key }
    if (from < 0 || to < 0 || from == to) return null
    return PlaylistDetailEvent.Move(from, to)
}
