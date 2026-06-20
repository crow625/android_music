package com.example.androidmusic.diagnostics

import com.example.androidmusic.domain.time.Clock
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** [Clock] backed by the real system clock and default time zone. */
@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun now(): Instant = Instant.now()
    override val zone: ZoneId get() = ZoneId.systemDefault()
}
