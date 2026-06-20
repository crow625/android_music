package com.example.androidmusic.domain.library

/** Pure reconciliation logic for a library scan. */
object LibraryScanMerge {

    /**
     * Track ids that exist in the index but were not seen in the latest scan of
     * the same sources — i.e. files that have been removed/moved and should be
     * soft-deleted from the library.
     */
    fun staleTrackIds(
        existingIds: Collection<String>,
        foundIds: Collection<String>,
    ): List<String> {
        val found = foundIds.toHashSet()
        return existingIds.toHashSet().filterNot { it in found }
    }
}
