package com.example.androidmusic.diagnostics

import android.util.Log
import com.example.androidmusic.domain.diagnostics.LogLevel
import com.example.androidmusic.domain.diagnostics.Logger
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** [Logger] implementation that routes to Timber (and thus Logcat in debug). */
@Singleton
class TimberLogger @Inject constructor() : Logger {

    override fun log(level: LogLevel, tag: String, message: String, error: Throwable?) {
        val priority = when (level) {
            LogLevel.Debug -> Log.DEBUG
            LogLevel.Info -> Log.INFO
            LogLevel.Warn -> Log.WARN
            LogLevel.Error -> Log.ERROR
        }
        Timber.tag(tag).log(priority, error, message)
    }
}
