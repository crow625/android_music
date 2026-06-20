package com.example.androidmusic.domain.stats

import com.example.androidmusic.domain.model.StatPeriod
import java.time.LocalDate
import java.time.ZoneId

/**
 * Converts a [StatPeriod] into a half-open epoch-millisecond range `[from, to)`,
 * computed in the supplied (local) time zone so day/month boundaries land
 * correctly regardless of UTC offset (technical-spec §8.2).
 */
object StatPeriodBounds {

    data class Bounds(val fromEpochMs: Long, val toEpochMs: Long)

    fun of(period: StatPeriod, zone: ZoneId): Bounds = when (period) {
        is StatPeriod.Month -> {
            val start = LocalDate.of(period.year, period.month, 1).atStartOfDay(zone)
            Bounds(start.toInstant().toEpochMilli(), start.plusMonths(1).toInstant().toEpochMilli())
        }

        is StatPeriod.Year -> {
            val start = LocalDate.of(period.year, 1, 1).atStartOfDay(zone)
            Bounds(start.toInstant().toEpochMilli(), start.plusYears(1).toInstant().toEpochMilli())
        }

        StatPeriod.AllTime -> Bounds(Long.MIN_VALUE, Long.MAX_VALUE)
    }
}
