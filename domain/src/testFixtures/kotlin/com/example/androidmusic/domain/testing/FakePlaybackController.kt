package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.model.PlayQueue
import com.example.androidmusic.domain.model.PlaybackState
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.player.ConnectionState
import com.example.androidmusic.domain.player.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePlaybackController : PlaybackController {
    var lastSource: QueueSource? = null
    var playCalls = 0
    var pauseCalls = 0
    var skipNextCalls = 0
    var skipPreviousCalls = 0
    var shuffleToggles = 0
    var repeatCycles = 0

    private val _connection = MutableStateFlow(ConnectionState.Connected)
    override val connection: StateFlow<ConnectionState> = _connection

    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val state: StateFlow<PlaybackState> = _state

    private val _queue = MutableStateFlow(PlayQueue.EMPTY)
    override val queue: StateFlow<PlayQueue> = _queue

    fun setConnection(value: ConnectionState) { _connection.value = value }
    fun setState(value: PlaybackState) { _state.value = value }
    fun setQueue(value: PlayQueue) { _queue.value = value }

    override fun playSource(source: QueueSource) {
        lastSource = source
        _state.value = PlaybackState.Playing(0, 0)
    }

    override fun play() { playCalls++ }
    override fun pause() { pauseCalls++ }
    override fun skipToNext() { skipNextCalls++ }
    override fun skipToPrevious() { skipPreviousCalls++ }
    override fun seekTo(positionMs: Long) = Unit
    override fun toggleShuffle() { shuffleToggles++ }
    override fun cycleRepeatMode() { repeatCycles++ }
}
