package com.example.androidmusic.domain.model

/** Observable playback state exposed by the player seam. */
sealed interface PlaybackState {
    data object Idle : PlaybackState
    data class Playing(val index: Int, val positionMs: Long) : PlaybackState
    data class Paused(val index: Int, val positionMs: Long) : PlaybackState
    data class Error(val index: Int, val message: String) : PlaybackState
}
