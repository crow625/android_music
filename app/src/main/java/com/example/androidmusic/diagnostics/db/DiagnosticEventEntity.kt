package com.example.androidmusic.diagnostics.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room row backing the `diagnostic_event` table. See technical-spec §7.2. */
@Entity(tableName = "diagnostic_event")
data class DiagnosticEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val severity: String,
    val message: String,
    @ColumnInfo(name = "stack_trace") val stackTrace: String?,
    @ColumnInfo(name = "context_json") val contextJson: String,
    @ColumnInfo(name = "occurred_at") val occurredAt: Long,
)
