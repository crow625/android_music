package com.example.androidmusic.di

import com.example.androidmusic.domain.concurrency.DispatcherProvider
import com.example.androidmusic.domain.repository.AudioFileRepository
import com.example.androidmusic.domain.repository.PlaylistRepository
import com.example.androidmusic.domain.usecase.BuildQueueUseCase
import com.example.androidmusic.player.MediaControllerPlaybackController
import com.example.androidmusic.domain.player.PlaybackController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    /** Application-lifetime scope for the playback controller (Main dispatcher: MediaController is main-thread). */
    @Provides
    @Singleton
    fun provideAppScope(dispatchers: DispatcherProvider): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatchers.main)

    @Provides
    fun provideBuildQueueUseCase(
        audioFileRepository: AudioFileRepository,
        playlistRepository: PlaylistRepository,
    ): BuildQueueUseCase = BuildQueueUseCase(audioFileRepository, playlistRepository)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PlaybackBindsModule {

    @Binds
    @Singleton
    abstract fun bindPlaybackController(impl: MediaControllerPlaybackController): PlaybackController
}
