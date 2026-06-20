package com.example.androidmusic.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceFolderDao {

    @Query("SELECT * FROM source_folder ORDER BY added_at")
    fun observeAll(): Flow<List<SourceFolderEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(source: SourceFolderEntity)

    @Query("DELETE FROM source_folder WHERE uri = :uri")
    suspend fun delete(uri: String)
}
