package com.example.androidmusic.domain.diagnostics

import com.example.androidmusic.domain.model.MediaUri
import kotlinx.coroutines.flow.Flow

/**
 * Seam for structured error reporting. Implementations persist errors to a
 * queryable store and an exportable log file, and surface them to the user.
 *
 * The rule across the codebase: every `catch` either handles a documented,
 * expected condition or calls [report] — errors are never silently swallowed.
 */
interface DiagnosticReporter {
    suspend fun report(error: AppError)

    /** Most recent errors, newest first, for the Diagnostics screen. */
    fun observeRecent(limit: Int = 100): Flow<List<AppError>>

    /** Writes the current log to a shareable file and returns its location. */
    suspend fun exportLog(): MediaUri
}
