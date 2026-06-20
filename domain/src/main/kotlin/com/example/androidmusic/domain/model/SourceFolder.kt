package com.example.androidmusic.domain.model

import java.time.Instant

/** A user-selected folder that the library scans for audio files. */
data class SourceFolder(
    val uri: MediaUri,
    val addedAt: Instant,
)
