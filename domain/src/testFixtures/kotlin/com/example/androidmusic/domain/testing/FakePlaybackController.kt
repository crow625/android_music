package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.model.PlaybackState
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.player.ConnectionState
import com.example.androidmusic.domain.player.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePlaybackController : PlaybackController {
    var lastSource: QueueSource? = null
    var lastStartIndex: Int? = null
    var shuffleToggles = 0
    var repeatCycles = 0

    private val _connection = MutableStateFlow(ConnectionState.Connected)
    override val connection: StateFlow<ConnectionState> = _connection

    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val state: StateFlow<PlaybackState> = _state

    fun setConnection(value: ConnectionState) { _connection.value = value }
    fun setState(value: PlaybackState) { _state.value = value }

    override fun playSource(source: QueueSource, startIndex: Int) {
        lastSource = source
        lastStartIndex = startIndex
        _state.value = PlaybackState.Playing(startIndex, 0)
    }

    override fun play() = Unit
    override fun pause() = Unit
    override fun skipToNext() = Unit
    override fun skipToPrevious() = Unit
    override fun seekTo(positionMs: Long) = Unit
    override fun toggleShuffle() { shuffleToggles++ }
    override fun cycleRepeatMode() { repeatCycles++ }
}
