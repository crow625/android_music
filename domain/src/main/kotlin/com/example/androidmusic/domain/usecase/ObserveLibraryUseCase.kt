package com.example.androidmusic.domain.usecase

import com.example.androidmusic.domain.model.Library
import com.example.androidmusic.domain.repository.AudioFileRepository
import kotlinx.coroutines.flow.Flow

class ObserveLibraryUseCase(private val audioFileRepository: AudioFileRepository) {
    operator fun invoke(): Flow<Library> = audioFileRepository.observeLibrary()
}
