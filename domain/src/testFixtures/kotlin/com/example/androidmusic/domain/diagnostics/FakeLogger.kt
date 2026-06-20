package com.example.androidmusic.domain.diagnostics

/** Recording [Logger] fake for tests: assert on what was logged. */
class FakeLogger : Logger {
    data class Entry(
        val level: LogLevel,
        val tag: String,
        val message: String,
        val error: Throwable?,
    )

    val entries = mutableListOf<Entry>()

    override fun log(level: LogLevel, tag: String, message: String, error: Throwable?) {
        entries += Entry(level, tag, message, error)
    }
}
