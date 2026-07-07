package com.example.androidmusic.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Transaction
    @Query("SELECT * FROM playlist ORDER BY name COLLATE NOCASE")
    fun observePlaylists(): Flow<List<PlaylistWithEntries>>

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlist SET name = :name WHERE id = :id")
    suspend fun renamePlaylist(id: Long, name: String)

    @Query("DELETE FROM playlist WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("SELECT COALESCE(MAX(position), -1) FROM playlist_entry WHERE playlist_id = :playlistId")
    suspend fun maxPosition(playlistId: Long): Int

    @Insert
    suspend fun insertEntry(entry: PlaylistEntryEntity): Long

    @Query("DELETE FROM playlist_entry WHERE id = :entryId")
    suspend fun deleteEntry(entryId: Long)

    @Query("SELECT * FROM playlist_entry WHERE playlist_id = :playlistId ORDER BY position")
    suspend fun entriesFor(playlistId: Long): List<PlaylistEntryEntity>

    @Update
    suspend fun updateEntries(entries: List<PlaylistEntryEntity>)
}
