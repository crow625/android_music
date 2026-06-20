package com.example.androidmusic.domain.usecase

import com.example.androidmusic.domain.model.MediaUri
import com.example.androidmusic.domain.model.SourceFolder
import com.example.androidmusic.domain.repository.SourceRepository
import kotlinx.coroutines.flow.Flow

class ObserveSourcesUseCase(private val sourceRepository: SourceRepository) {
    operator fun invoke(): Flow<List<SourceFolder>> = sourceRepository.observeSources()
}

class AddSourceUseCase(private val sourceRepository: SourceRepository) {
    suspend operator fun invoke(uri: MediaUri) = sourceRepository.addSource(uri)
}

class RemoveSourceUseCase(private val sourceRepository: SourceRepository) {
    suspend operator fun invoke(uri: MediaUri) = sourceRepository.removeSource(uri)
}
