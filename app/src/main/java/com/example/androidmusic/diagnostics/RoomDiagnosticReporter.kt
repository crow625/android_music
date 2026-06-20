package com.example.androidmusic.diagnostics

import com.example.androidmusic.diagnostics.db.DiagnosticEventDao
import com.example.androidmusic.diagnostics.db.DiagnosticEventEntity
import com.example.androidmusic.domain.concurrency.DispatcherProvider
import com.example.androidmusic.domain.diagnostics.AppError
import com.example.androidmusic.domain.diagnostics.DiagnosticReporter
import com.example.androidmusic.domain.diagnostics.ErrorCategory
import com.example.androidmusic.domain.diagnostics.Severity
import com.example.androidmusic.domain.model.MediaUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [DiagnosticReporter] that persists structured errors to Room (queryable) and
 * mirrors them to a rotating, exportable log file.
 */
@Singleton
class RoomDiagnosticReporter @Inject constructor(
    private val dao: DiagnosticEventDao,
    private val logFileWriter: LogFileWriter,
    private val dispatchers: DispatcherProvider,
) : DiagnosticReporter {

    override suspend fun report(error: AppError): Unit = withContext(dispatchers.io) {
        dao.insert(error.toEntity())
        logFileWriter.append(error.toLogLine())
    }

    override fun observeRecent(limit: Int): Flow<List<AppError>> =
        dao.observeRecent(limit).map { rows -> rows.map { it.toAppError() } }

    override suspend fun exportLog(): MediaUri = MediaUri(logFileWriter.fileUriString())
}

private fun AppError.toEntity() = DiagnosticEventEntity(
    category = category.name,
    severity = severity.name,
    message = message,
    stackTrace = stackTrace,
    contextJson = contextToJson(context),
    occurredAt = occurredAt.toEpochMilli(),
)

private fun DiagnosticEventEntity.toAppError() = AppError(
    category = runCatching { ErrorCategory.valueOf(category) }.getOrDefault(ErrorCategory.Unknown),
    severity = runCatching { Severity.valueOf(severity) }.getOrDefault(Severity.Error),
    message = message,
    stackTrace = stackTrace,
    context = jsonToContext(contextJson),
    occurredAt = Instant.ofEpochMilli(occurredAt),
)

private fun AppError.toLogLine(): String = buildString {
    append(occurredAt.toString())
    append(" [").append(severity.name).append('/').append(category.name).append("] ")
    append(message)
    if (context.isNotEmpty()) {
        append(' ').append(context.toString())
    }
    stackTrace?.let { append('\n').append(it) }
}

private fun contextToJson(context: Map<String, String>): String {
    val json = JSONObject()
    context.forEach { (key, value) -> json.put(key, value) }
    return json.toString()
}

private fun jsonToContext(json: String): Map<String, String> = runCatching {
    if (json.isBlank()) return@runCatching emptyMap<String, String>()
    val obj = JSONObject(json)
    obj.keys().asSequence().associateWith { obj.getString(it) }
}.getOrDefault(emptyMap())
