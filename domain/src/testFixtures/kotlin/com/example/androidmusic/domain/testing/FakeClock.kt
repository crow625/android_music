package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.time.Clock
import java.time.Instant
import java.time.ZoneId

/** Controllable [Clock] for deterministic time/timezone tests. */
class FakeClock(
    var instant: Instant = Instant.parse("2026-01-01T00:00:00Z"),
    override val zone: ZoneId = ZoneId.of("UTC"),
) : Clock {
    override fun now(): Instant = instant
}
