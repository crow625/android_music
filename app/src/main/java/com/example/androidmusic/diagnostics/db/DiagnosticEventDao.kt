package com.example.androidmusic.diagnostics.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticEventDao {
    @Insert
    suspend fun insert(event: DiagnosticEventEntity)

    @Query("SELECT * FROM diagnostic_event ORDER BY occurred_at DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<DiagnosticEventEntity>>
}
