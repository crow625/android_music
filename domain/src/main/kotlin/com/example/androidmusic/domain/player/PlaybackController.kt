package com.example.androidmusic.domain.player

import com.example.androidmusic.domain.model.PlayQueue
import com.example.androidmusic.domain.model.PlaybackState
import com.example.androidmusic.domain.model.QueueSource
import kotlinx.coroutines.flow.StateFlow

/** Connection state of the client-side controller to the player service. */
enum class ConnectionState { Connecting, Connected, Disconnected }

/**
 * Client-side player seam used by ViewModels. Backed by a Media3 MediaController
 * that connects to the service asynchronously, hence the [connection] state.
 */
interface PlaybackController {
    val connection: StateFlow<ConnectionState>
    val state: StateFlow<PlaybackState>

    /** The queue currently loaded into the player (drives the now-playing/queue UI). */
    val queue: StateFlow<PlayQueue>

    /** Builds a queue from [source] and starts playback (the source encodes where to start). */
    fun playSource(source: QueueSource)
    fun play()
    fun pause()
    fun skipToNext()
    fun skipToPrevious()
    fun seekTo(positionMs: Long)
    fun toggleShuffle()
    fun cycleRepeatMode()
}
