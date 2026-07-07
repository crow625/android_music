package com.example.androidmusic.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.androidmusic.diagnostics.db.DiagnosticEventDao
import com.example.androidmusic.diagnostics.db.DiagnosticEventEntity

/**
 * The single app database (`android_music.db`).
 *
 * Pre-release the schema is still evolving, so upgrades use destructive
 * migration (see DataModule). Real migrations + exportSchema land before release.
 */
@Database(
    entities = [
        DiagnosticEventEntity::class,
        TrackEntity::class,
        SourceFolderEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diagnosticEventDao(): DiagnosticEventDao
    abstract fun trackDao(): TrackDao
    abstract fun sourceFolderDao(): SourceFolderDao
}
