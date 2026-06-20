package com.example.androidmusic.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Upsert
    suspend fun upsert(tracks: List<TrackEntity>)

    @Query("SELECT * FROM track WHERE is_stale = 0 ORDER BY title COLLATE NOCASE")
    fun observeActive(): Flow<List<TrackEntity>>

    @Query("SELECT id FROM track WHERE folder_uri = :folderUri")
    suspend fun idsForFolder(folderUri: String): List<String>

    @Query("UPDATE track SET is_stale = 1 WHERE id IN (:ids)")
    suspend fun markStale(ids: List<String>)

    @Query("DELETE FROM track WHERE folder_uri = :folderUri")
    suspend fun deleteByFolder(folderUri: String)
}
