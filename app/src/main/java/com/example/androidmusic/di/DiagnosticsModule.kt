package com.example.androidmusic.di

import android.content.Context
import com.example.androidmusic.data.db.AppDatabase
import com.example.androidmusic.diagnostics.DefaultDispatcherProvider
import com.example.androidmusic.diagnostics.LogFileWriter
import com.example.androidmusic.diagnostics.RoomDiagnosticReporter
import com.example.androidmusic.diagnostics.SystemClock
import com.example.androidmusic.diagnostics.TimberLogger
import com.example.androidmusic.diagnostics.db.DiagnosticEventDao
import com.example.androidmusic.domain.concurrency.DispatcherProvider
import com.example.androidmusic.domain.diagnostics.DiagnosticReporter
import com.example.androidmusic.domain.diagnostics.Logger
import com.example.androidmusic.domain.time.Clock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

/** Binds the cross-cutting diagnostics/infrastructure seams to their implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DiagnosticsModule {

    @Binds
    @Singleton
    abstract fun bindLogger(impl: TimberLogger): Logger

    @Binds
    @Singleton
    abstract fun bindClock(impl: SystemClock): Clock

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    @Singleton
    abstract fun bindDiagnosticReporter(impl: RoomDiagnosticReporter): DiagnosticReporter

    companion object {
        @Provides
        fun provideDiagnosticEventDao(database: AppDatabase): DiagnosticEventDao =
            database.diagnosticEventDao()

        @Provides
        @Singleton
        fun provideLogFileWriter(@ApplicationContext context: Context): LogFileWriter =
            LogFileWriter(File(context.filesDir, "logs"))
    }
}
