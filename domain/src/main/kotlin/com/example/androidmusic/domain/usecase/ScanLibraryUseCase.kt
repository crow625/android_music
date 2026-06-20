package com.example.androidmusic.domain.usecase

import com.example.androidmusic.domain.model.ScanReport
import com.example.androidmusic.domain.repository.AudioFileRepository
import com.example.androidmusic.domain.repository.SourceRepository
import kotlinx.coroutines.flow.first

/** Rescans all current source folders and returns the resulting [ScanReport]. */
class ScanLibraryUseCase(
    private val sourceRepository: SourceRepository,
    private val audioFileRepository: AudioFileRepository,
) {
    suspend operator fun invoke(): ScanReport {
        val sources = sourceRepository.observeSources().first().map { it.uri }
        return audioFileRepository.scanSources(sources)
    }
}
