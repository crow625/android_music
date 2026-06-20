package com.example.androidmusic.domain.time

import java.time.Instant
import java.time.ZoneId

/**
 * Seam over the system clock and time zone. Domain/stats logic depends on this
 * instead of calling [Instant.now] directly, so time can be controlled in tests
 * and local-timezone bucketing is explicit.
 */
interface Clock {
    fun now(): Instant
    val zone: ZoneId
}
