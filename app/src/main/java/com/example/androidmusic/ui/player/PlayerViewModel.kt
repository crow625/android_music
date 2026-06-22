package com.example.androidmusic.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.PlayQueue
import com.example.androidmusic.domain.model.PlaybackProgress
import com.example.androidmusic.domain.model.PlaybackState
import com.example.androidmusic.domain.model.RepeatMode
import com.example.androidmusic.domain.player.ConnectionState
import com.example.androidmusic.domain.player.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Observes the shared (singleton) [PlaybackController]. Multiple instances (e.g.
 * mini-player and Now Playing) stay consistent because the state lives in the
 * controller, not the ViewModel.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val controller: PlaybackController,
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> =
        combine(
            controller.connection,
            controller.state,
            controller.queue,
            controller.progress,
            controller.repeatMode,
        ) { connection, state, queue, progress, repeatMode ->
            toUiState(connection, state, queue, progress, repeatMode)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlayerUiState())

    fun onPlayPause() {
        if (uiState.value.isPlaying) controller.pause() else controller.play()
    }

    fun onNext() = controller.skipToNext()
    fun onPrevious() = controller.skipToPrevious()
    fun onSeek(positionMs: Long) = controller.seekTo(positionMs)
    fun onToggleShuffle() = controller.toggleShuffle()
    fun onCycleRepeat() = controller.cycleRepeatMode()
    fun onJumpTo(index: Int) = controller.skipToQueueItem(index)
    fun onRemoveFromQueue(index: Int) = controller.removeQueueItem(index)
    fun onClearQueue() = controller.clearQueue()

    private fun toUiState(
        connection: ConnectionState,
        state: PlaybackState,
        queue: PlayQueue,
        progress: PlaybackProgress,
        repeatMode: RepeatMode,
    ): PlayerUiState {
        val index = state.indexOrNull()
        val current = index?.let { queue.items.getOrNull(it) }
        return PlayerUiState(
            isConnected = connection == ConnectionState.Connected,
            currentTrack = current?.let {
                NowPlayingTrack(it.id, it.title, it.artist, it.album, artworkUri = it.uri.value)
            },
            isPlaying = state is PlaybackState.Playing,
            positionMs = progress.positionMs,
            durationMs = progress.durationMs,
            isShuffleOn = queue.shuffled,
            repeatMode = repeatMode,
            queue = queue.items.mapIndexed { i, file ->
                QueueItemUi(i, file.id, file.title, file.artist, isCurrent = i == index)
            },
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

private fun PlaybackState.indexOrNull(): Int? = when (this) {
    is PlaybackState.Playing -> index
    is PlaybackState.Paused -> index
    is PlaybackState.Error -> index
    PlaybackState.Idle -> null
}
