package com.example.androidmusic.domain.model

import com.example.androidmusic.domain.testing.audioFile
import com.example.androidmusic.domain.testing.audioFiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PlayQueueTest {

    private fun queue(count: Int, currentIndex: Int) =
        PlayQueue(items = audioFiles(count), currentIndex = currentIndex)

    @Test
    fun `current, hasNext and hasPrevious reflect position`() {
        val middle = queue(3, currentIndex = 1)
        assertEquals("t2", middle.current?.id)
        assertTrue(middle.hasNext)
        assertTrue(middle.hasPrevious)

        val first = queue(3, currentIndex = 0)
        assertFalse(first.hasPrevious)

        val last = queue(3, currentIndex = 2)
        assertFalse(last.hasNext)
    }

    @Test
    fun `withCurrentIndex coerces into range`() {
        val q = queue(3, currentIndex = 0)
        assertEquals(2, q.withCurrentIndex(9).currentIndex)
        assertEquals(0, q.withCurrentIndex(-5).currentIndex)
    }

    // --- Navigation -------------------------------------------------------

    @Test
    fun `autoAdvance off stops at end`() {
        assertEquals(2, queue(3, 1).autoAdvanceIndex(RepeatMode.Off))
        assertNull(queue(3, 2).autoAdvanceIndex(RepeatMode.Off))
    }

    @Test
    fun `autoAdvance repeatQueue wraps at end`() {
        assertEquals(0, queue(3, 2).autoAdvanceIndex(RepeatMode.RepeatQueue))
    }

    @Test
    fun `autoAdvance repeatOne stays on current`() {
        assertEquals(2, queue(3, 2).autoAdvanceIndex(RepeatMode.RepeatOne))
    }

    @Test
    fun `manual skipNext wraps when repeating but stops when off`() {
        assertEquals(0, queue(3, 2).skipNextIndex(RepeatMode.RepeatQueue))
        assertEquals(0, queue(3, 2).skipNextIndex(RepeatMode.RepeatOne))
        assertNull(queue(3, 2).skipNextIndex(RepeatMode.Off))
        assertEquals(2, queue(3, 1).skipNextIndex(RepeatMode.Off))
    }

    @Test
    fun `manual skipPrevious wraps when repeating but stops when off`() {
        assertEquals(2, queue(3, 0).skipPreviousIndex(RepeatMode.RepeatQueue))
        assertNull(queue(3, 0).skipPreviousIndex(RepeatMode.Off))
        assertEquals(0, queue(3, 1).skipPreviousIndex(RepeatMode.Off))
    }

    // --- Mutation ---------------------------------------------------------

    @Test
    fun `moveItem keeps the same track current`() {
        val q = queue(4, currentIndex = 2) // current = t3
        val moved = q.moveItem(from = 0, to = 3)
        assertEquals("t3", moved.current?.id)
        assertEquals(listOf("t2", "t3", "t4", "t1"), moved.items.map { it.id })
    }

    @Test
    fun `addNext inserts immediately after current and tracks original order`() {
        val q = queue(3, currentIndex = 0) // current = t1
        val extra = audioFile(id = "x")
        val result = q.addNext(extra)
        assertEquals(listOf("t1", "x", "t2", "t3"), result.items.map { it.id })
        assertTrue(result.originalOrder.any { it.id == "x" })
    }

    @Test
    fun `addToEnd appends`() {
        val result = queue(2, 0).addToEnd(audioFile(id = "x"))
        assertEquals(listOf("t1", "t2", "x"), result.items.map { it.id })
    }

    @Test
    fun `removeAt before current shifts current index down`() {
        val q = queue(4, currentIndex = 2) // current = t3
        val result = q.removeAt(0)
        assertEquals("t3", result.current?.id)
        assertEquals(1, result.currentIndex)
    }

    @Test
    fun `removeAt current keeps index pointing at the next track`() {
        val q = queue(4, currentIndex = 1) // current = t2
        val result = q.removeAt(1)
        assertEquals("t3", result.current?.id)
        assertFalse(result.originalOrder.any { it.id == "t2" })
    }

    @Test
    fun `clearExceptCurrent keeps only the current track`() {
        val result = queue(5, currentIndex = 3).clearExceptCurrent()
        assertEquals(listOf("t4"), result.items.map { it.id })
        assertEquals(0, result.currentIndex)
        assertFalse(result.shuffled)
    }

    // --- Shuffle ----------------------------------------------------------

    @Test
    fun `shuffle keeps current track current and preserves the set of tracks`() {
        val original = queue(6, currentIndex = 2) // current = t3
        val shuffled = original.shuffle(Random(42))

        assertTrue(shuffled.shuffled)
        assertEquals("t3", shuffled.current?.id)
        assertEquals(0, shuffled.currentIndex)
        assertEquals(original.items.map { it.id }.toSet(), shuffled.items.map { it.id }.toSet())
        assertEquals(6, shuffled.items.size)
        assertEquals(original.items, shuffled.originalOrder)
    }

    @Test
    fun `unshuffle restores original order with the same current track`() {
        val original = queue(6, currentIndex = 2) // current = t3
        val roundTrip = original.shuffle(Random(7)).unshuffle()

        assertFalse(roundTrip.shuffled)
        assertEquals(original.items.map { it.id }, roundTrip.items.map { it.id })
        assertEquals("t3", roundTrip.current?.id)
        assertEquals(2, roundTrip.currentIndex)
    }

    @Test
    fun `reshuffling preserves the true original order`() {
        val original = queue(6, currentIndex = 0)
        val twice = original.shuffle(Random(1)).shuffle(Random(2))
        assertEquals(original.items, twice.originalOrder)
        assertEquals(original.items.map { it.id }, twice.unshuffle().items.map { it.id })
    }
}
