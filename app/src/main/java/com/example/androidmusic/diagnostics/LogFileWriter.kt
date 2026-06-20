package com.example.androidmusic.diagnostics

import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Appends diagnostic lines to a rotating text log in app-private storage. The
 * file is the exportable companion to the queryable `diagnostic_event` table.
 */
class LogFileWriter(private val logDir: File) {

    private val logFile: File get() = File(logDir, FILE_NAME)

    fun append(line: String) {
        try {
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            rotateIfNeeded()
            logFile.appendText(line + "\n")
        } catch (e: IOException) {
            // The diagnostics sink itself failing must not crash the app and
            // cannot recurse into the reporter; fall back to Logcat with the cause.
            Log.w(TAG, "Failed to append to diagnostic log file", e)
        }
    }

    fun fileUriString(): String = logFile.toURI().toString()

    private fun rotateIfNeeded() {
        if (logFile.exists() && logFile.length() > MAX_BYTES) {
            val rolled = File(logDir, "$FILE_NAME.1")
            if (rolled.exists()) {
                rolled.delete()
            }
            logFile.renameTo(rolled)
        }
    }

    companion object {
        private const val TAG = "LogFileWriter"
        private const val FILE_NAME = "app.log"
        private const val MAX_BYTES = 1_000_000L
    }
}
