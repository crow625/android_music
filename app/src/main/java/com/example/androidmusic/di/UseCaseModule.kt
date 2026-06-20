package com.example.androidmusic.di

import com.example.androidmusic.domain.repository.AudioFileRepository
import com.example.androidmusic.domain.repository.SourceRepository
import com.example.androidmusic.domain.usecase.AddSourceUseCase
import com.example.androidmusic.domain.usecase.ObserveLibraryUseCase
import com.example.androidmusic.domain.usecase.ObserveSourcesUseCase
import com.example.androidmusic.domain.usecase.RemoveSourceUseCase
import com.example.androidmusic.domain.usecase.ScanLibraryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Provides domain use cases. They are plain Kotlin (no @Inject) to keep the
 * domain free of DI annotations, so the app constructs them here.
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideObserveLibraryUseCase(repository: AudioFileRepository) =
        ObserveLibraryUseCase(repository)

    @Provides
    fun provideObserveSourcesUseCase(repository: SourceRepository) =
        ObserveSourcesUseCase(repository)

    @Provides
    fun provideAddSourceUseCase(repository: SourceRepository) = AddSourceUseCase(repository)

    @Provides
    fun provideRemoveSourceUseCase(repository: SourceRepository) = RemoveSourceUseCase(repository)

    @Provides
    fun provideScanLibraryUseCase(
        sourceRepository: SourceRepository,
        audioFileRepository: AudioFileRepository,
    ) = ScanLibraryUseCase(sourceRepository, audioFileRepository)
}
