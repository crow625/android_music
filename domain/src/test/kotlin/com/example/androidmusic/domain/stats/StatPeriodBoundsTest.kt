package com.example.androidmusic.domain.stats

import com.example.androidmusic.domain.model.StatPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class StatPeriodBoundsTest {

    private val utc = ZoneId.of("UTC")
    private val newYork = ZoneId.of("America/New_York")

    private fun epoch(iso: String) = Instant.parse(iso).toEpochMilli()

    @Test
    fun `month bounds in UTC span the calendar month`() {
        val bounds = StatPeriodBounds.of(StatPeriod.Month(2026, 6), utc)
        assertEquals(epoch("2026-06-01T00:00:00Z"), bounds.fromEpochMs)
        assertEquals(epoch("2026-07-01T00:00:00Z"), bounds.toEpochMs)
    }

    @Test
    fun `month bounds shift with local time zone offset`() {
        val bounds = StatPeriodBounds.of(StatPeriod.Month(2026, 6), newYork)
        // June is EDT (UTC-4), so local midnight is 04:00 UTC.
        assertEquals(epoch("2026-06-01T04:00:00Z"), bounds.fromEpochMs)
        assertEquals(epoch("2026-07-01T04:00:00Z"), bounds.toEpochMs)

        val utcBounds = StatPeriodBounds.of(StatPeriod.Month(2026, 6), utc)
        assertNotEquals(utcBounds.fromEpochMs, bounds.fromEpochMs)
    }

    @Test
    fun `a late-night local event lands in the correct local month`() {
        val bounds = StatPeriodBounds.of(StatPeriod.Month(2026, 6), newYork)
        // 23:30 local on the last day of June — in UTC this is already July 1.
        val lateJune = LocalDateTime.of(2026, 6, 30, 23, 30)
            .atZone(newYork).toInstant().toEpochMilli()
        assertTrue(lateJune >= bounds.fromEpochMs && lateJune < bounds.toEpochMs)

        // 00:30 local on July 1 must be excluded from June.
        val earlyJuly = LocalDateTime.of(2026, 7, 1, 0, 30)
            .atZone(newYork).toInstant().toEpochMilli()
        assertTrue(earlyJuly >= bounds.toEpochMs)
    }

    @Test
    fun `year bounds span the calendar year`() {
        val bounds = StatPeriodBounds.of(StatPeriod.Year(2026), utc)
        assertEquals(epoch("2026-01-01T00:00:00Z"), bounds.fromEpochMs)
        assertEquals(epoch("2027-01-01T00:00:00Z"), bounds.toEpochMs)
    }

    @Test
    fun `all-time is unbounded`() {
        val bounds = StatPeriodBounds.of(StatPeriod.AllTime, utc)
        assertEquals(Long.MIN_VALUE, bounds.fromEpochMs)
        assertEquals(Long.MAX_VALUE, bounds.toEpochMs)
    }
}
