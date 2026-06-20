package com.example.androidmusic.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Row in the `track` index (technical-spec §7.2). `id` is the stable track id. */
@Entity(
    tableName = "track",
    indices = [Index("artist"), Index("album"), Index("folder_uri")],
)
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    @ColumnInfo(name = "album_artist") val albumArtist: String,
    @ColumnInfo(name = "track_number") val trackNumber: Int,
    @ColumnInfo(name = "disc_number") val discNumber: Int,
    @ColumnInfo(name = "duration_ms") val durationMs: Long,
    @ColumnInfo(name = "file_path") val filePath: String,
    val uri: String,
    @ColumnInfo(name = "folder_uri") val folderUri: String,
    @ColumnInfo(name = "album_art_uri") val albumArtUri: String?,
    @ColumnInfo(name = "mb_recording_id") val mbRecordingId: String?,
    @ColumnInfo(name = "is_stale") val isStale: Boolean,
    @ColumnInfo(name = "date_indexed") val dateIndexed: Long,
)
