package com.example.androidmusic.domain.model

import kotlin.random.Random

/**
 * An ordered list of tracks plus the index of the one that is current.
 *
 * Immutable: every operation returns a new [PlayQueue]. Shuffle is owned here
 * (not by the player) so the queue the user sees always matches what plays, and
 * so the shuffle/restore logic is unit-testable. [originalOrder] retains the
 * pre-shuffle order so toggling shuffle off can restore it.
 */
data class PlayQueue(
    val items: List<AudioFile>,
    val currentIndex: Int,
    val shuffled: Boolean = false,
    val originalOrder: List<AudioFile> = items,
) {
    val current: AudioFile? get() = items.getOrNull(currentIndex)
    val hasNext: Boolean get() = currentIndex < items.lastIndex
    val hasPrevious: Boolean get() = currentIndex > 0
    val isEmpty: Boolean get() = items.isEmpty()

    fun withCurrentIndex(index: Int): PlayQueue =
        if (items.isEmpty()) this else copy(currentIndex = index.coerceIn(0, items.lastIndex))

    /** Index to play when the current track finishes naturally, or null to stop. */
    fun autoAdvanceIndex(repeatMode: RepeatMode): Int? = when (repeatMode) {
        RepeatMode.RepeatOne -> currentIndex.takeIf { current != null }
        RepeatMode.Off -> (currentIndex + 1).takeIf { it <= items.lastIndex }
        RepeatMode.RepeatQueue ->
            if (items.isEmpty()) null else (currentIndex + 1).takeIf { it <= items.lastIndex } ?: 0
    }

    /** Index for a manual "skip next" (ignores RepeatOne; wraps when repeating). */
    fun skipNextIndex(repeatMode: RepeatMode): Int? = when {
        items.isEmpty() -> null
        currentIndex < items.lastIndex -> currentIndex + 1
        repeatMode == RepeatMode.Off -> null
        else -> 0
    }

    /** Index for a manual "skip previous" (wraps to the end when repeating). */
    fun skipPreviousIndex(repeatMode: RepeatMode): Int? = when {
        items.isEmpty() -> null
        currentIndex > 0 -> currentIndex - 1
        repeatMode == RepeatMode.Off -> null
        else -> items.lastIndex
    }

    fun moveItem(from: Int, to: Int): PlayQueue {
        if (from !in items.indices || to !in items.indices || from == to) return this
        val currentId = current?.id
        val reordered = items.toMutableList().apply { add(to, removeAt(from)) }
        return copy(items = reordered, currentIndex = reordered.indexOfIdOr(currentId, currentIndex))
    }

    fun addNext(track: AudioFile): PlayQueue {
        if (items.isEmpty()) return singleton(track)
        val reordered = items.toMutableList().apply { add(currentIndex + 1, track) }
        return copy(items = reordered, originalOrder = originalOrder + track)
    }

    fun addToEnd(track: AudioFile): PlayQueue {
        if (items.isEmpty()) return singleton(track)
        return copy(items = items + track, originalOrder = originalOrder + track)
    }

    fun removeAt(index: Int): PlayQueue {
        if (index !in items.indices) return this
        val removed = items[index]
        val reordered = items.toMutableList().apply { removeAt(index) }
        val newOriginal = originalOrder.removeFirstById(removed.id)
        val newIndex = when {
            reordered.isEmpty() -> 0
            index < currentIndex -> currentIndex - 1
            index == currentIndex -> currentIndex.coerceAtMost(reordered.lastIndex)
            else -> currentIndex
        }
        return copy(items = reordered, currentIndex = newIndex, originalOrder = newOriginal)
    }

    /** Keeps only the current track (the "Clear queue" action). */
    fun clearExceptCurrent(): PlayQueue {
        val cur = current ?: return copy(items = emptyList(), currentIndex = 0, originalOrder = emptyList())
        return copy(items = listOf(cur), currentIndex = 0, originalOrder = listOf(cur), shuffled = false)
    }

    /** Shuffles the queue, keeping the current track current (moved to the front). */
    fun shuffle(random: Random): PlayQueue {
        val base = if (shuffled) originalOrder else items
        if (items.size <= 1) return copy(shuffled = true, originalOrder = base)
        val cur = current
        val rest = items.filterIndexed { index, _ -> index != currentIndex }.shuffled(random)
        val reordered = if (cur != null) listOf(cur) + rest else rest
        return copy(
            items = reordered,
            currentIndex = if (cur != null) 0 else currentIndex.coerceIn(0, reordered.lastIndex),
            shuffled = true,
            originalOrder = base,
        )
    }

    /** Restores [originalOrder], keeping the current track current. */
    fun unshuffle(): PlayQueue {
        if (!shuffled) return this
        val cur = current
        val restoreIndex = if (cur != null) {
            originalOrder.indexOfFirst { it.id == cur.id }.coerceAtLeast(0)
        } else {
            currentIndex.coerceIn(0, originalOrder.lastIndex.coerceAtLeast(0))
        }
        return copy(items = originalOrder, currentIndex = restoreIndex, shuffled = false)
    }

    private fun singleton(track: AudioFile) =
        copy(items = listOf(track), currentIndex = 0, originalOrder = listOf(track))

    companion object {
        val EMPTY = PlayQueue(items = emptyList(), currentIndex = 0)
    }
}

private fun List<AudioFile>.indexOfIdOr(id: String?, fallback: Int): Int {
    if (id == null) return fallback.coerceIn(0, lastIndex.coerceAtLeast(0))
    val index = indexOfFirst { it.id == id }
    return if (index >= 0) index else fallback.coerceIn(0, lastIndex.coerceAtLeast(0))
}

private fun List<AudioFile>.removeFirstById(id: String): List<AudioFile> {
    val index = indexOfFirst { it.id == id }
    return if (index < 0) this else toMutableList().apply { removeAt(index) }
}
