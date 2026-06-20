package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.model.AudioFile
import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.ScanReport
import com.example.androidmusic.domain.repository.AudioFileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAudioFileRepository(
    var library: Library = Library(),
    var filesByFolder: Map<String, List<AudioFile>> = emptyMap(),
    var scanReport: ScanReport = ScanReport.EMPTY,
) : AudioFileRepository {
    var lastScannedSources: List<MediaUri>? = null

    override fun observeLibrary(): Flow<Library> = flowOf(library)

    override suspend fun listAudioFiles(folderUri: MediaUri): List<AudioFile> =
        filesByFolder[folderUri.value].orEmpty()

    override suspend fun scanSources(sourceUris: List<MediaUri>): ScanReport {
        lastScannedSources = sourceUris
        return scanReport
    }
}
