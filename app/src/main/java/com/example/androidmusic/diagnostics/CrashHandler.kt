package com.example.androidmusic.diagnostics

import android.util.Log
import com.example.androidmusic.domain.diagnostics.DiagnosticReporter
import com.example.androidmusic.domain.diagnostics.Severity
import com.example.androidmusic.domain.diagnostics.toAppError
import com.example.androidmusic.domain.time.Clock
import kotlinx.coroutines.runBlocking

/**
 * Captures otherwise-uncaught exceptions, persists them as a fatal [AppError]
 * before the process dies, then delegates to the previously-installed handler
 * (so the normal crash flow — and the debugger — still runs).
 */
class CrashHandler(
    private val reporter: DiagnosticReporter,
    private val clock: Clock,
    private val delegate: Thread.UncaughtExceptionHandler?,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Best-effort synchronous persist; on failure log the cause rather than
        // swallow it, then always hand off to the delegate.
        runCatching {
            runBlocking {
                reporter.report(
                    throwable.toAppError(occurredAt = clock.now(), severity = Severity.Fatal),
                )
            }
        }.onFailure { Log.e(TAG, "Failed to persist crash report", it) }

        delegate?.uncaughtException(thread, throwable)
    }

    private companion object {
        const val TAG = "CrashHandler"
    }
}
