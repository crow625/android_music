package com.example.androidmusic.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NowPlayingRoute(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showQueue by rememberSaveable { mutableStateOf(false) }

    NowPlayingScreen(
        state = state,
        showQueue = showQueue,
        onEvent = { event ->
            when (event) {
                NowPlayingEvent.PlayPause -> viewModel.onPlayPause()
                NowPlayingEvent.Next -> viewModel.onNext()
                NowPlayingEvent.Previous -> viewModel.onPrevious()
                is NowPlayingEvent.Seek -> viewModel.onSeek(event.positionMs)
                NowPlayingEvent.ToggleShuffle -> viewModel.onToggleShuffle()
                NowPlayingEvent.CycleRepeat -> viewModel.onCycleRepeat()
                NowPlayingEvent.ToggleQueue -> showQueue = !showQueue
                is NowPlayingEvent.JumpTo -> {
                    viewModel.onJumpTo(event.index)
                    showQueue = false
                }
                is NowPlayingEvent.Remove -> viewModel.onRemoveFromQueue(event.index)
                NowPlayingEvent.ClearQueue -> viewModel.onClearQueue()
            }
        },
        modifier = modifier,
    )
}
