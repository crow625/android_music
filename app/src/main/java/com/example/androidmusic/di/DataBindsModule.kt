package com.example.androidmusic.di

import com.example.androidmusic.data.AndroidMetadataReader
import com.example.androidmusic.data.repository.MediaStoreAudioRepository
import com.example.androidmusic.data.repository.RoomSourceRepository
import com.example.androidmusic.domain.metadata.MetadataReader
import com.example.androidmusic.domain.repository.AudioFileRepository
import com.example.androidmusic.domain.repository.SourceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds the data-layer repository/reader seams to their implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {

    @Binds
    @Singleton
    abstract fun bindAudioFileRepository(impl: MediaStoreAudioRepository): AudioFileRepository

    @Binds
    @Singleton
    abstract fun bindSourceRepository(impl: RoomSourceRepository): SourceRepository

    @Binds
    @Singleton
    abstract fun bindMetadataReader(impl: AndroidMetadataReader): MetadataReader
}
