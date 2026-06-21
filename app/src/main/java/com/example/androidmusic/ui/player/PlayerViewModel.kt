package com.example.androidmusic.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidmusic.domain.model.PlaybackState
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
        combine(controller.connection, controller.state, controller.queue) { connection, state, queue ->
            val index = state.indexOrNull()
            val track = index?.let { queue.items.getOrNull(it) }
            PlayerUiState(
                isConnected = connection == ConnectionState.Connected,
                currentTrack = track?.let {
                    NowPlayingTrack(it.id, it.title, it.artist, it.albumArtUri?.value)
                },
                isPlaying = state is PlaybackState.Playing,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlayerUiState())

    fun onPlayPause() {
        if (uiState.value.isPlaying) controller.pause() else controller.play()
    }

    fun onNext() = controller.skipToNext()

    fun onPrevious() = controller.skipToPrevious()

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
