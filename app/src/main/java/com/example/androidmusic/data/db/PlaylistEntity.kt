package com.example.androidmusic.data.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** A user-created playlist (technical-spec §7.3). */
@Entity(tableName = "playlist")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

/**
 * One track slot in a playlist. Stores resilient identity (the metadata-derived
 * [trackId] plus [filePath]) captured at add-time so entries can be re-resolved
 * against the library after files move — see `PlaylistResolution`.
 *
 * Deleting the parent playlist cascades to its entries.
 */
@Entity(
    tableName = "playlist_entry",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlist_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("playlist_id")],
)
data class PlaylistEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "playlist_id") val playlistId: Long,
    @ColumnInfo(name = "track_id") val trackId: String?,
    @ColumnInfo(name = "track_title") val trackTitle: String,
    @ColumnInfo(name = "track_artist") val trackArtist: String,
    @ColumnInfo(name = "track_album") val trackAlbum: String,
    @ColumnInfo(name = "file_path") val filePath: String,
    val position: Int,
)

/** A playlist with its entries, assembled by Room's `@Relation`. */
data class PlaylistWithEntries(
    @Embedded val playlist: PlaylistEntity,
    @Relation(parentColumn = "id", entityColumn = "playlist_id")
    val entries: List<PlaylistEntryEntity>,
)
