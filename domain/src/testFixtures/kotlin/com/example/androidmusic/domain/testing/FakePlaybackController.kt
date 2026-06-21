package com.example.androidmusic.domain.testing

import com.example.androidmusic.domain.model.PlayQueue
import com.example.androidmusic.domain.model.PlaybackProgress
import com.example.androidmusic.domain.model.PlaybackState
import com.example.androidmusic.domain.model.QueueSource
import com.example.androidmusic.domain.model.RepeatMode
import com.example.androidmusic.domain.player.ConnectionState
import com.example.androidmusic.domain.player.PlaybackController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class FakePlaybackController : PlaybackController {
    var lastSource: QueueSource? = null
    var playCalls = 0
    var pauseCalls = 0
    var skipNextCalls = 0
    var skipPreviousCalls = 0
    var shuffleToggles = 0
    var repeatCycles = 0
    var lastSeekMs: Long? = null
    var jumpedToIndex: Int? = null
    var removedIndex: Int? = null
    var clearCalls = 0

    private val _connection = MutableStateFlow(ConnectionState.Connected)
    override val connection: StateFlow<ConnectionState> = _connection

    private val _state = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    override val state: StateFlow<PlaybackState> = _state

    private val _queue = MutableStateFlow(PlayQueue.EMPTY)
    override val queue: StateFlow<PlayQueue> = _queue

    private val _progress = MutableStateFlow(PlaybackProgress())
    override val progress: StateFlow<PlaybackProgress> = _progress

    private val _repeatMode = MutableStateFlow(RepeatMode.Off)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode

    fun setConnection(value: ConnectionState) { _connection.value = value }
    fun setState(value: PlaybackState) { _state.value = value }
    fun setQueue(value: PlayQueue) { _queue.value = value }
    fun setProgress(value: PlaybackProgress) { _progress.value = value }

    override fun playSource(source: QueueSource) {
        lastSource = source
        _state.value = PlaybackState.Playing(0, 0)
    }

    override fun play() { playCalls++ }
    override fun pause() { pauseCalls++ }
    override fun skipToNext() { skipNextCalls++ }
    override fun skipToPrevious() { skipPreviousCalls++ }
    override fun seekTo(positionMs: Long) { lastSeekMs = positionMs }

    override fun toggleShuffle() {
        shuffleToggles++
        _queue.value = if (_queue.value.shuffled) _queue.value.unshuffle() else _queue.value.shuffle(Random(0))
    }

    override fun cycleRepeatMode() {
        repeatCycles++
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.Off -> RepeatMode.RepeatQueue
            RepeatMode.RepeatQueue -> RepeatMode.RepeatOne
            RepeatMode.RepeatOne -> RepeatMode.Off
        }
    }

    override fun skipToQueueItem(index: Int) {
        jumpedToIndex = index
        _queue.value = _queue.value.withCurrentIndex(index)
        _state.value = PlaybackState.Playing(index, 0)
    }

    override fun moveQueueItem(from: Int, to: Int) {
        _queue.value = _queue.value.moveItem(from, to)
    }

    override fun removeQueueItem(index: Int) {
        removedIndex = index
        _queue.value = _queue.value.removeAt(index)
    }

    override fun clearQueue() {
        clearCalls++
        _queue.value = _queue.value.clearExceptCurrent()
    }
}
