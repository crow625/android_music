package com.example.androidmusic.domain.repository

import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.ScanReport
import kotlinx.coroutines.flow.Flow

interface AudioFileRepository {
    fun observeLibrary(): Flow<Library>
    suspend fun listAudioFiles(folderUri: MediaUri): List<AudioFile>
    suspend fun scanSources(sourceUris: List<MediaUri>): ScanReport
}
