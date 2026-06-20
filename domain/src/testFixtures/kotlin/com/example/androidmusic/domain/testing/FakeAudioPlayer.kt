package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.model.PlayQueue
import com.example.androidmusic.domain.model.PlaybackState
import com.example.androidmusic.domain.player.AudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAudioPlayer : AudioPlayer {
    var lastQueue: PlayQueue? = null
    var lastPlayedIndex: Int? = null

    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val state: StateFlow<PlaybackState> = _state

    override fun setQueue(queue: PlayQueue) { lastQueue = queue }
    override fun playIndex(index: Int) {
        lastPlayedIndex = index
        _state.value = PlaybackState.Playing(index, 0)
    }
    override fun play() = Unit
    override fun pause() = Unit
    override fun stop() { _state.value = PlaybackState.Idle }
    override fun skipToNext() = Unit
    override fun skipToPrevious() = Unit
    override fun seekTo(positionMs: Long) = Unit
}
