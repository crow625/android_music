package com.example.androidmusic.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "source_folder")
data class SourceFolderEntity(
    @PrimaryKey val uri: String,
    @ColumnInfo(name = "added_at") val addedAt: Long,
)
