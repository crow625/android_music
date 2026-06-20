package com.example.androidmusic.domain.player

import com.example.androidmusic.domain.model.PlayQueue
import com.example.androidmusic.domain.model.PlaybackState
import kotlinx.coroutines.flow.StateFlow

/**
 * Service-side player seam, implemented by ExoAudioPlayer inside the
 * MediaLibraryService. ViewModels do NOT use this directly — they use
 * [PlaybackController].
 */
interface AudioPlayer {
    fun setQueue(queue: PlayQueue)
    fun playIndex(index: Int)
    fun play()
    fun pause()
    fun stop()
    fun skipToNext()
    fun skipToPrevious()
    fun seekTo(positionMs: Long)
    val state: StateFlow<PlaybackState>
}
