package com.example.androidmusic.domain.model

/** Live playback position within the current track. */
data class PlaybackProgress(
    val positionMs: Long = 0,
    val durationMs: Long = 0,
)
