package com.example.androidmusic.domain.diagnostics

import java.time.Instant

/** Broad area of the app an error originated from. */
enum class ErrorCategory { Playback, Scan, Metadata, Database, Permission, Unknown }

/** How serious an error is. */
enum class Severity { Warn, Error, Fatal }

/**
 * A structured, persistable record of something that went wrong. Created at the
 * point of failure and handed to a [DiagnosticReporter]; never swallowed.
 */
data class AppError(
    val category: ErrorCategory,
    val severity: Severity,
    val message: String,
    val stackTrace: String?,
    val context: Map<String, String> = emptyMap(),
    val occurredAt: Instant,
)

/**
 * Builds an [AppError] from a throwable, capturing its message and full stack
 * trace. [occurredAt] is passed in (sourced from the `Clock` seam) rather than
 * read from the system clock, so this stays pure and testable.
 */
fun Throwable.toAppError(
    occurredAt: Instant,
    category: ErrorCategory = ErrorCategory.Unknown,
    severity: Severity = Severity.Error,
    context: Map<String, String> = emptyMap(),
): AppError = AppError(
    category = category,
    severity = severity,
    message = message ?: this::class.simpleName.orEmpty(),
    stackTrace = stackTraceToString(),
    context = context,
    occurredAt = occurredAt,
)
