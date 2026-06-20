package com.example.androidmusic.diagnostics.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The single app database (`android_music.db`). Phase 0 contains only the
 * diagnostic event log; later phases add track/playlist/play_event/etc. tables
 * and bump the version with migrations.
 */
@Database(
    entities = [DiagnosticEventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diagnosticEventDao(): DiagnosticEventDao
}
