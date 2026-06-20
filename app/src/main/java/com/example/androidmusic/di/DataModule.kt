package com.example.androidmusic.di

import android.content.Context
import androidx.room.Room
import com.example.androidmusic.data.db.AppDatabase
import com.example.androidmusic.data.db.SourceFolderDao
import com.example.androidmusic.data.db.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Provides the Room database and DAOs. */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "android_music.db")
            // Pre-release only: schema still evolving. Replaced with real
            // migrations + exportSchema before any release.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTrackDao(database: AppDatabase): TrackDao = database.trackDao()

    @Provides
    fun provideSourceFolderDao(database: AppDatabase): SourceFolderDao = database.sourceFolderDao()
}
