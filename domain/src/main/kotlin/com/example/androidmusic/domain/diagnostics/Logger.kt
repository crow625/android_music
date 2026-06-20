package com.example.androidmusic.domain.diagnostics

/** Severity of a log line. */
enum class LogLevel { Debug, Info, Warn, Error }

/**
 * Seam over the logging backend. The domain layer cannot call `android.util.Log`,
 * so all logging goes through this interface; tests use a recording fake.
 */
interface Logger {
    fun log(level: LogLevel, tag: String, message: String, error: Throwable? = null)
}

fun Logger.debug(tag: String, message: String) = log(LogLevel.Debug, tag, message)
fun Logger.info(tag: String, message: String) = log(LogLevel.Info, tag, message)
fun Logger.warn(tag: String, message: String, error: Throwable? = null) =
    log(LogLevel.Warn, tag, message, error)
fun Logger.error(tag: String, message: String, error: Throwable? = null) =
    log(LogLevel.Error, tag, message, error)
