package com.example.androidmusic.domain.diagnostics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class AppErrorTest {

    @Test
    fun `toAppError captures message, stack trace, category and context`() {
        val throwable = IllegalStateException("boom")
        val now = Instant.parse("2026-06-20T12:00:00Z")

        val error = throwable.toAppError(
            occurredAt = now,
            category = ErrorCategory.Playback,
            severity = Severity.Fatal,
            context = mapOf("trackId" to "abc123"),
        )

        assertEquals("boom", error.message)
        assertEquals(ErrorCategory.Playback, error.category)
        assertEquals(Severity.Fatal, error.severity)
        assertEquals(now, error.occurredAt)
        assertEquals("abc123", error.context["trackId"])
        assertNotNull(error.stackTrace)
        assertTrue(error.stackTrace!!.contains("IllegalStateException"))
    }

    @Test
    fun `toAppError falls back to class name when throwable has no message`() {
        val error = NullPointerException().toAppError(occurredAt = Instant.EPOCH)

        assertEquals("NullPointerException", error.message)
        assertEquals(ErrorCategory.Unknown, error.category)
        assertEquals(Severity.Error, error.severity)
    }

    @Test
    fun `FakeLogger records what was logged`() {
        val logger = FakeLogger()

        logger.warn(tag = "Scan", message = "could not read file")

        assertEquals(1, logger.entries.size)
        val entry = logger.entries.first()
        assertEquals(LogLevel.Warn, entry.level)
        assertEquals("Scan", entry.tag)
    }
}
