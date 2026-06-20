package com.example.androidmusic

import android.app.Application
import android.os.StrictMode
import com.example.androidmusic.diagnostics.CrashHandler
import com.example.androidmusic.domain.diagnostics.DiagnosticReporter
import com.example.androidmusic.domain.time.Clock
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class MusicApplication : Application() {

    @Inject lateinit var diagnosticReporter: DiagnosticReporter

    @Inject lateinit var clock: Clock

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            enableStrictMode()
        }
        installCrashHandler()
    }

    private fun installCrashHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(
            CrashHandler(diagnosticReporter, clock, previous),
        )
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build(),
        )
    }
}
